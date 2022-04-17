package dev.slimevr.gui.swing;

import com.formdev.flatlaf.FlatLaf;
import dev.slimevr.VRServer;
import dev.slimevr.gui.SkeletonList;
import dev.slimevr.gui.VRServerGUI;
import io.eiren.util.collections.FastList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.Timer;

public class SkeletonDraw extends JLabel {
	private VRServerGUI gui;
	private VRServer server;
	private List<SkeletonList.NodeStatus> nodes;

	public SkeletonDraw(VRServerGUI gui, VRServer server) {
		this.gui = gui;
		this.server = server;
		build();
	}

	public void build() {
		setMinimumSize(new Dimension(100,100));
		//setPreferredSize(new Dimension(1000,1000));
	}


	static Graphics g;
	@Override
	protected void paintComponent(Graphics g) {
		SkeletonDraw.g = g;
		if (ui != null) {
			ColorUIResource color = (ColorUIResource) UIManager.get("ComboBox.background");
			ColorUIResource fontColor = (ColorUIResource) UIManager.get("ComboBox.foreground");
			Graphics scratchGraphics = (g == null) ? null : g.create();
			try {

				//BufferedImage img = scale(src, (int) Math.round(getHeight()*0.9), (int) Math.round(getHeight()*0.9));
				Font font = getFont();
				assert g != null;

				Graphics2D g2 = (Graphics2D) g.create();
				RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHints(qualityHints);

				g2.setPaint(new GradientPaint(new Point(0, 0), new Color(color.getRed(), color.getGreen(),
						color.getBlue()), new Point(0, getHeight()), new Color(color.getRed(), color.getGreen(),
						color.getBlue())));
				g2.fillRoundRect(2, 2, getHeight()-3, getWidth()-3, 20, 20);
				g2.setPaint(new GradientPaint(new Point(0,0), Color.BLACK, new Point(0,getHeight()),Color.BLACK));
				for(int i = 0; i < gui.skeletonList.nodes.size(); i++) {
					g2.drawOval((int)Math.round(getHeight()*(1+Double.parseDouble(gui.skeletonList.nodes.get(i).x.getText().replace(',','.')))/3), (int)Math.round(getHeight()*(1+Double.parseDouble(gui.skeletonList.nodes.get(i).y.getText().replace(',','.')))/3), 10, 10);
				}

				g2.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				assert scratchGraphics != null;
				scratchGraphics.dispose();
			}
		}
	}
}
