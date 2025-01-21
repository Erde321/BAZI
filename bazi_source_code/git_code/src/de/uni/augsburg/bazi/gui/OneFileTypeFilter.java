/*
 * @(#)OneFileTypeFilter.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/** <b>Title:</b> Klasse OneFileTypeFilter<br>
 * <b>Description:</b> Dateifilter für einen Dateityp, der beim Datei-Öffnen- bzw. Speichern-Dialog verwendet wird<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @version 2.1
 * @author Jan Petzold, Christian Brand */
class OneFileTypeFilter extends FileFilter
{

	/** Hashtable mit allen Dateiendungen. */
	private Hashtable<String, OneFileTypeFilter> filters = null;

	/** Beschreibung des Dateityps.
	 * @uml.property name="description" */
	private String description = null;

	/** Zusammengesetzte Beschreibung. */
	private String fullDescription = null;

	/** Angabe, ob die Dateiendungen mit in der Beschreibung stehen sollen. */
	private boolean useExtensionsInDescription = true;

	/** Erzeugt einen Dateifilter, der alle Datei-Endungen akzeptiert. */
	public OneFileTypeFilter()
	{
		filters = new Hashtable<String, OneFileTypeFilter>();
	}

	/** Erzeugt einen Dateifilter, der eine Datei-Endung akzeptiert.
	 * 
	 * @param extension Dateiendung. Beispiel "txt" oder "java". */
	public OneFileTypeFilter(String extension)
	{
		this(extension, null);
	}

	/** Erzeugt einen Dateifilter, der eine Datei-Endung akzeptiert.
	 * Zusätzlich wird eine Beschreibung des Dateityps angegeben.
	 * 
	 * @param extension Dateiendung. Beispiel "txt" oder "java".
	 * @param description Beschreibung des Dateityp. Beispiel "Text-Datei" oder "Java Source". */
	public OneFileTypeFilter(String extension, String description)
	{
		this();
		if (extension != null)
		{
			addExtension(extension);
		}
		if (description != null)
		{
			setDescription(description);
		}
	}

	/** Erzeugt einen Dateifilter, der mehrere Datei-Endungen akzeptiert.
	 * 
	 * @param filters Datei-Endungen, die unterstützt werden. */
	public OneFileTypeFilter(String[] filters)
	{
		this(filters, null);
	}

	/** Erzeugt einen Dateifilter, der mehrere Datei-Endungen akzeptiert.
	 * Zusätzlich wird eine Beschreibung des Dateityps angegeben.
	 * 
	 * @param filters Datei-Endungen, die unterstützt werden.
	 * @param description Beschreibung des Dateityps. */
	public OneFileTypeFilter(String[] filters, String description)
	{
		this();
		for (int i = 0; i < filters.length; i++)
		{
			// fügt Filter für Filter hinzu
			addExtension(filters[i]);
		}
		if (description != null)
		{
			setDescription(description);
		}
	}

	/** Prüft, ob die Datei akzeptiert wird oder nicht.
	 * 
	 * @param file Datei, die untersucht wird.
	 * @return <b>true</b> wenn die Datei akzeptiert wurde */
	@Override
	public boolean accept(File file)
	{
		if (file != null)
		{
			if (file.isDirectory())
			{
				return true;
			}
			if (filters.get("*") != null)
			{
				return true;
			}
			String extension = getExtension(file);
			if (extension != null && filters.get(getExtension(file)) != null)
			{
				return true;
			}
		}
		return false;
	}

	/** Gibt die Datei-Endung von file zurück.
	 * 
	 * @param file Eine Datei
	 * @return Die Dateiendung, wenn sie eine hat; null bei keiner Endung */
	public String getExtension(File file)
	{
		if (file != null)
		{
			String filename = file.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1)
			{
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	/** Fügt eine neue Datei-Endung als Filter hinzu.
	 * 
	 * @param extension Datei-Endung. */
	public void addExtension(String extension)
	{
		if (filters == null)
		{
			filters = new Hashtable<String, OneFileTypeFilter>(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/** Gibt die Beschreibung des Dateityps an.
	 * @return Beschreibungen der Dateitypen
	 * @uml.property name="description" */
	@Override
	public String getDescription()
	{
		if (fullDescription == null)
		{
			if (description == null || isExtensionListInDescription())
			{
				fullDescription = description == null ? "(" : description + " (";
				// Beschreibung aus den Dateiendungen zusammenbauen
				Enumeration<String> extensions = filters.keys();
				if (extensions != null)
				{
					fullDescription += "*." + extensions.nextElement();
					while (extensions.hasMoreElements())
					{
						fullDescription += "; *." + extensions.nextElement();
					}
				}
				fullDescription += ")";
			}
			else
			{
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/** Setzen der Beschreibung des Dateityps.
	 * @param description Beschreibung
	 * @uml.property name="description" */
	public void setDescription(String description)
	{
		this.description = description;
		fullDescription = null;
	}

	/** Setzen der Eigenschaft, ob die Datei-Endungen in der Beschreibung
	 * aufgeführt werden sollen oder nicht.
	 * 
	 * @param b boolean */
	public void setExtensionListInDescription(boolean b)
	{
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/** Angabe, ob die Datei-Endungen in der Beschreibung mit aufgeführt werden
	 * oder nicht.
	 * 
	 * @return <b>true</b> wenn die Datei-Endungen aufgeführt werden */
	public boolean isExtensionListInDescription()
	{
		return useExtensionsInDescription;
	}
}
