/*
 * @(#)ListInputData.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse ListInputData<br>
 * <b>Description:</b> Beinhaltet die Eingabedaten einer Listenverbindung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * @version 2.1
 * @author Jan Petzold, Christian Brand, Marco Schumacher */
public class ListInputData extends InputData
{

	/** Referenz auf das Gewicht, welches aus dieser Liste besteht. */
	public Weight parentWeight;

	/** Index des Gewichts, welches aus dieser Liste besteht */
	public int parentIndex;
}
