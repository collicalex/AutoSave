package gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.imageio.ImageIO;

public class Logo {
	private static Logo _instance = null;
	private BufferedImage _logo;
	
	
	private Logo() {
		createImage();
	}
	
	public static Logo getInstance() {	
		if (_instance == null) { 	
			synchronized(Logo.class) {
				if (_instance == null) {
					_instance = new Logo();
				}
			}
		}
		return _instance;
	}
	
	public Image getLogo() {
		return _logo.getScaledInstance(80, 80, Image.SCALE_DEFAULT);
	}
	
	public Image getIcon() {
		return _logo;
	}
	
	private void createImage() {
		_logo = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	
		_logo.setRGB(0, 0, -9078401);
		_logo.setRGB(0, 1, -13025464);
		_logo.setRGB(0, 2, -13025464);
		_logo.setRGB(0, 3, -13025464);
		_logo.setRGB(0, 4, -13025464);
		_logo.setRGB(0, 5, -13025464);
		_logo.setRGB(0, 6, -13025464);
		_logo.setRGB(0, 7, -13025464);
		_logo.setRGB(0, 8, -13025464);
		_logo.setRGB(0, 9, -13025464);
		_logo.setRGB(0, 10, -13025464);
		_logo.setRGB(0, 11, -13025464);
		_logo.setRGB(0, 12, -13025464);
		_logo.setRGB(0, 13, -13025464);
		_logo.setRGB(0, 14, 16777215);
		_logo.setRGB(0, 15, 16777215);
		_logo.setRGB(1, 0, -13025464);
		_logo.setRGB(1, 1, -4932923);
		_logo.setRGB(1, 2, -7958110);
		_logo.setRGB(1, 3, -7958110);
		_logo.setRGB(1, 4, -7958110);
		_logo.setRGB(1, 5, -7958110);
		_logo.setRGB(1, 6, -7958110);
		_logo.setRGB(1, 7, -7958110);
		_logo.setRGB(1, 8, -7958110);
		_logo.setRGB(1, 9, -7958110);
		_logo.setRGB(1, 10, -7958110);
		_logo.setRGB(1, 11, -7958110);
		_logo.setRGB(1, 12, -7958110);
		_logo.setRGB(1, 13, -7958110);
		_logo.setRGB(1, 14, -13025464);
		_logo.setRGB(1, 15, 16777215);
		_logo.setRGB(2, 0, -13025464);
		_logo.setRGB(2, 1, -13025464);
		_logo.setRGB(2, 2, -13025464);
		_logo.setRGB(2, 3, -13025464);
		_logo.setRGB(2, 4, -13025464);
		_logo.setRGB(2, 5, -13025464);
		_logo.setRGB(2, 6, -13025464);
		_logo.setRGB(2, 7, -13025464);
		_logo.setRGB(2, 8, -9078401);
		_logo.setRGB(2, 9, -10063486);
		_logo.setRGB(2, 10, -10063486);
		_logo.setRGB(2, 11, -10063486);
		_logo.setRGB(2, 12, -10063486);
		_logo.setRGB(2, 13, -10063486);
		_logo.setRGB(2, 14, -10063486);
		_logo.setRGB(2, 15, -13025464);
		_logo.setRGB(3, 0, -13025464);
		_logo.setRGB(3, 1, -1);
		_logo.setRGB(3, 2, -3485738);
		_logo.setRGB(3, 3, -2959651);
		_logo.setRGB(3, 4, -2367773);
		_logo.setRGB(3, 5, -1841430);
		_logo.setRGB(3, 6, -1315344);
		_logo.setRGB(3, 7, -723466);
		_logo.setRGB(3, 8, -13025464);
		_logo.setRGB(3, 9, -10063486);
		_logo.setRGB(3, 10, -8683384);
		_logo.setRGB(3, 11, -12433068);
		_logo.setRGB(3, 12, -12433068);
		_logo.setRGB(3, 13, -12433068);
		_logo.setRGB(3, 14, -12433068);
		_logo.setRGB(3, 15, -13025464);
		_logo.setRGB(4, 0, -13025464);
		_logo.setRGB(4, 1, -1);
		_logo.setRGB(4, 2, -3485738);
		_logo.setRGB(4, 3, -2959651);
		_logo.setRGB(4, 4, -2367773);
		_logo.setRGB(4, 5, -1841430);
		_logo.setRGB(4, 6, -1315344);
		_logo.setRGB(4, 7, -723466);
		_logo.setRGB(4, 8, -13025464);
		_logo.setRGB(4, 9, -10063486);
		_logo.setRGB(4, 10, -12433068);
		_logo.setRGB(4, 11, -1);
		_logo.setRGB(4, 12, -1);
		_logo.setRGB(4, 13, -1);
		_logo.setRGB(4, 14, -1);
		_logo.setRGB(4, 15, -13025464);
		_logo.setRGB(5, 0, -13025464);
		_logo.setRGB(5, 1, -1);
		_logo.setRGB(5, 2, -3485738);
		_logo.setRGB(5, 3, -2959651);
		_logo.setRGB(5, 4, -2367773);
		_logo.setRGB(5, 5, -1841430);
		_logo.setRGB(5, 6, -1315344);
		_logo.setRGB(5, 7, -723466);
		_logo.setRGB(5, 8, -13025464);
		_logo.setRGB(5, 9, -10063486);
		_logo.setRGB(5, 10, -12433068);
		_logo.setRGB(5, 11, -1);
		_logo.setRGB(5, 12, -13025464);
		_logo.setRGB(5, 13, -13025464);
		_logo.setRGB(5, 14, -1);
		_logo.setRGB(5, 15, -13025464);
		_logo.setRGB(6, 0, -13025464);
		_logo.setRGB(6, 1, -1);
		_logo.setRGB(6, 2, -3485738);
		_logo.setRGB(6, 3, -2959651);
		_logo.setRGB(6, 4, -2367773);
		_logo.setRGB(6, 5, -1841430);
		_logo.setRGB(6, 6, -1315344);
		_logo.setRGB(6, 7, -723466);
		_logo.setRGB(6, 8, -13025464);
		_logo.setRGB(6, 9, -10063486);
		_logo.setRGB(6, 10, -12433068);
		_logo.setRGB(6, 11, -1);
		_logo.setRGB(6, 12, -1);
		_logo.setRGB(6, 13, -1);
		_logo.setRGB(6, 14, -1);
		_logo.setRGB(6, 15, -13025464);
		_logo.setRGB(7, 0, -13025464);
		_logo.setRGB(7, 1, -1);
		_logo.setRGB(7, 2, -3485738);
		_logo.setRGB(7, 3, -2959651);
		_logo.setRGB(7, 4, -2367773);
		_logo.setRGB(7, 5, -1841430);
		_logo.setRGB(7, 6, -1315344);
		_logo.setRGB(7, 7, -723466);
		_logo.setRGB(7, 8, -13025464);
		_logo.setRGB(7, 9, -10063486);
		_logo.setRGB(7, 10, -12433068);
		_logo.setRGB(7, 11, -1);
		_logo.setRGB(7, 12, -1);
		_logo.setRGB(7, 13, -1);
		_logo.setRGB(7, 14, -1);
		_logo.setRGB(7, 15, -13025464);
		_logo.setRGB(8, 0, -13025464);
		_logo.setRGB(8, 1, -1);
		_logo.setRGB(8, 2, -3485738);
		_logo.setRGB(8, 3, -2959651);
		_logo.setRGB(8, 4, -2367773);
		_logo.setRGB(8, 5, -1841430);
		_logo.setRGB(8, 6, -1315344);
		_logo.setRGB(8, 7, -723466);
		_logo.setRGB(8, 8, -13025464);
		_logo.setRGB(8, 9, -10063486);
		_logo.setRGB(8, 10, -12433068);
		_logo.setRGB(8, 11, -1);
		_logo.setRGB(8, 12, -1);
		_logo.setRGB(8, 13, -1);
		_logo.setRGB(8, 14, -1);
		_logo.setRGB(8, 15, -13025464);
		_logo.setRGB(9, 0, -13025464);
		_logo.setRGB(9, 1, -1);
		_logo.setRGB(9, 2, -3485738);
		_logo.setRGB(9, 3, -2959651);
		_logo.setRGB(9, 4, -2367773);
		_logo.setRGB(9, 5, -1841430);
		_logo.setRGB(9, 6, -1315344);
		_logo.setRGB(9, 7, -723466);
		_logo.setRGB(9, 8, -13025464);
		_logo.setRGB(9, 9, -10063486);
		_logo.setRGB(9, 10, -12433068);
		_logo.setRGB(9, 11, -1);
		_logo.setRGB(9, 12, -1);
		_logo.setRGB(9, 13, -1);
		_logo.setRGB(9, 14, -1);
		_logo.setRGB(9, 15, -13025464);
		_logo.setRGB(10, 0, -13025464);
		_logo.setRGB(10, 1, -1);
		_logo.setRGB(10, 2, -3485738);
		_logo.setRGB(10, 3, -2959651);
		_logo.setRGB(10, 4, -2367773);
		_logo.setRGB(10, 5, -1841430);
		_logo.setRGB(10, 6, -1315344);
		_logo.setRGB(10, 7, -723466);
		_logo.setRGB(10, 8, -13025464);
		_logo.setRGB(10, 9, -10063486);
		_logo.setRGB(10, 10, -12433068);
		_logo.setRGB(10, 11, -1);
		_logo.setRGB(10, 12, -1);
		_logo.setRGB(10, 13, -1);
		_logo.setRGB(10, 14, -1);
		_logo.setRGB(10, 15, -13025464);
		_logo.setRGB(11, 0, -13025464);
		_logo.setRGB(11, 1, -1);
		_logo.setRGB(11, 2, -3485738);
		_logo.setRGB(11, 3, -2959651);
		_logo.setRGB(11, 4, -2367773);
		_logo.setRGB(11, 5, -1841430);
		_logo.setRGB(11, 6, -1315344);
		_logo.setRGB(11, 7, -723466);
		_logo.setRGB(11, 8, -13025464);
		_logo.setRGB(11, 9, -10063486);
		_logo.setRGB(11, 10, -12433068);
		_logo.setRGB(11, 11, -1);
		_logo.setRGB(11, 12, -1);
		_logo.setRGB(11, 13, -1);
		_logo.setRGB(11, 14, -1);
		_logo.setRGB(11, 15, -13025464);
		_logo.setRGB(12, 0, -13025464);
		_logo.setRGB(12, 1, -1);
		_logo.setRGB(12, 2, -3485738);
		_logo.setRGB(12, 3, -2959651);
		_logo.setRGB(12, 4, -2367773);
		_logo.setRGB(12, 5, -1841430);
		_logo.setRGB(12, 6, -1315344);
		_logo.setRGB(12, 7, -723466);
		_logo.setRGB(12, 8, -13025464);
		_logo.setRGB(12, 9, -10063486);
		_logo.setRGB(12, 10, -8683384);
		_logo.setRGB(12, 11, -12433068);
		_logo.setRGB(12, 12, -12433068);
		_logo.setRGB(12, 13, -12433068);
		_logo.setRGB(12, 14, -12433068);
		_logo.setRGB(12, 15, -13025464);
		_logo.setRGB(13, 0, -13025464);
		_logo.setRGB(13, 1, -13025464);
		_logo.setRGB(13, 2, -13025464);
		_logo.setRGB(13, 3, -13025464);
		_logo.setRGB(13, 4, -13025464);
		_logo.setRGB(13, 5, -13025464);
		_logo.setRGB(13, 6, -13025464);
		_logo.setRGB(13, 7, -13025464);
		_logo.setRGB(13, 8, -9078401);
		_logo.setRGB(13, 9, -10063486);
		_logo.setRGB(13, 10, -10063486);
		_logo.setRGB(13, 11, -10063486);
		_logo.setRGB(13, 12, -10063486);
		_logo.setRGB(13, 13, -10063486);
		_logo.setRGB(13, 14, -10063486);
		_logo.setRGB(13, 15, -13025464);
		_logo.setRGB(14, 0, -13025464);
		_logo.setRGB(14, 1, -4932923);
		_logo.setRGB(14, 2, -9997693);
		_logo.setRGB(14, 3, -9997693);
		_logo.setRGB(14, 4, -9997693);
		_logo.setRGB(14, 5, -9997693);
		_logo.setRGB(14, 6, -10063486);
		_logo.setRGB(14, 7, -10063486);
		_logo.setRGB(14, 8, -10063486);
		_logo.setRGB(14, 9, -10063486);
		_logo.setRGB(14, 10, -10063486);
		_logo.setRGB(14, 11, -10063486);
		_logo.setRGB(14, 12, -10063486);
		_logo.setRGB(14, 13, -10063486);
		_logo.setRGB(14, 14, -10063486);
		_logo.setRGB(14, 15, -13025464);
		_logo.setRGB(15, 0, -9078401);
		_logo.setRGB(15, 1, -13025464);
		_logo.setRGB(15, 2, -13025464);
		_logo.setRGB(15, 3, -13025464);
		_logo.setRGB(15, 4, -13025464);
		_logo.setRGB(15, 5, -13025464);
		_logo.setRGB(15, 6, -13025464);
		_logo.setRGB(15, 7, -13025464);
		_logo.setRGB(15, 8, -13025464);
		_logo.setRGB(15, 9, -13025464);
		_logo.setRGB(15, 10, -13025464);
		_logo.setRGB(15, 11, -13025464);
		_logo.setRGB(15, 12, -13025464);
		_logo.setRGB(15, 13, -13025464);
		_logo.setRGB(15, 14, -13025464);
		_logo.setRGB(15, 15, -9078401);

	}
	
	//-------------------------------------------------------------------------
	
	public static void read() {
		try  {
			BufferedImage img = ImageIO.read(new File("res\\floppy.png"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("res\\logo85.txt"))); 
			
			for (int i = 0; i < img.getWidth(); ++i) {
				for (int j = 0; j < img.getHeight(); ++j) {
					bw.write("_logo.setRGB("+ i + ", " + j + ", " + img.getRGB(i, j) + ");\n");
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
}
