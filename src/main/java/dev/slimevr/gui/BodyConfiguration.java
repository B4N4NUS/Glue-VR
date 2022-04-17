package dev.slimevr.gui;

import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import dev.slimevr.VRServer;
import dev.slimevr.bridge.NamedPipeBridge;
import dev.slimevr.gui.swing.*;
import dev.slimevr.vr.trackers.TrackerRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.BoxLayout.PAGE_AXIS;

public class BodyConfiguration extends JFrame {
	private VRServer server;
	private VRServerGUI gui;
	private JList<FlatAllIJThemes.FlatIJLookAndFeelInfo> themeList;
	public static FlatAllIJThemes.FlatIJLookAndFeelInfo oldSel;

	private JCheckBox sound;
	private JCheckBox showSteamTrackerSelection;
	private JButton guiZoom;

	public BodyConfiguration(VRServerGUI gui, VRServer server) {
		this.server = server;
		this.gui = gui;

		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setTitle("UI Settings");
		//setPreferredSize(new Dimension(100,100));
		build();
		setVisible(true);
		//IJThemesPanel themes = new IJThemesPanel();
		setAlwaysOnTop(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	java.util.Timer timer = new Timer(true);
	private SkeletonDraw draw;
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			draw.repaint();
			System.out.println("draw");
		}
	};

	@Override
	public void dispose() {
		timer.cancel();
		super.dispose();
	}

	public void build() {
		setLayout(new GridBagLayout());
		// Кусок с кнопочками и частями тела.
		add(new EJBoxNoStretch(PAGE_AXIS, false, true) {{
			setAlignmentY(TOP_ALIGNMENT);
			JLabel l;
			add(l = new JLabel("Body proportions"));
			l.setFont(l.getFont().deriveFont(Font.BOLD));
			l.setAlignmentX(0.5f);
			add(new SkeletonConfigGUI(server, gui));

			// Проверка наличия ВР моста у сервера.
			if (server.hasBridge(NamedPipeBridge.class)) {
				NamedPipeBridge br = server.getVRBridge(NamedPipeBridge.class);
				add(l = new JLabel("SteamVR Trackers"));
				l.setFont(l.getFont().deriveFont(Font.BOLD));
				l.setAlignmentX(0.5f);
				add(l = new JLabel("Changes may require restart of SteamVR"));
				l.setFont(l.getFont().deriveFont(Font.ITALIC));
				l.setAlignmentX(0.5f);

				// Все ниже - чекбоксы виртуальных костей для SteamVR.
				add(new EJBagNoStretch(false, true) {{
					JCheckBox waistCb;
					add(waistCb = new JCheckBox("Waist"), c(1, 1));
					waistCb.setSelected(br.getShareSetting(TrackerRole.WAIST));
					waistCb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							server.queueTask(() -> {
								br.changeShareSettings(TrackerRole.WAIST, waistCb.isSelected());
							});
						}
					});

					JCheckBox legsCb;
					add(legsCb = new JCheckBox("Legs"), c(2, 1));
					legsCb.setSelected(br.getShareSetting(TrackerRole.LEFT_FOOT) && br.getShareSetting(TrackerRole.RIGHT_FOOT));
					legsCb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							server.queueTask(() -> {
								br.changeShareSettings(TrackerRole.LEFT_FOOT, legsCb.isSelected());
								br.changeShareSettings(TrackerRole.RIGHT_FOOT, legsCb.isSelected());
							});
						}
					});

					JCheckBox chestCb;
					add(chestCb = new JCheckBox("Chest"), c(1, 2));
					chestCb.setSelected(br.getShareSetting(TrackerRole.CHEST));
					chestCb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							server.queueTask(() -> {
								br.changeShareSettings(TrackerRole.CHEST, chestCb.isSelected());
							});
						}
					});

					JCheckBox kneesCb;
					add(kneesCb = new JCheckBox("Knees"), c(2, 2));
					kneesCb.setSelected(br.getShareSetting(TrackerRole.LEFT_KNEE) && br.getShareSetting(TrackerRole.RIGHT_KNEE));
					kneesCb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							server.queueTask(() -> {
								br.changeShareSettings(TrackerRole.LEFT_KNEE, kneesCb.isSelected());
								br.changeShareSettings(TrackerRole.RIGHT_KNEE, kneesCb.isSelected());
							});
						}
					});

				}});


				add(Box.createVerticalStrut(10));
			}
			// Инфа о состоянии скелета.
			add(new JLabel("Skeleton data"));
			add(gui.skeletonList);
			add(Box.createVerticalGlue());
		}}, EJPanel.k(0,0,GridBagConstraints.NONE, GridBagConstraints.EAST, 0,0,1,1));

		add(draw = new SkeletonDraw(gui, server), EJPanel.k(1,0,GridBagConstraints.NONE, GridBagConstraints.EAST, 0,0,1,1));
		timer.scheduleAtFixedRate(task,50,50);
		pack();
	}
}
