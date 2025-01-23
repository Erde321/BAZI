/*
 * @(#)PrimalAugmentingAlgorithm.java 1.0 15/06/10
 * 
 * Copyright (c) 2000-2010 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;

public class PrimalAugmentingAlgorithm extends AbstractBipropMethod
{

	private static final double eps = de.uni.augsburg.bazi.lib.Constants.epsilon;

	private final int[][] apportionmentMatrix;
	private int housesize;
	private int apportionedSeats = 0;
	private final int[][] localMinCond;
	private final int[][] localMaxCond;
	private final int[] currentRowCount;
	private final int[] currentColumnCount;
	private final int numberOfRows;
	private final int numberOfCols;
	private Date start;
	private boolean tiesOccur = false;
	private final double[] rowDivs;
	private final double[] colDivs;

	private final double[] logRowMults;

	// private final double[] logColMults;

	public PrimalAugmentingAlgorithm(Weight[][] weights, int[] aDistricts, int[] aParties, Signpost aSp, BipropLibMessenger aBmes, BipropRet aRet) throws BipropException
	{
		super(weights, aDistricts, aParties, aSp, aBmes, aRet);

		localMinCond = new int[theMatrix.length][];
		localMaxCond = new int[theMatrix.length][];
		apportionmentMatrix = new int[theMatrix.length][];

		currentRowCount = new int[theMatrix.length];
		currentColumnCount = new int[theMatrix[0].length];

		for (int i = 0; i < weights.length; i++)
		{
			housesize += rowApp[i];
			localMinCond[i] = new int[theMatrix[i].length];
			localMaxCond[i] = new int[theMatrix[i].length];
			apportionmentMatrix[i] = new int[theMatrix[i].length];

			for (int j = 0; j < weights[i].length; j++)
			{
				if (aSp.s(0) == 0d && weights[i][j].weight > 0 && weights[i][j].min == 0)
				{
					localMinCond[i][j] = 1;
				}
				else
				{
					localMinCond[i][j] = weights[i][j].min;
				}
				localMaxCond[i][j] = weights[i][j].max;
			}
		}

		numberOfRows = rowApp.length;
		numberOfCols = colApp.length;

		rowDivs = new double[numberOfRows];
		colDivs = new double[numberOfCols];

		logRowMults = new double[numberOfRows];
		// logColMults = new double[numberOfCols];

		for (int i = 0; i < numberOfRows; i++)
		{
			double temp = Double.POSITIVE_INFINITY;
			for (int j = 0; j < numberOfCols; j++)
			{
				double value = Math.log(sp.s(localMinCond[i][j]) / theMatrix[i][j].weight);
				if (value < temp)
				{
					temp = value;
				}
			}
			logRowMults[i] = temp;
		}
	}

	@Override
	public BipropRet calculate() throws BipropException, IterationExceededException
	{

		start = new Date();

		if (debug)
		{
			notifyMethodListeners("Starte Primalen augmentierenden Algorithmus...");
		}

		// Diverse Checks der Existenz; nur Nötig falls die Berechnung ganz von vorne beginnt
		// bei aktWeights != null wird angenommen, dass diese Checks schon gemacht worden sind

		if (checksAndPolish && !checkExistence())
		{
			return ret;
		}

		/* Starte Initialisierung
		 * Heuristik zur schnellen Zuteilung der ersten Sitze.
		 * Dabei wird in der Methode init() eine Priority Queue erstellt,die dann hier genutzt wird. */
		if (debug)
		{
			notifyMethodListeners("Beginne Zuteilung durch Heuristik...");
		}
		java.util.PriorityQueue<NextSeat> queue = init();
		while (apportionedSeats < housesize)
		{
			NextSeat next = queue.poll();
			int row = next.getRow();
			int column = next.getColumn();

			if (currentRowCount[row] < rowApp[row] &&
					currentColumnCount[column] < colApp[column] &&
					apportionmentMatrix[row][column] < localMaxCond[row][column])
			{

				// Sitzzahl erhöhen
				apportionmentMatrix[row][column]++;
				currentRowCount[row]++;
				currentColumnCount[column]++;
				apportionedSeats++;

				// Multiplikatoren updaten
				ArrayList<Integer> vorgaenger = new ArrayList<Integer>();
				vorgaenger.add(1 + numberOfRows + column);
				vorgaenger.add(1 + row);
				vorgaenger.add(0);
				aktualisiereLogMults(vorgaenger);

				// Nächsten Sitz dieser Zelle in Queue einfpgen
				next.setRow(row);
				next.setColumn(column);
				next.setValue(Math.log(sp.s(apportionmentMatrix[row][column]) / theMatrix[row][column].weight));
				queue.add(next);

			}
			else
			{
				break;
			}
		}
		ret.colit = apportionedSeats;
		ret.rowit = housesize - apportionedSeats;

		if (debug)
		{
			notifyMethodListeners("Zuteilung nach Startheuristik:");
			printApp();
		}

		if (housesize - apportionedSeats <= 0)
		{
			if (debug)
			{
				notifyMethodListeners("Startheuristik liefert bereits Zielzuteilung!");
			}

			for (int i = 0; i < apportionmentMatrix.length; i++)
			{
				for (int j = 0; j < apportionmentMatrix[i].length; j++)
				{
					theMatrix[i][j].rdWeight = apportionmentMatrix[i][j];
				}
			}

			calculateTies();
			removeUselessTies(theMatrix);

			return calculateBipropRet();
		}
		else if (debug)
		{
			notifyMethodListeners("Startheuristik hat " + apportionedSeats + " von " +
					housesize + " Sitzen zugeteilt\n\n" +
					"Starte augmentierenden Algorithmus...");
		}

		while (apportionedSeats < housesize)
		{
			// Entfernungen für das Netzwerk ermitteln
			double[][] distances = createDistanceMatrix();

			int[] vor = new int[2 + numberOfRows + numberOfCols];
			double[] dist = new double[2 + numberOfRows + numberOfCols];
			for (int i = 0; i < 2 + numberOfRows + numberOfCols; i++)
			{
				vor[i] = -1;
				if (i != 0)
				{
					dist[i] = Double.POSITIVE_INFINITY;
				}
			}

			for (int z = 0; z < 1 + numberOfRows + numberOfCols; z++)
			{
				for (int i = 0; i < 2 + numberOfRows + numberOfCols; i++)
				{
					for (int j = 0; j < 2 + numberOfRows + numberOfCols; j++)
					{
						if (distances[i][j] < Double.POSITIVE_INFINITY)
						{
							if (dist[i] + distances[i][j] < dist[j] - eps)
							{
								dist[j] = dist[i] + distances[i][j];
								vor[j] = i;
							}
						}
					}
				}
			}

			ArrayList<Integer> vorgaenger = new ArrayList<Integer>();
			int index = numberOfRows + numberOfCols + 1;
			// int count = 0;
			while (index != 0)
			{
				// count++;
				vorgaenger.add(vor[index]);
				index = vor[index];
			}

			boolean add = true;
			String s = "";

			for (int i = 0; i < vorgaenger.size() - 2; i++)
			{
				if (add)
				{
					int column = vorgaenger.get(i) - 1 - numberOfRows;
					int row = vorgaenger.get(i + 1) - 1;

					apportionmentMatrix[row][column]++;
					currentRowCount[row]++;
					currentColumnCount[column]++;

					if (debug)
					{
						if ("".equals(s))
						{
							s = "(" + row + "," + column + ") +1";
						}
						else
						{
							s = "(" + row + "," + column + ") +1  ->  " + s;
						}
					}

					add = false;
				}
				else
				{
					int row = vorgaenger.get(i) - 1;
					int column = vorgaenger.get(i + 1) - 1 - numberOfRows;

					apportionmentMatrix[row][column]--;
					currentRowCount[row]--;
					currentColumnCount[column]--;

					if (debug)
					{
						s = "(" + row + "," + column + ") -1  ->  " + s;
					}
					add = true;
				}
			}

			apportionedSeats++;
			if (debug)
			{
				notifyMethodListeners("Änderung Sitzzahlen bei Indizes: " + s);
			}

			aktualisiereLogMults(vorgaenger);

		}


		if (debug)
		{
			notifyMethodListeners("Zuteilung nach Algorithmus:");
			printApp();
		}

		for (int i = 0; i < apportionmentMatrix.length; i++)
		{
			for (int j = 0; j < apportionmentMatrix[i].length; j++)
			{
				theMatrix[i][j].rdWeight = apportionmentMatrix[i][j];
			}
		}

		calculateTies();
		removeUselessTies(theMatrix);

		return calculateBipropRet();
	}

	/** Methode zur Initialisierung des Problems
	 * 
	 * Dabei wird die Zuteilung auf die lokalen Minbedingungen gesetzt.
	 * Anschließend wird eine PriorityQueue für die erste Zuteilung gesetzt. */
	private java.util.PriorityQueue<NextSeat> init()
	{

		for (int i = 0; i < apportionmentMatrix.length; i++)
		{
			for (int j = 0; j < apportionmentMatrix[i].length; j++)
			{
				apportionmentMatrix[i][j] = localMinCond[i][j];
				apportionedSeats += localMinCond[i][j];
				currentRowCount[i] += localMinCond[i][j];
				currentColumnCount[j] += localMinCond[i][j];
			}
		}

		java.util.PriorityQueue<NextSeat> queue = new java.util.PriorityQueue<NextSeat>();
		for (int i = 0; i < apportionmentMatrix.length; i++)
		{
			for (int j = 0; j < apportionmentMatrix[i].length; j++)
			{
				if (theMatrix[i][j].weight > 0d)
				{
					NextSeat temp = new NextSeat();
					temp.setRow(i);
					temp.setColumn(j);
					temp.setValue(Math.log(sp.s(apportionmentMatrix[i][j]) / theMatrix[i][j].weight));
					queue.add(temp);
				}
			}
		}

		return queue;
	}

	private double[][] createDistanceMatrix()
	{
		double[][] distances = new double[numberOfRows + numberOfCols + 2][numberOfRows + numberOfCols + 2];

		for (int i = 0; i < distances.length; i++)
		{
			for (int j = 0; j < distances[i].length; j++)
			{
				// Quelle
				if (i == 0)
				{
					// Quelle -> Reihenknoten
					if (j >= 1 && j < 1 + numberOfRows)
					{
						if (rowApp[j - 1] > currentRowCount[j - 1])
						{
							distances[i][j] = 0d;
						}
						else
						{
							distances[i][j] = Double.POSITIVE_INFINITY;
						}
						// Quelle -> Quelle, Spaltenknoten, Senke
					}
					else
					{
						distances[i][j] = Double.POSITIVE_INFINITY;
					}
					// Reihenknoten
				}
				else if (i > 0 && i < 1 + numberOfRows)
				{
					// Reihe -> Quelle, Reihe, Senke
					if (j == 0 || j < 1 + numberOfRows || j == numberOfRows + numberOfCols + 1)
					{
						distances[i][j] = Double.POSITIVE_INFINITY;
						// Reihe -> Spalte
					}
					else
					{
						if (theMatrix[i - 1][j - 1 - numberOfRows].weight != 0d && apportionmentMatrix[i - 1][j - 1 - numberOfRows] < localMaxCond[i - 1][j - 1 - numberOfRows])
						{
							distances[i][j] = Math.log(sp.s(apportionmentMatrix[i - 1][j - 1 - numberOfRows]) / theMatrix[i - 1][j - 1 - numberOfRows].weight);
						}
						else
						{
							distances[i][j] = Double.POSITIVE_INFINITY;
						}
					}
					// Spaltenknoten
				}
				else if (i > numberOfRows && i < numberOfRows + numberOfCols + 1)
				{
					// Spalte -> Quelle, Spalte
					if (j == 0 || (j > numberOfRows && j < numberOfRows + numberOfCols + 1))
					{
						distances[i][j] = Double.POSITIVE_INFINITY;
						// Spalte -> Senke
					}
					else if (j == numberOfRows + numberOfCols + 1)
					{
						if (currentColumnCount[i - 1 - numberOfRows] < colApp[i - 1 - numberOfRows])
						{
							distances[i][j] = 0d;
						}
						else
						{
							distances[i][j] = Double.POSITIVE_INFINITY;
						}
						// Spalte -> Reihe
					}
					else
					{
						if (theMatrix[j - 1][i - 1 - numberOfRows].weight != 0d && apportionmentMatrix[j - 1][i - 1 - numberOfRows] > localMinCond[j - 1][i - 1 - numberOfRows])
						{
							distances[i][j] = -Math.log(sp.s(apportionmentMatrix[j - 1][i - 1 - numberOfRows] - 1) / theMatrix[j - 1][i - 1 - numberOfRows].weight);
						}
						else
						{
							distances[i][j] = Double.POSITIVE_INFINITY;
						}
					}
					// Senke
				}
				else
				{
					// Senke -> Quelle, Reihe, Spalte
					distances[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		return distances;
	}

	private BipropRet calculateBipropRet() throws BipropException
	{

		calculateDivisorsAndFinalChecks();

		ret.timeElapsed = new Date().getTime() - start.getTime();
		ret.app = theMatrix;

		return ret;
	}

	private void calculateDivisorsAndFinalChecks() throws BipropException
	{

		if (!tiesOccur)
		{

			double[] logRowMultipliers = new double[numberOfRows];
			double[] logColMultipliers = new double[numberOfCols];

			// Initialisierung
			for (int i = 0; i < numberOfRows; i++)
			{
				double sum_lower = 0d;
				double sum_upper = 0d;
				int numberOfLower = 0;
				int numberOfUpper = 0;
				for (int j = 0; j < numberOfCols; j++)
				{
					if (theMatrix[i][j].weight == 0d)
					{}
					else if (apportionmentMatrix[i][j] > localMinCond[i][j])
					{
						sum_lower += Math.log(sp.s(apportionmentMatrix[i][j] - 1) / theMatrix[i][j].weight);
						numberOfLower++;
					}

					if (theMatrix[i][j].weight == 0d)
					{}
					else if (apportionmentMatrix[i][j] < localMaxCond[i][j])
					{
						sum_upper += Math.log(sp.s(apportionmentMatrix[i][j]) / theMatrix[i][j].weight);
						numberOfUpper++;
					}
				}
				if (numberOfLower > 0 && numberOfUpper > 0)
				{
					logRowMultipliers[i] = 0.5 * (sum_upper / (numberOfUpper) - sum_lower / (numberOfLower));
				}
				else if (numberOfLower > 0)
				{
					logRowMultipliers[i] = sum_lower / (numberOfLower);
				}
				else if (numberOfUpper > 0)
				{
					logRowMultipliers[i] = sum_upper / (numberOfUpper);
				}
			}

			boolean calculate = true;

			int iter = 0;
			int max_iter = 10000;

			while (calculate)
			{
				if (iter >= max_iter)
				{
					// Zeilen Divs/Mults
					ret.divRowMin = new double[rows];
					ret.divRowMax = new double[rows];
					ret.divRowNice = new double[rows];
					ret.mulRowMin = new double[rows];
					ret.mulRowMax = new double[rows];
					ret.mulRowNice = new double[rows];
					for (int i = 0; i < rows; i++)
					{
						ret.divRowMin[i] = 1d;
						ret.divRowMax[i] = 1d;
						ret.divRowNice[i] = 1d;
						ret.mulRowMin[i] = 1d;
						ret.mulRowMax[i] = 1d;
						ret.mulRowNice[i] = 1d;
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
						ret.divColMin[i] = 1d;
						ret.divColMax[i] = 1d;
						ret.divColNice[i] = 1d;
						ret.mulColMin[i] = 1d;
						ret.mulColMax[i] = 1d;
						ret.mulColNice[i] = 1d;
					}
					ret.sError = "Divisor_not_calculated";
					bmes.setError(BipropLibMessenger.DIVISOR, "Error: Could not calculate proper multipliers due to Iteration exceeded", new String[0]);
					return;
				}
				iter++;

				int row = -1;
				int column = -1;
				double value = Double.NEGATIVE_INFINITY;
				boolean violatesLower = true;

				for (int i = 0; i < numberOfRows; i++)
				{
					for (int j = 0; j < numberOfCols; j++)
					{
						double lowerBound = Double.NEGATIVE_INFINITY;
						double upperBound = Double.POSITIVE_INFINITY;

						if (theMatrix[i][j].weight == 0d)
						{
							continue;
						}

						if (apportionmentMatrix[i][j] < localMaxCond[i][j])
						{
							upperBound = Math.log(sp.s(apportionmentMatrix[i][j]) / theMatrix[i][j].weight);
						}
						if (apportionmentMatrix[i][j] > localMinCond[i][j])
						{
							lowerBound = Math.log(sp.s(apportionmentMatrix[i][j] - 1) / theMatrix[i][j].weight);
						}

						double temp = logRowMultipliers[i] + logColMultipliers[j];

						if (temp - lowerBound < 0d)
						{
							if (Math.abs(temp - lowerBound) > value)
							{
								value = Math.abs(temp - lowerBound);
								row = i;
								column = j;
								violatesLower = true;
							}
						}
						if (temp - upperBound > 0)
						{
							if (Math.abs(temp - upperBound) > value)
							{
								value = Math.abs(temp - upperBound);
								row = i;
								column = j;
								violatesLower = false;
							}
						}
					}
				}

				if (row < 0)
				{
					calculate = false;
					continue;
				}

				double max_Stepsize_Row = Double.POSITIVE_INFINITY;
				for (int j = 0; j < numberOfCols; j++)
				{
					if (j == column)
						continue;

					if (violatesLower)
					{
						double temp = Double.POSITIVE_INFINITY;
						if (theMatrix[row][j].weight == 0d)
						{}
						else if (apportionmentMatrix[row][j] < localMaxCond[row][j])
						{
							// Abstand zu Obergrenze
							temp = Math.log(sp.s(apportionmentMatrix[row][j]) / theMatrix[row][j].weight) - logRowMultipliers[row] - logColMultipliers[j];
							// Wert verletzt Obergrenze -> Abstand zu Untergrenze
							if (temp < 0)
							{
								temp = logRowMultipliers[row] + logColMultipliers[j] - Math.log(sp.s(apportionmentMatrix[row][j] - 1) / theMatrix[row][j].weight);
							}
						}

						if (temp < max_Stepsize_Row)
						{
							max_Stepsize_Row = temp;
						}
					}
					else
					{
						double temp = Double.POSITIVE_INFINITY;
						if (theMatrix[row][j].weight == 0d)
						{}
						else if (apportionmentMatrix[row][j] > localMinCond[row][j])
						{
							// Abstand zu Untergrenze
							temp = logRowMultipliers[row] + logColMultipliers[j] - Math.log(sp.s(apportionmentMatrix[row][j] - 1) / theMatrix[row][j].weight);
							// Wert verletzt Untergrenze -> Abstand zu Obergrenze
							if (temp < 0)
							{
								temp = Math.log(sp.s(apportionmentMatrix[row][j]) / theMatrix[row][j].weight) - logRowMultipliers[row] - logColMultipliers[j];
							}
						}

						if (temp < max_Stepsize_Row)
						{
							max_Stepsize_Row = temp;
						}

					}
				}

				double max_Stepsize_Column = Double.POSITIVE_INFINITY;
				for (int i = 0; i < numberOfRows; i++)
				{
					if (i == row)
						continue;

					if (violatesLower)
					{
						double temp = Double.POSITIVE_INFINITY;
						if (theMatrix[i][column].weight == 0d)
						{}
						else if (apportionmentMatrix[i][column] < localMaxCond[i][column])
						{
							// Abstand zu Obergrenze
							temp = Math.log(sp.s(apportionmentMatrix[i][column]) / theMatrix[i][column].weight) - logRowMultipliers[i] - logColMultipliers[column];
							// Wert verletzt Obergrenze -> Abstand zu Untergrenze
							if (temp < 0)
							{
								temp = logRowMultipliers[i] + logColMultipliers[column] - Math.log(sp.s(apportionmentMatrix[i][column] - 1) / theMatrix[i][column].weight);
							}
						}

						if (temp < max_Stepsize_Column)
						{
							max_Stepsize_Column = temp;
						}
					}
					else
					{
						double temp = Double.POSITIVE_INFINITY;
						if (theMatrix[i][column].weight == 0d)
						{}
						else if (apportionmentMatrix[i][column] > localMinCond[i][column])
						{
							// Abstand zu Untergrenze
							temp = logRowMultipliers[i] + logColMultipliers[column] - Math.log(sp.s(apportionmentMatrix[i][column] - 1) / theMatrix[i][column].weight);
							// Wert verletzt Untergrenze -> Abstand zu Obergrenze
							if (temp < 0)
							{
								temp = Math.log(sp.s(apportionmentMatrix[i][column]) / theMatrix[i][column].weight) - logRowMultipliers[i] - logColMultipliers[column];
							}
						}

						if (temp < max_Stepsize_Column)
						{
							max_Stepsize_Column = temp;
						}

					}
				}


				double epsilon = 0.000001;

				if (max_Stepsize_Row + max_Stepsize_Column <= epsilon)
				{
					max_Stepsize_Row = value / 2d;
					max_Stepsize_Column = value / 2d;
				}

				if (apportionmentMatrix[row][column] > localMinCond[row][column] &&
						apportionmentMatrix[row][column] < localMaxCond[row][column])
				{
					double temp = Math.log(sp.s(apportionmentMatrix[row][column]) / theMatrix[row][column].weight)
							- Math.log(sp.s(apportionmentMatrix[row][column] - 1) / theMatrix[row][column].weight);
					value += temp;
				}


				if (max_Stepsize_Row > 0.5 * value && max_Stepsize_Column > 0.5 * value)
				{
					if (violatesLower)
					{
						logRowMultipliers[row] += 0.5 * value;
						logColMultipliers[column] += 0.5 * value;
					}
					else
					{
						logRowMultipliers[row] -= 0.5 * value;
						logColMultipliers[column] -= 0.5 * value;
					}
				}
				else if (max_Stepsize_Row + max_Stepsize_Column > value)
				{
					if (max_Stepsize_Row <= 0.5 * value)
					{
						if (max_Stepsize_Row > epsilon * value)
						{
							double part = max_Stepsize_Row / ((1 - epsilon) * value);
							if (violatesLower)
							{
								logRowMultipliers[row] += part * value;
								logColMultipliers[column] += (1 - part) * value;
							}
							else
							{
								logRowMultipliers[row] -= part * value;
								logColMultipliers[column] -= (1 - part) * value;
							}
						}
						else
						{
							if (violatesLower)
							{
								logColMultipliers[column] += value;
							}
							else
							{
								logColMultipliers[column] -= value;
							}
						}
					}
					else
					{
						if (max_Stepsize_Column > epsilon * value)
						{
							double part = max_Stepsize_Column / ((1 - epsilon) * value);
							if (violatesLower)
							{
								logRowMultipliers[row] += (1 - part) * value;
								logColMultipliers[column] += part * value;
							}
							else
							{
								logRowMultipliers[row] -= (1 - part) * value;
								logColMultipliers[column] -= part * value;
							}
						}
						else
						{
							if (violatesLower)
							{
								logRowMultipliers[row] += value;
							}
							else
							{
								logRowMultipliers[row] -= value;
							}
						}
					}
				}
				else
				{
					double part_Row = max_Stepsize_Row / value;
					double part_Column = max_Stepsize_Column / value;

					if (part_Row > 2 * epsilon)
					{
						part_Row -= epsilon;
					}
					if (part_Column > 2 * epsilon)
					{
						part_Column -= epsilon;
					}
					if (violatesLower)
					{
						logRowMultipliers[row] += part_Row * value;
						logColMultipliers[column] += part_Column * value;
					}
					else
					{
						logRowMultipliers[row] -= part_Row * value;
						logColMultipliers[column] -= part_Column * value;
					}

				}
			}

			for (int i = 0; i < numberOfRows; i++)
			{
				rowDivs[i] = 1d / Math.exp(logRowMultipliers[i]);
			}
			for (int j = 0; j < numberOfCols; j++)
			{
				colDivs[j] = 1d / Math.exp(logColMultipliers[j]);
			}
			if (debug)
			{
				notifyMethodListeners("Divisoren errechnet nach " + iter + " Iterationen!");
			}
		}
		else
		{
			/* Optimierungs-Versuch: */
			ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
			PairOfDouble[][] boundary = new PairOfDouble[numberOfRows][numberOfCols];

			for (int i = 0; i < numberOfRows; i++)
			{
				for (int j = 0; j < numberOfCols; j++)
				{

					boundary[i][j] = new PairOfDouble();

					if (theMatrix[i][j].weight == 0)
						continue;
					if (apportionmentMatrix[i][j] < localMaxCond[i][j])
					{
						double[] coefficients = new double[numberOfRows + numberOfCols];
						coefficients[i] = 1d;
						coefficients[numberOfRows + j] = 1d;
						double value = Math.log(sp.s(apportionmentMatrix[i][j]) / theMatrix[i][j].weight);

						boundary[i][j].upperBound = value;

						constraints.add(new LinearConstraint(coefficients, Relationship.LEQ, value));
					}
					if (apportionmentMatrix[i][j] > localMinCond[i][j])
					{
						double[] coefficients = new double[numberOfRows + numberOfCols];
						coefficients[i] = 1d;
						coefficients[numberOfRows + j] = 1d;
						double value = Math.log(sp.s(apportionmentMatrix[i][j] - 1) / theMatrix[i][j].weight);

						boundary[i][j].lowerBound = value;

						constraints.add(new LinearConstraint(coefficients, Relationship.GEQ, value));
					}
				}
			}
			double[] coefficients = new double[numberOfRows + numberOfCols];

			LinearObjectiveFunction f = new LinearObjectiveFunction(coefficients, 0);

			org.apache.commons.math.optimization.linear.SimplexSolver solver =
					new org.apache.commons.math.optimization.linear.SimplexSolver(eps);
			solver.setMaxIterations(2000);

			try
			{
				RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

				int index = -1;
				boolean indexInColumn = false;
				for (int i = 0; i < solution.getPoint().length; i++)
				{
					if (solution.getPoint()[i] == 0d)
					{
						index = i;
						break;
					}
				}

				if (index >= numberOfRows)
				{
					index = index - numberOfRows;
					indexInColumn = true;
				}

				if (indexInColumn)
				{
					double maxLowerBound = Double.NEGATIVE_INFINITY;
					double minUpperBound = Double.POSITIVE_INFINITY;

					for (int i = 0; i < numberOfRows; i++)
					{
						double localLowerBound = boundary[i][index].lowerBound - solution.getPoint()[i];
						double localUpperBound = boundary[i][index].upperBound - solution.getPoint()[i];

						if (localLowerBound > maxLowerBound)
						{
							maxLowerBound = localLowerBound;
						}
						if (localUpperBound < minUpperBound)
						{
							minUpperBound = localUpperBound;
						}
					}

					solution.getPointRef()[numberOfRows + index] = 0.5 * (maxLowerBound + minUpperBound);
				}
				else
				{
					double maxLowerBound = Double.NEGATIVE_INFINITY;
					double minUpperBound = Double.POSITIVE_INFINITY;

					for (int j = 0; j < numberOfCols; j++)
					{
						double localLowerBound = boundary[index][j].lowerBound - solution.getPoint()[numberOfRows + j];
						double localUpperBound = boundary[index][j].upperBound - solution.getPoint()[numberOfRows + j];

						if (localLowerBound > maxLowerBound)
						{
							maxLowerBound = localLowerBound;
						}
						if (localUpperBound < minUpperBound)
						{
							minUpperBound = localUpperBound;
						}
					}
					solution.getPointRef()[index] = 0.5 * (maxLowerBound + minUpperBound);
				}

				/* DEBUG:
				 * for (int i=0; i<this.numberOfRows; i++) {
				 * for (int j=0; j<this.numberOfCols; j++) {
				 * System.out.println("(" + i + "," + j + "): " + boundary[i][j].lowerBound + " ; " + (solution.getPoint()[i] + solution.getPoint()[this.numberOfRows+j]) + " ; " +
				 * boundary[i][j].upperBound);
				 * }
				 * } */
				for (int i = 0; i < numberOfRows; i++)
				{
					double minUpperBound = Double.POSITIVE_INFINITY;
					double maxLowerBound = Double.NEGATIVE_INFINITY;

					for (int j = 0; j < numberOfCols; j++)
					{
						double localLowerBound = boundary[i][j].lowerBound - solution.getPoint()[numberOfRows + j];
						double localUpperBound = boundary[i][j].upperBound - solution.getPoint()[numberOfRows + j];
						if (minUpperBound > localUpperBound)
						{
							minUpperBound = localUpperBound;
						}
						if (maxLowerBound < localLowerBound)
						{
							maxLowerBound = localLowerBound;
						}
					}
					/* DEBUG:
					 * System.out.println(i + ": " + maxLowerBound + " ; " + solution.getPoint()[i] + " ; " + minUpperBound + "\t(" + (Math.abs(maxLowerBound-solution.getPoint()[i]) <
					 * NextSeat.eps) + "," + (Math.abs(minUpperBound-solution.getPoint()[i]) < NextSeat.eps) + ")"); */

					solution.getPointRef()[i] = 0.5 * (maxLowerBound + minUpperBound);

				}

				for (int i = 0; i < numberOfRows; i++)
				{
					rowDivs[i] = 1d / Math.exp(solution.getPoint()[i]);
				}
				for (int j = 0; j < numberOfCols; j++)
				{
					colDivs[j] = 1d / Math.exp(solution.getPoint()[numberOfRows + j]);
				}
			}
			catch (OptimizationException e)
			{
				notifyMethodListeners(e.toString());
				// Zeilen Divs/Mults
				ret.divRowMin = new double[rows];
				ret.divRowMax = new double[rows];
				ret.divRowNice = new double[rows];
				ret.mulRowMin = new double[rows];
				ret.mulRowMax = new double[rows];
				ret.mulRowNice = new double[rows];
				for (int i = 0; i < rows; i++)
				{
					ret.divRowMin[i] = 1d;
					ret.divRowMax[i] = 1d;
					ret.divRowNice[i] = 1d;
					ret.mulRowMin[i] = 1d;
					ret.mulRowMax[i] = 1d;
					ret.mulRowNice[i] = 1d;
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
					ret.divColMin[i] = 1d;
					ret.divColMax[i] = 1d;
					ret.divColNice[i] = 1d;
					ret.mulColMin[i] = 1d;
					ret.mulColMax[i] = 1d;
					ret.mulColNice[i] = 1d;
				}
				ret.sError = "Divisor_not_calculated";
				bmes.setError(BipropLibMessenger.DIVISOR, "Error: Could not calculate proper multipliers", new String[0]);
				return;
			}
		}

		if (checksAndPolish)
		{
			finalChecks(theMatrix, rowDivs, colDivs);
		}

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
	}

	private void calculateTies() throws BipropException
	{

		TieFinder tf = new TieFinder(theMatrix, rowApp, colApp, localMinCond,
				localMaxCond, sp, methodListeners.get(0));
		tf.calculateTies();
		tiesOccur = tf.tiesOccur();
	}

	private void aktualisiereLogMults(ArrayList<Integer> vorgaenger)
	{

		if (true)
			return;

		/* boolean add = true;
		 * for (int i = 0; i < vorgaenger.size() - 2; i++)
		 * {
		 * if (add)
		 * {
		 * int column = vorgaenger.get(i) - 1 - numberOfRows;
		 * int row = vorgaenger.get(i + 1) - 1;
		 * double value =
		 * Math.log(sp.s(apportionmentMatrix[row][column]) / theMatrix[row][column].weight) - Math.log(sp.s(apportionmentMatrix[row][column] - 1) / theMatrix[row][column].weight);
		 * logRowMults[row] += value;
		 * add = false;
		 * }
		 * else
		 * {
		 * int row = vorgaenger.get(i) - 1;
		 * int column = vorgaenger.get(i + 1) - 1 - numberOfRows;
		 * double value =
		 * Math.log(sp.s(apportionmentMatrix[row][column]) / theMatrix[row][column].weight - 1) - Math.log(sp.s(apportionmentMatrix[row][column]) / theMatrix[row][column].weight);
		 * logColMults[column] += value;
		 * }
		 * } */
	}

	private class NextSeat implements Comparator<NextSeat>, Comparable<NextSeat>
	{

		private int row;
		private int column;
		private double value;

		public void setRow(int r)
		{
			row = r;
		}

		public void setColumn(int c)
		{
			column = c;
		}

		public void setValue(double v)
		{
			value = v;
		}

		public int getRow()
		{
			return row;
		}

		public int getColumn()
		{
			return column;
		}

		public double getValue()
		{
			return value;
		}

		@Override
		public int compare(NextSeat first, NextSeat second)
		{
			if (first.getValue() < second.getValue() - eps)
			{
				return -1;
			}
			else if (first.getValue() - eps > second.getValue())
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}

		@Override
		public int compareTo(NextSeat second)
		{
			return compare(this, second);
		}
	}

	private class PairOfDouble
	{
		public double upperBound = Double.POSITIVE_INFINITY;
		public double lowerBound = Double.NEGATIVE_INFINITY;
	}

	private void printApp()
	{

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < apportionmentMatrix.length; i++)
		{
			sb.append("\n");
			for (int j = 0; j < apportionmentMatrix[i].length; j++)
			{
				sb.append(apportionmentMatrix[i][j] + "\t");
			}
		}
		notifyMethodListeners(sb.toString());
	}

}
