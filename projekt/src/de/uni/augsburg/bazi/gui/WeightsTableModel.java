/*
 * @(#)WeightsTableModel.java 2.1 07/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.driver.OutputFormat;

/** <b>Title:</b> Klasse WeightsTableModel<br>
 * <b>Description:</b> Verwaltung der Tabelle zur Stimmen-Eingabe<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Jan Petzold, Christian Brand, Marco Schumacher */
public class WeightsTableModel extends AbstractTableModel
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Logger zum loggen der auftretenden Fehler */
	private static Logger logger = Logger.getLogger(WeightsTableModel.class);

	/** Unsortiert */
	public static final int UNSORTED = 0;

	/** Aufsteigend sortiert */
	public static final int INCREASING = 1;

	/** Absteigend sortiert */
	public static final int DECREASING = 2;

	/** Maximale Anzahl von Zeilen in der Tabelle. */
	private static int maxData = 1000;

	/** Spaltennamen. */
	private final String[] columnNames = new String[3];

	/** Werte der Tabelle. */
	private Object[][] data = new Object[maxData][3];

	/** Anzahl der Zeilen. */
	private int rowNumber = 1;

	/** Nummer der Zeile mit der Summe der Stimmen. */
	private int rowTotal = 1;

	/** Zeigt an, ob die Minimums-Spalte editierbar ist. */
	private boolean min = false;

	/** Eltern-TablePane */
	private final TablePane tpParent;

	/** Sortierung der ersten Spalte */
	private int order0 = UNSORTED;

	/** Sortierung der zweiten Spalte */
	private int order1 = UNSORTED;

	/** Erzeugt eine Instanz von WeightsTableModel.
	 * @param first Kopf der ersten Spalte
	 * @param second Kopf der zweiten Spalte
	 * @param third Kopf der dritten Spalte */
	public WeightsTableModel(String first, String second, String third,
			TablePane tpp)
	{
		tpParent = tpp;
		columnNames[0] = first;
		columnNames[1] = second;

		data[0][0] = "=1=";
		data[rowTotal][0] = third;
	}

	/** Gibt die Anzahl der Spalten an. */
	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}

	/** Gibt die Anzahl der Zeilen an. */
	@Override
	public int getRowCount()
	{
		return rowNumber + 1;
	}

	/** Gibt den Namen der Spalte col an. */
	@Override
	public String getColumnName(int col)
	{
		/* System.out.println("Call to wtm::getColumnName(" + col + ")");
		 * if (col>1) return columnNames[col];
		 * else return columnNames[col] + " " + getOrderString(col); */
		return columnNames[col];
	}

	/** Gibt den Wert der Zelle (row, col) an. */
	@Override
	public Object getValueAt(int row, int col)
	{
		return data[row][col];
	}

	/** Gibt an, ob die Zelle (row, col) editierbar ist. */
	@Override
	public boolean isCellEditable(int row, int col)
	{
		if (row >= rowTotal)
		{
			return false;
		}
		else if (col == 2 && min == false)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/** Setzt den Wert value in die Zelle (row, col). */
	@Override
	public void setValueAt(Object value, int row, int col)
	{
		if (col == 0)
		{
			if (value instanceof String)
			{
				data[row][col] = value;
			}
			else
			{
				data[row][col] = "=" + row + "=";
			}
		}
		else if (col == 1)
		{
			if (value == null)
			{
				data[row][col] = null;
			}
			else if (value instanceof Integer)
			{
				data[row][col] = value;
			}
			else if (value instanceof Double)
			{
				data[row][col] = value;
			}
			else
			{
				try
				{
					data[row][col] = new Integer(((String) value));
				}
				catch (NumberFormatException nfe)
				{
					try
					{
						data[row][col] = new Double(((String) value));
					}
					catch (NumberFormatException ex)
					{
						// data[row][col] = new Integer(0);
						data[row][col] = null;
					}
				}
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
				Integer int_Val = (Integer) value;
				data[row][col] = int_Val;
			}
			else if (tpParent.getSelectedCondition().getFormat() == OutputFormat.CONDITION_MIN_TO_MAX)
			{
				/* Explanation:
				 * String val could be something like: " 12..60 "
				 * (if .. is the value of the ROUNDFRAME_SEPERATOR variable)
				 * First, starting and ending whitespaces are cut off
				 * Now the String looks like "12.. 60"
				 * Then, the index of the Seperator is checked and compared with the
				 * last index of the Seperator. If the two values are not equal, than
				 * the value is not set.
				 * Next, the two values are extracted and whitespaces are cut off.
				 * Now, the Integer.parseInt() - method is called on these two Strings.
				 * If this throws a NumberFormatException, the value is not set.
				 * If all these tests are passed and Integer1 is not greater than Integer2,
				 * the value is set to
				 * Integer1 + Seperator + Integer2. */
				try
				{
					String val = ((String) value).trim();
					if (val.length() == 0)
					{
						data[row][col] = null;
						return;
					}

					int index = val.indexOf(RoundFrame.RANGE_SEPERATOR);
					// Falsches Format der Konvertierung
					if (index != val.lastIndexOf(RoundFrame.RANGE_SEPERATOR))
					{
						logger.warn("No proper Input:\n" +
								"the Seperator can only be used once!");
						data[row][col] = null;
						return;
					}
					else if (index < 0)
					{
						logger.debug("No proper Input:\n" +
								"No Seperator used!");
						data[row][col] = null;
						return;
					}
					String firstPart = val.substring(0, index).trim();
					String secondPart = val.substring(index + RoundFrame.RANGE_SEPERATOR.length()).trim();
					int first = Integer.parseInt(firstPart);
					int last = secondPart.equals("oo") ? Integer.MAX_VALUE : Integer.parseInt(secondPart);
					if (first > last || first < 0)
					{
						data[row][col] = null;
						logger.warn("No proper Input:\n" +
								"The first Integer is greater than the second or is negative!");
						return;
					}
					String str_data = firstPart + RoundFrame.RANGE_SEPERATOR + secondPart;
					data[row][col] = str_data;
					for (int i = row + 1; i < getRowTotal(); i++)
					{
						data[i][col] = str_data;
					}
				}
				catch (ClassCastException cce)
				{
					data[row][col] = null;
					logger.error("Problem with Third column of Weights Table!\n" +
							"Value is not null and neither instance of String or Integer!");
				}
				catch (NumberFormatException nfe)
				{
					data[row][col] = null;
					logger.warn("Problem while Parsing the values into Integer!\n" +
							"No proper Conversion!");
				}
			}
			else
			{
				Integer int_Val = null;
				try
				{
					int_Val = Integer.parseInt(value.toString());
				}
				catch (NumberFormatException nfe)
				{}

				data[row][col] = int_Val;
				if (tpParent.getSelectedCondition().getFormat() == OutputFormat.CONDITION_MIN ||
						tpParent.getSelectedCondition().getFormat() == OutputFormat.CONDITION_MAX ||
						tpParent.getSelectedCondition().getFormat() == OutputFormat.CONDITION_MIN_PLUS ||
						tpParent.getSelectedCondition().getFormat() == OutputFormat.CONDITION_MIN_VPV)
				{

					for (int i = row + 1; i < getRowTotal(); i++)
					{
						data[i][col] = int_Val;
					}
				}
			}
		}
		else
		{
			data[row][col] = value;
		}
		fireTableCellUpdated(row, col);
		order0 = UNSORTED;
		order1 = UNSORTED;
	}

	/** Setzt den Wert value in die Zelle (row, col). Nur zur
	 * internen Verwendung!
	 * @param value Wert der Zelle
	 * @param row Zeile
	 * @param col Spalte */
	public void setValueAtExt(Object value, int row, int col)
	{
		if (col == 0)
		{
			if (value instanceof String)
			{
				data[row][col] = value;
			}
			else
			{
				data[row][col] = "=" + row + "=";
			}
		}
		else if (col == 1)
		{
			if (value == null)
			{
				data[row][col] = null;
			}
			else if (value instanceof Integer)
			{
				data[row][col] = value;
			}
			else if (value instanceof Double)
			{
				data[row][col] = value;
			}
			else
			{
				try
				{
					data[row][col] = new Integer(((String) value));
				}
				catch (NumberFormatException nfe)
				{
					try
					{
						data[row][col] = new Double(((String) value));
					}
					catch (NumberFormatException ex)
					{
						// data[row][col] = new Integer(0);
						data[row][col] = null;
					}
				}
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
				data[row][col] = value;
			}
			else
			{
				try
				{
					data[row][col] = new Integer(((String) value));
				}
				catch (NumberFormatException nfe)
				{
					// data[row][col] = new Integer(0);
					data[row][col] = null;
				}
			}
		}
		else
		{
			data[row][col] = value;
		}
		// fireTableCellUpdated(row, col);
	}

	/** Setzt die Editierbarkeit der Minimums-Spalte. */
	public void setMinEditable(boolean bool)
	{
		min = bool;
	}

	/** Fügt eine Leerzeile an der Stelle row ein. */
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
			data[row + 1][0] = "=" + rowTotal + "=";
			data[row + 1][1] = null;
			data[row + 1][2] = null;
			fireTableRowsInserted(row, row);
		}
	}

	/** Fügt eine Leerzeile an der Stelle row ein, ruft aber keinen
	 * Listener auf
	 * @param row Stelle, an der die Zeile eingefügt werden soll
	 * @param neu Bezeichnung in der Name-Spalte */
	public void setNewlineExt(int row, String neu)
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
			// data[row + 1][0] = "=" + rowTotal + "=";
			data[row + 1][0] = neu;
			data[row + 1][1] = null;
			data[row + 1][2] = null;
		}
	}

	/** Gibt die Nummer der Zeile mit der Summe der Stimmen an.
	 * @uml.property name="rowTotal" */
	public int getRowTotal()
	{
		return rowTotal;
	}

	/** Setzt die Nummer der Zeile mit der Summe der Stimmen.
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
		min = false;
		data[0][0] = "=1=";
		data[rowTotal][0] = total;
		rowNumber = 1;
	}

	/** Setzt die Zahl der gesamten Zeilen auf number.
	 * @uml.property name="rowNumber" */
	public void setRowNumber(int number)
	{
		rowNumber = number;
	}

	/** Löscht die Zeile row aus der Tabelle. */
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
			}
			else
			{
				data[row][0] = "=" + new Integer(row + 1).toString() + "=";
				data[row][1] = null;
				data[row][2] = null;
			}
			fireTableRowsDeleted(row, row);
		}
	}

	int getOrder0()
	{
		return order0;
	}

	public int getOrder1()
	{
		return order1;
	}

	public void setOrder0(int o)
	{
		order0 = o;
	}

	public void setOrder1(int o)
	{
		order1 = o;
	}

	/** Sortiere die erste Spalte */
	public void sort0()
	{
		// System.out.println("Sortiere nach Namen");
		// Sortiere nur, wenn mehr als ein Element vorhanden ist
		if (rowTotal > 1)
		{
			String tmp1, tmp2;
			Object otmp0, otmp1, otmp2;
			for (int i = 0; i < rowTotal - 1; i++)
			{
				tmp1 = (String) data[i][0];
				// System.out.println("s1: " + tmp1);
				for (int j = i + 1; j < rowTotal; j++)
				{
					tmp2 = (String) data[j][0];
					// System.out.println("\ts2: " + tmp2);
					if ((((order0 == UNSORTED) || (order0 == DECREASING)) &&
							(tmp1.compareTo(tmp2) > 0))
							|| ((order0 == INCREASING) && (tmp1.compareTo(tmp2) < 0)))
					{
						// if (tmp1.compareTo(tmp2)>0) {
						// tauschen
						otmp0 = data[i][0];
						otmp1 = data[i][1];
						otmp2 = data[i][2];
						data[i][0] = data[j][0];
						data[i][1] = data[j][1];
						data[i][2] = data[j][2];
						data[j][0] = otmp0;
						data[j][1] = otmp1;
						data[j][2] = otmp2;
						tmp1 = (String) data[i][0];
					}
				}
			}
			if ((order0 == UNSORTED) || (order0 == DECREASING))
			{
				order0 = INCREASING;
			}
			else
			{
				// (order0==INCREASING)
				order0 = DECREASING;
			}
			order1 = UNSORTED;
		}
	}

	/** Sortiere die zweite Spalte */
	public void sort1()
	{
		// System.out.println("Sortiere nach Voten");
		// Sortiere nur, wenn mehr als ein Element vorhanden ist
		if (rowTotal > 1)
		{
			int tmp1, tmp2;
			Object otmp0, otmp1, otmp2;
			for (int i = 0; i < rowTotal - 1; i++)
			{
				tmp1 = ((Integer) data[i][1]).intValue();
				for (int j = i + 1; j < rowTotal; j++)
				{
					tmp2 = ((Integer) data[j][1]).intValue();
					if ((((order1 == UNSORTED) || (order1 == DECREASING)) &&
							(tmp1 > tmp2))
							|| ((order1 == INCREASING) && (tmp1 < tmp2)))
					{
						// if (tmp1 < tmp2) {
						// tauschen
						otmp0 = data[i][0];
						otmp1 = data[i][1];
						otmp2 = data[i][2];
						data[i][0] = data[j][0];
						data[i][1] = data[j][1];
						data[i][2] = data[j][2];
						data[j][0] = otmp0;
						data[j][1] = otmp1;
						data[j][2] = otmp2;
						tmp1 = ((Integer) data[i][1]).intValue();
					}
				}
			}
			if ((order1 == UNSORTED) || (order1 == DECREASING))
			{
				order1 = INCREASING;
			}
			else
			{
				// (order0==INCREASING)
				order1 = DECREASING;
			}
			order0 = UNSORTED;
		}
	}

}
