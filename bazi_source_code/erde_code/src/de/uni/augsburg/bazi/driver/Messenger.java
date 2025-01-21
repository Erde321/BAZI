/*
 * @(#)Messenger.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.util.Vector;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.Convert;
import de.uni.augsburg.bazi.lib.LibMessenger;
import de.uni.augsburg.bazi.lib.Rounding;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Title:</b> Klasse Messenger<br>
 * <b>Description:</b> Speichert und verwaltet Meldungen und Warnungen, die während der Berechnung auftreten<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Jan Petzold, Robert Bertossi, Christian Brand, Marco Schumacher */
public class Messenger
{

	/** Zeigt an, daß Hare mit 0 Mandaten aufgerufen wurde
	 * => Quote NA, aber für alle 0 Sitze */
	public boolean getZeroHare()
	{
		return myMessenger.getZeroHare();
	}

	/** Das LibMessenger Objekt, das verwaltet wird */
	private final LibMessenger myMessenger;

	/** Sammlung aller aufgetretenen globalen Fehlertypen */
	private final Vector<String> globalMessages = new Vector<String>();

	/** Daten für die Ausgabe */
	private int base, min, max, numberOfPosiveWeightsWithoutMinCond;

	/** Daten für die Ausgabe */
	private String labelCondition;

	/** Erzeugt ein Messenger Objekt, das den übergebenen LibMessenger verwaltet.
	 * @param lm LibMessenger, der verwaltet werden soll */
	public Messenger(LibMessenger lm)
	{
		myMessenger = lm;
	}

	/** Hier werden Infos wie base, min und max gespeichert, um die Fehlermeldungen
	 * genauer zu machen */
	public void setData(InputData data)
	{
		base = data.weightcount() * data.base;
		min = 0;
		max = 0;
		numberOfPosiveWeightsWithoutMinCond = 0;
		labelCondition = data.outputFormat.labelCondition;
		for (Weight w : data.originalWeights)
		{
			min += w.min;
			max += w.max;
		}
		for (int i = 0; i < data.weightcount(); i++)
		{
			if ((data.originalWeights[i].min == 0) && (data.originalWeights[i].weight > 0) && data.originalWeights[i].max != 0)
			{
				numberOfPosiveWeightsWithoutMinCond++;
			}
		}
	}

	/** Gibt ggf. eine Fehlernachricht zurück, die speziell für die aktuelle
	 * Berechnung relevant. Bei keinem speziellen Fehler wird der leere String
	 * zurückgegeben.
	 * 
	 * @return spezielle Fehlernachricht */
	public String getLibError()
	{
		switch (myMessenger.getErrorCode())
		{
		case -1:
			return "";
			// globale Fehler
		case LibMessenger.NEGATIVE_WEIGHTS:
		case LibMessenger.MINIMUM:
		case LibMessenger.MAXIMUM:
		case LibMessenger.SUM_OF_WS_ZERO:
		case LibMessenger.REMAIN:
		case LibMessenger.DROOP:
		case LibMessenger.MIN_SIGNPOST:
			return "";
		default:
			return myMessenger.getErrorMessage();
		}
	}

	/** Gibt alle globalen Fehlernachrichten zurück.
	 * Alle globalen Fehler der durchgeführten Berechnungen werden ausgelesen und
	 * zeilenweise zurückgegeben. Bei keinen globalen Fehlern wird der leere
	 * String zurückgegeben.
	 * 
	 * @return globale Fehlermeldungen */
	public String getGlobalErrors()
	{
		String out = "";
		for (String s : globalMessages)
			out += s + "\n";
		return out;
	}

	/** <b>true</b> wenn es globale Fehler gegeben hat, sonst <b>false</b>
	 * 
	 * @return <code>true</code> wenn es globale Fehler gegeben hat */
	public boolean isGlobalError()
	{
		return globalMessages.size() != 0;
	}

	/** Setzt den internen LibMessenger für die nächste Berechnung zurück. Globale
	 * Fehler werden im Messenger Objekt gespeichert, um sie am Ende aufzuzählen. */
	public void refresh()
	{
		// liegt ein Fehler vor, der eine globale Fehlermeldung erfordert?
		switch (myMessenger.getErrorCode())
		{
		case LibMessenger.MINIMUM:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.minplus1")
					+" \"" + labelCondition + "\" "
					+ Resource.getString("bazi.error.minplus2"));
		 break;
		 
		/*case LibMessenger.MINIMUM:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.min1")
					+ (myMessenger.getAccuracy() + base)
					+ Resource.getString("bazi.error.min2")
					+ " \"" + labelCondition + "\" "
					+ Resource.getString("bazi.error.min3")
					+ " " + getMin() + " "
					+ Resource.getString("bazi.error.min4"));
			break;*/
		case LibMessenger.MAXIMUM:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.max1")
					+ (myMessenger.getAccuracy() + base)
					+ Resource.getString("bazi.error.max2")
					+ " \"" + labelCondition + "\" "
					+ Resource.getString("bazi.error.max3")
					+ " " + getMax() + " "
					+ Resource.getString("bazi.error.max4"));
			break;
		case LibMessenger.REMAIN:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.quogrr.remain"));
			break;
		case LibMessenger.DROOP:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.droop"));
			break;
		case LibMessenger.NEGATIVE_WEIGHTS:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.weights.negative"));
			break;
		case LibMessenger.SUM_OF_WS_ZERO:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.weights.sumzero"));
			break;
		case LibMessenger.MIN_SIGNPOST:
		    globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.min1")
					+ (myMessenger.getAccuracy() + base)
				    + Resource.getString("bazi.error.min2")
					+ " \"" + labelCondition + "\" "
					+ Resource.getString("bazi.error.and")
					+ " \"" + myMessenger.getMethod() + "\" "
					+ Resource.getString("bazi.error.min3")
					+ " " + getMin2() + " "
					+ Resource.getString("bazi.error.min4"));
			break;
		 
			
		case LibMessenger.BASE:
			globalMessages.add(Calculation.COMMENT
					+ Resource.getString("bazi.error.base1")
					+ (myMessenger.getAccuracy() + base)
					+ Resource.getString("bazi.error.base2")
					+ " (" + base + ")");
		}
		myMessenger.refresh();
	}

	/** <b>true</b> genau dann, wenn die aktuelle Berechnung einen MINIMUM
	 * Fehlertyp produziert hat, oder es keine Lösung in der Hauptzuteilung gab
	 * (bei Listenverbindungen)
	 * 
	 * @return <code>true</code> wenn es gerade einen MINIMUM Fehler gab */
	public boolean isMinError()
	{
		return myMessenger.getErrorCode() == LibMessenger.MINIMUM ||
				myMessenger.getErrorCode() == LibMessenger.MAXIMUM ||
				myMessenger.getErrorCode() == LibMessenger.ERROR_MAIN_PROP ||
				myMessenger.getErrorCode() == LibMessenger.REMAIN;
	}

	/** <b>true</b> genau dann, wenn die aktuelle Berechnung einen DROOP Fehler
	 * produziert hat.
	 * 
	 * @return <code>true</code> wenn es gerade einen DROOP Fehler gab */
	public boolean isDroopError()
	{
		return myMessenger.getErrorCode() == LibMessenger.DROOP;
	}

	/** @return base>0 => (base+min=[base+min]) <br>
	 *         base=0 => (min) */
	private String getMin()
	{
		if (base > 0)
			return base + "+" + min + "=" + (base + min);
		else
			return min + "";
	}

	/** @return base>0 => (base+max=[base+max]) <br>
	 *         base=0 => (max) */
	private String getMax()
	{
		if (base > 0)
			return base + "+" + max + "=" + (base + max);
		else
			return max + "";
	}

	private String getMin2()
	{
		if (base + min > 0)
			return (base + min) + "+" + numberOfPosiveWeightsWithoutMinCond + "=" + (base + min + numberOfPosiveWeightsWithoutMinCond);
		else
			return numberOfPosiveWeightsWithoutMinCond + "";
	}


	/** Zeigt an, dass eine Partei die absolute Mehrheit an Stimmen hat (gilt nur für Quotenmethoden). */
	public boolean getAbsoluteMessage()
	{
		return myMessenger.getAbsoluteMessage();
	}

	/** Rückgabe der Meldung, dass eine Partei die absolute Mehrheit an Stimmen hat (gilt nur für Quotenmethoden).
	 * 
	 * @param labelWeights Bezeichnung der Stimmen. */
	public String getAbsoluteMessage(String labelWeights, int decimalPlaces)
	{
		String mes = Calculation.COMMENT + "\""
				+ myMessenger.getAbsolutName()
				+ "\" "
				+ Resource.getString("bazi.error.quogrr.absolute1")
				+ " "
				+ Convert.doubleToString(Rounding.round(myMessenger.getAbsoluteVotes(), decimalPlaces))
				+ " "
				+ Resource.getString("bazi.error.quogrr.absolute2")
				+ " "
				+ Convert.doubleToString(Rounding.round(myMessenger.getAbsoluteAllVotes(), decimalPlaces))
				+ " ";
		if (labelWeights.equals("Voti"))
		{
			mes += Resource.getString("bazi.error.quogrr.absolute3")
					+ "\n";
		}
		else
		{
			mes += labelWeights
					+ " "
					+ Resource.getString("bazi.error.quogrr.absolute3")
					+ "\n";
		}
		return mes;
	}

}
