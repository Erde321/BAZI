/*
 * @(#)Method.java 3.2 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */

package de.uni.augsburg.bazi.lib.vector;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.DivisorException;
import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.Signpost;
import de.uni.augsburg.bazi.lib.Stationary;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse Method<br>
 * <b>Description:</b> Abstrakte Klasse, die eine Methode zur Berechnung
 * von Vektorzuteilungsproblemen bereitstellt.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2008<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Christian Brand, Jan Petzold, Florian Kluge, Marco Schumacher
 * @version 3.2 */
public abstract class Method
{

	/** Logger, zum Speichern/Ausgeben aller Meldungen */
	private static Logger logger = Logger.getLogger(Method.class);

	/** Wert, der die Hare/Niemayer Quotenmethode repraesentiert */
	public static final double HAREQUOTE = 0d;
	public static final double HAREQUOTE_VAR01 = -0.1d;
	public static final double HAREQUOTE_VAR02 = -0.2d;

	/** Wert, die Hare/Niemayer Quotenmethode Variante 1 repraesentiert */
	public static final double HAREQUOTE_VAR10 = -1d;
	public static final double HAREQUOTE_VAR11 = -1.1d;
	public static final double HAREQUOTE_VAR12 = -1.2d;

	/** Wert, die Hare/Niemayer Quotenmethode Variante 2 repraesentiert */
	public static final double HAREQUOTE_VAR20 = -2d;
	public static final double HAREQUOTE_VAR21 = -2.1d;
	public static final double HAREQUOTE_VAR22 = -2.2d;

	/** Wert, die Droop Quotenmethode repraesentiert */
	public static final double DROOPQUOTE = 1d;
	public static final double DROOPQUOTE_VAR01 = 1.1d;
	public static final double DROOPQUOTE_VAR02 = 1.2d;

	public static final double DROOPQUOTE_VAR10 = 2d;
	public static final double DROOPQUOTE_VAR11 = 2.1d;
	public static final double DROOPQUOTE_VAR12 = 2.2d;

	public static final double DROOPQUOTE_VAR20 = 3d;
	public static final double DROOPQUOTE_VAR21 = 3.1d;
	public static final double DROOPQUOTE_VAR22 = 3.2d;

	public static final double DROOPQUOTE_VAR30 = 4d;
	public static final double DROOPQUOTE_VAR31 = 4.1d;
	public static final double DROOPQUOTE_VAR32 = 4.2d;
	
	public static final double DROOPQUOTE_VAR40 = 5d;
	public static final double DROOPQUOTE_VAR41 = 5.1d;
	public static final double DROOPQUOTE_VAR42 = 5.2d;

	/** Prüft Gleichheit zweier Zahlen bei gegebener Genauigkeit
	 * @param a double
	 * @param b double
	 * @param k Genauigkeit (Nachkommastellen)
	 * @return <b>true</b>, bei Gleichheit */
	public static boolean expDif(double a, double b, int k)
	{
		return (Math.round((a - b) * Math.pow(10, k)) == 0);
	}

	/** Divisor Method Apportionment for Vector Problems
	 * fuer Stationaere Rundung und Potenzmittelrundung
	 * 
	 * @param sp: Die zu verwendende Rundungsmethode (stationär oder Potenzmittel)
	 * @param divisor: Die zu verwendende Divisorklasse
	 * @param accuracy: Die zu vergebende Mandatszahl
	 * @param weights: Die zu rundenden Gewichte
	 * @param libMessenger: LibMessenger
	 * @param methodName: Name der Methode (fuer Fehlermeldungen)
	 * @return <b>true</b> wenn Berechnung erfolgreich */
	public static boolean statPowMethod(Signpost sp, Divisor divisor, int accuracy, Weight[] weights, LibMessenger libMessenger, String methodName)
	{
		/* <b> Start Ueberpruefungsphase </b>
		 * Zunaechst werden irregulaere Faelle ausgesondert. Diese sind:
		 * 1. keine positiven Gewichte
		 * 2. keine positive Mandatszahl
		 * 3. Evtl. vorhandene Min-Bedingung ist nicht erfuellbar.
		 * 4. Die Summe der Gewichte ist 0, und es gibt mindestens 1 Sitz zu vergeben.
		 * -Diese 4 Faelle werden in checkExistence ueberprueft-
		 * 5. Sollte es sich um eine Rundungsmethode handeln, bei der sp.s(0) = 0 ist, so sollte die Anzahl
		 * der zu vergebenen Sitze groesser als die Anzahl der Parteien mit postiven Gewicht sein.
		 * Ansonsten muss eine Zuteilung machen, bei der zuerst die Min-Bedingungen beruecksichtigt werden,
		 * und danach dann nach der Hoehe der Gewichte der Rest zugeteilt wird.
		 * Desweiteren wird auch der Fall abgehandelt, dass es keinen Sitz zu vergeben gibt! */
		/* Setzen der Ties auf "" aus evtl voriger Zuteilung */
		Method.prepareCalculation(weights);

		if (!Method.checkExistence(accuracy, weights, libMessenger))
		{
			return false;
		}

		/* Initialisierung einiger Hilfsvariablen */
		int numberOfWeights = weights.length;
		int sumOfMinSeats = 0;
		double sumOfPositiveWeights = 0.0;

		/* Berechnung der Hilfsvariablen */
		for (int i = 0; i < numberOfWeights; i++)
		{
			sumOfMinSeats += weights[i].min;
			sumOfPositiveWeights += weights[i].weight;
		}

		/* Behandlung eines Spezialfalls: Anzahl der zu vergebenen Sitze = 0
		 * Dann bekommt keine Partei einen Sitz und das Problem ist geloest! */
		if (accuracy == 0)
		{
			for (int i = 0; i < numberOfWeights; i++)
			{
				weights[i].rdWeight = 0;
			}
			try
			{
				divisor.setDivisorInterval(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
				divisor.setMultiplierInterval(0.0, 0.0);
			}
			catch (DivisorException de)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Fehler beim Setzen des Divisors!");
				}
				libMessenger.setErrorCode(LibMessenger.DIVISOR, 0, "Error while setting divisor to (infty, infty) using " + sp.getName());
				return false;
			}
			return true;
		}

		/* Abhandlung von Fall:
		 * Undurchlaessige Rundungsmethode (sp.s(0) = 0), aber die Anzahl der Parteien
		 * mit positiven Gewicht ohne Mindestbedingung ist groesser als die noch
		 * ausstehende Sitzzahl (Hausgroesse - Summe der Min Bedingungen). */
		if (sp.s(0) == 0.0)
		{
			int numberOfPosiveWeightsWithoutMinCond = 0;
			for (int i = 0; i < numberOfWeights; i++)
			{
				if ((weights[i].min == 0) && (weights[i].weight > 0) && weights[i].max != 0)
				{
					numberOfPosiveWeightsWithoutMinCond++;
				}
			}
			int help = accuracy - sumOfMinSeats;
			if (help < numberOfPosiveWeightsWithoutMinCond)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Keine Zuteilung mit diesen Daten moeglich!");
				}
				if (numberOfPosiveWeightsWithoutMinCond > 0)
				{
					libMessenger.setErrorCode(LibMessenger.MIN_SIGNPOST, accuracy, "No solution for " + accuracy
							+ " seats with these min requirements and the selected rounding method!");
					libMessenger.setMethodName(methodName);
				}
				else
					libMessenger.setErrorCode(LibMessenger.MINIMUM, accuracy, "No solution for " + accuracy
							+ " seats with these min requirements.");
				return false;
			}
		}

		// Kein Grund, den Fall gesondert zu betrachten
		// so würde kein Divisor angezeigt werden etc.
		// if (weights.length == 1) {
		// weights[0].rdWeight = accuracy;
		// try{
		// double div_min = weights[0].weight / sp.s(accuracy);
		// double div_max = weights[0].weight / sp.s(accuracy - 1);
		// divisor.setDivisorInterval(div_min, div_max);
		// divisor.setMultiplierInterval(1 / div_max, 1 / div_min);
		// //divisor.setNeeded(false);
		// }
		// catch(Exception de){
		// if(logger.isDebugEnabled()){
		// logger.debug("Fehler beim Setzen des Divisors!");
		// }
		// libMessenger.setErrorCode(LibMessenger.DIVISOR, 0, "Error while setting divisor to (infty, infty) using " + sp.getName());
		// return false;
		// }
		// return true;
		// }

		/* <b> Ende Ueberpruefungsphase </b>
		 * D.h.
		 * 1. eine evtl. vorhandenen Min Bedingung ist erfuellbar (auch bei Rundung mit s(0) = 0)
		 * 2. es gibt positive Gewichte
		 * 3. es muss mindestens 1 Sitz zugeteilt werden
		 * 4. wenn s(0) = 0 muessen mehr Sitze zugeteilt werden als es positive Gewichte gibt! */

		/* <b> Start Initialiserungsphase </b>
		 * Hierbei geschieht eine erste Zuteilung der Sitze gemaess der Min-Bedingung und der Zuteilung
		 * mit Hilfe des Unbiased Multipliers */
		double ubm = accuracy;
		/* Stationaere Methoden erlauben noch eine Verbesserung */
		if ((accuracy > numberOfWeights / 2) && (sp instanceof Stationary))
		{
			ubm += (sp.getParam() - 0.5) / numberOfWeights;
		}
		double temp;
		int sumOfRoundedWeights = 0;

		for (int i = 0; i < numberOfWeights; i++)
		{
			temp = ubm * weights[i].weight / sumOfPositiveWeights;
			weights[i].rdWeight = sp.signpostRound(temp);
			sumOfRoundedWeights += weights[i].rdWeight;
			/* Ueberpruefen, ob eine Partei die absolute Mehrheit hat. */
			if (weights[i].weight > sumOfPositiveWeights / 2d)
			{
				libMessenger.setAbsolute(weights[i].name, weights[i].weight, sumOfPositiveWeights);
			}
		}
		/* <b> Ende Initialisierungsphase </b>
		 * Eine erste Zuteilung ist nun geschehen, aber durch evtl. Rundungen, bzw. Min und Max
		 * Bedingungen, kann nun die tatsaechliche Anzahl vergebener Sitze noch von der vorgegebenen
		 * abweichen. */

		/* <b> Start Restzuteilungsphase </b>
		 * Es gibt 3 Moeglichkeiten:
		 * 1. Anzahl der Sitze durch die Erstverteilung entspricht genau der Anzahl zu vergebener Sitze
		 * => Es ist nichts zu tun!
		 * 2. Anzahl der bereits vergebenen Sitze ist kleiner als die zu vergebenen Sitze
		 * => Inkrementierung noetig
		 * 3. Anzahl der bereits vergebenen Sitze ist groesser als die zu vergebenen Sitze
		 * => Dekrementierung noetig */
		if (sumOfRoundedWeights < accuracy)
		{
			IncreasePriorityQueue pq = new IncreasePriorityQueue(weights, sp, libMessenger, false);
			pq.increase(accuracy - sumOfRoundedWeights);
			if (pq.isMultiple())
				pq.setTies();
		}
		else if (sumOfRoundedWeights > accuracy)
		{
			DecreasePriorityQueue pq = new DecreasePriorityQueue(weights, sp, libMessenger, false);
			pq.decrease(sumOfRoundedWeights - accuracy);
			if (pq.isMultiple())
				pq.setTies();
		}

		boolean needCond = false;
		sumOfRoundedWeights = 0;
		for (int i = 0; i < numberOfWeights; i++)
		{
			if (weights[i].rdWeight < weights[i].min)
			{
				weights[i].rdWeight = weights[i].min;
				needCond = true;
				weights[i].conditionEffective = true;
			}
			else if (weights[i].rdWeight > weights[i].max)
			{
				weights[i].rdWeight = weights[i].max;
				needCond = true;
				weights[i].conditionEffective = true;
			}
			else
				weights[i].conditionEffective = false;

			sumOfRoundedWeights += weights[i].rdWeight;
		}
		if (needCond)
		{
			libMessenger.setErrorCode(-1);
			for (Weight w : weights)
				w.multiple = "";

			if (sumOfRoundedWeights < accuracy)
			{
				IncreasePriorityQueue pq = new IncreasePriorityQueue(weights, sp, libMessenger, true);
				pq.increase(accuracy - sumOfRoundedWeights);
				if (pq.isMultiple())
					pq.setTies();
			}
			else if (sumOfRoundedWeights > accuracy)
			{
				DecreasePriorityQueue pq = new DecreasePriorityQueue(weights, sp, libMessenger, true);
				pq.decrease(sumOfRoundedWeights - accuracy);
				if (pq.isMultiple())
					pq.setTies();
			}
		}

		/* <b> Ende Restzuteilungsphase </b>
		 * Die Sitze sind nun korrekt zugeteilt und die Anzahl der Sitze entspricht der zu vergebenen Anzahl von Sitzen.
		 * Jetzt muessen noch Divisor und Multiplikator berechnet werden und evtl. die Ties gesetzt werden.
		 * Danach erfolgt noch die Proberechnung. */

		/* <b> Start Divisor/Multiplikator Berechnung </b>
		 * Der Divisor kann mit Hilfe der PriorityQueue berechnet werden, von der evtl. auch schon ein Wert existiert,
		 * da die Klasse evtl. schon erstellt wurde bei der Inkrementierung/Dekrementierung.
		 * *************************************************
		 * Der Divisor ist ein Wert aus dem Intervall
		 * v[i] v[i]
		 * [ max { -------- | für alle i } , min { ------------ | für alle i } ]
		 * s(m[i]) s(m[i] - 1)
		 * (v : weight, m : rdWeight, s(m) : Stationaere oder Potenzmittelrundung von m)
		 * ************************************************ */
		divisor.setEnabled(true);
		double dMin = new IncreasePriorityQueue(weights, sp, libMessenger, true).getNextValue();
		double dMax = new DecreasePriorityQueue(weights, sp, libMessenger, true).getNextValue();

		double dMinNice, dMaxNice;
		if (needCond)
		{
			dMinNice = dMin;
			dMaxNice = dMax;
		}
		else
		{
			dMinNice = new IncreasePriorityQueue(weights, sp, libMessenger, false).getNextValue();
			dMaxNice = new DecreasePriorityQueue(weights, sp, libMessenger, false).getNextValue();
		}

		 if (Math.abs(dMax - dMin) <= PriorityQueue.epsilon)
		 {
		 // Ties sind aufgetreten
		 double mult = 1 / dMin;
		 for (int i = 0; i < weights.length; i++)
		 {
		 /* Änderung 18. Januar 2008
		 * Wenn PT nicht gesetzt wird, so funktioniert bei den BiProp Methoden
		 * die Tie Auflösung nicht mehr!
		 * Daher wurde die if-Klausel auskommentiert um pt auch immer zu setzen! */
		 // if(weights[i].multiple.equals("")){
		 double val1 = sp.s(weights[i].rdWeight - 1) / weights[i].weight;
		 double val2 = sp.s(weights[i].rdWeight) / weights[i].weight;
		 if (Math.abs(val1 - mult) <= PriorityQueue.epsilon)
		 {
		 weights[i].multiple = "-";
		 }
		 else if (Math.abs(val2 - mult) <= PriorityQueue.epsilon)
		 {
		 weights[i].multiple = "+";
		 /* Diese Zeile ist essentiell! */
		 divisor.setPt(i);
		 }
		 // }
		 }
		 }

		try
		{
			divisor.setDivisorInterval(dMin, dMax);
			divisor.setDivisorIntervalNice(dMinNice, dMaxNice);
		}
		catch (DivisorException de)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Fehler beim Setzen der Divisoren!");
			}
			libMessenger.setErrorCode(LibMessenger.DIVISOR, 0, "Error while setting divisor to (" + dMin + ", " + dMax + ") using " + sp.getName());
			return false;
		}

		for (Weight w : weights)
			w.quotient = w.weight / divisor.getDivisor();

		/* Multiplikator ergibt sich aus Divisorintervall als 1/max, 1/min */
		try
		{
			divisor.setMultiplierInterval(1d / dMax, 1d / dMin);
		}
		catch (DivisorException de)
		{
			System.out.println(de);
			libMessenger.setErrorCode(LibMessenger.DIVISOR, 0, "Error while setting multiplier to (" + 1d / dMax + ", " + 1d / dMin + ") using " + sp.getName());
			return false;
		}

		/* <b> Ende Divisor/Multiplikator Berechnung </b>
		 * <b> Start Proberechnung </b> */

		double d, s;
		dMin = divisor.getDivisorLow();
		dMax = divisor.getDivisorHigh();
		d = divisor.getDivisor();

		/* Ueberpruefen der Divisorgrenzen */
		if (dMin > dMax)
		{
			libMessenger.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0, "dMin > dMax using " + sp.getName());
			return false;
		}
		/* Hilfsvariable, die nochmals die Summe der zugeteilten Sitze speichert */
		int sorw = 0;
		for (int i = 0; i < numberOfWeights; i++)
		{
			s = sp.s(weights[i].rdWeight);
			if (weights[i].rdWeight == weights[i].max)
			{
				s = 0d;
			}
			if (s > 0)
			{
				if (weights[i].getFlag() == 0)
				{
					if (weights[i].weight / (s * d) - 1d > 0)
					{
						d = 0.5 * (dMin + dMax);
						double neu = weights[i].weight / (s * d) - 1d;
						if (neu > 0)
						{
							libMessenger.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
									"@final untied hi; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
							return false;
						}
						else
						{
							logger.warn("Schöner Divisor liefert Fehlberechnung bei Final Check. Divisor in der Mitte liefert aber richtiges Ergebnis!");
						}
					}
				}
				if (weights[i].getFlag() == 1)
				{
					if (Math.round(((weights[i].weight / (s * d)) - 1d) * Math.pow(10, 15)) > 0)
					{
						libMessenger.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
								"@final tied+ hi; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
						return false;
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
						if (weights[i].weight / (s * d) - 1d < 0)
						{
							libMessenger.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
									"@final untied lo; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
							return false;
						}
					}
					if (weights[i].getFlag() == -1)
					{
						if (Math.round(((weights[i].weight / (s * d)) - 1) * Math.pow(10, 15)) < 0)
						{
							d = 0.5 * (dMin + dMax);
							double neu = weights[i].weight / (s * d) - 1d;
							if (neu > 0)
							{
								libMessenger.setErrorCode(LibMessenger.DIVISOR_DEFECTIVE, 0,
										"@final tied- lo; Weight: " + weights[i] + " Divisor: " + d + " using " + sp.getName());
								return false;
							}
							else
							{
								logger.warn("Schöner Divisor liefert Fehlberechnung bei Final Check. Divisor in der Mitte liefert aber richtiges Ergebnis!");
							}
						}
					}
				}
			}

			if ((weights[i].weight == 0) && (weights[i].rdWeight > 0))
			{
				libMessenger.setErrorCode(LibMessenger.APPORTIONMENT_DEFECTIVE, 0,
						"@final zero in->out; Weight: " + weights[i] + " using " + sp.getName());
				return false;
			}

			if (weights[i].rdWeight < weights[i].min)
			{
				libMessenger.setErrorCode(LibMessenger.MINIMUM, 0,
						"@final min; Weight: " + weights[i] + " using " + sp.getName());
				return false;
			}

			if (weights[i].rdWeight > weights[i].max && weights[i].max != 0)
			{
				libMessenger.setErrorCode(LibMessenger.MAXIMUM, 0,
						"@final max; Weight: " + weights[i] + " using " + sp.getName());
				return false;
			}
			sorw += weights[i].rdWeight;
		}

		if (sorw != accuracy)
		{
			libMessenger.setErrorCode(LibMessenger.APPORTIONMENT_DEFECTIVE, 0,
					"sum: " + sorw + " acc: " + accuracy + " using " + sp.getName());
			return false;
		}
		/* <b>Ende Proberechnung</b> */
		return true;
	}

	/** Methode zur Loesung von Vektorproblemen, jedoch nach einer Quotenmethode
	 * @param method: die zu verwendene Methode: 0 fuer Hare/Niemayer V / M
	 *          1 fuer Droop ceil(V / M+1)
	 * @param divisor: die zu verwendene DivisorKlasse
	 * @param weights: die Gewichte der Parteien
	 * @param accuracy: die Anzahl der zu vergebenen Sitze
	 * @param libMessenger: Klasse fuer Fehlermeldungen
	 * @return true, falls ein Ergebnis berechnet wurde */
	public static boolean quotenMethode(double method, Divisor divisor, int accuracy, Weight[] weights, LibMessenger libMessenger)
	{
		/* <b> Start Ueberpruefungsphase </b>
		 * In CheckExistence wird ausgesondert:
		 * 1. keine positiven Gewichte
		 * 2. keine positive Mandatszahl
		 * 3. Evtl. vorhandene Max oder Min-Bedingung ist nicht erfuellbar.
		 * 4. Die Summe der Gewichte ist 0, und es gibt mindestens 1 Sitz zu vergeben. */
		/* Setzen der Ties auf "" wegen voriger Kalkulation */
		Method.prepareCalculation(weights);
		divisor.setEnabled(false);

		if (!Method.checkExistence(accuracy, weights, libMessenger))
		{
			return false;
		}

		// Kein Grund, den Fall gesondert zu betrachten
		// so würde keine Quote angezeigt werden etc.
		// if (weights.length == 1) {
		// weights[0].rdWeight = accuracy;
		//
		// divisor.setQuote(0d);
		// //divisor.setNeeded(false);
		//
		// return true;
		// }

		/* <b> Ende Ueberpruefungsphase </b>
		 * <b> Start Berechnung </b>
		 * Berechnung, ob eine Partei die absolute Mehrheit hat!
		 * Berechnung der Quote und Zuteilung mit Hilfe dieser Quote.
		 * Dies geschieht in der Klasse <b>QuotePriorityQueue</b>,
		 * die selbststaendig die Reste verwaltet */
		double sumOfWeights = 0d, quote = 0;
		for (int i = 0; i < weights.length; i++)
		{
			sumOfWeights += weights[i].weight;
		}
		/* Berechnung, ob eine Partei die absolute Mehrheit hat. */
		for (int i = 0; i < weights.length; i++)
		{
			if (2.0 * weights[i].weight > sumOfWeights)
			{
				libMessenger.setAbsolute(weights[i].name, weights[i].weight, sumOfWeights);
				break;
			}
		}
		if (isMethod(method, Method.HAREQUOTE) || isMethod(method, Method.HAREQUOTE_VAR01) || isMethod(method, Method.HAREQUOTE_VAR02))
		{
			quote = sumOfWeights / accuracy;
		}
		else if (isMethod(method, Method.HAREQUOTE_VAR10) || isMethod(method, Method.HAREQUOTE_VAR11) || isMethod(method, Method.HAREQUOTE_VAR12))
		{
			quote = Math.max(1, Math.floor(sumOfWeights / (accuracy)));
		}
		else if (isMethod(method, Method.HAREQUOTE_VAR20) || isMethod(method, Method.HAREQUOTE_VAR21) || isMethod(method, Method.HAREQUOTE_VAR22))
		{
			quote = Math.ceil(sumOfWeights / (accuracy));
		}
		else if (isMethod(method, Method.DROOPQUOTE) || isMethod(method, Method.DROOPQUOTE_VAR01) || isMethod(method, Method.DROOPQUOTE_VAR02))
		{
			quote = 1d + Math.floor(sumOfWeights / (accuracy + 1));
		}
		else if (isMethod(method, Method.DROOPQUOTE_VAR10) || isMethod(method, Method.DROOPQUOTE_VAR11) || isMethod(method, Method.DROOPQUOTE_VAR12))
		{
			quote = Math.max(1d, Math.floor(sumOfWeights / (accuracy + 1)));
		}
		else if (isMethod(method, Method.DROOPQUOTE_VAR20) || isMethod(method, Method.DROOPQUOTE_VAR21) || isMethod(method, Method.DROOPQUOTE_VAR22))
		{
			quote = Math.ceil(sumOfWeights / (accuracy + 1));
		}
		else if (isMethod(method, Method.DROOPQUOTE_VAR30) || isMethod(method, Method.DROOPQUOTE_VAR31) || isMethod(method, Method.DROOPQUOTE_VAR32))
		{
			quote = Math.max(1d, Math.round(sumOfWeights / (accuracy + 1)));
		}
		else if (isMethod(method, Method.DROOPQUOTE_VAR40) || isMethod(method, Method.DROOPQUOTE_VAR41) || isMethod(method, Method.DROOPQUOTE_VAR42))
		{
			quote = sumOfWeights / (accuracy + 1);
		}
		else
		{
			libMessenger.setErrorCode(LibMessenger.INPUT_DEFECTIVE, 0, "keine gueltige Quotenmethode");
			return false;
		}

		QuotePriorityQueue pq = new QuotePriorityQueue(weights, quote, libMessenger, method, false);
		boolean needCond = !pq.increase(accuracy) || libMessenger.getError();
		if (!needCond)
		{
			for (Weight w : weights)
				if (w.rdWeight < w.min || w.rdWeight > w.max)
				{
					needCond = true;
					w.conditionEffective = true;
				}
				else
					w.conditionEffective = false;
		}

		if (needCond)
		{
			libMessenger.refresh();
			for (Weight w : weights)
				w.multiple = "";
			pq = new QuotePriorityQueue(weights, quote, libMessenger, method, true);
			if (!pq.increase(accuracy))
				return false;
		}

		/* Ties setzen */
		pq.setTies();
		/* <b> Ende Berechnung </b>
		 * Jetzt noch den Divisor setzen. */
		divisor.setQuote(quote);
		return true;
	}

	/** Methode ueberprueft, ob eine Loesung existiert. Es wird ueberprueft: <br>
	 * - positive Anzahl Sitze </br> <br>
	 * - nur positive Gewichte </br> <br>
	 * - Min/Max Bedingung ist erfuellbar </br> <br>
	 * - Summe der Gewichte nicht = 0 und mind. 1 Sitz zu vergeben
	 * @param accuracy die zu vergebene Sitzzahl
	 * @param weights die Gewichte auf denen agiert wird
	 * @param libMessenger Klasse fuer Fehlerbehandlung */
	private static boolean checkExistence(int accuracy, Weight[] weights, LibMessenger libMessenger)
	{
		/* <b> Start Ueberpruefungsphase </b>
		 * Es werden irregulaere Faelle ausgesondert. Diese sind:
		 * 1. keine positiven Gewichte
		 * 2. keine positive Mandatszahl
		 * 3. Evtl. vorhandene Max oder Min-Bedingung ist nicht erfuellbar.
		 * 4. Die Summe der Gewichte ist 0, und es gibt mindestens 1 Sitz zu vergeben. */

		if (logger.isTraceEnabled())
		{
			logger.trace("Starte Überprüfungsphase!");
		}

		/* Initialisierung einiger Hilfsvariablen */
		int numberOfWeights = weights.length;
		int sumOfMinSeats = 0;
		int sumOfMaxSeats = 0;
		double sumOfPositiveWeights = 0.0;

		/* Es wird die Anzahl der postiven Gewichte, die Summe der Gewichte und
		 * die Summe der Min-Anzahl der Sitze berechnet.
		 * Desweiteren wird abgebrochen, falls negative Gewichte oder eine
		 * negative Min Bedingung uebergeben wurden!
		 * Ausserdem wird angebrochen, wenn eine Min-Bedingung existiert, obwohl die Partei
		 * keine Stimmen erhalten hat!
		 * Abhandlung Fall 1. + 2. */
		for (int i = 0; i < numberOfWeights; i++)
		{
			if (weights[i].weight != 0.0)
			{
				/* negative Gewichte
				 * Dies könnte man auch in der GUI abfangen */
				if (weights[i].weight < 0.0)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Negatives Gewicht bei " + weights[i].name + ": " + weights[i].weight);
					}
					libMessenger.setErrorCode(LibMessenger.BAD_INPUT, 0, Resource.getString("bazi.error.illegal_input"));
					return false;
				}
				sumOfPositiveWeights += weights[i].weight;
			}
			if (weights[i].max < 0)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Negative Max Bedingung bei " + weights[i].name + ": " + weights[i].weight);
				}
				libMessenger.setErrorCode(LibMessenger.BAD_INPUT, 0, Resource.getString("bazi.error.illegal_input"));
				return false;
			}
			if (weights[i].min != 0)
			{
				/* Min-Bedingung <0
				 * Dies koennte evtl. schon in der GUI abgefangen werden! */
				if (weights[i].min < 0)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Negative Min Bedingung bei " + weights[i].name + ": " + weights[i].weight);
					}
					libMessenger.setErrorCode(LibMessenger.BAD_INPUT, 0, Resource.getString("bazi.error.illegal_input"));
					return false;
				}
				else if (weights[i].min > weights[i].max)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Min Bedingung (" + weights[i].min + ") > Max Bedingung ("
								+ weights[i].max + ") bei " + weights[i].name);
					}
					libMessenger.setErrorCode(LibMessenger.BAD_INPUT, 0, Resource.getString("bazi.error.illegal_input"));
					return false;
				}
				/* Zuweisung einer Mindestanzahl, obwohl die Partei keine Stimmen erhalten hat resultieren in einen Fehler */
				if (weights[i].weight == 0.0)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Gewicht = 0 und MinBedingung > 0 bei " + weights[i].name);
					}
					libMessenger.setErrorCode(LibMessenger.BAD_INPUT, 0, Resource.getString("bazi.error.illegal_input"));
					return false;
				}
				sumOfMinSeats += weights[i].min;
			}
			if (sumOfMaxSeats < Integer.MAX_VALUE)
			{
				if (weights[i].max == Integer.MAX_VALUE)
				{
					sumOfMaxSeats = Integer.MAX_VALUE;
				}
				else
				{
					sumOfMaxSeats += weights[i].max;
				}
			}
		}

		/* Ueberpruefe, ob die Min Bedingung erfuellbar ist, d.h. sumOfMinSeats muss >= accuracy sein
		 * Sonst gibt es keine Loesung fuer das Problem.
		 * Abhandlung Fall 3. */
		if (sumOfMinSeats > accuracy)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Summe der Min-Bedingungen ist größer als die Hauszahl.");
			}
			libMessenger.setErrorCode(LibMessenger.MINIMUM, accuracy, "NA = Not Available: With these minimum requirements no solution for " + accuracy + " seats!");
			return false;
		}
		if (sumOfMaxSeats < accuracy)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Summe der Max-Bedingungen ist kleiner als die Hauszahl.");
			}
			libMessenger.setErrorCode(LibMessenger.MAXIMUM, accuracy, "NA = Not Available: With these maximum requirements no solution for " + accuracy + " seats!");
			return false;
		}

		/* Ueberpruefe nun, ob sumOfPostiveWeights > 0 und accuracy == 0
		 * Abhandlung Fall 4. */
		if (sumOfPositiveWeights == 0.0 && accuracy > 0)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Summe aller Gewichte = 0 und mindestens 1 Sitz zu vergeben!");
			}
			libMessenger.setErrorCode(LibMessenger.SUM_OF_WS_ZERO, 0, "sumOfWeights == 0, M>0");
			return false;
		}

		if (logger.isTraceEnabled())
		{
			logger.trace("Ende Überprüfungsphase");
		}

		/* <b> Ende der Ueberpruefung </b> */
		return true;
	}

	/** Methode, die die Ties zurücksetzt. Diese wird vor der Berechnung aufgerufen.
	 * @param weights das Weight-Array, dessen Ties zurückgesetzt werden sollen. */
	private static void prepareCalculation(Weight[] weights)
	{
		if (logger.isTraceEnabled())
		{
			logger.trace("Alle evtl. noch vorhandenen Ties werden zurückgesetzt.");
		}

		for (int i = 0; i < weights.length; i++)
		{
			weights[i].multiple = "";
		}
	}

	public static boolean isMethod(double d, double method)
	{
		if (d < method + 0.05 && d > method - .05)
			return true;
		return false;
	}

	public static boolean isDroop(double d)
	{
		return d >= .5;
	}
}
