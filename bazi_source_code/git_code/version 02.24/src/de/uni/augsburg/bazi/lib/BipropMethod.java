/*
 * @(#)BipropMethod.java 3.1 18/04/05
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import de.uni.augsburg.bazi.lib.maxflow.Network;
import de.uni.augsburg.bazi.lib.vector.Method;

/** <b>Überschrift:</b> Biproportionale Zuteilung<br>
 * <b>Beschreibung:</b> Biproportionale Berechnungsmethode mit alternativer Skalierung Wurde im wesentlichen nach AbstractBipropMethod und ASMethod kopiert<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg<br>
 * @author Florian Kluge, Christian Brand
 * @version 3.1
 * @deprecated */
@Deprecated
public class BipropMethod
{

	/** Divisorupatemethode: midpoint */
	public static final int DIV_MDPT = 0;

	/** Divisorupatemethode: extreme */
	public static final int DIV_EXTR = 1;

	/** Divisorupatemethode: random */
	public static final int DIV_RAND = 2;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktRowDivs;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktRowDivMin;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktRowDivMax;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktColDivs;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktColDivMin;

	/** aktuelle Divisoren mit Grenzen */
	private double[] aktColDivMax;

	/** aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen */
	private double[] aktRowMuls;

	/** aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen */
	private double[] aktRowMulMin;

	/** aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen */
	private double[] aktRowMulMax;

	/** aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen */
	private double[] aktColMuls;

	/** aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen */
	private double[] aktColMulMin;

	/** aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen */
	private double[] aktColMulMax;

	/** Gewichtsmatrix
	 * @uml.property name="theMatrix"
	 * @uml.associationEnd multiplicity="(0 -1)" */
	private Weight[][] theMatrix;

	/** Spalten-Marginalien */
	private int[] colApp;

	/** Zeilen-Marginalien */
	private int[] rowApp;

	/** Anzahl der Zeilen */
	int rows = 0;

	/** Anzahl der Spalten */
	int cols = 0;

	/** MethodListeners */
	private final Vector<MethodListener> methodListeners = new Vector<MethodListener>();

	/** IterationListener */
	private final Vector<IterationListener> iterationListeners = new Vector<IterationListener>();

	/** Maximale Anzahl der Iteration, bis die Berechnung abbricht; -1 -> kein Limit
	 * @uml.property name="maxIterations" */
	private int maxIterations = -1;

	/** Rundungsmethode */
	private Signpost sp;

	/** Verwendete Divisorupdatemethode */
	private int divisorUpdateMethod = DIV_MDPT;

	/** Standardkonstruktor. Erzeugt ein leeres Objekt */
	public BipropMethod()
	{}

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

	/** Biproportionale Divisor Methode Überarbeitete Implementierung
	 * 
	 * @param weights Die Matrix mit den Gewichten
	 * @param aDistricts Die Sitze für die Distrikte
	 * @param aParties Die Sitze der Parteien
	 * @param sp Die zu verwendende Sprungstellenklasse
	 * @param bmes Messenger Objekt, um Fehlermeldungen zu codieren
	 * @param ret Biprop Rückgabeobjekt
	 * @param div_update Verwendete Methode um die Divisoren bei jedem Iterationsschritt anzupassen
	 * @param useTT gibt an, ob Tie&Transfer benutzt werden soll
	 * @return BipropRet == ret
	 * @throws BipropException Fehler bei Berechnung
	 * @throws IterationExceededException Maximale Anzahl der Iterationen wurde
	 *           überschritten */
	public BipropRet divMethMatrix(Weight[][] weights, int[] aDistricts,
			int[] aParties, Signpost sp,
			BipropLibMessenger bmes,
			BipropRet ret,
			int div_update,
			boolean useTT
			) throws
					BipropException, IterationExceededException
	{

		Date start = new Date();

		this.sp = sp;
		theMatrix = weights;
		rowApp = aDistricts;
		colApp = aParties;

		divisorUpdateMethod = div_update;

		rows = aDistricts.length;
		cols = aParties.length;

		// erste Prüfung: Summen der Distrikt- und Parteiensitze müssen gleich sein
		int atmp = 0;
		for (int i = 0; i < rows; i++)
		{
			atmp += aDistricts[i];
		}
		for (int j = 0; j < cols; j++)
		{
			atmp -= aParties[j];
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

		// int lpos = 0;
		// for (int j = 0; j < cols; j++) {
		// if (aParties[j] > 0) {
		// lpos++;
		// }
		// }

		// Speicherung der Fehlstände für Debug-Ausgabe am Schluß
		StringBuffer sFlaws = new StringBuffer("");
		int lastFlaw = Integer.MAX_VALUE;
		int rzahl = 0;
		String debtmp;
		String divtmp, divmintmp, divnicetmp, divmaxtmp;
		String sptr, sptc;

		// boolean bm;

		boolean usedLR = false;

		Weight[][] aktm = new Weight[rows][cols];
		Weight[][] sve = new Weight[rows][cols];
		// Weight[][] lr = new Weight[rows][cols];
		// Weight[][] lc = new Weight[rows][cols];
		int[][] support = new int[rows][cols];

		// akkumulierte Divisoren (a_i, b_j)
		double[] dRowDivs = new double[rows];
		double[] dColDivs = new double[cols];
		// aktuelle Divisoren (C_i, D_j) mit Grenzen
		aktRowDivs = new double[rows];
		aktRowDivMin = new double[rows];
		aktRowDivMax = new double[rows];
		aktColDivs = new double[cols];
		aktColDivMin = new double[cols];
		aktColDivMax = new double[cols];
		// aktuelle Multiplikatoren (rho_i, gamma_j) mit Grenzen
		aktRowMuls = new double[rows];
		aktRowMulMin = new double[rows];
		aktRowMulMax = new double[rows];
		aktColMuls = new double[cols];
		aktColMulMin = new double[cols];
		aktColMulMax = new double[cols];

		// Flaw count
		int[] frow = new int[rows];
		int[] fcol = new int[cols];

		// Last plus-tie
		int[] ptr = new int[rows];
		int[] ptc = new int[cols];

		// Zeigt an, ob auf LR gewechselt wurde
		// boolean blr = false;
		// Anzahl Fehlstände, bei denen auf LR gewechselt wurde
		int lrSwitch = -1;

		Divisor[] divObjs;
		LibMessenger messenger = new LibMessenger();
		// Method method = new Method(null, new Divisor(), messenger);

		int[] rowsum = new int[rows];
		int[] colsum = new int[cols];
		for (int i = 0; i < rows; i++)
		{
			rowsum[i] = 0;
		}
		for (int j = 0; j < cols; j++)
		{
			colsum[j] = 0;
		}
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				if ((sp.s(0) == 0) && (weights[i][j].weight > 0))
				{
					support[i][j] = 1;
					rowsum[i]++;
					colsum[j]++;
				}
				else
				{
					support[i][j] = 0;
				}
			}
		}

		boolean b = true;
		for (int i = 0; i < rows; i++)
		{
			b &= !(aDistricts[i] < rowsum[i]);
			// throw new DPPException(DPPException.EXISTENCE, Resource.getString("bazi.gui.biprop.na"));
		}
		for (int j = 0; j < cols; j++)
		{
			b &= !(aParties[j] < colsum[j]);
			// throw new DPPException(DPPException.EXISTENCE, Resource.getString("bazi.gui.biprop.na"));
		}

		if (!b)
		{
			// ret.sError = Resource.getString("bazi.gui.biprop.na");
			// ret.sError += " " + Resource.getString("bazi.gui.biprop.nonexs0");
			ret.sError = "NA = Not available: Prespecified marginals are toosmall to serve all positive weights.";
			bmes.setError(BipropLibMessenger.EXISTENCE, ret.sError, null);
			// System.out.println(ret.sError);
			// aktm = new Weight[rows][cols];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					aktm[i][j] = new Weight();
					aktm[i][j].rdWeight = -1;
					aktColDivMin[j] = aktColDivMax[j] = aktColDivs[j] = -1;
					aktColMulMin[j] = aktColMulMax[j] = aktColMuls[j] = -1;
				}
				aktRowDivMin[i] = aktRowDivMax[i] = aktRowDivs[i] = -1;
				aktRowMulMin[i] = aktRowMulMax[i] = aktRowMuls[i] = -1;
			}
			ret.app = aktm;
			ret.divRowMin = aktRowDivMin;
			ret.divRowMax = aktRowDivMax;
			ret.divRowNice = aktRowDivs;
			ret.divColMin = aktColDivMin;
			ret.divColMax = aktColDivMax;
			ret.divColNice = aktColDivs;
			ret.mulRowMin = aktRowMulMin;
			ret.mulRowMax = aktRowMulMax;
			ret.mulRowNice = aktRowMuls;
			ret.mulColMin = aktColMulMin;
			ret.mulColMax = aktColMulMax;
			ret.mulColNice = aktColMuls;
			// return false;
			return ret;
		}

		// MaxFlow
		ret.nw = null;
		if (sp.s(0) > 0)
		{
			ret.nw = new Network(weights, aDistricts, aParties, true);
		}
		else
		{
			ret.nw = new Network(weights, aDistricts, aParties, false);
		}

		/* System.out.println("nw.ex: " + nwb);
		 * if (!nwb) {
		 * System.out.println("\tS_D: " + nw.getDistrictTotal() + " DI: " +
		 * printArray(nw.getDistrictIndex()));
		 * System.out.println("\tS_P: " + nw.getPartyTotal() + " PI: " +
		 * printArray(nw.getPartyIndex()));
		 * } */
		// System.out.println(nw);
		// String st = graf.existSolution(weights);
		// ///////////////// Neu eingefügt

		// if (st != null) {
		if (!ret.nw.existSolution())
		{
			// il.iterationFinished();
			// throw new DPPException(DPPException.EXISTENCE, st);
			// Return-Daten auf NA setzen
			// ret.sError = Resource.getString("bazi.gui.biprop.na") + " " + st;
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					aktm[i][j] = new Weight();
					aktm[i][j].rdWeight = -1;
					aktColDivMin[j] = aktColDivMax[j] = aktColDivs[j] = -1;
					aktColMulMin[j] = aktColMulMax[j] = aktColMuls[j] = -1;
				}
				aktRowDivMin[i] = aktRowDivMax[i] = aktRowDivs[i] = -1;
				aktRowMulMin[i] = aktRowMulMax[i] = aktRowMuls[i] = -1;
			}
			ret.app = aktm;
			ret.divRowMin = aktRowDivMin;
			ret.divRowMax = aktRowDivMax;
			ret.divRowNice = aktRowDivs;
			ret.divColMin = aktColDivMin;
			ret.divColMax = aktColDivMax;
			ret.divColNice = aktColDivs;
			ret.mulRowMin = aktRowMulMin;
			ret.mulRowMax = aktRowMulMax;
			ret.mulRowNice = aktRowMuls;
			ret.mulColMin = aktColMulMin;
			ret.mulColMax = aktColMulMax;
			ret.mulColNice = aktColMuls;
			return ret;
		}

		// Zeilendivisoren werden mit der Mandatszahl des zugehörigen Distrikts initialisiert
		// 15.12.2003: jetzt wieder mit 1
		for (int i = 0; i < rows; i++)
		{
			dRowDivs[i] = 1;
			// dRowDivs[i] = i+1; // 17.03.2005 FK andere Initialisierung wg. Gaffkes Killerbeispiel
			// dRowDivs[i] = rows-i; // 18.03.2005 FK andere Initialisierung wg. Gaffkes Killerbeispiel
			// frow[i] = 1;
			frow[i] = 0;
		}
		// Spaltendivisoren mit 1
		for (int j = 0; j < cols; j++)
		{
			dColDivs[j] = 1;
			// dColDivs[j] = j+1; // 17.03.2005 FK andere Initialisierung wg. Gaffkes Killerbeispiel
			// dColDivs[j] = cols-j; // 18.03.2005 FK andere Initialisierung wg. Gaffkes Killerbeispiel
			fcol[j] = 1;
		}

		// für die Abbruchbedingung
		int fcount = 1;

		// Jetzt kommt die eigentliche Zuteilung
		while (fcount > 0)
		{
			// Benutzerabbruch?
			if (Thread.interrupted())
			{
				userInterrupted(ret, sFlaws, rzahl);
				bmes.setError(BipropLibMessenger.USER_ERROR,
						"User interrupted calculation @iteration " +
								(ret.rowit + ret.colit),
						new String[] { String.valueOf(ret.rowit + ret.colit) });
				throw new BipropException("User interrupted calculation @iteration " +
						(ret.rowit + ret.colit));
			}

			/************************************************************/
			// Zeilenschritt
			// Zuerst die Matrix erstellen
			int steps = ret.colit + ret.rowit;
			if (steps > 0)
			{
				sve = aktm;
			}
			if (steps > 0)
			{
				// lc = aktm;
			}
			if (maxIterations > 0)
			{
				if (steps > maxIterations)
				{
					throw new IterationExceededException("Limit of iterations exceeded: " +
							steps);
				}
			}
			aktm = new Weight[rows][cols];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					aktm[i][j] = weights[i][j].clonew();
					// falls ein Divisor unendlich ist, sollte dieses Gewicht auf 0 gesetzt werden!
					if (Double.isInfinite(dRowDivs[i]) || Double.isInfinite(dColDivs[j]))
					{
						aktm[i][j].weight = 0.0d;
					}
					else
					{
						aktm[i][j].weight /= (dRowDivs[i] * dColDivs[j]);
					}
					if (steps > 0)
					{
						// speichern der gerundeten Gewichte von der letzten Iteration für die Existenzprüfung
						aktm[i][j].rdWeight = sve[i][j].rdWeight;
					}
				}
			}

			// Existenzprüfung
			// benötigt die erstellte Gewichtematrix
			// if ((steps == 50*Math.round(steps/50.0)) && (steps>0)) {
			if (((steps % 50) == 0) && (steps > 0))
			{
				fireIterationChanged(steps, false);
				// alte Existenzprüfung
				// String st = testFlawMinimum(aktm, aDistricts, aParties);

				// /////////// NEU EINGEFÜGT
				// Existenzprüfung nur noch beim 100. Schritt
				// if (steps == 100) {
				// }
			}

			// Step = Step + 1
			ret.rowit++;

			// DEBUG
			if (Debug.BIPROP)
			{
				debtmp = "\nIteration: " + (ret.colit + ret.rowit);
				debtmp += "\nStartmatrix\n";
				debtmp += printMatrixW(aktm);
				notifyMethodListeners(debtmp);
			}
			divObjs = new Divisor[rows];
			// Berechnung
			// Zuerst die Zuteilung
			divtmp = "";
			divmintmp = "";
			divnicetmp = "";
			divmaxtmp = "";
			sptr = "";
			for (int i = 0; i < rows; i++)
			{
				divObjs[i] = new Divisor();
				Weight[] aktrow = new Weight[cols];
				for (int j = 0; j < cols; j++)
				{
					aktrow[j] = aktm[i][j];
				}
				// try {
				// bm = method.divMethVector(sp, divObjs[i], aDistricts[i], aktrow,
				// messenger);
				/* bm = */Method.statPowMethod(sp, divObjs[i], aDistricts[i], aktrow, messenger, "");
				// }
				// catch (MethodException e) {
				// throw new DPPException(me, "@rowstep " + (ret.rowit) + " row: " + i);
				// bmes.setError(BipropMessenger.METHOD,
				// messenger.getErrorMessage() + " @rowstep " + (ret.rowit) +
				// " row: " + i);
				// return false;
				// bmes.setError(BipropLibMessenger.METHOD,
				// "@rowstep " + (ret.rowit) + " row: " + i,
				// new String[] {String.valueOf(ret.rowit), String.valueOf(i)});
				// throw new BipropException("@rowstep " + (ret.rowit) + " row: " + i, e);
				// }
				// Update der Divisoren
				aktRowDivs[i] = divObjs[i].getDivisor();
				aktRowDivMin[i] = divObjs[i].getDivisorLow();
				aktRowDivMax[i] = divObjs[i].getDivisorHigh();
				// IF C_i=1 AND Frow_i>0 THEN C_i=2 ENDIF
				// IF C_i=1 AND Frow_i<0 THEN C_i=1/2 ENDIF
				// if ((aktRowDivs[i] == 1) && (frow[i] > 0)) {
				/* if ((Math.round((aktRowDivs[i] - 1)*Math.pow(10, 15)) == 0) && (frow[i] > 0)) {
				 * aktRowDivs[i] = 2;
				 * }
				 * if ((Math.round((aktRowDivs[i] - 1)*Math.pow(10, 15)) == 0) && (frow[i] < 0)) {
				 * aktRowDivs[i] = 0.5;
				 * } */
				// Divisorupdate wird aus der Schleife ausgelagert und für alle Zeilen auf einmal gemacht
				// dRowDivs[i] *= aktRowDivs[i];
				divmintmp += "Cmin" + i + ": " + aktRowDivMin[i] + "; ";
				divnicetmp += "C" + i + ": " + aktRowDivs[i] + "; ";
				divmaxtmp += "Cmax" + i + ": " + aktRowDivMax[i] + "; ";
				// plus-ties
				ptr[i] = divObjs[i].getPt();
				sptr += "ptr" + i + ": " + ptr[i] + "; ";
			}

			// Update der Divisoren
			updateDivisors(dRowDivs, divObjs, frow);

			// DEBUG
			if (Debug.BIPROP)
			{
				debtmp = "Zuteilung:\n";
				debtmp += printMatrixR(aktm);
				notifyMethodListeners(debtmp);
			}
			debtmp = "";

			if (Debug.BIPROP)
			{
				divtmp = divmintmp + "\n" + divnicetmp + "\n" + divmaxtmp + "\n" + sptr;
				divtmp += "\nKumuliert: ";
				for (int i = 0; i < rows; i++)
				{
					divtmp += ("a" + i + ": " + dRowDivs[i] + "; ");
				}

				notifyMethodListeners(divtmp);
			}

			// Jetzt noch die Fehlstände prüfen
			debtmp = "";
			for (int j = 0; j < cols; j++)
			{
				fcol[j] = 0;
				for (int i = 0; i < rows; i++)
				{
					fcol[j] += aktm[i][j].rdWeight;
				}
				fcol[j] -= aParties[j];
				debtmp += "fc" + j + ": " + fcol[j] + "; ";
			}
			if (Debug.BIPROP)
			{
				notifyMethodListeners(debtmp);
			}
			// Bei entarteten Zeilen kann ein Transfer zwischen Spalten die Fehlstände korrigieren
			// -> "In tied rows..."
			for (int i = 0; i < rows; i++)
			{
				// if (aktRowDivMin[i] == aktRowDivMax[i]) {
				if (ptr[i] > -1)
				{
					for (int j1 = 0; j1 < cols; j1++)
					{
						for (int j2 = 0; j2 < cols; j2++)
						{
							if ((aktm[i][j1].multiple.equals("+")) &&
									(aktm[i][j2].multiple.equals("-")) && (fcol[j1] < 0) &&
									(fcol[j2] > 0))
							{
								if (Debug.BIPROP)
								{
									notifyMethodListeners("i: " + i + " j1: " + j1 + " j2: " + j2);
									// Transfer
								}
								aktm[i][j1].rdWeight += 1;
								aktm[i][j2].rdWeight -= 1;
								// Update
								fcol[j1] += 1;
								fcol[j2] -= 1;
								// Reflag
								aktm[i][j1].multiple = "-";
								aktm[i][j2].multiple = "+";
							}
						}
					}
				}
			}

			// jetzt noch fcount neu berechnen
			fcount = 0;
			for (int j = 0; j < cols; j++)
			{
				fcount += Math.max(0, fcol[j]);
			}
			lastFlaw = fcount;

			// DEBUG
			if (Debug.BIPROP)
			{
				// sFlaws += fcount + "; ";
				sFlaws.append(fcount + "; ");
				if (lastFlaw < fcount)
				{
					rzahl++;
				}
				debtmp = "-- Zuteilung nach Korrektur --\n";
				debtmp += printMatrixR(aktm);
				debtmp += "\nFehlstände: " + fcount;
				notifyMethodListeners(debtmp);
			}
			// ENDE Rowstep


			// Falls noch Fehlstände vorhanden sind, einen Spaltenschritt
			if (fcount > 0)
			{
				/************************************************************/
				// Spaltenschritt
				// Teile Parteiweise zu

				// Zuerst die Matrix erstellen
				// lr = aktm;
				sve = aktm;
				aktm = new Weight[rows][cols];
				for (int i = 0; i < rows; i++)
				{
					for (int j = 0; j < cols; j++)
					{
						aktm[i][j] = weights[i][j].clonew();
						// Falls ein Divisor unendlich ist, sollte dieses Gewicht auf 0 gesetzt werden!
						if (Double.isInfinite(dRowDivs[i]) || Double.isInfinite(dColDivs[j]))
						{
							aktm[i][j].weight = 0.0d;
						}
						else
						{
							aktm[i][j].weight /= (dRowDivs[i] * dColDivs[j]);
						}
						aktm[i][j].rdWeight = sve[i][j].rdWeight;
					}
				}

				// Step = Step + 1
				ret.colit++;

				steps = ret.colit + ret.rowit;
				if (maxIterations > 0)
				{
					if (steps > maxIterations)
					{
						throw new IterationExceededException(
								"Limit of iterations exceeded: " + steps);
					}
				}

				// DEBUG
				if (Debug.BIPROP)
				{
					debtmp = "\nIteration: " + (ret.colit + ret.rowit) + "\n";
					debtmp += "Startmatrix:\n";
					debtmp += printMatrixW(aktm);
					notifyMethodListeners(debtmp);
				}
				divObjs = new Divisor[cols];

				// Berechnung
				// Zuerst die Zuteilung
				divtmp = "";
				divmintmp = "";
				divnicetmp = "";
				divmaxtmp = "";
				sptc = "";
				for (int j = 0; j < cols; j++)
				{
					divObjs[j] = new Divisor();
					Weight[] aktcol = new Weight[rows];
					for (int i = 0; i < rows; i++)
					{
						aktcol[i] = aktm[i][j];
					}
					// try {
					// alte Methode bm = method.divMethVector(sp, divObjs[j], aParties[j], aktcol,
					// messenger);

					Method.statPowMethod(sp, divObjs[j], aParties[j], aktcol, messenger, "");
					// }
					// catch (MethodException e) {
					// throw new DPPException(me, "@colstep " + (ret.colit + 1) + " col " + j);
					// throw new DPPException("@colstep " + (countColIters+1) + " col " + k, DPPException.METHFAULT, 1);
					// bmes.setError(BipropMessenger.METHOD,
					// messenger.getErrorMessage() + " @colstep " +
					// (ret.colit + 1) + " col " + j);
					// return false;
					// bmes.setError(BipropLibMessenger.METHOD,
					// "@colstep " + (ret.colit + 1) + " col " + j,
					// new String[] {String.valueOf(ret.rowit + 1), String.valueOf(j)});
					// throw new BipropException("@colstep " + (ret.colit + 1) + " col " +
					// j, e);
					// }
					// Update der Divisoren
					aktColDivs[j] = divObjs[j].getDivisor();
					aktColDivMin[j] = divObjs[j].getDivisorLow();
					aktColDivMax[j] = divObjs[j].getDivisorHigh();
					/* if ((Math.round((aktColDivs[j] - 1)*Math.pow(10, 15)) == 0) && (fcol[j] > 0)) {
					 * aktColDivs[j] = 2;
					 * }
					 * if ((Math.round((aktColDivs[j] - 1)*Math.pow(10, 15)) == 0) && (fcol[j] < 0)) {
					 * aktColDivs[j] = 0.5;
					 * } */
					// Divisorupdate wird aus der Schleife ausgelagert und für alle Zeilen auf einmal gemacht
					// dColDivs[j] *= aktColDivs[j];
					divmintmp += "Dmin" + j + ": " + aktColDivMin[j] + "; ";
					divnicetmp += "D" + j + ": " + aktColDivs[j] + "; ";
					divmaxtmp += "Dmax" + j + ": " + aktColDivMax[j] + "; ";
					// plus-ties
					ptc[j] = divObjs[j].getPt();
					sptc += "ptc" + j + ": " + ptc[j] + "; ";
				}

				// Divisorupdate
				updateDivisors(dColDivs, divObjs, fcol);

				// DEBUG
				if (Debug.BIPROP)
				{
					debtmp = "Zuteilung:\n";
					debtmp += printMatrixR(aktm);
					notifyMethodListeners(debtmp);
				}
				if (Debug.BIPROP)
				{
					divtmp = divmintmp + "\n" + divnicetmp + "\n" + divmaxtmp + "\n" + sptc;
					divtmp += "\nKumuliert: ";
					for (int j = 0; j < cols; j++)
					{
						divtmp += "b" + j + ": " + dColDivs[j] + "; ";
					}
					notifyMethodListeners(divtmp);
				}

				// Jetzt noch die Fehlstände prüfen
				debtmp = "";
				for (int i = 0; i < rows; i++)
				{
					frow[i] = 0;
					for (int j = 0; j < cols; j++)
					{
						frow[i] += aktm[i][j].rdWeight;
					}
					frow[i] -= aDistricts[i];
					debtmp += "fr" + i + ": " + frow[i] + "; ";
				}
				if (Debug.BIPROP)
				{
					notifyMethodListeners(debtmp);
				}

				// Bei entarteten Spalten kann ein Transfer zwischen Zeilen die Fehlstände korrigieren

				// -> "In tied cols..."
				for (int j = 0; j < cols; j++)
				{
					// if ((aktColDivMin[j] == aktColDivMax[j]) && (aParties[j] != 0)) {
					if (ptc[j] > -1)
					{
						for (int i1 = 0; i1 < rows; i1++)
						{
							for (int i2 = 0; i2 < rows; i2++)
							{
								if ((aktm[i1][j].multiple.equals("+")) &&
										(aktm[i2][j].multiple.equals("-")) && (frow[i1] < 0) &&
										(frow[i2] > 0))
								{
									if (Debug.BIPROP)
									{
										notifyMethodListeners("i1: " + i1 + " i2: " + i2 + " j: " +
												j);
										// Transfer
									}
									aktm[i1][j].rdWeight += 1;
									aktm[i2][j].rdWeight -= 1;
									// Update
									frow[i1] += 1;
									frow[i2] -= 1;
									// Reflag
									aktm[i1][j].multiple = "-";
									aktm[i2][j].multiple = "+";
								}
							}
						}
					}
				}


				// jetzt noch fcount neu berechnen
				fcount = 0;
				for (int i = 0; i < rows; i++)
				{
					fcount += Math.max(0, frow[i]);
				}
				// DEBUG
				if (Debug.BIPROP)
				{
					// sFlaws += fcount + "; ";
					sFlaws.append(fcount + "; ");
					if (lastFlaw < fcount)
					{
						rzahl++;
					}

					debtmp = "-- Zuteilung nach Korrektur --\n";
					debtmp += printMatrixR(aktm);
					debtmp += "Fehlstände: " + fcount;
					notifyMethodListeners(debtmp);
				}

				if (useTT && (lastFlaw == fcount))
				{
					usedLR = true;
					// Umschalten auf HybridLR
					// System.out.println("Start LR...");
					Weight[][] paramMatrix = new Weight[rows][cols]; // Originalgewichte und letzte Zuteilung
					// added by S.M.
					// FAK050818
					for (int i = 0; i < rows; i++)
					{
						for (int j = 0; j < cols; j++)
						{
							// Falls ein Divisor unendlich ist, sollte dieses Gewicht auf 0 gesetzt werden!
							/* if (Double.isInfinite(dRowDivs[i]) ||
							 * Double.isInfinite(dColDivs[j])) {
							 * aktm[i][j].weight = 0.0d;
							 * }
							 * else {
							 * aktm[i][j].weight /= aktColDivs[j];
							 * } */
							paramMatrix[i][j] = weights[i][j].clonew();
							paramMatrix[i][j].rdWeight = aktm[i][j].rdWeight;
						}
					}

					HybridTT hlr = new HybridTT(paramMatrix, aDistricts, aParties, dRowDivs,
							dColDivs, sp);
					for (Enumeration<MethodListener> e = methodListeners.elements(); e.hasMoreElements();)
					{
						hlr.addMethodListener(e.nextElement());
					}

					BipropRet tmpret;
					try
					{
						tmpret = hlr.start();
					}
					catch (BipropException e)
					{
						bmes.setError(BipropLibMessenger.COMMON, e.getMessage(), null);
						throw e;
					}

					if (tmpret != null)
					{
						if (tmpret.sError == null)
						{
							aktm = tmpret.app;
							dRowDivs = tmpret.divRowNice;
							dColDivs = tmpret.divColNice;
							ret.transfers = tmpret.transfers;
							ret.updates = tmpret.updates;
							// blr = true;
							// lrSteps = tmpret.lrSteps;
							lrSwitch = lastFlaw;
							fcount = 0;
							if (Debug.BIPROP)
							{
								debtmp = "\n-- Zuteilung nach HybridLR --\n";
								debtmp += printMatrixR(aktm);
							}
							// update ptr/ptc
							for (int i = 0; i < rows; i++)
							{
								int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
								for (int j = 0; j < cols; j++)
								{
									int flag = aktm[i][j].getFlag();
									if (flag < min)
										min = flag;
									if (flag > max)
										max = flag;
								}
								if ((min == -1) && (max == 1))
								{
									for (int j = 0; j < cols; j++)
									{
										if (aktm[i][j].getFlag() == 1)
											ptr[i] = j;
									}
								}
								else
								{
									for (int j = 0; j < cols; j++)
									{
										aktm[i][j].multiple = "";
									}
									ptr[i] = 0;
								}
							}
							for (int j = 0; j < cols; j++)
							{
								int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
								for (int i = 0; i < rows; i++)
								{
									int flag = aktm[i][j].getFlag();
									if (flag < min)
										min = flag;
									if (flag > max)
										max = flag;
								}
								if ((min == -1) && (max == 1))
								{
									for (int i = 0; i < rows; i++)
									{
										if (aktm[i][j].getFlag() == 0)
											ptc[j] = i;
									}
								}
								else
								{
									for (int i = 0; i < rows; i++)
									{
										aktm[i][j].multiple = "";
									}
									ptc[j] = 0;
								}
							}

						}
						else
						{
							userInterrupted(ret, sFlaws, rzahl);
							bmes.setError(BipropLibMessenger.USER_ERROR,
									"User interrupted calculation @iteration " +
											(ret.rowit + ret.colit) + " during HybridLR calculation",
									new String[] { String.valueOf(ret.rowit + ret.colit) });
							throw new BipropException(
									"User interrupted calculation @iteration " +
											(ret.rowit + ret.colit) + " during HybridLR calculation");
						}
					}
				}
				lastFlaw = fcount;
				if (Debug.BIPROP)
				{
					notifyMethodListeners(debtmp);
				}

				// ENDE Colstep

			}
		}

		int steps = ret.colit + ret.rowit;
		fireIterationChanged(steps, true);

		if (Debug.BIPROP)
		{
			debtmp = "ptr nach Iteration:\n";
			for (int i = 0; i < rows; i++)
			{
				debtmp += ptr[i] + " ";
			}
			debtmp += "\nptc nach Iteration:\n";
			for (int j = 0; j < cols; j++)
			{
				debtmp += ptc[j] + " ";
			}
			notifyMethodListeners(debtmp);
		}

		// /////////////////////////////////////////////////////////////////////////
		// Iteration beendet
		// /////////////////////////////////////////////////////////////////////////

		/* aktm ist die Endmatrix und wird am Schluß zurückgegeben
		 * akt{Row|Col}Div{Min|Max} enthalten die jeweiligen Divisoren von der letzen
		 * Zuteilung. Das sind die C{min|max}_i bzw. D{min|max}_j aus dem Pseudocode
		 * d{Row|Col}Divs enthalten die akkumulierten Divisoren. Dies sind
		 * die a_i bzw. b_j aus dem Pseudocode */


		// ### Finalize divisor limits in penultimate step of while-loop
		// # If loop ends on columns...
		// Divisor d = new Divisor();
		debtmp = "\n\nDivisorkorrektur: ";
		/* Achtung: Im Pseudocode wird nur die Gesamtzahl der Iterationen gezählt,
		 * die "richtige" Abfrage müßte also lauten:
		 * if ((step%2)==0) {...} */
		// if (ret.colit==ret.rowit) {
		// letzter schritt war ein Spaltenschritt
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
		// }
		// else {
		// letzter schritt war ein Zeilenschritt
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
		// }

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

		if (Debug.BIPROP)
		{
			debtmp = "-- Zuteilung nach Fahnenentfernung --\n";
			debtmp += printMatrixR(aktm);

			debtmp += "ptr: ";
			for (int i = 0; i < rows; i++)
			{
				debtmp += ptr[i] + " ";
			}
			debtmp += "\nptc: ";
			for (int j = 0; j < cols; j++)
			{
				debtmp += ptc[j] + " ";
			}
			notifyMethodListeners(debtmp);
		}

		// Falls TT benutzt wurde, müssen die Divisoren noch korrigiert werden
		if (usedLR)
		{
			try
			{
				reCalculateDivisors(aktm, dRowDivs, dColDivs);

				/* System.out.println("\n\n---\n");
				 * for(int i =0; i<rows; i++)
				 * System.out.println(dRowDivs[i]);
				 * for(int j =0; j< cols; j++)
				 * System.out.println(dColDivs[j]);
				 * System.out.println("\n\n---\n"); */
			}
			catch (BipropException e)
			{
				bmes.setError(BipropLibMessenger.DIVISOR, e.getMessage(), null);
				throw e;
			}
		}

		// ### Find median column divisor, standardize.
		debtmp = "Mediansuche...\n";
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
			if (dColDivs[j] > 0)
			{
				cjCount++;
			}
		}

		debtmp += "maxf: " + maxf + " maxptc: " + maxptc + "\n";

		if (maxf == 1)
		{
			ret.ties = true;
			// # either flagged columns
			debtmp += "flagged columns...";
			double[] tdc = new double[maxfiCount];
			int offset = 0; // should always be <= maxfiCount!
			for (int j = 0; j < cols; j++)
			{
				if (maxfi[j] == 1)
				{
					tdc[offset++] = dColDivs[j];
				}
			}
			// now should be offset==maxfiCount
			int[] sortIndex = Sort.sort(tdc);
			med = tdc[sortIndex[(int) Math.ceil((float) maxfiCount / 2) - 1]];
		}
		else if (maxptc > 0)
		{
			// # or tied columns
			debtmp += "tied columns...";
			double[] tdc = new double[ptcjCount];
			int offset = 0; // should always be <= ptcjCount!
			for (int j = 0; j < cols; j++)
			{
				if (ptc[j] > 0)
				{
					tdc[offset++] = dColDivs[j];
				}
			}
			// now should be offset==ptcjCount
			int[] sortIndex = Sort.sort(tdc);
			med = tdc[sortIndex[(int) Math.ceil((float) ptcjCount / 2) - 1]];
		}
		else
		{
			// # or columns with positive marginals
			debtmp += "positive marginals...";
			double[] tdc = new double[cjCount];
			int offset = 0; // should always be <= cjCount!
			for (int j = 0; j < cols; j++)
			{
				if (dColDivs[j] > 0)
				{
					tdc[offset++] = dColDivs[j];
					// debtmp += tdc[offset-1] + " ";
				}
			}
			// now should be offset==cjCount
			int[] sortIndex = Sort.sort(tdc);
			med = tdc[sortIndex[(int) Math.ceil((float) cjCount / 2) - 1]];
		}
		debtmp += "Median: " + med + "\n";

		// # Standardize column divisors D_j:
		divObjs = new Divisor[cols];
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
			// debtmp += "Spalte " + j + " maxf: " + maxf + "\n";
			if ((maxf == 1) && (dColDivs[j] == med))
			{
				// # Degenerate limits
				aktColMulMin[j] = aktColMulMax[j] = aktColMuls[j] = 1;
				aktColDivMin[j] = aktColDivMax[j] = aktColDivs[j] = 1;
				// System.out.println("Median gefunden in Spalte " + j);
			}
			else
			{
				// # else pick nice numbers
				// Call to DM!
				divObjs[j] = new Divisor();
				Weight[] aktcol = new Weight[rows];
				for (int i = 0; i < rows; i++)
				{
					aktcol[i] = weights[i][j].clonew();
					aktcol[i].weight /= dRowDivs[i] * med;
					// debtmp += "w_" + i + ": " + aktcol[i].weight + " ";
				}
				// debtmp += "\n";
				// try {
				// alte Methode: bm = method.divMethVector(sp, divObjs[j], aParties[j], aktcol,
				// messenger);

				Method.statPowMethod(sp, divObjs[j], aParties[j], aktcol, messenger, "");
				// }
				// catch (MethodException e) {
				// if (Debug.BIPROP) {
				// notifyMethodListeners(debtmp);
				// }
				// System.out.println(messenger.getErrorMessage());
				// throw new DPPException(me, "@final column " + j);
				// bmes.setError(BipropMessenger.METHOD,
				// messenger.getErrorMessage() + " @final column " + j);
				// return false;
				// bmes.setError(BipropLibMessenger.METHOD,
				// "@final column " + j,
				// new String[] {String.valueOf(j)});
				// throw new BipropException("@final column " + j, e);
				// }
				// Update der Divisoren
				aktColDivs[j] = divObjs[j].getDivisor();
				aktColDivMin[j] = divObjs[j].getDivisorLow();
				aktColDivMax[j] = divObjs[j].getDivisorHigh();
				aktColMuls[j] = divObjs[j].getMultiplier();
				aktColMulMin[j] = divObjs[j].getMultiplierLow();
				aktColMulMax[j] = divObjs[j].getMultiplierHigh();
				// plus-ties
				// ptc[j] = divObjs[j].getPt();
			}
		}

		// # Adjust row divisors C_i
		divObjs = new Divisor[rows];
		for (int i = 0; i < rows; i++)
		{
			maxf = -1; // max_j f_{ij}
			// int maxfdj = -1; // max{j: f_{ij}*D_j=1}, tie on col with median divisor
			// double maxfdjnum = 0; // max_j f_{ij}*D_j
			// int maxfj = -1; // max{j: f_{ij}=1}
			// double fxd = Double.NEGATIVE_INFINITY;
			for (int j = 0; j < cols; j++)
			{
				int fij = aktm[i][j].getFlag();
				if (maxf < fij)
				{
					maxf = fij;
					// double p = fij * aktColDivs[j];
					// System.out.print("p" + j + ": " + p + "; ");
					// if (p == 1) maxfdj = j;
					// if (maxfdjnum < p) maxfdjnum = p;
					// if (fij == 1) maxfj = j;
				}
			}
			// System.out.println("\nmaxfdjnum: " + maxfdjnum + " maxfdj: " + maxfdj);

			int lt = -1;
			// if (maxf == 1) { // # If row i is flagged//
			/* //if (maxfdjnum == 1) { // # if + flags meet unity divisor...
			 * if (maxfdj > -1) {
			 * //System.out.println("tie on col with median divisor");
			 * ptr[i] = maxfdj;
			 * }
			 * else { // # else use last plus-tie in row i
			 * //System.out.println("tie on common col");
			 * ptr[i] = maxfj;
			 * }
			 * aktRowMulMin[i] = aktRowMulMax[i] = aktRowMuls[i] =
			 * sp.s(aktm[i][ptr[i]].rdWeight) * aktColDivs[ptr[i]] / weights[i][ptr[i]].weight;
			 * aktRowDivMin[i] = aktRowDivMax[i] = aktRowDivs[i] =
			 * weights[i][ptr[i]].weight / (sp.s(aktm[i][ptr[i]].rdWeight) * aktColDivs[ptr[i]]); */
			for (int j = 0; j < cols; j++)
			{
				if ((Math.abs(aktm[i][j].getFlag()) == 1) && (aktColDivs[j] == 1))
				{
					lt = j;
				}
			}
			if (lt > -1)
			{
				aktRowMulMin[i] = aktRowMulMax[i] = aktRowMuls[i] =
						sp.s(aktm[i][lt].rdWeight - (1 - aktm[i][lt].getFlag()) / 2) /
								weights[i][lt].weight;
				aktRowDivMin[i] = aktRowDivMax[i] = aktRowDivs[i] =
						weights[i][lt].weight /
								sp.s(aktm[i][lt].rdWeight - (1 - aktm[i][lt].getFlag()) / 2);
			}
			/* else {//
			 * lt = -1;
			 * // lt = max{j: f_{ij}=1}
			 * for (int j=0; j<cols; j++) {
			 * if (aktm[i][j].getFlag() == 1)
			 * lt = j;
			 * }
			 * aktRowMulMin[i] = aktRowMulMax[i] = aktRowMuls[i] =
			 * sp.s(aktm[i][lt].rdWeight) * aktColDivs[lt] / weights[i][lt].weight;
			 * aktRowDivMin[i] = aktRowDivMax[i] = aktRowDivs[i] =
			 * weights[i][lt].weight / (sp.s(aktm[i][lt].rdWeight) * aktColDivs[lt]);
			 * }
			 * //} */
			else
			{ // # Else row i is unflagged
				ptr[i] = -1;
				// Call to DM!
				divObjs[i] = new Divisor();
				Weight[] aktrow = new Weight[cols];
				for (int j = 0; j < cols; j++)
				{
					aktrow[j] = weights[i][j].clonew();
					aktrow[j].weight /= aktColDivs[j];
				}
				// try {
				// bm = method.divMethVector(sp, divObjs[i], aDistricts[i], aktrow,
				// messenger);

				Method.statPowMethod(sp, divObjs[i], aDistricts[i], aktrow, messenger, "");
				// }
				// catch (MethodException e) {
				// if (Debug.BIPROP) {
				// notifyMethodListeners(debtmp);
				// }
				// System.out.println(messenger.getErrorMessage());
				// throw new DPPException(me, "@final row: " + i);
				// bmes.setError(BipropMessenger.METHOD,
				// messenger.getErrorMessage() + " @final row: " + i);
				// return false;
				// bmes.setError(BipropLibMessenger.METHOD,
				// "@final row " + i,
				// new String[] {String.valueOf(i)} );
				// throw new BipropException("@final row " + i, e);
				// }
				// Update der Divisoren
				aktRowDivs[i] = divObjs[i].getDivisor();
				aktRowDivMin[i] = divObjs[i].getDivisorLow();
				aktRowDivMax[i] = divObjs[i].getDivisorHigh();
				aktRowMuls[i] = divObjs[i].getMultiplier();
				aktRowMulMin[i] = divObjs[i].getMultiplierLow();
				aktRowMulMax[i] = divObjs[i].getMultiplierHigh();
				// plus-ties
				// ptr[i] = divObjs[i].getPt();
			}
		}

		if (Debug.BIPROP)
		{
			sptr = "ptr: ";
			sptc = "ptc: ";
			debtmp += "Zeilendivisoren:\n";
			for (int i = 0; i < rows; i++)
			{
				debtmp += "C_" + i + ": " + aktRowDivs[i] + " ";
				sptr += ptr[i] + " ";
			}
			debtmp += "\nSpaltendivisoren:\n";
			for (int j = 0; j < cols; j++)
			{
				debtmp += "D_" + j + ": " + aktColDivs[j] + " ";
				sptc += ptc[j] + " ";
			}
			debtmp += "\n" + sptr + "\n" + sptc + "\n";

			notifyMethodListeners(debtmp);
		}

		// Fehlerprüfung
		// ...
		// falls nicht erfolgreich, sofortiger Abbruch mit Exception
		debtmp = "";
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
			if (sumR[i] != aDistricts[i])
			{
				// fireIterationFinished();
				// throw new DPPException(DPPException.APPORTIONMENT_DEFECTIVE, Resource.getString("bazi.xcp.biprop.district"));
				// bmes.setError(BipropMessenger.APPORTIONMENT_DEFECTIVE,
				// Resource.getString("bazi.xcp.biprop.district"));
				// return false;
				bmes.setError(BipropLibMessenger.DISTRICT_APPORTIONMENT_DEFECTIVE,
						// Resource.getString("bazi.xcp.biprop.district"),
						"District apportionment defective",
						null);
				// throw new BipropException(Resource.getString("bazi.xcp.biprop.district"));
				throw new BipropException("District apportionment defective");
			}
			// throw new DPPException(Resource.getString("bazi.xcp.biprop.district"),
			// DPPException.APPORTIONMENT, i);

		}

		for (int j = 0; j < cols; j++)
		{
			if (sumC[j] != aParties[j])
			{
				// fireIterationFinished();
				// throw new DPPException(DPPException.APPORTIONMENT_DEFECTIVE, Resource.getString("bazi.xcp.biprop.party"));
				// bmes.setError(BipropMessenger.APPORTIONMENT_DEFECTIVE,
				// Resource.getString("bazi.xcp.biprop.party"));
				// return false;
				bmes.setError(BipropLibMessenger.PARTY_APPORTIONMENT_DEFECTIVE,
						// Resource.getString("bazi.xcp.biprop.party") +
						"Party apportionment defective" +
								"\nj: " + j + " sum: " + sumC[j] +
								" aParties: " + aParties[j],
						new String[] { String.valueOf(j), String.valueOf(sumC[j]), String.valueOf(aParties[j]) });
				// throw new BipropException(Resource.getString("bazi.xcp.biprop.party") +
				throw new BipropException("Party apportionment defective" +
						"\nj: " + j + " sum: " + sumC[j] +
						" aParties: " + aParties[j]);
			}
			// throw new DPPException(Resource.getString("bazi.xcp.biprop.party"),
			// DPPException.APPORTIONMENT, j);
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
					// unflagged entries
					// should be <= 0
					s = sp.s(aktm[i][j].rdWeight);
					if (s > 0)
					{
						// maxUntied = Math.max(maxUntied, (weights[i][j].weight/(aktColDivs[j]*s))-aktRowDivs[i]);
						tmp = (weights[i][j].weight / s) - (aktRowDivs[i] * aktColDivs[j]);
						if ((tmp > maxUntied) && (tmp > 0))
						{
							debtmp += "maxUntied: i: " + i + " j: " + j + " tmp: " + tmp +
									"\n";
						}
						maxUntied = Math.max(maxUntied, tmp);
					}
					s = sp.s(aktm[i][j].rdWeight - 1);
					if (s > 0)
					{
						// minUntied = Math.min(minUntied, (weights[i][j].weight/(aktColDivs[j]*s))-aktRowDivs[i]);
						tmp = (weights[i][j].weight / s) - (aktRowDivs[i] * aktColDivs[j]);
						if ((tmp < minUntied) && (tmp < 0))
						{
							debtmp += "minUntied: i: " + i + " j: " + j + " tmp: " + tmp +
									"\n";
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
						// maxTied = Math.max(maxTied, Math.round(Math.abs((weights[i][j].weight/(aktRowDivs[i]*s))-aktColDivs[j])*Math.pow(10, 15)));
						// tmp = Math.round(((weights[i][j].weight/s)
						// -(weights[i][ptr[i]].weight * aktColDivs[j]
						// / (sp.s(aktm[i][ptr[i]].rdWeight) * aktColDivs[ptr[i]])))*Math.pow(10, 15));
						tmp = Math.round(((weights[i][j].weight /
								(s * aktRowDivs[i] * aktColDivs[j])) - 1) *
								Math.pow(10, 15) / 2);
						if ((tmp > maxTied) && (tmp > 0))
						{
							debtmp += "maxTied: i: " + i + " j: " + j + " tmp: " + tmp +
									" s: " + s
									+ " Ci: " + aktRowDivs[i] + " Dj: " + aktColDivs[j] +
									" wij: " + weights[i][j].weight + "\n";
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
						// minTied = Math.min(minTied, Math.round(Math.abs((weights[i][j].weight/(aktRowDivs[i]*s))-aktColDivs[j])*Math.pow(10, 15)));
						// tmp = Math.round(((weights[i][j].weight/s)
						// -(weights[i][ptr[i]].weight * aktColDivs[j]
						// / (sp.s(aktm[i][ptr[i]].rdWeight) * aktColDivs[ptr[i]])))*Math.pow(10, 15));
						tmp = Math.round(((weights[i][j].weight /
								(s * aktRowDivs[i] * aktColDivs[j])) - 1) *
								Math.pow(10, 15) / 2);
						if ((tmp < minTied) && (tmp < 0))
						{
							debtmp += "minTied: i: " + i + " j: " + j + " tmp: " + tmp +
									"\n";
						}
						minTied = Math.min(minTied, tmp);

					}
					break;
				}
				}

				/* s = sp.s(aktm[i][j].rdWeight);
				 * if (s>0) {
				 * if (weights[i][j].weight/(dRowDivs[i]*s)>dColDivs[j]) {
				 * //System.out.println("w["+i+"]["+j+"]: " + weights[i][j].weight + " dr: " + dRowDivs[i] + " s: " + s + "dc: " + dColDivs[j]);
				 * throw new DPPException(DPPException.DIVISOR_DEFECTIVE, "Fehler in Divisoren 1 @i: " + i);
				 * //throw new DPPException("Fehler in Divisoren 1", DPPException.DIVISOR, i);
				 * }
				 * }
				 * s = sp.s(aktm[i][j].rdWeight-1);
				 * if (s>0) {
				 * if (weights[i][j].weight/(dRowDivs[i]*s)<dColDivs[j]) {
				 * throw new DPPException(DPPException.DIVISOR_DEFECTIVE, "Fehler in Divisoren 2 @i: " + i);
				 * //throw new DPPException("Fehler in Divisoren 2", DPPException.DIVISOR, i);
				 * }
				 * } */
			}
		}
		/* if ((maxTied>0) || (minTied<0) || (maxUntied>0) || (minUntied<0)) {
		 * throw new DPPException(DPPException.DIVISOR_DEFECTIVE, "Fehler in #final check");
		 * } */
		if (Debug.BIPROP)
		{
			notifyMethodListeners(debtmp);
		}
		boolean bapp = false;
		String errs = "";
		if (maxTied > 0)
		{
			errs += "Fehler in #final check: maxTied>0: " + maxTied + "\n";
			bapp = true;
		}
		if (minTied < 0)
		{
			errs += "Fehler in #final check: minTied<0: " + minTied + "\n";
			bapp = true;
		}
		if (maxUntied > 0)
		{
			errs += "Fehler in #final check: maxUntied>0: " + maxUntied + "\n";
			bapp = true;
		}
		if (minUntied < 0)
		{
			errs += "Fehler in #final check: minUntied<0: " + minUntied + "\n";
			bapp = true;
		}

		if (Debug.BIPROP)
		{
			notifyMethodListeners("Entwicklung der Fehlstände:\n" + sFlaws);
			notifyMethodListeners("Anzahl der Rücksprünge: " + rzahl);
		}

		if (bapp)
		{
			// fireIterationFinished();
			// throw new DPPException(DPPException.DIVISOR_DEFECTIVE, errs);
			// bmes.setError(BipropMessenger.DIVISOR_DEFECTIVE, errs);
			bmes.setError(BipropLibMessenger.DIVISOR_DEFECTIVE, errs, null);
			throw new BipropException(errs);
		}

		ret.timeElapsed = new Date().getTime() - start.getTime();

		// return aktm;
		// BipropRet ret = new BipropRet();
		ret.app = aktm;
		ret.divRowMin = aktRowDivMin;
		ret.divRowMax = aktRowDivMax;
		ret.divRowNice = aktRowDivs;
		ret.divColMin = aktColDivMin;
		ret.divColMax = aktColDivMax;
		ret.divColNice = aktColDivs;
		ret.mulRowMin = aktRowMulMin;
		ret.mulRowMax = aktRowMulMax;
		ret.mulRowNice = aktRowMuls;
		ret.mulColMin = aktColMulMin;
		ret.mulColMax = aktColMulMax;
		ret.mulColNice = aktColMuls;
		ret.lrSwitch = lrSwitch;
		/* ret.rowit = countRowIters;
		 * ret.colit = countColIters; */
		if (Debug.BIPROP)
		{
			notifyMethodListeners("Beende divMethMatrix...\n");
		}
		return ret;
		// return true;
	}

	/** Aktualisiert die Divisoren in dDivs mit den Divisoren in divObjs:
	 * 
	 * dDivs *= divNew
	 * 
	 * Wobei divNew je nach ausgewählter Methode (midpoint, random, extreme) aus divObjs gewählt wird
	 * 
	 * @param dDivs Aktuelle Divisoren
	 * @param divObjs Divisoren der letzten Berechnung
	 * @param faults Die Fehlstände des vorherigen Schritts */
	private void updateDivisors(double[] dDivs, Divisor[] divObjs, int[] faults)
	{
		switch (divisorUpdateMethod)
		{
		case DIV_MDPT:
			for (int i = 0; i < dDivs.length; i++)
				dDivs[i] *= divObjs[i].getDivisor();
			break;
		case DIV_RAND:
			Random rd = new Random();
			for (int i = 0; i < dDivs.length; i++)
				dDivs[i] *= rd.nextDouble() *
						(divObjs[i].getDivisorHigh() - divObjs[i].getDivisorLow())
						+ divObjs[i].getDivisorLow();
			break;
		case DIV_EXTR:
			for (int i = 0; i < dDivs.length; i++)
			{
				if (faults[i] > 0)
					dDivs[i] *= divObjs[i].getDivisorHigh();
				else if (faults[i] < 0)
					dDivs[i] *= divObjs[i].getDivisorLow();
				else
					dDivs[i] *= divObjs[i].getDivisor();
			}
		}
	}

	/** Ausgabe der Gewichte-Matrix
	 * @param wm Die Matrix
	 * @return String-Repräsentation der Gewichtsmatrix */
	public static String printMatrixW(Weight[][] wm)
	{
		int rows = wm.length;
		int cols = wm[0].length;
		String tmp = "";
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				tmp += (wm[i][j].weight + "; ");
			}
			tmp += "\n";
		}
		return tmp;
	}

	/** Ausgabe der Zuteilungs-Matrix
	 * @param wm Die Matrix
	 * @return String-Repräsentation der Zuteilungsmatrix */
	public static String printMatrixR(Weight[][] wm)
	{
		int rows = wm.length;
		int cols = wm[0].length;
		String tmp = "";
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				tmp += (wm[i][j].rdWeight + wm[i][j].multiple + "; ");
			}
			tmp += "\n";
		}
		return tmp;
	}

	/** Diese Methode beendet die Biprop Berechnung bei einem Abbruch durch den
	 * Benutzer kontrolliert.
	 * 
	 * @param ret Rückgabe Objekt
	 * @param sFlaws Fehlstände
	 * @param rzahl Rücksprünge */
	private void userInterrupted(BipropRet ret, StringBuffer sFlaws, int rzahl)
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
		if (Debug.BIPROP)
		{
			notifyMethodListeners("Entwicklung der Fehlstände:\n" + sFlaws);
			notifyMethodListeners("Anzahl der Rücksprünge: " + rzahl);
		}
	}

	/** Neuberechnung der Divisoren nach Tie&Transfer
	 * 
	 * @param appMatrix die Matrix mit der Zuteilung (wird wegen der Fähnchen
	 *          benötigt)
	 * @param rowDivs die Zeilendivisoren (aus T&T)
	 * @param colDivs die Spaltendivisoren (aus T&T)
	 * @throws BipropException */
	private void reCalculateDivisors(Weight[][] appMatrix, double[] rowDivs, double colDivs[]) throws BipropException
	{

		LibMessenger messenger = new LibMessenger();
		// Method method = new Method(null, new Divisor(), messenger);
		// boolean bm = false;

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
					// try {
					// bm = method.divMethVector(sp, divisor, colApp[j], aktcol,
					// messenger);

					Method.statPowMethod(sp, divisor, colApp[j], aktcol, messenger, "");
					// }
					// catch (MethodException e) {
					// throw new BipropException("Recalculating Divisors for Col " + j, e);
					// }
					// Update der Divisoren
					/* aktColDivs[j] = divisor.getDivisor();
					 * aktColDivMin[j] = divisor.getDivisorLow();
					 * aktColDivMax[j] = divisor.getDivisorHigh();
					 * aktColMuls[j] = divisor.getMultiplier();
					 * aktColMulMin[j] = divisor.getMultiplierLow();
					 * aktColMulMax[j] = divisor.getMultiplierHigh(); */
					colDivs[j] = divisor.getDivisor();

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
					// try {
					// bm = method.divMethVector(sp, divisor, rowApp[i], aktrow,
					// messenger);

					Method.statPowMethod(sp, divisor, rowApp[i], aktrow, messenger, "");
					// }
					// catch (MethodException e) {
					// throw new BipropException("Recalculating Divisors for rowc " + i, e);
					// }
					// Update der Divisoren
					/* aktRowDivs[i] = divisor.getDivisor();
					 * aktRowDivMin[i] = divisor.getDivisorLow();
					 * aktRowDivMax[i] = divisor.getDivisorHigh();
					 * aktRowMuls[i] = divisor.getMultiplier();
					 * aktRowMulMin[i] = divisor.getMultiplierLow();
					 * aktRowMulMax[i] = divisor.getMultiplierHigh(); */
					rowDivs[i] = divisor.getDivisor();
				}
			}
		}
	}


}
