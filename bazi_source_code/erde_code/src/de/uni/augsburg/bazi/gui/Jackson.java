package de.uni.augsburg.bazi.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/** <b>Title:</b> Klasse Jackson <br>
 *  <b>Description:</b> Jackson-Korrespondenzklasse zur Datei preferences.json<br> 
 *  <b>Company:</b> TH Rosenheim <br>
 * 
 * @author Maria Stelz */
public class Jackson 
{
	/** ACHTUNG: Die privaten Attribute und die Setter- & Getter-Funktionen muessen genau so heissen,
	 * 	sonst liefert die Jackson-Methode objectMapper.readValue bzw. objectMapper.writeValue einen Fehler, die bisher
	 * 	nicht abgefangen werden  */
	
	/** Ist Start Dialog */
	private boolean isStartDialog;
	
	/** Sprach Abkürzung*/
	private String language;

	/** Schriftgröße für den InputFont */
	private int inputPlainFontSize;
	
	/** Schriftgröße für den LabelBoldFont */
	private int labelBoldFontSize;
	
	/** Schriftgröße für den LabelPlainFont */
	private int labelPlainFontSize;

	/** Schriftgröße für den OutputFont */
	private int outputFontSize;
	
	/** ObjectMapper dieser Klasse */
	private static ObjectMapper objectmapper = new ObjectMapper();
	
	
	//Zugriffsmethoden
	/** Setzen ist Start Dialog */
	public void setIsStartDialog(boolean isStartDialog) { this.isStartDialog = isStartDialog; }
	
	/** Rückgabe ist Start Dialog */
	public boolean getIsStartDialog() { return this.isStartDialog; }
	
	/** Setzen der Sprachabkürzung */
	public void setLanguage(String language) { this.language = language; }
	
	/** Rückgabe des Sprachabkürzung */
	public String getLanguage() { return this.language; }
	
	/** Setzen der Schriftgröße des InputFont */
	public void setInputPlainFontSize(int inputPlainFontSize) { this.inputPlainFontSize = inputPlainFontSize; }
	
	/** Rückgabe der Schriftgröße des InputFont */
	public int getInputPlainFontSize() { return this.inputPlainFontSize;	}
	
	/** Setzen der Schriftgröße des OutputFont */
	public void setOutputFontSize(int outputFontSize) { this.outputFontSize = outputFontSize; }	
	
	/** Rückgabe der Schriftgröße des InputFont */
	public int getOutputFontSize() { return this.outputFontSize; }
	
	/** Setzen der Schriftgröße des LabelBoldFont */
	public void setLabelBoldFontSize(int labelBoldFontSize) { this.labelBoldFontSize = labelBoldFontSize; }
	
	/** Rückgabe der Schriftgröße des InputFont */
	public int getLabelBoldFontSize() { return this.labelBoldFontSize; }
	
	/** Setzen der Schriftgröße des LabelPlainFont */
	public void setLabelPlainFontSize(int labelPlainFontSize) { this.labelPlainFontSize = labelPlainFontSize; }
	
	/** Rückgabe der Schriftgröße des InputFont */
	public int getLabelPlainFontSize() { return this.labelPlainFontSize; }
	
	
	/** Rückgabe aller Schriftgrößen als Array */
	public int[] ArrayFontSize()
	{ 		
		int[] fontSizes = new int[GUIConstraints.FONT_MAX_INDEX + 1];
		fontSizes[GUIConstraints.INPUT_FONT_PLAIN] = getInputPlainFontSize();
		fontSizes[GUIConstraints.LABEL_FONT_BOLD] = getLabelBoldFontSize();
		fontSizes[GUIConstraints.LABEL_FONT_PLAIN] = getLabelPlainFontSize();
		fontSizes[GUIConstraints.OUTPUT_FONT] = getOutputFontSize();
		
		return fontSizes;
	}
	
	
	/** Schreibt die Einstellungen in die JSON-Datei */
	public static void writeJSON(int fontsizeIP, int fontsizeLB, int fontsizeLP, int fontsizeO, boolean isStartDialog, String language) 
	{
		Jackson jack = new Jackson();
		jack.setInputPlainFontSize(fontsizeIP); 
		jack.setLabelBoldFontSize(fontsizeLB); 
		jack.setLabelPlainFontSize(fontsizeLP); 
		jack.setOutputFontSize(fontsizeO); 
		jack.setIsStartDialog(isStartDialog);
		jack.setLanguage(language);
		
		try 
		{		
			objectmapper.writeValue(new FileOutputStream(".\\preferences.json"), jack); 
			
		} catch (IOException ea)
		{  
			ea.printStackTrace(); 
		}
		
	}
  
	
    /** Liest die Einstellungen aus der JSON-Datei */
	public static void readJSON(Jackson jackson) 
	{
		try 
		{
			File file = new File(".\\preferences.json");
			//File file = new File("C:\\Users\\biw\\Documents\\FH_Rosenheim\\Forschung_Entwicklung\\Labor\\Zuteilungsmethoden\\bazi,devstelz\\target\\preferences.json");
			if(file.exists()) 
			{	
				jackson = objectmapper.readValue(file, Jackson.class);
				new GUIConstraints(jackson.ArrayFontSize());
				new Language(jackson.getIsStartDialog(), jackson.getLanguage());
			}
			else 
			{			 
				int[] defaultFontSizes = new int[GUIConstraints.FONT_MAX_INDEX + 1];
				for (int i = 0; i <= GUIConstraints.FONT_MAX_INDEX; i++) defaultFontSizes[i] = 12;
			    new GUIConstraints(defaultFontSizes);
			    new Language(true, "en");
			}			
		}
		catch(Exception e) {}
	}
		
		
}

