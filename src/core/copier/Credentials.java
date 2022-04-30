package core.copier;

public class Credentials {
	public String username;
	public String password;
	public boolean isDefault;
	
	public Credentials(String u, String p, boolean d) {
		username = u;
		password = p;
		isDefault= d;
	}
}
