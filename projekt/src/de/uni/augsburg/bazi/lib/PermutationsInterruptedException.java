/*
 * @(#)PermutationsInterruptedException.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> Klasse PermutationsInterruptedException<br>
 * <b>Beschreibung:</b> Exception, die geworfen wird, wenn der Berechnungsthread unterbrochen wird,
 * wenn er gerade den ExploreCircles Algorithmus ausführt.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Robert Bertossi, Christian Brand
 * @version 3.1 */
public class PermutationsInterruptedException extends Exception
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Anzahl der schon berechneten Permutationen vor Abbruch */
	public int numPermutations = -1;

	/** Erzeugt eine Exception mit dem übergebenen Parameter
	 * 
	 * @param num Anzahl der schon berechneten Permutationen */
	public PermutationsInterruptedException(int num)
	{
		numPermutations = num;
	}

	/** Constructs a new exception with the specified detail message.
	 * 
	 * @param p0 error message
	 * @param num Anzahl der schon berechneten Permutationen */
	public PermutationsInterruptedException(String p0, int num)
	{
		super(p0);
		numPermutations = num;
	}

	/** Constructs a new exception with the specified cause and a detail message of
	 * (cause==null ? null : cause.toString()) (which typically contains the class
	 * and detail message of cause).
	 * 
	 * @param p0 Throwable
	 * @param num Anzahl der schon berechneten Permutationen */
	public PermutationsInterruptedException(Throwable p0, int num)
	{
		super(p0);
		numPermutations = num;
	}

	/** Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param p0 error message
	 * @param p1 Throwable
	 * @param num Anzahl der schon berechneten Permutationen */
	public PermutationsInterruptedException(String p0, Throwable p1, int num)
	{
		super(p0, p1);
		numPermutations = num;
	}
}
