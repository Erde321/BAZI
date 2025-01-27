/*
 * @(#)SuperApportionment.java 3.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> SuperApportionment<br>
 * <b>Description:</b> Diese Objekt stellt eine mögliche Oberzuteilung mit allen zugehörigen Unterzuteilungen dar.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<
 * @version 3.1
 * @author Florian Kluge, Christian Brand */
public class SuperApportionment
{

	/** The array containing the apportionment for all parties
	 * @uml.property name="superApportionment"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public Weight[] superApportionment = null;

	/** The array containing the subapportionments. If error is zero, it must contain at least one entry
	 * @uml.property name="subApportionment"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	public SubApportionment[] subApportionment = null;

	/** Standardkonstruktor */
	public SuperApportionment()
	{}

	/** Erzeugt ein neues SuperApportionment Objekt mit der übergebenen Zuteilung.
	 * 
	 * @param superApp Oberzuteilung */
	public SuperApportionment(Weight[] superApp)
	{
		this.superApportionment = superApp;
	}

}
