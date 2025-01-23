/*
 * @(#)CalculationBiprop.java 3.3 09/09/26
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Vector;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.ASMethod;
import de.uni.augsburg.bazi.lib.BipropException;
import de.uni.augsburg.bazi.lib.BipropLibMessenger;
import de.uni.augsburg.bazi.lib.BipropRet;
import de.uni.augsburg.bazi.lib.Convert;
import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.ExtendedPowerMean;
import de.uni.augsburg.bazi.lib.ExtendedStationary;
import de.uni.augsburg.bazi.lib.FinalCheckException;
import de.uni.augsburg.bazi.lib.IPFPMethod;
import de.uni.augsburg.bazi.lib.IterationExceededException;
import de.uni.augsburg.bazi.lib.IterationListener;
import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.MethodListener;
import de.uni.augsburg.bazi.lib.ParameterOutOfRangeException;
import de.uni.augsburg.bazi.lib.PermutationListener;
import de.uni.augsburg.bazi.lib.Permutations;
import de.uni.augsburg.bazi.lib.PermutationsCommunicator;
import de.uni.augsburg.bazi.lib.PermutationsInterruptedException;
import de.uni.augsburg.bazi.lib.PowerMean;
import de.uni.augsburg.bazi.lib.PrimalAugmentingAlgorithm;
import de.uni.augsburg.bazi.lib.Signpost;
import de.uni.augsburg.bazi.lib.Stationary;
import de.uni.augsburg.bazi.lib.TTflptMethod;
import de.uni.augsburg.bazi.lib.TTinteMethod;
import de.uni.augsburg.bazi.lib.Weight;
import de.uni.augsburg.bazi.lib.maxflow.Network;
import de.uni.augsburg.bazi.lib.newton.ReducedNewtonProcedureAdapter;

/** <b>Title:</b> CalculationBiprop<br>
 * <b>Description:</b> Berechnung von Biproportionalen Zuteilungen. Bindungen werden
 * dabei so weit wie möglich aufgelöst (für jede mögliche Oberzuteilung werden alle
 * möglichen Unterzuteilungen berechnet).<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher
 * @version 3.4 */
public class CalculationBiprop
{
	/* Versionshistorie:
	 * 2010.05-b-01 3.5
	 * - Einbau des primalen augmentierenden Algorithmus
	 * 2009.11-b-2009.11-b-04N 3.4
	 * - Einbau des Newton Verfahrens
	 * 2009.09-b-06: Version 3.3
	 * - Einfügen des ":"-Operators in Namen
	 * 2008.02-b-01: Version 3.2
	 * - Einführung der Versionshistorie
	 * - Überarbeitung der Methode removeLists()
	 * - Anpassen des Layouts an die JavaCodeConventions */

	/** Distrikt-Eingabedaten */
	private DistrictInputData did = null;

	/** Die einzelnen Distrikte */
	private InputData[] idData = null;

	/** NZZ verwenden? */
	private boolean bnzz = false;

	/** Verwendetes Ausgabeformat */
	private OutputFormat of = null;

	/** IterationListener */
	private IterationListener il = null;

	/** MethodListener */
	private MethodListener ml = null;

	/** Array mit allen Parteien und ihren kumulierten Stimmen
	 * über alle Distrikte (für die Oberzuteilung) */
	private Weight[] partyList = null;

	/** Speicherung der Oberzuteilungen (geordnet nach Methoden) */
	private Vector<Vector<SuperApportionment>> vSuperApps = null;

	/** Divisoren der Oberzuteilungen (geordnet nach Methoden) */
	private Divisor[] superAppDivs = null;

	/** Anzahl der Zuteilungsmethoden */
	private int numberOfMethods = 0;

	/** Gesamtsitzzahl */
	private int totalAcc = 0;

	/** Indikatoren, ob die Oberzuteilung erfolgreich war (geordnet nach Methoden) */
	private boolean[] superSuccess = null;

	/** Messenger für die Oberzuteilungen (geordnet nach Methoden) */
	private LibMessenger[] superMessengers = null;

	/** Randbedingung Zeilenzuteilung (Distriktsitze) */
	private int[] rowApp = null;

	/** Liste mit den Distriktbezeichnungen */
	private String[] districts = null;

	/** Hierhin wird die komplette Ausgabe geschrieben */
	private StringBuffer output = null;

	/** aktuell verwendete Methode */
	private int aktMethodNum = -1;

	/** Oberzuteilungen, nach Methoden geordnet */
	private String[] sSuperApp = null;

	/** Die aktuelle Oberzuteilung einer Berechnung, bereinigt, so dass nur
	 * Parteien vorhanden sind, die auch Sitze bekommen haben. */
	private Weight[] party = null;

	/** Die Startmatrix der aktuellen Berechnung */
	private Weight[][] matrix = null;

	/** Die Namen der verwendeten Rundungsmethoden */
	private Vector<String> vMethNames = null;

	/** PermutationsCommunicator */
	private PermutationsCommunicator pc = null;

	/** Erzeugt ein neues ClaculationBiprop Objekt mit den übergebenen Parametern.
	 * 
	 * @param did Eingabedaten mit mehreren Distrikten
	 * @param ilm IterationListener
	 * @param mlm MethodListener
	 * @param pc PermutationsCommunicator */
	public CalculationBiprop(DistrictInputData did, IterationListener ilm,
			MethodListener mlm, PermutationsCommunicator pc)
	{
		this.did = did;
		il = ilm;
		ml = mlm;
		this.pc = pc;

		// "private" Initialisierungen
		bnzz = (did.method == DistrictInputData.NZZ);
		of = did.outputFormat;
		idData = did.data;
		numberOfMethods = of.methods.length;

		removeLists();

		int len = idData.length;
		rowApp = new int[len];
		districts = new String[len];
		for (int i = 0; i < len; i++)
		{
			rowApp[i] = idData[i].accuracies[0];
			districts[i] = idData[i].district;
		}

		String[] methods = new String[of.methods.length];
		for (int i = 0; i < of.methods.length; i++)
		{
			methods[i] = of.methods[i].name;
		}
		if (ilm != null)
		{
			ilm.initialize(methods);
		}

		vMethNames = new Vector<String>();
	}

	/** Entfernen von Listenverbindungen */
	private void removeLists()
	{
		/* 01.02.2008 CB
		 * Das Listenentfernen hat nicht richtig funktioniert.
		 * Die Min Bedingungen wurden nicht richtig aufaddiert.
		 * Loesung: InputData-Objekte ohne "+" Zeichen initialisieren
		 * und dann das DistrictInputData Objekt neu erstellen. */
		for (int i = 0; i < idData.length; i++)
		{
			InputData akt = idData[i];
			Vector<String> names = akt.vNames;
			Vector<String> newNames = new Vector<String>();
			for (int j = 0; j < names.size(); j++)
			{
				String name = names.get(j);
				if (name.startsWith("+"))
				{
					name = name.substring(1);
				}
				newNames.add(name);
			}
			akt.vNames = newNames;
		}
		did.partyConds = new java.util.Hashtable<String, Integer>();
		did.prepare();
		// Ende Aenderung vom 01.02.2008

		/* alter Code:
		 * for (int i = 0; i < idData.length; i++) {
		 * InputData aktID = idData[i];
		 * ListInputData[] lists = aktID.listData;
		 * Vector<Weight> v = new Vector<Weight>();
		 * Weight[] w = aktID.weights;
		 * for (int j = 0; j < w.length; j++) {
		 * v.add(w[j]);
		 * }
		 * // für jede Liste
		 * for (int j = 0; j < lists.length; j++) {
		 * // remove parentWeight
		 * v.remove(lists[j].parentWeight);
		 * // add weights
		 * for (int k = 0; k < lists[j].weightcount(); k++) {
		 * v.add(lists[j].weights[k]);
		 * }
		 * }
		 * // now v contains only simple weights
		 * w = new Weight[v.size()];
		 * for (int j = 0; j < w.length; j++) {
		 * w[j] = (Weight) v.elementAt(j);
		 * }
		 * aktID.weights = w;
		 * } */
	}

	/** Startet die Berechnungen der Zuteilungen und gibt die Ergebnisse als
	 * formatierten String zurück.
	 * 
	 * @return Ergebnisse */
	public String start()
	{

		prepareParties();

		output = new StringBuffer();
		try
		{
			superApportionment();
		}
		catch (BipropException e)
		{
			System.out.println(e);
		}

		// Iterationlistener initialisieren
		String[] methodNames = new String[vMethNames.size()];
		for (int i = 0; i < vMethNames.size(); i++)
		{
			methodNames[i] = vMethNames.elementAt(i);
		}

		if (il != null)
			il.initialize(methodNames);

		subApportionments();

		if (il != null)
			il.iterationFinished();

		return output.toString();
	}

	/** Berechnung der Oberzuteilungen. Es wird für jede Methode eine mögliche
	 * Oberzuteilung berechnet, aus welcher dann (bei vorhandenen Bindungen) die
	 * Permutationen generiert werden. <code>vSuperApps</code> enthält je Methode
	 * einen Vektor, welcher die zugehörigen Zuteilungen als <code>Weight[]</code> enthält.
	 * 
	 * @throws BipropException */
	private void superApportionment() throws BipropException
	{

		collectParties();

		vSuperApps = new Vector<Vector<SuperApportionment>>(numberOfMethods);
		vSuperApps.setSize(numberOfMethods);
		superAppDivs = new Divisor[numberOfMethods];
		superSuccess = new boolean[numberOfMethods];
		superMessengers = new LibMessenger[numberOfMethods];
		sSuperApp = new String[numberOfMethods];

		Vector<Weight[]> vtmp = null;

		for (int m = 0; m < numberOfMethods; m++)
		{
			superSuccess[m] = true;
			// zuerst die Gewichte klonen
			Weight[] aktPartyWeights = new Weight[partyList.length];
			for (int i = 0; i < partyList.length; i++)
			{
				aktPartyWeights[i] = partyList[i].clonew();
			}

			// jetzt wird mit aktPartyWeights weitergearbeitet!
			// Dieses Feld und seine Klone werden später in den Permutationsvektoren
			// gespeichert und darf nicht mehr verändert werden. partyList wird
			// eventuell für weitere Zuteilungen benötigt.

			MethodData aktMethod = of.methods[m];
			superMessengers[m] = new LibMessenger();
			superAppDivs[m] = new Divisor();
			InputData id = makeInputData(aktPartyWeights, aktMethod);

			sSuperApp[m] = (new CalculationMonoprop(id)).start();
			superAppDivs[m] = id.divSave[0];

			// nur weitermachen, falls erfolgreich
			if (superSuccess[m])
			{
				vtmp = Permutations.getMonopropPermutations(aktPartyWeights);
				vSuperApps.set(m, new Vector<SuperApportionment>());
				if (vtmp.size() == 1)
				{
					vSuperApps.elementAt(m).add(new SuperApportionment(vtmp.firstElement()));
					vMethNames.add(aktMethod.name);
				}
				else
				{
					int pc = 1;
					for (Enumeration<Weight[]> e = vtmp.elements(); e.hasMoreElements();)
					{
						vSuperApps.elementAt(m).add(new SuperApportionment(e.nextElement()));
						vMethNames.add(aktMethod.name + " #" + (pc++));
					}
				}
			}
		}
	}

	/** Gibt ein Weight Array zurück, dass alle Parteien des übergebenen Arrays
	 * enthäkt, die mehr als 0 Sitze bekommen haben.
	 * 
	 * @param weights Eine Oberzuteilung
	 * @return Bereinigte Oberzuteilung */
	private Weight[] filterWeights(Weight[] weights)
	{
		Vector<Weight> vtmp = new Vector<Weight>();
		for (int i = 0; i < weights.length; i++)
		{
			if (weights[i].rdWeight > 0)
			{
				vtmp.add(weights[i]);
			}
		}
		Weight[] wret = new Weight[vtmp.size()];
		for (int i = 0; i < vtmp.size(); i++)
		{
			wret[i] = vtmp.elementAt(i);
		}
		return wret;
	}

	/** Sammelt die Parteien aller Distrikte in dem Feld <code>partyList</code>.
	 * Desweiteren wird hier die Anzahl der insgesamt zu vergebenden Sitze
	 * ermittelt (<code>totalAcc</code>). */
	private void collectParties()
	{
		// Sammeln der Parteistimmen
		WStore pStore = new WStore(1);

		// Und Ermittlung der gesamten Sitzzahl
		totalAcc = 0;

		// Normierung der Stimmenzahlen
		// erfolgt nur bei aktiviertem NZZ!

		/* mit:
		 * w_rel: Relatives Gewicht
		 * w_ij: Gewicht der j-ten Partei im Wahldistrikt i
		 * m_i: Zu vergebende Sitzzahl im i-ten Wahldistrikt
		 * k: Anzahl der Parteien im Wahldistrikt i */
		// jeder Distrikt
		for (int i = 0; i < idData.length; i++)
		{
			// Distrikt i
			InputData idp = idData[i];
			// Betrachte nur ersten Accuracy-Wert
			int iacc = idp.accuracies[0];
			totalAcc += iacc;

			// jede Partei
			for (int j = 0; j < idp.weightcount(); j++)
			{
				Weight w = idp.weights[0][0][0][j];
				double[] zw = new double[1];
				if (bnzz)
				{
					if (iacc == 0)
						zw[0] = 0.0;
					else
						zw[0] = Math.round(w.weight / iacc);
				}
				else
				{
					zw[0] = w.weight;
				}

				pStore.put(w.name, w.weight, zw);
			}
		}
		// pStore enthält nun jede aufgetretene Partei genau einmal

		// Berechne nun die Parteisitze aus totalAcc und den relativen Gewichten

		// Vorbereiten der Daten für die Zuteilungsmethode
		Vector<WStore.SumSet> vdata = pStore.getData();
		int numberOfWeights = vdata.size();
		// noch schnell nach Voten sortieren
		if (did.sortBiprop)
			for (int i = 0; i < numberOfWeights - 1; i++)
				for (int j = i + 1; j < numberOfWeights; j++)
				{
					WStore.SumSet ssa1 = vdata.elementAt(i);
					WStore.SumSet ssa2 = vdata.elementAt(j);
					if (ssa2.acc[0] > ssa1.acc[0])
					{
						vdata.setElementAt(ssa2, i);
						vdata.setElementAt(ssa1, j);
					}
				}

		partyList = new Weight[numberOfWeights];
		// double sofw = 0;
		for (int i = 0; i < numberOfWeights; i++)
		{
			WStore.SumSet ssa = vdata.elementAt(i);
			Weight w = new Weight();
			w.name = ssa.name;
			// sofw +=
			w.weight = ssa.acc[0];

			// Partei marginalie
			if (did.partyConds.get(w.name) != null)
				switch (did.outputFormat.condition)
				{
				case (OutputFormat.CONDITION_SUPER_MIN):
				case (OutputFormat.CONDITION_MIN):
					w.min = did.partyConds.get(w.name);
					break;
				case (OutputFormat.CONDITION_NAIV):
					w.naiv = did.partyConds.get(w.name);
					break;
				}

			partyList[i] = w;
		}
		// Gewichte sortieren!
		if (did.sortBiprop)
			for (int i = 0; i < numberOfWeights - 1; i++)
				for (int j = i + 1; j < numberOfWeights; j++)
					if (partyList[i].weight < partyList[j].weight)
					{
						Weight tmp = partyList[i];
						partyList[i] = partyList[j];
						partyList[j] = tmp;
					}
	}

	// /**
	// * Schreibt alle Zuteilungen.
	// * Für Debug Zwecke geeignet
	// */
	// @SuppressWarnings("unused")
	// private void printPartyApps() {
	// for (int i = 0; i < vSuperApps.size(); i++) {
	// Vector<SuperApportionment> av = vSuperApps.elementAt(i);
	// for (Enumeration e = av.elements(); e.hasMoreElements(); ) {
	// Weight[] app = (Weight[]) e.nextElement();
	// for (int j = 0; j < app.length; j++) {
	// System.out.print(app[j].rdWeight + app[j].multiple + " ");
	// }
	// System.out.println();
	// }
	// System.out.println();
	// }
	// }

	/** Berechnung aller Unterzuteilungen */
	private void subApportionments()
	{

		for (aktMethodNum = 0; aktMethodNum < of.methods.length; aktMethodNum++)
		{

			MethodData aktMethod = of.methods[aktMethodNum];
			if (aktMethod.subapportionment != null)
			{
				aktMethod = aktMethod.subapportionment;
			}

			output.append(sSuperApp[aktMethodNum]);
			output.append("\n");

			if (superSuccess[aktMethodNum])
			{
				// nur wenn die Methode erfolgreich war
				int countSuperApps = vSuperApps.elementAt(aktMethodNum).size();
				if (countSuperApps > 1)
				{
					String msg = Calculation.COMMENT +
							Resource.getString("bazi.gui.biprop.super.ties1") + " " +
							countSuperApps + " " +
							Resource.getString("bazi.gui.biprop.super.ties2") + "\n\n";
					output.append(msg + "\n");
					for (int i = 0; i < vSuperApps.elementAt(aktMethodNum).size(); i++)
					{
						SuperApportionment sa = vSuperApps.elementAt(aktMethodNum).elementAt(i);

						output.append(printSuper(sa, (i + 1)));

						subApportionment(sa, aktMethod);
					}
				}
				else
				{
					subApportionment(vSuperApps.elementAt(aktMethodNum).
							firstElement(), aktMethod);
				}
			}
		}
	}

	/** Erstellt die Ausgabetabelle einer Oberzuteilung
	 * @param sa Oberzuteilung
	 * @param num Nummer der Zuteilung (bei gleichwertigen Zuteilungen)
	 * @return Ausgabe */
	private String printSuper(SuperApportionment sa, int num)
	{
		int iParties = sa.superApportionment.length;

		Vector<String[]> vs = new Vector<String[]>();

		String[] s = new String[iParties + 3];

		s[0] = of.labelNames;
		for (int i = 0; i < iParties; i++)
		{
			s[i + 1] = "\"" + sa.superApportionment[i].name + "\"";
		}
		s[iParties + 1] = of.labelTotal;
		if (of.divisor == OutputFormat.DIV_QUOTIENT)
			s[iParties + 1] += String.format(" (%s)", MethodData.getLabel(of, false));

		s[iParties + 2] = of.labelDivisor; // Resource.getString("bazi.gui.output.divisor"); // hier sollte später auch Multiplikator etc. möglich sein
		vs.add(s);

		s = new String[iParties + 3];
		s[0] = bnzz ? Resource.getString("bazi.gui.biprop.tweights.nzz") :
				of.labelWeights;
		int sum = 0;
		for (int i = 0; i < iParties; i++)
		{
			s[i + 1] = Convert.doubleToString(sa.superApportionment[i].weight);
			sum += sa.superApportionment[i].weight;
		}
		s[iParties + 1] = sum + "";
		s[iParties + 2] = "";
		vs.add(s);

		if (of.divisor == OutputFormat.DIV_QUOTIENT && of.methods[aktMethodNum].method != MethodData.QUOTA)
			vs.addElement(Calculation.getQuotientenspalte(sa.superApportionment, superAppDivs[aktMethodNum].getDivisor(), of.methods[aktMethodNum], did));

		s = new String[iParties + 3];
		s[0] = of.methods[aktMethodNum].name + "#" + num;
		sum = 0;
		for (int i = 0; i < iParties; i++)
		{
			s[i + 1] = sa.superApportionment[i].rdWeight + "";
			sum += sa.superApportionment[i].rdWeight;
		}
		s[iParties + 1] = sum + "";

		if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA)
		{
			s[iParties + 2] = superAppDivs[aktMethodNum].getDivisor() + "";
			// System.out.println("DivColNice");
		}
		else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
		{
			s[iParties + 2] = "[" + superAppDivs[aktMethodNum].getDivisorLow() + ";" +
					superAppDivs[aktMethodNum].getDivisorHigh() + "]";
		}
		else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
		{
			s[iParties + 2] = superAppDivs[aktMethodNum].getMultiplier() + "";
		}
		else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
		{
			s[iParties + 2] = "[" + superAppDivs[aktMethodNum].getMultiplierLow() +
					";" + superAppDivs[aktMethodNum].getMultiplierHigh() + "]";
		}
		else
		{
			s[iParties + 2] = "";
		}
		vs.add(s);

		// Ausrichtung bestimmen
		int align = OutputFormat.ALIGN_HORIZONTAL;
		if (of.alignment == OutputFormat.ALIGN_VERTICAL ||
				of.alignment == OutputFormat.ALIGN_VERT_HORI)
			align = OutputFormat.ALIGN_VERTICAL;

		int lines = of.divisor == OutputFormat.DIV_QUOTIENT ? iParties + 1 : iParties + 2;
		return Calculation.COMMENT + Resource.getString("bazi.gui.biprop.name.super") +
				" #" + num + "\n" + Calculation.writeSolution(vs, lines, align);
	}
	/** Erstellt die Ausgabetabelle(n) zu einer Biproportionalen Zuteilung
	 * @param bpr Die Zuteilung
	 * @param num Nummer der Zuteilung, falls 0, so wird kein Kommentar "Unterzuteilung x" ausgegeben
	 * @return eine formatierte Tabelle (text/plain) */
	private String printSub(BipropRet bpr, int num)
	{
		int iParties = party.length; // sa.superApportionment.length;
		int iDistricts = idData.length;
		int now = 0;
		MethodData aktMethod = of.methods[aktMethodNum];
		NumberFormat nf = new DecimalFormat("#.########");
		if (aktMethod.subapportionment != null)
		{
			aktMethod = aktMethod.subapportionment;
		}

		// bei vorhandener Minimumsbedingung muss mins = now gesetzt werden
		// int mins = 0;
		boolean min = of.condition == OutputFormat.CONDITION_MIN;
		boolean diff = of.condition == OutputFormat.CONDITION_NAIV;
		boolean quotient = of.divisor == OutputFormat.DIV_QUOTIENT;

		int zusmin = 0, zusquo = 0, zusdiff = 0;
		if (min)
			zusmin++;
		if (quotient)
			zusquo++;
		if (diff)
			zusdiff++;

		// Anzahl der Spalten (min|diff)? quotient? weight seats
		int anzahl = 2 + zusmin + zusquo + zusdiff;

		Vector<String[]> vs = new Vector<String[]>();

		if (of.alignment == OutputFormat.ALIGN_VERT_HORI ||
				of.alignment == OutputFormat.ALIGN_HORIZONTAL)
		{
			now = anzahl * iParties + 2;

			String[] s = new String[anzahl * iParties + 3];

			// Erste Kopfzeile
			s[0] = "";
			s[1] = "";
			for (int i = 0; i < iParties; i++)
			{
				s[anzahl * i + 2] = "\"" + party[i].name + "\"";

				if (min)
					s[anzahl * i + 3] = of.labelCondition;
				if (quotient)
					s[anzahl * i + (3 + zusmin)] = "Quotient";

				if (did.bipropAlt == DistrictInputData.IPFP)
				{
					s[anzahl * i + (3 + zusmin + zusquo)] = Resource.getString("bazi.gui.biprop.alt.ipfp");
				}
				else if (did.bipropAlt == DistrictInputData.NEWTON)
				{
					s[anzahl * i + (3 + zusmin + zusquo)] = Resource.getString("bazi.gui.biprop.alt.newton");
				}
				else
				{
					s[anzahl * i + (3 + zusmin + zusquo)] = aktMethod.name;
				}

				if (diff)
					s[anzahl * i + (4 + zusmin + zusquo)] = of.labelCondition + "-" + aktMethod.name;
			}
			s[anzahl * iParties + 2] = of.labelDivisor;
			vs.add(s);

			// Zweite Kopfzeile
			s = new String[anzahl * iParties + 3];
			s[0] = "";
			s[1] = totalAcc + "";
			for (int i = 0; i < iParties; i++)
			{
				s[anzahl * i + 2] = "";

				if (min)
					s[anzahl * i + 3] = party[i].min + "";

				if (quotient)
					s[anzahl * i + (3 + zusmin)] = "";

				s[anzahl * i + (3 + zusmin + zusquo)] = party[i].rdWeight + "";

				if (diff)
					s[anzahl * i + (4 + zusmin + zusquo)] = (party[i].naiv - party[i].rdWeight) + "";
			}

			if (did.bipropAlt == DistrictInputData.IPFP)
			{
				s[anzahl * iParties + 2] = "[" + Resource.getString("bazi.gui.biprop.alt.ipfp") + "]";
			}
			else if (did.bipropAlt == DistrictInputData.NEWTON)
			{
				s[anzahl * iParties + 2] = Resource.getString("bazi.gui.biprop.alt.newton");
			}
			else
			{
				s[anzahl * iParties + 2] = "[" + aktMethod.name + "]";
			}
			vs.add(s);

			// Jetzt jeder Distrikt
			for (int i = 0; i < iDistricts; i++)
			{
				s = new String[anzahl * iParties + 3];
				s[0] = "\"" + idData[i].district + "\"";
				s[1] = idData[i].accuracies[0] + "";
				for (int j = 0; j < iParties; j++)
				{
					s[anzahl * j + 2] = Convert.doubleToString(matrix[i][j].weight) + "";
					Weight tmpw = bpr.getAppW(i, j);

					if (min)
						s[anzahl * j + 3] = String.valueOf(matrix[i][j].min);

					if (quotient)
					{
						double divisor = bpr.divRowNice[i] * bpr.divColNice[j];
						MethodData d = of.methods[aktMethodNum];
						s[anzahl * j + (3 + zusmin)] = Calculation.getQuotientenFeld(matrix[i][j].weight, tmpw.rdWeight, divisor, d);
					}

					if (tmpw.rdWeight != -1)
					{
						if (did.bipropAlt == DistrictInputData.IPFP)
						{
							s[anzahl * j + (3 + zusmin + zusquo)] = nf.format(tmpw.weight).replace(",", ".") +
									(((of.ties == OutputFormat.TIES_CODED) ||
									(of.ties == OutputFormat.TIES_LAC)) ? tmpw.multiple : "");
						}
						else if (did.bipropAlt == DistrictInputData.NEWTON)
						{
							s[anzahl * j + (3 + zusmin + zusquo)] = nf.format(tmpw.weight / (bpr.divRowNice[i] * bpr.divColNice[j])).replace(",", ".") +
									(((of.ties == OutputFormat.TIES_CODED) ||
									(of.ties == OutputFormat.TIES_LAC)) ? tmpw.multiple : "");
						}
						else
						{
							s[anzahl * j + (3 + zusmin + zusquo)] = tmpw.rdWeight +
									(((of.ties == OutputFormat.TIES_CODED) ||
									(of.ties == OutputFormat.TIES_LAC)) ? tmpw.multiple : "");
						}
					}
					else
					{
						s[anzahl * j + (3 + zusmin + zusquo)] = "NA";
					}

					if (diff)
						s[anzahl * j + (4 + zusmin + zusquo)] = String.valueOf(matrix[i][j].naiv - tmpw.rdWeight);
				}
				if (bpr.sError != null && bpr.sError.equals("Divisor_not_calculated"))
				{
					s[anzahl * iParties + 2] = "*";
				}
				else if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA
						|| of.divisor == OutputFormat.DIV_QUOTIENT)
				{
					s[anzahl * iParties + 2] = bpr.getDivRowNice(i);
				}
				else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
				{
					s[anzahl * iParties + 2] = "[" + bpr.getDivRowMin(i) + ";" +
							bpr.getDivRowMax(i) + "]";
				}
				else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
				{
					s[anzahl * iParties + 2] = bpr.getMulRowNice(i);
				}
				else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
				{
					s[anzahl * iParties + 2] = "[" + bpr.getMulRowMin(i) + ";" +
							bpr.getMulRowMax(i) + "]";
				}
				vs.add(s);
			}

			// Fußzeile
			s = new String[anzahl * iParties + 3];
			if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA
					|| of.divisor == OutputFormat.DIV_DIVISORINTERVAL
					|| of.divisor == OutputFormat.DIV_QUOTIENT)
			{
				s[0] = Resource.getString("bazi.gui.output.divisor");
			}
			else if ((of.divisor == OutputFormat.DIV_MULTIPLIER) || (of.divisor ==
					OutputFormat.DIV_MULTIPLIERINTERVAL))
			{
				s[0] = Resource.getString("bazi.gui.div.multiplier");
			}
			s[1] = "";
			for (int i = 0; i < iParties; i++)
			{
				s[anzahl * i + 2] = "";

				if (min)
					s[anzahl * i + 3] = "";
				if (quotient)
					s[anzahl * i + (3 + zusmin)] = "";

				if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA
						|| of.divisor == OutputFormat.DIV_DIVISORINTERVAL
						|| of.divisor == OutputFormat.DIV_QUOTIENT)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
						s[anzahl * i + (3 + zusmin + zusquo)] = bpr.getDivColNice(i) + "";
					else
						s[anzahl * i + (3 + zusmin + zusquo)] = "*";
				}
				else if ((of.divisor == OutputFormat.DIV_MULTIPLIER) || (of.divisor ==
						OutputFormat.DIV_MULTIPLIERINTERVAL))
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
						s[anzahl * i + (3 + zusmin + zusquo)] = bpr.getMulColNice(i) + "";
					else
						s[anzahl * i + (3 + zusmin + zusquo)] = "*";
				}

				if (diff)
					s[anzahl * i + (4 + zusmin + zusquo)] = "";
			}
			s[anzahl * iParties + 2] = "";
			vs.add(s);
		}
		// vertikal
		else
		{
			now = anzahl * iDistricts + 2;

			String[] s = new String[anzahl * iDistricts + 3];

			// Erste Kopfzeile
			s[0] = "";
			s[1] = "";
			for (int i = 0; i < iDistricts; i++)
			{
				s[anzahl * i + 2] = "\"" + idData[i].district + "\"";

				if (min)
					s[anzahl * i + 3] = of.labelCondition;
				if (quotient)
					s[anzahl * i + (3 + zusmin)] = "Quotient";

				if (did.bipropAlt == DistrictInputData.IPFP)
					s[anzahl * i + (3 + zusmin + zusquo)] = Resource.getString("bazi.gui.biprop.alt.ipfp");
				else if (did.bipropAlt == DistrictInputData.NEWTON)
					s[anzahl * i + (3 + zusmin + zusquo)] = Resource.getString("bazi.gui.biprop.alt.newton");
				else
					s[anzahl * i + (3 + zusmin + zusquo)] = aktMethod.name;

				if (diff)
					s[anzahl * i + (4 + zusmin + zusquo)] = of.labelCondition + "-" + aktMethod.name;
			}
			if ((of.divisor == OutputFormat.DIV_DIVISOR_QUOTA)
					|| (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
					|| (of.divisor == OutputFormat.DIV_QUOTIENT))
			{
				s[anzahl * iDistricts + 2] = Resource.getString("bazi.gui.output.divisor");
			}
			else if ((of.divisor == OutputFormat.DIV_MULTIPLIER) || (of.divisor ==
					OutputFormat.DIV_MULTIPLIERINTERVAL))
			{
				s[anzahl * iDistricts + 2] = Resource.getString("bazi.gui.div.multiplier");
			}
			vs.add(s);

			// Zweite Kopfzeile
			s = new String[anzahl * iDistricts + 3];
			s[0] = "";
			s[1] = totalAcc + "";
			for (int i = 0; i < iDistricts; i++)
			{
				s[anzahl * i + 2] = "";

				if (min)
					s[anzahl * i + 3] = "";
				if (quotient)
					s[anzahl * i + (3 + zusmin)] = "";

				s[anzahl * i + (3 + zusmin + zusquo)] = idData[i].accuracy + "";

				if (diff)
					s[anzahl * i + (4 + zusmin + zusquo)] = "";
			}
			if (did.bipropAlt == DistrictInputData.IPFP)
			{
				s[anzahl * iDistricts + 2] = "[" + Resource.getString("bazi.gui.biprop.alt.ipfp") + "]";
			}
			else if (did.bipropAlt == DistrictInputData.NEWTON)
			{
				s[anzahl * iDistricts + 2] = "[" + Resource.getString("bazi.gui.biprop.alt.newton") + "]";
			}
			else
			{
				s[anzahl * iDistricts + 2] = "[" + aktMethod.name + "]";
			}
			vs.add(s);

			// Zum Aufsummieren der Minimumsbedingungen pro Distrikt
			int[] minPerDistrict = new int[iDistricts];

			// Jetzt jede Partei
			for (int i = 0; i < iParties; i++)
			{
				s = new String[anzahl * iDistricts + 3];
				s[0] = "\"" + party[i].name + "\"";
				s[1] = party[i].rdWeight + "";
				for (int j = 0; j < iDistricts; j++)
				{
					s[anzahl * j + 2] = Convert.doubleToString(matrix[j][i].weight) + "";
					Weight tmpw = bpr.getAppW(j, i);

					minPerDistrict[j] += matrix[j][i].min;

					if (min)
						s[anzahl * j + 3] = String.valueOf(matrix[j][i].min);

					if (quotient)
					{
						double divisor = bpr.divRowNice[j] * bpr.divColNice[i];
						MethodData d = of.methods[aktMethodNum];
						s[anzahl * j + (3 + zusmin)] = Calculation.getQuotientenFeld(matrix[j][i].weight, tmpw.rdWeight, divisor, d);
					}

					if (tmpw.rdWeight != -1)
					{
						if (did.bipropAlt == DistrictInputData.IPFP)
						{
							s[anzahl * j + (3 + zusmin + zusquo)] = nf.format(tmpw.weight).replace(",", ".") +
									(((of.ties == OutputFormat.TIES_CODED) ||
									(of.ties == OutputFormat.TIES_LAC)) ? tmpw.multiple : "");
						}
						else if (did.bipropAlt == DistrictInputData.NEWTON)
						{
							s[anzahl * j + (3 + zusmin + zusquo)] = nf.format(tmpw.weight / (bpr.divRowNice[j] * bpr.divColNice[i])).replace(",", ".") +
									(((of.ties == OutputFormat.TIES_CODED) ||
									(of.ties == OutputFormat.TIES_LAC)) ? tmpw.multiple : "");
						}
						else
						{
							s[anzahl * j + (3 + zusmin + zusquo)] = tmpw.rdWeight +
									(((of.ties == OutputFormat.TIES_CODED) ||
									(of.ties == OutputFormat.TIES_LAC)) ? tmpw.multiple : "");
						}
					}
					else
					{
						s[anzahl * j + (3 + zusmin + zusquo)] = "NA";
					}

					if (diff)
						s[anzahl * j + (4 + zusmin + zusquo)] = String.valueOf(matrix[j][i].naiv - tmpw.rdWeight);
				}
				// Parteidivisoren nur nice
				if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA
						|| of.divisor == OutputFormat.DIV_QUOTIENT)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * iDistricts + 2] = bpr.getDivColNice(i);
					}
					else
					{
						s[anzahl * iDistricts + 2] = "*";
					}
				}
				else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * iDistricts + 2] = bpr.getDivColNice(i);
					}
					else
					{
						s[anzahl * iDistricts + 2] = "*";
					}
				}
				else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * iDistricts + 2] = bpr.getMulColNice(i);
					}
					else
					{
						s[anzahl * iDistricts + 2] = "*";
					}
				}
				else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * iDistricts + 2] = bpr.getMulColNice(i);
					}
					else
					{
						s[anzahl * iDistricts + 2] = "*";
					}
				}
				vs.add(s);
			}

			// Aufsummierte Minimumsbedingungen für Distrikte nachtragen
			// in der zweiten Zeile im Index 3 * i + 3 für Distrikt i
			if (min)
			{
				s = vs.get(1);
				for (int i = 0; i < iDistricts; i++)
					s[anzahl * i + 3] = String.valueOf(minPerDistrict[i]);
			}
			else if (diff)
			{
				s = vs.get(1);
				for (int i = 0; i < iDistricts; i++)
					s[anzahl * i + (4 + zusmin + zusquo)] = "";
			}

			// Fußzeile
			s = new String[anzahl * iDistricts + 3];
			s[0] = of.labelDivisor;

			s[1] = "";
			for (int i = 0; i < iDistricts; i++)
			{
				s[anzahl * i + 2] = "";

				if (min)
					s[anzahl * i + 3] = "";
				if (quotient)
					s[anzahl * i + (3 + zusmin)] = "";

				if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA
						|| of.divisor == OutputFormat.DIV_QUOTIENT)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = bpr.getDivRowNice(i) + "";
					}
					else
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = "*";
					}
				}
				else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = "[" + bpr.getDivRowMin(i) + ";" +
								bpr.getDivRowMax(i) + "]";
					}
					else
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = "*";
					}
				}
				else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = bpr.getMulRowNice(i) + "";
					}
					else
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = "*";
					}
				}
				else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
				{
					if (bpr.sError == null || !bpr.sError.equals("Divisor_not_calculated"))
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = "[" + bpr.getMulRowMin(i) + ";" +
								bpr.getMulRowMax(i) + "]";
					}
					else
					{
						s[anzahl * i + (3 + zusmin + zusquo)] = "*";
					}
				}

				if (diff)
					s[anzahl * i + (4 + zusmin + zusquo)] = "";
			}
			s[anzahl * iDistricts + 2] = "";
			vs.add(s);
		}

		String comment = "";
		if (num != 0)
		{
			comment = Calculation.COMMENT +
					Resource.getString("bazi.gui.biprop.name.sub") + " #" + num + "\n";
		}

		return "\n" + comment + Calculation.writeSolution(vs, now,
				OutputFormat.ALIGN_HORIZONTAL);
	}

	/** Erzeugt die Initialmatrix für die Bipropberechnung. Effektiv werden die
	 * Parteien entfernt, die keine Sitze in der Oberzuteilung bekommen haben.
	 * 
	 * @param sa Eine Oberzuteilung */
	private void createInitialMatrix(SuperApportionment sa)
	{
		party = filterWeights(sa.superApportionment);
		Weight[][] imat = new Weight[idData.length][party.length];
		WeightStore ws = new WeightStore();
		// zuerst mal die Parteien in einer Map speichern
		// Dies liefert dann die Zuordnung von Partei zu (Spalten-)Index
		for (int i = 0; i < party.length; i++)
		{
			ws.put(party[i].name, i);
		}

		// Jetzt wird die Matrix zusammengestellt
		for (int i = 0; i < idData.length; i++)
		{
			Weight[] wa = idData[i].weights[0][0][0];
			for (int j = 0; j < wa.length; j++)
			{
				int k = ws.getNumber(wa[j].name);
				// nur wenn die Partei vorhanden ist, wird sie übernommen
				if (k > -1)
				{
					imat[i][k] = wa[j].clonew();
				}
			}
		}

		// Jetzt noch alle übrigen Felder mit null Stimmen initialisieren
		for (int i = 0; i < idData.length; i++)
		{
			for (int j = 0; j < party.length; j++)
			{
				if (imat[i][j] == null)
				{
					Weight w = new Weight();
					w.name = party[j].name;
					w.weight = 0;
					imat[i][j] = w;
				}
			}
		}

		matrix = imat;
	}

	/** Erzeugt ein InputData Objekt mit den übergebenen Gewichten und Methoden
	 * Informationen.
	 * 
	 * Wird für die Oberzuteilung benutzt.
	 * 
	 * @param weights Gewichte
	 * @param method Rundungsmethode
	 * @return InputData */
	private InputData makeInputData(Weight[] weights, MethodData method)
	{
		InputData rid = new InputData();
		OutputFormat pof = new OutputFormat();
		rid.title = Resource.getString("bazi.gui.biprop.top");
		if (did.sortBiprop)
			rid.title += " " + Resource.getString("bazi.gui.biprop.sorted");
		pof.labelNames = of.labelNames; // Resource.getString("bazi.gui.biprop.tname");
		pof.labelWeights = bnzz ? Resource.getString("bazi.gui.biprop.tweights.nzz") :
				of.labelWeights;
		pof.labelTotal = Resource.getString("bazi.gui.biprop.ttotal");
		rid.accuracies = new int[1];
		rid.accuracies[0] = totalAcc;
		rid.weights = new Weight[1][1][1][];
		rid.weights[0][0][0] = weights;
		rid.originalWeights = weights;
		double sofw = 0;
		for (int i = 0; i < weights.length; i++)
		{
			sofw += weights[i].weight;
		}
		rid.totalWeight = sofw;
		rid.totalCondition = 0;
		if (did.outputFormat.condition == OutputFormat.CONDITION_SUPER_MIN)
			pof.condition = OutputFormat.CONDITION_MIN;
		else
			pof.condition = did.outputFormat.condition;

		// Nebenbedingung
		switch (did.outputFormat.condition)
		{
		case (OutputFormat.CONDITION_NONE):
			pof.labelCondition = "";
			break;
		case (OutputFormat.CONDITION_DIRECT):
			pof.labelCondition = Resource.getString("bazi.gui.output.over");
			break;
		case (OutputFormat.CONDITION_NAIV):
			pof.labelCondition = Resource.getString("bazi.gui.output.discrep");
			break;
		case (OutputFormat.CONDITION_SUPER_MIN):
		case (OutputFormat.CONDITION_MIN):
			pof.labelCondition = Resource.getString("bazi.gui.table.supermin");
			break;

		}

		pof.ties = OutputFormat.TIES_CODED;
		// Methoden
		MethodData[] methods = { method };
		pof.methods = methods;
		// Vorbereiten des Sicherungsfeldes
		rid.save = new Weight[1][rid.weightcount()];
		rid.divSave = new Divisor[1];
		pof.divisor = of.divisor;
		pof.labelDivisor = of.labelDivisor; // Resource.getString("bazi.gui.output.divisor");
		pof.alignment = of.alignment;
		pof.decimal_places = of.decimal_places;
		rid.outputFormat = pof;
		return rid;
	}

	/** Berechnet Zuteilungen für die Argumente und erweitert die Ausgabe
	 * entsprechend.
	 * 
	 * @param sa Eine Oberzuteilung
	 * @param aktMethod Rundungsmethode */
	private void subApportionment(SuperApportionment sa, MethodData aktMethod)
	{
		try
		{
			Signpost sp = null;
			// Signpost
			if (aktMethod.method == MethodData.PMEAN)
			{
				// Potenzrundung
				if (aktMethod.paramString == null)
				{
					sp = new PowerMean(aktMethod.param);
				}
				else
				{
					sp = new ExtendedPowerMean(aktMethod.paramString);
				}
			}
			else if (aktMethod.method == MethodData.RSTATION)
			{
				// Stationäre Rundung
				if (aktMethod.paramString == null)
				{
					sp = new Stationary(aktMethod.param);
				}
				else
				{
					sp = new ExtendedStationary(aktMethod.paramString);
				}
			}
			else
			{
				// irgendwas schiefgelaufen, eigentlich sollte keine Quotenmethode verwendet werden
				System.err.println("Quotenmethode gefunden");
				output.append("invalid method\n");
				return;
			}

			// Erstelle Startmatrix und Randbedingungen für diese Oberzuteilung
			// (Parteien mit 0 Sitzen werden nicht in die Matrix aufgenommen)
			createInitialMatrix(sa);
			int[] colApp = new int[party.length];
			for (int i = 0; i < party.length; i++)
			{
				colApp[i] = party[i].rdWeight;
			}

			// Rufe DM mit curSuperApp auf
			BipropLibMessenger libMsg = new BipropLibMessenger();
			BipropMessenger messenger = new BipropMessenger(libMsg);

			BipropRet bpr1 = new BipropRet();
			BipropRet bpr2 = null;
			try
			{
				bpr2 = computeResult(colApp, sp, libMsg, bpr1);
			}
			catch (FinalCheckException e)
			{
				output.append(Calculation.COMMENT +
						(bnzz ? Resource.getString("bazi.gui.district.nzz") :
								Resource.getString("bazi.gui.district.biprop")) + " " +
						getComputationInfo(bpr1));
				output.append("\n" + Calculation.COMMENT +
						messenger.getErrorMessage() + "\n");
				if (did.bipropAlt == DistrictInputData.ASRAND ||
						did.bipropAlt == DistrictInputData.H_ASRAND_TTFLPT ||
						did.bipropAlt == DistrictInputData.H_ASRAND_TTINTE)
				{
					output.append("You're using a randomized algorithm.\nTrying again may yield the correct result.\n\n");
				}
			}
			catch (BipropException e)
			{
				output.append("Error: " + messenger.getErrorMessage() + "\n");
			}
			catch (IterationExceededException e)
			{
				output.append(e + "\n\n");
			}

			if (bpr2 == bpr1)
			{
				// Erfolg!
				// Ausgabe bauen
				// if tied Tie-Message
				// Info-String über die Berechnung (abhängig von der benutzten Methode)
				String statOut = getComputationInfo(bpr1);

				String sTitle = bnzz ? Resource.getString("bazi.gui.district.nzz") :
						Resource.getString("bazi.gui.district.biprop");

				// Falls bpr1 Bindungen enthält, Ausgabe einer Meldung:
				// "Es gibt /n/ gleichberechtigte Unterzuteilungen!"
				// Dann Unterscheidung, je nach of.ties; falls LIST nicht aktiv,
				if (bpr1.ties)
				{
					// es gibt Bindungen....

					PermutationAdapter pa = new PermutationAdapter();
					Weight[][][] perms = null;
					PermutationsInterruptedException pex = null;
					try
					{
						perms = Permutations.getBipropPermutations(bpr1.app,
								rowApp, colApp, pa, pc);
					}
					catch (PermutationsInterruptedException pe)
					{
						pex = pe;
						// nur die erste Permutations wird zurückgegeben
						perms = new Weight[1][][];
						perms[0] = bpr1.app;
					}
					int count = perms.length;

					String msg = "";

					if (pex != null)
					{
						msg = Calculation.COMMENT + Resource.getString("bazi.biprop.permutations.interrupted1")
								+ " " + pex.numPermutations + " "
								+ Resource.getString("bazi.biprop.permutations.interrupted2") + "\n";
					}
					else if (pa.getMorePermutations())
					{
						msg = Calculation.COMMENT + Resource.getString("bazi.biprop.permutations.msg") + "\n";
					}
					else if (count > 1)
					{
						msg = Calculation.COMMENT +
								Resource.getString("bazi.gui.biprop.sub.ties1") + " " + count + " " +
								Resource.getString("bazi.gui.biprop.sub.ties2") + "\n";
					}
					output.append("\n");
					output.append(Calculation.COMMENT + sTitle + " " + statOut + msg);
					if (of.ties == OutputFormat.TIES_CODED)
					{
						bpr1.app = perms[0];
						output.append(printSub(bpr1, 0) + "\n");
					}
					else
					{
						// alle ausgeben....
						for (int i = 0; i < count; i++)
						{
							bpr1.app = perms[i];
							output.append(printSub(bpr1, (i + 1)) + "\n");
						}
					}
				}
				else
				{
					// nur eine gültige Zuteilung
					output.append("\n" + Calculation.COMMENT + sTitle + " " + statOut +
							printSub(bpr1, 0) +
							"\n");
				}

				if (bpr1.sError != null)
				{
					output.append(Calculation.COMMENT + messenger.getErrorMessage() + "\n\n");
				}
				if (bpr1.nw != null)
					if (!bpr1.nw.existSolution())
					{
						String[] parties = new String[matrix[0].length];
						for (int j = 0; j < matrix[0].length; j++)
						{
							parties[j] = matrix[0][j].name;
						}
						output.append(Calculation.COMMENT + Resource.getString("bazi.gui.biprop.na") + " " + createErrorMessage(bpr1.nw, districts, parties) + "\n");
					}

				if (bpr2.faultyDivisors)
				{
					output.append(Resource.getString("bazi.error.reducedAccuracy1") + "\n");
					output.append(Resource.getString("bazi.error.faultyDivisors") + "\n");
				}
				else if (bpr2.reducedAccuracy)
				{
					output.append(Resource.getString("bazi.error.reducedAccuracy1") + "\n");
					output.append(Resource.getString("bazi.error.reducedAccuracy2") + "\n");
					output.append(Resource.getString("bazi.error.reducedAccuracy3") + "\n");
				}
			}
			// else wird durch die Exceptions gefangen
		}
		catch (ParameterOutOfRangeException e)
		{
			System.err.println("Fehlerhafte Sprungstellenfunktion bei Methode: " +
					aktMethod);
			System.out.println(e);
		}
	}

	/** Stellt einen String mit Infos zur aktuellen Berechnung (Name und Iterationen)
	 * 
	 * @param bpr1 Ein Biprop Ergebnis
	 * @return String */
	private String getComputationInfo(BipropRet bpr1)
	{
		NumberFormat nf = new DecimalFormat("#.#########");
		String statOut = "[";
		switch (did.bipropAlt)
		{
		case (DistrictInputData.TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.ttflpt.short") +
					": " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
					", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers;
			break;
		case (DistrictInputData.TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.ttinte.short") +
					": " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
					", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers;
			break;
		case (DistrictInputData.ASMDPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.asmdpt.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit);
			break;
		case (DistrictInputData.ASRAND):
			statOut += Resource.getString("bazi.gui.biprop.alt.asrand.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit);
			break;
		case (DistrictInputData.ASEXTR):
			statOut += Resource.getString("bazi.gui.biprop.alt.asextr.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit);
			break;
		case (DistrictInputData.IPFP):
			statOut += Resource.getString("bazi.gui.biprop.alt.ipfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) + ", L1-Error=" + nf.format(de.uni.augsburg.bazi.lib.PureIPFPHelper.getPureIPFPHelper().getRealError()).replace(",", ".");
			break;
		case (DistrictInputData.NEWTON):
			statOut +=
					Resource.getString("bazi.gui.biprop.alt.newton.short") +
							": " + Resource.getString("bazi.gui.biprop.iterations") +
							"=" + (bpr1.colit + bpr1.rowit) + ", L1-Error="
							+ nf.format(de.uni.augsburg.bazi.lib.newton.ReducedNewtonProcedureAdapter.getReducedNewtonProcedureAdapter().getL1_error()).replace(",", ".");
			break;
		case (DistrictInputData.PRIMAL):
			statOut += Resource.getString("bazi.gui.biprop.alt.primal.short") +
					": " + Resource.getString("bazi.gui.biprop.primal.iterations") +
					"=(" + bpr1.colit + "," + bpr1.rowit + ")";
			break;
		case (DistrictInputData.H_ASMDPT_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asmp_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_ASRAND_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asrd_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_ASEXTR_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asex_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_IPFP_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_ipfp_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_IPFP_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_ipfp_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_ASRAND_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asrd_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_ASEXTR_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asex_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		case (DistrictInputData.H_ASMDPT_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asmp_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=" + (bpr1.colit + bpr1.rowit) +
					(" | " + Resource.getString("bazi.gui.biprop.updates") + "=" + bpr1.updates +
							", " + Resource.getString("bazi.gui.biprop.transfers") + "=" + bpr1.transfers);
			break;
		}
		statOut += "]\n";
		return statOut;
	}

	/** Initialisiert und ruft die BipropMethode anhand der Einstellungen auf.
	 * Benutzte globale Parameter: did, matrix, rowApp
	 * 
	 * @param colApp Spaltensummen
	 * @param sp Rundungsmethode
	 * @param libMsg BipropLibMessenger
	 * @param bpr1 BipropRet
	 * @return BipropRet
	 * @throws IterationExceededException Zulässige Iterationen wurde überschritten
	 * @throws BipropException Fehler bei Biprop Berechnung */
	private BipropRet computeResult(int[] colApp, Signpost sp, BipropLibMessenger libMsg, BipropRet bpr1)
			throws IterationExceededException, BipropException
	{
		BipropRet bpr2 = null;

		if (did.bipropAlt == DistrictInputData.IPFP)
		{
			de.uni.augsburg.bazi.lib.PureIPFPHelper.getPureIPFPHelper().setActive(true);
		}
		else
		{
			de.uni.augsburg.bazi.lib.PureIPFPHelper.getPureIPFPHelper().setActive(false);
		}

		if (did.bipropAlt == DistrictInputData.TTFLPT)
		{
			TTflptMethod ttflpt = new TTflptMethod(matrix, rowApp, colApp, sp, libMsg, bpr1);
			ttflpt.addIterationListener(il);
			ttflpt.addMethodListener(ml);
			bpr2 = ttflpt.calculate();
			ttflpt = null;
		}
		else if (did.bipropAlt == DistrictInputData.TTINTE)
		{
			TTinteMethod exactBip = new TTinteMethod(matrix, rowApp,
					colApp, sp, libMsg, bpr1);
			exactBip.addIterationListener(il);
			exactBip.addMethodListener(ml);
			bpr2 = exactBip.calculate();
			exactBip = null;
		}
		else if (did.bipropAlt == DistrictInputData.H_IPFP_TTFLPT)
		{
			IPFPMethod ipfp = new IPFPMethod(matrix, rowApp, colApp, sp, libMsg, bpr1,
					IPFPMethod.TTFLPT);
			ipfp.addMethodListener(ml);
			ipfp.addIterationListener(il);
			bpr2 = ipfp.calculate();
			ipfp = null;
		}
		else if (did.bipropAlt == DistrictInputData.H_IPFP_TTINTE)
		{
			IPFPMethod ipfp = new IPFPMethod(matrix, rowApp, colApp, sp, libMsg, bpr1,
					IPFPMethod.TTINTE);
			ipfp.addMethodListener(ml);
			ipfp.addIterationListener(il);
			bpr2 = ipfp.calculate();
			ipfp = null;
		}
		else if (did.bipropAlt == DistrictInputData.IPFP)
		{
			IPFPMethod ipfp = new IPFPMethod(matrix, rowApp, colApp, sp, libMsg, bpr1, IPFPMethod.NONE);
			ipfp.addMethodListener(ml);
			ipfp.addIterationListener(il);
			bpr2 = ipfp.calculate();
			ipfp = null;
		}
		else if (did.bipropAlt == DistrictInputData.NEWTON)
		{
			try
			{
				ReducedNewtonProcedureAdapter rnpa = new ReducedNewtonProcedureAdapter(matrix, rowApp, colApp, bpr1);
				rnpa.addMethodListener(ml);
				rnpa.addIterationListener(il);
				bpr2 = rnpa.calculate();
				rnpa = null;
			}
			catch (Exception e)
			{
				ml.printMessage("Fehler bei der Initialisierung:\n" + e.getClass().getName() + "\n" + e.getMessage());
			}
		}
		else if (did.bipropAlt == DistrictInputData.PRIMAL)
		{
			PrimalAugmentingAlgorithm prim = new PrimalAugmentingAlgorithm(matrix, rowApp, colApp, sp, libMsg, bpr1);
			prim.addMethodListener(ml);
			prim.addIterationListener(il);
			bpr2 = prim.calculate();
			prim = null;
		}
		else
		{
			int div_update = ASMethod.DIV_MDPT;
			int hybrid = ASMethod.HYBRID_TTFLPT;
			switch (did.bipropAlt)
			{
			case (DistrictInputData.ASMDPT):
				div_update = ASMethod.DIV_MDPT;
				hybrid = ASMethod.HYBRID_NONE;
				break;
			case (DistrictInputData.H_ASMDPT_TTFLPT):
				div_update = ASMethod.DIV_MDPT;
				hybrid = ASMethod.HYBRID_TTFLPT;
				break;
			case (DistrictInputData.H_ASMDPT_TTINTE):
				div_update = ASMethod.DIV_MDPT;
				hybrid = ASMethod.HYBRID_TTINTE;
				break;
			case (DistrictInputData.ASRAND):
				div_update = ASMethod.DIV_RAND;
				hybrid = ASMethod.HYBRID_NONE;
				break;
			case (DistrictInputData.H_ASRAND_TTFLPT):
				div_update = ASMethod.DIV_RAND;
				hybrid = ASMethod.HYBRID_TTFLPT;
				break;
			case (DistrictInputData.H_ASRAND_TTINTE):
				div_update = ASMethod.DIV_RAND;
				hybrid = ASMethod.HYBRID_TTINTE;
				break;
			case (DistrictInputData.ASEXTR):
				div_update = ASMethod.DIV_EXTR;
				hybrid = ASMethod.HYBRID_NONE;
				break;
			case (DistrictInputData.H_ASEXTR_TTFLPT):
				div_update = ASMethod.DIV_EXTR;
				hybrid = ASMethod.HYBRID_TTFLPT;
				break;
			case (DistrictInputData.H_ASEXTR_TTINTE):
				div_update = ASMethod.DIV_EXTR;
				hybrid = ASMethod.HYBRID_TTINTE;
				break;
			}

			ASMethod asmethod = new ASMethod(matrix, rowApp, colApp, sp, libMsg, bpr1,
					div_update, hybrid);
			asmethod.addMethodListener(ml);
			asmethod.addIterationListener(il);
			bpr2 = asmethod.calculate();
			asmethod = null;
		}

		return bpr2;
	}

	// /** Klont die übergebene Gewichtsmatrix
	// *
	// * @param app Weight[][]
	// * @return Weight[][] */
	// private Weight[][] cloneApp(Weight[][] app)
	// {
	// Weight[][] ret = new Weight[app.length][app[0].length];
	// for (int i = 0; i < app.length; i++)
	// {
	// for (int j = 0; j < app[0].length; j++)
	// {
	// ret[i][j] = app[i][j].clonew();
	// }
	// }
	// return ret;
	// }

	/** Erstellt aus dem Maxflow-Netzwerk die Fehlermeldung
	 * @param nw Network
	 * @param districts Namen der Distrikte
	 * @param parties Namen der Parteien
	 * @return String */
	private String createErrorMessage(Network nw, String[] districts, String[] parties)
	{
		if (nw.existSolution())
			return null;

		String tmp = Resource.getString("bazi.gui.biprop.maxflow1") + " ";

		int[] di = nw.getDistrictIndex();
		if (di.length > 0)
		{
			tmp += "\"" + districts[di[0]] + "\"";
			if (di.length > 1)
			{
				for (int i = 1; i < di.length; i++)
				{
					tmp += ", \"" + districts[di[i]] + "\"";
				}
			}
		}
		di = null;
		tmp += " " + Resource.getString("bazi.gui.biprop.maxflow2");
		tmp += nw.getDistrictTotal();
		tmp += Resource.getString("bazi.gui.biprop.maxflow3") + " ";
		int[] pi = nw.getPartyIndex();
		if (pi.length > 0)
		{
			tmp += "\"" + parties[pi[0]] + "\"";
			if (pi.length > 1)
			{
				for (int j = 1; j < pi.length; j++)
				{
					tmp += ", \"" + parties[pi[j]] + "\"";
				}
			}
		}
		pi = null;
		tmp += " " + Resource.getString("bazi.gui.biprop.maxflow4");
		tmp += nw.getPartyTotal();
		tmp += Resource.getString("bazi.gui.biprop.maxflow5") + "\n";
		return tmp;
	}

	private void prepareParties()
	{
		for (InputData id : idData)
		{
			for (Weight w : id.weights[0][0][0])
			{
				int index = w.name.lastIndexOf(":");
				if (index >= 0)
				{
					if (index < w.name.length() - 1)
					{
						w.name = w.name.substring(index + 1);
					}
					else
					{
						w.name = "";
					}
				}
			}
			for (int i = 0; i < id.weightcount(); i++)
			{
				for (int j = i + 1; j < id.weightcount(); j++)
				{
					if (id.weights[0][0][0][i].name.equals(id.weights[0][0][0][j].name))
					{
						id.weights[0][0][0][i].direct += id.weights[0][0][0][j].direct;
						if (id.weights[0][0][0][i].max < Integer.MAX_VALUE && id.weights[0][0][0][j].max < Integer.MAX_VALUE)
						{
							id.weights[0][0][0][i].max += id.weights[0][0][0][j].max;
						}
						else
						{
							id.weights[0][0][0][i].max = Integer.MAX_VALUE;
						}
						id.weights[0][0][0][i].min += id.weights[0][0][0][j].min;
						id.weights[0][0][0][i].naiv += id.weights[0][0][0][j].naiv;
						id.weights[0][0][0][i].weight += id.weights[0][0][0][j].weight;

						Weight[] new_W = new Weight[id.weightcount() - 1];
						for (int k = 0; k < id.weightcount(); k++)
						{
							if (k < j)
							{
								new_W[k] = id.weights[0][0][0][k];
							}
							else if (k > j)
							{
								new_W[k - 1] = id.weights[0][0][0][k];
							}
						}
						id.weights[0][0][0] = new_W;
						j--;
					}
				}
			}
		}
		// Aenderung 15.02.2010
		// this.did.setPartyConds();
	}

	private class PermutationAdapter implements PermutationListener
	{
		private boolean status = false;

		@Override
		public void setMorePermutations(boolean b)
		{
			status = b;
		}

		@Override
		public boolean getMorePermutations()
		{
			return status;
		}
	}
}
