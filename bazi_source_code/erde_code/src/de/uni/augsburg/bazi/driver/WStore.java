/*
 * @(#)WStore.java 3.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.util.Vector;

/** <b>Title:</b> BAZI<br>
 * <b>Description:</b> Sammelt und Verwaltet alle Parteien
 * mit ihren Stimmen und Sitzen pro Methode<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 3.1
 * @author Florian Kluge, Christian Brand */
public class WStore
{

	/** Vector mit den Daten */
	private Vector<SumSet> vData;

	/** Anzahl der Methoden */
	private int numberOfMethods;

	/** Konstruktor erzeugt leeren Store
	 * @param nm Anzahl der Methoden */
	public WStore(int nm)
	{
		vData = new Vector<SumSet>();
		numberOfMethods = nm;
	}

	/** Gibt den Vector mit den Daten zurück */
	public Vector<SumSet> getData()
	{
		return vData;
	}

	/** Ablegen eines Satzes im Speicher
	 * @param name Parteiname
	 * @param votes Stimmen
	 * @param acc gewonnene Sitze
	 * @return <b>true</b> falls die Partei schon existiert */
	public boolean put(String name, double votes, double[] acc)
	{
		if (contains(name))
		{
			int n = getNumber(name);
			SumSet tmp = (SumSet) vData.elementAt(n);
			tmp.votes += votes;
			for (int i = 0; i < numberOfMethods; i++)
			{
				tmp.acc[i] += acc[i];
			}
			return true;
		}
		else
		{
			SumSet tmp = new SumSet(name, numberOfMethods);
			tmp.votes = votes;
			for (int i = 0; i < numberOfMethods; i++)
			{
				tmp.acc[i] = acc[i];
			}
			vData.add(tmp);
			return false;
		}
	}

	/** Prüfung ob eine bestimmte Partei vorhanden ist
	 * @param pname Name der Partei
	 * @return <b>true</b>, falls die Partei vorhanden ist */
	public boolean contains(String pname)
	{
		for (int i = 0; i < vData.size(); i++)
		{
			SumSet ss = (SumSet) vData.elementAt(i);
			if (ss.name.equals(pname))
			{
				return true;
			}
		}
		return false;
	}

	/** Liefert die Nummer einer vorhandenen Partei
	 * @param pname Name der Partei
	 * @return Die Nummer der Partei, falls nicht vorhanden, -1 */
	public int getNumber(String pname)
	{
		for (int i = 0; i < vData.size(); i++)
		{
			SumSet ss = (SumSet) vData.elementAt(i);
			if (ss.name.equals(pname))
			{
				return i;
			}
		}
		return -1;
	}

	/** <b>Title:</b> Klasse SumSet<br>
	 * <b>Description:</b> Repräsentiert eine Partei mit ihren Stimmen und Sitzen<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @version 2.1
	 * @author Florian Kluge, Christian BRand */
	public class SumSet
	{

		/** Name der Partei */
		public String name;

		/** Anzahl der Stimmen */
		public double votes;

		/** Sitze pro Methode */
		public double[] acc;

		/** Erzeugt ein neues SumSet.
		 * 
		 * @param pname Name der Partei
		 * @param len Anzahl der Methoden */
		public SumSet(String pname, int len)
		{
			name = pname;
			acc = new double[len];
		}

		/** Erzeugt eine String Repräsentation dieses SumSets
		 * 
		 * @return SumSet als String */
		public String toString()
		{
			String tmp = name;
			tmp += " V: " + votes;
			for (int i = 0; i < acc.length; i++)
			{
				tmp += " " + acc[i] + ";";
			}
			return tmp;
		}
	}

}
