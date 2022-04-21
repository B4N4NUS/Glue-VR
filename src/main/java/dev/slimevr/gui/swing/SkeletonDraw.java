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

public class SkeletonDraw extends JPanel {
	private VRServerGUI gui;
	private VRServer server;
	private List<SkeletonList.NodeStatus> nodes;

	private JCheckBox showPoints;
	private JCheckBox showNames;
	private JCheckBox resizePoints;
	private JLabel resizeMultiplierLabel;
	private JSlider resizeMultiplier;

	public SkeletonDraw(VRServerGUI gui, VRServer server) {
		super();
		this.gui = gui;
		this.server = server;
		//setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		//setMinimumSize(new Dimension(100,100));
		//setPreferredSize(new Dimension(600,600));
		build();
	}

	public void build() {
		add(showPoints = new JCheckBox("Show Points"));
		add(showNames = new JCheckBox("Show Names"));
		add(resizePoints = new JCheckBox("Enable Depth"));
		add(resizeMultiplierLabel = new JLabel("Depth:"));
		add(resizeMultiplier = new JSlider(5, 200));
	}


	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (ui != null) {
			ColorUIResource color = (ColorUIResource) UIManager.get("ComboBox.background");
			ColorUIResource fontColor = (ColorUIResource) UIManager.get("ComboBox.foreground");
			Graphics scratchGraphics = (g == null) ? null : g.create();
			try {
				assert g != null;

				Graphics2D g2 = (Graphics2D) g.create();
				RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHints(qualityHints);


				g2.setPaint(new GradientPaint(new Point(0, 0), new Color(color.getRed(), color.getGreen(),
						color.getBlue()), new Point(0, getHeight()), new Color(color.getRed(), color.getGreen(),
						color.getBlue())));

				// first // second
				// third // fourth
				Section first = new Section(2, 2, getWidth() / 2 - 2, getHeight() / 2 - 2);
				Section second = new Section(getWidth() / 2 + 2, 2, getWidth() / 2 - 2, getHeight() / 2 - 2);
				Section third = new Section(2, getHeight() / 2 + 2, getWidth() / 2 - 2, getHeight() / 2 - 2);
				Section fourth = new Section(getWidth() / 2 + 2, getHeight() / 2 + 2, getWidth() / 2 - 2, getHeight() / 2 - 2);

				g2.fillRoundRect(first.startX, first.startY, first.stopX, first.stopY, 20, 20);

				g2.fillRoundRect(second.startX, second.startY, second.stopX, second.stopY, 20, 20);
				g2.fillRoundRect(third.startX, third.startY, third.stopX, third.stopY, 20, 20);
				g2.fillRoundRect(fourth.startX, fourth.startY, fourth.stopX, fourth.stopY, 20, 20);

				g2.setPaint(new GradientPaint(new Point(0, 0), new Color(fontColor.getRed(), fontColor.getGreen(),
						fontColor.getBlue()), new Point(0, getHeight()), new Color(fontColor.getRed(), fontColor.getGreen(),
						fontColor.getBlue())));


				for (int i = 0; i < gui.skeletonList.nodes.size(); i++) {
					if (gui.skeletonList.nodes.get(i).toString().contains("Tracker")) {
						continue;
					}
					int x = (int) Math.round(Double.parseDouble(gui.skeletonList.nodes.get(i).x.getText().replace(',', '.')) / 2 * first.stopX + first.stopX * 1.0 / 2);
					int y = (int) Math.round(first.stopY - Double.parseDouble(gui.skeletonList.nodes.get(i).y.getText().replace(',', '.')) / 2 * first.stopY);
					int z = (int) Math.round(Double.parseDouble(gui.skeletonList.nodes.get(i).z.getText().replace(',', '.')) * resizeMultiplier.getValue()) + 5;
					if (!resizePoints.isSelected()) {
						z = 5;
					}
					if (showPoints.isSelected()) {

						g2.drawOval(first.startX + x - z / 2, first.startY + y - z / 2, z, z);
					}
					if (showNames.isSelected()) {
						g2.drawString(gui.skeletonList.nodes.get(i).toString(), first.startX + x, first.startY + y);
					}
				}
				g2.drawString("Front View", first.startX + 10, first.startY + 20);


				for (int i = 0; i < gui.skeletonList.nodes.size(); i++) {
					if (gui.skeletonList.nodes.get(i).toString().contains("Tracker")) {
						continue;
					}

					int x = (int) Math.round(Double.parseDouble(gui.skeletonList.nodes.get(i).z.getText().replace(',', '.')) / 2 * second.stopX + second.stopX * 1.0 / 2);
					int y = (int) Math.round(second.stopY - Double.parseDouble(gui.skeletonList.nodes.get(i).y.getText().replace(',', '.')) / 2 * second.stopY);
					int z = (int) Math.round(Double.parseDouble(gui.skeletonList.nodes.get(i).x.getText().replace(',', '.')) * resizeMultiplier.getValue()) + 5;
					if (!resizePoints.isSelected()) {
						z = 5;
					}
					if (showPoints.isSelected()) {
						g2.drawOval(second.startX + x - z / 2, second.startY + y - z / 2, z, z);
					}
					if (showNames.isSelected()) {
						g2.drawString(gui.skeletonList.nodes.get(i).toString(), second.startX + x, second.startY + y);
					}
				}
				g2.drawString("Side View", second.startX + 10, second.startY + 20);


				for (int i = 0; i < gui.skeletonList.nodes.size(); i++) {
					if (gui.skeletonList.nodes.get(i).toString().contains("Tracker")) {
						continue;
					}

					int x = (int) Math.round(Double.parseDouble(gui.skeletonList.nodes.get(i).x.getText().replace(',', '.')) / 2 * third.stopX + third.stopX * 1.0 / 2);
					int y = (int) Math.round(third.stopY - Double.parseDouble(gui.skeletonList.nodes.get(i).z.getText().replace(',', '.')) / 2 * third.stopY);
					int z = (int) Math.round(Double.parseDouble(gui.skeletonList.nodes.get(i).y.getText().replace(',', '.')) * resizeMultiplier.getValue()/10) + 5;
					if (!resizePoints.isSelected()) {
						z = 5;
					}
					if (showPoints.isSelected()) {
						g2.drawOval(third.startX + x - z / 2, third.startY / 2 + y - z / 2, z, z);
					}
					if (showNames.isSelected()) {
						g2.drawString(gui.skeletonList.nodes.get(i).toString(), third.startX + x, third.startY / 2 + y);
					}
				}
				g2.drawString("Top-Down View", third.startX + 10, third.startY + 20);

				getComponent(0).setBounds(fourth.startX + 20, fourth.startY + 20, 100, 50);
				getComponent(1).setBounds(fourth.startX + 20, fourth.startY + 40, 100, 50);
				getComponent(2).setBounds(fourth.startX + 20, fourth.startY + 60, 100, 50);
				getComponent(3).setBounds(fourth.startX + 20, fourth.startY + 80, 100, 50);
				getComponent(4).setBounds(fourth.startX + 50, fourth.startY + 80, 100, 50);

				g2.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				assert scratchGraphics != null;
				scratchGraphics.dispose();
			}
			//System.out.println("ui drawn");
		}
	}

	private class Section {
		public int startX, startY, stopX, stopY;

		private Section(int srX, int srY, int spX, int spY) {
			startX = srX;
			startY = srY;
			stopX = spX;
			stopY = spY;
		}
	}
}
