/*
 * @(#)AbstractInputData.java 2.1 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

/** <b>Title:</b> Klasse AbstractInputData<br>
 * <b>Description:</b> Abstraktion der Eingabedaten. InputData und DistrictInputData
 * leiten von dieser Klasse ab.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2008<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public abstract class AbstractInputData
{

	/** Titel dieser Berechnung */
	public String title = "";

	/** Ausgabeformat dieser Berechnung */
	public OutputFormat outputFormat;

	/** Base+Min..Max? */
	public boolean BMM = false;

	/** Powered? */
	public boolean pow = false;

	/** Base+Min..Max Daten */
	public int base, min, max;

	public double minPlusValue;

	public boolean sortBiprop = false;
}
