# `org.eclipse.core.runtime.prefs`

Die Datei definiert grundlegende Einstellungen, insbesondere die Standardzeilenenden für Dateien im Projekt.

---

## Inhalt der Datei

```properties
eclipse.preferences.version=1
line.separator=\n
```

---

## Erklärung der Abschnitte

### 1. **Eclipse Preferences Version**

```properties
eclipse.preferences.version=1
```

- **Bedeutung**:
  - Diese Zeile gibt die Version des verwendeten Präferenzformats an. Hier ist die Version `1`.
  - Die Angabe sorgt dafür, dass Eclipse die Präferenzdatei korrekt interpretiert und kompatibel bleibt.

---

### 2. **Zeilenende-Einstellung**

```properties
line.separator=\n
```

- **Bedeutung**:
  - Diese Zeile definiert das Standard-Zeilenende für Dateien im Projekt.
  - **`\n`**:
    - Dies steht für einen **Unix/Linux-Stil Zeilenumbruch** (Line Feed, LF).
    - Dateien, die mit dieser Einstellung erstellt werden, nutzen LF als Zeilenumbruch.

---