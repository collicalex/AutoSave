package gui.locationchooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import gui.utils.GuiUtils;
import net.miginfocom.swing.MigLayout;

public class PCloudLocationChooser extends LocationChooserPanel implements ActionListener, CaretListener {

	
	//pcloud:region:method:path
	//pcloud:us:http:dir1/dir2
	//pcloud:us:https:dir3
	//pcloud:ue:http:dir4/dir5/dir6
	//pcloud:ue:https:dir7	
	
	private static final long serialVersionUID = 1877695554680022364L;
	private JRadioButton _https;
	private JRadioButton _http;
	private JRadioButton _usa;
	private JRadioButton _europe;
	private JTextField _path;

	public PCloudLocationChooser(LocationChooserDialog parent) {
		super(parent);
		
		_usa = new JRadioButton("USA");
		_europe = new JRadioButton("Europe");
		_https = new JRadioButton("https");
		_http = new JRadioButton("http");
		
		_https.addActionListener(this);
		_http.addActionListener(this);
		_usa.addActionListener(this);
		_europe.addActionListener(this);	
		
		ButtonGroup buttonGroupRegion = new ButtonGroup();
		buttonGroupRegion.add(_usa);
		buttonGroupRegion.add(_europe);

		ButtonGroup buttonGroupProtocol = new ButtonGroup();
		buttonGroupProtocol.add(_https);
		buttonGroupProtocol.add(_http);
		
		_europe.setSelected(true);
		_https.setSelected(true);
		
		_path = new JTextField();
		_path.addCaretListener(this);
		
		
		this.setLayout(new MigLayout("fillx", "[][fill][][grow]", "[]0[]"));
		
		this.add(GuiUtils.setBold(new JLabel("Region:")));
		this.add(_europe);
		this.add(_usa);
		this.add(new JLabel(""), "wrap");
				
		this.add(GuiUtils.setBold(new JLabel("Protocol:")));
		this.add(_https);
		this.add(_http);
		this.add(new JLabel(""), "wrap");
		
		this.add(GuiUtils.setBold(new JLabel("Directory:")));
		this.add(_path, "span, wrap");
	}

	@Override
	public void updateLocationString() {
		String path = "pcloud:";
		
		if (_europe.isSelected()) {
			path += "ue:";
		} else { //usa
			path += "us:";
		}
		
		if (_https.isSelected()) {
			path += "https:";
		} else { //http
			path += "http:";
		}
		
		path += _path.getText();
		
		_parent.setLocationString(path);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateLocationString();
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		updateLocationString();
	}

}
