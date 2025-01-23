/*
 * @(#)TieFinder.java 1.0 15/06/10
 * 
 * Copyright (c) 2000-2010 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */

package de.uni.augsburg.bazi.lib;

import java.util.ArrayList;
import java.util.HashSet;

public class TieFinder
{

	private static double[][] distanceMatrix;
	private static int[][] parentMatrix;

	private static double valueOfCircle = 0d;
	// private static int indexOfCircle = -1;

	private final Weight[][] weightMatrix;
	private final int[] rowMarginals;
	private final int[] columnMarginals;

	private final int[][] localMin;
	private final int[][] localMax;

	private final Signpost s;

	private final int numberOfRows;
	private final int numberOfCols;

	private boolean ties = false;

	private final MethodListener ml;

	public TieFinder(Weight[][] matrix, int[] rowM, int[] colM, int[][] localMin,
			int[][] localMax, Signpost s, MethodListener ml)
	{
		weightMatrix = matrix;
		rowMarginals = rowM;
		columnMarginals = colM;
		this.localMin = localMin;
		this.localMax = localMax;
		this.s = s;
		this.ml = ml;

		numberOfRows = matrix.length;
		numberOfCols = matrix[0].length;
	}

	public void calculateTies() throws BipropException
	{

		boolean lengthZeroOccurs;
		ArrayList<ArcInNetwork> removedArcs = new ArrayList<ArcInNetwork>();
		ArrayList<Circle> allCircles = new ArrayList<Circle>();

		do
		{
			lengthZeroOccurs = false;
			// Netzwerk erstellen
			TieFinder.createNetwork(weightMatrix, rowMarginals, columnMarginals,
					localMin, localMax, s);

			// Kanten aus Netzwerk entfernen, um neue Kreise zu finden
			for (ArcInNetwork e : removedArcs)
			{
				TieFinder.distanceMatrix[e.start][e.end] = Double.POSITIVE_INFINITY;
				TieFinder.parentMatrix[e.start][e.end] = -1;
			}

			TieFinder.doFloydWarshallAlgorithm(TieFinder.distanceMatrix, TieFinder.parentMatrix, ml);

			ArrayList<Circle> newCircles = new ArrayList<Circle>();

			for (int i = 0; i < numberOfRows + numberOfCols; i++)
			{
				if (TieFinder.distanceMatrix[i][i] < Constants.epsilon)
				{
					lengthZeroOccurs = true;
					ties = true;

					// Kreis bestimmen
					ArrayList<Integer> reverseCircle = new ArrayList<Integer>();
					HashSet<Integer> usedEdges = new HashSet<Integer>();

					int vorgaenger = TieFinder.parentMatrix[i][i];
					reverseCircle.add(i);
					boolean innerCircle = false;
					while (vorgaenger != i)
					{
						if (!usedEdges.contains(vorgaenger))
						{
							usedEdges.add(vorgaenger);
							reverseCircle.add(vorgaenger);
							vorgaenger = TieFinder.parentMatrix[i][vorgaenger];
						}
						else
						{
							// Innerer Kreis gefunden
							ArrayList<Integer> innerReverseCircle = new ArrayList<Integer>();
							innerCircle = true;
							boolean indexFound = false;
							for (int z = 0; z < reverseCircle.size(); z++)
							{
								if (reverseCircle.get(z) == vorgaenger)
								{
									indexFound = true;
								}
								if (indexFound)
								{
									innerReverseCircle.add(reverseCircle.get(z));
								}
							}
							innerReverseCircle.add(vorgaenger);
							reverseCircle = innerReverseCircle;
							break;
						}
					}
					if (!innerCircle)
						reverseCircle.add(i);

					ArrayList<Integer> circle = new ArrayList<Integer>();
					for (int z = reverseCircle.size() - 1; z >= 0; z--)
					{
						circle.add(reverseCircle.get(z));
					}
					reverseCircle = null;

					Circle c = new Circle();
					c.edges = circle;

					if (allCircles.contains(c))
						continue;

					newCircles.add(c);

					// Ties eintragen
					for (int k = 0; k < circle.size() - 1; k++)
					{

						boolean plus;
						int index_row;
						int index_column;

						if (circle.get(k) < numberOfRows)
						{
							plus = true;
							index_row = circle.get(k);
							index_column = circle.get(k + 1) - numberOfRows;
						}
						else
						{
							plus = false;
							index_column = circle.get(k) - numberOfRows;
							index_row = circle.get(k + 1);
						}

						if (plus)
						{
							weightMatrix[index_row][index_column].multiple = "+";
						}
						else
						{
							weightMatrix[index_row][index_column].multiple = "-";
						}
					}
				}
			}

			if (lengthZeroOccurs)
			{
				outer_for: for (Circle c1 : newCircles)
				{
					for (Circle c2 : allCircles)
					{
						if (c1.equals(c2))
						{
							continue outer_for;
						}
					}
					allCircles.add(c1);
				}

				int[][] arcs = new int[numberOfRows + numberOfCols][numberOfRows + numberOfCols];

				for (Circle c : allCircles)
				{
					ArrayList<Integer> edges = c.edges;
					for (int j = 0; j < c.edges.size() - 1; j++)
					{
						arcs[edges.get(j)][edges.get(j + 1)]++;
					}
				}

				int max_value = 0;
				int rowIndex = -1;
				int colIndex = -1;
				while (true)
				{
					for (int i = 0; i < arcs.length; i++)
					{
						for (int j = 0; j < arcs[0].length; j++)
						{
							if (arcs[i][j] > max_value)
							{
								max_value = arcs[i][j];
								rowIndex = i;
								colIndex = j;
							}
						}
					}

					ArcInNetwork arc = new ArcInNetwork();
					arc.start = rowIndex;
					arc.end = colIndex;

					if (removedArcs.contains(arc))
					{
						arcs[rowIndex][colIndex] = 0;
						max_value = 0;
						rowIndex = -1;
						colIndex = -1;
						continue;
					}
					else
					{
						removedArcs.add(arc);
						break;
					}
				}
			}

		}
		while (lengthZeroOccurs);


	}

	public boolean tiesOccur()
	{
		return ties;
	}

	public static void createNetwork(Weight[][] w, int[] rowM, int[] colM, int[][] localMin, int[][] localMax, Signpost s)
	{
		int numberOfRows = w.length;
		int numberOfCols = w[0].length;

		distanceMatrix = new double[numberOfRows + numberOfCols][numberOfRows + numberOfCols];
		parentMatrix = new int[numberOfRows + numberOfCols][numberOfRows + numberOfCols];

		// Erster Quadrant
		for (int i = 0; i < numberOfRows; i++)
		{
			for (int j = 0; j < numberOfRows; j++)
			{
				distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
				parentMatrix[i][j] = -1;
			}
		}

		// Vierter Quadrant
		for (int i = 0; i < numberOfCols; i++)
		{
			for (int j = 0; j < numberOfCols; j++)
			{
				distanceMatrix[numberOfRows + i][numberOfRows + j] = Double.POSITIVE_INFINITY;
				parentMatrix[numberOfRows + i][numberOfRows + j] = -1;
			}
		}

		// Zweiter Quadrant
		for (int i = 0; i < numberOfRows; i++)
		{
			for (int j = 0; j < numberOfCols; j++)
			{
				if (w[i][j].weight == 0d)
				{
					distanceMatrix[i][numberOfRows + j] = Double.POSITIVE_INFINITY;
					parentMatrix[i][numberOfRows + j] = -1;
				}
				else if (w[i][j].rdWeight == localMax[i][j])
				{
					distanceMatrix[i][numberOfRows + j] = Double.POSITIVE_INFINITY;
					parentMatrix[i][numberOfRows + j] = -1;
				}
				else
				{
					distanceMatrix[i][numberOfRows + j] = Math.log(s.s(w[i][j].rdWeight) / w[i][j].weight);
					parentMatrix[i][numberOfRows + j] = i;
				}
			}
		}

		// Dritter Quadrant
		for (int i = 0; i < numberOfRows; i++)
		{
			for (int j = 0; j < numberOfCols; j++)
			{
				if (w[i][j].weight == 0d)
				{
					distanceMatrix[numberOfRows + j][i] = Double.POSITIVE_INFINITY;
					parentMatrix[numberOfRows + j][i] = -1;
				}
				else if (w[i][j].rdWeight == localMin[i][j])
				{
					distanceMatrix[numberOfRows + j][i] = Double.POSITIVE_INFINITY;
					parentMatrix[numberOfRows + j][i] = -1;
				}
				else
				{
					distanceMatrix[numberOfRows + j][i] = -Math.log(s.s(w[i][j].rdWeight - 1) / w[i][j].weight);
					parentMatrix[numberOfRows + j][i] = numberOfRows + j;
				}
			}
		}
	}

	public static void doFloydWarshallAlgorithm(double[][] distances, int[][] parents,
			MethodListener ml) throws BipropException
	{

		int dimension = distances.length;
		// indexOfCircle = -1;
		valueOfCircle = 0d;

		for (int l = 0; l < dimension; l++)
		{
			for (int i = 0; i < dimension; i++)
			{
				for (int j = 0; j < dimension; j++)
				{
					if (distances[i][j] > distances[i][l] + distances[l][j])
					{
						distances[i][j] = distances[i][l] + distances[l][j];
						parents[i][j] = parents[l][j];
					}

					// Dieser Fall sollte nicht passieren
					if (i == j && distances[i][j] < -Constants.epsilon)
					{
						ml.printMessage("Negativer Kreis bei Index: " + i + " mit Wert: " + distances[i][j]);
						if (distances[i][j] < valueOfCircle)
						{
							valueOfCircle = distances[i][j];
							// indexOfCircle = i;
						}
						// throw new BipropException("Negativer Kreis bei Index: " + i);
					}
				}
			}
		}
	}

	private class ArcInNetwork
	{
		public int start;
		public int end;

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof ArcInNetwork))
				return false;

			ArcInNetwork a = (ArcInNetwork) obj;

			if (a.start == start && a.end == end)
				return true;

			return false;
		}
	}

	private class Circle
	{
		public ArrayList<Integer> edges = new ArrayList<Integer>();

		@Override
		public boolean equals(Object obj)
		{

			if (!(obj instanceof Circle))
				return false;

			Circle c = (Circle) obj;

			if (c.edges.size() != edges.size())
			{
				return false;
			}

			int index = -1;
			for (int i = 0; i < c.edges.size(); i++)
			{
				if (c.edges.get(i) == edges.get(0))
				{
					index = i;
					break;
				}
			}

			if (index == -1)
				return false;

			for (int i = 0; i < edges.size() - 1; i++)
			{
				int index_other;
				if (index + i >= edges.size())
				{
					index_other = (index + i) % c.edges.size() + 1;
				}
				else
				{
					index_other = index + i;
				}

				if (edges.get(i) != c.edges.get(index_other))
				{
					return false;
				}
			}

			return true;
		}
	}
}
