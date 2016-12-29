package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class EFont {

	
	private static EFont _instance = null;
	
	
	private EFont() {
	}
	
	public static EFont getInstance() {	
		if (_instance == null) { 	
			synchronized(Logo.class) {
				if (_instance == null) {
					_instance = new EFont();
				}
			}
		}
		return _instance;
	}
	
	public static void read() {
		try  {
			BufferedReader br = new BufferedReader(new FileReader(new File("res\\Inconsolata-Regular.ttf")));

			int i = 0;
			for (int c = br.read(); c != -1; c = br.read()) {
				System.out.println("_font["+i+"] = " + c);
				i++;
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
}
