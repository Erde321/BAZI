/*
 * @(#)InputData.java 4.2 07/05/14
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.driver;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.gui.RoundFrame;
import de.uni.augsburg.bazi.gui.WeightsTable;
import de.uni.augsburg.bazi.lib.Divisor;
import de.uni.augsburg.bazi.lib.MethodListener;
import de.uni.augsburg.bazi.lib.Rounding;
import de.uni.augsburg.bazi.lib.Weight;

/** <b>Überschrift:</b> Klasse InputData<br>
 * <b>Beschreibung:</b> Eingabedaten für einen Distrikt<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 4.2
 * @author Florian Kluge, Christian Brand, Marco Schumacher */
public class InputData extends AbstractInputData
{

	/** Return Wert falls Min-To-Max Bedingung ausgewaehlt wurde */
	private static final int RETURN_VALUE = 1337;

	/** Die ursprünglichen Gewichte */
	public Weight[] originalWeights;

	/** Feld mit den Daten für alle Gewichte.
	 * Indizes: [Accuracy][Method][Power][Weightindex] */
	public Weight[][][][] weights;

	/** Feld mit den Potenzen bei base+min..max(pow)
	 * Indizes: [Accuracy][Method] */
	public double[][][] powers;

	/** Feld mit den Daten für alle Listebverbindungen. */
	public ListInputData[] listData = null;

	/** Summe der Gewichte. */
	public double totalWeight;

	/** Summe der Werte der entsprechenden Nebenbedingung. */
	public int totalCondition;

	/** Feld mit den zu ermittelten Genauigkeiten (Mandate). */
	public int[] accuracies;

	/** String für die Accuracies aus einer eingelesenen Datei */
	public String accuracy;

	/** Feld zur Sicherung der berechneten Zuteilungen save[methodIndex][weightIndex] */
	public Weight[][] save;

	/** Feld zur Sicherung der Divisoren divSave[methodIndex] */
	public Divisor[] divSave;

	/** Gibt an, ob die entsprechende Berechnung erfolgreich war
	 * apValid[methodIndex] */
	public boolean[] apValid;

	/** Das Messengerobjekt für diesen Distrikt, wird in der separaten Zuteilung
	 * benötigt */
	public Messenger msgSave;

	/** Districtname */
	public String district;

	/** Vector mit den Namen (nur für FileIO) */
	public Vector<String> vNames;

	/** Vector mit den Gewichten */
	public Vector<Number> vWeights;

	/** Vector mit den Nebenbedingungen */
	public Vector<String> vCond;

	/** RoundFrame zur evtl. Fehlerausgabe */
	public RoundFrame rf = RoundFrame.getRoundFrame();

	/** MethodListener fuer Fehlerausgabe */
	public MethodListener ml;

	/** Standardkonstruktor. Erzeugt ein leeres Objekt */
	public InputData()
	{}

	/** Für die Verwendung in FileIO. Erzeugt ein neues leeres Objekt und übernimmt
	 * nur den Titel und das Ausgabeformat aus dem übergebenen Parameter.
	 * 
	 * @param id InputData */
	public InputData(InputData id)
	{
		title = id.title;
		outputFormat = id.outputFormat;
	}

	/** Konstruktur, der von der GUI aufgerufen werden sollte. Fehlermeldungen werden dann ueber diese
	 * Ausgabemedien gesteuert.
	 * 
	 * @param rf: RoundFrame
	 * @param ml: MethodListener(DebugDialog) */
	public InputData(RoundFrame rf, MethodListener ml)
	{
		this.rf = rf;
		this.ml = ml;
	}

	public int weightcount()
	{
		if (weights != null && weights[0] != null && weights[0][0] != null && weights[0][0][0] != null)
			return weights[0][0][0].length;
		return 0;
	}

	/** Bereitet das Objekt für die Verwendung bei einer Berechnung vor.
	 * 
	 * @return <code>true</code> wenn das Aufbereiten erfolgreich war */
	public boolean prepareData()
	{
		return this.parseInputData(vNames, vWeights, vCond, title, accuracy, outputFormat, false, false, 0, 0, Integer.MAX_VALUE, 0);
	}

	/** Bereitet das Objekt für die Verwendung bei einer Berechnung vor.
	 * 
	 * @param BMM gibt an, ob Base+Min..Max verwendet wird
	 * @param pow gibt an, ob Base+Min..Max(Pow) verwender wird
	 * @param base Anzal der Basis Sitze
	 * @param min Minimale Sitzzahl
	 * @param max Maximale Sitzzahl
	 * @param minPlusValue Prozent, die zum Minimum einer Listenverbindung addiert wird
	 * @return <code>true</code> wenn das Aufbereiten erfolgreich war */
	public boolean prepareData(boolean BMM, boolean pow, int base, int min, int max, double minPlusValue)
	{
		return this.parseInputData(vNames, vWeights, vCond, title, accuracy, outputFormat, BMM, pow, base, min, max, minPlusValue);
	}

	/** Methode zum Einlesen der GUI-Tabelle. Die Werte werden in dieses InputData Objekt konvertiert!
	 * 
	 * @param wTable: der WeightsTable(enthält Name, Gewicht, Nebenbedingung)
	 * @param dTitle: der TitelString
	 * @param sAcc: der String mit den Accuracies
	 * @param of: das Objekt mit den AusgabeParametern
	 * @return <b>true</b>, wenn parsen erfolgreich
	 *         <b>false</b>, wenn Fehler beim parsen */
	public boolean parseInputDataForDistrict(WeightsTable wTable, String dTitle, String sAcc, OutputFormat of,
			boolean BMM, boolean pow, int base, int min, int max, double minPlusValue)
	{
		int[] accuracies = parseAccuracy(sAcc);
		if (accuracies == null || accuracies.length > 1)
		{
			if (ml != null)
				ml.printMessage("Fehler beim Einlesen von Accuracy");
			if (rf != null)
				rf.errorAccuracy();
			return false;
		}
		return parseInputData(wTable, dTitle, sAcc, of, BMM, pow, base, min, max, minPlusValue);
	}

	/** Methode zum Einlesen der GUI-Tabelle. Die Werte werden in dieses InputData Objekt konvertiert!
	 * 
	 * @param wTable: der WeightsTable(enthält Name, Gewicht, Nebenbedingung)
	 * @param dTitle: der TitelString
	 * @param sAcc: der String mit den Accuracies
	 * @param of: das Objekt mit den AusgabeParametern
	 * @return <b>true</b>, wenn parsen erfolgreich
	 *         <b>false</b>, wenn Fehler beim parsen */
	public boolean parseInputData(WeightsTable wTable, String dTitle, String sAcc, OutputFormat of,
			boolean BMM, boolean pow, int base, int min, int max, double minPlusValue)
	{
		/** Vektor mit Parteinamen */
		Vector<String> names = wTable.getColumnValuesAsStrings(0);
		/** Vektor mit Gewichten(Stimmenanzahl) */
		Vector<Number> weights = wTable.getColumnValuesAsNumbers(1);
		/** Vektor mit Werten der 3en Spalte */
		Vector<String> cond = wTable.getColumnValuesAsStrings(2);
		totalWeight = wTable.getTotal();
		totalCondition = wTable.getMinTotal();
		return parseInputData(names, weights, cond, dTitle, sAcc, of, BMM, pow, base, min, max, minPlusValue);
	}

	/** Methode zum Einlesen der Daten. Diese werden in dieses Objekt von InputData konvertiert
	 * 
	 * @param names: Vektor mit den Namen
	 * @param weights: Vektor mit den Gewichten
	 * @param cond: Vektor mit der Nebenbedingung
	 * @param dTitle: Titel der Zuteilung
	 * @param sAcc: String mit den Accuracies
	 * @param of: das Objekt mit den Ausgabeparametern
	 * @return <b>true</b>, wenn parsen erfolgreich
	 *         <b>false</b>, wenn Fehler beim parsen */
	public boolean parseInputData(Vector<String> names, Vector<Number> weights, Vector<String> cond, String dTitle, String sAcc, OutputFormat of,
			boolean BBM, boolean pow, int base, int min, int max, double minPlusValue)
	{
		/* Zuweisen der unformatierten Objekte */
		title = dTitle;
		accuracy = sAcc;
		outputFormat = of;
		vNames = names;
		vWeights = weights;
		vCond = cond;
		BMM = BBM;
		this.pow = pow;
		this.base = base;
		this.min = min;
		this.max = max;
		this.minPlusValue = minPlusValue;
		/* Zunaechst wird Accuracies gelesen */
		int[] accuracies = parseAccuracy(sAcc);
		/* Wenn dort ein Fehler aufgetreten ist, wird null zurueck gegeben */
		if (accuracies == null)
		{
			if (ml != null)
				ml.printMessage("Fehler beim Einlesen von Accuracy");
			if (rf != null)
				rf.errorAccuracy();
			return false;
		}
		this.accuracies = accuracies;
		/* Überprüfen des Output-Formats */
		if (of == null)
		{
			if (ml != null)
				ml.printMessage("OutputFormat = null");
			return false;
		}
		if (of.methods == null)
		{
			if (ml != null)
				ml.printMessage("OutputFormat.methods = null");
			return false;
		}
		/* Output Format ist in Ordnung */
		outputFormat.prepare();
		outputFormat.labelTotal = Resource.getString("bazi.gui.table.sum");

		/* Vektor mit allen Gewichten */
		Vector<Weight> vWeights = new Vector<Weight>();
		/* Vektor mit den ListenGewichten */
		Vector<Weight> vListWeights = null;
		/* Vektor mit den Problemen fuer die Unterzuteilungen */
		Vector<ListInputData> vListData = new Vector<ListInputData>();
		/* Hilfsvariable */
		Weight tempWeight = null;

		for (int i = 0; i < this.vWeights.size(); i++)
		{
			/* Name der Partei */
			String name = vNames.elementAt(i);
			/* Fall: gueltige Listenverbindung */
			if (name.startsWith("+") && vWeights.size() > 0)
			{
				Weight parentWeight = vWeights.lastElement();
				vListWeights = new Vector<Weight>();

				parentWeight.parent = true;

				boolean vereinfachterListenName = false;
				if (parentWeight.weight > 0d)
				{
					vListWeights.add(parentWeight.clonew());
					parentWeight.name = "'" + parentWeight.name + "'";
				}
				else
				{
					vereinfachterListenName = true;
				}
				for (; i < vNames.size() && vNames.elementAt(i).startsWith("+"); i++)
				{
					String helpName = vNames.elementAt(i).substring(1);

					boolean indep = false;
					if (helpName.toLowerCase().startsWith("indep"))
					{
						indep = true;
					}

					if (!vereinfachterListenName)
					{
						parentWeight.name += " + '" + helpName + "'";
					}
					tempWeight = new Weight();
					tempWeight.name = helpName;
					Number helpObj = this.vWeights.elementAt(i);
					double helpValue = 0;
					if (helpObj == null || helpObj instanceof Double || helpObj instanceof Integer)
					{
						if (helpObj instanceof Double)
						{
							helpValue = ((Double) helpObj).doubleValue();
						}
						else if (helpObj instanceof Integer)
						{
							helpValue = ((Integer) helpObj).doubleValue();
						}
					}
					else
					{
						if (ml != null)
							ml.printMessage((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
						errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
						return false;
					}
					if (helpValue < 0)
					{
						if (ml != null)
							ml.printMessage("Negatives Gewicht in Zeile #" + (i + 1));
						errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
						return false;
					}
					// parentWeight.weight += helpValue;
					tempWeight.weight = helpValue;
					/* Wert von Spalte 3 */
					int helpCond = parseCondValue(vCond.elementAt(i), of);
					if (of.condition != OutputFormat.CONDITION_NONE & helpCond < 0)
					{
						if (ml != null)
							ml.printMessage("Fehlerhafter Eintrag in Spalte 3 Zeile #" + (i + 1));
						errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
						return false;
					}
					if (of.condition != OutputFormat.CONDITION_NONE)
					{
						if (of.condition == OutputFormat.CONDITION_DIRECT)
						{
							parentWeight.direct += helpCond;
							tempWeight.direct = helpCond;
						}
						else if (of.condition == OutputFormat.CONDITION_NAIV)
						{
							parentWeight.naiv += helpCond;
							tempWeight.naiv = helpCond;
						}
						else if (of.condition == OutputFormat.CONDITION_MIN ||
								of.condition == OutputFormat.CONDITION_SUPER_MIN ||
								of.condition == OutputFormat.CONDITION_MIN_PLUS ||
								of.condition == OutputFormat.CONDITION_MIN_VPV)
						{
							parentWeight.min += helpCond;
							tempWeight.min = helpCond;
						}
						else if (of.condition == OutputFormat.CONDITION_MAX)
						{

							if (indep)
							{
								helpCond = 1;
							}

							if (parentWeight.max < Integer.MAX_VALUE)
							{
								if (helpCond < Integer.MAX_VALUE)
								{
									parentWeight.max += helpCond;
								}
								else
								{
									parentWeight.max = Integer.MAX_VALUE;
								}
							}
							tempWeight.max = helpCond;
						}
						else if (of.condition == OutputFormat.CONDITION_MIN_TO_MAX && helpCond == InputData.RETURN_VALUE)
						{
							String text = vCond.elementAt(i);
							if (text == null || text.toLowerCase().equals("null") || text.length() == 0)
							{
								text = 0 + RoundFrame.RANGE_SEPERATOR + "oo";
							}
							int index = text.indexOf(RoundFrame.RANGE_SEPERATOR);
							try
							{
								int to, from;
								if (index == 0)
								{
									from = 0;
								}
								else
								{
									from = Integer.parseInt(text.substring(0, index).trim());
								}
								String temp = text.substring(index + RoundFrame.RANGE_SEPERATOR.length()).trim();
								if (temp.length() == 0 || temp.equals("oo"))
								{
									to = Integer.MAX_VALUE;
								}
								else
								{
									to = Integer.parseInt(text.substring(index + RoundFrame.RANGE_SEPERATOR.length()).trim());
								}

								if (indep)
								{
									to = 1;
								}

								parentWeight.min += from;
								tempWeight.min = from;
								if (parentWeight.max < Integer.MAX_VALUE)
								{
									if (to < Integer.MAX_VALUE)
									{
										parentWeight.max += to;
									}
									else
									{
										parentWeight.max = Integer.MAX_VALUE;
									}
								}
								tempWeight.max = to;
							}
							catch (Exception e)
							{
								if (ml != null)
								{
									ml.printMessage("Fehler in Condition Min_to_Max in 2. Anlauf\nThis shouldn't happen!");
								}
								return false;
							}
						}
						else
						{
							if (ml != null)
							{
								ml.printMessage("Nicht vorhandenen Bedingung ausgewählt!");
							}
							errorInInput("Bedingung nicht vorhanden! (Dies sollte nicht passieren)");
							return false;
						}
					}
					if (tempWeight.weight == 0 && tempWeight.min > 0)
					{
						if (ml != null)
						{
							ml.printMessage("Gewicht = 0 und MinBedingung > 0 bei " + tempWeight.name);
						}
						errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
						return false;
					}

					if (indep)
					{
						tempWeight.max = 1;
						if (parentWeight.max < Integer.MAX_VALUE)
						{
							parentWeight.max += 1;
						}
					}
					vListWeights.add(tempWeight);
				}

				int decimal_places = 0;
				parentWeight.weight = 0;
				for (Weight w : vListWeights)
				{
					parentWeight.weight += w.weight;
					decimal_places = Math.max(decimal_places, Rounding.getDecimal_places(w.weight));
				}
				parentWeight.weight = Rounding.round(parentWeight.weight, decimal_places);
				of.decimal_places = Math.max(of.decimal_places, Rounding.getDecimal_places(parentWeight.weight));

				OutputFormat lof = of.cloneOF();
				lof.decimal_places = decimal_places;
				if (of.condition == OutputFormat.CONDITION_MIN_PLUS
						|| of.condition == OutputFormat.CONDITION_MIN_VPV)
				{
					lof.condition = OutputFormat.CONDITION_MIN;
					lof.labelCondition = Resource.getString("bazi.gui.table.minimum");
				}
				lof.methods = new MethodData[accuracies.length * of.methods.length];
				ListInputData lip = new ListInputData();
				lip.parentWeight = parentWeight;
				lip.parentIndex = vWeights.indexOf(parentWeight);
				lip.outputFormat = lof;

				lip.weights = new Weight[accuracies.length * of.methods.length][1][1][vListWeights.size()];
				for (int a = 0; a < accuracies.length * of.methods.length; a++)
					for (int c = 0; c < vListWeights.size(); c++)
					{
						lip.weights[a][0][0][c] = vListWeights.elementAt(c).clonew();
					}
				lip.originalWeights = new Weight[vListWeights.size()];
				vListWeights.toArray(lip.originalWeights);

				lip.powers = new double[accuracies.length * of.methods.length][1][1];

				lip.title = Resource.getString("bazi.gui.output.partylist") + " \"" + parentWeight.name + "\"";
				lip.totalWeight = parentWeight.weight;
				lip.accuracies = new int[accuracies.length * of.methods.length];
				lip.save = new Weight[lof.methods.length][vListWeights.size()];
				lip.divSave = new Divisor[lof.methods.length];
				vListData.add(lip);
			}
			/* keine Listenverbindung */
			if (i < this.vWeights.size())
			{
				name = vNames.elementAt(i);
				boolean indep = false;
				if (name.toLowerCase().startsWith("indep"))
				{
					indep = true;
				}
				tempWeight = new Weight();
				tempWeight.name = name;
				if (BMM)
				{
					tempWeight.min = min;
					if (!pow)
						tempWeight.max = max;
				}
				Number helpObj = this.vWeights.elementAt(i);
				double helpValue = 0;
				if (helpObj == null || helpObj instanceof Double || helpObj instanceof Integer)
				{
					if (helpObj instanceof Double)
					{
						helpValue = ((Double) helpObj).doubleValue();
					}
					else if (helpObj instanceof Integer)
					{
						helpValue = ((Integer) helpObj).doubleValue();
					}
				}
				else
				{
					if (ml != null)
						ml.printMessage(Resource.getString("bazi.error.input.bad_Weight") + (i + 1));
					errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
					return false;
				}
				if (helpValue < 0)
				{
					if (ml != null)
						ml.printMessage("Negatives Gewicht in Zeile #" + (i + 1));
					errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
					return false;
				}
				tempWeight.weight = helpValue;
				int helpCond = parseCondValue(vCond.elementAt(i), of);
				if (of.condition != OutputFormat.CONDITION_NONE & helpCond < 0)
				{
					if (ml != null)
						ml.printMessage("Fehlerhafter Eintrag in Spalte 3 Zeile #" + (i + 1));
					if (vNames.elementAt(i) != null)
					{
						errorInInput((i + 1) + ", Eintrag \"" + vNames.elementAt(i) + "\":\n" + Resource.getString("bazi.error.input.description"));
					}
					else
					{
						errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
					}
					return false;
				}
				if (of.condition != OutputFormat.CONDITION_NONE)
				{
					if (of.condition == OutputFormat.CONDITION_DIRECT)
					{
						tempWeight.direct = helpCond;
					}
					else if (of.condition == OutputFormat.CONDITION_NAIV)
					{
						tempWeight.naiv = helpCond;
					}
					else if (of.condition == OutputFormat.CONDITION_MIN ||
							of.condition == OutputFormat.CONDITION_SUPER_MIN ||
							of.condition == OutputFormat.CONDITION_MIN_PLUS ||
							of.condition == OutputFormat.CONDITION_MIN_VPV)
					{
						tempWeight.min = helpCond;
					}
					else if (of.condition == OutputFormat.CONDITION_MAX)
					{
						tempWeight.max = helpCond;
					}
					else if (of.condition == OutputFormat.CONDITION_MIN_TO_MAX && helpCond == InputData.RETURN_VALUE)
					{
						String text = vCond.elementAt(i);
						if (text == null || text.toLowerCase().equals("null") || text.length() == 0)
						{
							text = 0 + RoundFrame.RANGE_SEPERATOR + "oo";
						}
						int index = text.indexOf(RoundFrame.RANGE_SEPERATOR);
						try
						{
							int to, from;
							if (index == 0)
							{
								from = 0;
							}
							else
							{
								from = Integer.parseInt(text.substring(0, index).trim());
							}
							String temp = text.substring(index + RoundFrame.RANGE_SEPERATOR.length()).trim();
							if (temp.length() == 0 || temp.trim().equals("oo"))
							{
								to = Integer.MAX_VALUE;
							}
							else
							{
								to = Integer.parseInt(text.substring(index + RoundFrame.RANGE_SEPERATOR.length()).trim());
							}
							tempWeight.min = from;
							tempWeight.max = to;
						}
						catch (Exception e)
						{
							if (ml != null)
								ml.printMessage("Fehler in Condition Min_to_Max in 2. Anlauf\nThis shouldn't happen!");
							return false;
						}
					}
				}
				if (indep)
				{
					tempWeight.max = 1;
				}
				if (tempWeight.weight == 0 && tempWeight.min > 0 && !(of.condition == OutputFormat.CONDITION_SUPER_MIN))
				// && !(i + 1 < vNames.size() && vNames.get(i+1).startsWith("+")))
				{
					if (ml != null)
					{
						ml.printMessage("Gewicht = 0 und MinBedingung > 0 bei " + tempWeight.name);
					}
					errorInInput((i + 1) + ":\n" + Resource.getString("bazi.error.input.description"));
					return false;
				}
				vWeights.add(tempWeight);
				of.decimal_places = Math.max(of.decimal_places, Rounding.getDecimal_places(tempWeight.weight));
			}
		}

		// Überprüfung ob eine Liste insgesamt 0 Sitze hat aber im Listenkopf Min angegeben wurde
		for (ListInputData lid : vListData)
		{
			double ges = 0;
			for (Weight w : lid.originalWeights)
				ges += w.weight;
			if (ges == 0 && vWeights.get(lid.parentIndex).min > 0)
			{
				if (ml != null)
				{
					ml.printMessage("Listengewicht = 0 und MinBedingung > 0 bei " + vWeights.get(lid.parentIndex).name);
				}
				errorInInput("'" + vWeights.get(lid.parentIndex).name + "':\n" + Resource.getString("bazi.error.input.description"));
				return false;
			}
		}

		// Die Basismandate werden von den Gesamtsitzen für die Berechnung abgezogen
		// und bei der Ausgabe wieder eingefügt
		int base_sum = vWeights.size() * base;
		for (int i = 0; i < this.accuracies.length; i++)
			this.accuracies[i] -= base_sum;

		this.weights = new Weight[accuracies.length][of.methods.length][1][vWeights.size()];
		for (int a = 0; a < accuracies.length; a++)
			for (int b = 0; b < of.methods.length; b++)
				for (int c = 0; c < vWeights.size(); c++)
				{
					this.weights[a][b][0][c] = vWeights.elementAt(c).clonew();
				}

		originalWeights = new Weight[vWeights.size()];
		vWeights.toArray(originalWeights);

		powers = new double[accuracies.length][of.methods.length][1];

		listData = new ListInputData[vListData.size()];
		vListData.toArray(listData);
		save = new Weight[of.methods.length][vWeights.size()];
		divSave = new Divisor[of.methods.length];
		apValid = new boolean[of.methods.length];

		return true;
	} // end readInputData

	/** Methode zum Einlesen des Accuracy-Strings.<br>
	 * Dieser wird in ein Feld mit Int-Werten umgewandelt.
	 * 
	 * @param temp: String mit den Accuracies
	 * @return <b>int[]</b>:Feld mit den geparsten Werten<br>
	 *         <b>null</b>: wenn ein Fehler aufgetreten ist */
	private int[] parseAccuracy(String temp)
	{
		if (temp == null || temp.length() == 0)
		{
			return null;
		}
		Vector<Integer> v = new Vector<Integer>();
		temp = temp.replace(',', ';');
		temp = temp.replace(' ', ';');
		if (temp.lastIndexOf(";") == temp.length() - 1)
		{
			temp = temp.substring(0, temp.length() - 1);
		}
		StringTokenizer st = new StringTokenizer(temp, ";");
		while (st.hasMoreTokens())
		{
			try
			{
				String s = st.nextToken().trim();
				int index = s.indexOf("-");
				if (index == -1)
				{
					index = s.indexOf(RoundFrame.RANGE_SEPERATOR);
					if (index == -1)
					{
						v.add(Integer.parseInt(s));
					}
					else
					{
						int first = Integer.parseInt(s.substring(0, index));
						int second = Integer.parseInt(s.substring(index + RoundFrame.RANGE_SEPERATOR.length()));
						if (first <= second)
						{
							for (int i = first; i <= second; i++)
							{
								v.add(i);
							}
						}
						else
						{
							for (int i = first; i >= second; i--)
							{
								v.add(i);
							}
						}
					}
				}
				else
				{
					int first = Integer.parseInt(s.substring(0, index));
					int second = Integer.parseInt(s.substring(index + 1));
					if (first <= second)
					{
						for (int i = first; i <= second; i++)
						{
							v.add(i);
						}
					}
					else
					{
						for (int i = first; i >= second; i--)
						{
							v.add(i);
						}
					}
				}
			}
			catch (NumberFormatException nfe)
			{
				return null;
			}
		}
		int[] accuracies = new int[v.size()];
		for (int i = 0; i < accuracies.length; i++)
		{
			accuracies[i] = v.elementAt(i);
		}
		return accuracies;
	} // end readAccuracy

	private void errorInInput(String error)
	{
		if (rf != null)
			JOptionPane.showMessageDialog(rf, Resource.getString("bazi.error.illegal_input") + " " + error, Resource.getString("bazi.error.attention"), JOptionPane.WARNING_MESSAGE);
	}

	/** Methode zum Parsen der Nebenbedingung in den richtigen Wert
	 * 
	 * @param text: String der Nebenbedingung
	 * @param of: OutputFormat
	 * @return <b>-1</b>, wenn Fehler in Berechnung<br>
	 *         <b>InputData.RETURN_VALUE</b>, wenn MIN_TO_MAX und Berechnung ok<br>
	 *         <b>Wert</b>, sonst */
	private int parseCondValue(String text, OutputFormat of)
	{
		if (of.condition == OutputFormat.CONDITION_MIN_TO_MAX)
		{
			if (text == null)
			{
				return -1;
			}
			int index = text.indexOf(RoundFrame.RANGE_SEPERATOR);
			if (index < 0)
			{
				if (text == null || text.equals("null") || text.length() == 0)
				{
					return InputData.RETURN_VALUE;
				}
				else
				{
					return -1;
				}
			}
			try
			{
				int to, from = Integer.parseInt(text.substring(0, index).trim());
				String temp = text.substring(index + RoundFrame.RANGE_SEPERATOR.length()).trim();
				if (temp.length() == 0 || temp.equals("oo"))
				{
					to = Integer.MAX_VALUE;
				}
				else
				{
					to = Integer.parseInt(temp);
				}
				if (from > to || from < 0 || to < 0)
				{
					return -1;
				}
				return InputData.RETURN_VALUE;
			}
			catch (NumberFormatException nfe)
			{
				return -1;
			}
		}
		else
		{
			if (text == null || text.length() == 0 || text.toLowerCase().equals("null"))
			{
				if (of.condition != OutputFormat.CONDITION_MAX)
				{
					return 0;
				}
				else
				{
					return Integer.MAX_VALUE;
				}
			}
			else
			{
				if (text.equals("oo"))
					return Integer.MAX_VALUE;
				try
				{
					return Integer.parseInt(text);
				}
				catch (NumberFormatException nfe)
				{
					return -1;
				}
			}
		}
	}

	/** Erzeugt eine String Repräsentation dieser Eingabedaten
	 * 
	 * @return Eingabedaten als String */
	@Override
	public String toString()
	{
		String tmp = title;
		tmp += " - d: " + district;
		tmp += " ML: " + outputFormat.methods.length;
		return tmp;
	}
}
