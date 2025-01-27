/*
 * @(#)DecreasePriorityQueue.java 1.2 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib.vector;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.Signpost;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse DecreasePriorityQueue<br>
 * <b>Description:</b> Klasse zur Dekrementierung bei Vektorproblemen<br>
 * <b>Copyright:</b> Copyright (c) 2007-2008<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 1.2
 * @author Christian Brand */
public class DecreasePriorityQueue extends PriorityQueue
{

	/** Logger, zum speichern aller auftretenden Fehlermeldungen */
	private final Logger logger = Logger.getLogger(PriorityQueue.class);

	/** Klasse zum Dekrementieren bei Vektorproblemen
	 * @param weights das Gewichtsarray
	 * @param sp die Rundungsmethode
	 * @param libMessenger Messenger, falls ein Fehler auftritt */
	public DecreasePriorityQueue(Weight[] weights, Signpost sp, LibMessenger libMessenger, boolean useCond)
	{
		super(weights, sp, libMessenger, useCond);
		int numberOfWeights = weights.length;

		/* Zunaechst werden die Werte "Gewicht von Partei i" / Wert der Rundungsmethode "der bereits erhaltenen Stimmen -1" berechnet,
		 * dieser ist +unendlich, falls das Ergebnis von Partei i bereits an der Min-Grenze ist */
		for (int i = 0; i < numberOfWeights; i++)
		{
			indizes[i] = i;
			double temp = signpost.s(this.weights[i].rdWeight - 1);
			if (temp == 0.0 || this.weights[i].rdWeight < temp || (useCond && this.weights[i].rdWeight <= this.weights[i].min))
			{
				values[i] = Double.POSITIVE_INFINITY;
			}
			else
			{
				values[i] = this.weights[i].weight / temp;
			}
		}


		/* Nun werden die Werte aufsteigend geordnet, d.h. vorne steht das Minimum der Werte.
		 * Also muss immer bei der Partei mit Index von indizes[0] dekrementiert werden */
		Arrays.sort(indizes, strictDecreaseComparator);
	}


	/** Methode, die einer Partei einen Sitz wegnimmt. Danach wird die Liste neu sortiert
	 * @return Dekrementierung erfolgreich? */
	public boolean decrease()
	{
		if (values[indizes[0]] == Double.POSITIVE_INFINITY)
		{
			logger.debug("keine Partei gefunden, bei der noch ein Sitz weggenommen werden kann");
			libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "no candidate for decrease found");
			return false;
		}
		weights[indizes[0]].rdWeight--;
		historyOfChanges.add(indizes[0]);
		double temp = signpost.s(weights[indizes[0]].rdWeight - 1);
		if (temp == 0.0 || weights[indizes[0]].rdWeight < temp || (useCond && weights[indizes[0]].rdWeight <= weights[indizes[0]].min))
		{
			values[indizes[0]] = Double.POSITIVE_INFINITY;
		}
		else
		{
			values[indizes[0]] = weights[indizes[0]].weight / temp;
		}

		Arrays.sort(indizes, strictDecreaseComparator);
		return true;
	}


	/** n-Maliges dekrementieren der Zuteilung
	 * @return dekrementieren erfolgreich? */
	public boolean decrease(int n)
	{
		if (n <= 0)
		{
			libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "negative number of decreases");
			logger.error("Negative Anzahl von Dekrementierungen notwendig");
			return false;
		}
		for (int i = 0; i < n; i++)
		{
			if (!this.decrease())
			{
				return false;
			}
		}
		return true;
	}


	@Override
	public boolean isMultiple()
	{
		try
		{
			// if (Math.abs(weights[historyOfChanges.lastElement()].weight / signpost.s(weights[historyOfChanges.lastElement()].rdWeight) - values[indizes[0]]) <= PriorityQueue.epsilon)
			weights[indizes[0]].rdWeight--;
			boolean isEqual = indizes[0] != historyOfChanges.lastElement() && increaseComparator.compare(historyOfChanges.lastElement(), indizes[0]) == 0;
			weights[indizes[0]].rdWeight++;

			if (isEqual)
				return true;

		}
		catch (NoSuchElementException nseEx)
		{
			libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "no decrease happened");
			logger.error("Es fand keine Dekrementerung statt, daher kann auch nicht ueberprueft werden," +
					"ob es eine identische Verteilung gibt!");
			return false;
		}
		return false;
	}


	@Override
	public boolean setTies()
	{
		if (!isMultiple())
			return false;

		/* Nun geht man durch die Liste, der zuletzte veraenderten Parteien. Sollte dort noch
		 * der Wert des dortigen Min bzw. Max Divisors mit dem aktuellen Divisor values[indizes[0]] uebereinstimmen,
		 * so muss ein Tie gesetzt werden.
		 * Im Falle, dass inkrementiert wurde, so muss ein "-" gesetzt werden, da auch die aktuelle Partei auf Platz 1
		 * der Liste der zu inkrementierenden Parteien den gleichen Divisor-Wert besitzt.
		 * Im Falle der Dekrementierung muss ein "+" gesetzt werden, da genau so gut, die andere Partei auf Platz 1
		 * der Liste dekrementiert werden koennte. */
		for (int i = historyOfChanges.size() - 1; i >= 0; i--)
			if (increaseComparator.compare(historyOfChanges.elementAt(i), historyOfChanges.lastElement()) == 0)
				weights[historyOfChanges.elementAt(i)].multiple = "+";
			else
				break;

		weights[indizes[0]].multiple = "-";
		for (int i = 1; i < indizes.length; i++)
			if (decreaseComparator.compare(indizes[0], indizes[i]) == 0)
				weights[indizes[i]].multiple = "-";
			else
				break;

		return true;
	}
}
