package dev.slimevr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import dev.slimevr.bridge.Bridge;
import dev.slimevr.bridge.NamedPipeBridge;
import dev.slimevr.bridge.SteamVRPipeInputBridge;
import dev.slimevr.bridge.VMCBridge;
import dev.slimevr.bridge.WebSocketVRBridge;
import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.vr.processor.HumanPoseProcessor;
import dev.slimevr.vr.processor.skeleton.HumanSkeleton;
import dev.slimevr.vr.trackers.HMDTracker;
import dev.slimevr.vr.trackers.ShareableTracker;
import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.TrackerConfig;
import dev.slimevr.vr.trackers.TrackersUDPServer;
import io.eiren.util.OperatingSystem;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.ann.ThreadSecure;
import io.eiren.util.collections.FastList;
import io.eiren.yaml.YamlException;
import io.eiren.yaml.YamlFile;
import io.eiren.yaml.YamlNode;

public class VRServer extends Thread {
	
	private final List<Tracker> trackers = new FastList<>();
	public final HumanPoseProcessor humanPoseProcessor;
	private final TrackersUDPServer trackersServer;
	private final List<Bridge> bridges = new FastList<>();
	private final Queue<Runnable> tasks = new LinkedBlockingQueue<>();
	private final Map<String, TrackerConfig> configuration = new HashMap<>();
	public final YamlFile config = new YamlFile();
	public final HMDTracker hmdTracker;
	private final List<Consumer<Tracker>> newTrackersConsumers = new FastList<>();
	private final List<Runnable> onTick = new FastList<>();
	private final List<? extends ShareableTracker> shareTrackers;

	/**
	 * Конструктор потока сервера.
	 */
	public VRServer() {
		super("VRServer");
		// Загрузка конфига.
		loadConfig();
		// Инициализация трекеров.
		hmdTracker = new HMDTracker("HMD");
		hmdTracker.position.set(0, 1.8f, 0); // Set starting position for easier debugging
		// TODO Multiple processors
		humanPoseProcessor = new HumanPoseProcessor(this, hmdTracker);
		shareTrackers = humanPoseProcessor.getComputedTrackers();
		
		// Start server for SlimeVR trackers
		trackersServer = new TrackersUDPServer(6969, "Sensors UDP server", this::registerTracker);
		
		// OpenVR bridge currently only supports Windows
		if(OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {
			/*
			// Create named pipe bridge for SteamVR driver
			NamedPipeVRBridge driverBridge = new NamedPipeVRBridge(hmdTracker, shareTrackers, this);
			tasks.add(() -> driverBridge.startBridge());
			bridges.add(driverBridge);
			//*/
			// Create named pipe bridge for SteamVR input
			SteamVRPipeInputBridge steamVRInput = new SteamVRPipeInputBridge(this);
			tasks.add(() -> steamVRInput.startBridge());
			bridges.add(steamVRInput);
			//*/
			NamedPipeBridge driverBridge = new NamedPipeBridge(hmdTracker, "steamvr", "SteamVR Driver Bridge", "\\\\.\\pipe\\SlimeVRDriver", shareTrackers);
			tasks.add(() -> driverBridge.startBridge());
			bridges.add(driverBridge);
		}
		
		// Create WebSocket server
		WebSocketVRBridge wsBridge = new WebSocketVRBridge(hmdTracker, shareTrackers, this);
		tasks.add(() -> wsBridge.startBridge());
		bridges.add(wsBridge);
		
		// Create VMCBridge
		try {
			VMCBridge vmcBridge = new VMCBridge(39539, 39540, InetAddress.getLocalHost());
			tasks.add(() -> vmcBridge.startBridge());
			bridges.add(vmcBridge);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Регистрация трекеров.
		registerTracker(hmdTracker);
		for(int i = 0; i < shareTrackers.size(); ++i)
			registerTracker(shareTrackers.get(i));
	}

	/**
	 * Метод, проверяющий наличие моста между инстансов класса наследника Bridge и массивом bridges
	 * @param bridgeClass - инстанс наследника класса brigde
	 * @return - true, если инстанс является дочерним от классов в массиве bridges.
	 */
	public boolean hasBridge(Class<? extends Bridge> bridgeClass) {
		for(int i = 0; i < bridges.size(); ++i) {
			if(bridgeClass.isAssignableFrom(bridges.get(i).getClass()))
				return true;
		}
		return false;
	}

	/**
	 * Метод, возвращающий инстанс класса наследника Bridge, если он представлен в массиве bridges
	 * @param bridgeClass - нужный нам класс
	 * @param <E> - класс
	 * @return - закастованный к переданному классу инстанс из bridges, являющийся наследником переданного класса.
	 */
	@ThreadSafe
	public <E extends Bridge> E getVRBridge(Class<E> bridgeClass) {
		for(int i = 0; i < bridges.size(); ++i) {
			Bridge b = bridges.get(i);
			if(bridgeClass.isAssignableFrom(b.getClass()))
				return bridgeClass.cast(b);
		}
		return null;
	}

	/**
	 * Метод получения конфига трекера.
	 * @param tracker - нужный нам трекер
	 * @return - конфиг переданного трекера.
	 */
	@ThreadSafe
	public TrackerConfig getTrackerConfig(Tracker tracker) {
		synchronized(configuration) {
			TrackerConfig config = configuration.get(tracker.getName());
			if(config == null) {
				config = new TrackerConfig(tracker);
				configuration.put(tracker.getName(), config);
			}
			return config;
		}
	}

	/**
	 * Метод загрузки конфига.
	 */
	private void loadConfig() {
		try {
			config.load(new FileInputStream(new File("vrconfig.yml")));
		} catch(FileNotFoundException e) {
			// Config file didn't exist, is not an error
		} catch(YamlException e) {
			e.printStackTrace();
		}
		List<YamlNode> trackersConfig = config.getNodeList("trackers", null);
		for(int i = 0; i < trackersConfig.size(); ++i) {
			TrackerConfig cfg = new TrackerConfig(trackersConfig.get(i));
			synchronized(configuration) {
				configuration.put(cfg.trackerName, cfg);
			}
		}
	}

	/**
	 * Метод, добавляющий новый поток в лист onTick.
	 * @param runnable
	 */
	public void addOnTick(Runnable runnable) {
		this.onTick.add(runnable);
	}

	/**
	 * Метод добавления нового Consumer для трекеров.
	 * (полагаю, что эта хрень добавляет нового слушателя событий для трекеров)
	 * @param consumer - новый Consumer.
	 */
	@ThreadSafe
	public void addNewTrackerConsumer(Consumer<Tracker> consumer) {
		queueTask(() -> {
			newTrackersConsumers.add(consumer);
			for(int i = 0; i < trackers.size(); ++i)
				consumer.accept(trackers.get(i));
		});
	}

	/**
	 * Метод, апдейтищий позу пользователя и конфиги.
	 * @param tracker - Определенный трекер.
	 */
	@ThreadSafe
	public void trackerUpdated(Tracker tracker) {
		queueTask(() -> {
			humanPoseProcessor.trackerUpdated(tracker);
			TrackerConfig tc = getTrackerConfig(tracker);
			tracker.saveConfig(tc);
			saveConfig();
		});
	}

	/**
	 * Метод, передающий потребителю информацию по обновлению скелета.
	 * @param consumer - потребитель.
	 */
	@ThreadSafe
	public void addSkeletonUpdatedCallback(Consumer<HumanSkeleton> consumer) {
		queueTask(() -> {
			humanPoseProcessor.addSkeletonUpdatedCallback(consumer);
		});
	}

	/**
	 * Метод сохранения конфига.
	 */
	@ThreadSafe
	public synchronized void saveConfig() {
		List<YamlNode> nodes = config.getNodeList("trackers", null);
		List<Map<String, Object>> trackersConfig = new FastList<>(nodes.size());
		for(int i = 0; i < nodes.size(); ++i) {
			trackersConfig.add(nodes.get(i).root);
		}
		config.setProperty("trackers", trackersConfig);
		synchronized(configuration) {
			Iterator<TrackerConfig> iterator = configuration.values().iterator();
			while(iterator.hasNext()) {
				TrackerConfig tc = iterator.next();
				Map<String, Object> cfg = null;
				for(int i = 0; i < trackersConfig.size(); ++i) {
					Map<String, Object> c = trackersConfig.get(i);
					if(tc.trackerName.equals(c.get("name"))) {
						cfg = c;
						break;
					}
				}
				if(cfg == null) {
					cfg = new HashMap<>();
					trackersConfig.add(cfg);
				}
				tc.saveConfig(new YamlNode(cfg));
			}
		}
		File cfgFile = new File("vrconfig.yml");
		try {
			config.save(new FileOutputStream(cfgFile));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Поточный метод.
	 */
	@Override
	@VRServerThread
	public void run() {
		// Старт сервера.
		trackersServer.start();
		// Крутим вайл, чтобы поток не загнулся.
		while(true) {
			//final long start = System.currentTimeMillis();
			// Крутим do-while, пока добавляются новые подпотоки.
			do {
				Runnable task = tasks.poll();
				if(task == null)
					break;
				task.run();
			} while(true);
			//
			for(int i = 0; i < onTick.size(); ++i) {
				this.onTick.get(i).run();
			}
			//
			for(int i = 0; i < bridges.size(); ++i)
				bridges.get(i).dataRead();
			//
			for(int i = 0; i < trackers.size(); ++i)
				trackers.get(i).tick();
			// Обновление положения тела.
			humanPoseProcessor.update();
			//
			for(int i = 0; i < bridges.size(); ++i)
				bridges.get(i).dataWrite();
			//final long time = System.currentTimeMillis() - start;
			// Заставляем поток заснуть на 1 мс.
			try {
				Thread.sleep(1); // 1000Hz
			} catch(InterruptedException e) {
			}
		}
	}

	/**
	 * Метод, добавляющий поток в очередь выполнения.
	 * @param r - новый поток.
	 */
	@ThreadSafe
	public void queueTask(Runnable r) {
		tasks.add(r);
	}

	/**
	 * Метод добавления нового трекера к обработчику позиций тела.
	 * @param tracker - трекер.
	 */
	@VRServerThread
	private void trackerAdded(Tracker tracker) {
		humanPoseProcessor.trackerAdded(tracker);
	}

	/**
	 * Метод, регистрирующий новый трекер в системе.
	 * @param tracker - трекер.
	 */
	@ThreadSecure
	public void registerTracker(Tracker tracker) {
		TrackerConfig config = getTrackerConfig(tracker);
		tracker.loadConfig(config);
		queueTask(() -> {
			trackers.add(tracker);
			trackerAdded(tracker);
			for(int i = 0; i < newTrackersConsumers.size(); ++i)
				newTrackersConsumers.get(i).accept(tracker);
		});
	}

	/**
	 * Метод ресета трекера.
	 */
	public void resetTrackers() {
		queueTask(() -> {
			humanPoseProcessor.resetTrackers();
		});
	}

	/**
	 *
	 */
	public void resetTrackersYaw() {
		queueTask(() -> {
			humanPoseProcessor.resetTrackersYaw();
		});
	}

	/**
	 * Метод, возвращающий количество подключенных трекеров.
	 * @return - число трекеров.
	 */
	public int getTrackersCount() {
		return trackers.size();
	}

	/**
	 * Метод получения подключенных трекеров.
	 * @return - массив трекеров.
	 */
	public List<Tracker> getAllTrackers() {
		return new FastList<>(trackers);
	}
}
