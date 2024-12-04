
# `.project` für Bazi

Die `.project`-Datei ist eine zentrale Konfigurationsdatei für Eclipse-Projekte. Sie enthält Metadaten, die von Eclipse genutzt werden, um das Projekt zu verwalten und zu strukturieren.

## Die `.project`-Datei

```xml
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
    <name>Bazi</name>
    <comment>@key 32303037303431392D313030302042617A692F43687269737469616E </comment>
    <projects>
    </projects>
    <buildSpec>
        <buildCommand>
            <name>org.eclipse.jdt.core.javabuilder</name>
            <arguments>
            </arguments>
        </buildCommand>
        <buildCommand>
            <name>com.soyatec.uml.std.Builder</name>
            <arguments>
            </arguments>
        </buildCommand>
        <buildCommand>
            <name>org.eclipse.m2e.core.maven2Builder</name>
            <arguments>
            </arguments>
        </buildCommand>
    </buildSpec>
    <natures>
        <nature>org.eclipse.m2e.core.maven2Nature</nature>
        <nature>org.eclipse.jem.workbench.JavaEMFNature</nature>
        <nature>org.eclipse.jdt.core.javanature</nature>
        <nature>org.eclipse.jem.beaninfo.BeanInfoNature</nature>
        <nature>com.soyatec.uml.std.Nature</nature>
    </natures>
    <filteredResources>
        <filter>
            <id>1733217840452</id>
            <name></name>
            <type>30</type>
            <matcher>
                <id>org.eclipse.core.resources.regexFilterMatcher</id>
                <arguments>node_modules|\.git|__CREATED_BY_JAVA_LANGUAGE_SERVER__</arguments>
            </matcher>
        </filter>
    </filteredResources>
</projectDescription>
```

---

## Erläuterung einzelner Abschnitte

### 1. `<?xml version="1.0" encoding="UTF-8"?>`
- **Bedeutung**: Deklariert die XML-Version und die Zeichencodierung (UTF-8).
- **Relevanz**: Sicherstellt, dass die Datei von Tools korrekt gelesen wird.

---

### 2. `<projectDescription>`
- **Bedeutung**: Hauptcontainer, der alle Projektinformationen zusammenfasst.

#### 2.1. `<name>`
```xml
<name>Bazi</name>
```
- **Beschreibung**: Der Name des Projekts, wie er in Eclipse angezeigt wird.
- **Relevanz**: Identifiziert das Projekt innerhalb der IDE.

#### 2.2. `<comment>`
```xml
<comment>@key 32303037303431392D313030302042617A692F43687269737469616E </comment>
```
- **Beschreibung**: Ein Kommentarfeld, das zusätzliche Metadaten enthalten kann.
- **Beispielinhalt**: Eine Schlüssel-/Kennzeichnungsinformation des Projekts. //TODO: Besser beschreiben was es ist und wofür es genutzt wird

#### 2.3. `<projects>`
```xml
<projects>
</projects>
```
- **Beschreibung**: Hier könnten abhängige Projekte aufgelistet sein. In diesem Fall ist der Abschnitt leer.
- **Relevanz**: Wird genutzt, wenn das aktuelle Projekt andere Projekte referenziert.

---

### 3. `<buildSpec>`
- **Bedeutung**: Enthält die Konfiguration der Build-Prozesse des Projekts.

#### 3.1. `<buildCommand>`
```xml
<buildCommand>
    <name>org.eclipse.jdt.core.javabuilder</name>
    <arguments>
    </arguments>
</buildCommand>
```
- **Beschreibung**: Definiert einen Build-Schritt.
- **Beispiel-Build-Kommandos**:
  - `org.eclipse.jdt.core.javabuilder`: Kompiliert den Java-Code.
  - `com.soyatec.uml.std.Builder`: Ein spezifischer UML-Builder. UML ist eine Gruppe an Diagrammen und ein UML-Builder baut diese/nutzt diese, um anhand dieser etwas zu bauen?
  - `org.eclipse.m2e.core.maven2Builder`: Unterstützt Maven-Builds.

---

### 4. `<natures>`
- **Bedeutung**: Bestimmt die "Natur" des Projekts, also welche Plugins und Funktionen aktiviert sind.

#### 4.1. Auflistung der Natures:
```xml
<nature>org.eclipse.m2e.core.maven2Nature</nature>
<nature>org.eclipse.jem.workbench.JavaEMFNature</nature>
<nature>org.eclipse.jdt.core.javanature</nature>
<nature>org.eclipse.jem.beaninfo.BeanInfoNature</nature>
<nature>com.soyatec.uml.std.Nature</nature>
```
- **Erläuterung**:
  - `org.eclipse.m2e.core.maven2Nature`: Aktiviert Maven-Funktionalität.
  - `org.eclipse.jem.workbench.JavaEMFNature`: Unterstützt EMF (Eclipse Modeling Framework). Eine Funktion um Datenmodelle (strukturierte Daten in Form von UML oder Klassen zu bearbeiten)
  - `org.eclipse.jdt.core.javanature`: Definiert das Projekt als Java-Projekt.
  - `org.eclipse.jem.beaninfo.BeanInfoNature`: Java-Beans-Integration. JBI ist eine Klasse, die normalerweise in der GUI verwendet wird, um auf Ereignisse reagieren zu können.
  - `com.soyatec.uml.std.Nature`: UML-Unterstützung.

---

### 5. `<filteredResources>`
- **Bedeutung**: Definiert Filter für Ressourcen, die Eclipse ignorieren soll.

#### 5.1. Beispiel für Filter:
```xml
<matcher>
    <id>org.eclipse.core.resources.regexFilterMatcher</id>
    <arguments>node_modules|\.git|__CREATED_BY_JAVA_LANGUAGE_SERVER__</arguments>
</matcher>
```
- **Beschreibung**:
  - Filtert Ressourcen basierend auf regulären Ausdrücken.
  - Hier werden z. B. `node_modules`, `.git`-Ordner und bestimmte temporäre Dateien ignoriert.

---

