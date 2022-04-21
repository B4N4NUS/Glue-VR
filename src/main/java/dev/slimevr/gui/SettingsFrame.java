package dev.slimevr.gui;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.net.URL;

import static dev.slimevr.gui.VRServerGUI.prefX;
import static dev.slimevr.gui.VRServerGUI.prefY;

public class SettingsFrame extends JFrame {
	VRServer server;
	VRServerGUI gui;
	private BodyConfiguration skeletonWindow;
	private UISettings UIWindow;
	private WiFiWindow WIFIWindow;

	public static boolean stillLiving = true;

	private final String url = "https://docs.slimevr.dev/";

	public void scalee() {
		body_config.setPreferredSize(new Dimension(4*prefX, prefY));
		ui_settings.setPreferredSize(new Dimension(4*prefX, prefY));
		about.setPreferredSize(new Dimension(4*prefX, prefY));
		wifi_settings.setPreferredSize(new Dimension(4*prefX, prefY));
		pack();
		if (skeletonWindow != null) {
			VRServerGUI.processNewZoom(gui.getZoom()/ gui.getInitZoom(), skeletonWindow);
			skeletonWindow.pack();
		}
		//setSize(new Dimension(4*prefX, 4*prefY));
	}

	public SettingsFrame(VRServer server, VRServerGUI gui) {
		this.gui = gui;
		this.server = server;
		build();
		setAlwaysOnTop(true);
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setIconImages(gui.getIconImages());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//setPreferredSize(new Dimension(200, 400));
		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		// Минимальный размер.
		setMinimumSize(new Dimension(100, 100));
		// Установка размера окна.
		setSize(Math.min(server.config.getInt("settings.width", getWidth()), screenBounds.width), Math.min(server.config.getInt("settings.height", getHeight()), screenBounds.height));
		// Установка расположения окна на экране.
		setLocation(server.config.getInt("settings.posx", getLocation().x), screenBounds.y + server.config.getInt("settings.posy", getLocation().y));
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
		server.config.setProperty("settings.width", b.width);
		server.config.setProperty("settings.height", b.height);
		server.config.setProperty("settings.posx", b.x);
		server.config.setProperty("settings.posy", b.y);
		server.saveConfig();
	}

	@Override
	public void dispose() {
		super.dispose();
		stillLiving = false;
	}

	JButton body_config, ui_settings, about, wifi_settings;

	private void build() {
		setTitle("Settings");
		//EJBagNoStretch pane = new EJBagNoStretch(false, false);
		setLayout(new GridBagLayout());

		add(body_config = new ButtonWImage("Body Configuration", "/settings/body_configuration", true), EJPanel.k(0, 1, GridBagConstraints.BOTH));
		body_config.setPreferredSize(new Dimension(4 * prefX, prefY));
		//body_config.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		body_config.addActionListener(e -> {
			if (skeletonWindow!= null & BodyConfiguration.stillLiving) {
				skeletonWindow.toFront();
			} else {
				SkeletonRenderer.stillLiving = true;
				skeletonWindow = new BodyConfiguration(gui, server);
				if (UIWindow != null & UISettings.stillLiving) {
					UIWindow.updateSkeleton(skeletonWindow);
				}
			}
		});

		add(ui_settings = new ButtonWImage("UI Settings", "/settings/ui_settings", true), EJPanel.k(0, 2, GridBagConstraints.BOTH));
		ui_settings.setPreferredSize(new Dimension(4 * prefX, prefY));
		//ui_settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		ui_settings.addActionListener(e -> {
			if (UIWindow != null & UISettings.stillLiving) {
				UIWindow.toFront();
			} else {
				UISettings.stillLiving = true;
				UIWindow = new UISettings(gui, server, this, skeletonWindow);
			}
		});

		add(about = new ButtonWImage("About SlimeVR (Credits)", "/settings/about", true), EJPanel.k(0, 3, GridBagConstraints.BOTH));
		about.setPreferredSize(new Dimension(4 * prefX, prefY));
		//about.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		about.addActionListener(e -> {
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			boolean canOpen = true;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse((new URL(url).toURI()));
				} catch (Exception ex) {
					canOpen = false;
				}
			} else {
				canOpen = false;
			}
			if (!canOpen) {
				JDialog err = new JDialog();
				err.add(new JLabel("Can't open web page"));
				err.add(new JLabel("Try using url:" + url + " at your browser"));
				err.setTitle("Error");
				err.setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
				err.setPreferredSize(new Dimension(200, 400));
				err.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				err.pack();
				err.setVisible(true);
			}
		});

		add(wifi_settings = new ButtonWImage("WIFI Settings", "/settings/wifi", true), EJPanel.k(0, 4, GridBagConstraints.BOTH));
		wifi_settings.setPreferredSize(new Dimension(4 * prefX, prefY));
		//wifi_settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		wifi_settings.addActionListener(e-> {
			if (WIFIWindow != null & WiFiWindow.stillLiving) {
				WIFIWindow.toFront();
			} else {
				WiFiWindow.stillLiving = true;
				WIFIWindow = new WiFiWindow(gui, server);
			}
		});

		pack();
	}
}
