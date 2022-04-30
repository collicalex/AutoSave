package core.copier;

import core.device.Device;

public interface CredentialsRequester {

	public Credentials requestCredential(Device device, boolean multipleProperties);
	
}
