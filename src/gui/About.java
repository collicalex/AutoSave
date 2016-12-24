package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


public class About extends JDialog {

	private static final long serialVersionUID = 1L;
	private String _version = "v1.0 - 2016/12/24";

	public About(Frame frame) {
		super(frame, "About", true);
		
		JPanel contentPanel = new JPanel();

		contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints cLogo = new GridBagConstraints();
		cLogo.gridx = 0;
		cLogo.gridy = 0;
		cLogo.gridheight = 5;
		cLogo.insets = new Insets(0,0,0,25);
		contentPanel.add(new LogoPanel(), cLogo);
		
		
		GridBagConstraints cTitle = new GridBagConstraints();
		cTitle.gridx = 1;
		cTitle.gridy = 0;
		cTitle.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getTitleLabel(), cTitle);

		GridBagConstraints cVersion = new GridBagConstraints();
		cVersion.gridx = 1;
		cVersion.gridy = 1;
		cVersion.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getVersionLabel(), cVersion);
		
		GridBagConstraints cGitHub = new GridBagConstraints();
		cGitHub.gridx = 1;
		cGitHub.gridy = 2;
		cGitHub.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getWebLabel("Check source and latest release","https://github.com/collicalex/AutoSave/releases"), cGitHub);
		
		GridBagConstraints cAuthor = new GridBagConstraints();
		cAuthor.gridx = 1;
		cAuthor.gridy = 3;
		cAuthor.fill = GridBagConstraints.HORIZONTAL;
		cAuthor.insets = new Insets(20,0,0,0);
		contentPanel.add(getAuthorLabel(), cAuthor);
		
		GridBagConstraints cGitHubRoot = new GridBagConstraints();
		cGitHubRoot.gridx = 1;
		cGitHubRoot.gridy = 4;
		cGitHubRoot.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(getWebLabel("Follow me on GitHub (Collicalex)","https://github.com/collicalex/"), cGitHubRoot);

		
		this.setContentPane(contentPanel);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(frame);
	}
	
	private JLabel getTitleLabel() {
		JLabel title = new JLabel("Auto Save");
		title.setFont(title.getFont().deriveFont(24.0f));
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		return title;
	}
	
	private JLabel getVersionLabel() {
		JLabel version = new JLabel(_version);
		return version;
	}
	
	private JLabel getAuthorLabel() {
		JLabel author = new JLabel("Created by : Alexandre Bargeton");
		return author;
	}
	
	private JLabel getWebLabel(String caption, final String url) {
		JLabel flickr = new JLabel(caption);
		flickr.setCursor(new Cursor(Cursor.HAND_CURSOR));
		flickr.setForeground(Color.GRAY);
		flickr.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				((JLabel)e.getSource()).setForeground(Color.GRAY);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				((JLabel)e.getSource()).setForeground(Color.BLUE);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (IOException ee) { 
					} catch (URISyntaxException e1) {
					}
				}
			}
		});
		return flickr;
	}
	
	private class LogoPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public LogoPanel() {
			int border = 10;
			Dimension dimension = new Dimension(Logo.getInstance().getLogo().getWidth(null) + border, Logo.getInstance().getLogo().getHeight(null) + border);
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
			int offsetX = (int)(0.5 + (getWidth() - Logo.getInstance().getLogo().getWidth(null)) / 2.);
			int offsetY = (int)(0.5 + (getHeight() - Logo.getInstance().getLogo().getHeight(null)) / 2.);
			
			System.out.println(offsetX + " " + offsetY);
			
			g2d.drawImage(Logo.getInstance().getLogo(), offsetX, offsetY, null);
		}

		
	}
	
}
