/*
 * @(#)IntSet.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Arrays;

/** <b>Überschrift:</b> IntSet<br>
 * <b>Beschreibung:</b> Stellt eine Menge ganzer Zahlen 0..(k-1) dar<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 3.1 */
public class IntSet
{

	/** Initiale Größe */
	private static int INITIAL_SIZE = 10;

	/** Die Mengenrepräsentation Bei direkten Zuweisungen an dieses Feld muß die Methode calcElemcount aufgerufen werden, um die Konsistenz der Objektdaten zu gewährleisten! */
	boolean[] theSet = null;

	/** Zeigt an, ob die Menge schon benutzt wurde
	 * @uml.property name="isNull" */
	private boolean isNull = true;

	/** Anzahl der Elemente */
	private int elemCount = 0;

	/** Erstellt ein neues Set */
	public IntSet()
	{
		theSet = new boolean[INITIAL_SIZE];
		initialize();
	}

	/** Erstellt ein neues Set
	 * @param s Startgröße des Sets */
	public IntSet(int s)
	{
		theSet = new boolean[s];
		initialize();
	}

	public IntSet(IntSet that)
	{
		theSet = Arrays.copyOf(that.theSet, that.theSet.length);
		isNull = that.isNull;
		elemCount = that.elemCount;
	}

	/** Initialisiere den Bitvektor */
	private void initialize()
	{
		for (int i = 0; i < theSet.length; i++)
		{
			theSet[i] = false;
		}
	}

	/** Liefert die Größe dieses Sets
	 * @return int */
	public int getSize()
	{
		return theSet.length;
	}

	/** Überprüft, ob dieses schon mal benutzt wurde
	 * @return <b>true</b> wenn schon mal eine Zahl zu dieser Menge hinzugefügt wurde
	 * @uml.property name="isNull" */
	public boolean isNull()
	{
		return isNull;
	}

	/** Hinzufügen einer Zahl zu diesem Set
	 * @param n int */
	public void add(int n)
	{
		if (n >= theSet.length)
		{
			// Set vergrößern
			boolean[] tmp = new boolean[n + 1];
			for (int i = 0; i < tmp.length; i++)
			{
				tmp[i] = false;
			}
			for (int i = 0; i < theSet.length; i++)
			{
				tmp[i] = theSet[i];
			}
			theSet = tmp;
		}
		if (!theSet[n])
		{
			elemCount++;
		}
		theSet[n] = true;
		isNull = false;
	}

	/** Prüfung, ob eine Zahl in diesem Set enthalten ist
	 * @param n int
	 * @return boolean */
	public boolean contains(int n)
	{
		if ((n < 0) || (n >= theSet.length))
		{
			return false;
		}
		else
		{
			return theSet[n];
		}
	}

	/** Entfernen einer Zahl aus diesem Set
	 * @param n int */
	public void remove(int n)
	{
		if ((n < 0) || (n >= theSet.length))
		{}
		else
		{
			if (theSet[n])
			{
				elemCount--;
			}
			theSet[n] = false;
		}
	}

	/** Entfernen der letzten Zahl aus diesem Set
	 * @return die entfernte Zahl, -1 falls das Set bereits leer war */
	public int removeLast()
	{
		int i = theSet.length;
		while (i > 0)
		{
			i--;
			if (theSet[i])
			{
				theSet[i] = false;
				return i;
			}
		}
		return -1;
	}

	/** Mengendifferenz: this\is
	 * @param is IntSet
	 * @return IntSet */
	public IntSet setDiff(IntSet is)
	{
		IntSet ret = null;
		ret = new IntSet();
		for (int i = 0; i < theSet.length; i++)
		{
			if (theSet[i])
			{
				if (!is.contains(i))
				{
					ret.add(i);
				}
			}
		}
		return ret;
	}

	/** Mengenvereinigung
	 * @param is IntSet
	 * @return IntSet */
	public IntSet union(IntSet is)
	{
		IntSet ret = null;
		int max = Math.max(getSize(), is.getSize());
		// int min = Math.min(this.getSize(), is.getSize());
		// boolean isSmaller = min == theSet.length;
		ret = new IntSet();
		for (int i = 0; i < max; i++)
		{
			ret.theSet[i] = contains(i) || is.contains(i);
		}
		ret.calcElemCount();
		return ret;
	}

	/** Prüft ob diese Menge leer ist.
	 * 
	 * @return <b>true</b> wenn diese Menge leer ist */
	public boolean isEmpty()
	{
		boolean b = false;
		for (int i = 0; i < getSize(); i++)
		{
			b |= contains(i);
		}
		return !b;
	}

	/** Liefert die Anzahl der Elemente in dieser Menge
	 * 
	 * @return Anzahl der Elemente */
	public int elemCount()
	{
		return elemCount;
	}

	/** Berechnent die Anzahl der Elemente und aktualisiert das entsprechende Feld */
	void calcElemCount()
	{
		elemCount = 0;
		for (int i = 0; i < theSet.length; i++)
		{
			if (theSet[i])
			{
				elemCount++;
			}
		}
	}

	/** Liefert das k-te Element in dieser Menge.
	 * 
	 * @param k Index
	 * @return k-tes Element, oder -1, falls es zu wenige Elemente gibt */
	public int elementAt(int k)
	{
		int cnt = 0;
		for (int i = 0; i < theSet.length; i++)
		{
			if (theSet[i])
			{
				if (cnt == k)
				{
					return i;
				}
				else
				{
					cnt++;
				}
			}
		}
		return -1;
	}

	/** Liefert die Menge [0,...,s-1].
	 * 
	 * @param s Maximum
	 * @return IntSet */
	public static IntSet fullSet(int s)
	{
		IntSet ret = new IntSet(s);
		for (int i = 0; i < s; i++)
		{
			ret.add(i);
		}
		return ret;
	}

	/** Erzeugt eine String Repräsentation dieser Menge.
	 * 
	 * @return Menge als String */
	@Override
	public String toString()
	{
		String tmp = "IntSet size: " + theSet.length + " elementCount: " +
				elemCount + " elements: {";
		for (int i = 0; i < theSet.length; i++)
		{
			if (theSet[i])
			{
				tmp += i + "; ";
			}
		}
		tmp += "}";
		return tmp;
	}

	/** Liefert ein int-Array mit den Elementen dieser Menge.
	 * 
	 * @return int-Array dieser Menge */
	public int[] elements()
	{
		int[] tmp = new int[elemCount()];
		int offset = 0;
		for (int i = 0; i < theSet.length; i++)
		{
			if (theSet[i])
			{
				tmp[offset++] = i;
			}
		}
		return tmp;
	}
}
