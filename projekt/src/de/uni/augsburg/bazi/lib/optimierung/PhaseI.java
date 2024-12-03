package de.uni.augsburg.bazi.lib.optimierung;

public class PhaseI
{
	private double[][] tableau;
	private double[][] mat_A;
	private double[] vec_b;
	private int[] basis;

	public PhaseI(double[][] A, double[] b)
	{
		this.mat_A = A;
		this.vec_b = b;
	}

	public boolean doPhaseI()
	{
		this.createTableau();

		return false;
	}

	private void createTableau()
	{
		// Anzahl Zeilen im Tableau: Anzahl Variablen + 1 Zusatzvariable + 1 Zeile für b
		// Anzahl Spalten im Tableau: Anzahl Nebenbedingungen + 1 ZusatzNB + Anzahl Variablen + 1 Zusatzvariable (=Zielfunktion)
		tableau = new double[mat_A[0].length + 2][mat_A.length + mat_A[0].length + 2];

		basis = new int[mat_A[0].length + 1];

		int i_min = 0;
		double value = vec_b[0];
		for (int i = 1; i < vec_b.length; i++)
		{
			if (vec_b[i] < value)
			{
				value = vec_b[i];
				i_min = i;
			}
		}

		// Teil mit Nebenbedingungen
		for (int i = 0; i < mat_A.length; i++)
		{
			for (int j = 0; j < mat_A[0].length; j++)
			{
				if (j == 0)
				{
					tableau[mat_A[0].length][i] = 1d;
					tableau[mat_A[0].length + 1][i] = vec_b[i] - vec_b[i_min];
				}
				tableau[j][i] = mat_A[i_min][j] - mat_A[i][j];
			}
		}

		for (int i = 0; i < mat_A[0].length; i++)
		{
			tableau[i][mat_A.length] = -mat_A[i_min][i];
		}
		tableau[mat_A[0].length][mat_A.length] = 1d;
		tableau[mat_A[0].length + 1][mat_A.length] = -vec_b[i_min];

		basis[mat_A[0].length] = i_min;

		for (int i = 0; i < mat_A[0].length + 2; i++)
		{
			if (i < mat_A[0].length)
			{
				basis[i] = mat_A[0].length + 1 + i;
			}
			for (int j = 0; j < mat_A[0].length; j++)
			{
				if (i == j)
				{
					tableau[i][mat_A.length + 1 + j] = 1d;
				}
				else
				{
					tableau[i][mat_A.length + 1 + j] = 0d;
				}
			}
		}

		for (int i = 0; i < mat_A[0].length; i++)
		{
			tableau[i][mat_A.length + 1 + mat_A[0].length] = -mat_A[i_min][i];
		}
		tableau[mat_A[0].length][mat_A.length + 1 + mat_A[0].length] = 1d;
		tableau[mat_A[0].length + 1][mat_A.length + 1 + mat_A[0].length] = -vec_b[i_min];
	}
}
