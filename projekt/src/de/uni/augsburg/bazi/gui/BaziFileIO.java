/*
 * @(#)BaziFileIO.java 3.1 07/06/10
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.AbstractInputData;
import de.uni.augsburg.bazi.driver.FileIO;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/** <b>Title:</b> Klasse BaziFileIO<br>
 * <b>Description:</b> Verwaltet das Laden und Speichern von Dateien für die GUI<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 3.2
 * @author Florian Kluge, Robert Bertossi, Christian Brand */
public class BaziFileIO
{

	/** Logger, um evtl auftretende Fehler zu loggen */
	private Logger logger = Logger.getLogger(BaziFileIO.class);

	/** Referenz auf das RoundFrame. */
	private RoundFrame roundFrame;

	/** Aktuelle Eingabedatei. */
	private File currentFile;

	/** Aktuelle Ausgabedatei. */
	private File outputFile;

	/** Aktuelle Eingabedaten */
	private AbstractInputData aid;

	/** Zugriff und Sichern von bazi-Dateien */
	private FileIO fileio;

	/** Verwendete Codierung bei Dateizugriffen */
	public static final String CHARSET = "ISO-8859-1";

	/** Erzeugen einer Instanz von FileIO.
	 * 
	 * @param roundFrame Referenz auf RoundFrame.
	 * @param fio Zugriff auf bazi-Dateien */
	public BaziFileIO(RoundFrame roundFrame, FileIO fio)
	{
		this.roundFrame = roundFrame;
		fileio = fio;
		if (logger.isEnabledFor(Level.WARN))
		{
			if (fileio == null)
			{
				logger.warn("Class BaziFileIO was initialized with a FileIO == null");
			}
			if (roundFrame == null)
			{
				logger.warn("Class BaziFileIO was initialized with a RoundFrame == null");
			}
		}
	}

	/** Dialog zum Oeffnen einer Eingabedatei anzeigen.
	 * 
	 * @return Eingabedaten */
	public AbstractInputData open()
	{
		JFileChooser chooser;
		if (currentFile == null || !currentFile.exists())
		{
			chooser = new JFileChooser();
		}
		else
		{
			if (logger.isTraceEnabled())
			{
				try
				{
					logger.trace("Starting JFileChooser with File: " + currentFile.getCanonicalPath());
				}
				catch (IOException ioe)
				{
					logger.debug("An IOException was thrown while calling getCanonicalPath()-method.\n" +
							"This just involved a Debug Message, no critical Programm Code!");
				}
			}
			chooser = new JFileChooser(currentFile);
		}
		OneFileTypeFilter filter = new OneFileTypeFilter("bazi",
				Resource.getString("bazi.fileio.bazi"));
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);

		int returnVal = chooser.showOpenDialog(roundFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File tempFile = chooser.getSelectedFile();
			open(tempFile);
		}
		return aid;
	}

	public AbstractInputData open(File f)
	{
		if (f.getName().endsWith(".bazi"))
		{
			currentFile = f;
			aid = fileio.open(currentFile);
			if (aid == null)
			{
				currentFile = null;
				if (logger.isEnabledFor(Level.DEBUG))
				{
					logger.debug("BaziFileIO could not open: " + f.getName());
				}
			}
			else
			{
				// roundFrame.setTitle(roundFrame.getTitle() + " [" + currentFile.getAbsolutePath() + "]");
				// roundFrame.setCurrentFile(currentFile);
			}
		}
		return aid;
	}

	/** Dialog zum Speichern einer Eingabedatei anzeigen.
	 * 
	 * @param fname Vorschlag für den Dateinamen
	 * @param _aid Eingabedaten, die abgespeichert werden sollen */
	public void saveAs(String fname, AbstractInputData _aid)
	{
		fname = fname.replaceAll(":", "_");
		JFileChooser chooser;
		if (currentFile == null)
		{
			chooser = new JFileChooser(new File(""));
		}
		else
		{
			chooser = new JFileChooser(currentFile);
		}
		OneFileTypeFilter filter = new OneFileTypeFilter("bazi",
				Resource.getString("bazi.fileio.bazi"));
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		// fname als Vorschlag
		chooser.setSelectedFile(new File(fname + ".bazi"));

		int returnVal = chooser.showSaveDialog(roundFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			currentFile = chooser.getSelectedFile();
			try
			{
				if (!currentFile.getName().endsWith(".bazi"))
				{
					currentFile = new File(currentFile.getCanonicalPath() + ".bazi");
				}
				if (!currentFile.exists())
				{
					if (!currentFile.createNewFile())
					{
						currentFile = null;
						System.out.println("~Fehler");
						return;
					}
				}
			}
			catch (IOException ioe)
			{
				System.out.println(ioe.getMessage());
			}
			aid = _aid;
			saveFile(currentFile);
		}
	}

	/** Dialog zum Speichern der Eingabedatei anzeigen.
	 * Hier wird saveAs(String) mit einem leeren String aufgerufen
	 * 
	 * @param _aid Eingabedaten, die abgespeichert werden sollen */
	public void saveAs(AbstractInputData _aid)
	{
		saveAs("", _aid);
	}

	/** Daten-Streams zum Schreiben von file öffnen.
	 * 
	 * @param file Datei, in die geschrieben werden soll. */
	private void saveFile(File file)
	{
		try
		{
			fileio.saveToFile(file, aid);
			if (!file.exists())
			{
				currentFile = null;
			}
		}
		catch (IOException e)
		{
			System.out.println("IO Error:" + e.getMessage());
		}
	}

	/** Speichern der Ausgabe in eine Textdatei.
	 * 
	 * @param content Ausgabetext. */
	public void export(String content)
	{
		JFileChooser chooser;
		if (outputFile == null)
		{
			/* Hier soll der Eintrag aus dem Titelfeld verwendet werden
			 * statt einem leeren Name */
			if (currentFile == null)
			{
				chooser = new JFileChooser(new File(""));
			}
			else
			{
				chooser = new JFileChooser(currentFile);
			}
		}
		else
		{
			chooser = new JFileChooser(outputFile);

		}
		chooser.addChoosableFileFilter(new OneFileTypeFilter("*",
				Resource.getString("bazi.fileio.all")));
		OneFileTypeFilter txtFilter = new OneFileTypeFilter("txt",
				Resource.getString("bazi.fileio.text"));
		chooser.addChoosableFileFilter(txtFilter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		int returnVal = chooser.showSaveDialog(roundFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			outputFile = chooser.getSelectedFile();
			try
			{
				if ((chooser.getFileFilter() == txtFilter) &&
						(!outputFile.getName().endsWith(".txt")))
				{
					outputFile = new File(outputFile.getCanonicalPath() + ".txt");
				}
				OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
						outputFile.getCanonicalFile()), CHARSET);
				BufferedWriter data = new BufferedWriter(out);
				StringTokenizer st = new StringTokenizer(content, "\n");
				while (st.hasMoreTokens())
				{
					String tmp = st.nextToken();
					// if (tmp == null) System.out.println("String == null");
					data.write(tmp);
					data.newLine();
				}

				data.flush();
				data.close();
				out.close();
			}
			catch (IOException e)
			{
				System.out.println("IO Error:" + e.getMessage());
			}
		} // end export

	}

	public File getLoadedFile()
	{
		return this.currentFile;
	}
}
