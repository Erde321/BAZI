/*
 * @(#)WeightsTable.java 2.1 07/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.OutputFormat;
import de.uni.augsburg.bazi.lib.Rounding;

/** <b>Title:</b> Klasse WeightsTable<br>
 * <b>Description:</b> Tabelle zur Stimmeneingabe<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Jan Petzold, Christian Brand, Marco Schumacher */
public class WeightsTable extends JPanel implements ItemListener
{
	/** Referenz auf RoundFrame */
	private RoundFrame rf;

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Auswahl von Minimum, Direktmandat und Diskrepanz. */
	private final JComboBox cMinDir;

	/** Eigentliche Java-Tabelle. */
	private final JTable table;

	/** TableModel für die Tabelle. */
	private final WeightsTableModel wtm;

	/** Überschrift der Namenspalte. */
	private final String name;

	/** Überschrift der Stimmenspalte. */
	private final String votes;

	/** Bezeichnung der Summe der Stimmen. */
	// private String sum;

	/** Renderer für die Auswahlbox im Tabellenkopf. */
	private final DefaultTableCellRenderer minRenderer;

	/** Container für die eigentliche Java-Tabelle. */
	private final JScrollPane scrollPane;

	/** Spaltenbreite. */
	private final int width_0 = 55;

	/** Spaltenbreite. */
	private final int width_1 = 100;

	/** Spaltenbreite. */
	private final int width_2 = 75;

	/** TablePane, Elternelement */
	private final TablePane tpParent;

	/** Flag für den ItemListener */
	private boolean bItem = false;
	
	/** Länge der cMinDir ComboBox */
	public static int cMinDirlength;
	
	private static Logger logger = Logger.getLogger(WeightsTable.class);

	/** Konstruktor */
	public WeightsTable(String name, String votes, String sum, ConditionItem[] items,
			TablePane tpn, RoundFrame rf)
	{
		this.name = name;
		this.votes = votes;
		// this.sum = sum;
		tpParent = tpn;
		this.rf = rf;

		wtm = new WeightsTableModel(name, votes, sum, tpParent);
		// deaktiviert um Synchronisation der Tabelle abzuschalten.
		// jetzt werden nur noch die Köpfe synchronisiert
		// wtm.addTableModelListener(new WTMListener());
		// erzeugen der JTable
		table = new JTable(wtm)
		{

			/** Default UID */
			private static final long serialVersionUID = 1L;

			@Override
			public void editingStopped(ChangeEvent e)
			{
				super.editingStopped(e);
				// System.out.println("ES, Col: " + table.getEditingColumn() + " Row: " + table.getEditingRow());
				wtm.setOrder0(WeightsTableModel.UNSORTED);
				wtm.setOrder1(WeightsTableModel.UNSORTED);
				// table.repaint();
				((EditableHeader) table.getTableHeader()).repaint();
				reCalcurate(1);
				reCalcurate(2);
				repaint();
			}
		};	
		
		//Zeilenhöhe
		logger.info("GUIConstraints.size():" + GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN));
		table.setIntercellSpacing(new Dimension(0,4));
		if(GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN) >= GUIConstraints.FONTSIZE_MIN) 
		{
			table.setRowHeight(GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN) + 10); 
		}

		// table.setMinimumSize(new Dimension(205, 32));
		// table.setPreferredSize(new Dimension(205, 100));
		/* table.addComponentListener(new ComponentAdapter() {
		 * public void componentResized(ComponentEvent e) {
		 * System.out.println("table resized to w: " + table.getWidth() + " h: " + table.getHeight() + " by: " + e.getComponent() + " param: " + e.paramString());
		 * //throw new RuntimeException("only for debug purposes....:-)");
		 * }
		 * }); */

		table.setFont(GUIConstraints.getFont(GUIConstraints.INPUT_FONT_PLAIN));
		table.addMouseListener(new MListener());
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.setColumnMargin(3);

		EditableHeader eh = new EditableHeader(columnModel, tpParent, this);
		// eh.addMouseListener(new MListener());
		table.setTableHeader(eh);

		table.getTableHeader().setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD)); 
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// table.setRowSelectionInterval(0, 1);


		// ComboBox erstellen
		cMinDir = new JComboBox();
		cMinDirlength = items.length;
		for (int i = 0; i < items.length; i++)
		{
			cMinDir.addItem(items[i]);
		}
		cMinDir.setSelectedIndex(0);
		cMinDir.addItemListener(this);
		cMinDir.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD)); 
		cMinDir.addMouseListener(new MouseAdapter(){
			
	            public void mouseClicked(MouseEvent event)
	            {
	            	if (event.getClickCount() == 2 && cMinDir.getSelectedIndex() == 6) 
	            	{
	            		RoundFrame.minPlusValue = new Dialog_Min_Plus(getrf(), RoundFrame.minPlusValue, getrf().gettpTable().getTables() ).getValue();
	            	}
	            }
	     });	
		
		
		
		
		// Schrift in Auswahlliste der dritten Spalte der Eingabetabelle

		// Eigenschaften der Spalten festlegen
		EditableHeaderTableColumn column = null;
		for (int i = 0; i < 3; i++)
		{
			column = (EditableHeaderTableColumn) table.getColumnModel().getColumn(i);
			if (i == 0)
			{
				column.setPreferredWidth(width_0);
				column.setHeaderEditable(true); 
				// Schrift in Eingabetextfeld der ersten Spalte setzen
				column.setCellEditor(createDefaultEditor()); 
			}
			else if (i == 1)
			{
				column.setPreferredWidth(width_1);
				column.setHeaderEditable(true); 
				// Schrift in Eingabetextfeld der zweiten Spalte setzen
				column.setCellEditor(createDefaultEditorRight());
			}
			else if (i == 2)
			{
				column.setPreferredWidth(width_2);
				column.setHeaderValue(cMinDir.getSelectedItem());
				column.setHeaderRenderer(new ComboRenderer(items));
				column.setHeaderEditor(new DefaultCellEditor(cMinDir)); 
				// Schrift in Eingabetextfeld der dritten Spalte setzen
				column.setCellEditor(createDefaultEditorRight());
			}
		}
		TableColumn tc;
		// Ausrichtung der Stimmenspalte nach rechts
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.RIGHT);
		tc = table.getColumnModel().getColumn(1);
		tc.setCellRenderer(dtcr);
		// Ausrichtung der Minimumsspalte nach rechts
		minRenderer = new DefaultTableCellRenderer();
		minRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		minRenderer.setEnabled(false);
		tc = table.getColumnModel().getColumn(2);
		tc.setCellRenderer(minRenderer);

		// erzeugen eines ScrollPane und einfuegen der Tabelle
		scrollPane = new JScrollPane(table); 
		
		
		
		scrollPane.getViewport().setBackground(Color.WHITE);
		/* Bei einem JScrollPane muß der Hintergrund des Viewports gesetzt werden,
		 * nicht der des Scrollpanes. */
		Insets scrollInsets = scrollPane.getInsets();
		int w = width_0 + width_1 + width_2 + scrollInsets.left +
				scrollInsets.right;
		int h = table.getRowHeight() * table.getRowCount() +
				table.getTableHeader().getPreferredSize().height
				+ scrollInsets.top + scrollInsets.bottom;
		scrollPane.setPreferredSize(new Dimension(w, h));

		// Einfuegen des ScrollPane in das Panel
		setLayout(new GridLayout(1, 1));
		add(scrollPane);
		// this.setBackground(Color.WHITE);
		table.addKeyListener(new NewlineListener());
	}

	public JTable getTable()
	{
		return table;
	}

	public JScrollPane getscp()
	{
		return scrollPane;
	}

	public WeightsTableModel getModel()
	{
		return wtm;
	}

	/** Rückgabe der Zeilenhöhe. */
	public int getRowHeight()
	{
		return table.getRowHeight();
	}

	/** Aufsummieren der Einträge der Stimmen-Spalte. */
	private void reCalcurate(int col)
	{
		int iTotalBase = 0;
		int iTotalMaxOnly = 0;
		boolean infinite = false;
		int iTotal = 0;
		double dTotal = 0.0f;
		boolean bDouble = false;
		boolean minToMax = (((ConditionItem) cMinDir.getSelectedItem()).getFormat()
					== OutputFormat.CONDITION_MIN_TO_MAX) && (col == 2);
		int decimal_places = 0;

		for (int i = 0; i < wtm.getRowTotal(); i++)
		{
			Object obj = wtm.getValueAt(i, col);
			if (obj instanceof Integer)
			{
				iTotal += ((Integer) obj).intValue();
			}
			else if (obj instanceof Double)
			{
				bDouble = true;
				dTotal += ((Double) obj).doubleValue();
				decimal_places = Math.max(decimal_places, Rounding.getDecimal_places((Double) obj));
				/* System.out.println("dtbsc: " + dTotal);
				 * dTotal *= Math.pow(10.0,digits);
				 * dTotal = Math.round(dTotal);
				 * dTotal /= Math.pow(10.0,digits);
				 * System.out.println("val: " + obj.toString() + " nd: " + newDigits + " od: " + oldDigits + " d: " + digits + " dt: " + dTotal); */
			}
			else if (minToMax && obj instanceof String)
			{
				String value = (String) obj;
				int helpIndex = value.indexOf(RoundFrame.RANGE_SEPERATOR);
				String first = value.substring(0, helpIndex);
				helpIndex += RoundFrame.RANGE_SEPERATOR.length();
				String second = value.substring(helpIndex);
				iTotal += Integer.parseInt(first);
				if (second.trim().equals("oo"))
					infinite = true;
				else
					iTotalMaxOnly += Integer.parseInt(second);
			}
			else
			{
				String val = (String) obj;
				try
				{
					if (val != null)
					{
						iTotal += Integer.parseInt(val);
					}
				}
				catch (NumberFormatException nfe)
				{
					System.out.println("Problems " + val);
				}
			}
		}
		if (dTotal + iTotal + iTotalMaxOnly + iTotalBase == 0.0)
		{
			wtm.setValueAt(null, wtm.getRowTotal(), col);
		}
		else
		{
			if (bDouble)
			{
				dTotal = Rounding.round(dTotal, decimal_places);
				wtm.setValueAt(new Double(dTotal + new Integer(iTotal).doubleValue()),
						wtm.getRowTotal(), col);
			}
			else if (minToMax)
			{
				wtm.setValueAt(new Integer(iTotal) + RoundFrame.RANGE_SEPERATOR + (infinite ? "oo" : iTotalMaxOnly),
						wtm.getRowTotal(), col);
			}
			else
			{
				wtm.setValueAt(new Integer(iTotal), wtm.getRowTotal(), col);
			}
		}
	}

	/** Abfangen der Änderung der Bedingungs-Combobox. */
	@Override
	public void itemStateChanged(ItemEvent ie)
	{
		if (!bItem)
		{
			/* if (cMinDir.getSelectedIndex() == 0) {
			 * wtm.setMinEditable(false);
			 * minRenderer.setEnabled(false);
			 * }
			 * else {
			 * wtm.setMinEditable(true);
			 * minRenderer.setEnabled(true);
			 * }
			 * repaint(); */
			tpParent.changeCondition(cMinDir.getSelectedIndex());

			repaint();
		}
	}

	/** Rückgabe der Werte der Spalte col. */
	public Vector<Object> getColumnValues(int col)
	{
		int row = wtm.getRowTotal();
		Vector<Object> v = new Vector<Object>();
		for (int i = 0; i < row; i++)
		{
			v.addElement(wtm.getValueAt(i, col));
		}
		return v;
	}

	/** Rückgabe der Werte der Spalte col. Alle Werte werden als Strings zurückgegeben. */
	public Vector<String> getColumnValuesAsStrings(int col)
	{
		int row = wtm.getRowTotal();
		Vector<String> v = new Vector<String>();
		for (int i = 0; i < row; i++)
		{
			if (wtm.getValueAt(i, col) == null)
			{
				v.addElement("");
			}
			else
			{
				v.addElement(wtm.getValueAt(i, col).toString());
			}
		}
		return v;
	}

	/** Rückgabe der Werte der Spalte col. Alle Werte werden als Numbers zurückgegeben. */
	public Vector<Number> getColumnValuesAsNumbers(int col)
	{
		int row = wtm.getRowTotal();
		Vector<Number> v = new Vector<Number>();
		for (int i = 0; i < row; i++)
		{
			Object value = wtm.getValueAt(i, col);
			if (value instanceof Number)
				v.addElement((Number) value);
			else
				v.addElement(null);
		}
		return v;
	}

	/** Rückgabe der Werte der Spalte col. Alle Werte werden als Numbers zurückgegeben. */
	public Vector<Integer> getColumnValuesAsIntegers(int col)
	{
		int row = wtm.getRowTotal();
		Vector<Integer> v = new Vector<Integer>();
		for (int i = 0; i < row; i++)
		{
			Object value = wtm.getValueAt(i, col);
			if (value instanceof Integer)
				v.addElement((Integer) value);
			else
				v.addElement(null);
		}
		return v;
	}

	/** Setzen der Werte der Spalte col.
	 * @param values Vector mit den Daten
	 * @param col Nummer der Spalte */
	public void setColumnValues(Vector<?> values, int col)
	{
		if (col == 0)
		{
			Object total = wtm.getValueAt(wtm.getRowTotal(), 0);
			wtm.setRowTotal(values.size());
			wtm.setValueAt(total, wtm.getRowTotal(), 0);
		}
		for (int i = 0; i < values.size(); i++)
		{
			wtm.setValueAt(values.elementAt(i), i, col);
		}
		if (col == 1)
		{
			reCalcurate(1);
		}
		if (col == 2)
		{
			reCalcurate(2);
		}
		table.revalidate();
		repaint();
	}

	/** Setzen der Werte der Spalte col. Nur zur internen Verwendung!
	 * @param values Vector mit den Daten
	 * @param col Nummer der Spalte */
	public void setColumnValuesExt(Vector<Object> values, int col)
	{
		if (col == 0)
		{
			Object total = wtm.getValueAt(wtm.getRowTotal(), 0);
			wtm.setRowTotal(values.size());
			wtm.setValueAtExt(total, wtm.getRowTotal(), 0);
		}
		for (int i = 0; i < values.size(); i++)
		{
			wtm.setValueAtExt(values.elementAt(i), i, col);
		}
		if (col == 1)
		{
			reCalcurate(1);
		}
		if (col == 2)
		{
			reCalcurate(2);
		}
		table.revalidate();
		repaint();
	}

	/** Setzen der Combobox auf das übergebene ConditionItem. */
	public void setMinDir(ConditionItem id)
	{
		/* Es wird überprüft, an welcher Stelle der ComboBox das gewünschte
		 * ConditionItem steht. Falls das ConditionItem gar nicht in der ComboBox
		 * vorkommt, so wird der Wert auf Condition_None gesetzt! */
		int format = id.getFormat();
		int numberOfItems = cMinDir.getItemCount();
		int indexOfSelectedConditionItem = 0;
		for (int i = 0; i < numberOfItems; i++)
		{
			ConditionItem ci = (ConditionItem) cMinDir.getItemAt(i);
			if (ci.getFormat() == format)
			{
				indexOfSelectedConditionItem = i;
				break;
			}
		}
		setMinDir(indexOfSelectedConditionItem);
		// cMinDir.setSelectedItem(id);
		// setMinDir(cMinDir.getSelectedIndex());
	}

	/** Setzen der Combobox auf id. */
	public void setMinDir(int id)
	{
		if (id == 0)
		{
			// An 0-ter Stelle steht immer Condition_None
			// Also wird die dritte Spalte deaktiviert
			wtm.setMinEditable(false);
			minRenderer.setEnabled(false);
		}
		else
		{
			wtm.setMinEditable(true);
			minRenderer.setEnabled(true);
			ConditionItem cond = (ConditionItem) (cMinDir.getItemAt(id));
			if (cond != null)
			{
				// Es wird auf Min_To_Max gewechselt
				if (cond.getFormat() == OutputFormat.CONDITION_MIN_TO_MAX)
				{
					int numberOfRows = wtm.getRowCount();
					for (int i = 0; i <= numberOfRows; i++)
					{
						Object val = wtm.getValueAt(i, 2);
						/* eine gueltige MinToMax-Bedingung wird durch einen String repraesentiert */
						if (val instanceof String)
						{
							String tempString = (String) val;
							int helpIndex = tempString.indexOf(RoundFrame.RANGE_SEPERATOR);
							if (helpIndex <= 0 || helpIndex + RoundFrame.RANGE_SEPERATOR.length() >= tempString.length()
									|| tempString.indexOf(RoundFrame.BASE_SEPERATOR) >= 0)
							{
								wtm.setValueAt(null, i, 2);
							}
							else
							{
								// String hat richtige Konvertierung, also stehen lassen
							}
						}
						else
						{
							wtm.setValueAt(null, i, 2);
						}
					}
				}
				else
				{
					try
					{
						int numberOfRows = wtm.getRowCount();
						for (int i = 0; i < numberOfRows; i++)
						{
							String val = (String) wtm.getValueAt(i, 2);
							if (val != null && val.indexOf(RoundFrame.RANGE_SEPERATOR) >= 0)
							{
								for (int j = 0; j <= numberOfRows; j++)
								{
									wtm.setValueAt(null, j, 2);
								}
								break;
							}
						}
					}
					catch (ClassCastException c)
					{
						// Do nothing
					}
				}
			}
		}
		bItem = true;
		EditableHeaderTableColumn column = (EditableHeaderTableColumn) table.
				getColumnModel().getColumn(2);
		cMinDir.setSelectedIndex(id);
		column.setHeaderValue(cMinDir.getSelectedItem());
		
		if (id+1 != OutputFormat.CONDITION_MIN_PLUS)
			rf.setMenuOptions(false);
		else
			rf.setMenuOptions(true);

		table.revalidate();
		repaint();
		bItem = false;
	}

	/** Zuruecksetzen der Tabelle auf den Initialzustand. */
	public void reset()
	{
		EditableHeaderTableColumn column;
		// 1. Spalte
		column = (EditableHeaderTableColumn) table.getColumnModel().getColumn(0);
		column.setHeaderValue(new String(name));
		column.setPreferredWidth(width_0);
		// 2. Spalte
		column = (EditableHeaderTableColumn) table.getColumnModel().getColumn(1);
		column.setHeaderValue(new String(votes));
		column.setPreferredWidth(width_1);
		// 3. Spalte
		column = (EditableHeaderTableColumn) table.getColumnModel().getColumn(2);
		cMinDir.setSelectedIndex(0);
		column.setHeaderValue(cMinDir.getSelectedItem());
		column.setPreferredWidth(width_2);
		wtm.setMinEditable(false);

		// falls eine Zelle bearbeitet wird
		table.editCellAt(wtm.getRowTotal(), 0);

		wtm.reset();
		table.revalidate();
		repaint();
		adaptRows();
	}

	/** Rückgabe der Summe der Stimmen. */
	public double getTotal()
	{
		WeightsTableModel tm = (WeightsTableModel) table.getModel();
		Object obj = tm.getValueAt(tm.getRowTotal(), 1);
		double d = 0.0;
		if (obj instanceof Integer)
		{
			d = (((Integer) obj).intValue());
		}
		else if (obj instanceof Double)
		{
			d = ((Double) obj).doubleValue();
		}
		return d;
	}

	/** Rückgabe der Zeilenzahl */
	public int getRowTotal()
	{
		return wtm.getRowTotal();
	}

	/** Rückgabe der Summe der Mindestsitze oder Direktmandate. */
	public int getMinTotal()
	{
		Object obj = wtm.getValueAt(wtm.getRowTotal(), 2);
		int value;
		if (obj instanceof Integer)
		{
			value = ((Integer) obj).intValue();
		}
		else
		{
			value = 0;
		}
		return value;
	}
	
	/** Zugriff auf die ComboBox der dritten Spalte
	 * @return ComboBox der dritten Spalte */
	public JComboBox getcMinDir()
	{
		return (cMinDir);
	}

	/** Rückgabe der Auswahl der Combobox (Status der Nebenbedingung). */
	public ConditionItem getSelectedCondition()
	{
		return (ConditionItem) cMinDir.getSelectedItem();
	}

	/** Rückgabe des Index der Auswahl der Combobox (Status der Nebenbedingung). */
	public int getSelectedConditionIndex()
	{
		return cMinDir.getSelectedIndex();
	}


	/** Anpassen der Anzahl der Zeilen an das ScrollPane. */
	public void adaptRows()
	{
		/* double fRow = (scrollPane.getSize().height - (table.getTableHeader().getPreferredSize().height + scrollPane.getInsets().top + scrollPane.getInsets().bottom)) /
		 * table.getRowHeight();
		 * int iRow = (int)Math.floor(fRow) - 1;
		 * int rowTotal = wtm.getRowTotal();
		 * //System.out.println("Called WeightsTable.adaptRows() with iRow: "+iRow + " rowTotal: " + rowTotal);
		 * if (iRow > rowTotal) {
		 * //System.out.println("Changed Rows ");
		 * wtm.setRowNumber(iRow);
		 * table.revalidate();
		 * repaint();
		 * } */
	}

	/** Update der Oberfläche */
	/* public void updateUI() {
	 * super.updateUI();
	 * if (scrollPane != null)
	 * scrollPane.updateUI();
	 * } */

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
			wtm.deleteRow(rows[i - 1]);
		}
		reCalcurate(1);
		reCalcurate(2);
		table.clearSelection();
		table.revalidate();
		repaint();

		adaptRows();
	}

	/** Löschen von Zeilen
	 * @param rows Array mit den Zeilenindizes */
	public void deleteRows(int[] rows)
	{
		for (int i = rows.length; i > 0; i--)
		{
			wtm.deleteRow(rows[i - 1]);
		}
		reCalcurate(1);
		reCalcurate(2);
		table.clearSelection();
		table.revalidate();
		repaint();
		adaptRows();
	}

	/** Setzen des Namen der Spalte col. */
	public void setColumnName(String name, int col)
	{
		if (name != null && !name.equals(""))
		{
			EditableHeaderTableColumn column = (EditableHeaderTableColumn) table.
					getColumnModel().getColumn(col);
			column.setHeaderValue(new String(name));
			table.revalidate();
			repaint();
		}
	}

	/** Rückgabe des Namen der Spalte col. */
	public String getColumnName(int col)
	{
		EditableHeaderTableColumn column = (EditableHeaderTableColumn) table.
				getColumnModel().getColumn(col);
		String name = (String) column.getHeaderValue();
		return name;
	}
	
	/** Zugriff auf die übergeordnete Roundframe
	 * @return Roundframe */
	public RoundFrame getrf()
	{
		return rf;
	}

	/** Einfügen einer Zeile
	 * @param row Stelle, an der die Zeile eingefügt werden soll
	 * @param neu Name der neuen Zeile */
	public void addRow(int row, String neu)
	{
		wtm.setNewlineExt(row, neu);
		table.repaint();
		table.revalidate();
	}

	/** (De-)Aktivieren der Combobox
	 * @param state Status */
	public void setCondEnabled(boolean state)
	{
		cMinDir.setEnabled(state);
		minRenderer.updateUI();
	}

	/** Setzt die Elemente der ComboBox in der 3. Spalte
	 * @param items Ein Array von Strings */
	public void setCondItems(ConditionItem[] items)
	{
		int selIndex = cMinDir.getSelectedIndex();
		
		cMinDir.removeAllItems();
		for (int i = 0; i < items.length; i++)
			cMinDir.addItem(items[i]);

		cMinDir.setSelectedIndex(selIndex); /* Selektion wieder wie zuvor */
		EditableHeaderTableColumn column = (EditableHeaderTableColumn) table.getColumnModel().getColumn(2);
		
		column.setHeaderValue(cMinDir.getSelectedItem());
		column.setHeaderRenderer(new ComboRenderer(items));
		column.setHeaderEditor(new DefaultCellEditor(cMinDir));

		minRenderer.updateUI();
	}

	/** Update der Oberfläche */
	@Override
	public void updateUI()
	{
		super.updateUI();
		if (table != null)
		{
			table.repaint();
			table.updateUI();
			table.getTableHeader().repaint();
			table.getTableHeader().updateUI();
			
			// Schrift in Tabelle und den Spaltenüberschrift der ersten beiden Spalten
			// erneut setzen (notwendig, da die Schriften sonst bei jedem Einfuegen eines Distrikts
			// wieder auf den Default zurueckgesetzt werden)
			table.getTableHeader().setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD)); 
			table.setFont(GUIConstraints.getFont(GUIConstraints.INPUT_FONT_PLAIN));
			
			EditableHeaderTableColumn column = null;
			for (int i = 0; i < 3; i++)
			{
				column = (EditableHeaderTableColumn) table.getColumnModel().getColumn(i);
				if (i == 0)
				{
					// Schrift in Eingabetextfeld der ersten Spalte setzen
					column.setCellEditor(createDefaultEditor()); 
				}
				else 
				{
					// Schrift in Eingabetextfeld der zweiten/dritten Spalte setzen
					column.setCellEditor(createDefaultEditorRight());
				}
			}
		}
		if (minRenderer != null)
		{
			minRenderer.updateUI();
		}
	}

	/** <b>Title:</b> Klasse ComboRenderer<br>
	 * <b>Description:</b> Darstellung der Combobox im Tabellenkopf<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Jan Petzold, Christian Brand
	 * @version 2.1 */
	class ComboRenderer extends JComboBox implements TableCellRenderer
	{

		/** Default UID */
		private static final long serialVersionUID = 1L;

		/** Erzeugt eine Instanz von ConmboRenderer. */
		ComboRenderer(Object[] items)
		{
			for (int i = 0; i < items.length; i++)
			{
				addItem(items[i]);
			}
		}

		/** Implementierung von TableCellRenderer. */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column)
		{

			setSelectedItem(value);
			setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD)); 
			// Font für Überschrift der dritten Spalte der Eingabetabelle
			return this;
		}
	}

	/** Hinzufügen eine ListSelectionListeners */
	public void addlsl(ListSelectionListener lsl)
	{
		table.getSelectionModel().addListSelectionListener(lsl);
	}

	/** Entfernen eines ListSelectionListener */
	public void remlsl(ListSelectionListener lsl)
	{
		table.getSelectionModel().removeListSelectionListener(lsl);
	}

	/** Beim Starten des Programms muß eine evtl. noch offene Eingabe beendet werden */
	public void finishInput()
	{
		// table.editingStopped(new ChangeEvent(this));
		// nur editingStopped() hilft nichts, da dann die Eingabe wieder gelöscht wird
	}

	/** <b>Title:</b> Klasse NewlineListener<br>
	 * <b>Description:</b> Einfügen einer Leerzeile in die Tabelle bei Betätigung der Enter-Taste.<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Jan Petzold, Christian Brand
	 * @version 2.1 */
	class NewlineListener extends KeyAdapter
	{

		/** Aufruf, wenn eine Taste gedrückt wurde. */
		@Override
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
			{
				int r = table.getSelectedRow();
				// wtm.setNewline(table.getSelectedRow());
				wtm.setNewline(r);

				table.repaint();
				table.revalidate();
				table.setRowSelectionInterval(r, r);
			}
		}
	}

	/* /**
	 * <b>Title:</b> Klasse WTMListener<br>
	 * <b>Description:</b> Überwacht die Tabelle auf Änderungen<br>
	 * <b>Copyright:</b> Copyright (c) 2002<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Florian Kluge
	 * @version 2.0
	 * @deprecated */
	/* public class WTMListener implements TableModelListener {
	 * public void tableChanged(TableModelEvent e) {
	 * /*System.out.println("Col: " + e.getColumn());
	 * System.out.println("RowF: " + e.getFirstRow());
	 * System.out.println("RowL: " + e.getLastRow());
	 * System.out.println("Typ: " + e.getType()); */
	/* if (e.getType() == TableModelEvent.INSERT) {
	 * // Neue Zeile eingefügt
	 * // Hole Name der Zeile:
	 * String tmpN = (String) getColumnValues(0).elementAt(e.getFirstRow()+1);
	 * tpParent.addRow(e.getFirstRow(), tmpN);
	 * }
	 * else if (e.getType() == TableModelEvent.UPDATE) {
	 * // etwas wurde geändert
	 * // hier muß aber nur eine Änderung in der ersten Spalte berücksichtigt werden
	 * if (e.getColumn() == 0) {
	 * //String tmpN = (String) getColumnValues(0).elementAt(e.getFirstRow());
	 * //System.out.println("Listener UPD: " + tmpN);
	 * //tpParent.setName(e.getFirstRow(), tmpN);
	 * tpParent.setName();
	 * }
	 * }
	 * else if (e.getType() == TableModelEvent.DELETE) {
	 * // Zeile gelöscht
	 * }
	 * }
	 * } */

	private class MListener extends MouseAdapter
	{
		/* public MListener() {
		 * super();
		 * System.out.println("MListener created");
		 * } */

		// public void MouseClicked(MouseEvent me) {
		// System.out.println("MouseClicked: " + me.getClickCount());
		// }
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
