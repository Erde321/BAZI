/*
 * @(#)OutputFormat.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.Resource;

/** <b>Title:</b> OutputFormat<br>
 * <b>Description:</b> Legt das Ausgabeformat der Zuteilung(en) fest<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * @version 2.1
 * @author Florian Kluge, Christian Brand, Marco Schumacher */
public class OutputFormat
{

	/** ID für Nebenbedingung: keine Nebenbedingung. */
	public final static int CONDITION_NONE = 0;

	/** ID für Nebenbedingung: minimale Mandate. */
	public final static int CONDITION_MIN = 1;

	/** ID für Nebenbedingung: Direktmandate. */
	public final static int CONDITION_DIRECT = 2;

	/** ID für Nebenbedingung: naive Zuteilung. */
	public final static int CONDITION_NAIV = 3;

	/** ID für Nebenbedingung: minimale Mandate in der Oberzuteilung */
	public final static int CONDITION_SUPER_MIN = 4;

	/** ID für Nebenbedingung: maximale Mandatae */
	public final static int CONDITION_MAX = 5;

	/** ID für Nebenbedingung: Mandate zwischen min und max */
	public final static int CONDITION_MIN_TO_MAX = 6;

	/** ID für Nebenbedingung: Min +x% */
	public final static int CONDITION_MIN_PLUS = 7;


	/** ID für Nebenbedingun: Min := max(Min, floor(wi / w * h)) */
	public final static int CONDITION_MIN_VPV = 8;

	/** Divisor-ID: Divisor bzw. Quote. */
	public final static int DIV_DIVISOR_QUOTA = 10;

	/** Divisor-ID: Divisorintervall. */
	public final static int DIV_DIVISORINTERVAL = 11;

	/** Divisor-ID: Multiplikator. */
	public final static int DIV_MULTIPLIER = 12;

	/** Divisor-ID: Multiplikatorintervall. */
	public final static int DIV_MULTIPLIERINTERVAL = 13;

	/** Divisor-ID: Quotientenspalte. */
	public final static int DIV_QUOTIENT = 14;

	/** ID für Ausrichtung der Ausgabe: horizontal. */
	public final static int ALIGN_HORIZONTAL = 20;

	/** ID für Ausrichtung der Ausgabe: vertikal. */
	public final static int ALIGN_VERTICAL = 21;

	/** ID für Ausrichtung der Ausgabe: Super horizontal, Sub vertikal */
	public final static int ALIGN_HORI_VERT = 22;

	/** ID für Ausrichtung der Ausgabe: Super vertikal, Sub horziontal */
	public final static int ALIGN_VERT_HORI = 23;

	/** Bindungs-ID: Codiert */
	public static final int TIES_CODED = 30;

	/** Bindungs-ID: List & Codiert */
	public static final int TIES_LAC = 31;

	/** Bindungs-ID: List */
	public static final int TIES_LIST = 32;

	/** Bezeichnung der Namen (1.Spalte der Gewichtstabelle), z.B. Partei oder Land. */
	public String labelNames = "";

	/** Bezeichnung der Gewichte (2.Spalte der Gewichtstabelle), z.B. Stimmen. */
	public String labelWeights = "";

	/** Bezeichnung der Nebenbedingung. */
	public String labelCondition = "";

	/** ID für Nebenbedingung. */
	public int condition = CONDITION_NONE;

	/** Bezeichung der Summe der Gewichte. */
	public String labelTotal = "";

	/** Feld mit den Daten über die zu benutzenden Zuteilungsmethoden.
	 * @uml.property name="methods"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public MethodData[] methods = null;

	/** Bezeichung des Divisor (Divisor, Divisorintervall, Multiplikator, Multiplikatorintervall). */
	public String labelDivisor = "";

	/** Divisor-ID. Use OutputFormat.DIV_* fields */
	public int divisor = DIV_DIVISOR_QUOTA;

	/** ID für die Ausrichtung der Ausgabe. Use OutputFormat.ALIGN_* fields */
	public int alignment = ALIGN_HORIZONTAL;

	/** ID für die Ausgabe der Bindungen */
	public int ties = TIES_CODED;

	/** Anzahl der Nachkommastellen (wenn die Gewichte keine ganzen Zahlen sind) */
	public int decimal_places = -1;

	/** Klont dieses Objekt.
	 * 
	 * @return OutputFormat */
	public OutputFormat cloneOF()
	{
		OutputFormat ofn = new OutputFormat();

		ofn.labelNames = labelNames;
		ofn.labelWeights = labelWeights;
		ofn.labelCondition = labelCondition;
		ofn.condition = condition;
		ofn.labelTotal = labelTotal;
		ofn.methods = methods; // Achtung! Methoden werden nicht geklont!
		ofn.labelDivisor = labelDivisor;
		ofn.divisor = divisor;
		ofn.alignment = alignment;
		ofn.ties = ties;
		ofn.decimal_places = decimal_places;

		return ofn;
	}

	/** Erzeugt eine String Repräsentation dieses OutputFormat
	 * 
	 * @return Ausgabe Informationen */
	@Override
	public String toString()
	{
		String tmp = "bazi.driver.OutputFormat\n";
		tmp += "\tln: " + labelNames + " lw: " + labelWeights + " lc: " +
				labelCondition + "\n";
		tmp += "\talign: " + alignment + " divisor: " + divisor + "\n";
		return tmp;
	}

	/** Bereitet dieses Objekt auf. Muss vor der Berechnung ausgeführt werden. */
	public void prepare()
	{

		// Nebenbedingungs-Beschriftungen
		if ("".equals(labelCondition))
		{
			if (condition == OutputFormat.CONDITION_NONE)
			{
				labelCondition = "";
			}
			else if (condition == OutputFormat.CONDITION_MIN)
			{
				labelCondition = Resource.getString("bazi.gui.table.minimum");
			}
			else if (condition == OutputFormat.CONDITION_DIRECT)
			{
				labelCondition = Resource.getString("bazi.gui.output.over");
			}
			else if (condition == OutputFormat.CONDITION_NAIV)
			{
				labelCondition = Resource.getString("bazi.gui.output.discrep");
			}
			else if (condition == OutputFormat.CONDITION_MAX)
			{
				labelCondition = Resource.getString("bazi.gui.table.maximum");
			}
			// TODO: weitere Bedingungen!
		}

		// "Divisor/Quote" soll nur dann angezeigt werden, falls auch beide Methodentypen
		// vertreten sind. Ansonsten wird entweder "Divisor" ODER "Quote" angezeigt
		if (divisor == OutputFormat.DIV_DIVISOR_QUOTA || divisor == OutputFormat.DIV_QUOTIENT)
		{
			boolean b = true;
			int mtmp = methods[0].method;
			int k = 1;
			while ((k < methods.length) && (b))
			{
				if ((mtmp == MethodData.QUOTA) &&
						(methods[k].method == MethodData.QUOTA))
				{
					mtmp = methods[k].method;
				}
				else if ((mtmp != MethodData.QUOTA) &&
						(methods[k].method != MethodData.QUOTA))
				{
					mtmp = methods[k].method;
				}
				else
				{
					// eine Quoten- und eine Divisormethode
					b = false;
				}
				k++;
			}
			if (b)
			{
				if (methods[0].method == MethodData.QUOTA)
				{
					labelDivisor = Resource.getString("bazi.gui.output.split");
				}
				else
				{
					labelDivisor = Resource.getString("bazi.gui.output.divisor");
				}
			}
			else
			{
				labelDivisor = Resource.getString("bazi.gui.output.divquo");
			}
		}
		else if (divisor == OutputFormat.DIV_DIVISORINTERVAL)
		{
			labelDivisor = Resource.getString("bazi.gui.div.divinterval");
		}
		else if (divisor == OutputFormat.DIV_MULTIPLIER)
		{
			labelDivisor = Resource.getString("bazi.gui.div.multiplier");
		}
		else if (divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
		{
			labelDivisor = Resource.getString("bazi.gui.div.multinterval");
		}

	}

}
