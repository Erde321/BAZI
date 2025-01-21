/*
 * @(#)PureIPFPHelper.java 1.0 09/01/11
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> PureIPFPHelper<br>
 * <b>Beschreibung:</b> Helper Klasse zur Einführung des reinen IPFP Algorithmus<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 1.0
 * @author Christian Brand */
public final class PureIPFPHelper
{
	/* Versionshistorie:
	 * 2009.01-b-01: Version 1.0
	 * - Einführung der Versionshistorie
	 * - Einführen eines reinen IPFP Algorithmus */

	/** Klasse wird als Singleton realisiert */
	private static PureIPFPHelper singleton = null;

	/** Erlaubte Fehlertoleranz */
	private double error = 0.01;

	/** Erreichte Fehlertoleranz */
	private double realError = 0d;

	/** Wird der Algorithmus gerade verwendet */
	private boolean active = false;

	/** Privater Konstruktor ohne Funktionalität */
	private PureIPFPHelper()
	{}

	/** Gibt die singleton Instanz zurück. Falls noch keine Instanz vorhanden, wird sie erstellt */
	public static PureIPFPHelper getPureIPFPHelper()
	{
		if (PureIPFPHelper.singleton == null)
		{
			PureIPFPHelper.singleton = new PureIPFPHelper();
		}
		return PureIPFPHelper.singleton;
	}

	public void setError(double _error)
	{
		this.error = _error;
	}

	public double getError()
	{
		return this.error;
	}

	public void setRealError(double _error)
	{
		this.realError = _error;
	}

	public double getRealError()
	{
		return this.realError;
	}

	public void setActive(boolean _active)
	{
		this.active = _active;
	}

	public boolean isActive()
	{
		return this.active;
	}
}
