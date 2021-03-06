package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.crypto.NoSuchPaddingException;

public class Properties extends LoggerAdapter implements PropertyListener {
	
	private Vector<Property> _properties = new Vector<Property>();
	private List<PropertiesListener> _listeners;
	private File _srcFile;
	private EncryptionUI _encryptionUI;
	
	private boolean _inIOoperation = false;
	private boolean _simulationOnly = false;
	
	public Properties(EncryptionUI encryptionUI) {
		_listeners = new LinkedList<PropertiesListener>();
		_encryptionUI = encryptionUI;
	}
	
	//-------------------------------------------------------------------------
	//-- Properties Listener
	//-------------------------------------------------------------------------
	
	public void addListener(PropertiesListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(PropertiesListener listener) {
		_listeners.remove(listener);
	}
	
	private void notifyListerners_propertiesLoad(File file) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesLoad(this, file);
		}
	}

	private void notifyListeners_propertiesSave(File file) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesSave(this, file);
		}
	}
	
	private void notifyListerners_propertiesAddProperty(Property property) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesAddProperty(this, property);
		}
	}
	
	private void notifyListerners_propertiesRemProperty(Property property) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesRemProperty(this, property);
		}
	}
	
	private void notifyListeners_propertiesUpdateProperty(Property property) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesUpdateProperty(this, property);
		}
	}
	
	private void notifyListerners_ioOperationStart() {
		for (PropertiesListener listener : _listeners) {
			listener.ioOperationStart(this);
		}
	}
	
	private void notifyListeners_ioOperationCountSrcFilesDone() {
		for (PropertiesListener listener : _listeners) {
			listener.ioOperationCountSrcFilesDone(this);
		}
	}
	
	public void notifyListeners_ioOperationOneFileProcessed(Properties properties) {
		for (PropertiesListener listener : _listeners) {
			listener.ioOperationOneFileProcessed(this);
		}
	}

	public void notifyListeners_ioOperationOneFileNew(Properties properties) {
		for (PropertiesListener listener : _listeners) {
			listener.ioOperationOneFileNew(this);
		}
	}
	
	private void notifyListerners_ioOperationEnd() {
		for (PropertiesListener listener : _listeners) {
			listener.ioOperationEnd(this);
		}
	}
	
	//-------------------------------------------------------------------------
	//-- Getters and Setters
	//-------------------------------------------------------------------------
	
	public int size() {
		return _properties.size();
	}
	
	public Property get(int index) {
		return _properties.get(index);
	}
	
	public void read(File file) throws IOException {
		_srcFile = file;
		_properties = new Vector<Property>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        
        Property property = new Property(this);
        boolean haveAddSomething = false;
        while ((str = br.readLine()) != null) {
        	str = str.trim();
        	if (str.length() == 0) { //empty line
        		if (haveAddSomething) {
        			this.addProperty(property);
        		}
    			property = new Property(this);
    			haveAddSomething = false;
        	} else {
        		property.add(str);
        		haveAddSomething = true;        			
        	}
        }
        br.close();
        if (haveAddSomething) {
        	this.addProperty(property);
        }
        notifyListerners_propertiesLoad(file);
	}
	
	public void save(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(this.toString());
		bw.close();
		_srcFile = file;
		notifyListeners_propertiesSave(file);
	}
	
	public void addProperty(Property property) {
		_properties.add(property);
		property.addListener(this);
		notifyListerners_propertiesAddProperty(property);
	}

	public void removeProperty(Property property) {
		_properties.remove(property);
		property.removeListener(this);
		notifyListerners_propertiesRemProperty(property);
	}
	
	public boolean needSave() {
		Properties properties = new Properties(null);
		if (_srcFile != null) {
			try {
				properties.read(_srcFile);
			} catch (IOException e) {
				return true;
			}
		}
		return (properties.toString().compareTo(this.toString()) != 0); 
	}
	
	public void isSimulationOnly(boolean state) {
		_simulationOnly = state;
	}
	
	public boolean isSimulationOnly() {
		return _simulationOnly;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < _properties.size(); ++i) {
			Property property = _properties.get(i);
			sb.append(property.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	

	//-------------------------------------------------------------------------
	//-- Property listener
	//-------------------------------------------------------------------------
	
	@Override
	public void propertyUpdate(Property property) {
		notifyListeners_propertiesUpdateProperty(property);
	}

	@Override
	public void ioOperationStart(Property property) {
		if (_inIOoperation == false) {
			reinitTotalSrcFiles();
			notifyListerners_ioOperationStart();
			logClear();
		}
	}
	
	@Override
	public void ioOperationOneFileProcessed(Property property) {
		notifyListeners_ioOperationOneFileProcessed(this);
	}
	
	@Override
	public void ioOperationOneFileNew(Property property) {
		notifyListeners_ioOperationOneFileNew(this);
	}
	
	@Override
	public void ioOperationCountSrcFilesDone(Property property) {
		if (_inIOoperation == false) {
			notifyListeners_ioOperationCountSrcFilesDone();
		}
	}
	

	@Override
	public void ioOperationEnd(Property property) {
		if (_inIOoperation == false) {
			notifyListerners_ioOperationEnd();
		}		
	}

	
	//-------------------------------------------------------------------------
	//-- Files IO (core)
	//-------------------------------------------------------------------------
	
	public long getTotalSrcFiles() {
		long count = 0;
		for (int i = 0; i < _properties.size(); ++i) {
			count += _properties.get(i).getTotalSrcFiles();
		}
		return count;
	}
	
	public long getTotalBackupedSrcFiles() {
		long count = 0;
		for (int i = 0; i < _properties.size(); ++i) {
			count += _properties.get(i).getTotalBackupedSrcFiles();
		}
		return count;
	}
	
	public long getTotalNewFilesSaved() {
		long count = 0;
		for (int i = 0; i < _properties.size(); ++i) {
			count += _properties.get(i).getTotalNewFilesSaved();
		}
		return count;
	}
	
	private void reinitTotalSrcFiles() {
		for (int i = 0; i < _properties.size(); ++i) {
			_properties.get(i).reinitTotalSrcFiles();
		}
	}
	
	synchronized public void backup() {
		ioOperation(Property.MODE_BACKUP);
	}
	
	synchronized public void restore() {
		ioOperation(Property.MODE_RESTORE);
	}
	
	private void ioOperation(int mode) {
		if (_inIOoperation == true) {
			System.err.println("ALREADY IO OPERATION???!!!");
		} else {
			_inIOoperation = true;
			
			Encryption encryption = null;
			try {
				encryption = getEncryption();
			} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
				logClear();
	    		logError(e.getMessage() + "\n");
	    		e.printStackTrace();
				_inIOoperation = false;
				notifyListerners_ioOperationEnd();
				return ;
			}
			
			notifyListerners_ioOperationStart();
			logClear();
			
			countFile(mode);
			notifyListeners_ioOperationCountSrcFilesDone();
			
			for (int i = 0; i < _properties.size(); ++i) {
				_properties.get(i).ioOperation(true, encryption, mode);
			}

			_inIOoperation = false;
			notifyListerners_ioOperationEnd();
		}
	}

	private void countFile(int mode) {
		long maxSrcPathLength = 0;
		for (int i = 0; i < _properties.size(); ++i) {
			maxSrcPathLength = Math.max(maxSrcPathLength, _properties.get(i).getSource(mode).length());
		}
		for (int i = 0; i < _properties.size(); ++i) {
			Property p = _properties.get(i);
			p.countFile(p.getSource(mode), maxSrcPathLength);
		}
	}
	
	public Encryption getEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
		for (int i = 0; i < _properties.size(); ++i) {
			if (_properties.get(i).getEncryption()) {
		        return new Encryption(_encryptionUI.askEncryptionKey());
			}
		}
		return null;
	}	
}
