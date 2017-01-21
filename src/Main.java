import java.io.IOException;

import core.Encryption;
import gui.Gui;


//TODO: CODE
//TODO: Use sonar analysis
//TODO: use password field (but not the standard one, which is max 10 length)!

//TODO: FEATURES
//TODO: Option (action) to retrieve lost file from local computer into the backup
//TODO: Add a last backup datetime somewhere?
//TODO: Helper to create destinations directory in config panel (= default dest dir + src dir name)
//TODO: Add option for Log part to show only COPY/SIMU lines
//TODO: Manage pattern matching in ignore list
//TODO: Auto check new version at program startup?

//TODO: BUG
//TODO: BUG: Page up / down does not work in JLogPanel
public class Main {
	
	public static void main(String[] args) {
		/*
		try {
			String str = "Ceci est un test très très long. Et une autre phrase! Avec encore un autre bout de phrase :p";
			String enc = Encryption.caesarCipherEncrypt(str);
			String dec = Encryption.caesarCipherDecrypte(enc);
			System.out.println(str);
			System.out.println(enc);
			System.out.println(dec);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		new Gui();
	}
}
