package gui.component;

import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;

import core.Logger;

public class JLogPanel extends JTextArea implements Logger {

	private static final long serialVersionUID = 6344052382743475486L;
	
	public JLogPanel() {
		this.setEditable(false);
		this.setFont(new Font("Consolas", Font.PLAIN, 12));
		//((DefaultCaret) this.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);		
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
    	append(prefix+":");
		append("  ");
		append(src);
		append(spaces(maxSrcPathLength - src.length()));
		append("  -->  ");
		append(dst);
		append("\n");
    }
    
    private void logTime(boolean display) {
    	if (display) {
    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss");
    		append("[" + sdf.format(new Date()) + "] ");
    	} else {
    		append("           ");
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
    	append("SKIP:");
    	append("  ");
		append(src);
		append("\n");
	}
	

	@Override
	public void logError(String text) {
		append(text);
	}

	@Override
	public void logClear() {
		this.setText("");
	}

	@Override
	public void logSave(String dir) {
		logTime(false);
		append("     \n");
		logTime(true);
		append("SAVE:");
		append("  " + dir);
		append("\n");
	}

	@Override
	public void logCountLabel(String src, long maxSrcPathLength) {
		logTime(true);
		append("CNT :");
		append("  ");
		append(src);
		append(spaces(maxSrcPathLength - src.length()));
	}

	@Override
	public void logCountValue(long value) {
		String valuestr = "" + value;
		String file = "file" + ((value > 1) ? "s" : "");
		append(" " + spaces(10 - valuestr.length()));
		append(valuestr + " " + file + "\n");		
	}
}
