package dev.slimevr.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.util.LoggingFacade;
import dev.slimevr.VRServer;
import dev.slimevr.gui.VRServerGUI;
import dev.slimevr.gui.swing.EJPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class UISettings extends JFrame {
	private VRServer server;
	private VRServerGUI gui;
	private JList<FlatAllIJThemes.FlatIJLookAndFeelInfo> themeList;
	public static FlatAllIJThemes.FlatIJLookAndFeelInfo oldSel;

	private JCheckBox sound;
	private JCheckBox showSteamTrackerSelection;
	private JButton guiZoom;

	public UISettings(VRServerGUI gui, VRServer server) {
		this.server = server;
		this.gui = gui;

		setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		setTitle("UI Settings");
		//setPreferredSize(new Dimension(100,100));
		build();
		setVisible(true);
		//IJThemesPanel themes = new IJThemesPanel();
		setAlwaysOnTop(true);
	}

	public void build() {
		setLayout(new GridBagLayout());

		themeList = new JList<>(FlatAllIJThemes.INFOS);
		//themeList.setPreferredSize(new Dimension(100,100));
		themeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(themeList);
		themeList.setLayoutOrientation(JList.VERTICAL);
		themeList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
														  int index, boolean isSelected, boolean cellHasFocus) {
				String name = ((FlatAllIJThemes.FlatIJLookAndFeelInfo) value).getName();
				int sep = name.indexOf('/');
				if (sep >= 0)
					name = name.substring(sep + 1).trim();

				JComponent c = (JComponent) super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
				c.setToolTipText(buildToolTip((FlatAllIJThemes.FlatIJLookAndFeelInfo) value));
				return c;
			}

			private String buildToolTip(FlatAllIJThemes.FlatIJLookAndFeelInfo ti) {
				return "Name: " + ti.getName();
			}
		});
		themeList.setModel(new AbstractListModel<FlatAllIJThemes.FlatIJLookAndFeelInfo>() {
			private static final long serialVersionUID = 1536029084261517876L;

			@Override
			public int getSize() {
				return FlatAllIJThemes.INFOS.length;
			}

			@Override
			public FlatAllIJThemes.FlatIJLookAndFeelInfo getElementAt(int index) {
				return FlatAllIJThemes.INFOS[index];
			}
		});

		if (oldSel != null) {
			for (int i = 0; i < FlatAllIJThemes.INFOS.length; i++) {
				FlatAllIJThemes.FlatIJLookAndFeelInfo theme = FlatAllIJThemes.INFOS[i];
				if (oldSel.getName().equals(theme.getName())) {
					themeList.setSelectedIndex(i);
					break;
				}
			}
			// select first theme if none selected
			if (themeList.getSelectedIndex() < 0)
				themeList.setSelectedIndex(0);
		}
		// scroll selection into visible area
		int sel = themeList.getSelectedIndex();
		if (sel >= 0) {
			Rectangle bounds = themeList.getCellBounds(sel, sel);
			if (bounds != null)
				themeList.scrollRectToVisible(bounds);
		}

		themeList.addListSelectionListener(this::themesListValueChanged);
		add(new JLabel("Themes:"), EJPanel.k(2,0,GridBagConstraints.BOTH, GridBagConstraints.WEST, 0.1,0.1, 1,1));
		//add(themeList, EJPanel.k(1,1,GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 1,1, 3,1));
		add(Box.createHorizontalStrut(50),  EJPanel.k(1,0,GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1,1, 6,1));

		JScrollPane themesPane;
		add(sound = new JCheckBox("Enable sound notification"), EJPanel.k(0,0,GridBagConstraints.BOTH,GridBagConstraints.WEST,0,0,1,1));
		add(themesPane = new JScrollPane(themeList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), EJPanel.k(2,1,GridBagConstraints.BOTH, GridBagConstraints.WEST, 1,1, 5,1));
		themesPane.setMinimumSize(new Dimension(200,200));

		pack();
	}

	private void themesListValueChanged(ListSelectionEvent e) {
		FlatAllIJThemes.FlatIJLookAndFeelInfo themeInfo = themeList.getSelectedValue();

		if (e.getValueIsAdjusting())
			return;

		EventQueue.invokeLater(() -> {
			setTheme(themeInfo);
			oldSel = themeList.getSelectedValue();
		});
	}

	private void setTheme(FlatAllIJThemes.FlatIJLookAndFeelInfo themeInfo) {
		try {
			UIManager.setLookAndFeel(themeInfo.getClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		FlatLaf.updateUI();
		FlatLaf.repaintAllFramesAndDialogs();
		repaint();
	}
}
