package gui;

import java.io.File;

import core.Properties;
import core.PropertiesListener;
import core.Property;
import core.antistandby.AntiStandby;
import gui.component.PropertiesPanelListener;

public class StandbyHibernateBlocker implements PropertiesListener, PropertiesPanelListener {

	private AntiStandby _antiStandby;
	
	public StandbyHibernateBlocker() {
		_antiStandby = AntiStandby.getInstance();
	}
	
	@Override
	public void ioOperationStart(Properties properties) {
		_antiStandby.enableAntiStandby();
	}

	@Override
	public void ioOperationEnd(Properties properties) {
		_antiStandby.disableAntiStandby();
	}
	
	@Override
	public void loadNewProperties(Properties oldProperties, Properties newProperties) {
		if (oldProperties != null) {
			oldProperties.removeListener(this);
		}
		if (newProperties != null) {
			newProperties.addListener(this);
		}
	}	
	
	@Override
	public void propertiesLoad(Properties properties, File file) {
	}

	@Override
	public void propertiesAddProperty(Properties properties, Property property) {
	}

	@Override
	public void propertiesRemProperty(Properties properties, Property property) {
	}

	@Override
	public void propertiesUpdateProperty(Properties properties, Property property) {
	}

	@Override
	public void propertiesSave(Properties properties, File file) {
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
}
