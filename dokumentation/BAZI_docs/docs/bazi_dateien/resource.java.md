### **Resource.java**

Hier ist der vollständige Code von `Resource.java`. Die Klasse Resource wird als Speicher für die Resourcen im Projekt genutzt.
Hierfür wird static benutzt damit man ständig darauf zugreifen kann ohne eine Instanz davon zu erstellen.

---

```java
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

	/** Default Properties als Fallback-Lösung */
	private static ResourceBundle defaultResource;

	/** String, der die aktuelle Sprache repräsentiert. Default ist "en" */
	private static String lang = "en";

	static
	{
		defaultResource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new UTF8Control());
		resource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new Locale(lang), new UTF8Control());
	}

	/** Setzen der Sprache
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
```

---

### **Codeabschnitte und Erklärung**

#### 1. **Klassenbeschreibung und Package**
```java
package de.uni.augsburg.bazi;
```
- Definiert, dass die Klasse Teil des `de.uni.augsburg.bazi`-Pakets ist.
- Wird verwendet, um die Klassen zu organisieren und Namenskonflikte zu vermeiden.

---

#### 2. **Logger und Ressourcen-Definitionen**
```java
private static Logger logger = Logger.getLogger(Resource.class);
private static ResourceBundle resource;
private static ResourceBundle defaultResource;
private static String lang = "en";
```
- **Logger:** Verwendet `log4j`, um Fehler zu protokollieren.
- **`resource` und `defaultResource`:** 
  - `resource`: Speichert die aktuellen Spracheinstellungen.
  - `defaultResource`: Fallback für Standardressourcen, wenn spezifische Spracheinstellungen fehlen.
- **`lang`:** Speichert die aktuelle Sprache, Standard ist Englisch (`"en"`).

---

#### 3. **Statische Initialisierung**
```java
static
{
    defaultResource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new UTF8Control());
    resource = ResourceBundle.getBundle("de.uni.augsburg.bazi.bazi", new Locale(lang), new UTF8Control());
}
```
- Lädt die Standardressourcen (`defaultResource`) und die Ressource für die Standardsprache (`lang`).
- Verwendet `UTF8Control`, um sicherzustellen, dass die Property-Dateien UTF-8-kodiert sind.

---

#### 4. **Sprache setzen**
```java
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
```
- **Funktion:** Setzt die aktuelle Sprache.
- **Fehlerbehandlung:**
  - `NullPointerException`: Falls die übergebene Sprache `null` ist.
  - `MissingResourceException`: Falls die entsprechenden Ressourcen nicht gefunden werden.

---

#### 5. **Ressourcen abrufen**
```java
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
```
- Gibt den Wert eines Schlüssels (`key`) aus den geladenen Ressourcen zurück.
- Verwendet die Fallback-Ressourcen (`defaultResource`), wenn der Schlüssel nicht in `resource` vorhanden ist.

---

#### 6. **UTF-8-Unterstützung für Property-Dateien**

Die innere Klasse `UTF8Control` überschreibt die Standardimplementierung von `ResourceBundle.Control`, um sicherzustellen, dass Property-Dateien im **UTF-8-Format** gelesen werden. Dies ist notwendig, da die Standardimplementierung von Java Property-Dateien im ISO-8859-1-Format liest.

---

##### **Code**

```java
private static class UTF8Control extends Control
{
    @Override
    public ResourceBundle newBundle
            (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException
    {
        // Der Standard-Algorithmus zur Bildung des Ressourcennamens
        String bundleName = toBundleName(baseName, locale); // Basisname und Locale kombinieren
        String resourceName = toResourceName(bundleName, "properties"); // ".properties"-Datei anhängen
        ResourceBundle bundle = null;
        InputStream stream = null;

        if (reload)
        {
            // Ressourcen neu laden, um sicherzustellen, dass Änderungen berücksichtigt werden
            URL url = loader.getResource(resourceName);
            if (url != null)
            {
                URLConnection connection = url.openConnection();
                if (connection != null)
                {
                    connection.setUseCaches(false); // Verhindert, dass die Datei aus dem Cache geladen wird
                    stream = connection.getInputStream();
                }
            }
        }
        else
        {
            // Standard-Lademechanismus
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream != null)
        {
            try
            {
                // Ändert die Standardimplementierung: Liest die Datei im UTF-8-Format
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
            }
            finally
            {
                stream.close(); // Schließt den Stream, um Speicherlecks zu vermeiden
            }
        }

        return bundle; // Gibt das geladene ResourceBundle zurück
    }
}
```

---

#### **Detaillierte Erklärung des Codes**

1. **`newBundle`-Methode:**
   - Diese Methode wird aufgerufen, um eine neue Instanz eines `ResourceBundle` zu erstellen.
   - Sie überschreibt die Standardmethode aus `ResourceBundle.Control`.

2. **Erstellung des Ressourcennamens:**
   ```java
   String bundleName = toBundleName(baseName, locale);
   String resourceName = toResourceName(bundleName, "properties");
   ```
   - Der Basisname (`baseName`) wird mit der `Locale` kombiniert, um den Bundle-Namen (`bundleName`) zu erstellen, z. B.:
     - Basisname: `bazi`
     - Locale: `de` (Deutsch)
     - Ergebnis: `bazi_de`
   - Anschließend wird der Bundle-Name in einen Ressourcennamen (`resourceName`) übersetzt, der die Dateiendung `.properties` enthält, z. B. `bazi_de.properties`.

3. **Laden der Ressourcen:**
   - **Falls `reload` aktiv ist:**
     ```java
     URL url = loader.getResource(resourceName);
     URLConnection connection = url.openConnection();
     connection.setUseCaches(false);
     stream = connection.getInputStream();
     ```
     - Die Ressource wird neu geladen, indem eine neue `URLConnection` geöffnet wird.
     - `setUseCaches(false)`: Stellt sicher, dass die Datei immer von der Quelle gelesen wird, nicht aus dem Cache.

   - **Andernfalls (Standardlademodus):**
     ```java
     stream = loader.getResourceAsStream(resourceName);
     ```
     - Die Ressource wird mithilfe des Standard-ClassLoaders als Stream geladen.

4. **UTF-8-Kodierung sicherstellen:**
   ```java
   bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
   ```
   - Der Stream wird mit einem `InputStreamReader` umwickelt, der explizit die Kodierung auf `"UTF-8"` setzt.
   - Diese Zeile ist die wesentliche Änderung gegenüber der Standardmethode, die ISO-8859-1 verwendet.

5. **Schließen des Streams:**
   ```java
   finally
   {
       stream.close();
   }
   ```
   - Der InputStream wird im `finally`-Block geschlossen, um Speicherlecks zu vermeiden.

6. **Rückgabe des geladenen Bundles:**
   ```java
   return bundle;
   ```
   - Das `ResourceBundle`, das aus der Property-Datei geladen wurde, wird zurückgegeben.

---

