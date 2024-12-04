# jar.xml - Maven Assembly Plugin Konfiguration

Die `jar.xml`-Datei wird im Maven-Build-Prozess verwendet, um ein **JAR-Archiv** zu erstellen, das alle erforderlichen Abhängigkeiten des Projekts enthält. Sie wird vom **Maven Assembly Plugin** genutzt unter plugins in pom.xml, um ein sogenanntes **„Fat JAR“** oder **„übergeordnetes JAR“** zu generieren, bei dem alle Abhängigkeiten, die für die Ausführung des Programms erforderlich sind, im JAR selbst enthalten sind. Diese Datei wird oft verwendet, wenn du ein JAR erstellen möchtest, das eigenständig ausführbar ist und keine externen Bibliotheken benötigt, um zu laufen.

## Der vollständige Code der `jar.xml`-Datei

```xml
<?xml version="1.0" encoding="UTF-8"?>

<assembly
    xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0 http://maven.apache.org/xsd/assembly-1.1.0.xsd">

    <id>jar-with-dependencies</id>

    <formats>
        <format>jar</format>
    </formats>

    <includeBaseDirectory>false</includeBaseDirectory>

    <dependencySets>
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <unpack>true</unpack>
            <scope>runtime</scope>
        </dependencySet>
    </dependencySets>

</assembly>
```

## Erklärung der einzelnen Abschnitte

### 1. XML-Deklaration und Namespaces

```xml
<?xml version="1.0" encoding="UTF-8"?>
<assembly
    xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.0 http://maven.apache.org/xsd/assembly-1.1.0.xsd">
```

- Die **XML-Deklaration** (`<?xml version="1.0" encoding="UTF-8"?>`) gibt an, dass es sich um eine XML-Datei handelt und verwendet UTF-8 als Zeichencodierung.
- Die **Namespaces** (`xmlns` und `xsi:schemaLocation`) definieren die Struktur und das Schema der Datei, sodass Maven weiß, wie es die Konfigurationsdatei interpretieren und verarbeiten soll. Diese sind notwendig, um die Konformität der Datei zu gewährleisten.

### 2. `id`

```xml
<id>jar-with-dependencies</id>
```

- Der **`id`-Tag** gibt dieser speziellen Assembly eine eindeutige ID. Hier heißt die ID `jar-with-dependencies`, was darauf hinweist, dass das JAR alle Abhängigkeiten des Projekts enthalten wird. Diese ID wird verwendet, um diese spezifische Assembly-Konfiguration im Maven-Prozess zu identifizieren und auszuführen.

### 3. `formats`

```xml
<formats>
    <format>jar</format>
</formats>
```

- Der **`formats`-Tag** gibt an, dass das Ergebnis dieser Assembly im **JAR-Format** vorliegen soll. Dies bedeutet, dass die Assembly ein JAR-Archiv erstellt, das dann als ausführbare Datei oder Bibliothek verwendet werden kann.

### 4. `includeBaseDirectory`

```xml
<includeBaseDirectory>false</includeBaseDirectory>
```

- Der **`includeBaseDirectory`-Tag** bestimmt, ob das Basisverzeichnis in das endgültige JAR-Archiv aufgenommen werden soll. In diesem Fall ist der Wert auf `false` gesetzt, was bedeutet, dass das Basisverzeichnis **nicht** in das JAR eingefügt wird. Normalerweise möchte man nur die relevanten Dateien und nicht das Verzeichnis selbst ins Archiv packen.

### 5. `dependencySets`

```xml
<dependencySets>
    <dependencySet>
        <outputDirectory>/</outputDirectory>
        <useProjectArtifact>true</useProjectArtifact>
        <unpack>true</unpack>
        <scope>runtime</scope>
    </dependencySet>
</dependencySets>
```

- In diesem Abschnitt wird konfiguriert, wie Abhängigkeiten in das JAR aufgenommen werden. Hier wird ein `dependencySet` definiert, der angibt, dass alle Laufzeit-Abhängigkeiten des Projekts in das JAR gepackt werden.

#### Details der Konfiguration:

- **`outputDirectory`**:  
  Der `outputDirectory` gibt an, in welchem Verzeichnis die Abhängigkeiten im JAR abgelegt werden sollen. Der Wert `/` bedeutet, dass die Abhängigkeiten im Root-Verzeichnis des JAR abgelegt werden.

- **`useProjectArtifact`**:  
  Dieser Wert gibt an, dass das Hauptartefakt des Projekts (das eigentliche JAR des Projekts) auch in das endgültige JAR gepackt werden soll. Dies ist besonders nützlich, wenn du das JAR zusammen mit den Abhängigkeiten bündeln möchtest.

- **`unpack`**:  
  Wenn `unpack` auf `true` gesetzt ist, werden die Abhängigkeiten **entpackt**, bevor sie in das endgültige JAR eingefügt werden. Das bedeutet, dass die Abhängigkeiten nicht als JAR-Dateien in das endgültige JAR gepackt werden, sondern ihre Klassen und Ressourcen direkt in das JAR-Archiv extrahiert werden.

- **`scope`**:  
  Der `scope` gibt den Gültigkeitsbereich der Abhängigkeiten an. In diesem Fall ist der Scope auf `runtime` gesetzt, was bedeutet, dass nur Abhängigkeiten, die für die **Laufzeit** des Programms erforderlich sind, in das endgültige JAR aufgenommen werden. Bibliotheken, die nur für die Kompilierung oder für Tests benötigt werden, werden nicht in das JAR aufgenommen.

