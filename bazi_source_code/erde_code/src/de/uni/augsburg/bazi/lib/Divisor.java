/*
 * @(#)Divisor.java 3.4 09/09/22
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.lib;

/** <b>Title:</b> Klasse Divisor<br>
 * <b>Description:</b> Speicherung und Formatierung des Divisor<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Jan Petzold, Florian Kluge, Christian Brand
 * @version 3.4 */
public class Divisor implements Cloneable
{
	/* Versionshistorie:
	 * 2009.09-b-05: Version 3.4
	 * - Einbau eines Feldes, falls der Divisor nicht benötigt wird.
	 * Dies wird bei Vektormethoden genutzt, bei denen nur eine
	 * Partei angetreteten ist.
	 * 2008.01-b-02: Version 3.3
	 * - Einführung der Versionshistorie
	 * - Die Methoden digits(...), scalePow(...), dnRoundDig(...),
	 * upRoundDig(...), stdRoundDig(...), buildData(...) wurden von
	 * "public static" zu "private" aus Stilgründen
	 * - Das gesamte Layout der Klasse wurde an die CodeConventions angepasst.
	 * - Ein Problem mit 10^-6 wurde behoben.
	 * Diese Zahl kann nicht genau dargestellt werden. */

	/** Zeigt an, ob der Divisor aktiv ist.
	 * Ist der Divisor nicht aktiv, handelt es sich um die Quote. */
	private boolean enabled = true;

	/** Quote bei der Berechnung mit Quotenmethode. */
	private double quote = 0.0;

	/** Divisorintervall: untere Grenze. */
	private double divisorLow = 0.0;

	/** Divisorintervall: oberer Grenze. */
	private double divisorHigh = 0.0;

	/** Multiplikatorintervall: untere Grenze. */
	private double multiplierLow = 0.0;

	/** Multiplikatorintervall: obere Grenze. */
	private double multiplierHigh = 0.0;

	/** hier wird der Divisor nach dem Setzen der Intervallgrenzen gespeichert */
	private NumberSet dsDivisor, dsDivisorNice;

	/** hier wird der Multiplikator nach dem Setzen der Intervallgrenzen gespeichert */
	private NumberSet dsMultiplier;

	/** Speicherung der letzten Partei mit +-Fähnchen. Falls keine Fähnchen existieren, so hat diese Variable den Wert -1 */
	private int pt = -1;

	/** Gibt an, ob evtl. Listenverbindungen bestanden */
	private boolean combinedList = false;

	/** Gibt an, ob der Divisor verwendet werden muss */
	private boolean needed = true;

	/** Ausgabe der Nummer des letzten Plus-Ties
	 * 
	 * @return die Nummer des letzten Plus-Ties */
	public int getPt()
	{
		return pt;
	}

	/** Setzen der Nummer des letzten Plus-Ties.
	 * 
	 * @param p Nummer des letzten Plus-Ties. p soll größer oder gleich -1 sein. Es wird <b>keine</b> Bereichsprüfung vorgenommen! */
	public void setPt(int p)
	{
		pt = p;
	}

	/** Umschalten zwischen Divisor und Quote. Gilt b = true, dann ist der Divisor aktiv. Gilt b = false, dann ist die Quote aktiv.
	 * 
	 * @param b Wenn <b>true</b>, so ist Divisor aktiv */
	public void setEnabled(boolean b)
	{
		enabled = b;
	}

	/** Setzen der Quote.
	 * 
	 * @param d die Quote */
	public void setQuote(double d)
	{
		quote = d;
	}

	/** setzen des Divisorintervalls
	 * 
	 * @param low untere Grenze des Intervalls
	 * @param high obere Grenze des Intervalls
	 * @throws DivisorException */
	public void setDivisorInterval(double low, double high) throws DivisorException
	{
		divisorLow = low;
		divisorHigh = high;
		dsDivisor = buildData(divisorLow, divisorHigh);
	}

	/** setzen des bevorzugten Divisorintervalls (in dem z.B. die Zuteilung ohne Min/Max möglich ist)
	 * 
	 * @param low untere Grenze des Intervalls
	 * @param high obere Grenze des Intervalls
	 * @throws DivisorException */
	public void setDivisorIntervalNice(double low, double high) throws DivisorException
	{
		dsDivisorNice = buildData(low, high);
	}

	/** Setzen des Multiplikatorenintervalls
	 * 
	 * @param low untere Grenze des Intervalls
	 * @param high obere Grenze des Intervalls
	 * @throws DivisorException */
	public void setMultiplierInterval(double low, double high) throws DivisorException
	{
		multiplierLow = low;
		multiplierHigh = high;
		dsMultiplier = buildData(multiplierLow, multiplierHigh);
	}

	/** Rückgabe der Quote.
	 * 
	 * @return die Quote */
	public double getQuote()
	{
		// vor der Rückgabe runden der Quote
		if (Double.isInfinite(quote))
		{
			return quote;
		}
		double q;
		if (quote < 100000)
		{
			q = stdRoundDig(6, quote);
		}
		else
		{
			q = ((double) (Math.round(quote * 10))) / 10;
		}
		if (Math.abs(q - Math.round(q)) < Math.pow(10, digits(q) - 16))
		{
			q = Math.round(q);
		}
		return q;
	}

	public double getUnroundedQuota()
	{
		return quote;
	}

	/** Rückgabe eines Divisors aus dem Divisorintervall.
	 * 
	 * @return ein schöner Divisor */
	public double getDivisor()
	{
		if (dsDivisorNice != null)
			return dsDivisorNice.c;
		return dsDivisor.c;
	}

	/** Rückgabe der unteren Grenze des Divisorintervalls.
	 * 
	 * @return die untere Grenze des Divisorintervalls */
	public double getDivisorLow()
	{
		return dsDivisor.low;
	}

	/** Rückgabe der oberen Grenze des Divisorintervalls.
	 * 
	 * @return die obere Grenze des Divisorintervalls */
	public double getDivisorHigh()
	{
		return dsDivisor.high;
	}

	/** Rückgabe eines Multiplikators aus dem Multiplikatorintervall.
	 * 
	 * @return ein schöner Multiplikator */
	public double getMultiplier()
	{
		return dsMultiplier.c;
	}

	/** Rückgabe der unteren Grenze des Multiplikatorintervalls.
	 * 
	 * @return die untere Grenze des Multiplikatorintervalls */
	public double getMultiplierLow()
	{
		return dsMultiplier.low;
	}

	/** Rückgabe der oberen Grenze des Multiplikatorintervalls.
	 * 
	 * @return die obere Grenze des Multiplikatorintervalls */
	public double getMultiplierHigh()
	{
		return dsMultiplier.high;
	}

	/** Anzeige, ob Divisor oder Quote aktiv ist.
	 * 
	 * @return <b>true</b>, wenn Divisor aktiv ist */
	public boolean isEnabled()
	{
		return enabled;
	}

	/** Berechnet die Dezimalstellen einer Zahl
	 * 
	 * @param x Die Zahl
	 * @return die Dezimalstellen der Zahl */
	private int digits(double x)
	{
		return (int) (1 + Math.floor(Math.log(x) / Math.log(10)));
	}

	/** Berechnet den notwendigen Skalierungsfaktor
	 * 
	 * @param k Stellenzahl
	 * @param x Die Zahl
	 * @return der Skalierungsfaktor */
	private double scalePow(int k, double x)
	{
		double erg = Math.pow(10.0, k - digits(x));
		return erg;
	}

	/** Intelligentes Abrunden
	 * 
	 * @param k Stellenzahl
	 * @param x zu rundende Zahl
	 * @return x abgerundet */
	private double dnRoundDig(int k, double x)
	{
		/* Änderung 2008.01-b-02
		 * Probleme mit 10^-6. Dies wird in Java nicht richtig berechnet.
		 * Daher wird an Stelle durch 10^-6 zu teilen mit 10^6 mal genommen. */
		if (k - digits(x) < 0)
		{
			double erg1 = Math.pow(10.0, digits(x) - k);
			double erg2 = Math.pow(10.0, k - digits(x));
			erg2 = Math.floor(x * erg2);
			return erg2 * erg1;
		}
		else
		{
			return Math.floor(x * scalePow(k, x)) / scalePow(k, x);
		}
		// Alter Code vor Änderung 2008.01-b-02
		// return Math.floor(x * scalePow(k, x)) / scalePow(k, x);
	}

	/** Intelligentes Abrunden
	 * 
	 * @param k Stellenzahl
	 * @param x zu rundende Zahl
	 * @return x abgerundet */
	private double upRoundDig(int k, double x)
	{
		if (k - digits(x) < 0)
		{
			/* Änderung 2008.01-b-02
			 * Probleme mit 10^-6. Dies wird in Java nicht richtig berechnet.
			 * Daher wird an Stelle durch 10^-6 zu teilen mit 10^6 mal genommen. */
			double erg1 = Math.pow(10.0, digits(x) - k);
			double erg2 = Math.pow(10.0, k - digits(x));
			erg2 = Math.ceil(x * erg2);
			return erg2 * erg1;
		}
		else
		{
			return Math.ceil(x * scalePow(k, x)) / scalePow(k, x);
		}
		// Alter Code vor Änderung 2008.01-b-02
		// return Math.ceil(x * scalePow(k, x)) / scalePow(k, x);
	}

	/** Intelligentes Abrunden
	 * 
	 * @param k Stellenzahl
	 * @param x zu rundende Zahl
	 * @return x abgerundet */
	private double stdRoundDig(int k, double x)
	{
		/* Änderung 2008.01-b-02
		 * Probleme mit 10^-6. Dies wird in Java nicht richtig berechnet.
		 * Daher wird an Stelle durch 10^-6 zu teilen mit 10^6 mal genommen. */
		if (k - digits(x) < 0)
		{
			double erg1 = Math.pow(10.0, digits(x) - k);
			double erg2 = Math.pow(10.0, k - digits(x));
			erg2 = Math.round(x * erg2);
			return erg2 * erg1;
		}
		else
		{
			return Math.round(x * scalePow(k, x)) / scalePow(k, x);
		}
		// Alter Code vor Änderung 2008.01-b-02
		// return (Math.round(x * scalePow(k, x))) / scalePow(k, x);
	}

	/** Erstelle den kompletten Datensatz mit den gerundeten Werten
	 * 
	 * @param low untere Grenze des Divisorintervalls
	 * @param high obere Grenze des Divisorintervalls
	 * @return Satz mit den gerundeten Werten
	 * @throws DivisorException */
	public NumberSet buildData(double low, double high) throws DivisorException
	{

		// Rückgabewert initialisieren
		NumberSet s = new NumberSet();

		// Falscher Input?
		if (low < 0)
		{
			throw new DivisorException("", DivisorException.IO_LESS_THAN_ZERO, low, high);
		}
		if (low > high)
		{
			throw new DivisorException("", DivisorException.IO_LIMITS_FAULT, low, high);
		}

		if (Double.isInfinite(high) && !Double.isInfinite(low))
		{
			s.low = low;
			s.high = high;
			double temp = Math.pow(10, (int) Math.log10(low));
			s.c = Math.ceil(low / temp) * temp;
			return s;
		}
		else if (Double.isInfinite(high) && Double.isInfinite(low))
		{
			s.low = low;
			s.high = high;
			s.c = 0;
		}

		// Entartungsfall
		if (low == high)
		{
			s.low = low;
			s.high = s.low;
			s.c = s.low;
			return s;
		}

		/* STANDARDFALL:
		 * 0 <= L < H < Double.POSITIVE_INFINITY */

		// Intervallmitte
		double dbar = (low + high) / 2;

		// untere Grenze
		int k = Math.max(6, Math.max(digits(low), (digits(low) - digits(high - low) + 2)));
		if (low == 0)
		{
			s.low = 0;
		}
		else
		{
			s.low = upRoundDig(k, low);
		}

		// obere Grenze
		k = Math.max(6, Math.max(digits(high), (digits(high) - digits(high - low) + 2)));
		s.high = dnRoundDig(k, high);
		if (s.low < s.high)
		{
			k = -1;
			double tmpr;
			do
			{
				k++;
				tmpr = stdRoundDig(k, dbar);
			}
			while ((k < 16) && ((tmpr <= s.low) || (tmpr >= s.high)));
			s.c = tmpr;
		}
		else
		{
			if (s.low == s.high)
			{
				// sollte nie passieren
				s.c = s.low;
			}
			else
			{
				// aber wer weiß...
				throw new DivisorException("", low, high, s.low, s.high);
			}
		}

		// IF (D < Dmin OR D > Dmax) THEN D = Dmax ENDIF # check!
		if ((s.c < s.low) || (s.c > s.high))
		{
			s.c = s.high;
		}

		// Entfernung der Multiplikations-/Divisionsfehler (x.0000000000001)
		double tmp = Math.round(s.c);
		// falls tmp im Intervall liegt, ist es mindestens genauso schön wie s.c
		// es ist also nur eine Verbesserung möglich!
		if ((s.low < tmp) && (tmp < s.high))
		{
			s.c = tmp;
		}
		return s;
	}

	/** Klont diesen Divisor
	 * 
	 * @return eine Kopie dieses Divisors */
	public Divisor cloneD()
	{
		Divisor tmp = new Divisor();
		if (isEnabled())
		{
			try
			{
				tmp.setDivisorInterval(divisorLow, divisorHigh);
				tmp.setMultiplierInterval(multiplierLow, multiplierHigh);
			}
			catch (DivisorException de)
			{
				System.out.println(de);
			}
		}
		else
		{
			tmp.setQuote(quote);
		}
		tmp.setEnabled(enabled);
		tmp.setCombinedList(combinedList);
		tmp.setNeeded(needed);
		return tmp;
	}

	@Override
	public Divisor clone()
	{
		return cloneD();
	}

	/** Gibt den Wert vom Attribut combinedList zurueck
	 * 
	 * @return */
	public boolean isCombinedList()
	{
		return combinedList;
	}

	/** Setzt den Wert vom Attribut combinedList auf b
	 * 
	 * @param b */
	public void setCombinedList(boolean b)
	{
		combinedList = b;
	}

	public boolean isNeeded()
	{
		return needed;
	}

	public void setNeeded(boolean b)
	{
		needed = b;
	}
}
