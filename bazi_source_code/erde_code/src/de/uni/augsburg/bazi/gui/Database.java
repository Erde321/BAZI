/*
 * @(#)Database.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.FileIO;
import de.uni.augsburg.bazi.driver.FileInputException;

/** <b>Title:</b> Klasse Database<br>
 * <b>Description:</b> Verwaltung der Datenbankdateien<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Jan Petzold, Robert Bertossi, Christian Brand */
public class Database implements ActionListener
{

	/** Referenz auf das RoundFrame. */
	private final RoundFrame roundFrame;

	/** Liste, die die Datenbankeinträge mit den entsprechenden Dateinamen enthält. */
	private final Hashtable<JMenuItem, String> files = new Hashtable<JMenuItem, String>();

	/** Fenster zur Informationsanzeige zu der entsprechenden Datenbankdatei. */
	private InfoFrame infoFrame = null;

	/** Erzeugt eine Datenbank.
	 * 
	 * @param roundFrame Referenz auf das RoundFrame.
	 * @param menu Menupunkt des RoundFrame, in dem die Datenbank eingetragen
	 *          wird. */
	public Database(RoundFrame roundFrame, JMenu menu)
	{
		this.roundFrame = roundFrame;

		InputStreamReader in;
		BufferedReader data;

		try
		{
			ZipFile zf = new ZipFile("data.zip");
			FileIO.CHARSET = "ISO-8859-1"; // wahlen.txt liegt in Standard ASCII-Kodierung vor
			in = new InputStreamReader(zf.getInputStream(zf.getEntry("wahlen.txt")), FileIO.CHARSET);

			data = new BufferedReader(in);

			// Beginn des Strukturaufbaus
			createMenuGroup(data, menu);

			data.close();
		}
		catch (MalformedURLException e)
		{
			System.out.println("Bad URL: " + e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("IO Error: " + e.getMessage());
		}
	}

	/** Einlesen der Datenbankstruktur, dabei Bildung der Menueinträge und
	 * Untermenus.
	 * 
	 * @param data Geöffneter Datenstrom auf die Datei <i>wahlen.txt</i>.
	 * @param jmi Menuitem an das ein neuer Menupunkt oder ein Untermenu gehängt
	 *          wird.
	 * @throws IOException */
	private void createMenuGroup(BufferedReader data, JMenuItem jmi) throws
			IOException
	{
		StringTokenizer st;
		String temp, filename, line;
		while ((line = data.readLine()) != null && !line.trim().equals("END"))
		{
			st = new StringTokenizer(line);
			temp = st.nextToken();
			// Untermenu
			if (temp.equals("GROUP"))
			{
				temp = st.nextToken();
				while (st.hasMoreTokens())
				{
					temp += " " + st.nextToken();
				}
				JMenu item = new JMenu(temp);
				item.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
				jmi.add(item);
				data.readLine(); // BEGIN
				createMenuGroup(data, item);
			}
			// Trennlinie
			else if (temp.equals("LINE"))
			{
				((JMenu) jmi).addSeparator();
			}
			// Menupunkt
			else
			{
				filename = temp;
				temp = st.nextToken();
				while (st.hasMoreTokens())
				{
					temp += " " + st.nextToken();
				}
				JMenuItem item = new JMenuItem(temp);
				item.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
				jmi.add(item);
				item.addActionListener(this);
				files.put(item, filename);
			}
		}
	}

	/** Reaktion bei Auswahl eines Menupunkt.
	 * (aus Interface ActionListener)
	 * 
	 * @param ae ActionEvent */
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		String filename = files.get(ae.getSource());
		readData(filename);
		Dialog_Min_Plus.minPlus(RoundFrame.minPlusValue, roundFrame.gettpTable().getTables());
	}

	/** Einlesen des im Menu ausgewählten Datenbankeintrags.
	 * 
	 * @param filename Zum Datenbankeintrag gehörende Datei. */
	public void readData(String filename)
	{
		InputStreamReader in;
		BufferedReader data;

		try
		{
			ZipFile zf = new ZipFile("data.zip");
			FileIO.CHARSET = "ISO-8859-1"; // immer zunaechst mit Standard ASCII-Kodierung öffnen
			in = new InputStreamReader(zf.getInputStream(zf.getEntry(filename)), FileIO.CHARSET);

			data = new BufferedReader(in);
			
			// Pruefen, ob UTF-8-Variante vorliegt
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
						FileIO.CHARSET = "UTF8";
						break;					
					}
				 }
			 }
						
			in.close();
			// jetzt mit der richtigen Kodierung öffnen
			in = new InputStreamReader(zf.getInputStream(zf.getEntry(filename)), FileIO.CHARSET);
						
			data = new BufferedReader(in);
			data.mark(10);

			data.reset();

			roundFrame.setCurrentFile(null);
			roundFrame.setTitle(roundFrame.getTitle());

			// einlesen der Daten und auffuellen der Oberflaeche
			try
			{
				roundFrame.open((new FileIO()).read(data));
			}
			catch (FileInputException fie)
			{
				roundFrame.restart();
				// call to Roundframe
			}

			try
			{
				// info ist im Internet ein URL, in der Applikation ein String
				Object info;
				// Info-Datei hat den selben Namen wie Datendatei
				String infofile = filename.substring(0, filename.indexOf(".")) +
						".html";

				ZipEntry ze = zf.getEntry(infofile);
				if (ze == null)
				{
					throw new IOException();
				}
				in = new InputStreamReader(zf.getInputStream(ze));
				data = new BufferedReader(in);
				String temp, note = "";
				while ((temp = data.readLine()) != null)
				{
					note += temp;
				}
				info = note;

				if (infoFrame == null)
				{
					infoFrame = new InfoFrame();
					infoFrame.setText(info);
					infoFrame.setVisible(true);
				}
				else
				{
					infoFrame.setText(info);

				}
			}
			catch (IOException ioe)
			{
				if (infoFrame != null)
				{
					infoFrame.dispose();
					infoFrame = null;
				}
			}

		}
		catch (IOException e)
		{
			System.out.println("IO Error:" + e.getMessage());
		}
		// System.out.println("File read");
	}

	/** Schließen des InfoFrame. */
	public void closeInfoFrame()
	{
		if (infoFrame != null)
		{
			infoFrame.dispose();
			infoFrame = null;
		}
	}

	/** <b>Title:</b> Innere Klasse InfoFrame<br>
	 * <b>Description:</b> Informationfenster für einen Datenbankeintrag.<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Jan Petzold, Christian Brand
	 * @version 2.1 */
	class InfoFrame extends JFrame implements HyperlinkListener
	{

		/** Default UID */
		private static final long serialVersionUID = 1L;

		/** Zur Anzeige des Inhalts der Infodatei */
		private final JEditorPane editorPane;

		/** Erzeugung eines leeren InfoFrames. */
		public InfoFrame()
		{
			super();
			setTitle(Resource.getString("bazi.database.info.title"));
			setIconImage(roundFrame.getIconImage());
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent we)
				{
					dispose();
					infoFrame = null;
				}
			});

			JPanel content = new JPanel();
			SuperLayout layout = new SuperLayout(content);

			editorPane = new JEditorPane();
			editorPane.setContentType("text/html");
			editorPane.setEditable(false);
			editorPane.addHyperlinkListener(this);

			JScrollPane scrollPane = new JScrollPane(editorPane);
			int w = Integer.parseInt(Resource.getString("bazi.database.info.width"));
			int h = Integer.parseInt(Resource.getString("bazi.database.info.height"));
			scrollPane.setPreferredSize(new Dimension(w, h));

			layout.add(scrollPane, 0, 0, 1, 1, 1, 1, GridBagConstraints.BOTH,
					GridBagConstraints.NORTHWEST);

			setContentPane(content);
			pack();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = getSize();
			int x = screenSize.width - frameSize.width;
			int y = screenSize.height - frameSize.height;
			setLocation(x, y);
		}

		/** Reaktion auf das Anklicken eines Links in Text.
		 * (Interface HyperlinkListener)
		 * 
		 * @param hle HyperlinkEvent */
		@Override
		public void hyperlinkUpdate(HyperlinkEvent hle)
		{
			if (hle.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			{
				roundFrame.showDocument(hle.getDescription());
			}
		}

		/** Setzen des Textes des Informationsfensters.
		 * 
		 * @param info HTML-Code der anzuzeigenden Info-Datei als Text. */
		public void setText(String info)
		{
			editorPane.setText(info);
			editorPane.setCaretPosition(0);
		}

		/** Setzen des Textes des Informationsfensters.
		 * 
		 * @param info URL der anzuzeigenden Info-Datei.
		 * @throws IOException */
		public void setText(URL info) throws IOException
		{
			editorPane.setPage(info);
			editorPane.setCaretPosition(0);
		}

		/** Setzen des Textes des Informationsfensters.
		 * 
		 * @param info URL oder HTML-Code als Text der anzuzeigenden Info-Datei. */
		public void setText(Object info) throws IOException
		{
			if (info instanceof URL)
			{
				editorPane.setPage((URL) info);
			}
			else if (info instanceof String)
			{
				editorPane.setText((String) info);
			}
			editorPane.setCaretPosition(0);
		}
	}
}
