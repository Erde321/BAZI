/*
 * @(#)ExploreCircle.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Iterator;
import java.util.Vector;

// Programm zum Auffinden aller Kreise in einem Netzwerk . Das Netzwerk in
//Adjazenzlistenschreibweise benötigt. Adjanetzwerk(i)~ Vector der alle von
//i aus ereichberaen Knoten enthält.
//die Anzahl der Bindungen für ein Problem ist gerade sder Anzahl
//überschneidungsfreier kreise im Netzwerk plus mögliche teilmengen von
//kreisen die keinen punkt gemeinsam haben.
/** <b>Überschrift:</b> Klasse ExploreCircle<br>
 * <b>Beschreibung:</b> Erzeugt aus einer Zuteilung mit Ties, alle gleichwertigen Zuteilungen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Bianca Joas, Christian Brand
 * @version 3.1 */
public class ExploreCircle
{
	private final Vector<Vector<Integer>> allCircles;
	private final Vector<Integer> Kreis;
	private Vector<Vector<Integer>> AdjaNetzwerk;
	private int Kreisanfang = 0;
	private final int[] r;
	private final int[] c;
	private int[][][] Matrix;

	private boolean[][][] Matrix_boolean;

	private PermutationListener pl = null;
	private PermutationsCommunicator pc = null;
	private boolean asked = false;
	private boolean finish = false;

	public ExploreCircle(Weight[][] w, int[] rr, int[] cc, PermutationListener pl,
			PermutationsCommunicator pc) throws PermutationsInterruptedException
	{

		allCircles = new Vector<Vector<Integer>>();
		// allCircles.clear();
		Kreis = new Vector<Integer>();
		// Kreis.clear();
		AdjaNetzwerk = new Vector<Vector<Integer>>();
		// AdjaNetzwerk.clear();
		r = rr;
		c = cc;
		this.pl = pl;
		this.pc = pc;

		AdjaNetzwerk = getNetwork(w, r, c);
		// System.out.println(allCircles.size());
		search_Circle(AdjaNetzwerk); // hier werden all Kreise gesucht die man
		// direkt aus dem Netzwerk erhält

		// Thread unterbrochen?
		if (Thread.interrupted())
			userInterrupted();

		// hier werden alleKombinationen von Kreises hinzugefügt, die weder die
		// selbe Partei noch den selben Distrikt enhalten
		// System.out.println("Gefundene Kreise: " + allCircles.size());
		// System.out.println("Alle Kreise gefunden");
		if (!finish)
			moreCircles();
		// System.out.println("Alle Kombinationen gefunden");

		// Thread unterbrochen?
		if (Thread.interrupted())
			userInterrupted();

		// PermutationsCommunicator benachrichtigen
		if (pc != null)
			pc.permutationChanged(allCircles.size());
	}

	private void search_Circle(Vector<Vector<Integer>> AdjaNetzwerk) throws PermutationsInterruptedException
	{
		// System.out.println("Suche Kreise...");
		for (int i = 1; i < AdjaNetzwerk.size(); i++)
		{
			Kreisanfang = i;
			// System.out.println("Jetzt wird kreis ab " +i+" gesucht!");
			next(i);
			if (finish)
				return;
		}
	}

	private void next(int i) throws PermutationsInterruptedException
	{

		// Thread unterbrochen?
		if (Thread.interrupted())
			userInterrupted();

		Kreis.addElement(new Integer(i));
		// System.out.println("in schleife für Knoten " +i);

		// Falls Knoten i ausgehende Kanten hat....
		if (!AdjaNetzwerk.get(i).isEmpty())
		{

			// Iterator it = ( (Vector) AdjaNetzwerk.get(i)).iterator();
			Iterator<Integer> it = AdjaNetzwerk.get(i).iterator();

			// für jede ausgehende Kante
			while (it.hasNext())
			{
				int j = it.next().intValue();
				if (j >= Kreisanfang)
				{

					if (j == Kreisanfang)
					// das heißt ein Kreis wurde gefunden falls es ihn
					// nicht schon gibt oder es sich um eine gleiche kante handelt
					{

						if (same_vertice(i, j, Kreis))
						{} // prüfen, ob Kante i->j schon enthalten ist
						// Kann die überhaupt schon enthalten sein?!?!
						// Einklich nicht, denn dann wären wir vorher schon einmal hier in
						// den else-Zweig gelaufen
						else
						{ // Kante ist noch nicht enthalten!
							// falls nicht schon enthalten zu den neuen kreisen aufnéhmen
							// zusätzlich müssen alle felder die verändert werden gleich sein
							Iterator<Vector<Integer>> iterator_C = allCircles.iterator();
							boolean enthaelt = false;
							Vector<Integer> circle = new Vector<Integer>();
							// zur überprüfung ob zwei kreise diegleichen sind muss j erstmal dem
							// Kreis hinzugefügt werden
							Kreis.addElement(new Integer(j));
							while (enthaelt == false && iterator_C.hasNext())
							{
								// circle = (Vector) iterator_C.next();
								circle = iterator_C.next();

								if ((circle).containsAll(Kreis) && Kreis.containsAll(circle))
								{
									enthaelt = true;
									// zusätzlich die Bedingung prüfen, ob alle matrixfelder gleich sind
									// zuerst die zwei matrizen initialisieren. Einträge false falls keine änderung auf der position, ansonsten true
									// 1.te Matrix
									boolean[][] kreis1 = new boolean[r.length][c.length];
									boolean[][] kreis2 = new boolean[r.length][c.length];
									for (int k = 0; k < r.length; k++)
									{
										for (int h = 0; h < c.length; h++)
										{
											kreis1[k][h] = false;
											kreis1[k][h] = false;
										}
									}
									Iterator<Integer> kreis1_Iter = circle.iterator();
									int z = kreis1_Iter.next().intValue();
									int s = kreis1_Iter.next().intValue();
									kreis1[z - 1][s - r.length - 1] = true;
									int zaehler_der_Schleife = 1;
									while (kreis1_Iter.hasNext())
									{
										if (zaehler_der_Schleife % 2 == 1)
										{
											z = kreis1_Iter.next().intValue();
											kreis1[z - 1][s - r.length - 1] = true;
										}
										else
										{
											s = kreis1_Iter.next().intValue();
											kreis1[z - 1][s - r.length - 1] = true;
										}
										zaehler_der_Schleife = zaehler_der_Schleife + 1;
									}

									// 2.te Matrix
									Iterator<Integer> kreis2_Iter = Kreis.iterator();
									z = kreis2_Iter.next().intValue();
									s = kreis2_Iter.next().intValue();
									kreis2[z - 1][s - r.length - 1] = true;
									zaehler_der_Schleife = 1;
									while (kreis2_Iter.hasNext())
									{
										if (zaehler_der_Schleife % 2 == 1)
										{
											z = kreis2_Iter.next().intValue();
											kreis2[z - 1][s - r.length - 1] = true;
										}
										else
										{
											s = kreis2_Iter.next().intValue();
											kreis2[z - 1][s - r.length - 1] = true;
										}
										zaehler_der_Schleife = zaehler_der_Schleife + 1;
									}
									// vergleichen der Matrizen

									for (int k = 0; k < r.length; k++)
									{
										for (int h = 0; h < c.length; h++)
										{
											if (kreis1[k][h] != kreis2[k][h])
											{
												enthaelt = false;
											}
										}
									}
								}
							}

							// hier wird j wieder entfernt, da es vorhin nur zu überprüfungszwecken hinzugefügt wurde
							Kreis.remove(Kreis.size() - 1);

							if (!enthaelt)
							{
								Kreis.addElement(new Integer(j));
								// for (int m=0; m<Kreis.size(); m++)
								// System.out.print("  " + Kreis.get(m));
								// Vector<Integer> b = (Vector<Integer>) Kreis.clone();
								Vector<Integer> b = cloneVectorInteger(Kreis);
								allCircles.addElement(b);
								if ((pc != null) && !asked)
								{
									if (allCircles.size() >= PermutationsCommunicator.MAX_PERMUTATIONS)
									{
										asked = true;
										if (pc.calcAllPermutations())
										{
											finish = false;
										}
										else
										{
											// System.out.println("ExploreCircle beenden....");
											finish = true;
											pl.setMorePermutations(true);
											return;
										}
									}
								}

								// PermutationCommunicator benachrichtigen
								if (pc != null && asked && allCircles.size() % 100 == 0)
									pc.permutationChanged(allCircles.size());

								// System.out.println("Kreis gefunden, Nummer: " + allCircles.size());
								Kreis.remove(Kreis.size() - 1);
								// System.out.println("&&&&");

							}
							next(j); // ??? Doppelkreise mit Gestalt der 8?
							if (finish)
								return;
						}

					}

					// wohl besser: else { if (Kreis.contains(new Integer(j)) { // FAK052016
					if (j != Kreisanfang && Kreis.contains(new Integer(j)))
					{
						// das heißt ein kreis innerhalb-> findet erst später berücksichtigung
						// jetzt muss noch überprüft werden ob es sich wirklich um die selbe kante handelt
						// betrachte also für all vorkommen von i den vorgänger, ist dieser gleich j dann
						// handelt es sih wirklich um gleiche kante
						if (same_vertice(i, j, Kreis))
						{}
						else
						{
							// Falls Kante i->j noch nicht besucht wurde, tu es jetzt....
							next(j);
							if (finish)
								return;
						}
					}
					if (!Kreis.contains(new Integer(j)))
					{
						// Falls j noch nicht dabei ist, weitersuchen
						next(j);
						if (finish)
							return;
					}
				}
			}
		}
		Kreis.remove(Kreis.size() - 1);
	}

	/** Prüft, ob V = {v_1..v_n} v_k-1, v_k enthält mit v_k-1=i, v_k=j
	 * @param i int
	 * @param j int
	 * @param Kreis Vector
	 * @return boolean */
	private boolean same_vertice(int i, int j, Vector<Integer> Kreis)
	{
		// Vector hilfe = (Vector) Kreis.clone();
		Vector<Integer> hilfe = cloneVectorInteger(Kreis);
		if (hilfe.contains(new Integer(j)))
		{
			if (hilfe.indexOf(new Integer(j)) != 0)
			{
				if (hilfe.get(hilfe.indexOf(new Integer(j)) - 1).intValue() == i)
				{
					return true;
				}
				else
				{
					hilfe.remove(hilfe.indexOf(new Integer(j)));
					return same_vertice(i, j, hilfe);
				}
			}
			else
			{
				hilfe.remove(hilfe.indexOf(new Integer(j)));
				return same_vertice(i, j, hilfe);
			}
		}
		else
		{
			return false;
		}
	}

	public int numberOfApp()
	{
		int number = allCircles.size() + 1;
		return number;

	}


	@SuppressWarnings("unused")
	private void show_Circle()
	{
		// System.out.println("Anzahl der möglichen und gleichwertigen Zuteilungen: " +
		// (allCircles.size() + 1));
		Iterator<Integer> iv;
		for (int i = 0; i < allCircles.size(); i++)
		{
			// iv = ( (Vector) allCircles.get(i)).iterator();
			iv = allCircles.get(i).iterator();
			while (iv.hasNext())
			{
				System.out.print(iv.next() + "  ");
			}
			// System.out.println(" zt");
		}
	}

	/** disjunkte Kreise miteinander kombinieren... */
	private void moreCircles() throws PermutationsInterruptedException
	{ // hier geht es um alle möglichen Kombinationen von Kreisen, die weder den selben Distrikt noch die selbe Partei beinhalten
		int wasgefunden = 1;
		while (wasgefunden > 0)
		{

			// Thread unterbrochen?
			if (Thread.interrupted())
				userInterrupted();

			// Vector<Vector<Integer>> Klon = (Vector<Vector<Integer>>) allCircles.clone();
			Vector<Vector<Integer>> Klon = cloneVectorVectorInteger(allCircles);
			int Kreisanzahl = Klon.size();
			// System.out.println(Kreisanzahl);

			wasgefunden = 0;
			for (int i = 0; i < Kreisanzahl; i++)
			{
				Vector<Integer> b = Klon.get(i);
				for (int j = i + 1; j < Kreisanzahl; j++)
				{
					Vector<Integer> c = Klon.get(j);
					int istdrin = 0;
					for (int l = 0; l < c.size(); l++)
					{
						int help = c.get(l);
						if (b.contains(help) && help > 0)
						{
							istdrin = 1;
							break;
						}
					}
					/* Iterator<Integer> it = c.iterator();
					 * while (it.hasNext()) {
					 * int help = ( (Integer) it.next()).intValue();
					 * if (b.contains(new Integer(help)) && help > 0) {
					 * istdrin = 1;
					 * break;
					 * }
					 * } */
					if (istdrin == 0)
					{

						// erstelle neuen Vektor neu, der sowohl b als aus c enthält
						Vector<Integer> neu = new Vector<Integer>(b.size() + c.size() + 2);
						for (int x : b)
						{
							neu.add(x);
						}
						neu.add(0); // 0 als Trenner
						for (int x : c)
						{
							neu.add(x);
						}
						/* Iterator<Integer> itb = b.iterator();
						 * while (itb.hasNext()) {
						 * neu.add(itb.next());
						 * }
						 * neu.add(new Integer(0)); // 0 als Trenner!
						 * Iterator<Integer> itc = c.iterator();
						 * while (itc.hasNext()) {
						 * neu.add(itc.next());
						 * } */

						// jetzt noch mal alle teilmengen durchgehen. ist er nicht schon vielleicht enthalten?
						int help2 = 0;
						for (Vector<Integer> circle : Klon)
						{
							if (circle.containsAll(neu) && neu.containsAll(circle))
							{
								// Kreis ist bereits vorhanden
								help2 = 1;
							}
						}

						/* Vector<Vector<Integer>> abcd = cloneVectorVectorInteger(allCircles);
						 * Iterator<Vector<Integer>> itec = abcd.iterator();
						 * while (itec.hasNext()) {
						 * Vector<Integer> abc = itec.next();
						 * if ( ( (abc).containsAll(neu) && neu.containsAll(abc))) {
						 * help2 = 1;
						 * }
						 * } */

						if (help2 == 0)
						{

							allCircles.add(neu);
							// System.out.println("Kombi-Kreis gefunden, Nummer: " + allCircles.size());
							if ((pc != null) && !asked)
							{
								if (allCircles.size() >= PermutationsCommunicator.MAX_PERMUTATIONS)
								{
									asked = true;
									if (pc.calcAllPermutations())
									{
										finish = false;
									}
									else
									{
										// System.out.println("ExploreCircle beenden....");
										finish = true;
										pl.setMorePermutations(true);
										return;
									}
								}
							}

							// PermutationCommunicator benachrichtigen
							if (pc != null && asked && allCircles.size() % 100 == 0)
								pc.permutationChanged(allCircles.size());

							wasgefunden = 1;
						}
					}
				}
			}
		}

	}

	public boolean[][][] transformCircles_boolean()
	{
		Matrix_boolean = new boolean[allCircles.size() + 1][r.length][c.length];
		for (int i = 0; i < allCircles.size() + 1; i++)
		{
			for (int j = 0; j < r.length; j++)
			{
				for (int k = 0; k < c.length; k++)
				{
					Matrix_boolean[i][j][k] = false;
				}
			}
		}

		Iterator<Vector<Integer>> iter = allCircles.iterator();
		int matrixNummer = 1;
		while (iter.hasNext())
		{
			Vector<Integer> Kreis = iter.next();
			Iterator<Integer> iterkreis = Kreis.iterator();
			int anfang = 0;
			int a = 0;
			int b = 0;
			while (iterkreis.hasNext())
			{
				if (anfang == 0)
				{
					a = iterkreis.next().intValue();
					b = iterkreis.next().intValue();
					Matrix_boolean[matrixNummer][a - 1][b - r.length - 1] = true;

					anfang++;
				}
				else
				{
					if (anfang > 0 && (anfang % 2) == 0)
					{
						b = iterkreis.next().intValue();
						if (b == 0)
						{
							anfang = 0;
						}
						else
						{
							anfang++;
							Matrix_boolean[matrixNummer][a - 1][b - r.length - 1] = true;
						}
					}
					else
					{ // (anfang>0 && (anfang % 2)>0)
						a = iterkreis.next().intValue();
						Matrix_boolean[matrixNummer][a - 1][b - r.length - 1] = true;
						anfang++;
					}
				}

			}

			matrixNummer++;
		}
		// ///AUSGABE DER MATRIX
		/* System.out.println(
		 * "/////////////////////////////////////////////////////////////");
		 * for (int i = 0; i < allCircles.size() + 1; i++) {
		 * System.out.println(i);
		 * System.out.println(i);
		 * System.out.println(i);
		 * for (int j = 0; j < r.length; j++) {
		 * for (int k = 0; k < c.length; k++) {
		 * System.out.print(j + " " + k + " : " + Matrix_boolean[i][j][k] + "     ,");
		 * }
		 * }
		 * }
		 * System.out.println(allCircles.size() + 1); */
		return Matrix_boolean;
	}

	public int[][][] transformCircles()
	{
		Matrix = new int[allCircles.size() + 1][r.length][c.length];
		for (int i = 0; i < allCircles.size() + 1; i++)
		{
			for (int j = 0; j < r.length; j++)
			{
				for (int k = 0; k < c.length; k++)
				{
					Matrix[i][j][k] = 0;
				}
			}
		}

		Iterator<Vector<Integer>> iter = allCircles.iterator();
		int matrixNummer = 1;
		while (iter.hasNext())
		{
			Vector<Integer> Kreis = iter.next();
			Iterator<Integer> iterkreis = Kreis.iterator();
			int anfang = 0;
			int a = 0;
			int b = 0;
			while (iterkreis.hasNext())
			{
				if (anfang == 0)
				{
					a = iterkreis.next().intValue();
					b = iterkreis.next().intValue();
					Matrix[matrixNummer][a - 1][b - r.length - 1] = 1;

					anfang++;
				}
				else
				{
					if (anfang > 0 && (anfang % 2) == 0)
					{
						b = iterkreis.next().intValue();
						if (b == 0)
						{
							anfang = 0;
						}
						else
						{
							anfang++;
							Matrix[matrixNummer][a - 1][b - r.length - 1] = 1;
						}
					}
					else
					{ // (anfang>0 && (anfang % 2)>0)
						a = iterkreis.next().intValue();
						Matrix[matrixNummer][a - 1][b - r.length - 1] = 1;
						anfang++;
					}
				}

			}

			matrixNummer++;
		}
		// ///AUSGABE DER MATRIX
		/* System.out.println("/////////////////////////////////////////////////////////////");
		 * for (int i=0; i<allCircles.size()+1; i++)
		 * {
		 * System.out.println( i);
		 * System.out.println( i);
		 * System.out.println( i);
		 * for(int j=0; j<r.length; j++)
		 * {
		 * for (int k=0; k<c.length; k++)
		 * {
		 * System.out.print(j +" "+k+" : "+Matrix[i][j][k] + "     ,");
		 * }
		 * }
		 * }
		 * System.out.println(allCircles.size()+1); */
		return Matrix;
	}

	/** Erstelle den Graphen mit diesen Eigenschaften:
	 * Adjazenzlisten in radj, cadj, hier werden beide zusammen in einen Vector
	 * verpackt. radj_i ist die Adjazenzliste von Knoten i...
	 * f_ij == -1 => Kante c_j->r_i => cadj_j += i;
	 * f_ij == +1 => Kante r_i->c_j => radj_i += j; */
	private Vector<Vector<Integer>> getNetwork(Weight[][] w, int[] r, int[] c)
	{
		Vector<Vector<Integer>> a = new Vector<Vector<Integer>>();

		for (int i = 0; i < (r.length + c.length + 1); i++)
		{
			Vector<Integer> b = new Vector<Integer>();
			a.addElement(b);
		}
		for (int i = 0; i < r.length; i++)
		{
			for (int j = 0; j < c.length; j++)
			{
				if ((w[i][j]).getFlag() == -1)
				{
					(a.get(j + r.length + 1)).addElement(new Integer(i + 1));
				}
				if ((w[i][j]).getFlag() == 1)
				{
					a.get(i + 1).addElement(new Integer(j + r.length + 1));
				}
			}
		}
		return a;
	}

	/** Klont ein Vector<Integer> Objekt
	 * @param vi
	 * @return Vector */
	private Vector<Integer> cloneVectorInteger(Vector<Integer> vi)
	{
		Vector<Integer> ret = new Vector<Integer>(vi.size());
		Iterator<Integer> it = vi.iterator();
		while (it.hasNext())
			ret.add(new Integer(it.next().intValue()));
		it = null;
		return ret;
	}

	/** Klont ein Vector<Vector<Integer>> Objekt
	 * @param vvi Vector
	 * @return Vector */
	private Vector<Vector<Integer>> cloneVectorVectorInteger(Vector<Vector<Integer>> vvi)
	{
		Vector<Vector<Integer>> ret = new Vector<Vector<Integer>>(vvi.size());
		Iterator<Vector<Integer>> it = vvi.iterator();
		while (it.hasNext())
			ret.add(cloneVectorInteger(it.next()));
		it = null;
		return ret;
	}

	/** Thread wurde unterbrochen. Die Suche nach Permutationen wird kontrolliert beendet. */
	private void userInterrupted() throws PermutationsInterruptedException
	{
		if (pc != null)
			pc.permutationChanged(allCircles.size());

		throw new PermutationsInterruptedException("Interrupted after calculating " + allCircles.size() + " Permutations.",
				allCircles.size());
	}
}
