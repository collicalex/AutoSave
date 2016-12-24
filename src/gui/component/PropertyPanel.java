package gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import core.Property;
import core.PropertyListener;
import gui.GuiUtils;

public class PropertyPanel extends JPanel implements PropertyListener, JTextField2Listener {
	
	private static final long serialVersionUID = -7782988719016798420L;
	
	private Property _property;
	
	private JLabel _entry;
	
	private JTextField2 _src;
	private JTextField2 _dst;
	private JComboBox<Boolean> _recur;
	
	private JButton _deleteButton;
	private JButton _srcBrowseButton;
	private JButton _dstBrowseButton;
	private JButton _backupButton;
	
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
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
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
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			    	_dst.setText(chooser.getSelectedFile().getAbsolutePath());
			    }
			}
		});	
		
		_backupButton = new JButton("Run backup");
		_backupButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				(new Thread() {
					public void run() {
						_property.backup(false);
					}
				}).start();
			}
		});

		_progressBar = new JProgressBar();
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		
		_entry = new JLabel("Entry:");
		
		this.setLayout(new GridLayout(5, 1));
		this.add(createPanel(_entry, null, _deleteButton));
		this.add(createPanel("Source", _src, _srcBrowseButton));
		this.add(createPanel("Destination", _dst, _dstBrowseButton));
		this.add(createPanel("Recursive", _recur, null));
		this.add(createPanel("Progress", _progressBar, _backupButton));
		
		_property = property;
		this.propertyUpdate(property);
		_property.addListener(this);
	}
	
	private JPanel createPanel(String caption, JComponent component, JButton button) {
		JLabel label = new JLabel(caption + ":");
		label.setPreferredSize(new Dimension(75, 1));
		return createPanel(label, component, button);
	}
	
	private JPanel createPanel(JLabel label, JComponent component, JButton button) {
		if (component == null) {
			Font f = label.getFont();
			label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		}
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.WEST);
		if (component != null) {
			panel.add(component, BorderLayout.CENTER);	
		}
		if (button != null) {
			panel.add(button, BorderLayout.EAST);
		}
		return panel;
	}
	
	@Override
	public void propertyUpdate(Property property) {
		if (_listen) {
			_src.setText(property.getSource());
			_src.notifyListeners();
			_dst.setText(property.getDestination());
			_recur.setSelectedItem(property.getRecursive());
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
		_deleteButton.setEnabled(enabled);
		_srcBrowseButton.setEnabled(enabled);
		_dstBrowseButton.setEnabled(enabled);
		_backupButton.setEnabled(enabled);
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

	
	@Override
	public String getID() {
		return "PropertyPanel";
	}

}
