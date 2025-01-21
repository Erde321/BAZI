package de.uni.augsburg.bazi.lib.newton;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.QRDecompositionImpl;

import de.uni.augsburg.bazi.lib.Weight;

public class ReducedNewtonProcedure
{

	/* Global Fields */
	// zero norm for delta_x_k
	private final double EPSILON_1 = 1e-13;
	// stop for L1 norm
	private static double EPSILON_2 = .01;
	private final int MAX_ITERATIONS = 100;
	private final boolean DEBUG = true;

	/* Matrices */
	// Original Matrix A (in R^{k*l})
	private final double[][] A;
	// Iteration Matrix A_k (in R^{k+l-1 * k+l-1}) Jacobinian of f at x_k
	private double[][] A_k;

	/* Vectors */
	// Row marginals r (in R^k)
	private final double[] r;
	// Column marginals c (in R^l)
	private final double[] c;
	// Row multipliers m_r (in R^k)
	private double[] m_r;
	// Column multipliers m_c (in R^l)
	private double[] m_c;

	/* Real numbers and integers */
	// House size h (r_+ = c_+ = h)
	// private double h;
	// Number of rows in A
	private int k;
	// Number of columns in A
	private int l;
	// Dimension for the newton problem
	private int problem_dimension;
	// Numbers of Iteration
	private int iteration = 0;

	// Iteration Vector x_k (in R^{k+l-1})
	private double[] x_k;
	// Delta x_k (in R^{k+l-1})
	private double[] delta_x_k;
	// Value of the function f at point x_k (f(x_k) in R^{k+l-1})
	private double[] f_x_k;
	// Current_Row_Sums
	private double[] r_k;
	// Current_Col_Sums
	private double[] c_k;
	// L1 Fehler
	private double l1_error;

	// Variables for checks
	private boolean consistent = false;
	private boolean delta_equals_zero = false;
	private boolean sums_reached = false;

	// Variables for evading zero and negative multiplikators
	private double smallestMult = 0.01;

	private StringBuffer aktIterationInfo = new StringBuffer();

	public static ReducedNewtonProcedure getReducedNewtonProcedure(Weight[][] w, int[] rowApp, int[] colApp)
	{
		double[][] A = new double[w.length][w[0].length];
		for (int i = 0; i < w.length; i++)
		{
			for (int j = 0; j < w[i].length; j++)
			{
				A[i][j] = w[i][j].weight;
			}
		}

		double[] r = new double[rowApp.length];
		for (int i = 0; i < r.length; i++)
		{
			r[i] = rowApp[i];
		}

		double[] c = new double[colApp.length];
		for (int i = 0; i < c.length; i++)
		{
			c[i] = colApp[i];
		}

		return new ReducedNewtonProcedure(A, r, c);
	}

	public ReducedNewtonProcedure(double[][] A, double[] r, double[] c)
	{
		this.A = A;
		this.r = r;
		this.c = c;
		setConsistence(isConsistent());

		if (getConsistence())
		{
			// Set dimensions and house size
			k = this.A.length;
			l = this.A[0].length;

			// for (double d : r) {
			// this.h += d;
			// }

			problem_dimension = k + l - 1;
		}
	}

	private boolean isConsistent()
	{
		if (A == null || r == null || c == null)
		{
			return false;
		}

		if (A.length != r.length)
		{
			return false;
		}

		for (double[] temp : A)
		{
			if (temp == null || temp.length != c.length)
			{
				return false;
			}
		}

		double checksum = 0d;
		for (double d : r)
		{
			checksum += d;
		}
		for (double d : c)
		{
			checksum -= d;
		}

		if (Math.abs(checksum) > EPSILON_1)
		{
			return false;
		}

		return true;
	}

	private void setConsistence(boolean c)
	{
		consistent = c;
	}
	private boolean getConsistence()
	{
		return consistent;
	}

	public boolean finished()
	{
		return delta_equals_zero || sums_reached || iteration >= MAX_ITERATIONS;
	}

	public boolean error()
	{
		return delta_equals_zero || iteration >= MAX_ITERATIONS;
	}

	public String getFinishReason()
	{
		if (!finished())
		{
			return "Der Algorithmus kann noch weitere Iterationen ausführen!";
		}
		else if (delta_equals_zero)
		{
			return "Der Updateschritt für x_k ist zu klein.";
		}
		else if (sums_reached)
		{
			return "Die Zeilensummen wurden erreicht!";
		}
		else
		{
			// iteration > MAX_Iterations
			return "Die maximale Zahl von Iterationen wurde erreicht.";
		}
	}

	public String getAktIterationInfo()
	{
		return aktIterationInfo.toString();
	}

	public int getNumberOfIterations()
	{
		return iteration;
	}

	public void doIteration()
	{
		if (finished())
		{
			// Weitere Bearbeitung macht keinen Sinn
			return;
		}

		if (iteration == 0)
		{
			initializeProblem();
		}
		else
		{
			update_fxk();
			update_Ak();
		}

		Array2DRowRealMatrix mat = new Array2DRowRealMatrix(A_k);
		QRDecompositionImpl ludec = new QRDecompositionImpl(mat);
		DecompositionSolver sol = ludec.getSolver();
		delta_x_k = sol.solve(f_x_k);

		double vectorNorm = 0d;

		double[] tmp_x_k = new double[problem_dimension];
		double lambda = 1d;
		boolean lambda_less_one = false;
		for (int i = 0; i < problem_dimension; i++)
		{
			tmp_x_k[i] = x_k[i] + delta_x_k[i];
			vectorNorm += delta_x_k[i] * delta_x_k[i];
			if (tmp_x_k[i] < smallestMult)
			{
				double temp_lambda;
				temp_lambda = (smallestMult - x_k[i]) / delta_x_k[i];
				if (temp_lambda < lambda)
				{
					lambda = temp_lambda;
					lambda_less_one = true;
				}
			}
		}

		if (lambda_less_one)
		{
			for (int i = 0; i < problem_dimension; i++)
			{
				x_k[i] += lambda * delta_x_k[i];
				vectorNorm += delta_x_k[i] * delta_x_k[i];
			}
			smallestMult = smallestMult / 10d;
		}
		else
		{
			x_k = tmp_x_k;
		}

		vectorNorm = Math.sqrt(vectorNorm);
		if (vectorNorm < EPSILON_1)
		{
			delta_equals_zero = true;
		}

		updateCurrentSums();
		l1_error = 0d;
		for (int i = 0; i < k; i++)
		{
			l1_error += Math.abs(r_k[i] - r[i]);
		}
		for (int j = 0; j < l; j++)
		{
			l1_error += Math.abs(c_k[j] - c[j]);
		}
		l1_error *= 0.5;
		if (l1_error < EPSILON_2)
		{
			sums_reached = true;
		}

		iteration++;

		if (DEBUG)
		{
			NumberFormat nf = new DecimalFormat("0.0000000000");
			aktIterationInfo = new StringBuffer();
			aktIterationInfo.append(iteration + ". Iteration:\n");
			if (lambda_less_one)
			{
				aktIterationInfo.append("Schrittweitensteuerung aktiv! Lambda=" + nf.format(lambda) + "\n");
			}
			aktIterationInfo.append("L2-Norm von Delta_x_k: " + nf.format(vectorNorm) + "\n");
			aktIterationInfo.append("L1-Fehler der Marginalien: " + nf.format(l1_error) + "\n");
			aktIterationInfo.append("Zeilenmultiplikatoren:\n");
			for (int i = 0; i < k; i++)
			{
				aktIterationInfo.append(nf.format(x_k[i]));
				if (i < k - 1)
				{
					aktIterationInfo.append(";");
				}
			}
			aktIterationInfo.append("\n\nSpaltenmultiplikatoren:\n");
			for (int i = 0; i < l - 1; i++)
			{
				aktIterationInfo.append(nf.format(x_k[k + i]));
				aktIterationInfo.append(";");
			}
			aktIterationInfo.append("1\n\n");
		}
	}

	private void initializeProblem()
	{
		x_k = new double[problem_dimension];
		f_x_k = new double[problem_dimension];
		A_k = new double[problem_dimension][problem_dimension];

		// initialize x_k
		if (m_r == null || m_c == null)
		{
			for (int i = 0; i < k; i++)
			{
				/* double rowSum = 0d;
				 * for (int j=0; j<this.l; j++) {
				 * rowSum += A[i][j];
				 * }
				 * this.x_k[i] = r[i]/rowSum; */
				x_k[i] = 1d;
			}
			for (int j = 0; j < l - 1; j++)
			{
				x_k[k + j] = 1d;
			}
		}
		else
		{
			for (int i = 0; i < k; i++)
			{
				x_k[i] = m_r[i];
			}
			for (int i = 0; i < l - 1; i++)
			{
				x_k[k + i] = m_c[i];
			}
		}

		// initialize f_x_k
		update_fxk();

		// initialize Jacobonian A_k
		update_Ak();

		// update current sums
		updateCurrentSums();
	}

	private void update_fxk()
	{
		for (int i = 0; i < l - 1; i++)
		{
			double tempsum = 0d;
			for (int j = 0; j < k; j++)
			{
				tempsum += x_k[j] * A[j][i];
			}
			f_x_k[i] = c[i] - tempsum * x_k[k + i];
		}

		for (int i = 0; i < k; i++)
		{
			double tempsum = 0d;
			for (int j = 0; j < l - 1; j++)
			{
				tempsum += x_k[k + j] * A[i][j];
			}
			tempsum += A[i][l - 1];
			f_x_k[l - 1 + i] = r[i] - tempsum * x_k[i];
		}
	}

	private void update_Ak()
	{
		// first Block
		for (int p = 0; p < l - 1; p++)
		{
			for (int q = 0; q < k; q++)
			{
				A_k[p][q] = A[q][p] * x_k[k + p];
			}
			for (int q = k; q < problem_dimension; q++)
			{
				if (p == q - k)
				{
					double tempsum = 0d;
					for (int i = 0; i < k; i++)
					{
						tempsum += x_k[i] * A[i][p];
					}
					A_k[p][q] = tempsum;
				}
				else
				{
					A_k[p][q] = 0d;
				}
			}
		}

		// Second block
		for (int p = 0; p < k; p++)
		{
			for (int q = 0; q < k; q++)
			{
				if (q == p)
				{
					double tempsum = 0d;
					for (int j = 0; j < l - 1; j++)
					{
						tempsum += x_k[k + j] * A[q][j];
					}
					tempsum += A[q][l - 1];
					A_k[p + l - 1][q] = tempsum;
				}
				else
				{
					A_k[p + l - 1][q] = 0d;
				}
			}
			for (int q = 0; q < l - 1; q++)
			{
				A_k[p + l - 1][q + k] = A[p][q] * x_k[p];
			}
		}
	}

	private void updateCurrentSums()
	{
		r_k = new double[k];
		c_k = new double[l];

		for (int i = 0; i < k; i++)
		{
			for (int j = 0; j < l; j++)
			{
				double value = A[i][j] * x_k[i];
				if (j < l - 1)
				{
					value *= x_k[k + j];
				} // else *1
				r_k[i] += value;
				c_k[j] += value;
			}
		}
	}

	public double[] getX_k()
	{
		return x_k;
	}
	public double getL1_error()
	{
		return l1_error;
	}
	public static void setMaxError(double err)
	{
		EPSILON_2 = err;
	}
	public static double getMaxError()
	{
		return EPSILON_2;
	}

	public static void main(String[] args)
	{

		// AHY
		double[][] A = new double[10][3];
		A[0][0] = 247856;
		A[0][1] = 25663;
		A[0][2] = 116230;
		A[1][0] = 8107;
		A[1][1] = 105274;
		A[1][2] = 14716;
		A[2][0] = 361566;
		A[2][1] = 110581;
		A[2][2] = 157069;
		A[3][0] = 280944;
		A[3][1] = 208094;
		A[3][2] = 146292;
		A[4][0] = 28794;
		A[4][1] = 191;
		A[4][2] = 17008;
		A[5][0] = 147607;
		A[5][1] = 308023;
		A[5][2] = 82248;
		A[6][0] = 100040;
		A[6][1] = 256643;
		A[6][2] = 79568;
		A[7][0] = 1611;
		A[7][1] = 203123;
		A[7][2] = 1649;
		A[8][0] = 250928;
		A[8][1] = 81512;
		A[8][2] = 281733;
		A[9][0] = 12041;
		A[9][1] = 140390;
		A[9][2] = 60151;

		double[] r = new double[10];
		double[] c = new double[3];

		r[0] = 9;
		r[1] = 3;
		r[2] = 14;
		r[3] = 13;
		r[4] = 3;
		r[5] = 10;
		r[6] = 9;
		r[7] = 3;
		r[8] = 13;
		r[9] = 3;
		c[0] = 30;
		c[1] = 30;
		c[2] = 20;

		// Beispiel
		A = new double[2][2];
		A[0][0] = 30;
		A[0][1] = 0;
		A[1][0] = 10;
		A[1][1] = 20;

		r = new double[2];
		r[0] = 3;
		r[1] = 3;

		c = new double[2];
		c[0] = 3;
		c[1] = 3;


		ReducedNewtonProcedure rnv = new ReducedNewtonProcedure(A, r, c);
		for (int i = 0; i < 10; i++)
			rnv.doIteration();
	}
}
