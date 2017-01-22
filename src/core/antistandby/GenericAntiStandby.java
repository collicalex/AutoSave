package core.antistandby;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

public class GenericAntiStandby extends AntiStandby {
	private Robot _robot;
	private Point _lastPosition;
	private int _direction;
	private Thread _thread;
	
	public GenericAntiStandby() {
		try {
			_robot = new Robot();
			_thread = createThread();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private Thread createThread() {
		if (_robot == null) {
			return null;
		}
		return new Thread() {
			@Override
			public void run() {
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
		};
	}

	@Override
	public boolean enableAntiStandby() {
		if (_thread != null) {
			_thread.start();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean disableAntiStandby() {
		if (_thread != null) {
			_thread.interrupt();
			_thread = createThread();
			return true;
		} else {
			return false;
		}
	}	
	
}
