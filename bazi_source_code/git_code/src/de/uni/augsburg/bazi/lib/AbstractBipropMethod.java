/*
 * @(#)AbstractBipropMethod.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Vector;

import de.uni.augsburg.bazi.lib.maxflow.Network;
import de.uni.augsburg.bazi.lib.vector.Method;
import de.uni.augsburg.bazi.lib.vector.PriorityQueue;

/** <b>Überschrift:</b> AbstractBipropMethod<br>
 * <b>Beschreibung:</b> Oberklasse für Biprop Algorithmen, die eine Form von alternierender Skalierung oder Tie&Transfer verwenden<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * 
 * @author Robert Bertossi, Christian Brand
 * @version 3.1 */
public abstract class AbstractBipropMethod
{

	/** Gewichtsmatrix */
	Weight[][] theMatrix;

	/** Parteisitze (Spalten-Marginalien) */
	int[] colApp;

	/** Distriktsitze (Zeilen-Marginalien) */
	int[] rowApp;

	/** Rundungsmethode */
	Signpost sp;

	/** Zeilendivisoren */
	Divisor[] dRowDivs;

	/** Spaltendivisoren */
	Divisor[] dColDivs;

	/** Anzahl der Zeilen */
	int rows = 0;

	/** Anzahl der Spalten */
	int cols = 0;

	/** MethodListeners */
	Vector<MethodListener> methodListeners = new Vector<MethodListener>();

	/** IterationListener */
	Vector<IterationListener> iterationListeners = new Vector<IterationListener>();

	/** Maximale Anzahl der Iteration, bis die Berechnung abbricht; -1 -> kein Limit */
	int maxIterations = -1;

	/** Rückgabeobjekt */
	BipropRet ret;

	/** BipropLibMessenger */
	BipropLibMessenger bmes;

	/** Last plus-ties */
	private int[] ptr = null;
	private int[] ptc = null;

	/** Debug? */
	boolean debug = Debug.BIPROP;

	/** Gibt an, ob vor der Berechnung eine Existenzprüfung stattfinden, nach der
	 * Berechnung die Divisoren poliert werden sollen und die Divisoren auf
	 * Korrektheit geprüft werden sollen. Beim inneren Hybrid Aufruf interessant. */
	boolean checksAndPolish = true;

	/** Setzt die Objekte die, für jede Berechnung gebraucht werden und initialisiert die Divisoren.
	 * @param weights Die Matrix mit den Gewichten
	 * @param aDistricts Die Sitze für die Distrikte
	 * @param aParties Die Sitze der Parteien
	 * @param aBmes Messenger Objekt, um Fehlermeldungen zu codieren
	 * @param aRet Biprop Rückgabeobjekt
	 * @param aSp Rundungsmethode
	 * @throws BipropException Fehler bei Berechnung */
	public AbstractBipropMethod(Weight[][] weights, int[] aDistricts,
			int[] aParties, Signpost aSp,
			BipropLibMessenger aBmes,
			BipropRet aRet)
			throws BipropException
	{
		theMatrix = weights;
		rowApp = aDistricts;
		colApp = aParties;
		bmes = aBmes;
		ret = aRet;
		sp = aSp;

		cols = colApp.length;
		rows = rowApp.length;
		dRowDivs = new Divisor[rows];
		dColDivs = new Divisor[cols];
	}

	/** Berechnet das Ergebnis, mit Hilfe des jeweiligen Algorithmuses.
	 * 
	 * @return Ergebnis der Berechnung
	 * @throws BipropException Fehler bei der Initialisierung oder Berechnung
	 * @throws IterationExceededException Überschreitung einer vorgegebenen oberen Grenze der Iterationen */
	public abstract BipropRet calculate() throws BipropException, IterationExceededException;

	/** Hinzufügen eines MethodListeners
	 * @param l MethodListener */
	public void addMethodListener(MethodListener l)
	{
		if (l == null)
		{
			return;
		}
		methodListeners.add(l);
	}

	/** Entfernen eines MethodListeners
	 * 
	 * @param l MethodListener, der entfernt werden soll
	 * @return <b>true</b>, fall der Listener entfernt werden konnte */
	public boolean removeMethodListener(MethodListener l)
	{
		return methodListeners.remove(l);
	}

	/** Benachrichtigen der Listener über eine neue Nachricht
	 * 
	 * @param msg die Nachricht */
	protected void notifyMethodListeners(String msg)
	{
		for (int i = 0; i < methodListeners.size(); i++)
		{
			MethodListener l = methodListeners.elementAt(i);
			l.printMessage(msg);
		}
	}

	/** Hinzufügen eines IterationListeners
	 * @param l IterationListener */
	public void addIterationListener(IterationListener l)
	{
		if (l == null)
		{
			return;
		}
		iterationListeners.add(l);
	}

	/** Entfernen eines IterationListeners
	 * 
	 * @param l IterationListener, der entfernt werden soll
	 * @return <b>true</b>, fall der Listener entfernt werden konnte */
	public boolean removeIterationListener(IterationListener l)
	{
		return iterationListeners.remove(l);
	}

	/** Benachrichtigen der Listener über eine Änderung des Iterationsstatus
	 * 
	 * @param k Nummer des Iterationsschrittes
	 * @param finished Ist die Berechnung beendet? */
	protected void fireIterationChanged(int k, boolean finished)
	{
		for (int i = 0; i < iterationListeners.size(); i++)
		{
			IterationListener l = iterationListeners.elementAt(i);
			l.iterationChanged(k, finished);
		}
	}

	/** Benachrichtigen der Listener über das Ende der Iteration */
	protected void fireIterationFinished()
	{
		for (int i = 0; i < iterationListeners.size(); i++)
		{
			IterationListener l = iterationListeners.elementAt(i);
			l.iterationFinished(); // (new IterationEvent(l, k, this, aktMethod, true));
		}
	}

	/** Maximalzahl der zulässigen Iterationen setzen. Falls ein Datensatz nach dieser Anzahl noch nicht berechnet ist, wird abgebrochen
	 * @param i Anzahl der zulässigen Iterationen
	 * @uml.property name="maxIterations" */
	public void setMaxIterations(int i)
	{
		maxIterations = i;
	}

	/** Maximalzahl der zulässigen Iterationen lesen
	 * @return Anzahl der zulässigen Iterationen
	 * @uml.property name="maxIterations" */
	public int getMaxIterations()
	{
		return maxIterations;
	}

	/** Überprüft die Eingabe auf offensichtliche Fehler (z.B. Zeilen-Marginalien != Spalten-Marginalien) und führt einen Existenz-Check mit dem MaxFlow Algorithmus durch.
	 * 
	 * Bei einem Fehler wird auch das Rückgabeobjekt entsprechend bestückt.
	 * 
	 * @return true, wenn alle Test erfolgreich waren, sonst false
	 * @throws BipropException Fehler, bei denen es keinen Sinn macht, das Rückgabeobjekt zu bestücken */
	public boolean checkExistence() throws BipropException
	{
		// erste Prüfung: Summen der Distrikt- und Parteiensitze müssen gleich sein
		// BD026-BD029
		int atmp = 0;
		for (int i = 0; i < rows; i++)
		{
			atmp += rowApp[i];
		}
		for (int j = 0; j < cols; j++)
		{
			atmp -= colApp[j];
		}
		if (atmp != 0)
		{
			// throw new DPPException(DPPException.INPUT_ERROR, "Difference Seats: " + atmp);
			// bmes.setError(BipropMessenger.INPUT_ERROR);
			// bmes.setMessage("Difference Seats: " + atmp);
			// return false;
			bmes.setError(BipropLibMessenger.INPUT_ERROR,
					"Input defective: difference Seats: " + atmp,
					new String[] { String.valueOf(atmp) });
			throw new BipropException("Input defective: difference Seats: " + atmp);
		}

		// Minimum Assignment/Check wether marginals are... BD030-BD040
		int[] rowsum = new int[rows];
		int[] colsum = new int[cols];

		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				if ((sp.s(0) == 0) && (theMatrix[i][j].weight > 0))
				{
					// support[i][j] = 1;
					rowsum[i]++;
					colsum[j]++;
				}
				else
				{
					// support[i][j] = 0;
				}
			}
		}

		boolean b = true;
		// boolean isMinError = false;
		for (int i = 0; i < rows; i++)
		{
			b &= !(rowApp[i] < rowsum[i]);
			// throw new DPPException(DPPException.EXISTENCE, Resource.getString("bazi.gui.biprop.na"));
		}
		for (int j = 0; j < cols; j++)
		{
			b &= !(colApp[j] < colsum[j]);
			// throw new DPPException(DPPException.EXISTENCE, Resource.getString("bazi.gui.biprop.na"));
		}
		// check wether row marginals are large enough for min requirements
		for (int i = 0; i < rows; i++)
		{
			int minSum = 0;
			for (int j = 0; j < cols; j++)
				minSum += theMatrix[i][j].min;
			b &= !(rowApp[i] < minSum);
			// isMinError &= ! (rowApp[i] < minSum);
		}

		if (!b)
		{
			ret.sError = "NA = Not available: Prespecified marginals are too small to serve all minimum requirements.";
			bmes.setError(BipropLibMessenger.EXISTENCE, ret.sError, null);
			// System.out.println(ret.sError);
			// aktm = new Weight[rows][cols];
			ret.app = new Weight[rows][cols];
			ret.divRowMin = new double[rows];
			ret.divRowMax = new double[rows];
			ret.divRowNice = new double[rows];
			ret.divColMin = new double[cols];
			ret.divColMax = new double[cols];
			ret.divColNice = new double[cols];
			ret.mulRowMin = new double[rows];
			ret.mulRowMax = new double[rows];
			ret.mulRowNice = new double[rows];
			ret.mulColMin = new double[cols];
			ret.mulColMax = new double[cols];
			ret.mulColNice = new double[cols];

			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					ret.app[i][j] = new Weight();
					ret.app[i][j].rdWeight = -1;
					ret.divColMin[j] = ret.divColMax[j] = ret.divColNice[j] = -1;
					ret.mulColMin[j] = ret.mulColMax[j] = ret.mulColNice[j] = -1;
				}
				ret.divRowMin[i] = ret.divRowMax[i] = ret.divRowNice[i] = -1;
				ret.mulRowMin[i] = ret.mulRowMax[i] = ret.mulRowNice[i] = -1;
			}
			return false;
			// return ret;
		}
		// -BD040
		// //

		// MaxFlow BD041
		ret.nw = null;
		if (sp.s(0) > 0)
		{
			ret.nw = new Network(theMatrix, rowApp, colApp, true);
		}
		else
		{
			ret.nw = new Network(theMatrix, rowApp, colApp, false);
		}

		if (!ret.nw.existSolution())
		{
			// il.iterationFinished();
			// throw new DPPException(DPPException.EXISTENCE, st);
			// Return-Daten auf NA setzen
			// ret.sError = Resource.getString("bazi.gui.biprop.na") + " " + st;
			ret.app = new Weight[rows][cols];
			ret.divRowMin = new double[rows];
			ret.divRowMax = new double[rows];
			ret.divRowNice = new double[rows];
			ret.divColMin = new double[cols];
			ret.divColMax = new double[cols];
			ret.divColNice = new double[cols];
			ret.mulRowMin = new double[rows];
			ret.mulRowMax = new double[rows];
			ret.mulRowNice = new double[rows];
			ret.mulColMin = new double[cols];
			ret.mulColMax = new double[cols];
			ret.mulColNice = new double[cols];

			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					ret.app[i][j] = new Weight();
					ret.app[i][j].rdWeight = -1;
					ret.divColMin[j] = ret.divColMax[j] = ret.divColNice[j] = -1;
					ret.mulColMin[j] = ret.mulColMax[j] = ret.mulColNice[j] = -1;
				}
				ret.divRowMin[i] = ret.divRowMax[i] = ret.divRowNice[i] = -1;
				ret.mulRowMin[i] = ret.mulRowMax[i] = ret.mulRowNice[i] = -1;
			}
			return false;
			// return ret;
		}

		return true;
	}

	/** Diese Methode beendet die Biprop Berechnung bei einem Abbruch durch den
	 * Benutzer kontrolliert.
	 * 
	 * @param ret Rückgabe Objekt
	 * @param sFlaws Fehlstände
	 * @param rzahl Rücksprünge */
	void userInterrupted(BipropRet ret, StringBuffer sFlaws, int rzahl)
	{
		ret.app = null;
		ret.divRowMin = null;
		ret.divRowMax = null;
		ret.divRowNice = null;
		ret.divColMin = null;
		ret.divColMax = null;
		ret.divColNice = null;
		fireIterationFinished();

		// noch schnell ein paar Debug-Ausgaben...
		if (debug)
		{
			notifyMethodListeners("Entwicklung der Fehlstände:\n" + sFlaws);
			notifyMethodListeners("Anzahl der Rücksprünge: " + rzahl);
		}
	}

	/** Ausgabe der Gewichte-Matrix
	 * @param wm Die Matrix
	 * @return String-Repräsentation der Gewichtsmatrix */
	public static String printMatrixW(Weight[][] wm)
	{
		int rows = wm.length;
		int cols = wm[0].length;
		int maximumLength = 0;
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				int l = (wm[i][j].weight + "").length();
				if (maximumLength < l)
				{
					maximumLength = l;
				}
			}
		}
		StringBuffer tmp = new StringBuffer("");
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				StringBuffer aktWeight = new StringBuffer("" + wm[i][j].weight);
				StringBuffer preSpaces = new StringBuffer();
				for (int k = aktWeight.length(); k < maximumLength; k++)
				{
					preSpaces.append(" ");
				}
				aktWeight.append("; ");
				tmp.append(preSpaces.append(aktWeight));
			}
			tmp.append("\n");
		}
		return tmp.toString();
	}

	/** Ausgabe der Zuteilungs-Matrix
	 * @param wm Die Matrix
	 * @return String-Repräsentation der Zuteilungsmatrix */
	public static String printMatrixR(Weight[][] wm)
	{
		int rows = wm.length;
		int cols = wm[0].length;
		int maximumLength = 0;
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				int l = (wm[i][j].rdWeight + wm[i][j].multiple).length();
				if (maximumLength < l)
				{
					maximumLength = l;
				}
			}
		}
		StringBuffer tmp = new StringBuffer("");
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				StringBuffer aktWeight = new StringBuffer(wm[i][j].rdWeight + wm[i][j].multiple);
				StringBuffer preSpaces = new StringBuffer();
				for (int k = aktWeight.length(); k < maximumLength; k++)
				{
					preSpaces.append(" ");
				}
				aktWeight.append("; ");
				tmp.append(preSpaces.append(aktWeight));
			}
			tmp.append("\n");
		}
		return tmp.toString();
	}


	/** Neuberechnung der Divisoren nach Tie&Transfer
	 * 
	 * @param appMatrix die Matrix mit der Zuteilung (wird wegen der Fähnchen
	 *          benötigt)
	 * @param rowDivs die Zeilendivisoren (aus T&T)
	 * @param colDivs die Spaltendivisoren (aus T&T)
	 * @throws BipropException */
	void reCalculateDivisors(Weight[][] appMatrix, double[] rowDivs,
			double colDivs[]) throws BipropException
	{

		LibMessenger messenger = new LibMessenger();
		// Method method = new Method(null, new Divisor(), messenger);
		boolean bm = false;

		// for muß später/irgendwann durch while ersetzt werden, Zusatz-Bedingung:
		// \exist f_ij=0 mit w_ij = s(m_ij) oder w_ij = s(m_ij-1) wobei w aus
		// Originalgewicht und aktuellen Divisoren berechnet wird.
		for (int k = 0; k < cols + rows; k++)
		{
			// erst spaltenweise
			for (int j = 0; j < cols; j++)
			{
				// Prüfe, die aktuelle Spalte ein Fähnchen enthält
				int maxf = 0;
				for (int i = 0; i < rows; i++)
				{
					maxf = Math.max(maxf, Math.abs(appMatrix[i][j].getFlag()));
					if (maxf > 0)
					{
						break;
					}
				}
				// Falls nicht...
				if (maxf == 0)
				{
					// Zuteilen und Divisoren anpassen!
					Weight[] aktcol = new Weight[rows];
					for (int i = 0; i < rows; i++)
					{
						aktcol[i] = theMatrix[i][j].clonew();
						aktcol[i].weight /= rowDivs[i];
					}
					Divisor divisor = new Divisor();

					bm = Method.statPowMethod(sp, divisor, colApp[j], aktcol, messenger, "");
					// Update der Divisoren
					/* aktColDivs[j] = divisor.getDivisor();
					 * aktColDivMin[j] = divisor.getDivisorLow();
					 * aktColDivMax[j] = divisor.getDivisorHigh();
					 * aktColMuls[j] = divisor.getMultiplier();
					 * aktColMulMin[j] = divisor.getMultiplierLow();
					 * aktColMulMax[j] = divisor.getMultiplierHigh(); */
					if (!bm)
						throw new BipropException("Error while recalculating divisors: " + messenger.getErrorMessage());
					colDivs[j] = divisor.getDivisor();
					dColDivs[j] = divisor;
				}
			}

			// jetzt zeilenweise
			for (int i = 0; i < rows; i++)
			{
				// Prüfe, die aktuelle Zeile ein Fähnchen enthält
				int maxf = 0;
				for (int j = 0; j < cols; j++)
				{
					maxf = Math.max(maxf, Math.abs(appMatrix[i][j].getFlag()));
					if (maxf > 0)
					{
						break;
					}
				}
				// Falls nicht...
				if (maxf == 0)
				{
					// Zuteilen und Divisoren anpassen!
					Weight[] aktrow = new Weight[cols];
					for (int j = 0; j < cols; j++)
					{
						aktrow[j] = theMatrix[i][j].clonew();
						aktrow[j].weight /= colDivs[j];
					}
					Divisor divisor = new Divisor();
					bm = Method.statPowMethod(sp, divisor, rowApp[i], aktrow, messenger, "");
					// Update der Divisoren
					/* aktRowDivs[i] = divisor.getDivisor();
					 * aktRowDivMin[i] = divisor.getDivisorLow();
					 * aktRowDivMax[i] = divisor.getDivisorHigh();
					 * aktRowMuls[i] = divisor.getMultiplier();
					 * aktRowMulMin[i] = divisor.getMultiplierLow();
					 * aktRowMulMax[i] = divisor.getMultiplierHigh(); */
					rowDivs[i] = divisor.getDivisor();
					dRowDivs[i] = divisor;
				}
			}
		}
	}

	/** Entfernt Ties, die offensichtlich keine verschiedenen Zuteilungen bedingen.
	 * 
	 * @param aktm Zuteilungsmatrix
	 * @throws BipropException */
	void removeUselessTies(Weight[][] aktm) throws BipropException
	{

		ptr = new int[rows];
		ptc = new int[cols];

		for (int i = 0; i < rows; i++)
		{
			// find min_j f_{ij}, max_j f_{ij}
			int min = 0;
			int max = 0;
			int maxj = -1;
			for (int j = 0; j < cols; j++)
			{
				if (aktm[i][j].getFlag() == 1)
				{
					max = aktm[i][j].getFlag();
					maxj = j;
				}
				if (aktm[i][j].getFlag() == -1)
				{
					min = aktm[i][j].getFlag();
				}
			}
			if ((min == -1) && (max == 1))
			{
				ptr[i] = maxj;
			}
			else
			{
				ptr[i] = -1;
				for (int j = 0; j < cols; j++)
				{
					aktm[i][j].multiple = "";
				}
			}
		}
		for (int j = 0; j < cols; j++)
		{
			// find min_j f_{ij}, max_j f_{ij}
			int min = 0;
			int max = 0;
			int maxi = -1;
			for (int i = 0; i < rows; i++)
			{
				if (aktm[i][j].getFlag() == 1)
				{
					max = aktm[i][j].getFlag();
					maxi = i;
				}
				if (aktm[i][j].getFlag() == -1)
				{
					min = aktm[i][j].getFlag();
				}
			}
			if ((min == -1) && (max == 1))
			{
				ptc[j] = maxi;
			}
			else
			{
				ptc[j] = -1;
				for (int i = 0; i < rows; i++)
				{
					aktm[i][j].multiple = "";
				}
			}
		}

		// ### Keep unflagging while remaining ties are only one-way
		int checkflags = 0;
		int maxpt = 0;
		for (int i = 0; i < rows; i++)
		{
			if (ptr[i] > maxpt)
			{
				maxpt = ptr[i];
			}
		}
		checkflags += maxpt;
		maxpt = 0;
		for (int j = 0; j < cols; j++)
		{
			if (ptc[j] > maxpt)
			{
				maxpt = ptc[j];
			}
		}
		checkflags += maxpt;
		while (checkflags > 0)
		{
			checkflags = 0;
			for (int i = 0; i < rows; i++)
			{
				// find max_j |f_{ij}|
				int flagged = 0;
				int maxj = 0;
				int minj = 0;
				for (int j = 0; j < cols; j++)
				{
					if (aktm[i][j].getFlag() != 0)
					{
						flagged = 1;
					}
					maxj = Math.max(maxj, aktm[i][j].getFlag());
					minj = Math.min(minj, aktm[i][j].getFlag());
				}
				if ((flagged == 1) && ((minj >= 0) || (maxj <= 0)))
				{
					for (int j = 0; j < cols; j++)
					{
						aktm[i][j].multiple = "";
						checkflags = 1;
					}
				}
			}
			for (int j = 0; j < cols; j++)
			{
				// find max_i |f_{ij}|
				int flagged = 0;
				int maxi = 0;
				int mini = 0;
				for (int i = 0; i < rows; i++)
				{
					if (aktm[i][j].getFlag() != 0)
					{
						flagged = 1;
					}
					maxi = Math.max(maxi, aktm[i][j].getFlag());
					mini = Math.min(mini, aktm[i][j].getFlag());
				}
				if ((flagged == 1) && ((mini >= 0) || (maxi <= 0)))
				{
					for (int i = 0; i < rows; i++)
					{
						aktm[i][j].multiple = "";
						checkflags = 1;
					}
				}
			}
		}

		// Nachdem die überflüssigen Fähnchen entfernt wurden, aktualiere ptr und ptc
		for (int i = 0; i < rows; i++)
		{
			// find min_j f_{ij}, max_j f_{ij}
			int min = 0;
			int max = 0;
			int maxj = -1;
			for (int j = 0; j < cols; j++)
			{
				if (aktm[i][j].getFlag() == 1)
				{
					max = aktm[i][j].getFlag();
					maxj = j;
				}
				if (aktm[i][j].getFlag() == -1)
				{
					min = aktm[i][j].getFlag();
				}
			}
			if ((min == -1) && (max == 1))
			{
				ptr[i] = maxj;
			}
			else
			{
				ptr[i] = -1;
				for (int j = 0; j < cols; j++)
				{
					aktm[i][j].multiple = "";
				}
			}
		}
		for (int j = 0; j < cols; j++)
		{
			// find min_j f_{ij}, max_j f_{ij}
			int min = 0;
			int max = 0;
			int maxi = -1;
			for (int i = 0; i < rows; i++)
			{
				if (aktm[i][j].getFlag() == 1)
				{
					max = aktm[i][j].getFlag();
					maxi = i;
				}
				if (aktm[i][j].getFlag() == -1)
				{
					min = aktm[i][j].getFlag();
				}
			}
			if ((min == -1) && (max == 1))
			{
				ptc[j] = maxi;
			}
			else
			{
				ptc[j] = -1;
				for (int i = 0; i < rows; i++)
				{
					aktm[i][j].multiple = "";
				}
			}
		}

		if (Debug.BIPROP)
		{
			StringBuffer debtmp = new StringBuffer("\n\nDivisorkorrektur: ");
			debtmp.append("-- Zuteilung nach Fahnenentfernung --\n");
			debtmp.append(printMatrixR(aktm));

			debtmp.append("ptr: ");
			for (int i = 0; i < rows; i++)
			{
				debtmp.append(ptr[i] + " ");
			}
			debtmp.append("\nptc: ");
			for (int j = 0; j < cols; j++)
			{
				debtmp.append(ptc[j] + " ");
			}
			debtmp.append("\n");
			notifyMethodListeners(debtmp.toString());
		}
	}


	/** Poliert die Divisoren und prüft, ob die Divisoren zu den Sitzen passen.
	 * 
	 * @param aktm Weight[][]
	 * @param rowDivs double[]
	 * @param colDivs double[]
	 * @throws BipropException */
	void finalChecks(Weight[][] aktm, double[] rowDivs, double[] colDivs) throws BipropException
	{

		// Falls die letzten +-Ties noch nicht berechnet wurden -> nachholen
		if (ptc == null || ptr == null)
			removeUselessTies(aktm);

		// ### Find median column divisor, standardize.

		double med = 0;
		// max_{ij} f_{ij} und max_j ptc_j
		int maxf = -1, maxptc = -1;
		// max_i f_{ij} \forall j ( := maxf_j)
		int[] maxfi = new int[cols];
		int maxfiCount = 0;
		int ptcjCount = 0;
		int cjCount = 0;
		int actf;
		// Hier ausnahmsweise cols außen, um max ptc gleichzeitig zu suchen!
		for (int j = 0; j < cols; j++)
		{
			maxfi[j] = -1;
			for (int i = 0; i < rows; i++)
			{
				actf = aktm[i][j].getFlag();
				if (actf > maxf)
				{
					maxf = actf;
				}
				if (actf > maxfi[j])
				{
					maxfi[j] = actf;
				}
			}
			if (maxfi[j] == 1)
			{
				maxfiCount++;
			}
			if (ptc[j] > maxptc)
			{
				maxptc = ptc[j];
			}
			if (ptc[j] > 0)
			{
				ptcjCount++;
			}
			if (colDivs[j] > 0)
			{
				cjCount++;
			}
		}

		if (maxf == 1)
		{
			ret.ties = true;
			// # either flagged columns
			double[] tdc = new double[maxfiCount];
			int offset = 0; // should always be <= maxfiCount!
			for (int j = 0; j < cols; j++)
			{
				if (maxfi[j] == 1)
				{
					tdc[offset++] = colDivs[j];
				}
			}
			// now should be offset==maxfiCount
			int[] sortIndex = Sort.sort(tdc);
			med = tdc[sortIndex[(int) Math.ceil((float) maxfiCount / 2) - 1]];
		}
		else if (maxptc > 0)
		{
			// # or tied columns
			double[] tdc = new double[ptcjCount];
			int offset = 0; // should always be <= ptcjCount!
			for (int j = 0; j < cols; j++)
			{
				if (ptc[j] > 0)
				{
					tdc[offset++] = colDivs[j];
				}
			}
			// now should be offset==ptcjCount
			int[] sortIndex = Sort.sort(tdc);
			med = tdc[sortIndex[(int) Math.ceil((float) ptcjCount / 2) - 1]];
		}
		else
		{
			// # or columns with positive marginals
			double[] tdc = new double[cjCount];
			int offset = 0; // should always be <= cjCount!
			for (int j = 0; j < cols; j++)
			{
				if (colDivs[j] > 0)
				{
					tdc[offset++] = colDivs[j];
				}
			}
			// now should be offset==cjCount
			int[] sortIndex = Sort.sort(tdc);
			med = tdc[sortIndex[(int) Math.ceil((float) cjCount / 2) - 1]];
		}

		// # Standardize column divisors D_j:
		Divisor[] divObjs = new Divisor[cols];
		LibMessenger libmsg = new LibMessenger();
		for (int j = 0; j < cols; j++)
		{
			// max_{ij} f_{ij}
			maxf = -1;
			for (int i = 0; i < rows; i++)
			{
				if (maxf < aktm[i][j].getFlag())
				{
					maxf = aktm[i][j].getFlag();
				}
			}
			if ((maxf == 1) && (colDivs[j] == med))
			{
				// # Degenerate limits
				try
				{
					if (dColDivs[j] == null)
						dColDivs[j] = new Divisor();
					dColDivs[j].setDivisorInterval(1, 1);
					dColDivs[j].setMultiplierInterval(1, 1);
				}
				catch (DivisorException e)
				{}
			}
			else
			{
				// # else pick nice numbers
				// Call to DM!
				divObjs[j] = new Divisor();
				Weight[] aktcol = new Weight[rows];
				for (int i = 0; i < rows; i++)
				{
					aktcol[i] = theMatrix[i][j].clonew();
					aktcol[i].weight /= rowDivs[i] * med;
				}
				Method.statPowMethod(sp, divObjs[j], colApp[j], aktcol, libmsg, "");

				// Update der Divisoren
				dColDivs[j] = divObjs[j];
				// plus-ties
				// ptc[j] = divObjs[j].getPt();
			}
		}

		// # Adjust row divisors C_i
		divObjs = new Divisor[rows];
		for (int i = 0; i < rows; i++)
		{
			maxf = -1; // max_j f_{ij}
			for (int j = 0; j < cols; j++)
			{
				int fij = aktm[i][j].getFlag();
				if (maxf < fij)
				{
					maxf = fij;
				}
			}

			int lt = -1;

			for (int j = 0; j < cols; j++)
			{
				if ((Math.abs(aktm[i][j].getFlag()) == 1) &&
						(dColDivs[j].getDivisor() == 1))
				{
					lt = j;
				}
			}
			if (lt > -1)
			{
				double d = sp.s(aktm[i][lt].rdWeight - (1 - aktm[i][lt].getFlag()) / 2) /
						theMatrix[i][lt].weight;
				try
				{
					if (dRowDivs[i] == null)
						dRowDivs[i] = new Divisor();
					dRowDivs[i].setMultiplierInterval(d, d);
				}
				catch (DivisorException e)
				{}
				d = theMatrix[i][lt].weight /
						sp.s(aktm[i][lt].rdWeight - (1 - aktm[i][lt].getFlag()) / 2);
				try
				{
					dRowDivs[i].setDivisorInterval(d, d);
				}
				catch (DivisorException e)
				{}
			}
			else
			{ // # Else row i is unflagged
				// ptr[i] = -1;
				// Call to DM!
				divObjs[i] = new Divisor();
				Weight[] aktrow = new Weight[cols];
				for (int j = 0; j < cols; j++)
				{
					aktrow[j] = theMatrix[i][j].clonew();
					aktrow[j].weight /= dColDivs[j].getDivisor();
				}
				Method.statPowMethod(sp, divObjs[i], rowApp[i], aktrow, libmsg, "");
				dRowDivs[i] = divObjs[i];

				// plus-ties
				// ptr[i] = divObjs[i].getPt();
			}
		}

		if (Debug.BIPROP)
		{
			StringBuffer debtmp = new StringBuffer("Mediansuche...\n");
			debtmp.append("maxf: " + maxf + " maxptc: " + maxptc + "\n");
			debtmp.append("Median: " + med + "\n");
			StringBuffer sptr = new StringBuffer("ptr: ");
			StringBuffer sptc = new StringBuffer("ptc: ");
			debtmp.append("Zeilendivisoren:\n");
			for (int i = 0; i < rows; i++)
			{
				debtmp.append("C_" + i + ": " + dRowDivs[i].getDivisor() + " ");
				sptr.append(ptr[i] + " ");
			}
			debtmp.append("\nSpaltendivisoren:\n");
			for (int j = 0; j < cols; j++)
			{
				debtmp.append("D_" + j + ": " + dColDivs[j].getDivisor() + " ");
				sptc.append(ptc[j] + " ");
			}
			debtmp.append("\n" + sptr + "\n" + sptc + "\n");

			notifyMethodListeners(debtmp.toString());

		}

		// Fehlerprüfung
		// ...
		// falls nicht erfolgreich, sofortiger Abbruch mit Exception
		StringBuffer debtmp = new StringBuffer("");
		// # check row fits, column fits.
		// im Pseudocode über max_x |sum....-M_x| > 0,
		// hier über einfachen Vergleich (geht schneller, ist aber im Pseudocode
		// nicht so schön :-)
		int[] sumR = new int[rows];
		int[] sumC = new int[cols];
		for (int j = 0; j < cols; j++)
		{
			sumC[j] = 0;
		}
		for (int i = 0; i < rows; i++)
		{
			sumR[i] = 0;
			for (int j = 0; j < cols; j++)
			{
				sumR[i] += aktm[i][j].rdWeight;
				sumC[j] += aktm[i][j].rdWeight;
			}
		}
		for (int i = 0; i < rows; i++)
		{
			if (sumR[i] != rowApp[i])
			{

				bmes.setError(BipropLibMessenger.DISTRICT_APPORTIONMENT_DEFECTIVE,
						// Resource.getString("bazi.xcp.biprop.district"),
						"District apportionment defective",
						null);
				throw new BipropException("District apportionment defective");
			}
		}

		for (int j = 0; j < cols; j++)
		{
			if (sumC[j] != colApp[j])
			{
				bmes.setError(BipropLibMessenger.PARTY_APPORTIONMENT_DEFECTIVE,
						// Resource.getString("bazi.xcp.biprop.party") +
						"Party apportionment defective" +
								"\nj: " + j + " sum: " + sumC[j] +
								" aParties: " + colApp[j],
						new String[] { String.valueOf(j), String.valueOf(sumC[j]),
								String.valueOf(colApp[j]) });
				throw new BipropException("Party apportionment defective" +
						"\nj: " + j + " sum: " + sumC[j] +
						" aParties: " + colApp[j]);
			}
		}

		// Divisoren prüfen
		double maxTied = Double.NEGATIVE_INFINITY, maxUntied = Double.NEGATIVE_INFINITY;
		double minTied = Double.POSITIVE_INFINITY, minUntied = Double.POSITIVE_INFINITY;
		double s;
		double tmp;
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				switch (aktm[i][j].getFlag())
				{
				case 0:
				{
					// unflagged entries should be <= 0
					s = sp.s(aktm[i][j].rdWeight);
					if (s > 0)
					{
						tmp = (theMatrix[i][j].weight / s) -
								(dRowDivs[i].getDivisor() * dColDivs[j].getDivisor());
						// wenn Minimumsbedingung eingehalten wird, wird tmp wieder auf 0 gesetzt
						if (aktm[i][j].rdWeight == theMatrix[i][j].min)
						{
							tmp = 0.0;
						}
						if (debug && (tmp > maxUntied) && (tmp > 0))
						{
							debtmp.append("maxUntied: i: " + i + " j: " + j + " tmp: " + tmp +
									"\n");
						}
						maxUntied = Math.max(maxUntied, tmp);
					}

					s = sp.s(aktm[i][j].rdWeight - 1);
					if (s > 0)
					{
						tmp = (theMatrix[i][j].weight / s) -
								(dRowDivs[i].getDivisor() * dColDivs[j].getDivisor());
						// wenn Minimumsbedingung eingehalten wird, wird tmp wieder auf 0 gesetzt
						if (aktm[i][j].rdWeight == theMatrix[i][j].min)
							tmp = 0.0;
						if (debug && (tmp < minUntied) && (tmp < 0))
						{
							debtmp.append("minUntied: i: " + i + " j: " + j + " tmp: " + tmp +
									"\n");
						}
						minUntied = Math.min(minUntied, tmp);
					}
					break;
				}
				case 1:
				{
					// maxTied should be <= 0
					s = sp.s(aktm[i][j].rdWeight);
					if (s > 0)
					{
						tmp = Math.round(((theMatrix[i][j].weight /
								(s * dRowDivs[i].getDivisor() * dColDivs[j].getDivisor()))
								- 1d) * 0.5 * Math.pow(10, 15));
						if (debug && (tmp > maxTied) && (tmp > 0))
						{
							debtmp.append("maxTied: i: " + i + " j: " + j + " tmp: " + tmp +
									" s: " + s
									+ " Ci: " + dRowDivs[i].getDivisor() + " Dj: " +
									dColDivs[j].getDivisor() +
									" wij: " + theMatrix[i][j].weight + "\n");
						}
						if (tmp > 0)
						{
							tmp = Math.round(Math.abs(((theMatrix[i][j].weight /
									(s * dRowDivs[i].getDivisor() * dColDivs[j].getDivisor()))
									- 1) * Math.pow(10, 14) / 2));
							ret.reducedAccuracy = true;
						}
						maxTied = Math.max(maxTied, tmp);
					}
					break;
				}
				case -1:
				{
					// minTied should be >= 0
					s = sp.s(aktm[i][j].rdWeight - 1);
					if (s > 0)
					{
						tmp = Math.round((theMatrix[i][j].weight /
								(s * dRowDivs[i].getDivisor() * dColDivs[j].getDivisor())
								- 1d) * 0.5 * Math.pow(10, 15));

						if (debug && (tmp < minTied) && (tmp < 0))
						{
							debtmp.append("minTied: i: " + i + " j: " + j + " tmp: " + tmp +
									" s: " + s
									+ " Ci: " + dRowDivs[i].getDivisor() + " Dj: " +
									dColDivs[j].getDivisor() +
									" wij: " + theMatrix[i][j].weight + "\n");
						}
						if (tmp < 0)
						{
							tmp = Math.round((theMatrix[i][j].weight /
									(s * dRowDivs[i].getDivisor() * dColDivs[j].getDivisor())
									- 1d) * 0.5 * Math.pow(10, 14));
							ret.reducedAccuracy = true;
						}
						minTied = Math.min(minTied, tmp);
					}
					break;
				}
				}

			}
		}

		if (debug)
		{
			notifyMethodListeners(debtmp.toString());
		}
		// Vorrübergehender Debugoutput
		if (Debug.BIPROP)
		{
			StringBuffer sb = new StringBuffer("Werte bei FinalChecks:\n");
			sb.append("MinUntied: " + minUntied + "\n");
			sb.append("MaxUntied: " + maxUntied + "\n");
			sb.append("MinTied: " + minTied + "\n");
			sb.append("MaxTied: " + maxTied + "\n");
			notifyMethodListeners(sb.toString());
		}

		// seats are calculated correctly, only divisors may be faulty => better show results with warning
		/* boolean bapp = false;
		 * String errs = "";
		 * if (maxTied > PriorityQueue.epsilon ) {
		 * errs += "Fatal #final check: maxTied>0: " + maxTied + "\n";
		 * bapp = true;
		 * }
		 * if (minTied < - PriorityQueue.epsilon) {
		 * errs += "Fatal #final check: minTied<0: " + minTied + "\n";
		 * bapp = true;
		 * }
		 * if (maxUntied > PriorityQueue.epsilon ) {
		 * errs += "Fatal #final check: maxUntied>0: " + maxUntied + "\n";
		 * bapp = true;
		 * }
		 * if (minUntied < - PriorityQueue.epsilon) {
		 * errs += "Fatal #final check: minUntied<0: " + minUntied + "\n";
		 * bapp = true;
		 * }
		 * if (bapp) {
		 * bmes.setError(BipropLibMessenger.DIVISOR_DEFECTIVE, errs, null);
		 * throw new FinalCheckException(errs);
		 * } */

		if (maxTied > PriorityQueue.epsilon || minTied < -PriorityQueue.epsilon || maxUntied > PriorityQueue.epsilon || minUntied < -PriorityQueue.epsilon)
		{
			ret.faultyDivisors = true;
		}
	}


	/** Erzeugt eine String Repräsentation eines int-Arrays
	 * 
	 * @param array int-Array
	 * @return int-Array als String */
	public static String printArray(int[] array)
	{
		StringBuffer tmp = new StringBuffer("");
		for (int i = 0; i < array.length; i++)
		{
			tmp.append(array[i] + "; ");
		}
		return tmp.toString();
	}

	/** Erzeugt eine String Repräsentation eines double-Arrays
	 * 
	 * @param array double-Array
	 * @return double-Array als String */
	public static String printDArray(double[] array)
	{
		StringBuffer tmp = new StringBuffer("");
		for (int i = 0; i < array.length; i++)
		{
			tmp.append(array[i] + "; ");
		}
		return tmp.toString();
	}

}
