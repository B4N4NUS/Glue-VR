package dev.slimevr.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.ButtonWBack;
import dev.slimevr.gui.swing.EJBagNoStretch;
import dev.slimevr.gui.swing.EJBoxNoStretch;
import dev.slimevr.gui.swing.LabelWBack;
import dev.slimevr.vr.trackers.ComputedTracker;
import dev.slimevr.vr.trackers.HMDTracker;
import dev.slimevr.vr.trackers.IMUTracker;
import dev.slimevr.vr.trackers.ReferenceAdjustedTracker;
import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.TrackerConfig;
import dev.slimevr.vr.trackers.TrackerMountingRotation;
import dev.slimevr.vr.trackers.TrackerPosition;
import dev.slimevr.vr.trackers.TrackerWithBattery;
import dev.slimevr.vr.trackers.TrackerWithTPS;
import io.eiren.util.StringUtils;
import io.eiren.util.ann.AWTThread;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.collections.FastList;

public class TrackersList extends EJBoxNoStretch {

	private static final long UPDATE_DELAY = 50;

	Quaternion quaternion = new Quaternion();
	Vector3f vector3f = new Vector3f();
	float[] angles = new float[3];

	final int prefX = 50;
	final int prefY = 50;

	private List<TrackerPanel> trackers = new FastList<>();

	private final VRServer server;
	private final VRServerGUI gui;
	private long lastUpdate = 0;

	public TrackersList(VRServer server, VRServerGUI gui) {
		super(BoxLayout.Y_AXIS, false, true);
		this.server = server;
		this.gui = gui;

		setAlignmentY(TOP_ALIGNMENT);

		server.addNewTrackerConsumer(this::newTrackerAdded);
	}

	@AWTThread
	private void build() {
		removeAll();

		trackers.sort(Comparator.comparingInt(tr -> getTrackerSort(tr.tracker)));

		Class<? extends Tracker> currentClass = null;

		EJBoxNoStretch line = null;
		boolean first = true;
		JPanel labels = new JPanel(new GridBagLayout());
		//labels.setBackground(Color.red);
		JLabel status, ip, battery, rotation, position, ping, designation, mount;

		labels.add(status = new JLabel("Status"), k(0, 0));
		labels.add(ip = new JLabel("IP"), k(1, 0));
		labels.add(battery = new JLabel("Battery"), k(2, 0));
		labels.add(rotation = new JLabel("Rotation"), k(3, 0));
		labels.add(position = new JLabel("Position"), k(4, 0));
		labels.add(ping = new JLabel("Ping"), k(5, 0));
		labels.add(designation = new JLabel("Designation"), k(6, 0));
		labels.add(mount = new JLabel("Mount"), k(7, 0));

		status.setPreferredSize(new Dimension(prefX, prefX));
		ip.setPreferredSize(new Dimension(3*prefX, prefY));
		battery.setPreferredSize(new Dimension(prefX, prefY));
		rotation.setPreferredSize(new Dimension(prefX, prefY));
		position.setPreferredSize(new Dimension(prefX, prefY));
		ping.setPreferredSize(new Dimension(prefX, prefY));
		designation.setPreferredSize(new Dimension(2*prefX, prefY));
		mount.setPreferredSize(new Dimension(2*prefX, prefY));
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
				JLabel nameLabel;
				line.add(nameLabel = new JLabel(currentClass.getSimpleName()));
				nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
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


		@AWTThread
		public TrackerPanel(Tracker t) {
			super(false, true);

			this.tracker = t;
		}

		@SuppressWarnings("unchecked")
		@AWTThread
		public TrackersList.TrackerPanel build() {

			// Трекер.
			Tracker realTracker = tracker;
			if (tracker instanceof ReferenceAdjustedTracker)
				realTracker = ((ReferenceAdjustedTracker<? extends Tracker>) tracker).getTracker();
			// Очистка формы.
			removeAll();
			// Инициализация лейбла с именем.
			JLabel nameLabel;
			add(nameLabel = new JLabel(realTracker.getDescriptiveName()), k(1, 0, 2, GridBagConstraints.CENTER));
			nameLabel.setPreferredSize(new Dimension(3*prefX, prefY));
			//add(nameLabel = new JLabel(tracker.getDescriptiveName()), s(c(0, row, 2, GridBagConstraints.FIRST_LINE_START), 4, 1));
			//nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
			//nameLabel.setBorder(BorderFactory.createLineBorder(new Color(0x663399), 2, true));

//			JLabel label = new JLabel(" ");
//			label.setPreferredSize(new Dimension(prefX, prefY));
//			for(int i = 0; i < 9; i++) {
//				add(label, k(i,0));
//			}

			// Если пользователь может редактировать трекер.
			if (tracker.userEditable()) {
				TrackerConfig cfg = server.getTrackerConfig(tracker);

				JButton desSelect;
				//JComboBox<String> desSelect;
				//System.out.println(cfg.designation);
				add(desSelect = new JButton(cfg.designation), s(k(7, 0, 2, GridBagConstraints.CENTER), 1, 1));
				desSelect.setPreferredSize(new Dimension(2*prefX, prefY));
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
				desSelect.addActionListener(e->{
					DesignationSelection selection = new DesignationSelection(gui, tracker);
					selection.setVisible(true);
					desSelect.setText(selection.selected);
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
					IMUTracker imu = (IMUTracker) realTracker;
					TrackerMountingRotation tr = imu.getMountingRotation();
					JButton mountSelect;
					add(mountSelect = new JButton(), s(k(8, 0, 2, GridBagConstraints.CENTER), 1, 1));
					mountSelect.setPreferredSize(new Dimension(2*prefX, prefY));
//					for (TrackerMountingRotation p : TrackerMountingRotation.values) {
//						mountSelect.addItem(p.name());
//					}
					if (tr != null) {
						mountSelect.setText(tr.name());
					} else {
						mountSelect.setText(TrackerMountingRotation.BACK.name());
					}
					mountSelect.addActionListener(e -> {
						MountSelection selection = new MountSelection(gui, imu, server);
						selection.setVisible(true);
						mountSelect.setText(selection.selected);
//						TrackerMountingRotation tr1 = TrackerMountingRotation.valueOf(String.valueOf(mountSelect.getSelectedItem()));
//						imu.setMountingRotation(tr1);
//						server.trackerUpdated(tracker);
					});
				} else {
					JLabel lab;
					add(lab = new JLabel(" "), s(k(8, 0, 2, GridBagConstraints.CENTER), 1, 1));
					lab.setPreferredSize(new Dimension(prefX, prefY));
				}
				//row++;
			} else {
				JLabel mountSelect;
				add(mountSelect = new JLabel(" "), s(k(8, 0, 2, GridBagConstraints.CENTER), 1, 1));
				mountSelect.setPreferredSize(new Dimension(2*prefX, prefY));
				JLabel lab;
				add(lab = new JLabel(" "), s(k(7, 0, 2, GridBagConstraints.CENTER), 1, 1));
				lab.setPreferredSize(new Dimension(2*prefX, prefY));

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
			add(rotation = new LabelWBack("0 0 0"), k(3, 0, 2, GridBagConstraints.CENTER));
			rotation.setPreferredSize(new Dimension(prefX, prefY));
			rotation.setBackground(Color.RED);
			//if(tracker.hasPosition())
			add(position = new LabelWBack("0 0 0"), k(4, 0, 2, GridBagConstraints.CENTER));
			position.setPreferredSize(new Dimension(prefX, prefY));

			add(ping = new LabelWBack(" "), k(5, 0, 2, GridBagConstraints.CENTER));
			ping.setPreferredSize(new Dimension(prefX, prefY));
			ping.setBackground(Color.RED);
			//add(signalStrength = new JLabel(""), c(4, row, 2, GridBagConstraints.FIRST_LINE_START));

//			if(realTracker instanceof TrackerWithTPS) {
//				add(tps = new JLabel("0"), c(3, row, 2, GridBagConstraints.FIRST_LINE_START));
//			} else {
//				add(new JLabel(""), c(3, row, 2, GridBagConstraints.FIRST_LINE_START));
//			}
			//row++;
			//add(new JLabel("Status:"), c(0, 0, 2, GridBagConstraints.FIRST_LINE_START));
			status = new JLabel(tracker.getStatus().toString().toLowerCase());
			statBut = new JButton(" ");
			statBut.putClientProperty("JButton.buttonType", "roundRect" );
			add(statBut, k(0, 0, 2, GridBagConstraints.CENTER));
			statBut.setPreferredSize(new Dimension(prefX, prefX));
			statBut.setActionCommand("stop");
			//statBut.setOpaque(false);
			//statBut.setBorder(new RoundedBorder(100)); //10 is the radius
//			statBut.setOpaque(false);
//			statBut.setFocusPainted(false);
//			statBut.setBorderPainted(false);
//			statBut.setContentAreaFilled(false);
//			setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			statBut.addActionListener(e-> {
				if (e.getActionCommand() == "stop") {
					statBut.setBackground(Color.RED);
					statBut.setActionCommand("start");
					statBut.setEnabled(false);
				} else {
					statBut.setBackground(Color.GREEN);
					statBut.setActionCommand("stop");
				}
			});
			if (realTracker instanceof TrackerWithBattery) {
				add(bat = new JLabel("0"), k(2, 0, 2, GridBagConstraints.CENTER));
				bat.setPreferredSize(new Dimension(prefX, prefY));
			} else {
				add(bat = new JLabel(" "), k(2, 0, 2, GridBagConstraints.CENTER));
				bat.setPreferredSize(new Dimension(prefX, prefY));
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
			if (tracker.getStatus().toString().toLowerCase().equals("ok")) {
				statBut.setBackground(Color.GREEN);
			} else {
				statBut.setBackground(Color.RED);
			}
			//status.setText(tracker.getStatus().toString().toLowerCase());

//			if(realTracker instanceof TrackerWithTPS) {
//				tps.setText(StringUtils.prettyNumber(((TrackerWithTPS) realTracker).getTPS(), 1));
//			}
//			if(realTracker instanceof TrackerWithBattery)
//				bat.setText(String.format("%d%% (%sV)", Math.round(((TrackerWithBattery) realTracker).getBatteryLevel()), StringUtils.prettyNumber(((TrackerWithBattery) realTracker).getBatteryVoltage()), 1));
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
//				if (signalStrength != null) {
//					int signal = ((IMUTracker) realTracker).signalStrength;
//					if (signal == -1) {
//						signalStrength.setText("N/A");
//					} else {
//						// -40 dBm is excellent, -95 dBm is very poor
//						int percentage = (signal - -95) * (100 - 0) / (-40 - -95) + 0;
//						percentage = Math.max(Math.min(percentage, 100), 0);
//						signalStrength.setText(String.valueOf(percentage) + "% " + "(" + String.valueOf(signal) + " dBm" + ")");
//					}
//				}
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
}


class RoundedBorder implements Border {

	private int radius;


	public RoundedBorder(int radius) {
		this.radius = radius;
	}


	public Insets getBorderInsets(Component c) {
		return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
	}


	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		g.drawRoundRect(x, y, width-1, height-1, radius, radius);
	}

	public boolean isBorderOpaque() {
		return true;
	}
}

