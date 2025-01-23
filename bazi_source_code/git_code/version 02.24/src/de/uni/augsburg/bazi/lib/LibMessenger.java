/*
 * @(#)LibMessenger.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> LibMessenger<br>
 * <b>Beschreibung:</b> Sammlung der Nachrichten, die während einer Vektor-Zuteilung auftreten<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand, Marco Schumacher
 * @version 3.1 */

public class LibMessenger
{

	/** Kein Fehler */
	public static final int NONE = -1;

	/** Allgemeiner Fehler in Divisormethode */
	public static final int DIVMETH = 0;

	/** Allgemeiner Fehler in Quotenmethode */
	public static final int QUOTA = 1;

	/** Fehler bei der Berechnung des Divisors */
	public static final int DIVISOR = 2;

	/** Fehler in den Eingabedaten */
	public static final int INPUT_DEFECTIVE = 3;

	/** Fehler bei Proberechnung: Divisor */
	public static final int DIVISOR_DEFECTIVE = 4;

	/** Fehler bei Proberechnung: Zuteilung */
	public static final int APPORTIONMENT_DEFECTIVE = 5;

	/** Fehler bei Signpost-Parameter */
	public static final int PARAM = 6;

	/** Fehler bei Minimums-Bedingung */
	public static final int MINIMUM = 7;

	/** Fehler bei Doop (Quotenmethode) */
	public static final int DROOP = 8;

	/** Negative Gewichte nicht erlaubt */
	public static final int NEGATIVE_WEIGHTS = 9;

	/** Summe aller Gewichte ist 0 */
	public static final int SUM_OF_WS_ZERO = 10;

	/** Fehler: Remainder (Quotenmethode) */
	public static final int REMAIN = 11;

	/** Fehler in der Hauptzuteilung (bei Listenverbindungen) */
	public static final int ERROR_MAIN_PROP = 12;

	/** Andere Fehler */
	public static final int OTHER = 13;

	/** Fehler bei b+Min..Max Bedingung */
	public static final int MIN_SIGNPOST = 14;

	/** Fehler bei b+Min..Max Bedingung */
	public static final int BASE = 15;

	/** Schlechter Input */
	public static final int BAD_INPUT = 50;

	/** Fehler in der MaximumsBedingung */
	public static final int MAXIMUM = 100;

	/** Fehlercode, mit dem die Berechungsmethode geendet hat
	 * @uml.property name="errorCode" */
	private int errorCode = NONE;

	/** Eine optionale Nachricht, die weiteren Aufschluß über den Fehler geben soll; wird als Debugnachricht ausgegeben
	 * @uml.property name="errorMessage" */
	private String errorMessage = "";

	/** Zeigt an, daß Hare mit 0 Mandaten aufgerufen wurde => Quote NA, aber für alle 0 Sitze
	 * @uml.property name="zeroHare" */
	private boolean zeroHare = false;

	/** Zeigt an, dass eine Partei die absolute Mehrheit an Stimmen hat (gilt nur für Quotenmethoden).
	 * @uml.property name="absoluteMessage" */
	private boolean absoluteMessage = false;

	/** Name der Partei mit der absoluten Mehrheit an Stimmen (gilt nur für Quotenmethoden). */
	private String name;

	/** Stimmen der Partei mit der absoluten Mehrheit an Stimmen (gilt nur für Quotenmethoden). */
	private double votes;

	/** Gesamtstimmen zur Ausgabe der Meldung, dass eine Partei die absolute Mehrheit an Stimmen hat (gilt nur für Quotenmethoden). */
	private double allVotes;

	/** Gesamtsitze */
	private int accuracy;

	/** Name der Methode (nur bei MIN_SIGNPOST) */
	private String method;

	/** Standardkonstruktor */
	public LibMessenger()
	{}

	/** Setzen des Fehlercodes
	 * @param code die Nummer des Fehlers
	 * @uml.property name="errorCode" */
	public void setErrorCode(int code)
	{
		errorCode = code;
	}

	/** Setzt den Fehlercode und eine Debug Fehlernachricht. Bei bestimmten Fehlern
	 * ist ein int Wert erforderlich. (z.B. MINIMUM) Andere Fehlertypen ignorieren
	 * acc.
	 * 
	 * @param code Fehlertyp
	 * @param acc optionaler Fehlerwert
	 * @param message Debug-Fehlernachricht */
	public void setErrorCode(int code, int acc, String message)
	{
		errorCode = code;
		errorMessage = message;
		accuracy = acc;
	}

	/** @param name Der Name der Methode */
	public void setMethodName(String name)
	{
		method = name;
	}

	/** Setzt den LibMessenger zurück, um ihn für die nächste Berechnung
	 * vorzubereiten.
	 * Achtung: Setzt nicht alle Werte zurück, einige globale Variablen (zeroHare,
	 * absoluteMessage) werden nicht zurückgesetzt! */
	public void refresh()
	{
		errorCode = -1;
		errorMessage = "";
	}

	/** Gibt an, ob ein Fehler aufgetreten ist
	 * @return <b>true</b>, falls eine Fehlernachricht vorhanden ist */
	public boolean getError()
	{
		return errorCode > -1;
	}

	/** Auslesen des Fehlercodes
	 * @return der Fehlercode (-1, falls keiner vorhanden)
	 * @uml.property name="errorCode" */
	public int getErrorCode()
	{
		return errorCode;
	}

	public String getMethod()
	{
		return method;
	}

	public int getAccuracy()
	{
		return accuracy;
	}

	/** Setzen der Fehlernachricht
	 * @param msg die Nachricht
	 * @uml.property name="errorMessage" */
	public void setErrorMessage(String msg)
	{
		errorMessage = msg;
	}

	/** Lesen der Fehlernachricht
	 * @return die Nachricht
	 * @uml.property name="errorMessage" */
	public String getErrorMessage()
	{
		return errorMessage;
	}


	/** Setzen der Daten für die Meldung, dass eine Partei die Absolute Mehrheit
	 * an Stimmen hat (gilt nur für Quotenmethoden).
	 * 
	 * @param name String
	 * @param votes double
	 * @param allVotes double */
	public void setAbsolute(String name, double votes, double allVotes)
	{
		absoluteMessage = true;
		this.name = name;
		this.votes = votes;
		this.allVotes = allVotes;
	}

	/** getAbsolutName
	 * 
	 * @return String */
	public String getAbsolutName()
	{
		return name;
	}

	/** getAbsoluteVotes
	 * 
	 * @return double */
	public double getAbsoluteVotes()
	{
		return votes;
	}

	/** getAbsoluteAllVotes
	 * 
	 * @return double */
	public double getAbsoluteAllVotes()
	{
		return allVotes;
	}

	public boolean getAbsoluteMessage()
	{
		return absoluteMessage;
	}

	public boolean getZeroHare()
	{
		return zeroHare;
	}

	public void setZeroHare(boolean b)
	{
		zeroHare = b;
	}
}
