/*
 * @(#)IterationListener.java 2.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> IterationListener<br>
 * <b>Description:</b> Definiert Methoden zur Überwachen der Biprop-Iterationen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 2.1 */

public interface IterationListener
{

	/** Muss vor Beginn der Berechnung aufgerufen werden
	 * 
	 * @param methods Die Namen aller verwendeten Rundungsmethoden */
	public void initialize(String[] methods);

	/** Kann nach jeder Iteration aufgerufen
	 * 
	 * @param cnt Anzahl der bisherigen Iterationen
	 * @param finished Gibt an, ob die Berechnung beendet wurde */
	public void iterationChanged(int cnt, boolean finished);

	/** Muss nach dem Ende der Berechnung aufgerufen werden */
	public void iterationFinished();

}
