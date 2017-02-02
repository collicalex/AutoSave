package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.crypto.NoSuchPaddingException;

public class Property {
	
	public static int MODE_BACKUP = 0;
	public static int MODE_RESTORE = 1;
	
	private String _src = "";
	private String _dst = "";
	private boolean _recur = true;
	private List<String> _ignored;
	private boolean _encryption = false;
	
	private List<PropertyListener> _listeners;
	
	private boolean _inIOoperation = false;
	
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
		} else if (key.compareTo("encryption") == 0) {
			setEncryption(value);
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
	
	public void setEncryption(boolean value) {
		if (value != _encryption) {
			_encryption = value;
			notifyListerners_propertyUpdate();
		}
	}	
	
	private void setEncryption(String value) throws IOException {
		setEncryption(str2bool(value));
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
	
	public String getSource(int mode) {
		if (mode == MODE_BACKUP) {
			return _src;
		} else if (mode == MODE_RESTORE) {
			return _dst;
		} else {
			return null;
		}
	}
	
	public String getDestination(int mode) {
		if (mode == MODE_BACKUP) {
			return _dst;
		} else if (mode == MODE_RESTORE) {
			return _src;
		} else {
			return null;
		}
	}
	
	public boolean getRecursive() {
		return _recur;
	}
	
	public boolean getEncryption() {
		return _encryption;
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
		sb.append("encryption=" + this.getEncryption() + "\n");
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
	
	//-------------------------------------------------------------------------
	
	synchronized public void backup() {
		ioOperation(false, null, MODE_BACKUP);
	}

	synchronized public void restore() {
		ioOperation(false, null, MODE_RESTORE);
	}
	
	synchronized public void ioOperation(boolean countSrcFileAlreadyDone, Encryption encryption, int mode) {
		if (_inIOoperation == true) {
			System.err.println("ALREADY IO OPERATION???!!!");
		} else {
			_inIOoperation = true;
			notifyListerners_ioOperationStart();
			
			if (encryption == null) {
				try {
					encryption = _properties.getEncryption();
				} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
		    		_properties.logError(e.getMessage() + "\n");
		    		e.printStackTrace();
					_inIOoperation = false;
					notifyListerners_ioOperationEnd();
					return ;
				}
			}
			
			_totalBackupedSrcFiles = 0;
			
			if (countSrcFileAlreadyDone == false) {
				countFile(getSource(mode));
			}
			
			
			if (_encryption) {
				_properties.logEncryptionUsed();
			}
			
			try {
				save_files(new File(getSource(mode)), new File(getDestination(mode)), encryption, mode);
			} catch (IOException e) {
				_properties.logError(e.getMessage() + "\n");
				e.printStackTrace();
			}
			
			_inIOoperation = false;
			notifyListerners_ioOperationEnd();
		}
	}
	
	synchronized public void countFile(String dir) {
		countFile(dir, dir.length());
	}
	
	synchronized public void countFile(String dir, long maxPathLength) {
		_properties.logCountLabel(dir, maxPathLength);
		_totalSrcFiles = nb_files(new File(dir));
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
	
	private void save_files(File src, File dst, Encryption encryption, int mode) throws IOException {
		
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
			    	try {
				    	//String dstFilename = (encryption != null) ? (mode == Property.MODE_BACKUP) ? Encryption.caesarCipherEncrypt2(f.getName()) : Encryption.caesarCipherDecrypt2(f.getName())  : f.getName();
				    	String dstFilename = (encryption != null) ? (mode == Property.MODE_BACKUP) ? encryption.encrypt(f.getName(), f) : encryption.decrypt(f.getName())  : f.getName();
			    		//String dstFilename = f.getName();
				    	File file_dst = new File(sd.dst.getPath(), dstFilename);
				    	if (f.isFile()) {
							copy_file(f, file_dst, maxPathLength, encryption, mode);
				    	} else if (f.isDirectory() && _recur) {
							dirs.add(new SrcDst(f, file_dst));
						}
			    	} catch (Exception e) {
			    		e.printStackTrace();
			    	}
			    }
			}
		}
	}
	
    // Copies src file to dst file.
	private void copy_file(File src, File dst, long maxPathLength, Encryption encryption, int mode) {
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
    	
    	if (_properties.isSimulationOnly()) {
    		_properties.logSimu(src.getAbsolutePath(), dst.getAbsolutePath(), maxPathLength);
    		notifyListerners_ioOperationOneFileNew();
    		return ;
    	} else {
    		_properties.logCopy(src.getAbsolutePath(), dst.getAbsolutePath(), maxPathLength);
    	}

    	dst.getParentFile().mkdirs();
    	
    	
		InputStream fis = null;
		InputStream cfis = null;
		OutputStream fos = null;
		OutputStream cfos = null;
    	ReadableByteChannel inputChannel = null;
    	WritableByteChannel outputChannel = null;
    	
    	try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dst);
    		
    		if (encryption != null) {
    			if (mode == Property.MODE_BACKUP) {
    				cfos = encryption.encrypt(fos);
    				inputChannel = Channels.newChannel(fis);
    				outputChannel = Channels.newChannel(cfos);
    			} else if (mode == Property.MODE_RESTORE) {
    				cfis = encryption.decrypt(fis);
    				inputChannel = Channels.newChannel(cfis);
    				outputChannel = Channels.newChannel(fos);
    			}
    		} else {
        		inputChannel = Channels.newChannel(fis);
        		outputChannel = Channels.newChannel(fos);
    		}
    		
    		fastChannelCopy(inputChannel, outputChannel);
    		
    		_totlaNewFilesSaved++;
    		notifyListerners_ioOperationOneFileNew();
    	} catch (Exception e) {
    		_properties.logError(e.getMessage() + "\n");
    		e.printStackTrace();
    	} finally {
    		close(inputChannel);
    		close(outputChannel);
    		close(fis);
    		close(fos);
    		close(cfis);    		
    		close(cfos);
    	}
    	
    	if (dst.setLastModified(src.lastModified()) ==  false) {
    		_properties.logError("Unable to set the lastModifier datetime" + "\n");
    	}
    }
    
    
    //https://thomaswabner.wordpress.com/2007/10/09/fast-stream-copy-using-javanio-channels/
    private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
    	final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    	while (src.read(buffer) != -1) {
    		// prepare the buffer to be drained
    		buffer.flip();
    		// write to the channel, may block
    		dest.write(buffer);
    		// If partial transfer, shift remainder down
    		// If buffer is empty, same as doing clear()
    		buffer.compact();
    	}
    	// EOF will leave buffer in fill state
    	buffer.flip();
    	// make sure the buffer is fully drained.
    	while (buffer.hasRemaining()) {
    		dest.write(buffer);
    	}
	}    
    
	private void close(Channel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				_properties.logError(e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
    }
	
	private void close(InputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				_properties.logError(e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
	}

	private void close(OutputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				_properties.logError(e.getMessage() + "\n");
				e.printStackTrace();
			}
		}
	}


}

