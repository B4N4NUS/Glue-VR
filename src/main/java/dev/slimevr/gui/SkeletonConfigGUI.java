package dev.slimevr.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.*;
import dev.slimevr.vr.processor.skeleton.HumanSkeleton;
import dev.slimevr.vr.processor.skeleton.SkeletonConfigValue;
import io.eiren.util.StringUtils;
import io.eiren.util.ann.ThreadSafe;

/**
 * Враппер над JPanel с нерастягивающимся GridBagLayout'ом.
 */
public class SkeletonConfigGUI extends JPanel {

	// Сервер.
	private final VRServer server;
	// Главное окно приложения.
	private final VRServerGUI gui;
	// Окно автоматической калибровки скелета.
	private final AutoBoneWindow autoBone;
	// Словарь с связями между костями.
	private Map<SkeletonConfigValue, SkeletonLabel> labels = new HashMap<>();

	private EJBag pane;

	/**
	 * Конструктор.
	 * @param server - сервер.
	 * @param gui - главное окно.
	 */
	public SkeletonConfigGUI(VRServer server, VRServerGUI gui) {
		//super(false, true);
		this.server = server;
		this.gui = gui;
		this.autoBone = new AutoBoneWindow(server, this);

		//setAlignmentY(TOP_ALIGNMENT);
		server.humanPoseProcessor.addSkeletonUpdatedCallback(this::skeletonUpdated);
		// штука снизу возможно важна---------------------------------------------------------------------------------АААААААААААААААААААААААААААААААААААААААААААААААААААААААААААААААААА
		//skeletonUpdated(null);
		add(pane = new EJBag());

		build();
	}

	private void build() {
		pane.removeAll();

		int row = 0;

		pane.add(new TimedResetButton("Reset All"), EJPanel.s(EJPanel.c(1, row, 2), 3, 1));
		row++;

		for (SkeletonConfigValue config : SkeletonConfigValue.values) {
			pane.add(new JLabel(config.label), EJPanel.c(0, row, 2));
			pane.add(new AdjButton("+", config, 0.01f), EJPanel.c(1, row, 2));
			pane.add(new SkeletonLabel(config), EJPanel.c(2, row, 2));
			pane.add(new AdjButton("-", config, -0.01f), EJPanel.c(3, row, 2));

			// Only use a timer on configs that need time to get into position for
			switch (config) {
				case TORSO:
				case LEGS_LENGTH:
					pane.add(new TimedResetButton("Reset", config), EJPanel.c(4, row, 2));
					break;
				default:
					pane.add(new ResetButton("Reset", config), EJPanel.c(4, row, 2));
					break;
			}

			row++;
		}
	}


	// штука снизу возможно важна---------------------------------------------------------------------------------АААААААААААААААААААААААААААААААААААААААААААААААААААААААААААААААААА
	/**
	 * Метод обновления конфигурации скелета и перересовки гуи.
	 * @param newSkeleton - скелет.
	 */
	@ThreadSafe
	public void skeletonUpdated(HumanSkeleton newSkeleton) {
		java.awt.EventQueue.invokeLater(() -> {
			pane.removeAll();

			int row = 0;

			/**
			add(new JCheckBox("Extended pelvis model") {{
				addItemListener(new ItemListener() {
				    @Override
				    public void itemStateChanged(ItemEvent e) {
				        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended pelvis model", true);
				        	}
				        } else {
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended pelvis model", false);
				        	}
				        }
				    }
				});
				if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
	        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
	        		setSelected(hswl.getSkeletonConfigBoolean("Extended pelvis model"));
				}
			}}, s(c(0, row, 2), 3, 1));
			row++;
			//*/
			/*
			add(new JCheckBox("Extended knee model") {{
				addItemListener(new ItemListener() {
				    @Override
				    public void itemStateChanged(ItemEvent e) {
				        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended knee model", true);
				        	}
				        } else {
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended knee model", false);
				        	}
				        }
				    }
				});
				if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
	        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
	        		setSelected(hswl.getSkeletonConfigBoolean("Extended knee model"));
				}
			}}, s(c(0, row, 2), 3, 1));
			row++;
			//*/

			pane.add(new TimedResetButton("Reset All"), EJPanel.s(EJPanel.c(1, row, 2), 3, 1));
//			add(new JButton("Auto") {{
//				addMouseListener(new MouseInputAdapter() {
//					@Override
//					public void mouseClicked(MouseEvent e) {
//						autoBone.setVisible(true);
//						autoBone.toFront();
//					}
//				});
//			}}, s(c(4, row, 2), 3, 1));
			row++;

			for (SkeletonConfigValue config : SkeletonConfigValue.values) {
				pane.add(new JLabel(config.label), EJPanel.c(0, row, 2));
				pane.add(new AdjButton("+", config, 0.01f), EJPanel.c(1, row, 2));
				pane.add(new SkeletonLabel(config), EJPanel.c(2, row, 2));
				pane.add(new AdjButton("-", config, -0.01f), EJPanel.c(3, row, 2));

				// Only use a timer on configs that need time to get into position for
				switch (config) {
				case TORSO:
				case LEGS_LENGTH:
					pane.add(new TimedResetButton("Reset", config), EJPanel.c(4, row, 2));
					break;
				default:
					pane.add(new ResetButton("Reset", config), EJPanel.c(4, row, 2));
					break;
				}

				row++;
			}

			//gui.refresh();
		});
	}

	/**
	 * Обновление значений на JLabl'ах.
	 */
	@ThreadSafe
	public void refreshAll() {
		java.awt.EventQueue.invokeLater(() -> {
			labels.forEach((joint, label) -> {
				label.setText(StringUtils.prettyNumber(server.humanPoseProcessor.getSkeletonConfig(joint) * 100, 0));
			});
		});
	}

	/**
	 * Метод изменения длины кости.
	 * @param joint - сустав.
	 * @param diff - разница.
	 */
	private void change(SkeletonConfigValue joint, float diff) {
		// Update config value
		float current = server.humanPoseProcessor.getSkeletonConfig(joint);
		server.humanPoseProcessor.setSkeletonConfig(joint, current + diff);
		server.humanPoseProcessor.getSkeletonConfig().saveToConfig(server.config);
		server.saveConfig();

		// Update GUI
		labels.get(joint).setText(StringUtils.prettyNumber((current + diff) * 100, 0));
	}

	/**
	 * Метод ресета значения определенного сустава.
	 * @param joint - сустав.
	 */
	private void reset(SkeletonConfigValue joint) {
		// Update config value
		server.humanPoseProcessor.resetSkeletonConfig(joint);
		server.humanPoseProcessor.getSkeletonConfig().saveToConfig(server.config);
		server.saveConfig();

		// Update GUI
		float current = server.humanPoseProcessor.getSkeletonConfig(joint);
		labels.get(joint).setText(StringUtils.prettyNumber((current) * 100, 0));
	}

	/**
	 * Ресет всего скелета.
	 */
	private void resetAll() {
		// Update config value
		server.humanPoseProcessor.resetAllSkeletonConfigs();
		server.humanPoseProcessor.getSkeletonConfig().saveToConfig(server.config);
		server.saveConfig();

		// Update GUI
		refreshAll();
	}

	// Всякие врапперы.
	private class SkeletonLabel extends JLabel {

		public SkeletonLabel(SkeletonConfigValue joint) {
			super(StringUtils.prettyNumber(server.humanPoseProcessor.getSkeletonConfig(joint) * 100, 0));
			labels.put(joint, this);
		}
	}

	private class AdjButton extends JButton {

		public AdjButton(String text, SkeletonConfigValue joint, float diff) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					change(joint, diff);
				}
			});
		}
	}

	private class ResetButton extends JButton {

		public ResetButton(String text, SkeletonConfigValue joint) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					reset(joint);
				}
			});
		}
	}

	private class TimedResetButton extends JButton {

		public TimedResetButton(String text, SkeletonConfigValue joint) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ButtonTimer.runTimer(TimedResetButton.this, 3, text, () -> reset(joint));
				}
			});
		}

		public TimedResetButton(String text) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ButtonTimer.runTimer(TimedResetButton.this, 3, text, () -> resetAll());
				}
			});
		}
	}
}
