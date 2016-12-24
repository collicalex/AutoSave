package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class Property implements Logger {
	
	private boolean SIMU_ONLY = false;
	
	private String _src = "";
	private String _dst = "";
	private boolean _recur = true;
	
	private List<PropertyListener> _listeners;
	
	private boolean _backuping = false;
	
	private Properties _properties;
	
	public Property(Properties properties) {
		_properties = properties;
		_listeners = new LinkedList<PropertyListener>();
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
	//-- Logger
	//-------------------------------------------------------------------------
	
	@Override
	public void log(String text) {
		if (_properties != null) {
			_properties.log(text);
		}
	}

	@Override
	public void error(String text) {
		if (_properties != null) {
			_properties.error(text);
		}
	}
	
	@Override
	public void clear() {
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
	
	public String getSource() {
		return _src;
	}
	
	public String getDestination() {
		return _dst;
	}
	
	public boolean getRecursive() {
		return _recur;
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
				countSrcFile();
			}
			
			log("\nSaving from " + _src + "\n");
			save_files(new File(_src), new File(_dst));
			
			_backuping = false;
			notifyListerners_ioOperationEnd();
		}
	}
	
	synchronized public void countSrcFile() {
		log("Counting number of files in " + _src);
		_totalSrcFiles = nb_files(new File(_src));
		log("     " + _totalSrcFiles + "\n");
		notifyListerners_ioOperationCountSrcFilesDone();
	}
	
	
	//-- Low level IO ---------------------------------------------------------
	
	private long nb_files(File file) {
		long res = 0;
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						res++;
					} else if ((files[i].isDirectory()) && _recur) {
						res += nb_files(files[i]);
					}
				}
			} else {
				res++;
			}
		}
		return res;
	}
	
	private void save_files(File src, File dst) {
		log("Saving " + src.getAbsolutePath() + "\n");
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
			error("Source file doesn't exist! (" + src.getAbsolutePath() + ")\n");
		}
	}
	
    // Copies src file to dst file.
    @SuppressWarnings("resource")
	private void copy_file(File src, File dst) {
    	_totalBackupedSrcFiles++;
    	notifyListerners_ioOperationOneFileProcessed();
    	
    	if (dst.exists()) {
    		if (src.lastModified() <= dst.lastModified()) {
    			return ;
    		}
    	}
    	
    	log("New file : " + src.getAbsolutePath() + "\n");
    	notifyListerners_ioOperationOneFileNew();
    	
    	if (SIMU_ONLY) {
    		log("SIMU: " + src.getAbsolutePath() + " --> " + dst.getAbsolutePath() + "\n");
    		return ;
    	}
    	
    	dst.getParentFile().mkdirs();
    	
    	FileChannel srcChannel = null;
    	FileChannel dstChannel = null;
    	
    	try {
    		srcChannel = new FileInputStream(src).getChannel();
    		dstChannel = new FileOutputStream(dst).getChannel();
    		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    	} catch (Exception e) {
    		error(e.getMessage() + "\n");
    		e.printStackTrace();
    	} finally {
    		closeChannel(srcChannel);
    		closeChannel(dstChannel);
    	}
    	
    	_totlaNewFilesSaved++;
    	
    	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }	

    private void closeChannel(FileChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				error(e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
    }


}
