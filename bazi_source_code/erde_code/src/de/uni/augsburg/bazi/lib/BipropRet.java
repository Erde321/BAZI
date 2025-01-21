/*
 * @(#)BipropRet.java 2.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import de.uni.augsburg.bazi.lib.maxflow.Network;

/** <b>Title:</b> Klasse Biprop<br>
 * <b>Description:</b> Rückgabekonstrukt von <code>divMethMatrix(...)</code><br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 2.1 */
public class BipropRet
{

	/** hier steht die Zuteilung mit den zugehörigen Fähnchen. Das Feld <code>weight</code> ist irrelevant! */
	public Weight[][] app;

	/** Alle Zeilendivisoren */
	public double[] divRowMin;

	/** Alle Zeilendivisoren */
	public double[] divRowMax;

	/** Alle Zeilendivisoren */
	public double[] divRowNice;

	/** Alle Spaltendivisoren */
	public double[] divColMin;

	/** Alle Spaltendivisoren */
	public double[] divColMax;

	/** Alle Spaltendivisoren */
	public double[] divColNice;

	/** Alle Zeilenmultiplikatoren */
	public double[] mulRowMin;

	/** Alle Zeilenmultiplikatoren */
	public double[] mulRowMax;

	/** Alle Zeilenmultiplikatoren */
	public double[] mulRowNice;

	/** Alle Spaltenmultiplikatoren */
	public double[] mulColMin;

	/** Alle Spaltenmultiplikatoren */
	public double[] mulColMax;

	/** Alle Spaltenmultiplikatoren */
	public double[] mulColNice;

	/** Anzahl der Iterationen (Zeilen bzw. Spalten) */
	public int rowit;

	/** Anzahl der Iterationen (Zeilen bzw. Spalten) */
	public int colit;

	/** Anzahl Fehlstände bei denen auf LR gewechselt wurde */
	public double lrSwitch = -1;

	/** Anzahl der Transfers und Updates bei Tie&Transfer */
	public int transfers;

	/** Anzahl der Transfers und Updates bei Tie&Transfer */
	public int updates;

	/** eine Fehlermeldung */
	public String sError = null;

	/** Es gibt Ties in der Matrix */
	public boolean ties = false;

	/** Das Netzwerk zur Existenzprüfung */
	public Network nw = null;

	/** Zeitverbrauch der verwendeten Biprop Methode in ms */
	public long timeElapsed;

	/** Reduzierte Genauigkeit */
	public boolean reducedAccuracy = false, faultyDivisors = false;

	/** Liefert das Weight Objekt an der Position (r,c)
	 * 
	 * @param r Zeile
	 * @param c Spalte
	 * @return Weight */
	public Weight getAppW(int r, int c)
	{
		if ((app.length <= r) || (app[0].length <= c))
		{
			Weight w = new Weight();
			w.weight = 0;
			w.rdWeight = 0;
			w.multiple = "";
			return w;
		}
		else
		{
			// alles OK
			return app[r][c];
		}
	}

	/** Liefert den minimalen Divisor von Zeile r.
	 * 
	 * @param r Zeile
	 * @return Der Divisor als String */
	public String getDivRowMin(int r)
	{
		if (r < divRowMin.length)
		{
			if (divRowMin[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(divRowMin[r]);
			}
		}
		else
		{
			return "oo";
		}
	}

	/** Liefert den maximalen Divisor von Zeile r.
	 * 
	 * @param r Zeile
	 * @return Der Divisor als String */
	public String getDivRowMax(int r)
	{
		if (r < divRowMax.length)
		{
			if (divRowMax[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(divRowMax[r]);
			}
		}
		else
		{
			return "oo";
		}
	}

	/** Liefert den "schönen" Divisor von Zeile r.
	 * 
	 * @param r Zeile
	 * @return Der Divisor als String */
	public String getDivRowNice(int r)
	{
		if (r < divRowNice.length)
		{
			if (divRowNice[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(divRowNice[r]);
			}
		}
		else
		{
			return "oo";
		}
	}

	/** Liefert den "schönen" Divisor von Spalte r.
	 * 
	 * @param r Spalte
	 * @return Der Divisor als String */
	public String getDivColNice(int r)
	{
		if (r < divColNice.length)
		{
			if (divColNice[r] == -1.0)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(divColNice[r]);
			}
		}
		else
		{
			return "oo";
		}
	}

	/** Liefert den minimalen Multiplikator von Zeile r.
	 * 
	 * @param r Zeile
	 * @return Der Multiplikator als String */
	public String getMulRowMin(int r)
	{
		if (r < mulRowMin.length)
		{
			if (mulRowMin[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(mulRowMin[r]);
			}
		}
		else
		{
			return "0";
		}
	}

	/** Liefert den maximalen Multiplikator von Zeile r.
	 * 
	 * @param r Zeile
	 * @return Der Multiplikator als String */
	public String getMulRowMax(int r)
	{
		if (r < mulRowMax.length)
		{
			if (mulRowMax[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(mulRowMax[r]);
			}
		}
		else
		{
			return "0";
		}
	}

	/** Liefert den "schönen" Multiplikator von Zeile r.
	 * 
	 * @param r Zeile
	 * @return Der Multiplikator als String */
	public String getMulRowNice(int r)
	{
		if (r < mulRowNice.length)
		{
			if (mulRowNice[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(mulRowNice[r]);
			}
		}
		else
		{
			return "0";
		}
	}

	/** Liefert den "schönen" Multiplikator von Spalte r.
	 * 
	 * @param r Spalte
	 * @return Der Multiplikator als String */
	public String getMulColNice(int r)
	{
		if (r < mulColNice.length)
		{
			if (mulColNice[r] == -1)
			{
				return "NA";
			}
			else
			{
				return Convert.doubleToString(mulColNice[r]);
			}
		}
		else
		{
			return "0";
		}
	}

}
