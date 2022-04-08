package dev.slimevr.gui.swing;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BoxLayout;

/**
 * Враппед над EJPanel с кастомным лейаутом.
 */
public class EJBoxNoStretch extends EJPanel {

	/**
	 * Конструктор с инициализацией флагов о растягивании по вертикали и горизонтали.
	 * @param stretchVertical - растягивать ли панель по вертикали.
	 * @param stretchHorizontal - растягивать ли панель по горизонтали.
	 */
	public EJBoxNoStretch(int layout, boolean stretchVertical, boolean stretchHorizontal) {
		super();
		setLayout(new BoxLayoutNoStretch(this, layout, stretchVertical, stretchHorizontal));
	}

	/**
	 * Враппер над BoxLayout'ом.
	 */
	private static class BoxLayoutNoStretch extends BoxLayout {
		// Флаги растяжения по вертикали и горизонтали.
		private final boolean stretchVertical;
		private final boolean stretchHorizontal;

		/**
		 * Конструктор.
		 * @param stretchVertical - растягивать ли по вертикали.
		 * @param stretchHorizontal - растягивать ли по горизонтали.
		 */
		public BoxLayoutNoStretch(Container target, int axis, boolean stretchVertical, boolean stretchHorizontal) {
			super(target, axis);
			this.stretchVertical = stretchVertical;
			this.stretchHorizontal = stretchHorizontal;
		}

		/**
		 * Оверрайд метода установки максимального размера расширения компонента.
		 * @param target - компонент.
		 * @return - новый максимальный размер компонента.
		 */
		@Override
		public Dimension maximumLayoutSize(Container target) {
			Dimension pref = preferredLayoutSize(target);
			if(stretchVertical)
				pref.height = Integer.MAX_VALUE;
			if(stretchHorizontal)
				pref.width = Integer.MAX_VALUE;
			return pref;
	    }
	}
}
