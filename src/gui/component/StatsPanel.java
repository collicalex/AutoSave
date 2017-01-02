package gui.component;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import core.Properties;
import core.PropertiesListener;
import core.Property;
import gui.GuiUtils;

public class StatsPanel extends JPanel implements PropertiesListener, PropertiesPanelListener {

	private static final long serialVersionUID = -4513946935373261608L;

	private JLabel _nbFilesToCopy;
	private JLabel _nbFilesProcessed;
	private JLabel _nbFilesNew;
	
	private long _nFilesToCopyLong;
	private long _nbFilesProcessedLong;
	private long _nbFilesNewLong;
	
	private JLabel _startTime;
	private JLabel _ellapsedTime;
	private JLabel _remainingTime;
	private JLabel _endTime;
	private JLabel _endTilmeLabel;
	
	private long _startTimeMS;
	private long _ellapsedTimeMS;
	private long _remainingTimeMS;
	private long _endTimeMS;
	
	private JProgressBar _progressBar;
	
	public StatsPanel() {
		_nbFilesToCopy = new JLabel("", SwingConstants.RIGHT);
		_nbFilesProcessed = new JLabel("", SwingConstants.RIGHT);
		_nbFilesNew = new JLabel("", SwingConstants.RIGHT);
		JPanel leftPanel = new JPanel(new GridLayout(3, 2));
		leftPanel.add(createCaption("Source files"));
		leftPanel.add(_nbFilesToCopy);
		leftPanel.add(createCaption("Processed files"));
		leftPanel.add(_nbFilesProcessed);
		leftPanel.add(createCaption("New files"));
		leftPanel.add(_nbFilesNew);
		leftPanel.setBorder(new EmptyBorder(5, 5, 5, 25));
		
		_startTime = new JLabel("", SwingConstants.RIGHT);
		_endTime = new JLabel("", SwingConstants.RIGHT);
		_ellapsedTime = new JLabel("", SwingConstants.RIGHT);
		_remainingTime = new JLabel("", SwingConstants.RIGHT);
		JPanel rightPanel = new JPanel(new GridLayout(4, 2));
		rightPanel.add(createCaption("Start time"));
		rightPanel.add(_startTime);
		rightPanel.add(createCaption("Elapsed time"));
		rightPanel.add(_ellapsedTime);
		rightPanel.add(createCaption("Remaining time"));
		rightPanel.add(_remainingTime);
		_endTilmeLabel = createCaption("End time (estimated)"); 
		rightPanel.add(_endTilmeLabel);
		rightPanel.add(_endTime);
		rightPanel.setBorder(new EmptyBorder(5, 25, 5, 5));
		
		
		_progressBar = new JProgressBar();
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		
		
		this.setLayout(new BorderLayout());
		this.add(leftPanel, BorderLayout.WEST);
		
		JPanel hackPanel = new JPanel(new BorderLayout());
		hackPanel.add(rightPanel, BorderLayout.WEST);
		this.add(hackPanel, BorderLayout.CENTER);
		
		this.add(_progressBar, BorderLayout.SOUTH);
	}

	
	private JLabel createCaption(String caption) {
		JLabel label = new JLabel(caption + " :  ", SwingConstants.RIGHT);
		Font f = label.getFont();
		label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		return label;
	}
	
    private String formatTime(long nb) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String date[] = sdf.format(nb).split(":");
		return date[0] + "h" + date[1] + "m" + date[2] + "s";
    }
    
    private String formatDuration(long time) {
        int milliseconds = (int)(time % 1000);
        int seconds = (int)((time/1000) % 60);
        int minutes = (int)((time/60000) % 60);
        int hours = (int)((time/3600000) % 24);
        String secondsStr = (seconds<10 ? "0" : "")+seconds;
        String minutesStr = (minutes<10 ? "0" : "")+minutes;
        if (hours > 0)
            return new String(hours+"h"+minutesStr+"m"+secondsStr+"s");
        if (minutes > 0)
            return new String(minutes+"m"+secondsStr+"s");
        if (seconds > 0)
            return new String(seconds+"s");
        if (milliseconds > 0) {
        	return new String(milliseconds + "ms");
        }
        return new String("0s");
    }
    
    private String formatCount(long nb) {
    	return NumberFormat.getIntegerInstance().format(nb);
    }
	
	@Override
	public void ioOperationStart(Properties properties) {
		_startTimeMS = System.currentTimeMillis();
		_startTime.setText(formatTime(_startTimeMS));
		updateElaspedTime();
		_endTime.setText("");
		_endTilmeLabel.setText("End time (estimated) : ");
		
		_nbFilesProcessedLong = -1;
		_nbFilesNewLong = -1;
		
		_nbFilesToCopy.setText("Counting...");
		ioOperationOneFileProcessed(properties);
		ioOperationOneFileNew(properties);
	}
	

	@Override
	public void ioOperationCountSrcFilesDone(Properties properties) {
		_nFilesToCopyLong = properties.getTotalSrcFiles();
		_nbFilesToCopy.setText(formatCount(_nFilesToCopyLong));
	}

	@Override
	public void ioOperationOneFileProcessed(Properties properties) {
		updateElaspedTime();
		_nbFilesProcessedLong++;
		_nbFilesProcessed.setText(formatCount(_nbFilesProcessedLong));
		
		double ratio = (double)_nbFilesProcessedLong / (double)_nFilesToCopyLong;
		_progressBar.setValue((int)(ratio * 100.));
		
		estimateEndTime();
	}
	
	@Override
	public void ioOperationOneFileNew(Properties properties) {
		updateElaspedTime();
		_nbFilesNewLong++;
		_nbFilesNew.setText(formatCount(_nbFilesNewLong));
		estimateEndTime();
	}

	@Override
	public void ioOperationEnd(Properties properties) {
		_endTimeMS = System.currentTimeMillis();
		_endTime.setText(formatTime(_endTimeMS));
		_endTilmeLabel.setText("End time : ");		

		_remainingTimeMS = 0;
		_remainingTime.setText(formatDuration(_remainingTimeMS));
		
		updateElaspedTime();
		
		String message = _nbFilesNewLong + " file" + ((_nbFilesNewLong > 1) ? "s" : "") + " has been saved!";
		JOptionPane.showMessageDialog(GuiUtils.getOldestParent(this), "Backup done!\n" + message, "AutoSave", JOptionPane.INFORMATION_MESSAGE);
	}

	private void updateElaspedTime() {
		if (_endTilmeLabel.getText().contains("estimated")) {
			_ellapsedTimeMS = System.currentTimeMillis() - _startTimeMS;
		} else {
			_ellapsedTimeMS = _endTimeMS - _startTimeMS;
		}
		_ellapsedTime.setText(formatDuration(_ellapsedTimeMS));
	}
	
	private void estimateEndTime() {
		if (_nbFilesProcessedLong > 0) {
			long estimatedTotalEllapsedTimeMS = (_nFilesToCopyLong * _ellapsedTimeMS) / _nbFilesProcessedLong;
			_endTimeMS = _startTimeMS + estimatedTotalEllapsedTimeMS;
			_endTime.setText(formatTime(_endTimeMS));
			
			_remainingTimeMS = _endTimeMS - System.currentTimeMillis();
			if (_remainingTimeMS < 0) { //it must never happen, but to be sure...
				_remainingTimeMS = 0; 
			}
			_remainingTime.setText(formatDuration(_remainingTimeMS));
			
		} else {
			_endTime.setText("");
		}
	}

	@Override
	public void loadNewProperties(Properties oldProperties, Properties newProperties) {
		if (oldProperties != null) {
			oldProperties.removeListener(this);
		}
		if (newProperties != null) {
			newProperties.addListener(this);
		}
	}

	@Override
	public void propertiesLoad(Properties properties, File file) {
	}


	@Override
	public void propertiesAddProperty(Properties properties, Property property) {
	}


	@Override
	public void propertiesRemProperty(Properties properties, Property property) {
	}
	
	@Override
	public void propertiesUpdateProperty(Properties properties, Property property) {
	}


	@Override
	public void propertiesSave(Properties properties, File file) {
	}

	
}
