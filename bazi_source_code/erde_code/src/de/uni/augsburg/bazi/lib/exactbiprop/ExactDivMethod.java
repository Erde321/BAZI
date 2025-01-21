package de.uni.augsburg.bazi.lib.exactbiprop;

/* ExactDivMethod.java -- Divisor method for rounding of vectors
 **                        using exact rational arithmetic 
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

/** Vector apportionment via divisor method and rational exact arithmetic.
 * Computes vector apportionment with an optional divisor guess.
 * If no divisor guess is provided, the canonical divisor is used as initial divisor.
 * The signpost formula is given as a parameter to the constructor.
 * Minimum restrictions on seat numbers can be given. Apportionment, ties and min/max divisors can be returned.
 * <P>
 * Usage samples: <SMALL>
 * 
 * <PRE>
 * int[] weights = { 10, 10, 9, 9, 5, 5, 5 }; // weights
 * int[] min_restr = { 1, 1, 1, 1, 1, 1, 1 }; // minimum restrictions
 * int h = 8; // required total
 * ExactSignPost sp = new ExactSignPost(); // signpost formula
 * // compute apportionment
 * ExactDivMethod dm = new ExactDivMethod(weights, h, sp);
 * dm.setMinRestrictions(min_restr); // minimum restrictions (optional)
 * dm.computeApportionment();
 * BigInteger[] app = dm.getApportionment();
 * int[] ties = dm.getTies();
 * int i;
 * for (i = 0; i &lt; 7; i++)
 * 	System.out.println(&quot;Apportionment &quot; + i + &quot; = &quot; + app[i].toString());
 * for (i = 0; i &lt; 7; i++)
 * 	System.out.println(&quot;Ties &quot; + i + &quot; = &quot; + ties[i]);
 * </PRE>
 * 
 * </SMALL>
 * <P>
 * The ExactDivMethod source is available here: <A HREF="ExactDivMethod.java">ExactDivMethod.java</A>.
 * 
 * @author Martin Zachariasen
 * @version 1.3, January 2007 */
public class ExactDivMethod
{

	// Weights that should be used for making the apportionment
	private BigRational[] _weights;

	// Required sum
	private BigInteger _h;

	// Signpost formula
	private ExactSignPost _sp;

	// Any minimum restrictions?
	private boolean any_min_restrictions = false;

	// Actual minimum restrictions (if any)
	private BigInteger[] _min_restrictions;

	// Divisor
	private BigRational D = BigRational.ZERO;

	// Computed apportionment
	private BigInteger[] _app;

	// Has apportionment been computed?
	private boolean app_computed = false;

	// Vector identifying positive weight elements
	private boolean[] weights_positive;

	// A kind of infinity (but usually not large enough...)
	private final static BigRational INFINITY = new BigRational("1000000");

	/** Constructor (int input)
	 * @param weights Weights
	 * @param h Required sum
	 * @param sp Signpost formula */
	public ExactDivMethod(int[] weights,
			int h,
			ExactSignPost sp)
	{
		int i;
		_weights = new BigRational[weights.length];
		for (i = 0; i < weights.length; i++)
			_weights[i] = new BigRational(weights[i]);

		_h = new BigInteger(new Integer(h).toString());
		_sp = sp;
		checkInput();
	}

	/** Constructor (int and double input)
	 * @param weights Weights
	 * @param h Required sum
	 * @param sp Signpost formula */
	public ExactDivMethod(double[] weights,
			int h,
			ExactSignPost sp)
	{
		int i;
		_weights = new BigRational[weights.length];
		for (i = 0; i < weights.length; i++)
			_weights[i] = new BigRational(new Double(weights[i]).toString());

		_h = new BigInteger(new Integer(h).toString());
		_sp = sp;
		checkInput();
	}

	/** Constructor (BigInteger and BigRational input)
	 * @param weights Weights
	 * @param h Required sum
	 * @param sp Signpost formula */
	public ExactDivMethod(BigRational[] weights,
			BigInteger h,
			ExactSignPost sp)
	{
		_weights = weights;
		_h = h;
		_sp = sp;
		checkInput();
	}

	/** Sets initial divisor (double input).
	 * @param initial_divisor Initial divisors guess */
	public void setInitialDivisor(double initial_divisor)
	{
		// Check that divisor guess is positive
		if (initial_divisor <= 0.0)
			throw new IllegalArgumentException("Divisor guess must be positive.");

		D = new BigRational(new Double(initial_divisor).toString());
	}

	/** Sets initial divisor (BigRational input).
	 * @param initial_divisor Initial divisors guess */
	public void setInitialDivisor(BigRational initial_divisor)
	{
		// Check that divisor guess is positive
		if (initial_divisor.compareTo(BigRational.ZERO) <= 0.0)
			throw new IllegalArgumentException("Divisor guess must be positive.");

		D = initial_divisor;
	}

	/** Sets minimum restrictions (BigInteger input)
	 * @param min_restrictions Minimum restrictions */
	public void setMinRestrictions(BigInteger[] min_restrictions)
	{
		// Check that array has correct length
		if (min_restrictions.length != _weights.length)
			throw new IllegalArgumentException("Minimum restrictions array wrong dimension");

		_min_restrictions = min_restrictions;
		any_min_restrictions = true;
		checkInput();
	}

	/** Sets minimum restrictions (int input)
	 * @param min_restrictions Minimum restrictions */
	public void setMinRestrictions(int[] min_restrictions)
	{
		// Check that array has correct length
		if (min_restrictions.length != _weights.length)
			throw new IllegalArgumentException("Minimum restrictions array wrong dimension");

		int i;
		_min_restrictions = new BigInteger[min_restrictions.length];
		for (i = 0; i < _min_restrictions.length; i++)
			_min_restrictions[i] = new BigInteger(new Integer(min_restrictions[i]).toString());
		any_min_restrictions = true;
		checkInput();
	}


	/** Computes apportionment */
	public void computeApportionment()
	{
		if (app_computed)
			return; // apportionment has already been computed
		int i, n = _weights.length;

		// Set up initial divisor
		if (D.compareTo(BigRational.ZERO) == 0)
		{

			// no divisor guess given as input - use canonical divisor

			BigRational sum_w = BigRational.ZERO;
			for (i = 0; i < n; i++)
				sum_w = sum_w.add(_weights[i]);

			D = sum_w.div(new BigRational(_h)); // canonical divisor
		}

		// Initialize apportionment and sum
		_app = new BigInteger[n];
		BigInteger sum_t = BigInteger.ZERO;
		for (i = 0; i < n; i++)
		{
			_app[i] = _sp.round(_weights[i].div(D));
			sum_t = sum_t.add(_app[i]);
		}

		// If there are any minimum restrictions then enforce these
		if (any_min_restrictions)
		{
			sum_t = BigInteger.ZERO;
			for (i = 0; i < n; i++)
			{
				if (_app[i].compareTo(_min_restrictions[i]) < 0)
					_app[i] = _min_restrictions[i];
				sum_t = sum_t.add(_app[i]);
			}
		}

		// Make final adjustment of seat assignments
		if (sum_t.compareTo(_h) < 0)
		{

			// current sum is less than required sum

			while (sum_t.compareTo(_h) < 0)
			{

				// Pick next seat
				int si = 0;
				BigRational min_r = null;
				for (i = 0; i < n; i++)
				{
					if (weights_positive[i])
					{
						BigRational r = _sp.get(_app[i]).div(_weights[i]);
						if ((min_r == null) || (r.compareTo(min_r) < 0))
						{
							si = i; // new minimum ratio found
							min_r = r;
						}
					}
				}
				// If no positive weight then problem is infeasible
				if (min_r == null)
					throw new IllegalArgumentException("Problem is infeasible - no positive weights.");

				// Add seat to party
				_app[si] = _app[si].add(BigInteger.ONE);
				sum_t = sum_t.add(BigInteger.ONE);
				if (min_r.compareTo(BigRational.ZERO) > 0)
					D = min_r.inv(); // new divisor
				else
					D = INFINITY;
			}
		}
		else
		{

			// current sum is greater than or equal to required sum

			while (sum_t.compareTo(_h) > 0)
			{
				// Pick next seat
				int si = 0;
				BigRational max_r = null;
				for (i = 0; i < n; i++)
				{
					if (weights_positive[i])
					{

						// check minimum restrictions
						if ((any_min_restrictions) && (_app[i].equals(_min_restrictions[i])))
							continue; // cannot subtract seat

						BigRational r = _sp.get(_app[i].subtract(BigInteger.ONE)).div(_weights[i]);
						if ((max_r == null) || (r.compareTo(max_r) > 0))
						{
							si = i; // new maximum ratio found
							max_r = r;
						}
					}
				}

				// Subtract seat from party
				_app[si] = _app[si].subtract(BigInteger.ONE);
				sum_t = sum_t.subtract(BigInteger.ONE);
				if (max_r.compareTo(BigRational.ZERO) > 0)
					D = max_r.inv(); // new divisor
				else
					D = INFINITY;
			}
		}
		app_computed = true;
	}

	/** Gets apportionment
	 * @return Apportionment array */
	public BigInteger[] getApportionment()
	{
		if (!(app_computed))
			computeApportionment();
		return _app;
	}

	/** Gets ties
	 * @return Tie array (0 = no tie, +1 = can be rounded up, -1 = can be rounded down) */
	public int[] getTies()
	{
		if (!(app_computed))
			computeApportionment();
		int i, n = _weights.length;

		// Compute average divisor
		BigRational D_min = getMinDivisor();
		BigRational D_max = getMaxDivisor();
		BigRational D = D_min.add(D_max).div(new BigRational("2"));

		int[] ties = new int[n];

		for (i = 0; i < n; i++)
		{
			ties[i] = 0;

			if (weights_positive[i])
			{

				BigRational q = _weights[i].div(D);

				if (q.equals(_sp.get(_app[i])))
				{
					ties[i] = 1;
				}
				else
				{
					// minimum restrictions?
					if ((any_min_restrictions) && (_app[i].equals(_min_restrictions[i])))
						continue; // cannot subtract seat

					if (q.equals(_sp.get(_app[i].subtract(BigInteger.ONE))))
						ties[i] = -1;
				}
			}
		}

		return ties;
	}

	/** Gets minimum divisor
	 * @return Minimum possible divisor for this problem */
	public BigRational getMinDivisor()
	{
		if (!(app_computed))
			computeApportionment();
		int i, n = _weights.length;

		BigRational D_min = null;
		BigRational min_r = null;

		for (i = 0; i < n; i++)
		{
			if (weights_positive[i])
			{
				BigRational r = _sp.get(_app[i]).div(_weights[i]);
				if ((min_r == null) || r.compareTo(min_r) < 0)
					min_r = r;
			}
		}

		if ((min_r != null) && (min_r.compareTo(BigRational.ZERO) > 0))
			D_min = min_r.inv();
		else
			D_min = INFINITY;

		return D_min;
	}

	/** Gets maximum divisor
	 * @return Maximum possible divisor for this problem */
	public BigRational getMaxDivisor()
	{
		if (!(app_computed))
			computeApportionment();
		int i, n = _weights.length;

		BigRational D_max = null;
		BigRational max_r = null;

		for (i = 0; i < n; i++)
		{
			if (weights_positive[i])
			{
				// check minimum restrictions
				if ((any_min_restrictions) && (_app[i].equals(_min_restrictions[i])))
					continue; // cannot subtract seat

				BigInteger a = _app[i].subtract(BigInteger.ONE);
				BigRational r = _sp.get(a).div(_weights[i]);

				if ((max_r == null) || r.compareTo(max_r) > 0)
					max_r = r;
			}
		}

		if ((max_r != null) && (max_r.compareTo(BigRational.ZERO) > 0))
			D_max = max_r.inv();
		else
			D_max = INFINITY;

		return D_max;
	}

	/** Sets up data structures and checks that problem is feasible */
	private void checkInput()
	{
		int i;
		int num_positive = 0;
		weights_positive = new boolean[_weights.length];

		for (i = 0; i < _weights.length; i++)
		{

			// If negative weight then throw error
			if (_weights[i].compareTo(BigInteger.ZERO) < 0)
				throw new IllegalArgumentException("Negative weight given as input.");

			// Positive or zero weight?
			if (_weights[i].equals(BigRational.ZERO))
			{
				weights_positive[i] = false;
			}
			else
			{
				weights_positive[i] = true;
				num_positive++;
			}
		}

		// Is house size negative?
		if (_h.compareTo(BigInteger.ZERO) < 0)
		{
			throw new IllegalArgumentException("Negative house size.");
		}

		// Is problem obviously infeasible? (d(0) = 0 and too many positive weights)
		if ((_sp.get(BigInteger.ZERO).equals(BigRational.ZERO)) &&
				(_h.compareTo(new BigInteger(new Integer(num_positive).toString())) < 0))
		{
			throw new IllegalArgumentException("Problem is infeasible - too many positive weights.");
		}

		// If there are minimum restrictions then check these
		if (any_min_restrictions)
		{
			BigInteger min_restrictions_sum = BigInteger.ZERO;
			for (i = 0; i < _min_restrictions.length; i++)
			{
				min_restrictions_sum = min_restrictions_sum.add(_min_restrictions[i]);
			}
			if (min_restrictions_sum.compareTo(_h) > 0)
			{
				throw new IllegalArgumentException("Sum of minimum restrictions exceeds house size.");
			}
		}
	}

}
