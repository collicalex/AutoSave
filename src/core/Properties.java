package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Properties extends LoggerAdapter implements PropertyListener {
	
	private Vector<Property> _properties = new Vector<Property>();
	private List<PropertiesListener> _listeners;
	private File _srcFile;
	
	private boolean _backuping = false;
	private boolean _simulationOnly = false;
	
	public Properties() {
		_listeners = new LinkedList<PropertiesListener>();
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
	
	private void notifyListerners_propertiesLoad() {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesLoad(this);
		}
	}

	private void notifyListeners_propertiesSave() {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesSave(this);
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
        notifyListerners_propertiesLoad();
	}
	
	public void save(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(this.toString());
		bw.close();
		notifyListeners_propertiesSave();
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
		Properties properties = new Properties();

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
		if (_backuping == false) {
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
		if (_backuping == false) {
			notifyListeners_ioOperationCountSrcFilesDone();
		}
	}
	

	@Override
	public void ioOperationEnd(Property property) {
		if (_backuping == false) {
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
		if (_backuping == true) {
			System.err.println("ALREADY BACKUPING???!!!");
		} else {
			_backuping = true;
			notifyListerners_ioOperationStart();
			logClear();
			
			for (int i = 0; i < _properties.size(); ++i) {
				_properties.get(i).countSrcFile();
			}
			
			notifyListeners_ioOperationCountSrcFilesDone();
			
			for (int i = 0; i < _properties.size(); ++i) {
				_properties.get(i).backup(true);
			}

			_backuping = false;
			notifyListerners_ioOperationEnd();
		}
	}

}
