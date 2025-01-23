package de.uni.augsburg.bazi.lib;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import de.uni.augsburg.bazi.Resource;

public class ExtendedStationary extends Stationary
{

	private final Hashtable<Integer, Double> hash;

	private static Hashtable<Integer, Double> tempHash;

	public ExtendedStationary(String s) throws ParameterOutOfRangeException
	{
		super(extractParam(s));
		hash = tempHash;
		tempHash = null;
	}

	@Override
	public String getName()
	{
		String name = "rstat with r=" + param + "\nand some different signposts";
		return name;
	}

	@Override
	public double s(int num)
	{
		Double erg = hash.get(num);
		if (erg == null)
		{
			return super.s(num);
		}
		return erg;
	}

	private static double extractParam(String s)
	{
		StringTokenizer st = new StringTokenizer(s, ",");
		double param;
		if (st.hasMoreTokens())
		{
			String str_param = st.nextToken().trim();
			try
			{
				param = Double.parseDouble(str_param);
			}
			catch (NumberFormatException nfe)
			{
				return 2d;
			}
		}
		else
		{
			// Kein Parameter gegeben, kann auch passieren
			return 2d;
		}

		boolean upperBound = false;
		boolean lowerBound = false;
		if (param == 0d)
		{
			lowerBound = true;
		}
		else if (param == 1d)
		{
			upperBound = true;
		}

		tempHash = new Hashtable<Integer, Double>();

		// int intervalLowerBound = 0;

		while (st.hasMoreTokens())
		{
			String token = st.nextToken().trim();
			boolean left = false;
			boolean right = false;
			if (token.startsWith("["))
			{
				left = true;
				token = token.substring(1);
			}
			else if (token.endsWith("]"))
			{
				right = true;
				token = token.substring(0, token.length() - 1);
			}
			try
			{
				double temp = Double.parseDouble(token);
				double interval_lowerBound = Math.floor(temp);
				double interval_upperBound = Math.ceil(temp);

				if (temp == interval_lowerBound)
				{
					if (left)
					{
						lowerBound = true;
						interval_upperBound += 1d;
					}
					else if (right)
					{
						upperBound = true;
						interval_lowerBound -= 1d;
					}
				}
				else if (left || right)
				{
					errorWhileRetrievingHashtable();
					return 2d;
				}
				if (interval_lowerBound < 0 || interval_lowerBound + 1d != interval_upperBound)
				{
					errorWhileRetrievingHashtable();
					return 2d;
				}
				tempHash.put((int) interval_lowerBound, temp);
			}
			catch (NumberFormatException nfe)
			{
				errorWhileRetrievingHashtable();
				return 2d;
			}
			/* if(!token.equals("-")){
			 * try{
			 * double temp = Double.parseDouble(token);
			 * if(temp < intervalLowerBound){
			 * errorWhileRetrievingHashtable();
			 * return 2d;
			 * } else if (temp == intervalLowerBound){
			 * lowerBound = true;
			 * } else if (temp == intervalLowerBound +1) {
			 * upperBound = true;
			 * } else if (temp > intervalLowerBound +1) {
			 * errorWhileRetrievingHashtable();
			 * return 2d;
			 * }
			 * tempHash.put(intervalLowerBound, temp);
			 * } catch (NumberFormatException nfe) {
			 * errorWhileRetrievingHashtable();
			 * return 2d;
			 * }
			 * }
			 * intervalLowerBound++; */
		}
		if (upperBound && lowerBound)
		{
			errorInSignPostSequence();
			return 2d;
		}
		if (param < 0d || param > 1d)
		{
			errorWhileRetrievingHashtable();
			return 2d;
		}
		return param;
	}

	private static void errorWhileRetrievingHashtable()
	{
		JOptionPane.showMessageDialog(null, Resource.getString("bazi.gui.tooltip.divsta"),
				Resource.getString("bazi.error.title"), JOptionPane.OK_OPTION);
	}

	private static void errorInSignPostSequence()
	{
		// errorWhileRetrievingHashtable();
		JOptionPane.showMessageDialog(null, Resource.getString("bazi.error.extendedstationary.badsequence"),
				Resource.getString("bazi.error.title"), JOptionPane.OK_OPTION);
	}
}
