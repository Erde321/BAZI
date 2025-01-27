/*
 * @(#)DistrictTable.java 2.1 18/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui.district;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.uni.augsburg.bazi.gui.EditableHeaderTableColumn;
import de.uni.augsburg.bazi.gui.GUIConstraints;
import de.uni.augsburg.bazi.gui.MultiplicityEvent;
import de.uni.augsburg.bazi.gui.MultiplicityListener;

/** <b>Title:</b> Klasse DistrictTable<br>
 * <b>Description:</b> Ein JPanel, das die Distrikttabelle darstellt und Änderungen verwaltet<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @version 2.1
 * @author Florian Kluge, Robert Bertossi, Christian Brand */

public class DistrictTable extends JPanel
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	// /** Auswahl von Minimum, Direktmandat und Diskrepanz. */
	// private JComboBox cMinDir;

	/** Eigentliche Java-Tabelle. */
	private JTable table;

	/** TableModel für die Tabelle. */
	private DistrictTableModel dtm;

	/** Überschrift der Nummernspalte. */
	private String number;

	/** Überschrift der Namenspalte. */
	private String name;

	/** Bezeichnung der Summe der Stimmen. */
	private String sum;

	/** Überschrift der Mandatsspalte
	 * @uml.property name="total" */
	private String total;

	/** Renderer für die Auswahlbox im Tabellenkopf. */
	private DefaultTableCellRenderer minRenderer;

	/** Container für die eigentliche Java-Tabelle. */
	private JScrollPane scrollPane;

	/** Spaltenbreiten. */
	private final int width_0 = 30;

	/** Spaltenbreiten. */
	private final int width_1 = 150;

	/** Spaltenbreiten. */
	private final int width_2 = 55;

	/** Schlange für die Änderungen */
	private DistrictQueue dq;

	/** Multiplicity Listener */
	private Vector<MultiplicityListener> multiListeners = new Vector<MultiplicityListener>();

	/** Konstruktor
	 * 
	 * @param pnumber Überschrift der Nummernspalte
	 * @param pname Überschrift der Namensspalte
	 * @param psum Bezeichnung der Summe der 3. Spalte
	 * @param ptotal Überschrift der Mandatsspalte
	 * @param q DistrictQueue, die verwendet werden soll
	 * @param lsl ListSelectionListener */
	public DistrictTable(String pnumber, String pname, String psum, String ptotal,
			DistrictQueue q, ListSelectionListener lsl)
	{
		this.number = pnumber;
		this.name = pname;
		this.sum = psum;
		this.total = ptotal;
		this.dq = q;

		dtm = new DistrictTableModel(number, name, sum, total);
		// deaktiviert um Synchronisation der Tabelle abzuschalten.
		// jetzt werden nur noch die Köpfe synchronisiert
		// wtm.addTableModelListener(new WTMListener());
		// erzeugen der JTable
		table = new JTable(dtm)
		{
			/** Default UID */
			private static final long serialVersionUID = 1L;

			public void editingStopped(ChangeEvent e)
			{
				super.editingStopped(e);
				// System.out.println("ES, Col: " + table.getEditingColumn() + " Row: " + table.getEditingRow());
				// System.out.println(e);
				// int erow = table.getEditingRow();
				int erow = table.getSelectedRow();
				// System.out.println("Row: " + erow);

				Integer itmp = (Integer) dtm.getValueAt(erow, 2);
				String acctmp;
				if (itmp == null)
				{
					acctmp = "";
				}
				else
				{
					acctmp = itmp.toString();
				}
				dq.enqueue(new District(erow + 1, (String) dtm.getValueAt(erow, 1),
						acctmp), QElem.UPD);
				reCalcurate(1);
				reCalcurate(2);
				repaint();
			}
		};
		//Zeilenhöhe
		if(GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN) > GUIConstraints.FONTSIZE_MIN) 
		{
			table.setRowHeight(GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN) + 10); 
		}
		table.setFont(GUIConstraints.getFont(GUIConstraints.INPUT_FONT_PLAIN));
		table.addKeyListener(new NewlineListener());
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.setColumnMargin(3);
		// table.setTableHeader(new EditableHeader(columnModel, tpParent));
		table.getTableHeader().setFont(GUIConstraints.getFont(GUIConstraints.INPUT_FONT_BOLD));

		// Eigenschaften der Spalten festlegen
		TableColumn column = null;
		for (int i = 0; i < 3; i++)
		{
			column = (TableColumn) table.getColumnModel().getColumn(i);
			if (i == 0)
			{
				column.setPreferredWidth(width_0);
				// column.setHeaderEditable(false);
				column.setCellEditor(createDefaultEditor());
			}
			else if (i == 1)
			{
				column.setPreferredWidth(width_1);
				// column.setHeaderEditable(true);
				column.setCellEditor(createDefaultEditor());
			}
			else if (i == 2)
			{
				column.setPreferredWidth(width_2);
				// column.setHeaderEditable(false);
				/* column.setHeaderValue(cMinDir.getSelectedItem());
				 * column.setHeaderRenderer(new ComboRenderer(items));
				 * column.setHeaderEditor(new DefaultCellEditor(cMinDir)); */
				column.setCellEditor(createDefaultEditorRight());					
			}
		}
		TableColumn tc;
		// Ausrichtung der Stimmenspalte nach rechts
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(JLabel.LEFT);
		tc = table.getColumnModel().getColumn(1);
		tc.setCellRenderer(dtcr);
		// Ausrichtung der Minimumsspalte nach rechts
		minRenderer = new DefaultTableCellRenderer();
		minRenderer.setHorizontalAlignment(JLabel.RIGHT);
		minRenderer.setEnabled(true);
		tc = table.getColumnModel().getColumn(2);
		tc.setCellRenderer(minRenderer);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(lsl);

		// erzeugen eines ScrollPane und einfuegen der Tabelle
		scrollPane = new JScrollPane(table);
		Insets scrollInsets = scrollPane.getInsets();
		int w = width_0 + width_1 + width_2 + scrollInsets.left +
				scrollInsets.right;
		int h = table.getRowHeight() * table.getRowCount() +
				table.getTableHeader().getPreferredSize().height
				+ scrollInsets.top + scrollInsets.bottom;
		scrollPane.setPreferredSize(new Dimension(w, h));

		// Einfuegen des ScrollPane in das Panel
		setLayout(new GridLayout(1, 0));
		add(scrollPane);
	}

	/** Setzt die Anzahl der Distrikte. Wird beim Erzeugen des DistrictDialog
	 * aufgerufen.
	 * 
	 * @param c Anzahl der Distrikte */
	void setCount(int c)
	{
		// Event generieren, falls nötig
		boolean fire = false;
		if (dtm.getCount() == 1 && c > 1 || dtm.getCount() > 1 && c == 1)
			fire = true;

		dtm.setCount(c);

		if (fire)
			fireMultiplicityEvent();
	}

	/** Aufsummieren der Einträge der Spalte mit dem übergebenen Index.
	 * 
	 * @param col Spaltenindex */
	private void reCalcurate(int col)
	{
		int iTotal = 0;
		double dTotal = 0.0f;
		boolean bDouble = false;
		for (int i = 0; i < dtm.getRowTotal(); i++)
		{
			Object obj = dtm.getValueAt(i, col);
			if (obj instanceof Integer)
			{
				iTotal += ((Integer) obj).intValue();
			}
			else if (obj instanceof Double)
			{
				bDouble = true;
				String temp = obj.toString();
				int newDigits = temp.length() - temp.indexOf(".") - 1;
				temp = Double.toString(dTotal);
				int oldDigits = temp.length() - temp.indexOf(".") - 1;
				int digits = Math.max(newDigits, oldDigits);
				dTotal += ((Double) obj).doubleValue();
				dTotal *= Math.pow(10.0, digits);
				dTotal = Math.round(dTotal);
				dTotal /= Math.pow(10.0, digits);
			}
		}
		if (dTotal + iTotal == 0.0)
		{
			dtm.setValueAt(null, dtm.getRowTotal(), col);
		}
		else
		{
			if (bDouble)
			{
				dtm.setValueAt(new Double(dTotal + new Integer(iTotal).doubleValue()),
						dtm.getRowTotal(), col);
			}
			else
			{
				dtm.setValueAt(new Integer(iTotal), dtm.getRowTotal(), col);
			}
		}
	}

	/** Setzt die Nummer der Zeile mit der Summe der Stimmen.
	 * 
	 * @param t Spaltenindex */
	public void setRowTotal(int t)
	{
		dtm.setRowTotal(t);
	}

	/** Setzt den Wert der Zelle mit den angegebenen Koordinaten
	 * 
	 * @param value String
	 * @param row Zeilenindex
	 * @param col Spaltenindex */
	public void setValueAt(String value, int row, int col)
	{
		dtm.setValueAt((Object) value, row, col);
		if (col == 2)
		{
			reCalcurate(2);
		}
		table.revalidate();
		repaint();
	}

	/** Rückgabe der Summe der Mandate.
	 * @return Summe der Mandate
	 * @uml.property name="total" */
	public int getTotal()
	{
		DistrictTableModel tm = (DistrictTableModel) table.getModel();
		Object obj = tm.getValueAt(tm.getRowTotal(), 2);
		return ((Integer) obj).intValue();
	}

	/** Rückgabe der Zeilenzahl
	 * 
	 * @return Zeilenanzahl */
	public int getRowTotal()
	{
		return dtm.getRowTotal();
	}

	/** Anpassen der Anzahl der Zeilen an das ScrollPane. */
	public void adaptRows()
	{
		double fRow = (scrollPane.getSize().height -
				(table.getTableHeader().getPreferredSize().height +
						scrollPane.getInsets().top + scrollPane.getInsets().bottom)) /
				table.getRowHeight();
		int iRow = (int) Math.floor(fRow) - 1;
		int rowTotal = dtm.getRowTotal();
		if (iRow > rowTotal)
		{
			dtm.setRowNumber(iRow);
			table.revalidate();
			repaint();
		}
	}

	/** Auslesen der markierten Zeilen
	 * @return Array mit den Zeilennummern */
	public int[] getSelectedRows()
	{
		return table.getSelectedRows();
	}

	/** Löschen der markierten Zeilen. */
	public void deleteSelectRows()
	{
		int[] rows = table.getSelectedRows();
		for (int i = rows.length; i > 0; i--)
		{
			dq.enqueue(new District(rows[i - 1] + 1, null, null), QElem.DEL);
			dtm.deleteRow(rows[i - 1]);
		}
		reCalcurate(1);
		reCalcurate(2);
		table.clearSelection();
		table.revalidate();
		repaint();

		adaptRows();

		// Event generieren, falls nun nur noch ein Distrikt da ist
		if (dtm.getCount() == 1)
			fireMultiplicityEvent();
	}

	/** Löschen von Zeilen
	 * @param rows Array mit den Zeilenindizes */
	public void deleteRows(int[] rows)
	{
		for (int i = rows.length; i > 0; i--)
		{
			dtm.deleteRow(rows[i - 1]);
		}
		reCalcurate(1);
		reCalcurate(2);
		table.clearSelection();
		table.revalidate();
		repaint();
		adaptRows();
	}

	/** Setzen des Namen der Spalte col.
	 * 
	 * @param name Überschrift
	 * @param col Spaltenindex */
	public void setColumnName(String name, int col)
	{
		EditableHeaderTableColumn column = (EditableHeaderTableColumn) table.
				getColumnModel().getColumn(col);
		column.setHeaderValue(new String(name));
		table.revalidate();
		repaint();
	}

	/** Rückgabe des Namen der Spalte col. */
	public String getColumnName(int col)
	{
		EditableHeaderTableColumn column = (EditableHeaderTableColumn) table.
				getColumnModel().getColumn(col);
		String name = (String) column.getHeaderValue();
		return name;
	}

	/** <b>Title:</b> Klasse ComboRenderer<br>
	 * <b>Description:</b> Darstellung der Combobox im Tabellenkopf<br>
	 * <b>Copyright:</b> Copyright (c) 2001<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Jan Petzold
	 * @version 2.0 */
	/* class ComboRenderer
	 * extends JComboBox
	 * implements TableCellRenderer { */

	/** Erzeugt eine Instanz von ConmboRenderer. */
	/* ComboRenderer(String[] items) {
	 * for (int i = 0; i < items.length; i++) {
	 * addItem(items[i]);
	 * }
	 * } */

	/** Implementierung von TableCellRenderer. */
	/* public Component getTableCellRendererComponent(JTable table, Object value,
	 * boolean isSelected, boolean hasFocus, int row, int column) {
	 * setSelectedItem(value);
	 * return this;
	 * }
	 * } */

	/** <b>Title:</b> Klasse NewlineListener<br>
	 * <b>Description:</b> Einfügen einer Leerzeile in die Tabelle bei Betätigung der Enter-Taste.<br>
	 * <b>Copyright:</b> Copyright (c) 2001<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Jan Petzold
	 * @version 2.0 */
	class NewlineListener
			extends KeyAdapter
	{

		/** Aufruf, wenn eine Taste gedrückt wurde. */
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
			{
				int sel = table.getSelectedRow();
				dtm.setNewline(sel);
				Object tmpo = dtm.getValueAt(sel + 1, 2);
				String tmps = (tmpo == null) ? null : ((Integer) tmpo).toString();
				dq.enqueue(new District(((Integer) dtm.getValueAt(sel + 1, 0)).
						intValue(), (String) dtm.getValueAt(sel + 1, 1),
						tmps), QElem.INS);
				table.repaint();
				table.revalidate();

				// Event generieren, falls nun 2 Distrikte vorhanden sind
				if (dtm.getCount() == 2)
					fireMultiplicityEvent();
			}
		}
	}

	/** Registriert einen MultiplicityListener
	 * 
	 * @param ml MultiplicityListener */
	public void addMultiplicityListener(MultiplicityListener ml)
	{
		multiListeners.add(ml);
	}

	/** Entfernt einen MultiplicityListener
	 * 
	 * @param ml MultiplicityListener
	 * @return <b>true</b> falls der übergebene MultiplicityListener registriert
	 *         war */
	public boolean removeMultiplicityListener(MultiplicityListener ml)
	{
		return multiListeners.remove(ml);
	}

	/** wird nur aufgerufen, wenn die Anzahl der Distrikte größer als 1 wird, oder wieder auf 1 gesetzt wird */
	private void fireMultiplicityEvent()
	{
		Iterator<MultiplicityListener> it = multiListeners.iterator();
		MultiplicityEvent me = new MultiplicityEvent(this, 0, dtm.getCount());
		while (it.hasNext())
			it.next().multiplicityChanged(me);
	}

	/** Erstellen von Default-Komponenten für Eingabetextfeler */
	protected TableCellEditor createDefaultEditor()
	{
		JTextField tf = new JTextField();
		tf.setFont(GUIConstraints.getFont(GUIConstraints.INPUT_FONT_PLAIN)); // Schrift in Textfeldern der ersten und zweiten
		return new DefaultCellEditor(tf); // Spalte der Eingabetabelle setzen
	}
	
	/** dasselbe rechtsbuendig */
	protected TableCellEditor createDefaultEditorRight()
	{
		JTextField tf = new JTextField();
		tf.setFont(GUIConstraints.getFont(GUIConstraints.INPUT_FONT_PLAIN)); // Schrift in Textfeldern der ersten und zweiten
		tf.setHorizontalAlignment(SwingConstants.RIGHT);
		return new DefaultCellEditor(tf); // Spalte der Eingabetabelle setzen
	}
}
