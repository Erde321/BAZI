/*
 * @(#)Weight.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */

package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse Weight<br>
 * <b>Description:</b> Beinhaltet alle Daten über ein Gewicht<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Jan Petzold, Florian Kluge, Christian Brand, Marco Schumacher
 * @version 3.1 */
public class Weight implements Cloneable
{

	/** Name der Partei bzw. des Landes. */
	public String name = "";

	/** Gewicht bzw. Stimmen der Partei. */
	public double weight = 0.0, quotient = 0.0;

	/** Zuteilung bzw. gewonnene Mandate der Partei. */
	public int rdWeight = 0;

	/** Rest bei Quotenmethoden. */
	public double remainder = 0.0;

	/** Rang bei Quotenmethoden. */
	public int rank = 0;

	/** Eindeutige Lösung: multiple = ""
	 * Mehrfach Lösung: multiple = "+"
	 * oder multiple = "-" */
	public String multiple = "";

	/** Nebenbedingung maximale Anzahl Mandate. */
	public int max = Integer.MAX_VALUE;

	/** Nebenbedingung minimale Anzahl Mandate. */
	public int min = 0;

	// /** Nebenbedingung Basis-Mandate. */
	// public int base = 0;

	/** Nebenbedingung Direktmandate. */
	public int direct = 0;

	/** Nebenbedingung naive Zuteilung. */
	public int naiv = 0;

	/** Greift eine der Nebenbedingungen? */
	public boolean conditionEffective = false;

	/** Wert der dritten Spalte (min|overhang|discrepancy) */
	// public int thirdColumn = 0;

	/** <b>parent</b> ist true, wenn das Weight eine Listenverbindung einleitet ohne selbst eine Partei zu sein<br>
	 * (d.h. weight == 0)<br>
	 * Das wird benötigt um das Weight in CalculationSeperate.sumParties() nicht doppelt zu zählen. */
	public boolean parent = false;

	/** Standardkonstruktor erstellt ein leeres Gewicht */
	public Weight()
	{
		name = new String();
		multiple = new String();
	}

	/** Erstellt ein Gewicht für eine Partei
	 * 
	 * @param n Name der Partei
	 * @param w Stimmenzahl/Gewicht */
	public Weight(String n, double w)
	{
		name = n;
		weight = w;
		multiple = new String();
	}

	/** Der Flag-Wert als int
	 * 
	 * @return -1 für "-", 0 für "", +1 für "" */
	public int getFlag()
	{
		if (multiple.equals("+"))
		{
			return 1;
		}
		else if (multiple.equals("-"))
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}

	/** Klonen des Objects
	 * 
	 * @return Ein Weight mit genau den selben Daten */
	public Weight clonew()
	{
		Weight tmp = new Weight();
		tmp.name = name;
		tmp.weight = weight;
		tmp.rdWeight = rdWeight;
		tmp.remainder = remainder;
		tmp.rank = rank;
		tmp.multiple = multiple;
		tmp.max = max;
		tmp.min = min;
		tmp.direct = direct;
		tmp.naiv = naiv;
		tmp.parent = parent;
		// System.out.println("geklont");
		return tmp;
	}

	/** Überschreiben der toString Methode
	 * 
	 * @return "Name: AnzStimmen rd: AnzMandate" <br>
	 *         z.B. "SPD: 123456 rd: 123" */
	@Override
	public String toString()
	{
		String tmp = name;
		tmp += ": " + weight + " rd: " + rdWeight;
		return tmp;
	}

	/** Wechseln des Ties (falls vorhanden), d.h. aus + wird - und ein Sitz wird
	 * addierd und umgekehrt */
	public void flip()
	{
		if (multiple.equals("+"))
		{
			rdWeight++;
			multiple = "-";
		}
		else if (multiple.equals("-"))
		{
			rdWeight--;
			multiple = "+";
		}
		else
		{
			// do nothing
		}
	}

	/** Vereinigt zwei Weights zu einem.
	 * @param w1 Weight 1
	 * @param w2 Weight 2
	 * @return das vereinigte Weight */
	public static Weight merge(Weight w1, Weight w2)
	{
		Weight out = new Weight();
		out.weight = w1.weight + w2.weight;
		out.rdWeight = w1.rdWeight + w2.rdWeight;
		out.remainder = w1.remainder + w2.remainder;
		out.rank = w1.rank + w2.rank;

		if (w1.max == Integer.MAX_VALUE || w1.max == Integer.MAX_VALUE || Integer.MAX_VALUE - w1.max < w2.max)
			out.max = Integer.MAX_VALUE;
		else
			out.max = w1.max + w2.max;

		out.min = w1.min + w2.min;
		out.direct = w1.direct + w2.direct;
		out.naiv = w1.naiv + w2.naiv;

		return out;
	}

}
