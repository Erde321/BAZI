/*
 * @(#)FileInputException.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

/** <b>Title:</b> Klasse FileInputException<br>
 * <br>
 * <b>Description:</b> Wird erzeugt bei Fehlern, die beim Einlesen von bazi-Dateien passieren.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */

public class FileInputException extends Exception
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Fehlermeldung */
	protected String mes;

	/** Nummer der ersten fehlerhaften Zeile */
	protected int line;

	/** Die fehlerhafte Zeile */
	protected String aktLine;

	/** Standardkonstruktor.
	 * Entspricht <code>new FileInputException("Fehler", 0, null)</code> */
	public FileInputException()
	{
		mes = "Fehler";
		line = 0;
	}

	/** Erzeugt eine neue Instanz einer FileInputException.
	 * 
	 * @param message Fehlermeldung
	 * @param l Zeilennummer
	 * @param al Inhalt der Zeile */
	public FileInputException(String message, int l, String al)
	{
		super(message);
		mes = message;
		line = l;
		aktLine = al;
	}

	/** Erzeugt eine String Repräsentation dieser Exception
	 * 
	 * @return Fehlermeldung */
	public String toString()
	{
		String tmp;
		tmp = "bazi.fileio.FileInputException: " + mes + "\n";
		tmp += "Zeile " + line + ": " + aktLine + "\n";
		return tmp;
	}
}
