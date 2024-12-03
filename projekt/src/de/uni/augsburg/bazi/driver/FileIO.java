/*
 * @(#)FileIO.java 3.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */

package de.uni.augsburg.bazi.driver;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.gui.InfoFrame;
import de.uni.augsburg.bazi.gui.RoundFrame;

/** <b>Überschrift:</b> Klasse FileIO<br>
 * <b>Beschreibung:</b> Liest und schreibt Eingabedaten in Dateien im bazi-Format<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 3.1
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */
public class FileIO
{
	/** Aktuelle Zeile */
	private int rec_line = 0;

	/** Alle Zeilen als Array */
	private Vector<String> lines;

	/** Aktueller WarnAndErrorDialog */
	private WarnAndErrorDialog wed;

	/** Aktuelle Eingabedaten */
	private AbstractInputData aid;

	private boolean BMM = false, pow = false;

	private int base = 0, min = 0, max = Integer.MAX_VALUE;

	private double minPlus = 0;

	/** Info Fenster */
	public static InfoFrame info = null;

	/** Zum Schreiben und Einlesen verwendete Kodierung */
	public static String CHARSET;


	/** Standardkonstruktor. Erzeugt ein leeres Objekt. */
	public FileIO()
	{}

	/** Öffnet die übergebene Datei und liefert die Eingabedaten als Objekt zurück.
	 * 
	 * @param file Eine Datei im bazi-Format
	 * @return gelesene Eingabedaten */
	public AbstractInputData open(File file)
	{
		openFile(file);
		return aid;
	}

	/** Öffnet und liest die übergebene Datei und speichert die Eingabedaten im Feld aid.
	 * 
	 * @param file Datei im bazi-Format */
	private void openFile(File file)
	{
		try
		{
			CHARSET = "ISO-8859-1"; // immer zunaechst mit Standard ASCII-Kodierung öffnen
			InputStreamReader in = new InputStreamReader(new FileInputStream(file.
					getCanonicalFile()), CHARSET);
			BufferedReader data = new BufferedReader(in);

			// UTF-8-Variante
			while (data.ready())
			{
				String line = data.readLine().toLowerCase();
				
				if (line.indexOf(" ") >= 1) // Leerzeichen vorhanden und nicht an erster Stelle?
				{
					// alles vor dem ersten Whitespace (das ist das \s) als tag abspeichern
					
					String tag = line.split("\\s")[0].toLowerCase();
				    
				    if (tag.length() == 0)
				    	continue;
					
				    if ( tag.matches(Resource.getString("bazi.fileIORules.encoding"))
							&& ((line.indexOf("utf8") >= 0 || line.indexOf("utf-8") >= 0)))
					{	
						CHARSET = "UTF8";
						break;					
					}
				}
			}
			in.close();
			if (CHARSET.equals("ISO-8859-1"))
			{
				// keine UTF-8-Variante, noch auf alte Griechenlandvariante testen
				InputStreamReader in2 = new InputStreamReader(new FileInputStream(file.
						getCanonicalFile()), CHARSET);
				BufferedReader data2 = new BufferedReader(in2);
				
				while (data2.ready())
				{
					String line = data2.readLine().toLowerCase();
					if (line.indexOf("greek") >= 0 || line.indexOf("griechenland") >= 0)
					{	
						// Griechenland Speziallösung (veraltet, seit es eine UTF-8-Variante gibt)
						CHARSET = "ISO-8859-7";
							break;
					}
				}
				in2.close();
			}			
			
			// jetzt mit der richtigen Kodierung öffnen
			in = new InputStreamReader(new FileInputStream(file.
						getCanonicalFile()), CHARSET);
			
			data = new BufferedReader(in);

			rec_line = 0;
			aid = read(data);
			data.close();
			in.close();
		}
		catch (IOException e)
		{
			System.out.println("IO Error:" + e.getMessage());
		}
		catch (FileInputException e)
		{

		}
	}

	/** Einlesen der Daten aus einem Stream und Erzeugen des Objekts aid.
	 * 
	 * @param data Geöffneter Daten-Stream auf der Datenbankdatei oder Eingabedatei im bazi-Format.
	 * @throws IOException Fehler beim Lesen der Datei
	 * @throws FileInputException Datei entspricht nicht dem bazi-Format
	 * @return Die gelesenen Eingabedaten */
	public AbstractInputData read(BufferedReader data) throws IOException, FileInputException
	{
		wed = new WarnAndErrorDialog();

		// ohne Distrikte ist dist der Rückgabewert
		// mit Distrikten ist did der Rückgabewert und enthält mehrere InputData-Objekte
		// dist referenziert in dem Fall das InputData-Objekt zum aktuellen Distrikt
		InputData dist = new InputData();
		DistrictInputData did = new DistrictInputData();

		// true wenn Distrikte verwendet werden
		boolean bDist = false;

		// für die Title-Zeile
		String title = "";
		OutputFormat of = new OutputFormat();


		// Bool Variable zur Steuerrung, ob ein =INFO= Tag vorkam oder nicht.
		boolean infoTag = false;

		// Alle Zeilen einlesen und in lines speichern
		lines = new Vector<String>();
		try
		{
			String line = "";
			while ((line = data.readLine()) != null)
				lines.add(line.trim());
		}
		catch (IOException e)
		{}


		// ////////////////////////////////////////////////////////////
		// Beginn der Schleife, die jede Zeile untersucht
		// ////////////////////////////////////////////////////////////
		try
		{
			rec_line = -1;
			while (rec_line < lines.size() - 1)
			{
				// splittet die Zeile, nimmt alles vor dem ersten Whitespace (das ist das \s)
				// als tag und den Rest als line
				String tag = lines.get(++rec_line).split("\\s")[0].toLowerCase();
				String line = lines.get(rec_line).substring(tag.length()).trim();


				if (tag.matches(Resource.getString("bazi.fileIORules.data")))
				{
					parseDaten(line, dist);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.end")))
				{
					// Ende der Daten gefunden, einlesen beenden!
					break;
				}
				
				else if (tag.matches(Resource.getString("bazi.fileIORules.encoding")))
				{
					// Einstellungen zur Kodierung überlesen, da diese bereits beim
					// open ausgewertet werden
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.title")))
				{
					title = line;
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.method")))
				{
					parseMethoden(line, of);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.output")))
				{
					parseAusgabe(line, of);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.ties")))
				{
					parsePatts(line, of);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.input")))
				{
					parseEingabe(line, of);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.districtoption")))
				{
					did = new DistrictInputData();
					parseDistriktoption(line, did);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.mandates")))
				{
					parseMandate(line, dist, bDist);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.info")))
				{
					infoTag = true;
					parseInfo(line);
				}

				else if (tag.matches(Resource.getString("bazi.fileIORules.district")))
				{
					if (bDist)
						did.districts.add(dist);

					bDist = true;
					dist = new InputData();
					dist.outputFormat = of;
					dist.title = title;
					dist.minPlusValue = minPlus;

					String dName = "";
					if (line.length() > 0)
					{
						String[] elemente = line.split("\\s+");
						for (String element : elemente)
						{
							dName += element + " ";
						}
					}
					else
					{
						dName = new String(Calculation.COMMENT + "na");
						// falls kein Name angegeben ist, soll im Tablepane
						// "District $i" angezeigt werden
					}


					dist.district = dName.replace('\"', ' ').trim();
					dist.title = dName.replace('\"', ' ').trim();
					String stmp = new String(dName);
					dName = stmp.trim();
				}

				else
				{
					wed.add(Resource.getString("bazi.fileIOError.unknown_tag") + ": " + tag, rec_line + 1, lines.get(rec_line), false);
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			wed.add(Resource.getString("bazi.fileIOError.eof"), lines.size(), "", true);
		}
		// ////////////////////////////////////////////////////////////
		// Ende der Schleife, nun noch abschließen
		// ////////////////////////////////////////////////////////////

		// Falls der WarnAndErrorDialog Fehler enthält, zeige den Dialog
		if (!wed.isEmpty())
		{
			wed.show();

			// Falls schwerwiegende Fehler auftraten, die verhindern
			// dass das Laden erfolgreich abgeschlossen werden kann,
			// soll null zurückgegeben
			if (wed.containsFatalError())
				throw new FileInputException();
		}

		if (info != null && !Calculation.isCommandLineBazi)
			info.setVisible(infoTag);


		if (bDist)
		{
			did.title = title;
			did.districts.add(dist);
			did.outputFormat = of;
			did.BMM = BMM;
			did.pow = pow;
			did.base = base;
			did.min = min;
			did.max = max;
			did.minPlusValue = minPlus;
			did.prepare();
			return did;
		}
		else
		{
			dist.title = title;
			dist.outputFormat = of;
			dist.prepareData(BMM, pow, base, min, max, minPlus);
			return dist;
		}
	}


	private void parseDaten(String line, InputData dist)
	{
		Vector<String> vNames = new Vector<String>();
		Vector<String> vWeights = new Vector<String>();
		Vector<String> vMin = new Vector<String>();

		if (line.length() == 0)
			line = lines.get(++rec_line).trim();

		while (!line.toLowerCase().matches(Resource.getString("bazi.fileIORules.endData") + ".*")
				&& rec_line < lines.size() - 1)
		{
			String name = "", weight = "", min = "";

			// Steht das + für die Listenverbindung außerhalb
			// der Anführungszeichen?
			if (line.startsWith("+"))
			{
				name = "+";
				line = line.substring(1).trim();
			}

			// kein Name vorhanden?
			if (line.length() == 0)
			{
				wed.add(Resource.getString("bazi.fileIOError.missing_name"), rec_line + 1, lines.get(rec_line), false);

				// nächste Zeile, sofern vorhanden
				if (rec_line == lines.size() - 1)
					break;
				line = lines.get(++rec_line).trim();
				continue;
			}
			
			//Parteiname nur mit einem Anführungszeichen
			int count = 0;
			for(int i = 0; i < line.toCharArray().length; i++) 
				if(line.toCharArray()[i] == '\"')
					count++;
			
			if(count > 0 && count != 2) 
			{
				wed.add(Resource.getString("bazi.fileIOError.wrong_name"), rec_line + 1, lines.get(rec_line), false);
				
				// nächste Zeile, sofern vorhanden
				if (rec_line == lines.size() - 1)
					break;
				line = lines.get(++rec_line).trim();
				continue;
			}
			
			// Parteiname, if: mit Anführungszeichen, else: ohne
			if (line.startsWith("\""))
			{
				name += line.split("\"")[1];
				line = line.substring(name.length() + 2).trim();
			}
			else
			{
				name += line.split("(\\s|,|;)+")[0];
				line = line.substring(name.length()).trim();
			}

			// keine Stimmen vorhanden?
			if (line.length() == 0)
			{
				wed.add(Resource.getString("bazi.fileIOError.missing_weights"), rec_line + 1, lines.get(rec_line), false);

				// nächste Zeile, sofern vorhanden
				if (rec_line == lines.size() - 1)
					break;
				line = lines.get(++rec_line).trim();
				continue;
			}

			// Stimmen
			weight = line.split("(\\s|,|;)+")[0];
			line = line.substring(weight.length()).trim();
			if (!weight.matches("(\\d+)|(\\d+\\.\\d*)|(\\d*\\.\\d+)"))
			{
				weight = "0";
				wed.add(Resource.getString("bazi.fileIOError.wrong_weights"), rec_line + 1, lines.get(rec_line), false);
			}


			// Minimale Sitze (optional)
			if (line.length() > 0)
			{
				min = line;
			}
			else
			{
				min = "";
			}

			vNames.add(name);
			vWeights.add(weight);
			vMin.add(min);

			// Letzte Zeile?
			if (rec_line == lines.size() - 1)
				break;

			line = lines.get(++rec_line).trim();
		}
		rec_line--;


		// Daten in die Tabelle schreiben
		dist.vNames = vNames;
		dist.vWeights = convertToDoubleOrInt(vWeights);
		dist.vCond = vMin;
	}

	private void parseMethoden(String line, OutputFormat of)
	{
		Vector<MethodData> vm = new Vector<MethodData>();

		String[] methoden = line.split(",|;");

		for (int i = 0; i < methoden.length; i++)
		{
			String methode = methoden[i].trim();

			boolean b = false;
			if ((b = methode.toLowerCase().matches(Resource.getString("bazi.fileIORules.method.bmm_pow") + ".*"))
					|| methode.toLowerCase().matches(Resource.getString("bazi.fileIORules.method.bmm") + ".*"))
			{
				BMM = true;
				pow = b;

				try
				{
					String[] parameter = methode.split(" ");

					if (parameter.length < 2)
						continue;
					base = Integer.parseInt(parameter[1]);

					if (parameter.length < 3)
						continue;
					min = Integer.parseInt(parameter[2]);

					if (parameter.length < 4)
						continue;
					if (parameter[3].equals("oo"))
						max = Integer.MAX_VALUE;
					else
						max = Integer.parseInt(parameter[3]);

				}
				catch (NumberFormatException e)
				{
					String s = b ? Resource.getString("bazi.fileIOError.bmm_pow") : Resource.getString("bazi.fileIOError.bmm");
					wed.add(s + ": " + methode, rec_line + 1, lines.get(rec_line), false);
				}
			}


			// QSTATION
			else if (methode.startsWith("q=") || methode.startsWith("r=")) // TODO: q löschen?
			{
				methode = methode.substring(2).trim();
				String[] params = methode.split("\\s+");

				if (methode.length() == 0)
					wed.add(Resource.getString("bazi.error.read.r"), rec_line + 1, lines.get(rec_line), false);

				try
				{
					double param = Double.parseDouble(params[0]);
					MethodData mdata = new MethodData();
					mdata.method = MethodData.RSTATION;
					mdata.param = param;
					if (params.length > 1)
						mdata.paramString = methode;
					mdata.name = "DivSta";
					vm.add(mdata);
				}
				catch (NumberFormatException nfe)
				{
					wed.add(Resource.getString("bazi.error.read.r"), rec_line + 1, lines.get(rec_line), false);
				}

			}


			// DivPot
			else if (methode.startsWith("p="))
			{
				methode = methode.substring(2).trim();
				String[] params = methode.split("\\s+");

				if (methode.length() == 0)
					wed.add(Resource.getString("bazi.error.read.p"), rec_line + 1, lines.get(rec_line), false);

				try
				{
					double param = Double.parseDouble(params[0]);
					MethodData mdata = new MethodData();
					mdata.method = MethodData.PMEAN;
					mdata.param = param;
					if (params.length > 1)
						mdata.paramString = methode;
					mdata.name = "DivPot";
					vm.add(mdata);
				}
				catch (NumberFormatException nfe)
				{
					wed.add(Resource.getString("bazi.error.read.p"), rec_line + 1, lines.get(rec_line), false);
				}
			}

			else if (methode.equals(Resource.getString("bazi.fileIORules.method.bmm"))
					|| methode.equals(Resource.getString("bazi.fileIORules.method.bmm_pow")))
			{
				try
				{

				}
				catch (NumberFormatException e)
				{

				}
			}

			// sonstige
			else
			{
				MethodData md = new MethodData(methode);
				if (md.isValid())
					vm.add(md);
				else
					wed.add(Resource.getString("bazi.fileIOError.unknown_value") + ": " + methode, rec_line + 1, lines.get(rec_line), false);
			}
		}

		MethodData[] md = new MethodData[vm.size()];
		for (int i = 0; i < vm.size(); i++)
			md[i] = vm.get(i);
		of.methods = md;
	}

	private void parseAusgabe(String line, OutputFormat of)
	{
		String[] elemente = line.split("(,|;)+");

		for (String element : elemente)
		{
			element = element.trim().toLowerCase();
			if (element.matches(Resource.getString("bazi.fileIORules.output.horizontal")))
			{
				of.alignment = OutputFormat.ALIGN_HORIZONTAL;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.vertical")))
			{
				of.alignment = OutputFormat.ALIGN_VERTICAL;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.hori_vert")))
			{
				of.alignment = OutputFormat.ALIGN_HORI_VERT;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.vert_hori")))
			{
				of.alignment = OutputFormat.ALIGN_VERT_HORI;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.div_divisor_quota")))
			{
				of.divisor = OutputFormat.DIV_DIVISOR_QUOTA;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.div_divisorinterval")))
			{
				of.divisor = OutputFormat.DIV_DIVISORINTERVAL;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.div_multiplier")))
			{
				of.divisor = OutputFormat.DIV_MULTIPLIER;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.div_multiplierinterval")))
			{
				of.divisor = OutputFormat.DIV_MULTIPLIERINTERVAL;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.div_quotient")))
			{
				of.divisor = OutputFormat.DIV_QUOTIENT;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.ties_coded")))
			{
				of.ties = OutputFormat.TIES_CODED;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.ties_lac")))
			{
				of.ties = OutputFormat.TIES_LAC;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.output.ties_list")))
			{
				of.ties = OutputFormat.TIES_LIST;
			}
			else
			{
				wed.add(Resource.getString("bazi.fileIOError.unknown_value") + ": " + element, rec_line + 1, lines.get(rec_line), false);
			}
		}
	}

	private void parsePatts(String line, OutputFormat of)
	{
		line = line.toLowerCase();

		if (line.matches(Resource.getString("bazi.fileIORules.output.ties_coded")))
		{
			of.ties = OutputFormat.TIES_CODED;
		}
		else if (line.matches(Resource.getString("bazi.fileIORules.output.ties_lac")))
		{
			of.ties = OutputFormat.TIES_LAC;
		}
		else if (line.matches(Resource.getString("bazi.fileIORules.output.ties_list")))
		{
			of.ties = OutputFormat.TIES_LIST;
		}
		else
		{
			wed.add(Resource.getString("bazi.fileIOError.unknown_value") + ": " + line, rec_line + 1, lines.get(rec_line), false);
		}
	}

	private void parseEingabe(String line, OutputFormat of)
	{
		String[] elemente = line.split("(,|;)+");

		// 1. Spalte, z.B. Name
		if (elemente.length > 0)
		{
			of.labelNames = elemente[0].trim();
		}
		// 2. Spalte, Stimmen
		if (elemente.length > 1)
		{
			of.labelWeights = elemente[1].trim();
		}
		// 3. Spalte, Nebenbedingung
		if (elemente.length > 2)
		{
			String element = elemente[2].trim().toLowerCase();
			if (element.matches(Resource.getString("bazi.fileIORules.input.condition_min")))
			{
				of.condition = OutputFormat.CONDITION_MIN;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_minplus")))
			{
				Matcher m = Pattern.compile(Resource.getString("bazi.fileIORules.input.condition_minplusValue")).matcher(element);
				if (!m.matches())
					minPlus = 10;
				else
					minPlus = Double.parseDouble(m.group(m.groupCount()));
				of.condition = OutputFormat.CONDITION_MIN_PLUS;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_min_vpv")))
			{
				of.condition = OutputFormat.CONDITION_MIN_VPV;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_direct")))
			{
				of.condition = OutputFormat.CONDITION_DIRECT;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_naiv")))
			{
				of.condition = OutputFormat.CONDITION_NAIV;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_super_min")))
			{
				of.condition = OutputFormat.CONDITION_SUPER_MIN;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_max")))
			{
				of.condition = OutputFormat.CONDITION_MAX;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_min_to_max")))
			{
				of.condition = OutputFormat.CONDITION_MIN_TO_MAX;
			}
			else if (element.matches(Resource.getString("bazi.fileIORules.input.condition_none")))
			{
				of.condition = OutputFormat.CONDITION_NONE;
			}
			else
			{
				wed.add(Resource.getString("bazi.fileIOError.unknown_value") + ": " + element, rec_line + 1, lines.get(rec_line), false);
				of.condition = OutputFormat.CONDITION_NONE;
			}
		}
	}

	private void parseDistriktoption(String line, DistrictInputData did)
	{
		line = line.toLowerCase();
		if (line.equals("separat") || line.equals("separate") || line.startsWith("seperat"))
		{
			did.method = DistrictInputData.SEPARATE;
		}
		else if (line.equals("biprop"))
		{
			did.method = DistrictInputData.BIPROP;
		}
		else if (line.equals("nzz"))
		{
			did.method = DistrictInputData.NZZ;
		}
		else
		{
			wed.add(Resource.getString("bazi.fileIOError.unknown_value") + ": " + line, rec_line + 1, lines.get(rec_line), false);
		}
	}

	private void parseMandate(String line, InputData dist, boolean bDist)
	{
		if (bDist && !line.matches("[0-9]+"))
		{
			if (line.matches("[0-9]+;.*"))
			{
				line = line.split(";")[0];
				wed.add(Resource.getString("bazi.fileIOError.district_mandate"), rec_line + 1, lines.get(rec_line), false);
			}
			else
			{
				line = "0";
				wed.add(Resource.getString("bazi.fileIOError.wrong_mandate_dis"), rec_line + 1, lines.get(rec_line), false);
			}
		}
		else if (!bDist && !line.matches("(\\d+|\\d+\\s*\\.\\.\\s*\\d+|\\d+\\s*-\\s*\\d+)((\\s*(;|,)\\s*)(\\d+|\\d+\\s*\\.\\.\\s*\\d+|\\d+\\s*-\\s*\\d+))*"))
		{
			wed.add(Resource.getString("bazi.fileIOError.wrong_mandate"), rec_line + 1, lines.get(rec_line), false);
			line = "0";
		}

		dist.accuracy = line;
	}

	private void parseInfo(String line)
	{
		String infoText = line.length() > 0 ? line + "\n" : "";

		line = lines.get(++rec_line).trim();
		while (!line.toLowerCase().trim().matches(Resource.getString("bazi.fileIORules.endInfo")+ ".*")
				&& rec_line < lines.size())
		{
			infoText += line + "\n";
			line = lines.get(++rec_line);
		}
		rec_line--;

		if (info == null)
		{
			info = InfoFrame.getInfoFrame();
		}
		info.setText(infoText);
	}

	/** Speichert die übergebenen Eingabedaten in eine Datei mit dem angegebenen Dateinamen.
	 * 
	 * @param file Datei
	 * @param _aid AbstractInputData
	 * @throws IOException */
	public void saveToFile(File file, AbstractInputData _aid) throws IOException
	{
		aid = _aid;
		saveToFile(file);
	}

	/** Speichert die Eingabe Daten in eine bazi Datei mit dem angegebenen Dateinamen
	 * 
	 * @param file Datei
	 * @throws IOException Fehler beim Dateizugriff */
	public void saveToFile(File file) throws IOException
	{
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), CHARSET);
		BufferedWriter data = new BufferedWriter(out);

		data.write(Resource.getString("bazi.save.title") + " " + aid.title);
		data.newLine();
		data.flush();

		OutputFormat of = aid.outputFormat;

		// Methoden
		String names = "";
		String p = "", r = "";
		boolean dp = false, dq = false;
		for (int i = 0; i < of.methods.length; i++)
		{
			if (i != 0)
				names += ",";
			// String tmp = (String)v.elementAt(i);
			String tmp = of.methods[i].name;
			if (tmp.startsWith("r="))
			{
				String paramString = of.methods[i].paramString;
				if (paramString != null)
				{
					tmp = "r=" + paramString.replace(",", " ");
				}
			}
			else if (tmp.startsWith("p="))
			{
				String paramString = of.methods[i].paramString;
				if (paramString != null)
				{
					tmp = "p=" + paramString.replace(",", " ");
				}
			}
			names += tmp;
			if (of.methods[i].subapportionment != null)
			{
				names += "+" + of.methods[i].subapportionment.name;
			}
			if (tmp.equals("DivPow") || tmp.equals("DivPot"))
			{
				dp = true;
				p = of.methods[i].param + "";
			}
			if (tmp.equals("DivSta"))
			{
				dq = true;
				if (of.methods[i].paramString == null)
				{
					r = of.methods[i].param + "";
				}
				else
				{
					r = of.methods[i].paramString;
				}
			}
		}
		boolean b = false;
		if ((b = aid.pow) || aid.BMM)
		{
			if (names.length() > 0)
				names += ",";
			if (b)
				names += Resource.getString("bazi.save.bmm_pow");
			else
				names += Resource.getString("bazi.save.bmm");
			names += " " + aid.base + " " + aid.min;
			names += " " + (aid.max < Integer.MAX_VALUE ? aid.max : "oo");
		}

		data.write(Resource.getString("bazi.save.method") + " " + names);
		data.newLine();
		data.flush();

		// p bzw q speichern
		if (dp)
		{
			data.write(Resource.getString("bazi.save.p") + " " + p);
			data.newLine();
			data.flush();
		}
		if (dq)
		{
			data.write(Resource.getString("bazi.save.r") + " " + r);
			data.newLine();
			data.flush();
		}

		// Ausgabe
		String output = "";
		switch (of.alignment)
		{
		case (OutputFormat.ALIGN_HORIZONTAL):
			output = Resource.getString("bazi.save.horizontal") + ",";
			break;
		case (OutputFormat.ALIGN_VERTICAL):
			output = Resource.getString("bazi.save.verical") + ",";
			break;
		case (OutputFormat.ALIGN_HORI_VERT):
			output = Resource.getString("bazi.save.horver") + ",";
			break;
		case (OutputFormat.ALIGN_VERT_HORI):
			output = Resource.getString("bazi.save.verhor") + ",";
			break;
		}
		switch (of.divisor)
		{
		case (OutputFormat.DIV_DIVISOR_QUOTA):
			output += Resource.getString("bazi.save.divquota") + ",";
			break;
		case (OutputFormat.DIV_DIVISORINTERVAL):
			output += Resource.getString("bazi.save.divinterval") + ",";
			break;
		case (OutputFormat.DIV_MULTIPLIER):
			output += Resource.getString("bazi.save.mult") + ",";
			break;
		case (OutputFormat.DIV_MULTIPLIERINTERVAL):
			output += Resource.getString("bazi.save.multinterval") + ",";
			break;
		case (OutputFormat.DIV_QUOTIENT):
			output += Resource.getString("bazi.save.quotient") + ",";
			break;
		}
		switch (of.ties)
		{
		case (OutputFormat.TIES_CODED):
			output += Resource.getString("bazi.save.tiescoded");
			break;
		case (OutputFormat.TIES_LAC):
			output += Resource.getString("bazi.save.tieslistcoded");
			break;
		case (OutputFormat.TIES_LIST):
			output += Resource.getString("bazi.save.tieslistuncoded");
			break;
		}

		data.write(Resource.getString("bazi.save.output") + " " + output);
		data.newLine();
		data.flush();

		// Eingabe
		String input = of.labelNames + "," + of.labelWeights + ",";
		int min = of.condition;
		if (min == OutputFormat.CONDITION_MIN)
		{
			if (aid instanceof InputData)
				input += Resource.getString("bazi.save.condmin");
			else
				input += Resource.getString("bazi.save.condminbiprop");
		}
		else if (min == OutputFormat.CONDITION_MIN_PLUS)
			input += Resource.getString("bazi.save.condminplus") + aid.minPlusValue;
		else if (min == OutputFormat.CONDITION_MIN_VPV)
			input += Resource.getString("bazi.save.condmin_vpv");
		else if (min == OutputFormat.CONDITION_DIRECT)
			input += Resource.getString("bazi.save.conddirect");
		else if (min == OutputFormat.CONDITION_NAIV)
			input += Resource.getString("bazi.save.condnaiv");
		else if (min == OutputFormat.CONDITION_SUPER_MIN)
			input += Resource.getString("bazi.save.condsupermin");
		else if (min == OutputFormat.CONDITION_MAX)
			input += Resource.getString("bazi.save.condmax");
		else if (min == OutputFormat.CONDITION_MIN_TO_MAX)
			input += Resource.getString("bazi.save.condmintomax");
		else
			input += Resource.getString("bazi.save.condno");
		data.write(Resource.getString("bazi.save.input") + " " + input);
		data.newLine();
		data.flush();

		if (aid instanceof InputData)
		{
			InputData id0 = (InputData) aid;
			write(data, id0);
		}
		else
		{
			DistrictInputData did = (DistrictInputData) aid;
			did.prepare();

			data.write(Resource.getString("bazi.save.districtoption") + " ");
			switch (did.method)
			{
			case DistrictInputData.SEPARATE:
				data.write(Resource.getString("bazi.save.distoptsep"));
				break;
			case DistrictInputData.BIPROP:
				data.write(Resource.getString("bazi.save.distoptbiprop"));
				break;
			case DistrictInputData.NZZ:
				data.write(Resource.getString("bazi.save.distoptnzz"));
				break;
			default:
				data.write(Resource.getString("bazi.save.distoptsep"));
			}
			data.newLine();

			for (int i = 0; i < did.data.length; i++)
			{
				data.write(Resource.getString("bazi.save.district") + " " + did.data[i].district);
				data.newLine();
				data.flush();
				write(data, did.data[i]);
			}
		}
		if (info != null)
		{
			if (!(info.getText() == null || info.getText().length() == 0))
			{
				data.write(Resource.getString("bazi.save.info"));
				data.newLine();
				StringBuffer text = new StringBuffer(info.getText());
				while (true)
				{
					int index1 = text.indexOf("\n");
					int index2 = text.indexOf("\r");
					if (index1 == -1 || index2 == -1)
					{
						data.write(text.toString());
						break;
					}
					else
					{
						if (index2 < index1)
						{
							int help = index1;
							index1 = index2;
							index2 = help;
						}

						data.write(text.substring(0, index1));
						data.newLine();
						if (index2 == index1 + 1)
						{
							text = new StringBuffer(text.substring(index1 + 2));
						}
						else
						{
							text = new StringBuffer(text.substring(index1 + 1));
						}
					}
				}
			}
		}
		if (CHARSET.equals("UTF8"))
		{
			data.write(Resource.getString("bazi.save.encoding")+" UTF-8");
			data.newLine();			
		}
		data.write(Resource.getString("bazi.save.end"));
		data.newLine();
		data.flush();

		data.close();
	}

	/** Auslesen der Eingabedaten und schreiben dieser Daten in den Stream.
	 * 
	 * @param data Geöffneter Daten-Stream auf die Eingabedatei.
	 * @param id Eingabedaten
	 * @throws IOException Fehler beim Dateizugriff */
	private void write(BufferedWriter data, InputData id) throws IOException
	{

		// Mandate
		data.write(Resource.getString("bazi.save.seats") + " " + id.accuracy);
		data.newLine();
		data.flush();

		// Datensätze
		Vector<String> vNames = id.vNames;
		Vector<Number> vWeights = id.vWeights;
		Vector<String> vMin = id.vCond;

		data.write(Resource.getString("bazi.save.data") + " ");
		for (int n = 0; n < vNames.size(); n++)
		{
			String tmp = new String(vNames.elementAt(n));
			// Das + für die Listenverbindungen steht außerhalb der
			// Anführungszeichen!
			if (tmp.charAt(0) == '+')
			{
				String tmp2 = tmp.substring(1, tmp.length());
				data.write("+\"" + tmp2 + "\" ");
			}
			else
			{
				data.write("\"" + vNames.elementAt(n) + "\" ");
			}
			data.write(vWeights.elementAt(n) + " " + vMin.elementAt(n) + " ");
			data.newLine();
		}
	}

	/** Setzt das AbstractInputData Objekt dieses FileIO-Objekts
	 * 
	 * @param _aid AbstractInputData */
	public void setAbstractInputData(AbstractInputData _aid)
	{
		aid = _aid;
	}

	/** Wandelt alle Strings Parameters in Integer oder Double um.
	 * 
	 * Schlägt das Umwandeln fehl wird der entsprechende Eintrag auf null gesetzt.
	 * 
	 * @param v Vector mit Zahlen als String
	 * @return Vector mit Zahlen als Integer oder Double */
	private Vector<Number> convertToDoubleOrInt(Vector<String> v)
	{
		Iterator<String> vi = v.iterator();
		Vector<Number> ret = new Vector<Number>();
		while (vi.hasNext())
		{
			String value = vi.next();
			try
			{
				ret.add(new Integer(value));
			}
			catch (NumberFormatException nfe)
			{
				try
				{
					// System.out.println("NFE by new Integer");
					ret.add(new Double(value));
				}
				catch (NumberFormatException ex)
				{
					ret.add(null);
				}
			}
		}
		return ret;
	}

	/* Wandelt alle Strings Parameters in Integer um.
	 * Schlägt das Umwandeln fehl wird der entsprechende Eintrag auf null gesetzt.
	 * @param v Vector mit Zahlen als String
	 * @return Vector mit Zahlen als Integer oder Double
	 * @deprecated */
	/* Sei bazi.2007.10-b-01 rausgenommen private Vector<Integer> convertToInt(Vector<String> v) { Iterator<String> vi = v.iterator(); Vector<Integer> ret = new Vector<Integer>();
	 * while (vi.hasNext()) { String value = vi.next(); try { ret.add(new Integer(value)); } catch (NumberFormatException nfe) { ret.add(null); } } return ret; } */

	/* Sammelt Warn und Errormeldungen und zeigt sie nach dem Einlesen der gesamten Datei an. Sind nur Warnungen vorhanden, kann das Programm fortfahren */


	/** <b>Überschrift:</b> Klasse WarnAndErrorDialog<br>
	 * <b>Beschreibung:</b> Sammelt Meldungen zu fatalen und nicht-fatalen Fehlern beim Einlesen einer Datei<br>
	 * <b>Copyright:</b> Copyright (c) 2010<br>
	 * <b>Organisation:</b> Universität Augsburg
	 * 
	 * @version 1.0
	 * @author Marco Schumacher */
	private class WarnAndErrorDialog
	{
		/** Nachricht */
		private final Vector<String> message = new Vector<String>();
		/** Zeilennummer */
		private final Vector<Integer> line_nr = new Vector<Integer>();
		/** Zeilentext */
		private final Vector<String> line = new Vector<String>();
		/** ob ein Fehler auftrat, der das Fertigladen unmöglich macht */
		private final Vector<Boolean> fatal = new Vector<Boolean>();


		/** fügt eine Warnung oder einen Fehler hinzu
		 * 
		 * @param message Fehlernachricht
		 * @param line_nr Zeilennummer
		 * @param line Zeilentext
		 * @param fatal <b>true</b> wenn ein Fehler auftrat, der das Fertigladen unmöglich macht */
		public void add(String message, int line_nr, String line, boolean fatal)
		{
			this.message.add(message);
			this.line_nr.add(line_nr);
			this.line.add(line);
			this.fatal.add(fatal);
		}


		/** Zeigt den Dialog an */
		public void show()
		{
			// Ausgabe für Kommandozeile
			if (Calculation.isCommandLineBazi)
			{
				String s = "";
				for (int i = 0; i < message.size(); i++)
				{
					s += fatal.get(i) ? "Error: " : "Warning: ";
					s += message.get(i) + "\n";
					s += "(line " + line_nr.get(i) + ") " + line.get(i);
					s += "\n";
				}
				if (!containsFatalError())
					s += Resource.getString("bazi.fileIOError.continue") + "\n\n";
				System.out.println(s);
			}

			// Ausgabe im Fenster. HTML-formatiert
			else
			{
				String ausgabe = "<html>";
				for (int i = 0; i < message.size(); i++) // für jede Nachricht...
				{
					ausgabe += fatal.get(i) ? "<b color='red'>Error:</b>" : "<b>Warning:</b>"; // Überschrift
					ausgabe += "<br>";
					ausgabe += "<p>" + message.get(i) + "</p"; // Nachricht
					ausgabe += "<p>(line " + line_nr.get(i) + ")"; // Zeilennummer
					ausgabe += "<font color='gray'>" + line.get(i) + "</font></p>"; // Zeileninhalt
					ausgabe += "<br><br>";
				}
				if (!containsFatalError())
					ausgabe += Resource.getString("bazi.fileIOError.continue");
				ausgabe += "</html>";

				JScrollPane content = new JScrollPane(new JLabel(ausgabe));
				content.setBorder(null);

				// verhindert, dass der Warndialog zu groß wird
				if (message.size() > 3)
					content.setPreferredSize(new Dimension(400, 300));

				int type = containsFatalError() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;

				JOptionPane.showMessageDialog(RoundFrame.getRoundFrame(), content,
						Resource.getString("bazi.error.title"), type);
			}
		}


		/** @return <b>true</b> wenn noch kein Fehler hinzugefügt wurde */
		public boolean isEmpty()
		{
			return message.size() == 0;
		}


		/** @return <b>true</b> wenn mindestens ein Fehler das Fertigladen verhindert */
		public boolean containsFatalError()
		{
			boolean ret = false;
			for (boolean b : fatal)
				if (b)
					ret = true;

			return ret;
		}
	}
}
