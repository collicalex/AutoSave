package gui;

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

import core.copier.CopierListener;

public class LogPanel extends JTable implements CopierListener {

	private static final long serialVersionUID = 5720382236542086335L;
	private LoggerTableModel _model;
	
	private Font _plainFont = new Font("Consolas", Font.PLAIN, 12);
	private Font _boldFont = new Font("Consolas", Font.BOLD, 12);
	
	private Object _sourceEvent;
	
	
	public LogPanel() {
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
		this.setSelectionBackground(new Color(235, 235, 235));
	}
	
	public void log(final int action, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_model.addRow(action, text);
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
	
    public void setSourceEvent(Object sourceEvent) {
    	_sourceEvent = sourceEvent;
    }
    
    
	//-------------------------------------------------------------------------
	//-- CopierListener
	//-------------------------------------------------------------------------	


	@Override
	public void copierCountStart(Object sourceEvent) {
		if (sourceEvent == _sourceEvent) {
			this.clear();
			this.log(Action.COUNT_START, "Counting files to copy..."); 
		}
	}

	@Override
	public void copierCountFinish(Object sourceEvent, String parentDirectory, long nbFilesToCopy) {
		if (sourceEvent == _sourceEvent) {
			this.log(Action.COUNT_FINISH, "Total files to copy : " + StringUtils.formatCount(nbFilesToCopy)); 
		} else {
			this.log(Action.COUNT_FINISH, "  " + parentDirectory + " : " + StringUtils.formatCount(nbFilesToCopy) + " files");
		}
	}

	@Override
	public void copierCopyStart(Object sourceEvent) {
		if (sourceEvent == _sourceEvent) {
			this.log(Action.COPY_START, "Copying..."); 
		}
	}

	@Override
	public void copierFileCopied(Object sourceEvent, String srcPath, String dstPath) {
		this.log(Action.FILE_COPIED, srcPath + " --> "+  dstPath); 
	}

	@Override
	public void copierFileIgnored(Object sourceEvent, String srcPath, String matchedPattern) {
		this.log(Action.FILE_IGNORED, srcPath + " (" + matchedPattern + ")");
	}

	@Override
	public void copierFileSkip(Object sourceEvent, String srcPath, long srcLastModified, String dstPath, long dstLastModified) {
		this.log(Action.FILE_SKIPPED, srcPath + " (" + StringUtils.formatDateTime(srcLastModified) + ") <= " + dstPath + "(" + StringUtils.formatDateTime(dstLastModified) + ")");
	}

	@Override
	public void copierCopyFinish(Object sourceEvent) {
		if (sourceEvent == _sourceEvent) {
			this.log(Action.COPY_FINISH, "Copy finish!"); 
		}
	}
	
	@Override
	public void fileTransferStart(String path, long sizeToTransfert) {
	}

	@Override
	public void fileTransferPart(long sizeTransmitted) {
	}

	@Override
	public void fileTransferFinish() {
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
	//-- Specific types--------------------------------------------------------
	//-------------------------------------------------------------------------
	
	public class RowData {

		private Object[] _data = new Object[3];
		
		public RowData(Action action, String text) {
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
	}
	
	public class Action {

		public static final int COUNT_START = 0;
		public static final int COUNT_FINISH = 1;
		public static final int COPY_START = 2;
		public static final int COPY_FINISH = 3;
		public static final int FILE_COPIED = 4;
		public static final int FILE_IGNORED = 5;
		public static final int FILE_SKIPPED = 6;
		
		private int _action;
		
		public Action(int action) {
			_action = action;
		}
		
		public boolean is(int action) {
			return _action == action;
		}

		//TODO: pad the action string on 5 characters
		@Override
		public String toString() {
			if (_action == COUNT_START) {
				return "COUNT  ";
			} else if (_action == COUNT_FINISH) {
				return "COUNT  ";
			} else if (_action == COPY_START) {
				return "START  ";
			} else if (_action == COPY_FINISH) {
				return "FINISH ";
			} else if (_action == FILE_COPIED) {
				return "COPY   ";
			} else if (_action == FILE_IGNORED) {
				return "IGNORE ";				
			} else if (_action == FILE_SKIPPED) {
				return "SKIP   ";
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
			_data.add(new RowData(new Action(action), text));
			fireTableRowsInserted(_data.size() - 1, _data.size());
		}

		public void clear() {
			_data = new ArrayList<RowData>(50);
			fireTableDataChanged();
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
			Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        if (action.is(Action.FILE_IGNORED)) {
	        	cmp.setForeground(Color.LIGHT_GRAY);
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.WHITE);
	        	}
	        } else if (action.is(Action.FILE_SKIPPED)) {
	        	cmp.setForeground(Color.GRAY);
	        	if (isSelected == false) {
	        		cmp.setBackground(Color.WHITE);
	        	}
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
	        //cmp.setBackground(Color.LIGHT_GRAY);
	        //cmp.setFont(_boldFont);
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
