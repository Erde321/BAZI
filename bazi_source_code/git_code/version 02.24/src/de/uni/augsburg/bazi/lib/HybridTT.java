/*
 * @(#)HybridTT.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Vector;

import de.uni.augsburg.bazi.lib.vector.Method;

/** <b>Überschrift:</b> Klasse HybridTT<br>
 * <b>Beschreibung:</b> Implementierung des Tie&Transfer Algorithmus In TTflpt neu implementiert<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <p>
 * Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 3.1
 * @deprecated */

public class HybridTT
{

	/** MethodListener */
	private Vector<MethodListener> methodListeners = new Vector<MethodListener>();

	// Eingabedaten
	private Weight[][] inputWeights = null;

	/** Zeilen-Marginalien */
	private int[] row = null;

	/** Spalten-Marginalien */
	private int[] col = null;

	/** Zeilendivisoren */
	private double[] rowDivs = null;

	/** Spaltendivisoren */
	private double[] colDivs = null;

	/** Anzahl der Zeilen */
	private int rows = 0;

	/** Anzahl der Spalten */
	private int cols = 0;

	/** Rundungsmethode */
	private Signpost sp = null;

	// Arbeitsdaten
	/** Aktuelle Zeilen Divisoren */
	private double[] aktRowDivs = null;

	/** Aktuelle Spalten Divisoren */
	private double[] aktColDivs = null;

	/** Zeilen Fehlstände */
	private int[] rowFlaws = null;

	/** Spalten Fehlstände */
	private int[] colFlaws = null;

	/** Gesamte Zeilenfehlstände */
	private int fRow = 0;

	/** Gesamte Spaltenfehlstände */
	private int fCol = 0;

	/** Gewichtstabelle, mit der gearbeitet wird
	 * @uml.property name="tmpWeights"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	private Weight[][] tmpWeights = new Weight[rows][cols];

	/** Erstellt ein neues Objekt
	 * @param w Enthält die Originalgewichte sowie die letzte Zuteilung aus BipropMethod.
	 *          Es gilt: w = Weight[row.length][col.length]
	 * @param row Zeilensummen
	 * @param col Spaltensummen
	 * @param rowDivs Zeilendivisoren (akkumuliert)
	 * @param colDivs Spaltendivisoren (akkumuliert)
	 * @param sp Sprungstellenfunktion */
	public HybridTT(Weight[][] w, int[] row, int[] col, double[] rowDivs,
			double[] colDivs, Signpost sp)
	{
		this.inputWeights = w;
		this.row = row;
		this.col = col;
		rows = row.length;
		cols = col.length;
		this.rowDivs = rowDivs;
		this.colDivs = colDivs;
		this.sp = sp;
	}

	/** Hinzufügen eines MethodListeners
	 * @param l MethodListener */
	public void addMethodListener(MethodListener l)
	{
		if (l == null)
		{
			return;
		}
		methodListeners.add(l);
	}

	/** Entfernen eines MethodListeners
	 * 
	 * @param l MethodListener, der entfernt werden soll
	 * @return <b>true</b>, fall der Listener entfernt werden konnte */
	public boolean removeMethodListener(MethodListener l)
	{
		return methodListeners.remove(l);
	}

	/** Benachrichtigen der Listener über eine neue Nachricht
	 * 
	 * @param msg die Nachricht */
	protected void notifyMethodListeners(String msg)
	{
		for (int i = 0; i < methodListeners.size(); i++)
		{
			MethodListener l = (MethodListener) methodListeners.elementAt(i);
			l.printMessage(msg);
		}
	}

	/** Startet die Tie&Transfer Berechnung
	 * 
	 * @return BipropRet
	 * @throws BipropException */
	public BipropRet start() throws BipropException
	{
		String debtmp;

		if (Debug.BIPROP)
		{
			notifyMethodListeners("\nStarte HybridLR\n\n");
		}
		// ## Initialisierung
		int step = 0;
		// added by. S.M.
		int iter = 0;

		boolean[][] scriptA = new boolean[rows][cols];
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				if (inputWeights[i][j].weight > 0)
				{
					scriptA[i][j] = true;
				}
				else
				{
					scriptA[i][j] = false;
				}
			}
		}

		// Achtung Indizierung!
		int nb = rows + 0;
		int nf = cols + 0;
		int mb = rows + 1;
		int mf = cols + 1;

		aktRowDivs = new double[rows];
		aktColDivs = new double[cols];
		// aktApp = new int[rows][cols];
		for (int i = 0; i < rows; i++)
		{
			/* for (int j = 0; j < cols; j++) {
			 * aktApp[i][j] = inputWeights[i][j].rdWeight;
			 * } */
			// aktRowDivs[i] = 1; //rowDivs[i];
			aktRowDivs[i] = rowDivs[i];
		}
		for (int j = 0; j < cols; j++)
		{
			// aktColDivs[j] = 1; // colDivs[j];
			aktColDivs[j] = colDivs[j];
		}

		// Transfers, Updates Zähler initialisieren
		int transfers = 0, updates = 0;

		// ## Initialisierung abgeschlossen!


		// ### Compute apportionment
		rowFlaws = new int[rows];
		colFlaws = new int[cols];

		// added by S.M.
		int acc = 12;
		// acc wird bei mehr als 10^6 auf einen kleineren Wert gesetzt
		int availSeats = 0;
		for (int i = 0; i < row.length; i++)
			availSeats += row[i];
		if (availSeats > Math.pow(10, 6))
			acc = 9;

		// FAK050816
		int remainder = 2;

		// Gewichte kopieren
		tmpWeights = new Weight[rows][cols];
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				tmpWeights[i][j] = inputWeights[i][j].clonew();
				// next by S.M.
				// FAK050818 jetzt wird wieder mit den richtigen Gewichten gearbeitet
				tmpWeights[i][j].weight /= aktRowDivs[i] * aktColDivs[j];
				// if (Method.expDifVal(tmpWeights[i][j].weight, tmpWeights[i][j].rdWeight, acc, 1)) { // FAK050815
				if (Math.abs(tmpWeights[i][j].weight - tmpWeights[i][j].rdWeight) > remainder)
				{ // FAK050816
					throw new BipropException("remainder greater than one at " + i + " " +
							j + " " +
							tmpWeights[i][j].weight + " " +
							tmpWeights[i][j].rdWeight + "\n");
				}

			}
		}
		// added by S.M.
		if (Debug.BIPROP)
		{
			notifyMethodListeners("-*-*-Gewichte---\n" +
					BipropMethod.printMatrixW(tmpWeights) +
					"\n---Zuteilung---\n" +
					BipropMethod.printMatrixR(tmpWeights));
			notifyMethodListeners("--inputWeights--\n" +
					BipropMethod.printMatrixW(inputWeights) +
					"\nZeilendivisoren: " +
					printDArray(rowDivs) + "\nSpaltendivisoren: " +
					printDArray(colDivs));
		}

		// zuerst Fehlstände
		computeFlaws();
		// added by S.M.
		if (fRow > 100000)
		{
			acc = 10;
		}

		if (Debug.BIPROP)
		{
			notifyMethodListeners("Startkonfiguration:\nZeilen: " + rows +
					" Spalten: " + cols);
			notifyMethodListeners(printFlawInformation("Start"));
		}

		// Start: äußere WHILE-Schleife
		while (fRow > 0)
		{
			step++;
			iter++;
			if (Debug.BIPROP)
			{
				notifyMethodListeners("\n\nHybridLR: Schritt " + step);
			}
			if (Thread.interrupted())
			{
				throw new BipropException(
						"User interrupted HybridLR calculation @step " + step);
			}
			// added by S.M.
			if (iter > rows + cols + 2)
			{
				if (Debug.BIPROP)
				{
					notifyMethodListeners("---tmpWeights---\n" +
							BipropMethod.printMatrixW(tmpWeights) +
							"\n---Zuteilung---\n" +
							BipropMethod.printMatrixR(tmpWeights));
				}
				throw new BipropException(
						"Too many steps without change in apportionment");
			}

			boolean noMoreLabels = false;

			IntSet[] labelb = new IntSet[rows + 2];
			for (int i = 0; i < rows + 2; i++)
			{
				labelb[i] = new IntSet(rows);
			}
			IntSet[] labelf = new IntSet[cols + 2];
			for (int i = 0; i < cols + 2; i++)
			{
				labelf[i] = new IntSet(cols);
			}

			// ## (0)

			IntSet im = new IntSet(rows);

			for (int i = 0; i < rows; i++)
			{
				int sum = 0;
				for (int j = 0; j < cols; j++)
				{
					sum += tmpWeights[i][j].rdWeight;
				}
				if (sum < row[i])
				{
					im.add(i);
				}
			}

			IntSet jm = new IntSet(cols);
			for (int j = 0; j < cols; j++)
			{
				int sum = 0;
				for (int i = 0; i < rows; i++)
				{
					sum += tmpWeights[i][j].rdWeight;
				}
				if (sum < col[j])
				{
					jm.add(j);
				}
			}

			if (Debug.BIPROP)
			{
				debtmp = "Unterrepräsentierte\n-Zeilen: " + im;
				debtmp += "\n-Spalten: " + jm + "\n";
				debtmp += "---Zuteilung:---\n" + BipropMethod.printMatrixR(tmpWeights);
				debtmp += "---Gewichte:---\n" + BipropMethod.printMatrixW(tmpWeights);
				notifyMethodListeners(debtmp);
			}

			IntSet sI = im;
			IntSet sJ = new IntSet(cols);
			IntSet ic = IntSet.fullSet(rows).setDiff(sI);
			IntSet jc = IntSet.fullSet(cols).setDiff(sJ);
			labelf[nf] = sI;

			if (Debug.BIPROP)
			{
				debtmp = "ic: " + ic + "\njc: " + jc;
				notifyMethodListeners(debtmp);
				// notifyMethodListeners("nml: " + noMoreLabels + " labelb_nb.empty: " + labelb[nb].isEmpty());
			}

			while (!noMoreLabels && labelb[nb].isEmpty())
			{
				if (Debug.BIPROP)
				{
					notifyMethodListeners("Betrete innere while-Schleife");
				}
				noMoreLabels = true;

				// ## (1)

				// FORALL i IN sI DO
				debtmp = "(1) Zeilen:\n";
				for (int i = 0; i < rows; i++)
				{
					if (sI.contains(i))
					{
						int sum = 0;
						for (int j = 0; j < cols; j++)
						{
							sum += tmpWeights[i][j].rdWeight;
						}
						debtmp += "Summe Zeile " + i + ": " + sum + "\n";
						if (sum > row[i])
						{
							labelb[nb].add(i);
							debtmp += "Zeile " + i + " zu labelb_nb hinzugefügt\n";
						}

						// FORALL j IN jc DO
						for (int j = 0; j < cols; j++)
						{
							if (jc.contains(j))
							{
								if (Debug.BIPROP)
								{
									debtmp += "w[" + i + "][" + j + "]: " + tmpWeights[i][j].weight + " m_ij: " + tmpWeights[i][j].rdWeight + " s(m_ij): " + sp.s(tmpWeights[i][j].rdWeight) + "\n";
								}
								if (scriptA[i][j] &&
										Method.expDif(tmpWeights[i][j].weight,
												sp.s(tmpWeights[i][j].rdWeight), acc))
								{
									sJ.add(j);
									labelf[j].add(i);
									debtmp += "Spalte " + j + " zu J hinzugefügt\n";
									debtmp += "\tZeile " + i + " zu labelf[j] hinzugefügt\n";
									noMoreLabels = false;
								}
							}
						}

					}
				}
				if (Debug.BIPROP)
				{
					notifyMethodListeners(debtmp);
				}
				// labelb_nb enthält nun gerade die überrepräsentierten Zeilen
				// labelf_j enthält die Zeilen, bei denen in Spalte j etwas hinzugefügt werden kann

				// LR093
				ic = IntSet.fullSet(rows).setDiff(sI);
				jc = IntSet.fullSet(cols).setDiff(sJ);

				// ## (2)

				debtmp = "\n(2) Spalten:\n";
				// FORALL j IN sJ DO
				for (int j = 0; j < cols; j++)
				{
					if (sJ.contains(j))
					{
						int sum = 0;
						for (int i = 0; i < rows; i++)
						{
							sum += tmpWeights[i][j].rdWeight;
						}
						debtmp += "Summe Spalte " + j + ": " + sum + "\n";
						if (sum < col[j])
						{
							labelf[mf].add(j);
							debtmp += "Spalte " + j + " zu labelf_mf hinzugefügt\n";
						}
						for (int i = 0; i < rows; i++)
						{
							if (ic.contains(i))
							{
								if (Debug.BIPROP)
								{
									debtmp +=
											"w[" + i + "][" + j + "]: " + tmpWeights[i][j].weight + " m_ij: " + tmpWeights[i][j].rdWeight + " s(m_ij-1): " + sp.s(tmpWeights[i][j].rdWeight - 1) + "\n";
								}
								if (scriptA[i][j] &&
										Method.expDif(tmpWeights[i][j].weight,
												sp.s(tmpWeights[i][j].rdWeight - 1), acc))
								{
									sI.add(i);
									labelb[i].add(j);
									debtmp += "Zeile " + i + " zu I hinzugefügt\n";
									debtmp += "\tSpalte " + j + " zu labelb[i] hinzugefügt\n";
									noMoreLabels = false;
								}
							}
						}
					}
				}
				if (Debug.BIPROP)
				{
					notifyMethodListeners(debtmp);
				}

				// LR108
				ic = IntSet.fullSet(rows).setDiff(sI);
				jc = IntSet.fullSet(cols).setDiff(sJ);

				// added by S.M.
				if (Debug.BIPROP)
				{
					debtmp = "Spaltenfehler: " + printArray(colFlaws);
					debtmp += "\nZeilenfehler: " + printArray(rowFlaws);
					debtmp += "\nMarkierte\n -Zeilen: " + sI;
					debtmp += "\n -Spalten: " + sJ + "\n";
					debtmp += "labelb_nb: " + labelb[nb] + "\n";
					debtmp += "labelf_mf: " + labelf[mf] + "\n";
					debtmp += "---Zuteilung:---\n" + BipropMethod.printMatrixR(tmpWeights) +
							"\n";
					// added by S.M.
					debtmp += "---tmpWeights:--\n" + BipropMethod.printMatrixW(tmpWeights);
					notifyMethodListeners(debtmp);
				}

			} // ende innere while-Schleife

			if (Debug.BIPROP)
			{
				notifyMethodListeners("Innerer while-Schleife beendet");
				notifyMethodListeners("I: " + sI);
				notifyMethodListeners("IC: " + ic);
				notifyMethodListeners("J: " + sJ);
				notifyMethodListeners("JC: " + jc);
				notifyMethodListeners("labelb_nb: " + labelb[nb]);
				notifyMethodListeners("if: labelb_nb.empty: " + labelb[nb].isEmpty());
				notifyMethodListeners("else if: labelb_nb.empty: " + labelb[nb].isEmpty() +
						" labelb_mb.empty: " + labelb[mb].isEmpty() +
						" labelf_mf.empty: " + labelf[mf].isEmpty());
				notifyMethodListeners("labelf_mf [mf=" + mf + "]: " + labelf[mf]);
			}

			// ## (i)

			if (!labelb[nb].isEmpty())
			{
				transfers++;
				if (Debug.BIPROP)
				{
					notifyMethodListeners("labelb_nb NOT empty -> if");
				}
				// added by S.M.
				iter = 0;
				IntSet usedi = new IntSet(rows);
				IntSet usedj = new IntSet(cols);

				for (int i = 0; i < labelb[nb].getSize(); i++)
				{ // hier findet er nicht alle Moeglichkeiten vgl. FG1.txt
					if (labelb[nb].contains(i))
					{
						boolean balancing = true;

						int k = 0;
						k = 0;
						while (usedj.contains(labelb[i].elementAt(k)) &&
								(k <= labelb[i].elemCount()))
						{
							k++;
						}

						// LR125
						if (labelb[i].elementAt(k) > -1)
						{
							int j = labelb[i].elementAt(k);
							tmpWeights[i][j].rdWeight -= 1;
							if (Debug.BIPROP)
							{
								debtmp = "decreased at ij (" + i + "; " + j +
										")\n";
								debtmp += "---Zuteilung:---\n" +
										BipropMethod.printMatrixR(tmpWeights);
								notifyMethodListeners(debtmp);
							}
							usedi.add(i);
							usedj.add(j);
							while (balancing)
							{
								if (!labelf[j].isNull())
								{ // LR131
									k = 0;
									while (usedi.contains(labelf[j].elementAt(k)) &&
											(k <= labelf[j].elemCount()))
									{
										k++;
									}
									int ii = labelf[j].elementAt(k);
									if (ii >= 0)
									{
										i = ii;
									}
									if (Debug.BIPROP)
									{
										debtmp = "labelf_j[j=" + j + "]: " + labelf[j] + "\n";
										debtmp += "k: " + k + " ii: " + ii + "\n";
										notifyMethodListeners(debtmp);
									}
								}
								tmpWeights[i][j].rdWeight++;
								if (Debug.BIPROP)
								{
									debtmp = "increased at ij (" + i + "; " + j +
											")\n";
									debtmp += "---Zuteilung:---\n" +
											BipropMethod.printMatrixR(tmpWeights);
									notifyMethodListeners(debtmp);
								}
								usedi.add(i);
								usedj.add(j);
								if (labelb[i].isEmpty() || labelf[nf].contains(i))
								{
									balancing = false;
								}
								else
								{
									k = 0;
									while (usedj.contains(labelb[i].elementAt(k)))
									{
										k++;
									}
									int jj = labelb[i].elementAt(k);
									if (jj <= -1)
									{
										balancing = false;
									}
									else if (labelf[jj].setDiff(usedj).isEmpty())
									{
										balancing = false;
									}
									else
									{
										j = jj;
										tmpWeights[i][j].rdWeight--;
										if (Debug.BIPROP)
										{
											debtmp = "decreased at ij (" + i + "; " + j +
													")\n";
											debtmp += "---Zuteilung:---\n" +
													BipropMethod.printMatrixR(tmpWeights);
											notifyMethodListeners(debtmp);
										}
									}
								}
							}
						}
					}
				}
			}

			// ## (ii)
			else if (labelb[nb].isEmpty() && labelb[mb].isEmpty() &&
					labelf[mf].isEmpty())
			{
				updates++;
				if (Debug.BIPROP)
				{
					notifyMethodListeners("labelb_nb, labelb_mb, labelf_mf EMPTY -> else if");
				}
				double epsilon1;
				double tmp;
				if ((sI.elemCount() > 0) && (jc.elemCount() > 0))
				{
					double min = Double.POSITIVE_INFINITY;
					for (int i = 0; i < sI.elemCount(); i++)
					{
						for (int j = 0; j < jc.elemCount(); j++)
						{
							// added by. S.M.
							// if (tmpWeights[sI.elementAt(i)][jc.elementAt(j)].weight >0) {
							/* min = Math.min(min,
							 * sp.s(tmpWeights[sI.elementAt(i)][jc.elementAt(j)].
							 * rdWeight) /
							 * tmpWeights[sI.elementAt(i)][jc.elementAt(j)].
							 * weight); */
							// }
							/* notifyMethodListeners("=== " + sp.s(tmpWeights[sI.elementAt(i)][jc.elementAt(j)].rdWeight) +
							 * "\n" + tmpWeights[sI.elementAt(i)][jc.elementAt(j)].weight +
							 * "\n" + sp.s(tmpWeights[sI.elementAt(i)][jc.elementAt(j)].rdWeight) /
							 * tmpWeights[sI.elementAt(i)][jc.elementAt(j)].weight + "\n"); */
							// FAK050818
							double top = sp.s(tmpWeights[sI.elementAt(i)][jc.elementAt(j)].rdWeight);
							double bottom = tmpWeights[sI.elementAt(i)][jc.elementAt(j)].weight;
							if ((top > 0) && (top <= Double.MAX_VALUE) && (bottom > 0) && (bottom <= Double.MAX_VALUE))
							{
								tmp = top / bottom;
							}
							else
							{
								tmp = Double.POSITIVE_INFINITY;
							}
							min = Math.min(min, tmp);
						}
					}
					epsilon1 = min;
				}
				else
				{
					epsilon1 = Double.POSITIVE_INFINITY;
				}
				double epsilon2;
				if ((ic.elemCount() > 0) && (sJ.elemCount() > 0))
				{
					double min = Double.POSITIVE_INFINITY;
					for (int i = 0; i < ic.elemCount(); i++)
					{
						for (int j = 0; j < sJ.elemCount(); j++)
						{
							// added by S.M.
							/* if (tmpWeights[ic.elementAt(i)][sJ.elementAt(j)].weight > 0) {
							 * min = Math.min(min,
							 * tmpWeights[ic.elementAt(i)][sJ.elementAt(j)].
							 * weight /
							 * sp.s(tmpWeights[ic.elementAt(i)][sJ.elementAt(j)].
							 * rdWeight - 1));
							 * } */
							// FAK050818
							double top = tmpWeights[ic.elementAt(i)][sJ.elementAt(j)].weight;
							double bottom = sp.s(tmpWeights[ic.elementAt(i)][sJ.elementAt(j)].rdWeight - 1);
							if ((top > 0) && (top <= Double.MAX_VALUE) && (bottom > 0) && (bottom <= Double.MAX_VALUE))
							{
								tmp = top / bottom;
							}
							else
							{
								tmp = Double.POSITIVE_INFINITY;
							}
							min = Math.min(min, tmp);

						}
					}
					epsilon2 = min;
				}
				else
				{
					epsilon2 = Double.POSITIVE_INFINITY;

				}
				// Zeugs mit NaN added by S.M.
				// FAK050818 sollte jetzt nicht mehr auftreten können!
				double epsilon;
				if (Double.isNaN(epsilon1) || Double.isNaN(epsilon2))
				{
					if (Debug.BIPROP)
					{
						notifyMethodListeners("NaN aufgetreten!");
					}
					if (Double.isNaN(epsilon1))
					{
						epsilon = epsilon2;
					}
					else if (Double.isNaN(epsilon2))
					{
						epsilon = epsilon1;
					}
					else
					{
						throw new BipropException("Alle Epsilons NaN");
					}
				}
				else
				{
					epsilon = Math.min(epsilon1, epsilon2);
				}
				// added by S.M.
				if (Debug.BIPROP)
				{
					notifyMethodListeners("==== eps1: " + epsilon1 + " eps2: " + epsilon2 +
							" eps: " +
							epsilon + "====\n");
				}

				if (epsilon < Double.POSITIVE_INFINITY)
				{
					// LR182...
					if (sI.elemCount() > 0)
					{
						for (int i = 0; i < sI.elemCount(); i++)
						{
							aktRowDivs[sI.elementAt(i)] /= epsilon;
							if (Debug.BIPROP)
								notifyMethodListeners("update Zeilendivisor " + sI.elementAt(i));
						}
					}
					if (sJ.elemCount() > 0)
					{
						for (int j = 0; j < sJ.elemCount(); j++)
						{
							aktColDivs[sJ.elementAt(j)] *= epsilon;
							if (Debug.BIPROP)
								notifyMethodListeners("update Spaltendivisor " + sJ.elementAt(j));
						}
					}
				}
				if (Debug.BIPROP)
				{
					notifyMethodListeners("Zeilendivisoren: " + printDArray(aktRowDivs) +
							"\nSpaltendivisoren: " + printDArray(aktColDivs));
				}
			}
			else
			{
				// never come here
				if (Debug.BIPROP)
				{
					notifyMethodListeners("Either N is labelled nor NOT (N and M) nor M");
				}
				// BipropRet ret = new BipropRet();
				// ret.sError = "Either N is labelled nor NOT (N and M) nor M";
				// return ret;
				throw new BipropException(
						"Either N is labelled nor NOT (N and M) nor M");
			}

			// # new start values for next iteration
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					// added by S.M.
					// notifyMethodListeners("===" + printDArray(aktRowDivs) + "\n" + printDArray(aktColDivs));
					tmpWeights[i][j].weight = inputWeights[i][j].weight /
							(aktRowDivs[i] * aktColDivs[j]);
					// if (Method.expDifVal(tmpWeights[i][j].weight, tmpWeights[i][j].rdWeight, acc, 1)) { // FAK050815
					if (Math.abs(tmpWeights[i][j].weight - tmpWeights[i][j].rdWeight) > remainder)
					{ // FAK050816
						throw new BipropException("remainder greater than one at " + i +
								" " + j + " " +
								tmpWeights[i][j].weight + " " +
								tmpWeights[i][j].rdWeight + "\n");
					}
				}
			}

			/* fRow = 0;
			 * for (int i = 0; i < rows; i++) {
			 * int tmp = 0;
			 * for (int j = 0; j < cols; j++) {
			 * //tmp += aktApp[i][j];
			 * tmp += tmpWeights[i][j].rdWeight;
			 * }
			 * fRow += Math.abs(tmp - row[i]);
			 * } */
			// added by S.M.
			// int oldFlaw = fRow;

			computeFlaws();

			// added by S.M.
			/* if (iter == 0 && oldFlaw == fRow) {
			 * throw new BipropException(
			 * "No change in apportionment though it should");
			 * }
			 * if (Debug.DPP) {
			 * notifyMethodListeners(printFlawInformation("Iteration " + step));
			 * } */
			/* if (step > 10) {
			 * throw new BipropException("Iteration cancelled for debug");
			 * } */
		} // end while

		// ## find ties
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				/* System.out.println("weight: " + tmpWeights[i][j].weight + "\n" +
				 * "rdWeight: " + tmpWeights[i][j].rdWeight + "\n" +
				 * "s(rdWeight): " + sp.s(tmpWeights[i][j].rdWeight) + "\n" +
				 * "s(rdWeight-1): " + sp.s(tmpWeights[i][j].rdWeight - 1) + "\n\n"); */
				if (Method.expDif(tmpWeights[i][j].weight,
						sp.s(tmpWeights[i][j].rdWeight), acc) &&
						(tmpWeights[i][j].rdWeight < tmpWeights[i][j].weight ||
						Method.expDif(tmpWeights[i][j].weight,
								(double) tmpWeights[i][j].rdWeight, acc)))
				{
					tmpWeights[i][j].multiple = "+";
				}
				else if (Method.expDif(tmpWeights[i][j].weight,
						sp.s(tmpWeights[i][j].rdWeight - 1), acc) &&
						(tmpWeights[i][j].rdWeight > 0) &&
						((tmpWeights[i][j].rdWeight > tmpWeights[i][j].weight) ||
						Method.expDif(tmpWeights[i][j].weight,
								(double) tmpWeights[i][j].rdWeight, acc)))
				{
					tmpWeights[i][j].multiple = "-";
				}
			}
		}

		// fertige Zuteilung in tmpWeights
		BipropRet ret = new BipropRet();
		ret.divColNice = aktColDivs;
		ret.divRowNice = aktRowDivs;
		ret.app = tmpWeights;
		ret.transfers = transfers;
		ret.updates = updates;
		return ret;
	}

	/** Erzeugt eine String Repräsentation eines int-Arrays
	 * 
	 * @param array int-Array
	 * @return int-Array als String */
	private String printArray(int[] array)
	{
		String tmp = "";
		for (int i = 0; i < array.length; i++)
		{
			tmp += array[i] + "; ";
		}
		return tmp;
	}

	/** Erzeugt eine String Repräsentation eines double-Arrays
	 * 
	 * @param array double-Array
	 * @return double-Array als String */
	private String printDArray(double[] array)
	{
		String tmp = "";
		for (int i = 0; i < array.length; i++)
		{
			tmp += array[i] + "; ";
		}
		return tmp;
	}

	/** Berechne Anzahl der Fehlstände */
	private void computeFlaws()
	{
		fRow = 0;
		for (int i = 0; i < rows; i++)
		{
			int tmp = 0;
			for (int j = 0; j < cols; j++)
			{
				// tmp += aktApp[i][j];
				// tmp += inputWeights[i][j].rdWeight;
				tmp += tmpWeights[i][j].rdWeight;
			}
			fRow += Math.abs(tmp - row[i]);
			rowFlaws[i] = tmp - row[i];
		}
		fCol = 0;
		for (int j = 0; j < cols; j++)
		{
			int tmp = 0;
			for (int i = 0; i < rows; i++)
			{
				// tmp += aktApp[i][j];
				// tmp += inputWeights[i][j].rdWeight;
				tmp += tmpWeights[i][j].rdWeight;
			}
			fCol += Math.abs(tmp - col[j]);
			colFlaws[j] = tmp - col[j];
		}
	}

	/** Liefert Informationen über die Fehlstände
	 * 
	 * @param info Info
	 * @return Informationen über die aktuellen Fehlstände */
	private String printFlawInformation(String info)
	{
		String tmp = "\nFehlstände bei " + info + ":\n";
		tmp += "Zeilen: " + printArray(rowFlaws);
		tmp += "\n\tZ-Summe: " + fRow;
		tmp += "\nSpalten: " + printArray(colFlaws);
		tmp += "\n\tS-Summe: " + fCol + "\n";
		return tmp;
	}


}
