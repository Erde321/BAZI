/*
 * @(#)Network.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.maxflow;

import de.uni.augsburg.bazi.lib.IntSet;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Überschrift:</b> Network<br>
 * <b>Beschreibung:</b> beschreibt ein Flußnetzwerk, welches für den Maxflow-(Ford-Fulkerson)Algo benötigt wird<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Robert Bertossi, Christian Brand
 * @version 3.1 */
public class Network
{

	private int initCapacity = 1000000;

	public Node source = null;

	public Node sink = null;

	/** @uml.property name="districts"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Node[] districts = null;

	/** @uml.property name="parties"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Node[] parties = null;

	/** @uml.property name="allNodes"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Node[] allNodes = null;

	/** Anzahl Kanten von Distrikten zu Parteien */
	public int innerArcs = 0;

	private boolean divMethImpervious = false;

	/** @uml.property name="weights"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	private Weight[][] weights = null;

	private int[] row = null;

	private int[] col = null;
	private int rows = -1;

	private int cols = -1;

	private int sumFlow = 0;

	private int h = 0;

	private boolean existenceCalculated = false;
	private boolean existSolution = false;

	private boolean cutCalculated = false;
	/** @uml.property name="districtIndex" */
	private int[] districtIndex = null;

	/** @uml.property name="partyIndex" */
	private int[] partyIndex = null;
	private int sumDistrict = 0;

	private int sumParty = 0;

	/** Restkapazitäten von Zeilen und Spalten (eingefügt wegen Min Bedingung) */
	private int[] remRowCap;
	private int[] remColCap;

	/** Soll die lokale Minimumsbedingung berücksichtigt werden? (eingefügt wegen Min Bedingung) */
	private boolean locMinReq = false;

	/** Erstellt ein Maxflow-Netzwerk
	 * @param ws Die Gewichte der Parteien in den Wahldistrikten
	 * @param row Districtsitze
	 * @param col Parteisitze
	 * @param divMethImpervious gibt an, ob die Methode durchlässig ist (s(0)=0 => <b>false</b> */
	public Network(Weight[][] ws, int[] row, int[] col, boolean divMethImpervious)
	{
		this.row = row;
		this.col = col;

		rows = row.length;
		cols = col.length;
		this.divMethImpervious = divMethImpervious;

		// this.weights = weights; geändert wegen lokaler Min Bedingung
		for (int i = 0; i < ws.length; i++)
		{
			if (weights == null)
				weights = new Weight[ws.length][ws[0].length];
			for (int j = 0; j < ws[0].length; j++)
				this.weights[i][j] = ws[i][j].clonew();
		}

		h = 0;
		for (int i = 0; i < row.length; i++)
		{
			h += row[i];
		}
		initCapacity = h;


		// /
		// eingefügt wegen Min Bedingung
		remRowCap = row.clone();
		remColCap = col.clone();

		// Berechnung ob es lokale Minima gibt
		locMinReq = false;
		for (int i = 0; i < weights.length; i++)
		{
			for (int j = 0; j < weights[0].length; j++)
			{
				if (weights[i][j].min > 0)
				{
					locMinReq = true;
					break;
				}
			}
			if (locMinReq)
				break;
		}

		// Überschneidungen mit divMethImpervious erkennen
		if (locMinReq && !divMethImpervious)
		{
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					if (weights[i][j].weight > 0 && (weights[i][j].min < 1))
					{
						weights[i][j].min = 1;
					}
				}
			}
			divMethImpervious = true; // damit das Netzwerk nicht doppelt angepasst wird
		}

		// Berechnung der verbleibenden Restkapazität
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				remRowCap[i] -= weights[i][j].min;
			}
		}
		for (int j = 0; j < cols; j++)
		{
			for (int i = 0; i < rows; i++)
			{
				remColCap[j] -= weights[i][j].min;
			}
		}
		// /

		// Knoten initialisieren
		allNodes = new Node[rows + cols + 2];
		source = new Node();
		source.number = -2;
		source.id = 0;
		allNodes[0] = source;
		sink = new Node();
		sink.number = -3;
		sink.id = rows + cols + 1;
		allNodes[rows + cols + 1] = sink;
		districts = new Node[rows];
		for (int i = 0; i < rows; i++)
		{
			districts[i] = new Node();
			districts[i].number = i;
			districts[i].id = 1 + i;
			allNodes[i + 1] = districts[i];
		}
		parties = new Node[cols];
		for (int j = 0; j < cols; j++)
		{
			parties[j] = new Node();
			parties[j].number = j;
			parties[j].id = 1 + rows + j;
			allNodes[rows + 1 + j] = parties[j];
		}

		// Netzwerk aufbauen
		int arcCnt = 0;
		// source
		for (int i = 0; i < rows; i++)
		{
			Arc a = new Arc();
			a.from = source;
			a.to = districts[i];
			// a.capacity = row[i]; geändert wegen lokaler Min Bedingung
			a.capacity = remRowCap[i];
			a.flow = 0;
			a.num = arcCnt++;
			source.addArc(a);
			districts[i].addRArc(a);
		}

		// District- und Parteiknoten
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				if (weights[i][j].weight > 0)
				{
					Arc a = new Arc();
					a.from = districts[i];
					a.to = parties[j];
					a.capacity = initCapacity;
					a.flow = 0;
					a.num = arcCnt++;
					districts[i].addArc(a);
					parties[j].addRArc(a);
					innerArcs++;
				}
			}
		}

		// sink
		for (int j = 0; j < cols; j++)
		{
			Arc a = new Arc();
			a.from = parties[j];
			a.to = sink;
			// a.capacity = col[j]; geändert wegen lokaler Min Bedingung
			a.capacity = remColCap[j];
			a.flow = 0;
			a.num = arcCnt++;
			parties[j].addArc(a);
			sink.addRArc(a);
		}

		// jetzt Vectoren in Arrays konvertieren
		for (int i = 0; i < allNodes.length; i++)
		{
			allNodes[i].convertVectorsToArrays();
			allNodes[i].id = i;
		}

		// bei durchlässigen Methoden ist noch die Anpassung nötig
		if (!divMethImpervious)
			adjustForPervious();

		// bei lokaler Min Bedingung muss der Gesamtfluss noch angepasst werden
		if (locMinReq)
		{
			h = 0;
			for (int i = 0; i < rows; i++)
			{
				h += remRowCap[i];
			}
		}

	}


	/** Anpassung für durchlässige (s(0)=0) Divisormethoden */
	private void adjustForPervious()
	{
		for (int i = 0; i < source.adj.length; i++)
		{
			source.adj[i].capacity -= source.adj[i].to.adj.length;
			h -= source.adj[i].to.adj.length;
		}
		for (int j = 0; j < sink.rAdj.length; j++)
		{
			sink.rAdj[j].capacity -= sink.rAdj[j].from.rAdj.length;
		}
	}

	@Override
	public String toString()
	{
		String tmp = "";
		tmp += "Network {\n";
		for (int i = 0; i < allNodes.length; i++)
		{
			tmp += "\t" + allNodes[i] + "\n";
		}
		tmp += "}";
		return tmp;
	}

	/** Gibt an, ob eine Lösung existiert.
	 * @return <b>true</b>, fall eine Lösung existiert, <b>false</b> sonst (in diesem Fall können mit weiteren Funktionen genauere Daten gelesen werden) */
	public boolean existSolution()
	{
		if (!existenceCalculated)
		{
			checkExistence();
		}
		return existSolution;
	}

	private void checkExistence()
	{
		while (containsPath());
		// System.out.println("h: " + h + " flow: " + sumFlow);
		existSolution = sumFlow == h;
		existenceCalculated = true;
	}

	private boolean containsPath()
	{
		IntSet list = new IntSet();
		int[] eps = new int[allNodes.length];
		int[] predOrd = new int[allNodes.length]; // Knoten -> Vorgänger
		Arc[] predArc = new Arc[allNodes.length]; // Knoten -> Kante zu Vorgänger
		for (int i = 0; i < allNodes.length; i++)
		{
			eps[i] = initCapacity;
			predOrd[i] = -1;
			predArc[i] = null;
		}

		unlabelAllNodes();
		list.add(0);

		// Weg suchen
		while (!sink.isLabelled())
		{
			if (!list.isEmpty())
			{
				int nodei = list.removeLast();
				Node aktNode = allNodes[nodei];
				for (int i = 0; i < aktNode.adj.length; i++)
				{
					Arc arc = aktNode.adj[i];
					Node nextNode = arc.to;
					if (!nextNode.isLabelled() && (arc.flow < arc.capacity))
					{
						predOrd[nextNode.id] = nodei;
						predArc[nextNode.id] = arc;
						eps[nextNode.id] = Math.min(eps[nodei], arc.capacity - arc.flow);
						nextNode.label();
						list.add(nextNode.id);
					}
				}

				// System.out.println("radj.len: " + aktNode.rAdj.length);
				for (int j = 0; j < aktNode.rAdj.length; j++)
				{
					// System.out.println(j);
					Arc arc = aktNode.rAdj[j];
					Node prevNode = arc.from;
					if (!prevNode.isLabelled() && (arc.flow > 0))
					{
						predOrd[prevNode.id] = -1 * nodei;
						predArc[prevNode.id] = arc;
						eps[prevNode.id] = Math.min(eps[nodei], arc.flow);
						prevNode.label();
						list.add(prevNode.id);
					}
				}
			}
			else
			{
				// es gibt keinen Weg mehr
				return false;
			}
		}

		// jetzt augmentieren
		Node aktNode = sink;
		// System.out.println("augmenting...");
		// System.out.println("source: " + source + "\n");
		while (aktNode != source)
		{
			// System.out.println(aktNode);
			if (predOrd[aktNode.id] >= 0)
			{
				predArc[aktNode.id].flow += eps[sink.id];
				aktNode = allNodes[predOrd[aktNode.id]];
			}
			else
			{
				predArc[aktNode.id].flow -= eps[sink.id];
				aktNode = allNodes[-1 * predOrd[aktNode.id]];
			}
		}
		// System.out.println("Fluß: " + eps[sink.id] + "\n");
		sumFlow += eps[sink.id];
		return true;
	}

	/** Alle Knotenmarkierungen entfernen */
	private void unlabelAllNodes()
	{
		for (int i = 0; i < allNodes.length; i++)
		{
			allNodes[i].unlabel();
		}
	}


	/* Alle Knoten markieren */
	/* private void labelAllNodes() { for (int i=0; i<allNodes.length; i++) { allNodes[i].label(); } } */

	/** Liefert die Indizes der Distrikte, in denen die Zuteilung fehlschlägt
	 * @return ein Array mit den Distriktindizes
	 * @uml.property name="districtIndex" */
	public int[] getDistrictIndex()
	{
		if (!cutCalculated)
		{
			calcCut();
		}
		return districtIndex;
	}

	/** Liefert die Indizes der Parteien, für die die Zuteilung fehlschlägt
	 * @return ein Array mit den Parteiindizes
	 * @uml.property name="partyIndex" */
	public int[] getPartyIndex()
	{
		if (!cutCalculated)
		{
			calcCut();
		}
		return partyIndex;
	}

	/** Summe der Distriktsitze (der fehlschlagenden Distrikte)
	 * @return int */
	public int getDistrictTotal()
	{
		if (!cutCalculated)
		{
			calcCut();
		}
		return sumDistrict;
	}

	/** Summe der Parteisitze (der fehlschlagenden Parteien)
	 * @return int */
	public int getPartyTotal()
	{
		if (!cutCalculated)
		{
			calcCut();
		}
		return sumParty;
	}

	/** Berechnen der Daten fur die Fehlermeldung */
	private void calcCut()
	{
		IntSet partySet = new IntSet();
		IntSet partySetOut;
		IntSet districtSet = new IntSet();
		// if (divMethImpervious) { geändert wegen lokaler Min Bedingung
		if (divMethImpervious && !locMinReq)
		{
			sumParty = 0;
			for (int j = 0; j < parties.length; j++)
			{
				if (!parties[j].isLabelled())
				{
					partySet.add(j);
					sumParty += col[j];
				}
			}
			sumDistrict = 0;
			for (int i = 0; i < districts.length; i++)
			{
				if (!districts[i].isLabelled())
				{
					districtSet.add(i);
					sumDistrict += row[i];
				}
			}
		}
		else if (!locMinReq)
		{ // else Block hinzugefügt wegen lokaler Min Bedingung
			sumParty = 0;
			for (int j = 0; j < parties.length; j++)
			{
				if (!parties[j].isLabelled())
				{
					partySet.add(j);
					sumParty += col[j];
				}
			}
			sumDistrict = 0;
			for (int i = 0; i < rows; i++)
			{
				boolean b = false;
				for (int j = 0; j < cols; j++)
				{
					if (!parties[j].isLabelled())
					{
						if (weights[i][j].weight > 0)
						{
							b = true;
						}
					}
				}
				if (b)
				{
					districtSet.add(i);
					sumDistrict += row[i];
				}
			}
			partySetOut = new IntSet(partySet);
			for (int i = 0; i < rows; i++)
			{
				if (districtSet.contains(i))
				{
					for (int j = 0; j < cols; j++)
					{
						if (!partySet.contains(j) && (weights[i][j].weight > 0))
						{
							partySetOut.add(j);
							// sumParty += col[j];
							sumParty += 1;
						}
					}
				}
			}
			partySet = partySetOut;
		}
		else
		{
			sumParty = 0;
			for (int j = 0; j < parties.length; j++)
			{
				if (!parties[j].isLabelled())
				{
					partySet.add(j);
					sumParty += col[j];
				}
			}
			sumDistrict = 0;
			for (int i = 0; i < rows; i++)
			{
				boolean b = false;
				for (int j = 0; j < cols; j++)
				{
					if (!parties[j].isLabelled())
					{
						if (weights[i][j].weight > 0)
						{
							b = true;
						}
					}
				}
				if (b)
				{
					districtSet.add(i);
					sumDistrict += row[i];
				}
			}
			partySetOut = new IntSet(partySet);
			for (int i = 0; i < rows; i++)
			{
				if (districtSet.contains(i))
				{
					for (int j = 0; j < cols; j++)
					{
						if (!partySet.contains(j) && (weights[i][j].weight > 0))
						{
							partySetOut.add(j);
							// sumParty += col[j];
							// sumParty += 1; geändert wegen lokaler Min Bedingung
							sumParty += weights[i][j].min;
						}
					}
				}
			}
			partySet = partySetOut;
		}
		partyIndex = partySet.elements();
		districtIndex = districtSet.elements();
		cutCalculated = true;
	}

}
