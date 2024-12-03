/*
 * @(#)DistrictTableModel.java 2.1 18/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui.district;

import javax.swing.table.AbstractTableModel;

import de.uni.augsburg.bazi.Resource;

/** <b>Title:</b> Klasse DistrictTableModel<br>
 * <b>Description:</b> TableModel Implementierung zur Anzeige und Verwaltung der Distrikte in der Distrikt-Tabelle<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public class DistrictTableModel extends AbstractTableModel
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Maximale Anzahl von Zeilen in der Tabelle. */
	private static int maxData = 200;

	/** Spaltennamen. */
	private String[] columnNames = new String[3];

	/** Werte der Tabelle. */
	private Object[][] data = new Object[maxData][3];

	/** Anzahl der Zeilen.
	 * @uml.property name="rowNumber" */
	private int rowNumber = 26;

	/** Nummer der Zeile mit der Summe der Stimmen.
	 * @uml.property name="rowTotal" */
	private int rowTotal = 1;

	/** Zeigt an, ob die Minimums-Spalte editierbar ist. */
	// private boolean min = false;

	/** District-Zähler */
	private int dCount = 1;

	/** Bezeichnungsstring */
	private String desc;

	/** Erzeugt eine Instanz von WeightsTableModel.
	 * 
	 * @param first Kopf der ersten Spalte
	 * @param second Kopf der zweiten Spalte
	 * @param mand Bezeichnugn der Summe */
	public DistrictTableModel(String first, String second, String third,
			String mand)
	{
		columnNames[0] = first;
		columnNames[1] = second;
		columnNames[2] = mand;

		data[0][0] = "1";
		data[rowTotal][0] = third;
		desc = Resource.getString("bazi.gui.district");
	}

	/** Setzt die Anzahl der Distrikte.
	 * 
	 * @param c Anzahl der Distrikte */
	public void setCount(int c)
	{
		dCount = c;
	}

	/** Liefert die Anzahl der Distrikte.
	 * 
	 * @return Anzahl der Distrikte */
	public int getCount()
	{
		return dCount;
	}

	/** Gibt die Anzahl der Spalten an.
	 * 
	 * @return Anzahl der Spalten */
	public int getColumnCount()
	{
		return columnNames.length;
	}

	/** Gibt die Anzahl der Zeilen an.
	 * 
	 * @return Anzahl der Zeilen */
	public int getRowCount()
	{
		return rowNumber + 1;
	}

	/** Gibt den Namen der Spalte col an.
	 * 
	 * @return Spaltenname
	 * @param col Spaltenindex */
	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	/** Gibt den Wert der Zelle (row, col) zurück.
	 * 
	 * @param row Zeilenindex
	 * @param col Spaltenindex
	 * @return Inhalt der Zelle */
	public Object getValueAt(int row, int col)
	{
		if ((col == 0) && (row < rowTotal))
		{
			return new Integer(row + 1);
		}
		else
		{
			return data[row][col];
		}
	}

	/** Gibt an, ob die Zelle (row, col) editierbar ist.
	 * 
	 * @param row Zeilenindex
	 * @param col Spaltenindex
	 * @return <b>true</b> wenn die Zelle editierbar ist */
	public boolean isCellEditable(int row, int col)
	{
		if (row >= rowTotal)
		{
			return false;
		}
		else if (col == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/** Setzt den Wert value in die Zelle (row, col).
	 * 
	 * @param value Object
	 * @param row Zeilenindex
	 * @param col Spaltenindex */
	public void setValueAt(Object value, int row, int col)
	{
		if (col == 0)
		{
			if (value instanceof String)
			{
				data[row][col] = (String) value;
			}
			else
			{
				data[row][col] = row + "";
			}
		}
		else if (col == 1)
		{
			if (value == null)
			{
				data[row][col] = null;
			}
			else if (value instanceof String)
			{
				data[row][col] = (String) value;
			}
			else
			{
				data[row][col] = "District " + row;
			}
		}
		else if (col == 2)
		{
			if (value == null)
			{
				data[row][col] = null;
			}
			else if (value instanceof Integer)
			{
				data[row][col] = (Integer) value;
			}
			else
			{
				try
				{
					// System.out.print("Trying cast of: " + value + "... ");
					data[row][col] = new Integer(((String) value));
					// System.out.println("cast successful!");
				}
				catch (NumberFormatException nfe)
				{
					// System.out.println("Fehler beim parsen in setValueAt bei: " + value);
					// System.out.println(nfe);
					data[row][col] = null;
				}
			}
		}
		else
		{
			data[row][col] = value;
		}
		fireTableCellUpdated(row, col);
	}

	/** Setzt die Editierbarkeit der Minimums-Spalte. */
	/* public void setMinEditable(boolean bool) {
	 * min = bool;
	 * } */

	/** Fügt eine Leerzeile an der Stelle row ein.
	 * 
	 * @param row Zeilenindex an dem die neue Zeile eingesetzt werden soll */
	public void setNewline(int row)
	{
		if (row < rowTotal)
		{
			if (rowTotal == rowNumber)
			{
				rowNumber++;
			}
			for (int i = rowTotal; i > row; i--)
			{
				for (int k = 0; k < 3; k++)
				{
					data[i + 1][k] = data[i][k];
				}
			}
			rowTotal++;
			data[row + 1][0] = rowTotal + "";
			data[row + 1][1] = desc + " " + (++dCount);
			data[row + 1][2] = null;
		}
	}

	/** Gibt die Nummer der Zeile mit der Summe der Stimmen an.
	 * @return Zeilenindex
	 * @uml.property name="rowTotal" */
	public int getRowTotal()
	{
		return rowTotal;
	}

	/** Setzt die Nummer der Zeile mit der Summe der Stimmen.
	 * @param total Zeilenindex
	 * @uml.property name="rowTotal" */
	public void setRowTotal(int total)
	{
		rowTotal = total;
		if (total > rowNumber)
		{
			rowNumber = total;
		}
	}

	/** Setzt die Tabelle auf den Initialzustand zurück. */
	public void reset()
	{
		Object total = data[rowTotal][0];
		data = new Object[maxData][4];
		rowTotal = 1;
		// min = false;
		data[0][0] = "1";
		data[rowTotal][0] = total;
	}

	/** Setzt die Zahl der gesamten Zeilen auf number.
	 * @param number Anzahl der Zeilen
	 * @uml.property name="rowNumber" */
	public void setRowNumber(int number)
	{
		rowNumber = number;
	}

	/** Löscht die Zeile row aus der Tabelle.
	 * 
	 * @param row Zeilenindex */
	public void deleteRow(int row)
	{
		if (row < rowTotal)
		{
			if (rowTotal > 1)
			{
				for (int i = 0; i < 3; i++)
				{
					for (int k = row; k < rowTotal + 1; k++)
					{
						data[k][i] = data[k + 1][i];
					}
				}
				if (rowNumber == rowTotal)
				{
					rowNumber--;
				}
				rowTotal--;
				dCount--;
			}
			else
			{
				data[row][0] = "" + new Integer(row + 1).toString() + "";
				data[row][1] = null;
				data[row][2] = null;
			}
		}
	}

}
