/*
 * @(#)CalculationMonoprop.java 3.2 07/05/14
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.Calculation.RString;
import de.uni.augsburg.bazi.gui.RoundFrame;
import de.uni.augsburg.bazi.lib.Convert;
import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.DivisorException;
import de.uni.augsburg.bazi.lib.ExtendedPowerMean;
import de.uni.augsburg.bazi.lib.ExtendedStationary;
import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.MethodListener;
import de.uni.augsburg.bazi.lib.ParameterOutOfRangeException;
import de.uni.augsburg.bazi.lib.Permutations;
import de.uni.augsburg.bazi.lib.PowerMean;
import de.uni.augsburg.bazi.lib.Rounding;
import de.uni.augsburg.bazi.lib.Stationary;
import de.uni.augsburg.bazi.lib.Weight;
import de.uni.augsburg.bazi.lib.vector.Method;

/** <b>Title:</b> Klasse CalculationMonoprop<br>
 * <b>Description:</b> steuert den Berechnungsablauf einer monoproportionalen Zuteilung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 3.2
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */
public class CalculationMonoprop
{

	/** Eingabe-Daten. */
	private final InputData data;

	/** Vektor mit den Ausgabezeilen. */
	private final Vector<String[]> vOutput = new Vector<String[]>();

	/** Speicherung und Formatierung des Divisor. */
	private Divisor divisor = new Divisor();

	/** Falls mehrere Potenzen, die Divisoren speichern */
	Divisor[] divs = { divisor };

	/** Speichert Meldungen bzw. Warnungen der Berechnung. */
	private final LibMessenger lm = new LibMessenger();

	/** Interpretiert die Meldungen aus der Berechnung Locale-abhängig um */
	private final Messenger messenger = new Messenger(lm);

	/** StringBuffer mit allen Auswertungen */
	// private StringBuffer sbTotalOut = new StringBuffer();

	/** Nummer der aktuellen Methode */
	private int nme;

	/** Formatierung der Ausgabe */
	private final OutputFormat of;

	/** Gibt an, ob Ties als +/- kodiert werden sollen */
	private boolean printMult = true;

	/** Sammelt alle Fehlermeldungen einer Berechnung */
	private String errorMessages = "";

	/** Debug Fenster */
	private final MethodListener methodListener;
	
	/** Erzeugt eine Instanz von Calculation.
	 * @param data Zu verarbeitendes InputData */
	public CalculationMonoprop(InputData data, MethodListener ml)
	{
		this.data = data;
		methodListener = ml;
		// System.out.println(data.title);
		of = data.outputFormat;
		data.msgSave = messenger;
	}

	/** Erzeugt eine Instanz von Calculation, nur ohne MethodListener
	 * @param data Zu verarbeitendes InputData */
	public CalculationMonoprop(InputData data)
	{
		this(data, new MethodListener()
		{
			public void printMessage(String msg)
			{
				// do nothing
			}
		});
	}

	/** Start der Berechnung.
	 * 
	 * @return Ergebnisse der berechnung in einem formatierten String. */
	public String start()
	{
		messenger.setData(data);
		prepareOutput(data instanceof ListInputData);
		boolean ret = false;
		if (data.vNames != null)
		{
			for (int i = 0; i < data.vNames.size(); i++)
			{
				String name = data.vNames.get(i);
				if (name.startsWith("+"))
				{
					divisor.setCombinedList(true);
					break;
				}
			}
		}
		
		// wb: Start
//		data.ml.printMessage("Start");
//		data.ml.printMessage("\n--------------\n");
		// wb: Ende

		// Listenverbindung?
		if (data instanceof ListInputData)
		{
			divisor.setCombinedList(true);
			data.apValid = new boolean[of.methods.length];
			for (int i = 0; i < data.accuracies.length; i++)
			{
				// Abbruch durch Benutzer
				if (Thread.interrupted())
				{
					return null;
				}
				MethodData d = of.methods[i];

				// Existierte in der Haupzuteilung keine Lösung, wurde die Mandatszahl
				// auf minus unendlich gesetzt. Somit existiert in der Listenzuteilung
				// auch keine Lösung.
				if (data.accuracies[i] > Integer.MIN_VALUE)
				{
					ret = false;
					if (d.subapportionment != null)
					{
						d = d.subapportionment;
					}

					if (d.method == MethodData.RSTATION)
					{
						try
						{
							Stationary stat;
							if (d.paramString != null)
							{
								stat = new ExtendedStationary(d.paramString);
							}
							else
							{
								stat = new Stationary(d.param);
							}
							ret = Method.statPowMethod(stat, divisor, data.accuracies[i], data.weights[i][0][0], lm, d.name);
						}
						catch (ParameterOutOfRangeException ooREx)
						{
							ooREx.printStackTrace();
							ret = false;
						}
					}
					else if (d.method == MethodData.PMEAN)
					{
						PowerMean power;
						if (d.paramString != null)
						{
							power = new ExtendedPowerMean(d.paramString);
						}
						else
						{
							power = new PowerMean(d.param);
						}
						ret = Method.statPowMethod(power, divisor, data.accuracies[i], data.weights[i][0][0], lm, d.name);
					}
					else if (d.method == MethodData.QUOTA)
					{
						ret = Method.quotenMethode(d.param, divisor, data.accuracies[i], data.weights[i][0][0], lm);
					}
					data.apValid[i] = ret;
					if (!ret)
					{
						System.out.println("Error: " + lm.getErrorMessage());
						methodListener.printMessage("Error: " + lm.getErrorMessage());
					}
				}
				else
				{
					// Fehler im LibMessenger setzen
					lm.setErrorCode(LibMessenger.ERROR_MAIN_PROP, 0, "");
					methodListener.printMessage("Error in Main Prop!");
				}
				nme = i;
				putOutput(i, 0, d, d.name, data.accuracies[i]);

			}
		}
		// Hauptzuteilung
		else
		{
			// wb: Start
//			data.ml.printMessage("Start: Hauptzuteilung");
//			data.ml.printMessage("\n--------------\n");
			// wb: Ende
			// Berechnung für jeden Wert aus iAccuracy
			for (int i = 0; i < data.accuracies.length; i++)
			{
				data.apValid = new boolean[of.methods.length];
				// Berechnungen der Nummerierung nach durchfuehren
				for (int k = 0; k < of.methods.length; k++)
				{

					divs = new Divisor[data.weights[i][k].length];

					for (int p = 0; p < data.weights[i][k].length; p++)
					{
//						// wb: Start
//						data.ml.printMessage("Start: Berechnungen der Nummerierung nach durchfuehren");
//						data.ml.printMessage("data.weights[i][k].length"+data.weights[i][k].length);
//						data.ml.printMessage("\n--------------\n");
						// wb: Ende
						divs[p] = new Divisor();
						divisor = divs[p];
						
						// Abbruch durch Benutzer
						if (Thread.interrupted())
						{
							return null;
						}
						// Bei Solothurn 2005 Dorneck/Thierstein mit geänderter Min-Bedingung
						// existiert hier keine Lösung (solutionExist(...) == false). Das
						// muß entsprechend gefangen werden!

						// existiert fuer data.accuracy[i] eine Loesung unter Beruecksichtigung der Mindestbedingungen?
						if ((of.condition != OutputFormat.CONDITION_MIN && of.condition != OutputFormat.CONDITION_MAX)
								|| solutionExist(data.accuracies[i], of.methods[k].method == MethodData.QUOTA))
						{
							ret = false;
							if (of.methods[k].method == MethodData.RSTATION)
							{
								try
								{
									Stationary stat;
									if (of.methods[k].paramString != null)
									{
										stat = new ExtendedStationary(of.methods[k].paramString);
									}
									else
									{
										stat = new Stationary(of.methods[k].param);
									}
									ret = Method.statPowMethod(stat, divisor, data.accuracies[i], data.weights[i][k][p], lm, of.methods[k].name);
								}
								catch (ParameterOutOfRangeException pe)
								{
									methodListener.printMessage("Paramter Out of Range: " + of.methods[k].param);
									System.out.println(pe);
									ret = false;
								}
							}
							else if (of.methods[k].method == MethodData.PMEAN)
							{
								PowerMean power;
								if (of.methods[k].paramString != null)
								{
									power = new ExtendedPowerMean(of.methods[k].paramString);
								}
								else
								{
									power = new PowerMean(of.methods[k].param);
								}
								ret = Method.statPowMethod(power, divisor, data.accuracies[i], data.weights[i][k][p], lm, of.methods[k].name);
							}
							else if (of.methods[k].method == MethodData.QUOTA)
							{
								ret = Method.quotenMethode(of.methods[k].param, divisor, data.accuracies[i], data.weights[i][k][p], lm);
							}
							else
							{
								// keine gueltige Methode
							}

							if (!ret)
							{
								// System.out.println("Error: " + lm.getErrorMessage());
								methodListener.printMessage("Error: " + lm.getErrorMessage());
							}
							data.apValid[k] = ret;
							
							// wb: Start							
//							data.ml.printMessage("divisor"+divisor.getDivisor());
//							data.ml.printMessage("\n--------------\n");
							//data.ml.printMessage("divsav[0]"+data.divSave[0].getDivisor());
							// wb: Ende
						}
						/** Es existiert keine Loesung! */
						else
						{
							// System.out.println("Keine Lösung?!?");
							fillSave(k);
						}
					}
					nme = k;
					try
					{
						putOutput(i, k, of.methods[k], of.methods[k].name, data.accuracies[i]);
					}
					catch (NullPointerException npe)
					{
						methodListener.printMessage(npe.getMessage());
						return "";
					}

					// Listenverbindungen
					if (data.listData != null)
					{
						for (int m = 0; m < data.listData.length; m++)
						{
							data.listData[m].accuracies[(i * of.methods.length) + k] =
									data.weights[i][k][0][data.listData[m].parentIndex].rdWeight;
							data.listData[m].outputFormat.methods[(i * of.methods.length) + k] = of.methods[k];
						}
					}
				}
			}
		}
		
		// wb: Start
//				methodListener.printMessage("Start: Ende");
//				methodListener.printMessage("\n--------------\n");
				// wb: Ende
		return writeSolution();
	}

	/** Sichert die k-te Zuteilung in das entsprechende InputData Feld.
	 * 
	 * @param k Nummer der aktuellen Berechnung */
	private void fillSave(int k)
	{
		// Save-Gewichte erstellen, aber mit -1 initialisieren!
		for (int n = 0; n < data.weightcount(); n++)
		{
			data.save[k][n] = data.weights[0][k][0][n].clonew();
			data.save[k][n].rdWeight = -1;
		}
		data.divSave[k] = new Divisor();
		try
		{
			data.divSave[k].setDivisorInterval(0, 0);
			data.divSave[k].setMultiplierInterval(0, 0);
		}
		catch (DivisorException e)
		{
			System.out.println(e);
		}
		data.apValid[k] = false;
	}

	/** Prüft auf Existenz einer Lösung mit Mindestbedingungen für die Mandatszahl
	 * accuracy.
	 * 
	 * @param accuracy Mandatszahl
	 * @return <code>true</code> wenn es eine Lösung gibt */
	private boolean solutionExist(int accuracy, boolean quote)
	{
		int minSum = 0;
		int maxSum = 0;
		for (int j = 0; j < data.weightcount(); j++)
		{
			minSum += data.originalWeights[j].min;
			if (maxSum < Integer.MAX_VALUE && data.originalWeights[j].max != Integer.MAX_VALUE)
			{
				maxSum += data.originalWeights[j].max;
			}
			else
			{
				maxSum = Integer.MAX_VALUE;
			}
		}
		if (!quote && minSum > accuracy)
		{
			lm.setErrorCode(LibMessenger.MINIMUM, accuracy, "");
			for (int j = 0; j < data.weightcount(); j++)
			{
				data.originalWeights[j].rdWeight = Integer.MIN_VALUE;
			}
			return false;
		}
		else if (!quote && maxSum < accuracy)
		{
			lm.setErrorCode(LibMessenger.MAXIMUM, accuracy, "");
			for (int j = 0; j < data.weightcount(); j++)
			{
				data.originalWeights[j].rdWeight = Integer.MIN_VALUE;
			}
			return false;
		}
		else if (quote && accuracy < 0)
		{
			lm.setErrorCode(LibMessenger.BASE, accuracy, "");
			for (int j = 0; j < data.weightcount(); j++)
				data.originalWeights[j].rdWeight = Integer.MIN_VALUE;
			return false;
		}
		else
		{
			return true;
		}
	}

	/** Vorbereitung des Vektor mit den Ausgabezeilen, d.h. Namen und Stimmen
	 * werden in vOutput eingetragen. */
	private void prepareOutput(boolean sub)
	{
		int numberOfWeights = data.weightcount();

		// Namen
		String s1[] = new String[numberOfWeights + 3];
		s1[0] = of.labelNames;
		for (int n = 0; n < numberOfWeights; n++)
		{
			s1[n + 1] = "\"" + data.originalWeights[n].name + "\"";
		}
		s1[numberOfWeights + 1] = of.labelTotal;
		if (of.divisor == OutputFormat.DIV_QUOTIENT)
			s1[numberOfWeights + 1] += String.format(" (%s)", MethodData.getLabel(of, sub));

		s1[numberOfWeights + 2] = MethodData.getLabel(of, sub);
		vOutput.addElement(s1);
		// printStrings(s1);

		// Gewichte
		String s2[] = new String[numberOfWeights + 3];
		s2[0] = of.labelWeights;
		for (int n = 0; n < numberOfWeights; n++)
		{
			double rounded = Rounding.round(data.originalWeights[n].weight, of.decimal_places);
			s2[n + 1] = Convert.doubleToString(rounded);
		}
		double rounded = Rounding.round(data.totalWeight, of.decimal_places);
		s2[numberOfWeights + 1] = Convert.doubleToString(rounded);
		s2[numberOfWeights + 2] = "";
		vOutput.addElement(s2);
		// printStrings(s2);

		// Bedingungen
		if (of.condition == OutputFormat.CONDITION_MIN
				|| of.condition == OutputFormat.CONDITION_MIN_PLUS
				|| of.condition == OutputFormat.CONDITION_MIN_VPV)
		{
			int minsum = 0;
			String s3[] = new String[numberOfWeights + 3];
			s3[0] = of.labelCondition;

			for (int n = 0; n < numberOfWeights; n++)
			{
				s3[n + 1] = data.originalWeights[n].min + "";
				minsum += data.originalWeights[n].min;
			}
			// s3[numberOfWeights + 1] = data.totalCondition + "";
			s3[numberOfWeights + 1] = minsum + "";
			s3[numberOfWeights + 2] = "";
			vOutput.addElement(s3);
		}
		else if (of.condition == OutputFormat.CONDITION_MAX)
		{
			int maxsum = 0;
			String s3[] = new String[numberOfWeights + 3];
			s3[0] = of.labelCondition;
			for (int n = 0; n < numberOfWeights; n++)
			{
				if (data.originalWeights[n].max == Integer.MAX_VALUE)
				{
					s3[n + 1] = "oo";
					maxsum = Integer.MAX_VALUE;
				}
				else
				{
					s3[n + 1] = data.originalWeights[n].max + "";
					if (maxsum < Integer.MAX_VALUE)
						maxsum += data.originalWeights[n].max;
				}
			}
			if (maxsum < Integer.MAX_VALUE)
				s3[numberOfWeights + 1] = maxsum + "";
			else
				s3[numberOfWeights + 1] = "oo";

			s3[numberOfWeights + 2] = "";
			vOutput.addElement(s3);
		}
		else if (of.condition == OutputFormat.CONDITION_MIN_TO_MAX)
		{
			int minsum = 0;
			int maxsum = 0;
			String s3[] = new String[numberOfWeights + 3];
			s3[0] = of.labelCondition;

			for (int n = 0; n < numberOfWeights; n++)
			{
				minsum += data.originalWeights[n].min;
				s3[n + 1] = data.originalWeights[n].min + RoundFrame.RANGE_SEPERATOR;
				if (data.originalWeights[n].max == Integer.MAX_VALUE)
				{
					s3[n + 1] += "oo";
					maxsum = Integer.MAX_VALUE;
				}
				else
				{
					s3[n + 1] += data.originalWeights[n].max + "";
					if (maxsum < Integer.MAX_VALUE)
						maxsum += data.originalWeights[n].max;
				}
			}
			if (maxsum < Integer.MAX_VALUE)
				s3[numberOfWeights + 1] = minsum + RoundFrame.RANGE_SEPERATOR + maxsum;
			else
				s3[numberOfWeights + 1] = minsum + RoundFrame.RANGE_SEPERATOR + "oo";
			s3[numberOfWeights + 2] = "";
			vOutput.addElement(s3);
		}
		else if (of.condition == OutputFormat.CONDITION_DIRECT)
		{
			int sum = 0;
			String s3[] = new String[numberOfWeights + 3];
			s3[0] = Resource.getString("bazi.gui.table.direct");
			for (int n = 0; n < numberOfWeights; n++)
			{
				sum += data.originalWeights[n].direct;
				s3[n + 1] = Integer.toString(data.originalWeights[n].direct);
			}
			s3[numberOfWeights + 1] = Integer.toString(sum);
			s3[numberOfWeights + 2] = "";
			vOutput.addElement(s3);
		}
	}

	/** Erstellt die Ausgabe, für das aktuelle Ergebnis der Berechnung
	 * 
	 * @param name Bezeichnung der Zuteilungsmethode
	 * @param total Mandatszahl */
	private void putOutput(int acc, int met, MethodData method, String name, int total)
	{
		int numberOfWeights = data.originalWeights.length;
		double[] powers;
		if (data.powers == null || data.powers[acc] == null || data.powers[acc][met] == null || data.powers[acc][met].length == 0)
			powers = new double[] { 1 };
		else
			powers = data.powers[acc][met];
		
		for (int pow = 0; pow < data.weights[acc][met].length; pow++)
		{
			divisor = divs[pow];
			
			// wb: Start
//			data.ml.printMessage("putOutput: Divisor"+divisor.getDivisor());
//			data.ml.printMessage("\n--------------\n");
			// wb: Ende
			
			boolean multiple = false;
			for (int i = 0; i < data.weightcount(); i++)
			{
				if (data.weights[acc][met][pow][i].getFlag() != 0)
				{
					multiple = true;
					break;
				}
			}
			Vector<Weight[]> v = new Vector<Weight[]>();
			if (multiple)
			{
				// Vielfachheiten vorhanden
				if (of.ties == OutputFormat.TIES_CODED)
				{
					// nur codiert
					v.add(data.weights[acc][met][pow]);
				}
				else if ((of.ties == OutputFormat.TIES_LAC) ||
						(of.ties == OutputFormat.TIES_LIST))
				{
					// Liste
					v = Permutations.getMonopropPermutations(data.weights[acc][met][pow]);
				}

				if (of.ties == OutputFormat.TIES_LIST)
				{
					printMult = false;
				}
				else
				{
					printMult = true;
				}
			}
			else
			{
				// keine Vielfachheiten, einfach ausgeben
				v.add(data.weights[acc][met][pow]);
				printMult = false;
			}


			if (data.pow)
			{
				String[] s3 = new String[numberOfWeights + 3];
				s3[0] = "Pop^" + data.powers[acc][met][pow];
				if (powers.length > 1)
					s3[0] += "#" + (pow + 1);
				for (int i = 0; i < numberOfWeights; i++)
				{
					DecimalFormat df = new DecimalFormat("#.0", new DecimalFormatSymbols(Locale.ENGLISH));
					s3[i + 1] = df.format(data.weights[acc][met][pow][i].weight);
				}
				s3[numberOfWeights + 1] = "";
				s3[numberOfWeights + 2] = "";
				vOutput.add(s3);
			}
			if (!lm.getError() && of.divisor == OutputFormat.DIV_QUOTIENT)
			{
				if (method.method == MethodData.QUOTA)
					vOutput.addElement(Calculation.getQuotientenspalteOR(data.weights[acc][met][pow], divisor.getUnroundedQuota(), method, data));
				else
					vOutput.addElement(Calculation.getQuotientenspalte(data.weights[acc][met][pow], divisor.getDivisor(), method, data));
			}
			for (int i = 0; i < v.size(); i++)
			{
				Weight[] wa = v.elementAt(i);
				// wa in Ausgabe einfügen...
				String name2;
				if (of.ties == OutputFormat.TIES_CODED)
				{
					name2 = name;
				}
				else
				{
					if (v.size() == 1)
					{
						name2 = name;
					}
					else
					{
						name2 = name + "#" + (i + 1);
					}
				}
				putOutput2(name2, total, wa, method, (pow==0));
			}
		}
	}

	/** Einfügen einer berechneten Lösung in den vOutput.
	 * 
	 * @param name Bezeichnung der Zuteilungsmethode
	 * @param total Mandatszahl
	 * @param wa Berechnete Zuteilung 
	 * @param isFirstPow gibt an, ob putOutput2 fuer erste Power der Methode aufgerufen wurde
	 *        (nur relevant für Base+Min..Max(Pwr)-Zuteilung) */
	private void putOutput2(String name, int total, Weight[] wa, MethodData method, Boolean isFirstPow)
	{
		int numberOfWeights = wa.length;

		// Da die Basis-Mandate für die Berechnung entfernt wurden
		// werden sie nun wieder zur Summe der Sitze addiert
		int sum_base = wa.length * data.base;
		total += sum_base;

		String s[] = new String[numberOfWeights + 3]; // Zuteilung
		String s2[] = new String[numberOfWeights + 3]; // Nebenbedingung
		int sumOver = 0;
		int sumDisk = 0;
		s[0] = name;

		boolean methEx = lm.getError();
		if (messenger.isDroopError())
			methEx = false;

		// 17.03.2002 by Flo
		// Aus der Schleife herausgenommen, damit s2[0] auf alle Fälle belegt ist
		// -> keine Probleme mehr mit Droop (N/A)
		// Überhangmandate und naive Zuteilung
		if (of.condition == OutputFormat.CONDITION_DIRECT)
			s2[0] = of.labelCondition;
		if (of.condition == OutputFormat.CONDITION_NAIV)
			s2[0] = of.labelCondition + "-" + name;

		// war die Restzuteilung bei Hamilton erfuellbar
		// und war die Mindestbedingungen mit den Mandatszahlen erfuellbar
		/* if (!messenger.getNoMinimumConditionForRemain() &&
		 * !messenger.getNoSolutionForMinimum()) { */
		if (!messenger.isMinError() && !(lm.getErrorCode() == LibMessenger.QUOTA)
				&& lm.getErrorCode() != LibMessenger.MIN_SIGNPOST)
		{

			for (int n = 0; n < numberOfWeights; n++)
			{
				data.save[nme][n] = wa[n].clonew();
				// if (!messenger.getDroopRemainder()) {
				if (!messenger.isDroopError())
				{
					s[n + 1] = (methEx ? "NA" :
							(wa[n].rdWeight + data.base) + (printMult ? wa[n].multiple : ""));
					if (of.condition == OutputFormat.CONDITION_DIRECT && wa[n].direct - wa[n].rdWeight > 0)
						s[n + 1] += "*";
					
					if (of.condition == OutputFormat.CONDITION_MIN_PLUS) 
					{
						/* Einen Stern auch nach der zugeteilten Sitzzahl zeichnen,
						 * wenn ein Stern nach dem Quotienten gezeichnet wird bzw. würde
						 * */ 
						boolean WTA = Calculation.isWTA(method);
						boolean gR1 = !WTA && Calculation.isGR1(method);
						
						double quota = (method.method==MethodData.QUOTA)?divisor.getUnroundedQuota():divisor.getDivisor();
						
						RString nice = new RString(Calculation.getSplitString(wa, quota, method));
						RString seats = new RString(wa[n].weight/quota);
						int nicedigits = Calculation.niceDigits(seats, nice);
						seats = seats.round(nicedigits);
						s[n + 1] += (Calculation.isPragmaticModifcation(wa[n].min, new RString(wa[n].max), nicedigits, 
								                                        gR1, seats, nice)?"*":"");
						
					}	
				}
				else
				{
					s[n + 1] = "NA";
				}
				// Überhangmandate
				if (of.condition == OutputFormat.CONDITION_DIRECT)
				{
					if (!methEx)
					{
						int over = wa[n].direct - wa[n].rdWeight;
						if (over < 0)
						{
							over = 0;
						}
						s2[n + 1] = over + "";
						sumOver += over;
					}
					else
					{
						s2[n + 1] = "NA";
					}
				}
				// naive Zuteilung
				else if (of.condition == OutputFormat.CONDITION_NAIV)
				{
					if (!methEx)
					{
						int disk = wa[n].naiv - wa[n].rdWeight - data.base;
						s2[n + 1] = disk + "";
						sumDisk += disk;
					}
					else
					{
						s2[n + 1] = "NA";
					}
				}
			}
			// messenger.setDroopRemainder(false);

			s[numberOfWeights + 1] = (methEx ? "NA" : (total + ""));

			if (of.condition == OutputFormat.CONDITION_DIRECT)
			{
				s2[numberOfWeights + 1] = sumOver + "";
			}
			else if (of.condition == OutputFormat.CONDITION_NAIV)
			{
				s2[numberOfWeights + 1] = sumDisk + "";
			}
			if (methEx)
			{
				s2[numberOfWeights + 1] = "NA";
			}

			// System.out.println("cpO " + name + " " + total);

			// Divisor-Ausgabe;
			if (methEx)
			{
				s[numberOfWeights + 2] = "NA";
			}
			else if (messenger.isDroopError())
			{
				s[numberOfWeights + 2] = "NA";
			}
			else
			{
				if (!divisor.isNeeded())
				{
					s[numberOfWeights + 2] = "---";
				}
				else if (divisor.isEnabled())
				{
					// System.out.println("Adding divisor to output");
					if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA)
					{
						s[numberOfWeights + 2] = Convert.doubleToString(divisor.getDivisor());
						// System.out.println("writing divisor " + divisor.getDivisor());
					}
					else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
					{
						s[numberOfWeights + 2] = "[" +
								Convert.doubleToString(divisor.getDivisorLow()) +
								";"
								+ Convert.doubleToString(divisor.getDivisorHigh()) + "]";
					}
					else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
					{
						s[numberOfWeights + 2] = Convert.doubleToString(divisor.getMultiplier());
					}
					else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
					{
						s[numberOfWeights + 2] = "[" +
								Convert.doubleToString(divisor.getMultiplierLow()) + ";"
								+ Convert.doubleToString(divisor.getMultiplierHigh()) + "]";
					}
					else if (of.divisor == OutputFormat.DIV_QUOTIENT)
					{
						s[numberOfWeights + 2] = "";
					}
				}
				else
				{
					if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA)
						s[numberOfWeights + 2] = Calculation.getSplitString(wa, divisor.getUnroundedQuota(), method);
					else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
						s[numberOfWeights + 2] = Calculation.getSplitIntervalString(wa, divisor.getUnroundedQuota(), method);

					else if (of.divisor == OutputFormat.DIV_QUOTIENT)
						s[numberOfWeights + 2] = "";
					else
					{
						s[numberOfWeights + 2] = "---";
					}
				}
				// Divisor sichern und *neuen* erstellen (nur bei erster power)
				/* System.out.print("nme: " + nme);
				 * System.out.println(" len: " + data.divSave.length); */
				if (isFirstPow)
					data.divSave[nme] = divisor.cloneD();			
				// wb: Start
//				data.ml.printMessage("putOutput2: divisor.getDivisor() = "+divisor.getDivisor());
//				data.ml.printMessage("putOutput2: data.divSave[nme] = "+data.divSave[nme].getDivisor());
//				data.ml.printMessage("\n--------------\n");
				// wb: Ende
				
			}
			if (methEx)
			{
				s[numberOfWeights + 2] = "NA";
			}
		}
		// falls die Restzuteilung bei den Quotenmethoden
		// oder Mindestbedingung bei den Mandatszahlen nicht erfüllbar war
		else
		{
			for (int n = 0; n < numberOfWeights + 2; n++)
			{
				s[n + 1] = "NA";
				s2[n + 1] = "NA";
			}
			// Bei Integer.MIN_VALUE "NA" ausgeben
			if (total > Integer.MIN_VALUE)
				s[numberOfWeights + 1] = total + "";
			else
				s[numberOfWeights + 1] = "NA";

			/* messenger.setNoMinimumConditionForRemain(false);
			 * messenger.setNoSolutionForMinimum(false); */
			// errorMessages += messenger.getLibError() + "\n";
			// Sichern der Daten für separate Auswertung
			for (int n = 0; n < numberOfWeights; n++)
			{
				data.save[nme][n] = wa[n].clonew();
			}
			data.divSave[nme] = divisor.cloneD();

			if (of.divisor == OutputFormat.DIV_QUOTIENT)
			{
				s[numberOfWeights + 2] = "";
				s2[numberOfWeights + 2] = "";
				String[] quotientenspalte = new String[wa.length + 3];
				quotientenspalte[0] = "Quotient";
				for (int i = 1; i < quotientenspalte.length - 2; i++)
					quotientenspalte[i] = "NA";
				quotientenspalte[quotientenspalte.length - 2] = "(NA)";
				quotientenspalte[quotientenspalte.length - 1] = "";
				vOutput.add(quotientenspalte);
			}
		}

		// Datenfelder in den Vector einfuegen
		vOutput.addElement(s);
		if (of.condition == OutputFormat.CONDITION_DIRECT ||
				of.condition == OutputFormat.CONDITION_NAIV)
		{
			s2[numberOfWeights + 2] = "";
			vOutput.addElement(s2);
		}

		// System.out.println("Putting output");

		if (!messenger.getLibError().equals(""))
			errorMessages += Calculation.COMMENT + messenger.getLibError() + "\n";
		messenger.refresh();
	} // putOutput

	/** Formatiert die gesamte Lösung, d.h. die Ausrichtung wird festgelegt und die Spaltenbreite wird angepasst. */
	private String writeSolution()
	{
		int numberOfLines = data.weightcount() + 3;
		if (of.divisor == OutputFormat.DIV_QUOTIENT)
			numberOfLines--;

		StringBuffer sOutput;
		if (data.title.equals(""))
		{
			sOutput = new StringBuffer("\n");
		}
		else
		{
			if (data instanceof ListInputData)
			{
				sOutput = new StringBuffer("\n" + Calculation.COMMENT + "" +
						Calculation.COMMENT + data.title +
						"\n");
			}
			else
			{
				sOutput = new StringBuffer(Calculation.COMMENT + data.title + "\n");
			}
		}

		// Hat eine Partei die absolute Mehrheit?
		if (messenger.getAbsoluteMessage())
		{
			sOutput.append(messenger.getAbsoluteMessage(of.labelWeights, of.decimal_places));

		}
		if (of.alignment == OutputFormat.ALIGN_HORIZONTAL || of.alignment == OutputFormat.ALIGN_HORI_VERT)
		{
			// String mit maximaler Länge in jeder Spalte suchen
			// Maximum: 16
			int columnLength[] = new int[numberOfLines];
			for (int m = 0; m < numberOfLines; m++)
			{
				columnLength[m] = 0;
			}
			for (int i = 0; i < vOutput.size(); i++)
			{
				String s[] = vOutput.elementAt(i);
				for (int k = 0; k < numberOfLines; k++)
				{
					int temp = s[k].length();
					if (temp > columnLength[k])
					{
						columnLength[k] = temp;
					}
				}
			}

			// jeder Vectoreintrag ist eine Zeile der Ausgabe
			for (int i = 0; i < vOutput.size(); i++)
			{
				String s[] = vOutput.elementAt(i);
				for (int k = 0; k < numberOfLines; k++)
				{
					s[k] = s[k].replace("\t", " ");
					int temp = s[k].length();
					if (k == 0)
					{
						sOutput.append(s[k]);
					}
					for (int n = 0; n < (columnLength[k] - temp); n++)
					{
						sOutput.append(" ");
					}
					if (k != 0)
					{
						// if (s[k].charAt(1)=='=') sOutput.append("  " + s[k]);
						// else
						// sOutput.append("  " + ((s[k].length())<16?s[k]:s[k].substring(0,15)+"...")); // ?: 02.2002 by Flo
						sOutput.append("  " + s[k]);
					}
				}
				sOutput.append(" \n");
			}
		}
		else
		{ // Vertikale Ausgabe
			// String mit maximaler Länge in jeder Spalte suchen
			int columnLength[] = new int[vOutput.size()];
			for (int m = 0; m < vOutput.size(); m++)
			{
				columnLength[m] = 0;
			}
			for (int i = 0; i < vOutput.size(); i++)
			{
				String s[] = vOutput.elementAt(i);
				for (int k = 0; k < numberOfLines; k++)
				{
					s[k] = s[k].replace("\t", " ");
					// int temp = ((s[k].length()<16)?s[k].length():19); // ?: 02.2002 by Flo
					int temp = 0;
					try
					{
						temp = s[k].length();
					}
					catch (NullPointerException npe)
					{
						System.out.println(s[k - 1] + " : " + s[k]);
					}
					if (temp > columnLength[i])
					{
						columnLength[i] = temp;
					}
				}
			}

			// jeder Vectoreintrag ist eine Spalte der Ausgabe
			// System.out.println("foreach Vector Entry");
			for (int k = 0; k < numberOfLines; k++)
			{
				for (int i = 0; i < vOutput.size(); i++)
				{
					String s[] = vOutput.elementAt(i);
					// int temp = ((s[k].length()<16)?s[k].length():18); // ?: 02.2002 by Flo
					// System.out.print(s[k] + "; ");
					int temp = s[k].length();
					// if (i == 0) sOutput.append(((s[k].length())<16?s[k]:s[k].substring(0,15)+"...")); // ?: 02.2002 by Flo
					if (i == 0)
					{
						sOutput.append(s[k]);
					}
					for (int n = 0; n < (columnLength[i] - temp); n++)
					{
						sOutput.append(" ");
					}
					if (i != 0)
					{
						// if (s[k].charAt(1)=='=') sOutput.append("  " + s[k]);
						// else
						// sOutput.append("  " + ((s[k].length())<16?s[k]:s[k].substring(0,15)+"...")); // ?: 02.2002 by Flo
						sOutput.append("  " + s[k]);
					}
				}
				// System.out.println();
				sOutput.append(" \n");
			}
		}
		//
		// // Sind die Mindestbedingungen aus der Mandatszahl erfuellbar?
		// if (messenger.minimumMessage()) {
		// sOutput.append(messenger.getMinimumMessage());

		// War die Mindestbed. aus Restzuteilung bei Hamilton erfüllbar?
		// }
		// if (messenger.remainMessage()) {
		// sOutput.append(messenger.getRemainMessage());

		// Probleme bei Droop?
		// }
		// if (messenger.droopMessage()) {
		// sOutput.append(messenger.getDroopMessage());

		// }

		sOutput.append(errorMessages);

		sOutput.append(messenger.getGlobalErrors());

		return sOutput.toString();
	}

}
