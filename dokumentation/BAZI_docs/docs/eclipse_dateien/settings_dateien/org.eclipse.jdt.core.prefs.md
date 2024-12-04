# Dokumentation: `org.eclipse.jdt.core.prefs`

Die Datei definiert spezifische Compiler- und Formatierungsoptionen für das Java Development Tools (JDT)-Plugin in Eclipse. Diese Datei wird verwendet, um das Verhalten des Java-Compilers sowie die Formatierungsregeln für den Code zu konfigurieren.

---

## Inhalt der Datei

```properties
eclipse.preferences.version=1
org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled
org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.8
org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve
org.eclipse.jdt.core.compiler.compliance=1.8
org.eclipse.jdt.core.compiler.debug.lineNumber=generate
org.eclipse.jdt.core.compiler.debug.localVariable=generate
org.eclipse.jdt.core.compiler.debug.sourceFile=generate
org.eclipse.jdt.core.compiler.problem.assertIdentifier=error
org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures=disabled
org.eclipse.jdt.core.compiler.problem.enumIdentifier=error
org.eclipse.jdt.core.compiler.problem.forbiddenReference=warning
org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures=ignore
org.eclipse.jdt.core.compiler.processAnnotations=disabled
org.eclipse.jdt.core.compiler.release=disabled
org.eclipse.jdt.core.compiler.source=1.8
org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member=insert
```

---

## Erklärung der Abschnitte

### 1. **Eclipse Preferences Version**

```properties
eclipse.preferences.version=1
```

- **Bedeutung**:
  - Diese Zeile gibt die Version des verwendeten Präferenzformats an. Die Version `1` stellt sicher, dass Eclipse diese Datei korrekt interpretiert.

---

### 2. **Compiler-Einstellungen**

Die folgenden Einstellungen betreffen den Java-Compiler und bestimmen, wie der Quellcode kompiliert wird:

#### 2.1. **Inline-JSR Bytecode**

```properties
org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled
```

- **Bedeutung**:
  - Diese Einstellung aktiviert die Inline-Erstellung von JSR (Jump Subroutine) Bytecode. Diese Technik wird verwendet, um die Performance der erzeugten Bytecode-Dateien zu optimieren.

#### 2.2. **Zielplattform für den Bytecode**

```properties
org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.8
```

- **Bedeutung**:
  - Diese Einstellung legt fest, dass der generierte Bytecode auf **Java 8** (1.8) abzielt. Dies bedeutet, dass der Code mit den Features und der API von Java 8 kompatibel ist.

#### 2.3. **Behandlung von nicht verwendeten lokalen Variablen**

```properties
org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve
```

- **Bedeutung**:
  - Diese Einstellung sorgt dafür, dass nicht verwendete lokale Variablen im Bytecode beibehalten werden. Es werden keine Optimierungen durchgeführt, um ungenutzte Variablen zu entfernen.

#### 2.4. **Compiler-Konformität**

```properties
org.eclipse.jdt.core.compiler.compliance=1.8
```

- **Bedeutung**:
  - Diese Einstellung gibt an, dass der Java-Compiler den **Java 8**-Standard befolgen soll. Der Compiler wird Warnungen oder Fehler erzeugen, wenn der Quellcode nicht mit der Java 8-Spezifikation übereinstimmt.

#### 2.5. **Generierung von Debugging-Informationen**

```properties
org.eclipse.jdt.core.compiler.debug.lineNumber=generate
org.eclipse.jdt.core.compiler.debug.localVariable=generate
org.eclipse.jdt.core.compiler.debug.sourceFile=generate
```

- **Bedeutung**:
  - Diese Einstellungen sorgen dafür, dass beim Kompilieren **Zeilenummern**, **lokale Variablen** und **Quellcode-Dateinamen** im Bytecode generiert werden. Diese Debugging-Informationen erleichtern das Debuggen des Programms.

---

### 3. **Compiler-Warnungen und -Fehler**

Die folgenden Einstellungen definieren, wie der Compiler auf verschiedene Probleme reagiert:

#### 3.1. **`assert`-Identifier als Fehler**

```properties
org.eclipse.jdt.core.compiler.problem.assertIdentifier=error
```

- **Bedeutung**:
  - Diese Einstellung sorgt dafür, dass die Verwendung des reservierten Schlüsselworts `assert` als Fehler betrachtet wird. In einigen Java-Versionen ist `assert` ein Schlüsselwort, und in anderen ist es ein Keyword nur, wenn die Assertion aktiviert ist.

#### 3.2. **Deaktivierung von Preview-Features**

```properties
org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures=disabled
```

- **Bedeutung**:
  - Diese Einstellung deaktiviert die Unterstützung für **Preview-Features** von Java. Preview-Features sind experimentelle Features, die in zukünftigen Java-Versionen standardisiert werden könnten, aber noch nicht vollständig stabil sind.

#### 3.3. **`enum`-Identifier als Fehler**

```properties
org.eclipse.jdt.core.compiler.problem.enumIdentifier=error
```

- **Bedeutung**:
  - Diese Einstellung behandelt die Verwendung des reservierten Keywords `enum` als Fehler. `enum` ist ein Schlüsselwort, das in neueren Java-Versionen eingeführt wurde, um Aufzählungstypen zu definieren.

#### 3.4. **Verbotene Referenzen als Warnung**

```properties
org.eclipse.jdt.core.compiler.problem.forbiddenReference=warning
```

- **Bedeutung**:
  - Diese Einstellung sorgt dafür, dass die Verwendung von **verbotenen Referenzen** (z. B. von bestimmten veralteten oder nicht erlaubten Klassen) als Warnung angezeigt wird, aber nicht als Fehler.

#### 3.5. **Fehlerbehandlung von Preview-Features**

```properties
org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures=ignore
```

- **Bedeutung**:
  - Diese Einstellung legt fest, dass **Preview-Features** ignoriert werden sollen, ohne eine spezielle Meldung oder Warnung auszulösen.

---

### 4. **Annotationen**

```properties
org.eclipse.jdt.core.compiler.processAnnotations=disabled
```

- **Bedeutung**:
  - Diese Einstellung deaktiviert die Verarbeitung von **Annotationen**. Wenn aktiviert, werden Annotationen während der Kompilierung verarbeitet, um z. B. Quellcode zu generieren.

---

### 5. **Java-Release und Quellversion**

```properties
org.eclipse.jdt.core.compiler.release=disabled
org.eclipse.jdt.core.compiler.source=1.8
```

- **Bedeutung**:
  - **`release`**: Diese Einstellung ist auf `disabled` gesetzt, was bedeutet, dass keine spezifische Release-Version des JDK erzwungen wird.
  - **`source`**: Diese Einstellung gibt an, dass der Quellcode auf **Java 8** (1.8) basiert, was bedeutet, dass der Code mit den Funktionen und APIs von Java 8 kompatibel ist.

---

### 6. **Formatierungseinstellungen**

```properties
org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_member=insert
```

- **Bedeutung**:
  - Diese Einstellung sorgt dafür, dass nach **Annotationen auf Mitgliedern** (z. B. Methoden oder Felder) eine neue Zeile eingefügt wird, um den Code besser lesbar und strukturiert zu halten.

---
