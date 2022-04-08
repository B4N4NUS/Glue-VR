package dev.slimevr.gui;


import java.awt.Container;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import com.formdev.flatlaf.FlatDarculaLaf;
import dev.slimevr.gui.swing.EJBox;
import dev.slimevr.vr.trackers.CalibratingTracker;
import dev.slimevr.vr.trackers.Tracker;
import io.eiren.util.ann.AWTThread;

/**
 * Окно калибровки скелета.
 */
public class CalibrationWindow extends JFrame {

	public final Tracker tracker;
	private JTextArea currentCalibration;
	private JTextArea newCalibration;
	private JButton calibrateButton;

	/**
	 * Конструктор.
	 * @param t - трекер.
	 */
	public CalibrationWindow(Tracker t) {
		super(t.getName() + " calibration");
		this.tracker = t;
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
		
		build();
	}

	/**
	 * Получение нынешнего состояния калибровки.
	 * @param str
	 */
	public void currentCalibrationRecieved(String str) {
		java.awt.EventQueue.invokeLater(() -> {
			currentCalibration.setText(str);
			pack();
		});
	}

	/**
	 * Получение нового состояния калибровки.
	 * @param str
	 */
	public void newCalibrationRecieved(String str) {
		java.awt.EventQueue.invokeLater(() -> {
			calibrateButton.setText("Calibrate");
			newCalibration.setText(str);
			pack();
		});
	}

	/**
	 * Отрисовка гуи.
	 */
	@AWTThread
	private void build() {
		Container pane = getContentPane();
		
		pane.add(calibrateButton = new JButton("Calibrate") {{
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					calibrateButton.setText("Calibrating...");
					((CalibratingTracker) tracker).startCalibration(CalibrationWindow.this::newCalibrationRecieved);
				}
			});
		}});

		pane.add(new EJBox(BoxLayout.PAGE_AXIS) {{
			setBorder(new EmptyBorder(i(5)));
			add(new JLabel("Current calibration"));
			add(currentCalibration = new JTextArea(10, 25));
			
			((CalibratingTracker) tracker).requestCalibrationData(CalibrationWindow.this::currentCalibrationRecieved);
		}});
		pane.add(new EJBox(BoxLayout.PAGE_AXIS) {{
			setBorder(new EmptyBorder(i(5)));
			add(new JLabel("New calibration"));
			add(newCalibration = new JTextArea(10, 25));
		}});
		
		
		// Pack and display
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				toFront();
				repaint();
			}
		});
	}
}
