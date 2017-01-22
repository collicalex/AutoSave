package core.antistandby;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public class WindowsAntiStandby extends AntiStandby {
    private static final int ES_NULL = 0x00000000;
    private static final int ES_SYSTEM_REQUIRED = 0x00000001;
    //private static final int ES_DISPLAY_REQUIRED = 0x00000002;
    private static final int ES_CONTINUOUS = 0x80000000;
    private Kernel32 _kernel32;
    
    public WindowsAntiStandby() {
    	_kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
    }
    
    private interface Kernel32 extends StdCallLibrary {
    	public int SetThreadExecutionState(int esFLAGS);
     }
    
    public boolean enableAntiStandby() {
    	int state = _kernel32.SetThreadExecutionState(ES_CONTINUOUS | ES_SYSTEM_REQUIRED); // | ES_DISPLAY_REQUIRED
    	return (state != ES_NULL);
    }
    
    public boolean disableAntiStandby() {
    	 int state = _kernel32.SetThreadExecutionState(ES_CONTINUOUS);
    	 return (state != ES_NULL);
    }
}
