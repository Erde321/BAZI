/*
 * @(#)District.java 2.1 18/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui.district;

/** <b>Title:</b> Klasse District<br>
 * <b>Description:</b> Datenaustausch zwischen DistrictDialog und RoundFrame<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public class District
{

	/** Distriktnummer */
	public int nummer;

	/** Distrikname */
	public String name;

	/** zu vergebene Sitze als String */
	public String mandate;

	/** Standardkonstruktor. Erzeugt einen leeren Distrikt ohne Namen */
	public District()
	{
		nummer = 0;
		name = new String();
		mandate = new String();
	}

	/** Erzeugt einen Distrikt mit den übergebenen Paramtern. Ist m null, werden
	 * die Sitze auf 0 gesetzt.
	 * 
	 * @param n Distriktnummer
	 * @param na Distriktname
	 * @param m Mandate */
	public District(int n, String na, String m)
	{
		nummer = n;
		name = na;
		mandate = (m == null ? "0" : m);
	}

	/** Erzeugt eine String-Repräsentation dieses Distrikts.
	 * 
	 * @return Distriktinformationen als String */
	public String toString()
	{
		String tmp = "Nummer: " + nummer;
		tmp += " Name: " + name;
		tmp += " Mandate: " + mandate;
		// tmp += " Cast: " + (Integer.parseInt(mandate));
		return tmp;
	}
}
