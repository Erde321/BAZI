/*
 * @(#)PriorityQueue.java 1.2 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.vector;

import java.util.Comparator;
import java.util.Vector;

import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.Signpost;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse PriorityQueue<br>
 * <b>Description:</b> abstrakte Klasse dient als Oberklasse fuer Increase-
 * und DecreasePriorityQueue<br>
 * <b>Copyright:</b> Copyright (c) 2007-2008<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @author Christian Brand
 * @version 1.2 */
public abstract class PriorityQueue
{

	/** Genauigkeit, bei der 2 Zuteilungen als identisch gelten. */
	public static final double epsilon = 1.0e-12;

	/** Das Weights Array mit dem gearbeitet wird. */
	protected Weight[] weights;

	/** Ein Array, dass sich die Reihenfolge merkt. */
	protected Integer[] indizes;

	/** Ein Array, dass den derzeitgen Wert zum Weights Objekt hat. */
	protected double[] values;

	/** Die zu verwendende Rundungsmethode. */
	protected Signpost signpost;

	/** Der Messenger zur Ausgabe/Speicherung eventueller Fehler. */
	protected LibMessenger libMessenger;

	/** Vektor der sich alle Inkrementierungen bzw. Dekrementierungen merkt */
	protected Vector<Integer> historyOfChanges = new Vector<Integer>();

	/** Ob Rücksicht auf Min/Max genommen werden soll */
	protected boolean useCond;

	/** Einziger Konstruktur zur Erstellung einer PriorityQueue!
	 * @param weights: Die Gewichte ueber die inkrementiert/dekrementiert wird
	 * @param sp: Die Rundungsmethode bei Potenzmittelrundung oder stationaerer Rundung
	 * @param libMessenger: Klasse fuer Fehlermeldungen */
	public PriorityQueue(Weight[] weights, Signpost sp, LibMessenger libMessenger, boolean useCond)
	{
		this.weights = weights;
		this.libMessenger = libMessenger;
		signpost = sp;
		indizes = new Integer[weights.length];
		values = new double[weights.length];
		this.useCond = useCond;
	}

	/** Ueberprueft, ob es sich um eine eindeutige Loesung handelt! */
	public abstract boolean isMultiple();

	/** Gibt den naechsten Wert zurueck. */
	public double getNextValue()
	{
		return values[indizes[0]];
	}

	/** Gibt die naechste Stelle zurueck. */
	public int getNextIndex()
	{
		return indizes[0];
	}

	/** Setzt die Ties entsprechend ob es sich um eine Increase oder
	 * DecreasePriorityQueue handelt
	 * @return true: wenn Ties gesetzt wurden
	 *         false: wenn keine Ties gesetzt werden mussten */
	public boolean setTies()
	{
		/* Zunaechst wird getestet, ob ueberhaupt Ties gesetzt werden muessen. */
		if (!isMultiple())
		{
			return false;
		}
		/* Nun geht man durch die Liste, der zuletzte veraenderten Parteien. Sollte dort noch
		 * der Wert des dortigen Min bzw. Max Divisors mit dem aktuellen Divisor values[indizes[0]] uebereinstimmen,
		 * so muss ein Tie gesetzt werden.
		 * Im Falle, dass inkrementiert wurde, so muss ein "-" gesetzt werden, da auch die aktuelle Partei auf Platz 1
		 * der Liste der zu inkrementierenden Parteien den gleichen Divisor-Wert besitzt.
		 * Im Falle der Dekrementierung muss ein "+" gesetzt werden, da genau so gut, die andere Partei auf Platz 1
		 * der Liste dekrementiert werden koennte. */
		for (int i = historyOfChanges.size() - 1; i >= 0; i--)
		{
			if (Math.abs(values[historyOfChanges.elementAt(i)] - values[historyOfChanges.lastElement()]) > PriorityQueue.epsilon)
			{
				break;
			}
			if (this instanceof IncreasePriorityQueue)
			{
				weights[historyOfChanges.elementAt(i)].multiple = "-";
			}
			else
			{
				weights[historyOfChanges.elementAt(i)].multiple = "+";
			}
		}
		/* Nun werden die Gewichte dupliziert, und es wird der Wert an der Stelle indizes[0] schon mit richtigen Tie
		 * versehen. Dieser Wert bekommt auf jeden Fall ein Tie, da dies ja schon zu Beginn geprueft wurde. */
		Weight[] clone = new Weight[weights.length];
		for (int i = 0; i < clone.length; i++)
		{
			clone[i] = weights[i].clonew();
		}
		if (this instanceof IncreasePriorityQueue)
		{
			weights[indizes[0]].multiple = "+";
		}
		else
		{
			weights[indizes[0]].multiple = "-";
		}
		/* Nun wird hypothetisch weiter inkrementiert bzw. dekrementiert um die naechsten Divisoren zu ueberpruefen
		 * und dann dementsprechend die Ties zu setzen. */
		if (this instanceof IncreasePriorityQueue)
		{
			IncreasePriorityQueue pq = new IncreasePriorityQueue(clone, signpost, libMessenger, useCond);
			while (pq.increase())
			{
				if (Math.abs(pq.getNextValue() - values[indizes[0]]) <= PriorityQueue.epsilon)
				{
					weights[pq.getNextIndex()].multiple = "+";
				}
				else
				{
					break;
				}
			}
		}
		else
		{
			DecreasePriorityQueue pq = new DecreasePriorityQueue(clone, signpost, libMessenger, useCond);
			while (pq.decrease())
			{
				if (Math.abs(pq.getNextValue() - values[indizes[0]]) <= PriorityQueue.epsilon)
				{
					weights[pq.getNextIndex()].multiple = "-";
				}
				else
				{
					break;
				}
			}
		}
		return true;
	}


	protected double getIncreaseValue(int i)
	{
		double temp = signpost.s(weights[i].rdWeight);
		if (temp <= 0d || (useCond && weights[i].rdWeight >= weights[i].max))
			return 0d;
		else
			return weights[i].weight / temp;
	}


	protected final Comparator<Integer> increaseComparator = new Comparator<Integer>()
	{
		public int compare(Integer i1, Integer i2)
		{
			if (Math.abs(getIncreaseValue(i1) - getIncreaseValue(i2)) <= epsilon)
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
			return -Double.compare(getIncreaseValue(i1), getIncreaseValue(i2));
		}
	};

	protected final Comparator<Integer> strictIncreaseComparator = new Comparator<Integer>()
	{
		public int compare(Integer i1, Integer i2)
		{
			int c = increaseComparator.compare(i1, i2);
			if (c != 0)
				return c;
			return -Double.compare(getIncreaseValue(i1), getIncreaseValue(i2));
		}
	};


	protected double getDeceraseValue(int i)
	{
		double temp = signpost.s(weights[i].rdWeight - 1);
		if (temp == 0.0 || weights[i].rdWeight < temp || (useCond && weights[i].rdWeight <= weights[i].min))
			return Double.POSITIVE_INFINITY;
		else
			return weights[i].weight / temp;
	}


	protected final Comparator<Integer> decreaseComparator = new Comparator<Integer>()
	{
		public int compare(Integer i1, Integer i2)
		{
			if (Math.abs(getDeceraseValue(i1) - getDeceraseValue(i2)) <= epsilon)
			{
				boolean max1 = weights[i1].rdWeight > weights[i1].max;
				boolean max2 = weights[i2].rdWeight > weights[i2].max;
				if (max1 && !max2)
					return -1;
				if (!max1 && max2)
					return 1;

				boolean min1 = weights[i1].rdWeight <= weights[i1].min;
				boolean min2 = weights[i2].rdWeight <= weights[i2].min;
				if (min1 && !min2)
					return 1;
				if (!min1 && min2)
					return -1;

				return 0;
			}
			return Double.compare(getDeceraseValue(i1), getDeceraseValue(i2));
		}
	};

	protected final Comparator<Integer> strictDecreaseComparator = new Comparator<Integer>()
	{
		public int compare(Integer i1, Integer i2)
		{
			int c = decreaseComparator.compare(i1, i2);
			if (c != 0)
				return c;
			return Double.compare(getDeceraseValue(i1), getDeceraseValue(i2));
		}
	};
}
