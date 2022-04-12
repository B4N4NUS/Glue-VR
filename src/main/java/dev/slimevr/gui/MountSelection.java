package dev.slimevr.gui;

import dev.slimevr.VRServer;
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

	public MountSelection(JFrame owner, IMUTracker tracker, VRServer server) {
		super(owner, true);
		this.tracker = tracker;
		this.server = server;
		build();
	}

	private void build() {
		setTitle("Mount");
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;

		forward = new JButton("forward");
		forward.setActionCommand("FORWARD");
		forward.addActionListener(this);
		add(forward, constraints);

		constraints.gridy = 1;
		forward = new JButton("left");
		forward.setActionCommand("LEFT");
		forward.addActionListener(this);
		add(forward, constraints);

		constraints.gridy = 2;
		forward = new JButton("back");
		forward.setActionCommand("BACK");
		forward.addActionListener(this);
		add(forward, constraints);

		constraints.gridy = 3;
		forward = new JButton("right");
		forward.setActionCommand("RIGHT");
		forward.addActionListener(this);
		add(forward, constraints);


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
