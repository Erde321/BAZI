/*
 * @(#)WeightsCollection.java 3.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */

package de.uni.augsburg.bazi.driver;

import java.util.Hashtable;
import java.util.Vector;

import de.uni.augsburg.bazi.lib.Weight;

/** <b>Überschrift:</b> WeightCollection<br>
 * <b>Beschreibung:</b> Sammlung und Aufsummierung über alle Distrikte bei
 * seperater Auswertung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 3.1
 * @author Florian Kluge, Christian Brand, Marco Schumacher */
public class WeightsCollection
{

	/** Anzahl der Distrikte */
	private int cntDistricts = 0;

	/** Anzahl der Methoden */
	private int cntMethods = 0;

	/** Speicherung der Elemente */
	private Vector<CollectionElement> vElements = new Vector<CollectionElement>();

	/** Zuordnung der Elemente zu Parteinamen */
	private Hashtable<String, CollectionElement> htElements = new Hashtable<String, CollectionElement>();

	/** Erstellt eine leere WeightsCollection
	 * 
	 * @param districts
	 *          Anzahl der Distrikte
	 * @param methods
	 *          Anzahl der Methoden */
	public WeightsCollection(int districts, int methods)
	{
		cntDistricts = districts;
		cntMethods = methods;
		// System.out.println("WeightsCollection[" + methods + "][" + districts
		// + "] created");
	}

	/** Ablegen von Parteidaten
	 * 
	 * <code>apps.length == cntMethods</code>!
	 * 
	 * @param name
	 *          Parteiname
	 * @param district
	 *          die Nummer des Distrikts, zu dem dieser Datensatz gehört.
	 * @param votes
	 *          Anzahl der Stimmen in diesem Distrikt
	 * @param apps
	 *          die Zuteilungen in diesem Distrikt (nach Methoden sortiert), */
	public void putParty(String name, int district, double votes, Weight[] apps)
	{
		CollectionElement ce = htElements.get(name);
		if (ce == null)
		{
			ce = new CollectionElement(cntMethods, cntDistricts, name);
			htElements.put(name, ce);
			vElements.add(ce);
		}
		ce.add(district, votes, apps);
	}

	/** Zusammenfassen der Parteien zu Fraktionen (z.B. Fraktionen im
	 * Europaparlament) */
	public void merge()
	{
		Vector<CollectionElement> temp = new Vector<CollectionElement>();
		for (CollectionElement ce : vElements)
		{
			// isoliert die Fraktion (z.B.: "Pop:EPP" => "EPP")
			int index = ce.name.lastIndexOf(":");
			if (index > 0 && index < ce.name.length() - 1)
				ce.name = ce.name.substring(index + 1);
			temp.add(ce);
		}

		// werden geleert und anschließend neu befüllt
		htElements = new Hashtable<String, CollectionElement>();
		vElements = new Vector<CollectionElement>();
		for (CollectionElement ce : temp)
		{
			// falls htElements die Fraktion ce noch nicht enthält...
			if (htElements.get(ce.name) == null)
			{
				// ... lege ce in htElements und vElements ab
				htElements.put(ce.name, ce);
				vElements.add(ce);
			}
			else
			{
				// ... sonst integriere ce in Bestehendes
				htElements.get(ce.name).merge(ce);
			}
		}
	}

	/** Sortieren der Daten nach Größe (größte Partei zuerst) */
	public void sort()
	{
		// noch schnell nach Voten sortieren
		for (int i = 0; i < vElements.size() - 1; i++)
		{
			for (int j = i; j < vElements.size(); j++)
			{
				CollectionElement ce1 = (CollectionElement) vElements
						.elementAt(i);
				CollectionElement ce2 = (CollectionElement) vElements
						.elementAt(j);
				if (ce2.sumVotes > ce1.sumVotes)
				{
					vElements.setElementAt(ce1, j);
					vElements.setElementAt(ce2, i);
				}
			}
		}
	}

	/** Auslesen der Parteinamen
	 * 
	 * @return Ein Feld mit den Namen aller Parteien */
	public String[] getPartyNames()
	{
		String[] tmp = new String[vElements.size()];
		for (int i = 0; i < vElements.size(); i++)
		{
			tmp[i] = ((CollectionElement) vElements.elementAt(i)).name;
		}
		return tmp;
	}

	/** Liefert eine Matrix mit den Stimmzahlen pro Partei und Distrikt.
	 * 
	 * @return double[parties][districts] */
	public double[][] getVotesPerParty()
	{
		double tmp[][] = new double[vElements.size()][cntDistricts];
		for (int i = 0; i < tmp.length; i++)
		{
			tmp[i] = getVotes(vElements.elementAt(i).name);
		}
		return tmp;
	}

	/** Auslesen der Votenzahl, die eine Partei in allen Distrikten erhielt.
	 * 
	 * @param party
	 *          Name der Partei
	 * @return die Votenzahl */
	public double getSumVotes(String party)
	{
		return htElements.get(party).sumVotes;
	}

	/** Auslesen der Votenzahlen, die eine Partei in den einzelnen Distrikten
	 * erhielt
	 * 
	 * @param party
	 *          Name der Partei
	 * @return die Votenzahlen, sortiert nach Distrikt */
	public double[] getVotes(String party)
	{
		return htElements.get(party).votes;
	}

	/** Auslesen der aufsummierten Sitze
	 * 
	 * @param party
	 *          Name der Partei
	 * @return ein Feld mit den Sitzen, sortiert nach Methoden */
	public int[] getSums(String party)
	{
		return htElements.get(party).seats;
	}

	/** Liefert eine Matrix mit den Summen der zugeteilten Sitze pro Methode
	 * 
	 * @return int[party][method] */
	public int[][] getSums()
	{
		int[][] tmp = new int[vElements.size()][cntMethods];
		for (int p = 0; p < tmp.length; p++)
			tmp[p] = getSums(vElements.elementAt(p).name);
		return tmp;
	}

	/** Auslesen eines Parteigewichts
	 * 
	 * @param party
	 *          Name der Partei
	 * @param district
	 *          Nummer des gewünschten Distrikts
	 * @param method
	 *          Nummer der gewünschten Methode
	 * @return zugeteiltes Gewicht an der spezifizierten Stelle */
	public Weight getWeight(String party, int district, int method)
	{
		Weight w = ((CollectionElement) htElements.get(party)).app[district][method];
		if (w == null)
		{
			// System.out.println("Creating empty Weight for party \"" + party +
			// "\" d: " + district + " m: " + method);
			return new Weight(party, 0);
		}
		else
		{
			return w;
		}
	}

	/** Gibt eine Matrix mit den zugeteilten Gewichten zurück
	 * 
	 * @return Weight[party][district][method] */
	public Weight[][][] getWeights()
	{
		Weight[][][] tmp = new Weight[vElements.size()][cntDistricts][cntMethods];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = vElements.elementAt(i).app;
		return tmp;
	}

	/** Auslesen der Parteizahl
	 * 
	 * @return die aktuelle Anzahl an Parteien */
	public int getPartyCount()
	{
		return vElements.size();
	}

	/** Ein Element einer WeightsCollection. */
	private class CollectionElement
	{
		/** Anzahl der Methoden */
		private int cntMethods = 0;

		/** Anzahl der Distrikte */
		private int cntDistricts = 0;

		/** Parteiname */
		public String name = "";

		/** Voten nach Distrikten sortiert */
		public double[] votes = null;

		/** Voten gesamt */
		public double sumVotes = 0;

		/** Zugeteilte Sitze, [district][method]
		 * 
		 * @uml.property name="app"
		 * @uml.associationEnd multiplicity="(0 -1)" */
		public Weight[][] app = null;

		/** Zugeteilte Sitze gesamt, [method] */
		private int[] seats = null;

		/** Erzeugt ein neues CollectionElement mit den übergebenen Werten und 0
		 * Sitzen in allen Distrikten.
		 * 
		 * @param methods
		 *          Anzahl der Methoden
		 * @param districts
		 *          Anzahl der Distrikte
		 * @param pName
		 *          Name der Partei */
		public CollectionElement(int methods, int districts, String pName)
		{
			cntMethods = methods;
			cntDistricts = districts;
			name = pName;

			app = new Weight[cntDistricts][cntMethods];
			seats = new int[cntMethods];
			for (int i = 0; i < cntMethods; i++)
			{
				seats[i] = 0;
			}
			votes = new double[cntDistricts];
			for (int i = 0; i < cntDistricts; i++)
			{
				votes[i] = 0;
			}
			// System.out.println("CE[" + methods + "][" + districts +
			// "] created for party " + pName);
		}

		/** Addiert die Stimmen und Sitze einer Partei eines Distrikts.
		 * 
		 * @param district
		 *          Distriktnummer
		 * @param v
		 *          Stimmenzahl
		 * @param apps
		 *          Zuteilung für jede Methode */
		public void add(int district, double v, Weight[] apps)
		{
			// hier ein Plus, damit gleichnamige Parteien im gleichen Distrikt
			// sich nicht verdrängen
			votes[district] += v;
			sumVotes += v;
			for (int i = 0; i < cntMethods; i++)
			{
				// System.out.println("i: " + i);
				// System.out.println("seats[i]: " + seats[i]);
				// System.out.println("apps[i]: " + apps[i]);
				seats[i] += apps[i].rdWeight;
			}

			for (int j = 0; j < cntMethods; j++)
			{
				if (app[district][j] == null)
					app[district][j] = apps[j];
				else if (app[district][j] != null && apps[j] != null)
				{
					app[district][j] = Weight.merge(app[district][j], apps[j]);
				}
			}
		}

		/** Fasst zwei CollectionElements zusammen.<br>
		 * Dabei wird das übergebene <b>ce</b> in das verarbeitende Object
		 * integriert
		 * 
		 * @param ce
		 *          das zu integrierende Element */
		private void merge(CollectionElement ce)
		{
			// im Prinzip nur der Reihe nach die Variablen verschmelzen
			for (int i = 0; i < votes.length; i++)
				votes[i] += ce.votes[i];

			sumVotes += ce.sumVotes;

			for (int i = 0; i < cntDistricts; i++)
				for (int j = 0; j < cntMethods; j++)
				{
					if (app[i][j] == null)
						app[i][j] = ce.app[i][j];
					else if (app[i][j] != null && ce.app[i][j] != null)
					{
						app[i][j] = Weight.merge(app[i][j], ce.app[i][j]);
					}
				}

			for (int i = 0; i < seats.length; i++)
				seats[i] += ce.seats[i];
		}

	}

}
