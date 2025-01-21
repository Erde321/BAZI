/*
 * @(#)TTflptMethod.java 3.1 19/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Date;

import de.uni.augsburg.bazi.lib.vector.Method;

/** <b>Überschrift:</b> Tie&Transfer floating point arithmetic<br>
 * <b>Beschreibung:</b> Implementiert den TTflpt<br>
 * Structure of the implementation inspired by Martin Zachariasen's ExactBipropMethod <code>http://www.diku.dk/~martinz/biprop/</code> <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Robert Bertossi, Christian Brand
 * @version 3.1 */
public class TTflptMethod extends AbstractBipropMethod
{

	/** Aktuelle Zeilendivisoren (Distrikte) */
	private double[] rowDivs = null;

	/** Aktuelle Spaltendivisoren (Spalten) */
	private double[] colDivs = null;

	/** Aktuelle Gewichtsmatrix */
	private Weight[][] aktWeights = null;

	/** Transfers */
	private int transfers = 0;

	/** Updates */
	private int updates = 0;

	/** Fehlstände der Zeilen */
	private int[] fRow = new int[rows];

	/** Summe aller Beträge der Fehlstände in den einzelnen Zeilen */
	private int fRowSum = 0;

	/** Fehlstände der Spalten */
	private int[] fCol = new int[cols];

	/** Summe aller Beträge der Fehlstände in den einzelnen Spalten */
	private int fColSum = 0;


	/** Setzt die Objekte, die für die Berechnung gebraucht werden und initialisiert die Divisoren.
	 * 
	 * Für die Stand-Alone Benutzung.
	 * 
	 * @param weights Die Matrix mit den Gewichten
	 * @param aDistricts Die Sitze für die Distrikte
	 * @param aParties Die Sitze der Parteien
	 * @param aBmes Messenger Objekt, um Fehlermeldungen zu codieren
	 * @param aRet Biprop Rückgabeobjekt
	 * @param aSp Rundungsmethode
	 * @throws BipropException Fehler bei Berechnung */
	public TTflptMethod(Weight[][] weights, int[] aDistricts,
			int[] aParties, Signpost aSp,
			BipropLibMessenger aBmes,
			BipropRet aRet) throws BipropException
	{
		super(weights, aDistricts, aParties, aSp, aBmes, aRet);
	}

	/** Setzt die Objekte, die für die Berechnung gebraucht werden und
	 * initialisiert die Divisoren. Für die Hybrid Benutzung. Wenn hier eine
	 * Startzuteilung gegeben wird, dürfen deren Spaltensummen keine Fehler
	 * aufweisen.
	 * 
	 * @param weights Die Matrix mit den Gewichten
	 * @param aDistricts Die Sitze für die Distrikte
	 * @param aParties Die Sitze der Parteien
	 * @param aSp Rundungsmethode
	 * @param aBmes Messenger Objekt, um Fehlermeldungen zu codieren
	 * @param aRet Biprop Rückgabeobjekt
	 * @param startWeights Startzuteilung
	 * @param startRowDivs Start Zeilendivisoren
	 * @param startColDivs Start Spaltendivisoren
	 * @throws BipropException Fehler bei Berechnung */
	public TTflptMethod(Weight[][] weights, int[] aDistricts,
			int[] aParties, Signpost aSp,
			BipropLibMessenger aBmes,
			BipropRet aRet,
			Weight[][] startWeights,
			double[] startRowDivs, double[] startColDivs) throws BipropException
	{
		this(weights, aDistricts, aParties, aSp, aBmes, aRet);

		rowDivs = startRowDivs;
		colDivs = startColDivs;

		if (startWeights != null)
		{
			aktWeights = new Weight[startWeights.length][startWeights[0].length];
			for (int i = 0; i < aktWeights.length; i++)
				for (int j = 0; j < aktWeights[0].length; j++)
					aktWeights[i][j] = startWeights[i][j].clonew();
		}

		if (startWeights != null && (startRowDivs == null || startColDivs == null))
		{
			bmes.setError(BipropLibMessenger.INPUT_ERROR, "A starting apportionment also needs starting divisors", new String[] { "" });
			throw new BipropException(
					"A starting apportionment also needs starting divisors");
		}
	}

	/** Berechnet die Zuteilung nach dem TTflpt Algorithmus.
	 * 
	 * @return Ergebnis der Berechnung
	 * @throws BipropException Fehler bei der Initialisierung oder Berechnung */
	public BipropRet calculate() throws BipropException
	{

		if (debug)
			notifyMethodListeners("Starte TTflpt...");

		Date start = new Date();
		// Diverse Checks der Existenz; nur Nötig falls die Berechnung ganz von vorne beginnt
		// bei aktWeights != null wird angenommen, dass diese Checks schon gemacht worden sind
		if (checksAndPolish && aktWeights == null && !checkExistence())
			return ret;

		// Genauigkeit der Vergleiche beim Aufsprüren von Sprungstellen
		int acc = 11;
		// acc wird bei mehr als 10^6 auf einen kleineren Wert gesetzt
		int availSeats = 0;
		for (int i = 0; i < rowApp.length; i++)
			availSeats += rowApp[i];
		if (availSeats > Math.pow(10, 6))
			acc = 8;

		// Initialisierung
		// Startzuteilung TTn48-86
		if (aktWeights == null)
		{
			// Es wurde keine Startzuteilung gegeben. Es wird nun eine Zuteilung berechnet,
			// bei denen die Spaltensummen korrekt sind

			// Start Divisoren
			if (rowDivs == null || colDivs == null)
			{
				rowDivs = new double[rows];
				colDivs = new double[cols];
				for (int i = 0; i < rows; i++)
					rowDivs[i] = 1.0;
				for (int j = 0; j < cols; j++)
					colDivs[j] = 1.0;
			}
			// else Die Divisoren wurden im Konstruktor vorbelegt

			// Kopiere die Original Matrix und passe die Geichte mit den Divisoren an
			aktWeights = new Weight[rows][cols];
			for (int i = 0; i < rows; i++)
				for (int j = 0; j < cols; j++)
				{
					aktWeights[i][j] = theMatrix[i][j].clonew();
					aktWeights[i][j].weight /= (rowDivs[i] * colDivs[j]);
				}
			// Passe die Spalten an
			Divisor tmpDiv;
			LibMessenger libmsg = new LibMessenger();
			// Method method = new Method(null, new Divisor(), libmsg);

			for (int j = 0; j < cols; j++)
			{
				tmpDiv = new Divisor();
				Weight[] aktcol = new Weight[rows];
				for (int i = 0; i < rows; i++)
				{
					aktcol[i] = aktWeights[i][j];
				}
				// try {
				// alt: method.divMethVector(sp, tmpDiv, colApp[j], aktcol,
				// libmsg);
				Method.statPowMethod(sp, tmpDiv, colApp[j], aktcol, libmsg, "");
				// }
				// catch (MethodException e) {
				// bmes.setError(BipropLibMessenger.METHOD,
				// "@colstep " + (ret.colit + 1) + " col " + j,
				// new String[] {String.valueOf(ret.rowit + 1),
				// String.valueOf(j)});
				// throw new BipropException("@colstep " + (ret.colit + 1) + " col " +
				// j, e);
				// }
				// Update der Divisoren
				colDivs[j] *= tmpDiv.getDivisor();
			}

		}
		else
		{
			// Start Zuteilung wurde gegeben -> dann müssen die Start Divisoren passen
			// und die Spaltensummen dürfen keine Fehler aufweisen

			// Gewichte der aktuellen Matrix skalieren
			for (int i = 0; i < rows; i++)
				for (int j = 0; j < cols; j++)
					aktWeights[i][j].weight = theMatrix[i][j].weight /
							(rowDivs[i] * colDivs[j]);

			// Prüfe ob die Spaltensummen passen
			calculateFaults();
			if (fColSum > 0)
			{
				bmes.setError(BipropLibMessenger.INPUT_ERROR,
						"Columns aren't fitted in starting apportionment",
						new String[] { "" });
				throw new BipropException(
						"Columns aren't fitted in starting apportionment");
			}

			// Check ob die Sprungstellen eingehalten werden
			/* for (int i = 0; i < rows; i++) {
			 * for (int j = 0; j < cols; j++) {
			 * if (Method.expDif(aktWeights[i][j].weight,
			 * sp.s(aktWeights[i][j].weight),
			 * acc)) {
			 * //weight steht auf einer Sprungstelle
			 * if (aktWeights[i][j].rdWeight !=
			 * sp.signpostRound(aktWeights[i][j].weight) &&
			 * aktWeights[i][j].rdWeight - 1 !=
			 * sp.signpostRound(aktWeights[i][j].weight)) {
			 * bmes.setError(BipropLibMessenger.INPUT_ERROR,
			 * "Start apportionment does not correspond to divisors.",
			 * new String[] {""});
			 * throw new BipropException(
			 * "Start apportionment does not correspond to divisors.");
			 * }
			 * }
			 * else{
			 * //weight steht nicht auf einer Sprungstelle
			 * if (aktWeights[i][j].rdWeight !=
			 * sp.signpostRound(aktWeights[i][j].weight) ){
			 * bmes.setError(BipropLibMessenger.INPUT_ERROR,
			 * "Start apportionment does not correspond to divisors.",
			 * new String[] {""});
			 * throw new BipropException(
			 * "Start apportionment does not correspond to divisors.");
			 * }
			 * }
			 * }
			 * } */
		}

		if (debug)
		{
			StringBuffer deptmp = new StringBuffer("-*-*-Gewichte---\n");
			deptmp.append(printMatrixW(aktWeights));
			deptmp.append("\n---Zuteilung---\n");
			deptmp.append(printMatrixR(aktWeights));
			deptmp.append("--inputWeights--\n");
			deptmp.append(printMatrixW(theMatrix));
			deptmp.append("\n\nZeilendivisoren: ");
			deptmp.append(printDArray(rowDivs));
			deptmp.append("\nSpaltendivisoren: ");
			deptmp.append(printDArray(colDivs));
			notifyMethodListeners(deptmp.toString());
		}

		// Berechnung der Fehlstände TTn88-91
		calculateFaults();
		if (fColSum > 0)
		{
			bmes.setError(BipropLibMessenger.INPUT_ERROR, "Columns aren't fitted", new String[] { "" });
			throw new BipropException("Columns aren't fitted");
		}

		// Indikatormatrix für positive Gewichte TTn93-96
		boolean[][] scriptA = new boolean[rows][cols];

		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				if (theMatrix[i][j].weight > 0)
					scriptA[i][j] = true;
				else
					scriptA[i][j] = false;

		// Markierte Spalten/Zeilen; Vorgänger Array TTn104-122
		boolean[] labeled = new boolean[rows + cols];
		int[] pred = new int[rows + cols];
		int[] ij = new int[rows + cols];
		int ijStart = 0;
		int ijEnd = 0;

		for (int i = 0; i < rows; i++)
		{
			if (fRow[i] < 0)
				labeled[i] = true;
			else
				labeled[i] = false;
			pred[i] = -1;
		}

		for (int j = 0; j < cols; j++)
		{
			labeled[rows + j] = false;
			pred[rows + j] = -1;
		}

		for (int n = 0; n < rows + cols; n++)
			if (labeled[n])
				ij[ijEnd++] = n;

		// Einige Hilfsvariablen
		int currentIndex = 0;
		int foundTransfer = 0;
		double epsilon = 0.0;
		int step = 0;

		int oldFRowSum = -1;
		int stepsWithoutChange = 0;
		int maxStepsWithoutChange = rows + cols;

		if (debug)
		{
			StringBuffer deptmp = new StringBuffer("Startkonfiguration:\nZeilen: ");
			deptmp.append(rows);
			deptmp.append(" Spalten: ");
			deptmp.append(cols);
			deptmp.append("\n");
			deptmp.append(printFlawInformation("Start"));
			notifyMethodListeners(deptmp.toString());
		}

		// /////////////////////////////////
		// "Die große While" TTn125-230
		while (fRowSum > 0)
		{

			step++;

			if (oldFRowSum == fRowSum)
				stepsWithoutChange++;
			else
				stepsWithoutChange = 0;

			if (stepsWithoutChange > maxStepsWithoutChange)
			{
				bmes.setError(BipropLibMessenger.COMMON,
						"Too many steps: " + step,
						null);
				throw new BipropException(
						"Too many steps: " + step);

			}

			oldFRowSum = fRowSum;

			if (Thread.interrupted())
			{
				userInterrupted(ret, new StringBuffer(""), 0);
				bmes.setError(BipropLibMessenger.USER_ERROR,
						"User interrupted TTflpt calculation @step " + step,
						new String[] { String.valueOf(step) });
				throw new BipropException(
						"User interrupted TTflpt calculation @step " + step);
			}

			// Abarbeitung der Queue TTn126-159
			while (ijStart < ijEnd)
			{
				currentIndex = ij[ijStart++];

				if (currentIndex < rows)
				{
					// (1) es handelt sich um eine Zeile
					for (int j = 0; j < cols; j++)
						if (!labeled[rows + j] && scriptA[currentIndex][j] &&
								// aktWeights[currentIndex][j].weight == sp.s(aktWeights[currentIndex][j].rdWeight) ){ //@todo Genauigkeit
								Method.expDif(aktWeights[currentIndex][j].weight, sp.s(aktWeights[currentIndex][j].rdWeight), acc))
						{
							labeled[rows + j] = true;
							pred[rows + j] = currentIndex;
							ij[ijEnd++] = rows + j;
						}
				}
				else
				{
					// (2) es handelt sich um eine Spalte
					currentIndex -= rows;
					for (int i = 0; i < rows; i++)
						if (!labeled[i] && scriptA[i][currentIndex] &&
								// aktWeights[i][currentIndex].weight == sp.s(aktWeights[i][currentIndex].rdWeight - 1) &&
								Method.expDif(aktWeights[i][currentIndex].weight, sp.s(aktWeights[i][currentIndex].rdWeight - 1), acc) &&
								aktWeights[i][currentIndex].rdWeight > aktWeights[i][currentIndex].min)
						{ // @todo Genauigkeit
							labeled[i] = true;
							pred[i] = rows + currentIndex;
							ij[ijEnd++] = i;
						}
				}

				// Transfer möglich?
				foundTransfer = -1;
				for (int i = 0; i < rows; i++)
					if (labeled[i] && fRow[i] > 0)
					{
						foundTransfer = i;
						break;
					}

				if (foundTransfer > -1)
					break;

			} // End Abarbeitung Queue

			notifyMethodListeners(printFlawInformation(labeled));

			// Transfer oder Update?
			if (foundTransfer > -1)
			{
				// (i) Transfer
				transfers++;
				currentIndex = foundTransfer;

				while (pred[currentIndex] > -1)
				{
					if (currentIndex < rows)
						// Eine Zeile
						aktWeights[currentIndex][pred[currentIndex] - rows].rdWeight -= 1;
					else
						// Eine Spalte
						aktWeights[pred[currentIndex]][currentIndex - rows].rdWeight += 1;

					currentIndex = pred[currentIndex];
				}

				if (debug)
				{
					StringBuffer buff = new StringBuffer("\nAktuelle Zuteilung nach Transfer " + transfers + ":\n");
					for (int i = 0; i < aktWeights.length; i++)
					{
						for (int j = 0; j < aktWeights[i].length; j++)
						{
							buff.append(aktWeights[i][j].rdWeight + "; ");
						}
						buff.append("\n");
					}
					buff.append("\nAktuelle ZeilenDivisoren:\n");
					for (int i = 0; i < rowDivs.length; i++)
					{
						buff.append(rowDivs[i] + "; ");
					}
					buff.append("\nAktuelle SpaltenDivisoren:\n");
					for (int i = 0; i < colDivs.length; i++)
					{
						buff.append(colDivs[i] + "; ");
					}
					buff.append("\n");
					notifyMethodListeners(buff.toString());
				}

				calculateFaults();
				if (fColSum > 0)
				{
					bmes.setError(BipropLibMessenger.INPUT_ERROR, "Columns aren't fitted", new String[] { "" });
					throw new BipropException("Columns aren't fitted");
				}
			}
			else
			{
				// (ii) Update
				updates++;
				epsilon = Double.POSITIVE_INFINITY;

				for (int i = 0; i < rows; i++)
					for (int j = 0; j < cols; j++)
						if (labeled[i] && !labeled[rows + j] && scriptA[i][j])
							epsilon = Math.min(epsilon,
									sp.s(aktWeights[i][j].rdWeight) / aktWeights[i][j].weight);
						else if (!labeled[i] && labeled[rows + j] && scriptA[i][j] &&
								aktWeights[i][j].rdWeight > aktWeights[i][j].min)
							epsilon = Math.min(epsilon,
									aktWeights[i][j].weight / sp.s(aktWeights[i][j].rdWeight - 1));

				// Update der Divisoren
				for (int i = 0; i < rows; i++)
					if (labeled[i])
						rowDivs[i] /= epsilon;

				for (int j = 0; j < cols; j++)
					if (labeled[rows + j])
						colDivs[j] *= epsilon;

				// Gewichte neu inistialisieren
				for (int i = 0; i < rows; i++)
					for (int j = 0; j < cols; j++)
						aktWeights[i][j].weight = theMatrix[i][j].weight / (rowDivs[i] * colDivs[j]);
				notifyMethodListeners("Epsilon: " + epsilon);
			}

			// Felder neu inistialisieren
			ijStart = 0;
			ijEnd = 0;

			for (int i = 0; i < rows; i++)
			{
				if (fRow[i] < 0)
					labeled[i] = true;
				else
					labeled[i] = false;
				pred[i] = -1;
			}

			for (int j = 0; j < cols; j++)
			{
				labeled[rows + j] = false;
				pred[rows + j] = -1;
			}

			for (int n = 0; n < rows + cols; n++)
				if (labeled[n])
					ij[ijEnd++] = n;

			if (debug)
			{
				StringBuffer deptmp = new StringBuffer();
				deptmp.append(printFlawInformation("Groß-Schritt " + step));
				notifyMethodListeners(deptmp.toString());
			}

		} // End "Große While"

		if (debug)
		{
			StringBuffer deptmp = new StringBuffer("\n\n---------------\nEnde große While\nZeilendivs:\n");
			deptmp.append(printDArray(rowDivs));
			deptmp.append("\nSpaltendivs:\n");
			deptmp.append(printDArray(colDivs));
			notifyMethodListeners(deptmp.toString());
		}

		// Finde Ties TTn232-239 @todo Genauigkeit
		Weight aktW;
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
			{
				aktW = aktWeights[i][j];

				if (Method.expDif(aktW.weight, sp.s(aktW.rdWeight), acc) &&
						aktW.rdWeight <= aktW.weight)
					aktW.multiple = "+";
				else if (Method.expDif(aktW.weight, sp.s(aktW.rdWeight - 1), acc) &&
						aktW.rdWeight >= aktW.weight &&
						aktW.rdWeight > 0)
					aktW.multiple = "-";
			}

		removeUselessTies(aktWeights);

		// Die Divisoren müssen noch korrigiert werden
		try
		{
			// Die Divisoren mit den gefundenen Divisoren besetzen

			for (int i = 0; i < rows; i++)
			{
				dRowDivs[i] = new Divisor();
				dRowDivs[i].setDivisorInterval(rowDivs[i], rowDivs[i]);
				dRowDivs[i].setMultiplierInterval(1 / rowDivs[i], 1 / rowDivs[i]);
			}
			for (int j = 0; j < cols; j++)
			{
				dColDivs[j] = new Divisor();
				dColDivs[j].setDivisorInterval(colDivs[j], colDivs[j]);
				dColDivs[j].setMultiplierInterval(1 / colDivs[j], 1 / colDivs[j]);
			}

			reCalculateDivisors(aktWeights, rowDivs, colDivs);

			/* for(int i =0; i<rows; i++)
			 * System.out.println(dRowDivs[i].getDivisor() + " " + rowDivs[i]);
			 * for(int j =0; j< cols; j++)
			 * System.out.println(dColDivs[j].getDivisor() + " " + colDivs[j]); */

		}
		catch (BipropException e)
		{
			bmes.setError(BipropLibMessenger.DIVISOR, e.getMessage(), null);
			throw e;
		}
		catch (DivisorException e)
		{
			bmes.setError(BipropLibMessenger.DIVISOR, e.getMessage(), null);
			throw new BipropException("Error calculating Divisors");
		}

		ret.transfers = transfers;
		ret.updates = updates;

		if (checksAndPolish)
			finalChecks(aktWeights, rowDivs, colDivs);

		// Alle Checks bestanden, das Ergebnis zurückgeben
		ret.app = aktWeights;

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

		// ret.transfers = transfers;
		// ret.updates = updates;

		if (Debug.BIPROP)
		{
			notifyMethodListeners("Beende TTflpt...\n");
		}

		fireIterationChanged(-1, true);

		return ret;
	}

	/** Berechnung der Fehlstände TTn88-91 */
	private void calculateFaults()
	{
		fRowSum = 0;
		fColSum = 0;

		for (int j = 0; j < cols; j++)
			fCol[j] = 0;

		for (int i = 0; i < rows; i++)
		{

			fRow[i] = 0;
			for (int j = 0; j < cols; j++)
			{
				fRow[i] += aktWeights[i][j].rdWeight;
				fCol[j] += aktWeights[i][j].rdWeight;
			}
			fRow[i] -= rowApp[i];
			fRowSum += Math.abs(fRow[i]);
		}

		for (int j = 0; j < cols; j++)
			fColSum += Math.abs(fCol[j] - colApp[j]);
	}

	private String printFlawInformation(boolean[] labeled)
	{
		StringBuffer tmp = new StringBuffer("\n");
		tmp.append("\nMarkierte Zeilen: ");

		boolean added = false;
		for (int i = 0; i < rows; i++)
		{
			if (labeled[i])
			{
				if (added)
				{
					tmp.append(", ");
				}
				tmp.append("i_" + (i + 1));
				added = true;
			}
		}

		tmp.append("\nMarkierte Spalten: ");
		added = false;
		for (int j = 0; j < cols; j++)
		{

			if (labeled[rows + j])
			{
				if (added)
				{
					tmp.append(", ");
				}
				tmp.append("j_" + (j + 1));
				added = true;
			}
		}
		return tmp.toString();
	}

	/** Liefert Informationen über die Fehlstände
	 * 
	 * @param info Info
	 * @return Informationen über die aktuellen Fehlstände */
	private String printFlawInformation(String info)
	{
		StringBuffer tmp = new StringBuffer("\nFehlstände bei ");
		tmp.append(info);
		tmp.append(":\n");
		tmp.append(printArray(fRow));
		tmp.append("\n\tZ-Summe: ");
		tmp.append(fRowSum);
		tmp.append("\n\tS-Summe: ");
		tmp.append(fColSum);


		tmp.append("\nZeilendivisoren: ");
		for (int i = 0; i < rowDivs.length; i++)
		{
			tmp.append(rowDivs[i] + "; ");
		}
		tmp.append("\nSpaltendivisoren: ");
		for (int i = 0; i < colDivs.length; i++)
		{
			tmp.append(colDivs[i] + "; ");
		}
		return tmp.toString();
	}

}
