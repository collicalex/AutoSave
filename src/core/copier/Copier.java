package core.copier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.device.Device;
import core.device.FileTransfertListener;
import core.device.LocalStorage;
import core.properties.Properties;
import core.properties.Property;

public class Copier {

	private List<CopierListener> _listeners;
	private Map<String, Device> _devices;
	private HashMap<Class<?>, Credentials> _credentials;
	
	public Copier() {
		_listeners = new LinkedList<CopierListener>();
	}
	
	//-------------------------------------------------------------------------
	//-- Copy from Property(s)
	//-------------------------------------------------------------------------	
	
	public void copy(CredentialsRequester credentialsRequester, Properties properties, boolean simulation) throws IOException {
		//-- Authenticate
		initAuthentication();
		login(credentialsRequester, properties);
		//-- Count files
		long nbTotalFilesToCopy = 0;
		notifyerListenersCountStart(properties);
		for (Property property : properties.getProperties()) {
			notifyerListenersCountStart(property);
			long nbFilesToCopy = countFiles(properties, property);
			notifyListenersCountFinish(property, property.getSource(), nbFilesToCopy);
			nbTotalFilesToCopy += nbFilesToCopy;
		}
		notifyListenersCountFinish(properties, null, nbTotalFilesToCopy);
		//-- Copy files
		notifyListenersCopyStart(properties);
		for (Property property : properties.getProperties()) {
			notifyListenersCopyStart(property);
			copyFiles(properties, property, simulation);
			notifyListenersCopyFinish(property);
		}
		notifyListenersCopyFinish(properties);
		//-- Logout
		logout(properties);
	}
	
	
	public void copy(CredentialsRequester credentialsRequester, Property property, boolean simulation) throws IOException {
		//-- Authenticate
		initAuthentication();
		login(credentialsRequester, property);
		//-- Count files
		notifyerListenersCountStart(property);
		long nbFilesToCopy = countFiles(null, property);
		notifyListenersCountFinish(property, property.getSource(), nbFilesToCopy);		
		//-- Copy files
		notifyListenersCopyStart(property);
		copyFiles(null, property, simulation);
		notifyListenersCopyFinish(property);
		//-- Logout
		logout(property);
	}
	
	private void copyFiles(Properties properties, Property property, boolean simulation) throws IOException {
		Device source = getDevice(property.getSource());
		Device destination = getDevice(property.getDestination());
		boolean recursive = property.getRecursive();
		List<Pattern> ignoredList = compileIgnoredList(property.getIgnoredList());
		
		copy(property, source, destination, recursive, ignoredList, simulation);
	}
	
	
	private long countFiles(Properties properties, Property property) throws IOException {
		Device source = getDevice(property.getSource());
		boolean recursive = property.getRecursive();
		List<Pattern> ignoredList = compileIgnoredList(property.getIgnoredList());
		
		return source.countFiles(recursive, ignoredList);
	}
	
	private Device getDevice(String path) throws IOException {
		Device device = _devices.get(path);
		if (device == null) {
			throw new IOException("Unable to retrieve device '" + path + "'");
		}
		return device;
	}

	
	//-------------------------------------------------------------------------
	//-- Authentication methods
	//-------------------------------------------------------------------------	
	
	private void initAuthentication() {
		_devices =  new HashMap<String, Device>();	
		_credentials = new HashMap<Class<?>, Credentials>();
	}
	
	private void login(CredentialsRequester credentialsRequester, Properties properties) throws IOException {
		for (Property property : properties.getProperties()) {
			login(credentialsRequester, property, true);
		}
	}
	
	private void login(CredentialsRequester credentialsRequester, Property property) throws IOException {
		login(credentialsRequester, property, false);
	}	

	private void login(CredentialsRequester credentialsRequester, Property property, boolean multipleProperties) throws IOException {
		login(credentialsRequester, property.getSource(), multipleProperties);
		login(credentialsRequester, property.getDestination(), multipleProperties);
	}
	
	private void login(CredentialsRequester credentialsRequester, String path, boolean multipleProperties) throws IOException {
		if (_devices.containsKey(path) == false) {
			Device device = Device.getDevice(path);
			if (device.requiredCredentials()) {
				
				Credentials credentials = null;
				if (_credentials.containsKey(device.getClass())) {
					credentials = _credentials.get(device.getClass());
				} else {
					credentials = credentialsRequester.requestCredential(device, multipleProperties);
					if (credentials != null) {
						if (credentials.isDefault) {
							_credentials.put(device.getClass(), credentials);
						}
					}
				}
				
				if (credentials != null) {
					device.login(credentials.username, credentials.password);
				} else {
					throw new IOException("No credential given");
				}
			}
			_devices.put(path, device);
		}
	}
	
	private void logout(Properties properties) throws IOException {
		for (Property property : properties.getProperties()) {
			logout(property);
		}
	}

	private void logout(Property property) throws IOException {
		logout(property.getSource());
		logout(property.getDestination());
	}
	
	private void logout(String path) throws IOException {
		if (_devices.containsKey(path) == false) {
			Device device = Device.getDevice(path);
			device.logout();
			_devices.remove(path);
		}
	}
	
	
	//-------------------------------------------------------------------------
	//-- Copy from Devices
	//-------------------------------------------------------------------------	
	
	private void copy(Object sourceEvent, Device source, Device destination, boolean recursive, List<Pattern> ignoredList, boolean simulation) throws IOException {
		if (source instanceof LocalStorage) {
			LocalStorage ls_source = (LocalStorage) source;
			save_files(sourceEvent, ls_source.getEntryPoint(), destination, recursive, ignoredList, simulation);
		} else {
			throw new IOException("Unable to copy when source is not local");
		}
	}
	
	
	private void save_files(Object sourceEvent, File src, Device dst, boolean recursive, List<Pattern> ignoredList, boolean simulation) throws IOException {
		if (src.exists() == false) {
			return ;
		}
		
		String matchedPattern1 = isIgnored(ignoredList, src.getAbsolutePath());
		if (matchedPattern1 != null) {
			notifyListenersFileIgnored(sourceEvent, src.getAbsolutePath(), matchedPattern1);
			return ;
		}
		if (src.isFile()) {
			save_file(sourceEvent, dst, src, src.getName(), simulation);
		}
		
		//Copy an entire directory
		Queue<File> dirs = new LinkedList<File>();
		dirs.add(src);
		while (!dirs.isEmpty()) {
			File dir = dirs.poll();
			File[] files = dir.listFiles();
			
			for (File f : files) {
				String matchedPattern = isIgnored(ignoredList, f.getAbsolutePath());
			    if (matchedPattern != null) {
			    	notifyListenersFileIgnored(sourceEvent, f.getAbsolutePath(), matchedPattern);
			    } else {
			    	if (f.isFile()) {
			    		String destPath = f.getAbsolutePath().substring(src.getAbsolutePath().length()+1);
			    		save_file(sourceEvent, dst, f, destPath, simulation);
			    	} else { //it's a directory
			    		if (recursive) {
			    			dirs.add(f);
			    		}
			    	}
			    }
			}
		}
	}
	
	private List<FileTransfertListener> getFileTransfertListeners() {
		List<FileTransfertListener> listeners = new LinkedList<FileTransfertListener>();
		listeners.addAll(_listeners);
		return listeners;
	}
	
	private void save_file(Object sourceEvent, Device dst, File file, String path, boolean simulation) throws IOException {
		String destFullPath = dst.getTargetFullPath(path);
		
		if (simulation == false) {
			long srcLastModified = file.lastModified();
			long dstLastModified = dst.lastModified(path);
			if (srcLastModified <= dstLastModified) {
				notifyListenersFileSkip(sourceEvent, file.getAbsolutePath(), srcLastModified, destFullPath, dstLastModified);
				return ;
			}
		}

		notifyListenersFileCopied(sourceEvent, file.getAbsolutePath(), destFullPath);
		if (simulation == false) {
			dst.copyOneFile(file, path, getFileTransfertListeners());
		}
	}
	
	
	//-------------------------------------------------------------------------
	//-- Ignored file?
	//-------------------------------------------------------------------------	
	
	//Pattern list can be found here: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
	public static String isIgnored(List<Pattern> ignoredList, String filename) {
		if (ignoredList != null) {
			for (Pattern p : ignoredList) {
				Matcher m = p.matcher(filename);
				if (m.matches()) {
					return p.pattern();
				}
			}
		}
		return null;
	}	
	
	
	public static List<Pattern> compileIgnoredList(List<String> ignoredList) {
		if (ignoredList != null) {
			List<Pattern> result = new LinkedList<Pattern>();
			for (String pattern : ignoredList) {
				Pattern p = Pattern.compile(pattern);
				result.add(p);
			}
			return result;
		}
		return null;
	}
	
	
	//-------------------------------------------------------------------------
	//-- Listeners
	//-------------------------------------------------------------------------	
	
	public void addListener(CopierListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(CopierListener listener) {
		_listeners.remove(listener);
	}
	
	private void notifyerListenersCountStart(Object sourceEvent) {
		for (CopierListener listener : _listeners) {
			listener.copierCountStart(sourceEvent);
		}
	}
	
	private void notifyListenersCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
		for (CopierListener listener : _listeners) {
			listener.copierCountFinish(sourceEvent, parentDirectory, nbFilesToCopy);
		}
	}	
	
	private void notifyListenersCopyStart(Object sourceEvent) {
		for (CopierListener listener : _listeners) {
			listener.copierCopyStart(sourceEvent);
		}
	}
	
	private void notifyListenersFileCopied(Object sourceEvent, String srcPath, String dstPath) {
		for (CopierListener listener : _listeners) {
			listener.copierFileCopied(sourceEvent, srcPath, dstPath);
		}
	}
	
	private void notifyListenersFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
		for (CopierListener listener : _listeners) {
			listener.copierFileIgnored(sourceEvent, srcPath, matchedPattern);
		}
	}
	
	private void notifyListenersFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
		for (CopierListener listener : _listeners) {
			listener.copierFileSkip(sourceEvent, srcPath, srcLastModified, dstPath, dstLastModified);
		}
	}

	private void notifyListenersCopyFinish(Object sourceEvent) {
		for (CopierListener listener : _listeners) {
			listener.copierCopyFinish(sourceEvent);
		}
	}

}
