package de.uni.augsburg.bazi.lib.exactbiprop;

/* ExactBipropDivMethod.java -- Biproportional rounding of matrices 
 **                              using exact rational arithmetic 
 **
 ** Copyright (C) 2007 Martin Zachariasen <martinz@diku.dk>.  
 ** All rights reserved.
 **
 ** @author Martin Zachariasen
 ** @version 1.3, January 2007
 **
 ** This program is free software;
 ** you can redistribute it and/or modify it.
 **
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 **
 */

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Biproportional rounding (or apportionment) via divisor method and exact rational arithmetic. A fast version of the tie-and-transfer algorithm by Balinski, Demange and Rachev is
 * used to compute the apportionment. The signpost formula is given as a parameter to the constructor. Minimum restrictions can be given. Also, initial divisors and apportionment
 * can be given as input. Apportionment, ties and divisors can be returned.
 * <P>
 * Usage samples: <SMALL>
 * 
 * <PRE>
 * // Faroese election 2004 int[][] weights = {{1434, 1217, 531, 2041, 532, 259, 516}, { 602, 2012, 663, 2638, 663, 105, 818}, { 741, 1254, 489, 2267, 373, 326, 1471}, { 264, 529, 60,
 * // 527, 39, 22, 20}, { 849, 1254, 582, 3076, 263, 285, 581}, { 148, 466, 78, 790, 79, 26, 74}, { 29, 144, 89, 432, 24, 0, 29}}; int [] r = {7, 7, 7, 1, 7, 2, 1}; int [] c = {4, 7,
 * // 2, 12, 2, 1, 4}; ExactSignPost sp = new ExactSignPost(); // signpost formula (standard rounding) // compute apportionment ExactBipropDivMethod bp = new
 * // ExactBipropDivMethod(weights, r, c, sp); bp.computeApportionment(); // print apportionment, ties and divisors BigInteger[][] app = bp.getApportionment();
 * // System.out.println(&quot;\nApportionment:&quot;); int n = r.length, m = c.length, i, j; for (i = 0; i &lt; n; i++) { for (j = 0; &lt; m; j++) { System.out.print(&quot;   &quot; + app[i][j].toString()); }
 * // System.out.println(&quot;&quot;); } int[][] ties = bp.getTies(); boolean any_ties = false; for (i = 0; i &lt; n; i++) for (j = 0; j &lt; m; j++) if (ties[i][j] != 0) any_ties = true; if
 * // (any_ties) { System.out.println(&quot;\nTies:&quot;); for (i = 0; i &lt; n; i++) { for (j = 0; j &lt; m; j++) { if (ties[i][j] == 0) System.out.print(&quot;  .&quot;); if (ties[i][j] == -1)
 * // System.out.print(&quot; -1&quot;); if (ties[i][j] == 1) System.out.print(&quot; +1&quot;); } System.out.println(); } } BigRational[] row_D = bp.getRowDivisors(); BigRational[] col_D =
 * // bp.getColumnDivisors(); System.out.println(); for (i = 0; i &lt; n; i++) System.out.println(&quot;Row &quot; + i + &quot; divisor = &quot; + row_D[i].toStringDot(6) + &quot; (&quot; + row_D[i].toString() +
 * // &quot;)&quot;); System.out.println(); for (j = 0; j &lt; m; j++) System.out.println(&quot;Column &quot; + j + &quot; divisor = &quot; + col_D[j].toStringDot(6) + &quot; (&quot; + col_D[j].toString() + &quot;)&quot;);
 * // System.out.println(&quot;\nNumber of transfers: &quot; + bp.getNumberOfTransfers()); System.out.println(&quot;\nNumber of divisor updates: &quot; + bp.getNumberOfUpdates());
 * </PRE>
 * 
 * </SMALL>
 * <P>
 * The ExactBipropDivMethod source is available here: <A HREF="ExactBipropDivMethod.java">ExactBipropDivMethod.java</A>.
 * @author Martin Zachariasen
 * @version 1.3, January 2007 */
public class ExactBipropDivMethod
{

	// Weights that should be used for making the apportionment
	/** @uml.property name="_weights"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	protected BigRational[][] _weights;

	// Required row, column and total sums
	protected BigInteger[] _r;
	protected BigInteger[] _c;
	protected BigInteger _h;

	// Signpost formula
	protected ExactSignPost _sp;

	// Any minimum restrictions?
	private boolean any_min_restrictions = false;

	// Actual minimum restrictions (if any)
	private BigInteger[][] _min_restrictions;

	// Initial apportionment given?
	private boolean any_initial_apportionment = false;

	// Computed apportionment
	private BigInteger[][] _app;

	// Initial row and/or column divisors given?
	private boolean any_initial_row_divisors = false;
	private boolean any_initial_col_divisors = false;

	// Row and column divisors
	/** @uml.property name="_row_D"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	private BigRational[] _row_D;
	/** @uml.property name="_col_D"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	private BigRational[] _col_D;

	// Has apportionment been computed?
	private boolean app_computed = false;

	// Matrix identifying positive weight elements
	private boolean[][] weights_positive;

	// Define constants for the value of delta
	private final static BigRational DELTA_INF = new BigRational("-1");

	// Should trace information be printed?
	private final static boolean TRACE = false;

	// Statistics
	private int _NumberOfUpdates = 0;
	private int _NumberOfTransfers = 0;

	/** Constructor (int input)
	 * @param weights Weights
	 * @param r Required row sums
	 * @param c Required column sums
	 * @param sp Signpost formula */
	public ExactBipropDivMethod(int[][] weights,
			int[] r,
			int[] c,
			ExactSignPost sp)
	{
		// Check that dimensions of matrix and arrays correspond
		if (weights.length != r.length)
			throw new IllegalArgumentException("Number of rows in weight and r are not indentical.");
		if (weights[0].length != c.length)
			throw new IllegalArgumentException("Number of columns in weight and c are not identical.");

		// Check that sums of r and c match
		int i, j, n = r.length, m = c.length;

		int sum_r = 0;
		for (i = 0; i < n; i++)
			sum_r += r[i];

		int sum_c = 0;
		for (j = 0; j < m; j++)
			sum_c += c[j];

		if (sum_r != sum_c)
			throw new IllegalArgumentException("Required row and column sums are not identical");

		_weights = new BigRational[n][m];
		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
				_weights[i][j] = new BigRational(weights[i][j]);

		_r = new BigInteger[n];
		for (i = 0; i < n; i++)
			_r[i] = new BigInteger(new Integer(r[i]).toString());

		_c = new BigInteger[m];
		for (j = 0; j < m; j++)
			_c[j] = new BigInteger(new Integer(c[j]).toString());

		_h = new BigInteger(new Integer(sum_r).toString());
		_sp = sp;

		app_computed = false; // apportionment has not been computed
	}

	/** Constructor (int and double input)
	 * @param weights Weights
	 * @param r Required row sums
	 * @param c Required column sums
	 * @param sp Signpost formula */
	public ExactBipropDivMethod(double[][] weights,
			int[] r,
			int[] c,
			ExactSignPost sp)
	{
		// Check that dimensions of matrix and arrays correspond
		if (weights.length != r.length)
			throw new IllegalArgumentException("Number of rows in weight and r are not indentical.");
		if (weights[0].length != c.length)
			throw new IllegalArgumentException("Number of columns in weight and c are not identical.");

		// Check that sums of r and c match
		int i, j, n = r.length, m = c.length;

		int sum_r = 0;
		for (i = 0; i < n; i++)
			sum_r += r[i];

		int sum_c = 0;
		for (j = 0; j < m; j++)
			sum_c += c[j];

		if (sum_r != sum_c)
			throw new IllegalArgumentException("Required row and column sums are not identical.");

		_weights = new BigRational[n][m];
		DecimalFormat df = new DecimalFormat("0.#################", new DecimalFormatSymbols(Locale.US));
		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
				_weights[i][j] = new BigRational(df.format(weights[i][j]));

		_r = new BigInteger[n];
		for (i = 0; i < n; i++)
			_r[i] = new BigInteger(new Integer(r[i]).toString());

		_c = new BigInteger[m];
		for (j = 0; j < m; j++)
			_c[j] = new BigInteger(new Integer(c[j]).toString());

		_h = new BigInteger(new Integer(sum_r).toString());
		_sp = sp;

		app_computed = false; // apportionment has not been computed
	}

	/** Constructor (BigInteger and BigRational input)
	 * @param weights Weights
	 * @param r Required row sums
	 * @param c Required column sums
	 * @param sp Signpost formula */
	public ExactBipropDivMethod(BigRational[][] weights,
			BigInteger[] r,
			BigInteger[] c,
			ExactSignPost sp)
	{
		// Check that dimensions of matrix and arrays correspond
		if (weights.length != r.length)
			throw new IllegalArgumentException("Number of rows in weight and r are not indentical.");
		if (weights[0].length != c.length)
			throw new IllegalArgumentException("Number of columns in weight and c are not identical.");

		// Check that sums of r and c match
		int i, j, n = r.length, m = c.length;

		BigInteger sum_r = BigInteger.ZERO;
		for (i = 0; i < n; i++)
			sum_r = sum_r.add(r[i]);

		BigInteger sum_c = BigInteger.ZERO;
		for (j = 0; j < m; j++)
			sum_c = sum_c.add(c[j]);

		if (!sum_r.equals(sum_c))
			throw new IllegalArgumentException("Required row and column sums are not identical.");

		_weights = weights;
		_r = r;
		_c = c;
		_h = sum_r;
		_sp = sp;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets minimum restrictions (int input)
	 * @param min_restrictions Minimum restrictions */
	public void setMinRestrictions(int[][] min_restrictions)
	{
		// Check that dimensions of matrix are valid
		if (min_restrictions.length != _r.length)
			throw new IllegalArgumentException("Number of rows in minimum restrictions matrix invalid.");
		if (min_restrictions[0].length != _c.length)
			throw new IllegalArgumentException("Number of columns in minimum restrictions matrix invalid.");

		int i, j, n = _r.length, m = _c.length;
		_min_restrictions = new BigInteger[n][m];

		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
				_min_restrictions[i][j] = new BigInteger(new Integer(min_restrictions[i][j]).toString());
		any_min_restrictions = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets minimum restrictions (BigInteger input)
	 * @param min_restrictions Minimum restrictions */
	public void setMinRestrictions(BigInteger[][] min_restrictions)
	{
		// Check that dimensions of matrix are valid
		if (min_restrictions.length != _r.length)
			throw new IllegalArgumentException("Number of rows in minimum restrictions matrix invalid.");
		if (min_restrictions[0].length != _c.length)
			throw new IllegalArgumentException("Number of columns in minimum restrictions matrix invalid.");

		_min_restrictions = min_restrictions;
		any_min_restrictions = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets initial apportionment (int input).
	 * Only makes sense if corresponding row and column divisors also are given;
	 * in this case the column marginals must be fulfilled for the given apportionment.
	 * @param initial_apportionment Initial apportionment */
	public void setInitialApportionment(int[][] initial_apportionment)
	{
		// Check that dimensions of matrix are valid
		if (initial_apportionment.length != _r.length)
			throw new IllegalArgumentException("Number of rows in initial apportionment matrix invalid.");
		if (initial_apportionment[0].length != _c.length)
			throw new IllegalArgumentException("Number of columns in initial apportionment matrix invalid.");

		int i, j, n = _r.length, m = _c.length;
		_app = new BigInteger[n][m];

		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
				_app[i][j] = new BigInteger(new Integer(initial_apportionment[i][j]).toString());
		any_initial_apportionment = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets initial apportionment (BigInteger input).
	 * Only makes sense if corresponding row and column divisors also are given;
	 * in this case the column marginals must be fulfilled for the given apportionment.
	 * @param initial_apportionment Initial apportionment */
	public void setInitialApportionment(BigInteger[][] initial_apportionment)
	{
		// Check that dimensions of matrix are valid
		if (initial_apportionment.length != _r.length)
			throw new IllegalArgumentException("Number of rows in initial apportionment matrix invalid.");
		if (initial_apportionment[0].length != _c.length)
			throw new IllegalArgumentException("Number of columns in initial apportionment matrix invalid.");

		_app = initial_apportionment;
		any_initial_apportionment = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets initial row divisors (double input).
	 * If no corresponding initial apportionment is given,
	 * only the given row divisors are used.
	 * @param initial_row_divisors Initial row divisors */
	public void setInitialRowDivisors(double[] initial_row_divisors)
	{
		// Check that dimension of array is valid
		if (initial_row_divisors.length != _r.length)
			throw new IllegalArgumentException("Number of initial row divisors invalid.");

		int i, n = _r.length;
		_row_D = new BigRational[n];

		for (i = 0; i < n; i++)
			_row_D[i] = new BigRational(new Double(initial_row_divisors[i]).toString());

		any_initial_row_divisors = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets initial row divisors (BigRational input).
	 * If no corresponding initial apportionment is given,
	 * only the given row divisors are used.
	 * @param initial_row_divisors Initial row divisors */
	public void setInitialRowDivisors(BigRational[] initial_row_divisors)
	{
		// Check that dimension of array is valid
		if (initial_row_divisors.length != _r.length)
			throw new IllegalArgumentException("Number of initial row divisors invalid.");

		_row_D = initial_row_divisors;

		any_initial_row_divisors = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets initial column divisors (double input).
	 * If no corresponding initial apportionment is given,
	 * only the given row divisors are used.
	 * @param initial_col_divisors Initial column divisors */
	public void setInitialColumnDivisors(double[] initial_col_divisors)
	{
		// Check that dimension of array is valid
		if (initial_col_divisors.length != _c.length)
			throw new IllegalArgumentException("Number of initial column divisors invalid.");

		int j, m = _c.length;
		_col_D = new BigRational[m];

		for (j = 0; j < m; j++)
			_col_D[j] = new BigRational(new Double(initial_col_divisors[j]).toString());

		any_initial_col_divisors = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Sets initial column divisors (BigRational input).
	 * If no corresponding initial apportionment is given,
	 * only the given row divisors are used.
	 * @param initial_col_divisors Initial column divisors */
	public void setInitialColumnDivisors(BigRational[] initial_col_divisors)
	{
		// Check that dimension of array is valid
		if (initial_col_divisors.length != _c.length)
			throw new IllegalArgumentException("Number of initial column divisors invalid.");

		_col_D = initial_col_divisors;

		any_initial_col_divisors = true;

		app_computed = false; // apportionment has not been computed
	}

	/** Quotient for a given element in matrix
	 * @param i Row index
	 * @param j Column index
	 * @return Quotient for element (i,j) */
	private BigRational Quotient(int i,
			int j)
	{
		BigRational q = _weights[i][j];
		q = q.div(_row_D[i]);
		q = q.div(_col_D[j]);
		return q;
	}

	/** Breadth-first search in row/column graph
	 * @param labeled Labeled rows and columns (input/output)
	 * @param predecessor predecessor in BFS-tree (output) */
	private void bfsRowColumnGraph(boolean[] labeled,
			int[] predecessor)
	{
		int i, j, n = _r.length, m = _c.length;
		int[] Q = new int[n + m]; // Queue of labeled rows and columns
		int Q_front = 0;
		int Q_end = 0;

		// Append (pre)labeled rows/columns to Q
		for (i = 0; i < n + m; i++)
			if (labeled[i])
				Q[Q_end++] = i;

		while (Q_front < Q_end)
		{ // Q is not empty
			int row_col_index = Q[Q_front++];

			if (row_col_index < n)
			{
				// this is a row index
				i = row_col_index;

				// label all unlabeled columns where rounding UP is possible

				for (j = 0; j < m; j++)
				{
					if (!labeled[j + n] && (weights_positive[i][j]))
					{

						// column is not already labeled - is quotient equal to signpost function?
						BigRational q = Quotient(i, j);
						if (q.equals(_sp.get(_app[i][j])))
						{
							// rounding up is possible - label column j
							Q[Q_end++] = j + n;
							labeled[j + n] = true;
							predecessor[j + n] = i;
						}
					}
				}
			}
			else
			{
				// this is a column index
				j = row_col_index - n;

				// label all unlabeled rows where rounding DOWN is possible

				for (i = 0; i < n; i++)
				{
					if (!labeled[i] && (weights_positive[i][j]))
					{

						// row is not already labeled - any minimum restrictions?
						if (any_min_restrictions)
							if (_app[i][j].equals(_min_restrictions[i][j]))
								continue; // cannot round down

						// is quotient equal to signpost function?
						BigRational q = Quotient(i, j);
						if (q.equals(_sp.get(_app[i][j].subtract(BigInteger.ONE))))
						{
							// rounding down is possible - label row i
							Q[Q_end++] = i;
							labeled[i] = true;
							predecessor[i] = n + j;
						}
					}
				}
			}
		}
	}

	/** Compute delta for simultanous updating of divisors
	 * @param labeled Labeled rows and columns
	 * @return Delta value */
	private BigRational computeDelta(boolean[] labeled)
	{
		int i, j, n = _r.length, m = _c.length;
		BigRational delta = DELTA_INF;

		for (i = 0; i < n; i++)
		{
			for (j = 0; j < m; j++)
			{

				if ((labeled[i]) && (!labeled[j + n]) && weights_positive[i][j])
				{

					// labeled row and unlabeled column
					BigRational q = Quotient(i, j);
					BigRational d = _sp.get(_app[i][j]).div(q);

					if ((delta.equals(DELTA_INF)) || (d.compareTo(delta) < 0))
						delta = d;
				}

				if ((!labeled[i]) && (labeled[j + n]) && weights_positive[i][j])
				{

					// unlabeled row and labeled column - any minimum restrictions?
					if (any_min_restrictions)
						if (_app[i][j].equals(_min_restrictions[i][j]))
							continue; // cannot round down

					BigRational q = Quotient(i, j);
					BigRational dfunc = _sp.get(_app[i][j].subtract(BigInteger.ONE));
					if (dfunc.compareTo(BigRational.ZERO) > 0)
					{
						BigRational d = q.div(dfunc);

						if ((delta.equals(DELTA_INF)) || (d.compareTo(delta) < 0))
							delta = d;
					}
				}
			}
		}

		if ((delta.compareTo(BigRational.ONE) <= 0) &&
				(delta.compareTo(BigRational.ZERO) > 0))
		{
			throw new RuntimeException("Delta between 0 and 1. Should not happen.");
		}

		return delta;
	}

	/** Updated row and column divisors based on labeling and delta
	 * @param labeled Labeled rows and columns
	 * @param delta Delta */
	private void updateDivisors(boolean[] labeled,
			BigRational delta)
	{
		int i, j, n = _r.length, m = _c.length;

		// update row divisors for labeled rows
		for (i = 0; i < n; i++)
		{
			if (labeled[i])
				_row_D[i] = _row_D[i].div(delta);
		}

		// update column divisors for labeled columns
		for (j = 0; j < m; j++)
		{
			if (labeled[j + n])
				_col_D[j] = _col_D[j].mul(delta);
		}
	}


	/** Tie-and-transfer algorithm */
	private void tieAndTransfer()
	{
		int i, j, n = _r.length, m = _c.length;
		boolean[] labeled = new boolean[n + m];
		int[] predecessor = new int[n + m];

		// Main iteration of the algorithm

		while (true)
		{

			if (TRACE)
			{

				// apportionment
				System.out.println("Weights:");
				for (i = 0; i < n; i++)
				{
					for (j = 0; j < m; j++)
					{
						System.out.print(" " + _weights[i][j].toString());
					}
					System.out.println("");
				}
				System.out.println("");

				// apportionment
				System.out.println("Apportionment:");
				for (i = 0; i < n; i++)
				{
					for (j = 0; j < m; j++)
					{
						System.out.print(" " + _app[i][j].toString());
					}
					System.out.println("");
				}
				System.out.println("");

				System.out.println("Quotients:");
				// print quotients
				for (i = 0; i < n; i++)
				{
					for (j = 0; j < m; j++)
					{
						System.out.print(" " + Quotient(i, j).toStringDot(6));;
					}
					System.out.println("");
				}
				System.out.println("");
				for (i = 0; i < n; i++)
					System.out.println("divisors row " + i + " is " + _row_D[i].toString());
				for (j = 0; j < m; j++)
					System.out.println("divisors column " + j + " is " + _col_D[j].toString());
			}

			// Find errors in rows
			BigInteger[] err_row_app = new BigInteger[n];
			BigInteger err_total = BigInteger.ZERO;
			for (i = 0; i < n + m; i++)
			{
				labeled[i] = false;
				predecessor[i] = -1;
			}

			for (i = 0; i < n; i++)
			{
				BigInteger cur_sum = BigInteger.ZERO;
				for (j = 0; j < m; j++)
					cur_sum = cur_sum.add(_app[i][j]);
				err_row_app[i] = cur_sum.subtract(_r[i]);
				err_total = err_total.add(err_row_app[i].abs());

				// Add to queue if negative error
				if (err_row_app[i].compareTo(BigInteger.ZERO) < 0)
					labeled[i] = true;
			}

			if (TRACE)
				System.out.println("- total error is now "
						+ err_total.divide(new BigInteger("2")).toString() + "\n");

			if (err_total.equals(BigInteger.ZERO))
				break; // problem is solved!

			boolean positive_row_labeled = false;
			int row_col_index = 0;
			while (true)
			{

				// Run BFS on row/column graph
				bfsRowColumnGraph(labeled, predecessor);

				// We have finished labeling - check if a positive row was reached
				for (i = 0; i < n; i++)
				{
					if ((labeled[i]) &&
							(err_row_app[i].compareTo(BigInteger.ZERO) > 0))
					{
						positive_row_labeled = true;
						row_col_index = i;
						break;
					}
				}
				if (positive_row_labeled)
					break; // positive row reached - exit

				// No positive row was reached - update divisors

				if (TRACE)
					System.out.println("- no positive row reached - updating divisors.\n");

				BigRational delta = computeDelta(labeled);

				// if delta is inifinity then the problem is infeasible!
				if (delta.equals(DELTA_INF))
				{
					String rows_infeas = " Rows: (";
					String cols_infeas = " Columns: (";

					for (i = 0; i < n; i++)
						if (labeled[i])
							rows_infeas += (i + " ");
					rows_infeas += ")";

					for (j = 0; j < m; j++)
						if (labeled[n + j])
							cols_infeas += (j + " ");
					cols_infeas += ")";

					throw new IllegalArgumentException("Problem is infeasible." + rows_infeas + cols_infeas);
				}

				_NumberOfUpdates++;
				updateDivisors(labeled, delta);
			}

			// Positive row has been reached - make transfer

			if (TRACE)
				System.out.println("- positive row reached - making transfer.\n");

			_NumberOfTransfers++;
			while (predecessor[row_col_index] >= 0)
			{

				if (row_col_index < n)
				{
					// this is a row index
					i = row_col_index;
					j = predecessor[i] - n;
					_app[i][j] = _app[i][j].subtract(BigInteger.ONE);
				}
				else
				{
					// this is a column index
					j = row_col_index - n;
					i = predecessor[j + n];
					_app[i][j] = _app[i][j].add(BigInteger.ONE);
				}
				row_col_index = predecessor[row_col_index];
			}
		}
		app_computed = true;
	}

	/** Computes apportionment by the fast tie-and-transfer algorithm */
	public void computeApportionment()
	{
		if (app_computed)
			return;

		int i, j, k, n = _r.length, m = _c.length;
		boolean[] labeled = new boolean[n + m];
		int[] predecessor = new int[n + m];
		boolean adjust_columns = true; // should columns be adjusted?

		// Various initial feasibility checks
		check_basic_feasibility();
		if (any_min_restrictions)
			check_minimum_restrictions();

		// Should scaling be performed?
		boolean perform_scaling = true;
		if (_h.intValue() < 10 * n * m)
			perform_scaling = false;

		// Set up data structures depending on what is given already
		if (any_initial_apportionment &
				any_initial_row_divisors &
				any_initial_col_divisors)
		{

			// Both apportionment, row and column divisors are given
			check_quotients(); // apportionment and quotionts must be consistent
			check_col_sums(); // we assume that the column marginals are fulfilled
			adjust_columns = false;
			perform_scaling = false;
		}
		else
		{
			// No initial apportionment
			_app = new BigInteger[n][m];
			_col_D = new BigRational[m];

			if (any_initial_row_divisors)
			{

				// Row divisors are given
				perform_scaling = false;
			}
			else
			{
				_row_D = new BigRational[n];
				for (i = 0; i < n; i++)
					_row_D[i] = BigRational.ONE; // all equal to 1
			}
		}

		if (perform_scaling)
		{

			BigRational TEN = new BigRational(10); // scaling factor

			// Compute new total sum
			BigInteger scaled_h = (new BigRational(_h)).div(TEN).ceil().bigIntegerValue();

			// Compute scaled row sums
			BigInteger[] scaled_r = new BigInteger[n];
			for (i = 0; i < n; i++)
				scaled_r[i] = _r[i].divide(TEN.bigIntegerValue());

			// Round up so that the sum becomes the correct one (can be improved...)
			BigInteger scaled_r_total = BigInteger.ZERO;
			for (i = 0; i < n; i++)
				scaled_r_total = scaled_r_total.add(scaled_r[i]);
			int diff = scaled_h.subtract(scaled_r_total).intValue();
			for (i = 0; i < diff; i++)
				scaled_r[i] = scaled_r[i].add(BigInteger.ONE);

			// Compute scaled column sums
			BigInteger[] scaled_c = new BigInteger[m];
			for (j = 0; j < m; j++)
				scaled_c[j] = _c[j].divide(TEN.bigIntegerValue());

			// Round up so that the sum becomes the correct one (can be improved...)
			BigInteger scaled_c_total = BigInteger.ZERO;
			for (j = 0; j < m; j++)
				scaled_c_total = scaled_c_total.add(scaled_c[j]);
			diff = scaled_h.subtract(scaled_c_total).intValue();
			for (j = 0; j < diff; j++)
				scaled_c[j] = scaled_c[j].add(BigInteger.ONE);

			if (TRACE)
			{
				System.out.println("Scaled problem with house size " + scaled_h.toString());
			}

			// Compute solution to scaled problem using normal algorithm
			try
			{
				ExactBipropDivMethod bp = new ExactBipropDivMethod(_weights, scaled_r, scaled_c, _sp);

				bp.computeApportionment();
				_app = bp.getApportionment();
				_row_D = bp.getRowDivisors();
				_col_D = bp.getColumnDivisors();
				_NumberOfTransfers += bp.getNumberOfTransfers();
				_NumberOfUpdates += bp.getNumberOfUpdates();

				for (i = 0; i < n; i++)
					_row_D[i] = _row_D[i].div(TEN); // scale row divisors
			}
			catch (IllegalArgumentException e)
			{
				// In case the scaled problem is infeasible, then just ignore the result
			}

		}

		if (adjust_columns)
		{

			// Initialize column divisors such that the columns add upp correctly

			for (j = 0; j < m; j++)
			{

				BigRational[] col_weights = new BigRational[n];
				for (i = 0; i < n; i++)
					col_weights[i] = _weights[i][j].div(_row_D[i]);

				ExactDivMethod dm = new ExactDivMethod(col_weights, _c[j], _sp);

				// any minimum restrictions?
				if (any_min_restrictions)
				{
					BigInteger[] col_min_restrictions = new BigInteger[n];
					for (i = 0; i < n; i++)
						col_min_restrictions[i] = _min_restrictions[i][j];
					dm.setMinRestrictions(col_min_restrictions);
				}

				dm.computeApportionment();
				BigInteger[] col_app = dm.getApportionment();
				_col_D[j] = dm.getMaxDivisor();

				for (i = 0; i < n; i++)
					_app[i][j] = col_app[i];
			}
		}

		// Now we are ready to solve the problem using the tie-and-transfer algorithm

		tieAndTransfer();

		// Now have found an apportionment
		// Check for ties and update divisors

		for (i = 0; i < n; i++)
		{
			for (j = 0; j < m; j++)
			{

				// Weight must be positive to be a tie...
				if (!weights_positive[i][j])
					continue;

				// Check if quotient is equal to rounding function
				BigRational q = Quotient(i, j);
				boolean can_round_up = q.equals(_sp.get(_app[i][j]));
				boolean can_round_down = q.equals(_sp.get(_app[i][j].subtract(BigInteger.ONE)));

				if (any_min_restrictions)
					if (_app[i][j].equals(_min_restrictions[i][j]))
						can_round_down = false;

				if (can_round_up || can_round_down)
				{

					// Initialize labels and predecessors
					for (k = 0; k < n + m; k++)
					{
						labeled[k] = false;
						predecessor[k] = -1;
					}

					// Check for ties and update divisors

					if (can_round_down)
					{
						// can round DOWN: start search by labeling row i
						labeled[i] = true;
					}
					else
					{
						// can round UP: start search by labeling column j
						labeled[j + n] = true;
					}

					// Run BFS on row/column graph
					bfsRowColumnGraph(labeled, predecessor);

					// if row i or column j is not labeled then update divisors
					if (((can_round_down) && !labeled[j + n]) ||
							((can_round_up) && !labeled[i]))
					{

						BigRational delta = computeDelta(labeled);

						if (!delta.equals(DELTA_INF))
							// Adjust to middle: delta = (delta+1)/2
							delta = delta.add(BigRational.ONE).div(new BigRational("2"));
						else
							// can choose any value > 1
							delta = new BigRational("2");

						updateDivisors(labeled, delta);
					}
				}
			}
		}

		// Scale divisors such that the first column divisor is 1

		BigRational div_factor = _col_D[0];

		for (i = 0; i < n; i++)
			_row_D[i] = _row_D[i].mul(div_factor);
		for (j = 0; j < m; j++)
			_col_D[j] = _col_D[j].div(div_factor);

		// Perform final feasibility checks
		check_quotients();
		check_row_sums();
		check_col_sums();

		app_computed = true;
	}

	/** Gets apportionment (rounded matrix)
	 * @return Apportionment matrix */
	public BigInteger[][] getApportionment()
	{
		if (!(app_computed))
			computeApportionment();
		return _app;
	}

	/** Gets ties
	 * @return Tie matrix (0 = no tie, +1 = can be rounded up, -1 = can be rounded down) */
	public int[][] getTies()
	{
		if (!(app_computed))
			computeApportionment();
		int i, j, n = _r.length, m = _c.length;
		int[][] ties = new int[n][m];

		for (i = 0; i < n; i++)
		{
			for (j = 0; j < m; j++)
			{
				ties[i][j] = 0;
				if (weights_positive[i][j])
				{

					// Do we have a tie?
					BigRational q = Quotient(i, j);

					if (q.equals(_sp.get(_app[i][j])))
					{
						ties[i][j] = 1;
					}
					else
					{
						if (q.equals(_sp.get(_app[i][j].subtract(BigInteger.ONE))))
							ties[i][j] = -1;

						if (any_min_restrictions)
							if (_app[i][j].equals(_min_restrictions[i][j]))
								ties[i][j] = 0; // cannot round down after all
					}
				}
			}
		}

		return ties;
	}

	/** Gets row divisors
	 * @return Array of row divisors */
	public BigRational[] getRowDivisors()
	{
		if (!(app_computed))
			computeApportionment();
		return _row_D;
	}

	/** Gets column divisors
	 * @return Array of column divisors */
	public BigRational[] getColumnDivisors()
	{
		if (!(app_computed))
			computeApportionment();
		return _col_D;
	}

	/** Gets number of transfers made in tie-and-transfer algorithm
	 * @return Number of transfers */
	public int getNumberOfTransfers()
	{
		if (!(app_computed))
			computeApportionment();
		return _NumberOfTransfers;
	}

	/** Gets number of divisor updates made in tie-and-transfer algoritm
	 * @return Number of divisor updates */
	public int getNumberOfUpdates()
	{
		if (!(app_computed))
			computeApportionment();
		return _NumberOfUpdates;
	}

	/** Check that problem is not obviously infeasible */
	private void check_basic_feasibility()
	{
		int i, j, n = _r.length, m = _c.length;

		// Check that the weights are non-negative

		weights_positive = new boolean[n][m];
		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
			{

				// If negative weight then throw error
				if (_weights[i][j].compareTo(BigInteger.ZERO) < 0)
					throw new IllegalArgumentException("Negative weight given as input.");

				// Positive or zero weight?
				if (_weights[i][j].equals(BigRational.ZERO))
					weights_positive[i][j] = false;
				else
					weights_positive[i][j] = true;
			}

		// Check that problem is not obviously infeasible for d(0) = 0

		if (_sp.get(BigInteger.ZERO).equals(BigRational.ZERO))
		{

			for (i = 0; i < n; i++)
			{
				// Check that there are not too many positive weights in this row

				int num_positive = 0;
				for (j = 0; j < m; j++)
					if (weights_positive[i][j])
						num_positive++;

				if (_r[i].compareTo(new BigInteger(new Integer(num_positive).toString())) < 0)
				{
					throw new IllegalArgumentException("Problem is infeasible - too many positive weights in row " + i + ".");
				}
			}

			for (j = 0; j < m; j++)
			{
				// Check that there are not too many positive weights in this column

				int num_positive = 0;
				for (i = 0; i < n; i++)
					if (weights_positive[i][j])
						num_positive++;

				if (_c[j].compareTo(new BigInteger(new Integer(num_positive).toString())) < 0)
				{
					throw new IllegalArgumentException("Problem is infeasible - too many positive weights in column " + j + ".");
				}
			}
		}
	}

	/** Check feasibility of minimum restrictions */
	private void check_minimum_restrictions()
	{
		int i, j, n = _r.length, m = _c.length;

		for (i = 0; i < n; i++)
		{
			// Check that there are not too many restrictions in this row

			BigInteger row_sum = BigInteger.ZERO;
			for (j = 0; j < m; j++)
				row_sum = row_sum.add(_min_restrictions[i][j]);

			if (_r[i].compareTo(row_sum) < 0)
			{
				throw new IllegalArgumentException("Problem is infeasible - too many restrictions in row " + i + ".");
			}
		}

		for (j = 0; j < m; j++)
		{
			// Check that there are not too many restrictions in this column

			BigInteger col_sum = BigInteger.ZERO;
			for (i = 0; i < n; i++)
				col_sum = col_sum.add(_min_restrictions[i][j]);

			if (_c[j].compareTo(col_sum) < 0)
			{
				throw new IllegalArgumentException("Problem is infeasible - too many restrictions in column " + j + ".");
			}
		}
	}

	/** Check feasibility of quotionts */
	private void check_quotients()
	{
		int i, j, n = _r.length, m = _c.length;

		for (i = 0; i < n; i++)
			for (j = 0; j < m; j++)
			{

				if (weights_positive[i][j])
				{

					if (any_min_restrictions)
					{

						// Check that minimum restriction is fulfilled
						if (_app[i][j].compareTo(_min_restrictions[i][j]) < 0)
							throw new RuntimeException("Minimum restrictions not fulfilled.");

						// Check that the quotient is valid
						BigRational q = Quotient(i, j);
						if (!_app[i][j].equals(_min_restrictions[i][j]))
							if ((q.compareTo(_sp.get(_app[i][j].subtract(BigInteger.ONE))) < 0) ||
									(q.compareTo(_sp.get(_app[i][j])) > 0))
								throw new RuntimeException("Quotients not valid.");
					}
					else
					{

						// Check that the quotient is valid
						BigRational q = Quotient(i, j);
						if ((q.compareTo(_sp.get(_app[i][j].subtract(BigInteger.ONE))) < 0) ||
								(q.compareTo(_sp.get(_app[i][j])) > 0))
							throw new RuntimeException("Quotients not valid.");
					}
				}
				else
				{
					// The weight is zero - so should the apportionment be
					if (!_app[i][j].equals(BigInteger.ZERO))
						throw new RuntimeException("Positive apportionment but zero weight.");
				}
			}
	}

	/** Check marginal row sums */
	private void check_row_sums()
	{
		int i, j, n = _r.length, m = _c.length;

		for (i = 0; i < n; i++)
		{

			BigInteger row_sum = BigInteger.ZERO;

			for (j = 0; j < m; j++)
				if (weights_positive[i][j])
					row_sum = row_sum.add(_app[i][j]);

			if (!row_sum.equals(_r[i]))
				throw new RuntimeException("Row marginals not fulfilled.");
		}
	}

	/** Check marginal column sums */
	private void check_col_sums()
	{
		int i, j, n = _r.length, m = _c.length;

		for (j = 0; j < m; j++)
		{

			BigInteger col_sum = BigInteger.ZERO;

			for (i = 0; i < n; i++)
				if (weights_positive[i][j])
					col_sum = col_sum.add(_app[i][j]);

			if (!col_sum.equals(_c[j]))
				throw new RuntimeException("Column marginals not fulfilled.");
		}
	}
	public ExactSignPost getSP()
	{
		return _sp;
	}
	public BigInteger[] getRowSums()
	{
		return _r;
	}
	public BigInteger[] getColSums()
	{
		return _c;
	}
	public BigRational[][] getWeights()
	{
		return _weights;
	}
}
