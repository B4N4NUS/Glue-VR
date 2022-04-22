package dev.slimevr.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.util.LoggingFacade;
import dev.slimevr.VRServer;
import dev.slimevr.gui.VRServerGUI;
import dev.slimevr.gui.swing.EJPanel;
import io.eiren.util.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import static dev.slimevr.gui.VRServerGUI.prefX;
import static dev.slimevr.gui.VRServerGUI.prefY;

public class UISettings extends JFrame {
	private VRServer server;
	private VRServerGUI gui;
	private SettingsFrame owner;
	private JList<FlatAllIJThemes.FlatIJLookAndFeelInfo> themeList;
	public static FlatAllIJThemes.FlatIJLookAndFeelInfo oldSel;

	public static boolean stillLiving = true;

	private JCheckBox sound, ping, tps, rssi, autoSkeleton, autoSteam, showRawData;
	private JButton guiZoom;
	private BodyConfiguration skeleton;

	public UISettings(VRServerGUI gui, VRServer server, SettingsFrame owner, BodyConfiguration skeletonFrame) {
		this.server = server;
		this.gui = gui;
		this.owner = owner;
		this.skeleton = skeletonFrame;

		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setTitle("UI Settings");
		//setPreferredSize(new Dimension(100,100));
		build();
		setVisible(true);
		//IJThemesPanel themes = new IJThemesPanel();
		setAlwaysOnTop(true);
		setIconImages(gui.getIconImages());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		// Минимальный размер.
		setMinimumSize(new Dimension(100, 100));
		// Установка размера окна.
		setSize(Math.min(server.config.getInt("ui.width", getWidth()), screenBounds.width), Math.min(server.config.getInt("ui.height", getHeight()), screenBounds.height));
		// Установка расположения окна на экране.
		setLocation(server.config.getInt("ui.posx", getLocation().x), screenBounds.y + server.config.getInt("ui.posy", getLocation().y));
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
		server.config.setProperty("ui.width", b.width);
		server.config.setProperty("ui.height", b.height);
		server.config.setProperty("ui.posx", b.x);
		server.config.setProperty("ui.posy", b.y);
		server.saveConfig();
	}

	@Override
	public void dispose() {
		super.dispose();
		stillLiving = false;
	}

	public void updateSkeleton(BodyConfiguration skeleton) {
		this.skeleton = skeleton;
	}

	public void build() {
		setLayout(new GridBagLayout());

		JPanel global, tracker, skeleton, themes;

		themeList = new JList<>(FlatAllIJThemes.INFOS);
		//themeList.setPreferredSize(new Dimension(100,100));
		themeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(themeList);
		themeList.setLayoutOrientation(JList.VERTICAL);
		themeList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
														  int index, boolean isSelected, boolean cellHasFocus) {
				String name = ((FlatAllIJThemes.FlatIJLookAndFeelInfo) value).getName();
				int sep = name.indexOf('/');
				if (sep >= 0)
					name = name.substring(sep + 1).trim();

				JComponent c = (JComponent) super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
				c.setToolTipText(buildToolTip((FlatAllIJThemes.FlatIJLookAndFeelInfo) value));
				return c;
			}

			private String buildToolTip(FlatAllIJThemes.FlatIJLookAndFeelInfo ti) {
				return "Name: " + ti.getName();
			}
		});
		themeList.setModel(new AbstractListModel<FlatAllIJThemes.FlatIJLookAndFeelInfo>() {
			private static final long serialVersionUID = 1536029084261517876L;

			@Override
			public int getSize() {
				return FlatAllIJThemes.INFOS.length;
			}

			@Override
			public FlatAllIJThemes.FlatIJLookAndFeelInfo getElementAt(int index) {
				return FlatAllIJThemes.INFOS[index];
			}
		});

		if (oldSel != null) {
			for (int i = 0; i < FlatAllIJThemes.INFOS.length; i++) {
				FlatAllIJThemes.FlatIJLookAndFeelInfo theme = FlatAllIJThemes.INFOS[i];
				if (oldSel.getName().equals(theme.getName())) {
					themeList.setSelectedIndex(i);
					break;
				}
			}
		} else {
			for (int i = 0; i < FlatAllIJThemes.INFOS.length; i++) {
				String currentName = FlatAllIJThemes.INFOS[i].getClassName();
				if (currentName.equals(server.config.getString("laf"))) {
					themeList.setSelectedIndex(i);
					break;
				}
			}
		}
		// select first theme if none selected
		if (themeList.getSelectedIndex() < 0)
			themeList.setSelectedIndex(0);
		// scroll selection into visible area
		int sel = themeList.getSelectedIndex();
		if (sel >= 0) {
			Rectangle bounds = themeList.getCellBounds(sel, sel);
			if (bounds != null)
				themeList.scrollRectToVisible(bounds);
		}
		themeList.addListSelectionListener(this::themesListValueChanged);


		global = new JPanel();
		global.setLayout(new GridBagLayout());
		global.add(new JLabel("Global Settings:"), EJPanel.k(0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 1, 1, 1));
		global.add(guiZoom = new JButton("GUI Zoom (x" + StringUtils.prettyNumber(gui.getZoom(), 2) + ")") {{
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					gui.guiZoom();
					VRServerGUI.processNewZoom(gui.getZoom() / gui.getInitZoom(), getContentPane());
					pack();
					setText("GUI Zoom (x" + StringUtils.prettyNumber(gui.getZoom(), 2) + ")");
					owner.scalee();
				}
			});

		}}, EJPanel.k(0, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 1, 1, 1));
		global.add(sound = new JCheckBox("Enable Sound Notification"), EJPanel.k(0, 2, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 0, 1, 1));
		sound.setSelected(server.config.getBoolean("sound", true));
		sound.addActionListener(e -> {
			TrackersList.playSounds = sound.isSelected();
			server.config.setProperty("sound", sound.isSelected());
			server.saveConfig();
		});
		add(global, EJPanel.k(0, 0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 1, 1, 1));


		themes = new JPanel();
		themes.setLayout(new GridBagLayout());
		themes.add(new JLabel("Themes:"), EJPanel.k(0, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0.1, 0.1, 1, 1));
		//add(themeList, EJPanel.k(1,1,GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1,1, 3,1));
		add(Box.createHorizontalStrut(50), EJPanel.k(1, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, 6, 1));

		JScrollPane themesPane;
		themes.add(themesPane = new JScrollPane(themeList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), EJPanel.k(0, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, 10, 2));
		themesPane.setMinimumSize(new Dimension(200, 200));
		add(themes, EJPanel.k(1, 0, GridBagConstraints.BOTH, GridBagConstraints.NORTHEAST, 1, 1, 10, 2));

		tracker = new JPanel();
		tracker.setLayout(new GridBagLayout());
		tracker.add(new JLabel("Tracker Table Settings:"), EJPanel.k(0, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 2));
		tracker.add(ping = new JCheckBox("Show Ping"), EJPanel.k(0, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 2));
		ping.setSelected(server.config.getBoolean("ping", false));
		ping.addActionListener(e -> {
			TrackersList.showPing = ping.isSelected();
			server.config.setProperty("ping", ping.isSelected());
			server.saveConfig();
			gui.trackersList.build();
		});

		tracker.add(tps = new JCheckBox("Show TPS"), EJPanel.k(0, 2, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));
		tps.setSelected(server.config.getBoolean("tps", false));
		tps.addActionListener(e -> {
			TrackersList.showTPS = tps.isSelected();
			server.config.setProperty("tps", tps.isSelected());
			server.saveConfig();
			gui.trackersList.build();
		});

		tracker.add(rssi = new JCheckBox("Show RSSI"), EJPanel.k(0, 3, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));
		rssi.setSelected(server.config.getBoolean("rssi", false));
		rssi.addActionListener(e -> {
			TrackersList.showRSSI = rssi.isSelected();
			server.config.setProperty("rssi", rssi.isSelected());
			server.saveConfig();
			gui.trackersList.build();
		});
		add(tracker, EJPanel.k(0, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));

		skeleton = new JPanel();
		skeleton.setLayout(new GridBagLayout());
		skeleton.add(new JLabel("Skeleton settings:"), EJPanel.k(0, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));
		skeleton.add(autoSkeleton = new JCheckBox("Auto Skeleton Length"), EJPanel.k(0, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));
		autoSkeleton.setSelected(server.config.getBoolean("autoskeleton", false));
		autoSkeleton.addActionListener(e -> {
			BodyConfiguration.showAutoSkeleton = autoSkeleton.isSelected();
			server.config.setProperty("autoskeleton", autoSkeleton.isSelected());
			server.saveConfig();
			try {
				this.skeleton.build();
			} catch(Exception ignored){}
		});
		skeleton.add(autoSteam = new JCheckBox("Auto SteamVR Trackers Selection"), EJPanel.k(0, 2, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));
		autoSteam.setSelected(server.config.getBoolean("autosteam", false));
		autoSteam.addActionListener(e -> {
			BodyConfiguration.showSteamTrackerSelection = autoSteam.isSelected();
			server.config.setProperty("autosteam", autoSteam.isSelected());
			server.saveConfig();
			try {
				this.skeleton.build();
			} catch(Exception ignored){}
		});
		skeleton.add(showRawData = new JCheckBox("Show Raw Position Data"), EJPanel.k(0, 3, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0, 1, 1, 1));
		showRawData.setSelected(server.config.getBoolean("showraw", false));
		showRawData.addActionListener(e-> {
			BodyConfiguration.showRawData = showRawData.isSelected();
			server.config.setProperty("showraw",showRawData.isSelected());
			server.saveConfig();
			try {
				this.skeleton.build();
			} catch(Exception ignored){}
		});
		add(skeleton, EJPanel.k(0, 2, GridBagConstraints.BOTH, GridBagConstraints.SOUTHWEST, 0, 1, 1, 1));

//		add(autoSkeleton = new JCheckBox("Auto Steam Trackers"), EJPanel.k(0,3,GridBagConstraints.BOTH,GridBagConstraints.WEST,0,1,1,1));
//		autoSkeleton.setSelected(server.config.getBoolean("autoskeleton", false));
//		autoSkeleton.addActionListener(e-> {
//			TrackersList.showRSSI = autoSkeleton.isSelected();
//			server.config.setProperty("rssi", autoSkeleton.isSelected());
//			server.saveConfig();
//		});


		pack();
	}

	private void themesListValueChanged(ListSelectionEvent e) {
		FlatAllIJThemes.FlatIJLookAndFeelInfo themeInfo = themeList.getSelectedValue();

		if (e.getValueIsAdjusting())
			return;

		EventQueue.invokeLater(() -> {
			setTheme(themeInfo);
			oldSel = themeList.getSelectedValue();
		});
	}

	private void setTheme(FlatAllIJThemes.FlatIJLookAndFeelInfo themeInfo) {
		try {
			UIManager.setLookAndFeel(themeInfo.getClassName());
			server.config.setProperty("laf", themeInfo.getClassName());
			server.saveConfig();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		FlatLaf.updateUI();
		FlatLaf.repaintAllFramesAndDialogs();
		repaint();
	}
}
