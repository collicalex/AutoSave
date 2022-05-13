package gui.locationchooser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gui.icon.Icon;

public class LocationChooserDialog extends JDialog implements ChangeListener {

	private static final long serialVersionUID = 1322342154509949983L;
	private LocalLocationChooser _localLocationChooser;
	private PCloudLocationChooser _pcloudLocationChooser;
	private boolean _locationChoosen = false;
	private JTextField _locationString;

    public LocationChooserDialog(final JFrame parent) {
        super(parent, "Location Chooser", true);
        
        _localLocationChooser = new LocalLocationChooser(this);
        _pcloudLocationChooser = new PCloudLocationChooser(this);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        tabbedPane.addTab("Local", new ImageIcon(Icon.decode(Icon.localStorageIco)), _localLocationChooser);
        tabbedPane.addTab("pCloud", new ImageIcon(Icon.decode(Icon.pcloudIco)), _pcloudLocationChooser);
        
        tabbedPane.addChangeListener(this);
        
        
        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        
        _locationString = new JTextField();
        _locationString.setEditable(false);
        JPanel locationPanel = new JPanel(new BorderLayout());
        locationPanel.add(new JLabel(" Location : "), BorderLayout.WEST);
        locationPanel.add(_locationString, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(locationPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        this.setContentPane(contentPanel);
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setIconImage(Icon.decode(Icon.searchFolderIco));

        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	_locationChoosen = true;
            	setVisible(false);
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	_locationChoosen = false;
                setVisible(false);
            }
        });
        
        updateLocationString(tabbedPane);
        this.setVisible(true);
    }
    
    public void setLocationString(String location) {
    	_locationString.setText(location);
    }
    
    public String getLocationString() {
    	return _locationString.getText();
    }
	
    public boolean isLocationChoosen() {
    	return _locationChoosen;
    }

    private void updateLocationString(JTabbedPane pane) {
    	LocationChooserPanel lcp = (LocationChooserPanel) pane.getSelectedComponent();
        lcp.updateLocationString();
    }
    
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            updateLocationString(pane);
        }
	}
}
