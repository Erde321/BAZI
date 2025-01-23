/*
 * @(#)FinalCheckException.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Überschrift:</b> FinalCheckException<br>
 * <b>Beschreibung:</b> Wird geworfen, wenn es beim check, ob die Divisoren zu den
 * Gewichten passen eine zu große Abweichung gibt.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Robert Bertossi, Christian Brand
 * @version 3.1 */
public class FinalCheckException extends BipropException
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	public FinalCheckException()
	{
		super();
	}

	public FinalCheckException(String p0)
	{
		super(p0);
	}

	public FinalCheckException(Throwable p0)
	{
		super(p0);
	}

	public FinalCheckException(String p0, Throwable p1)
	{
		super(p0, p1);
	}
}
