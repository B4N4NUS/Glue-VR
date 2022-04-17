package dev.slimevr.gui;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.ButtonWImage;
import dev.slimevr.vr.trackers.IMUTracker;
import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.TrackerMountingRotation;
import dev.slimevr.vr.trackers.TrackerPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MountSelection extends JDialog implements ActionListener {
	IMUTracker tracker;
	VRServer server;
	JButton forward, left, back, right;
	JButton[] buttons = new JButton[]{forward, left, back, right};

	public int prefX = 256;
	public int prefY = 128;

	public MountSelection(JFrame owner, IMUTracker tracker, VRServer server) {
		super(owner, true);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		this.tracker = tracker;
		this.server = server;
		build();
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
	}

	private void build() {
		//setTitle("Mount");
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;

		add(new Label("Select mount for your tracker"), constraints);

		constraints.gridy++;
		forward = new ButtonWImage("forward", "/mount/front", true);
		forward.setPreferredSize(new Dimension(prefX, prefY));
		forward.setActionCommand("FORWARD");
		forward.addActionListener(this);
		add(forward, constraints);

		constraints.gridy++;
		left = new ButtonWImage("left", "/mount/left", true);
		left.setPreferredSize(new Dimension(prefX, prefY));
		left.setActionCommand("LEFT");
		left.addActionListener(this);
		add(left, constraints);

		constraints.gridy++;
		back = new ButtonWImage("back", "/mount/back", true);
		back.setPreferredSize(new Dimension(prefX, prefY));
		back.setActionCommand("BACK");
		back.addActionListener(this);
		add(back, constraints);

		constraints.gridy++;
		right = new ButtonWImage("right", "/mount/right", true);
		right.setPreferredSize(new Dimension(prefX, prefY));
		right.setActionCommand("RIGHT");
		right.addActionListener(this);
		add(right, constraints);


		//add(pane);
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
		setVisible(false);
	}
}
