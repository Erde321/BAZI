/*
 * @(#)BipropException.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> Klasse BipropException<br>
 * <b>Beschreibung:</b> Wird geworfen, bei Fehlern bei der Biprop-Berechnung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */

public class BipropException extends Exception
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Constructs a new exception with null as its detail message. */
	public BipropException()
	{}

	/** Constructs a new exception with the specified detail message.
	 * @param p0 error message */
	public BipropException(String p0)
	{
		super(p0);
	}

	/** Constructs a new exception with the specified cause and a detail message of
	 * (cause==null ? null : cause.toString()) (which typically contains the class
	 * and detail message of cause).
	 * @param p0 Throwable */
	public BipropException(Throwable p0)
	{
		super(p0);
	}

	/** Constructs a new exception with the specified detail message and cause.
	 * @param p0 error message
	 * @param p1 Throwable */
	public BipropException(String p0, Throwable p1)
	{
		super(p0, p1);
	}
}
