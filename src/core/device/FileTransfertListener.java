package core.device;

public interface FileTransfertListener {

	public void fileTransferStart(String path, long sizeToTransfert);
	public void fileTransferPart(long sizeTransmitted);
	public void fileTransferFinish();
	
}
