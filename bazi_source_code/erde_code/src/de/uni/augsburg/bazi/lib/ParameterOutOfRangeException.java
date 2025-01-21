/*
 * @(#)ParameterOutOfRangeException.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> ParameterOutOfRangeException<br>
 * <b>Beschreibung:</b> Wird geworfen, wenn beim Initialisieren von Stationary der
 * Parameter nicht zwischen 0 und 1 liegt<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class ParameterOutOfRangeException extends Exception
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Constructs a new exception with null as its detail message. */
	public ParameterOutOfRangeException()
	{
		super();
	}

	/** Constructs a new exception with the specified detail message.
	 * @param p0 error message */
	public ParameterOutOfRangeException(String p0)
	{
		super(p0);
	}

	/** Constructs a new exception with the specified cause and a detail message of
	 * (cause==null ? null : cause.toString()) (which typically contains the class
	 * and detail message of cause).
	 * @param p0 Throwable */
	public ParameterOutOfRangeException(Throwable p0)
	{
		super(p0);
	}

	/** Constructs a new exception with the specified detail message and cause.
	 * @param p0 error message
	 * @param p1 Throwable */
	public ParameterOutOfRangeException(String p0, Throwable p1)
	{
		super(p0, p1);
	}
}
