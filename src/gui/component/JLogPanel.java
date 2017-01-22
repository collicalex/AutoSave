package gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import core.Logger;



public class JLogPanel extends JTable implements Logger {

	private static final long serialVersionUID = 5720382236542086335L;
	private LoggerTableModel _model;
	
	private Font _plainFont = new Font("Consolas", Font.PLAIN, 12);
	private Font _boldFont = new Font("Consolas", Font.BOLD, 12);
	
	public JLogPanel() {
		_model = new LoggerTableModel();
		this.setModel(_model);
		this.setTableHeader(null);
		this.setFillsViewportHeight(true);
		this.setDefaultRenderer(Date.class, new TimeCellRenderer());
		this.setDefaultRenderer(Action.class, new ActionCellRenderer());
		this.setDefaultRenderer(String.class, new StringCellRenderer());
		this.setFont(_plainFont);
		this.getModel().addTableModelListener(this);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		this.setShowGrid(false);
		this.setIntercellSpacing(new Dimension(0, 0));
	}
	
	public void log(final int action, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_model.addRow(action, text);
			}
		});
	}
	
	public void appendToLastLog(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_model.appendToLastLog(text);
			}
		});
	}
	
	public void clear() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_model.clear();;
			}
		});
	}
	
	private String spaces(long l) {
		StringBuilder str = new StringBuilder();
		for (long i = 0; i < l; ++i) {
			str.append(" ");
		}
		return str.toString();
	}
	
	//-------------------------------------------------------------------------
	//-- Override some JTable functions ---------------------------------------
	//-------------------------------------------------------------------------
	
	//Auto fit column width for all column except the last ones, which is autoresize
	//thanks to this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	@Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        if (row == 0) { //do it only when first row is displayed
        	if ((column+1) < getColumnModel().getColumnCount()) { //do it for all columns except last one
	        	int rendererWidth = component.getPreferredSize().width;
	        	TableColumn tableColumn = getColumnModel().getColumn(column);
	        	int width = rendererWidth + getIntercellSpacing().width;
	        	tableColumn.setMinWidth(width);
	        	tableColumn.setMaxWidth(width);
        	}
        }
        return component;
     }
	
	//-------------------------------------------------------------------------
	// Logger interface
	//-------------------------------------------------------------------------
	
	@Override
	public void logCopy(String src, String dst, long maxSrcPathLength) {
		this.log(Action.COPY, src + spaces(maxSrcPathLength - src.length()) + " --> "+  dst);
	}

	@Override
	public void logSimu(String src, String dst, long maxSrcPathLength) {
		this.log(Action.SIMU, src + spaces(maxSrcPathLength - src.length()) + " --> "+  dst);
	}

	@Override
	public void logSkip(String src) {
		this.log(Action.SKIP, src);
	}

	@Override
	public void logError(String text) {
		this.log(Action.ERROR, text);
	}

	@Override
	public void logClear() {
		this.clear();
	}

	@Override
	public void logSave(String dir) {
		this.log(Action.EMPTY, "");
		this.log(Action.SAVE, dir);
	}

	@Override
	public void logCountLabel(String src, long maxSrcPathLength) {
		this.log(Action.COUNT, src +  spaces(maxSrcPathLength - src.length()));		
	}

	@Override
	public void logCountValue(long value) {
		String vstr = "" + value;
		this.appendToLastLog(" " + spaces(10 - vstr.length()) + vstr);
	}	
	
	@Override
	public void logEncryptionUsed() {
		this.log(Action.EMPTY, "");		
		this.log(Action.ENCRYPTION, "Encryption will be used to save files");	
	}

	
	//-------------------------------------------------------------------------
	//-- Specific types--------------------------------------------------------
	//-------------------------------------------------------------------------
	
	public class RowData {

		private Object[] _data = new Object[3];
		
		public RowData(int action, String text) {
			this.init(new Action(action), text);
		}
		
		public RowData(Action action, String text) {
			this.init(action, text);
		}
		
		private void init(Action action, String text) {
			_data[0] = new Date();
			_data[1] = action;
			_data[2] = text;
		}
		
		public Object get(int columnIndex) {
			return _data[columnIndex];
		}
		
		public Date getDate() {
			return (Date) _data[0];
		}
		
		public Action getAction() {
			return (Action) _data[1];
		}
		
		public String getText() {
			return (String) _data[2];
		}

		public void setText(String text) {
			_data[2] = text;
		}
	}
	
	public class Action {

		public static final int COPY = 0;
		public static final int SIMU = 1;
		public static final int SKIP = 2;
		public static final int SAVE = 3;
		public static final int COUNT = 4;
		public static final int EMPTY = 5;
		public static final int ERROR = 6;
		public static final int ENCRYPTION = 7;
		
		private int _action;
		
		public Action(int action) {
			_action = action;
		}
		
		public boolean is(int action) {
			return _action == action;
		}

		public boolean isError() {
			return _action == ERROR;
		}
		
		public boolean isSkip() {
			return _action == SKIP;
		}
		
		public boolean isEmpty() {
			return _action == EMPTY;
		}
		
		public boolean isSave() {
			return _action == SAVE;
		}
		
		public boolean isEncryption() {
			return _action == ENCRYPTION;
		}
		
		//pad the action string on 5 characters
		@Override
		public String toString() {
			if (_action == COPY) {
				return "COPY ";
			} else if (_action == SIMU) {
				return "SIMU ";
			} else if (_action == SKIP) {
				return "SKIP ";
			} else if (_action == SAVE) {
				return "SAVE ";
			} else if (_action == COUNT) {
				return "COUNT";
			} else if (_action == EMPTY) {
				return "     ";				
			} else if (_action == ERROR) {
				return "ERROR";
			} else if (_action == ENCRYPTION) {
				return "CRYPT";
			} else {
				return "???? ";
			}
		}
		
	}
	
	//-------------------------------------------------------------------------
	//-- Table model ----------------------------------------------------------
	//-------------------------------------------------------------------------
	
	public class LoggerTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private String[] _columnNames = new String[]{"Date", "Action", "Text"};
		@SuppressWarnings("rawtypes")
		private Class[] _columnClasses = new Class[]{Date.class, Action.class, String.class};
		private List<RowData> _data;
		
		public LoggerTableModel() {
			_data = new ArrayList<RowData>(500);
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return _columnClasses[columnIndex];
		}

		@Override
		public String getColumnName(int columnIndex) {
			return _columnNames[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return _columnNames.length;
		}

		@Override
		public int getRowCount() {
			return _data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return _data.get(rowIndex).get(columnIndex);
		}
		
		public RowData getRowDataAt(int rowIndex) {
			return _data.get(rowIndex);
		}

		public void addRow(int action, String text) {
			_data.add(new RowData(action, text));
			fireTableRowsInserted(_data.size() - 1, _data.size());
		}

		public void clear() {
			_data = new ArrayList<RowData>(50);
			fireTableDataChanged();
		}
		
		public void appendToLastLog(String text) {
			int rowIndex = _data.size() - 1;
			if (rowIndex >= 0) {
				RowData rd = _data.get(rowIndex);
				rd.setText(rd.getText() + text);
				fireTableCellUpdated(rowIndex, 2);
			}
		}
	}	
	
	//-------------------------------------------------------------------------
	//-- Renderers ------------------------------------------------------------
	//-------------------------------------------------------------------------

	public class RowCellRenderer extends DefaultTableCellRenderer {
		
		private static final long serialVersionUID = 1L;
		
		@Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Action action = _model.getRowDataAt(row).getAction();
			if (action.isEmpty()) {
				value = "";
			}
			Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        if (action.isError()) {
	        	cmp.setForeground(Color.RED);
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.WHITE);
	        	}
	        } else if (action.isSkip()) {
	        	cmp.setForeground(Color.GRAY);
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.WHITE);
	        	}
	        } else if (action.isSave()) {
	        	cmp.setForeground(Color.BLACK);
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.LIGHT_GRAY);
	        	}
	        	cmp.setFont(_boldFont);
	        } else if (action.isEncryption()) {
	        	cmp.setForeground(new Color(0, 100, 0));
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.WHITE);
	        	}
	        	cmp.setFont(_boldFont);
	        } else {
	        	cmp.setForeground(Color.BLACK);
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.WHITE);
	        	}
	        }
	        return cmp;
	    }
	}
	
	public class LabelCellRenderer extends RowCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        cmp.setBackground(Color.LIGHT_GRAY);
	        cmp.setFont(_boldFont);
	        return cmp;
	    }
	}
	
	public class TimeCellRenderer extends LabelCellRenderer {

		private static final long serialVersionUID = 1L;
		private DateFormat _df;

	    public TimeCellRenderer() {
	    	_df = new SimpleDateFormat("[HH:mm:ss]");
	    }
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	if (value instanceof Date) {
	    		value = _df.format(value);
	    	}
	        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    }
	}
	
	public class ActionCellRenderer extends LabelCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	if (value instanceof Action) {
	    		value = value.toString();
	    	}
	    	return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    }
		
	}
	
	public class StringCellRenderer extends RowCellRenderer {

		private static final long serialVersionUID = 1L;

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        ((JComponent) cmp).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
	        return cmp;
	    }
	}	
	
}
