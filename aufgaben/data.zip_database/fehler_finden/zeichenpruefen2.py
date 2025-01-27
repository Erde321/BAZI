import os

def main():
    # Eingabe für die Ordner- und Datei-Pfade
    folder_path = input("Geben Sie den Pfad des Ordners an: ").strip()
    txt_file_1 = input("Geben Sie den Pfad der ersten TXT-Datei an: ").strip()
    txt_file_2 = input("Geben Sie den Pfad der zweiten TXT-Datei an: ").strip()

    # Lesen der Fehlerzeichen aus den TXT-Dateien
    with open(txt_file_1, "r", encoding="ISO-8859-1") as file:
        error_chars_iso = set(file.read())

    with open(txt_file_2, "r", encoding="utf-8") as file:
        error_chars_utf8 = set(file.read())

    # Rekursives Durchsuchen des Ordners nach .bazi-Dateien
    for root, _, files in os.walk(folder_path):
        for file in files:
            if file.endswith(".bazi"):
                bazi_path = os.path.join(root, file)

                # Prüfen, ob die Datei die Kodierungsinformation enthält
                with open(bazi_path, "r", encoding="ISO-8859-1") as bazi_file:
                    content = bazi_file.read()

                if "=KODIERUNG= UTF-8" in content:
                    encoding = "utf-8"
                    error_chars = error_chars_utf8
                else:
                    encoding = "ISO-8859-1"
                    error_chars = error_chars_iso

                # Datei erneut in der richtigen Kodierung öffnen und prüfen
                try:
                    with open(bazi_path, "r", encoding=encoding) as bazi_file:
                        content = bazi_file.read()

                    # Prüfen auf Fehlerzeichen
                    if any(char in content for char in error_chars):
                        print(f"Fehlerhafte Datei gefunden: {bazi_path}")

                except (UnicodeDecodeError, FileNotFoundError) as e:
                    print(f"Fehler beim Verarbeiten der Datei {bazi_path}: {e}")

if __name__ == "__main__":
    main()





def utf8_chars_from_128_to_255():
    print("UTF-8 Zeichen von Codepunkt 128 bis 255:")
    for codepoint in range(128, 256):  # Unicode Codepunkte 128–255
        char = chr(codepoint)  # Unicode-Zeichen
        utf8_bytes = char.encode("utf-8")  # UTF-8 Bytes
        print(f"Codepoint {codepoint:3} (U+{codepoint:04X}): {char} - UTF-8 Bytes: {utf8_bytes}")

