/*
 * @(#)DivisorException.java 2.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> DivisorException<br>
 * <b>Description:</b> Wird geworfen, wenn beim Aufbauen des Divisors
 * (Divisor.buildData(...)) ein Fehler auftritt<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 2.1 */

public class DivisorException extends Exception
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Zeigt einen allgemeinen Fehler an */
	public static final int COMMON = 0;

	/** Zeigt an, daß die Grenzen des Divisorintervalls fehlerhaft sind */
	public static final int IO_LIMITS_FAULT = 1;

	/** Zeigt an, daß eine negative Zahl im Divisorintervall auftrat */
	public static final int IO_LESS_THAN_ZERO = 2;

	/** Zeigt an, daß bei der Rundung ein Fehler auftrat */
	public static final int ROUNDING_ERROR = 3;

	/** Nachrichten, die den Fehlern zugeordnet sind */
	public static final String[] FAULTS = {
			"General Error",
			"Limits defective (low>high)",
			"Limits less than zero",
			"Error in rounding operation" };

	/** Fehlermeldung */
	protected String mes;

	/** Fehlernummer */
	protected int iErr;

	/** Divisor (untere Grenze) */
	protected double dlo;

	/** Divisor (untere Grenze; gerundet) */
	protected double dlr;

	/** Divisor (obere Grenze) */
	protected double dho;

	/** Divisor (obere Grenze; gerundet) */
	protected double dhr;

	/** Erzeugt eine allgemeine <code>DivisorException</code> */

	/** erzeugt eine <code>DivisorException</code>, die anzeigt, dass die eingegebenen
	 * Grenzen fehlerhaft sind
	 * @param message detaillierte Nachricht
	 * @param errno der Fehlercode
	 * @param lo Untere Intervallgrenze
	 * @param ho Obere Intervallgrenze */
	public DivisorException(String message, int errno, double lo, double ho)
	{
		super(message);
		mes = message;
		iErr = errno;
		dlo = lo;
		dho = ho;
	}

	/** erzeugt eine <code>DivisorException</code>, die anzeigt, dass die
	 * gerundeten Intervallgrenzen Fehlerhaft sind
	 * @param message detaillierte Nachricht
	 * @param lo eingegebene untere Intervallgrenze
	 * @param ho eingegebene obere Intervallgrenze
	 * @param lr gerundete untere Intervallgrenze
	 * @param hr gerundete obere Intervallgrenze */
	public DivisorException(String message, double lo, double ho,
			double lr, double hr)
	{
		super(message);
		mes = message;
		iErr = ROUNDING_ERROR;
		dlo = lo;
		dho = ho;
		dlr = lr;
		dhr = hr;
	}

	/** Erzeugt eine String Repräsentation dieser Ausnahme
	 * 
	 * @return Repräsentation dieser Ausnahme */
	public String toString()
	{
		String tmp = "bazi.lib.DivisorException: " + mes + "\n";
		tmp += iErr + ": " + FAULTS[iErr];
		switch (iErr)
		{
		case COMMON:
			break;
		case IO_LIMITS_FAULT:
		{
			tmp += "\nl-orig:  " + dlo + " h-orig:  " + dho;
			break;
		}
		case IO_LESS_THAN_ZERO:
		{
			tmp += "\nl-orig:  " + dlo + " h-orig:  " + dho;
			break;
		}
		case ROUNDING_ERROR:
		{
			tmp += "\nl-orig:  " + dlo + " h-orig:  " + dho;
			tmp += "\nl-round: " + dlr + " h-round: " + dhr;
			break;
		}
		}
		return tmp;
	}

}
