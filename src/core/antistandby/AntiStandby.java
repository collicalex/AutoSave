package core.antistandby;

import com.sun.jna.Platform;

public abstract class AntiStandby {
	public abstract boolean enableAntiStandby();
	public abstract boolean disableAntiStandby();
	
	public static AntiStandby getInstance() {
		if (Platform.isWindows()) {
			return new WindowsAntiStandby();
		} else {
			return new GenericAntiStandby();
		}
	}
}
