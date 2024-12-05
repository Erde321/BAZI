# `pom.xml` für BAZI

Dieses Dokument beschreibt, wie das Projekt **BAZI** mit Maven gebaut wird. Maven ist ein Build-Management-Tool, das die Verwaltung von Abhängigkeiten, das Kompilieren von Quellcode und das Erstellen von Artefakten wie `.jar`-Dateien automatisiert.

---

## Die `pom.xml`

### Der vollständige Code
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>bazi</groupId>
	<artifactId>bazi</artifactId>
	<version>2024.02</version>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	</properties>

	<build>
		<sourceDirectory>src</sourceDirectory>

		<resources>
			<resource>
				<directory>src</directory>
				<excludes>
					<exclude>**/*.java</exclude>
				</excludes>
				<filtering>true</filtering>
			</resource>
		</resources>

		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.1</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
					<encoding>UTF-8</encoding>
				</configuration>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-assembly-plugin</artifactId>
				<version>2.4</version>
				<configuration>
					<descriptor>jar.xml</descriptor>
					<archive>
						<manifest>
							<mainClass>de.uni.augsburg.bazi.Start</mainClass>
						</manifest>
					</archive>
					<finalName>${project.artifactId}</finalName>
					<appendAssemblyId>false</appendAssemblyId>
					<attach>false</attach>
				</configuration>
				<executions>
					<execution>
						<id>make-assembly</id>
						<phase>package</phase>
						<goals>
							<goal>single</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

		</plugins>

	</build>

	<dependencies>
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>1.2.12</version>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-math</artifactId>
			<version>2.1</version>
		</dependency>		
		<dependency>
  			<groupId>com.fasterxml.jackson.core</groupId>
  			<artifactId>jackson-databind</artifactId>
  			<version>2.9.7</version>
		</dependency>
	</dependencies>
</project>
```

---

### Projektinformationen

```xml
<modelVersion>4.0.0</modelVersion>
<groupId>bazi</groupId>
<artifactId>bazi</artifactId>
<version>2024.02</version>
```
- **`modelVersion`**: Gibt die Version des Maven-Modells an, hier `4.0.0`.
- **`groupId`**: Identifiziert die Organisation oder das Projekt eindeutig, z. B. `bazi`.
- **`artifactId`**: Der Name des generierten Artefakts (z. B. der `.jar`-Datei). Dies ist der spezifische Name des Projekts.
- **`version`**: Gibt die Version des Projekts an, z. B. `2024.02`. Sollte die Zeichenkette für die Version **"-b-"** enthalten, dann wird nach der start.java main-Funktion eine .jar mit Maven erstell, die ein zusätzliches Fenster mit log-Daten öffnet.

---

### Eigenschaften

```xml
<properties>
	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```
- Definiert wichtige Projektkonfigurationen, z. B. die Kodierung (`UTF-8`) für Quellcode und Ressourcen. 

---

### Ressourcen und Filtering

```xml
<resources>
	<resource>
		<directory>src</directory>
		<excludes>
			<exclude>**/*.java</exclude>
		</excludes>
		<filtering>true</filtering>
	</resource>
</resources>
```
- **`directory`**: Gibt den Speicherort der Ressourcen (z. B. Konfigurationsdateien) an. Hier: `src`.
- **`excludes`**: Schließt Dateien vom Typ `**/*.java` aus, da sie keine Ressourcen, sondern Quellcode sind.
- **`filtering`**: Erlaubt das Ersetzen von Platzhaltern in Ressourcen. Platzhalter wie `${project.version}` werden während des Build-Prozesses automatisch durch ihre Werte ersetzt.

---

### Maven Compiler Plugin

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<version>3.8.1</version>
	<configuration>
		<source>1.8</source>
		<target>1.8</target>
		<encoding>UTF-8</encoding>
	</configuration>
</plugin>
```
- Dieses Plugin wird verwendet, um den Quellcode zu kompilieren.
- **`source` und `target`**: Legen die Java-Version für den Quellcode und das Ziel fest (hier: Java 8).
- **`encoding`**: Gibt die Zeichenkodierung an (UTF-8).

---

### Maven Assembly Plugin

Das Maven Assembly Plugin wird verwendet, um spezielle Artefakte zu erstellen, wie z. B. ausführbare `.jar`-Dateien, die alle Abhängigkeiten und Ressourcen enthalten. Es ist besonders nützlich, um ein Java-Programm in einer einzigen Datei zu bündeln, die direkt ausgeführt werden kann.

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-assembly-plugin</artifactId>
	<version>2.4</version>
	<configuration>
		<descriptor>jar.xml</descriptor>
		<archive>
			<manifest>
				<mainClass>de.uni.augsburg.bazi.Start</mainClass>
			</manifest>
		</archive>
		<finalName>${project.artifactId}</finalName>
		<appendAssemblyId>false</appendAssemblyId>
		<attach>false</attach>
	</configuration>
	<executions>
		<execution>
			<id>make-assembly</id>
			<phase>package</phase>
			<goals>
				<goal>single</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

- **`descriptor`**:  
  Gibt die Datei an, die die Anweisungen für das Erstellen der Assembly enthält. In diesem Fall ist `jar.xml` spezifiziert, was bedeutet, dass die Details des Zusammenfügens (z. B. welche Dateien und Abhängigkeiten enthalten sein sollen) in dieser Datei definiert sind.

- **`archive`**:  
  Konfiguriert Metainformationen, die in die erstellte `.jar`-Datei eingebunden werden.  
  - **`manifest`**: Enthält die Metadaten des JAR-Manifests, wie z. B. die Angabe der Hauptklasse (`mainClass`). Diese Klasse enthält die `main`-Methode und definiert den Einstiegspunkt der Anwendung.

- **`finalName`**:  
  Legt den endgültigen Namen der erstellten Datei fest. Hier wird der Wert aus `${project.artifactId}` übernommen, der den Namen des Projekts (z. B. `bazi`) enthält.

- **`appendAssemblyId`**:  
  Standardmäßig fügt Maven der Assembly einen Suffix (z. B. `-assembly`) hinzu. Wenn `false` gesetzt ist, wird dieses Suffix unterdrückt, und der Dateiname bleibt unverändert.

- **`attach`**:  
  Gibt an, ob die Assembly dem Maven-Projekt als zusätzliches Artefakt angehängt werden soll. Wenn `false` gesetzt ist, wird das Artefakt erstellt, aber nicht automatisch an den Maven-Lifecycle (z. B. Deploy- oder Install-Phasen) angehängt.

---

#### `executions` im Detail

Die `executions`-Sektion definiert, wann und wie das Plugin während des Build-Lifecycles ausgeführt wird.

```xml
<execution>
	<id>make-assembly</id>
	<phase>package</phase>
	<goals>
		<goal>single</goal>
	</goals>
</execution>
```

- **`id`**:  
  Ein eindeutiger Bezeichner für die Konfiguration dieser Ausführung. Hier `make-assembly`.

- **`phase`**:  
  Bindet die Ausführung des Plugins an eine bestimmte Phase im Maven-Build-Lifecycle. In diesem Fall wird das Plugin in der `package`-Phase ausgeführt, wenn die Anwendung als `.jar`-Datei verpackt wird.

- **`goals`**:  
  Gibt die Ziele (engl. *goals*) an, die das Plugin ausführt.  
  - **`single`**: Baut eine einzelne Assembly basierend auf der `descriptor`-Datei (`jar.xml`).

---

Das Maven Assembly Plugin wird somit konfiguriert, um ein ausführbares `.jar` zu erstellen, das sowohl den Projektcode als auch die benötigten Abhängigkeiten enthält. Mit dieser Konfiguration kann das Projekt BAZI in einer einzigen, lauffähigen Datei bereitgestellt werden.

---

### Abhängigkeiten

```xml
<dependencies>
	<dependency>
		<groupId>log4j</groupId>
		<artifactId>log4j</artifactId>
		<version>1.2.12</version>
	</dependency>
	<dependency>
		<groupId>org.apache.commons</groupId>
		<artifactId>commons-math</artifactId>
		<version>2.1</version>
	</dependency>		
	<dependency>
  		<groupId>com.fasterxml.jackson.core</groupId>
  		<artifactId>jackson-databind</artifactId>
  		<version>2.9.7</version>
	</dependency>
</dependencies>
```
- **`log4j`**: Eine Logging-Bibliothek, die das Protokollieren von Nachrichten in der Anwendung ermöglicht.
- **`commons-math`**: Eine Sammlung von mathematischen und statistischen Funktionen.
- **`jackson-databind`**: Ermöglicht die Umwandlung von JSON-Daten in Java-Objekte und umgekehrt.
