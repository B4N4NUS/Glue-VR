package dev.slimevr.gui.swing;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatLabelUI;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class LabelWBack extends JLabel {
	public LabelWBack(String text) {
		super(text);
	}

	public LabelWBack() {
		super();
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (ui != null) {
			ColorUIResource color = (ColorUIResource)UIManager.get("ComboBox.background");
			//System.out.println(color);

			Graphics scratchGraphics = (g == null) ? null : g.create();
			try {
				String text = getText();
				Font font = getFont();
				//g.setFont(font);
				//g.setColor(Color.white);
				FontMetrics fm = g.getFontMetrics();
				int width = fm.stringWidth(text);




				Graphics2D g2 = (Graphics2D)g.create();
				RenderingHints qualityHints =
						new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHints(qualityHints);

				g2.setPaint(new GradientPaint(new Point(0,0),new Color(color.getRed(), color.getGreen(), color.getBlue()), new Point(0,getHeight()),new Color(color.getRed(), color.getGreen(), color.getBlue())));
				g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-4, 20, 20);
				//g2.drawRoundRect(0, 0, getWidth()-2, getHeight()-2, 20, 20);
				g.drawString(text,  (getWidth()-width)/2,(int) Math.round(getHeight()/2*1.1));
				g2.dispose();

				//ui.update(scratchGraphics, this);
			}
			finally {
				assert scratchGraphics != null;
				scratchGraphics.dispose();
			}
		}
	}
}
