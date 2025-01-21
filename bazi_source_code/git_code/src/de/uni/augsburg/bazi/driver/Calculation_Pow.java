package de.uni.augsburg.bazi.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.Signpost;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse Calculation_Pow<br/>
 * <b>Description:</b> Bereitet Input Daten für base+prop(pow) vor, berechnet die
 * Potenzen und startet schließlich die Berechnungen mit den
 * gewichteten Bevölkerungszahlen<br/>
 * <b>Copyright:</b> Copyright (c) 2000-2011<br/>
 * <b>Company:</b> Universität Augsburg<br/>
 * 
 * @version 2011.08
 * @author Marco Schumacher */
public class Calculation_Pow
{
	public int iterations = 1000;
	boolean verbose = false;

	/** Berechnet Zuteilungen nach base+prop(pow)
	 * 
	 * @param aid Eingabedaten
	 * @return Ausgabe als String */
	public String calculate(AbstractInputData aid)
	{
		if (!(aid instanceof InputData))
			return "Noch nicht implementiert...";

		// //////////////////////////////////////////////////////////////////
		// Daten vorbereiten: Eine Permutation festlegen,
		// sodas die meisten Stimmen zuerst kommen
		// //////////////////////////////////////////////////////////////////
		final InputData id = ((InputData) aid);

		Integer[] perm = new Integer[id.weightcount()];
		for (int i = 0; i < perm.length; i++)
			perm[i] = i;

		Arrays.sort(perm, new Comparator<Integer>()
		{
			@Override
			public int compare(Integer o1, Integer o2)
			{
				return -Double.compare(id.originalWeights[o1].weight, id.originalWeights[o2].weight);
			}
		});


		// //////////////////////////////////////////////////////////////////
		// Algorithumus von Seite 6 (Maple Code) in 2001-03-28-5+pwr+upw.pdf
		// Jeweils für alle Hausgrößen und alle Methoden
		// //////////////////////////////////////////////////////////////////

		Calculation calc = new Calculation(id);
		calc.calculate2();

		// Zeile (1) //
		int k = id.weightcount();

		// Zeile (2) //
		// Speichert die Bevölerungszahlen der Größe nach geordnet
		int[] pop = new int[k];
		for (int i = 0; i < k; i++)
		{
			pop[i] = (int) id.originalWeights[perm[i]].weight;
		}


		// für jede Hausgröße acc, für jede Zuteilungsmethode met...
		for (int acc = 0; acc < id.accuracies.length; acc++)
			for (int met = 0; met < id.outputFormat.methods.length; met++)
			{
				// Wenn die größte Partei max nicht verletzt keine Gewichtung
				if (id.weights[acc][met][0][perm[0]].rdWeight <= id.max)
				{
					id.powers[acc][met] = new double[] { 1 };
					continue;
				}

				int[] seats = new int[k];
				for (int i = 0; i < k; i++)
					seats[i] = id.weights[acc][met][0][perm[i]].rdWeight;

				MethodData md = id.outputFormat.methods[met];
				Signpost sp = Calculation.getSignpost(md);

				// Untere Grenze, bei der eine Partei noch einen Sitz
				// abgeben kann
				int bound = sp.s(0) == 0 ? 2 : 1;
				if (id.min > bound - 1)
					bound = id.min + 1;

				ArrayList<Double> powers = new ArrayList<Double>();
				double Emin = 1;
				// Wechsle Intervall und transferiere einen Sitz solange,
				// bis die größte Partei genau max(insges. base+max) Sitze hat
				while (seats[0] > id.max)
				{
					EData ed = Emin(pop, seats, k, bound, sp);

					Emin = ed.E;
					seats[ed.from]--;
					seats[ed.to]++;
				}
				while (seats[0] == id.max)
				{
					EData ed = Emin(pop, seats, k, bound, sp);

					double Emax = Emin;
					Emin = ed.E;
					double Enice = 1;
					try
					{
						Enice = new Divisor().buildData(Emin, Emax).c;
						vprintln("Enice: " + Enice);
						vprintln("\n--------------\n");
						// wb: Start
						//if (id.ml != null)
						//{
						//	id.ml.printMessage("Enice: " + Enice);
						//	id.ml.printMessage("\n--------------\n");
						//}
						// wb: Ende

					}
					catch (Exception e)
					{
						vprintln("Fehlerhafter Enice!");
					}
					powers.add(Enice);

					seats[ed.from]--;
					seats[ed.to]++;
				}
				Collections.sort(powers);

				id.powers[acc][met] = new double[powers.size()];
				Weight[] temp = id.weights[acc][met][0];
				id.weights[acc][met] = new Weight[powers.size()][k];
				for (int p = 0; p < powers.size(); p++)
				{
					id.powers[acc][met][p] = powers.get(p);
					for (int i = 0; i < k; i++)
					{
						id.weights[acc][met][p][perm[i]] = temp[perm[i]].clonew();
						id.weights[acc][met][p][perm[i]].weight = Math.pow(pop[i], powers.get(p));
					}
				}

			}

		// //////////////////////////////////////////////////////////////////
		// Nochmal Sitzberechnung, diesmal mit ENice
		// //////////////////////////////////////////////////////////////////
		return calc.calculate2();
	}


	/** Die Funktion E(i,j) berechnet die näheste Potenz, mit der ein Sitz von
	 * Partei j an Partei i gehen kann
	 * 
	 * @param i
	 * @param j
	 * @param seats Array der Sitze
	 * @param pop Array der Gewichte/Bevoelkerungszahlen
	 * @param sp Sprungstellenobjekt zur aktuellen Zuteilungsmethode
	 * @return E(i,j) besagte Potenz */
	private double E(int i, int j, int[] seats, int[] pop, Signpost sp)
	{
		double temp = Math.log((sp.s(seats[j] - 1)) / sp.s(seats[i]));
		temp /= Math.log(pop[j] / (double) pop[i]);
		return temp;
	}

	/** Berechnet für jede Kombination (Land i mit weniger Bevölkerung als Land j)
	 * E(i,j) und gibt den größten Wert zurück
	 * 
	 * @param pop Array der Gewichte/Bevoelkerungszahlen
	 * @param seats Array der Sitze
	 * @param k Anzahl der Parteien
	 * @param bound Untere Grenze, um noch einen Sitz abgeben zu können
	 * @param sp Sprungstellenobjekt zur aktuellen Zuteilungsmethode
	 * @return Die maximale Potenz <b>E</b> und die Indizes <b>i,j</b>,
	 *         zwischen denen ein Sitz transferiert wird; */
	private EData Emin(int[] pop, int[] seats, int k, int bound, Signpost sp)
	{
		EData ed = new EData();
		ed.E = Double.MIN_VALUE;
		for (int j = 0; j < k; j++)
			for (int i = 0; i < k; i++)
			{
				if (!(pop[i] < pop[j] && seats[j] >= bound))
					continue;
				double temp = E(i, j, seats, pop, sp);
				if (temp > ed.E)
				{
					ed.E = temp;
					ed.from = j;
					ed.to = i;
				}
			}
		return ed;
	}

	/** Innere Klasse <b>EData</b>:<br/>
	 * Fasst eine Potenz <b>E</b> und Indizes <b>i,j</b>
	 * zusammen
	 * 
	 * @author Marco Schumacher */
	private class EData
	{
		public double E;
		public int from, to;
	}

	private void vprintln(String s)
	{
		if (verbose)
			System.out.println(s);
	}
}
