/*
 * @(#)MultiplicityEvent.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.AWTEvent;

/** <b>Title:</b> Klasse MultiplicityEvent<br>
 * <b>Description:</b> Wird erzeugt, wenn sich die Anzahl der Distrikte in der
 * Gewichtstabelle verändert.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.0
 * @author Florian Kluge, Robert Bertossi, Christian Brand */

public class MultiplicityEvent extends AWTEvent
{

	/** Default UID */
	private static final long serialVersionUID = 1L;
	/** Neue Anzahl der Distrikte */
	private int count;

	/** Erzeugt ein neues MultiplicityEvent.
	 * 
	 * @param source Quelle
	 * @param id Event-ID
	 * @param c Anzahl der Distrikte */
	public MultiplicityEvent(Object source, int id, int c)
	{
		super(source, id);
		count = c;
	}

	/** Liefert die Anzahl der Distrikte
	 * 
	 * @return Anzahl der Distrikte */
	public int getMultiplicity()
	{
		return count;
	}
}
