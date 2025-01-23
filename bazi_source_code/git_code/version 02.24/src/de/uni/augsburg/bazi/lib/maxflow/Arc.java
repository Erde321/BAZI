/*
 * @(#)Arc.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.maxflow;

/** <b>Überschrift:</b> Arc<br>
 * <b>Beschreibung:</b> Kante in einem Flußnetzwerk<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class Arc
{

	/** Quellknoten */
	public Node from = null;

	/** Zielknoten */
	public Node to = null;

	/** Kapazität */
	public int capacity = -1;

	/** aktueller Fluß auf dieser Kante */
	public int flow = -1;

	/** Kantennummer */
	public int num = -1;

	/** Standardkonstruktor. Erzeugt eine leere Kante. */
	public Arc()
	{}

	/** Erzeugt eine String-Repräsentation dieser Kante
	 * 
	 * @return Kante */
	public String toString()
	{
		String tmp = "";
		tmp = "Arc " + num + " connecting " + from.id + " to " + to.id + " @ " + capacity + "/" + flow;
		return tmp;
	}
}
