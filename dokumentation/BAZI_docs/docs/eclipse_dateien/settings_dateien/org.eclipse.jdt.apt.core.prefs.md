# `org.eclipse.jdt.apt.core.prefs`

Die Datei definiert Einstellungen, die mit der Annotation Processing Tool (APT)-Integration in Eclipse verbunden sind. Diese Datei steuert, ob und wie Annotationen während des Kompilierungsprozesses verarbeitet werden.

## Annotationen

**Annotationen** in Java sind eine Art von Metadaten, die zusätzliche Informationen über den Code bereitstellen, ohne die eigentliche Logik des Programms zu verändern. Sie ermöglichen es, bestimmte Verhaltensweisen zur Kompilierzeit oder zur Laufzeit zu steuern, ohne dass man den Code direkt ändern muss.

### Wichtige Punkte zu Annotationen:
1. **Definition**:
   - Annotationen sind spezielle Markierungen im Code, die durch das `@`-Zeichen gefolgt von einem Namen definiert werden.
   - Sie können an Klassen, Methoden, Feldern, Parametern und anderen Code-Elementen angebracht werden.

2. **Syntax**:
   - Eine einfache Annotation sieht folgendermaßen aus:
     ```java
     @Override
     public String toString() {
         return "Beispiel";
     }
     ```
     In diesem Fall wird die `@Override`-Annotation verwendet, um dem Compiler zu signalisieren, dass die Methode `toString` eine Methode aus der übergeordneten Klasse überschreibt.

---

## Inhalt der Datei

```properties
eclipse.preferences.version=1
org.eclipse.jdt.apt.aptEnabled=false
```

---

## Erklärung der Abschnitte

### 1. **Eclipse Preferences Version**

```properties
eclipse.preferences.version=1
```

- **Bedeutung**:
  - Diese Zeile gibt die Version des verwendeten Präferenzformats an. Die aktuelle Version ist `1`.
  - Diese Einstellung stellt sicher, dass Eclipse das Format korrekt interpretiert und mit zukünftigen Versionen kompatibel bleibt.

---

### 2. **Annotation Processing aktivieren/deaktivieren**

```properties
org.eclipse.jdt.apt.aptEnabled=false
```

- **Bedeutung**:
  - Diese Einstellung steuert, ob **Annotation Processing** für das Projekt aktiviert oder deaktiviert ist.
  - **`false`** bedeutet, dass Annotation Processing **deaktiviert** ist.
  - Wenn Annotation Processing aktiviert wäre (`true`), würde Eclipse während des Kompilierungsprozesses automatisch nach Annotationen suchen und die entsprechenden Generierungsprozesse durchführen.

---
