/*
 * @(#)NumberSet.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse NumberSet<br>
 * <b>Description:</b> Hilfdatensatz für Divisor<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class NumberSet
{

	/** Untere Grenze */
	public double low;

	/** Obere Grenze */
	public double high;

	/** Zahl aus dem Intervall */
	public double c;

	/** Klonen dieses NumberSets
	 * @return ein NumberSet mit genau den selben Daten */
	public NumberSet cloneNS()
	{
		NumberSet tmp = new NumberSet();
		tmp.low = this.low;
		tmp.high = this.high;
		tmp.c = this.c;
		return tmp;
	}
}
