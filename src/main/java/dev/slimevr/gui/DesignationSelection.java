package dev.slimevr.gui;

import dev.slimevr.VRServer;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static dev.slimevr.gui.VRServerGUI.prefX;
import static dev.slimevr.gui.VRServerGUI.prefY;

public class DesignationSelection extends JDialog implements ActionListener {
	Tracker tracker;
	JButton none,hmd,chest,waist,hip,leftLeg, rightLeg, leftAnkle, rightAnkle,leftFoot, rightFoot,
			leftController, rightController;
	ArrayList<JButton> buttons = new ArrayList<>();
	public String selected;

	public DesignationSelection(JFrame owner, Tracker tracker, VRServer server, String oldSelect) {
		super(owner, true);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		this.tracker = tracker;
		timer.scheduleAtFixedRate(task, 50,100);
		selected = oldSelect;
		if (Objects.equals(selected, "")) {
			selected = "";
		}
		build();
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		int xloc, yloc;

		if (MouseInfo.getPointerInfo().getLocation().x+getBounds().width > screenBounds.width) {
			xloc = screenBounds.width - getBounds().width;
		} else  {
			xloc = MouseInfo.getPointerInfo().getLocation().x;

		}
		if (MouseInfo.getPointerInfo().getLocation().y+getBounds().height > screenBounds.height) {
			yloc = screenBounds.height - getBounds().height;
		} else {
			yloc = MouseInfo.getPointerInfo().getLocation().y;
		}
		setLocation(xloc, yloc);
	}

	java.util.Timer timer = new Timer("SOSI");
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
	@Override
	public void dispose() {
		super.dispose();
		timer.cancel();
	}

	private void build() {
		setTitle("Designation");
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		int y = 1;

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		//add(new Label("Select body part to assign your tracker to"), constraints);

		hmd = new ButtonWImage("hmd", "/designation/hmd", false);
		hmd.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		hmd.setActionCommand(TrackerPosition.HMD+"");
		hmd.addActionListener(this);
		add(hmd, EJPanel.k(0,y));
		buttons.add(hmd);

		chest = new ButtonWImage("chest", "/designation/chest", false);
		chest.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		chest.setActionCommand(TrackerPosition.CHEST+"");
		chest.addActionListener(this);
		add(chest,EJPanel.k(1,y++));
		buttons.add(chest);

		waist = new ButtonWImage("waist", "/designation/waist", false);
		waist.setPreferredSize(new Dimension((int)(1.5*prefX),(int)(1.5*prefY)));
		waist.setActionCommand(TrackerPosition.WAIST+"");
		waist.addActionListener(this);
		add(waist, EJPanel.k(0,y));
		buttons.add(waist );

		hip = new ButtonWImage("hip", "/designation/hip", false);
		hip.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		hip.setActionCommand(TrackerPosition.HIP+"");
		hip.addActionListener(this);
		add(hip,EJPanel.k(1,y++));
		buttons.add(hip);

		leftLeg = new ButtonWImage("leftLeg", "/designation/left_leg", false);
		leftLeg.setPreferredSize(new Dimension((int)(1.5*prefX),(int)(1.5*prefY)));
		leftLeg.setActionCommand(TrackerPosition.LEFT_LEG+"");
		leftLeg.addActionListener(this);
		add(leftLeg, EJPanel.k(0,y));
		buttons.add(leftLeg);

		rightLeg = new ButtonWImage("rightLeg", "/designation/right_leg", false);
		rightLeg.setPreferredSize(new Dimension((int)(1.5*prefX),(int)(1.5*prefY)));
		rightLeg.setActionCommand(TrackerPosition.RIGHT_LEG+"");
		rightLeg.addActionListener(this);
		add(rightLeg,EJPanel.k(1,y++));
		buttons.add(rightLeg);

		leftAnkle = new ButtonWImage("leftAnkle", "/designation/left_ankle", false);
		leftAnkle.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		leftAnkle.setActionCommand(TrackerPosition.LEFT_ANKLE+"");
		leftAnkle.addActionListener(this);
		add(leftAnkle,EJPanel.k(0,y));
		buttons.add(leftAnkle);

		rightAnkle = new ButtonWImage("rightAnkle", "/designation/right_ankle", false);
		rightAnkle.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		rightAnkle.setActionCommand(TrackerPosition.RIGHT_ANKLE+"");
		rightAnkle.addActionListener(this);
		add(rightAnkle,EJPanel.k(1,y++));
		buttons.add(rightAnkle);

		leftFoot = new ButtonWImage("leftFoot", "/designation/left_foot", false);
		leftFoot.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		leftFoot.setActionCommand(TrackerPosition.LEFT_FOOT+"");
		leftFoot.addActionListener(this);
		add(leftFoot, EJPanel.k(0,y));
		buttons.add(leftFoot);

		rightFoot = new ButtonWImage("rightFoot", "/designation/right_foot", false);
		rightFoot.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		rightFoot.setActionCommand(TrackerPosition.RIGHT_FOOT+"");
		rightFoot.addActionListener(this);
		add(rightFoot,EJPanel.k(1,y++));
		buttons.add(rightFoot);

		leftController = new ButtonWImage("leftController", "/designation/left_controller", false);
		leftController.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		leftController.setActionCommand(TrackerPosition.LEFT_CONTROLLER+"");
		leftController.addActionListener(this);
		add(leftController,EJPanel.k(0,y));
		buttons.add(leftController);

		rightController = new ButtonWImage("rightController", "/designation/right_controller", false);
		rightController.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		rightController.setActionCommand(TrackerPosition.RIGHT_CONTROLLER+"");
		rightController.addActionListener(this);
		add(rightController,EJPanel.k(1,y++));
		buttons.add(rightController);

		none = new ButtonWImage("None", "/designation/none", false);
		none.setPreferredSize(new Dimension((int)(1.5*prefX), (int)(1.5*prefY)));
		none.setActionCommand(TrackerPosition.NONE+"");
		none.addActionListener(this);
		add(none, EJPanel.k(0,y));
		buttons.add(none);

		pack();

		for(JButton butt : buttons) {
			if (Objects.equals(selected, butt.getActionCommand())) {
				butt.requestFocus();
				break;
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		tracker.setBodyPosition(TrackerPosition.getByDesignation(e.getActionCommand()));
		selected = e.getActionCommand().equals("") ? "NONE": e.getActionCommand();
		dispose();
	}
}
