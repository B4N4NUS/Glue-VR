package dev.slimevr.gui.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;

/**
 * Враппед над EJPanel с кастомным лейаутом.
 */
public class EJBagNoStretch extends EJPanel {

	/**
	 * Конструктор с инициализацией флагов о растягивании по вертикали и горизонтали.
	 * @param stretchVertical - растягивать ли панель по вертикали.
	 * @param stretchHorizontal - растягивать ли панель по горизонтали.
	 */
	public EJBagNoStretch(boolean stretchVertical, boolean stretchHorizontal) {
		super(new EGridBagLayoutNoStretch(stretchVertical, stretchHorizontal));
	}

	/**
	 * Враппер над GridBagLayout'ом.
	 */
	private static class EGridBagLayoutNoStretch extends GridBagLayout {
		// Флаги растяжения по вертикали и горизонтали.
		private final boolean stretchVertical;
		private final boolean stretchHorizontal;

		/**
		 * Конструктор.
		 * @param stretchVertical - растягивать ли по вертикали.
		 * @param stretchHorizontal - растягивать ли по горизонтали.
		 */
		public EGridBagLayoutNoStretch(boolean stretchVertical, boolean stretchHorizontal) {
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
