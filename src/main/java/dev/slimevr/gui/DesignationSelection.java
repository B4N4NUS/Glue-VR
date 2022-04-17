package dev.slimevr.gui;

import dev.slimevr.gui.swing.ButtonWImage;
import dev.slimevr.gui.swing.EJBagNoStretch;
import dev.slimevr.gui.swing.EJBoxNoStretch;
import dev.slimevr.gui.swing.EJPanel;
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
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		this.tracker = tracker;
		build();
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
	}

	public int prefX = 128;
	public int prefY = 128;

	private void build() {
		setTitle("Designation");
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		int y = 1;

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		add(new Label("Select body part to assign your tracker to"), constraints);

		hmd = new ButtonWImage("hmd", "/designation/hmd", false);
		hmd.setPreferredSize(new Dimension(prefX, prefY));
		hmd.setActionCommand(TrackerPosition.HMD+"");
		hmd.addActionListener(this);
		add(hmd, EJPanel.k(0,y));

		chest = new ButtonWImage("chest", "/designation/chest", false);
		chest.setPreferredSize(new Dimension(prefX, prefY));
		chest.setActionCommand(TrackerPosition.CHEST+"");
		chest.addActionListener(this);
		add(chest,EJPanel.k(1,y++));

		waist = new ButtonWImage("waist", "/designation/waist", false);
		waist.setPreferredSize(new Dimension(prefX, prefY));
		waist.setActionCommand(TrackerPosition.WAIST+"");
		waist.addActionListener(this);
		add(waist, EJPanel.k(0,y));

		hip = new ButtonWImage("hip", "/designation/hip", false);
		hip.setPreferredSize(new Dimension(prefX, prefY));
		hip.setActionCommand(TrackerPosition.HIP+"");
		hip.addActionListener(this);
		add(hip,EJPanel.k(1,y++));

		leftLeg = new ButtonWImage("leftLeg", "/designation/left_leg", false);
		leftLeg.setPreferredSize(new Dimension(prefX, prefY));
		leftLeg.setActionCommand(TrackerPosition.LEFT_LEG+"");
		leftLeg.addActionListener(this);
		add(leftLeg, EJPanel.k(0,y));

		rightLeg = new ButtonWImage("rightLeg", "/designation/right_leg", false);
		rightLeg.setPreferredSize(new Dimension(prefX, prefY));
		rightLeg.setActionCommand(TrackerPosition.RIGHT_LEG+"");
		rightLeg.addActionListener(this);
		add(rightLeg,EJPanel.k(1,y++));

		leftAnkle = new ButtonWImage("leftAnkle", "/designation/left_ankle", false);
		leftAnkle.setPreferredSize(new Dimension(prefX, prefY));
		leftAnkle.setActionCommand(TrackerPosition.LEFT_ANKLE+"");
		leftAnkle.addActionListener(this);
		add(leftAnkle,EJPanel.k(0,y));

		rightAnkle = new ButtonWImage("rightAnkle", "/designation/right_ankle", false);
		rightAnkle.setPreferredSize(new Dimension(prefX, prefY));
		rightAnkle.setActionCommand(TrackerPosition.RIGHT_ANKLE+"");
		rightAnkle.addActionListener(this);
		add(rightAnkle,EJPanel.k(1,y++));

		leftFoot = new ButtonWImage("leftFoot", "/designation/left_foot", false);
		leftFoot.setPreferredSize(new Dimension(prefX, prefY));
		leftFoot.setActionCommand(TrackerPosition.LEFT_FOOT+"");
		leftFoot.addActionListener(this);
		add(leftFoot, EJPanel.k(0,y));

		rightFoot = new ButtonWImage("rightFoot", "/designation/right_foot", false);
		rightFoot.setPreferredSize(new Dimension(prefX, prefY));
		rightFoot.setActionCommand(TrackerPosition.RIGHT_FOOT+"");
		rightFoot.addActionListener(this);
		add(rightFoot,EJPanel.k(1,y++));

		leftController = new ButtonWImage("leftController", "/designation/left_controller", false);
		leftController.setPreferredSize(new Dimension(prefX, prefY));
		leftController.setActionCommand(TrackerPosition.LEFT_CONTROLLER+"");
		leftController.addActionListener(this);
		add(leftController,EJPanel.k(0,y));

		rightController = new ButtonWImage("rightController", "/designation/right_controller", false);
		rightController.setPreferredSize(new Dimension(prefX, prefY));
		rightController.setActionCommand(TrackerPosition.RIGHT_CONTROLLER+"");
		rightController.addActionListener(this);
		add(rightController,EJPanel.k(1,y++));

		none = new ButtonWImage("None", "/designation/none", false);
		none.setPreferredSize(new Dimension(prefX, prefY));
		none.setActionCommand(TrackerPosition.NONE+"");
		none.addActionListener(this);
		add(none, EJPanel.k(0,y));
		pack();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		tracker.setBodyPosition(TrackerPosition.getByDesignation(e.getActionCommand()));
		setVisible(false);
		selected = e.getActionCommand().equals("") ? "NONE": e.getActionCommand();
	}
}
