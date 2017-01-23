package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	
	public static JLabel setBold(JLabel label) {
		Font f = label.getFont();
		label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		return label;
	}
}
