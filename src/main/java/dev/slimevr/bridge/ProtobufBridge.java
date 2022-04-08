package dev.slimevr.bridge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import dev.slimevr.Main;
import dev.slimevr.bridge.ProtobufMessages.Position;
import dev.slimevr.bridge.ProtobufMessages.ProtobufMessage;
import dev.slimevr.bridge.ProtobufMessages.TrackerAdded;
import dev.slimevr.bridge.ProtobufMessages.TrackerStatus;
import dev.slimevr.bridge.ProtobufMessages.UserAction;
import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.vr.trackers.ComputedTracker;
import dev.slimevr.vr.trackers.HMDTracker;
import dev.slimevr.vr.trackers.ShareableTracker;
import dev.slimevr.vr.trackers.TrackerRole;
import dev.slimevr.vr.trackers.VRTracker;
import io.eiren.util.ann.Synchronize;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.collections.FastList;

public abstract class ProtobufBridge<T extends VRTracker> implements Bridge {

	private final Vector3f vec1 = new Vector3f();
	private final Quaternion quat1 = new Quaternion();
	
	@ThreadSafe
	private final Queue<ProtobufMessage> inputQueue = new LinkedBlockingQueue<>();
	@ThreadSafe
	private final Queue<ProtobufMessage> outputQueue = new LinkedBlockingQueue<>();
	@VRServerThread
	protected final List<ShareableTracker> sharedTrackers = new FastList<>();
	@Synchronize("self")
	private final Map<String, T> remoteTrackersBySerial = new HashMap<>();
	@Synchronize("self")
	private final Map<Integer, T> remoteTrackersByTrackerId = new HashMap<>();
	
	private boolean hadNewData = false;
	
	private T hmdTracker;
	private final HMDTracker hmd;
	protected final String bridgeName;
	
	public ProtobufBridge(String bridgeName, HMDTracker hmd) {
		this.bridgeName = bridgeName;
		this.hmd = hmd;
	}

	@BridgeThread
	protected abstract boolean sendMessageReal(ProtobufMessage message);

	@BridgeThread
	protected void messageRecieved(ProtobufMessage message) {
		inputQueue.add(message);
	}
	
	@ThreadSafe
	protected void sendMessage(ProtobufMessage message) {
		outputQueue.add(message);
	}

	@BridgeThread
	protected void updateMessageQueue() {
		ProtobufMessage message = null;
		while((message = outputQueue.poll()) != null) {
			if(!sendMessageReal(message))
				return;
		}
	}
	
	@VRServerThread
	@Override
	public void dataRead() {
		hadNewData = false;
		ProtobufMessage message = null;
		while((message = inputQueue.poll()) != null) {
			processMessageRecieved(message);
			hadNewData = true;
		}
		if(hadNewData && hmdTracker != null) {
			trackerOverrideUpdate(hmdTracker, hmd);
		}
	}

	@VRServerThread
	protected void trackerOverrideUpdate(T source, ComputedTracker target) {
		target.position.set(source.position);
		target.rotation.set(source.rotation);
		target.setStatus(source.getStatus());
		target.dataTick();
	}

	@VRServerThread
	@Override
	public void dataWrite() {
		if(!hadNewData) // Don't write anything if no message were recieved, we always process at the speed of the other side
			return;
		for(int i = 0; i < sharedTrackers.size(); ++i) {
			writeTrackerUpdate(sharedTrackers.get(i));
		}
	}

	@VRServerThread
	protected void writeTrackerUpdate(ShareableTracker localTracker) {
		Position.Builder builder = Position.newBuilder().setTrackerId(localTracker.getTrackerId());
		if(localTracker.getPosition(vec1)) {
			builder.setX(vec1.x);
			builder.setY(vec1.y);
			builder.setZ(vec1.z);
		}
		if(localTracker.getRotation(quat1)) {
			builder.setQx(quat1.getX());
			builder.setQy(quat1.getY());
			builder.setQz(quat1.getZ());
			builder.setQw(quat1.getW());
		}
		sendMessage(ProtobufMessage.newBuilder().setPosition(builder).build());
	}
	
	@VRServerThread
	protected void processMessageRecieved(ProtobufMessage message) {
		//if(!message.hasPosition())
		//	LogManager.log.info("[" + bridgeName + "] MSG: " + message);
		if(message.hasPosition()) {
			positionRecieved(message.getPosition());
		} else if(message.hasUserAction()) {
			userActionRecieved(message.getUserAction());
		} else if(message.hasTrackerStatus()) {
			trackerStatusRecieved(message.getTrackerStatus());
		} else if(message.hasTrackerAdded()) {
			trackerAddedRecieved(message.getTrackerAdded());
		}
	}
	
	@VRServerThread
	protected void positionRecieved(Position positionMessage) {
		T tracker = getInternalRemoteTrackerById(positionMessage.getTrackerId());
		if(tracker != null) {
			if(positionMessage.hasX())
				tracker.position.set(positionMessage.getX(), positionMessage.getY(), positionMessage.getZ());
			tracker.rotation.set(positionMessage.getQx(), positionMessage.getQy(), positionMessage.getQz(), positionMessage.getQw());
			tracker.dataTick();
		}
	}
	
	@VRServerThread
	protected abstract T createNewTracker(TrackerAdded trackerAdded);

	@VRServerThread
	protected void trackerAddedRecieved(TrackerAdded trackerAdded) {
		T tracker = getInternalRemoteTrackerById(trackerAdded.getTrackerId());
		if(tracker != null) {
			// TODO reinit?
			return;
		}
		tracker = createNewTracker(trackerAdded);
		synchronized(remoteTrackersBySerial) {
			remoteTrackersBySerial.put(tracker.getName(), tracker);
		}
		synchronized(remoteTrackersByTrackerId) {
			remoteTrackersByTrackerId.put(tracker.getTrackerId(), tracker);
		}
		if(trackerAdded.getTrackerRole() == TrackerRole.HMD.id) {
			hmdTracker = tracker;
		} else {
			Main.vrServer.registerTracker(tracker);
		}
	}

	@VRServerThread
	protected void userActionRecieved(UserAction userAction) {
		switch(userAction.getName()) {
		case "calibrate":
			// TODO : Check pose field
			Main.vrServer.resetTrackers();
			break;
		}
	}

	@VRServerThread
	protected void trackerStatusRecieved(TrackerStatus trackerStatus) {
		T tracker = getInternalRemoteTrackerById(trackerStatus.getTrackerId());
		if(tracker != null) {
			tracker.setStatus(dev.slimevr.vr.trackers.TrackerStatus.getById(trackerStatus.getStatusValue()));
		}
	}
    
	@ThreadSafe
	protected T getInternalRemoteTrackerById(int trackerId) {
		synchronized(remoteTrackersByTrackerId) {
			return remoteTrackersByTrackerId.get(trackerId);
		}
	}

	@VRServerThread
	protected void reconnected() {
		for(int i = 0; i < sharedTrackers.size(); ++i) {
			ShareableTracker tracker = sharedTrackers.get(i);
			TrackerAdded.Builder builder = TrackerAdded.newBuilder().setTrackerId(tracker.getTrackerId()).setTrackerName(tracker.getDescriptiveName()).setTrackerSerial(tracker.getName()).setTrackerRole(tracker.getTrackerRole().id);
			sendMessage(ProtobufMessage.newBuilder().setTrackerAdded(builder).build());
		}
	}

	@VRServerThread
	protected void disconnected() {
		synchronized(remoteTrackersByTrackerId) {
			Iterator<Entry<Integer, T>> iterator = remoteTrackersByTrackerId.entrySet().iterator();
			while(iterator.hasNext()) {
				iterator.next().getValue().setStatus(dev.slimevr.vr.trackers.TrackerStatus.DISCONNECTED);
			}
		}
		if(hmdTracker != null) {
			hmd.setStatus(dev.slimevr.vr.trackers.TrackerStatus.DISCONNECTED);
		}
	}

	@VRServerThread
	@Override
	public void addSharedTracker(ShareableTracker tracker) {
		if(sharedTrackers.contains(tracker))
			return;
		sharedTrackers.add(tracker);
		TrackerAdded.Builder builder = TrackerAdded.newBuilder().setTrackerId(tracker.getTrackerId()).setTrackerName(tracker.getDescriptiveName()).setTrackerSerial(tracker.getName()).setTrackerRole(tracker.getTrackerRole().id);
		sendMessage(ProtobufMessage.newBuilder().setTrackerAdded(builder).build());
	}

	@VRServerThread
	@Override
	public void removeSharedTracker(ShareableTracker tracker) {
		sharedTrackers.remove(tracker);
		// No message can be sent to the remote side, protocol doesn't support tracker removal (yet)
	}
}
