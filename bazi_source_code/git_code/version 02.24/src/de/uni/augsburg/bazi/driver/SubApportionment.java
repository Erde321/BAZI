/*
 * @(#)SubApportionment.java 3.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.lib.Weight;

/** <b>Überschrift:</b> SubApportionment<br>
 * <b>Beschreibung:</b> Stellt eine mögliche Unterzuteilung dar<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * @version 3.1
 * @author Florian Kluge, Christian Brand */
public class SubApportionment
{

	/** one possible subapportionment
	 * @uml.property name="subApportionment"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Weight[][] subApportionment = null;

	/** Erzeugt eine leere Unterzuteilung in der angegebenen Größe.
	 * 
	 * @param countDistricts Anzahl der Distritke
	 * @param countParties Anzahl der Parteien */
	public SubApportionment(int countDistricts, int countParties)
	{
		subApportionment = new Weight[countDistricts][countParties];
	}

	/** Erzeugt ein neues SubApportionment Objekt mit der übergebenen Zuteilung.
	 * 
	 * @param subApp Eine Unterzuteilung */
	public SubApportionment(Weight[][] subApp)
	{
		subApportionment = subApp;
	}

}
