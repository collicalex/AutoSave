package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import gui.icon.Icon;
import gui.utils.GuiUtils;


public class About extends JDialog {

	private static final long serialVersionUID = 1L;
	private String _version = "v5.0 - 2022-05-01";

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
		contentPanel.add(new LogoPanel(Icon.decode(Icon.floppyIco, 80, 80, false), 10), cLogo);
		
		
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
		contentPanel.add(GuiUtils.setLink(new JLabel("Check source and latest release"),"https://collicalex.github.io/AutoSave/"), cGitHub);
		
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
		contentPanel.add(GuiUtils.setLink(new JLabel("Follow me on GitHub (Collicalex)"),"https://collicalex.github.io/"), cGitHubRoot);

		
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
	

	
}
