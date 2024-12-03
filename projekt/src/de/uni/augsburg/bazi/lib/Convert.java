/*
 * @(#)Convert.java 4.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** <b>Überschrift:</b> Klasse Convert<br>
 * <b>Beschreibung:</b> Stellt eine Methode zur Verfügung um einen double-Wert in
 * die vollausgeschriebene Dezimalschreibweise umzuwandeln.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 4.1 */
public abstract class Convert
{
	/** Statische Klassen-Variable die die Formatierung durchführen. */
	private static DecimalFormat format = new DecimalFormat("#.###################");
	private static boolean firstCall = true;

	/** Formatiert einen Double-Wert in einen String.
	 * 
	 * @param d double
	 * @return Eine Stringrepräsentation des doubles in Dezimalschreibweise. */
	public static String doubleToString(double d)
	{
		if (Convert.firstCall)
		{
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
			dfs.setDecimalSeparator('.');
			Convert.format.setDecimalFormatSymbols(dfs);
			Convert.firstCall = false;
		}
		if (Double.isInfinite(d))
		{
			return "oo";
		}
		else if (Double.isNaN(d))
		{
			return new String("NaN");
		}
		/* Große Änderung: Es wird jetzt auf die Standardklassen von Java zugegriffen um
		 * ein Double Wert in einen String zu verwandeln. */
		return Convert.format.format(d);
	}
}
