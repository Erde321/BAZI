/*
 * @(#)WeightStore.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.util.Vector;

/** <b>Title:</b> Klasse WeightStore<br>
 * <b>Description:</b> Hash-Map zur Erstellung der initialen Matrix<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public class WeightStore
{

	/** Vector mit den Daten */
	private final Vector<StoreSet> vData;

	// /** Anzahl der Methoden */
	// private int numberOfMethods;

	/** Konstruktor erzeugt leeren Store */
	public WeightStore()
	{
		vData = new Vector<StoreSet>();
	}

	/** Ablegen eines Satzes im Speicher
	 * @param name Parteiname
	 * @param number Nummer der Partei
	 * @return <b>true</b> falls erfolgreich */
	public boolean put(String name, int number)
	{
		vData.add(new StoreSet(name, number));
		return true;
	}

	/** Lesen der Nummer einer Partei
	 * @param name Name der Partei
	 * @return Nummer der Partei, -1 falls diese nicht existiert */
	public int getNumber(String name)
	{
		int i = 0;
		boolean b = false;
		while ((!b) && (i < vData.size()))
		{
			StoreSet tmp = vData.elementAt(i);
			if (tmp.name.equals(name))
			{
				b = true;
			}
			else
			{
				i++;
			}
		}
		if (b)
		{
			return i;
		}
		else
		{
			return -1;
		}
	}

	/** Speichert Name und Nummer einer Partei */
	private class StoreSet
	{

		// /** Nummer der Partei */
		// public int number;

		/** Name der Partei */
		public String name;

		/** Erzeugt einen neuen StoreSet.
		 * 
		 * @param na Name der Partei
		 * @param nu Nummer der Partei */
		public StoreSet(String na, int nu)
		{
			name = na;
			// number = nu;
		}
	}
}
