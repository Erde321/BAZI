/*
 * @(#)EditableHeaderTableColumn.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

/** <b>Title:</b> Klasse EditableHeaderTableColumn<br>
 * <b>Description:</b> Spalte einer JTable, deren Kopf Oberflächenkomponenten enthalten kann<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @version 2.1
 * @author Jan Petzold, Christian Brand */
public class EditableHeaderTableColumn extends TableColumn
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Bearbeitung der Oberflächenkomponente.
	 * @uml.property name="headerEditor" */
	protected TableCellEditor headerEditor;

	/** Zustand der Bearbeitung.
	 * @uml.property name="isHeaderEditable" */
	protected boolean isHeaderEditable;

	/** Erzeugt eine Instanz einer EditableHeaderTableColumn. */
	public EditableHeaderTableColumn()
	{
		setHeaderEditor(createDefaultHeaderEditor());
		isHeaderEditable = true;
	}

	/** Setzen des headerEditor.
	 * @uml.property name="headerEditor" */
	public void setHeaderEditor(TableCellEditor headerEditor)
	{
		this.headerEditor = headerEditor;
	}

	/** Rückgabe des headerEditor.
	 * @uml.property name="headerEditor" */
	public TableCellEditor getHeaderEditor()
	{
		return headerEditor;
	}

	/** Setzen des Zustandes der Bearbeitung.
	 * @uml.property name="isHeaderEditable" */
	public void setHeaderEditable(boolean isEditable)
	{
		isHeaderEditable = isEditable;
	}

	/** Angabe des Zustandes der Bearbeitung.
	 * @uml.property name="isHeaderEditable" */
	public boolean isHeaderEditable()
	{
		return isHeaderEditable;
	}

	/** Übernahme der Werte der normalen Spalte base auf diese Spalte. */
	public void copyValues(TableColumn base)
	{
		modelIndex = base.getModelIndex();
		identifier = base.getIdentifier();
		width = base.getWidth();
		minWidth = base.getMinWidth();
		setPreferredWidth(base.getPreferredWidth());
		maxWidth = base.getMaxWidth();
		headerRenderer = base.getHeaderRenderer();
		headerValue = base.getHeaderValue();
		cellRenderer = base.getCellRenderer();
		cellEditor = base.getCellEditor();
		isResizable = base.getResizable();
	}

	/** Erstellen einer Default-Komponente. */
	protected TableCellEditor createDefaultHeaderEditor()
	{
		JTextField tf = new JTextField();
		tf.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD)); 
		// Schrift in Textfeldern der Kopfzeile der ersten und zweiten
		return new DefaultCellEditor(tf); // Spalte der Eingabetabelle
	}
}
