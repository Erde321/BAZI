package de.uni.augsburg.bazi.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import de.uni.augsburg.bazi.Resource;

/** <b>Title:</b> Klasse Dialog_Settings <br>
 * <b>Description:</b> Dialog_Settings Fenster zur Einstellung der Schriftgröße und IsStartDialog <br>
 * <b>Company:</b> TH Rosenheim <br>
 * 
 * @author Maria Stelz */
public class Dialog_Settings extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	/** Layout-Manager */
	private GridBagLayout gbl;

	/** Layout-Manager */
	private GridBagConstraints gbc;
	
	/** ComboBox der einzelnen Schriftarten */
	private JComboBox<String> LabelPlain, Output, LabelBold, InputPlain;
	
	/** Anzahl der Objekte vom Inhalt der ComboBox */
	private int anz = GUIConstraints.FONTSIZE_MAX - GUIConstraints.FONTSIZE_MIN + 1;
	
	/** Inhalt der einzeln ComboBoxen */
	private String[] fontsize =	new String[anz];
	
	/** Bezeichungen der einzelen Schriftarten */
	private JLabel Label;	
	
	/** RadioButton für ist StartDialog on */
	private JRadioButton RadioButton_on;
	
	/** RadioButton für ist StartDialog off */
	private JRadioButton RadioButton_off;
	
	/** ButtonGroup für ist StartDialog */
	private ButtonGroup bg;
	
	/** Button für OK und Cancel */
	private JButton Button;

	
	/** Konstruktor erzeugt das Fenster */
	public Dialog_Settings(RoundFrame rf)
	{
		setTitle(Resource.getString("bazi.settings.titel"));
		setLocationRelativeTo(rf);
		setIconImage(new ImageIcon(getClass().getResource("images/bazi_icon.gif")).getImage());
		setModal(true);
		setResizable(true);		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//Fenster an Schriftgröße anpassen
		setSize(setWidthandHeight()[0], setWidthandHeight()[1]);
		initialize();
		setVisible(true);
	}
	
	
	/** Intializiert die einzeln Kompoenten des Fenster */
	private void initialize() 
	{		
		//Schriftgrößen		
		initTitel(Resource.getString("bazi.settings.fontsize"), 1);
		
		fontsize[0] = String.valueOf(GUIConstraints.FONTSIZE_MIN);
		for(int i = 1; i < anz; i++)
			fontsize[i] = String.valueOf(Integer.valueOf(fontsize[i - 1]) + 1);
		fontsize[4] = fontsize[4] + " " + Resource.getString("bazi.settings.font.default");
		
		initLabel(Resource.getString("bazi.settings.font.inputplain"), 2);
		InputPlain = new JComboBox<String>(fontsize);
		InputPlain.setSelectedIndex(GUIConstraints.getFontSize(0) - GUIConstraints.FONTSIZE_MIN);
		gbcCombobox(InputPlain, 2);
		
		initLabel(Resource.getString("bazi.settings.font.output"), 3);
		Output = new JComboBox<String>(fontsize);
		Output.setSelectedIndex(GUIConstraints.getFontSize(3) - GUIConstraints.FONTSIZE_MIN);
		gbcCombobox(Output, 3);

		initLabel(Resource.getString("bazi.settings.font.labelbold"), 4);
		LabelBold = new JComboBox<String>(fontsize);
		LabelBold.setSelectedIndex(GUIConstraints.getFontSize(1) - GUIConstraints.FONTSIZE_MIN);
		gbcCombobox(LabelBold, 4);
		
		initLabel(Resource.getString("bazi.settings.font.labelplain"), 5);	
		LabelPlain = new JComboBox<String>(fontsize);
		LabelPlain.setSelectedIndex(GUIConstraints.getFontSize(2) - GUIConstraints.FONTSIZE_MIN);
		gbcCombobox(LabelPlain, 5);
		
		Label = new JLabel(Resource.getString("bazi.settings.font.text"));
		Label.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.EAST;
		gbc.gridwidth = 7;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 6;
		getContentPane().add(Label, gbc);
		
		//Anzeige Sprachauswahl
		initTitel(Resource.getString("bazi.settings.startdialog"), 8);	
		
		RadioButton_on = new JRadioButton(Resource.getString("bazi.settings.yes"));
		RadioButton_on.setSelected(Language.getStartDialog());
		initRadioButton(RadioButton_on, 6);
		
		RadioButton_off = new JRadioButton(Resource.getString("bazi.settings.no"));
		RadioButton_off.setSelected(!Language.getStartDialog());
		initRadioButton(RadioButton_off, 7);
		
		bg = new ButtonGroup();
		bg.add(RadioButton_on);
		bg.add(RadioButton_off);	
		
		//Button für Speichern & Abbrechen
		initButton(Resource.getString("bazi.gui.save"));
		initButton(Resource.getString("bazi.gui.cancel"));	
	}
	
	
	/** Layout der RadtioButtons */
	private void initRadioButton(JRadioButton rb, int gridx) 
	{
		rb.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.WEST;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = gridx;
		gbc.gridy = 8;
		getContentPane().add(rb, gbc);
	}
	
	
	/** Initialisierung & Layout der Buttons */
	private void initButton(String titel) 
	{
		Button = new JButton(titel);
		Button.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		Button.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 5, 5);
		if(titel == Resource.getString("bazi.gui.save")) 
		{
			gbc.gridwidth = 2;
			gbc.gridx = 4;
			
		}
		else if(titel == Resource.getString("bazi.gui.cancel")) 
		{
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridwidth = 2;
			gbc.gridx = 6;
		}
		gbc.gridy = 10;
		getContentPane().add(Button, gbc);
		
	}
	
	
	/** Initialisierung & Layout der Label Überschriften */
	private void initTitel(String titel, int gridy) 
	{
		Label = new JLabel(titel);
		Label.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		gbc = new GridBagConstraints();
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = gridy;
		getContentPane().add(Label, gbc);
	}
	
	
	/** Initialisierung & Layout der Label */
	private void initLabel(String titel, int gridy) 
	{
		Label = new JLabel(titel);
		Label.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 3;
		gbc.gridy = gridy;
		getContentPane().add(Label, gbc);		
	}
	
	
	/** Layout der ComboBox*/
	private void gbcCombobox(JComboBox<String> comboBox, int gridy) 
	{
		comboBox.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 6;
		gbc.gridy = gridy;
		getContentPane().add(comboBox, gbc);	
	}	

	
	/** Fenstergröße abhängig von der LabelPlain Schriftgröße */
	private int[] setWidthandHeight() 
	{
		int width = 450,  height = 350;
		int size = (GUIConstraints.getFontSize(GUIConstraints.LABEL_FONT_PLAIN) - GUIConstraints.FONTSIZE_MIN);

		if(GUIConstraints.getFontSize(GUIConstraints.LABEL_FONT_PLAIN) >= GUIConstraints.FONTSIZE_MIN)
		{
			width = width + size * 7;
			height = height + size * 10;
		}	
		
		gbl = new GridBagLayout();
		gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl.columnWidths = new int[]{25, 37, 37, 47, 27, 10, 27, 47, 25};
		gbl.rowHeights = new int[]{10+size, 25+size, 25+size, 25+size, 25+size, 25+size, 55+size, 10+size ,25+size, 25+size, 25+size, 10+size};
		getContentPane().setLayout(gbl);
		return new int[] {width, height};
	}
	
	
	/** Aufruf, wenn ein Button angeklickt wurde */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(Resource.getString("bazi.gui.save"))) 
		{					
			Jackson.writeJSON(InputPlain.getSelectedIndex() + GUIConstraints.FONTSIZE_MIN, 
							  LabelBold.getSelectedIndex() + GUIConstraints.FONTSIZE_MIN, 
							  LabelPlain.getSelectedIndex() + GUIConstraints.FONTSIZE_MIN, 
							  Output.getSelectedIndex() + GUIConstraints.FONTSIZE_MIN,
							  RadioButton_on.isSelected(), 
							  Language.getLanguage());
			
			Jackson jackson = new Jackson();		
			Jackson.readJSON(jackson);
			
			dispose();
			
		}
		else if (e.getActionCommand().equals(Resource.getString("bazi.gui.cancel")))
			//schliest das Fenster
			dispose();	
	}
	
}

