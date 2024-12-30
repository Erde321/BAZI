# Fehler in den BAZI Dateien der Database

Die Database besteht aus mehreren Bazi Dateien, welche entweder in UTF-8, ISO-8859-1 oder ISO-8859-7 kodiert sind. Beim Erstellen dieser können Fehler passieren durch das Schreiben einer BAZI Datei in einer anderen Kodierung als vorgesehen. Zum Beispiel eine Datei geschrieben in UTF-8, aber sie wird als ISO-8859-1 interpretiert.

Das Python Programm in diesem Unterordner soll solche Fehler in Dateien, welche als ISO-8859-1 ausgelegt sind erkennen. Dabei werden UTF-8 und ISO-8859-7 Dateien ignoriert und es wird nach Zeichen gesucht, welche als Fehler bei ISO-8859-1 erscheinen können.

Hierbei wurde davon ausgegangen, dass es größtenteils Fehler sind, die dadurch stammen, dass die Datei in UTF-8 geschrieben wurden, aber als ISO-8859-1 interpretiert werden. Die Entscheidung dazu stammt dadurch, dass genau dieser Fehler mir passiert ist.

UTF-8 und ISO-8859-1 teilen einen Teil ihrer Zeichen, aber unterscheiden sich bei Umlauten und Zeichen, welche in der deutschen Sprache nicht auftauchen und dieser Unterschied wird zum Finden der Fehler genutzt.

Die Datei 'zu_bearbeitende_dateien.md' listet die Fehler auf und wie man diese verbessern kann.

---

Ich habe keine Aufgabe dafür bekommen und es aus Eigeninitiative erstellt, da mir ein solcher Fehler beim Einfügen in die Database aufgefallen ist.