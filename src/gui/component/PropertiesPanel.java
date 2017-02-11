package gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import core.EncryptionUI;
import core.Logger;
import core.Properties;
import core.PropertiesListener;
import core.Property;
import gui.GuiListener;
import gui.GuiUtils;
import net.miginfocom.swing.MigLayout;

public class PropertiesPanel extends JPanel implements PropertiesListener, GuiListener, EncryptionUI {

	private static final long serialVersionUID = 1461088403508238922L;

	private Properties _properties;
	private JPanel _contentPanel;
	private List<PropertyPanel> _propertyPanelList;
	private JTextField _configFile;
	private Logger _logger;
	private JButton _backupAllButton;
	private JButton _loadConfigFile;
	private JButton _saveConfigFile;
	private JButton _newEntryButton;
	private JCheckBox _simulationMode;
	
	private List<PropertiesPanelListener> _listeners;
	
	private File _default;
	
	private JScrollPane _scrollPane;
	
	public PropertiesPanel(Logger logger) {
		_default = new File("autosave.ini");
		_logger = logger;
		
		JPanel configPanel = createConfigPanel();
		_listeners = new LinkedList<PropertiesPanelListener>();
		
		_contentPanel = new JPanel();
		_contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));

		_scrollPane = new JScrollPane(_contentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		_scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		
		_backupAllButton = new JButton("Backup All");
		_backupAllButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				(new Thread() {
					public void run() {
						_properties.backup();
					}
				}).start();
			}
		});
		
		
		
		this.setLayout(new BorderLayout());
		this.add(configPanel, BorderLayout.NORTH);
		this.add(_scrollPane, BorderLayout.CENTER);
		this.add(_backupAllButton, BorderLayout.SOUTH);
		
		setProperties(new Properties(this));
		loadConfigFile(_default);
		
		this.setPreferredSize(new Dimension(400,600));
	}

	
	//-------------------------------------------------------------------------
	//-- Listeners
	//-------------------------------------------------------------------------
	
	public void addListener(PropertiesPanelListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
			listener.loadNewProperties(null, _properties);
		}
	}
	
	public void removeListener(PropertiesPanelListener listener) {
		_listeners.remove(listener);
	}
	
	private void notifyListerners_propertyUpdate(Properties oldProperties, Properties newProperties) {
		for (PropertiesPanelListener listener : _listeners) {
			listener.loadNewProperties(oldProperties, newProperties);
		}
	}	
	
	//-------------------------------------------------------------------------
	//-- Others
	//-------------------------------------------------------------------------
	
	private JPanel createConfigPanel() {
		_configFile = new JTextField();
		_configFile.setEditable(false);
		_configFile.setBackground(Color.WHITE);
		
		_loadConfigFile = new JButton("Load");
		_loadConfigFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = getFileChooser();
			    if (chooser.showOpenDialog(GuiUtils.getOldestParent(_loadConfigFile)) == JFileChooser.APPROVE_OPTION) {
			    	loadConfigFile(chooser.getSelectedFile());
			    }
			}
		});
		
		_saveConfigFile = new JButton("Save");
		_saveConfigFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = getFileChooser();
			    if (chooser.showSaveDialog(GuiUtils.getOldestParent(_saveConfigFile)) == JFileChooser.APPROVE_OPTION) {
		    		saveConfigFile(chooser.getSelectedFile());
			    }
			}
		});
		
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		buttonsPanel.add(_loadConfigFile);
		buttonsPanel.add(_saveConfigFile);
		
		JLabel label = new JLabel("Config File");
		label.setPreferredSize(new Dimension(75, 1));
		GuiUtils.setBold(label);
		JPanel configPanel = new JPanel(new BorderLayout());
		configPanel.add(label, BorderLayout.WEST);
		configPanel.add(_configFile, BorderLayout.CENTER);
		configPanel.add(buttonsPanel, BorderLayout.EAST);
		
		JLabel label2 = new JLabel("Simulation");
		label2.setPreferredSize(new Dimension(75, 1));
		GuiUtils.setBold(label2);
		JPanel simuPanel = new JPanel(new BorderLayout());
		simuPanel.add(label2, BorderLayout.WEST);
		_simulationMode = new JCheckBox();
		_simulationMode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				_properties.isSimulationOnly(_simulationMode.isSelected());			
			}
		});
		
		_simulationMode.setBorder(new EmptyBorder(0, 0, 5, 0));
		simuPanel.add(_simulationMode, BorderLayout.CENTER);
		configPanel.add(simuPanel, BorderLayout.NORTH);

		configPanel.setBorder(new EmptyBorder(5, 5, 10, 5));
		
		return configPanel;
	}
	
	private JFileChooser getFileChooser() {
		JFileChooser chooser = new JFileChooser(); 
		if (_configFile.getText().trim().isEmpty() == false) {
			chooser.setSelectedFile(new File(_configFile.getText()));
		} else {
			chooser.setSelectedFile(_default);
		}
	    chooser.setDialogTitle("Select configuration file");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Configuration file (*.ini)", "ini");
	    chooser.setFileFilter(filter);
	    chooser.setAcceptAllFileFilterUsed(false);
	    return chooser;
	}
	
	private void loadConfigFile(File file) {
		Properties properties = new Properties(this);
		
		if (file.exists()) {
			try {
				properties.read(file);
				propertiesLoad(properties, file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
		//setProperties(properties);
	}
	
	private void saveConfigFile(File file) {
		if (file.exists()) {
			int dialogResult = JOptionPane.showConfirmDialog(GuiUtils.getOldestParent(this), "File " + file.getAbsolutePath() + "\nalready exists.\n\nAre you sure you want to overwrite it?", "Warning", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.NO_OPTION){
				return ;
			}
		}
		
		try {
			_properties.save(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void setProperties(Properties properties) {
		if (properties != _properties) {
			notifyListerners_propertyUpdate(_properties, properties);
			
			if (_properties != null) {
				_properties.removeListener(this);
				_properties.setLogger(null);
			}
	
			_properties = properties;
			if (_properties != null) {
				_properties.addListener(this);
				_properties.setLogger(_logger);
			}
			
			rebuildPanel();
		}
	}
	
	private void rebuildPanel() {
		_contentPanel.removeAll();
		_propertyPanelList = new LinkedList<PropertyPanel>();
		int height = 0;
		
		if (_properties != null) {
			List<JComponent> cmps = new LinkedList<JComponent>();
			for (int i = 0; i < _properties.size(); ++i) {
				PropertyPanel pp = new PropertyPanel(_properties.get(i));
				cmps.add(pp);
				_propertyPanelList.add(pp);
				height += pp.getPreferredSize().height;
			}
			JPanel nep = createNewEntryPanel();
			cmps.add(nep);
			height += nep.getPreferredSize().height;
			
			_contentPanel.add(GuiUtils.stackNorth(cmps));
		}
		
		_contentPanel.setPreferredSize(new Dimension(1, height)); //Hack because ScrollPanelLayout is bugged and can't compute this height alone!
		
		_contentPanel.revalidate();
	}
	

	private JPanel createNewEntryPanel() {
		JPanel panel = new JPanel(new MigLayout("fillx", "[][grow, fill][fill]", "[]0[]"));
		panel.add(GuiUtils.setBold(new JLabel("Add a backup entry")));
		
		
		_newEntryButton = new JButton("+");
		_newEntryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Property property = new Property(_properties);
				_properties.addProperty(property);
			}
		});
		
		panel.add(_newEntryButton, "skip 1, wrap");

		return panel;
	}
	
	private boolean isSaveNeeded() {
		if (_properties != null) {
			if (_properties.needSave()) {
				return true;
			}
		}
		return false;
	}
	
	private void updateSaveButton() {
		_saveConfigFile.setForeground(isSaveNeeded() ? Color.RED : Color.BLACK);
	}
	
	public boolean isSimulationModeChecked() {
		return _simulationMode.isSelected();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (PropertyPanel pp : _propertyPanelList) {
			pp.setEnabled(enabled);
		}
		_backupAllButton.setEnabled(enabled);
		_loadConfigFile.setEnabled(enabled);
		_saveConfigFile.setEnabled(enabled);
		_newEntryButton.setEnabled(enabled);
		_simulationMode.setEnabled(enabled);
	}
	
	@Override
	public void propertiesLoad(Properties properties, File file) {
		_configFile.setText(file.getAbsolutePath());
		setProperties(properties);
	}
	
	@Override
	public void propertiesSave(Properties properties, File file) {
		_configFile.setText(file.getAbsolutePath());
		setProperties(properties); //TODO?
		updateSaveButton();
	}

	@Override
	public void propertiesAddProperty(Properties properties, Property property) {
		rebuildPanel();
		updateSaveButton();
	}

	@Override
	public void propertiesRemProperty(Properties properties, Property property) {
		rebuildPanel();
		updateSaveButton();
	}
	
	@Override
	public void propertiesUpdateProperty(Properties properties, Property property) {
		updateSaveButton();
	}

	
	@Override
	public void ioOperationStart(Properties properties) {
		setEnabled(false);
	}

	@Override
	public void ioOperationEnd(Properties properties) {
		setEnabled(true);
	}

	@Override
	public void ioOperationCountSrcFilesDone(Properties properties) {
	}
	
	@Override
	public void ioOperationOneFileProcessed(Properties properties) {
	}

	@Override
	public void ioOperationOneFileNew(Properties properties) {
	}	

	@Override
	public void applicationClosing() {
		if (isSaveNeeded()) {
			int result = JOptionPane.showConfirmDialog (GuiUtils.getOldestParent(this), "Configuiration have changed!\nDo you want to save it?", "Warning", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				JFileChooser chooser = getFileChooser();
			    if (chooser.showSaveDialog(GuiUtils.getOldestParent(_saveConfigFile)) == JFileChooser.APPROVE_OPTION) {
		    		saveConfigFile(chooser.getSelectedFile());
			    }				
			}
		}
	}
	
	@Override
	public String askEncryptionKey() {
		return JPasswordDialog.askPassword(GuiUtils.getOldestParent(this));
	}
}
