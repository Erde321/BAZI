/*
 * @(#)Sort.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse Sort<br>
 * <b>Description:</b> Stellt einen Sortier-Algorithmus zur Verfügung. Da seine Anwendung hier nicht zeitkritisch ist und die Eingabegrößen typischerweise eher klein sind, reicht
 * hier LinSort.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Jan Petzold, Florian Kluge, Christian Brand
 * @version 3.1 */
public class Sort
{

	/** Sortiert ein Feld von Integer-Werten aufsteigend.
	 * 
	 * @param values Feld der zu sortierenden Integer-Werten.
	 * @return Feld mit den sortierten Indizes. */
	public static int[] sort(int[] values)
	{
		double[] dValues = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			dValues[i] = values[i];
		}
		return sort(dValues);
	}

	/** Sortiert ein Feld von Double-Werten aufsteigend.
	 * 
	 * @param values Feld der zu sortierenden Double-Werten.
	 * @return Feld mit den sortierten Indizes. */
	public static int[] sort(double[] values)
	{
		int[] index = new int[values.length];
		for (int i = 0; i < values.length; i++)
		{
			index[i] = i;
			// 23.12.2003 quicksort durch linSort ersetzt, da dieses hier stabil ist
			// quicksort muß überarbeitet werden...
			// quicksort(values, index, 0, values.length-1);
		}
		linSort(values, index);
		/*
		 * for (int l=0; l<index.length; l++) System.out.print(index[l] + " ");
		 * System.out.println(); */
		return index;
	}

	/** Sortiert ein Feld von Double-Werten absteigend.
	 * 
	 * @param values Feld der zu sortierenden Double-Werten.
	 * @return Feld mit den sortierten Indizes. */
	public static int[] decreaseSort(double[] values)
	{
		int[] index = sort(values);
		return reverse(index);
	}

	/** Umkehrung eines Feldes von Integer-Werten
	 * 
	 * @param values Feld der umzukehrenden Integer-Werten.
	 * @return Feld mit den umgekehrten Integer-Werten. */
	public static int[] reverse(int[] values)
	{
		int len = values.length;
		int[] reValues = new int[len];
		for (int i = 0; i < len; i++)
		{
			reValues[i] = values[len - 1 - i];
		}
		return reValues;
	}

	/** Lineares sortieren eines Arrays (aufsteigend)
	 * @param array Das Array mit den zu sortierenden Zahlen
	 * @param index Das Array mit den sortierten Indizes */
	private static void linSort(double[] array, int[] index)
	{
		int tmp;
		for (int i = 0; i < index.length - 1; i++)
		{
			for (int j = i + 1; j < index.length; j++)
			{
				if (array[index[i]] >= array[index[j]])
				{
					// System.out.print("swap i: " + i + " j: " + j + " index[i]: " + index[i] + " index[j]: " + index[j]);
					tmp = index[i];
					index[i] = index[j];
					index[j] = tmp;
					// System.out.println(" ...nach swap index[i]: " + index[i] + " index[j]: " + index[j]);
				}
			}
		}
		/*
		 * for (int l=0; l<index.length; l++) System.out.print(index[l] + " ");
		 * System.out.println(); */

	}

}
