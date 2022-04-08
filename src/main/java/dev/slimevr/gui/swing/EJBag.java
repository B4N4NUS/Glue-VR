package dev.slimevr.gui.swing;

import java.awt.GridBagLayout;

/**
 * Враппер над EJPanel с GridBagLayout'ом.
 */
public class EJBag extends EJPanel {
	
	public EJBag() {
		super(new GridBagLayout());
	}
}
