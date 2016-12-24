package gui.component;

import core.Properties;

public interface PropertiesPanelListener {
	public String getID();
	public void loadNewProperties(Properties oldProperties, Properties newProperties);
}
