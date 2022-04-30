package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import core.copier.CopierListener;
import gui.utils.GuiUtils;

public class StatsPanel extends JPanel implements CopierListener {

	private static final long serialVersionUID = -4513946935373261608L;

	private JLabel _nbFilesToCopy;
	private JLabel _nbFilesCopied;
	private JLabel _nbFilesSkipped;
	private JLabel _nbFilesRemained;
	
	private long _nFilesToCopyLong;
	private long _nbFilesCopiedLong;
	private long _nbFilesSkippedLong;
	private long _nbFilesRemainedLong;
	
	private JLabel _startTime;
	private JLabel _ellapsedTime;
	private JLabel _remainingTime;
	private JLabel _endTime;
	
	private long _startTimeMS;
	private long _ellapsedTimeMS;
	private long _remainingTimeMS;
	private long _endTimeMS;
	
	private JProgressBar _progressBar;
	
	private Object _sourceEvent;
	private boolean _running = false;
	
	public StatsPanel() {
		
		//-- Left part
		
		JLabel nbFilesToCopyCaption = GuiUtils.setBold(new JLabel("Total : ", SwingConstants.RIGHT));
		JLabel nbFilesCopiedCaption  = GuiUtils.setBold(new JLabel("Copied : ", SwingConstants.RIGHT));
		JLabel nbFilesSkippedCaption  = GuiUtils.setBold(new JLabel("Skipped : ", SwingConstants.RIGHT));
		JLabel nbFilesRemainedCaption  = GuiUtils.setBold(new JLabel("Remaining : ", SwingConstants.RIGHT));
		
		_nbFilesToCopy = new JLabel("", SwingConstants.RIGHT);
		_nbFilesCopied = new JLabel("", SwingConstants.RIGHT);
		_nbFilesSkipped = new JLabel("", SwingConstants.RIGHT);
		_nbFilesRemained = new JLabel("", SwingConstants.RIGHT);
		
		JPanel leftPanel = new JPanel(new GridLayout(4, 2));
		leftPanel.add(nbFilesToCopyCaption);
		leftPanel.add(_nbFilesToCopy);
		leftPanel.add(nbFilesCopiedCaption);
		leftPanel.add(_nbFilesCopied);
		leftPanel.add(nbFilesSkippedCaption);
		leftPanel.add(_nbFilesSkipped);
		leftPanel.add(nbFilesRemainedCaption);
		leftPanel.add(_nbFilesRemained);
		leftPanel.setBorder(new EmptyBorder(5, 5, 5, 25));
		
		//-- Right part
		
		JLabel startTimeyCaption = GuiUtils.setBold(new JLabel("Start : ", SwingConstants.RIGHT));
		JLabel elapsedTimeCaption  = GuiUtils.setBold(new JLabel("Elapsed : ", SwingConstants.RIGHT));
		JLabel remainingTimeCaption  = GuiUtils.setBold(new JLabel("Remaining : ", SwingConstants.RIGHT));
		JLabel endTimeCaption = GuiUtils.setBold(new JLabel("End : ", SwingConstants.RIGHT));		
		
		_startTime = new JLabel("", SwingConstants.RIGHT);
		_endTime = new JLabel("", SwingConstants.RIGHT);
		_ellapsedTime = new JLabel("", SwingConstants.RIGHT);
		_remainingTime = new JLabel("", SwingConstants.RIGHT);
		
		JPanel rightPanel = new JPanel(new GridLayout(4, 2));
		rightPanel.add(startTimeyCaption);
		rightPanel.add(_startTime);
		rightPanel.add(elapsedTimeCaption);
		rightPanel.add(_ellapsedTime);
		rightPanel.add(remainingTimeCaption);
		rightPanel.add(_remainingTime);
		rightPanel.add(endTimeCaption);
		rightPanel.add(_endTime);
		rightPanel.setBorder(new EmptyBorder(5, 25, 5, 5));
		
		
		//-- Bottom part
		
		_progressBar = new JProgressBar();
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		
		//-- Put all together
		
		this.setLayout(new BorderLayout());
		this.add(leftPanel, BorderLayout.WEST);
		
		JPanel hackPanel = new JPanel(new BorderLayout());
		hackPanel.add(rightPanel, BorderLayout.WEST);
		this.add(hackPanel, BorderLayout.CENTER);
		
		this.add(_progressBar, BorderLayout.SOUTH);
	}

    private void resetAll() {
    	_nbFilesToCopy.setText("Counting...");
    	_nbFilesCopied.setText("");
    	_nbFilesSkipped.setText("");
    	_nbFilesRemained.setText("");
    	
    	_nFilesToCopyLong = 0;
    	_nbFilesCopiedLong = 0;
    	_nbFilesSkippedLong = 0;
    	_nbFilesRemainedLong = 0;    	
    	
    	_startTime.setText("");
    	_ellapsedTime.setText("");
    	_remainingTime.setText("");
    	_endTime.setText("");
    	
    	_startTimeMS = 0;
    	_ellapsedTimeMS = 0;
    	_remainingTimeMS = 0;
    	_endTimeMS = 0;
    	
    	_progressBar.setValue(0);	
    }
    
    private void reinitFilesCounters() {
    	_nbFilesCopiedLong = 0;
    	_nbFilesCopied.setText(StringUtils.formatCount(_nbFilesCopiedLong));
    	_nbFilesSkippedLong = 0;
    	_nbFilesSkipped.setText(StringUtils.formatCount(_nbFilesSkippedLong));
    	
    	updateNbFilesRemainedLong();
    }
    
    private void reinitTimersCounters() {
    	_startTimeMS = System.currentTimeMillis();
    	_startTime.setText(StringUtils.formatTime(_startTimeMS));
    	updateTimersCounters();
    }
    
    private void updateTimersCounters() {
		if (_running) {
			_ellapsedTimeMS = System.currentTimeMillis() - _startTimeMS;
		} else {
			_ellapsedTimeMS = _endTimeMS - _startTimeMS;
		}
		_ellapsedTime.setText(StringUtils.formatDuration(_ellapsedTimeMS));

    	
    	
    	long nbFilesProcessedLong = _nbFilesCopiedLong + _nbFilesSkippedLong;
		
		if (nbFilesProcessedLong > 0) {
			
			double estimatedTotalEllapsedTimeMS_d = (double)(_nFilesToCopyLong * _ellapsedTimeMS) / (double)nbFilesProcessedLong;
			long estimatedTotalEllapsedTimeMS = (int)(estimatedTotalEllapsedTimeMS_d + 0.5);
			
			_endTimeMS = _startTimeMS + estimatedTotalEllapsedTimeMS;
			_endTime.setText(StringUtils.formatTime(_endTimeMS));
			
			_remainingTimeMS = _endTimeMS - System.currentTimeMillis();
			if (_remainingTimeMS < 0) { //it must never happen, but to be sure...
				_remainingTimeMS = 0; 
			}
			_remainingTime.setText(StringUtils.formatDuration(_remainingTimeMS));
			
		} else {
			_endTime.setText("");
		}

    }
    
    private void setNbFilesToCopy(long nbFilesToCopy) {
    	_nFilesToCopyLong = nbFilesToCopy;
    	_nbFilesToCopy.setText(StringUtils.formatCount(_nFilesToCopyLong));
    }
    
    private void addOneFileCopied() {
    	_nbFilesCopiedLong++;
    	_nbFilesCopied.setText(StringUtils.formatCount(_nbFilesCopiedLong));
    	updateNbFilesRemainedLong();
    	updateTimersCounters();
    }
    
    private void addOneFileSkipped() {
    	_nbFilesSkippedLong++;
    	_nbFilesSkipped.setText(StringUtils.formatCount(_nbFilesSkippedLong));
    	updateNbFilesRemainedLong();
    	updateTimersCounters();
    }
    
	private void updateNbFilesRemainedLong() {
		long totalProcessed = _nbFilesCopiedLong + _nbFilesSkippedLong;
		
		_nbFilesRemainedLong = _nFilesToCopyLong - totalProcessed;
		_nbFilesRemained.setText(StringUtils.formatCount(_nbFilesRemainedLong));
		
		double ratio = (double)totalProcessed / (double)_nFilesToCopyLong;
		_progressBar.setValue((int)(ratio * 100d + 0.5d));
	}
    
    public void setSourceEvent(Object sourceEvent) {
    	_sourceEvent = sourceEvent;
    }
	
	//-------------------------------------------------------------------------
	//-- CopierListener
	//-------------------------------------------------------------------------	

	@Override
	public void copierCountStart(Object sourceEvent) {
		if (sourceEvent == _sourceEvent) {
			_running = true;
			resetAll();
		}
	}


	@Override
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
		if (sourceEvent == _sourceEvent) {
			setNbFilesToCopy(nbFilesToCopy);
		} else {
			setNbFilesToCopy(_nFilesToCopyLong + nbFilesToCopy);
		}
	}


	@Override
	public void copierCopyStart(Object sourceEvent) {
		if (sourceEvent == _sourceEvent) {
			reinitFilesCounters();
			reinitTimersCounters();
		}
	}


	@Override
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath) {
		addOneFileCopied();
	}


	@Override
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
	}


	@Override
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
		addOneFileSkipped();
	}

	@Override
	public void copierCopyFinish(Object sourceEvent) {
		if (sourceEvent == _sourceEvent) {
			_running = false;
			_endTimeMS = System.currentTimeMillis();
			updateTimersCounters();
		}
	}
	
	@Override
	public void fileTransferStart(String path, long sizeToTransfert) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileTransferPart(long sizeTransmitted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileTransferFinish() {
		// TODO Auto-generated method stub
		
	}	
}
