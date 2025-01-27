/*
 * @(#)PermutationsWatcher.java 3.2 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import javax.swing.JOptionPane;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.PermutationsCommunicator;

/** <b>Überschrift:</b> Im Nicht-Debug Modus übernimmt diese Klasse die Aufgabe beim Berechnen der Permutationen bei Ties zu Fragen, ob fortgefahren werden soll.<br>
 * <b>Beschreibung:</b><br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * @author Robert Bertossi, Christian Brand
 * @version 3.2 */
public class PermutationsWatcher implements PermutationsCommunicator
{

	/** Aufrufender Frame */
	private RoundFrame rf;

	/** Erzeugt einen neuen PermutationsWatcher */
	public PermutationsWatcher(RoundFrame round)
	{
		rf = round;
	}

	/** Zeigt einen Auswahldialog an, um zu bestimmen, ob alle Permutationen berechnet werden sollen
	 * (Interface PermutationsCommunicator)
	 * 
	 * @return <b>true</b> wenn 'Ja' ausgewählt wurde */
	public boolean calcAllPermutations()
	{

		int val = JOptionPane.showConfirmDialog(rf,
				Resource.getString(
						"bazi.biprop.permutations.dialog"),
				Resource.getString(
						"bazi.error.attention"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return val != JOptionPane.YES_OPTION;
	}

	/** permutationChanged; ist hier leer. Die Anzahl wird nicht angezeigt.
	 * (Interface PermutationsCommunicator)
	 * 
	 * @param cnt Momentane Anzahl der Permutationen */
	public void permutationChanged(int cnt)
	{}
}
