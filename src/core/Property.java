package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class Property {
	
	private String _src = "";
	private String _dst = "";
	private boolean _recur = true;
	private List<String> _ignored;
	
	private List<PropertyListener> _listeners;
	
	private boolean _backuping = false;
	
	private Properties _properties;
	
	public Property(Properties properties) {
		_properties = properties;
		_listeners = new LinkedList<PropertyListener>();
		_ignored = new LinkedList<String>();
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
	
	private void notifyListerners_propertyUpdate() {
		for (PropertyListener listener : _listeners) {
			listener.propertyUpdate(this);
		}
	}
	
	private void notifyListerners_ioOperationStart() {
		for (PropertyListener listener : _listeners) {
			listener.ioOperationStart(this);
		}
	}
	
	private void notifyListerners_ioOperationEnd() {
		for (PropertyListener listener : _listeners) {
			listener.ioOperationEnd(this);
		}
	}
	
	private void notifyListerners_ioOperationOneFileProcessed() {
		for (PropertyListener listener : _listeners) {
			listener.ioOperationOneFileProcessed(this);
		}
	}

	private void notifyListerners_ioOperationOneFileNew() {
		for (PropertyListener listener : _listeners) {
			listener.ioOperationOneFileNew(this);
		}
	}
	
	private void notifyListerners_ioOperationCountSrcFilesDone() {
		for (PropertyListener listener : _listeners) {
			listener.ioOperationCountSrcFilesDone(this);
		}
	}
	
	//-------------------------------------------------------------------------
	//-- Getters and Setters
	//-------------------------------------------------------------------------
	
	public void deleteMe() {
		_properties.removeProperty(this);
	}
	
	public void add(String str) throws IOException {
		int pos = str.indexOf('=');
		if (pos == -1) return ;
		String key = str.substring(0, pos);
		String value = str.substring(pos+1);
		
		if (key.compareTo("src") == 0) {
			setSource(value);
		} else if (key.compareTo("dst") == 0) {
			setDestination(value);
		} else if (key.compareTo("recur") == 0) {
			setRecursive(value);
		} else if (key.compareTo("ignore") == 0) {
			addToIgnoreList(value);
		} else {
			throw new IOException("Wrong property key '" + key + "'");
		}
	}
	
	private String isNull(String value) {
		return (value == null) ? "" : value;
	}
	
	public void setSource(String value) {
		value = isNull(value);
		if (value.compareTo(_src) != 0) {
			_src = value;
			notifyListerners_propertyUpdate();
		}
	}
	
	public void setDestination(String value) {
		value = isNull(value);
		if (value.compareTo(_dst) != 0) {
			_dst = value;
			notifyListerners_propertyUpdate();
		}
	}
	
	public void setRecursive(boolean value) {
		if (value != _recur) {
			_recur = value;
			notifyListerners_propertyUpdate();
		}
	}
	
	public void setRecursive(String value) {
		setRecursive(str2bool(value));
	}
	
	public void addToIgnoreList(String value) {
		if (_ignored.contains(value) == false) {
			_ignored.add(value);
			notifyListerners_propertyUpdate();
		}
	}
	
	public void removeFromIgnoreList(String value) {
		if (_ignored.contains(value) == true) {
			_ignored.remove(value);
			notifyListerners_propertyUpdate();
		}
	}
	
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
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("src=" + this.getSource() + "\n");
		sb.append("dst=" + this.getDestination() + "\n");
		sb.append("recur=" + bool2str(this.getRecursive()) + "\n");
		for (String ignored : _ignored) {
			sb.append("ignore=" + ignored + "\n");	
		}
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	//-- Files IO (core)
	//-------------------------------------------------------------------------
	
	private long _totalSrcFiles = 0;
	private long _totalBackupedSrcFiles = 0;
	private long _totlaNewFilesSaved = 0; 
	
	private long _maxPathLength = 0;
	
	public long getTotalSrcFiles() {
		return _totalSrcFiles;
	}
	
	public long getTotalBackupedSrcFiles() {
		return _totalBackupedSrcFiles;
	}
	
	public long getTotalNewFilesSaved() {
		return _totlaNewFilesSaved;
	}
	
	public void reinitTotalSrcFiles() {
		_totalSrcFiles = 0;
	}
	
	synchronized public void backup(boolean countSrcFileAlreadyDone) {
		if (_backuping == true) {
			System.err.println("ALREADY BACKUPING???!!!");
		} else {
			_backuping = true;
			notifyListerners_ioOperationStart();
			
			_totalBackupedSrcFiles = 0;
			
			if (countSrcFileAlreadyDone == false) {
				countSrcFile();
			}
			
			_properties.logText("\nSaving from " + _src + "\n");
			save_files(new File(_src), new File(_dst));
			
			_backuping = false;
			notifyListerners_ioOperationEnd();
		}
	}
	
	synchronized public void countSrcFile() {
		_properties.logText("Counting number of files in " + _src);
		_maxPathLength = 0;
		_totalSrcFiles = nb_files(new File(_src));
		_properties.logText("     " + _totalSrcFiles + "\n");
		notifyListerners_ioOperationCountSrcFilesDone();
	}
	
	
	//-- Low level IO : Count files -------------------------------------------
	
	private long nb_files(File file) {
		long res = 0;
		if (isIgnored(file.getAbsolutePath())) {
			return res;
		}		
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						res += count_file(files[i]);
					} else if ((files[i].isDirectory()) && _recur) {
						res += nb_files(files[i]);
					}
				}
			} else {
				res += count_file(file);
			}
		}
		return res;
	}
	
	private int count_file(File file) {
	    if (isIgnored(file.getAbsolutePath())) {
	    	return 0;
	    }
	    _maxPathLength = Math.max(_maxPathLength, file.getAbsolutePath().length());
	    return 1;
	}
	
	
	//-- Low level IO : Copy files --------------------------------------------
	
	private void save_files(File src, File dst) {
		_properties.logText("Saving " + src.getAbsolutePath() + "\n");
	    if (isIgnored(src.getAbsolutePath())) {
	    	_properties.logSkip(src.getAbsolutePath());
	    	return;
	    }
		if (src.exists()) {
			if (src.isDirectory()) {
				File[] files = src.listFiles();
				for (int i = 0; i < files.length; i++) {
					File file_dst = new File(dst.getPath(), files[i].getName());
					if (files[i].isFile()) {
						copy_file(files[i], file_dst);
					} else if ((files[i].isDirectory()) && _recur) {
						save_files(files[i], file_dst);
					}
				}
			} else {
				copy_file(src, dst);
			}
		} else {
			_properties.logError("Source file doesn't exist! (" + src.getAbsolutePath() + ")\n");
		}
	}
	
    // Copies src file to dst file.
    @SuppressWarnings("resource")
	private void copy_file(File src, File dst) {
	    if (isIgnored(src.getAbsolutePath())) {
	    	_properties.logSkip(src.getAbsolutePath());
	    	return;
	    }
    	
    	_totalBackupedSrcFiles++;
    	notifyListerners_ioOperationOneFileProcessed();
    	
    	if (dst.exists()) {
    		if (src.lastModified() <= dst.lastModified()) {
    			return ;
    		}
    	}
    	
    	notifyListerners_ioOperationOneFileNew();
    	
    	if (_properties.isSimulationOnly()) {
    		_properties.logSimu(src.getAbsolutePath(), dst.getAbsolutePath(), _maxPathLength);
    		return ;
    	} else {
    		_properties.logCopy(src.getAbsolutePath(), dst.getAbsolutePath(), _maxPathLength);
    	}

    	dst.getParentFile().mkdirs();
    	
    	FileChannel srcChannel = null;
    	FileChannel dstChannel = null;
    	
    	try {
    		srcChannel = new FileInputStream(src).getChannel();
    		dstChannel = new FileOutputStream(dst).getChannel();
    		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    	} catch (Exception e) {
    		_properties.logError(e.getMessage() + "\n");
    		e.printStackTrace();
    	} finally {
    		closeChannel(srcChannel);
    		closeChannel(dstChannel);
    	}
    	
    	_totlaNewFilesSaved++;
    }	

	private void closeChannel(FileChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				_properties.logError(e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
    }




}
