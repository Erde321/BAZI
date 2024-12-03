package de.uni.augsburg.bazi.gui;

/** <b>Title:</b> Klasse Language <br>
 *  <b>Description:</b> Zugriffsmethoden für die Sprache und das Flag: IsStartDialog <br>
 *  <b>Company:</b> TH Rosenheim <br>
 * 
 * @author Maria Stelz */
public class Language {
	
	/** Ist Start Dialog */
	private static boolean isStartDialog;
	
	/** Sprache */
	private static String language;
	
	
	/** Konstruktor */
	public Language(boolean start, String lang) 
	{
		isStartDialog = start;
		language = lang;
	}
	
	//Zugriffsmethoden
	/** Rückgabe ob StartDialog */
	public static boolean getStartDialog() { return isStartDialog; }
	
	/** Setzt StartDialog */
	public static void setStartDialog(boolean start) { isStartDialog = start; }
	
	/** Rückgabe der Sprache */
	public static String getLanguage(){ return language; }

	/** Setzt die Sprache */
	public static void setLanguage(String lang) { language = lang; }
}
