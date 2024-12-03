/*
 * @(#)MethodData.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.Resource;

/** <b>Title:</b> Klasse MethodData<br>
 * <b>Description:</b> Beinhaltet Daten über die ausgewählte Methoden<br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1 // Version?
 * @author Jan Petzold, Christian Brand, Marco Schumacher */
public class MethodData
{

	/** Methoden-ID für Quotenmethode mit größten Resten. */
	public static int QUOTA = 0;

	/** Methoden-ID für Divisormethode mit stationärer Rundung. */
	public static int RSTATION = 1;

	/** Methoden-ID für Divisormethode mit Potenz-Rundung. */
	public static int PMEAN = 2;

	/** Methoden-ID. */
	public int method;

	/** Methoden-Parameter. */
	public double param;

	/** Methoden-Name. */
	public String name;

	public String paramString = null;

	public MethodData subapportionment;

	/** wird auf false gesetzt, wenn dem Konstruktor ein unbekannter Wert uebergeben wird */
	private boolean valid = true;

	/** Standardkonstruktor */
	public MethodData()
	{}

	/** Erzeugt ein neues MethodData Objekt und setzt die Felder entsprechend des
	 * übergebenen Strings.
	 * 
	 * @param n Methodenname */
	public MethodData(String n)
	{

		int index = n.indexOf("+");
		if (index >= 0)
		{
			name = n.substring(0, index);
			n = n.substring(index + 1);
			subapportionment = new MethodData(n);
		}
		else
		{
			name = n;
		}

		if (name.equals("haqgrr") || name.equals("HaQgrR") || name.equals("quogrr") ||
				name.equals("QuoGrR") || name.equals("Hare-Niemeyer") ||
				name.equals("Hare/Niemeyer") || name.equals("Hamilton") ||
				name.equals("Vinton"))
		{
			method = MethodData.QUOTA;
			param = 0.0;
		}
		else if (name.equalsIgnoreCase("hq1grr"))
		{
			method = MethodData.QUOTA;
			param = -1.0;
		}
		else if (name.equalsIgnoreCase("hq2grr"))
		{
			method = MethodData.QUOTA;
			param = -2.0;
		}
		else if (name.equalsIgnoreCase("haqWTA"))
		{
			method = MethodData.QUOTA;
			param = -0.2;
		}
		else if (name.equalsIgnoreCase("haqgR1"))
		{
			method = MethodData.QUOTA;
			param = -0.1;
		}
		else if (name.equalsIgnoreCase("hq1WTA"))
		{
			method = MethodData.QUOTA;
			param = -1.2;
		}
		else if (name.equalsIgnoreCase("hq1gR1"))
		{
			method = MethodData.QUOTA;
			param = -1.1;
		}
		else if (name.equalsIgnoreCase("hq2WTA"))
		{
			method = MethodData.QUOTA;
			param = -2.2;
		}
		else if (name.equalsIgnoreCase("hq2gR1"))
		{
			method = MethodData.QUOTA;
			param = -2.1;
		}
		else if (name.equals("divabr") || name.equals("DivAbr") ||
				name.equals("divdwn") || name.equals("DivDwn") ||
				name.equals("d'Hondt") || name.equals("d´Hondt") ||
				name.equals("d`Hondt") || name.equals("Jefferson") ||
				name.equals("Hagenbach-Bischoff"))
		{
			method = MethodData.RSTATION;
			param = 1.0;
		}
		else if (name.equals("divstd") || name.equals("DivStd") ||
				name.equals("Sainte-Lague") || name.equals("Sainté-Lague") ||
				name.equals("Saintè-Lague") || name.equals("Webster"))
		{
			method = MethodData.RSTATION;
			param = 0.5;
		}
		else if (name.equals("divauf") || name.equals("DivAuf") ||
				name.equals("divup") || name.equals("DivUp") ||
				name.equals("divupw") || name.equals("DivUpw") ||
				name.equals("Adams"))
		{
			method = MethodData.RSTATION;
			param = 0.0;
		}
		else if (name.equals("divhar") || name.equals("DivHar") ||
				name.equals("Dean"))
		{
			method = MethodData.PMEAN;
			param = -1.0;
		}
		else if (name.equals("divgeo") || name.equals("DivGeo") ||
				name.equals("Hill") || name.equals("Huntington") ||
				name.equals("Hill-Huntington") || name.equals("Hill/Huntington"))
		{
			method = MethodData.PMEAN;
			param = 0.0;
		}
		else if (name.equals("drqgrr") || name.equals("DrQgrR") ||
				name.equals("Droop"))
		{
			method = MethodData.QUOTA;
			param = 1.0;
		}
		else if (name.equals("dq1grr") || name.equals("DQ1grR"))
		{
			method = MethodData.QUOTA;
			param = 2.0;
		}
		else if (name.equals("dq2grr") || name.equals("DQ2grR"))
		{
			method = MethodData.QUOTA;
			param = 3.0;
		}
		else if (name.equals("dq3grr") || name.equals("DQ3grR"))
		{
			method = MethodData.QUOTA;
			param = 4.0;
		}
		else if (name.equals("dq4grr") || name.equals("DQ4grR"))
		{
			method = MethodData.QUOTA;
			param = 5.0;
		}
		else if (name.equals("drqWTA") || name.equals("DrQWTA"))
		{
			method = MethodData.QUOTA;
			param = 1.2;
		}
		else if (name.equals("dq1WTA") || name.equals("DQ1WTA"))
		{
			method = MethodData.QUOTA;
			param = 2.2;
		}
		else if (name.equals("dq2WTA") || name.equals("DQ2WTA"))
		{
			method = MethodData.QUOTA;
			param = 3.2;
		}
		else if (name.equals("dq3WTA") || name.equals("DQ3WTA"))
		{
			method = MethodData.QUOTA;
			param = 4.2;
		}
		else if (name.equals("dq4WTA") || name.equals("DQ4WTA"))
		{
			method = MethodData.QUOTA;
			param = 5.2;
		}
		else if (name.equals("drqgR1") || name.equals("DrQgR1"))
		{
			method = MethodData.QUOTA;
			param = 1.1;
		}
		else if (name.equals("dq1gR1") || name.equals("DQ1gR1"))
		{
			method = MethodData.QUOTA;
			param = 2.1;
		}
		else if (name.equals("dq2gR1") || name.equals("DQ2gR1"))
		{
			method = MethodData.QUOTA;
			param = 3.1;
		}
		else if (name.equals("dq3gR1") || name.equals("DQ3gR1"))
		{
			method = MethodData.QUOTA;
			param = 4.1;
		}
		else if (name.equals("dq4gR1") || name.equals("DQ4gR1"))
		{
			method = MethodData.QUOTA;
			param = 5.1;
		}
		else if (name.equals("DivPow") || name.equals("DivPot"))
		{
			// Potenzrundung
			method = MethodData.PMEAN;
		}
		else if (name.equals("DivSta"))
		{
			// stationäre Rundung
			method = MethodData.RSTATION;
		}
		else
		{
			valid = false;
		}
	}

	/** Erzeugt eine String Repräsentation dieser Methode
	 * 
	 * @return Methoden Informationen */
	@Override
	public String toString()
	{
		return "MethodData method=" + method + " param=" + param;
	}

	/** Ob das MethodData-Objekt gueltig ist
	 * @return <b>false</b> falls beim Erschaffen des Objekts ein unbekannter Wert uebergeben wurde */
	public boolean isValid()
	{
		return valid;
	}

	public static String getLabel(OutputFormat of, boolean sub)
	{
		MethodData[] methods = of.methods;

		if (sub)
		{
			for (int i = 0; i < methods.length; i++)
				if (methods[i].subapportionment != null)
					methods[i] = methods[i].subapportionment;
		}

		boolean both = false;
		int m0 = methods[0].method;
		for (MethodData m : methods)
			if ((m0 == 0 && m.method > 0) || (m0 > 0 && m.method == 0))
				both = true;

		if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA || of.divisor == OutputFormat.DIV_QUOTIENT)
		{
			if (both)
				return Resource.getString("bazi.gui.output.divquo");
			else if (m0 == 0)
				return Resource.getString("bazi.gui.output.split");
			else
				return Resource.getString("bazi.gui.output.divisor");

		}
		else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
		{
			if (both)
				return Resource.getString("bazi.gui.div.divquointerval");
			else if (m0 == 0)
				return Resource.getString("bazi.gui.div.splitinterval");
			else
				return Resource.getString("bazi.gui.div.divisorinterval");
		}
		else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
		{
			return Resource.getString("bazi.gui.div.multiplier");
		}
		else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
		{
			return Resource.getString("bazi.gui.div.multinterval");
		}

		return null;
	}
}
