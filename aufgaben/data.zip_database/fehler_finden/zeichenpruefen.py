import os

def read_file(file_path, encoding):
    """Liest eine Datei mit einem bestimmten Encoding und gibt den Inhalt zurück."""
    with open(file_path, 'r', encoding=encoding, errors='ignore') as f:
        return f.read()

def contains_keywords(content, keywords):
    """Überprüft, ob eines der Stichwörter im Text enthalten ist (ignoriert Groß- und Kleinschreibung)."""
    content_lower = content.lower()
    return any(keyword.lower() in content_lower for keyword in keywords)

def find_matching_files(root_folder, keyword_file, forbidden_keywords, characters_to_check):
    """Durchsucht alle .bazi-Dateien und überprüft auf das Vorhandensein verbotener Keywords und Zeichen."""
    # Laden der zu überprüfenden Zeichen
    with open(keyword_file, 'r', encoding='ISO-8859-1') as f:
        characters_to_check = set(f.read().strip())

    # Durchlaufe alle Dateien im Ordner und Unterordner
    for dirpath, dirnames, filenames in os.walk(root_folder):
        for filename in filenames:
            if filename.endswith(".bazi"):
                file_path = os.path.join(dirpath, filename)
                
                # Lies die Datei im UTF-8 Encoding
                try:
                    file_content_utf8 = read_file(file_path, encoding="utf-8")
                except UnicodeDecodeError:
                    # Wenn die Datei nicht im UTF-8 Encoding gelesen werden kann, überspringen
                    continue
                
                # Wenn keine verbotenen Stichwörter gefunden werden
                if not contains_keywords(file_content_utf8, forbidden_keywords):
                    # Versuche die Datei als ISO-8859-1 zu lesen
                    try:
                        file_content_iso = read_file(file_path, encoding="ISO-8859-1")
                    except UnicodeDecodeError:
                        continue
                    
                    # Suche nach den Zeichen aus der Liste
                    for char in characters_to_check:
                        if char in file_content_iso:
                            print(f"Gefunden in: {file_path}")
                            break

if __name__ == "__main__":
    # Nach dem Ordnerpfad und der Liste der Zeichen fragen
    root_folder = input("Gib den Pfad zum Ordner ein: ").strip()
    keyword_file = input("Gib den Pfad zur listezeichen.txt ein: ").strip()
    
    # Verbotene Stichwörter
    forbidden_keywords = ["greek", "griechenland", "utf-8", "utf8"]
    
    # Führe die Suche durch
    find_matching_files(root_folder, keyword_file, forbidden_keywords, set())
