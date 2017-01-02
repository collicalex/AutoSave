package gui;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.File;

import core.Properties;
import core.PropertiesListener;
import core.Property;
import gui.component.PropertiesPanelListener;

public class StandbyHibernateBlocker extends Thread implements PropertiesListener, PropertiesPanelListener {

	private Robot _robot;
	private Point _lastPosition;
	private int _direction;
	
	public StandbyHibernateBlocker() {
		try {
			_robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		if (_robot != null) {
			_lastPosition = MouseInfo.getPointerInfo().getLocation();
			_direction = 1;
			while(true) {
				try {
					Thread.sleep(60000);
					Point currentPosition = MouseInfo.getPointerInfo().getLocation();
					if ((currentPosition.x == _lastPosition.x) && (currentPosition.y == _lastPosition.y)) {
						_robot.mouseMove(currentPosition.x+_direction, currentPosition.y+_direction);
						_direction *= -1;
					}
					_lastPosition = MouseInfo.getPointerInfo().getLocation();
	            } catch (InterruptedException ex) {
	            	Thread.currentThread().interrupt();
	                break;
	            }
	        }
		}
    }
	
	@Override
	public void ioOperationStart(Properties properties) {
		this.start();
	}

	@Override
	public void ioOperationEnd(Properties properties) {
		if (_robot != null) {
			this.interrupt();
		}
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
