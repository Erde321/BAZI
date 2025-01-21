/*
 * @(#)QuotePriorityQueue.java 1.2 09/09/20
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.vector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse QuotePriorityQueue<br>
 * <b>Description:</b> Klasse zur Zuteilung bei Vektorproblemen mit Quotenmethoden<br>
 * <b>Copyright:</b> Copyright (c) 2007-2009<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Christian Brand, Marco Schumacher
 * @version 1.2 */
public class QuotePriorityQueue extends PriorityQueue
{

	private final Logger logger = Logger.getLogger(QuotePriorityQueue.class);

	/** Kodierung der Quotenmethode, die hier angewandt wird */
	private final double method;

	/** Stelle von der die letzte Stimme zugeteilt wurde. */
	private int pt = -1;

	/** Summe der bereits vergebenen Sitze */
	private int sumOfRoundedWeights = 0;

	private final boolean useCond;

	/** Konstruktur zur Erstellung der Klasse um die (Rest-)Zuteilung der Quotenmethode nach Hare/Niemeyer
	 * oder Droop zu machen.
	 * @param weights: Die Gewichte der Zuteilung
	 * @param quote: Die Quote
	 * @param libMessenger: Klasse zur Fehlerbehandlung
	 * @param method: Die zu verwendene Methode
	 *          <b>Method.HAREQUOTE</b> Hare/Niemeyer
	 *          <b>Method.DROOPQUOTE</b> Droop */
	public QuotePriorityQueue(Weight[] weights, double quote, LibMessenger libMessenger, double method, boolean useCond)
	{
		super(weights, null, libMessenger, false);
		this.method = method;
		this.useCond = useCond;
		/* Zunaechst werden die Werte fuer die erste Zuteilung berechnet und die Reste in values geschrieben */
		for (int i = 0; i < this.weights.length; i++)
		{
			double temp = this.weights[i].weight / quote;
			values[i] = temp - Math.floor(temp);
			this.weights[i].rdWeight = (int) temp;


			/* Die Zuteilung ist schon ueber der Max Bedingung
			 * => Setzen auf die Max Bedingung, und Rest = 0,
			 * damit hier auch keine Stimme mehr dazu kommt! */
			if (useCond && weights[i].rdWeight >= weights[i].max)
			{
				weights[i].rdWeight = weights[i].max;
				values[i] = 0.0;
			}

			indizes[i] = i;
			sumOfRoundedWeights += this.weights[i].rdWeight;
			/* Sollte eine Partei keinen Sitz erhalten, so erhaelt sie bei Droop auch keinen weiteren Sitz. */
			if ((Method.isMethod(method, Method.DROOPQUOTE_VAR01)
					|| Method.isMethod(this.method, Method.DROOPQUOTE_VAR11)
					|| Method.isMethod(this.method, Method.DROOPQUOTE_VAR21)
					|| Method.isMethod(this.method, Method.DROOPQUOTE_VAR31)
					|| Method.isMethod(this.method, Method.HAREQUOTE_VAR01)
					|| Method.isMethod(this.method, Method.HAREQUOTE_VAR11)
					|| Method.isMethod(this.method, Method.HAREQUOTE_VAR21)) && (this.weights[i].rdWeight == 0))
			{
				values[i] = 0.0;
			}
		}

		/* Sortierung der Reste in absteigender Reihenfolge */
		Arrays.sort(indizes, increaseComparator);
	}


	private final Comparator<Integer> increaseComparator = new Comparator<Integer>()
	{
		public int compare(Integer i1, Integer i2)
		{
			if (Math.abs(values[i1] - values[i2]) <= epsilon)
			{
				boolean min1 = weights[i1].rdWeight < weights[i1].min;
				boolean min2 = weights[i2].rdWeight < weights[i2].min;
				if (min1 && !min2)
					return -1;
				if (!min1 && min2)
					return 1;

				boolean max1 = weights[i1].rdWeight >= weights[i1].max;
				boolean max2 = weights[i2].rdWeight >= weights[i2].max;
				if (max1 && !max2)
					return 1;
				if (!max1 && max2)
					return -1;

				return 0;
			}
			return -Double.compare(values[i1], values[i2]);
		}
	};

	/** Methode, die die Restzuteilung auf die Gesamtanzahl der Sitze <b>accuracy</b> macht
	 * Hierbei wird auch ueberprueft, ob eine Min oder Max Bedingung evtl noch erfuellt werden kann.
	 * @param accuracy: Gesamtzahl der Sitze
	 * @return <b>true</b>, wenn Zuteilung erfolgreich <br>
	 *         <b>false</b>, falls Fehler in der Zuteilung */
	public boolean increase(int accuracy)
	{
		/* Dies Beachtung der Min Bedingung geschieht so:
		 * n = accuracy - bereits zugeteilte sitze (d.h. der fehlende Rest)
		 * r = noch fehlende Sitze zur Erfuellung aller Min Bedingungen
		 * Gilt r > n: keine Zuteilung moeglich
		 * sonst: Setze dort wo noetig die Sitze auf die Min Bedingung, und teile dann nach Resten zu. */
		if (logger.isDebugEnabled())
		{
			logger.debug("Increase up to " + accuracy + " seats.");
		}

		boolean variant_WTA = Method.isMethod(method, Method.DROOPQUOTE_VAR02)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR12)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR22)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR32)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR02)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR12)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR22);

		int n = accuracy - sumOfRoundedWeights;
		if (n < 0)
		{
			if (Method.isDroop(method))
			{
				libMessenger.setErrorCode(LibMessenger.DROOP, 0, "");
				if (logger.isDebugEnabled())
				{
					logger.debug("Negative Increase needed within Droop Method.");
				}
				return false;
			}
			if (logger.isDebugEnabled())
			{
				logger.debug("Negative Increase needed within Hare-Quote Method.");
			}
			libMessenger.setErrorCode(LibMessenger.QUOTA, 0, "negative increase needed");
			return false;
		}


		if (useCond)
		{
			int restMinSeats = 0;
			for (int i = 0; i < weights.length; i++)
			{
				if (weights[i].min - weights[i].rdWeight > 0)
				{
					restMinSeats += weights[i].min - weights[i].rdWeight;
				}
			}
			if (restMinSeats > n)
			{
				libMessenger.setErrorCode(LibMessenger.MINIMUM, accuracy, "NA = Not Available: With these minimum requirements no solution for " + accuracy + " seats!");
				return false;
			}
			if (restMinSeats != 0)
			{
				for (int i = 0; i < weights.length; i++)
				{
					if (weights[i].min > weights[i].rdWeight)
					{
						sumOfRoundedWeights += (weights[i].min - weights[i].rdWeight);
						weights[i].rdWeight = weights[i].min;
						/* Partei hat jetzt mindestens einen Sitz dazu bekommen =>
						 * bei evtl. Restzuteilung sollte kein weiter Sitz mehr dazu kommen, also
						 * setze values[aktuelle Stelle]=0 und sortiere dann komplett neu. */
						values[i] = 0.0;
					}
				}

				/* Liste erneut sortieren, da Sitze zugeteilt worden. */
				Arrays.sort(indizes, increaseComparator);
				/* Aktualisierung von n! */
				n = accuracy - sumOfRoundedWeights;
			}
		}

		// Keine Sitze mehr zuzuteilen!
		if (n == 0)
			return true;

		/* Berechnung der Anzahl der Parteien, bei der noch einmal
		 * inkrementiert werden darf. */
		int weightsWithPositiveRemainder = 0;
		for (int i = 0; i < weights.length; i++)
		{
			if (values[indizes[i]] > 0.0)
			{
				weightsWithPositiveRemainder++;
			}
			/* Wenn einmal values[i] == 0, dann sind auch alle Nachfolger = 0 da die Liste sortiert ist! */
			else
			{
				break;
			}
		}
		if (n > weightsWithPositiveRemainder && !variant_WTA)
		{
			libMessenger.setErrorCode(LibMessenger.QUOTA, 0, "NA = Not Available: With these requirements no solution for " + accuracy + " seats!");
			if (logger.isDebugEnabled())
			{
				logger.debug(n + " Seats need to be assigned, but only " + weightsWithPositiveRemainder + " Weights can obtain a seat!");
			}
			return false;
		}

		// Sonderzuteilung bei Variante 1
		// Die stärkste Partei bekommt alles
		if (variant_WTA)
		{

			double strongestList = -1d;
			for (Weight w : weights)
			{
				if (w.weight > strongestList)
				{
					strongestList = w.weight;
				}
			}

			Vector<Weight> parties = new Vector<Weight>();
			for (Weight w : weights)
			{
				if (w.weight == strongestList)
				{
					parties.add(w);
				}
			}

			int prefer = 0;
			for (int i = 0; i < parties.size(); i++)
				if (parties.get(i).rdWeight < parties.get(i).min)
				{
					prefer = i;
					break;
				}

			parties.get(prefer).rdWeight += n;
			if (parties.size() != 1)
			{
				for (int i = 0; i < parties.size(); i++)
				{
					parties.get(i).multiple = i == prefer ? "-" : "+";
				}
			}
			return true;
		}

		for (int i = 0; i < n; i++)
		{
			if (values[indizes[i]] == 0d)
			{
				if (Method.isDroop(method))
				{
					libMessenger.setErrorCode(LibMessenger.DROOP, 0, "");
					return false;
				}
				libMessenger.setErrorCode(LibMessenger.QUOTA, 0, "no candidate for increase found");
				return false;
			}
			weights[indizes[i]].rdWeight++;
		}
		pt = n;
		return true;
	}

	@Override
	public boolean isMultiple()
	{
		if (pt <= 0 || pt >= indizes.length)
		{
			// this.libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "no increase happened");
			/* Es ist keine Zuteilung mit Hilfe der Reste geschehen, d.h. entweder ging die Quote glatt auf,
			 * oder eine Min Bedingung konnte grad so erfuellt werden. => Ties sind nicht moeglich */
			return false;
		}
		weights[indizes[pt - 1]].rdWeight--;
		boolean isEqual = increaseComparator.compare(indizes[pt], indizes[pt - 1]) == 0;
		weights[indizes[pt - 1]].rdWeight++;
		return isEqual;
		// return (Math.abs(values[indizes[pt]] - values[indizes[pt - 1]])) < PriorityQueue.epsilon;
	}

	/** Setzt die Ties bei Zuteilung mit einer Quotenmethode. Ueberprueft auch, ob eine Min Bedingung
	 * noch erfuellt werden kann.
	 * @return true: Ties wurden gesetzt
	 *         false: es gibt keine Ties */
	@Override
	public boolean setTies()
	{
		if (!isMultiple())
		{
			return false;
		}
		weights[indizes[pt]].multiple = "+";
		weights[indizes[pt - 1]].multiple = "-";

		weights[indizes[pt - 1]].rdWeight--;
		for (int i = pt - 2; i >= 0; i--)
		{
			weights[indizes[i]].rdWeight--;
			boolean isEqual = increaseComparator.compare(indizes[i], indizes[pt - 1]) == 0;
			weights[indizes[i]].rdWeight++;
			if (isEqual)
			{
				weights[indizes[i]].multiple = "-";
			}
			else
			{
				break;
			}
		}
		weights[indizes[pt - 1]].rdWeight++;

		for (int i = pt + 1; i < weights.length; i++)
		{
			if (increaseComparator.compare(indizes[i], indizes[pt]) == 0)
			{
				weights[indizes[i]].multiple = "+";
			}
			else
			{
				break;
			}
		}
		return true;
	}
}
