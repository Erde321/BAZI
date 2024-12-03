/*
 * @(#)EditableHeader.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/** <b>Title:</b> Klasse EditableHeader<br>
 * <b>Description:</b> Kopf einer JTable, der Oberflächenkomponenten enthalten kann<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @version 2.1
 * @author Jan Petzold, Florian Kluge, Robert Bertossi, Christian Brand */
public class EditableHeader extends JTableHeader implements CellEditorListener
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	public final int HEADER_ROW = -10;

	/** Nummer der Spalte, mit der zu bearbeitenden Komponente.
	 * @uml.property name="editingColumn" */
	transient protected int editingColumn;

	/** Bearbeitung der Kopfzellen.
	 * @uml.property name="cellEditor" */
	transient protected TableCellEditor cellEditor;

	/** Zu bearbeitende Oberflächenkomponente. */
	transient protected Component editorComp;

	/** Referenz auf das TablePane */
	private TablePane tpParent;

	/** Tabellenmodell */
	private WeightsTable wt;

	/** Spaltenmodell */
	private TableColumnModel tcm;

	/** Punkte für den Pfeil */
	private final int[] xp = {
			0, 5, 5, 5, 10, 5 };
	private final int[] yp = {
			10, 15, 0, 15, 10, 15 };
	int len = 6;
	int arrowHeight = 15;

	/** Konstruktor
	 * Erzeugt eine Instanz von EditableHeader.
	 * @param columnModel Spaltenmodell
	 * @param tpp Referenz auf das zugehörige TablePane
	 * @param w Referenz auf das Tabellenmodell */
	public EditableHeader(TableColumnModel columnModel, TablePane tpp,
			WeightsTable w)
	{
		super(columnModel);
		tpParent = tpp;
		setReorderingAllowed(false);
		cellEditor = null;
		recreateTableColumn(columnModel);
		this.wt = w;
		// kann erst jetzt gesetzt werden, da updateUI schon von super(...) aufgerufen wird
		((EditableHeaderUI) getUI()).setWT(wt);
		tcm = columnModel;
	}

	/** Aufruf, wenn das Look and Feel geändert wird. */
	public void updateUI()
	{
		// System.out.println("Call to EditableHeader::updateUI()");
		// um wt in der UI richtig zu setzen
		setUI(new EditableHeaderUI());
		((EditableHeaderUI) getUI()).setWT(wt);
		// --ende
		resizeAndRepaint();
		invalidate();
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		g.setColor(Color.GRAY);
		// g.fillPolygon(getX(0), getY(0), 3);
		// g.fillPolygon(getX(0), getYInv(0), 3);

		int x0 = tcm.getColumn(0).getWidth() - 15;
		int x1 = tcm.getColumn(0).getWidth() + tcm.getColumn(1).getWidth() - 15;

		int y0 = this.getHeight() / 4;

		// am rechten Rand der Spalte
		WeightsTableModel wtm = wt.getModel();
		int order = wtm.getOrder0();
		if (order == WeightsTableModel.DECREASING)
		{
			g.drawPolygon(getX(x0), getYInv(y0), len);
		}
		else if (order == WeightsTableModel.INCREASING)
		{
			g.drawPolygon(getX(x0), getY(y0), len);
		}
		order = wtm.getOrder1();
		if (order == WeightsTableModel.DECREASING)
		{
			g.drawPolygon(getX(x1), getYInv(y0), len);
		}
		else if (order == WeightsTableModel.INCREASING)
		{
			g.drawPolygon(getX(x1), getY(y0), len);
		}
		// super.paint(g);
	}

	/** Austauschen der normalen Spalten mit den Spalten mit den Komponenten im Kopf. */
	protected void recreateTableColumn(TableColumnModel columnModel)
	{
		int n = columnModel.getColumnCount();
		EditableHeaderTableColumn[] newCols = new EditableHeaderTableColumn[n];
		TableColumn[] oldCols = new TableColumn[n];
		for (int i = 0; i < n; i++)
		{
			oldCols[i] = columnModel.getColumn(i);
			newCols[i] = new EditableHeaderTableColumn();
			newCols[i].copyValues(oldCols[i]);
		}
		for (int i = 0; i < n; i++)
		{
			columnModel.removeColumn(oldCols[i]);
		}
		for (int i = 0; i < n; i++)
		{
			columnModel.addColumn(newCols[i]);
		}
	}

	/** Komponenten im Spaltenkopf erstellen.
	 * 
	 * @param index Spaltennummer.
	 * @param e Objekt, welches ein Ereignis auf der Zelle ausgelöst hat. */
	public boolean editCellAt(int index, EventObject e)
	{
		if (cellEditor != null && !cellEditor.stopCellEditing())
		{
			return false;
		}
		if (!isCellEditable(index))
		{
			return false;
		}
		TableCellEditor editor = getCellEditor(index);

		if (editor != null && editor.isCellEditable(e))
		{
			editorComp = prepareEditor(editor, index);
			editorComp.setBounds(getHeaderRect(index));
			// editorComp.setFont(GUIConstraints.LabelBoldFont);
			// System.out.println("Debug:hallo, bin auch da");
			add(editorComp);
			editorComp.validate();
			setCellEditor(editor);
			setEditingColumn(index);
			editor.addCellEditorListener(this);

			return true;
		}
		return false;
	}

	/** Gibt an, ob der Kopf der Spalte index eine Komponente aufnehmen kann. */
	public boolean isCellEditable(int index)
	{
		if (getReorderingAllowed())
		{
			return false;
		}
		int columnIndex = columnModel.getColumn(index).getModelIndex();
		EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel.
				getColumn(columnIndex);
		return col.isHeaderEditable();
	}

	/** Rückgabe des TableCellEditor der Spalte index. */
	public TableCellEditor getCellEditor(int index)
	{
		int columnIndex = columnModel.getColumn(index).getModelIndex();
		EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel.
				getColumn(columnIndex);
		return col.getHeaderEditor();
	}

	/** Rückgabe des TableCellEditor.
	 * @uml.property name="cellEditor" */
	public TableCellEditor getCellEditor()
	{
		return cellEditor;
	}

	/** Setzen des TableCellEditor für alle Spalten mit Komponenten im Kopf.
	 * @uml.property name="cellEditor" */
	public void setCellEditor(TableCellEditor newEditor)
	{
		TableCellEditor oldEditor = cellEditor;
		cellEditor = newEditor;

		if (oldEditor != null && oldEditor instanceof TableCellEditor)
		{
			((TableCellEditor) oldEditor).removeCellEditorListener((
					CellEditorListener) this);
		}
		if (newEditor != null && newEditor instanceof TableCellEditor)
		{
			((TableCellEditor) newEditor).addCellEditorListener((CellEditorListener) this);
		}
	}

	/** Vorbereiten der Komponente im Kopf der Spalte index. */
	public Component prepareEditor(TableCellEditor editor, int index)
	{
		Object value = columnModel.getColumn(index).getHeaderValue();
		boolean isSelected = true;
		int row = HEADER_ROW;
		JTable table = getTable();
		Component comp = editor.getTableCellEditorComponent(table, value,
				isSelected, row, index);
		if (comp instanceof JComponent)
		{
			// ((JComponent)comp).setNextFocusableComponent(this);
		}
		// comp.setFont(GUIConstraints.LabelBoldFont);
		return comp;
	}

	/** Rückgabe von zu bearbeitenden Komponente. */
	public Component getEditorComponent()
	{
		return editorComp;
	}

	/** Setzen der Spaltennummer, mit der zu bearbeitenden Komponente.
	 * @uml.property name="editingColumn" */
	public void setEditingColumn(int aColumn)
	{
		editingColumn = aColumn;
	}

	/** Rückgabe der Spaltennummer, mit der zu bearbeitenden Komponente.
	 * @uml.property name="editingColumn" */
	public int getEditingColumn()
	{
		return editingColumn;
	}

	/** Entfernen des TableCellEditor. */
	public void removeEditor()
	{
		TableCellEditor editor = getCellEditor();
		if (editor != null)
		{
			editor.removeCellEditorListener(this);

			requestFocus();
			remove(editorComp);

			int index = getEditingColumn();
			Rectangle cellRect = getHeaderRect(index);

			setCellEditor(null);
			setEditingColumn(-1);
			editorComp = null;

			repaint(cellRect);
		}
	}

	/** Rückgabe, ob eine Kopfzelle bearbeitet wird. */
	public boolean isEditing()
	{
		return (cellEditor == null) ? false : true;
	}

	/** Bearbeitung der Kopfzelle ist zu ende.
	 * Methode aus CellEditorListener. */
	public void editingStopped(ChangeEvent e)
	{
		TableCellEditor editor = getCellEditor();
		if (editor != null)
		{
			Object value = editor.getCellEditorValue();
			int index = getEditingColumn();
			columnModel.getColumn(index).setHeaderValue(value);
			// send Msg an TP
			// tpParent.setColumnName( (String) value, index);
			if (value == null)
			{
				value = "";
			}
			tpParent.setColumnName(value.toString(), index);
			removeEditor();
		}
	}

	/** Bearbetung der Kopfzelle ist abgebrochen wurden.
	 * Methode aus CellEditorListener. */
	public void editingCanceled(ChangeEvent e)
	{
		removeEditor();
	}

	private int[] getX(int xOffset)
	{
		int[] r = new int[len];
		for (int i = 0; i < len; i++)
		{
			r[i] = xp[i] + xOffset;
		}
		return r;
	}

	private int[] getY(int yOffset)
	{
		int[] r = new int[len];
		for (int i = 0; i < len; i++)
		{
			r[i] = yp[i] + yOffset;
		}
		return r;
	}

	private int[] getYInv(int yOffset)
	{
		/* int[] r = getY(yOffset);
		 * int[] rev = new int[len];
		 * for (int i=0; i<len; i++) {
		 * rev[i] = r[len-i-1];
		 * }
		 * return rev; */
		int[] r = new int[len];
		for (int i = 0; i < len; i++)
		{
			r[i] = yOffset - yp[i] + arrowHeight;
		}
		return r;
	}
}
