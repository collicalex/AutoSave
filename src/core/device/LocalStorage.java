package core.device;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import core.copier.Copier;

public class LocalStorage extends Device {

	private File _file = null;
	
	//localstorage:typeId:Id:path
	//localstorage:name:Data:\test2
	//localstorage:letter:D:\test2
	//localstorage:relative:.\bla bla bla
	public LocalStorage(String path) throws IOException {
		String pathParts[] = path.split(":");
		if (pathParts.length < 3) {
			throw new IOException("Path \"" + path + "\" must contains at least 3 parameters");
		}
		
		String storageType = pathParts[0];
		if ("localstorage".compareTo(storageType.toLowerCase()) != 0) {
			throw new IOException("Storage type \"" + storageType + "\" in path \'" + path + "\' is not \"localstorage\"");
		}
		
		String typeId = pathParts[1];
		if ("name".compareTo(typeId.toLowerCase()) == 0) {
			int pos = path.indexOf(':', "localstorage:x".length());
			String value = path.substring(pos+1);
			String driveName = pathParts[2];
			File drive = retrieveDriveFromName(driveName, path);
			_file = new File(drive, value.substring(driveName.length() + 1));
		} else if ("letter".compareTo(typeId.toLowerCase()) == 0) {
			int pos = path.indexOf(':', "localstorage:x".length());
			String value = path.substring(pos+1);
			_file = new File(value);
		} else if ("relative".compareTo(typeId.toLowerCase()) == 0) {
			int pos = path.indexOf(':', "localstorage:x".length());
			String value = path.substring(pos+1);
			_file = new File(value);
		} else {
			throw new IOException("TypeId \"" + typeId + "\" in path \'" + path + "\' must be 'name', 'letter' or 'relative'");
		}
	}
	
	
	private File retrieveDriveFromName(String name, String path) throws IOException {
		name = name.trim();
		List<File> files = Arrays.asList(File.listRoots());
		
		File result = null;
		List<String> matched = new LinkedList<String>();
		
		for (File f : files) {
			String fullDeviceName = FileSystemView.getFileSystemView().getSystemDisplayName(f); //return : ESD-USB (F:)
			String deviceName = fullDeviceName.substring(0, fullDeviceName.lastIndexOf('(')).trim(); // return : ESD-USB
			if (name.compareTo(deviceName) == 0) {
				matched.add(fullDeviceName);
				result = f;
			}
		}
		
		if (matched.size() == 0) {
			throw new IOException("Unable to find a drive with name \"" + name + "\" from path \"" + path + "\"");
		} else if (matched.size() > 1) {
			throw new IOException("Too many drive with name \"" + name + "\" from path \"" + path + "\" found : " + matched);
		} else {
			return result;
		}
	}
	
	
	//-------------------------------------------------------------------------
	//-- Authentication methods
	//-------------------------------------------------------------------------
	
	@Override
	public boolean requiredCredentials() {
		return false;
	}


	@Override
	public void login(String username, String password) throws IOException {
	}


	@Override
	public void logout() throws IOException {
	}
	
	
	
	//-------------------------------------------------------------------------
	//-- Count files
	//-------------------------------------------------------------------------
	
	public File getEntryPoint() {
		return _file;
	}

	//-------------------------------------------------------------------------
	//-- Count files
	//-------------------------------------------------------------------------
	
	@Override
	public long countFiles(boolean recursive, List<Pattern> ignoredList) {
		return countFiles(_file, recursive, ignoredList);
	}
	
	private long countFiles(File file, boolean recursive, List<Pattern> ignoredList) {
		if (file.exists() == false) {
			return 0;
		}
		if (Copier.isIgnored(ignoredList, file.getName()) != null) {
			return 0;
		}
		if (file.isFile()) {
			return 1;
		}
		//file is a directory (not to be ignored)
		long res = 0;
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!(files[i].isDirectory() && (recursive == false))) {
				res += countFiles(files[i], recursive, ignoredList);
			}
		}
		return res;
	}

	
	//-------------------------------------------------------------------------
	//-- Target file and path
	//-------------------------------------------------------------------------	
	
	private File getTargetFile(String path) {
		return new File(_file, path);
	}
	
	@Override
	public String getTargetFullPath(String path) {
		File destination = getTargetFile(path);
		return destination.getAbsolutePath();
	}
	
	
	//-------------------------------------------------------------------------
	//-- Last modification date
	//-------------------------------------------------------------------------	
	
	@Override
	public long lastModified(String path) throws IOException {
		File destination = getTargetFile(path);
		return destination.lastModified();
	}
	
	
	//-------------------------------------------------------------------------
	//-- Copy one file
	//-------------------------------------------------------------------------

	@Override
	public void copyOneFile(File source, String path, List<FileTransfertListener> listeners) throws IOException {
		File destination = getTargetFile(path);
		copy_file(source, destination, listeners);
	}
	
    // Copies src file to dst file.
	private void copy_file(File src, File dst, List<FileTransfertListener> listeners) throws IOException {
    	dst.getParentFile().mkdirs();
    	
		InputStream fis = null;
		OutputStream fos = null;
    	ReadableByteChannel inputChannel = null;
    	WritableByteChannel outputChannel = null;
    	
    	try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dst);
    		
       		inputChannel = Channels.newChannel(fis);
       		outputChannel = Channels.newChannel(fos);
    		
       		notifyListenersFileTransferStart(listeners, src.getAbsolutePath(), src.length());
    		fastChannelCopy(inputChannel, outputChannel, listeners);
    		notifyListenersFileTransferFinish(listeners);
		} finally {
    		close(inputChannel);
    		close(outputChannel);
    		close(fis);
    		close(fos);
    	}
    	
    	/*
    	if (dst.setLastModified(src.lastModified()) ==  false) {
    		throw new IOException("Unalbe to set the last modification of destination file : " + dst.getAbsolutePath());
    	}
    	*/
    }
    
    
    //https://thomaswabner.wordpress.com/2007/10/09/fast-stream-copy-using-javanio-channels/
    private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest, List<FileTransfertListener> listeners) throws IOException {
    	final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    	while (src.read(buffer) != -1) {
    		// prepare the buffer to be drained
    		buffer.flip();
    		// write to the channel, may block
    		int byteWritten = dest.write(buffer);
    		notifyListenersFileTransferPart(listeners, byteWritten);
    		
    		// If partial transfer, shift remainder down
    		// If buffer is empty, same as doing clear()
    		buffer.compact();
    	}
    	// EOF will leave buffer in fill state
    	buffer.flip();
    	// make sure the buffer is fully drained.
    	while (buffer.hasRemaining()) {
    		int byteWritten = dest.write(buffer);
    		notifyListenersFileTransferPart(listeners, byteWritten);
    	}
	}  	

	private void close(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
}
