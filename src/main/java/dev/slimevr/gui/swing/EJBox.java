package dev.slimevr.gui.swing;

import javax.swing.BoxLayout;

/**
 * Враппер над EJPanel с BoxLayout'ом.
 */
public class EJBox extends EJPanel {
	
	public EJBox(int layout) {
		super();
		setLayout(new BoxLayout(this, layout));
	}
}
