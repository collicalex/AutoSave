package gui.component;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private Style _styleSKIPSRC;
	
	private Style _styleNormal;
	private Style _styleBold;
	private Style _errorStyle;
	
	public JLogPanel() {
		this.setEditable(false);
		this.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		_styleNormal    = createStyle(Color.BLACK, false, null);
		_styleBold      = createStyle(Color.BLACK, true, null);
		_styleLabel     = createStyle(Color.BLACK, true, new Color(225, 225, 225));
        _styleSRC       = createStyle(Color.BLUE.darker(), false, null);
        _styleDST       = createStyle(Color.BLUE, false, null);
        _styleSKIPLabel = createStyle(Color.GRAY, true, new Color(225, 225, 225));
        _styleSKIPSRC   = createStyle(Color.GRAY, false, null);
        _errorStyle     = createStyle(Color.RED, false, null);
	}
	
	private Style createStyle(Color fg, boolean isBold, Color bg) {
		String name = "fg" + fg.getRGB() + (isBold ? "BOLD" : "PLAIN") + (bg != null ? "bg" + bg.getRGB() : "");
		Style style = this.addStyle(name, null);
        StyleConstants.setForeground(style, fg);
        StyleConstants.setBold(style, isBold);
        if (bg != null) {
        	StyleConstants.setBackground(style, bg);
        }
        return style;
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
	
    private void logSrcToDst(String prefix, String src, String dst, long maxSrcPathLength) {
    	logTime(true);
    	append(prefix+":", _styleLabel);
		append("  ", _styleNormal);
		append(src, _styleSRC);
		append(spaces(maxSrcPathLength - src.length()), _styleNormal);
		append("  -->  ", _styleNormal);
		append(dst, _styleDST);
		append("\n", _styleNormal);
    }
    
    private void logTime(boolean display) {
    	if (display) {
    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss");
    		append("[" + sdf.format(new Date()) + "] ", _styleLabel);
    	} else {
    		append("           ", _styleLabel);
    	}
    }
    
	//-------------------------------------------------------------------------
	// Logger interface
	//-------------------------------------------------------------------------
	
	@Override
	public void logCopy(String src, String dst, long maxSrcPathLength) {
		logSrcToDst("COPY", src, dst, maxSrcPathLength);
	}

	@Override
	public void logSimu(String src, String dst, long maxSrcPathLength) {
		logSrcToDst("SIMU", src, dst, maxSrcPathLength);
	}

	@Override
	public void logSkip(String src) {
		logTime(true);
    	append("SKIP:", _styleSKIPLabel);
    	append("  ", _styleNormal);
		append(src, _styleSKIPSRC);
		append("\n", _styleNormal);
	}
	

	@Override
	public void logError(String text) {
		append(text, _errorStyle);
	}

	@Override
	public void logClear() {
		this.setText("");
	}

	@Override
	public void logSave(String dir) {
		logTime(false);
		append("     \n", _styleLabel);
		logTime(true);
		append("SAVE:", _styleLabel);
		append("  " + dir, _styleBold);
		append("\n", _styleNormal);
	}

	@Override
	public void logCountLabel(String src, long maxSrcPathLength) {
		logTime(true);
		append("CNT :", _styleLabel);
		append("  ", _styleNormal);
		append(src, _styleNormal);
		append(spaces(maxSrcPathLength - src.length()), _styleNormal);
	}

	@Override
	public void logCountValue(long value) {
		String valuestr = "" + value;
		String file = "file" + ((value > 1) ? "s" : "");
		append(" " + spaces(10 - valuestr.length()), _styleNormal);
		append(valuestr + " " + file + "\n", _styleNormal);		
	}
}
