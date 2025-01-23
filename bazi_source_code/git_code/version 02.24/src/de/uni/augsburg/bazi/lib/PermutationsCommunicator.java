/*
 * @(#)PermutationsCommunicator.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> PermutationsCommunicator<br>
 * <b>Beschreibung:</b> Wird aufgerufen, falls es zu viele Permutationen gibt (nur Biprop; für die GUI)<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public interface PermutationsCommunicator
{

	/** Anzahl Permuationen, nach denen unterbrochen wird */
	public final static int MAX_PERMUTATIONS = 100;

	/** Abfrage, ob alle Permutationen berechnet werden sollen. Wird aufgerufen,
	 * sobald MAX_PERMUTATIONS überschritten wird
	 * @return <b>true</b>, falls ja */
	public boolean calcAllPermutations();

	/** Wird aufgerufen, wenn die Zahl der schon berechneten Permutationen mod 100 == 0 ist
	 * @param cnt Anzahl der schon berechneten Permutationen */
	public void permutationChanged(int cnt);

}
