package dev.slimevr.gui.swing;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;

public class ButtonTimer {

	private static Timer timer = new Timer();

	/**
	 * Метод обратного отсчета.
	 * @param button - привязанная кнопка.
	 * @param seconds - количество секунд.
	 * @param defaultText - текст кнопки.
	 * @param runnable - задача.
	 */
	public static void runTimer(AbstractButton button, int seconds, String defaultText, Runnable runnable) {
		if(seconds <= 0) {
			button.setText(defaultText);
			runnable.run();
		} else {
			button.setText(String.valueOf(seconds));
			timer.schedule(new ButtonTimerTask(button, seconds - 1, defaultText, runnable), 1000);
		}
	}

	/**
	 * Поточный таймер
	 */
	private static class ButtonTimerTask extends TimerTask {
		// Привязанная кнопка.
		private final AbstractButton button;
		// Пройденные секунды.
		private final int seconds;
		// Текст кнопки.
		private final String defaultText;
		// Поток.
		private final Runnable runnable;

		/**
		 * Конструктор класса задачи кнопки-таймера.
		 * @param button - привязанная кнопка.
		 * @param seconds - сколько секунд отсчитывать.
		 * @param defaultText - текст кнопки.
		 * @param runnable - поток.
		 */
		private ButtonTimerTask(AbstractButton button, int seconds, String defaultText, Runnable runnable) {
			this.button = button;
			this.seconds = seconds;
			this.defaultText = defaultText;
			this.runnable = runnable;
		}

		/**
		 * Поточный метод работы с таймером.
		 */
		@Override
		public void run() {
			runTimer(button, seconds, defaultText, runnable);
		}
		
	}
}
