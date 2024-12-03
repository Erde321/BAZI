/*
 * @(#)CalculationSeparate.java 2.2 07/05/14
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.util.Vector;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.gui.RoundFrame;
import de.uni.augsburg.bazi.lib.Convert;
import de.uni.augsburg.bazi.lib.Rounding;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> CalculationSeparate<br>
 * <b>Description:</b> Berechnet die Zuteilung in separaten Wahldistrikten<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.2
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */
public class CalculationSeparate
{

	/** Eingabedaten mit mehreren Distrikten */
	private final DistrictInputData did;

	/** Anzahl der Distrikte */
	private int numberOfDistricts = -1;

	/** Anzahl der Parteien */
	private int numberOfParties = -1;

	/** Anzahl der Methoden */
	public static int numberOfMethods = -1;

	/** Alle Methoden, die benutzt werden sollen */
	private final MethodData[] md;

	/** Ausgabeformat */
	private final OutputFormat of;

	/** Summierte Parteistimmen über alle Distrikte */
	private WeightsCollection myWeights = null;

	private final boolean isCombi = false;
	
	/** Erstellt ein neues Berechnungsobjekt
	 * 
	 * @param d Die Daten der zu berechnenden Distrikte */
	public CalculationSeparate(DistrictInputData d)
	{
		did = d;
		numberOfDistricts = did.data.length;
		of = d.outputFormat;
		md = of.methods;
		numberOfMethods = md.length;
	}
	

	/** Start der Berrechnungen.
	 * 
	 * @return Alle Ergebnisse als formatierter String */
	public String start()
	{
		StringBuffer output = new StringBuffer();
		// boolean valid = true;
		// Monoproportionale Zuteilungen berechnen
		for (int i = 0; i < did.data.length; i++)
		{
			output.append(new CalculationMonoprop(did.data[i]).start());
			// for (int k = 0; k < did.data[i].apValid.length; k++)
			// valid &= did.data[i].apValid[k];
			// Listen
			for (int j = 0; j < did.data[i].listData.length; j++)
			{
				output.append(new CalculationMonoprop(did.data[i].listData[j]).start());
				// ListInputData ld = did.data[i].listData[j];
				// System.out.println("apValid: " + ld.apValid);
				// for (int k = 0; k < did.data[i].listData[j].apValid.length; k++)
				// {
				// valid &= did.data[i].listData[j].apValid[k];
				// }
			}
			output.append("\n\n");
			// System.out.println("valid: " + valid);
		}

		sumParties();
		// fasst die Parteien zu Fraktionen zusammen (z.B. Fraktionen im Europaparlament)
		myWeights.merge();

		for (int m = 0; m < numberOfMethods; m++)
		{
			output.append(createOverview(m) + "\n");
			if (isCombi)
			{
				output.append("\n" + Calculation.COMMENT + Resource.getString("bazi.gui.seperate.combi") +
						"\n");
			}
		}

		return output.toString();
	}
	
	/** Berechnungen für MinPlus
	 * 
	 * @return Alle Ergebnisse als formatierter String */
	public String getCalc() {
		String output = "";
		sumParties();
		for (int m = 0; m < numberOfMethods; m++)
			output += createOverview(m) + "\n";
		return output;
	}

	/** Summiert die Parteisitze über alle Distrikte auf */
	public void sumParties()
	{
		InputData[] data = did.data;

		// Jetzt die Summe
		int numberOfMethods = of.methods.length;
		int numberOfDistricts = data.length;
		myWeights = new WeightsCollection(numberOfDistricts, numberOfMethods);
		DistrictInputData did = new DistrictInputData();
		did.data = new InputData[1];
		// Zuerst die Parteinamen
		// Jedes Tab bearbeiten
		for (int i = 0; i < numberOfDistricts; i++)
		{
			// und dort jede Partei
			// int numberOfWeights = data[i].weightcount();
			for (int j = 0; j < data[i].weightcount(); j++)
			{
				String party = data[i].weights[0][0][0][j].name;
				// System.out.println("Partei: " + partei + " +: " + partei.indexOf("+"));
				// falls keine Listenverbindung
				if (!data[i].weights[0][0][0][j].parent)
				{
					double votes = data[i].weights[0][0][0][j].weight;
					// noch die Zuteilung
					// int[] acctmp = new int[numberOfMethods];
					Weight[] wtmp = new Weight[numberOfMethods];
					for (int k = 0; k < numberOfMethods; k++)
					{
						wtmp[k] = data[i].save[k][j];
					}
					// System.out.println("numberOfMethods: " + numberOfMethods);
					// sComp.put(partei, votes, acctmp);
					// System.out.println("party: " + party + " i: " + i + " votes: " + votes + " wtmp: " + wtmp);
					myWeights.putParty(party, i, votes, wtmp);
				}
			}
			// Jetzt noch die Listenverbindungen auflösen
			if (data[i].listData.length != 0)
			{
				// für jede Liste...
				for (int m = 0; m < data[i].listData.length; m++)
				{
					InputData idh = data[i].listData[m];
					// int numberOfWeightsLv = idh.weightcount();
					for (int j = 0; j < idh.weightcount(); j++)
					{
						String party = idh.weights[0][0][0][j].name;
						double votes = idh.weights[0][0][0][j].weight;
						// noch die Zuteilung
						// int[] acctmp = new int[numberOfMethods];
						Weight[] wtmp = new Weight[numberOfMethods];
						for (int k = 0; k < numberOfMethods; k++)
						{
							// System.out.print(k + " ");
							wtmp[k] = idh.save[k][j];
						}
						// sComp.put(partei, votes, acctmp);
						myWeights.putParty(party, i, votes, wtmp);
					}
				}
			}
		}

		myWeights.sort();
		numberOfParties = myWeights.getPartyCount();

	} // sumParties
	/** Erstellt eine Übersicht über alle Parteien und Distrikte
	 * wobei auch Nebenbedingungen angezeigt werden
	 * 
	 * @return die komplette Übersicht über alle Parteien und Distrikte als
	 *         formatierten String */
	public String createOverview(int method)
	{
		Vector<String[]> v = new Vector<String[]>();

		// für jede Methode prüfen, ob die Zuteilung gültig/erfolgreich war
		boolean valid = true;
		for (int i = 0; i < did.data.length; i++)
		{
			valid &= did.data[i].apValid[method];
		}

		boolean vsum = valid;

		/* Nun muß die Ausgabetabelle erstellt werden. Ziel:
		 * "Partei1" "Method" ["$Option"] .... Wkr.-Gr. "Divisor"
		 * "Method1" "Method1"....
		 * "District1"
		 * "District2"
		 * ...
		 * Summe StimmenS MandateS OptionS ..... did.data.accuracy * */

		int colLength = 0;

		// Horizontal
		if (of.alignment == OutputFormat.ALIGN_HORIZONTAL ||
				of.alignment == OutputFormat.ALIGN_VERT_HORI)
		{
			// Ausgabe Spaltenweise aufbauen
			// Länge eines Spalten-Arrays
			colLength = 3 + numberOfDistricts;

			// Spalte mit den Distriktnamen
			String[] s = new String[colLength];
			s[0] = "";
			s[1] = "";
			for (int i = 0; i < numberOfDistricts; i++)
				s[i + 2] = "\"" + did.data[i].district + "\"";
			
			s[colLength - 1] = of.labelTotal;
			v.add(s);

			// Distriktgrößen
			int[] overhangs = new int[numberOfDistricts];
			for (int i = 0; i < numberOfDistricts; i++)
				overhangs[i] = 0;
			
			String[] parties = myWeights.getPartyNames();
			numberOfParties = myWeights.getPartyCount();
			double[] sumVotesDistrict = new double[numberOfDistricts];
			
			// Jetzt für jede Partei...
			for (int i = 0; i < numberOfParties; i++)
			{
				// Zuerst Parteiname und Voten
				s = new String[colLength];
				s[0] = "\"" + parties[i] + "\"";
				s[1] = "";
				double dsum = 0;
				double[] votes = myWeights.getVotes(parties[i]);
				for (int j = 0; j < numberOfDistricts; j++)
				{
					double rounded = Rounding.round(votes[j], of.decimal_places);
					s[j + 2] = Convert.doubleToString(rounded);
					sumVotesDistrict[j] += votes[j];
					dsum += votes[j];
				}
				double rounded = Rounding.round(dsum, of.decimal_places);
				s[colLength - 1] = Convert.doubleToString(rounded);
				v.add(s);

				if (of.condition == OutputFormat.CONDITION_MIN)
				{
					
					int minsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for (int n = 0; n < numberOfDistricts; n++)
					{
						Weight w = myWeights.getWeight(parties[i], n, 0);
						s[n + 2] = w.min + "";
						minsum += w.min;
					}
					s[colLength - 1] = minsum + "";
					v.add(s);
				 }
				else if (of.condition == OutputFormat.CONDITION_MAX)
				{
					int maxsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for (int n = 0; n < numberOfDistricts; n++)
					{
						Weight w = myWeights.getWeight(parties[i], n, 0);
						if (w.max != Integer.MAX_VALUE)
						{
							s[n + 2] = w.max + "";
							if (maxsum != Integer.MAX_VALUE)
							{
								maxsum += w.max;
							}
						}
						else
						{
							s[n + 2] = "oo";
							maxsum = Integer.MAX_VALUE;
						}
					}
					if (maxsum < Integer.MAX_VALUE)
					{
						s[colLength - 1] = maxsum + "";
					}
					else
					{
						s[colLength - 1] = "oo";
					}
					v.add(s);
				}
				else if (of.condition == OutputFormat.CONDITION_MIN_TO_MAX)
				{
					int minsum = 0;
					int maxsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for (int n = 0; n < numberOfDistricts; n++)
					{
						Weight w = myWeights.getWeight(parties[i], n, 0);
						if (w.max != Integer.MAX_VALUE)
						{
							s[n + 2] = w.min + RoundFrame.RANGE_SEPERATOR + w.max;
							if (maxsum != Integer.MAX_VALUE)
							{
								maxsum += w.max;
							}
						}
						else
						{
							s[n + 2] = w.min + RoundFrame.RANGE_SEPERATOR + "oo";
							maxsum = Integer.MAX_VALUE;
						}
						minsum += w.min;
					}
					if (maxsum < Integer.MAX_VALUE)
					{
						s[colLength - 1] = minsum + RoundFrame.RANGE_SEPERATOR + maxsum;
					}
					else
					{
						s[colLength - 1] = minsum + RoundFrame.RANGE_SEPERATOR + "oo";
					}
					v.add(s);
				}
				else if (of.condition == OutputFormat.CONDITION_MIN_PLUS)
				{
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					//v.add(s);
				}
				// Jetzt für jede Methode...
				s = new String[colLength];
				String[] s2 = new String[colLength];
				s[0] = md[method].name;
				s[1] = "";

				// Überhangmandate und naive Zuteilung
				if (of.condition == OutputFormat.CONDITION_DIRECT)
					s2[0] = of.labelCondition;
				if (of.condition == OutputFormat.CONDITION_NAIV)
					s2[0] = of.labelCondition + "-" + md[method].name;
				if(of.condition == OutputFormat.CONDITION_MIN_PLUS)
					s2[0] = of.labelCondition;

				s2[1] = "";

				int sum = 0;
				int sumOver = 0;
				int sumDisk = 0;
				for (int j = 0; j < numberOfDistricts; j++)
				{
					Weight w = myWeights.getWeight(parties[i], j, method);
					if (did.data[j].apValid[method])
					{
						s[j + 2] = w.rdWeight + w.multiple;
						if (of.condition == OutputFormat.CONDITION_DIRECT && w.direct - w.rdWeight > 0)
							s[j + 2] += "*";
					}
					else
					{
						s[j + 2] = "NA";
					}
					sum += w.rdWeight;

					// Überhangmandate
					if (of.condition == OutputFormat.CONDITION_DIRECT)
					{
						int over = w.direct - w.rdWeight;
						if (over < 0)
						{
							over = 0;
						}
						s2[j + 2] = over + "";
						sumOver += over;
						overhangs[j] += over;
					}
					// naive Zuteilung
					else if (of.condition == OutputFormat.CONDITION_NAIV)
					{
						int disk = w.naiv - w.rdWeight;
						s2[j + 2] = disk + "";
						sumDisk += disk;
					}
				}
				if (vsum)
				{
					s[colLength - 1] = Convert.doubleToString(sum);
					if (of.condition == OutputFormat.CONDITION_DIRECT && sumOver > 0)
						s[colLength - 1] += "*";
				}
				else
				{
					s[colLength - 1] = "NA";
				}
				v.add(s);

				if (of.condition == OutputFormat.CONDITION_DIRECT)
				{
					s2[colLength - 1] = sumOver + "";
				}
				else if (of.condition == OutputFormat.CONDITION_NAIV)
				{
					s2[colLength - 1] = sumDisk + "";
				}
				else if (of.condition == OutputFormat.CONDITION_MIN_PLUS)
				{
					for (int n = 0; n < numberOfDistricts; n++)
					{
						Weight w = myWeights.getWeight(parties[i], n, 0);
						s2[n + 2] = w.min + "";
						sumOver += w.min;
					}
					s2[colLength - 1] = sumOver + "";
					v.add(s2);
				}

			
				
				if (of.condition == OutputFormat.CONDITION_DIRECT ||
						of.condition == OutputFormat.CONDITION_NAIV)
				{
					v.add(s2);
				}

			}

			// Summe der Stimmen in den Distrikten
			s = new String[colLength];
			s[0] = of.labelTotal;
			s[1] = of.labelWeights;
			double sumVotes = 0;
			for (int i = 0; i < numberOfDistricts; i++)
			{
				double rounded = Rounding.round(sumVotesDistrict[i], of.decimal_places);
				s[i + 2] = Convert.doubleToString(rounded);
				sumVotes += sumVotesDistrict[i];
			}
			double rounded = Rounding.round(sumVotes, of.decimal_places);
			s[colLength - 1] = Convert.doubleToString(rounded);
			v.add(s);

			// Distriktgrößen
			s = new String[colLength];
			s[0] = of.labelTotal;
			s[1] = md[method].name;

			int sum = 0;
			for (int i = 0; i < numberOfDistricts; i++)
			{
				if (of.condition == OutputFormat.CONDITION_MIN_PLUS) {
					int anz = did.data[i].accuracies.length;
					s[i + 2] = String.valueOf(did.data[i].accuracies[anz - 1]);
					sum += did.data[i].accuracies[anz - 1];
				}
				else{
					s[i + 2] = String.valueOf(did.data[i].accuracies[0]);
					// hier werden am Schluß noch die Überhänge angehängt
					sum += did.data[i].accuracies[0];
				}
			}
			s[colLength - 1] = String.valueOf(sum);
			v.add(s);

			/** // zum Schluß die Divisoren
			 * s = new String[colLength];
			 * s[0] = of.labelDivisor;
			 * s[1] = "[" + md[method].name + "]";
			 * for (int i = 0; i < numberOfDistricts; i++)
			 * {
			 * String erg = did.data[i].apValid[method] ? prepareDivisor(did.data[i].divSave[method]) : "NA";
			 * if (erg.equals("Combi"))
			 * {
			 * isCombi = true;
			 * }
			 * s[i + 2] = erg;
			 * }
			 * s[colLength - 1] = "";
			 * v.add(s); */

			// Überhänge pro Distrikt
			if (of.condition == OutputFormat.CONDITION_DIRECT)
			{
				s = v.elementAt(v.size() - 1);
				int sumover = 0;
				for (int i = 0; i < numberOfDistricts; i++)
				{
					s[i + 2] += "+" + overhangs[i];
					sumover += overhangs[i];
				}
				s[colLength - 1] += "+" + sumover;
			}
		}
		// Vertikal
		else
		{
			// Länge eines Spalten-Arrays
			String[] parties = myWeights.getPartyNames();
			numberOfParties = myWeights.getPartyCount();
			colLength = 3 + numberOfParties;

			// Spalte mit den Parteinamen
			String[] s = new String[colLength];
			s[0] = "";
			s[1] = "";
			for (int i = 0; i < numberOfParties; i++)
			{
				s[i + 2] = "\"" + parties[i] + "\"";
			}
			s[colLength - 1] = of.labelTotal;
			// s[colLength - 1] = of.labelDivisor;

			v.add(s);

			// Distrikte
			double[][] votes = myWeights.getVotesPerParty();
			Weight[][][] weights = myWeights.getWeights();
			int sumover = 0; // Summe aller Überhange
			double sumvotes = 0; // Summe aller Stimmen
			// Array mit den Summen aller Überhangmandate bzw. Diskrepanzen der Parteien in allen Distrikten
			int[] appendix = new int[numberOfParties];
			for (int d = 0; d < numberOfDistricts; d++)
			{

				// Stimmen der Parteien
				s = new String[colLength];
				s[0] = "\"" + did.data[d].district + "\"";
				// s[1] = did.data[d].accuracy;
				s[1] = "";
				// Parteistimmen im Distrikt
				sumvotes = 0;
				for (int p = 0; p < numberOfParties; p++)
				{
					s[2 + p] = Convert.doubleToString(votes[p][d]);
					sumvotes += votes[p][d];
				}

				s[colLength - 1] = Convert.doubleToString(sumvotes);
				v.add(s);

				// Optionale Spalte
				if (of.condition == OutputFormat.CONDITION_MIN)
				{
					int minsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for (int p = 0; p < numberOfParties; p++)
					{
						if (weights[p][d][0] == null)
							s[p + 2] = "0";
						else
						{
							s[p + 2] = String.valueOf(weights[p][d][0].min);
							minsum += weights[p][d][0].min;
						}
					}
					s[colLength - 1] = String.valueOf(minsum);
					v.add(s);
				}
				else if (of.condition == OutputFormat.CONDITION_MAX)
				{
					int maxsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for (int p = 0; p < numberOfParties; p++)
					{
						if (weights[p][d][0] == null)
							s[p + 2] = "oo";
						else
						{
							if (weights[p][d][0].max < Integer.MAX_VALUE)
							{
								s[p + 2] = String.valueOf(weights[p][d][0].max);
								if (maxsum < Integer.MAX_VALUE)
								{
									maxsum += weights[p][d][0].max;
								}
							}
							else
							{
								s[p + 2] = "oo";
								maxsum = Integer.MAX_VALUE;
							}
						}
					}
					if (maxsum < Integer.MAX_VALUE)
					{
						s[colLength - 1] = String.valueOf(maxsum);
					}
					else
					{
						s[colLength - 1] = "oo";
					}
					v.add(s);
				}
				else if (of.condition == OutputFormat.CONDITION_MIN_TO_MAX)
				{
					int maxsum = 0;
					int minsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for (int p = 0; p < numberOfParties; p++)
					{
						if (weights[p][d][0] == null)
							s[p + 2] = "0..oo";
						else
						{
							if (weights[p][d][0].max < Integer.MAX_VALUE)
							{
								s[p + 2] = weights[p][d][0].min + RoundFrame.RANGE_SEPERATOR +
										String.valueOf(weights[p][d][0].max);
								if (maxsum < Integer.MAX_VALUE)
								{
									maxsum += weights[p][d][0].max;
								}
							}
							else
							{
								s[p + 2] = weights[p][d][0].min + RoundFrame.RANGE_SEPERATOR + "oo";
								maxsum = Integer.MAX_VALUE;
							}
							minsum += weights[p][d][0].min;
						}
					}
					if (maxsum < Integer.MAX_VALUE)
					{
						s[colLength - 1] = minsum + RoundFrame.RANGE_SEPERATOR + String.valueOf(maxsum);
					}
					else
					{
						s[colLength - 1] = minsum + RoundFrame.RANGE_SEPERATOR + "oo";
					}
					v.add(s);
				}
				else if(of.condition == OutputFormat.CONDITION_MIN_PLUS)
				{
					int minsum = 0;
					s = new String[colLength];
					s[0] = of.labelCondition;
					s[1] = "";
					for(int p = 0; p < numberOfParties; p++) 
					{
						s[p + 2] = String.valueOf(weights[p][d][0].min);
						minsum += weights[p][d][0].min;
					}
					s[colLength - 1] = String.valueOf(minsum);
					v.add(s);
				}

				int overhangs = 0;

				// Sitze für jede Methode
				s = new String[colLength];
				String[] s2 = new String[colLength];
				s[0] = md[method].name;

				s[1] = "";
				s2[0] = "";

				// Überhangmandate und naive Zuteilung
				if (of.condition == OutputFormat.CONDITION_DIRECT ||
						of.condition == OutputFormat.CONDITION_NAIV ||
						of.condition == OutputFormat.CONDITION_MIN_PLUS)
					s2[0] = of.labelCondition;
				
				s2[1] = "";

				int sum = 0;
				// int sumOver = 0;
				int sumDisk = 0;
				for (int p = 0; p < numberOfParties; p++)
				{
					Weight w = weights[p][d][method];
					if (w == null)
						w = new Weight();
					if (did.data[d].apValid[method])
					{
						s[p + 2] = w.rdWeight + w.multiple;
						if (of.condition == OutputFormat.CONDITION_DIRECT && w.direct - w.rdWeight > 0)
							s[p + 2] += "*";
						if(of.condition == OutputFormat.CONDITION_MIN_PLUS)
							sum += w.rdWeight;
					}
					else
					{
						s[p + 2] = "NA";
					}
					// sum += w.rdWeight;

					// Überhangmandate
					if (of.condition == OutputFormat.CONDITION_DIRECT)
					{
						int over = w.direct - w.rdWeight;
						if (over < 0)
						{
							over = 0;
						}
						s2[p + 2] = over + "";
						// sumOver += over;
						overhangs += over;
						appendix[p] += over;
					}
					// naive Zuteilung
					else if (of.condition == OutputFormat.CONDITION_NAIV)
					{
						int disk = w.naiv - w.rdWeight;
						s2[p + 2] = disk + "";
						sumDisk += disk;
						appendix[p] += disk;
					}
				}


				// if (vsum) {
				if (did.data[d].apValid[method])
				{
					if(of.condition == OutputFormat.CONDITION_MIN_PLUS)
						s[colLength - 1] = String.valueOf(sum);
					else {
						s[colLength - 1] = did.data[d].accuracy;
						if (of.condition == OutputFormat.CONDITION_DIRECT && overhangs > 0)
							s[colLength - 1] += "*";
					}
				}
				else
				{
					s[colLength - 1] = "NA";
				}
				v.add(s);


				s2[colLength - 1] = "";

				if (of.condition == OutputFormat.CONDITION_DIRECT ||
						of.condition == OutputFormat.CONDITION_NAIV)
				{
					v.add(s2);
				}

				// Überhänge
				if (of.condition == OutputFormat.CONDITION_DIRECT)
				{
					// s = v.elementAt(lastDist);
					s2[colLength - 1] = String.valueOf(overhangs);
					sumover += overhangs;
				}
				else if (of.condition == OutputFormat.CONDITION_NAIV)
				{
					s2[colLength - 1] = String.valueOf(sumDisk);
				}

			}

			// Summen
			// Parteistimmen
			s = new String[colLength];
			s[0] = of.labelTotal;
			s[1] = of.labelWeights;

			/* int accTotal = 0;
			 * for(int d=0;d<numberOfDistricts;d++)
			 * accTotal += did.data[d].accuracies[0];
			 * s[colLength - 2] = accTotal + "";
			 * if (of.condition == OutputFormat.CONDITION_DIRECT) {
			 * s[colLength - 2] += "+" + sumover;
			 * } */

			double psum = 0;
			double psumAll = 0;
			for (int p = 0; p < numberOfParties; p++)
			{
				psum = 0;
				for (int d = 0; d < numberOfDistricts; d++)
					psum += votes[p][d];
				psumAll += psum;
				s[2 + p] = Convert.doubleToString(psum);
			}

			s[colLength - 1] = Convert.doubleToString(psumAll);
			v.add(s);

			// Parteisitze
			int[][] sums = myWeights.getSums();

			s = new String[colLength];
			s[0] = of.labelTotal;
			s[1] = md[method].name;
			int sumSeats = 0;
			if (vsum && of.condition == OutputFormat.CONDITION_DIRECT)
				for (int p = 0; p < numberOfParties; p++)
				{
					s[2 + p] = sums[p][method] + "+" + appendix[p];
					sumSeats += sums[p][method];
				}
			else if (vsum)
				for (int p = 0; p < numberOfParties; p++)
				{
					s[2 + p] = String.valueOf(sums[p][method]);
					sumSeats += sums[p][method];
				}
			else
				for (int p = 0; p < numberOfParties; p++)
					s[2 + p] = "NA";

			if (vsum)
			{
				s[colLength - 1] = String.valueOf(sumSeats);
				if (of.condition == OutputFormat.CONDITION_DIRECT)
					s[colLength - 1] += "+" + sumover;
			}
			else
				s[colLength - 1] = "NA";

			v.add(s);

		}
		String headline;
		String temp;
		temp = "[" + md[method].name + "]";
		headline = Resource.getString("bazi.gui.district.head1") + " " +
				did.data.length + " " + Resource.getString("bazi.gui.district.head2") +
				temp;
		String output = "";
		if (of.condition == OutputFormat.CONDITION_MIN_PLUS)
			output += "\n";
		output += Calculation.COMMENT + headline + "\n" +(Calculation.writeSolution(v, colLength - 1,OutputFormat.ALIGN_VERTICAL));
		return output;
	} // createOverview
}
