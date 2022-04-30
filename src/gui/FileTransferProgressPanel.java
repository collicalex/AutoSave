package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import core.copier.CopierListener;

public class FileTransferProgressPanel extends JPanel implements CopierListener {

	private static final long serialVersionUID = -3687902218589801816L;
	private Color _fgColor;
	private Font _font;
	private long _sizeToTransfert = 0;
	private long _sizeTransmitted = 0;
	private long _percent = 0;
	
	private String _sizeToTransfertStr = "";
	
	public FileTransferProgressPanel() {
		JProgressBar progressBar = new JProgressBar();
		_fgColor = progressBar.getForeground();
		_font = progressBar.getFont();
	}
	

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		if (_sizeToTransfert > 0) {
		
			g2.setColor(_fgColor);
			double p = (double)_percent / 100.;
			int width = (int)(this.getWidth() * p);
			g2.fillRect(0, 0, width, this.getHeight());
			
			g2.setColor(Color.BLACK);
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	        g2.setFont(_font);
	        FontMetrics metrics = g2.getFontMetrics();
	        int sx = (this.getWidth() - metrics.stringWidth(_sizeToTransfertStr)) / 2;
	        int sy = ((this.getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
	        g2.drawString(_sizeToTransfertStr, sx, sy);
		}
		
	}
	
    private String format(double bytes, int digits) {
        String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }
	
	
	//-------------------------------------------------------------------------
	//-- CopierListener
	//-------------------------------------------------------------------------		

	@Override
	public void copierCountStart(Object sourceEvent) {
	}

	@Override
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
	}

	@Override
	public void copierCopyStart(Object sourceEvent) {
	}

	@Override
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath) {
	}

	@Override
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
	}

	@Override
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
	}

	@Override
	public void copierCopyFinish(Object sourceEvent) {
	}
	
	@Override
	public void fileTransferStart(String path, long sizeToTransfert) {
		_sizeToTransfert = sizeToTransfert;
		_sizeToTransfertStr = format(_sizeToTransfert, 2);
		_sizeTransmitted = 0;
		_percent = 0;
		this.repaint();
	}

	@Override
	public void fileTransferPart(long sizeTransmitted) {
		_sizeTransmitted += sizeTransmitted;
		double percentD = (double)_sizeTransmitted / (double)_sizeToTransfert;
		int percentL = (int) (percentD * 100);
		percentL = Math.min(percentL, 100);
		if (percentL != _percent) {
			_percent = percentL;
			this.repaint();
		}
	}

	@Override
	public void fileTransferFinish() {
		_sizeToTransfert = 0;
		_sizeTransmitted = 0;
		_percent = 0;
		this.repaint();
	}	

}
