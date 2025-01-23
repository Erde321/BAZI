/*
 * @(#)Calculation.java 3.2 09/10/05
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.io.File;
import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.VersionControl;
import de.uni.augsburg.bazi.gui.RoundFrame;
import de.uni.augsburg.bazi.lib.Convert;
import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.DivisorException;
import de.uni.augsburg.bazi.lib.ExtendedPowerMean;
import de.uni.augsburg.bazi.lib.ExtendedStationary;
import de.uni.augsburg.bazi.lib.IterationListener;
import de.uni.augsburg.bazi.lib.MethodListener;
import de.uni.augsburg.bazi.lib.ParameterOutOfRangeException;
import de.uni.augsburg.bazi.lib.PermutationsCommunicator;
import de.uni.augsburg.bazi.lib.PowerMean;
import de.uni.augsburg.bazi.lib.Signpost;
import de.uni.augsburg.bazi.lib.Stationary;
import de.uni.augsburg.bazi.lib.Weight;
import de.uni.augsburg.bazi.lib.vector.Method;

/** <b>Title:</b> Klasse Calculation<br>
 * <b>Description:</b> Steuert den kompletten Berechnungsablauf und
 * stellt die Kommandozeilenversion des Bazi zur Verfügung<br>
 * <b>Copyright:</b> Copyright (c) 2005-2010<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 3.2
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */
public class Calculation
{
	/** Copyright */
	public static final String COPYRIGHT = "2000-2011";
	/* Weitere Copyrightangaben:
	 * - bazi.properties: bazi.gui.info
	 * - Startbild
	 * - gnupl.txt */

	/** Zeichen, dass für Kommentare in der Ausgabe benutzt wird */
	public static final char COMMENT = '*';

	/** Standard Fußzeile der Ausgabe */
	public static final String APPENDIX = COMMENT + "BAZI " + VersionControl.getVersion() +
			" - Made in TH Rosenheim";

	/** Trennzeile in der Ausgabe (eine Zeile nur mit COMMENTs) */
	private static String outputBoundary = null;

	/** Parameter bei einem Kommandozeilen Aufruf */
	private static Hashtable<String, String> params = new Hashtable<String, String>();

	/** bei Kommandozeilen Aufruf wird dieser Wert gesetzt */
	public static boolean isCommandLineBazi = false;

	/** Eingelesene Eingabedaten */
	private AbstractInputData aid = null;

	/** IterationListener (für Kommandozeile) */
	private IterationListener il = null;

	/** MethodListener (für Kommandozeile) */
	private MethodListener ml = null;

	/** PermutationsCommunicator (für Kommandozeile) */
	private PermutationsCommunicator pc = null;

	/** Erstellt ein neues Calculation Objekt mit den übergebenen Eingabedaten
	 * @param aid Eingabedaten */
	public Calculation(AbstractInputData aid)
	{
		this.aid = aid;
		if (ml == null)
		{
			ml = new DummyMethodListener();
		}
	}

	/** main-Methode für Commandline-BAZI
	 * @param args Parameter */
	public static void main(String[] args)
	{

		Calculation.isCommandLineBazi = true;

		File propertyFile = new File(".\\log4j.properties");
		if (!propertyFile.exists())
		{
			Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
			Logger.getRootLogger().setLevel(Level.OFF);
		}
		else
		{
			PropertyConfigurator.configure(".\\log4j.properties");
			Logger.getRootLogger().info("Logging-Konfiguration in: .\\log4j.properties");
			Logger.getRootLogger().info("Logging bis Level: "+Logger.getRootLogger().getLevel());
		}

		parseParams(args);
		// Datei muss angegeben werden!
		String file = params.get("file");
		if (file == null)
		{
			System.out.println("File to calculate must be stated");
			printUsageMessage();
		}
		String lang = params.get("lang");
		if (lang != null)
		{
			Resource.setLang(lang);
		}
		// System.out.println("lang: " + lang);
		// System.out.println("file: " + file);
		File f = new File(file);
		if (!f.exists())
		{
			System.out.println("File " + file + " does not exist!");
			System.exit(0);
		}
		// Einlesen der Datei
		FileIO fileio = new FileIO();
		AbstractInputData aid = fileio.open(f);
		if (aid instanceof DistrictInputData)
		{
			DistrictInputData did = (DistrictInputData) aid;
			String alt = params.get("bp");
			if (alt != null)
			{
				if (alt.equals("11") || alt.equals("h_asmdpt_ttflpt"))
					did.bipropAlt = DistrictInputData.H_ASMDPT_TTFLPT;
				else if (alt.equals("13") || alt.equals("asmdpt"))
					did.bipropAlt = DistrictInputData.ASMDPT;

				else if (alt.equals("21") || alt.equals("h_asextr_ttflpt"))
					did.bipropAlt = DistrictInputData.H_ASEXTR_TTFLPT;
				else if (alt.equals("23") || alt.equals("asextr"))
					did.bipropAlt = DistrictInputData.ASEXTR;

				else if (alt.equals("h_asmdpt_ttinte"))
					did.bipropAlt = DistrictInputData.H_ASMDPT_TTINTE;
				else if (alt.equals("h_asextr_ttinte"))
					did.bipropAlt = DistrictInputData.H_ASEXTR_TTINTE;
				else if (alt.equals("h_asrand_ttinte"))
					did.bipropAlt = DistrictInputData.H_ASRAND_TTINTE;

				else if (alt.equals("31") || alt.equals("h_asrand_ttflpt"))
					did.bipropAlt = DistrictInputData.H_ASRAND_TTFLPT;
				else if (alt.equals("33") || alt.equals("asrand"))
					did.bipropAlt = DistrictInputData.ASRAND;

				else if (alt.equals("41") || alt.equals("h_ipfp_ttflpt"))
					did.bipropAlt = DistrictInputData.H_IPFP_TTFLPT;
				else if (alt.equals("42") || alt.equals("h_ipfp_ttinte"))
					did.bipropAlt = DistrictInputData.H_IPFP_TTINTE;

				else if (alt.equals("51") || alt.equals("ttflpt"))
					did.bipropAlt = DistrictInputData.TTFLPT;
				else if (alt.equals("52") || alt.equals("ttinte"))
					did.bipropAlt = DistrictInputData.TTINTE;
			}
		}
		Calculation calc = new Calculation(aid);
		calc.setIterationListener(new DummyIterationListener());
		calc.setMethodListener(new DummyMethodListener());
		calc.setPermutationsCommunicator(new DummyPermutationsCommunicator());

		System.out.println(calc.calculate());

		System.exit(0);
	}

	/** Liefert die Trennzeile für die Ausgabe. Beim ersten Aufruf wird die Trennzeile generiert, so dass sie die gleiche Länge hat wie die Fußzeile.
	 * @return Trennzeile */
	public static String getOutputBoundary()
	{
		if (outputBoundary == null)
		{
			outputBoundary = "";
			for (int i = 0; i < APPENDIX.length(); i++)
			{
				outputBoundary += Calculation.COMMENT;
			}
			outputBoundary += "\n";
		}
		return outputBoundary;
	}

	/** Prints a usage message and exits */
	private static void printUsageMessage()
	{
		System.out.println("\n" +
				"////  Usage  /////////////////////////////////////////////////////////\n" +
				"To print version:\n" +
				"java -cp bazi.jar de.uni.augsburg.bazi.driver.Calculation --version\n" +
				"\n" +
				"To process a bazi input file:\n" +
				"java -cp bazi.jar de.uni.augsburg.bazi.driver.Calculation" +
				" [-bp <value>] [-l <value>] -f <filename>\n" +
				"\n" +
				"\tbp: possible values are {asmdpt|asrand|asextr|ttflpt|ttinte\n" +
				"\t    |h_asmdpt_ttflpt|h_asrand_ttflpt|h_asextr_ttflpt|h_asmdpt_ttinte\n" +
				"\t    |h_asextr_ttinte|h_asrand_ttinte|h_ipfp_ttflpt|h_ipfp_ttinte}\n" +
				"\tl : possible values are {de|en|es|fr|it}\n" +
				"\tf : followed by the path of the input file\n" +
				"//////////////////////////////////////////////////////////////////////\n");
		System.exit(0);
	}

	/** Prints version information and exits */
	private static void printVersionMessage()
	{
		System.out.println("Bazi CommandLine Version " + VersionControl.getVersion());
		System.exit(0);
	}

	/** Liest die Eingabeparameter aus und füllt die Hashtable params entsprechend.
	 * @param args Parameter */
	private static void parseParams(String[] args)
	{
		if (args.length == 1)
		{
			if (args[0].equals("--version"))
			{
				printVersionMessage();
			}
			else
			{
				printUsageMessage();
			}
		}

		int i = 0;
		while (i < args.length)
		{
			String tmp = args[i];
			if (tmp.equals("-l"))
			{
				i++;
				if (i == args.length)
				{
					printUsageMessage();
				}
				String lang = args[i];
				if (lang.equals("de") || lang.equals("en") || lang.equals("es")
						|| lang.endsWith("fr") || lang.equals("it"))
				{
					params.put("lang", lang);
				}
				else
				{
					System.out.println("Invalid language");
					printUsageMessage();
				}
			}
			if (tmp.equals("-f"))
			{
				i++;
				if (i == args.length)
				{
					printUsageMessage();
				}
				String file = args[i];
				params.put("file", file);
			}
			if (tmp.equals("-bp"))
			{
				i++;
				if (i == args.length)
				{
					printUsageMessage();
				}
				String bp = args[i];
				if (!bp.equals("11") && !bp.equals("h_asmdpt_ttflpt") &&
						!bp.equals("13") && !bp.equals("asmdpt") &&
						!bp.equals("21") && !bp.equals("h_asextr_ttflpt") &&
						!bp.equals("23") && !bp.equals("asextr") &&
						!bp.equals("31") && !bp.equals("h_asrand_ttflpt") &&
						!bp.equals("33") && !bp.equals("asrand") &&
						!bp.equals("41") && !bp.equals("h_ipfp_ttflpt") &&
						!bp.equals("42") && !bp.equals("h_ipfp_ttinte") &&
						!bp.equals("51") && !bp.equals("ttflpt") &&
						!bp.equals("52") && !bp.equals("ttinte") &&
						!bp.equals("h_asmdpt_ttinte") &&
						!bp.equals("h_asextr_ttinte") &&
						!bp.equals("h_asrand_ttinte"))
					printUsageMessage();
				params.put("bp", bp);
			}
			i++;
		}
	}

	/** Setzt den IterationListener
	 * @param l IterationListener */
	public void setIterationListener(IterationListener l)
	{
		il = l;
	}

	/** Setzt den MethodListener
	 * 
	 * @param l MethodListener */
	public void setMethodListener(MethodListener l)
	{
		ml = l;
	}

	/** Setzt den PermutationsCommunicator
	 * @param c PermutationsCommunicator */
	public void setPermutationsCommunicator(PermutationsCommunicator c)
	{
		pc = c;
	}

	public String calculate()
	{
		if (aid.pow)
			return new Calculation_Pow().calculate(aid);

		if (aid.outputFormat.condition == OutputFormat.CONDITION_MIN_PLUS
			  || aid.outputFormat.condition == OutputFormat.CONDITION_MIN_VPV)
			return new Calculation_LevelingSeats().calculate(aid);

		return calculate2();
	}

	/** Berechnet die Zuteilung und gibt diese aus (formatiert als String)
	 * @return Zuteilung */
	public String calculate2()
	{
		
		String output;
		if (aid.outputFormat.condition == OutputFormat.CONDITION_MIN_PLUS && Calculation_LevelingSeats.comment == false)
			output = "\n";
		else
			output = getOutputBoundary();
		
		// monoproportional => einfach ;-)
		if (aid instanceof InputData)
		{
			InputData id = (InputData) aid;			
			// wb: Start
//			if (id.ml != null)
//			{
//				id.ml.printMessage("calculate2");
//				id.ml.printMessage("\n--------------\n");
//			}
			// wb: Ende
			
			CalculationMonoprop c = new CalculationMonoprop(id, ml);
			output += c.start();
			for (int i = 0; i < id.listData.length; i++)
			{
				String listOutput = new CalculationMonoprop(id.listData[i], ml).start();
				output += listOutput;
			}	
		}
			
		// mit Distrikten
		else if (aid instanceof DistrictInputData)
		{
			output += Calculation.COMMENT + aid.title + "\n";
			DistrictInputData did = (DistrictInputData) aid;
			switch (did.method)
			{
			case DistrictInputData.SEPARATE:
			{
				output += calcSeparate(did);
				break;
			}
			case DistrictInputData.BIPROP:
			case DistrictInputData.NZZ:
				output += calcBiprop(did);
				break;
			}
		}

		if (!output.endsWith("\n"))
		{
			output += "\n";
		}
		if(!(aid.outputFormat.condition == OutputFormat.CONDITION_MIN_PLUS && Calculation_LevelingSeats.comment == false))
			output += APPENDIX + "\n" + outputBoundary;
		ml.printMessage(APPENDIX + "\n");
		return output;
	}

	/** Berechnet die Zuteilung für die übergebenen Eingabedaten. (Seperate
	 * Distriktauswertung)
	 * @param did Eingabedaten mit mehreren Distrikten
	 * @return Die Zuteilung als String formatiert */
	public String calcSeparate(DistrictInputData did)
	{
		did.prepare();
		String output = new CalculationSeparate(did).start();
		return output;
	}

	/** Berechnet die Zuteilung für die übergebenen Eingabedaten. (Biproportionale
	 * Berechnung)
	 * @param did Eingabedaten mit mehreren Distrikten
	 * @return Die Zuteilung als String formatiert */
	public String calcBiprop(DistrictInputData did)
	{
		did.prepare();
		String output = new CalculationBiprop(did, il, ml, pc).start();
		return output;
	}
	

	private static class DummyMethodListener implements MethodListener
	{
		@Override
		public void printMessage(String msg)
		{
			// Leerer Method Listener
			// System.out.println(msg);
		}
	}

	private static class DummyIterationListener implements IterationListener
	{
		@Override
		public void initialize(String[] methods)
		{}

		@Override
		public void iterationChanged(int cnt, boolean finished)
		{}

		@Override
		public void iterationFinished()
		{}
	}

	private static class DummyPermutationsCommunicator implements PermutationsCommunicator
	{
		@Override
		public boolean calcAllPermutations()
		{
			return false;
		}

		@Override
		public void permutationChanged(int cnt)
		{}
	}

	/** Formatiert die gesamte Lösung, d.h. die Ausrichtung wird festgelegt und die
	 * Spaltenbreite wird angepasst.
	 * @param v Ein Vektor Array mit den Spalten bzw. Zeilen (je nach
	 *          Ausrichtung) der Ausgabe
	 * @param now Anzahl der Gewichte
	 * @param align Ausrichtung (aus OutputFormat)
	 * @return Formatierte Ausgabe */
	public static String writeSolution(Vector<String[]> v, int now, int align)
	{
		int numberOfWeights = now;
		// System.out.println(now);
		// System.out.println("now: " + now);
		// Titel ausgeben
		// StringBuffer sOutput = new StringBuffer("#" + title + "\n");
		StringBuffer sOutput = new StringBuffer();
		if (align == OutputFormat.ALIGN_HORIZONTAL)
		{
			// String mit maximaler Länge in jeder Spalte suchen
			// Maximum: 16
			int columnLength[] = new int[numberOfWeights + 1];
			for (int m = 0; m < numberOfWeights + 1; m++)
			{
				columnLength[m] = 0;
			}
			for (int i = 0; i < v.size(); i++)
			{
				// System.out.print("\ni: " + i + " --- ");
				String s[] = v.elementAt(i);
				for (int k = 0; k < numberOfWeights + 1; k++)
				{
					// System.out.print("k: " + k + "; " + s[k] + " ;; ");
					int temp = s[k].length();
					if (temp > columnLength[k])
					{
						columnLength[k] = temp;
					}
				}
			}

			// jeder Vectoreintrag ist eine Zeile der Ausgabe
			for (int i = 0; i < v.size(); i++)
			{
				String s[] = v.elementAt(i);
				for (int k = 0; k < numberOfWeights + 1; k++)
				{
					int temp = s[k].length();
					if (k == 0)
					{
						sOutput.append(s[k]);
					}
					for (int n = 0; n < (columnLength[k] - temp); n++)
					{
						sOutput.append(" ");
					}
					if (k != 0)
					{
						sOutput.append("  " + s[k]);
					}
				}
				sOutput.append(" \n");
			}
		}
		else
		{ // Vertikale Ausgabe
			// String mit maximaler Länge in jeder Spalte suchen
			int columnLength[] = new int[v.size()];
			for (int m = 0; m < v.size(); m++)
			{
				columnLength[m] = 0;
			}
			for (int i = 0; i < v.size(); i++)
			{
				String s[] = v.elementAt(i);
				for (int k = 0; k < numberOfWeights + 1; k++)
				{
					int temp = s[k].length();
					if (temp > columnLength[i])
					{
						columnLength[i] = temp;
					}
				}
			}
			// jeder Vectoreintrag ist eine Spalte der Ausgabe
			for (int k = 0; k < numberOfWeights + 1; k++)
			{
				for (int i = 0; i < v.size(); i++)
				{
					String s[] = v.elementAt(i);
					int temp = s[k].length();
					if (i == 0)
					{
						sOutput.append(s[k]);
					}
					for (int n = 0; n < (columnLength[i] - temp); n++)
					{
						sOutput.append(" ");
					}
					if (i != 0)
					{
						sOutput.append("  " + s[k]);
					}
				}
				sOutput.append(" \n");
			}
		}
		return sOutput.toString();
	}


	public static String[] getQuotientenspalte(Weight[] wa, double divisor, MethodData d, AbstractInputData data)
	{
		Signpost sp = getSignpost(d);

		String[] out = new String[wa.length + 3];
		out[0] = Resource.getString("bazi.gui.output.quotient");

		for (int i = 0; i < wa.length; i++)
		{
			String base = data.BMM ? data.base + RoundFrame.BASE_SEPERATOR : "";
			out[i + 1] = base + outputRoundQuotient(wa[i].weight / divisor, sp);
			if (wa[i].weight / divisor < sp.s(wa[i].rdWeight - 1)
					|| wa[i].weight / divisor > sp.s(wa[i].rdWeight))
				out[i + 1] += "*";
		}

		String div = Convert.doubleToString(divisor);
		out[wa.length + 1] = String.format("(%s)", div);
		out[wa.length + 2] = "";
		return out;
	}

	public static String getQuotientenFeld(double weight, int rd, double divisor, MethodData d)
	{
		Signpost sp = getSignpost(d);
		String s = outputRoundQuotient(weight / divisor, sp) + "";
		if (weight / divisor < sp.s(rd - 1) || weight / divisor > sp.s(rd))
			s += "*";
		return s;
	}

	public static Signpost getSignpost(MethodData d)
	{
		Signpost sp = null;
		try
		{
			if (d.method == MethodData.RSTATION)
			{
				if (d.paramString != null)
				{
					sp = new ExtendedStationary(d.paramString);
				}
				else
				{
					sp = new Stationary(d.param);
				}

			}
			else if (d.method == MethodData.PMEAN)
			{
				if (d.paramString != null)
				{
					sp = new ExtendedPowerMean(d.paramString);
				}
				else
				{
					sp = new PowerMean(d.param);
				}
			}
			else
				sp = new Stationary(0.5);
		}
		catch (ParameterOutOfRangeException ooREx)
		{}

		return sp;
	}

	public static class RString
	{

		public final String value;

		public RString(String value)
		{
			while (value.startsWith("0"))
				value = value.substring(1);
			if (value.indexOf(".") == -1)
				value += ".0";
			else
				while (value.endsWith("0"))
					value = value.substring(0, value.length() - 1);
			if (value.indexOf(".") == value.length() - 1)
				value += "0";
			if (value.indexOf(".") == 0)
				value = "0" + value;
			this.value = value;
		}

		public RString(double value)
		{
			this(new BigDecimal(value).toString());
		}

		public RString(char[] value)
		{
			this(new String(value));
		}

		private int pre()
		{
			return value.indexOf(".");
		}

		private int post()
		{
			return value.length() - value.indexOf(".") - 1;
		}

		private String normalize(int pre, int post)
		{
			String out = value;
			for (int i = 0; i < pre - pre(); i++)
				out = "0" + out;
			for (int i = 0; i < post - post(); i++)
				out += "0";
			return out.replace(".", "");
		}

		public int compare(RString o)
		{
			int pre = Math.max(pre(), o.pre());
			int post = Math.max(post(), o.post());
			String s0 = normalize(pre, post), s1 = o.normalize(pre, post);

			for (int i = 0; i < s0.length(); i++)
			{
				int diff = s0.charAt(i) - s1.charAt(i);
				if (diff < 0)
					return -1;
				if (diff > 0)
					return 1;
			}
			return 0;
		}

		public boolean equals(RString o)
		{
			return compare(o) == 0;
		}

		public RString round(int digits)
		{
			if (post() <= digits)
				return this;

			int p = value.indexOf(".");
			char[] out = value.substring(0, p + digits + 1).toCharArray();

			if (value.charAt(p + digits + 1) > '4')
			{
				int i = digits;
				if (i == 0)
					i = -1;
				while (out[p + i] == '9')
				{
					out[p + i] = '0';
					i--;
					if (i == 0)
						i = -1;
					if (p + i < 0)
					{
						out = ("0" + new String(out)).toCharArray();
						i++;
					}
				}
				out[p + i]++;
			}
			return new RString(out);
		}

		public RString floor(int digits)
		{
			if (value.indexOf(".") + digits + 1 < value.length())
				return new RString(value.substring(0, value.indexOf(".") + digits + 1));
			return this;
		}

		public RString ceil(int digits)
		{
			char[] temp = value.toCharArray();
			if (value.indexOf(".") + digits + 1 < value.length())
				temp[value.indexOf(".") + digits + 1] = '9';
			return new RString(temp).round(digits);
		}

		public RString rest()
		{
			return new RString(value.substring(value.indexOf(".")));
		}

		public double toDouble()
		{
			return Double.parseDouble(value);
		}

		@Override
		public String toString()
		{
			return value;
		}

		public String pad(int digits)
		{
			String s = value;
			for (int i = 0; i < digits - post(); i++)
				s += "0";
			return s;
		}

		public String without0()
		{
			if (value.startsWith("0"))
				return value.substring(1);
			return value;
		}

		public RString withRest(RString rest)
		{
			String s0 = value.substring(0, pre());
			String s1 = rest.toString();
			s1 = s1.substring(Math.min(rest.pre() + 1, s1.length()));
			return new RString(s0 + "." + s1);
		}
	}

	public static String[] getQuotientenspalteOR(Weight[] wa, double quota, MethodData d, AbstractInputData data)
	{
		boolean WTA = isWTA(d);
		boolean gR1 = !WTA && isGR1(d);
		RString one = new RString(1);

		String[] out = new String[wa.length + 3];
		out[0] = Resource.getString("bazi.gui.output.quotient");

		RString[] q = new RString[wa.length];
		for (int i = 0; i < wa.length; i++)
			q[i] = new RString(wa[i].weight / quota);
		if (q.length == 1)
			q[0] = new RString(wa[0].rdWeight);

		RString nice;
		if (WTA)
			nice = one;
		else
			nice = getSplit(wa, quota, gR1);

		int nicedigits = 3;
		for (int i = 0; i < wa.length; i++)
			if (wa[i].multiple.isEmpty())
			{
				nicedigits = niceDigits(q[i],nice);
			}
		nice = nice.round(nicedigits);

		for (int i = 0; i < wa.length; i++)
		{
			String base = data.BMM ? data.base + RoundFrame.BASE_SEPERATOR : "";
			RString rest = q[i].rest(), round;

			if (WTA || (gR1 && q[i].compare(one) < 0))
			{
				int digits = 3;
				do
				{
					round = rest.round(digits);
					digits++;
				}
				while (round.equals(one));
				round = q[i].withRest(round);
			}
			else if (!wa[i].multiple.isEmpty())
			{
				round = q[i].withRest(nice);
			}
			else
			{
				int digits = 3, correct = rest.compare(nice);
				do
				{
					round = rest.round(digits);
					digits++;
				}
				while (round.compare(nice) != correct || round.equals(one));
				round = q[i].withRest(round);
			}
			out[i + 1] = base + round.pad(3) 
			+  (isPragmaticModifcation(wa[i].min,new RString(wa[i].max),nicedigits ,gR1, q[i], nice)?"*":"");
		}

		if (WTA)
			out[wa.length + 1] = "(NA)";
		else
		{
			String s = nice + "";
			s = s.substring(s.indexOf("."));
			out[wa.length + 1] = String.format("(%s)", s);
		}

		out[wa.length + 2] = "";
		return out;
	}
	
	public static int niceDigits(RString q, RString nice) 
	{
		int nicedigits = 3;
		RString rest = q.rest();
		int correct = rest.compare(nice);
		while (rest.compare(nice.round(nicedigits)) != correct)
			nicedigits++;
		return nicedigits;
	}
	
	
	/** Prüft, ob die berechneten Sitze wegen einer Minimal- oder Maximalbedingung
	 *  angepasst werden mussten (pragmatic modification)
	 * 
	 * @param min         Sitze, die wegen Minimlabed. mindestens zugeteilt werden müssen
	 * @param max         Sitze, die höchstens zugeteilt werden dürfen
	 * @param niceDigits  Dezimalstellen, auf die die ungerundeten Sitzansprueche
	 *                    gerundet werden muessen
	 * @param gR1         Flag, das angibt, ob die Methode mind. einen Sitz zuordnet
	 * @param q           ungerundete Sitzansprueche aller Listen
	 * @param nice        "schoener" Splitwert mit moeglichst wenig Dezimalstellen  */
	public static Boolean isPragmaticModifcation( double min, RString max, int niceDigits, boolean gR1, 
										          RString q, RString nice )
	{
		RString one = new RString(1);	
		if (gR1 && q.compare(one) < 0) 
			return true;
		else 
		{
			min -= 1;
			RString seats = q.round(niceDigits);
			RString helpseats = new RString(min + nice.toDouble());
			
			if(seats.compare(helpseats.round(niceDigits)) < 0 || (seats.compare(max) >= 0)) 
				return true;
		}
		return false;
	}

	public static boolean isWTA(MethodData d)
	{
		double method = d.param;
		return Method.isMethod(method, Method.DROOPQUOTE_VAR02)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR12)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR22)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR32)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR02)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR12)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR22);
	}

	public static boolean isGR1(MethodData d)
	{
		double method = d.param;
		return Method.isMethod(method, Method.DROOPQUOTE_VAR01)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR11)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR21)
				|| Method.isMethod(method, Method.DROOPQUOTE_VAR31)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR01)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR11)
				|| Method.isMethod(method, Method.HAREQUOTE_VAR21);
	}

	public static RString getSplit(Weight[] wa, double quota, boolean gR1)
	{
		RString[] iv = getSplitInterval(wa, quota, gR1);
		RString low = iv[0], high = iv[1];
		RString mid = new RString(0.5);

		if (low.compare(mid) < 0 && high.compare(mid) > 0)
			return mid;
		else
		{
			Divisor div = new Divisor();
			try
			{
				div.setDivisorInterval(low.toDouble(), high.toDouble());
				return new RString(div.getDivisor());
			}
			catch (DivisorException e)
			{}
			return low;
		}
	}

	public static String getSplitString(Weight[] wa, double quota, MethodData d)
	{
		if (isWTA(d))
			return "NA";
		return String.format("%s", getSplit(wa, quota, isGR1(d)).round(3).without0());
	}
	
	/** Gibt die Variablen als array formatiert für die Methode getSplitInterval zurück
	 * @param w Gewicht für eine Partei
	 * @param quota Quote*/
	private static RString[] set(Weight w, double quota) {
		RString q = new RString(w.weight / quota);
		return new RString[] {q.floor(0), q.rest(), new RString(w.max), new RString(w.min), new RString(w.rdWeight)};
	}
	
	public static RString[] getSplitInterval(Weight[] wa, double quota, boolean gR1)
	{
		RString low = new RString(0), high = new RString(1);
		RString n = new RString(0);
		// erstmal pruefen, ob auch ohne Anpassung durch Minimalbedingung schon
		// eine korrekte Sitzzuteilung raus kommt
		for (Weight w : wa)
		{
			RString[] set = set(w, quota);
			if (gR1 && set[0].equals(n))
				continue;
			if (set[0].equals(set[4]) && set[2].compare(set[4]) > 0 && low.compare(set[1]) < 0)
				low = set[1];
			if (set[0].compare(set[4]) < 0 && high.compare(set[1]) > 0)
				high = set[1];
		}
		
		if (low.compare(high) <= 0)
			// prima, das passt auch ohne Korrektur wg. Minimalbedingung
			return new RString[] { low, high };

		// hat leider nicht geklappt; jetzt unter Berücksichtigung der Minimalbedingung
		// wieder auf Start
		low =  new RString(0); high = new RString(1);
		n = new RString(0);
		for (Weight w : wa)
		{
			RString[] set = set(w, quota);
			if (gR1 && set[0].equals(n))
				continue;
			if (set[0].equals(set[4]) && set[2].compare(set[4]) > 0 && low.compare(set[1]) < 0)
				low = set[1];
			if (set[0].compare(set[4]) < 0 && set[3].compare(set[4]) < 0 && high.compare(set[1]) > 0)
				high = set[1];
		}
		if (low.compare(high) > 0)
			low = high;
		return new RString[] { low, high };
	}

	public static String getSplitIntervalString(Weight[] wa, double quota, MethodData d)
	{
		if (isWTA(d))
			return "NA";
		RString[] iv = getSplitInterval(wa, quota, isGR1(d));
		RString low = iv[0], high = iv[1], rLow, rHigh;
		if (low.equals(high))
			return String.format("[%s;%s]", low.round(3).without0(), high.round(3).without0());
		int digits = 3;
		do
		{
			rLow = low.ceil(digits);
			rHigh = high.floor(digits);
			digits++;
		}
		while (rLow.compare(rHigh) >= 0);
		return String.format("[%s;%s]", rLow.without0(), rHigh.without0());
	}

	private static String outputRoundQuotient(double q, Signpost sp)
	{
		double s1 = sp.s((int) q - 1), s2 = sp.s((int) q), s3 = sp.s((int) q + 1);
		int stellen = 1;
		double faktor = 10.0;
		double x = q - (int) q;
		int max = Double.toString(x).length() - 1;
		double gerundet = 0;

		while (stellen <= max
				&& ((gerundet = Math.round(q * faktor) / faktor) == (Math.round(s1 * faktor) / faktor)
						|| (gerundet == (Math.round(s2 * faktor) / faktor))
						|| (gerundet == (Math.round(s3 * faktor) / faktor))))
		{
			stellen++;
			faktor *= 10;
		}

		return Double.toString((gerundet));
	}
}
