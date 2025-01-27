/*
 * Signpost.java 3.2 	25.06.2009
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */
package de.uni.augsburg.bazi.lib;


/** <b>Title:</b> Klasse Signpost<br>
 * <b>Description:</b> Deklaration der für die Divisormethoden notwendigen Berechnungen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.2 */
public abstract class Signpost
{

	/** Parameter der Rundungsmethode */
	protected double param = 0;

	/** Der Konstruktor
	 * @param p Parameter der Rundungsmethode */
	public Signpost(double p)
	{
		param = p;
	}

	/** Setzen des Parameters
	 * @param p Der neue Parameter */
	public void setParam(double p)
	{
		param = p;
	}

	/** Auslesen des Parameters
	 * @return der aktuelle Parameter */
	public double getParam()
	{
		return param;
	}

	/** Liefert die Sprungstelle
	 * @param num Zahl, zu der die Sprungstelle berechnet werden soll
	 * @return Sprungstelle */
	abstract public double s(int num);

	/** runde bezüglich der Sprungstelle
	 * @param num die zu rundende Zahl
	 * @return die gerundete Zahl */
	public int signpostRound(double num)
	{
		// Function signpostRnd(x) sets k = DwnRd(x) and
		// returns IF x <= s(k) THEN k ELSE k+1 ENDIF
		int k = (int) Math.floor(num);
		return (num <= s(k)) ? k : (k + 1);
	}

	/** Liefert den Name (und auch Parameter dieser Signpost-Klasse
	 * @return der Name dieses Signposts */
	abstract public String getName();
}
