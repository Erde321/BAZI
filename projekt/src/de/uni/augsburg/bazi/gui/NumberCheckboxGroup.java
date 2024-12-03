/*
 * @(#)NumberCheckboxGroup.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.util.Vector;

/** <b>Title:</b> Klasse NumberCheckboxGroup<br>
 * <b>Description:</b> Gruppe von untereinander abhängigen Objekten von NumberCheckbox<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Jan Petzold, Christian Brand
 * @version 2.1 */

public class NumberCheckboxGroup
{
	/** Vektor mit den markierten Ckeckboxen. */
	private Vector<NumberCheckbox> v = new Vector<NumberCheckbox>();

	/** Registrieren einer markierten Checkbox.
	 * 
	 * @param nc markierte NumberCheckbox
	 * @return Anzahl der markierten Checkboxen */
	public int register(NumberCheckbox nc)
	{
		v.addElement(nc);
		return v.size();
	}

	/** Entfernen einer demarkierten Checkbox.
	 * 
	 * @param nc NumberCheckbox */
	void cancel(NumberCheckbox nc)
	{
		v.removeElement(nc);
		for (int i = 0; i < v.size(); i++)
		{
			NumberCheckbox tempNc = (NumberCheckbox) v.elementAt(i);
			tempNc.setNumberState(i + 1);
		}
	}

	/** Rückgabe der Anzahl der markierten Checkboxen.
	 * 
	 * @return Anzahl der markierten Checkboxen */
	public int getRegistered()
	{
		return v.size();
	}
}
