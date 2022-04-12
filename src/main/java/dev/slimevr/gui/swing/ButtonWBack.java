package dev.slimevr.gui.swing;

import javax.swing.*;
import java.awt.*;

public class ButtonWBack extends JButton {
	public final static Color BUTTON_TOP_GRADIENT = new Color(176, 176, 176);
	public final static Color BUTTON_BOTTOM_GRADIENT = new Color(156, 156, 156);

	public ButtonWBack(String text) {
		super(text);
	}
	public ButtonWBack() {
		super();
	}

	@Override
	protected void paintComponent(Graphics g) {		Graphics2D g2 = (Graphics2D)g.create();

		RenderingHints qualityHints =
				new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHints(qualityHints);

		g2.setPaint(new GradientPaint(new Point(0, 0), BUTTON_TOP_GRADIENT, new Point(0, getHeight()),
				BUTTON_BOTTOM_GRADIENT));
		//g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
		g2.drawRoundRect(0, 0, getWidth()-2, getHeight(), 20, 20);

		g2.dispose();
	}
}
