
# VersionControl

## Einführung

Die `VersionControl`-Klasse dient dazu, zu überprüfen, ob eine neue Version der Software verfügbar ist. Sie liest die aktuelle Versionsnummer aus einer `version.properties`-Datei.

---

## Gesamter Code

```java
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
```

---

## Code-Abschnitte


### 1. **Konstante `YEAR`**

```java
public static final String YEAR = "2019";
```

- **Beschreibung**: Eine Konstante, die das aktuelle Copyright-Jahr definiert.

---

### 2. **Variable `VERSION`**

```java
private static volatile String VERSION = null;
```

- **Beschreibung**: 
  - `VERSION` ist `volatile`, um sicherzustellen, dass mehrere Threads die richtige Version lesen und schreiben.
  - Initialisiert als `null` und wird später mit dem Inhalt aus der Datei `version.properties` befüllt.

---

### 3. **Methode `getVersion()`**

```java
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
        {
            // Fehlerbehandlung bei der Eingabe/Ausgabe (z.B. wenn die Datei nicht gefunden wird)
        }
        finally
        {
            if (stream != null)
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                    // Fehlerbehandlung beim Schließen des Streams (z.B. bei IO-Problemen)
                }
        }
    }
    return VERSION;
}
```

- **Beschreibung**: Die Methode `getVersion()` gibt die aktuelle Version der Anwendung zurück. Sie prüft zuerst, ob die Version bereits geladen wurde und lädt sie andernfalls aus einer Konfigurationsdatei namens `version.properties`.

- **Schritte**:
  1. Prüft, ob `VERSION` bereits geladen ist.
  2. Wenn nicht, wird der Zugriff auf die Methode synchronisiert, um sicherzustellen, dass nur ein Thread die Version gleichzeitig lädt.
  3. Liest die `version.properties`-Datei ein, um die Versionsnummer zu extrahieren.
  4. Stellt sicher, dass der InputStream korrekt geschlossen wird, auch im Falle eines Fehlers.

