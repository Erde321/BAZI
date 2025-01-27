/*
 * @(#)PermutationListener.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> PermutationListener<br>
 * <b>Beschreibung:</b> Wird aufgerufen, falls es zu viele Permutationen gibt (nur Biprop; für die Ausgabe))<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public interface PermutationListener
{

	/** Setzen des Wertes, der angibt, ob es noch weitere Permutationen gibt
	 * @param b <b>true</b>, falls weitere Permutationen existieren
	 * @uml.property name="morePermutations" */
	public void setMorePermutations(boolean b);

	/** Gibt an, ob weitere Permuationen existieren
	 * @return <b>true</b>, falls ja
	 * @uml.property name="morePermutations" */
	public boolean getMorePermutations();

}
