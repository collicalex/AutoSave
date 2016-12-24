package gui.component;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class JLogPanel extends JTextPane {

	private static final long serialVersionUID = 6344052382743475486L;

	private Style _logStyle;
	private Style _errorStyle;
	
	public JLogPanel() {
		this.setEditable(false);
		
		_logStyle = this.addStyle("logStyle", null);
        StyleConstants.setForeground(_logStyle, Color.BLACK);
        
        _errorStyle = this.addStyle("errorStyle", null);
        StyleConstants.setForeground(_errorStyle, Color.RED);
	}
	
	private void append(String text, Style style) {
		try {
			StyledDocument doc = (StyledDocument)this.getDocument();
			doc.insertString(doc.getLength(), text, style);
			this.setCaretPosition(doc.getLength());
			//JScrollBar bar = this.getVerticalScrollBar();
			//bar.setValue(bar.getMaximum());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void log(String text) {
		this.append(text, _logStyle);
	}
	
	public void error(String text) {
		this.append(text, _errorStyle);
	}

	public void clear() {
		this.setText("");
	}
}
