# `org.eclipse.core.resources.prefs`

Die Datei definiert verschiedene Projekteinstellungen, insbesondere **Kodierungen** für bestimmte Dateien und Ordner.

---

## Inhalt der Datei

```properties
eclipse.preferences.version=1
encoding//src/de/uni/augsburg/bazi/bazi.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_de.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_en.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_es.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_fr.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_it.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/log4j.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/version.properties=UTF-8
encoding/<project>=UTF-8
encoding/src=UTF-8
```

---

## Erklärung der Abschnitte

### 1. **Eclipse Preferences Version**

```properties
eclipse.preferences.version=1
```

- **Bedeutung**:
  - Diese Zeile gibt die Version des verwendeten Präferenzformats an. Die aktuelle Version ist `1`.
  - Sie dient zur Kompatibilität mit verschiedenen Eclipse-Versionen.

---

### 2. **Kodierung für einzelne Dateien**

```properties
encoding//src/de/uni/augsburg/bazi/bazi.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_de.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_en.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_es.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_fr.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/bazi_it.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/log4j.properties=UTF-8
encoding//src/de/uni/augsburg/bazi/version.properties=UTF-8
```

- **Bedeutung**:
  - Jede Zeile definiert die Kodierung für eine spezifische Datei im Projekt.
  - **Kodierung**: UTF-8 ist ein Standard für Unicode, der sicherstellt, dass verschiedene Sprachen und Sonderzeichen korrekt dargestellt werden.

---

### 3. **Kodierung für das Projekt**

```properties
encoding/<project>=UTF-8
```

- **Bedeutung**:
  - Diese Zeile legt die Standardkodierung für das gesamte Projekt fest.
  - `UTF-8` wird hier als Standard verwendet, was bedeutet, dass alle Dateien, die nicht explizit anders angegeben sind, in UTF-8 behandelt werden.

---

### 4. **Kodierung für den Quellordner**

```properties
encoding/src=UTF-8
```

- **Bedeutung**:
  - Diese Zeile legt die Kodierung für den Quellordner `src` fest.
  - Alle Dateien in diesem Ordner werden standardmäßig mit der Kodierung UTF-8 behandelt.

---
