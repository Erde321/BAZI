/*
 * @(#)ASMethod.java 3.3 08/02/18
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import de.uni.augsburg.bazi.lib.exactbiprop.BigRational;
import de.uni.augsburg.bazi.lib.exactbiprop.ExactDivMethod;
import de.uni.augsburg.bazi.lib.exactbiprop.ExactSignPost;
import de.uni.augsburg.bazi.lib.vector.Method;

/** <b>Überschrift:</b> ASMethod<br>
 * <b>Beschreibung:</b> Biproportionale Berechnungsmethode mit alternativer Skalierung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2008<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Florian Kluge, Robert Bertossi, Christian Brand
 * @version 3.3 */
public class ASMethod extends AbstractBipropMethod
{
	/* Versionshistorie:
	 * 2008.02-b-02: Version 3.3
	 * - Einführung der Versionshistorie
	 * - Überarbeitung des Debug Outputs */

	/** Divisorupdatemethode: midpoint */
	public static final int DIV_MDPT = 0;

	/** Divisorupdatemethode: extreme */
	public static final int DIV_EXTR = 1;

	/** Divisorupdatemethode: random */
	public static final int DIV_RAND = 2;

	/** Hybrid: kein Umschalten auf einen T&T Algorithmus */
	public static final int HYBRID_NONE = 10;

	/** Hybrid: Umschalten auf TTflpt */
	public static final int HYBRID_TTFLPT = 11;

	/** Hybrid: Umschalten auf TTinte */
	public static final int HYBRID_TTINTE = 12;

	/** Divisorupdatemethode */
	private int divisorUpdateMethod = DIV_MDPT;

	/** Hybrid-Verhalten */
	private int hybrid = HYBRID_TTFLPT;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktRowDivs;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktRowDivMin;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktRowDivMax;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktColDivs;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktColDivMin;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktColDivMax;

	private BigRational[] brRowDivs;
	private BigRational[] brColDivs;

	public ASMethod(Weight[][] weights, int[] aDistricts, int[] aParties, Signpost aSp,
			BipropLibMessenger aBmes, BipropRet aRet, int aDiv_update,
			int aHybrid) throws BipropException
	{

		super(weights, aDistricts, aParties, aSp, aBmes, aRet);

		divisorUpdateMethod = aDiv_update;
		hybrid = aHybrid;
	}

	/** Berechnet das Ergebnis nach dem Alternating Scaling Algorithmus und wechselt ggf. auf einen T&T Algorithmus, falls sich die Fehlstände nicht schnell genug abbauen lassen
	 * können.
	 * 
	 * @return Ergebnis der Berechnung
	 * @throws BipropException Fehler bei der Initialisierung oder Berechnung
	 * @throws IterationExceededException Überschreitung einer vorgegebenen oberen Grenze der Iterationen */
	public BipropRet calculate() throws BipropException, IterationExceededException
	{

		Date start = GregorianCalendar.getInstance().getTime();

		// Speicherung der Fehlstände für Debug-Ausgabe am Schluß
		StringBuffer sFlaws = new StringBuffer("");
		int lastFlaw = Integer.MAX_VALUE;
		int rzahl = 0;
		String debtmp;
		String divtmp, divmintmp, divnicetmp, divmaxtmp;
		String sptr, sptc;

		boolean bm;

		boolean usedLR = false;

		Weight[][] aktm = new Weight[rows][cols];
		Weight[][] sve = new Weight[rows][cols];

		// akkumulierte Divisoren (a_i, b_j)
		double[] rowDivs = new double[rows];
		double[] colDivs = new double[cols];

		// aktuelle Divisoren (C_i, D_j) mit Grenzen
		aktRowDivs = new double[rows];
		aktRowDivMin = new double[rows];
		aktRowDivMax = new double[rows];
		aktColDivs = new double[cols];
		aktColDivMin = new double[cols];
		aktColDivMax = new double[cols];

		// Flaw count
		int[] frow = new int[rows];
		int[] fcol = new int[cols];

		// Last plus-tie
		int[] ptr = new int[rows];
		int[] ptc = new int[cols];

		// Anzahl Fehlstände, bei denen auf LR gewechselt wurde
		int lrSwitch = -1;

		Divisor[] divObjs;
		LibMessenger messenger = new LibMessenger();

		// diverse Checks...
		if (checksAndPolish && !checkExistence())
		{
			return ret;
		}

		// Zeilendivisoren mit 1 initialisieren
		for (int i = 0; i < rows; i++)
		{
			rowDivs[i] = 1d;
			frow[i] = 0;
		}
		// Spaltendivisoren mit 1 initialisieren
		for (int j = 0; j < cols; j++)
		{
			colDivs[j] = 1d;
			fcol[j] = 1;
		}

		// für die Abbruchbedingung
		int fcount = 1;

		// Jetzt kommt die eigentliche Zuteilung
		while (fcount > 0)
		{
			// Benutzerabbruch?
			if (Thread.interrupted())
			{
				userInterrupted(ret, sFlaws, rzahl);
				bmes.setError(BipropLibMessenger.USER_ERROR,
						"User interrupted calculation @iteration " +
								(ret.rowit + ret.colit),
						new String[] { String.valueOf(ret.rowit + ret.colit) });
				throw new BipropException("User interrupted calculation @iteration " +
						(ret.rowit + ret.colit));
			}

			/* ----------------------------------------------
			 * - ZEILENSCHRITT -
			 * ---------------------------------------------- */

			// Zuerst die Matrix erstellen
			int steps = ret.colit + ret.rowit;
			if (((steps % 50) == 0) && (steps > 0))
			{
				fireIterationChanged(steps, false);
			}

			// Alte Matrix in sve abspeichern
			if (steps > 0)
			{
				sve = aktm;
			}

			// Bei zu vielen Iterationen wird abgebrochen!
			if (maxIterations > 0)
			{
				if (steps > maxIterations)
				{
					throw new IterationExceededException(
							"Limit of iterations exceeded: " + steps);
				}
			}

			// Matrix neu erstellen und Gewichte skalieren
			aktm = new Weight[rows][cols];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					aktm[i][j] = theMatrix[i][j].clonew();
					// falls ein Divisor unendlich ist, sollte dieses Gewicht auf 0 gesetzt werden!
					if (Double.isInfinite(rowDivs[i]) || Double.isInfinite(colDivs[j]))
					{
						aktm[i][j].weight = 0.0d;
					}
					else
					{
						aktm[i][j].weight /= (rowDivs[i] * colDivs[j]);
					}
					if (steps > 0)
					{
						// speichern der gerundeten Gewichte von der letzten Iteration für die Existenzprüfung
						aktm[i][j].rdWeight = sve[i][j].rdWeight;
					}
				}
			}

			// Existenzprüfung
			ret.rowit++;

			// DEBUG-Ausgabe Zeileniteration
			if (Debug.BIPROP)
			{
				debtmp = "\nIteration: " + (ret.colit + ret.rowit);
				debtmp += "\nStartmatrix vor Zeilenschritt: " + ret.rowit + "\n";
				debtmp += printMatrixW(aktm);
				notifyMethodListeners(debtmp);
			}

			divObjs = new Divisor[rows];
			// Berechnung
			// Zuerst die Zuteilung
			divtmp = "";
			divmintmp = "";
			divnicetmp = "";
			divmaxtmp = "";
			sptr = "";

			// Für jede Zeile eine Zuteilung berechnen
			for (int i = 0; i < rows; i++)
			{
				divObjs[i] = new Divisor();
				Weight[] aktrow = new Weight[cols];
				for (int j = 0; j < cols; j++)
				{
					aktrow[j] = aktm[i][j];
				}

				bm = Method.statPowMethod(sp, divObjs[i], rowApp[i], aktrow, messenger, "");
				if (!bm)
				{
					/* Fehler in der Zuteilung */
					bmes.setError(BipropLibMessenger.METHOD, "@rowstep " + (ret.rowit) + " row: " + i, new String[] { String.valueOf(ret.rowit), String.valueOf(i) });
					throw new BipropException("@rowstep " + (ret.rowit) + " row: " + i);
				}

				// Update der Divisoren
				aktRowDivs[i] = divObjs[i].getDivisor();
				aktRowDivMin[i] = divObjs[i].getDivisorLow();
				aktRowDivMax[i] = divObjs[i].getDivisorHigh();

				if (Debug.BIPROP)
				{
					StringBuffer temp_min = new StringBuffer("Cmin[" + i + "]: " + aktRowDivMin[i] + "; ");
					StringBuffer temp_nor = new StringBuffer("   C[" + i + "]: " + aktRowDivs[i] + "; ");
					StringBuffer temp_max = new StringBuffer("Cmax[" + i + "]: " + aktRowDivMax[i] + "; ");
					int maxL = temp_min.length();
					if (maxL < temp_nor.length())
					{
						maxL = temp_nor.length();
					}
					if (maxL < temp_max.length())
					{
						maxL = temp_max.length();
					}
					for (int k = temp_min.length(); k < maxL; k++)
					{
						temp_min.append(" ");
					}
					for (int k = temp_nor.length(); k < maxL; k++)
					{
						temp_nor.append(" ");
					}
					for (int k = temp_max.length(); k < maxL; k++)
					{
						temp_max.append(" ");
					}
					divmintmp += temp_min.toString();
					divnicetmp += temp_nor.toString();
					divmaxtmp += temp_max.toString();
				}

				// plus-ties
				ptr[i] = divObjs[i].getPt();

				if (Debug.BIPROP)
				{
					sptr += "ptr[" + i + "]: " + ptr[i] + "; ";
				}
			}

			// Update der Divisoren
			updateDivisors(rowDivs, divObjs, frow);

			// DEBUG - Ausgabe der Zuteilung
			if (Debug.BIPROP)
			{
				debtmp = "Zuteilung nach Iteration: " + (ret.colit + ret.rowit) + "  Zeilenschritt: " + ret.rowit + "\n";
				debtmp += printMatrixR(aktm);
				notifyMethodListeners(debtmp);

				StringBuffer temp = new StringBuffer(divmintmp);
				temp.append("\n");
				temp.append(divnicetmp);
				temp.append("\n");
				temp.append(divmaxtmp);
				temp.append("\n\n");
				temp.append(sptr);
				temp.append("\nKumuliert: ");
				for (int i = 0; i < rows; i++)
				{
					temp.append("a[" + i + "]: " + rowDivs[i] + "; ");
				}
				debtmp = "";
				divtmp = temp.toString();
				notifyMethodListeners(divtmp);
			}

			// Jetzt noch die Fehlstände prüfen
			for (int j = 0; j < cols; j++)
			{
				fcol[j] = 0;
				for (int i = 0; i < rows; i++)
				{
					fcol[j] += aktm[i][j].rdWeight;
				}
				fcol[j] -= colApp[j];

				if (Debug.BIPROP)
				{
					debtmp += "fc[" + j + "]: " + fcol[j] + "; ";
				}
			}

			if (Debug.BIPROP)
			{
				notifyMethodListeners(debtmp);
			}

			// Bei entarteten Zeilen kann ein Transfer zwischen Spalten die Fehlstände korrigieren
			// -> "In tied rows..."
			// for (int i = 0; i < rows; i++) {
			// if (ptr[i] > -1) {
			// for (int j1 = 0; j1 < cols; j1++) {
			// for (int j2 = 0; j2 < cols; j2++) {
			// if ( (aktm[i][j1].multiple.equals("+")) &&
			// (aktm[i][j2].multiple.equals("-")) &&
			// (fcol[j1] < 0) &&
			// (fcol[j2] > 0)) {
			//
			// if (Debug.BIPROP) {
			// StringBuffer tempB = new StringBuffer("\nSitztransfer in Zeile: " + i);
			// tempB.append("\nSitz geht von Spalte j1: " + j1 + " nach j2: " + j2);
			// notifyMethodListeners(tempB.toString());
			// }
			// aktm[i][j1].rdWeight += 1;
			// aktm[i][j2].rdWeight -= 1;
			// // Update
			// fcol[j1] += 1;
			// fcol[j2] -= 1;
			// // Reflag
			// aktm[i][j1].multiple = "-";
			// aktm[i][j2].multiple = "+";
			// }
			// }
			// }
			// }
			// }
			while (transfer(aktm, false));
			for (int j = 0; j < cols; j++)
			{
				int sum = 0;
				for (int i = 0; i < rows; i++)
					sum += aktm[i][j].rdWeight;
				fcol[j] = sum - colApp[j];
			}

			// jetzt noch fcount neu berechnen
			fcount = 0;
			for (int j = 0; j < cols; j++)
			{
				fcount += Math.max(0, fcol[j]);
			}
			lastFlaw = fcount;

			// DEBUG
			if (Debug.BIPROP)
			{
				sFlaws.append(fcount + "; ");
				if (lastFlaw < fcount)
				{
					rzahl++;
				}
				debtmp = "-- Zuteilung nach evtl. Korrektur in den Zeilen --\n";
				debtmp += printMatrixR(aktm);
				debtmp += "\nFehlstände: " + fcount;
				notifyMethodListeners(debtmp);
			}
			/* ----------------------------------------------
			 * - ENDE ZEILENSCHRITT -
			 * ---------------------------------------------- */


			// Falls noch Fehlstände vorhanden sind, einen Spaltenschritt
			if (fcount > 0)
			{
				/* ----------------------------------------------
				 * - SPALTENSCHRITT -
				 * ---------------------------------------------- */

				// Zuerst die Matrix erstellen
				sve = aktm;
				aktm = new Weight[rows][cols];
				for (int i = 0; i < rows; i++)
				{
					for (int j = 0; j < cols; j++)
					{
						aktm[i][j] = theMatrix[i][j].clonew();
						// Falls ein Divisor unendlich ist, sollte dieses Gewicht auf 0 gesetzt werden!
						if (Double.isInfinite(rowDivs[i]) || Double.isInfinite(colDivs[j]))
						{
							aktm[i][j].weight = 0.0d;
						}
						else
						{
							aktm[i][j].weight /= (rowDivs[i] * colDivs[j]);
						}
						aktm[i][j].rdWeight = sve[i][j].rdWeight;
					}
				}

				ret.colit++;

				steps = ret.colit + ret.rowit;
				if (maxIterations > 0)
				{
					if (steps > maxIterations)
					{
						throw new IterationExceededException(
								"Limit of iterations exceeded: " + steps);
					}
				}

				// DEBUG
				if (Debug.BIPROP)
				{
					debtmp = "\nIteration: " + (ret.colit + ret.rowit) + "\n";
					debtmp += "Startmatrix vor Spaltenschritt: " + ret.colit + "\n";
					debtmp += printMatrixW(aktm);
					notifyMethodListeners(debtmp);
				}
				divObjs = new Divisor[cols];

				// Berechnung
				// Zuerst die Zuteilung
				divtmp = "";
				divmintmp = "";
				divnicetmp = "";
				divmaxtmp = "";
				sptc = "";
				for (int j = 0; j < cols; j++)
				{
					divObjs[j] = new Divisor();
					Weight[] aktcol = new Weight[rows];
					for (int i = 0; i < rows; i++)
					{
						aktcol[i] = aktm[i][j];
					}
					bm = Method.statPowMethod(sp, divObjs[j], colApp[j], aktcol, messenger, "");
					if (!bm)
					{
						bmes.setError(BipropLibMessenger.METHOD, "@colstep " + (ret.colit + 1) + " col " + j,
								new String[] { String.valueOf(ret.rowit + 1), String.valueOf(j) });
						throw new BipropException("@colstep " + (ret.colit + 1) + " col " + j);
					}


					// Update der Divisoren
					aktColDivs[j] = divObjs[j].getDivisor();
					aktColDivMin[j] = divObjs[j].getDivisorLow();
					aktColDivMax[j] = divObjs[j].getDivisorHigh();

					if (Debug.BIPROP)
					{
						StringBuffer temp_min = new StringBuffer("Dmin[" + j + "]: " + aktColDivMin[j] + "; ");
						StringBuffer temp_nor = new StringBuffer("   D[" + j + "]: " + aktColDivs[j] + "; ");
						StringBuffer temp_max = new StringBuffer("Dmax[" + j + "]: " + aktColDivMax[j] + "; ");
						int maxL = temp_min.length();
						if (maxL < temp_nor.length())
						{
							maxL = temp_nor.length();
						}
						if (maxL < temp_max.length())
						{
							maxL = temp_max.length();
						}
						for (int k = temp_min.length(); k < maxL; k++)
						{
							temp_min.append(" ");
						}
						for (int k = temp_nor.length(); k < maxL; k++)
						{
							temp_nor.append(" ");
						}
						for (int k = temp_max.length(); k < maxL; k++)
						{
							temp_max.append(" ");
						}
						divmintmp += temp_min.toString();
						divnicetmp += temp_nor.toString();
						divmaxtmp += temp_max.toString();
					}

					// plus-ties
					ptc[j] = divObjs[j].getPt();

					if (Debug.BIPROP)
					{
						sptc += "ptc[" + j + "]: " + ptc[j] + "; ";
					}
				}

				// Divisorupdate
				updateDivisors(colDivs, divObjs, fcol);

				// DEBUG
				if (Debug.BIPROP)
				{
					debtmp = "Zuteilung nach Iteration: " + (ret.colit + ret.rowit) + "  Spaltenschritt: " + ret.colit + "\n";
					debtmp += printMatrixR(aktm);
					notifyMethodListeners(debtmp);

					StringBuffer temp = new StringBuffer(divmintmp);
					temp.append("\n");
					temp.append(divnicetmp);
					temp.append("\n");
					temp.append(divmaxtmp);
					temp.append("\n\n");
					temp.append(sptc);
					temp.append("\nKumuliert: ");
					for (int j = 0; j < cols; j++)
					{
						temp.append("b[" + j + "]: " + colDivs[j] + "; ");
					}
					debtmp = "";
					divtmp = temp.toString();
					notifyMethodListeners(divtmp);
				}

				// Jetzt noch die Fehlstände prüfen
				for (int i = 0; i < rows; i++)
				{
					frow[i] = 0;
					for (int j = 0; j < cols; j++)
					{
						frow[i] += aktm[i][j].rdWeight;
					}
					frow[i] -= rowApp[i];
					if (Debug.BIPROP)
					{
						debtmp += "fr[" + i + "]: " + frow[i] + "; ";
					}
				}
				if (Debug.BIPROP)
				{
					notifyMethodListeners(debtmp);
				}

				// Bei entarteten Spalten kann ein Transfer zwischen Zeilen die Fehlstände korrigieren
				// -> "In tied cols..."
				// for (int j = 0; j < cols; j++) {
				// if (ptc[j] > -1) {
				// for (int i1 = 0; i1 < rows; i1++) {
				// for (int i2 = 0; i2 < rows; i2++) {
				// if ( (aktm[i1][j].multiple.equals("+")) &&
				// (aktm[i2][j].multiple.equals("-")) &&
				// (frow[i1] < 0) &&
				// (frow[i2] > 0)) {
				//
				// if (Debug.BIPROP) {
				// StringBuffer tempB = new StringBuffer("\nSitztransfer in Spalte: " + j);
				// tempB.append("\nSitz geht von Zeile i1: " + i1 + " nach i2: " + i2);
				// notifyMethodListeners(tempB.toString());
				// }
				//
				// aktm[i1][j].rdWeight += 1;
				// aktm[i2][j].rdWeight -= 1;
				// // Update
				// frow[i1] += 1;
				// frow[i2] -= 1;
				// // Reflag
				// aktm[i1][j].multiple = "-";
				// aktm[i2][j].multiple = "+";
				// }
				// }
				// }
				// }
				// }
				while (transfer(aktm, true));
				for (int i = 0; i < rows; i++)
				{
					int sum = 0;
					for (int j = 0; j < cols; j++)
						sum += aktm[i][j].rdWeight;
					frow[i] = sum - rowApp[i];
				}

				// jetzt noch fcount neu berechnen
				fcount = 0;
				for (int i = 0; i < rows; i++)
				{
					fcount += Math.max(0, frow[i]);
				}
				// DEBUG
				if (Debug.BIPROP)
				{
					sFlaws.append(fcount + "; ");
					if (lastFlaw < fcount)
					{
						rzahl++;
					}

					debtmp = "-- Zuteilung nach evtl. Korrektur in den Spalten --\n";
					debtmp += printMatrixR(aktm);
					debtmp += "Fehlstände: " + fcount;
					notifyMethodListeners(debtmp);
					debtmp = "";
				}

				if (hybrid != HYBRID_NONE && (lastFlaw == fcount))
				{
					usedLR = true;
					// Umschalten auf HybridLR
					Weight[][] paramMatrix = new Weight[rows][cols]; // Originalgewichte und letzte Zuteilung
					// added by S.M.
					// FAK050818
					for (int i = 0; i < rows; i++)
					{
						for (int j = 0; j < cols; j++)
						{
							paramMatrix[i][j] = theMatrix[i][j].clonew();
							paramMatrix[i][j].rdWeight = aktm[i][j].rdWeight;
						}
					}

					// Wähle TT Algorithmus aus
					if (Debug.BIPROP)
						notifyMethodListeners("\n\n--------------------------\nStarting switch to Hybrid\n\n");

					BipropRet tmpret = new BipropRet();
					AbstractBipropMethod ttmethod;
					try
					{
						switch (hybrid)
						{
						case HYBRID_TTINTE:
							TTinteMethod _TTinteMeth = new TTinteMethod(paramMatrix, rowApp, colApp, sp, bmes, tmpret);
							int[][] app = new int[paramMatrix.length][paramMatrix[0].length];
							for (int i = 0; i < paramMatrix.length; i++)
							{
								for (int j = 0; j < paramMatrix[i].length; j++)
								{
									app[i][j] = paramMatrix[i][j].rdWeight;
								}
							}
							this.calculateBRDivisors(paramMatrix, rowDivs, colDivs, sp);
							_TTinteMeth.setInitialApportionment(app, brRowDivs, brColDivs);

							for (int i = 0; i < this.methodListeners.size(); i++)
							{
								_TTinteMeth.addMethodListener(this.methodListeners.get(i));
							}
							tmpret = _TTinteMeth.calculate();
							break;
						case HYBRID_TTFLPT:
						default:
							ttmethod = new TTflptMethod(paramMatrix, rowApp, colApp, sp, bmes,
									tmpret,
									paramMatrix, rowDivs, colDivs);

							// Checks und Divisorpolitur ist nicht nötig
							ttmethod.checksAndPolish = false;
							tmpret = ttmethod.calculate();
						}
					}
					catch (BipropException be)
					{
						bmes.setError(BipropLibMessenger.COMMON, be.getMessage(), null);
						throw be;
					}

					if (tmpret != null)
					{
						if (tmpret.sError == null)
						{
							aktm = tmpret.app;
							rowDivs = tmpret.divRowNice;
							colDivs = tmpret.divColNice;
							ret.transfers = tmpret.transfers;
							ret.updates = tmpret.updates;
							lrSwitch = lastFlaw;
							fcount = 0;
							if (Debug.BIPROP)
							{
								debtmp = "\n-- Zuteilung nach HybridLR --\n";
								debtmp += printMatrixR(aktm);
							}
							// update ptr/ptc
							for (int i = 0; i < rows; i++)
							{
								int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
								for (int j = 0; j < cols; j++)
								{
									int flag = aktm[i][j].getFlag();
									if (flag < min)
										min = flag;
									if (flag > max)
										max = flag;
								}
								if ((min == -1) && (max == 1))
								{
									for (int j = 0; j < cols; j++)
									{
										if (aktm[i][j].getFlag() == 1)
											ptr[i] = j;
									}
								}
								else
								{
									for (int j = 0; j < cols; j++)
									{
										aktm[i][j].multiple = "";
									}
									ptr[i] = 0;
								}
							}
							for (int j = 0; j < cols; j++)
							{
								int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
								for (int i = 0; i < rows; i++)
								{
									int flag = aktm[i][j].getFlag();
									if (flag < min)
										min = flag;
									if (flag > max)
										max = flag;
								}
								if ((min == -1) && (max == 1))
								{
									for (int i = 0; i < rows; i++)
									{
										if (aktm[i][j].getFlag() == 0)
											ptc[j] = i;
									}
								}
								else
								{
									for (int i = 0; i < rows; i++)
									{
										aktm[i][j].multiple = "";
									}
									ptc[j] = 0;
								}
							}

						}
						else if (tmpret.sError.equals("Error using TTinte algorithm: Quotients not valid."))
						{
							bmes.setError(BipropLibMessenger.METHOD, "Bad Divisors while switching to Hybrid.", null);
							throw new BipropException(tmpret.sError);
						}
						else
						{
							System.out.println("Error: " + tmpret.sError);
							userInterrupted(ret, sFlaws, rzahl);
							bmes.setError(BipropLibMessenger.USER_ERROR,
									"User interrupted calculation @iteration " +
											(ret.rowit + ret.colit) +
											" during HybridLR calculation",
									new String[] { String.valueOf(ret.rowit + ret.colit) });
							throw new BipropException(
									"User interrupted calculation @iteration " +
											(ret.rowit + ret.colit) + " during HybridLR calculation");
						}
					}
				}
				lastFlaw = fcount;
				if (Debug.BIPROP)
				{
					notifyMethodListeners(debtmp);
				}
				/* ----------------------------------------------
				 * - ENDE SPALTENSCHRITT -
				 * ---------------------------------------------- */
			}
		}

		int steps = ret.colit + ret.rowit;
		fireIterationChanged(steps, true);

		/* 2008.02-b-02 Rausgenommen, da unnütz
		 * if (Debug.BIPROP) {
		 * debtmp = "ptr nach Iteration:\n";
		 * for (int i = 0; i < rows; i++) {
		 * debtmp += ptr[i] + " ";
		 * }
		 * debtmp += "\nptc nach Iteration:\n";
		 * for (int j = 0; j < cols; j++) {
		 * debtmp += ptc[j] + " ";
		 * }
		 * notifyMethodListeners(debtmp);
		 * } */

		/* ----------------------------------------------
		 * - ITERATION BEENDET -
		 * ---------------------------------------------- */

		/* aktm ist die Endmatrix und wird am Schluß zurückgegeben
		 * akt{Row|Col}Div{Min|Max} enthalten die jeweiligen Divisoren von der letzen
		 * Zuteilung. Das sind die C{min|max}_i bzw. D{min|max}_j aus dem Pseudocode
		 * d{Row|Col}Divs enthalten die akkumulierten Divisoren. Dies sind
		 * die a_i bzw. b_j aus dem Pseudocode */

		// Entferne ties
		removeUselessTies(aktm);

		// Falls TT benutzt wurde, müssen die Divisoren noch korrigiert werden
		if (usedLR)
		{
			try
			{
				reCalculateDivisors(aktm, rowDivs, colDivs);
			}
			catch (BipropException e)
			{
				bmes.setError(BipropLibMessenger.DIVISOR, e.getMessage(), null);
				throw e;
			}
		}

		// final checks
		if (checksAndPolish)
		{
			finalChecks(aktm, rowDivs, colDivs);
		}

		ret.timeElapsed = new Date().getTime() - start.getTime();

		ret.app = aktm;

		// Zeilen Divs/Mults
		ret.divRowMin = new double[rows];
		ret.divRowMax = new double[rows];
		ret.divRowNice = new double[rows];
		ret.mulRowMin = new double[rows];
		ret.mulRowMax = new double[rows];
		ret.mulRowNice = new double[rows];
		for (int i = 0; i < rows; i++)
		{
			ret.divRowMin[i] = dRowDivs[i].getDivisorLow();
			ret.divRowMax[i] = dRowDivs[i].getDivisorHigh();
			ret.divRowNice[i] = dRowDivs[i].getDivisor();
			ret.mulRowMin[i] = dRowDivs[i].getMultiplierLow();
			ret.mulRowMax[i] = dRowDivs[i].getMultiplierHigh();
			ret.mulRowNice[i] = dRowDivs[i].getMultiplier();
		}
		// Spalten Divs/Mults
		ret.divColMin = new double[cols];
		ret.divColMax = new double[cols];
		ret.divColNice = new double[cols];
		ret.mulColMin = new double[cols];
		ret.mulColMax = new double[cols];
		ret.mulColNice = new double[cols];
		for (int i = 0; i < cols; i++)
		{
			ret.divColMin[i] = dColDivs[i].getDivisorLow();
			ret.divColMax[i] = dColDivs[i].getDivisorHigh();
			ret.divColNice[i] = dColDivs[i].getDivisor();
			ret.mulColMin[i] = dColDivs[i].getMultiplierLow();
			ret.mulColMax[i] = dColDivs[i].getMultiplierHigh();
			ret.mulColNice[i] = dColDivs[i].getMultiplier();
		}

		ret.lrSwitch = lrSwitch;

		if (Debug.BIPROP)
		{
			notifyMethodListeners("Beende divMethMatrix...\n");
		}
		return ret;
	}

	private boolean transfer(Weight[][] C, boolean isRow)
	{
		HashSet<Integer> Iminus = new HashSet<Integer>();
		HashSet<Integer> Iplus = new HashSet<Integer>();

		int n = C.length, m = C[0].length;
		int[] app = isRow ? rowApp : colApp;

		if (!isRow)
		{
			int ti = n;
			n = m;
			m = ti;
			Weight[][] temp = new Weight[n][m];
			for (int i = 0; i < n; i++)
				for (int j = 0; j < m; j++)
					temp[i][j] = C[j][i];
			C = temp;
		}

		for (int i = 0; i < n; i++)
		{
			int sum = 0;
			for (int j = 0; j < m; j++)
				sum += C[i][j].rdWeight;
			if (sum < app[i])
				Iminus.add(i);
			else if (sum > app[i])
				Iplus.add(i);
		}

		HashSet<Integer> L = new HashSet<Integer>(Iminus);
		LinkedList<Integer> Q = new LinkedList<Integer>(L);
		int[] pre = new int[n + m];

		while (!Q.isEmpty())
		{
			int u = Q.pop();

			if (u < n)
			{
				int i = u;
				for (int j = 0; j < m; j++)
					if (!L.contains(j + n) && C[i][j].weight > 0 && C[i][j].multiple.equals("+"))
					{
						Q.add(j + n);
						L.add(j + n);
						pre[j + n] = i;
					}
			}
			else
			{
				int j = u - n;
				for (int i = 0; i < n; i++)
					if (!L.contains(i) && C[i][j].weight > 0 && C[i][j].multiple.equals("-"))
					{
						Q.add(i);
						L.add(i);
						pre[i] = j + n;
					}
			}
		} // end while (!Q.isEmpty())

		ArrayList<Integer> L_Iplus = new ArrayList<Integer>(L);
		L_Iplus.retainAll(Iplus);

		if (!L_Iplus.isEmpty())
		{
			int i = L_Iplus.get(0);
			do
			{
				int j = pre[i] - n;
				C[i][j].rdWeight--;
				C[i][j].multiple = "+";

				i = pre[j + n];
				C[i][j].rdWeight++;
				C[i][j].multiple = "-";
			}
			while (!Iminus.contains(i));
			return true;
		}
		return false;
	}

	/** Aktualisiert die Divisoren in dDivs mit den Divisoren in divObjs:
	 * 
	 * dDivs *= divNew
	 * 
	 * Wobei divNew je nach ausgewählter Methode (midpoint, random, extreme) aus divObjs gewählt wird
	 * 
	 * @param dDivs Aktuelle Divisoren
	 * @param divObjs Divisoren der letzten Berechnung
	 * @param faults Die Fehlstände des vorherigen Schritts */
	private void updateDivisors(double[] dDivs, Divisor[] divObjs, int[] faults)
	{
		switch (divisorUpdateMethod)
		{
		case DIV_MDPT:
			for (int i = 0; i < dDivs.length; i++)
				dDivs[i] *= divObjs[i].getDivisor();
			break;
		case DIV_RAND:
			Random rd = new Random();
			for (int i = 0; i < dDivs.length; i++)
				if (Double.isInfinite(divObjs[i].getDivisorHigh()))
					dDivs[i] *= rd.nextDouble() *
							(divObjs[i].getDivisor() - divObjs[i].getDivisorLow())
							+ divObjs[i].getDivisorLow();
				else
					dDivs[i] *= rd.nextDouble() *
							(divObjs[i].getDivisorHigh() - divObjs[i].getDivisorLow())
							+ divObjs[i].getDivisorLow();
			break;
		case DIV_EXTR:
			for (int i = 0; i < dDivs.length; i++)
			{
				if (faults[i] > 0)
					if (Double.isInfinite(divObjs[i].getDivisorHigh()))
						dDivs[i] *= divObjs[i].getDivisor();
					else
						dDivs[i] *= divObjs[i].getDivisorHigh();

				else if (faults[i] < 0)
					dDivs[i] *= divObjs[i].getDivisorLow();
				else
					dDivs[i] *= divObjs[i].getDivisor();
			}
		}
	}

	/** Methode die Divisoren fuer den TTinte neu berechnet. Hier treten manchmal Fehler aufgrund von
	 * numerischen Problemen auf.
	 * 
	 * @param weights
	 * @param rowDivs
	 * @param colDivs
	 * @param sp
	 * @throws BipropException */
	private void calculateBRDivisors(Weight[][] weights, double[] rowDivs, double[] colDivs, Signpost sp) throws BipropException
	{
		brRowDivs = new BigRational[rowDivs.length];
		brColDivs = new BigRational[colDivs.length];

		boolean globalProb = false;

		for (int i = 0; i < rowDivs.length; i++)
		{
			brRowDivs[i] = new BigRational(Convert.doubleToString(rowDivs[i]));
			for (int j = 0; j < colDivs.length; j++)
			{
				if (i == 0)
				{
					brColDivs[j] = new BigRational(Convert.doubleToString(colDivs[j]));
				}
				double value = weights[i][j].weight / (rowDivs[i] * colDivs[j]);
				double sp_value1 = sp.s(weights[i][j].rdWeight);
				double sp_value2 = sp.s(weights[i][j].rdWeight + 1);

				BigRational v1 = new BigRational(Convert.doubleToString(value));
				BigRational v2 = new BigRational(Convert.doubleToString(sp_value1));
				BigRational v3 = new BigRational(Convert.doubleToString(sp_value2));

				if ((v1.compareTo(v2) < 0) || (v1.compareTo(v3) > 0))
				{
					globalProb = true;
					if (i > 0)
					{
						break;
					}
				}
			}
		}

		if (!globalProb)
		{
			return;
		}

		// Spalten stimmen: berechne SpaltenDivisoren neu!
		BigRational[][] mat = new BigRational[weights.length][weights[0].length];
		for (int i = 0; i < mat.length; i++)
		{
			BigRational factor = brRowDivs[i];
			for (int j = 0; j < mat[i].length; j++)
			{
				BigRational t_weight = new BigRational(Convert.doubleToString(weights[i][j].weight));
				mat[i][j] = t_weight.div(factor);
			}
		}

		ExactSignPost ex_sp = TTinteMethod.createExactSignpost(sp);

		for (int i = 0; i < mat[0].length; i++)
		{
			BigRational[] weightVec = new BigRational[mat.length];
			BigInteger[] min = new BigInteger[mat.length];
			boolean min_restr = false;
			int houseSize = 0;
			for (int j = 0; j < mat.length; j++)
			{
				weightVec[j] = mat[j][i];
				min[j] = new BigInteger("" + weights[j][i].min);
				if (weights[j][i].min > 0)
				{
					min_restr = true;
				}
				houseSize += weights[j][i].rdWeight;
			}

			ExactDivMethod exact_dm = new ExactDivMethod
					(weightVec, new BigInteger("" + houseSize), ex_sp);
			if (min_restr)
			{
				exact_dm.setMinRestrictions(min);
			}
			brColDivs[i] = exact_dm.getMinDivisor();
		}
	}
}
