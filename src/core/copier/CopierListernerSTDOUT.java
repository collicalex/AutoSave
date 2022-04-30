package core.copier;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CopierListernerSTDOUT implements CopierListener {

	private DateFormat _df;
	
	/*
	 COUNT START..
	 COUNT FINISH.
	 .COPY START..
	 .COPY FINISH.
	 .FILE COPIED.
	 .FILE IGNORED
	 .FILE SKIP...
	 */
	
	public CopierListernerSTDOUT() {
		_df = new SimpleDateFormat("HH:mm:ss");
	}
	
	private String getPrefix(String prefix) {
		return "[" + _df.format(new Date()) + "][" + prefix + "]";
	}
	
	public String convertTime(long time){
	    Date date = new Date(time);
	    Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    return format.format(date);
	}
	
	@Override
	public void copierCountStart(Object sourceEvent) {
		System.out.println(getPrefix("COUNT START  "));
	}

	@Override
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
		System.out.println(getPrefix("COUNT FINISH ") + " Total files : " + nbFilesToCopy);
	}

	@Override
	public void copierCopyStart(Object sourceEvent) {
		System.out.println(getPrefix(" COPY START  "));
	}

	@Override
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath) {
		System.out.println(getPrefix(" FILE COPIED ") + " " + srcPath + " TO " + dstPath);
	}

	@Override
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
		System.out.println(getPrefix(" FILE IGNORED") + " " + srcPath + " (" + matchedPattern + ")");
	}

	@Override
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
		String srcLastModifiedDate = convertTime(srcLastModified);
		String dstLastModifiedDate = convertTime(dstLastModified);
		System.out.println(getPrefix(" FILE SKIP   ") + " " + srcPath + " (" + srcLastModifiedDate + ") <= " + dstPath + "(" + dstLastModifiedDate + ")");
	}

	@Override
	public void copierCopyFinish(Object sourceEvent) {
		System.out.println(getPrefix(" COPY FINISH "));
	}

	@Override
	public void fileTransferStart(String path, long sizeToTransfert) {
	}

	@Override
	public void fileTransferPart(long sizeTransmitted) {
	}

	@Override
	public void fileTransferFinish() {
	}

}
