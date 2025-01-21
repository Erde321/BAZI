/*
 * @(#)MethodListener.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> Klasse MethodListener<br>
 * <b>Beschreibung:</b> Für Debug-Meldungen, die beim Berechnen erzeugt werden<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public interface MethodListener
{

	/** Ausgabe einer Nachricht aus der Berechnung
	 * 
	 * @param msg String */
	public void printMessage(String msg);
}
