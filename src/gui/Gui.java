package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import core.copier.Copier;
import core.copier.Credentials;
import core.copier.CredentialsRequester;
import core.device.Device;
import core.device.LocalStorage;
import core.device.PCloudStorage;
import gui.icon.Icon;
import gui.locationchooser.LocationChooserDialog;
import gui.properties.PropertiesPanel;
import gui.regexpbuilder.RegexpBuilderDialog;
import gui.utils.GuiUtils;
import gui.utils.SmartScroller;



public class Gui implements CredentialsRequester {

	private JFrame _jFrame;
	private List<GuiListener> _listeners;
	private StatsPanel _statsPanel;
	private LogPanel _logPanel;
	private FileTransferProgressPanel _fileTransferProgressPanel;
	
	public Gui() {
		GuiUtils.setSystemLookAndFeel();
		
		_listeners = new LinkedList<GuiListener>();
		
        PropertiesPanel propertiesPanel = new PropertiesPanel(this);
        propertiesPanel.setPreferredSize(new Dimension(500,800));
        this.addListener(propertiesPanel);

        _statsPanel = new StatsPanel();
        
		_logPanel = new LogPanel();
		JScrollPane logScrollPane = new JScrollPane(_logPanel);
		logScrollPane.getVerticalScrollBar().setUnitIncrement(15);
		new SmartScroller(logScrollPane);

        JLabel aboutLabel = new JLabel("About");
        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aboutLabel.setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new CompoundBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY), new EmptyBorder(1, 4, 1, 5))));
        aboutLabel.addMouseListener(new MouseAdapter() {
			
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
				new About(_jFrame).setVisible(true);
			}
		});
		
        _fileTransferProgressPanel = new FileTransferProgressPanel();
        
		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.add(_fileTransferProgressPanel);
		statusBar.add(aboutLabel, BorderLayout.EAST);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(_statsPanel, BorderLayout.NORTH);
		rightPanel.add(logScrollPane, BorderLayout.CENTER);
		rightPanel.add(statusBar, BorderLayout.SOUTH); 
		rightPanel.setPreferredSize(new Dimension(700,800));
        
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, propertiesPanel, rightPanel);
        splitPane.setContinuousLayout(true);
        propertiesPanel.setMinimumSize(new Dimension(0,0));
        rightPanel.setMinimumSize(new Dimension(0,0));        
        
        
        _jFrame = new JFrame("AutoSave");
        _jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	notifyListeners();
            }
        });        
        
        _jFrame.setContentPane(splitPane);
        _jFrame.setIconImage(Icon.decode(Icon.floppyIco));
        _jFrame.pack();
        _jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		_jFrame.setLocationRelativeTo(null);
		_jFrame.setVisible(true);
	}
	

	//-------------------------------------------------------------------------
	//-- Copier
	//-------------------------------------------------------------------------		
	
	private Copier _currentCopier = null;
	
	public void listenCopier(Copier copier, Object sourceEvent) {
		if (_currentCopier != null) {
			_currentCopier.removeListener(_statsPanel);
			_currentCopier.removeListener(_logPanel);
			_currentCopier.removeListener(_fileTransferProgressPanel);
			
		}
		
		copier.addListener(_statsPanel);
		copier.addListener(_logPanel);
		copier.addListener(_fileTransferProgressPanel);
		
		_statsPanel.setSourceEvent(sourceEvent);
		_logPanel.setSourceEvent(sourceEvent);
		
		_currentCopier = copier;
	}
	
	
	//-------------------------------------------------------------------------
	//-- Listeners
	//-------------------------------------------------------------------------		
	
	public void addListener(GuiListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(GuiListener listener) {
		_listeners.remove(listener);
	}
	
	private void notifyListeners() {
		for (GuiListener listener : _listeners) {
			listener.applicationClosing();
		}
	}

	
	//-------------------------------------------------------------------------
	//-- CredentialsRequester
	//-------------------------------------------------------------------------		

	@Override
	public Credentials requestCredential(Device device, boolean multipleProperties) {
		BufferedImage icon = null;
		BufferedImage logo = null;
		String name = "";
		if (device instanceof LocalStorage) {
			icon = Icon.decode(Icon.floppyIco);
			logo = Icon.decode(Icon.floppyIco, 80, 80, false);
			name = "LocalStorage";
		} else if (device instanceof PCloudStorage) {
			icon = Icon.decode(Icon.pcloudIco);
			logo = Icon.decode(Icon.pcloudLogo);
			name = "pCloud";
		} else {
			icon = Icon.decode(Icon.floppyIco);
			logo = Icon.decode(Icon.floppyIco, 80, 80, false);
			name = "???";
		}

		PasswordDialog pwdDialog = new PasswordDialog(_jFrame, true, icon, logo, name, multipleProperties);
		if (pwdDialog.isCredentialsGiven()) {
			return new Credentials(pwdDialog.getUsername(), pwdDialog.getPassworzd(), pwdDialog.isDefault());
		} else {
			return null;
		}
	}
	
	
	//-------------------------------------------------------------------------
	//-- Other
	//-------------------------------------------------------------------------	
	
	public String chooseRegexp() {
		RegexpBuilderDialog rgb = new RegexpBuilderDialog(_jFrame);
		if (rgb.isRegexpBuilt()) {
			return rgb.getRegexp();
		} else {
			return null;
		}
	}
	
	public String chooseLocation() {
		LocationChooserDialog lcd = new LocationChooserDialog(_jFrame);
		if (lcd.isLocationChoosen()) {
			return lcd.getLocationString();
		} else {
			return null;
		}
	}
	
}
