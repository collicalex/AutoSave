package core.properties;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Property {

	private String _icon = null;
	private String _src = null;
	private String _dst = null;
	private boolean _recur = true;
	private List<String> _ignored;
	
	private List<PropertyListener> _listeners;
	
	
	public Property() {
		_listeners = new LinkedList<PropertyListener>();
		_ignored = new LinkedList<String>();
	}	
	
	
	//-------------------------------------------------------------------------
	//-- Add a property line key=value
	//-------------------------------------------------------------------------
	
	public void add(String str) throws IOException {
		int pos = str.indexOf('=');
		if (pos == -1) return ;
		String key = str.substring(0, pos);
		String value = str.substring(pos+1);
		this.add(key, value);
	}
	
	public void add(String key, String value) throws IOException {
		if (key.compareTo("src") == 0) {
			setSource(value);
		} else if (key.compareTo("dst") == 0) {
			setDestination(value);
		} else if (key.compareTo("recur") == 0) {
			setRecursive(value);
		} else if (key.compareTo("ignore") == 0) {
			addToIgnoreList(value);
		} else if (key.compareTo("icon") == 0) {
			setIcon(value);
		} else {
			throw new IOException("Wrong property key '" + key + "'");
		}	
	}
	
	//-------------------------------------------------------------------------
	//-- Setters
	//-------------------------------------------------------------------------
	
	public void setSource(String value) throws IOException {
		if (value == null) {
			throw new IOException("Unable to set source as null");
		}
		
		if (_src == null) {
			_src = value;
			notifyListerners_propertyUpdateSrc(value);
		} else if (value.compareTo(_src) != 0) {
			_src = value;
			notifyListerners_propertyUpdateSrc(value);
		}
	}
	
	public void setDestination(String value) throws IOException {
		if (value == null) {
			throw new IOException("Unable to set destination as null");
		}
		
		if (_dst == null) {
			_dst = value;
			notifyListerners_propertyUpdateDst(value);
		} else if (value.compareTo(_dst) != 0) {
			_dst = value;
			notifyListerners_propertyUpdateDst(value);
		}
	}
	
	public void setRecursive(boolean value) {
		if (value != _recur) {
			_recur = value;
			notifyListerners_propertyUpdateRecur(value);
		}
	}
	
	public void setRecursive(String value) {
		setRecursive(str2bool(value));
	}
	
	public void addToIgnoreList(String value) {
		if (_ignored.contains(value) == false) {
			_ignored.add(value);
			notifyListerners_propertyUpdateAddIgnore(value);
		}
	}
	
	public void removeFromIgnoreList(String value) {
		if (_ignored.contains(value) == true) {
			_ignored.remove(value);
			notifyListerners_propertyUpdateRemoveIgnore(value);
		}
	}
	
	public void setIcon(String value) throws IOException {
		if (value == null) {
			throw new IOException("Unable to set icon as null");
		}
		
		if (_icon == null) {
			_icon = value;
			notifyListerners_propertyUpdateIcon(value);
		} else if (value.compareTo(_icon) != 0) {
			_icon = value;
			notifyListerners_propertyUpdateIcon(value);
		}
	}
	
	//-------------------------------------------------------------------------
	//-- Getters
	//-------------------------------------------------------------------------	
	
	public String getSource() {
		return _src;
	}
	
	public String getDestination() {
		return _dst;
	}
	
	public boolean getRecursive() {
		return _recur;
	}
	
	public boolean isIgnored(String path) {
		return _ignored.contains(path);
	}
	
	public List<String> getIgnoredList() {
		return _ignored;
	}
	
	public String getIcon() {
		return _icon;
	}
	
	//-------------------------------------------------------------------------
	//-- Validor
	//-------------------------------------------------------------------------	
	
	public void check() throws IOException {
		if (_src == null) {
			throw new IOException("src is not defined");
		}
		if (_dst == null) {
			throw new IOException("dst is not defined");
		}
	}
	
	//-------------------------------------------------------------------------
	//-- Utils
	//-------------------------------------------------------------------------	
	
	private String bool2str(boolean bool) {
		return bool ? "true" : "false";
	}
	
	private boolean str2bool(String str) {
		if (str == null) {
			return false;
		} else if (str.toLowerCase().compareTo("yes") == 0) {
			return true;
		} else if (str.toLowerCase().compareTo("true") == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	
	//-------------------------------------------------------------------------
	//-- Listeners
	//-------------------------------------------------------------------------	
	
	public void addListener(PropertyListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(PropertyListener listener) {
		_listeners.remove(listener);
	}
	
	public void notifyListerner(PropertyListener listener) {
		listener.propertyUpdateSrc(this, _src);
		listener.propertyUpdateDst(this, _dst);
		listener.propertyUpdateRecur(this, _recur);
		for (String ignored : _ignored) {
			listener.propertyUpdateAddIgnore(this, ignored);
		}
		listener.propertyUpdateIcon(this, _icon);
	}
	
	private void notifyListerners_propertyUpdateSrc(String value) {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdateSrc(this, value);
		}
	}
	
	private void notifyListerners_propertyUpdateDst(String value) {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdateDst(this, value);
		}
	}
	
	private void notifyListerners_propertyUpdateRecur(boolean value) {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdateRecur(this, value);
		}
	}	
	
	private void notifyListerners_propertyUpdateAddIgnore(String value) {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdateAddIgnore(this, value);
		}
	}
	
	private void notifyListerners_propertyUpdateRemoveIgnore(String value) {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdateRemoveIgnore(this, value);
		}
	}
	
	private void notifyListerners_propertyUpdateIcon(String value) {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdateIcon(this, value);
		}
	}
	
	//-------------------------------------------------------------------------
	//-- toString
	//-------------------------------------------------------------------------		
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (_icon != null) {
			sb.append("icon=" + this.getIcon() + "\n");	
		}
		sb.append("src=" + this.getSource() + "\n");
		sb.append("dst=" + this.getDestination() + "\n");
		sb.append("recur=" + bool2str(this.getRecursive()) + "\n");
		for (String ignored : _ignored) {
			sb.append("ignore=" + ignored + "\n");	
		}
		return sb.toString();
	}	
	
}
