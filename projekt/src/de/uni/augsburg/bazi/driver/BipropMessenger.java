/*
 * @(#)BipropMessenger.java 3.1 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.BipropLibMessenger;

/** <b>Überschrift:</b> Klasse BipropMessenger<br>
 * <b>Beschreibung:</b> Verwaltet und lokalisiert die Nachrichten aus der Biprop Berechnung<br>
 * <b>Copyright:</b> Copyright (c) 2006-2008<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @version 3.1
 * @author Robert Bertossi, Christian Brand */
public class BipropMessenger
{

	/** BipropLibMessenger, der verwaltet werden soll */
	private BipropLibMessenger libMessenger = null;

	/** Erzeugt einen neuen BipropMessenger.
	 * @param libm BipropLibMessenger */
	public BipropMessenger(BipropLibMessenger libm)
	{
		libMessenger = libm;
	}

	/** Ist bei der Berechnung ein Fehler aufgetreten?
	 * @return <b>true</b>, falls ein Fehler gesetzt wurde */
	public boolean isError()
	{
		return libMessenger.isError();
	}

	/** Liefert eine lokalisierte Fehlermeldung oder null, falls es keinen Fehler gab.
	 * @return Fehlermeldung */
	public String getErrorMessage()
	{
		switch (libMessenger.getError())
		{
		case BipropLibMessenger.NONE:
			return null;
		case BipropLibMessenger.COMMON:
			return libMessenger.getDebugMessage();
		case BipropLibMessenger.INPUT_ERROR:
			return libMessenger.getDebugMessage();
		case BipropLibMessenger.USER_ERROR:
			return libMessenger.getDebugMessage();
		case BipropLibMessenger.METHOD:
			return libMessenger.getDebugMessage();
		case BipropLibMessenger.DIVISOR:
			return libMessenger.getDebugMessage();
		case BipropLibMessenger.DIVISOR_DEFECTIVE:
			return libMessenger.getDebugMessage();
		case BipropLibMessenger.DISTRICT_APPORTIONMENT_DEFECTIVE:
			return Resource.getString("bazi.xcp.biprop.district");
		case BipropLibMessenger.PARTY_APPORTIONMENT_DEFECTIVE:
			return Resource.getString("bazi.xcp.biprop.party") +
					"\nj: " + libMessenger.getErrorParams()[0] + " sum: " +
					libMessenger.getErrorParams()[1] +
					" aParties: " + libMessenger.getErrorParams()[3];
		case BipropLibMessenger.EXISTENCE:
			return Resource.getString("bazi.gui.biprop.na") + " " +
					Resource.getString("bazi.gui.biprop.nonexs0");
		}
		return null;
	}
}
