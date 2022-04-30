package core.properties;

import java.io.File;

public interface PropertiesListener {
	
	public void propertiesLoad(Properties properties, File file);
	public void propertiesAddProperty(Properties properties, Property property);
	public void propertiesRemoveProperty(Properties properties, Property property);
	public void propertiesSave(Properties properties, File file);

}
