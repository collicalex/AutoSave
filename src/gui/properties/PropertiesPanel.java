package gui.properties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import core.copier.Copier;
import core.copier.CopierListener;
import core.properties.Properties;
import core.properties.PropertiesListener;
import core.properties.Property;
import gui.Gui;
import gui.GuiListener;
import gui.utils.GuiUtils;
import gui.utils.VerticalFlowLayout;
import net.miginfocom.swing.MigLayout;

public class PropertiesPanel extends JPanel implements PropertiesListener, GuiListener, ActionListener, CopierListener {

	private static final long serialVersionUID = 1461088403508238922L;

	private Properties _properties;
	
	private JPanel _contentPanel;
	private JTextField _configFile;
	private JButton _backupAllButton;
	private JButton _loadConfigFileButton;
	private JButton _saveConfigFileButton;
	private JButton _newEntryButton;
	private JCheckBox _simulationMode;
	private JPanel _newEntryPanel;

	private JScrollPane _scrollPane;
	private Gui _gui;
	
	public PropertiesPanel(Gui gui) {
		_gui = gui;
		
		JPanel configPanel = createConfigPanel();
		_newEntryPanel = createNewEntryPanel();
		
		int scrollBarWidth = new JScrollPane().getVerticalScrollBar().getPreferredSize().width;
		
		_contentPanel = new JPanel();
		_contentPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT, VerticalFlowLayout.TOP, -scrollBarWidth, 0, true)); //small hack to prevent JScrollBar bug (not correctly compute its own size)
		//_contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
		_contentPanel.add(_newEntryPanel);
		

		_scrollPane = new JScrollPane(_contentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		_scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		


		
		_backupAllButton = new JButton("Backup All");
		_backupAllButton.addActionListener(this);
		
		this.setLayout(new BorderLayout());
		this.add(configPanel, BorderLayout.NORTH);
		this.add(_scrollPane, BorderLayout.CENTER);
		this.add(_backupAllButton, BorderLayout.SOUTH);
		
		loadConfigFile(new File("autosave.ini"));
	}


	//-------------------------------------------------------------------------
	//-- Create Sub Panels
	//-------------------------------------------------------------------------
	
	private JPanel createConfigPanel() {
		_configFile = new JTextField();
		_configFile.setEditable(false);
		_configFile.setBackground(Color.WHITE);
		
		_loadConfigFileButton = new JButton("Load");
		_loadConfigFileButton.addActionListener(this);
		
		_saveConfigFileButton = new JButton("Save");
		_saveConfigFileButton.addActionListener(this);
		
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		buttonsPanel.add(_loadConfigFileButton);
		buttonsPanel.add(_saveConfigFileButton);
		
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
		
		_simulationMode.setBorder(new EmptyBorder(0, 0, 5, 0));
		simuPanel.add(_simulationMode, BorderLayout.CENTER);
		configPanel.add(simuPanel, BorderLayout.NORTH);

		configPanel.setBorder(new EmptyBorder(5, 5, 10, 5));
		
		return configPanel;
	}
	
	private JPanel createNewEntryPanel() {
		JPanel panel = new JPanel(new MigLayout("fillx", "[][grow, fill][fill]", "[]0[]"));
		panel.add(GuiUtils.setBold(new JLabel("Add a backup entry")));
		_newEntryButton = new JButton("+");
		_newEntryButton.addActionListener(this);
		panel.add(_newEntryButton, "skip 1, wrap");
		return panel;
	}
	
	
	//-------------------------------------------------------------------------
	//-- Load / Save config
	//-------------------------------------------------------------------------
	
	private JFileChooser getFileChooser() {
		JFileChooser chooser = new JFileChooser(); 
		if (_configFile.getText().trim().isEmpty() == false) {
			chooser.setSelectedFile(new File(_configFile.getText()));
		} else {
			chooser.setSelectedFile(new File("./"));
		}
	    chooser.setDialogTitle("Select configuration file");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Configuration file (*.ini)", "ini");
	    chooser.setFileFilter(filter);
	    chooser.setAcceptAllFileFilterUsed(false);
	    return chooser;
	}
	
	private void loadConfigFile(File file) {
		Properties properties = new Properties();
		properties.addListener(this);
		
		if (file.exists()) {
			try {
				properties.read(file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
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
	
	
	private void loadConfigFile() {
		JFileChooser chooser = getFileChooser();
	    if (chooser.showOpenDialog(GuiUtils.getOldestParent(_loadConfigFileButton)) == JFileChooser.APPROVE_OPTION) {
	    	loadConfigFile(chooser.getSelectedFile());
	    }
	}
	
	private void saveConfigFile() {
		JFileChooser chooser = getFileChooser();
	    if (chooser.showSaveDialog(GuiUtils.getOldestParent(_saveConfigFileButton)) == JFileChooser.APPROVE_OPTION) {
    		saveConfigFile(chooser.getSelectedFile());
	    }
	}
	
	
	//-------------------------------------------------------------------------
	//-- Others
	//-------------------------------------------------------------------------
	
	private boolean isSaveNeeded() {
		if (_properties != null) {
			if (_properties.needSave()) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isSimulationModeChecked() {
		return _simulationMode.isSelected();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		for (Component cmp : _contentPanel.getComponents()) {
			if (cmp instanceof PropertyPanel) {
				PropertyPanel pp = (PropertyPanel) cmp;
				pp.setEnabled(enabled);
			}
		}
		
		_backupAllButton.setEnabled(enabled);
		_loadConfigFileButton.setEnabled(enabled);
		_saveConfigFileButton.setEnabled(enabled);
		_newEntryButton.setEnabled(enabled);
		_simulationMode.setEnabled(enabled);
	}
	
	public void removeProperty(Property property) {
		_properties.removeProperty(property);
	}
	
	private void runBackup() {
		setEnabled(false);
		Copier copier = new Copier();
		copier.addListener(this);
		_gui.listenCopier(copier, _properties);
		
		for (Component cmp : _contentPanel.getComponents()) {
			if (cmp instanceof PropertyPanel) {
				PropertyPanel pp = (PropertyPanel) cmp;
				copier.addListener(pp);
				pp.initProgressBar();
			}
		}
		
		(new Thread() {
			public void run() {
				try {
					copier.copy(_gui, _properties, isSimulationModeChecked());
					JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_contentPanel), _nbFilesCopied + " files had been copied", "Copy finish", JOptionPane.INFORMATION_MESSAGE);
					setEnabled(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_contentPanel), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					setEnabled(true);
				}		
			}
		}).start();
	}
	
	//-------------------------------------------------------------------------
	//-- GuiListener
	//-------------------------------------------------------------------------		
	
	@Override
	public void applicationClosing() {
		if (isSaveNeeded()) {
			int result = JOptionPane.showConfirmDialog (GuiUtils.getOldestParent(this), "Configuiration have changed!\nDo you want to save it?", "Warning", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				JFileChooser chooser = getFileChooser();
			    if (chooser.showSaveDialog(GuiUtils.getOldestParent(_saveConfigFileButton)) == JFileChooser.APPROVE_OPTION) {
		    		saveConfigFile(chooser.getSelectedFile());
			    }				
			}
		}
	}
	
	//-------------------------------------------------------------------------
	//-- PropertiesListener
	//-------------------------------------------------------------------------			

	@Override
	public void propertiesLoad(Properties properties, File file) {
		if (_properties != null) {
			_properties.removeListener(this);
		}
		_configFile.setText(file.getAbsolutePath());
		_contentPanel.removeAll();
		_properties = properties;
	}

	@Override
	public void propertiesAddProperty(Properties properties, Property property) {
		PropertyPanel pp = new PropertyPanel(_gui, this, property);
		
		int vBorderGap = 10;
		Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 0, vBorderGap, 0),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
						BorderFactory.createEmptyBorder(0, 0, vBorderGap, 0)
				)
		);
		pp.setBorder(border);
		
		_contentPanel.remove(_newEntryPanel);
		_contentPanel.add(pp);
		_contentPanel.add(_newEntryPanel);
		_contentPanel.revalidate();
	}

	@Override
	public void propertiesRemoveProperty(Properties properties, Property property) {
		for (Component cmp : _contentPanel.getComponents()) {
			if (cmp instanceof PropertyPanel) {
				PropertyPanel pp = (PropertyPanel) cmp;
				if (pp.isProperty(property)) {
					System.out.println(pp);
					pp.stopListeningPropertyChange();
					_contentPanel.remove(pp);
					
					//_contentPanel.setPreferredSize(new Dimension(1, 1000));
					
					_contentPanel.revalidate();
					break ;
				}
			}
		}
	}

	@Override
	public void propertiesSave(Properties properties, File file) {
		_configFile.setText(file.getAbsolutePath());
	}


	//-------------------------------------------------------------------------
	//-- ActionListener
	//-------------------------------------------------------------------------			
		
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _loadConfigFileButton) {
			loadConfigFile();
		} else if (e.getSource() == _saveConfigFileButton) {
			saveConfigFile();
		} else if (e.getSource() == _newEntryButton) {
			_properties.addProperty(new Property());
		} else if (e.getSource() == _backupAllButton) {
			runBackup();
		}
	}


	//-------------------------------------------------------------------------
	//-- CopierListener
	//-------------------------------------------------------------------------		
	
	private long _nbFilesCopied = 0;
	
	@Override
	public void copierCountStart(Object sourceEvent) {
	}


	@Override
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
	}


	@Override
	public void copierCopyStart(Object sourceEvent) {
		if (sourceEvent == _properties) {
			_nbFilesCopied = 0;
		}
	}


	@Override
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath) {
		_nbFilesCopied++;
	}


	@Override
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
	}


	@Override
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
		_nbFilesCopied++;
	}


	@Override
	public void copierCopyFinish(Object sourceEvent) {
	}

	
	@Override
	public void fileTransferStart(String path, long sizeToTransfert) {
	}

	@Override
	public void fileTransferPart(long sizeTransmitted) {
	}

	@Override
	public void fileTransferFinish() {
	}	
}
