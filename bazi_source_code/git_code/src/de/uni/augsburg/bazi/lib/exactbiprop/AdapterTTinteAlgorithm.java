/*
 * @(#)AdapterTTinteAlgorithm.java 1.1 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.exactbiprop;

import java.math.BigInteger;

/** <b>Überschrift:</b> AdapterTTinteAlgorithm<br>
 * <b>Beschreibung:</b> Adapter Klasse zur Implementierung des TTinte Algorithmus
 * von Martin Zachariasen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2008<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 1.1
 * @author Christian Brand */
public class AdapterTTinteAlgorithm extends ExactBipropDivMethod
{

	/** Einbinden des Konstruktors der Superklasse */
	public AdapterTTinteAlgorithm(int[][] weights, int[] r, int[] c, ExactSignPost sp)
	{
		super(weights, r, c, sp);
	}

	/** Einbinden des Konstruktors der Superklasse */
	public AdapterTTinteAlgorithm(double[][] weights, int[] r, int[] c, ExactSignPost sp)
	{
		super(weights, r, c, sp);
	}

	/** Einbinden des Konstruktors der Superklasse */
	public AdapterTTinteAlgorithm(BigRational[][] weights, BigInteger[] r, BigInteger[] c, ExactSignPost sp)
	{
		super(weights, r, c, sp);
	}

	/** Gibt die Rundungsmethode zurück */
	public ExactSignPost getSP()
	{
		return this._sp;
	}

	/** Gibt die Reihenmarginalien wieder */
	public BigInteger[] getRowSums()
	{
		return this._r;
	}

	/** Gibt die Spaltenmarginalien wieder */
	public BigInteger[] getColSums()
	{
		return this._c;
	}

	/** Gibt die Gewichtsmatrix zurück */
	public BigRational[][] getWeights()
	{
		return this._weights;
	}
}
