package dev.slimevr.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

//import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme;
import dev.slimevr.Main;
import dev.slimevr.VRServer;
import dev.slimevr.bridge.NamedPipeBridge;
import dev.slimevr.gui.swing.*;
import dev.slimevr.vr.trackers.TrackerRole;
import io.eiren.util.MacOSX;
import io.eiren.util.OperatingSystem;
import io.eiren.util.StringUtils;
import io.eiren.util.ann.AWTThread;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.formdev.flatlaf.*;

import static javax.swing.BoxLayout.PAGE_AXIS;
import static javax.swing.BoxLayout.LINE_AXIS;

public class VRServerGUI extends JFrame {

	public static final String TITLE = "Glue Server (" + Main.VERSION + ")";

	public final VRServer server;
	public final TrackersList trackersList;
	public final SkeletonList skeletonList;
	private JButton resetButton;
	private JButton settingsButton;
	private JPanel pane;

	private float zoom = 1.5f;
	private float initZoom = zoom;

	/**
	 * Конструктор главного окна приложения.
	 * @param server - сервер.
	 */
	@AWTThread
	public VRServerGUI(VRServer server) {
		// Установка имени программы.
		super(TITLE);
		UIManager.put("Button.arc",  20);

		try {
//			UIManager.setLookAndFeel(new FlatHighContrastIJTheme());
//			FlatLaf.updateUI();
//			repaint();
//			FlatLaf.repaintAllFramesAndDialogs();
		}
		catch (Exception ex){
			ex.printStackTrace();
		}

//		// Устнановка лукэндфила.
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch(Exception e) {
//			e.printStackTrace();
//		}

		// Проверка на MACOS.
		if(OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX)
			MacOSX.setTitle(TITLE);
		try {
			List<BufferedImage> images = new ArrayList<BufferedImage>(6);
			images.add(ImageIO.read(VRServerGUI.class.getResource("/icon16.png")));
			images.add(ImageIO.read(VRServerGUI.class.getResource("/icon32.png")));
			images.add(ImageIO.read(VRServerGUI.class.getResource("/icon48.png")));
			images.add(ImageIO.read(VRServerGUI.class.getResource("/icon64.png")));
			images.add(ImageIO.read(VRServerGUI.class.getResource("/icon128.png")));
			images.add(ImageIO.read(VRServerGUI.class.getResource("/icon256.png")));
			setIconImages(images);
			// Проверка на MACOS.
			if(OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) {
				MacOSX.setIcons(images);
			}
		} catch(IOException e1) {
			e1.printStackTrace();
		}

		// Сохраняем инстанс сервера в инстансе гуи.
		this.server = server;

		// Достаем зум из конфига.
		this.zoom = server.config.getFloat("zoom", zoom);
		// Зумим.
		this.initZoom = zoom;
		setDefaultFontSize(zoom);
		// All components should be constructed to the current zoom level by default

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		// Устнановка лейаута.
		getContentPane().setLayout(new BoxLayout(getContentPane(), PAGE_AXIS));

		// Инициализация трекеров и скелетов.
		this.trackersList = new TrackersList(server, this);
		this.skeletonList = new SkeletonList(server, this);

		// Добавление скрола по вертикали и горизонтали.
		add(new JScrollPane(pane = new JPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// Нахождение границ экрана.
		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		// Минимальный размер.
		setMinimumSize(new Dimension(100, 100));
		// Установка размера окна.
		setSize(Math.min(server.config.getInt("window.width", 800), screenBounds.width), Math.min(server.config.getInt("window.height", 800), screenBounds.height));
		// Установка расположения окна на экране.
		setLocation(server.config.getInt("window.posx", screenBounds.x + (screenBounds.width - getSize().width) / 2), screenBounds.y + server.config.getInt("window.posy", (screenBounds.height - getSize().height) / 2));

		// Resize and close listeners to save position and size betwen launcher starts
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

		build();
	}

	/**
	 * Метод сохранения инфы о состоянии гуи в конфиг.
	 */
	protected void saveFrameInfo() {
		Rectangle b = getBounds();
		server.config.setProperty("window.width", b.width);
		server.config.setProperty("window.height", b.height);
		server.config.setProperty("window.posx", b.x);
		server.config.setProperty("window.posy", b.y);
		server.saveConfig();
	}

	/**
	 * Метод получения инфы о зуме.
	 * @return - зум гуи.
	 */
	public float getZoom() {
		return this.zoom;
	}

	/**
	 * Метод обновления дисплея.
	 */
	public void refresh() {
		// Pack and display
		//pack();
		setVisible(true);
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
	}

	/**
	 * Расставляем элементы гуи.
	 */
	@AWTThread
	private void build() {
		pane.removeAll();
		pane.setLayout(new BoxLayout(pane, PAGE_AXIS));

		settingsButton = new ButtonWImage("settings", "/settings/settings", false);
		settingsButton.addActionListener(e -> {
			SettingsFrame settingsF = new SettingsFrame(server, this);
			settingsF.setVisible(true);
		});

		JPanel panelWButtons = new JPanel();
		panelWButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		panelWButtons.setLayout(new GridBagLayout());
		//panelWButtons.setPreferredSize(new Dimension(1000,75));
		//panelWButtons.setBackground(Color.black);


		GridBagConstraints con = new GridBagConstraints();
		con.gridy = 0;
		con.gridx = 0;
		con.weighty = 1;
		con.weightx = 0;
		con.fill = GridBagConstraints.BOTH;
		con.anchor = GridBagConstraints.NORTHEAST;

		//panelWButtons.add(Box.createVerticalStrut(50));
		//settingsButton.setMinimumSize(new Dimension(100,100));
		//settingsButton.setMaximumSize(new Dimension(100,100));
		settingsButton.setPreferredSize(new Dimension(50,50));
		panelWButtons.add(settingsButton, con);
		con.gridx++;
		panelWButtons.add(Box.createHorizontalStrut(10), con);
		con.gridx++;

		panelWButtons.add(resetButton = new JButton("RESET") {{
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					reset();
				}
			});
		}}, con);
		con.gridx++;
		panelWButtons.add(Box.createHorizontalStrut(10), con);
		con.gridx++;

		panelWButtons.add(new JButton("Fast Reset") {{
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					resetFast();
				}
			});
		}}, con);
		con.gridx++;
		panelWButtons.add(Box.createHorizontalStrut(10), con);
		con.gridx++;

		panelWButtons.add(new JButton("GUI Zoom (x" + StringUtils.prettyNumber(zoom, 2) + ")") {{
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					guiZoom();
					setText("GUI Zoom (x" + StringUtils.prettyNumber(zoom, 2) + ")");
				}
			});

		}}, con);
		con.gridx++;
		panelWButtons.add(Box.createHorizontalStrut(10), con);
		con.gridx++;

		con.weightx = 1;

		panelWButtons.add(Box.createHorizontalStrut(100), con);
		con.weightx = 0;
		pane.add(panelWButtons);
		pane.add(Box.createHorizontalGlue());

		// Место с инфой о трекерах.
		pane.add(new EJBox(LINE_AXIS) {{
			setBorder(new EmptyBorder(i(5)));
			add(new EJBoxNoStretch(PAGE_AXIS, false, true) {{
				setAlignmentY(TOP_ALIGNMENT);
//				JLabel l;
//				add(l = new JLabel("Trackers list"));
//				l.setFont(l.getFont().deriveFont(Font.BOLD));
//				l.setAlignmentX(0.5f);
				add(trackersList);
				add(Box.createVerticalGlue());
			}});

//			//	Кусок с кнопочками и частями тела.
//			add(new EJBoxNoStretch(PAGE_AXIS, false, true) {{
//				setAlignmentY(TOP_ALIGNMENT);
//				JLabel l;
//				add(l = new JLabel("Body proportions"));
//				l.setFont(l.getFont().deriveFont(Font.BOLD));
//				l.setAlignmentX(0.5f);
//				add(new SkeletonConfigGUI(server, VRServerGUI.this));
//
//				// Проверка наличия ВР моста у сервера.
//				if(server.hasBridge(NamedPipeBridge.class)) {
//					NamedPipeBridge br = server.getVRBridge(NamedPipeBridge.class);
//					add(l = new JLabel("SteamVR Trackers"));
//					l.setFont(l.getFont().deriveFont(Font.BOLD));
//					l.setAlignmentX(0.5f);
//					add(l = new JLabel("Changes may require restart of SteamVR"));
//					l.setFont(l.getFont().deriveFont(Font.ITALIC));
//					l.setAlignmentX(0.5f);
//
//					// Все ниже - чекбоксы виртуальных костей для SteamVR.
//					add(new EJBagNoStretch(false, true) {{
//						JCheckBox waistCb;
//						add(waistCb = new JCheckBox("Waist"), c(1, 1));
//						waistCb.setSelected(br.getShareSetting(TrackerRole.WAIST));
//						waistCb.addActionListener(new ActionListener() {
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								server.queueTask(() -> {
//									br.changeShareSettings(TrackerRole.WAIST, waistCb.isSelected());
//								});
//							}
//						});
//
//						JCheckBox legsCb;
//						add(legsCb = new JCheckBox("Legs"), c(2, 1));
//						legsCb.setSelected(br.getShareSetting(TrackerRole.LEFT_FOOT) && br.getShareSetting(TrackerRole.RIGHT_FOOT));
//						legsCb.addActionListener(new ActionListener() {
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								server.queueTask(() -> {
//									br.changeShareSettings(TrackerRole.LEFT_FOOT, legsCb.isSelected());
//									br.changeShareSettings(TrackerRole.RIGHT_FOOT, legsCb.isSelected());
//								});
//							}
//						});
//
//						JCheckBox chestCb;
//						add(chestCb = new JCheckBox("Chest"), c(1, 2));
//						chestCb.setSelected(br.getShareSetting(TrackerRole.CHEST));
//						chestCb.addActionListener(new ActionListener() {
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								server.queueTask(() -> {
//									br.changeShareSettings(TrackerRole.CHEST, chestCb.isSelected());
//								});
//							}
//						});
//
//						JCheckBox kneesCb;
//						add(kneesCb = new JCheckBox("Knees"), c(2, 2));
//						kneesCb.setSelected(br.getShareSetting(TrackerRole.LEFT_KNEE) && br.getShareSetting(TrackerRole.RIGHT_KNEE));
//						kneesCb.addActionListener(new ActionListener() {
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								server.queueTask(() -> {
//									br.changeShareSettings(TrackerRole.LEFT_KNEE, kneesCb.isSelected());
//									br.changeShareSettings(TrackerRole.RIGHT_KNEE, kneesCb.isSelected());
//								});
//							}
//						});
//
//					}});
//
//
//					add(Box.createVerticalStrut(10));
//				}
//				// Инфа о состоянии скелета.
//				add(new JLabel("Skeleton data"));
//				add(skeletonList);
//				add(Box.createVerticalGlue());
			//}});
		}});
		pane.add(Box.createVerticalGlue());

		refresh();

		server.addOnTick(trackersList::updateTrackers);
		server.addOnTick(skeletonList::updateBones);
	}

	// For now only changes font size, but should change fixed components size in the future too
	private void guiZoom() {
		if(zoom <= 1.0f) {
			zoom = 1.5f;
		} else if(zoom <= 1.5f) {
			zoom = 1.75f;
		} else if(zoom <= 1.75f) {
			zoom = 2.0f;
		} else if(zoom <= 2.0f) {
			zoom = 2.5f;
		} else {
			zoom = 1.0f;
		}
		processNewZoom(zoom / initZoom, pane);
		refresh();
		server.config.setProperty("zoom", zoom);
		server.saveConfig();
	}

	private static void processNewZoom(float zoom, Component comp) {
		if(comp.isFontSet()) {
			Font newFont = new ScalableFont(comp.getFont(), zoom);
			comp.setFont(newFont);
		}
		if(comp instanceof Container) {
			Container cont = (Container) comp;
			for(Component child : cont.getComponents())
				processNewZoom(zoom, child);
		}
	}

	private static void setDefaultFontSize(float zoom) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if(value instanceof javax.swing.plaf.FontUIResource) {
				javax.swing.plaf.FontUIResource f = (javax.swing.plaf.FontUIResource) value;
				javax.swing.plaf.FontUIResource f2 = new javax.swing.plaf.FontUIResource(f.deriveFont(f.getSize() * zoom));
				UIManager.put(key, f2);
			}
		}
	}

	@AWTThread
	private void resetFast() {
		server.resetTrackersYaw();
	}

	@AWTThread
	private void reset() {
		ButtonTimer.runTimer(resetButton, 3, "RESET", server::resetTrackers);
	}
}
