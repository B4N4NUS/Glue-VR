package dev.slimevr.gui;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.EJPanel;
import dev.slimevr.gui.swing.SkeletonDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.Timer;
import java.util.TimerTask;

public class SkeletonRenderer extends JFrame {
	private VRServerGUI gui;
	private VRServer server;
	private java.util.Timer timer = new Timer(true);
	private SkeletonDraw draw;
	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			draw.repaint();
			//System.out.println("draw");
		}
	};

	public static boolean stillLiving = true;

	public SkeletonRenderer(VRServerGUI gui, VRServer server) {
		this.gui = gui;
		this.server = server;
		build();
		setVisible(true);
		//setAlwaysOnTop(true);
		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(1000,1000));
		setIconImages(gui.getIconImages());
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		// Минимальный размер.
		setMinimumSize(new Dimension(100, 100));
		// Установка размера окна.
		setSize(Math.min(server.config.getInt("renderer.width", getWidth()), screenBounds.width), Math.min(server.config.getInt("renderer.height", getHeight()), screenBounds.height));
		// Установка расположения окна на экране.
		setLocation(server.config.getInt("renderer.posx", getLocation().x), screenBounds.y + server.config.getInt("renderer.posy", getLocation().y));
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
		server.config.setProperty("renderer.width", b.width);
		server.config.setProperty("renderer.height", b.height);
		server.config.setProperty("renderer.posx", b.x);
		server.config.setProperty("renderer.posy", b.y);
		server.saveConfig();
	}

	@Override
	public void dispose() {
		timer.cancel();
		stillLiving = false;
		super.dispose();
	}
	private void build() {
		//setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		add(draw = new SkeletonDraw(gui, server));
		//add(new SkeletonDraw(gui, server), EJPanel.c(0,1));

		setTitle("Skeleton Renderer");
		pack();
		timer.scheduleAtFixedRate(task,50,50);
	}
}
