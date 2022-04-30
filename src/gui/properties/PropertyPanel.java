package gui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import core.copier.Copier;
import core.copier.CopierListener;
import core.properties.Property;
import core.properties.PropertyListener;
import gui.Gui;
import gui.icon.Icon;
import gui.utils.GuiUtils;
import net.miginfocom.swing.MigLayout;

public class PropertyPanel extends JPanel implements PropertyListener, DocumentListener, ActionListener, CopierListener {
	
	private static final long serialVersionUID = -7782988719016798420L;
	
	private Property _property;
	
	private JLabel _entry;
	
	private JTextField _src;
	private JTextField _dst;
	private JComboBox<Boolean> _recur;
	private DefaultListModel<String> _ignoredListModel;
	private JList<String> _ignoredList;
	
	private JButton _deleteButton;
	private JButton _srcBrowseButton;
	private JButton _dstBrowseButton;
	private JButton _backupButton;
	private JButton _addToIgnoreListButton;
	private JButton _removeFromIgnoreListButton;
	
	private JProgressBar _progressBar;
	
	private PropertiesPanel _parent;
	private Gui _gui;
	
	public PropertyPanel(Gui gui, PropertiesPanel parent, Property property) {
		_gui = gui;
		_parent = parent;
		this.setBorder(new EmptyBorder(5, 5, 10, 5));
		
		_src = new JTextField();
		_src.getDocument().addDocumentListener(this);
		
		_dst = new JTextField();
		_dst.getDocument().addDocumentListener(this);
		
		_recur = new JComboBox<Boolean>(new Boolean[]{true, false});
		_recur.addActionListener(this);
		_recur.setRenderer(new YesNoBooleanCellRenderer());
		
		_deleteButton = new JButton("X");
		_deleteButton.addActionListener(this);
		
		_srcBrowseButton = new JButton("...");
		_srcBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: helper window to select a Device
				//_src.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});		
		
		
		_dstBrowseButton = new JButton("...");
		_dstBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: helper window to select a Device
				//_dst.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});	
		
		_backupButton = new JButton("Backup");
		_backupButton.addActionListener(this);
		
		
		_progressBar = new JProgressBar();
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		
		_entry = new JLabel("Entry:");
		_entry.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		_entry.addMouseListener(new MouseAdapter() {
				
			@Override
			public void mouseExited(MouseEvent e) {
			    e.getComponent().setForeground(Color.BLACK);
			    e.getComponent().setCursor(Cursor.getDefaultCursor());
			}
	
			@Override
			public void mouseEntered(MouseEvent e) {
				e.getComponent().setForeground(Color.BLUE);
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
	
			@Override
			public void mouseClicked(MouseEvent e) {
				selectIcon();
			}
		});	
		
		
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
				//TODO: Helper to create Pattern
				//_property.addToIgnoreList(chooser.getSelectedFile().getAbsolutePath());			    	
			}
		});
		
		_removeFromIgnoreListButton.addActionListener(this);

		this.setLayout(new MigLayout("fillx", "[][grow, fill][fill]", "[]0[]"));
		//--
		this.add(GuiUtils.setBold(_entry), "span 2");
		this.add(_deleteButton, "wrap");
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
		this.add(GuiUtils.setBold(new JLabel("Progress:")));
		this.add(_progressBar, "span, wrap");
		//--
		
		this.add(GuiUtils.setBold(new JLabel("Action:")));
		this.add(_backupButton, "span");

		_property = property;
		_property.notifyListerner(this);
		_property.addListener(this);
		
	}
	
	
	public void stopListeningPropertyChange() {
		_property.removeListener(this);
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
	
	public boolean isProperty(Property property) {
		return _property == property;
	}
	
	
	private void runBackup() {
		setEnabled(false);
		Copier copier = new Copier();
		copier.addListener(this);
		_gui.listenCopier(copier, _property);
		
		(new Thread() {
			public void run() {
				try {
					copier.copy(_gui, _property, _parent.isSimulationModeChecked());
					JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_parent), _nbFilesCopied + " files had been copied", "Copy finish", JOptionPane.INFORMATION_MESSAGE);
					setEnabled(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_parent), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					setEnabled(true);
				}			
			}
		}).start();
	}
	

	private void selectIcon() {
		JFileChooser chooser = new JFileChooser();
	    chooser.setDialogTitle("Select image (16x16)");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpeg", "jpg", "png", "bmp", "gif", "webmp");
	    chooser.setFileFilter(filter);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(GuiUtils.getOldestParent(_entry)) == JFileChooser.APPROVE_OPTION) {
	    	try {
	    		BufferedImage bi = ImageIO.read(chooser.getSelectedFile());
	    		if ((bi.getWidth() != 16) || (bi.getHeight() != 16)) {
	    			JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_entry), "The selected image must be 16x16 size.", "Error", JOptionPane.ERROR_MESSAGE);
	    		} else {
	    			String b64 = Icon.encode(chooser.getSelectedFile());
	    			_property.setIcon(b64);
	    		}
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(GuiUtils.getOldestParent(_entry), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
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
		_addToIgnoreListButton.setEnabled(enabled);
		_removeFromIgnoreListButton.setEnabled(enabled);
		_ignoredList.setEnabled(enabled);
	}

	//-------------------------------------------------------------------------
	//-- PropertyListener
	//-------------------------------------------------------------------------			
	
	@Override
	public void propertyUpdateSrc(Property property, String newValue) {
		if (newValue == null) {
			return ;
		}
		
		if (_src.getText().compareTo(newValue) != 0) {
			_src.setText(newValue);
		}
		
		String title = _property.getSource();
		int pos0 = _property.getSource().lastIndexOf(":");
		if (pos0 != -1) {
			title = _property.getSource().substring(pos0+1);
		}
		
		int pos1 = title.lastIndexOf("/");
		int pos2 = title.lastIndexOf("\\");
		int pos = Math.max(pos1, pos2);
		
		if (pos != -1) {
			_entry.setText(title.substring(pos+1));
		} else {
			_entry.setText(title);
		}
	}

	@Override
	public void propertyUpdateDst(Property property, String newValue) {
		if (newValue == null) {
			return ;
		}
		if (_dst.getText().compareTo(newValue) != 0) {
			_dst.setText(newValue);
		}
	}

	@Override
	public void propertyUpdateRecur(Property property, boolean newValue) {
		if ((Boolean)_recur.getSelectedItem() != newValue) {
			_recur.setSelectedItem(newValue);
		}
	}

	@Override
	public void propertyUpdateAddIgnore(Property property, String newValue) {
		_ignoredListModel.addElement(newValue);
		
	}

	@Override
	public void propertyUpdateRemoveIgnore(Property property, String newValue) {
		_ignoredListModel.removeElement(newValue);
	}

	
	@Override
	public void propertyUpdateIcon(Property property, String newValue) {
		if (newValue != null) {
			_entry.setIcon(new ImageIcon(Icon.decode(newValue)));
		} else {
			_entry.setIcon(null);
		}
	}
	
	//-------------------------------------------------------------------------
	//-- DocumentListener
	//-------------------------------------------------------------------------		
	
	private void sourceUpdated() {
		try {
			_property.setSource(_src.getText());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void destinationUpdated() {
		try {
			_property.setDestination(_dst.getText());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void inputFieldTextUpdated(DocumentEvent e) {
		if (e.getDocument() == _src.getDocument()) {
			sourceUpdated();
		} else if (e.getDocument() == _dst.getDocument()) {
			destinationUpdated();
		}
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		inputFieldTextUpdated(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		inputFieldTextUpdated(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		inputFieldTextUpdated(e);
	}

	//-------------------------------------------------------------------------
	//-- ActionListener
	//-------------------------------------------------------------------------			
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _recur) {
			_property.setRecursive((Boolean)_recur.getSelectedItem());
		} else if (e.getSource() == _removeFromIgnoreListButton) {
			_property.removeFromIgnoreList(_ignoredList.getSelectedValue());
		} else if (e.getSource() == _deleteButton) {
			int dialogResult = JOptionPane.showConfirmDialog(GuiUtils.getOldestParent(_deleteButton), "Are you sure you want to remove this entry from the configuration file?", "Delete", JOptionPane.YES_NO_OPTION);
			if(dialogResult == 0) {
				_parent.removeProperty(_property);
			}
		} else if (e.getSource() == _backupButton) {
			runBackup();
		}
	}

	
	//-------------------------------------------------------------------------
	//-- CopierListener
	//-------------------------------------------------------------------------	

	private long _nbFilesCopied = 0;
	private long _nbFilesToCopy = 0;
	
	public void initProgressBar() {
		_progressBar.setValue(0);
	}
	
	private void updateProgressBar() {
		double ratio = (double)_nbFilesCopied / (double)_nbFilesToCopy;
		int percent = (int)((ratio * 100.d) + 0.5d);
		_progressBar.setValue(percent);
	}
	
	@Override
	public void copierCountStart(Object sourceEvent) {
		if (sourceEvent == _property) {
			_progressBar.setValue(0);
		}
	}


	@Override
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
		if (sourceEvent == _property) {
			_nbFilesCopied = 0;
			_nbFilesToCopy = nbFilesToCopy;
		}
	}


	@Override
	public void copierCopyStart(Object sourceEvent) {
	}


	@Override
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath) {
		if (sourceEvent == _property) {
			_nbFilesCopied++;
			updateProgressBar();
		}
	}


	@Override
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
	}


	@Override
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
		if (sourceEvent == _property) {
			_nbFilesCopied++;
			updateProgressBar();
		}
	}


	@Override
	public void copierCopyFinish(Object sourceEvent) {
		if (sourceEvent == _property) {
			_progressBar.setValue(100);
		}
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
