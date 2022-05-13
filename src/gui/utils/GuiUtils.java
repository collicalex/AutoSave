package gui.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GuiUtils {
	
	public static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
    public static Container getOldestParent(Container container) {
    	while (container.getParent() != null) {
    		container = container.getParent();
    	}
    	return container;
    }
    
    /*
	public static JPanel stackNorth(List<JComponent> components) {
		JPanel last = new JPanel(new BorderLayout());
		JPanel first = last;
		for (JComponent jComponent : components) {
			last.add(jComponent, BorderLayout.NORTH);
			JPanel tmp = new JPanel(new BorderLayout());
			last.add(tmp, BorderLayout.CENTER);
			last = tmp;
		}
		return first;
	}
	*/   
	
	public static JLabel setBold(JLabel label) {
		Font f = label.getFont();
		label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		return label;
	}
	
	public static JLabel alignRight(JLabel label) {
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		return label;
	}
	
	public static JLabel setLink(JLabel label, final String url) {
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		label.setForeground(Color.GRAY);
		label.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				((JLabel)e.getSource()).setForeground(Color.GRAY);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				((JLabel)e.getSource()).setForeground(Color.BLUE);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (IOException ee) { 
					} catch (URISyntaxException e1) {
					}
				}
			}
		});
		return label;
	}	
}
