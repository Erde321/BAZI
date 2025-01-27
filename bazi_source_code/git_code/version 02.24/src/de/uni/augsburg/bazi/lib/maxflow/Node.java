/*
 * @(#)Node.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.maxflow;

import java.util.Vector;

/** <b>Überschrift:</b> Node<br>
 * <b>Beschreibung:</b> Knoten in einem Flußnetzwerk<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class Node
{

	/** Vorwärts-Adjazenzliste, muß durch Aufruf von convertVectors2Arrays initialisiert sein!
	 * @uml.property name="adj"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Arc[] adj = null;

	/** Rückwärts-Adjazenzliste, muß durch Aufruf von convertVectors2Arrays initialisiert sein!
	 * @uml.property name="rAdj"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Arc[] rAdj = null;

	/** Vector zum Anlegen der Vorwärts-Adjazenzliste */
	private Vector<Arc> vAdj = null;

	/** Vector zum Anlegen der Rückwärts-Adjazenzliste */
	private Vector<Arc> vRAdj = null;

	/** Ist dieser Knoten markiert?
	 * @uml.property name="labelled" */
	private boolean labelled = false;

	/** Knotennummer, enthält bei Distrikt- bzw. Parteiknoten i bzw. j, bei Source -2, bei Sink -3 */
	public int number = -1;

	/** Knoten-ID, gibt den Index im allNodes-Array an */
	public int id = -1;

	/** Standard- und einziger Konstruktor */
	public Node()
	{
		vAdj = new Vector<Arc>();
		vRAdj = new Vector<Arc>();
	}

	/** Hinzufügen einer Vorwärtskante
	 * @param a Arc */
	public void addArc(Arc a)
	{
		vAdj.add(a);
	}

	/** Hinzufügen einer Rückwärtskante
	 * @param a Arc */
	public void addRArc(Arc a)
	{
		vRAdj.add(a);
	}

	/** Konvertierung der Adjazenzvectoren in Felder
	 * falls Vectoren leer sind, so ist das Feld <b>null</b> */
	public void convertVectorsToArrays()
	{
		adj = new Arc[vAdj.size()];
		for (int i = 0; i < adj.length; i++)
		{
			adj[i] = (Arc) vAdj.elementAt(i);
		}

		vAdj = null;

		rAdj = new Arc[vRAdj.size()];
		for (int i = 0; i < rAdj.length; i++)
		{
			rAdj[i] = (Arc) vRAdj.elementAt(i);
		}

		vRAdj = null;
	}

	/** Erzeugt eine String-Repräsentation dieses Knotens.
	 * 
	 * @return Knoten */
	public String toString()
	{
		String tmp = "";
		tmp += "Node " + id + "[";
		for (int i = 0; i < adj.length; i++)
		{
			tmp += adj[i].to.id + "/";
			tmp += adj[i].capacity + "/";
			tmp += adj[i].flow + "; ";
		}
		tmp += "]";
		if (this.isLabelled())
		{
			tmp += "L";
		}
		return tmp;
	}

	/** Auslesen, ob der Knoten markiert ist
	 * @return <b>true</b>, falls markiert
	 * @uml.property name="labelled" */
	public boolean isLabelled()
	{
		return labelled;
	}

	/** Knoten markieren */
	public void label()
	{
		labelled = true;
	}

	/** Markierung entfernen */
	public void unlabel()
	{
		labelled = false;
	}


}
