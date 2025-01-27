package de.uni.augsburg.bazi.lib.exactbiprop;

/* ExactSignPost.java -- Signpost functions 
 **                       using exact rational arithmetic 
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
import java.lang.Math;

/** Signpost functions using exact rational arithmetic. Supports all classical rounding functions. Default construction without parameters results in standard rounding.
 * <P>
 * Usage samples: <SMALL>
 * 
 * <PRE>
 * // Standard rounding/Sainte-Lague/Webster [three alternatives for construction] ExactSignPost sp = new ExactSignPost(); ExactSignPost sp = new ExactSignPost( new BigRational(&quot;1/2&quot;)
 * // ); ExactSignPost sp = new ExactSignPost( ExactSignPost.STATIONARY_ROUNDING, new BigRational(&quot;1/2&quot;) ); // Adams [two alternatives for construction] ExactSignPost sp = new
 * // ExactSignPost( BigRational.ZERO ); ExactSignPost sp = new ExactSignPost( ExactSignPost.STATIONARY_ROUNDING, BigRational.ZERO ); // Jefferson [two alternatives for construction]
 * // ExactSignPost sp = new ExactSignPost( BigRational.ONE ); ExactSignPost sp = new ExactSignPost( ExactSignPost.STATIONARY_ROUNDING, BigRational.ONE ); // Dean ExactSignPost sp =
 * // new ExactSignPost( ExactSignPost.DEAN_ROUNDING, BigRational.ZERO ); // Hill ExactSignPost sp = new ExactSignPost( ExactSignPost.HILL_ROUNDING, BigRational.ZERO ); // Standard
 * // rounding with lower bound of 1, i.e., always round positive weights up ExactSignPost sp = new ExactSignPost(); sp.put( BigInteger.ZERO, BigRational.ZERO ); // d(0) = 0 //
 * // Modified/Scandinavian Sainte-Lague ExactSignPost sp = new ExactSignPost(); sp.put( BigInteger.ZERO, new BigRational(&quot;0.7&quot;) ); // d(0) = 0.7
 * </PRE>
 * 
 * </SMALL>
 * <P>
 * The ExactSignPost source is available here: <A HREF="ExactSignPost.java">ExactSignPost.java</A>.
 * @author Martin Zachariasen
 * @version 1.3, January 2007 */
public class ExactSignPost
{

	// Define signpost function types
	public final static int STATIONARY_ROUNDING = 1;
	public final static int DEAN_ROUNDING = 2;
	public final static int HILL_ROUNDING = 3;

	// Rounding type (default: stationary rounding)
	private int _rounding_type = STATIONARY_ROUNDING;

	// Parameter for stationary rounding (default: standard rounding)
	private BigRational _stationary_param = new BigRational("1/2");

	// Cached function values (includes changes by the user)
	private int INIT_CACHE_SIZE = 100;
	private BigRational[] cached_values;

	/** Constructor (standard rounding) */
	public ExactSignPost()
	{
		/* No parameter (standard rounding) */
		initializeCache();
	}

	/** Constructor (stationary rounding)
	 * @param stationary_param Parameter for stationary rounding (number in the interval [0,1]) */
	public ExactSignPost(BigRational stationary_param)
	{
		_rounding_type = STATIONARY_ROUNDING;
		_stationary_param = stationary_param;
		initializeCache();
	}

	/** Constructor (general rounding)
	 * @param rounding_type Rounding method (stationary = 1, Dean = 2, Hill = 3)
	 * @param stationary_param Parameter for stationary rounding (number in the interval [0,1]) */
	public ExactSignPost(int rounding_type,
			BigRational stationary_param)
	{
		if ((rounding_type != STATIONARY_ROUNDING) &&
				(rounding_type != DEAN_ROUNDING) &&
				(rounding_type != HILL_ROUNDING))
		{
			throw new IllegalArgumentException("Unknown rounding method given");
		}

		if ((rounding_type == STATIONARY_ROUNDING) &&
				((stationary_param.compareTo(BigRational.ZERO) < 0) ||
				(stationary_param.compareTo(BigRational.ONE) > 0)))
		{
			throw new IllegalArgumentException("Parameter for stationary rounding not in interval [0,1]");
		}
		_rounding_type = rounding_type;
		_stationary_param = stationary_param;
		initializeCache();
	}

	/** Initializes table with cached function valuse */
	private void initializeCache()
	{
		int i;
		cached_values = new BigRational[INIT_CACHE_SIZE];

		for (i = 0; i < INIT_CACHE_SIZE; i++)
			cached_values[i] = null;
	}

	/** Gets value of signpost formula
	 * @param x Integer value
	 * @return Signpost value for integer */
	public BigRational get(BigInteger x)
	{
		int int_x = x.intValue();

		// If argument is less than zero then return zero
		if (int_x < 0)
			return (BigRational.ZERO);

		// If function value is cached then return it
		if ((int_x < cached_values.length) && (cached_values[int_x] != null))
			return cached_values[int_x];

		// Otherwise compute value directly (and cache it)
		double dx;
		BigRational sp = null;
		switch (_rounding_type)
		{
		case STATIONARY_ROUNDING:
			// Compute x + param exactly
			sp = new BigRational(x).add(_stationary_param);
			break;
		case DEAN_ROUNDING:
			// Compute x*(x+1)/(x + 0.5) exactly
			sp = new BigRational(x);
			BigRational t = new BigRational(x);
			t = t.add(BigRational.ONE);
			sp = sp.multiply(t);
			t = t.subtract(new BigRational("1/2"));
			sp = sp.divide(t);
			break;
		case HILL_ROUNDING:
			// Compute sqrt(x*(x+1)) by double approximation
			dx = x.doubleValue();
			sp = new BigRational(Double.toString(Math.sqrt(dx * (dx + 1.0))));
			break;
		}
		if (int_x < cached_values.length)
			cached_values[int_x] = sp;

		return sp;
	}

	/** Defines value of signpost formula
	 * @param x Integer value
	 * @param fx New signpost value */
	public void put(BigInteger x, BigRational fx)
	{
		// Check that given signpost value is feasible
		BigRational rx = new BigRational(x);
		BigRational rx1 = rx.add(BigRational.ONE);
		if ((fx.compareTo(rx) < 0) || (fx.compareTo(rx1) > 0))
			throw new IllegalArgumentException("Given function value is out of bounds.");

		// Update cached value (and increase cache table if necessary)
		int int_x = x.intValue();
		if (int_x < cached_values.length)
		{
			// Update cached value
			cached_values[int_x] = fx;
		}
		else
		{
			// Increase cache table and update
			int i;
			BigRational[] tmp_cached_values = new BigRational[2 * int_x];

			for (i = 0; i < cached_values.length; i++)
				tmp_cached_values[i] = cached_values[i];

			for (i = cached_values.length; i < tmp_cached_values.length; i++)
				tmp_cached_values[i] = null;

			tmp_cached_values[int_x] = fx; // update with given value

			cached_values = tmp_cached_values; // make new table the current table
		}
	}

	/** Rounding using signpost formula
	 * @param q Rational number that should be rounded
	 * @return Rounded integer value */
	public BigInteger round(BigRational q)
	{
		// If input is zero or less then return zero
		if (q.compareTo(BigRational.ZERO) <= 0)
			return BigInteger.ZERO;

		// Compute floor of q and compare to signpost function
		BigInteger x = q.floor().bigIntegerValue();
		if (q.compareTo(get(x)) >= 0)
			x = x.add(BigInteger.ONE);
		return x;
	}
	public BigRational getStationary_Param()
	{
		return this._stationary_param;
	}

}
