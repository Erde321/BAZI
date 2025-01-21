/*
 * @(#)Start.java 2.3 09/01/07
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;

import de.uni.augsburg.bazi.gui.GUIConstraints;
import de.uni.augsburg.bazi.gui.Jackson;
import de.uni.augsburg.bazi.gui.Language;
import de.uni.augsburg.bazi.gui.RoundFrame;
import de.uni.augsburg.bazi.gui.StartDialog;


/** <b>Title:</b> Klasse Start<br>
 * <b>Description:</b> Behandlung der Aufrufparameter für die GUI<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.3
 * @author Jan Petzold, Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */
public class Start
{

	/** Logger dieser Klasse */
	private static Logger logger = Logger.getLogger(Start.class);
	
	/** Instanz zum einlesen aus der JSON */
	private static Jackson jackson;
	
	

	/** Programmstart beim Aufruf als Applikation.
	 * 
	 * @param args Aufrufparameter */
	public static void main(String[] args)
	{
		
		if (VersionControl.getVersion().indexOf("b") != -1)
		{

			// Testversion
		
			// Initialisierung des Root Loggers
		
			try
			{
				java.io.InputStream is = Start.class.getResourceAsStream("log4j.properties");
				java.util.Properties props = new java.util.Properties();
				props.load(is);
				PropertyConfigurator.configure(props);
				Logger.getRootLogger().info("Logging-Konfiguration in: bazi.jar: src\\de\\uni\\augsburg\\bazi\\log4j.properties");
				Logger.getRootLogger().info("Logging bis Level: "+Logger.getRootLogger().getLevel());
			}
			catch (Exception e)
			{
				Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
				Logger.getRootLogger().setLevel(Level.DEBUG);
			}
		}
		else
		{
			// In Release Version Standardlogger nehmen und Logger deaktivieren
			Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
			Logger.getRootLogger().setLevel(Level.OFF);
		}

		if (Start.logger.isTraceEnabled())
		{
			String parameters = "";
			for (int i = 0; i < args.length; i++)
			{
				parameters += "\"" + args[i] + "\" ; ";
			}
			Start.logger.trace("Programm durch Klasse Start aufgerufen mit" + args.length +
					"Parametern: " + parameters);
		}


		boolean max = false;
		//Liest die .json datei aus
				Jackson.readJSON(jackson);
				
				//wenn StartDialog = true
				if(Language.getStartDialog()) 
				{	
					// Keine Parameter übergeben
					if (args.length == 0)
					{
						new StartDialog();
					}
					else
					{
						boolean setLang = false;
						if (args[0].equals("max"))
							max = true;
						else
						{
							Resource.setLang(args[0]);
							setLang = true;
						}
						if (args.length > 1)
						{
							if (args[1].equals("max"))
								max = true;
							else
							{
								Resource.setLang(args[1]);
								setLang = true;
							}
						}
						if (!setLang)
							new StartDialog();
					}
				}
				
				//wenn StartDialog = false, dann Sprach aus .json lesen
				if(!Language.getStartDialog())
					Resource.setLang(Language.getLanguage());
				//sonst neue Sprache in die .json schreiben
				else
					Jackson.writeJSON(GUIConstraints.getFontSize(0), GUIConstraints.getFontSize(1), GUIConstraints.getFontSize(2), GUIConstraints.getFontSize(3), 
							Language.getStartDialog(), Language.getLanguage());
		new RoundFrame("Version " + VersionControl.getVersion(), max);
		
	}
}
