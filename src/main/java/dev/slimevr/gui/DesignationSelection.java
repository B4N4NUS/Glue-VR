package dev.slimevr.gui;

import dev.slimevr.vr.trackers.Tracker;
import dev.slimevr.vr.trackers.TrackerPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DesignationSelection extends JDialog implements ActionListener {
	Tracker tracker;
	JButton none,hmd,chest,waist,hip,leftLeg, rightLeg, leftAnkle, rightAnkle,leftFoot, rightFoot,
			leftController, rightController;
	JButton[] buttons = new JButton[]{none,hmd,chest,waist,hip,leftLeg, rightLeg, leftAnkle, rightAnkle,leftFoot, rightFoot,
			leftController, rightController};
	public String selected;

	public DesignationSelection(JFrame owner, Tracker tracker) {
		super(owner, true);
		this.tracker = tracker;
		build();
	}

	private void build() {
		int counter = 0;
		setTitle("Designation");
		//JPanel pane = new JPanel();
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		for (TrackerPosition p : TrackerPosition.values) {
			//desSelect.addItem(p.name());
			constraints.gridy = counter;
			buttons[counter] = new JButton(p.designation.equals("") ? "NONE": p.designation);
			buttons[counter].setActionCommand(p.designation);
			buttons[counter].addActionListener(this);
			add(buttons[counter++], constraints);
		}
		//add(pane);
		pack();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		tracker.setBodyPosition(TrackerPosition.getByDesignation(e.getActionCommand()));
		setVisible(false);
		selected = e.getActionCommand().equals("") ? "NONE": e.getActionCommand();
	}
}
