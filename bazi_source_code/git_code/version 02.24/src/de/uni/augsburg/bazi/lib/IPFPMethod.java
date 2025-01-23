/*
 * @(#)IPFPMethod.java 3.2 08/04/20
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Enumeration;
import java.util.Date;

/** <b>Überschrift:</b> Iterative Proportional Fitting Procedure<br>
 * <b>Beschreibung:</b> Implementiert die IPF Prozedur. Es werden abwechselnd die Zeilen
 * und die Spalten (stetig) skaliert, bis ein gewisser Fehler erreicht wird. Danach
 * wird die skalierte Matrix einem Tie&Transfer Algorithmus übergeben.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2008<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Robert Bertossi, Christian Brand
 * @version 3.2 */
public class IPFPMethod extends AbstractBipropMethod
{
	/* Versionshistorie:
	 * 2009.06-b-01: Version 3.2
	 * - Einführung der Versionshistorie
	 * - Überarbeitung der Methode calculate() Bei reiner Berechnung mit IPFP
	 * wird als Rundungsmethode immer DivStd verwendet */

	/** Verwende TTflpo */
	public static final int NONE = 0;

	/** Verwende TTflpo */
	public static final int TTFLPT = 1;

	/** Verwende TTinte */
	public static final int TTINTE = 2;

	/** Abbruchbedingung: IPF läuft solange, bis der Fehler diese Grenze unterschreitet */
	private double abErr = DEFAULT_ABERR;

	private static double DEFAULT_ABERR = 5.0;

	/** T&T Algorithmus, der beim Umschalten benutzt werden soll */
	private int ttAlgorithm = TTFLPT;

	/** Zeilenmultiplikatoren */
	private double[] rowMults;

	/** Spaltenmultiplikatoren */
	private double[] colMults;

	/** Entwicklung der Fehlstände (Debug) */
	StringBuffer sFlaws;

	private Weight[][] originalWeights;

	/** Setzt die Objekte, die für die Berechnung gebraucht werden und initialisiert die Divisoren.
	 * 
	 * @param weights Die Matrix mit den Gewichten
	 * @param aDistricts Die Sitze für die Distrikte
	 * @param aParties Die Sitze der Parteien
	 * @param aBmes Messenger Objekt, um Fehlermeldungen zu codieren
	 * @param aRet Biprop Rückgabeobjekt
	 * @param aSp Rundungsmethode
	 * @param ttAlgo Welcher TT Algorithmus soll benutzt werden?
	 * @throws BipropException Fehler bei Berechnung */
	public IPFPMethod(Weight[][] weights, int[] aDistricts,
			int[] aParties, Signpost aSp,
			BipropLibMessenger aBmes,
			BipropRet aRet,
			int ttAlgo) throws BipropException
	{
		super(weights, aDistricts, aParties, aSp, aBmes, aRet);

		originalWeights = new Weight[weights.length][];

		for (int i = 0; i < weights.length; i++)
		{
			originalWeights[i] = new Weight[weights[i].length];
			for (int j = 0; j < weights[i].length; j++)
			{
				originalWeights[i][j] = weights[i][j].clonew();
			}
		}

		ttAlgorithm = ttAlgo;
		if (ttAlgo == NONE)
		{
			abErr = PureIPFPHelper.getPureIPFPHelper().getError();
			// Bei reiner IPFP Berechnung muss eine durchlässige Rundungsmethode gewählt werden
			try
			{
				this.sp = new Stationary(0.5);
			}
			catch (ParameterOutOfRangeException e)
			{
				// Dies sollte nicht passieren
				System.err.println("Fehler beim Setzen eines Stationary!");
				e.printStackTrace();
			}
		}
		else
		{
			abErr = DEFAULT_ABERR;
		}
		rowMults = new double[rows];
		colMults = new double[cols];
	}

	/** Skaliert iterativ die Gewichtsmatrix stetig (erst Zeilen, dann Spalten),
	 * solange bis ein gewisser Fehler unterschritten wird. Danach wird die
	 * skalierte Matrix einem TT Algorithmus übergeben.
	 * 
	 * @return Ergebnis der Berechnung
	 * @throws BipropException Fehler bei der Initialisierung oder Berechnung */
	public BipropRet calculate() throws BipropException
	{
		Date start = new Date();
		// Diverse Checks IPF23
		if (checksAndPolish && !checkExistence())
			return ret;

		// Initilisierung IPF25-IPF32
		// Divisoren
		for (int i = 0; i < rows; i++)
			rowMults[i] = 1.0;
		for (int j = 0; j < cols; j++)
			colMults[j] = 1.0;

		// "Fehlstände" initialisieren
		double err = abErr + 1.0;

		double tmpMult;
		double tmpSum;

		// Arbeitsmatrix
		Weight[][] aktm = new Weight[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				aktm[i][j] = theMatrix[i][j].clonew();

		// Infos für Debug-Zwecke
		// Entwicklung der Fehlstände
		sFlaws = new StringBuffer("");
		StringBuffer sRowMults = new StringBuffer("");
		StringBuffer sColMults = new StringBuffer("");
		StringBuffer debtmp = new StringBuffer("");

		// IPF Algorithmus IPF34-IPF58
		while (err > abErr)
		{

			// Debug
			if (debug)
			{
				sRowMults.delete(0, sRowMults.length());
				sColMults.delete(0, sColMults.length());
			}

			// Benutzerabbruch?
			if (Thread.interrupted())
			{
				userInterrupted(ret, sFlaws, 0);
				bmes.setError(BipropLibMessenger.USER_ERROR,
						"User interrupted calculation @iteration " +
								(ret.rowit + ret.colit),
						new String[] { String.valueOf(ret.rowit + ret.colit) });
				throw new BipropException("User interrupted calculation @iteration " +
						(ret.rowit + ret.colit));
			}

			// Zeilenschritt IPF35-IPF42
			ret.rowit++;
			for (int i = 0; i < rows; i++)
			{
				// Summe der aktuellen Zeile
				tmpSum = 0;
				for (int j = 0; j < cols; j++)
					tmpSum += aktm[i][j].weight;

				// Update Multiplikator
				tmpMult = rowApp[i] / tmpSum;
				rowMults[i] = rowMults[i] * tmpMult;

				if (debug)
				{
					sRowMults.append(rowMults[i] + "; ");
				}

				// Update der skalierten Gewichte
				for (int j = 0; j < cols; j++)
					aktm[i][j].weight *= tmpMult;
			}
			// Ende Zeilenschritt

			// Fehlstände IPF44
			err = 0d;
			for (int j = 0; j < cols; j++)
			{
				tmpSum = 0;
				for (int i = 0; i < rows; i++)
					tmpSum += aktm[i][j].weight;
				err += Math.abs(tmpSum - colApp[j]);
			}
			err *= 0.5;
			// Ende Fehlstände

			// Debug
			if (debug)
			{
				sFlaws.append(err + "; ");
			}

			// Spaltenschritt IPF46-IPF55
			if (err > abErr)
			{
				ret.colit++;
				for (int j = 0; j < cols; j++)
				{
					// Summe der aktuellen Spalte
					tmpSum = 0;
					for (int i = 0; i < rows; i++)
						tmpSum += aktm[i][j].weight;

					// Update Multiplikator
					tmpMult = colApp[j] / tmpSum;
					colMults[j] = colMults[j] * tmpMult;

					if (debug)
					{
						sColMults.append(colMults[j] + "; ");
					}

					// Update der skalierten Gewichte
					for (int i = 0; i < rows; i++)
						aktm[i][j].weight *= tmpMult;
				}

				// Fehlstände IPF57
				err = 0;
				for (int i = 0; i < rows; i++)
				{
					tmpSum = 0;
					for (int j = 0; j < cols; j++)
						tmpSum += aktm[i][j].weight;
					err += Math.abs(tmpSum - rowApp[i]);
				}
				err *= 0.5;
				// Ende Fehlstände

				if (debug)
				{
					sFlaws.append(err + "; ");
				}
			}
			// Ende Spaltenschritt

			// IterationListener
			if ((ret.rowit + ret.colit) % 50 == 0)
			{
				fireIterationChanged(ret.rowit + ret.colit, false);
			}

			// Debug
			if (debug)
			{
				debtmp = new StringBuffer();
				debtmp.append("\nIteration: " + (ret.colit + ret.rowit));
				debtmp.append("\nMatrix\n");
				debtmp.append(printMatrixW(aktm));
				debtmp.append("RowMults: \n" + sRowMults + "\n");
				debtmp.append("ColMults: \n" + sColMults + "\n\n");
				notifyMethodListeners(debtmp.toString());
			}
		}
		// Ende IPF Algorithmus

		// IterationListener --> wird bei TTinte selbst gehandhabt (HACK)
		fireIterationChanged(ret.rowit + ret.colit, true);

		if (debug)
		{
			debtmp = new StringBuffer();
			debtmp.append("Entwicklung der Fehlstände:\n");
			debtmp.append(sFlaws.toString());
			notifyMethodListeners(debtmp.toString());
		}

		// Die Gewichtsmatrix wird nun einem T&T Algorithmus übergeben
		// Multiplikatoren -> Divisoren
		double[] rowDivs = multsToDivs(rowMults);
		double[] colDivs = multsToDivs(colMults);

		BipropRet tmpRet = new BipropRet();

		switch (ttAlgorithm)
		{
		case TTFLPT:
			TTflptMethod flpt = new TTflptMethod(theMatrix, rowApp, colApp, sp, bmes, tmpRet,
					null, rowDivs, colDivs);
			for (Enumeration<MethodListener> e = methodListeners.elements(); e.hasMoreElements();)
				flpt.addMethodListener(e.nextElement());

			// Checks und Divisorpolitur ist nicht nötig
			flpt.checksAndPolish = false;

			tmpRet = flpt.calculate();

			aktm = tmpRet.app;
			ret.transfers = tmpRet.transfers;
			ret.updates = tmpRet.updates;
			ret.ties = tmpRet.ties;
			ret.sError = tmpRet.sError;

			// Divisoren übernehmen
			try
			{
				for (int i = 0; i < rows; i++)
				{
					dRowDivs[i] = new Divisor();
					dRowDivs[i].setDivisorInterval(tmpRet.divRowMin[i], tmpRet.divRowMax[i]);
					dRowDivs[i].setMultiplierInterval(tmpRet.mulRowMin[i], tmpRet.mulRowMax[i]);
				}
				for (int j = 0; j < cols; j++)
				{
					dColDivs[j] = new Divisor();
					dColDivs[j].setDivisorInterval(tmpRet.divColMin[j], tmpRet.divColMax[j]);
					dColDivs[j].setMultiplierInterval(tmpRet.mulColMin[j], tmpRet.mulColMax[j]);
				}
			}
			catch (DivisorException e)
			{
				throw new BipropException("Fehler beim Aktualisieren der Divisoren nach TTflpt");
			}
			break;
		case TTINTE:
			TTinteMethod exactBiprop = new TTinteMethod(theMatrix, rowApp, colApp, sp, bmes, tmpRet);
			exactBiprop.setInitialRowDivisors(rowDivs);
			// TTinteMethod exactBiprop = new TTinteMethod(originalWeights, rowApp, colApp, sp, bmes, tmpRet);
			for (Enumeration<MethodListener> e = methodListeners.elements(); e.hasMoreElements();)
				exactBiprop.addMethodListener(e.nextElement());

			// Checks und Divisorpolitur ist nicht nötig
			exactBiprop.checksAndPolish = false;

			tmpRet = exactBiprop.calculate();

			aktm = tmpRet.app;
			ret.transfers = tmpRet.transfers;
			ret.updates = tmpRet.updates;
			ret.ties = tmpRet.ties;
			ret.sError = tmpRet.sError;

			// Divisoren übernehmen
			try
			{
				for (int i = 0; i < rows; i++)
				{
					dRowDivs[i] = new Divisor();
					/*dRowDivs[i].setDivisorInterval(tmpRet.divRowMin[i] / rowMults[i],
					 * tmpRet.divRowMax[i] / rowMults[i]);
					 * dRowDivs[i].setMultiplierInterval(rowMults[i] * tmpRet.mulRowMin[i],
					 * rowMults[i] * tmpRet.mulRowMax[i]); */
					dRowDivs[i].setDivisorInterval(tmpRet.divRowMin[i], tmpRet.divRowMax[i]);
					dRowDivs[i].setMultiplierInterval(tmpRet.mulRowMin[i], tmpRet.mulRowMax[i]);
				}
				for (int j = 0; j < cols; j++)
				{
					dColDivs[j] = new Divisor();
					/*dColDivs[j].setDivisorInterval(tmpRet.divColMin[j] / colMults[j],
					 * tmpRet.divColMax[j] / colMults[j]);
					 * dColDivs[j].setMultiplierInterval(colMults[j] * tmpRet.mulColMin[j],
					 * colMults[j] * tmpRet.mulColMax[j]); */
					dColDivs[j].setDivisorInterval(tmpRet.divColMin[j], tmpRet.divColMax[j]);
					dColDivs[j].setMultiplierInterval(tmpRet.mulColMin[j], tmpRet.mulColMax[j]);
				}
			}
			catch (DivisorException e)
			{
				throw new BipropException("Fehler beim Aktualisieren der Divisoren nach TTinte");
			}
			break;
		case NONE:
			try
			{
				for (int i = 0; i < rows; i++)
				{
					dRowDivs[i] = new Divisor();
					dRowDivs[i].setDivisorInterval(rowDivs[i], rowDivs[i]);
					dRowDivs[i].setMultiplierInterval(1d / rowDivs[i], 1d / rowDivs[i]);
				}
				for (int j = 0; j < cols; j++)
				{
					dColDivs[j] = new Divisor();
					dColDivs[j].setDivisorInterval(colDivs[j], colDivs[j]);
					dColDivs[j].setMultiplierInterval(1d / colDivs[j], 1d / colDivs[j]);
				}
				PureIPFPHelper.getPureIPFPHelper().setRealError(err);
			}
			catch (DivisorException e)
			{
				throw new BipropException("Fehler beim Aktualisieren der Divisoren nach TTinte");
			}
			break;
		}

		ret.app = aktm;

		// Divisoren extrahieren
		rowDivs = new double[rows];
		colDivs = new double[cols];

		for (int d = 0; d < dRowDivs.length; d++)
		{
			rowDivs[d] = dRowDivs[d].getDivisor();
		}

		for (int p = 0; p < dColDivs.length; p++)
		{
			colDivs[p] = dColDivs[p].getDivisor();
		}

		// final checks nur wenn während der TT Berechnung kein Fehler aufgetrten ist
		if (checksAndPolish && ret.sError == null && this.ttAlgorithm != NONE)
			finalChecks(ret.app, rowDivs, colDivs);

		ret.timeElapsed = new Date().getTime() - start.getTime();

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

		ret.lrSwitch = err;

		return ret;
	}

	/** Wandelt ein Multiplikatoren Array in ein Divisoren Array um.
	 * @param mults Multiplikatoren
	 * @return Divisoren */
	private double[] multsToDivs(double[] mults)
	{
		double[] divs = new double[mults.length];
		for (int i = 0; i < mults.length; i++)
			divs[i] = mults[i] != 0 ? 1 / mults[i] : Double.POSITIVE_INFINITY;
		return divs;
	}

}
