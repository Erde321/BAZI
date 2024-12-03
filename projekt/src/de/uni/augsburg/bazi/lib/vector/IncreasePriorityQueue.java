/*
 * @(#)IncreasePriorityQueue.java 1.2 08/02/07
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

/** <b>Title:</b> Klasse IncreasePriorityQueue<br>
 * <b>Description:</b> Klasse zur Inkrementierung bei Vektorproblemen<br>
 * <b>Copyright:</b> Copyright (c) 2007-2008<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 1.2
 * @author Christian Brand */
public class IncreasePriorityQueue extends PriorityQueue
{

	/** Logger, zum speichern aller auftretenden Fehlermeldungen */
	private final Logger logger = Logger.getLogger(IncreasePriorityQueue.class);

	/** Klasse zum Inkrementieren bei Vektorproblemen
	 * @param weights das Gewichtsarray
	 * @param sp die Rundungsmethode
	 * @param libMessenger Messenger, falls ein Fehler auftritt */
	public IncreasePriorityQueue(final Weight[] weights, Signpost sp, LibMessenger libMessenger, boolean useCond)
	{
		super(weights, sp, libMessenger, useCond);
		int numberOfWeights = weights.length;
		// this.controlValues = new double[numberOfWeights];

		/* Zunaechst werden die Werte: Gewicht von Partei i / Wert der Rundungsmethode der Sitze von Partei i berechnet
		 * Dieser ist 0, falls die Partei schon am Maximum angelangt ist, oder der Wert der Rundungsmethode <= 0 ist */
		for (int i = 0; i < numberOfWeights; i++)
		{
			indizes[i] = i;
			double temp = signpost.s(this.weights[i].rdWeight);
			if (temp <= 0d || (useCond && this.weights[i].rdWeight >= this.weights[i].max))
			{
				values[i] = 0d;
			}
			else
			{
				values[i] = this.weights[i].weight / temp;
			}
			/* temp = this.signpost.s(this.weights[i].rdWeight-1);
			 * if(this.weights[i].rdWeight < temp || this.weights[i].rdWeight <= this.weights[i].min){
			 * this.controlValues[i] = Double.POSITIVE_INFINITY;
			 * }
			 * else{
			 * this.controlValues[i] = this.weights[i].weight / temp;
			 * } */
		}


		/* Nun werden die Werte aufsteigend geordnet, d.h. vorne steht das Minimum der Werte.
		 * Also muss immer bei der Partei mit Index von indizes[0] dekrementiert werden */
		Arrays.sort(indizes, strictIncreaseComparator);
	}


	/** Methode, die einer Partei einen Sitz hinzufuegt. Danach wird die Liste neu geordnet
	 * @return Inkrementierung erfolgreich? */
	public boolean increase()
	{
		if (values[indizes[0]] == 0d)
		{
			libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "no candidate for increase found");
			return false;
		}
		/* if(this.indizes.length>1 && this.values[this.indizes[0]] == this.values[this.indizes[1]]){
		 * int pt=1;
		 * for(int i = pt+1; i<this.indizes.length && this.values[this.indizes[0]] == this.values[this.indizes[i]]; pt++);
		 * } */
		weights[indizes[0]].rdWeight++;
		historyOfChanges.add(indizes[0]);
		double temp = signpost.s(weights[indizes[0]].rdWeight);
		if (temp <= 0d || (useCond && weights[indizes[0]].rdWeight == weights[indizes[0]].max))
		{
			values[indizes[0]] = 0d;
		}
		else
		{
			values[indizes[0]] = weights[indizes[0]].weight / temp;
		}

		Arrays.sort(indizes, strictIncreaseComparator);
		return true;
	}

	/** n-Maliges inkrementieren der Zuteilung
	 * @return inkrementieren erfolgreich? */
	public boolean increase(int n)
	{
		if (n <= 0)
		{
			libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "negative number of increases");
			return false;
		}
		for (int i = 0; i < n; i++)
		{
			if (!this.increase())
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
			// if (Math.abs(weights[historyOfChanges.lastElement()].weight / signpost.s(weights[historyOfChanges.lastElement()].rdWeight - 1) - values[indizes[0]]) <=
			// PriorityQueue.epsilon)
			weights[indizes[0]].rdWeight++;
			boolean isEqual = indizes[0] != historyOfChanges.lastElement() && decreaseComparator.compare(historyOfChanges.lastElement(), indizes[0]) == 0;
			weights[indizes[0]].rdWeight--;

			if (isEqual)
				return true;

		}
		catch (NoSuchElementException nseEx)
		{
			libMessenger.setErrorCode(LibMessenger.DIVMETH, 0, "no increase happened");
			logger.error("Es fand keine Inkrementerung statt, daher kann auch nicht ueberprueft werden," +
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
			if (decreaseComparator.compare(historyOfChanges.elementAt(i), historyOfChanges.lastElement()) == 0)
				weights[historyOfChanges.elementAt(i)].multiple = "-";
			else
				break;


		weights[indizes[0]].multiple = "+";
		for (int i = 1; i < indizes.length; i++)
			if (increaseComparator.compare(indizes[0], indizes[i]) == 0)
				weights[indizes[i]].multiple = "+";
			else
				break;

		return true;
	}
}
