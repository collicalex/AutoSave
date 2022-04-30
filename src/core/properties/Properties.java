package core.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


public class Properties {

	private Vector<Property> _properties = new Vector<Property>();
	private List<PropertiesListener> _listeners;
	public File _srcFile;

	public Properties() {
		_listeners = new LinkedList<PropertiesListener>();
	}
	
	//-------------------------------------------------------------------------
	//-- Read / Write properties.ini file
	//-------------------------------------------------------------------------
	
	public void read(File file) throws IOException {
		_srcFile = file;
		_properties = new Vector<Property>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        
        Property property = new Property();
        boolean haveAddSomething = false;
        while ((str = br.readLine()) != null) {
        	str = str.trim();
        	if (str.length() == 0) { //empty line
        		if (haveAddSomething) {
        			this.addPropertyInternal(property);
        		}
    			property = new Property();
    			haveAddSomething = false;
        	} else {
        		property.add(str);
        		haveAddSomething = true;        			
        	}
        }
        br.close();
        if (haveAddSomething) {
        	this.addPropertyInternal(property);
        }
        notifyListeners_propertiesLoad(file);
        notifyListerners_propertiesAddProperties();
	}
	
	public void save(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(this.toString());
		bw.close();
		_srcFile = file;
		notifyListeners_propertiesSave(file);
	}
	
	//-------------------------------------------------------------------------
	//-- Getter
	//-------------------------------------------------------------------------
	
	
	public Vector<Property> getProperties() {
		return _properties;
	}
	
	//-------------------------------------------------------------------------
	//-- Add / Remove property
	//-------------------------------------------------------------------------
	
	private void addPropertyInternal(Property property) {
		_properties.add(property);
	}
	
	public void addProperty(Property property) {
		addPropertyInternal(property);
		notifyListerners_propertiesAddProperty(property);
	}

	public void removeProperty(Property property) {
		_properties.remove(property);
		notifyListerners_propertiesRemoveProperty(property);
	}
	
	
	//-------------------------------------------------------------------------
	//-- Others
	//-------------------------------------------------------------------------
	
	
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
	
	
	//-------------------------------------------------------------------------
	//-- Listeners
	//-------------------------------------------------------------------------	
	
	public void addListener(PropertiesListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(PropertiesListener listener) {
		_listeners.remove(listener);
	}
	
	private void notifyListerners_propertiesAddProperties() {
		for (Property property : _properties) {
			notifyListerners_propertiesAddProperty(property);
		}
	}
	
	private void notifyListerners_propertiesAddProperty(Property property) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesAddProperty(this, property);
		}
	}
	
	private void notifyListerners_propertiesRemoveProperty(Property property) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesRemoveProperty(this, property);
		}
	}
	
	private void notifyListeners_propertiesLoad(File file) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesLoad(this, file);
		}
	}		
	
	private void notifyListeners_propertiesSave(File file) {
		for (PropertiesListener listener : _listeners) {
			listener.propertiesSave(this, file);
		}
	}
	
	//-------------------------------------------------------------------------
	//-- toString
	//-------------------------------------------------------------------------		
	
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
}
