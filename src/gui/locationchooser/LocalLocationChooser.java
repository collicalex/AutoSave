package gui.locationchooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileSystemView;


public class LocalLocationChooser extends LocationChooserPanel implements ActionListener {

	private static final long serialVersionUID = -672199529450127567L;
	private JRadioButton _letter;
	private JRadioButton _name;
	private JRadioButton _relative;
	private JFileChooser _fileChooser;
	private File _selectedFile = new File("./");
	
	public LocalLocationChooser(LocationChooserDialog parent) {
		super(parent);
		
		
		_letter = new JRadioButton("Drive Letter");
		_name = new JRadioButton("Drive Name");
		_relative = new JRadioButton("Relative Path");
		
		_letter.addActionListener(this);
		_name.addActionListener(this);
		_relative.addActionListener(this);
		
		_letter.setSelected(true);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(_letter);
		buttonGroup.add(_name);
		buttonGroup.add(_relative);
		
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.add(_letter);
		buttonPanel.add(_name);
		buttonPanel.add(_relative);
		
		//----
		
		JButton chooserButton = new JButton("Choose location");
		chooserButton.addActionListener(this);

		_fileChooser = new JFileChooser("./");
		_fileChooser.setDialogTitle("Select directory");
		_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		_fileChooser.setAcceptAllFileFilterUsed(false);

		this.add(buttonPanel);
		this.add(chooserButton);
	}

	@Override
	public void updateLocationString() {
		if (_selectedFile != null) {
			String path = null;
			if (_letter.isSelected()) {
				path = "localstorage:letter:" + _selectedFile.getAbsolutePath();
			} else if (_name.isSelected()) {
				String driveName = getDriveName(_selectedFile);
				if (driveName != null) {
					String currentLetter = Paths.get(_selectedFile.getAbsolutePath()).getRoot().toString();
					path = "localstorage:name:" + driveName + ":\\" + _selectedFile.getAbsolutePath().substring(currentLetter.length());
				}
			} else if (_relative.isSelected()) {
				path = getRealtivePath(new File("./"), _selectedFile);
				if (path != null) {
					path = "localstorage:relative:" + path;
				}
			}
			if (path != null) {
				_parent.setLocationString(path);
			}
		}
	}

	private String getDriveName(File file) {
		Path currentLetter = Paths.get(file.getAbsolutePath()).getRoot();
		
		List<File> files = Arrays.asList(File.listRoots());
		for (File f : files) {
			Path letter = Paths.get(f.getAbsolutePath()).getRoot();
			if (currentLetter.compareTo(letter) == 0) {
				String fullDeviceName = FileSystemView.getFileSystemView().getSystemDisplayName(f); //return : ESD-USB (F:)
				String deviceName = fullDeviceName.substring(0, fullDeviceName.lastIndexOf('(')).trim(); // return : ESD-USB
				return deviceName;
			}
		}
    	JOptionPane.showMessageDialog(this, "Unable to retrieve drive name from\n" + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
    	return null;		
	}
	
	private String getRealtivePath(File root, File file) {
        Path path = Paths.get(root.getAbsolutePath());
        Path passedPath = Paths.get(file.getAbsolutePath());
        try {
        	Path relativize = path.relativize(passedPath);
        	return relativize.toString();
        }  catch (IllegalArgumentException e) {
        	JOptionPane.showMessageDialog(this, file.getAbsolutePath() + "\nis not relative to current location\n" + root.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        	return null;
        }
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			if (_fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				_selectedFile = _fileChooser.getSelectedFile();
			}
		}
		updateLocationString();
	}
	
}
