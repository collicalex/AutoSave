package core.device;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;


public abstract class Device {

	//localstorage:xxx:\yyyy
	//pcloud:xxx:\yyy
	public static Device getDevice(String path) throws IOException {
	
		int pos = path.indexOf(':');
		if (pos == -1) {
			throw new IOException("Path '"+ path + "' does not contains ':' character");
		}
		
		String storageType = path.substring(0, pos);
		
		if ("localstorage".compareTo(storageType.toLowerCase()) == 0) {
			return new LocalStorage(path);
		} else if ("pcloud".compareTo(storageType.toLowerCase()) == 0) {
			return new PCloudStorage(path);
		} else {
			throw new IOException("Unkonwon storage type \"" + storageType + "\"");
		}
	}
	
	
	//-------------------------------------------------------------------------
	//-- To be implemented by each Storage - Authentication functions
	//-------------------------------------------------------------------------
	
	abstract public boolean requiredCredentials();
	
	abstract public void login(String username, String password) throws IOException;
	
	abstract public void logout() throws IOException;
	
	
	//-------------------------------------------------------------------------
	//-- To be implemented by each Storage - Copye functions
	//-------------------------------------------------------------------------	
	
	
	/*
	 * Must count the number of files contains in the Device
	 * Does not include the ignored files and folders
	 */
	abstract public long countFiles(boolean recursive, List<Pattern> ignoredList);
	
	/*
	 * Must copy the file "source" (which must be accessible) to path (which is relative to the current Device entry point)
	 * Example :
	 *    - if the current device entry point is "c:\test\" or "http://pcloud.api.com/test/"
	 *    - if the "source" file is "d:\dirA\dirB\test.txt"
	 *    - if the path is "dirC\test.txt" 
	 *    => Then it will copy the file to 
	 *       * c:\test\dirC\test.txt
	 *       * or http://pcloud.api.com/test/dirC/test.txt
	 */
	abstract public void copyOneFile(File source, String path, List<FileTransfertListener> listeners) throws IOException;
	
	/*
	 * Return the last modification date of the destination file (if exist), else 0;
	 */
	abstract public long lastModified(String path) throws IOException;
	
	/*
	 * Return the full path (human readable) where the target file will be saved (used only by Logger to display information)
	 */
	abstract public String getTargetFullPath(String path);
	
	
	//-------------------------------------------------------------------------
	//-- Listeners
	//-------------------------------------------------------------------------	
	
	protected void notifyListenersFileTransferStart(List<FileTransfertListener> listeners, String path, long sizeToTransfert) {
		if (listeners != null) {
			for (FileTransfertListener listener : listeners) {
				listener.fileTransferStart(path, sizeToTransfert);
			}
		}
	}
	
	protected void notifyListenersFileTransferPart(List<FileTransfertListener> listeners, long sizeTransmitted) {
		if (listeners != null) {
			for (FileTransfertListener listener : listeners) {
				listener.fileTransferPart(sizeTransmitted);
			}
		}
	}
	
	protected void notifyListenersFileTransferFinish(List<FileTransfertListener> listeners) {
		if (listeners != null) {
			for (FileTransfertListener listener : listeners) {
				listener.fileTransferFinish();
			}
		}
	}

}
