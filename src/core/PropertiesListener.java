package core;

import java.io.File;

public interface PropertiesListener {
	
	public void propertiesLoad(Properties properties, File file);
	public void propertiesAddProperty(Properties properties, Property property);
	public void propertiesRemProperty(Properties properties, Property property);
	public void propertiesUpdateProperty(Properties properties, Property property);
	public void propertiesSave(Properties properties, File file);
	
	public void ioOperationStart(Properties properties);
	public void ioOperationCountSrcFilesDone(Properties properties);
	public void ioOperationOneFileProcessed(Properties properties);
	public void ioOperationOneFileNew(Properties properties);
	public void ioOperationEnd(Properties properties);
}
