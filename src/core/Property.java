package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
				countSrcFile(_src.length());
			}
			
			save_files(new File(_src), new File(_dst));
			
			_backuping = false;
			notifyListerners_ioOperationEnd();
		}
	}
	
	synchronized public void countSrcFile(long maxSrcPathLength) {
		_properties.logCountLabel(_src, maxSrcPathLength);
		_totalSrcFiles = nb_files(new File(_src));
		_properties.logCountValue(_totalSrcFiles);
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
	    return 1;
	}
	
	
	//-- Low level IO : Max path length ---------------------------------------
	
	private long maxPathLength(File[] files) {
		long maxPathLength = 0;
		for (int i = 0; i < files.length; i++) {
			String path = files[i].getAbsolutePath();
		    if (isIgnored(path) == false) {
		    	maxPathLength = Math.max(maxPathLength, path.length());
		    }
		}
		return maxPathLength;
	}
	
	
	//-- Low level IO : Copy files --------------------------------------------
	
	private class SrcDst {
		public File src;
		public File dst;
		public SrcDst(File src, File dst) {
			this.src = src;
			this.dst = dst;
		}
	}
	
	private void save_files(File src, File dst) {
		
	    if (isIgnored(src.getAbsolutePath())) {
	    	_properties.logSave(src.getAbsolutePath());
	    	_properties.logSkip(src.getAbsolutePath());
	    	return;
	    }
		
		Queue<SrcDst> dirs = new LinkedList<SrcDst>();
		dirs.add(new SrcDst(src, dst));
		while (!dirs.isEmpty()) {
			SrcDst sd = dirs.poll();
			_properties.logSave(sd.src.getAbsolutePath());
			File[] files = sd.src.listFiles();
			long maxPathLength = maxPathLength(files);
			for (File f : files) {
			    if (isIgnored(f.getAbsolutePath())) {
			    	_properties.logSkip(f.getAbsolutePath());
			    } else {
			    	File file_dst = new File(sd.dst.getPath(), f.getName());
			    	if (f.isFile()) {
						copy_file(f, file_dst, maxPathLength);
			    	} else if (f.isDirectory() && _recur) {
						dirs.add(new SrcDst(f, file_dst));
					}
			    }
			}
		}
	}
	
    // Copies src file to dst file.
    @SuppressWarnings("resource")
	private void copy_file(File src, File dst, long maxPathLength) {
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
    		_properties.logSimu(src.getAbsolutePath(), dst.getAbsolutePath(), maxPathLength);
    		return ;
    	} else {
    		_properties.logCopy(src.getAbsolutePath(), dst.getAbsolutePath(), maxPathLength);
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
