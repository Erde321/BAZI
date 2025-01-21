/*
 * @(#)Method.java 2.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Vector;


/** <b>Title:</b> Klasse Method<br>
 * <b>Description:</b> Algorithmen der Zuteilungsmethoden, Inzwischen gibt es ein Package für diese
 * Art von Problemen: de.uni.augsburg.bazi.lib.Vector<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Jan Petzold, Florian Kluge, Christian Brand
 * @version 2.1
 * @deprecated */

@Deprecated
public class Method
{

	/** Genauigkeit bei Vergleichen */
	static final double EPSILON = 1.0e-10;

	/** Eingabedaten aller Zuteilungs-Teilnehmer. */
	private Weight[] weights;

	/** Speicherung und Formatierung des Divisor. */
	private Divisor divisor;

	/** Verwaltung von Meldungen und Warnungen, die während der Berechnung auftreten. */
	private LibMessenger messenger;

	/** registrierte MethodListeners */
	private final Vector<MethodListener> listeners = new Vector<MethodListener>();

	/** Erzeugt eine Instanz von Method.
	 * 
	 * @param weights Array von Gewichten
	 * @param divisor Divisor
	 * @param messenger LibMessenger */
	public Method(Weight[] weights, Divisor divisor, LibMessenger messenger)
	{
		this.weights = weights;
		this.divisor = divisor;
		this.messenger = messenger;
	}

	/** addMethodListener
	 * 
	 * @param ml MethodListener */
	public void addMethodListener(MethodListener ml)
	{
		listeners.add(ml);
	}

	/** Entfernen eines MethodListeners
	 * 
	 * @param ml MethodListener, der entfernt werden soll
	 * @return <b>true</b>, fall der Listener entfernt werden konnte */
	public boolean removeMethodListener(MethodListener ml)
	{
		return listeners.remove(ml);
	}

	/** Benachrichtigen der Listener über eine neue Nachricht
	 * 
	 * @param msg die Nachricht */
	protected void notifyListeners(String msg)
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			MethodListener ml = listeners.elementAt(i);
			ml.printMessage(msg);
		}
	}

	/** Neuinitialisierung
	 * 
	 * @param weights Array von Gewichten
	 * @param divisor Divisor
	 * @param messenger LibMessenger */
	public void reAssign(Weight[] weights, Divisor divisor,
			LibMessenger messenger)
	{
		this.weights = weights;
		this.divisor = divisor;
		this.messenger = messenger;
	}

	/** Berechnet die Summe aller Gewichte.
	 * 
	 * @param number Anzahl der Gewichte, die zusammengezählt werden sollen (&#60;=
	 *          Gesamtanzahl der Gewichte)
	 * @return Summe der Gewichte */
	private double addUpWeights(int number)
	{

		double sum = 0.0f;

		for (int i = 0; i < number; i++)
		{
			// String temp = Double.toString(weights[i].weight);
			// int newDigits = temp.length() - temp.indexOf(".") - 1;
			// temp = Double.toString(sum);
			// int oldDigits = temp.length() - temp.indexOf(".") - 1;
			// int digits = Math.max(newDigits, oldDigits);
			sum += weights[i].weight;
		}
		return sum;
	}

	/** Berechnet die Summe aller Gewichte.
	 * 
	 * @param number Anzahl der Gewichte, die zusammengezählt werden sollen (&#60;=
	 *          Gesamtanzahl der Gewichte)
	 * @param weights Array von Gewichten
	 * @return Summe der Gewichte */
	private double addUpWeights(int number, Weight[] weights)
	{

		double sum = 0.0f;

		for (int i = 0; i < number; i++)
		{
			/* String temp = Double.toString(weights[i].weight);
			 * int newDigits = temp.length() - temp.indexOf(".") - 1;
			 * temp = Double.toString(sum);
			 * int oldDigits = temp.length() - temp.indexOf(".") - 1;
			 * int digits = Math.max(newDigits, oldDigits); */
			sum += weights[i].weight;
		}
		return sum;
	}

	/** Falls die Summe aller Gewichte 0 ist, sind auch die gerundeten Gewichte 0.
	 * Dies wird hier in das Feld weights geschrieben.
	 * 
	 * @param number Anzahl der Gewichte (&#60;= weights.length)
	 * @param weights Array von Gewichten */
	private void sumIsNull(int number, Weight[] weights)
	{
		for (int i = 0; i < number; i++)
		{
			weights[i].rdWeight = 0;
			weights[i].multiple = "";
		}
	}

	/** Falls die Summe aller Gewichte 0 ist, sind auch die gerundeten Gewichte 0.
	 * Dies wird hier in das Feld weights geschrieben.
	 * (verwendet this.weights)
	 * 
	 * @param number Anzahl der Gewichte (&#60;= weights.length) */
	private void sumIsNull(int number)
	{
		sumIsNull(number, weights);
	}

	/** Quotenmethode mit der Zuteilung nach den größten Resten.
	 * 
	 * @param accuracy Anzahl der zu vergebenden Mandate
	 * @param m <br>
	 *          m=0 Quotenmethode nach Hare/Niemeyer Quote = V / M <br>
	 *          m=1
	 *          Quotenmethode nach Droop Quote = ceil( V / (M+1) )
	 * @return <b>true</b>, falls ein Ergebnis gefunden wurde */
	public boolean quota(int accuracy, int m)
	{
		int defect;
		int i, j, index = 0;
		double temp, maxRemainder, lastRemainder = 0.0;
		boolean flagMultiple = false;
		int sumOfRoundedWeights = 0;

		int numberOfWeights = weights.length;

		double sumOfWeights = addUpWeights(numberOfWeights);

		// falls sumOfWeights gleich 0 ist, rufe sumIsNull() auf und verlasse diese Methode
		if (sumOfWeights == 0.0)
		{
			sumIsNull(numberOfWeights);
			divisor.setEnabled(false);
			divisor.setQuote(Double.POSITIVE_INFINITY);
			return true;
		}

		if (accuracy == 0)
		{
			sumIsNull(numberOfWeights);
			divisor.setEnabled(false);
			divisor.setQuote(Double.POSITIVE_INFINITY);
			return true;
		}

		// hat eine Partei die absolute Mehrheit
		double abs = sumOfWeights / 2.0;
		for (i = 0; i < numberOfWeights; i++)
		{
			if (weights[i].weight > abs)
			{
				messenger.setAbsolute(weights[i].name, weights[i].weight, sumOfWeights);
			}
		}

		// Quote berechnen
		double quota;
		if (accuracy + m > 0)
		{
			quota = sumOfWeights / ((double) accuracy + (double) m);
		}
		else
		{
			// Hare-Quote mit 0 Mandaten aufgerufen
			quota = Double.POSITIVE_INFINITY;
			messenger.setZeroHare(true);
			divisor.setQuote(quota);
			System.err.println("Fehler in Hare, Aufruf mit 0 Mandaten");
			for (i = 0; i < numberOfWeights; i++)
			{
				weights[i].rdWeight = 0;
				weights[i].multiple = "";
			}
			// messenger.minimumMessage = true;
			// messenger.noSolutionForMinimum = true;
			// messenger.addMinimumAccuracy(accuracy);
			// messenger.addRemainAccuray(accuracy);
			return false;
		}
		// bei Droop ist der Quotient eine ganze Zahl
		if (m == 1)
		{
			quota = Math.ceil(quota);
			// if (m==1) System.out.println("Droop-Quote: " + quota);

		}
		// int countOfParties = 0;

		// initialisieren von rank mit -1
		// berechnen der gerundeten Gewichte, der Reste und der Summe der gerundeten Gewichte
		for (i = 0; i < numberOfWeights; i++)
		{
			weights[i].rank = -1;
			// m_i = floor(v_i/Q)
			temp = weights[i].weight / quota;
			weights[i].rdWeight = (int) Math.floor(temp);
			// if (weights[i].rdWeight > 0) {
			// countOfParties++;
			//
			// // bei Droop bekommen, die Verlierer der Hauptverteilung, d.h. rdWeight==0,
			// // auch in der Restzuteilung nichts, d.h. remainder=0 setzen
			// }
			if (m == 1 && weights[i].rdWeight == 0)
			{
				weights[i].remainder = 0.0;
				// sonst: bereite Reste v_i/Q - m_i vor
			}
			else
			{
				weights[i].remainder = temp - Math.floor(temp);

			}
			sumOfRoundedWeights += weights[i].rdWeight;
		}

		// berechnen des Defekts
		defect = accuracy - sumOfRoundedWeights;

		int pcount = 0;
		for (i = 0; i < numberOfWeights; i++)
		{
			if ((weights[i].rdWeight > 0) &&
					(weights[i].weight > weights[i].rdWeight * quota))
			{
				pcount++;
			}
		}
		if ((m == 1) && (defect > pcount))
		{ // (defect > countOfParties)) {
			// PROMPT "NA = Nicht anwendbar: In der Restzuteilung gibt es mehr Sitze
			// als Parteien, die gemaess Hauptzuteilung repraesentiert sind";
			// quota = Double.POSITIVE_INFINITY;
			/* messenger.setDroopRemainder(true);
			 * messenger.setDroopMessage(true); */
			messenger.setErrorCode(LibMessenger.DROOP, 0, "");
			divisor.setEnabled(false);
			divisor.setQuote(quota);
			// System.out.println("\tQuote bei Droop-Fehler: " + divisor.getQuote());
			for (i = 0; i < numberOfWeights; i++)
			{
				weights[i].rdWeight = 0;
				weights[i].multiple = "";
			}
			return true;

		}

		/* 22.02.2002 by Flo
		 * Aus Droop Algorithmus:
		 * IF sum_{i=1}^l{m_i} < M-l THEN Abbruch
		 * mit l = numberOfWeights
		 * m_i = weights[i].rdweight
		 * M = accuracy */
		if (numberOfWeights < defect)
		{
			// noch Nachricht an Messenger, daß statt "0" nun "N/A" angezeigt wird!
			/* messenger.setMinimumMessage(true);
			 * messenger.setNoSolutionForMinimum(true);
			 * messenger.addMinimumAccuracy(accuracy);
			 * messenger.addRemainAccuray(accuracy); */
			messenger.setErrorCode(LibMessenger.MINIMUM, accuracy,
					"Fehler in quota, numberOfWeights < defect");
			// System.err.println("Fehler in quota, numberOfWeights < defect");
			return false;
		}

		// berechnen der Differenz von Minimum und gerundeten Gewicht
		int difference = 0;
		for (i = 0; i < numberOfWeights; i++)
		{
			if (weights[i].min > weights[i].rdWeight)
			{
				difference += weights[i].min - weights[i].rdWeight;
			}
		}

		// Gerundete Gewichte auf das Minimum setzen
		if (difference > 0 && defect < difference)
		{
			/* messenger.setNoMinimumConditionForRemain(true);
			 * messenger.setRemainMessage(true);
			 * messenger.addRemainAccuray(accuracy); */
			messenger.setErrorCode(LibMessenger.REMAIN, accuracy,
					"Fehler in quota, difference>0 && defect < difference");
			// System.err.println("Fehler in quota, difference>0 && defect < difference");
			return false;
		}
		else
		{
			for (i = 0; i < numberOfWeights; i++)
			{
				if (weights[i].min > weights[i].rdWeight)
				{
					weights[i].rdWeight = weights[i].min;
					weights[i].remainder = 0.0;
				}
			}
			defect -= difference;
		}

		// berechnen von rank und aktualisieren der gerundeten Gewichte
		if (defect > 0)
		{
			for (i = 0; i < numberOfWeights; i++)
			{
				maxRemainder = -EPSILON;
				for (j = 0; j < numberOfWeights; j++)
				{
					if (weights[j].rank == -1 && weights[j].remainder > maxRemainder)
					{
						maxRemainder = weights[j].remainder;
						index = j;
					}
				}
				weights[index].rank = i;
				if (i == defect - 1)
				{
					lastRemainder = maxRemainder;
				}
			}
			for (i = 0; i < numberOfWeights; i++)
			{
				if (weights[i].rank < defect)
				{
					weights[i].rdWeight++;
				}
				if (weights[i].rank == defect &&
						Math.abs(weights[i].remainder - lastRemainder) < EPSILON)
				{
					flagMultiple = true;
				}
			}
		}
		// falls die Summe der gerundeten Gewichte = accuracy + 1
		// nimm dem kleinsten gerundeten Gewicht einen Sitz weg
		else if (defect < 0)
		{
			int minRdWeight = Integer.MAX_VALUE;
			int minIndex = 0;
			for (i = 0; i < numberOfWeights; i++)
			{
				if (weights[i].rdWeight <= minRdWeight)
				{
					minRdWeight = weights[i].rdWeight;
					minIndex = i;
				}
				weights[i].rank = -2;
			}

			weights[minIndex].rdWeight--;
			weights[minIndex].rank = 0;
			flagMultiple = true;
			lastRemainder = 0.0;
		}

		// mehrfach oder eindeutige Lösung
		for (i = 0; i < numberOfWeights; i++)
		{
			// eindeutige Lösung
			if (defect == 0 || !flagMultiple)
			{
				weights[i].multiple = "";
				// mehrfach Lösung
			}
			else
			{
				if (Math.abs(weights[i].remainder - lastRemainder) < EPSILON)
				{
					if (weights[i].rank < (accuracy - sumOfRoundedWeights))
					{
						weights[i].multiple = "-";
					}
					else
					{
						weights[i].multiple = "+";
					}
				}
				else
				{
					weights[i].multiple = "";
				}
			}
		}

		// Quotenmethode ist keine Divisormethode
		divisor.setEnabled(false);
		divisor.setQuote(quota);

		return true;
	} // end quota

	public boolean divMethVector2(Signpost sp, Divisor divisor, int accuracy,
			Weight[] weights, LibMessenger lm) throws
			MethodException
	{
		for (int i = 0; i < weights.length; i++)
		{
			weights[i].multiple = "";
		}
		return true; // Method2.divMethVector(sp, divisor, accuracy, weights, lm);
	}
	/** Divisormethode
	 * 
	 * @param sp Die zu verwendende Rundungsmethode (stationär oder Potenzmittel)
	 * @param divisor die zu verwendende Divisorklasse
	 * @param accuracy die zu vergebende Mandatszahl
	 * @param weights die zu rundenden Gewichte
	 * @param lm LibMessenger
	 * @return <b>true</b> wenn alles glatt ging
	 * @throws MethodException Fehler bei Eingabedaten, oder bei Berechnung */
	public boolean divMethVector(Signpost sp, Divisor divisor, int accuracy,
			Weight[] weights, LibMessenger lm) throws
			MethodException
	{
		double ubm; // unbiased multiplier
		double temp;
		// int index;
		int sumOfRoundedWeights = 0;
		double min, max;

		String debtmp = "";

		// weights = wi;

		int numberOfWeights = weights.length;
		int numberOfPositiveWeights = 0;
		int pt = -1;
		divisor.setEnabled(true);

		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < numberOfWeights; i++)
		{
			/* if (weights[i].weight<min) {
			 * min = weights[i].weight;
			 * } */
			min = Math.min(min, weights[i].weight);
			/* if (max<weights[i].weight) {
			 * max = weights[i].weight;
			 * } */
			// System.out.println("Weight " + i + ": " + weights[i].weight);
			max = Math.max(max, weights[i].weight);
			if (weights[i].weight > 0)
			{
				numberOfPositiveWeights++;
			}
		}
		// keine negativen Gewichte, und auch nicht alle == 0
		if (min < 0)
		{
			// später 'ne Exception!
			// throw new MethodException(MethodException.INPUT_DEFECTIVE, sp.getName(), "Negative Weight: " + min);
			// lm.setErrorCode(LibMessenger.INPUT_DEFECTIVE);
			// lm.setErrorMessage("Negative Weight: " + min);
			lm.setErrorCode(LibMessenger.NEGATIVE_WEIGHTS, 0, "Negative weight: " + min);
			// throw new MethodException("Negative weight: " + min);
			return false;
		}

		// Minimumsbedingung aktiv?
		boolean minCond = false;
		for (int i = 0; i < weights.length; i++)
		{
			minCond |= weights[i].min > 0;
		}
		// ## If "Min" selected,...
		if (minCond)
		{
			boolean bna = false;

			if (sp.s(0) > 0)
			{
				int sum = 0;
				for (int i = 0; i < weights.length; i++)
				{
					if (weights[i].weight > 0)
					{
						sum += weights[i].min;
					}
				}
				if (accuracy < sum)
				{
					bna = true;
				}
			}
			else
			{ // sp.s(0) == 0
				int sum = 0;
				for (int i = 0; i < weights.length; i++)
				{
					if (weights[i].weight > 0)
					{
						sum += Math.max(weights[i].min, 1);
					}
				}
				if (accuracy < sum)
				{
					bna = true;
				}
			}

			if (bna)
			{
				// Rückkehr mit Fehler!
				for (int i = 0; i < weights.length; i++)
				{
					weights[i].rdWeight = -1;
				}
				lm.setErrorCode(LibMessenger.MINIMUM, accuracy,
						"NA = Not Available: With these minimum requirements no solution for " +
								accuracy + " seats!");
				/* lm.setNoSolutionForMinimum(true);
				 * lm.setMinimumMessage(true);
				 * lm.addMinimumAccuracy(accuracy);
				 * lm.setErrorMessage("NA = Not Available: With these minimum requirements no solution for " + accuracy + " seats!"); */
				return false;
				// throw new MethodException("NA = Not Available: With these minimum requirements no solution for " + accuracy + " seats!");
			}
		}

		double sumOfWeights = addUpWeights(numberOfWeights, weights);
		if (Debug.DIVMETH)
		{
			notifyListeners("sumOfWeights: " + sumOfWeights);
		}

		// falls die Summe der Gewicht 0 ist, aufrufen der Methode sumIsNull() und verlassen dieser Methode
		// System.out.println("sumOfWeights: " + sumOfWeights);
		if ((sumOfWeights == 0.0) && (accuracy == 0))
		{
			// System.out.println("Sumnull");
			sumIsNull(numberOfWeights, weights);
			try
			{
				divisor.setDivisorInterval(Double.POSITIVE_INFINITY,
						Double.POSITIVE_INFINITY);
				divisor.setMultiplierInterval(0, 0);
			}
			catch (DivisorException de)
			{
				System.out.println(de);
				/* lm.setErrorCode(LibMessenger.DIVISOR);
				 * lm.setErrorMessage(
				 * "Error while setting divisor to (infty, infty) using " + sp.getName()); */
				// throw new MethodException(sp.getName(), de);
				lm.setErrorCode(LibMessenger.DIVISOR, 0,
						"Error while setting divisor to (infty, infty) using " + sp.getName());
				/* throw new MethodException(
				 * "Error while setting divisor to (infty, infty) using " + sp.getName(),
				 * de); */
				return false;
			}
			return true;
		}

		if (max == 0)
		{
			// später 'ne Exception
			// throw new MethodException(MethodException.INPUT_DEFECTIVE, sp.getName(), "sumOfWeights == 0, M>0");
			// lm.setErrorCode(LibMessenger.INPUT_DEFECTIVE);
			// lm.setErrorMessage("sumOfWeights == 0, M>0");
			lm.setErrorCode(LibMessenger.SUM_OF_WS_ZERO, 0, "sumOfWeights == 0, M>0");
			return false;
			// throw new MethodException("sumOfWeights == 0, M>0");
		}

		// erste Initialisierung der Gewichte
		for (int i = 0; i < numberOfWeights; i++)
		{
			weights[i].multiple = "";
			if (weights[i].weight == 0)
			{
				weights[i].rdWeight = 0;
			}
		}

		// irregulärer Fall: keine Sitze
		if (accuracy == 0)
		{
			for (int i = 0; i < numberOfWeights; i++)
			{
				weights[i].rdWeight = 0;
			}
			try
			{
				divisor.setDivisorInterval(Double.POSITIVE_INFINITY,
						Double.POSITIVE_INFINITY);
				divisor.setMultiplierInterval(0, 0);
			}
			catch (DivisorException de)
			{
				System.out.println(de);
				// lm.setErrorCode(LibMessenger.DIVISOR);
				// lm.setErrorMessage("Error while setting divisor to (infty, infty) using " + sp.getName());
				// throw new MethodException(sp.getName(), de);
				lm.setErrorCode(LibMessenger.DIVISOR, 0,
						"Error while setting divisor to (infty, infty) using " + sp.getName());
				return false;
				/* throw new MethodException(
				 * "Error while setting divisor to (infty, infty) using " + sp.getName()); */
			}
			return true;
		}

		// ### Irregular cases: s(0)=0 and fewer seats than there are positive Weights
		// irregulärer Fall: weniger Sitze als Parteien
		// if ((accuracy < numberOfWeights) && (sp.s(0)==0)) {
		if ((accuracy < numberOfPositiveWeights) && (sp.s(0) == 0))
		{
			// sortiere Gewichte absteigend
			double[] tempWeights = new double[numberOfWeights];
			for (int k = 0; k < numberOfWeights; k++)
			{
				tempWeights[k] = weights[k].weight;
			}
			int[] sortIndex = Sort.decreaseSort(tempWeights);
			// if (weights[sortIndex[accuracy]].weight > 0) {
			for (int j = 0; j < accuracy; j++)
			{
				weights[sortIndex[j]].rdWeight = 1;
				// 28.10.2004
				// if ((weights[sortIndex[j]].weight == weights[accuracy-1].weight) && (weights[accuracy-1].weight == weights[accuracy].weight)) {
				if (weights[sortIndex[j]].weight == weights[sortIndex[accuracy]].weight)
				{
					weights[sortIndex[j]].multiple = "-";
				}
			}
			for (int j = accuracy; j < numberOfWeights; j++)
			{
				weights[sortIndex[j]].rdWeight = 0;
				if (weights[sortIndex[j]].weight == weights[sortIndex[accuracy - 1]].weight)
				{
					weights[sortIndex[j]].multiple = "+";
				}
			}
			try
			{
				// System.out.println("irregular setting divisor, old d: " + divisor.getDivisor());
				divisor.setDivisorInterval(0, 0);
				divisor.setMultiplierInterval(Double.POSITIVE_INFINITY,
						Double.POSITIVE_INFINITY);
				// System.out.println("dmin: " + divisor.getDivisorLow() + " dmax: " + divisor.getDivisorHigh() + " d: " + divisor.getDivisor());
			}
			catch (DivisorException de)
			{
				System.out.println(de);
				// lm.setErrorCode(LibMessenger.DIVISOR);
				// lm.setErrorMessage("Error while setting divisor to (infty, infty) using " + sp.getName());
				// throw new MethodException(sp.getName(), de);
				lm.setErrorCode(LibMessenger.DIVISOR, 0,
						"Error while setting divisor to (infty, infty) using " + sp.getName());
				return false;
				/* throw new MethodException(
				 * "Error while setting divisor to (infty, infty) using " + sp.getName(),
				 * de); */
			}
			return true;
			// }
		}

		// hat eine Partei die absolute Mehrheit
		double abs = sumOfWeights / 2.0;
		for (int i = 0; i < numberOfWeights; i++)
		{
			if (weights[i].weight > abs)
			{
				messenger.setAbsolute(weights[i].name, weights[i].weight, sumOfWeights);
			}
		}

		if (Debug.DIVMETH)
		{
			debtmp += "Gewichte:\n";
			for (int i = 0; i < weights.length; i++)
			{
				debtmp += weights[i].weight + " ";
			}
			debtmp += "\nmin-cond:\n";
			for (int i = 0; i < weights.length; i++)
			{
				debtmp += weights[i].min + " ";
			}
			debtmp += "\n";
		}

		// berechnen des unbaised multiplier

		// Versuche zuerst D = (sum_i w_i)/ubm als Divisor,
		// um möglichst nah ans Ziel zu gelangen
		ubm = accuracy; // Standardmultiplikator
		// Stationäre Methoden erlauben noch eine Verbesserung
		if ((accuracy > numberOfWeights / 2) && (sp instanceof Stationary))
		{
			ubm += (sp.getParam() - 0.5) / numberOfWeights;
		}
		debtmp += "Ubm: " + ubm + " acc: " + accuracy + "\n";
		for (int i = 0; i < numberOfWeights; i++)
		{
			temp = ubm * weights[i].weight / sumOfWeights;
			weights[i].rdWeight = Math.max(weights[i].min, sp.signpostRound(temp));

			// System.out.println("i: " + i + " m_i: " + weights[i].rdWeight + " temp: " + temp);

			// setzen der gerundeten Gewichte auf das Minimum bzw. Maximum
			if (weights[i].rdWeight < weights[i].min)
			{
				weights[i].rdWeight = weights[i].min;
			}
			else if (weights[i].rdWeight > weights[i].max && weights[i].max != 0)
			{
				weights[i].rdWeight = weights[i].max;
			}

			sumOfRoundedWeights += weights[i].rdWeight;
		}

		if (Debug.DIVMETH)
		{
			debtmp += "Zuteilung vor Korrektur:\n";
			for (int i = 0; i < weights.length; i++)
			{
				debtmp += weights[i].rdWeight + " ";
			}
			debtmp += "\n";
		}
		// Berechnung mit sortierten Gewichten
		// Feld mit Gewichten aufsteigend sortieren
		/* double [] tempWeights = new double[numberOfWeights];
		 * for (int k=0; k<numberOfWeights; k++) tempWeights[k] = weights[k].weight;
		 * int [] sortIndex = QuickSort.decreaseSort(tempWeights); */

		int j = 0;

		// Inkrementierung nötig
		while ((j < numberOfWeights) && (sumOfRoundedWeights < accuracy))
		{
			double dmin = 0;
			temp = 0;
			for (int k = 0; k < numberOfWeights; k++)
			{
				double s = sp.s(weights[k].rdWeight);
				if (s > 0)
				{
					temp = weights[k].weight / s;
					// if (temp>dmin) dmin = temp;
				}
				dmin = Math.max(dmin, temp);
			}
			if (Debug.DIVMETH)
			{
				debtmp += "dmin: " + dmin + "\n";
			}

			// while ( (sp.s(weights[j].rdWeight) > 0) &&
			while ((j < weights.length) &&
					((weights[j].weight == 0)
					|| (weights[j].weight / sp.s(weights[j].rdWeight) < dmin)))
			{
				j++;
			}
			if (j == weights.length)
			{
				lm.setErrorCode(LibMessenger.DIVMETH, 0, "No candidate for increase found");
				// throw new MethodException("No candidate for increase found");
				return false;
			}
			weights[j].rdWeight++;
			sumOfRoundedWeights++;
			if (Debug.DIVMETH)
			{
				debtmp += "increment: " + j + "\n";
			}
			// double t1=0, t2=Double.MAX_VALUE;
			max = 0;
			min = Double.MAX_VALUE;
			// max{w_j/s(m_j):s(m_j)>0}
			for (int k = 0; k < numberOfWeights; k++)
			{
				double s = sp.s(weights[k].rdWeight);
				if (s > 0)
				{
					temp = weights[k].weight / s;
					// if (temp>t1) t1 = temp;
					max = Math.max(max, temp);
				}
			}
			// min{w_j/s(m_j-1):s(m_j-1)>s(b_j-1)}
			for (int k = 0; k < numberOfWeights; k++)
			{
				double s = sp.s(weights[k].rdWeight - 1);
				if (s > sp.s(weights[k].min - 1))
				{
					temp = weights[k].weight / s;
					// if (temp>t2) t2 = temp;
					min = Math.min(min, temp);
				}
			}
			if (Debug.DIVMETH)
			{
				debtmp += "max: " + max + " min: " + min + "\n";
			}
			// max>min?
			if (max > min)
			{
				weights[j].rdWeight--;
				sumOfRoundedWeights--;
				if (Debug.DIVMETH)
				{
					debtmp += "undo; mj: " + weights[j].rdWeight + " j: " + j + " Sum: " +
							sumOfRoundedWeights + "\n";
				}
				j++;
			}
			else
			{
				j = 0;
			}
		}

		// Dekrementierung nötig
		j = 0;
		while ((j < numberOfWeights) && (sumOfRoundedWeights > accuracy))
		{
			double dmax = Double.MAX_VALUE;
			temp = Double.MAX_VALUE;
			for (int k = 0; k < numberOfWeights; k++)
			{
				double s = sp.s(weights[k].rdWeight - 1);
				if (s > sp.s(weights[k].min))
				{
					temp = weights[k].weight / s;
					// if (temp<dmax) dmax = temp;
				}
				dmax = Math.min(dmax, temp);
			}
			if (Debug.DIVMETH)
			{
				debtmp += "dmax: " + dmax + "\n";
			}

			while ((j < weights.length) &&
					((weights[j].weight / sp.s(weights[j].rdWeight - 1) > dmax)
					|| (sp.s(weights[j].rdWeight - 1) == sp.s(weights[j].min - 1))))
			{
				j++;
				// System.out.println("j: " + j);
			}
			if (j == weights.length)
			{
				lm.setErrorCode(LibMessenger.DIVMETH, 0, "No candidate for decrease found");
				// throw new MethodException("No candidate for decrease found");
				return false;
			}
			weights[j].rdWeight--;
			sumOfRoundedWeights--;
			if (Debug.DIVMETH)
			{
				debtmp += "decrement: " + j + "\n";
			}
			// double t1=0, t2=Double.MAX_VALUE;
			max = 0;
			min = Double.MAX_VALUE;
			// max{w_j/s(m_j):s(m_j)>0}
			for (int k = 0; k < numberOfWeights; k++)
			{
				double s = sp.s(weights[k].rdWeight);
				if (s > 0)
				{
					temp = weights[k].weight / s;
					// if (temp>t1) t1 = temp;
					max = Math.max(max, temp);
				}
			}
			// min{w_j/s(m_j-1):s(m_j-1)>s(b_j-1)}
			for (int k = 0; k < numberOfWeights; k++)
			{
				double s = sp.s(weights[k].rdWeight - 1);
				if (s > sp.s(weights[k].min - 1))
				{
					temp = weights[k].weight / s;
					// if (temp<t2) t2 = temp;
					min = Math.min(min, temp);
				}
			}
			if (Debug.DIVMETH)
			{
				debtmp += "max: " + max + " min: " + min + "\n";
			}
			if (max > min)
			{
				weights[j].rdWeight++;
				sumOfRoundedWeights++;
				if (Debug.DIVMETH)
				{
					debtmp += "undo; mj: " + weights[j].rdWeight + " j: " + j + "S um: " +
							sumOfRoundedWeights + "\n";
				}
				j++;
			}
			else
			{
				j = 0;
			}
		}

		if (Debug.DIVMETH)
		{
			debtmp += "Zuteilung nach Korrektur:\n";
			for (int i = 0; i < weights.length; i++)
			{
				debtmp += weights[i].rdWeight + " ";
			}
			debtmp += "\n";
		}

		// Berechnung des Divisor aus der Lösung
		/************************************************** Der Divisor ist ein Wert aus dem Intervall
		 * 
		 * v[i] v[i]
		 * [ max { -------- | für alle i } , min { ------------ | für alle i } ]
		 * m[i] + q m[i] - 1 + q
		 * 
		 * (v : weight, m : rdWeight) **************************************************/
		divisor.setEnabled(true);
		double low, high, s;
		min = Double.POSITIVE_INFINITY; // obere Grenze des Intervalls
		max = Double.NEGATIVE_INFINITY; // untere Grenze des Intervalls
		for (int i = 0; i < numberOfWeights; i++)
		{
			// if (weights[i].rdWeight != weights[i].max || weights[i].max==0){
			s = sp.s(weights[i].rdWeight);
			if (s > 0)
			{
				temp = weights[i].weight / s;
				// if (temp > max) max = temp;
				max = Math.max(max, temp);
			}
			// }
			if (weights[i].rdWeight != weights[i].min)
			{
				/* s = sp.s(weights[i].rdWeight-1);
				 * //if (s>sp.s(weights[i].min)) {
				 * if (s>0) {
				 * temp = (double) weights[i].weight / s;
				 * if (temp < min && temp != 0)
				 * min = temp;
				 * } */
				s = sp.s(weights[i].rdWeight - 1);
				// temp = sp.s(weights[i].rdWeight-1);
				/* if (temp <= sp.s(weights[i].min)) temp = Double.POSITIVE_INFINITY;
				 * else {
				 * temp = (double) weights[i].weight / temp;
				 * if (temp < min && temp != 0)
				 * min = temp;
				 * } */
				if (s > sp.s(weights[i].min - 1))
				{
					temp = weights[i].weight / s;
					min = Math.min(min, temp);
				}
			}
		}
		low = max;
		high = min;

		if (Debug.DIVMETH)
		{
			debtmp += "D_low: " + max + "\nD_high: " + min + "\n";
			notifyListeners(debtmp);
		}
		try
		{
			divisor.setDivisorInterval(low, high);
		}
		catch (DivisorException de)
		{
			System.out.println(de);
			lm.setErrorCode(LibMessenger.DIVISOR, 0,
					"Error while setting divisor to (" + low + ", " + high + ") using " + sp.getName());
			return false;
			/* throw new MethodException("Error while setting divisor to (" + low + ", " +
			 * high + ") using " + sp.getName(), de); */
		}

		// Multiplikator ermitteln, so dass gilt:
		/************************************************** v[i]
		 * round ( ---- * Multiplier ) = m[i]
		 * V
		 * 
		 * (v : weight, m : rdWeight, V: sum of all weight) **************************************************/
		// ab 28.10.2004: Keine Standardisierung mehr, um Verwirrung mit Biprop zu vermeiden!
		// Multiplikatorintervall ergibt sich aus dem Divisorintervall
		// mit vertauschten Grenzen
		/* low = (1 / min) * sumOfWeights;
		 * high = (1 / max) * sumOfWeights; */
		low = 1 / min;
		high = 1 / max;
		// System.out.println("lc: " + low + " hc: " + high);
		try
		{
			divisor.setMultiplierInterval(low, high);
		}
		catch (DivisorException de)
		{
			System.out.println(de);
			lm.setErrorCode(LibMessenger.DIVISOR, 0,
					"Error while setting multiplier to (" + low + ", " + high + ") using " + sp.getName());
			return false;
			// throw new MethodException(sp.getName(), de);
			/* throw new MethodException("Error while setting multiplier to (" + low +
			 * ", " + high + ") using " + sp.getName(), de); */
		}

		double dmin, dmax, d;
		dmin = divisor.getDivisorLow();
		dmax = divisor.getDivisorHigh();
		d = divisor.getDivisor();
		double mumin, mumax;
		mumin = divisor.getMultiplierLow();
		mumax = divisor.getMultiplierHigh();
		// mu = divisor.getMultiplier();

		/* if (max < min) {
		 * for (i = 0; i < numberOfWeights; i++) {
		 * weights[i].multiple = "";
		 * }
		 * } */


		// # If tied, set trailing flags to indicate multiplicities:

		// Achtung: hier werden die originale (ungerundeten) Grenzen geprüft!
		if (dmin > dmax)
		{
			lm.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0, "dmin > dmax using " + sp.getName());
			return false;
			// throw new MethodException(MethodException.DIVISOR_DEFECTIVE, sp.getName(), "dmin > dmax");
			// throw new MethodException("dmin > dmax using " + sp.getName());
		}
		if (Math.round(((dmax / dmin) - 1) * Math.pow(10, 15)) > 0)
		{
			// pickdiv normal
			// Werte wurden bereits berechnet!!!
		}
		else
		{
			/* int jd = -1;
			 * for (int i=0; i<numberOfWeights-1; i++) {
			 * for (j=i+1; j<numberOfWeights; j++) {
			 * if (((sp.s(weights[i].rdWeight)*sp.s(weights[j].rdWeight-1)) > 0)
			 * && (Math.round(((weights[i].weight*sp.s(weights[j].rdWeight-1))
			 * - (weights[j].weight*sp.s(weights[i].rdWeight)))*Math.pow(10, 15))==0)) {
			 * weights[i].multiple = "+";
			 * weights[j].multiple = "-";
			 * jd = i;
			 * pt = Math.max(pt, i);
			 * }
			 * if (((sp.s(weights[i].rdWeight-1)*sp.s(weights[j].rdWeight)) > 0)
			 * && (Math.round(((weights[i].weight*sp.s(weights[j].rdWeight))
			 * - (weights[j].weight*sp.s(weights[i].rdWeight-1)))*Math.pow(10, 15))==0)) {
			 * weights[i].multiple = "-";
			 * weights[j].multiple = "+";
			 * jd = j;
			 * pt = Math.max(pt, j);
			 * }
			 * }
			 * }
			 * if (jd > 0) {
			 * d = weights[jd].weight / sp.s(weights[jd].rdWeight);
			 * }
			 * else {
			 * d = (dmin+dmax)/2;
			 * }
			 * dmin = dmax = d; */
			/* for (int i=0; i<numberOfWeights; i++) {
			 * for (j=0; j<numberOfWeights; j++) {
			 * if (((sp.s(weights[i].rdWeight-1)*sp.s(weights[j].rdWeight)) > 0)
			 * && (Math.round(((weights[i].weight*sp.s(weights[j].rdWeight))
			 * - (weights[j].weight*sp.s(weights[i].rdWeight-1)))*Math.pow(10, 15))==0)) {
			 * weights[i].multiple = "-";
			 * weights[j].multiple = "+";
			 * if (j>pt) pt = j; // suche gleich hier nach Maximum (pseudoCode: extra Schleife)
			 * }
			 * }
			 * }
			 * // Pseudocode: if max_j f_j = 1
			 * // pt>-1 heißt: es existieren Bindungen => gleichwertig
			 * if (pt > -1) {
			 * d = weights[pt].weight / sp.s(weights[pt].rdWeight);
			 * mu = sp.s(weights[pt].rdWeight) / weights[pt].weight;
			 * }
			 * else {
			 * d = (dmin+dmax)/2;
			 * mu = (mumin+mumax)/2;
			 * }
			 * mu = sp.s(weights[pt].rdWeight) / weights[pt].weight;
			 * dmin = dmax = d;
			 * mumin = mumax = mu; */

			for (int i = 0; i < numberOfWeights; i++)
			{
				// if ((sp.s(weights[i].rdWeight) > 0) && ((weights[i].weight/weights[i].rdWeight) == dmin)) {
				if ((sp.s(weights[i].rdWeight) > 0)
						&&
						(expDif((weights[i].weight / (sp.s(weights[i].rdWeight) * dmin)),
								1,
								15)))
				{
					weights[i].multiple = "+";
					pt = i;
				}
				// if ((sp.s(weights[i].rdWeight-1) > sp.s(weights[i].min)) && ((weights[i].weight/(weights[i].rdWeight-1)) == dmax)) {
				if ((sp.s(weights[i].rdWeight - 1) > sp.s(weights[i].min - 1))
						&&
						(expDif((weights[i].weight / (sp.s(weights[i].rdWeight - 1) * dmax)),
								1, 15)))
				{
					weights[i].multiple = "-";
				}
			}
			divisor.setPt(pt);
			if (pt > -1)
			{
				mumin = mumax = sp.s(weights[pt].rdWeight) / weights[pt].weight;
				dmin = dmax = d = weights[pt].weight / sp.s(weights[pt].rdWeight);
			}

			if (Debug.DIVMETH)
			{
				debtmp += "Neue Divisorgrenzen: D_low: " + max + "\nD_high: " + min +
						"\n";
				notifyListeners(debtmp);
			}
			try
			{
				divisor.setDivisorInterval(dmin, dmax);
				divisor.setMultiplierInterval(mumin, mumax);
			}
			catch (DivisorException de)
			{
				System.out.println(de);
				lm.setErrorCode(LibMessenger.DIVISOR, 0, "Error while setting divisor to (" + low + ", " + high + ") using " + sp.getName());
				return false;
				// throw new MethodException(sp.getName(), de);
				/* throw new MethodException("Error while setting divisor to (" + low +
				 * ", " + high + ") using " + sp.getName(), de); */
			}
		}
		/* if (Math.round((dmax-dmin)*Math.pow(10, 15)) == 0) {
		 * d = (dmin+dmax)/2;
		 * dmin = dmax = d;
		 * //double s;
		 * for (int i = 0; i < numberOfWeights; i++) {
		 * weights[i].multiple = "";
		 * //if (weights[i].rdWeight < weights[i].max || weights[i].max==0){
		 * s = sp.s(weights[i].rdWeight);
		 * if ((s>0) && (Math.round((weights[i].weight/s-d)*Math.pow(10,15)) == 0))
		 * weights[i].multiple = "+";
		 * //}
		 * //if (weights[i].rdWeight > weights[i].min) {
		 * s = sp.s(weights[i].rdWeight-1);
		 * if ((s>sp.s(weights[i].min-1)) && (Math.round((weights[i].weight/s-d)*Math.pow(10,15)) == 0))
		 * //if ((s>0) && (weights[i].weight/s == d))
		 * weights[i].multiple = "-";
		 * //}
		 * }
		 * if (Debug.DIVMETH) {
		 * debtmp += "Neue Divisorgrenzen: D_low: " + max + "\nD_high: " + min + "\n";
		 * dd.println(debtmp);
		 * }
		 * try {
		 * divisor.setDivisorInterval(dmin, dmax);
		 * }
		 * catch (DivisorException de) {
		 * throw new MethodException(sp.getName(), de);
		 * }
		 * } */

		// Proberechnung
		int sorw = 0;
		for (int i = 0; i < numberOfWeights; i++)
		{
			s = sp.s(weights[i].rdWeight);
			// double dtmp = (weights[jd].weight/weights[jd].rdWeight);
			if (s > 0)
			{
				if (weights[i].getFlag() == 0)
				{
					if (weights[i].weight / (s) - d > 0)
					{
						lm.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
								"@final untied hi; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
						return false;
						// throw new MethodException(MethodException.DIVISOR_DEFECTIVE,
						// sp.getName(),
						// "Weight: " + weights[i] + " Divisor: " + d);
						/* throw new MethodException("@final untied hi; Weight: " + weights[i] +
						 * " Divisor: " + d + " using " + sp.getName()); */
					}
				}
				// if (weights[i].getFlag() == 1) if (Math.round((weights[i].weight/s-(weights[pt].weight/sp.s(weights[pt].rdWeight)))*Math.pow(10, 15)) > 0) {
				if (weights[i].getFlag() == 1)
				{
					if (Math.round(((weights[i].weight / (s * d)) - 1) *
							Math.pow(10, 15)) > 0)
					{
						lm.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
								"@final tied+ hi; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
						return false;
						// throw new MethodException(MethodException.DIVISOR_DEFECTIVE,
						// sp.getName(),
						// "Weight: " + weights[i] + " Divisor: " + d);
						/* throw new MethodException("@final tied+ hi; Weight: " + weights[i] +
						 * " Divisor: " + d + " using " + sp.getName()); */
					}
				}

			}

			s = sp.s(weights[i].rdWeight - 1);
			if (s > 0)
			{
				if (s > sp.s(weights[i].min))
				{
					if (weights[i].getFlag() == 0)
					{
						if (weights[i].weight / (s) - d < 0)
						{
							lm.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
									"@final untied lo; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
							return false;
							// throw new MethodException(MethodException.DIVISOR_DEFECTIVE,
							// sp.getName(),
							// "Weight: " + weights[i] + " Divisor: " + d);
							/* throw new MethodException("@final untied lo; Weight: " +
							 * weights[i] + " Divisor: " + d +
							 * " using " + sp.getName()); */
						}
					}
					// if (weights[i].getFlag() == -1) if (Math.round((weights[i].weight/s-(weights[pt].weight/sp.s(weights[pt].rdWeight)))*Math.pow(10, 15)) < 0) {
					if (weights[i].getFlag() == -1)
					{
						if (Math.round(((weights[i].weight / (s * d)) - 1) *
								Math.pow(10, 15)) < 0)
						{
							// if (Math.round((weights[i].weight/s-(weights[pt].weight/sp.s(weights[pt].rdWeight)))*Math.pow(10, 15)) < 0) {
							lm.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
									"@final tied- lo; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
							return false;
							// throw new MethodException(MethodException.DIVISOR_DEFECTIVE,
							// sp.getName(),
							// "Weight: " + weights[i] + " Divisor: " + d);
							/* throw new MethodException("@final tied- lo; Weight: " + weights[i] +
							 * " Divisor: " + d + " using " +
							 * sp.getName()); */
						}
					}
				}
			}

			if ((weights[i].weight == 0) && (weights[i].rdWeight > 0))
			{
				lm.setErrorCode(LibMessenger.APPORTIONMENT_DEFECTIVE, 0,
						"@final zero in->out; Weight: " + weights[i] + " using " + sp.getName());
				return false;
				/* throw new MethodException("@final zero in->out; Weight: " + weights[i] +
				 * " using " + sp.getName()); */
			}

			if (weights[i].rdWeight < weights[i].min)
			{
				lm.setErrorCode(LibMessenger.MINIMUM, 0,
						"@final min; Weight: " + weights[i] + " using " + sp.getName());
				return false;
				// throw new MethodException(MethodException.MINIMUM, sp.getName(),
				// "Weight: " + weights[i]);
				/* throw new MethodException("@final min; Weight: " + weights[i] +
				 * " using " + sp.getName()); */
			}

			sorw += weights[i].rdWeight;
		}

		if (sorw != accuracy)
		{
			lm.setErrorCode(LibMessenger.APPORTIONMENT_DEFECTIVE, 0,
					"sum: " + sorw + " acc: " + accuracy + " using " + sp.getName());
			return false;
			// throw new MethodException(MethodException.APPORTIONMENT_DEFECTIVE, sp.getName(),
			// "sum: " + sorw + " acc: " + accuracy);
			/* throw new MethodException("sum: " + sorw + " acc: " + accuracy +
			 * " using " + sp.getName()); */
		}

		return true;
	} // end divMethVector

	/** Ruft die Divisormethode anhand der übergebenen Parametern mit stationärer Rundung auf.
	 * 
	 * @param accuracy Anzahl der Mandate
	 * @param q Reelle Zahl, mit der ein Stationary Objekt initialisiert wird
	 * @param w Array von Gewichten
	 * @return <b>true</b>, wenn die Berechnung erfolgreich war
	 * @throws MethodException Fehler beim Aufruf der Divisormethode
	 * @throws ParameterOutOfRangeException Fehler beim Initialisieren des
	 *           Stationary Objekts */
	public boolean qstation(int accuracy, double q, Weight[] w) throws
			MethodException, ParameterOutOfRangeException
	{
		return divMethVector(new Stationary(q), divisor, accuracy, w, messenger);
	}

	/** Ruft die Divisormethode anhand der übergebenen Parametern mit Potenzmittel-Rundung auf.
	 * 
	 * @param accuracy Anzahl der Mandate
	 * @param p Reelle Zahl, mit der ein PowerMean Objekt initialisiert wird
	 * @param w Array von Gewichten
	 * @return <b>true</b>, wenn die Berechnung erfolgreich war
	 * @throws MethodException Fehler beim Aufruf der Divisormethode */
	public boolean pmean(int accuracy, double p, Weight[] w) throws
			MethodException
	{
		// System.out.println("lib.Method-pmean");
		return divMethVector(new PowerMean(p), divisor, accuracy, w, messenger);
	}

	/** Die alte Q-Stationäre Rundung, ersetzt jetzt qStationNewImp und qStationOld
	 * ist nur noch aus Kompatibilitätsgründen drin, fliegt bald auch noch raus
	 * Parameter q aus [0,1] */
	public boolean qstation(int accuracy, double q) throws MethodException,
			ParameterOutOfRangeException
	{
		return divMethVector(new Stationary(q), divisor, accuracy, weights,
				messenger);
	}

	/** Die alte Potenzmittel-Rundung, ersetzt jetzt qStationOld
	 * ist nur noch aus Kompatibilitätsgründen drin, fliegt bald auch noch raus */
	public boolean pmean(int accuracy, double p) throws MethodException
	{
		// System.out.println("lib.Method-pmean");
		return divMethVector(new PowerMean(p), divisor, accuracy, weights,
				messenger);
	}

	/** Prüft Gleichheit zweier Zahlen bei gegebener Genauigkeit
	 * @param a double
	 * @param b double
	 * @param k Genauigkeit (Nachkommastellen)
	 * @return <b>true</b>, bei Gleichheit */
	public static boolean expDif(double a, double b, int k)
	{
		return (Math.round((a - b) * Math.pow(10, k)) == 0);
		// return expDifVal(a, b, k, 0);
	}

}
