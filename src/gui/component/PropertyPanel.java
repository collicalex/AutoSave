package gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import core.Property;
import core.PropertyListener;
import gui.GuiUtils;
import net.miginfocom.swing.MigLayout;

public class PropertyPanel extends JPanel implements PropertyListener, JTextField2Listener {
	
	private static final long serialVersionUID = -7782988719016798420L;
	
	private Property _property;
	
	private JLabel _entry;
	
	private JTextField2 _src;
	private JTextField2 _dst;
	private JComboBox<Boolean> _recur;
	private DefaultListModel<String> _ignoredListModel;
	private JList<String> _ignoredList;
	private JComboBox<Boolean> _crypt;
	
	private JButton _deleteButton;
	private JButton _srcBrowseButton;
	private JButton _dstBrowseButton;
	private JButton _backupButton;
	private JButton _addToIgnoreListButton;
	private JButton _removeFromIgnoreListButton;
	
	private JButton _restoreButton;
	
	private JProgressBar _progressBar;
	
	private boolean _listen = true;
	
	public PropertyPanel(Property property) {
		this.setBorder(new EmptyBorder(5, 5, 10, 5));
		
		_src = new JTextField2(this);
		_dst = new JTextField2(this);
		_recur = new JComboBox<Boolean>(new Boolean[]{true, false});
		_recur.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				_listen = false;
				_property.setRecursive((Boolean)_recur.getSelectedItem());
				_listen = true;
			}
		});
		_recur.setRenderer(new YesNoBooleanCellRenderer());
		
		_crypt = new JComboBox<Boolean>(new Boolean[]{true, false});
		_crypt.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				_listen = false;
				_property.setEncryption((Boolean)_crypt.getSelectedItem());
				_listen = true;
			}
		});
		_crypt.setRenderer(new YesNoBooleanCellRenderer());
		
		
		_deleteButton = new JButton("X");
		_deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int dialogResult = JOptionPane.showConfirmDialog(GuiUtils.getOldestParent(_deleteButton), "Are you sure you want to remove this entry from the configuration file?", "Delete", JOptionPane.YES_NO_OPTION);
				if(dialogResult == 0) {
					_property.deleteMe();
				}
			}
		});
		
		_srcBrowseButton = new JButton("...");
		_srcBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new File(_src.getText()));
			    chooser.setDialogTitle("Select source directory");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showOpenDialog(GuiUtils.getOldestParent(_srcBrowseButton)) == JFileChooser.APPROVE_OPTION) { 
			    	_src.setText(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
		});		
		
		
		_dstBrowseButton = new JButton("...");
		_dstBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new File(_src.getText()));
			    chooser.setDialogTitle("Select destination directory");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showOpenDialog(GuiUtils.getOldestParent(_dstBrowseButton)) == JFileChooser.APPROVE_OPTION) { 
			    	_dst.setText(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
		});	
		
		_backupButton = new JButton("Backup");
		_backupButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				(new Thread() {
					public void run() {
						_property.backup();
					}
				}).start();
			}
		});
		
		_restoreButton = new JButton("Restore");
		_backupButton.setPreferredSize(_restoreButton.getPreferredSize());

		_progressBar = new JProgressBar();
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		
		_entry = new JLabel("Entry:");
		
		_ignoredListModel = new DefaultListModel<String>();
		
		_ignoredList = new JList<String>(_ignoredListModel);
		_ignoredList.setVisibleRowCount(1);
		
		_addToIgnoreListButton = new JButton("+");
		_removeFromIgnoreListButton = new JButton("-");
		
		_addToIgnoreListButton.setPreferredSize(_srcBrowseButton.getPreferredSize());
		_removeFromIgnoreListButton.setPreferredSize(_srcBrowseButton.getPreferredSize());
		
		_addToIgnoreListButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new File(_src.getText()));
			    chooser.setDialogTitle("Select ignoring directory or file");
			    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showOpenDialog(GuiUtils.getOldestParent(_addToIgnoreListButton)) == JFileChooser.APPROVE_OPTION) {
			    	
			    	boolean showWarningMessage = false;
			    	if (_recur.getSelectedItem() == Boolean.FALSE) {
			    		showWarningMessage = (chooser.getSelectedFile().getParentFile().compareTo(new File(_src.getText())) != 0);
			    	} else {
			    		showWarningMessage = (isChild(_src.getText(), chooser.getSelectedFile().getAbsolutePath()) == false);
			    	}
			    	
			    	if (showWarningMessage == true) {
			    		if (chooser.getSelectedFile().isDirectory()) {
			    			JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_addToIgnoreListButton), "Selected directory is not a sub directory of source one!", "Warning", JOptionPane.WARNING_MESSAGE);
			    		} else {
			    			JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_addToIgnoreListButton), "Selected file to ignore is not inside the source directory one (or inside one of its sub folder)!", "Warning", JOptionPane.WARNING_MESSAGE);
			    		}
			    	} 
			    	_property.addToIgnoreList(chooser.getSelectedFile().getAbsolutePath());			    	
			    }
			}
		});
		
		_removeFromIgnoreListButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_property.removeFromIgnoreList(_ignoredList.getSelectedValue());
			}
		});

		
		this.setLayout(new MigLayout("fillx", "[][grow, fill][fill]", "[]0[]"));
		//--
		this.add(GuiUtils.setBold(_entry));
		this.add(_deleteButton, "skip 1, wrap");
		//--
		this.add(GuiUtils.setBold(new JLabel("Source:")));
		this.add(_src);
		this.add(_srcBrowseButton, "wrap");
		//--
		this.add(GuiUtils.setBold(new JLabel("Destination:")));
		this.add(_dst);
		this.add(_dstBrowseButton, "wrap");
		//--
		this.add(GuiUtils.setBold(new JLabel("Recursive:")));
		this.add(_recur, "span, wrap");
		//--
		this.add(GuiUtils.setBold(new JLabel("Ignored:")));
		this.add(new JScrollPane(_ignoredList), "height 50");
		this.add(_addToIgnoreListButton, "wrap, id myid");
		this.add(_removeFromIgnoreListButton, "pos myid.x myid.y2");
		//--
		this.add(GuiUtils.setBold(new JLabel("Encryption:")));
		this.add(_crypt, "span, wrap");
		//--
		this.add(GuiUtils.setBold(new JLabel("Progress:")));
		this.add(_progressBar, "span, wrap");
		//--
		
		this.add(GuiUtils.setBold(new JLabel("Action:")));
		this.add(_backupButton, "span, split 2");
		this.add(_restoreButton);

		_property = property;
		this.propertyUpdate(property);
		_property.addListener(this);
	}
	
	private class YesNoBooleanCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 2070768274356412077L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (Boolean.TRUE.equals(value)) { 
	             value = "Yes";
	          } else if (Boolean.FALSE.equals(value)) {
	             value = "No";
	          }
			
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	
	@Override
	public void propertyUpdate(Property property) {
		if (_listen) {
			_src.setText(property.getSource());
			_src.notifyListeners();
			_dst.setText(property.getDestination());
			_recur.setSelectedItem(property.getRecursive());
			_crypt.setSelectedItem(property.getEncryption());
			_ignoredListModel.removeAllElements();
			for (String ignored : property.getIgnoredList()) {
				_ignoredListModel.addElement(ignored);
			}
			_removeFromIgnoreListButton.setEnabled(property.getIgnoredList().size() > 0);
		}
	}

	@Override
	public void jTextFieldUpdate(JTextField2 jTextField) {
		if (jTextField == _src) {
			_listen = false;
			_property.setSource(_src.getText());
			_listen = true;
			
			
			File file = new File(_property.getSource());
			if (_property.getSource().trim().length() > 0) {
				_entry.setText(file.getName());
			} else {
				_entry.setText("Entry");
			}
			if (file.exists() == false) {
				_src.setBackground(new Color(253, 253, 150));
			} else {
				_src.setBackground(Color.WHITE);
			}
			
		} else if (jTextField == _dst) {
			_listen = false;
			_property.setDestination(_dst.getText());
			_listen = true;
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_src.setEnabled(enabled);
		_dst.setEnabled(enabled);
		_recur.setEnabled(enabled);
		_crypt.setEnabled(enabled);
		_deleteButton.setEnabled(enabled);
		_srcBrowseButton.setEnabled(enabled);
		_dstBrowseButton.setEnabled(enabled);
		_backupButton.setEnabled(enabled);
		_addToIgnoreListButton.setEnabled(enabled);
		_removeFromIgnoreListButton.setEnabled(enabled);
		_ignoredList.setEnabled(enabled);
	}

	@Override
	public void ioOperationStart(Property property) {
		_progressBar.setValue(0);
	}

	@Override
	public void ioOperationOneFileProcessed(Property property) {
		double ratio = (double)property.getTotalBackupedSrcFiles() / (double)property.getTotalSrcFiles();
		_progressBar.setValue((int)(ratio * 100.));
	}

	@Override
	public void ioOperationOneFileNew(Property property) {
		
	}	

	@Override
	public void ioOperationCountSrcFilesDone(Property property) {
		_progressBar.setValue(0);
	}
	
	@Override
	public void ioOperationEnd(Property property) {
		_progressBar.setValue(100);
	}

	private boolean isChild(String parentText, String childText) {
		Path child = Paths.get(childText).toAbsolutePath();
	    Path parent = Paths.get(parentText).toAbsolutePath();
	    return child.startsWith(parent);
	}
	
}
