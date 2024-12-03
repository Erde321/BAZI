package de.uni.augsburg.bazi.lib.newton;

import de.uni.augsburg.bazi.lib.BipropException;
import de.uni.augsburg.bazi.lib.BipropLibMessenger;
import de.uni.augsburg.bazi.lib.BipropRet;
import de.uni.augsburg.bazi.lib.IterationListener;
import de.uni.augsburg.bazi.lib.MethodListener;
import de.uni.augsburg.bazi.lib.ParameterOutOfRangeException;
import de.uni.augsburg.bazi.lib.Stationary;
import de.uni.augsburg.bazi.lib.Weight;

public class ReducedNewtonProcedureAdapter extends de.uni.augsburg.bazi.lib.AbstractBipropMethod
{

	private static ReducedNewtonProcedureAdapter self;

	private Weight[][] matrix;
	private int[] rowApp;
	private int[] colApp;
	private MethodListener ml;
	private IterationListener il;
	private BipropRet ret;
	private double l1_error;

	public static ReducedNewtonProcedureAdapter getReducedNewtonProcedureAdapter()
	{
		return self;
	}

	public ReducedNewtonProcedureAdapter(Weight[][] matrix, int[] rowApp, int[] colApp, BipropRet bpr1) throws BipropException, ParameterOutOfRangeException
	{
		super(matrix, rowApp, colApp, new Stationary(0.5), new BipropLibMessenger(), bpr1);
		this.matrix = matrix;
		this.rowApp = rowApp;
		this.colApp = colApp;
		this.ret = bpr1;

		self = this;
	}


	public void addMethodListener(MethodListener ml)
	{
		this.ml = ml;
	}

	public void addIterationListener(IterationListener il)
	{
		this.il = il;
	}

	public BipropRet calculate()
	{

		try
		{
			if (!checkExistence())
			{
				return ret;
			}
		}
		catch (BipropException e)
		{
			ml.printMessage(e.toString() + "\n" + e.getMessage());
			return ret;
		}
		ReducedNewtonProcedure rnm = ReducedNewtonProcedure.getReducedNewtonProcedure(matrix, rowApp, colApp);
		while (!rnm.finished())
		{
			rnm.doIteration();
			if (ml != null)
				ml.printMessage(rnm.getAktIterationInfo());
			if (il != null)
				il.iterationChanged(rnm.getNumberOfIterations(), rnm.finished());
		}

		if (ml != null)
			ml.printMessage(rnm.getFinishReason());

		ret.colit = rnm.getNumberOfIterations();
		ret.rowit = rnm.getNumberOfIterations();
		ret.app = this.matrix;

		double[] erg = rnm.getX_k();

		ret.divColMax = new double[ret.app[0].length];
		ret.divColMin = new double[ret.app[0].length];
		ret.divColNice = new double[ret.app[0].length];
		ret.divRowMax = new double[ret.app.length];
		ret.divRowMin = new double[ret.app.length];
		ret.divRowNice = new double[ret.app.length];

		for (int i = 0; i < ret.app.length; i++)
		{
			ret.divRowMax[i] = 1d / erg[i];
			ret.divRowMin[i] = 1d / erg[i];
			ret.divRowNice[i] = 1d / erg[i];
		}
		for (int i = 0; i < ret.app[0].length - 1; i++)
		{
			ret.divColMax[i] = 1d / erg[ret.app.length + i];
			ret.divColMin[i] = 1d / erg[ret.app.length + i];
			ret.divColNice[i] = 1d / erg[ret.app.length + i];
		}
		ret.divColMax[ret.app[0].length - 1] = 1d;
		ret.divColMin[ret.app[0].length - 1] = 1d;
		ret.divColNice[ret.app[0].length - 1] = 1d;

		if (rnm.error())
		{
			ret.sError = rnm.getFinishReason();
		}

		l1_error = rnm.getL1_error();

		return ret;
	}

	public double getL1_error()
	{
		return this.l1_error;
	}
}
