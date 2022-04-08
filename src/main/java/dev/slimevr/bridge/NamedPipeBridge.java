package dev.slimevr.bridge;

import java.io.IOException;
import java.util.List;

import com.google.protobuf.CodedOutputStream;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.ptr.IntByReference;

import dev.slimevr.Main;
import dev.slimevr.bridge.Pipe.PipeState;
import dev.slimevr.bridge.ProtobufMessages.ProtobufMessage;
import dev.slimevr.bridge.ProtobufMessages.TrackerAdded;
import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.vr.trackers.HMDTracker;
import dev.slimevr.vr.trackers.ShareableTracker;
import dev.slimevr.vr.trackers.TrackerPosition;
import dev.slimevr.vr.trackers.TrackerRole;
import dev.slimevr.vr.trackers.VRTracker;
import io.eiren.util.logging.LogManager;

public class NamedPipeBridge extends ProtobufBridge<VRTracker> implements Runnable {
	
	private final TrackerRole[] defaultRoles = new TrackerRole[] {TrackerRole.WAIST, TrackerRole.LEFT_FOOT, TrackerRole.RIGHT_FOOT};

	private final byte[] buffArray = new byte[2048];
	
	protected Pipe pipe;
	protected final String pipeName;
	protected final String bridgeSettingsKey;
	protected final Thread runnerThread;
	private final List<? extends ShareableTracker> shareableTrackers;
	
	public NamedPipeBridge(HMDTracker hmd, String bridgeSettingsKey, String bridgeName, String pipeName, List<? extends ShareableTracker> shareableTrackers) {
		super(bridgeName, hmd);
		this.pipeName = pipeName;
		this.bridgeSettingsKey = bridgeSettingsKey;
		this.runnerThread = new Thread(this, "Named pipe thread");
		this.shareableTrackers = shareableTrackers;
	}

	@Override
	@VRServerThread
	public void startBridge() {
		for(TrackerRole role : defaultRoles) {
			changeShareSettings(role, Main.vrServer.config.getBoolean("bridge." + bridgeSettingsKey + ".trackers." + role.name().toLowerCase(), true));
		}
		for(int i = 0; i < shareableTrackers.size(); ++i) {
			ShareableTracker tr = shareableTrackers.get(i);
			TrackerRole role = tr.getTrackerRole();
			changeShareSettings(role, Main.vrServer.config.getBoolean("bridge." + bridgeSettingsKey + ".trackers." + role.name().toLowerCase(), false));
		}
		runnerThread.start();
	}

	@VRServerThread
	public boolean getShareSetting(TrackerRole role) {
		for(int i = 0; i < shareableTrackers.size(); ++i) {
			ShareableTracker tr = shareableTrackers.get(i);
			if(tr.getTrackerRole() == role) {
				return sharedTrackers.contains(tr);
			}
		}
		return false;
	}

	@VRServerThread
	public void changeShareSettings(TrackerRole role, boolean share) {
		if(role == null)
			return;
		for(int i = 0; i < shareableTrackers.size(); ++i) {
			ShareableTracker tr = shareableTrackers.get(i);
			if(tr.getTrackerRole() == role) {
				if(share) {
					addSharedTracker(tr);
				} else {
					removeSharedTracker(tr);
				}
				Main.vrServer.config.setProperty("bridge." + bridgeSettingsKey + ".trackers." + role.name().toLowerCase(), share);
				Main.vrServer.saveConfig();
			}
		}
	}

	@Override
	@VRServerThread
	protected VRTracker createNewTracker(TrackerAdded trackerAdded) {
		VRTracker tracker = new VRTracker(trackerAdded.getTrackerId(), trackerAdded.getTrackerSerial(), trackerAdded.getTrackerName(), true, true);
		TrackerRole role = TrackerRole.getById(trackerAdded.getTrackerRole());
		if(role != null) {
			tracker.setBodyPosition(TrackerPosition.getByRole(role));
		}
		return tracker;
	}

	@Override
	@BridgeThread
	public void run() {
		try {
			createPipe();
			while(true) {
				boolean pipesUpdated = false;
				if(pipe.state == PipeState.CREATED) {
					tryOpeningPipe(pipe);
				}
				if(pipe.state == PipeState.OPEN) {
					pipesUpdated = updatePipe();
					updateMessageQueue();
				}
				if(pipe.state == PipeState.ERROR) {
					resetPipe();
				}
				if(!pipesUpdated) {
					try {
						Thread.sleep(5); // Up to 200Hz
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	@BridgeThread
	protected boolean sendMessageReal(ProtobufMessage message) {
		if(pipe.state == PipeState.OPEN) {
			try {
		        int size = message.getSerializedSize();
		        CodedOutputStream os = CodedOutputStream.newInstance(buffArray, 4, size);
		        message.writeTo(os);
		        size += 4;
		        buffArray[0] = (byte) (size & 0xFF);
		        buffArray[1] = (byte) ((size >> 8) & 0xFF);
		        buffArray[2] = (byte) ((size >> 16) & 0xFF);
		        buffArray[3] = (byte) ((size >> 24) & 0xFF);
		        if(Kernel32.INSTANCE.WriteFile(pipe.pipeHandle, buffArray, size, null, null)) {
		            return true;
		        }
		        pipe.state = PipeState.ERROR;
		        LogManager.log.severe("[" + bridgeName + "] Pipe error: " + Kernel32.INSTANCE.GetLastError());
			} catch(IOException e) {
				e.printStackTrace();
			}
	    }
		return false;
	}
	
	private boolean updatePipe() throws IOException {
		if(pipe.state == PipeState.OPEN) {
			boolean readAnything = false;
			IntByReference bytesAvailable = new IntByReference(0);
			while(Kernel32.INSTANCE.PeekNamedPipe(pipe.pipeHandle, buffArray, 4, null, bytesAvailable, null)) {
				if(bytesAvailable.getValue() >= 4) { // Got size
					int messageLength = (buffArray[3] << 24) | (buffArray[2] << 16) | (buffArray[1] << 8) | buffArray[0];
					if(messageLength > 1024) { // Overflow
						LogManager.log.severe("[" + bridgeName + "] Pipe overflow. Message length: " + messageLength);
						pipe.state = PipeState.ERROR;
						return readAnything;
					}
					if(bytesAvailable.getValue() >= messageLength) {
						if(Kernel32.INSTANCE.ReadFile(pipe.pipeHandle, buffArray, messageLength, bytesAvailable, null)) {
							ProtobufMessage message = ProtobufMessage.parser().parseFrom(buffArray, 4, messageLength - 4);
							messageRecieved(message);
							readAnything = true;
						} else {
							pipe.state = PipeState.ERROR;
							LogManager.log.severe("[" + bridgeName + "] Pipe error: " + Kernel32.INSTANCE.GetLastError());
							return readAnything;
						}
					} else {
						return readAnything; // Wait for more data
					}
				} else {
					return readAnything; // Wait for more data
				}
			}
			pipe.state = PipeState.ERROR;
			LogManager.log.severe("[" + bridgeName + "] Pipe error: " + Kernel32.INSTANCE.GetLastError());
		}
		return false;
	}
	
	private void resetPipe() {
		Pipe.safeDisconnect(pipe);
		pipe.state = PipeState.CREATED;
		Main.vrServer.queueTask(this::disconnected);
	}
	
	private void createPipe() throws IOException {
		try {
			pipe = new Pipe(Kernel32.INSTANCE.CreateNamedPipe(pipeName, WinBase.PIPE_ACCESS_DUPLEX, // dwOpenMode
					WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_WAIT, // dwPipeMode
					1, // nMaxInstances,
					1024 * 16, // nOutBufferSize,
					1024 * 16, // nInBufferSize,
					0, // nDefaultTimeOut,
					null), pipeName); // lpSecurityAttributes
			LogManager.log.info("[" + bridgeName + "] Pipe " + pipe.name + " created");
			if(WinBase.INVALID_HANDLE_VALUE.equals(pipe.pipeHandle))
				throw new IOException("Can't open " + pipeName + " pipe: " + Kernel32.INSTANCE.GetLastError());
			LogManager.log.info("[" + bridgeName + "] Pipes are created");
		} catch(IOException e) {
			Pipe.safeDisconnect(pipe);
			throw e;
		}
	}
	
	private boolean tryOpeningPipe(Pipe pipe) {
		if(Kernel32.INSTANCE.ConnectNamedPipe(pipe.pipeHandle, null) || Kernel32.INSTANCE.GetLastError() == WinError.ERROR_PIPE_CONNECTED) {
			pipe.state = PipeState.OPEN;
			LogManager.log.info("[" + bridgeName + "] Pipe " + pipe.name + " is open");
			Main.vrServer.queueTask(this::reconnected);
			return true;
		}
		LogManager.log.info("[" + bridgeName + "] Error connecting to pipe " + pipe.name + ": " + Kernel32.INSTANCE.GetLastError());
		return false;
	}
}
