def utf8_chars_from_128_to_255():
    print("UTF-8 Zeichen von Codepunkt 128 bis 255:")
    for codepoint in range(128, 256):  # Unicode Codepunkte 128–255
        char = chr(codepoint)  # Unicode-Zeichen
        utf8_bytes = char.encode("utf-8")  # UTF-8 Bytes
        print(f"Codepoint {codepoint:3} (U+{codepoint:04X}): {char} - UTF-8 Bytes: {utf8_bytes}")

if __name__ == "__main__":
    utf8_chars_from_128_to_255()
