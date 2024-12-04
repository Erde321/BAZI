# Die `.classpath`-Datei: Aufbau und Bedeutung

Die `.classpath`-Datei ist eine zentrale Konfigurationsdatei in Eclipse-Projekten, die den **Klassenpfad** definiert. Der Klassenpfad bestimmt, welche Ressourcen und Bibliotheken für ein Projekt verfügbar sind. Im Folgenden wird die Datei einmal vollständig angezeigt und dann Schritt für Schritt erklärt.

---

## Die `.classpath`-Datei

```xml
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry including="**/*.java" kind="src" output="target/classes" path="src">
		<attributes>
			<attribute name="optional" value="true"/>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="output" path="target/classes"/>
</classpath>
```

---

## Erklärung der Abschnitte

Die `.classpath`-Datei besteht aus mehreren `<classpathentry>`-Einträgen, die jeweils eine Ressource oder Konfiguration im Klassenpfad definieren.

### 1. **Quellordner (Source Folder)**

```xml
<classpathentry including="**/*.java" kind="src" output="target/classes" path="src">
	<attributes>
		<attribute name="optional" value="true"/>
		<attribute name="maven.pomderived" value="true"/>
	</attributes>
</classpathentry>
```

- **`kind="src"`**: Gibt an, dass es sich um einen Quellcode-Ordner handelt.
- **`path="src"`**: Der Quellordner des Projekts (hier: `src`).
- **`including="**/*.java"`**: Begrenzt die inkludierten Dateien auf alle `.java`-Dateien im Ordner und seinen Unterverzeichnissen.
- **`output="target/classes"`**: Kompilierte Klassen aus diesem Ordner werden in den Ordner `target/classes` geschrieben.
- **`<attributes>`**: Zusätzliche Eigenschaften:
  - `optional="true"`: Macht diesen Eintrag optional; bei fehlenden Dateien wird kein Fehler geworfen.
  - `maven.pomderived="true"`: Der Eintrag wurde automatisch aus der Maven-POM-Datei abgeleitet.

---

### 2. **JRE-Container**

```xml
<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8">
	<attributes>
		<attribute name="maven.pomderived" value="true"/>
	</attributes>
</classpathentry>
```

- **`kind="con"`**: Gibt an, dass dies ein Container-Eintrag ist.
- **`path="org.eclipse.jdt.launching.JRE_CONTAINER/.../JavaSE-1.8"`**:
  - Stellt die JDK-Version für das Projekt ein (hier: Java SE 8).
  - Der Container wird automatisch verwaltet und bietet Zugriff auf die JDK-Bibliotheken.
- **`<attributes>`**: 
  - `maven.pomderived="true"`: Zeigt an, dass dieser Eintrag durch die Maven-Integration erstellt wurde.

---

### 3. **Maven-Klassenpfad-Container**

```xml
<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
	<attributes>
		<attribute name="maven.pomderived" value="true"/>
	</attributes>
</classpathentry>
```

- **`kind="con"`**: Wieder ein Container-Eintrag.
- **`path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER"`**:
  - Dieser Container wird von **Maven (M2E)** verwaltet.
  - Er stellt sicher, dass Maven-Abhängigkeiten, die in der `pom.xml` definiert sind, automatisch in den Klassenpfad aufgenommen werden.
- **`<attributes>`**:
  - `maven.pomderived="true"`: Zeigt an, dass der Container aus der Maven-Konfiguration stammt.

---

### 4. **Ausgabeordner (Output Folder)**

```xml
<classpathentry kind="output" path="target/classes"/>
```

- **`kind="output"`**: Definiert den Zielordner für alle kompilierten Dateien.
- **`path="target/classes"`**: Gibt an, dass alle kompilierten Dateien des Projekts im Ordner `target/classes` gespeichert werden.

---


