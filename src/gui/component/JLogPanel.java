package gui.component;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import core.Logger;

public class JLogPanel extends JTextPane implements Logger {

	private static final long serialVersionUID = 6344052382743475486L;
	
	private Style _styleLabel;
	private Style _styleSKIPLabel;
	private Style _styleSRC;
	private Style _styleDST;
	private Style _styleSKIP;
	
	private Style _styleNormal;
	private Style _errorStyle;
	
	public JLogPanel() {
		this.setEditable(false);
		this.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		_styleNormal = this.addStyle("logStyleNormal", null);
        StyleConstants.setBackground(_styleNormal, Color.WHITE);
        
		_styleLabel = this.addStyle("logStyleBold", null);
        StyleConstants.setBackground(_styleLabel, Color.WHITE);
        StyleConstants.setForeground(_styleLabel, Color.BLACK);
        StyleConstants.setBold(_styleLabel, true);
        

        _styleSRC = this.addStyle("logStyleSRC", null);
        StyleConstants.setBackground(_styleSRC, Color.WHITE);
        StyleConstants.setForeground(_styleSRC, Color.BLUE.darker());
        
        _styleDST = this.addStyle("logStyleDST", null);
        StyleConstants.setBackground(_styleDST, Color.WHITE);
        StyleConstants.setForeground(_styleDST, Color.BLUE);
       
        
        _styleSKIP = this.addStyle("logStyleSKIP", null);
        StyleConstants.setBackground(_styleSKIP, Color.WHITE);
        StyleConstants.setForeground(_styleSKIP, Color.GRAY);
        
        _styleSKIPLabel = this.addStyle("logStyleSKIPLabel", null);
        StyleConstants.setBackground(_styleSKIPLabel, Color.WHITE);
        StyleConstants.setForeground(_styleSKIPLabel, Color.GRAY);
        StyleConstants.setBold(_styleSKIPLabel, true);
        
        
        _errorStyle = this.addStyle("errorStyle", null);
        StyleConstants.setBackground(_errorStyle, Color.RED);
	}
	
	private void append(String text, Style style) {
		try {
			StyledDocument doc = (StyledDocument)this.getDocument();
			doc.insertString(doc.getLength(), text, style);
			this.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
    private String spaces(long l) {
		StringBuilder str = new StringBuilder();
		for (long i = 0; i < l; ++i) {
			str.append(" ");
		}
		return str.toString();
	}
	
    private void logSimuCopy(String prefix, String src, String dst, long maxSrcPathLength) {
    	append(prefix, _styleLabel);
		append(":  ", _styleLabel);
		append(src, _styleSRC);
		append(spaces(maxSrcPathLength - src.length()), _styleNormal);
		append("  -->  ", _styleNormal);
		append(dst, _styleDST);
		append("\n", _styleNormal);
    }
    
	//-------------------------------------------------------------------------
	// Logger interface
	//-------------------------------------------------------------------------
	
	@Override
	public void logCopy(String src, String dst, long maxSrcPathLength) {
		logSimuCopy("COPY", src, dst, maxSrcPathLength);
	}

	@Override
	public void logSimu(String src, String dst, long maxSrcPathLength) {
		logSimuCopy("SIMU", src, dst, maxSrcPathLength);
	}

	@Override
	public void logSkip(String src) {
    	append("SKIP", _styleSKIPLabel);
		append(":  ", _styleSKIPLabel);
		append(src, _styleSKIP);
		append("\n", _styleNormal);
	}
	
	@Override
	public void logText(String text) {
		append(text, _styleNormal);
	}

	@Override
	public void logError(String text) {
		append(text, _errorStyle);
	}

	@Override
	public void logClear() {
		this.setText("");
	}
	
	

}
