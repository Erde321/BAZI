/*
 * @(#)Debug.java 2.1 09/01/07
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse Debug<br>
 * <b>Description:</b> Steuert die Ausgabe von Debug Informationen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 2.1 */
public class Debug
{

	/** Debug-Ausgabe für biprop */
	public static final boolean BIPROP = true;

	/** Debug-Ausgabe für (monoproportionale) Divisormethoden */
	public static final boolean DIVMETH = false;

	/** Standardkonstruktor (private, weil nicht benötigt) */
	private Debug()
	{}

}
