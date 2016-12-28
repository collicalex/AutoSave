package core;

public interface PropertyListener {
	
	public void propertyUpdate(Property property);
	public void ioOperationStart(Property property);
	public void ioOperationCountSrcFilesDone(Property property);
	public void ioOperationOneFileProcessed(Property property);
	public void ioOperationOneFileNew(Property property);
	public void ioOperationEnd(Property property);
	
}
