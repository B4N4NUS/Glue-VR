package dev.slimevr.gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.*;
import dev.slimevr.sound.SoundPlayer;
import dev.slimevr.vr.processor.ComputedHumanPoseTracker;
import dev.slimevr.vr.trackers.*;
import io.eiren.util.StringUtils;
import io.eiren.util.ann.AWTThread;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.collections.FastList;

import static dev.slimevr.gui.VRServerGUI.prefX;
import static dev.slimevr.gui.VRServerGUI.prefY;

public class TrackersList extends EJBoxNoStretch {

	private static final long UPDATE_DELAY = 50;

	Quaternion quaternion = new Quaternion();
	Vector3f vector3f = new Vector3f();
	float[] angles = new float[3];

	private List<TrackerPanel> trackers = new FastList<>();

	private final VRServer server;
	private final VRServerGUI gui;
	private long lastUpdate = 0;

	public static boolean playSounds = true;
	public static boolean showPing = false;
	public static boolean showRSSI = false;
	public static boolean showTPS = false;

	private HashMap<Tracker, Long> lastConnection = new HashMap<>();
	private HashMap<Tracker, Boolean> connected = new HashMap<>();
	private HashMap<Tracker, Boolean> firstInit = new HashMap<>();
	private HashMap<Tracker, Boolean> wasPlayed = new HashMap<>();

	private JLabel status, name, battery, rotation, position, ping, designation, mount, type, rssi, tps;

	public void scale() {
		status.setPreferredSize(new Dimension(prefX, prefY));
		type.setPreferredSize(new Dimension(prefX, prefY));
		name.setPreferredSize(new Dimension(3 * prefX, prefY));
		battery.setPreferredSize(new Dimension(prefX, prefY));
		rotation.setPreferredSize(new Dimension(2 * prefX, prefY));
		position.setPreferredSize(new Dimension(2 * prefX, prefY));
		ping.setPreferredSize(new Dimension(prefX, prefY));
		tps.setPreferredSize(new Dimension(prefX, prefY));
		rssi.setPreferredSize(new Dimension(2 * prefX, prefY));
		designation.setPreferredSize(new Dimension(2 * prefX, prefY));
		mount.setPreferredSize(new Dimension(2 * prefX, prefY));

		for (int i = 0; i < trackers.size(); i++) {
			trackers.get(i).statBut.setPreferredSize(new Dimension(prefX, prefY));
			trackers.get(i).nameLabel.setPreferredSize(new Dimension(3 * prefX, prefY));
			trackers.get(i).bat.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).rotation.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).position.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).ping.setPreferredSize(new Dimension(prefX, prefY));
			trackers.get(i).tps.setPreferredSize(new Dimension(prefX, prefY));
			trackers.get(i).signalStrength.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).lab.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).lab2.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).desSelect.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).mountSelect.setPreferredSize(new Dimension(2 * prefX, prefY));
			trackers.get(i).type.setPreferredSize(new Dimension(prefX, prefY));
		}
	}

	public TrackersList(VRServer server, VRServerGUI gui) {
		super(BoxLayout.Y_AXIS, false, true);
		this.server = server;
		this.gui = gui;

		setAlignmentY(TOP_ALIGNMENT);

		server.addNewTrackerConsumer(this::newTrackerAdded);
		playSounds = server.config.getBoolean("sound", true);
		showPing = server.config.getBoolean("ping", false);
		showRSSI = server.config.getBoolean("rssi", false);
		showTPS = server.config.getBoolean("tps", false);
	}

	@AWTThread
	public void build() {
		removeAll();
		trackers.sort(Comparator.comparingInt(tr -> getTrackerSort(tr.tracker)));

		Class<? extends Tracker> currentClass = null;

		EJBoxNoStretch line = null;
		boolean first = true;
		JPanel labels = new JPanel(new GridBagLayout());

		labels.add(status = new JLabel("Status:"), s(k(0, 0, GridBagConstraints.VERTICAL), 1, 1));
		labels.add(type = new JLabel("Type:"), s(k(1, 0, GridBagConstraints.VERTICAL), 1, 1));
		labels.add(name = new JLabel("Name:"), s(k(2, 0, GridBagConstraints.VERTICAL), 3, 1));
		labels.add(battery = new JLabel("Battery:"), s(k(5, 0, GridBagConstraints.VERTICAL), 2, 1));
		labels.add(rotation = new JLabel("Rotation:"), s(k(7, 0, GridBagConstraints.VERTICAL), 2, 1));
		labels.add(position = new JLabel("Position:"), s(k(9, 0, GridBagConstraints.VERTICAL), 2, 1));
		if (showPing) {
			labels.add(ping = new JLabel("Ping:"), s(k(11, 0, GridBagConstraints.VERTICAL), 1, 1));
		} else {
			ping = new JLabel(" ");
		}
		if (showTPS) {
			labels.add(tps = new JLabel("TPS:"), s(k(12, 0, GridBagConstraints.VERTICAL), 1, 1));
		} else {
			tps = new JLabel(" ");
		}
		if (showRSSI) {
			labels.add(rssi = new JLabel("RSSI:"), s(k(13, 0, GridBagConstraints.VERTICAL), 2, 1));
		} else {
			rssi = new JLabel(" ");
		}
		labels.add(designation = new JLabel("Designation:"), s(k(15, 0, GridBagConstraints.VERTICAL), 2, 1));
		labels.add(mount = new JLabel("Mount:"), s(k(17, 0, GridBagConstraints.VERTICAL), 2, 1));


		status.setPreferredSize(new Dimension(prefX, prefY));
		type.setPreferredSize(new Dimension(prefX, prefY));
		name.setPreferredSize(new Dimension(3 * prefX, prefY));
		battery.setPreferredSize(new Dimension(2 * prefX, prefY));
		rotation.setPreferredSize(new Dimension(2 * prefX, prefY));
		position.setPreferredSize(new Dimension(2 * prefX, prefY));
		ping.setPreferredSize(new Dimension(prefX, prefY));
		tps.setPreferredSize(new Dimension(prefX, prefY));
		rssi.setPreferredSize(new Dimension(2 * prefX, prefY));
		designation.setPreferredSize(new Dimension(2 * prefX, prefY));
		mount.setPreferredSize(new Dimension(2 * prefX, prefY));

		add(labels);

		for (int i = 0; i < trackers.size(); ++i) {
			TrackerPanel tr = trackers.get(i);
			Tracker t = tr.tracker;
			if (t instanceof ReferenceAdjustedTracker)
				t = ((ReferenceAdjustedTracker<?>) t).getTracker();
			if (currentClass != t.getClass()) {
				currentClass = t.getClass();
				if (line != null)
					line.add(Box.createHorizontalGlue());
				line = null;
				line = new EJBoxNoStretch(BoxLayout.Y_AXIS, false, true);
				line.add(Box.createHorizontalGlue());
				//JLabel nameLabel;
				//line.add(nameLabel = new JLabel(currentClass.getSimpleName()));
				//nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
				line.add(Box.createHorizontalGlue());
				add(line);
				line = null;
			}

			if (line == null) {
				line = new EJBoxNoStretch(BoxLayout.Y_AXIS, false, true);
				add(Box.createVerticalStrut(3));
				add(line);
				first = true;
			} else {
				line.add(Box.createHorizontalStrut(3));
				first = false;
			}
			tr.build();
			line.add(tr);
			if (!first)
				line = null;
		}
		validate();
		gui.refresh();
//		try {
//			Thread silence = new Thread() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep(1000);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					} finally {
//						justStarted = false;
//					}
//				}
//			};
//			//silence.join();
//			silence.run();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}

	@ThreadSafe
	public void updateTrackers() {
		if (lastUpdate + UPDATE_DELAY > System.currentTimeMillis())
			return;
		lastUpdate = System.currentTimeMillis();
		java.awt.EventQueue.invokeLater(() -> {
			for (int i = 0; i < trackers.size(); ++i)
				trackers.get(i).update();
		});
	}

	@ThreadSafe
	public void newTrackerAdded(Tracker t) {
		java.awt.EventQueue.invokeLater(() -> {
			trackers.add(new TrackerPanel(t));
			build();
		});
	}

	private class TrackerPanel extends EJBagNoStretch {

		final Tracker tracker;
		JLabel position;
		JLabel rotation;
		JLabel status;
		JLabel tps;
		JLabel bat;
		JLabel ping;
		JLabel raw;
		JLabel rawMag;
		JLabel calibration;
		JLabel magAccuracy;
		JLabel adj;
		JLabel adjYaw;
		JLabel correction;
		JLabel signalStrength;
		JButton statBut;
		JLabel nameLabel;
		JButton desSelect;
		JButton mountSelect;
		JLabel type;
		JLabel lab;
		JLabel lab2;


		@AWTThread
		public TrackerPanel(Tracker t) {
			super(false, true);

			this.tracker = t;
		}

		@SuppressWarnings("unchecked")
		@AWTThread
		public TrackersList.TrackerPanel build() {
			lab = new JLabel(" ");
			lab2 = new JLabel(" ");
			desSelect = new JButton(" ");
			mountSelect = new JButton(" ");
			int trackerRole = 0;
			// Трекер.
			Tracker realTracker = tracker;
			if (tracker instanceof ReferenceAdjustedTracker)
				realTracker = ((ReferenceAdjustedTracker<? extends Tracker>) tracker).getTracker();
			// Очистка формы.
			removeAll();
			// Инициализация лейбла с именем.
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  имя
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			add(nameLabel = new LabelWBack(realTracker.getDescriptiveName()), s(k(2, 0, 2, GridBagConstraints.CENTER), 3, 1));
			nameLabel.setPreferredSize(new Dimension(3 * prefX, prefY));
			//add(nameLabel = new JLabel(tracker.getDescriptiveName()), s(c(0, row, 2, GridBagConstraints.FIRST_LINE_START), 4, 1));
			//nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
			//nameLabel.setBorder(BorderFactory.createLineBorder(new Color(0x663399), 2, true));

//			JLabel label = new JLabel(" ");
//			label.setPreferredSize(new Dimension(prefX, prefY));
//			for(int i = 0; i < 9; i++) {
//				add(label, k(i,0));
//			}
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  тип
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			add(type = new LabelWBack(" "), k(1, 0, 2, GridBagConstraints.CENTER));
			type.setPreferredSize(new Dimension(prefX, prefY));
			switch (getTrackerRole(tracker)) {
				case 1: {
					type.setText("IMU");
					type.setToolTipText("IMU Tracker");
					break;
				}
				case 2: {
					type.setText("HMD");
					type.setToolTipText("HMD Tracker");
					break;
				}
				case 3: {
					type.setText("CHP");
					type.setToolTipText("Computed Human Pose Tracker");
					break;
				}
				default: {
					type.setText("TRC");
					type.setToolTipText("Tracker");
				}
			}


			// Если пользователь может редактировать трекер.
			if (tracker.userEditable()) {
				TrackerConfig cfg = server.getTrackerConfig(tracker);

				/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  десигнейшен
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
				//JComboBox<String> desSelect;
				//System.out.println(cfg.designation);
				add(desSelect = new JButton(cfg.designation), s(k(14, 0, 2, GridBagConstraints.CENTER), 2, 1));
				desSelect.setPreferredSize(new Dimension(2 * prefX, prefY));
				desSelect.putClientProperty("arc", 99);
				//desSelect.putClientProperty("JButton.buttonType", "roundRect" );
//				for (TrackerPosition p : TrackerPosition.values) {
//					desSelect.addItem(p.name());
//				}
				// Если у данного трекера уже привязано назначение.
				if (cfg.designation != null) {
					TrackerPosition p = TrackerPosition.getByDesignation(cfg.designation);
					if (p != null)
						desSelect.setText(p.name());
				} else {
					desSelect.setText("NONE");
				}
				desSelect.addActionListener(e -> {
					DesignationSelection selection = new DesignationSelection(gui, tracker, server, desSelect.getText());
					selection.setVisible(true);
					desSelect.setText(selection.selected);
					TrackerPosition p = TrackerPosition.valueOf(String.valueOf(selection.selected));
					tracker.setBodyPosition(p);
					server.trackerUpdated(tracker);
				});
				// Добавление слушателя.
//				desSelect.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						TrackerPosition p = TrackerPosition.valueOf(String.valueOf(desSelect.getSelectedItem()));
//						tracker.setBodyPosition(p);
//						server.trackerUpdated(tracker);
//					}
//				});
				// Если трекер имеет IMU на борту.
				if (realTracker instanceof IMUTracker) {
					/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  маунт
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
					IMUTracker imu = (IMUTracker) realTracker;
					TrackerMountingRotation tr = imu.getMountingRotation();
					add(mountSelect = new JButton(), s(k(16, 0, 2, GridBagConstraints.CENTER), 2, 1));
					mountSelect.setPreferredSize(new Dimension(2 * prefX, prefY));
//					for (TrackerMountingRotation p : TrackerMountingRotation.values) {
//						mountSelect.addItem(p.name());
//					}
					if (tr != null) {
						mountSelect.setText(tr.name());
					} else {
						mountSelect.setText(TrackerMountingRotation.BACK.name());
					}
					mountSelect.addActionListener(e -> {
						MountSelection selection = new MountSelection(gui, imu, server, mountSelect.getText());
						selection.setVisible(true);
						mountSelect.setText(selection.selected);
						TrackerMountingRotation tr1 = TrackerMountingRotation.valueOf(String.valueOf(selection.selected));
						imu.setMountingRotation(tr1);
						server.trackerUpdated(tracker);
					});
				} else {
					add(lab = new JLabel(" "), s(k(16, 0, 2, GridBagConstraints.CENTER), 2, 1));
					lab.setPreferredSize(new Dimension(prefX, prefY));
				}
				//row++;
			} else {
				add(lab = new JLabel(" "), s(k(14, 0, 2, GridBagConstraints.CENTER), 2, 1));
				lab.setPreferredSize(new Dimension(2 * prefX, prefY));
				add(lab2 = new JLabel(" "), s(k(16, 0, 2, GridBagConstraints.CENTER), 2, 1));
				lab2.setPreferredSize(new Dimension(2 * prefX, prefY));
			}
			if (tracker.hasRotation())
				//add(new JLabel("Rotation"), c(0, 2, 2, GridBagConstraints.FIRST_LINE_START));
				if (tracker.hasPosition())
					//add(new JLabel("Position"), c(0, 3, 2, GridBagConstraints.FIRST_LINE_START));
					//add(new JLabel("TPS"), c(3, row, 2, GridBagConstraints.FIRST_LINE_START));
					if (realTracker instanceof IMUTracker) {
						//add(new JLabel("Ping"), c(0, 4, 2, GridBagConstraints.FIRST_LINE_START));
						//add(new JLabel("RSSI"), c(4, row, 2, GridBagConstraints.FIRST_LINE_START));
					}
			//row++;
			//if(tracker.hasRotation())
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  повороты
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			add(rotation = new LabelWBack("0 0 0"), s(k(7, 0, 2, GridBagConstraints.CENTER), 2, 1));
			rotation.setPreferredSize(new Dimension(2 * prefX, prefY));
			//rotation.setBackground(Color.RED);
			//if(tracker.hasPosition())
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  позиция
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			add(position = new LabelWBack("0 0 0"), s(k(9, 0, 2, GridBagConstraints.CENTER), 2, 1));
			position.setPreferredSize(new Dimension(2 * prefX, prefY));
/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  пинг
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			ping = new LabelWBack(" ");
			if (showPing) {
				add(ping, s(k(11, 0, 2, GridBagConstraints.CENTER), 1, 1));
			}
			ping.setPreferredSize(new Dimension(prefX, prefY));
			//ping.setBackground(Color.RED);

			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  TPS
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			tps = new LabelWBack(" ");
			if (showTPS) {
				add(tps, s(k(12, 0, 2, GridBagConstraints.CENTER), 1, 1));
			}

			tps.setPreferredSize(new Dimension(prefX, prefY));
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  RSSI
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			signalStrength = new LabelWBack(" ");
			if (showRSSI) {
				add(signalStrength, s(k(13, 0, 2, GridBagConstraints.CENTER), 1, 1));
			}
			signalStrength.setPreferredSize(new Dimension(2 * prefX, prefY));
//
			//row++;
			//add(new JLabel("Status:"), c(0, 0, 2, GridBagConstraints.FIRST_LINE_START));
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																							  статус
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			status = new JLabel(tracker.getStatus().toString().toLowerCase());
			statBut = new JButton(" ");
			statBut.putClientProperty("JButton.buttonType", "roundRect");
			add(statBut, k(0, 0, 2, GridBagConstraints.CENTER));
			if (!(realTracker instanceof IMUTracker)) {
				statBut.setEnabled(false);
			}
			statBut.setPreferredSize(new Dimension(prefX, prefX));
			statBut.setActionCommand("stop");
			//statBut.setOpaque(false);
			//statBut.setBorder(new RoundedBorder(100)); //10 is the radius
//			statBut.setOpaque(false);
//			statBut.setFocusPainted(false);
//			statBut.setBorderPainted(false);
//			statBut.setContentAreaFilled(false);
//			setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			statBut.addActionListener(e -> {
				if (e.getActionCommand() == "stop") {
					statBut.setBackground(Color.RED);
					statBut.setActionCommand("start");
					statBut.setEnabled(false);
				} else {
					statBut.setBackground(Color.GREEN);
					statBut.setActionCommand("stop");
				}
			});
			/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
																		батарея
		 --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
			if (realTracker instanceof TrackerWithBattery) {
				add(bat = new LabelWBack("0"), s(k(5, 0, 2, GridBagConstraints.CENTER), 2, 1));
				bat.setPreferredSize(new Dimension(2 * prefX, prefY));
			} else {
				add(bat = new LabelWBack(" "), s(k(5, 0, 2, GridBagConstraints.CENTER), 2, 1));
				bat.setPreferredSize(new Dimension(2 * prefX, prefY));
			}
			//row++;
//			add(new JLabel("Raw:"), c(0, row, 2, GridBagConstraints.FIRST_LINE_START));
//			add(raw = new JLabel("0 0 0"), s(c(1, row, 2, GridBagConstraints.FIRST_LINE_START), 3, 1));
			//row++;

//			if(realTracker instanceof IMUTracker) {
//				add(new JLabel("Raw mag:"), c(0, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(rawMag = new JLabel("0 0 0"), s(c(1, row, 2, GridBagConstraints.FIRST_LINE_START), 3, 1));
//				row++;
//				add(new JLabel("Cal:"), c(0, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(calibration = new JLabel("0"), c(1, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(new JLabel("Mag acc:"), c(2, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(magAccuracy = new JLabel("0°"), c(3, row, 2, GridBagConstraints.FIRST_LINE_START));
//				row++;
//				add(new JLabel("Correction:"), c(0, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(correction = new JLabel("0 0 0"), s(c(1, row, 2, GridBagConstraints.FIRST_LINE_START), 3, 1));
//				row++;
//			}
//
//
//
//			if(t instanceof ReferenceAdjustedTracker) {
//				add(new JLabel("Adj:"), c(0, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(adj = new JLabel("0 0 0 0"), c(1, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(new JLabel("AdjY:"), c(2, row, 2, GridBagConstraints.FIRST_LINE_START));
//				add(adjYaw = new JLabel("0 0 0 0"), c(3, row, 2, GridBagConstraints.FIRST_LINE_START));
//			}

			//setBorder(BorderFactory.createLineBorder(new Color(0x663399), 2, true));
			TrackersList.this.add(this);
			lastConnection.put(tracker, System.currentTimeMillis());
			connected.put(tracker, false);
			firstInit.put(tracker, true);
			wasPlayed.put(tracker, true);
			return this;
		}

		@SuppressWarnings("unchecked")
		@AWTThread
		public void update() {
			if (position == null && rotation == null)
				return;
			Tracker realTracker = tracker;
			if (tracker instanceof ReferenceAdjustedTracker)
				realTracker = ((ReferenceAdjustedTracker<? extends Tracker>) tracker).getTracker();
			tracker.getRotation(quaternion);
			tracker.getPosition(vector3f);
			quaternion.toAngles(angles);

			if (position != null) {
				position.setText(StringUtils.prettyNumber(vector3f.x, 1)
						+ " " + StringUtils.prettyNumber(vector3f.y, 1)
						+ " " + StringUtils.prettyNumber(vector3f.z, 1));
			} else {
				position.setText(" ");
			}
			if (rotation != null) {
				rotation.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0)
						+ " " + StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0)
						+ " " + StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
			} else {
				rotation.setText(" ");
			}
			if (tracker.getStatus().toString().equalsIgnoreCase("ok")) {
				statBut.setBackground(Color.GREEN);

				if (tracker.userEditable()) {
					lastConnection.put(tracker, System.currentTimeMillis());
					if (!firstInit.get(tracker)) {
						if (!connected.get(tracker)) {
							connected.put(tracker, true);
							if (!wasPlayed.get(tracker)) {
								wasPlayed.put(tracker, true);
								if (playSounds) {
									SoundPlayer.playSound("/sounds/connected");
								}
							}
						}
					} else {
						firstInit.put(tracker, false);
					}
				}
			} else {
				statBut.setBackground(Color.RED);

				if (tracker.userEditable()) {
					if (!firstInit.get(tracker)) {
						if (System.currentTimeMillis() - lastConnection.get(tracker) > 500) {
							wasPlayed.put(tracker, false);
							if (connected.get(tracker)) {
								connected.put(tracker, false);
								if (playSounds) {
									SoundPlayer.playSound("/sounds/disconnected");
								}
							}
						}
					} else {
						firstInit.put(tracker, false);
					}
				}
			}
			//status.setText(tracker.getStatus().toString().toLowerCase());

			if (realTracker instanceof TrackerWithTPS) {
				tps.setText(StringUtils.prettyNumber(((TrackerWithTPS) realTracker).getTPS(), 1));
			}
			if (realTracker instanceof TrackerWithBattery)
				bat.setText(String.format("%d%% (%sV)", Math.round(((TrackerWithBattery) realTracker).getBatteryLevel()), StringUtils.prettyNumber(((TrackerWithBattery) realTracker).getBatteryVoltage()), 1));
			if (tracker instanceof ReferenceAdjustedTracker) {
				((ReferenceAdjustedTracker<Tracker>) tracker).attachmentFix.toAngles(angles);
				if (adj != null)
					adj.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
				((ReferenceAdjustedTracker<Tracker>) tracker).yawFix.toAngles(angles);
				if (adjYaw != null)
					adjYaw.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
			}
			if (realTracker instanceof IMUTracker) {
				if (ping != null)
					ping.setText(String.valueOf(((IMUTracker) realTracker).ping));
				if (signalStrength != null) {
					int signal = ((IMUTracker) realTracker).signalStrength;
					if (signal == -1) {
						signalStrength.setText("N/A");
					} else {
						// -40 dBm is excellent, -95 dBm is very poor
						int percentage = (signal - -95) * (100 - 0) / (-40 - -95) + 0;
						percentage = Math.max(Math.min(percentage, 100), 0);
						signalStrength.setText(String.valueOf(percentage) + "% " + "(" + String.valueOf(signal) + " dBm" + ")");
					}
				}
			}
			realTracker.getRotation(quaternion);
			quaternion.toAngles(angles);
//			raw.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0)
//					+ " " + StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0)
//					+ " " + StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
			if (realTracker instanceof IMUTracker) {
				((IMUTracker) realTracker).rotMagQuaternion.toAngles(angles);
				if (rawMag != null)
					rawMag.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
				if (calibration != null)
					calibration.setText(((IMUTracker) realTracker).calibrationStatus + " / " + ((IMUTracker) realTracker).magCalibrationStatus);
				if (magAccuracy != null)
					magAccuracy.setText(StringUtils.prettyNumber(((IMUTracker) realTracker).magnetometerAccuracy * FastMath.RAD_TO_DEG, 1) + "°");
				((IMUTracker) realTracker).getCorrection(quaternion);
				quaternion.toAngles(angles);
				if (correction != null)
					correction.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0)
							+ " " + StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
			}
		}
	}

	private static int getTrackerSort(Tracker t) {
		if (t instanceof ReferenceAdjustedTracker)
			t = ((ReferenceAdjustedTracker<?>) t).getTracker();
		if (t instanceof IMUTracker)
			return 0;
		if (t instanceof HMDTracker)
			return 100;
		if (t instanceof ComputedTracker)
			return 200;
		return 1000;
	}

	private static int getTrackerRole(Tracker t) {
		if (t instanceof ReferenceAdjustedTracker)
			t = ((ReferenceAdjustedTracker<?>) t).getTracker();
		if (t instanceof IMUTracker)
			return 1;
		if (t instanceof HMDTracker)
			return 2;
		if (t instanceof ComputedTracker)
			return 3;
		return 0;
	}
}
