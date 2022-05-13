package gui.regexpbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import gui.icon.Icon;
import gui.utils.GuiUtils;
import net.miginfocom.swing.MigLayout;

public class RegexpBuilderDialog extends JDialog implements ActionListener, CaretListener {

	private static final long serialVersionUID = 1322342154509949983L;
	private boolean _isRegexpBuilt;
	
	private JTextField _extension;
	private JTextField _directory;
	private JTextField _regexp;
	
	private JTextField _testPath;
	private JTextField _matchedResult;
	

    public RegexpBuilderDialog(final JFrame parent) {
        super(parent, "Location Chooser", true);
        
 
        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(buildRegexpPanel());
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        this.setContentPane(contentPanel);
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setIconImage(Icon.decode(Icon.searchFolderIco));

        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	_isRegexpBuilt = true;
            	setVisible(false);
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	_isRegexpBuilt = false;
                setVisible(false);
            }
        });
        
        this.setVisible(true);
    }
    
    private JPanel buildRegexpPanel() {
    	JPanel panel = new JPanel();
    	
    	panel.setLayout(new MigLayout("fillx", "[][grow, fill][fill]"));
    	
    	panel.add(GuiUtils.setBold(new JLabel("Ignore all files :")), "span 2");
    	panel.add(GuiUtils.alignRight(GuiUtils.setLink(new JLabel("Help?"), "https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html")), "wrap");

    	_extension = new JTextField(30);
    	_directory = new JTextField(30);
    	_regexp = new JTextField(30);
    	_testPath = new JTextField();
    	_matchedResult = new JTextField();

    	_extension.addCaretListener(this);
    	_directory.addCaretListener(this);
    	_regexp.addCaretListener(this);
    	_testPath.addCaretListener(this);
    	
    	_matchedResult.setEditable(false);
    	_matchedResult.setBackground(Color.WHITE);

    	JButton browse = new JButton("...");
    	browse.addActionListener(this);
    	
    	
    	panel.add(new JLabel("- With extension:"));
    	panel.add(_extension, "span, wrap");
    	
    	panel.add(new JLabel("- In directory:"));
    	panel.add(_directory, "span, wrap");
    	
    	panel.add(GuiUtils.setBold(new JLabel("Regexp:")));
    	panel.add(_regexp, "span, wrap");
    	
    	panel.add(GuiUtils.setBold(new JLabel("Test against file:")));
    	panel.add(_testPath);
    	panel.add(browse, "wrap");
    	
    	panel.add(GuiUtils.setBold(new JLabel("Result:")));
    	panel.add(_matchedResult, "span, wrap");
    	
    	return panel;
    }
    

	public boolean isRegexpBuilt() {
		return _isRegexpBuilt;
	}

	public String getRegexp() {
		return _regexp.getText();
	}

	public void caretUpdate(CaretEvent e) {
		if ((e.getSource() == _directory) || (e.getSource() == _extension)) {
			buildRegexp();
		}
		if ((e.getSource() == _regexp) || (e.getSource() == _testPath)) {
			testRegexp();
		}
	}

	private void buildRegexp() {
		String regexp = "";
		
		if ((_directory.getText().trim().length() == 0) && (_extension.getText().trim().length() == 0)) {
			_regexp.setText("");
			return;
		}
		
		if (_directory.getText().trim().length() > 0) {
			String dir = _directory.getText().trim();
			if (dir.startsWith("\\")) {
				dir = dir.substring(1);
			}
			if (dir.endsWith("\\")) {
				dir = dir.substring(0, dir.length()-1);
			}
			
			
			dir = dir.replaceAll("\\\\", "\\\\\\\\");
			regexp = ".*\\\\" + dir + "\\\\.*";
		} else {
			regexp = ".*";
		}
		
		if (_extension.getText().trim().length() > 0) {
			String ext = _extension.getText().trim();
			if (ext.charAt(0) == '.') {
				ext = ext.substring(1);
			}
			ext = ext.replaceAll("\\.", "\\\\\\.");
			regexp += "\\." + ext;
		}
		
		_regexp.setText(regexp);
	}
	
	private void testRegexp() {
		if (_regexp.getText().trim().length() == 0) {
			_matchedResult.setForeground(Color.GRAY);
			_matchedResult.setText("no regexp");
			return ;
		}
		
		if (_testPath.getText().trim().length() == 0) {
			_matchedResult.setForeground(Color.GRAY);
			_matchedResult.setText("no file to test regexp");
			return ;
		}
		
		
		Pattern p = Pattern.compile(_regexp.getText().trim());
		Matcher m = p.matcher(_testPath.getText().trim());
		if (m.matches()) {
			_matchedResult.setForeground(new Color(0, 128, 0));
			_matchedResult.setText("MATCHED");
		} else {
			_matchedResult.setForeground(Color.RED);
			_matchedResult.setText("NOT MATCHED");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(); 
		if (_testPath.getText().trim().isEmpty() == false) {
			chooser.setSelectedFile(new File(_testPath.getText()));
		} else {
			chooser.setSelectedFile(new File("./"));
		}
	    chooser.setDialogTitle("Select file");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	_testPath.setText(chooser.getSelectedFile().getAbsolutePath());
	    }
	}
    
   
}
