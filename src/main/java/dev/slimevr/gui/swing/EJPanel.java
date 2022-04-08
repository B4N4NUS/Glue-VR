package dev.slimevr.gui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.JPanel;

/**
 * Враппер над JPanel
 */
public abstract class EJPanel extends JPanel {

	// Контроллеры уменьшения гуи.
	public static boolean NEEDS_DOWNSCALE = false;
	public static float DOWNSCALE_FACTOR = 0.75f;

	/**
	 * Стандартный конструктор.
	 */
	public EJPanel() {
		super();
	}

	/**
	 * Стандартный конструктор.
	 */
	public EJPanel(LayoutManager manager) {
		super(manager);
	}

	/**
	 * Статичный метод доунскейла компонента.
	 * @param c - элемент гуи
	 * @param width - ширина.
	 * @param height - высота.
	 */
	public static void s(Component c, int width, int height) {
		// Если нужен даунскейл.
		if(NEEDS_DOWNSCALE) {
			width = (int) Math.ceil(width * DOWNSCALE_FACTOR);
			height = (int) Math.ceil(height * DOWNSCALE_FACTOR);
		}
		// Установка новых параметров компонента, связанных с размером.
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
	
	public static void minWidth(Component c, int width, int height) {
		if(NEEDS_DOWNSCALE) {
			height = (int) Math.ceil(height * DOWNSCALE_FACTOR);
			width = (int) Math.ceil(width * DOWNSCALE_FACTOR);
		}
		c.setPreferredSize(new Dimension(Short.MAX_VALUE, height));
		c.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
		c.setMinimumSize(new Dimension(width, height));
	}
	
	public static void minHeight(Component c, int width, int height) {
		if(NEEDS_DOWNSCALE) {
			height = (int) Math.ceil(height * DOWNSCALE_FACTOR);
			width = (int) Math.ceil(width * DOWNSCALE_FACTOR);
		}
		c.setPreferredSize(new Dimension(width, Short.MAX_VALUE));
		c.setMaximumSize(new Dimension(width, Short.MAX_VALUE));
		c.setMinimumSize(new Dimension(width, height));
	}

	// Методы для более удобной работы с GridBagConstrains.
	public static GridBagConstraints c(int x, int y) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		return c;
	}
	
	public static GridBagConstraints c(int x, int y, int padding) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.insets = new Insets(padding, padding, padding, padding);
		return c;
	}
	
	public static GridBagConstraints c(int x, int y, int padding, int anchor) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.insets = new Insets(padding, padding, padding, padding);
		c.anchor = anchor;
		return c;
	}
	
	public static GridBagConstraints c(int x, int y, Insets insets) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.insets = insets;
		return c;
	}
	
	public static GridBagConstraints s(GridBagConstraints c, int gridwidth, int gridheight) {
		c.gridwidth = gridwidth;
		c.gridheight = gridheight;
		return c;
	}
	
	public static Insets i(int s) {
		return new Insets(s, s, s, s);
	}

	public static Insets i(int h, int v) {
		return new Insets(v, h, v, h);
	}
	
	public static Component padding(int width, int height) {
		return Box.createRigidArea(new Dimension(width, height));
	}
	
	public static int fontSize(int baseSize) {
		return NEEDS_DOWNSCALE ? (int) Math.ceil(baseSize * DOWNSCALE_FACTOR) : baseSize;
	}
}
