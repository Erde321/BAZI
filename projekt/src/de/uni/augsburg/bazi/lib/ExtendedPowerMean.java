package de.uni.augsburg.bazi.lib;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import de.uni.augsburg.bazi.Resource;

public class ExtendedPowerMean extends PowerMean
{

	private final Hashtable<Integer, Double> hash;

	private static Hashtable<Integer, Double> tempHash;

	public ExtendedPowerMean(String s) throws NumberFormatException
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

	private static double extractParam(String s) throws NumberFormatException
	{
		StringTokenizer st = new StringTokenizer(s, ",");
		double param;
		if (st.hasMoreTokens())
		{
			String str_param = st.nextToken().trim();
			param = Double.parseDouble(str_param);
		}
		else
		{
			throw new NumberFormatException("No parameter given!");
		}
		tempHash = new Hashtable<Integer, Double>();

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

		while (st.hasMoreTokens())
		{
			String token = st.nextToken().trim();
			token = token.replace(",", ".");
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
					throw new NumberFormatException("Bad parameter given!");
				}
				if (interval_lowerBound < 0 || interval_lowerBound + 1d != interval_upperBound)
				{
					errorWhileRetrievingHashtable();
					throw new NumberFormatException("Bad parameter given!");
				}
				tempHash.put((int) interval_lowerBound, temp);
			}
			catch (NumberFormatException nfe)
			{
				errorWhileRetrievingHashtable();
				throw new NumberFormatException("Bad parameter given!");
			}
		}
		if (upperBound && lowerBound)
		{
			errorInSignPostSequence();
			return 2d;
		}

		/* int intervalLowerBound = 0;
		 * while(st.hasMoreTokens()){
		 * String token = st.nextToken().trim();
		 * if(!token.equals("-")){
		 * try{
		 * double temp = Double.parseDouble(token);
		 * if(temp < intervalLowerBound){
		 * throw new NumberFormatException("Bad parameter given!");
		 * } else if (temp > intervalLowerBound +1d) {
		 * throw new NumberFormatException("Bad parameter given!");
		 * }
		 * tempHash.put(intervalLowerBound, temp);
		 * } catch (NumberFormatException nfe) {
		 * errorWhileRetrievingHashtable();
		 * throw new NumberFormatException("Bad parameter given!");
		 * }
		 * }
		 * intervalLowerBound++;
		 * } */
		return param;
	}

	private static void errorWhileRetrievingHashtable()
	{
		JOptionPane.showMessageDialog(null, Resource.getString("bazi.gui.tooltip.divpot"),
				Resource.getString("bazi.error.title"), JOptionPane.OK_OPTION);
	}

	private static void errorInSignPostSequence()
	{
		JOptionPane.showMessageDialog(null, Resource.getString("bazi.error.extendedstationary.badsequence"),
				Resource.getString("bazi.error.title"), JOptionPane.OK_OPTION);
	}
}
