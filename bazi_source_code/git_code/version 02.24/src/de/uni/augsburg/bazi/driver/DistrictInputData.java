/*
 * @(#)DistrictInputData.java 3.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.util.Hashtable;
import java.util.Vector;

import de.uni.augsburg.bazi.gui.TablePane;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse DistrictInputData<br>
 * <b>Description:</b> Eingabedaten mit mehreren Distrikten<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * @version 3.1
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */
public class DistrictInputData
		extends AbstractInputData
{

	/** keine (?) */
	public static final int NONE = 0;

	/** Seperate Distriktauswertung */
	public static final int SEPARATE = 1;

	/** Biproportionale Auswertung */
	public static final int BIPROP = 2;

	/** Neue Zürcher Zuteilung */
	public static final int NZZ = 3;

	// Biprop Algorithmen

	/** Alternating Scaling: mid-point divisors */
	public static final int ASMDPT = 4;

	/** Alternating Scaling: extreme divisors */
	public static final int ASEXTR = 5;

	/** Alternating Scaling: random divisors */
	public static final int ASRAND = 6;

	/** Iterative Proportional Fitting */
	public static final int IPFP = 7;

	/** Newton Verfahren */
	public static final int NEWTON = 8;

	/** Tie-And-Transfer: floating point arithmetic */
	public static final int TTFLPT = 10;

	/** Tie-And-Transfer: integer arithmetic */
	public static final int TTINTE = 20;

	/** Primal Augmenting Algorithm */
	public static final int PRIMAL = 30;

	/** Hybrid: ASmdpt|TTflpt */
	public static final int H_ASMDPT_TTFLPT = ASMDPT + TTFLPT;

	/** Hybrid: ASextr|TTflpt */
	public static final int H_ASEXTR_TTFLPT = ASEXTR + TTFLPT;

	/** Hybrid: ASrand|TTflpt */
	public static final int H_ASRAND_TTFLPT = ASRAND + TTFLPT;

	/** Hybrid: ASmdpt|TTinte */
	public static final int H_ASMDPT_TTINTE = ASMDPT + TTINTE;

	/** Hybrid: ASextr|TTinte */
	public static final int H_ASEXTR_TTINTE = ASEXTR + TTINTE;

	/** Hybrid: ASrand|TTinte */
	public static final int H_ASRAND_TTINTE = ASRAND + TTINTE;

	/** Hybrid: IPFP|TTflpt */
	public static final int H_IPFP_TTFLPT = IPFP + TTFLPT;

	/** Hybrid: IPFP|TTflpt */
	public static final int H_IPFP_TTINTE = IPFP + TTINTE;

	/** Standardkonstruktor */
	public DistrictInputData()
	{}

	/** Daten der einzelnen Distrikte (für Berechnungen relevantes Feld); wird mit prepare() aus districts erzeugt
	 * @uml.property name="data"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public InputData[] data = null;

	/** Daten der einzelnen Distrikte als Vector; wird beim Einlesen der Daten benutzt; ist nicht relevant für Berechnungen */
	public Vector<InputData> districts = new Vector<InputData>();

	/** Marginalien von Parteien für die Oberzuteilung; nur Parteien, die eine nicht triviale Bedingung
	 * erfüllen müssen gehören hier rein. */
	public Hashtable<String, Integer> partyConds = new Hashtable<String, Integer>();

	// /** Art der Bedingung für Parteien für die Oberzuteilung */
	// public int partyCondition = OutputFormat.CONDITION_NONE;

	/** Berechnungsmethode für mehrere Distrikte */
	public int method = NONE;

	/** Zeigt an, welche Biprop Alternative verwendet werden soll */
	public int bipropAlt = H_ASMDPT_TTFLPT;

	public boolean parseDistrictInputData(TablePane tp, String dtitle, OutputFormat of)
	{
		this.districts = new Vector<InputData>(tp.length());
		this.outputFormat = of;
		this.title = dtitle;

		// Baustelle

		return true;
	}
	/** Bereitet die Eingabedaten auf, indem die prepareData() Methode jedes
	 * InputData Objekts in data aufgerufen wird.
	 * 
	 * Wenn data noch nicht gesetzt ist, wird vorher ein neues Array aus districts
	 * erzeugt.
	 * 
	 * Hat ein Distrikt 0 Sitze zu vergeben, wird dieser Distrikt entfernt. */
	public void prepare()
	{
		boolean bd = false;

		if (data == null)
		{
			bd = true;
			data = new InputData[districts.size()];
		}

		for (int i = 0; i < districts.size(); i++)
		{
			if (bd)
			{
				InputData id = districts.elementAt(i);
				data[i] = id;
			}
			data[i].prepareData(BMM, pow, base, min, max, 0);
		}
		this.setPartyConds();
		// System.out.println(partyConds.toString());
	}

	public void setPartyConds()
	{
		this.partyConds = new Hashtable<String, Integer>();
		// Bei Mindestbedingung für die Oberzuteilung muss das Maximum der einzelnen Bedingung der Parteien genommen werden
		if (outputFormat.condition == OutputFormat.CONDITION_SUPER_MIN)
		{
			InputData id = null;
			for (int i = 0; i < data.length; i++)
			{
				id = data[i];
				for (int j = 0; j < id.weightcount(); j++)
					if (id.weights[0][0][0][j].min > 0)
					{
						if (!partyConds.containsKey(id.weights[0][0][0][j].name) ||
								partyConds.get(id.weights[0][0][0][j].name) < id.weights[0][0][0][j].min)
							partyConds.put(id.weights[0][0][0][j].name, id.weights[0][0][0][j].min);
						// WICHTIG: Bedingung im Distrikt auf 0 stellen!
						id.weights[0][0][0][j].min = 0;
					}
				id.totalCondition = 0;
			}
		}
		// Bei lokaler Mindestbedingung müssen die Summen einzelnen Bedingung der Parteien genommen werden
		else if (outputFormat.condition == OutputFormat.CONDITION_MIN)
		{
			InputData id = null;
			int tmp = 0;
			for (int i = 0; i < data.length; i++)
			{
				id = data[i];
				for (int j = 0; j < id.weightcount(); j++)
					if (id.weights[0][0][0][j].min > 0)
					{
						tmp = 0;
						if (partyConds.containsKey(id.weights[0][0][0][j].name))
							tmp = partyConds.get(id.weights[0][0][0][j].name);
						tmp += id.weights[0][0][0][j].min;
						// nur speichern, wenn Bedingung nicht trivial (>0) ist
						if (tmp > 0)
							partyConds.put(id.weights[0][0][0][j].name, tmp);
					}
			}
		}
		// Bei Diff muss für die Oberzuteilung noch die naive Zuteilung errechnet werden
		else if (outputFormat.condition == OutputFormat.CONDITION_NAIV)
		{
			for (InputData id : data)
			{
				for (Weight w : id.weights[0][0][0])
				{
					if (w.naiv > 0)
					{
						int tmp;
						if (partyConds.containsKey(w.name))
						{
							tmp = partyConds.get(w.name);
						}
						else
						{
							tmp = 0;
						}
						tmp += w.naiv;
						partyConds.put(w.name, tmp);
					}
				}
			}
		}
	}
}
