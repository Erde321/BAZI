package de.uni.augsburg.bazi.lib;


public class Rounding
{

	public static int getDecimal_places(double d)
	{
		if (!Double.toString(d).contains("."))
			return 0;
		return Double.toString(d).length() - Double.toString(d).indexOf(".") - 1;
	}

	public static double round(double d, int decimal_places)
	{
		if (decimal_places == -1)
			decimal_places = Integer.MAX_VALUE;

		if (getDecimal_places(d) <= decimal_places)
			return d;
		double faktor = Math.pow(10, decimal_places);
		return Math.round(d * faktor) / faktor;
	}
}
