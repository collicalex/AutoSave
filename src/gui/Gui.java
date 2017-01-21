package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import gui.component.JLogPanel;
import gui.component.PropertiesPanel;
import gui.component.StatsPanel;

public class Gui {

	private JLogPanel _logPanel;
	private List<GuiListener> _listeners;
	private JFrame _jFrame;
	
	public Gui() {
		GuiUtils.setSystemLookAndFeel();
		_listeners = new LinkedList<GuiListener>();
		
		_logPanel = new JLogPanel();
		JPanel noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(_logPanel );
		JScrollPane logScrollPane = new JScrollPane(noWrapPanel);
		logScrollPane.getVerticalScrollBar().setUnitIncrement(15);
		new SmartScroller(logScrollPane);

		
		PropertiesPanel propertiesPanel = new PropertiesPanel(_logPanel);
		this.addListener(propertiesPanel);
		
		StatsPanel statPanel = new StatsPanel();
		propertiesPanel.addListener(statPanel);
		
		StandbyHibernateBlocker shb = new StandbyHibernateBlocker();
		propertiesPanel.addListener(shb);
		
		
        JLabel aboutLabel = new JLabel("About");
        aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aboutLabel.setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new CompoundBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY), new EmptyBorder(1, 4, 1, 5))));
        aboutLabel.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
		        e.getComponent().setForeground(Color.BLACK);
		        e.getComponent().setCursor(Cursor.getDefaultCursor());
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				e.getComponent().setForeground(Color.BLUE);
				e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				new About(_jFrame).setVisible(true);
			}
		});
        
		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.add(aboutLabel, BorderLayout.EAST);
		
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(statPanel, BorderLayout.NORTH);
		rightPanel.add(logScrollPane, BorderLayout.CENTER);
		rightPanel.add(statusBar, BorderLayout.SOUTH);
		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, propertiesPanel, rightPanel);
        splitPane.setContinuousLayout(true);

        _jFrame = new JFrame("AutoSave");
        _jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	notifyListeners();
            }
        });
        
        _jFrame.setContentPane(splitPane);
        _jFrame.setIconImage(Logo.getInstance().getIcon());
        _jFrame.pack();
        _jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		_jFrame.setLocationRelativeTo(null);
		_jFrame.setVisible(true);
	}
	
	public void addListener(GuiListener listener) {
		if (_listeners.contains(listener) == false) {
			_listeners.add(listener);
		}
	}
	
	public void removeListener(GuiListener listener) {
		_listeners.remove(listener);
	}
	
	private void notifyListeners() {
		for (GuiListener listener : _listeners) {
			listener.applicationClosing();
		}
	}
	
}

