package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import gui.utils.GuiUtils;
import net.miginfocom.swing.MigLayout;

public class PasswordDialog extends JDialog {

	private static final long serialVersionUID = -1775339544789386172L;
	
    private final JTextField _username;
    private final JPasswordField _password;
    private final JCheckBox _isDefault;
    
    private boolean _credentialGiven = false;

    public PasswordDialog(final JFrame parent, boolean modal, BufferedImage icon, BufferedImage logo, String name, boolean multipleProperties) {
        super(parent, "Login", modal);

        LogoPanel logoPanel = new LogoPanel(logo, 25);
        
        JLabel usernameL = GuiUtils.setBold(new JLabel("Username :"));
        JLabel passwordL = GuiUtils.setBold(new JLabel("Password :"));
        
        _username = new JTextField(20);
        _password = new JPasswordField();
        _isDefault = new JCheckBox("Use these credentials for all " + name + " connections");
        
        _isDefault.setBackground(Color.WHITE);
        
        JPanel credentialsPanel = new JPanel(new MigLayout("fillx", "[][grow, fill]", "[]5[]"));
        credentialsPanel.add(usernameL);
        credentialsPanel.add(_username, "wrap");
        credentialsPanel.add(passwordL);
        credentialsPanel.add(_password, "wrap");
        
        if (multipleProperties) {
        	credentialsPanel.add(_isDefault, "span, wrap");
        }
        
        credentialsPanel.setBackground(Color.WHITE);
        
        JButton okButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBackground(Color.WHITE);
        
        
        this.add(logoPanel, BorderLayout.NORTH);
        this.add(credentialsPanel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        this.setBackground(Color.WHITE);
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.setIconImage(icon);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	_credentialGiven = true;
            	setVisible(false);
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	_credentialGiven = false;
            	_username.setText("");
            	_password.setText("");
                setVisible(false);
            }
        });
        
        this.setVisible(true);
    }
    
    public boolean isCredentialsGiven() {
    	return _credentialGiven;
    }
    
    public String getUsername() {
    	return _username.getText();	
    }
	
    public String getPassworzd() {
    	return String.valueOf(_password.getPassword());
    }
    
    public boolean isDefault() {
    	return _isDefault.isSelected();
    }
    
}
