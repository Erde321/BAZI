/*
 * @(#)BipropLibMessenger.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> Klasse BipropLibMessenger<br>
 * <b>Beschreibung:</b> Für die Kommunikation zwischen lib und der Außenwelt. Hier werden Fehler gesetzt, die während der Berechnung auftreten.<br>
 * <b>Copyright:</b> Copyright (c) 2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class BipropLibMessenger
{

	/** kein Fehler (errorParams.length==null) */
	public final static int NONE = -1;

	/** allgemeiner Fehler (errorParams.length==0) */
	public final static int COMMON = 0;

	/** Fehler in den Eingabedaten (errorParams.length==1) */
	public final static int INPUT_ERROR = 1;

	/** Benutzerfehler bzw. Abbruch (errorParams.length==1) */
	public final static int USER_ERROR = 2;

	/** Fehler bei der Berechnung der Zuteilung (errorParams.length==2) */
	public final static int METHOD = 3;

	/** Fehler bei der Berechnung eines Divisors (errorParams.length==null) */
	public final static int DIVISOR = 4;

	/** Fehler bei Proberechnung: Divisor (errorParams.length==null) */
	public final static int DIVISOR_DEFECTIVE = 5;

	/** Fehler bei Proberechnung: Zuteilung (errorParams.length==null) */
	public final static int DISTRICT_APPORTIONMENT_DEFECTIVE = 6;

	/** Fehler bei Proberechnung: Zuteilung (errorParams.length==3) */
	public final static int PARTY_APPORTIONMENT_DEFECTIVE = 7;

	/** Fehler bei Existenzprüfung (errorParams.length==null) */
	public final static int EXISTENCE = 8;

	/** Speicherung der Fehlercodes
	 * @uml.property name="error" */
	private int error = NONE;

	/** Speicherung der internen Fehlernachricht
	 * @uml.property name="debugMessage" */
	private String debugMessage;

	/** Informationen zum Lokalisieren der Fehlernachricht
	 * @uml.property name="errorParams" */
	private String[] errorParams;

	/** Erzeugt ein neues leeres BipropMessenger Objekt */
	public BipropLibMessenger()
	{}

	/** Setzt einen Fehler mit Debug Nachricht und Parametern, die für eine Lokalisierung nützlich sind.
	 * 
	 * @param err Fehlercode
	 * @param debugMsg Debug Meldung
	 * @param params Parameter */
	void setError(int err, String debugMsg, String[] params)
	{
		error = err;
		debugMessage = debugMsg;
		errorParams = params;
	}

	/** Liefert den gesetzten Errorcode
	 * @return Errorcode
	 * @uml.property name="error" */
	public int getError()
	{
		return error;
	}

	/** Liefert die gesetzte Debug Nachricht
	 * @return Debug Nachricht
	 * @uml.property name="debugMessage" */
	public String getDebugMessage()
	{
		return debugMessage;
	}

	/** Liefert die gesetzten Fehler Parameter
	 * @return Fehlerparameter
	 * @uml.property name="errorParams" */
	public String[] getErrorParams()
	{
		return errorParams;
	}

	/** Prüft, ob ein Fehler gesetzt ist.
	 * 
	 * @return <b>true</b> wenn ein Fehlercode gesetzt ist */
	public boolean isError()
	{
		return error != NONE;
	}

}
