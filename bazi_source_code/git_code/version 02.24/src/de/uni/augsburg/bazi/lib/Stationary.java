/*
 * @(#)Stationary.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse Stationary<br>
 * <b>Description:</b> Sprungstellenmethoden für die stationäre Rundung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class Stationary extends Signpost
{

	/** Erstellt eine neue Sprungstellenklasse für stationäre Rundung
	 * @param p Sprungstelle im Bereich [0;1]
	 * @throws ParameterOutOfRangeException falls der Parameter außerhalb des Gültigkeitsbereichs liegt */
	public Stationary(double p) throws ParameterOutOfRangeException
	{
		super(p);
		if ((p < 0) || (1 < p))
		{
			throw new ParameterOutOfRangeException(
					"Invalid parameter for stationary Method: " + p);
		}
	}

	/** Berechnet die Sprungstelle zu einer positiven ganzen Zahl
	 * @param num Zahl, zu der die Sprungstelle berechnet werden soll
	 * @return die Sprungstelle */
	@Override
	public double s(int num)
	{
		if (num == -1)
		{
			return 0;
		}
		else
		{
			return (num + param);
		}
	}

	/** Liefert den Name und Parameter dieses Sprungstellenobjekts
	 * @return ein String mit Name und Parameterwert */
	@Override
	public String getName()
	{
		return "rstat with r=" + param;
	}

}
