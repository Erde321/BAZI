# Start.java

Diese Seite beschreibt die Funktionsweise und Struktur der `Start.java`-Datei des Projekts BAZI. Die Start.java enthält die main-Funktion für das Projekt.

## Gesamter Code

```java
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
        Jackson.readJSON(jackson);
        
        if(Language.getStartDialog()) 
        {    
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
        
        if(!Language.getStartDialog())
            Resource.setLang(Language.getLanguage());
        else
            Jackson.writeJSON(GUIConstraints.getFontSize(0), GUIConstraints.getFontSize(1), GUIConstraints.getFontSize(2), GUIConstraints.getFontSize(3), 
                    Language.getStartDialog(), Language.getLanguage());
        new RoundFrame("Version " + VersionControl.getVersion(), max);
    }
}
```

---

## Erklärung der Codeabschnitte

### Paket- und Importdeklarationen

```java
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
```

Diese Sektion definiert:

- **Paketzugehörigkeit**: `de.uni.augsburg.bazi` enthält die `Start`-Klasse.
- **Log4j-Imports**: Für das Logging .
- **GUI-Klassen**: Diese unterstützen GUI-Komponenten und JSON-Datenverarbeitung.

---

### Klassen- und Logger-Definition

```java
public class Start
{
    /** Logger dieser Klasse */
    private static Logger logger = Logger.getLogger(Start.class);
    
    /** Instanz zum Einlesen aus der JSON */
    private static Jackson jackson;
```

Der Klasse `Start` werden die Attribute logger und jackson mitgegeben:

 - **Definiert einen Logger** zur Verfolgung von Programmausgaben.
 - **Initialisiert `jackson`** für JSON-basierte Konfigurationsdaten.

---

### Main-Methode

#### Hauptablauf

```java
public static void main(String[] args)
{
    if (VersionControl.getVersion().indexOf("b") != -1)
    {
        // Testversion
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

    ...
}
```

### **Unterscheidung zwischen Test- und Release-Version**
Der Code überprüft, ob sich die Anwendung in einer Test- oder einer Release-Version befindet, basierend auf der Version, die durch `VersionControl.getVersion()` geliefert wird.

- **Bedingung: `if (VersionControl.getVersion().indexOf("b") != -1)`**
  - Hier wird geprüft, ob die Versionszeichenkette ein `-b-` enthält. 
    - `-b-` steht für eine **Testversion**.
    - Wenn die Bedingung wahr ist, wird der Testlogger eingerichtet.
  - Andernfalls wird der Logger im Release-Modus deaktiviert.

  VersionControl ist eine Klasse mit static Attributen und wird als Singleton genutzt, um die Version des Projekts zu verwalten.

---

### **Fall 1: Testversion**

```java
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
```

- **Logging-Konfiguration mit `log4j.properties`:**
  - Es wird versucht, die Logging-Konfiguration aus der Datei `log4j.properties` zu laden:
    1. **`Start.class.getResourceAsStream("log4j.properties")`:** Lädt die Datei aus den Ressourcen der Anwendung.
    2. **`props.load(is)`:** Liest die Konfiguration in ein `Properties`-Objekt ein.
    3. **`PropertyConfigurator.configure(props)`:** Konfiguriert Log4j basierend auf den geladenen Eigenschaften.

- **Erfolgreiches Logging (Info-Meldungen):**
  - Wenn die Konfiguration erfolgreich ist, werden Informationen ins Log geschrieben:
    - **Pfad zur Logging-Konfiguration:** `src\\de\\uni\\augsburg\\bazi\\log4j.properties`.
    - **Aktuelles Logging-Level:** Gibt an, bis zu welchem Detailgrad geloggt wird.

- **Fang des Fehlers:**
  - Falls ein Fehler auftritt (z. B. Datei nicht gefunden), wird ein einfacher Fallback-Logger eingerichtet:
    1. **`ConsoleAppender`:** Log-Ausgabe wird auf die Konsole geleitet.
    2. **`SimpleLayout`:** Log-Nachrichten werden in einem einfachen Format dargestellt.
    3. **`Level.DEBUG`:** Aktiviert detaillierte Debug-Informationen.

---

### **Fall 2: Release-Version**

```java
Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
Logger.getRootLogger().setLevel(Level.OFF);
```

- **Standard-Logger:**
  - In der Release-Version wird ein einfacher `ConsoleAppender` verwendet, der auf die Konsole schreibt.
  
- **Logging deaktivieren:**
  - Das Logging-Level wird auf **`OFF`** gesetzt, wodurch keine Log-Nachrichten ausgegeben werden.
  - Dies dient dazu, die Ausgabe in der Produktionsumgebung minimal zu halten, um Leistung zu optimieren und sensible Informationen nicht preiszugeben.


---

### **Verarbeitung von Programmargumenten**

```java
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
```

#### **1. Protokollierung der Programmargumente**
- **Bedingung:** `Start.logger.isTraceEnabled()`
  - Prüft, ob das TRACE-Logging-Level aktiv ist. Also falls Logging genutzt wird, dann sollen die Übergabeparameter beim Aufruf des Projekts geloggt werden.
  - Wenn nicht aktiv, wird dieser Code-Block übersprungen.

- **Verarbeitung der Argumente:**
  - Die Schleife iteriert über die Elemente des Arrays `args`.
  - Jedes Argument wird in Anführungszeichen (`"`) eingeschlossen und durch ein `;` getrennt in die Variable `parameters` geschrieben.

- **Protokollierung:**
  - Der Logger gibt die Anzahl der Argumente (`args.length`) und deren Werte aus, z. B.:
    ```
    Programm durch Klasse Start aufgerufen mit 2 Parametern: "en" ; "max" ;
    ```

---

### **Initialisierung der Konfiguration**

```java
boolean max = false;

// Liest die JSON-Datei ein und lädt Konfigurationsdaten
Jackson.readJSON(jackson);

// Überprüfen, ob der Startdialog aktiviert ist
if (Language.getStartDialog()) 
{    
    // Fall 1: Keine Programmargumente übergeben
    if (args.length == 0) 
    {
        // Öffnet den Startdialog
        new StartDialog();
    } 
    else 
    {
        boolean setLang = false;

        // Prüfen des ersten Arguments
        if (args[0].equals("max")) 
        {
            max = true; // Aktiviert den Maximierungsmodus
        } 
        else 
        {
            Resource.setLang(args[0]); // Setzt die Sprache basierend auf dem ersten Argument
            setLang = true;
        }

        // Prüfen des zweiten Arguments (falls vorhanden)
        if (args.length > 1) 
        {
            if (args[1].equals("max")) 
            {
                max = true; // Aktiviert den Maximierungsmodus
            } 
            else 
            {
                Resource.setLang(args[1]); // Setzt die Sprache basierend auf dem zweiten Argument
                setLang = true;
            }
        }

        // Wenn keine Sprache festgelegt wurde, wird der Startdialog geöffnet
        if (!setLang) 
        {
            new StartDialog();
        }
    }
} 
else 
{
    // Fall 2: Startdialog ist deaktiviert – Sprache aus JSON-Konfiguration laden
    Resource.setLang(Language.getLanguage());
}

// Aktuelle Spracheinstellungen und UI-Konfiguration in die JSON-Datei schreiben
if (Language.getStartDialog()) 
{
    Jackson.writeJSON(
        GUIConstraints.getFontSize(0), 
        GUIConstraints.getFontSize(1), 
        GUIConstraints.getFontSize(2), 
        GUIConstraints.getFontSize(3), 
        Language.getStartDialog(), 
        Language.getLanguage()
    );
}

// Hauptfenster der Anwendung starten
new RoundFrame("Version " + VersionControl.getVersion(), max);
```

### **Erklärungen zu den Codeabschnitten**
1. **JSON-Verarbeitung:**
   - `Jackson.readJSON(jackson)` liest die Konfigurationsdatei preferences.json aus und speichert den Inhalt im Attribut der start-Klasse "jackson". Wenn keine preferences.json Datei gefunden wurde, werden default-Werte geladen.
   
2. **Prüfen des Startdialogs:**
   - **Aktivierter Startdialog (`Language.getStartDialog() == true`):**
     
     Wenn noch kein Startfenster gestartet wurde, dann:
     - Ohne Argumente wird der Startdialog direkt geöffnet.
     - Mit Argumenten:
       - `"max"` im ersten oder zweiten Argument aktiviert den Vollbildmodus.
       - Andere Argumente setzen die Sprache.
       - Falls keine Sprache festgelegt wird, öffnet sich der Startdialog, wie wenn keine Argumente mitgegeben wurden.

   - **Deaktivierter Startdialog (`Language.getStartDialog() == false`):**
     - Die Sprache wird direkt aus den JSON-Einstellungen geladen.

     #### StartDialog()

     Die Methode StartDialog aus der Klasse StartDialog, öffnet ein Fenster, wo man die Sprache auswählen kann. In dieser Methode wird die Sprache in die Resource und Language Klasse geschrieben.

3. **Einstellungen speichern:**
   - Wenn der Startdialog aktiviert ist, speichert `Jackson.writeJSON(...)` die aktuellen UI- und Spracheinstellungen.

4. **Hauptfenster starten:**
   - Mit `new RoundFrame(...)` wird das Hauptfenster geöffnet. Die Größe des Fensters hängt vom `max`-Flag ab:
     - **`max == true`:** Fenster wird maximiert.
     - **Sonst:** Normales Fenster.