package dev.slimevr.gui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputAdapter;

import com.fazecast.jSerialComm.SerialPort;

import com.formdev.flatlaf.FlatDarculaLaf;
import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.EJBox;
import io.eiren.util.ann.AWTThread;

/**
 * Окно вайфая.
 */
public class WiFiWindow extends JFrame {
	// Таймер.
	private static final Timer timer = new Timer();
	// Параметры подключения к маршрутизатору.
	private static String savedSSID = "";
	private static String savedPassword = "";
	// Гуишные поля с информацией о подключении к маршрутизатору.
	JTextField ssidField;
	JTextField passwdField;
	// Порт с трекерами.
	SerialPort trackerPort = null;
	// Логи.
	JTextArea log;
	// Таск для таймера.
	TimerTask readTask;
	VRServer server;

	public static boolean stillLiving = true;

	/**
	 * Конструктор
	 * @param gui - главное окно приложения.
	 */
	public WiFiWindow(VRServerGUI gui, VRServer server) {
		super("WiFi Settings");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
		this.server = server;
		build();
		setAlwaysOnTop(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		GraphicsConfiguration gc = getGraphicsConfiguration();
		Rectangle screenBounds = gc.getBounds();
		// Минимальный размер.
		setMinimumSize(new Dimension(100, 100));
		// Установка размера окна.
		setSize(Math.min(server.config.getInt("wifi.width", getWidth()), screenBounds.width), Math.min(server.config.getInt("wifi.height", getHeight()), screenBounds.height));
		// Установка расположения окна на экране.
		setLocation(server.config.getInt("wifi.posx", getLocation().x), screenBounds.y + server.config.getInt("wifi.posy", getLocation().y));
		addComponentListener(new AbstractComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				saveFrameInfo();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				saveFrameInfo();
			}
		});
		setIconImages(gui.getIconImages());
	}
	protected void saveFrameInfo() {
		Rectangle b = getBounds();
		server.config.setProperty("wifi.width", b.width);
		server.config.setProperty("wifi.height", b.height);
		server.config.setProperty("wifi.posx", b.x);
		server.config.setProperty("wifi.posy", b.y);
		server.saveConfig();
	}
	@Override
	public void dispose() {
		super.dispose();
		stillLiving = false;
	}

	/**
	 * Отрисовка гуи.
	 */
	@AWTThread
	private void build() {
		Container pane = getContentPane();

		// Получение comm портов.
		SerialPort[] ports = SerialPort.getCommPorts();
		for(SerialPort port : ports) {
			// Ищем открытые порты для подключения к трекерам.
			if(port.getDescriptivePortName().toLowerCase().contains("ch340") || port.getDescriptivePortName().toLowerCase().contains("cp21") || port.getDescriptivePortName().toLowerCase().contains("ch910")) {
				trackerPort = port;
				break;
			}
		}
		pane.add(new EJBox(BoxLayout.PAGE_AXIS) {{
			if(trackerPort == null) {
				add(new JLabel("No trackers connected, connect tracker to USB and reopen window"));
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						WiFiWindow.this.dispose();
					}
				}, 5000);
			} else {
				add(new JLabel("Tracker connected to " + trackerPort.getSystemPortName() + " (" + trackerPort.getDescriptivePortName() + ")"));
				JScrollPane scroll;
				add(scroll = new JScrollPane(log = new JTextArea(10, 20), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
				log.setLineWrap(true);
				scroll.setAutoscrolls(true);
				if(trackerPort.openPort()) {
					trackerPort.setBaudRate(115200);
					log.append("[OK] Port opened\n");
					readTask = new ReadTask();
					timer.schedule(readTask, 500, 500);
				} else {
					log.append("ERROR: Can't open port");
				}
				
				add(new JLabel("Enter WiFi credentials:"));
				add(new EJBox(BoxLayout.LINE_AXIS) {{
					add(new JLabel("Network name:"));
					add(ssidField = new JTextField(savedSSID));
				}});
				add(new EJBox(BoxLayout.LINE_AXIS) {{
					add(new JLabel("Network password:"));
					add(passwdField = new JTextField(savedPassword));
				}});
				add(new JButton("Send") {{
					addMouseListener(new MouseInputAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							send(ssidField.getText(), passwdField.getText());
						}
					});
				}});
			}
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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent windowEvent) {
	            if(trackerPort != null)
	            	trackerPort.closePort();
	            if(readTask != null)
	            	readTask.cancel();
	            System.out.println("Port closed okay");
	            dispose();
	        }
	    });
	}

	/**
	 * Отправка информации о подключении к маршрутизатору на подключенный трекер.
	 * @param ssid - имя сети.
	 * @param passwd - пароль.
	 */
	protected void send(String ssid, String passwd) {
		savedSSID = ssid;
		savedPassword = passwd;
		OutputStream os = trackerPort.getOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(os);
		try {
			writer.append("SET WIFI \"" + ssid + "\" \"" + passwd + "\"\n");
			writer.flush();
		} catch(IOException e) {
			log.append(e.toString() + "\n");
			e.printStackTrace();
		}
	}
	
	private class ReadTask extends TimerTask {
		
		final InputStream is;
		final Reader reader;
		StringBuffer sb = new StringBuffer();
		
		public ReadTask() {
			is = trackerPort.getInputStreamWithSuppressedTimeoutExceptions();
			reader = new InputStreamReader(is);
		}

		@Override
		public void run() {
			try {
				while(reader.ready())
					sb.appendCodePoint(reader.read());
				if(sb.length() > 0)
					log.append(sb.toString());
				sb.setLength(0);
			} catch(Exception e) {
				log.append(e.toString() + "\n");
				e.printStackTrace();
			}
		}
		
	}
}
