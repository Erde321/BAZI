/*
 * @(#)ConditionItem.java 3.2 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

/** <b>Überschrift:</b> Bedingung für Berechnung<br>
 * <b>Beschreibung:</b> Stellt eine Bedingung für die Berechnung. Kann z.B. eine Minimumsbedingung sein.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * @version 3.2
 * @author Robert Bertossi, Christian Brand */
public class ConditionItem
{

	/** Name dieser Bedingung (wird in GUI angezeigt) */
	private String name;

	/** Kurzbezeichnung (wird in Ausgabe angezeigt) */
	private String shortname;

	/** Format Konstante aus OutputFormat */
	private int format;

	/** Konstruktor zum Anlegen einer Instanz */
	public ConditionItem(String _name, String _short, int _outputformat)
	{
		name = _name;
		shortname = _short;
		format = _outputformat;
	}

	/** Liefert das Attribut name zurueck */
	public String toString()
	{
		return name;
	}

	/** @return
	 * @uml.property name="shortname" */
	public String getShortname()
	{
		return shortname;
	}

	/** @return
	 * @uml.property name="format" */
	public int getFormat()
	{
		return format;
	}
}
