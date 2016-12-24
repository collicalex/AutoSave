package gui.component;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JTextField2 extends JTextField implements DocumentListener {

	private static final long serialVersionUID = -8195098481755724196L;
	private List<JTextField2Listener> _listeners;
	
	public JTextField2(JTextField2Listener listener) {
		_listeners = new LinkedList<JTextField2Listener>();
		this.getDocument().addDocumentListener(this);
		this.addListener(listener);
	}
	
	public void addListener(JTextField2Listener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(JTextField2 listener) {
		_listeners.remove(listener);
	}
	
	public void notifyListeners() {
		for (JTextField2Listener listener : _listeners) {
			listener.jTextFieldUpdate(this);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		this.notifyListeners();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		this.notifyListeners();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		this.notifyListeners();
	}
	
}
