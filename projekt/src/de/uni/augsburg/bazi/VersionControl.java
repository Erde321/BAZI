package de.uni.augsburg.bazi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** <b>Title:</b> Klasse VersionControl<br>
 * <b>Description:</b> Zum Überprüfen, ob eine neue Version verfügbar ist<br>
 * <b>Copyright:</b> Copyright (c) 2000-20010<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2011-01-06
 * @author Marco Schumacher */
public class VersionControl 
{
	/** Jahr für Copyright: 2002-YEAR */
	public static final String YEAR = "2019";

	/** aktuelle Versionsnummer */
	private static volatile String VERSION = null;

	public static String getVersion()
	{
		if (VERSION != null)
			return VERSION;

		synchronized (VersionControl.class)
		{
			if (VERSION != null)
				return VERSION;

			InputStream stream = null;
			try
			{
				stream = VersionControl.class.getResourceAsStream("version.properties");
				Properties prop = new Properties();
				prop.load(stream);

				VERSION = prop.getProperty("version");
			}
			catch (IOException e)
			{}
			finally
			{
				if (stream != null)
					try
					{
						stream.close();
					}
					catch (IOException e)
					{}
			}
		}
		return VERSION;
	}
}
