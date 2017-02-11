package gui.component;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class JPasswordDialog extends JDialog implements ActionListener, KeyListener {
	
	private static final long serialVersionUID = -7622202277007997897L;
	private JPasswordField _passwordField1;
	private JPasswordField _passwordField2;
	private JButton _okButton;

	public JPasswordDialog() {
		_passwordField1 = new JPasswordField(30);
		_passwordField2 = new JPasswordField(30);
		_passwordField1.addKeyListener(this);
		_passwordField2.addKeyListener(this);
		_passwordField1.addActionListener(this);
		_passwordField2.addActionListener(this);

		_okButton = new JButton("OK");
		_okButton.addActionListener(this);

		JPanel contentPanel = new JPanel();
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 0;
		
		contentPanel.setLayout(new GridBagLayout());

		cons.gridy = 0;	
		contentPanel.add(new JLabel("You have chosen to encrypt some files."), cons);
		
		cons.insets = new Insets(10,0,0,0);
		cons.gridy = 1;
		contentPanel.add(new JLabel("Please enter your secret key:"), cons);
		
		cons.insets = new Insets(0,0,0,0);
		cons.gridy = 2;
		contentPanel.add(_passwordField1, cons);

		cons.insets = new Insets(10,0,0,0);
		cons.gridy = 3;
		contentPanel.add(new JLabel("And a second time to prevent from mistyped error:"), cons);
		
		cons.insets = new Insets(0,0,0,0);
		cons.gridy = 4;
		contentPanel.add(_passwordField2, cons);
		
		
		cons.insets = new Insets(10,0,0,0);
		cons.gridy = 5;
		contentPanel.add(new JLabel("Be careful : Your secret key will not be saved anywhere."), cons);
		
		cons.insets = new Insets(0,0,0,0);
		cons.gridy = 6;
		contentPanel.add(new JLabel("You have to remember it. It will be unpossible to retrieve it."), cons);
		
		
		
		cons.insets = new Insets(10,0,0,0);
		cons.fill = GridBagConstraints.NONE;
		cons.gridy = 7;
		contentPanel.add(_okButton, cons);
		
		
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		_passwordField1.setText("");
		_passwordField2.setText("");
		_okButton.setEnabled(false);
		
		this.setContentPane(contentPanel);
		this.pack();
		
		this.setTitle("Secret Key");
		this.setResizable(false);
		this.setModal(true);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	  
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _okButton) {
			this.setVisible(false);
		} else if ((e.getSource() == _passwordField1) || (e.getSource() == _passwordField2)) {
			if (_okButton.isEnabled()) {
				this.setVisible(false);
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		char[] pwd1 = _passwordField1.getPassword();
		char[] pwd2 = _passwordField2.getPassword();

		boolean aresame = Arrays.equals(pwd1, pwd2) && (pwd1.length > 0) && (pwd2.length > 0);
		Arrays.fill(pwd1, (char)0);
		Arrays.fill(pwd2, (char)0);
       	_okButton.setEnabled(aresame);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}	
	
	public char[] getPassword() {
		char[] pwd1 = _passwordField1.getPassword();
		char[] pwd2 = _passwordField2.getPassword();

		if (Arrays.equals(pwd1, pwd2)) {
			Arrays.fill(pwd2, (char)0);
			return pwd1;
		} else {
			return null;
		}
	}
	
	public static String askPassword(Container parent) {
		JPasswordDialog dialog = new JPasswordDialog();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return new String(dialog.getPassword());
	}
}
