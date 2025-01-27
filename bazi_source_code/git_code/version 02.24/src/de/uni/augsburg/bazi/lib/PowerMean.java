/*
 * @(#)PowerMean.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse Powermean<br>
 * <b>Description:</b> Sprungstellenfunktionen für die Potenzmittelrundung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class PowerMean extends Signpost
{

	/** Genauigkeit, mit der gearbeitet werden soll */
	public static final double EPSILON = 1.0e-10;

	/** Erstellt eine neue Sprungstellenklasse für die Potenzmittelrundung
	 * @param p Ordnung des Potenzmittels */
	public PowerMean(double p)
	{
		super(p);
	}

	/** Berechnet die Sprungstelle zu einer positiven ganzen Zahl
	 * @param num Zahl, zu der die Sprungstelle berechnet werden soll
	 * @return die Sprungstelle */
	public double s(int num)
	{
		if (num == -1)
		{
			return 0;
		}
		else
		{
			/****************************************** Das Mittel p-ter Ordnung von x und y ist
			 * für p ungleich 0 wie folgt definiert
			 * 
			 * p p
			 * x + y 1/p
			 * ( ------- )
			 * 2
			 * 
			 * für p=0 gilt sqrt(x*y) **************************************************/

			// p=0
			if (Math.abs(param) < EPSILON)
			{
				return Math.sqrt((num + 0.0) * (num + 1.0));
			}

			// p ungleich 0
			else
			{
				return Math.pow(0.5 *
						(Math.pow(num + 0.0, param) + Math.pow(num + 1.0, param)),
						1.0 / param);
			}

		}
	}

	/** Liefert den Name und Parameter dieses Sprungstellenobjekts
	 * @return ein String mit Name und Parameterwert */
	public String getName()
	{
		return "pMean with p=" + param;
	}
}
