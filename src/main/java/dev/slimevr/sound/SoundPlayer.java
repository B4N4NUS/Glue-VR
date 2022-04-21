package dev.slimevr.sound;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.util.Objects;

public class SoundPlayer {
	/**
	 * Метод, позволяющий запускать в отдельном потоке аудио формата .wav
	 * @param url - имя аудиофайла в папке sounds
	 */
	public static synchronized void playSound(final String url) {
		new Thread(() -> {
			try {
				String sound = url+".wav";
				// Делаем немного javaмагии для создания звукогого потока.
				Clip clip = AudioSystem.getClip();
				AudioInputStream inputStream = AudioSystem.getAudioInputStream(
						Objects.requireNonNull(SoundPlayer.class.getResourceAsStream(sound)));
				clip.open(inputStream);
				clip.start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}
}
