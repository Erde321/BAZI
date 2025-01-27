/*
 * @(#)Permutations.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Vector;

/** <b>Überschrift:</b> Permutations<br>
 * <b>Beschreibung:</b> Stellt Methoden zur Berechnung von Permutationen bereit<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Robert Bertossi, Christian Brand
 * @version 3.1 */
public class Permutations
{

	/** generate all possible permutation of ties in a super apportionement
	 * 
	 * @param wa a supper apportionment
	 * @return a Vector of all results */
	public static Vector<Weight[]> getMonopropPermutations(Weight[] wa)
	{
		// 'sort' flags (bindings); neccessary before calling the recursiv part of generatePermutations
		// '-'s in front, then the '+'s; objects without flags are ignored
		int i = 0, j = 0;
		while (i < wa.length)
		{

			// flag '+' found
			if (wa[i].getFlag() == 1)
			{

				// search for next '-'
				for (j = i + 1; j < wa.length; j++)
				{
					if (wa[j].getFlag() == -1)
					{ // Object with '-'
						// 'switch' flags
						wa[i].rdWeight++;
						wa[i].multiple = "-";

						wa[j].rdWeight--;
						wa[j].multiple = "+";

						break;
					}
				}
			}

			// j == weights.length --> no other entry with '-' found --> finished
			if (j == wa.length)
			{
				break;
			}

			i++;
		} // end while

		Vector<Weight[]> v = new Vector<Weight[]>();
		generatePermutations(wa, v);

		// test consistency
		// check v.size() == binomial_coefficient(numMinus+numPlus, numMinus)
		int numMinus = 0, numPlus = 0;
		for (int k = 0; k < wa.length; k++)
		{
			if (wa[k].getFlag() == 1)
			{
				numPlus++;
			}
			if (wa[k].getFlag() == -1)
			{
				numMinus++;
			}
		}

		if (v.size() != binomialCoefficient(numMinus + numPlus, numMinus))
		{
			System.out.println(
					"WARNING: Number of found returned super apportionments " +
							"does not match number of theoretical apportionments!");
		}

		return v;
	}

	/** generates all possible permutations of flags (ties)
	 * is called recursivly by generatePermutataions(Weight[])
	 * 
	 * @param wa apportionment; array of weight objects
	 * @param v all Permutations will be saved into this Vector */
	private static void generatePermutations(Weight[] wa, Vector<Weight[]> v)
	{

		// copy array and objects
		Weight[] wa_new = new Weight[wa.length];
		for (int k = 0; k < wa.length; k++)
		{
			wa_new[k] = wa[k].clonew();
			// add given array
		}
		v.add(wa_new);

		int i = 0, j = 0;

		// Variable i --> Index des letzten Eintrags mit '-' vor dem ersten Eintrag mit '+'
		// i == -1 --> keine Einträge mehr zu bearbeiten
		i = -1;

		// Suche letzten Eintrag mit '-' vor dem ersten Eintrag mit '+'
		for (j = 0; j < wa.length; j++)
		{
			if (wa[j].getFlag() == -1)
			{
				i = j;
			}
			if (wa[j].getFlag() == 1)
			{
				break;
			}
		}

		if (i != -1)
		{

			// zu bearbeitenden Eintrag gefunden
			// Eintrag mit Index i nach rechts verschieben (bzw. mit '+' Flags tauschen)
			// bis ein Eintrag mit '-' gefunden wird
			// für jede neue Permutation wird die Methode rekursiv aufgerufen
			for (j = i + 1; j < wa.length; j++)
			{
				// Eintrag mit '+' gefunden
				if (wa[j].getFlag() == 1)
				{
					// Einträge vertauschen
					wa[i].rdWeight--;
					wa[i].multiple = "+";

					wa[j].rdWeight++;
					wa[j].multiple = "-";

					// 'neue' Position des '-'
					i = j;

					// Reskursiver Aufruf
					// Kopiere Array und Objekte
					Weight[] wa_new2 = new Weight[wa.length];
					for (int k = 0; k < wa.length; k++)
					{
						wa_new2[k] = wa[k].clonew();

					}
					generatePermutations(wa_new2, v);
				}

				// Eintrag mit '-' gefunden
				else if (wa[j].getFlag() == -1)
				{
					break;
				}
			}

		}
	}

	/** Berechnet den Binomial-Koeffizient (n über k)
	 * 
	 * @param n Zahl
	 * @param k Zahl
	 * @return n über k */
	public static int binomialCoefficient(int n, int k)
	{
		if (k > n || k < 0)
		{
			return 0;
		}

		int p = n - k + 1;
		int zaehler = 1;
		int nenner = 1;

		for (int i = 1; i <= k; i++)
		{
			nenner *= i;

		}
		for (int i = p; i <= n; i++)
		{
			zaehler *= i;

		}
		return zaehler / nenner;
	}

	/** Erzeugt ein Array von boolschen Matrizen, die alle Biprop-Permutationen der
	 * übergebenen Matrix darstellen.
	 * Eintrag in i,j gibt an, ob bei der aktuellen Permutation die Bindung in der
	 * ursprünglichen Matrix umgedreht werden soll.
	 * 
	 * @param waa ursprüngliche Zuteilung
	 * @param rowApp Zeilensummen
	 * @param colApp Spaltensummen
	 * @return 3-dimensionales boolean Array */
	public static boolean[][][] getBipropPermutationMatrix(Weight[][] waa,
			int[] rowApp, int[] colApp, PermutationListener pl, PermutationsCommunicator pc)
			throws PermutationsInterruptedException
	{
		ExploreCircle ec = new ExploreCircle(waa, rowApp, colApp, pl, pc);

		boolean[][][] permutations = ec.transformCircles_boolean();
		return permutations;
		// return null;
	}

	/** Erzeugt alle Biprop-Permutationen der übergebenen Zuteilung.
	 * 
	 * @param waa Matrix mit der Zuteilung
	 * @param rowApp Zeilensummen
	 * @param colApp Spaltensummen
	 * @return 3-dimensionales Array mit allen gleichwertigen Zuteilungen */
	public static Weight[][][] getBipropPermutations(Weight[][] waa, int[] rowApp,
			int[] colApp, PermutationListener pl, PermutationsCommunicator pc)
			throws PermutationsInterruptedException
	{
		boolean[][][] permutations = getBipropPermutationMatrix(waa, rowApp, colApp, pl, pc);
		Weight[][][] wret = new Weight[permutations.length][waa.length][waa[0].
				length];
		for (int i = 0; i < permutations.length; i++)
		{
			wret[i] = cloneApp(waa);
			flipApp(wret[i], permutations[i]);
		}
		removeIrrelevantTies(wret);
		return wret;
	}

	/** Dreht alle Bindungen in der übergebenen Zuteilung um, wenn der Eitnrag in
	 * der boolschen Matrix true ist.
	 * 
	 * @param app Zuteilung
	 * @param matrix boolsche Matrix */
	private static void flipApp(Weight[][] app, boolean[][] matrix)
	{
		for (int i = 0; i < app.length; i++)
		{
			for (int j = 0; j < app[0].length; j++)
			{
				if (matrix[i][j])
				{
					app[i][j].flip();
				}
			}
		}
	}

	/** Erstellt eine Kopie der übergebenen Matrix
	 * 
	 * @param app Gewichts Matrix
	 * @return Kopie der übergebenen Matrix */
	private static Weight[][] cloneApp(Weight[][] app)
	{
		Weight[][] ret = new Weight[app.length][app[0].length];
		for (int i = 0; i < app.length; i++)
		{
			for (int j = 0; j < app[0].length; j++)
			{
				ret[i][j] = app[i][j].clonew();
			}
		}
		return ret;
	}

	/** Entfernt alle überflüssigen Bindungen, d.h. Bindungen, die zu keiner neuen
	 * Permutation geführt haben.
	 * 
	 * @param theTensor Alle gleichwertigen Zuteilungen */
	public static void removeIrrelevantTies(Weight[][][] theTensor)
	{
		// System.out.println("rmIT");
		int flag = 0;
		boolean delete = true;

		if (theTensor.length == 0)
			return;

		// wenn sich ein Tie (+,-) in keiner der Permutationen ändert
		// (von + auf - oder von - auf +), wird er aus allen Permutationen entfernt
		for (int i = 0; i < theTensor[0].length; i++)
			for (int j = 0; j < theTensor[0][0].length; j++)
			{
				// bei keinem Tie, auf zum nächsten Eintrag
				if (theTensor[0][i][j].getFlag() == 0)
					continue;
				else
				{
					// Tie gefunden; es wird überprüft, ob es sich jemals ändert
					flag = theTensor[0][i][j].getFlag();
					delete = true;
					for (int k = 0; k < theTensor.length; k++)
						if (flag != theTensor[k][i][j].getFlag())
						{
							delete = false;
							break;
						}
					if (delete)
						// Tie ändert sich nie; aus allen Permutationen löschen
						for (int k = 0; k < theTensor.length; k++)
							theTensor[k][i][j].multiple = "";
				}
			}
	}
}
