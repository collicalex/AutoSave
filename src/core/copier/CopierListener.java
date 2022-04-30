package core.copier;

import core.device.FileTransfertListener;

public interface CopierListener extends FileTransfertListener {

	public void copierCountStart(Object sourceEvent);
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy);
	
	public void copierCopyStart(Object sourceEvent);
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath);
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern);
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified);
	public void copierCopyFinish(Object sourceEvent);
	
}
