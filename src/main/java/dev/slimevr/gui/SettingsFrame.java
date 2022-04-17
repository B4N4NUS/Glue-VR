package dev.slimevr.gui;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;

public class SettingsFrame extends JFrame {
	VRServer server;
	VRServerGUI gui;

	private final String url = "https://docs.slimevr.dev/";

	public SettingsFrame(VRServer server, VRServerGUI gui) {
		this.gui = gui;
		this.server = server;
		build();
		setAlwaysOnTop(true);
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setPreferredSize(new Dimension(200,400));
	}

	public int prefX = 200;
	public int prefY = 50;

	JButton body_config, ui_settings, about, wifi_settings;

	private void build() {
		setTitle("Settings");
		//EJBagNoStretch pane = new EJBagNoStretch(false, false);
		setLayout(new GridBagLayout());

		add(body_config = new ButtonWImage("Body Configuration", "/settings/body_configuration", true), EJPanel.k(0,1, GridBagConstraints.HORIZONTAL));
		body_config.setPreferredSize(new Dimension(prefX, prefY));
		body_config.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		body_config.addActionListener(e-> {
			new BodyConfiguration(gui, server);
		});

		add(ui_settings = new ButtonWImage("UI Settings", "/settings/ui_settings", true), EJPanel.k(0,2, GridBagConstraints.HORIZONTAL));
		ui_settings.setPreferredSize(new Dimension(prefX, prefY));
		ui_settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		ui_settings.addActionListener(e-> {
			new UISettings(gui, server);
		});

		add(about = new ButtonWImage("About SlimeVR (Credits)", "/settings/about", true), EJPanel.k(0,3, GridBagConstraints.HORIZONTAL));
		about.setPreferredSize(new Dimension(prefX, prefY));
		about.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		about.addActionListener(e-> {
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
				err.setPreferredSize(new Dimension(200,400));
				err.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				err.pack();
				err.setVisible(true);
			}
		});

		add(wifi_settings = new ButtonWImage("WIFI Settings", "/settings/wifi", true), EJPanel.k(0,4, GridBagConstraints.HORIZONTAL));
		wifi_settings.setPreferredSize(new Dimension(prefX, prefY));
		wifi_settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefY));
		wifi_settings.addMouseListener(new MouseInputAdapter() {
				@SuppressWarnings("unused")
				@Override
				public void mouseClicked(MouseEvent e) {
					new WiFiWindow(gui);
				}
			});

		pack();
	}
}
