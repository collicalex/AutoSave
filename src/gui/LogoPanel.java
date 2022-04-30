package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class LogoPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private Image _image;

	public LogoPanel(Image image, int border) {
		_image = image;
		Dimension dimension = new Dimension(image.getWidth(null) + border, image.getHeight(null) + border);
		this.setSize(dimension);
		this.setPreferredSize(dimension);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		//background
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		//logo
		int offsetX = (int)(0.5 + (getWidth() - _image.getWidth(null)) / 2.);
		int offsetY = (int)(0.5 + (getHeight() - _image.getHeight(null)) / 2.);
		
		g2d.drawImage(_image, offsetX, offsetY, null);
	}

}
