package dev.slimevr.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import dev.slimevr.VRServer;
import dev.slimevr.bridge.NamedPipeBridge;
import dev.slimevr.gui.swing.*;
import dev.slimevr.vr.trackers.TrackerRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

import static dev.slimevr.gui.VRServerGUI.prefX;
import static dev.slimevr.gui.VRServerGUI.prefY;
import static javax.swing.BoxLayout.PAGE_AXIS;

public class BodyConfiguration extends JFrame {
	private VRServer server;
	private VRServerGUI gui;
	private JList<FlatAllIJThemes.FlatIJLookAndFeelInfo> themeList;
	public static FlatAllIJThemes.FlatIJLookAndFeelInfo oldSel;


	private JButton guiZoom;


	public static boolean showAutoSkeleton = false;
	public static boolean showSteamTrackerSelection = false;
	public static boolean showRawData = false;
	public static boolean stillLiving = true;

	public BodyConfiguration(VRServerGUI gui, VRServer server) {
		//super();
		this.server = server;
		this.gui = gui;

		showAutoSkeleton = server.config.getBoolean("autoskeleton", false);
		showSteamTrackerSelection = server.config.getBoolean("autosteam", false);
		showRawData = server.config.getBoolean("showraw", false);
		//setSize(new Dimension(400,400));
		setIconImages(gui.getIconImages());
		build();

		//setPreferredSize(new Dimension(100,100));

		//IJThemesPanel themes = new IJThemesPanel();
		setVisible(true);
		setAlwaysOnTop(true);
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		// Минимальный размер.
		setMinimumSize(new Dimension(100, 100));
		// Установка размера окна.
		setSize(Math.min(server.config.getInt("body.width", getWidth()), screenBounds.width), Math.min(server.config.getInt("body.height", getHeight()), screenBounds.height));
		// Установка расположения окна на экране.
		setLocation(server.config.getInt("body.posx", getLocation().x), screenBounds.y + server.config.getInt("body.posy", getLocation().y));
		addComponentListener(new AbstractComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				saveFrameInfo();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				saveFrameInfo();
			}
		});
	}

	protected void saveFrameInfo() {
		Rectangle b = getBounds();
		server.config.setProperty("body.width", b.width);
		server.config.setProperty("body.height", b.height);
		server.config.setProperty("body.posx", b.x);
		server.config.setProperty("body.posy", b.y);
		server.saveConfig();
	}

	@Override
	public void dispose() {
		super.dispose();
		stillLiving = false;
	}

	public void build() {
		getContentPane().removeAll();

		JPanel body, steam, data;
		body = new JPanel(new GridBagLayout());
		steam = new JPanel(new GridBagLayout());
		data = new JPanel(new GridBagLayout());
		//removeAll();

		setTitle("Body Configuration");
		setLayout(new GridBagLayout());
		JLabel l;

		SkeletonConfigGUI skeleton = new SkeletonConfigGUI(server, gui);
		 //skeleton.setPreferredSize(new Dimension(100,400));
		body.add(l = new JLabel("Body proportions"), EJPanel.k(0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 1));
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		l.setAlignmentX(0.5f);

		if (!showAutoSkeleton) {
			body.add(skeleton, EJPanel.k(0, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1, 1, 10, 5));
		} else {
			AutoBoneWindow autoBone = new AutoBoneWindow(server, skeleton);
			body.add(autoBone, EJPanel.k(0, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1, 1, 1, 5));
		}
		add(body, EJPanel.k(0, 0, GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1, 1, 1, 5));


		// Проверка наличия ВР моста у сервера.
		if (server.hasBridge(NamedPipeBridge.class)) {

			NamedPipeBridge br = server.getVRBridge(NamedPipeBridge.class);
			steam.add(l = new JLabel("SteamVR Trackers"), EJPanel.k(0, 3 + (showAutoSkeleton? 0:9), GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 5));
			l.setFont(l.getFont().deriveFont(Font.BOLD));
			l.setAlignmentX(0.5f);
			steam.add(l = new JLabel("Changes may require restart of SteamVR"), EJPanel.k(0, 4 + (showAutoSkeleton? 0:9), GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 5));
			l.setFont(l.getFont().deriveFont(Font.ITALIC));
			l.setAlignmentX(0.5f);
			if (!showSteamTrackerSelection) {
				// Все ниже - чекбоксы виртуальных костей для SteamVR.
				steam.add(new EJBagNoStretch(false, false) {{
					JCheckBox waistCb;
					add(waistCb = new JCheckBox("Waist"), EJPanel.k(0, 0, GridBagConstraints.NONE));
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
					add(legsCb = new JCheckBox("Legs"), EJPanel.k(1, 0, GridBagConstraints.NONE));
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
					add(chestCb = new JCheckBox("Chest"), EJPanel.k(0, 1, GridBagConstraints.NONE));
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
					add(kneesCb = new JCheckBox("Knees"), EJPanel.k(1, 1, GridBagConstraints.NONE));
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

				}}, EJPanel.k(0, 5 + (showAutoSkeleton? 0:9), GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 1));
			} else {
				steam.add(new JButton("Calculate SteamVR Trackers") {{
					addActionListener(e -> {
						for (int i = 0; i < gui.skeletonList.nodes.size(); i++) {
							if (gui.skeletonList.nodes.get(i).toString().contains("Tracker")) {
								//System.out.println(gui.skeletonList.nodes.get(i).toString());
								if (gui.skeletonList.nodes.get(i).toString().toLowerCase().contains("left-knee")) {
									br.changeShareSettings(TrackerRole.LEFT_KNEE, true);
									System.out.println("added left knee");
								}
								if (gui.skeletonList.nodes.get(i).toString().toLowerCase().contains("right-knee")) {
									br.changeShareSettings(TrackerRole.RIGHT_KNEE, true);
									System.out.println("added right knee");
								}
								if (gui.skeletonList.nodes.get(i).toString().toLowerCase().contains("chest")) {
									br.changeShareSettings(TrackerRole.CHEST, true);
									System.out.println("added chest");
								}
								if (gui.skeletonList.nodes.get(i).toString().toLowerCase().contains("waist")) {
									br.changeShareSettings(TrackerRole.WAIST, true);
									System.out.println("added waist");
								}
								if (gui.skeletonList.nodes.get(i).toString().toLowerCase().contains("left-foot")) {
									br.changeShareSettings(TrackerRole.LEFT_FOOT, true);
									System.out.println("added left foot");
								}
								if (gui.skeletonList.nodes.get(i).toString().toLowerCase().contains("right-foot")) {
									br.changeShareSettings(TrackerRole.RIGHT_FOOT, true);
									System.out.println("added right foot");
								}
							}
						}
					});
				}}, EJPanel.k(0, 6 + (showAutoSkeleton? 0:9), GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 1));
			}
			//add(Box.createVerticalStrut(10));
		} else {
			steam.add(new Label("Cant Make Bridge Connection"), EJPanel.k(0, 6 + (showAutoSkeleton? 0:9), GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 15));
		}


		if (showRawData) {
			add(steam, EJPanel.k(0, 1, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1, 1, 1, 1));
			data.add(l = new JLabel("Skeleton data"), EJPanel.k(0, 0, GridBagConstraints.BOTH, GridBagConstraints.SOUTH, 1, 1, 1, 5));
			l.setFont(l.getFont().deriveFont(Font.BOLD));
			data.add(gui.skeletonList, EJPanel.k(0, 2, GridBagConstraints.BOTH, GridBagConstraints.SOUTH, 1, 1, 1, 1));
			add(data,EJPanel.k(0, 2, GridBagConstraints.BOTH, GridBagConstraints.SOUTH, 1, 1, 1, 1));
			FlatLaf.updateUI();
		} else {
			add(steam, EJPanel.k(0, 1, GridBagConstraints.VERTICAL, GridBagConstraints.SOUTH, 1, 1, 1, 1));
		}

//		add(new EJBoxNoStretch(PAGE_AXIS, false, true) {{
//			setAlignmentY(TOP_ALIGNMENT);
//
//
//			//pack();
//		}}, EJPanel.k(0,0,GridBagConstraints.NONE));
		// Инфа о состоянии скелета.


//
//

		//gui.skeletonList.setBackground(Color.red);
		//add(Box.createHorizontalGlue(), EJPanel.k(0,0,GridBagConstraints.NONE, GridBagConstraints.CENTER, 0,0,12,1));

		pack();
		//repaint();
		//draw.setPreferredSize(new Dimension(400, 400));


	}
}
