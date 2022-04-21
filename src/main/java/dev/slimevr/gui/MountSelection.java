package dev.slimevr.gui;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.ButtonWImage;
import dev.slimevr.vr.trackers.IMUTracker;
import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.TrackerMountingRotation;
import dev.slimevr.vr.trackers.TrackerPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class MountSelection extends JDialog implements ActionListener {
	IMUTracker tracker;
	VRServer server;
	JButton forward, left, back, right;
	JButton[] buttons = new JButton[]{forward, left, back, right};

	Timer timer = new Timer("SOSI");
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			PointerInfo cursor = MouseInfo.getPointerInfo();
			//System.out.println(cursor.getLocation() + " - cursor");
			//System.out.println(getLocation());
			if (cursor.getLocation().x < getLocation().x || cursor.getLocation().x > getLocation().x + getSize().width ||
					cursor.getLocation().y < getLocation().y || cursor.getLocation().y > getLocation().y + getSize().height) {
					//System.out.println("SOSI");
					cancel();
					dispose();

			}
		}
	};

	public int prefX = 256;
	public int prefY = 128;

	public MountSelection(JFrame owner, IMUTracker tracker, VRServer server, String oldSelect) {
		super(owner, true);
		timer.scheduleAtFixedRate(task, 50,100);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		this.tracker = tracker;
		this.server = server;

		selected = oldSelect;

		build();
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
	}

	@Override
	public void dispose() {
		super.dispose();
		timer.cancel();
	}

	private void build() {
		JPanel pane = new JPanel(new GridBagLayout());
		//setTitle("Mount");
		//setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;

		pane.add(new Label("Select mount for your tracker"), constraints);

		constraints.gridy++;
		forward = new ButtonWImage("forward", "/mount/front", true);
		forward.setPreferredSize(new Dimension(prefX, prefY));
		forward.setActionCommand("FORWARD");
		forward.addActionListener(this);
		pane.add(forward, constraints);

		constraints.gridy++;
		left = new ButtonWImage("left", "/mount/left", true);
		left.setPreferredSize(new Dimension(prefX, prefY));
		left.setActionCommand("LEFT");
		left.addActionListener(this);
		pane.add(left, constraints);

		constraints.gridy++;
		back = new ButtonWImage("back", "/mount/back", true);
		back.setPreferredSize(new Dimension(prefX, prefY));
		back.setActionCommand("BACK");
		back.addActionListener(this);
		pane.add(back, constraints);

		constraints.gridy++;
		right = new ButtonWImage("right", "/mount/right", true);
		right.setPreferredSize(new Dimension(prefX, prefY));
		right.setActionCommand("RIGHT");
		right.addActionListener(this);
		pane.add(right, constraints);

		add(pane);
		pack();

	}
	public String selected;
	@Override
	public void actionPerformed(ActionEvent e) {
//		tracker.setBodyPosition(TrackerPosition.getByDesignation(e.getActionCommand()));
//		setVisible(false);

		//IMUTracker imu = (IMUTracker) tracker;
		TrackerMountingRotation tr = tracker.getMountingRotation();
		TrackerMountingRotation tr1 = TrackerMountingRotation.valueOf(String.valueOf(e.getActionCommand()));
		tracker.setMountingRotation(tr1);
		server.trackerUpdated(tracker);
		selected = e.getActionCommand();
		dispose();
	}
}
