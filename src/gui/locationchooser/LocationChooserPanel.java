package gui.locationchooser;

import javax.swing.JPanel;

public abstract class LocationChooserPanel extends JPanel {

	private static final long serialVersionUID = -2617886932061280772L;
	protected LocationChooserDialog _parent;
	
	public LocationChooserPanel(LocationChooserDialog parent) {
		_parent = parent;
	}
	
	public abstract void updateLocationString();
	
	public void setLocationString(String location) {
		_parent.setLocationString(location);
	}
	
}
