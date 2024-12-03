/*
 * @(#)GUIConstraints.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.log4j.Logger;

/** <b>Title:</b> Klasse GUIConstraints<br>
 * <b>Description:</b> Enthält die Objekte für ein einheitliches L&F<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public class GUIConstraints
{
	/** Logger dieser Klasse */
	private static Logger logger = Logger.getLogger(GUIConstraints.class);
	
	/** Minimale Schriftgröße */
    public static final int FONTSIZE_MIN = 8;
	
    /** Maximale Schriftgröße */
	public static final int FONTSIZE_MAX = 20;
	
	/** benutzte Schriften */
	public static final int INPUT_FONT_PLAIN = 0;
	public static final int INPUT_FONT_BOLD  = 1; 
	public static final int LABEL_FONT_PLAIN = 2;
	public static final int LABEL_FONT_BOLD  = INPUT_FONT_BOLD;
	public static final int OUTPUT_FONT      = 3;
	public static final int FONT_MAX_INDEX   = 3;
	
	/** Schrifttypen */
	public static final int SANSSERIF  = 0;
	public static final int MONOSPACED = 1;
	public static final int PLAIN      = 0;
	public static final int BOLD       = 1;
	
	// Schriftarten
	/** Array für alle moeglichen Schriften (erster Index: sansserif/monospaced;
	 *  zweiter Index: plain/bold; ditter Index: Fontsize */
	private static FontUIResource[][][] fontArray = new FontUIResource[2][2][FONTSIZE_MAX - FONTSIZE_MIN + 1];
	
	/** aktuelle Schriftgrößen */
	private static int[] fontSizes = new int[GUIConstraints.FONT_MAX_INDEX + 1];
	
	//Zugriffsmethoden
	/** Rückgabe des Fonts abhängig von der Schriftart */
	public static FontUIResource getFont(int fontIdx)
	{		
		//Default (fontsize = 12) wenn Zahl kleiner FONTSIZE_MIN oder größer FONTSIZE_MAX eingeben wird
		if(fontIdx == INPUT_FONT_PLAIN && (fontSizes[fontIdx] < FONTSIZE_MIN || fontSizes[fontIdx] > FONTSIZE_MAX)) 	
			return fontArray[SANSSERIF][PLAIN][4];
		
		else if(fontIdx == LABEL_FONT_BOLD && (fontSizes[fontIdx] < FONTSIZE_MIN || fontSizes[fontIdx] > FONTSIZE_MAX)) 
			return fontArray[SANSSERIF][BOLD][4];
		
		else if(fontIdx == LABEL_FONT_PLAIN && (fontSizes[fontIdx] < FONTSIZE_MIN || fontSizes[fontIdx] > FONTSIZE_MAX)) 
			return fontArray[SANSSERIF][PLAIN][4];
		
		else if(fontIdx == OUTPUT_FONT && (fontSizes[fontIdx] < FONTSIZE_MIN || fontSizes[fontIdx] > FONTSIZE_MAX)) 	
			return fontArray[MONOSPACED][PLAIN][4];
		
		else 
		{
			int fontSizeIdx = fontSizes[fontIdx] - FONTSIZE_MIN;
			if (fontIdx == INPUT_FONT_PLAIN) return fontArray[SANSSERIF][PLAIN][fontSizeIdx];
			if (fontIdx == LABEL_FONT_BOLD)  return fontArray[SANSSERIF][BOLD][fontSizeIdx];
			if (fontIdx == LABEL_FONT_PLAIN) return fontArray[SANSSERIF][PLAIN][fontSizeIdx];
			if (fontIdx == OUTPUT_FONT)      return fontArray[MONOSPACED][PLAIN][fontSizeIdx];
			
		}
		/* Default font */
		return fontArray[SANSSERIF][PLAIN][4];

		
	}

	/** Rückgabe der Schriftgrößen abhängig von der Schriftart */
	public static int getFontSize(int fontIdx)
	{ 		
		if(fontSizes[fontIdx] < FONTSIZE_MIN || fontSizes[fontIdx] > FONTSIZE_MAX) 
			return 12;
		else if (fontIdx < 0 || fontIdx > GUIConstraints.FONT_MAX_INDEX) 
			return 0;
		else 
			return fontSizes[fontIdx];
	}
	
	/** Standard-Konstruktor (baut fontArray und fontSizes fuer alle Schriftgroessen auf*/
	public GUIConstraints(int[] fSizes)
	{
		// fontArray
		for(int serifType = 0; serifType <= 1; serifType++)
		{
			for(int boldType = 0; boldType <= 1; boldType++)
			{
				for(int fontSize = FONTSIZE_MIN; fontSize <= FONTSIZE_MAX; fontSize++)
				{
					fontArray[serifType][boldType][fontSize-FONTSIZE_MIN]
							= new FontUIResource((serifType==SANSSERIF)?"SansSerif":"Monospaced", 
									(boldType==PLAIN)?Font.PLAIN:Font.BOLD, fontSize);	
				}
			}
		}
		
		// fontSizes
		for(int fontType = 0; fontType <= FONT_MAX_INDEX; fontType++)
			fontSizes[fontType] = fSizes[fontType];
	}
	
	/** Main-Methode nur zum Testen */
	public static void main(String[] argv)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}

		JTable jt = new JTable();
		FontUIResource f = new FontUIResource(jt.getFont());
		System.out.println("JT: " + f);
		JMenu jm = new JMenu();
		f = (FontUIResource) jm.getFont();
		System.out.println("JM: " + f);
		JMenuItem jmi = new JMenuItem();
		f = (FontUIResource) jmi.getFont();
		System.out.println("MI: " + f);
		JLabel jl = new JLabel();
		f = (FontUIResource) jl.getFont();
		System.out.println("JL: " + f);
		JTextField jtf = new JTextField();
		f = (FontUIResource) jtf.getFont();
		System.out.println("TF: " + f);
	}
}


