/*
 * @(#)TTinteMethod.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.math.BigInteger;
import java.util.Date;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.lib.exactbiprop.BigRational;
import de.uni.augsburg.bazi.lib.exactbiprop.ExactBipropDivMethod;
import de.uni.augsburg.bazi.lib.exactbiprop.ExactSignPost;

/** <b>Überschrift:</b> Adapter für die Biprop-Berechnung von Martin Zachariasen<br>
 * <b>Beschreibung:</b> <code>http://www.diku.dk/~martinz/biprop/</code><br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Robert Bertossi, Christian Brand
 * @version 3.1 */
public class TTinteMethod extends AbstractBipropMethod
{

	private static Logger logger = Logger.getLogger(TTinteMethod.class);
	private int[][] initialApportionment;
	private BigRational[] rowDivs;
	private BigRational[] colDivs;
	private boolean initialApportionmentIsGiven = false;
	private boolean rowDivisorsGiven = false;

	/** ExactBipropDivMethod Objekt, mit dem gearbeitet wird */
	private ExactBipropDivMethod exactBiprop = null;

	public TTinteMethod(Weight[][] weights, int[] aDistricts, int[] aParties,
			Signpost aSp, BipropLibMessenger aBmes, BipropRet aRet) throws
			BipropException
	{
		super(weights, aDistricts, aParties, aSp, aBmes, aRet);
	}

	/** Generiere Debug Informationen über die Parameter an ExactBipropDivMethod */
	private void postDebugBefore()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------------------\n" +
				"Parameters for ExactBipropDivMethod:\n\n");

		sb.append("Signpost: " + exactBiprop.getSP().getStationary_Param() + "\n\n");

		sb.append("Districts:\n");
		BigInteger[] bi = exactBiprop.getRowSums();
		for (int i = 0; i < bi.length; i++)
			sb.append(bi[i] + ";");
		sb.append("\n\n");

		sb.append("Parties:\n");
		bi = exactBiprop.getColSums();
		for (int i = 0; i < bi.length; i++)
			sb.append(bi[i] + ";");
		sb.append("\n\n");

		sb.append("Weights per district:\n");
		BigRational[][] brm = exactBiprop.getWeights();
		for (int i = 0; i < brm.length; i++)
		{
			for (int j = 0; j < brm[0].length; j++)
				sb.append(brm[i][j] + ";");
			sb.append("\n");
		}
		sb.append("\n\n");

		if (initialApportionmentIsGiven)
		{
			sb.append("Initial Apportionment:\n");
			for (int i = 0; i < initialApportionment.length; i++)
			{
				for (int j = 0; j < initialApportionment[0].length; j++)
				{
					sb.append(initialApportionment[i][j] + ";");
				}
				sb.append("\n");
			}
			sb.append("\n\n");

			sb.append("Row Divisors: \n");
			for (int i = 0; i < rowDivs.length; i++)
			{
				sb.append(rowDivs[i] + "\n");
			}
			sb.append("\n\n");

			sb.append("Col Divisors: \n");
			for (int i = 0; i < colDivs.length; i++)
			{
				sb.append(colDivs[i] + "\n");
			}
			sb.append("\n\n");

			initialApportionmentIsGiven = false;
		}
		notifyMethodListeners(sb.toString());
	}

	/** Berechne und liefere die Ergebnisse.
	 * 
	 * @return BipropRet
	 * 
	 * @throws BipropException */
	@Override
	public BipropRet calculate() throws BipropException
	{

		ret.app = theMatrix;

		// Divisoren und Multiplikatoren initialisieren
		// (um Nullpointer zu vermeiden, falls es einen Fehler in der Berechnung gibt)
		ret.divColMax = new double[theMatrix[0].length];
		ret.divColMin = new double[theMatrix[0].length];
		ret.divColNice = new double[theMatrix[0].length];
		ret.divRowMax = new double[theMatrix.length];
		ret.divRowMin = new double[theMatrix.length];
		ret.divRowNice = new double[theMatrix.length];

		ret.mulColMax = new double[theMatrix[0].length];
		ret.mulColMin = new double[theMatrix[0].length];
		ret.mulColNice = new double[theMatrix[0].length];
		ret.mulRowMax = new double[theMatrix.length];
		ret.mulRowMin = new double[theMatrix.length];
		ret.mulRowNice = new double[theMatrix.length];

		Date start = new Date();
		// Diverse Checks IPF23
		if (checksAndPolish && !checkExistence())
			return ret;

		try
		{

			// Initialisiere ExactBipropDivMethod
			double[][] ws = new double[rowApp.length][colApp.length];
			int[][] minRestr = new int[rowApp.length][colApp.length];
			boolean anyMinRestrictions = false;
			for (int d = 0; d < rowApp.length; d++)
				for (int p = 0; p < colApp.length; p++)
				{
					ws[d][p] = theMatrix[d][p].weight;
					minRestr[d][p] = theMatrix[d][p].min;
					if (theMatrix[d][p].min > 0)
					{
						anyMinRestrictions = true;
					}
				}
			ExactSignPost exSp;
			try
			{
				exSp = createExactSignpost(sp);
			}
			catch (BipropException be)
			{
				bmes.setError(BipropLibMessenger.INPUT_ERROR, be.getMessage(), null);
				throw be;
			}
			// exactBiprop = new ExactBipropDivMethod(ws, rowApp, colApp, exSp);
			exactBiprop = new ExactBipropDivMethod(ws, rowApp, colApp, exSp);
			if (initialApportionmentIsGiven)
			{
				exactBiprop.setInitialApportionment(initialApportionment);
				exactBiprop.setInitialRowDivisors(rowDivs);
				exactBiprop.setInitialColumnDivisors(colDivs);

				if (!debug)
				{
					initialApportionmentIsGiven = false;
				}
			}
			else if (rowDivisorsGiven)
			{
				exactBiprop.setInitialRowDivisors(rowDivs);
			}
			if (anyMinRestrictions)
			{
				exactBiprop.setMinRestrictions(minRestr);
			}

			// Debug
			if (debug)
				postDebugBefore();

			BigInteger[][] app = exactBiprop.getApportionment();
			BigRational[] rowDivsBigRat = exactBiprop.getRowDivisors();
			BigRational[] colDivsBigRat = exactBiprop.getColumnDivisors();

			// Divisoren extrahieren
			double[] rowDivs = new double[rows];
			double[] colDivs = new double[cols];

			for (int d = 0; d < rowDivsBigRat.length; d++)
			{
				rowDivs[d] = bigRatToDouble(rowDivsBigRat[d]);
				for (int p = 0; p < colDivsBigRat.length; p++)
					ret.app[d][p].rdWeight = app[d][p].intValue();
			}

			for (int p = 0; p < colDivsBigRat.length; p++)
			{
				colDivs[p] = bigRatToDouble(colDivsBigRat[p]);
			}

			// Divisoren übernehmen
			try
			{
				for (int i = 0; i < rows; i++)
				{
					dRowDivs[i] = new Divisor();
					dRowDivs[i].setDivisorInterval(bigRatToDouble(rowDivsBigRat[i]),
							bigRatToDouble(rowDivsBigRat[i]));
					dRowDivs[i].setMultiplierInterval(1d / bigRatToDouble(rowDivsBigRat[i]),
							1d / bigRatToDouble(rowDivsBigRat[i]));
				}
				for (int j = 0; j < cols; j++)
				{
					dColDivs[j] = new Divisor();
					dColDivs[j].setDivisorInterval(bigRatToDouble(colDivsBigRat[j]),
							bigRatToDouble(colDivsBigRat[j]));
					dColDivs[j].setMultiplierInterval(1d / bigRatToDouble(colDivsBigRat[j]),
							1d / bigRatToDouble(colDivsBigRat[j]));
				}
			}
			catch (DivisorException e)
			{
				throw new BipropException("Fehler beim übernehmen der Divisoren");
			}

			// Ties
			int[][] ties = exactBiprop.getTies();
			for (int d = 0; d < rowDivsBigRat.length; d++)
				for (int p = 0; p < colDivsBigRat.length; p++)
					switch (ties[d][p])
					{
					case 0:
						ret.app[d][p].multiple = "";
						break;
					case 1:
						ret.app[d][p].multiple = "+";
						ret.ties = true;
						break;
					case -1:
						ret.app[d][p].multiple = "-";
						ret.ties = true;
						break;
					}

			// final checks
			if (checksAndPolish)
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

			// Transfers und Updates
			ret.transfers = exactBiprop.getNumberOfTransfers();
			ret.updates = exactBiprop.getNumberOfUpdates();

			fireIterationChanged(-1, true);

			if (debug)
				postDebugAfter();

			return ret;
		}
		catch (BipropException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			// e.printStackTrace(System.out);
			ret.sError = "Error using TTinte algorithm: " + e.getMessage();
			if (bmes != null)
				bmes.setError(BipropLibMessenger.COMMON, "Error using TTinte algorithm: " + e.getMessage(), null);
			// throw new BipropException(ret.sError);
			return ret;
		}

	}

	/** Generiere Debug Informationen über das Ergebnis von ExactBipropDivMethod */
	private void postDebugAfter()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------------------\n" +
				"Results for ExactBipropDivMethod:\n\n");

		sb.append("District divisors:\n");
		BigRational[] br = exactBiprop.getRowDivisors();
		for (int d = 0; d < br.length; d++)
			sb.append(br[d] + ";");
		sb.append("\n\n");

		sb.append("District divisors:\n");
		br = exactBiprop.getColumnDivisors();
		for (int p = 0; p < br.length; p++)
			sb.append(br[p] + ";");
		sb.append("\n\n");

		sb.append("Apportionment per district:\n");
		BigInteger[][] bim = exactBiprop.getApportionment();
		for (int d = 0; d < bim.length; d++)
		{
			for (int p = 0; p < bim[0].length; p++)
				sb.append(bim[d][p] + ";");
			sb.append("\n");
		}
		sb.append("\n");

		sb.append("Ties per district:\n");
		int[][] im = exactBiprop.getTies();
		for (int d = 0; d < im.length; d++)
		{
			for (int p = 0; p < im[0].length; p++)
				sb.append(im[d][p] + ";");
			sb.append("\n");
		}
		sb.append("\n\n");

		notifyMethodListeners(sb.toString());

	}

	/** Wandelt einen BigRational in einen double um.
	 * @param br BigRational
	 * @return double */
	public static double bigRatToDouble(BigRational br)
	{
		return new Double(br.toStringDot(16));
	}

	/** Wandelt ein Divisor Array in ein Multiplikatoren Array um.
	 * @param divs Divisoren
	 * @return Multiplikatoren */
	public static double[] divsToMults(double[] divs)
	{
		double[] mults = new double[divs.length];
		for (int i = 0; i < divs.length; i++)
			mults[i] = divs[i] != 0 ? 1 / divs[i] : Double.POSITIVE_INFINITY;
		return mults;
	}

	/** Wandelt einen Signpost in einen ExactSignPost um.
	 * 
	 * @param sp Signpost
	 * @return ExactSignPost
	 * @throws BipropException */
	public static ExactSignPost createExactSignpost(Signpost sp) throws BipropException
	{
		if (sp instanceof Stationary)
		{
			String help = Convert.doubleToString(sp.getParam()).replace(',', '.');
			return new ExactSignPost(new BigRational(help));
		}
		else if (sp instanceof PowerMean)
		{
			PowerMean pm = (PowerMean) sp;
			double param = pm.getParam();
			// geometrische Rundung / Hill-Huntigton
			if (param == 0.0)
			{
				return new ExactSignPost(ExactSignPost.HILL_ROUNDING, new BigRational(new java.math.BigInteger("10")));
			}
			else if (param == -1.0)
			{
				return new ExactSignPost(ExactSignPost.DEAN_ROUNDING, new BigRational(new java.math.BigInteger("10")));
			}
			else
			{
				// andere Power Mean Methoden werden noch nicht unterstuetzt!
				logger.error("Nicht unterstützte Power Mean Methode: " + pm.getName());
				throw new BipropException("Nicht unterstützte Power Mean Methode: " + pm.getName());
			}
		}
		else
		{
			// Fehler: keine gueltige Signpost Klasse
			logger.error("Fehler in der Rundungsmethode gefunden. (Weder Stationary noch Signpost!): " + sp.getClass().getName());
			throw new BipropException("fehlerhafte Rundungsmethode!");
		}
	}
	public void setInitialApportionment(int[][] app, BigRational[] rDiv, BigRational[] cDiv)
	{
		initialApportionment = app;
		rowDivs = rDiv;
		colDivs = cDiv;
		initialApportionmentIsGiven = true;
	}

	public void setInitialRowDivisors(double[] rowDivs)
	{
		rowDivisorsGiven = true;

		this.rowDivs = new BigRational[rowDivs.length];
		for (int i = 0; i < rowDivs.length; i++)
		{
			this.rowDivs[i] = new BigRational(Convert.doubleToString(rowDivs[i]));
		}
	}


}
