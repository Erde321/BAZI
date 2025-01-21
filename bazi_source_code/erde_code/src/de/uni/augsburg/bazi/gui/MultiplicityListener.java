/*
 * @(#)MultiplicityListener.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.util.EventListener;

/** <b>Title:</b> Klasse MultiplicityListener<br>
 * <b>Description:</b> Überwacht Änderungen der Anzahl der Distrikte<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */

public interface MultiplicityListener extends EventListener
{

	/** Wird aufgerufen, wenn sich die Anzahl der Distrikte geändert hat.
	 * 
	 * @param me MultiplicityEvent */
	public void multiplicityChanged(MultiplicityEvent me);
}
