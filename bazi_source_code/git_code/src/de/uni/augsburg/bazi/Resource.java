/*
 * @(#)Resource.java 2.2 08/02/07
 * 
 * Copyright (c) 2000-2008 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.apache.log4j.Logger;

/** <b>Title:</b> Klasse Resource<br>
 * <b>Description:</b> Verwaltung der properties-Dateien. Syntax des
 * Dateinamens einer Properties-Datei: bazi{_$LANG}.properties<br>
 * <b>Copyright:</b> Copyright (c) 2000-2008<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.2
 * @author Jan Petzold, Robert Bertossi, Christian Brand */
public class Resource
{

	/** Logger um alle auftretenden Fehlermeldungen zu loggen */
	private static Logger logger = Logger.getLogger(Resource.class);

	/** Verwaltung der BAZI-properties. */
	private static ResourceBundle resource;

	/** efaut Properties als FallBack Loesung */
	private static ResourceBundle defaultResource;

	/** String, der die aktuelle Sprache repraesentiert. Default ist "en" */
	private static String lang = "en";

	static
	{
		defaultResource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new UTF8Control());
		resource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new Locale(lang), new UTF8Control());
	}

	/** setzen der Sprache Achtung: Bereits gelesene String werden nicht geändert!!! Für eine komplette Übernahme ist ein Neustart des Programms erforderlich!
	 * @param l Kürzel der Sprache (de|en|fr|es|it) */
	public static void setLang(String l)
	{
		lang = l;
		try
		{
			resource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new Locale(l), new UTF8Control());
		}
		catch (NullPointerException e)
		{
			logger.error("Fehler beim Setzen des Resource Bundles:\nLocale = null", e);
		}
		catch (MissingResourceException e)
		{
			logger.error("Fehler beim Setzen des Resource Bundles:\nKeine Resource gefunden!", e);
		}
	}

	/** Rückgabe des zum Schlüssel key gehörenden Wertes aus den BAZI-properties.
	 * Wird ein Schlüssel nicht gefunden wird die erzeugte Exception nicht
	 * abgefangen. Dadurch sollen fehlende Schlüssel besser erkannt werden.
	 * 
	 * @param key Schlüssel
	 * @return Property zum übergebenen Schlüssel */
	public static String getString(String key)
	{
		try
		{
			return resource.getString(key);
		}
		catch (MissingResourceException mre)
		{
			return defaultResource.getString(key);
		}

	}


	private static class UTF8Control extends Control
	{
		@Override
		public ResourceBundle newBundle
				(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
						throws IllegalAccessException, InstantiationException, IOException
		{
			// The below is a copy of the default implementation.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			ResourceBundle bundle = null;
			InputStream stream = null;
			if (reload)
			{
				URL url = loader.getResource(resourceName);
				if (url != null)
				{
					URLConnection connection = url.openConnection();
					if (connection != null)
					{
						connection.setUseCaches(false);
						stream = connection.getInputStream();
					}
				}
			}
			else
			{
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null)
			{
				try
				{
					// Only this line is changed to make it to read properties files as UTF-8.
					bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
				}
				finally
				{
					stream.close();
				}
			}
			return bundle;
		}
	}
}
