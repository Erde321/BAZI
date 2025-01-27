/*
 * @(#)RoundFrame.java 2.3 09/09/28
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.AbstractInputData;
import de.uni.augsburg.bazi.driver.Calculation;
import de.uni.augsburg.bazi.driver.DistrictInputData;
import de.uni.augsburg.bazi.driver.FileIO;
import de.uni.augsburg.bazi.driver.InputData;
import de.uni.augsburg.bazi.driver.MethodData;
import de.uni.augsburg.bazi.driver.OutputFormat;
import de.uni.augsburg.bazi.gui.district.DistrictDialog;
import de.uni.augsburg.bazi.gui.district.DistrictQueue;
import de.uni.augsburg.bazi.lib.BrowserLaunch;
import de.uni.augsburg.bazi.lib.Debug;
import de.uni.augsburg.bazi.lib.ExtendedPowerMean;
import de.uni.augsburg.bazi.lib.ExtendedStationary;
import de.uni.augsburg.bazi.lib.ParameterOutOfRangeException;

/** <b>Title:</b> Klasse RoundFrame<br>
 * <b>Description:</b> Hauptfenster des Programms<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Jan Petzold, Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher
 * @version 2.3 */

public class RoundFrame extends JFrame implements Runnable, ItemListener, MultiplicityListener
{

	/** Logger dieser Klasse */
	private static Logger logger = Logger.getLogger(RoundFrame.class);
	
	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** String zur Abtrennung von Bereichen */
	public static final String BASE_SEPERATOR = "+";

	/** String zur Abtrennung von Bereichen */
	public static final String RANGE_SEPERATOR = "..";

	/** Fenstertitel. */
	private String title = "BAZI"; // - Berechnung von Anzahlen mit Zuteilungsmethoden im Internet";

	/** Referenz auf sich selbst. */
	private static RoundFrame roundFrame;

	/** Thread zum Berechnen der Zuteilungen. */
	private Thread calculateThread;

	public BaziProperties properties = new BaziProperties();

	// Benutzeroberflaeche

	/** Haupt-Panel. */
	private final JPanel contentPanel;

	/** Bezeichnung des Titel-Feldes. */
	private final JLabel lTitle;

	/** Titel-Feld. */
	private final JTextField tTitle;

	/** Tabelle für die Stimmeneingabe mit Name, Stimmen und Bedingung. */
	private WeightsTable wTable;

	/** TablePane für die Stimmeneingabe */
	private final TablePane tpTable;

	/** Beispiel zur Mandatseingabe. */
	private final JLabel lAccHelp;

	/** Feld zur Mandatseingabe. */
	private final JTextField tAccuracy;

	/** Panel für die Distriktmethoden */
	private final JPanel prb;

	/** Panel für die Distriktmethoden */
	private final JPanel pAcc;

	/** Zusammenschluss und Verwaltung der Checkboxen zur Methodenauswahl. */
	private final NumberCheckboxGroup methodcbg;

	/** Checkboxen zur Methodenauswahl. */
	private final NumberCheckbox[] methodc;

	/** Zusätzliche Bemerkung zu den Methoden. */
	private final JLabel[] methodl, methodl2;

	/** MouseListener für die MethodenLabels */
	private final QuotaMouseAdapter quotaMouseAdapter = new QuotaMouseAdapter();
	private final SubMouseAdapter subMouseAdapter = new SubMouseAdapter();

	/** IDs der Methoden */
	String[] keys = { "haqgrr", "divabr", "divstd", "divgeo", "divhar", "divauf", "divpot", "divsta", "drqgrr" };

	/** Berechnung starten und abbrechen. */
	private final JButton bCalculate;

	/** Darstellung des BAZI-Logo. */
	private final JLabel logo;

	/** Bezeichung des Ausgabe-Bereiches. */
	private final JLabel lOutput;

	/** Ausgabe-Bereich. */
	private final JTextArea tOutput;

	/** Scrollpane des Ausgabe-Bereichs */
	private final JScrollPane scrollOutput;

	/** Parameter-Felder. */
	private final JTextField tQ;

	/** Parameter-Felder. */
	private final JTextField tP;

	/** Radiobuttons für die Zuteilungsarten bei mehreren Distrikten */
	private final JRadioButton rbDistricts;

	/** Radiobuttons für die Zuteilungsarten bei mehreren Distrikten */
	private final JRadioButton rbNZZ;

	/** Radiobuttons für die Zuteilungsarten bei mehreren Distrikten */
	private final JRadioButton rbBiprop;

	/** Auswahl der Ausrichtung der Ausgabe. */
	private final JComboBox cAlignment;

	/** Auswahl des Formats des Divisors. */
	private final JComboBox cDivisor;

	/** Auswahl der Bindungs-Ausgabe */
	private final JComboBox cTies;

	/** Breite der Stimmentabelle */
	private int dim_Width_Table = 230;
	
	/** Breite des Ausgabe-Textfeld */
	private int dim_Width_Output = 400;
	
	// Ereignisbehandlung

	/** Abfangen der Ereignisse auf die Checkboxen. */
	private final IListener checkboxListener;

	/** Abfangen der Menu-Ereignisse und der Ereignisse auf den Calculate-Button. */
	private final AListener actionListener;

	/** Selectionlistener für die WeightsTables */
	private final SListener sltp = new SListener();

	/** Layout-Manager. */
	private final GridBagLayout gbl = new GridBagLayout();

	/** Menu. */
	private final JMenu menuFile;

	/** Menu. */
	private JMenu menuEdit;

	/** Menu. */
	private final JMenu menuOptions;

	/** Menu. */
	private final JMenu menuDatabase;

	/** Menu. */
	private final JMenu menuHelp;

	/** Menu-Eintrag. */
	private final JMenuItem mOpen;

	/** Menu-Eintrag. */
	private final JMenuItem mReOpen;

	/** Menu-Eintrag. */
	private final JMenuItem mSave;

	/** Menu-Eintrag. */
	private JMenuItem mExport;

	/** Menu-Eintrag. */
	private final JMenuItem mRestart;

	/** Menu-Eintrag. */
	private final JMenuItem mExit;

	/** Menu-Eintrag. */
	private final JMenuItem mLinesDelete;

	/** Menu-Eintrag. */
	private final JMenuItem mInputDelete;

	/** Menu-Eintrag. */
	private final JMenuItem mOutputDelete;

	/** Menu-Eintrag. */
	private final JMenuItem mSep;

	/** Menu-Eintrag. */
	private final JMenuItem mDirecthelp;

	/** Menu-Eintrag. */
	private final JMenuItem mInfo;
	
	/** Menu-Eintrag */
	private final JMenuItem mSettings;
	
	/** Menu-Eintrag. */
	private final JMenuItem mLicense;

	/** Menu-Eintrag. */
 	/*private final JMenuItem mChangelog; */

	/** Menu-Eintrag. */
	private final JMenuItem mUpdate;

	/** Menu-Eintrag. */
	private final JCheckBoxMenuItem mSortBiprop;

	/** Menu-Eintrag. */
	private final JMenuItem mBipropAlts;

	/** Menu-Eintrag. */
	private final JMenuItem mDroopAlts;

	/** Menu-Eintrag. */
	private final JMenuItem mSubapportionmentAlts;

	/** Menu-Eintrag. */
	private final JMenuItem mBBM;

	/** Menu-Eintrag. */
	private final JMenuItem mMinPlus;

	/** Menu-Eintrag. */
	private final JMenuItem mHareAlts;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mASmdpt;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mASextr;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mASrand;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mTTflpt;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mTTinte;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mIPFP;
	JRadioButton mNewton;
	JRadioButton mPrimal;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_ASmp_TTfp;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_IPFP_TTfp;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_IPFP_TTin;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_ASrd_TTfp;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_ASex_TTfp;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_ASmp_TTin;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_ASex_TTin;

	/** Radio-Buttons Biprop Algorithmen */
	JRadioButton mH_ASrd_TTin;


	/** ButtonGroup der Radio-Buttons für Biprop Algorithmen */
	ButtonGroup bipropBG;

	/** Bedingungen für die Berechnung */
	private final ConditionItem cond_none;

	/** Bedingungen für die Berechnung */
	private final ConditionItem cond_min;

	/** Bedingungen für die Berechnung */
	private final ConditionItem cond_dir;

	/** Bedingungen für die Berechnung */
	private final ConditionItem cond_naiv;

	/** Bedingungen für die Berechnung */
	private final ConditionItem cond_max;

	/** Bedingungen für die Berechnung */
	private final ConditionItem cond_min_to_max;

	/** Bedingungen für die Berechnung */
	private ConditionItem cond_min_plus, cond_min_vpv;

	/** Bedingungen für die Berechnung */
	private final ConditionItem bp_cond_min;

	/** Bedingungen für die Berechnung */
	private final ConditionItem bp_cond_super_min;

	// Bilder

	/** Icon für die Direkthilfe im Menu. */
	private final ImageIcon helpicon;

	/** Cursor für die Direkthilfe. */
	private final ImageIcon helpcursor;
	// private ImageIcon handcursor;

	/** BAZI-Logo. */
	private final ImageIcon bazilogo;

	// Datenbank

	/** Instanz von Database zur Verwaltung der Datenbank-Dateien. */
	private final Database database;

	/** Writer zum Speichern von Eingabe-Daten und berechneten Ergebnissen */
	private final BaziFileIO bfio = new BaziFileIO(this, new FileIO());

	/** DebugDialog */
	private final DebugDialog dd;

	/** Flusskontrolle um Stackoverflow durch Rekursion beim automatischen Setzen der Radiobuttons zu vermeiden */
	private boolean bRB = false;

	/** Ränder für Fensterelemente */
	private TitledBorder tbAcc;

	/** Ränder für Fensterelemente */
	private TitledBorder tbMeth;

	/** Ränder für Fensterelemente */
	private TitledBorder tbDistricts;

	/** Name der aktuelle geladenen Datei */
	private java.io.File currentFile = null;

	/** Die verwendetete Droop Methode */
	private int droopMethod = 0;
	private int droopResidualMethod = 0;

	/** Die verwendetete Hare Methode */
	private int hareMethod = 0;
	private int hareResidualMethod = 0;

	/** Methode für die Unterzuteilung */
	private String subapportionment = SubapportionmentSelector.DEFAULT;

	/** Base+Min..Max aktivieren? */
	private boolean BMM = false;

	/** PowerWeighting aktivieren? */
	private boolean powerWeighted = false;

	/** Daten für base+min..max */
	private int base = 0, min = 0, max = Integer.MAX_VALUE;

	public static double minPlusValue = 5;

	private final ArrayList<Integer> scroll_positions = new ArrayList<Integer>();

	/** Erzeugt eine Instanz von RoundFrame. Baut die Benutzeroberfläche auf.
	 * 
	 * @param version Versionsangabe. */
	public RoundFrame(String version)
	{
		this(version, false);
	}

	/** Erzeugt eine Instanz von RoundFrame. Baut die Benutzeroberfläche auf.
	 * 
	 * @param version Versionsangabe.
	 * @param maximized Soll das Fenster maximiert werden? */
	public RoundFrame(String version, boolean maximized)
	{
		super();

		title += " - " + version;

		JWindow loadWindow = new JWindow();
		ImageIcon startImage = new ImageIcon(getClass().getResource(
				"images/bazi_start.png"));
		loadWindow.getContentPane().add(new JLabel(startImage));
		loadWindow.setSize(startImage.getIconWidth(), startImage.getIconHeight());
		loadWindow.setLocationRelativeTo(null);
		loadWindow.setVisible(true);

		setTitle(title);
		roundFrame = this;

		// Setzen des Look & Feel
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

		// Look&Feel für Dialoge
		JDialog.setDefaultLookAndFeelDecorated(false);

		// gif's laden
		setIconImage(new ImageIcon(getClass().getResource("images/bazi_icon.gif")).
				getImage());
		bazilogo = new ImageIcon(getClass().getResource("images/bazi_logo.gif"));
		helpcursor = new ImageIcon(getClass().getResource("images/helpcursor.gif"));
		// handcursor = new ImageIcon(getClass().getResource("images/handcursor.gif"));
		helpicon = new ImageIcon(getClass().getResource("images/help.gif"));

		setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));

		// ToolTipManager setzen
		ToolTipManager manager = ToolTipManager.sharedInstance();
		manager.setDismissDelay(20000);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				close();
			}
		});

		// Listener erzeugen
		checkboxListener = new IListener();
		actionListener = new AListener();

		// Direkthilfe
		roundFrame.getGlassPane().addMouseListener(new HelpListener());

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Menu Datei
		menuFile = new JMenu(Resource.getString("bazi.gui.menu.file"));
		menuFile.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mOpen = new JMenuItem(Resource.getString("bazi.gui.menu.open"));
		mOpen.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mOpen.addActionListener(actionListener);
		mOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(mOpen);
		mReOpen = new JMenuItem(Resource.getString("bazi.gui.menu.reopen"));
		mReOpen.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mReOpen.addActionListener(actionListener);
		mReOpen.setEnabled(false);
		mReOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(mReOpen);
		mSave = new JMenuItem(Resource.getString("bazi.gui.menu.save"));
		mSave.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mSave.addActionListener(actionListener);
		mSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(mSave);
		// mExport = new JMenuItem(Resource.getString("bazi.gui.menu.export"));
		// mExport.setFont(GUIConstraints.boldFont);
		// mExport.addActionListener(actionListener);
		// menuFile.add(mExport);
		mRestart = new JMenuItem(Resource.getString("bazi.gui.menu.restart"));
		mRestart.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mRestart.addActionListener(actionListener);
		mRestart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(mRestart);

		menuFile.addSeparator();

		mExit = new JMenuItem(Resource.getString("bazi.gui.menu.exit"));
		mExit.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mExit.addActionListener(actionListener);
		mExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		menuFile.add(mExit);
		menuBar.add(menuFile);

		// Menu Bearbeiten
		menuEdit = new JMenu(Resource.getString("bazi.gui.menu.edit"));
		menuEdit.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mLinesDelete = new JMenuItem(Resource.getString("bazi.gui.menu.lines"));
		mLinesDelete.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mLinesDelete.addActionListener(actionListener);
		mLinesDelete.setEnabled(false);
		mLinesDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
		menuEdit.add(mLinesDelete);
		mInputDelete = new JMenuItem(Resource.getString("bazi.gui.menu.input"));
		mInputDelete.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mInputDelete.addActionListener(actionListener);
		mInputDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		menuEdit.add(mInputDelete);
		mOutputDelete = new JMenuItem(Resource.getString("bazi.gui.menu.output"));
		mOutputDelete.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mOutputDelete.addActionListener(actionListener);
		mOutputDelete.setEnabled(false);
		mOutputDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		menuEdit.add(mOutputDelete);
		menuBar.add(menuEdit);


		// Menu Optionen
		menuOptions = menuEdit = new JMenu(Resource.getString("bazi.gui.menu.options"));
		menuOptions.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));

		mHareAlts = new JMenuItem(Resource.getString("bazi.gui.biprop.hare.alt") + "...");
		mHareAlts.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mHareAlts.addActionListener(actionListener);
		menuOptions.add(mHareAlts);
		mDroopAlts = new JMenuItem(Resource.getString("bazi.gui.biprop.droop.alt") + "...");
		mDroopAlts.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mDroopAlts.addActionListener(actionListener);
		menuOptions.add(mDroopAlts);
		mSubapportionmentAlts = new JMenuItem(Resource.getString("bazi.gui.biprop.sub.alt") + "...");
		mSubapportionmentAlts.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mSubapportionmentAlts.addActionListener(actionListener);
		menuOptions.add(mSubapportionmentAlts);
		mBBM = new JMenuItem(Resource.getString("bazi.gui.bmm") + "...");
		mBBM.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mBBM.addActionListener(actionListener);
		menuOptions.add(mBBM);
		mMinPlus = new JMenuItem(Resource.getString("bazi.gui.menu.minplus") + "...");
		mMinPlus.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mMinPlus.addActionListener(actionListener);
		mMinPlus.setEnabled(false);
		menuOptions.add(mMinPlus);
		mSortBiprop = new JCheckBoxMenuItem(Resource.getString("bazi.gui.menu.sortbiprop"));
		mSortBiprop.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mSortBiprop.setToolTipText(Resource.getString("bazi.gui.menu.sortbiprop_tooltip"));
		mSortBiprop.setSelected(false);
		menuOptions.add(mSortBiprop);
		menuOptions.addSeparator();
		mBipropAlts = new JMenuItem(Resource.getString("bazi.gui.biprop.alt") + "...");
		mBipropAlts.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mBipropAlts.addActionListener(actionListener);
		menuOptions.add(mBipropAlts);
		mSep = new JMenuItem(Resource.getString("bazi.gui.menu.sep"));
		mSep.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mSep.addActionListener(actionListener);
		menuOptions.add(mSep);
		menuBar.add(menuOptions);

		// Menu Datenbank
		menuDatabase = new JMenu(Resource.getString("bazi.gui.menu.database"));
		menuDatabase.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		menuBar.add(menuDatabase);
		database = new Database(this, menuDatabase);

		// Menu Hilfe
		menuHelp = new JMenu(Resource.getString("bazi.gui.menu.help"));
		menuHelp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mDirecthelp = new JMenuItem(Resource.getString("bazi.gui.menu.directhelp"),
				helpicon);
		mDirecthelp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mDirecthelp.setMargin(new Insets(0, 0, 0, 0));
		mDirecthelp.addActionListener(actionListener);
		menuHelp.add(mDirecthelp);

		
		/*mChangelog = new JMenuItem(Resource.getString("bazi.gui.menu.changelog"));
		mChangelog.setFont(GUIConstraints.boldFont);
		mChangelog.addActionListener(actionListener);
		menuHelp.add(mChangelog);*/

		mUpdate = new JMenuItem(Resource.getString("bazi.gui.menu.update"));
		mUpdate.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mUpdate.addActionListener(actionListener);
		menuHelp.add(mUpdate);
		
		mLicense = new JMenuItem(Resource.getString("bazi.gui.menu.license"));
		mLicense.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mLicense.addActionListener(actionListener);
		menuHelp.add(mLicense);
		
		mInfo = new JMenuItem(Resource.getString("bazi.gui.menu.about"));
		mInfo.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mInfo.addActionListener(actionListener);
		menuHelp.add(mInfo);
		
		mSettings = new JMenuItem(Resource.getString("bazi.gui.menu.settings"));
		mSettings.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		mSettings.addActionListener(actionListener);
		menuHelp.add(mSettings);
		
		menuBar.add(menuHelp);

		// /////////////////////////////////////////////////////////////////////////
		//
		// Ab hier Inhalt des Frame
		//
		// /////////////////////////////////////////////////////////////////////////

		contentPanel = new JPanel(gbl);

		// Toppanel, vielleicht wird die Titeltextbox dann richtig vergrößert...
		// --> success!
		JPanel topPanel = new JPanel(new BorderLayout(5, 0));

		// Titelangabe
		lTitle = new JLabel(Resource.getString("bazi.gui.title") + ":");
		lTitle.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		topPanel.add(lTitle, BorderLayout.WEST);

		// KEIN "PROBLEM!" MEHR!
		tTitle = new JTextField(1);
		topPanel.add(tTitle, BorderLayout.CENTER);

		// Format der Ausgabe
		JPanel outOptsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 1));

		lOutput = new JLabel(Resource.getString("bazi.gui.output") + ":");
		lOutput.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		outOptsPanel.add(lOutput);
		cAlignment = new JComboBox();
		cAlignment.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		cAlignment.addItem(Resource.getString("bazi.gui.align.horizontal"));
		cAlignment.addItem(Resource.getString("bazi.gui.align.vertical"));
		cAlignment.setSelectedIndex(1);
		outOptsPanel.add(cAlignment);
		cDivisor = new JComboBox();
		cDivisor.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		cDivisor.addItem(Resource.getString("bazi.gui.div.divisor"));
		cDivisor.addItem(Resource.getString("bazi.gui.div.divinterval"));
		cDivisor.addItem(Resource.getString("bazi.gui.div.multiplier"));
		cDivisor.addItem(Resource.getString("bazi.gui.div.multinterval"));
		cDivisor.addItem(Resource.getString("bazi.gui.div.quotient"));
		outOptsPanel.add(cDivisor);

		cTies = new JComboBox();
		cTies.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		cTies.addItem(Resource.getString("bazi.gui.ties.coded"));
		cTies.addItem(Resource.getString("bazi.gui.ties.listandcoded"));
		cTies.addItem(Resource.getString("bazi.gui.ties.list"));
		outOptsPanel.add(cTies);

		topPanel.add(outOptsPanel, BorderLayout.EAST);
		topPanel.setMinimumSize(new Dimension(1, 25));
		addToFrame(topPanel, 0, 0, 9, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0);

		// Stimmentabelle
		cond_none = new ConditionItem(Resource.getString("bazi.gui.table.void"),
				Resource.getString("bazi.gui.table.void"),
				OutputFormat.CONDITION_NONE);
		cond_min = new ConditionItem(Resource.getString("bazi.gui.table.minimum"),
				Resource.getString("bazi.gui.table.minimum"),
				OutputFormat.CONDITION_MIN);
		cond_dir = new ConditionItem(Resource.getString("bazi.gui.table.direct"),
				Resource.getString("bazi.gui.output.over"),
				OutputFormat.CONDITION_DIRECT);
		cond_naiv = new ConditionItem(Resource.getString("bazi.gui.table.etc"),
				Resource.getString("bazi.gui.output.discrep"),
				OutputFormat.CONDITION_NAIV);
		cond_max = new ConditionItem(Resource.getString("bazi.gui.table.maximum"),
				Resource.getString("bazi.gui.table.maximum"),
				OutputFormat.CONDITION_MAX);
		cond_min_to_max = new ConditionItem(Resource.getString("bazi.gui.table.min_to_max"),
				Resource.getString("bazi.gui.table.min_to_max"),
				OutputFormat.CONDITION_MIN_TO_MAX);
		
		cond_min_plus = new ConditionItem(Resource.getString("bazi.gui.table.minimum") + "+" + RoundFrame.minPlusValue + "%", 
    			Resource.getString("bazi.gui.table.minimum") + "+" + RoundFrame.minPlusValue + "%", 
    			OutputFormat.CONDITION_MIN_PLUS);
		cond_min_vpv = new ConditionItem(Resource.getString("bazi.gui.table.min_vpv"),
				Resource.getString("bazi.gui.table.min_vpv"),
				OutputFormat.CONDITION_MIN_VPV);

		ConditionItem[] items =
		{ cond_none, cond_dir, cond_min, cond_max, cond_min_to_max, cond_naiv, cond_min_plus, cond_min_vpv };

		bp_cond_super_min = new ConditionItem(Resource.getString("bazi.gui.table.supermin_long"),
				Resource.getString("bazi.gui.table.supermin"),
				OutputFormat.CONDITION_SUPER_MIN);
		bp_cond_min = new ConditionItem(Resource.getString("bazi.gui.table.minimum_long"),
				Resource.getString("bazi.gui.table.bplocalmin"),
				OutputFormat.CONDITION_MIN);

		ConditionItem[] bipropItems =
		{ cond_none, bp_cond_super_min, bp_cond_min, cond_naiv };
		ConditionItem[] BMMItems =
		{ cond_none, cond_naiv };

		tAccuracy = new JTextField(9); // Vorgezogen für das Tablepane
		tpTable = new TablePane(Resource.getString("bazi.gui.table.name"),
				Resource.getString("bazi.gui.table.votes"),
				Resource.getString("bazi.gui.table.sum"),
				items, bipropItems, BMMItems, roundFrame, tAccuracy, sltp, new MListener());

		//Stimmtabellenbreite abhängig von der Schriftgröße
		if(GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN) >= GUIConstraints.FONTSIZE_MIN)
		{
			dim_Width_Table = dim_Width_Table + (GUIConstraints.getFontSize(GUIConstraints.INPUT_FONT_PLAIN) - GUIConstraints.FONTSIZE_MIN )* 15;
		}	
		tpTable.addMultiplicityListener(this);
		Dimension dTable = new Dimension(dim_Width_Table, dim_Height());
		tpTable.setPreferredSize(dTable);
		tpTable.setMinimumSize(new Dimension(150, 300));
		addToFrame(tpTable, 0, 1, 2, 8, 0, 1, GridBagConstraints.NORTHWEST,
				GridBagConstraints.VERTICAL);

		// Mandatseingabe
		pAcc = new JPanel();
		pAcc.setBorder(tbAcc = BorderFactory.createTitledBorder(Resource.getString(
				"bazi.gui.accuracy")));
		pAcc.setToolTipText(Resource.getString("bazi.gui.tooltip.accuracy"));
		tbAcc.setTitleFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));

		lAccHelp = new JLabel(Resource.getString("bazi.gui.accuracy.example"));
		lAccHelp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));

		pAcc.setLayout(new BorderLayout());
		pAcc.add(lAccHelp, BorderLayout.NORTH);
		pAcc.add(tAccuracy, BorderLayout.CENTER);
		addToFrame(pAcc, 2, 1, 2, 3, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL);

		// Methodenfeld
		JPanel pMethods = new JPanel();
		pMethods.setBorder(tbMeth = BorderFactory.createTitledBorder(Resource.getString(
				"bazi.gui.methods")));
		tbMeth.setTitleFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		SuperLayout slMethods = new SuperLayout(pMethods);

		methodcbg = new NumberCheckboxGroup();
		methodc = new NumberCheckbox[keys.length];
		methodl = new JLabel[keys.length];
		methodl2 = new JLabel[keys.length];

		for (int i = 0; i < keys.length; i++)
		{
			String name = Resource.getString("bazi.gui.method." + keys[i]);
			String tooltip = Resource.getString("bazi.gui.tooltip." + keys[i]);
			String help = Resource.getString("bazi.gui.method." + keys[i] + ".help");

			methodc[i] = new NumberCheckbox(name + "   ", methodcbg); // kleiner Hack: so ist das Label breit genug für Hare-/Droopvarianten
			methodc[i].setName(name);
			methodc[i].setToolTipText(tooltip);

			methodl[i] = new JLabel(help);

			methodl[i].setToolTipText(tooltip);
			methodl[i].setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));

			methodl2[i] = new JLabel();
			methodl2[i].setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
			methodl2[i].setForeground(Color.RED);
			methodl2[i].addMouseListener(subMouseAdapter);
			methodl2[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
			methodl2[i].setVisible(false);

			int b = (i == 2 || i == 5) ? 5 : 0;
			slMethods.add(methodc[i], 0, i + 1, 1, 1, 0, 0, GridBagConstraints.NONE,
					GridBagConstraints.WEST, 2, 5, b, 5);
			slMethods.add(methodl[i], 1, i + 1, 1, 1, 0, 0, GridBagConstraints.NONE,
					GridBagConstraints.WEST, 2, 5, b, 5);
			slMethods.add(methodl2[i], 1, i + 1, 1, 1, 0, 0, GridBagConstraints.NONE,
					GridBagConstraints.WEST, 2, 5, b, 5);
		}

		methodl[0].addMouseListener(quotaMouseAdapter);
		methodl[0].setCursor(new Cursor(Cursor.HAND_CURSOR));
		methodl[8].addMouseListener(quotaMouseAdapter);
		methodl[8].setCursor(new Cursor(Cursor.HAND_CURSOR));

		methodc[6].addItemListener(checkboxListener);
		tP = new JTextField();
		tP.setToolTipText(Resource.getString("bazi.gui.tooltip.divpot2"));
		slMethods.add(tP, 1, 7, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST, 6, 0, 1, 5);


		methodc[7].addItemListener(checkboxListener);
		tQ = new JTextField();
		tQ.setToolTipText(Resource.getString("bazi.gui.tooltip.divsta2"));
		slMethods.add(tQ, 1, 8, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.WEST, 1, 0, 1, 5);

		JLabel lMethods = new JLabel(Resource.getString("bazi.gui.methods") + ":");
		lMethods.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));


		addToFrame(pMethods, 2, 4, 2, 3, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, 1, 5, 7, 5);

		// Distriktmethoden
		prb = new JPanel(new GridLayout(3, 1));
		prb.setBorder(tbDistricts = BorderFactory.createTitledBorder(Resource.getString(
				"bazi.gui.district.title")));
		tbDistricts.setTitleFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));

		ButtonGroup bg = new ButtonGroup();
		rbDistricts = new JRadioButton(Resource.getString(
				"bazi.gui.district.districs"));
		rbDistricts.setSelected(true);
		rbDistricts.addItemListener(this);
		rbDistricts.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bg.add(rbDistricts);
		prb.add(rbDistricts);
		rbBiprop = new JRadioButton(Resource.getString("bazi.gui.district.biprop"));
		rbBiprop.addItemListener(this);
		rbBiprop.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bg.add(rbBiprop);
		prb.add(rbBiprop);
		rbNZZ = new JRadioButton(Resource.getString("bazi.gui.district.nzz"));
		rbNZZ.addItemListener(this);
		rbNZZ.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bg.add(rbNZZ);
		prb.add(rbNZZ);

		addToFrame(prb, 2, 7, 2, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL);

		// Berechnung - Button und Logo
		JPanel pCalc = new JPanel();
		pCalc.setLayout(new BorderLayout(5, 5));
		bCalculate = new JButton(Resource.getString("bazi.gui.calculation.start"));
		bCalculate.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		
		bCalculate.setPreferredSize(new Dimension(150, 25));
		bCalculate.setMargin(new Insets(5, 5, 5, 5));
		bCalculate.addActionListener(actionListener);
		logo = new JLabel(bazilogo);
		logo.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		pCalc.add(BorderLayout.NORTH, bCalculate);
		pCalc.add(BorderLayout.SOUTH, logo);
		addToFrame(pCalc, 2, 8, 2, 1, 0, 0, GridBagConstraints.NORTH,
				GridBagConstraints.NONE);

		// Ausgabe-Textfeld
		tOutput = new JTextArea(23, 40);
		tOutput.setFont(GUIConstraints.getFont(GUIConstraints.OUTPUT_FONT));
		tOutput.setEditable(false);
		tOutput.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), "scroll_down");
		tOutput.getActionMap().put("scroll_down", scroll_down);
		tOutput.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), "scroll_up");
		tOutput.getActionMap().put("scroll_up", scroll_up);
		scrollOutput = new JScrollPane();
		//Breite des Ergebnisfenster abhängig von der Schriftgröße
		if(GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) >= GUIConstraints.FONTSIZE_MIN)
		{
			dim_Width_Output = dim_Width_Output + (GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) - GUIConstraints.FONTSIZE_MIN) * 20;
		}
		scrollOutput.setPreferredSize(new Dimension(dim_Width_Output, dim_Height()));
		scrollOutput.getViewport().add(tOutput);
		addToFrame(scrollOutput, 4, 1, 5, 8, 1, 1, GridBagConstraints.NORTHWEST,
				GridBagConstraints.BOTH);
		setRBEnabled(false);
		setVisibleQ(false);
		setVisibleP(false);
		setContentPane(contentPanel);
		pack();

		// Checkboxen für Biprop Algorithmen initialisieren
		bipropBG = new ButtonGroup();
		mASmdpt = new JRadioButton();
		mASmdpt.setActionCommand(String.valueOf(DistrictInputData.ASMDPT));
		mASmdpt.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mASmdpt);
		mASextr = new JRadioButton();
		mASextr.setActionCommand(String.valueOf(DistrictInputData.ASEXTR));
		mASextr.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mASextr);
		mASrand = new JRadioButton();
		mASrand.setActionCommand(String.valueOf(DistrictInputData.ASRAND));
		mASrand.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mASrand);
		mTTflpt = new JRadioButton();
		mTTflpt.setActionCommand(String.valueOf(DistrictInputData.TTFLPT));
		mTTflpt.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mTTflpt);
		mTTinte = new JRadioButton();
		mTTinte.setActionCommand(String.valueOf(DistrictInputData.TTINTE));
		mTTinte.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mTTinte);
		mH_ASmp_TTfp = new JRadioButton();
		mH_ASmp_TTfp.setActionCommand(String.valueOf(DistrictInputData.H_ASMDPT_TTFLPT));
		mH_ASmp_TTfp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mH_ASmp_TTfp);
		mH_ASmp_TTfp.setSelected(true);
		mH_IPFP_TTfp = new JRadioButton();
		mH_IPFP_TTfp.setActionCommand(String.valueOf(DistrictInputData.H_IPFP_TTFLPT));
		mH_IPFP_TTfp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mH_IPFP_TTfp);
		mH_IPFP_TTin = new JRadioButton();
		mH_IPFP_TTin.setActionCommand(String.valueOf(DistrictInputData.H_IPFP_TTINTE));
		mH_IPFP_TTin.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mH_IPFP_TTin);
		mH_ASrd_TTfp = new JRadioButton();
		mH_ASrd_TTfp.setActionCommand(String.valueOf(DistrictInputData.H_ASRAND_TTFLPT));
		mH_ASrd_TTfp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mH_ASrd_TTfp);
		mH_ASex_TTfp = new JRadioButton();
		mH_ASex_TTfp.setActionCommand(String.valueOf(DistrictInputData.H_ASEXTR_TTFLPT));
		mH_ASex_TTfp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mH_ASex_TTfp);
		mH_ASmp_TTin = new JRadioButton();
		mH_ASmp_TTin.setActionCommand(String.valueOf(DistrictInputData.H_ASMDPT_TTINTE));
		mH_ASmp_TTin.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		// mH_ASmp_TTin.setEnabled(false);
		bipropBG.add(mH_ASmp_TTin);
		mH_ASex_TTin = new JRadioButton();
		mH_ASex_TTin.setActionCommand(String.valueOf(DistrictInputData.H_ASEXTR_TTINTE));
		mH_ASex_TTin.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		// mH_ASex_TTin.setEnabled(false);
		bipropBG.add(mH_ASex_TTin);
		mH_ASrd_TTin = new JRadioButton();
		mH_ASrd_TTin.setActionCommand(String.valueOf(DistrictInputData.H_ASRAND_TTINTE));
		mH_ASrd_TTin.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		// mH_ASrd_TTin.setEnabled(false);
		bipropBG.add(mH_ASrd_TTin);
		mIPFP = new JRadioButton();
		mIPFP.setActionCommand(String.valueOf(DistrictInputData.IPFP));
		mIPFP.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		// mIPFP.setEnabled(false);
		bipropBG.add(mIPFP);

		mNewton = new JRadioButton();
		mNewton.setActionCommand(String.valueOf(DistrictInputData.NEWTON));
		mNewton.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mNewton);

		mPrimal = new JRadioButton();
		mPrimal.setActionCommand(String.valueOf(DistrictInputData.PRIMAL));
		mPrimal.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bipropBG.add(mPrimal);

		setDate();

		dd = new DebugDialog(this);
		dd.setVisible(version.indexOf("b") != -1);

		setVisible(true);

		if (maximized)
		{
			setExtendedState(Frame.MAXIMIZED_BOTH);
		}
		else
		{
			Point p = properties.getPoint("window_position");
			if (p != null)
				setLocation(p);
			else
				setLocationRelativeTo(null);

			Integer state = properties.getInteger("window_extended_state");
			if (state != null)
				setExtendedState(state);
		}

		loadWindow.dispose();
	}
	
	/** Höhe des Fensters */
	private int dim_Height() 
	{
		int height = 400;
		if(GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) >= GUIConstraints.FONTSIZE_MIN) 
		{
			height = height + (GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) - GUIConstraints.FONTSIZE_MIN ) * 25;
		}
		return height;
	}

	/** Aktiviert oder deaktiviert die Checkboxen für die Berechnung mit mehreren
	 * Distrikten.
	 * 
	 * @param b Sollen die Checkboxen aktiviert werden? */
	public void setRBEnabled(boolean b)
	{
		setBMM(false, false, null, true);
		rbDistricts.setEnabled(b);
		rbNZZ.setEnabled(b);
		rbBiprop.setEnabled(b);

		TitledBorder tb = (TitledBorder) prb.getBorder();
		if (b)
		{
			tb.setTitleColor(Color.BLACK);
		}
		else
		{
			tb.setTitleColor(Color.GRAY);
		}
	}

	/** De-/Aktivieren von Rundungsmethoden beim Auswählen von Seperater Auswertung
	 * oder Biprop/NZZ
	 * (Interface ItemListener)
	 * 
	 * @param ie ItemEvent */
	public void itemStateChanged(ItemEvent ie)
	{

		// nur den Event beachten, bei dem ein Radiobutton auf TRUE gesetzt wurde
		if ((ie.getStateChange() == 1) && !bRB)
		{
			if (ie.getSource() == rbDistricts)
			{
				setNZZ(false);
				setBiprop(false);
				setAccMoreDistricts();
			}
			else if (ie.getSource() == rbNZZ)
			{
				setBiprop(true);
				setNZZ(true);
				setAccMoreDistricts();
			}
			else if (ie.getSource() == rbBiprop)
			{
				setBiprop(true);
				setNZZ(false);
				setAccMoreDistricts();
			}
		}

	}

	/** Referenz auf den DebugDialog
	 * 
	 * @return der aktuelle DebugDialog */
	public DebugDialog getDD()
	{
		return dd;
	}
	
	/** Referenz auf den TablePane
	 * 
	 * @return die aktuelle TablePane */
	public TablePane gettpTable()
	{
		return tpTable;
	}

	/** Referenz auf den FileIO
	 * 
	 * @return fileIO */
	public BaziFileIO getFileIO()
	{
		return bfio;
	}

	/** Setzen von Datum und Uhrzeit in das Titelfeld. */
	private void setDate()
	{
		Date d = new Date(System.currentTimeMillis());
		String s = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).
				format(d);
		tTitle.setText(s);
		tTitle.setCaretPosition(0);
	}

	/** Einfügen von Komponenten mittels des GridBagLayout in das Frame.
	 * 
	 * @param comp Einzufügende Komponente.
	 * @param x Gibt die Spalte an, in der der linke Teil der Komponente angezeigt wird.
	 * @param y Gibt die Zeile an, in der der obere Teil der Komponente angezeigt wird.
	 * @param width Gibt die Anzahl der Zellen an, in denen die Komponente in einer Zeile angezeigt wird.
	 * @param height Gibt die Anzahl der Zellen an, in denen die Komponente in einer Spalte angezeigt wird.
	 * @param weightx Gibt an, wie zusätzlicher horizontaler Raum aufgeteilt wird.
	 * @param weighty Gibt an, wie zusätzlicher vertikaler Raum aufgeteilt wird.
	 * @param fill Wenn der für die Komponente benötigte Platz kleiner als der zur Verfügung stehende Platz ist,
	 *          gibt dieser Parameter an, wie die Komponente den Platz ausfüllen soll.
	 * @param anchor Wenn der für die Komponente benötigte Platz kleiner als der zur Verfügung stehende Platz ist,
	 *          gibt dieser Parameter an, wo die Komponente platziert werden soll.
	 * @param top Gibt den Raum um die Komponente an, der frei bleibt.
	 * @param left Gibt den Raum um die Komponente an, der frei bleibt.
	 * @param bottom Gibt den Raum um die Komponente an, der frei bleibt.
	 * @param right Gibt den Raum um die Komponente an, der frei bleibt. */
	private void addToFrame(Component comp, int x, int y, int width, int height,
			double weightx, double weighty, int anchor, int fill,
			int top, int left, int bottom, int right)
	{
		// Layoutbedingungen setzen
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.fill = fill;
		gbc.anchor = anchor;
		gbc.insets = new Insets(top, left, bottom, right);
		gbl.setConstraints(comp, gbc);
		contentPanel.add(comp);
	}

	/** Einfügen von Komponenten mittels des GridBagLayout in das Frame.
	 * 
	 * @param comp Einzufügende Komponente.
	 * @param x Gibt die Spalte an, in der der linke Teil der Komponente
	 *          angezeigt wird.
	 * @param y Gibt die Zeile an, in der der obere Teil der Komponente angezeigt
	 *          wird.
	 * @param width Gibt die Anzahl der Zellen an, in denen die Komponente in
	 *          einer Zeile angezeigt wird.
	 * @param height Gibt die Anzahl der Zellen an, in denen die Komponente in
	 *          einer Spalte angezeigt wird.
	 * @param weightx Gibt an, wie zusätzlicher horizontaler Raum aufgeteilt wird.
	 * @param weighty Gibt an, wie zusätzlicher vertikaler Raum aufgeteilt wird.
	 * @param anchor Wenn der für die Komponente benötigte Platz kleiner als der
	 *          zur Verfügung stehende Platz ist, gibt dieser Parameter an, wo die
	 *          Komponente platziert werden soll.
	 * @param fill Wenn der für die Komponente benötigte Platz kleiner als der
	 *          zur Verfügung stehende Platz ist, gibt dieser Parameter an, wie die
	 *          Komponente den Platz ausfüllen soll. */
	private void addToFrame(Component comp, int x, int y, int width, int height,
			double weightx, double weighty, int anchor, int fill)
	{
		addToFrame(comp, x, y, width, height, weightx, weighty, anchor, fill, 5, 5,
				5, 5);
	}

	/** Setzen der Sichtbarkeit des Eingabefeldes für q.
	 * 
	 * @param bool Soll das Eingabefeld sichtbar sein? */
	public void setVisibleQ(boolean bool)
	{
		if (bool)
		{
			methodc[7].setLabel("r =");
		}
		else
		{
			methodc[7].setLabel();
		}
		methodl[7].setVisible(!bool);
		methodl2[7].setVisible(!bool);
		tQ.setVisible(bool);
		tQ.setText("");
	}

	/** Setzen der Sichtbarkeit des Eingabefeldes für p.
	 * 
	 * @param bool Soll das Eingabefeld sichtbar sein? */
	public void setVisibleP(boolean bool)
	{
		if (bool)
		{
			methodc[6].setLabel("p =");
		}
		else
		{
			methodc[6].setLabel();
		}
		methodl[6].setVisible(!bool);
		methodl2[6].setVisible(!bool);
		tP.setVisible(bool);
		tP.setText("");
	}

	/** Setzt den Text im Mandatspanel für einen Distrikt */
	private void setAccMono()
	{
		tbAcc.setTitle(Resource.getString("bazi.gui.accuracy"));
		lAccHelp.setText(Resource.getString("bazi.gui.accuracy.example"));
		pAcc.updateUI();
	}

	/** Setzt den Text im Mandatspanel für mehrere Distrikte */
	private void setAccMoreDistricts()
	{
		tbAcc.setTitle(Resource.getString("bazi.gui.subtotal"));
		lAccHelp.setText(Resource.getString("bazi.gui.accuracy.example2"));
		pAcc.updateUI();
	}

	/** Löschen aller Eingabe-Daten. */
	public void clearInput()
	{
		setTitle(title);
		setDate();
		tpTable.reset();
		tAccuracy.setText("");
		tAccuracy.setEditable(true);
		for (NumberCheckbox c : methodc)
			c.setState(false);
		setVisibleQ(false);
		setVisibleP(false);
		tpTable.adaptRows();
		rbDistricts.setSelected(true);
		setRBEnabled(false);
		setNZZ(false);
		setBiprop(false);
		setAccMono();
		setBMM(false, false, new int[]
		{ 0, 0, Integer.MAX_VALUE }, true);
		minPlusValue = 5;
		// System.out.println("clearInput");
		// mH_ASmp_TTfp.setSelected(true);
	}

	/** Liest die Eingabedaten für mehrere Distrikte aus der GUI und liefert sie
	 * als Objekt zurück.
	 * 
	 * @return Eingabedaten mit mehreren Distritken */
	private DistrictInputData readDistrictInputData()
	{
		DistrictInputData did = new DistrictInputData();
		InputData id = null;
		OutputFormat of = readOutputFormat();
		did.title = tTitle.getText();
		did.outputFormat = of;
		did.districts = new Vector<InputData>(tpTable.length());
		did.BMM = BMM;
		did.pow = powerWeighted;
		did.base = base;
		did.min = min;
		did.max = max;

		for (int i = 0; i < tpTable.length(); i++)
		{
			id = new InputData(this, dd);
			String tmp = Resource.getString("bazi.gui.district2") + " " + (i + 1) +
					": " + tpTable.getTabTitle(i);
			boolean parse = id.parseInputDataForDistrict(tpTable.getTable(i), tmp, tpTable.getAccAt(i), of, BMM, powerWeighted, base, min, max, minPlusValue);
			if (!parse)
				return null;
			id.district = tpTable.getTabTitle(i);
			did.districts.add(id);
			did.outputFormat.decimal_places = Math.max(did.outputFormat.decimal_places, id.outputFormat.decimal_places);
			// System.out.println("Added District " + i);
		}

		if (rbDistricts.isSelected())
		{
			did.method = DistrictInputData.SEPARATE;
		}
		else if (rbBiprop.isSelected())
		{
			did.method = DistrictInputData.BIPROP;
		}
		else if (rbNZZ.isSelected())
		{
			did.method = DistrictInputData.NZZ;
		}

		did.bipropAlt = Integer.valueOf(bipropBG.getSelection().getActionCommand());
		return did;
	}

	/** Liest die Einstellungen für das Ausgabeformat aus der GUI und erzeugt ein
	 * OutputFormat Objekt.
	 * 
	 * @return Ausgabeformat */
	private OutputFormat readOutputFormat()
	{
		OutputFormat of = new OutputFormat();
		wTable = tpTable.getFirstTable();
		of.labelNames = wTable.getColumnName(0);
		of.labelWeights = wTable.getColumnName(1);

		ConditionItem conditem = wTable.getSelectedCondition();
		of.condition = conditem.getFormat();
		if (of.condition == OutputFormat.CONDITION_MIN_PLUS) {
			if(minPlusValue % 1 == 0.0) 
				of.labelCondition = conditem.getShortname().replace("x", (int)Math.floor(minPlusValue) + "");
			else
				of.labelCondition = conditem.getShortname().replace("x", minPlusValue + "");
		}
		else
			of.labelCondition = conditem.getShortname();
		of.labelTotal = Resource.getString("bazi.gui.table.sum");

		// Methoden
		MethodData[] methods = readMethods();
		// if (methods == null) data[j] = null;
		if (methods == null)
		{
			return null;
		}
		else
		{
			of.methods = methods;

			// Divisor
		}

		if (cDivisor.getSelectedIndex() == 0)
			of.divisor = OutputFormat.DIV_DIVISOR_QUOTA;
		else if (cDivisor.getSelectedIndex() == 4)
			of.divisor = OutputFormat.DIV_QUOTIENT;
		else if (cDivisor.getSelectedIndex() == 1)
			of.divisor = OutputFormat.DIV_DIVISORINTERVAL;
		else if (cDivisor.getSelectedIndex() == 2)
			of.divisor = OutputFormat.DIV_MULTIPLIER;
		else if (cDivisor.getSelectedIndex() == 3)
			of.divisor = OutputFormat.DIV_MULTIPLIERINTERVAL;
		of.labelDivisor = MethodData.getLabel(of, false);

		// Ausrichtung
		switch (cAlignment.getSelectedIndex())
		{
		case 0:
			of.alignment = OutputFormat.ALIGN_HORIZONTAL;
			break;
		case 1:
			of.alignment = OutputFormat.ALIGN_VERTICAL;
			break;
		case 2:
			of.alignment = OutputFormat.ALIGN_HORI_VERT;
			break;
		case 3:
			of.alignment = OutputFormat.ALIGN_VERT_HORI;
			break;
		}

		if (cTies.getSelectedIndex() == 0)
		{
			of.ties = OutputFormat.TIES_CODED;
		}
		else if (cTies.getSelectedIndex() == 1)
		{
			of.ties = OutputFormat.TIES_LAC;
		}
		else if (cTies.getSelectedIndex() == 2)
		{
			of.ties = OutputFormat.TIES_LIST;

		}
		return of;
	}

	/** Schreiben der eingelesenen Daten in die GUI
	 * 
	 * @param data die eingelesenen Daten. Achtung: Es werden nur die Rohdaten
	 *          verarbeitet, Listenverbindungen müssen also getrennt (wie in der Datei)
	 *          vorliegen!
	 * @param wtable WeightsTable, in die die Daten geschrieben werden sollen */
	private void writeInputData(InputData data, WeightsTable wtable)
	{
		tTitle.setText(data.title);
		tTitle.setCaretPosition(0);

		tAccuracy.setText(data.accuracy);

		OutputFormat of = data.outputFormat;

		wtable.setColumnName(of.labelNames, 0);
		wtable.setColumnName(of.labelWeights, 1);

		// Tue hier so als wäre es nie Biprop. Bei Biprop wird die Bedingung später noch korrekt gesetzt
		switch (of.condition)
		{
		case OutputFormat.CONDITION_MIN:
			wtable.setMinDir(cond_min);
			break;
		case OutputFormat.CONDITION_DIRECT:
			wtable.setMinDir(cond_dir);
			break;
		case OutputFormat.CONDITION_NAIV:
			wtable.setMinDir(cond_naiv);
			break;
		case OutputFormat.CONDITION_MAX:
			wtable.setMinDir(cond_max);
			break;
		case OutputFormat.CONDITION_MIN_TO_MAX:
			wtable.setMinDir(cond_min_to_max);
			break;
		case OutputFormat.CONDITION_MIN_PLUS:
			wtable.setMinDir(cond_min_plus);
			minPlusValue = data.minPlusValue;
			break;
		case OutputFormat.CONDITION_MIN_VPV:
			wtable.setMinDir(cond_min_vpv);
			break;
		default:
			wtable.setMinDir(cond_none);
		}

		wtable.setColumnValues(data.vNames, 0);
		wtable.setColumnValues(data.vWeights, 1);
		if (data.vCond != null)
		{
			wtable.setColumnValues(data.vCond, 2);

		}
		if (of.methods != null)
		{
			MethodData[] met = of.methods;
			for (int i = 0; i < met.length; i++)
			{
				if (met[i].subapportionment != null)
				{
					setSubapportionment(met[i].subapportionment.name);
				}
				else
				{
					setSubapportionment(SubapportionmentSelector.DEFAULT);
				}
				String temp = met[i].name;
				if (temp.equals("haqgrr") || temp.equals("HaQgrR") ||
						temp.equals("quogrr") || temp.equals("QuoGrR") ||
						temp.equals("Hare-Niemeyer") || temp.equals("Hare/Niemeyer") ||
						temp.equals("Hamilton") || temp.equals("Vinton"))
				{
					methodc[0].setState(true);
				}
				else if (temp.equalsIgnoreCase("hq1grr"))
				{
					methodc[0].setState(true);
					setHareMethode(1);
					setHareResidualMethod(0);
				}
				else if (temp.equalsIgnoreCase("hq2grr"))
				{
					methodc[0].setState(true);
					setHareMethode(2);
					setHareResidualMethod(0);
				}
				else if (temp.equalsIgnoreCase("haqWTA"))
				{
					methodc[0].setState(true);
					setHareMethode(0);
					setHareResidualMethod(2);
				}
				else if (temp.equalsIgnoreCase("hq1WTA"))
				{
					methodc[0].setState(true);
					setHareMethode(1);
					setHareResidualMethod(2);
				}
				else if (temp.equalsIgnoreCase("hq2WTA"))
				{
					methodc[0].setState(true);
					setHareMethode(2);
					setHareResidualMethod(2);
				}
				else if (temp.equalsIgnoreCase("haqgR1"))
				{
					methodc[0].setState(true);
					setHareMethode(0);
					setHareResidualMethod(1);
				}
				else if (temp.equalsIgnoreCase("hq1gR1"))
				{
					methodc[0].setState(true);
					setHareMethode(1);
					setHareResidualMethod(1);
				}
				else if (temp.equalsIgnoreCase("hq2gR1"))
				{
					methodc[0].setState(true);
					setHareMethode(2);
					setHareResidualMethod(1);
				}
				else if (temp.equals("divabr") || temp.equals("DivAbr") ||
						temp.equals("divdwn") || temp.equals("DivDwn") ||
						temp.equals("d'Hondt") || temp.equals("d´Hondt") ||
						temp.equals("d`Hondt") || temp.equals("Jefferson") ||
						temp.equals("Hagenbach-Bischoff"))
				{
					methodc[1].setState(true);
				}
				else if (temp.equals("divstd") || temp.equals("DivStd") ||
						temp.equals("Sainte-Lague") || temp.equals("Sainté-Lague") ||
						temp.equals("Saintè-Lague") || temp.equals("Webster"))
				{
					methodc[2].setState(true);
				}
				else if (temp.equals("divauf") || temp.equals("DivAuf") ||
						temp.equals("divup") || temp.equals("DivUp") ||
						temp.equals("divupw") || temp.equals("DivUpw") ||
						temp.equals("Adams"))
				{
					methodc[5].setState(true);
				}
				else if (temp.equals("divhar") || temp.equals("DivHar") ||
						temp.equals("Dean"))
				{
					methodc[4].setState(true);
				}
				else if (temp.equals("divgeo") || temp.equals("DivGeo") ||
						temp.equals("Hill") || temp.equals("Huntington") ||
						temp.equals("Hill-Huntington") ||
						temp.equals("Hill/Huntington"))
				{
					methodc[3].setState(true);
				}
				else if (temp.equals("drqgrr") || temp.equals("DrQgrR") ||
						temp.equals("Droop"))
				{
					methodc[8].setState(true);
					setDroopMethode(0);
					setDroopResidualMethod(0);
				}
				else if (temp.equals("dq1grr") || temp.equals("DQ1grR"))
				{
					methodc[8].setState(true);
					setDroopMethode(1);
					setDroopResidualMethod(0);
				}
				else if (temp.equals("dq2grr") || temp.equals("DQ2grR"))
				{
					methodc[8].setState(true);
					setDroopMethode(2);
					setDroopResidualMethod(0);
				}
				else if (temp.equals("dq3grr") || temp.equals("DQ3grR"))
				{
					methodc[8].setState(true);
					setDroopMethode(3);
					setDroopResidualMethod(0);
				}
				else if (temp.equals("dq4grr") || temp.equals("DQ4grR"))
				{
					methodc[8].setState(true);
					setDroopMethode(4);
					setDroopResidualMethod(0);
				}
				else if (temp.equals("drqWTA") || temp.equals("DrQWTA") ||
						temp.equals("Droop"))
				{
					methodc[8].setState(true);
					setDroopMethode(0);
					setDroopResidualMethod(2);
				}
				else if (temp.equals("dq1WTA") || temp.equals("DQ1WTA"))
				{
					methodc[8].setState(true);
					setDroopMethode(1);
					setDroopResidualMethod(2);
				}
				else if (temp.equals("dq2WTA") || temp.equals("DQ2WTA"))
				{
					methodc[8].setState(true);
					setDroopMethode(2);
					setDroopResidualMethod(2);
				}
				else if (temp.equals("dq3WTA") || temp.equals("DQ3WTA"))
				{
					methodc[8].setState(true);
					setDroopMethode(3);
					setDroopResidualMethod(2);
				}
				else if (temp.equals("dq4WTA") || temp.equals("DQ4WTA"))
				{
					methodc[8].setState(true);
					setDroopMethode(4);
					setDroopResidualMethod(2);
				}
				else if (temp.equals("drqgR1") || temp.equals("DrQgR1") ||
						temp.equals("Droop"))
				{
					methodc[8].setState(true);
					setDroopMethode(0);
					setDroopResidualMethod(1);
				}
				else if (temp.equals("dq1gR1") || temp.equals("DQ1gR1"))
				{
					methodc[8].setState(true);
					setDroopMethode(1);
					setDroopResidualMethod(1);
				}
				else if (temp.equals("dq2gR1") || temp.equals("DQ2gR1"))
				{
					methodc[8].setState(true);
					setDroopMethode(2);
					setDroopResidualMethod(1);
				}
				else if (temp.equals("dq3gR1") || temp.equals("DQ3gR1"))
				{
					methodc[8].setState(true);
					setDroopMethode(3);
					setDroopResidualMethod(1);
				}
				else if (temp.equals("dq4gR1") || temp.equals("DQ4gR1"))
				{
					methodc[8].setState(true);
					setDroopMethode(4);
					setDroopResidualMethod(1);
				}
				else if (temp.equals("DivPow") || temp.equals("DivPot"))
				{
					methodc[6].setState(true);
					setVisibleP(true);
					if (met[i].paramString == null)
					{
						tP.setText(met[i].param + "");
					}
					else
					{
						tP.setText(met[i].paramString);
					}
				}
				else if (temp.equals("DivSta"))
				{
					methodc[7].setState(true);
					setVisibleQ(true);
					if (met[i].paramString == null)
					{
						tQ.setText(met[i].param + "");
					}
					else
					{
						tQ.setText(met[i].paramString);
					}
				}
			}
		}

		// System.out.println("of.alignment: " + of.alignment + " of.divisor: " + of.divisor);

		if (of.alignment == OutputFormat.ALIGN_HORIZONTAL)
		{
			setSelectAlignmentMode(0);
		}
		else if (of.alignment == OutputFormat.ALIGN_VERTICAL)
		{
			setSelectAlignmentMode(1);
		}
		else if (of.alignment == OutputFormat.ALIGN_HORI_VERT)
		{
			if (cAlignment.getItemCount() > 2)
				setSelectAlignmentMode(2);
			else
				setSelectAlignmentMode(0);
		}
		else if (of.alignment == OutputFormat.ALIGN_VERT_HORI)
		{
			if (cAlignment.getItemCount() > 3)
				setSelectAlignmentMode(3);
			else
				setSelectAlignmentMode(1);
		}
		if (of.divisor == OutputFormat.DIV_DIVISOR_QUOTA)
		{
			setSelectDivisorMode(0);
		}
		else if (of.divisor == OutputFormat.DIV_DIVISORINTERVAL)
		{
			setSelectDivisorMode(1);
		}
		else if (of.divisor == OutputFormat.DIV_MULTIPLIER)
		{
			setSelectDivisorMode(2);
		}
		else if (of.divisor == OutputFormat.DIV_MULTIPLIERINTERVAL)
		{
			setSelectDivisorMode(3);
		}
		else if (of.divisor == OutputFormat.DIV_QUOTIENT)
		{
			setSelectDivisorMode(4);
		}

		if (of.ties == OutputFormat.TIES_CODED)
		{
			cTies.setSelectedIndex(0);
		}
		else if (of.ties == OutputFormat.TIES_LAC)
		{
			cTies.setSelectedIndex(1);
		}
		else if (of.ties == OutputFormat.TIES_LIST)
		{
			cTies.setSelectedIndex(2);

		}

		setBMM(data.BMM, data.pow, new int[] { data.base, data.min, data.max }, false);
	} // writeInputData

	
	
	/** Schreiben mehrerer Distrikte in die GUI
	 * 
	 * @param did Die Distrikt-Daten */
	private void writeDistrictInputData(DistrictInputData did)
	{
		for (int i = 0; i < did.districts.size(); i++)
		{
			WeightsTable wtable = tpTable.addDistrict(did.districts.
					elementAt(i).title);
			writeInputData(did.districts.elementAt(i), wtable);
		}
		tTitle.setText(did.title);
		tTitle.setCaretPosition(0);
		tpTable.setSelectedIndex(0);
		setAccMoreDistricts();

		// setMethods!
		// System.out.println("method: " + did.method);
		switch (did.method)
		{
		case DistrictInputData.SEPARATE:
		case DistrictInputData.NONE:
		{
			rbDistricts.setSelected(true);
			switch (did.outputFormat.condition)
			{
			case OutputFormat.CONDITION_DIRECT:
				tpTable.changeCondition(cond_dir);
				break;
			case OutputFormat.CONDITION_NAIV:
				tpTable.changeCondition(cond_naiv);
				break;
			case OutputFormat.CONDITION_MAX:
				tpTable.changeCondition(cond_max);
				break;
			case OutputFormat.CONDITION_MIN_TO_MAX:
				tpTable.changeCondition(cond_min_to_max);
				break;
			case OutputFormat.CONDITION_MIN_PLUS:
				tpTable.changeCondition(cond_min_plus);
				minPlusValue = did.minPlusValue;
				break;
			case OutputFormat.CONDITION_MIN_VPV:
				tpTable.changeCondition(cond_min_vpv);
				break;
			default: // nothing to do
			}
			break;
		}
		case DistrictInputData.BIPROP:
			rbBiprop.setSelected(true);
			break;
		case DistrictInputData.NZZ:
			rbNZZ.setSelected(true);
			break;
		}

		// ComboBox der 3. Spalte der Gewichtstabelle muss gesetzt werden, weil sie
		// beim Umschalten auf Biprop zurückgesetzt wird
		if (did.method == DistrictInputData.BIPROP || did.method == DistrictInputData.NZZ)
		{
			switch (did.outputFormat.condition)
			{
			case OutputFormat.CONDITION_SUPER_MIN:
				tpTable.changeCondition(bp_cond_super_min);
				break;
			case OutputFormat.CONDITION_MIN:
				tpTable.changeCondition(bp_cond_min);
				break;
			case OutputFormat.CONDITION_NAIV:
				tpTable.changeCondition(cond_naiv);
				break;
			default:
				tpTable.changeCondition(cond_none);
			}
		}
		repaint();
	}

	/** Fehleranzeige beim Einlesen der Mandatszahlen. */
	public void errorAccuracy()
	{
		StringTokenizer st;
		if (isMultiple())
		{
			st = new StringTokenizer(Resource.getString(
					"bazi.error.accuracy2"), "\n");
		}
		else
		{
			st = new StringTokenizer(Resource.getString(
					"bazi.error.accuracy"), "\n");
		}
		Vector<String> v = new Vector<String>();
		while (st.hasMoreTokens())
		{
			v.addElement(st.nextToken());
		}
		Object[] mes = new Object[v.size()];
		for (int i = 0; i < mes.length; i++)
		{
			mes[i] = v.elementAt(i);

		}
		JOptionPane.showMessageDialog(this, mes,
				Resource.getString("bazi.error.attention"),
				JOptionPane.WARNING_MESSAGE);
	}

	/** Einlesen der für die Berechnung zu benutzenden Methoden.
	 * 
	 * @return Nach Benutzerangabe sortiertes Feld mit den Methoden. */
	private MethodData[] readMethods()
	{
		// Anzahl der ausgewaehlten Methoden
		int number = methodcbg.getRegistered();

		// falls keine Methode ausgewählt wurde
		if (number == 0)
		{
			String[] mes = Resource.getString("bazi.error.method").split("\n");
			JOptionPane.showMessageDialog(this, mes,
					Resource.getString("bazi.error.attention"),
					JOptionPane.WARNING_MESSAGE);
			return null;
		}

		MethodData[] data = new MethodData[number];
		for (int n = 1; n <= number; n++)
		{
			data[n - 1] = new MethodData();
			if (n == methodc[2].getNumber())
			{
				if (SubapportionmentSelector.DEFAULT.equals(subapportionment))
				{
					data[n - 1].method = MethodData.RSTATION;
					data[n - 1].param = 0.5;
					data[n - 1].name = methodc[2].getName();
				}
				else
				{
					data[n - 1] = new MethodData(methodc[2].getName() + "+" + getSubapportionment());
				}
			}
			else if (n == methodc[5].getNumber())
			{
				if (SubapportionmentSelector.DEFAULT.equals(subapportionment))
				{
					data[n - 1].method = MethodData.RSTATION;
					data[n - 1].param = 0.0;
					data[n - 1].name = methodc[5].getName();
				}
				else
				{
					data[n - 1] = new MethodData(methodc[5].getName() + "+" + getSubapportionment());
				}
			}
			else if (n == methodc[1].getNumber())
			{
				if (SubapportionmentSelector.DEFAULT.equals(subapportionment))
				{
					data[n - 1].method = MethodData.RSTATION;
					data[n - 1].param = 1.0;
					data[n - 1].name = methodc[1].getName();
				}
				else
				{
					data[n - 1] = new MethodData(methodc[1].getName() + "+" + getSubapportionment());
				}
			}
			else if (n == methodc[7].getNumber())
			{
				double q = -1d;
				String sq = tQ.getText().replace(';', ',').replace(' ', ',').replace(",,", ",").replace(",,", ",").trim();
				try
				{
					// Hier geschieht das Parsen und die Erzeugung der Fehlermeldung
					ExtendedStationary ext = new ExtendedStationary(sq);
					q = ext.getParam();
				}
				catch (ParameterOutOfRangeException poore)
				{
					JOptionPane.showMessageDialog(this, Resource.getString("bazi.error.r_parameter"), Resource.getString("bazi.error.attention"),
							JOptionPane.WARNING_MESSAGE);
					return null;
				}
				data[n - 1].method = MethodData.RSTATION;
				data[n - 1].param = q;
				if (sq.lastIndexOf(",") == sq.length() - 1)
				{
					sq = sq.substring(0, sq.length() - 1);
				}
				data[n - 1].name = "r=" + sq;
				if (sq.indexOf(",") >= 0)
				{
					data[n - 1].paramString = sq;
				}
			}
			else if (n == methodc[3].getNumber())
			{
				if (SubapportionmentSelector.DEFAULT.equals(subapportionment))
				{
					data[n - 1].method = MethodData.PMEAN;
					data[n - 1].param = 0.0;
					data[n - 1].name = methodc[3].getName();
				}
				else
				{
					data[n - 1] = new MethodData(methodc[3].getName() + "+" + getSubapportionment());
				}
			}
			else if (n == methodc[4].getNumber())
			{
				if (SubapportionmentSelector.DEFAULT.equals(subapportionment))
				{
					data[n - 1].method = MethodData.PMEAN;
					data[n - 1].param = -1.0;
					data[n - 1].name = methodc[4].getName();
				}
				else
				{
					data[n - 1] = new MethodData(methodc[4].getName() + "+" + getSubapportionment());
				}
			}
			else if (n == methodc[6].getNumber())
			{
				double p;
				String sp = tP.getText().replace(';', ',').replace(' ', ',').replace(",,", ",").replace(",,", ",").trim();
				try
				{
					ExtendedPowerMean extPM = new ExtendedPowerMean(sp);
					p = extPM.getParam();
				}
				catch (NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(this, Resource.getString("bazi.error.p_parameter"), Resource.getString("bazi.error.attention"),
							JOptionPane.WARNING_MESSAGE);
					return null;
				}
				data[n - 1].method = MethodData.PMEAN;
				data[n - 1].param = p;
				if (sp.lastIndexOf(",") == sp.length() - 1)
				{
					sp = sp.substring(0, sp.length() - 1);
				}
				data[n - 1].name = "p=" + sp;
				if (sp.indexOf(",") >= 0)
				{
					data[n - 1].paramString = sp;
				}
			}
			else if (n == methodc[0].getNumber())
			{
				if (SubapportionmentSelector.DEFAULT.equals(subapportionment))
				{
					data[n - 1].method = MethodData.QUOTA;
					data[n - 1].param = -hareMethod - hareResidualMethod / 10d;
					switch (10 * hareMethod + hareResidualMethod)
					{
					case 0:
						data[n - 1].name = Resource.getString("bazi.gui.method.haqgrr");
						break;
					case 2:
						data[n - 1].name = Resource.getString("bazi.gui.method.haqWTA");
						break;
					case 1:
						data[n - 1].name = Resource.getString("bazi.gui.method.haqgR1");
						break;
					case 10:
						data[n - 1].name = Resource.getString("bazi.gui.method.hq1grr");
						break;
					case 12:
						data[n - 1].name = Resource.getString("bazi.gui.method.hq1WTA");
						break;
					case 11:
						data[n - 1].name = Resource.getString("bazi.gui.method.hq1gR1");
						break;
					case 20:
						data[n - 1].name = Resource.getString("bazi.gui.method.hq2grr");
						break;
					case 22:
						data[n - 1].name = Resource.getString("bazi.gui.method.hq2WTA");
						break;
					case 21:
						data[n - 1].name = Resource.getString("bazi.gui.method.hq2gR1");
						break;
					}
				}
				else
				{
					data[n - 1] = new MethodData(methodc[0].getName() + "+" + getSubapportionment());
				}
			}
			else if (n == methodc[8].getNumber())
			{
				data[n - 1].method = MethodData.QUOTA;
				data[n - 1].param = 1.0d + droopMethod + droopResidualMethod / 10d;
				switch (10 * droopMethod + droopResidualMethod)
				{
				case 0:
					data[n - 1].name = Resource.getString("bazi.gui.method.drqgrr");
					break;
				case 2:
					data[n - 1].name = Resource.getString("bazi.gui.method.drqWTA");
					break;
				case 1:
					data[n - 1].name = Resource.getString("bazi.gui.method.drqgR1");
					break;
				case 10:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq1grr");
					break;
				case 12:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq1WTA");
					break;
				case 11:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq1gR1");
					break;
				case 20:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq2grr");
					break;
				case 22:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq2WTA");
					break;
				case 21:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq2gR1");
					break;
				case 30:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq3grr");
					break;
				case 32:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq3WTA");
					break;
				case 31:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq3gR1");
					break;
				case 40:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq4grr");
					break;
				case 42:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq4WTA");
					break;
				case 41:
					data[n - 1].name = Resource.getString("bazi.gui.method.dq4gR1");
					break;
				}
			}
		}

		return data;
	}

	/** Setzen eines Divisor-Formats.
	 * 
	 * @param index Index */
	public void setSelectDivisorMode(int index)
	{
		cDivisor.setSelectedIndex(index);
	}

	/** Setzen des Formats für die Ausrichtung der Ausgabe.
	 * 
	 * @param index Index */
	public void setSelectAlignmentMode(int index)
	{
		cAlignment.setSelectedIndex(index);
	}

	/** Multiplizität des Tablepanes hat sich geändert
	 * Implementierung von MultiplicityListener
	 * 
	 * @param me Der ausgelöste Event */
	public void multiplicityChanged(MultiplicityEvent me)
	{
		// System.out.println("Vielfachheit geändert: " + me);
		int mult = me.getMultiplicity();
		boolean bMult = false;
		if (mult > 1)
		{
			// aktiviere Distriktmethoden
			bMult = true;
			// if (rbDistricts.isSelected()) {
			// setAccSep();
			// setAccMoreDistricts();
			// setAlignCbBiprop();
			// }
			// else {
			// setAccBiprop();
			setAccMoreDistricts();
			setAlignCbMoreDistricts();
			// }
		}
		else
		{
			// mult==1
			// deaktiviere Distriktmethoden

			bMult = false;
			setAccMono();
			setAlignCbMonoprop();
		}
		setRBEnabled(bMult);
	}

	/** Gibt an, ob mehrere Distrikte aktiv sind
	 * 
	 * @return Multiple-Zustand (<code>false</code>: ein Distrikt, <code>true</code>:
	 *         mehrere Distrikte */
	public boolean isMultiple()
	{
		return tpTable.getMultiple();
	}

	/** Starten der Berechnung als Thread.
	 * Implementierung von Runnable. */
	public void run()
	{
		/* Testlogausgabe: per logger und log4j-Mechanismus und in Debugfenster
		 * per printMessage
		 */
		/*
		 * logger.warn("This is warn : ");
		logger.error("This is error : ");
		logger.fatal("This is fatal : ");
		
		if (dd != null) {
			String output = "<" + Resource.getString("bazi.gui.calculation.start") + "> gedrückt!";
			dd.printMessage(output);
			dd.printMessage(System.getProperty("user.dir"));
		}*/

		// Zuerst überprüfen, ob Listenverbundungen mit base+min..max benutzt wird
		if (BMM)
		{
			WeightsTable wt = tpTable.getFirstTable();
			for (String s : wt.getColumnValuesAsStrings(0))
				if (s.contains("+"))
				{
					JOptionPane.showMessageDialog(this, Resource.getString("bazi.error.bmm+list"),
							Resource.getString("bazi.error.title"),
							JOptionPane.WARNING_MESSAGE);
					bCalculate.setText(Resource.getString("bazi.gui.calculation.start"));
					calculateThread = null;
					return;
				}
		}

		try
		{
			AbstractInputData aid;
			if (isMultiple())
			{
				aid = readDistrictInputData();
			}
			else
			{
				// Monoproportional in einem Distrikt
				// aid = readInputData(tpTable.getFirstTable(), tTitle.getText(),
				// tAccuracy.getText(), readOutputFormat());
				aid = new InputData(this, dd);
				// Einstellungen für Base+Min..Max(pow)
				boolean parse = ((InputData) aid).parseInputData(tpTable.getFirstTable(), tTitle.getText(), tAccuracy.getText(), readOutputFormat(),
						BMM, powerWeighted, base, min, max, minPlusValue);
				if (!parse)
					aid = null;
			}
			if (aid == null)
			{
				bCalculate.setText(Resource.getString("bazi.gui.calculation.start"));
				calculateThread = null;
				return;
			}

			aid.sortBiprop = !mSortBiprop.isSelected();

			// ProgressDialog pd = new ProgressDialog(this);
			ProgressDialog pd = null;
			PermutationsWatcher pw = null;

			// Im Debug Modus wird übernimmt der ProgressDialog die Aufgaben des
			// PermutationsCommunicator.
			if (Debug.BIPROP)
				pd = new ProgressDialog(this);
			else
				pw = new PermutationsWatcher(this);

			Calculation calc = new Calculation(aid);
			calc.setIterationListener(pd);
			calc.setMethodListener(dd);

			if (Debug.BIPROP)
				calc.setPermutationsCommunicator(pd);
			else
				calc.setPermutationsCommunicator(pw);
			String output = calc.calculate();

			if (output != null)
			{
				int pos = scrollOutput.getVerticalScrollBar().getMaximum() - 20;
				if (pos == scrollOutput.getViewport().getHeight() - 20)
					pos = 0;
				if (scroll_positions.size() == 0 || !scroll_positions.get(scroll_positions.size() - 1).equals(pos))
					scroll_positions.add(pos);
			}

			tOutput.append(output);

			if (output != null)
			{
				tOutput.append("\n\n");
				tOutput.setCaretPosition(tOutput.getText().length());
			}
			mOutputDelete.setEnabled(true);
			bCalculate.setText(Resource.getString("bazi.gui.calculation.start"));
			calculateThread = null;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			String mes = e.toString();
			StackTraceElement[] stea = e.getStackTrace();
			for (int i = 0; i < stea.length; i++)
			{
				mes += "\n\t" + stea[i];
			}
			JOptionPane.showMessageDialog(this, mes,
					Resource.getString("bazi.error.attention"),
					JOptionPane.WARNING_MESSAGE);
			bCalculate.setText(Resource.getString("bazi.gui.calculation.start"));
			calculateThread = null;
		}
	} // end run()

	/** Beenden des Programms. */
	private void close()
	{
		properties.setPoint("window_position", getLocation());
		properties.setInteger("window_extended_state", getExtendedState());
		properties.save();
		System.exit(0);
	}

	/** Rückgabe der Ausgabe der Berechung.
	 * 
	 * @return Output */
	public String getOutput()
	{
		return tOutput.getText();
	}

	/** Öffnen einer URL in einem neuen Browser-Fenster.
	 * 
	 * @param docName URL im String-Format. */
	public void showDocument(String docName)
	{
		BrowserLaunch.openURL(docName);
	}

	/** Startet den Dialog zum Auswählen von verschiedenen Biprop Methoden */
	void bipropSelector()
	{

		new BipropSelector(this);

	}

	/** Eingabe "Wahl in getrennten Bezirken..." */
	void districts()
	{
		boolean forced = tpTable.getMultiple() && tpTable.getDistrictData().size() == 1;
		DistrictDialog dd = new DistrictDialog(roundFrame, tpTable.getDistrictData(), forced);
		boolean tmp = dd.start();
		// System.out.println("Rückgabe vom DD: " + tmp);
		if (tmp)
		{
			// jetzt müssen die Distrikte geschrieben werden

			DistrictQueue dq = dd.getData();
			// System.out.println(dq);
			tpTable.flushQueue(dq);

			// Soll Biprop verwendet werden, auch wenn es nur einen Distrikt gibt?
			if (dd.forceBiprop() && !tpTable.getMultiple())
				if (tpTable.forceBiprop())
				{
					// Yeah, it's a hack, but at least it doesn't break the logic...
					multiplicityChanged(new MultiplicityEvent(tpTable, 0, 2));
					tpTable.getTabbedPane().setTitleAt(0, "1: " + tpTable.getTabbedPane().getTitleAt(0));
				}

			if (tpTable.getMultiple())
			{
				tAccuracy.setText(tpTable.getAccAt(tpTable.getSelectedIndex()));
				setRBEnabled(true);
			}
			else
			{
				tAccuracy.setEditable(true);
				tAccuracy.setText(tpTable.getFirstAcc());
				setBiprop(false);
				setNZZ(false);
				setRBEnabled(false);

				// Still a hack, see above
				multiplicityChanged(new MultiplicityEvent(tpTable, 0, 1));
			}
		}

		int index = dd.getSelectedDistrict();
		if (index >= 0)
			tpTable.setSelectedIndex(index);

	}

	/** De-/Aktivierung des Doppeltproportionalen Zuteilungsverfahrens
	 * 
	 * @param b an oder aus */
	public void setBiprop(boolean b)
	{
		// DEBUG
		// System.err.println("DPP-aufruf");
		// System.out.println("De-/Aktivierung des Dpp: " + b);
		// mBipropAlts.setEnabled(b);
		setHareMethode(0);
		methodc[8].setEnabled(!b);
		methodl[8].setEnabled(!b);
		methodc[0].setEnabled(!b);
		methodl[0].setEnabled(!b);
		// cDean.setEnabled(!b);
		// lDean.setEnabled(!b);
		// cHill.setEnabled(!b);
		// lHill.setEnabled(!b);
		// cAdams.setEnabled(!b);
		// lAdams.setEnabled(!b);
		// cPmean.setEnabled(!b);
		// lPmean.setEnabled(!b);

		// if (b && cDivisor.getItemCount() > 4)
		// {
		// if (cDivisor.getSelectedIndex() == 4)
		// cDivisor.setSelectedIndex(0);
		// cDivisor.removeItemAt(4);
		// } else if (!b && cDivisor.getItemCount() < 5)
		// cDivisor.addItem(Resource.getString("bazi.gui.div.quotient"));

		if (b)
		{
			setRBEnabled(true);
			bRB = true;
			rbBiprop.setSelected(true);
			bRB = false;

			// Bei TTinte noch ein paar Methoden deaktivieren
			// nicht mehr notwendig
			/* boolean b2 = !mTTinte.isSelected();
			 * cHill.setEnabled(b2);
			 * lHill.setEnabled(b2);
			 * cDean.setEnabled(b2);
			 * lDean.setEnabled(b2);
			 * cAdams.setEnabled(b2);
			 * lAdams.setEnabled(b2);
			 * cPmean.setEnabled(b2);
			 * lPmean.setEnabled(b2); */

		}
		else
		{
			if (!isMultiple())
				// setAlignCbBiprop();
				// else
				setAlignCbMonoprop();

			methodc[6].setToolTipText(Resource.getString("bazi.gui.tooltip.divpot"));

			// Wegen TTinte noch ein paar Methoden re-aktivieren
			// nicht mehr notwendig
			/* cHill.setEnabled(true);
			 * lHill.setEnabled(true);
			 * cDean.setEnabled(true);
			 * lDean.setEnabled(true);
			 * cAdams.setEnabled(true);
			 * lAdams.setEnabled(true);
			 * cPmean.setEnabled(true);
			 * lPmean.setEnabled(true); */

		}

		// tpTable.setCondEnabeled(!b);
		ConditionItem conditem = tpTable.getSelectedCondition();

		if (b)
		{
			tpTable.changeCondition(cond_none);
		}
		tpTable.setCondBiprop(b);

		if (!b && conditem == bp_cond_min)
			tpTable.changeCondition(cond_min);
		else if (b && conditem == cond_min)
			tpTable.changeCondition(bp_cond_min);
		else if (b && (conditem == bp_cond_min || conditem == bp_cond_super_min))
			tpTable.changeCondition(conditem);

		repaint();
	}

	/** Setzt die Einträge in der Combobox fürs Ausgabeformat für mehrere Distrikte. */
	private void setAlignCbMoreDistricts()
	{
		cAlignment.removeAllItems();
		cAlignment.addItem(Resource.getString("bazi.gui.align.horizontal"));
		cAlignment.addItem(Resource.getString("bazi.gui.align.vertical"));
		cAlignment.addItem(Resource.getString("bazi.gui.align.horivert"));
		cAlignment.addItem(Resource.getString("bazi.gui.align.verthori"));
		cAlignment.setSelectedIndex(3);
	}

	/** Setzt die Einträge in der Combobox fürs Ausgabeformat für einen Distrikt. */
	private void setAlignCbMonoprop()
	{
		cAlignment.removeAllItems();
		cAlignment.addItem(Resource.getString("bazi.gui.align.horizontal"));
		cAlignment.addItem(Resource.getString("bazi.gui.align.vertical"));
		cAlignment.setSelectedIndex(1);
	}

	/** Ist Biprop aktiv?
	 * 
	 * @return Status von Biprop */
	public boolean isBipropSelected()
	{
		return (rbBiprop.isEnabled() && (rbBiprop.isSelected() || rbNZZ.isSelected()));
	}

	/** Setzen des NZZ
	 * 
	 * @param bn Status des NZZ */
	public void setNZZ(boolean bn)
	{
		// bNzz = bn;
		if (bn)
		{
			bRB = true;
			rbNZZ.setSelected(true);
			bRB = false;
		}
	}

	/** nach Tabellenwechsel müssen die Menüs angepaßt werden (identisch mit SListener.valueChanged()) */
	public void tableSwitched()
	{
		WeightsTable wtInput = tpTable.getTable(tpTable.getSelectedIndex());
		int[] a = wtInput.getSelectedRows();
		// for (int i=0; i<a.length; i++) System.out.print(a[i] + " ");
		// System.out.println("\\");
		if (a.length == 0)
		{
			// bDel.setEnabled(false);
			mLinesDelete.setEnabled(false);
			return;
		}
		if (a[0] < wtInput.getRowTotal())
		{
			// bDel.setEnabled(true);
			mLinesDelete.setEnabled(true);
			return;
		}
		// bDel.setEnabled(false);
		mLinesDelete.setEnabled(false);
	}

	/** Stellt BAZI komplett auf den Anfangszustand */
	public void restart()
	{
		clearInput();
		database.closeInfoFrame();
		if (FileIO.info != null && FileIO.info.isVisible())
		{
			FileIO.info.setText("");
			FileIO.info.setVisible(false);
		}
		mReOpen.setEnabled(false);
		tOutput.setText("");
		scroll_positions.clear();
		cDivisor.setSelectedIndex(0);
		cAlignment.setSelectedIndex(1);
		cTies.setSelectedIndex(0);
		mOutputDelete.setEnabled(false);
		setTitle(title);
		currentFile = null;
		setDroopMethode(0);
		setDroopResidualMethod(0);
		setHareMethode(0);
		setHareResidualMethod(0);
		setSubapportionment(SubapportionmentSelector.DEFAULT);
		dd.clear();
	}

	/** Schreibt die übergebenen Eingabedaten in die GUI
	 * 
	 * @param aid Eingabedaten */
	public void open(AbstractInputData aid)
	{
		clearInput();
		if (aid instanceof InputData)
		{
			writeInputData((InputData) aid, tpTable.getFirstTable());
		}
		else
		{
			writeDistrictInputData((DistrictInputData) aid);
		}
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setCurrentFile(java.io.File f)
	{
		currentFile = f;
		if (f == null)
		{
			mReOpen.setEnabled(false);
		}
		else
		{
			mReOpen.setEnabled(true);
		}
	}

	public void droopSelection()
	{
		new DroopSelector(this);
	}

	public void hareSelection()
	{
		new HareSelector(this);
	}

	public int getDroopMethod()
	{
		assert droopMethod >= 0 && droopMethod <= 4;
		return droopMethod;
	}

	public int getDroopResidualMethod()
	{
		assert droopResidualMethod >= 0 && droopResidualMethod <= 2;
		return droopResidualMethod;
	}

	public void setDroopMethode(int method)
	{
		assert method >= 0 && method <= 4;
		droopMethod = method;
		aktualisiereDroopAnzeige();
	}

	public void setDroopResidualMethod(int method)
	{
		assert method >= 0 && method <= 2;
		droopResidualMethod = method;
		aktualisiereDroopAnzeige();
	}

	public void aktualisiereDroopAnzeige()
	{
		if (droopMethod > 0 || droopResidualMethod > 0)
		{
			String[] name = { "", "gR1", "WTA" };
			if (droopResidualMethod == 0)
			{
				// this.droopMethod != 0
				methodc[8].setName(Resource.getString("bazi.gui.method.dq" + droopMethod + "grr"));
				methodc[8].setColor(Color.RED);
				methodl[8].setForeground(Color.RED);
				methodl[8].setText(Resource.getString("bazi.gui.method.dq" + droopMethod + "grr.help"));
			}
			else if (droopMethod == 0)
			{
				// this.droopResidualMethod != 0
				methodc[8].setName(Resource.getString("bazi.gui.method.drq" + name[droopResidualMethod]));
				methodc[8].setColor(Color.RED);
				methodl[8].setForeground(Color.RED);
				methodl[8].setText(Resource.getString("bazi.gui.method.drq" + name[droopResidualMethod] + ".help"));
			}
			else
			{
				// this.droopResidualMethod != 0 && this.droopMethod != 0
				methodc[8].setName(Resource.getString("bazi.gui.method.dq" + droopMethod + name[droopResidualMethod]));
				methodc[8].setColor(Color.RED);
				methodl[8].setForeground(Color.RED);
				methodl[8].setText(Resource.getString("bazi.gui.method.dq" + droopMethod + name[droopResidualMethod] + ".help"));
			}
		}
		else
		{
			methodc[8].setName(Resource.getString("bazi.gui.method.drqgrr"));
			methodc[8].setColor(Color.BLACK);
			methodl[8].setForeground(Color.BLACK);
			methodl[8].setText(Resource.getString("bazi.gui.method.drqgrr.help"));
		}
	}

	public int getHareMethod()
	{
		assert hareMethod >= 0 && hareMethod <= 2;
		return hareMethod;
	}

	public void setHareMethode(int method)
	{
		assert method >= 0 && method <= 2;
		hareMethod = method;
		aktualisiereHareAnzeige();
	}

	public int getHareResidualMethod()
	{
		assert hareResidualMethod >= 0 && hareResidualMethod <= 2;
		return hareResidualMethod;
	}

	public void setHareResidualMethod(int method)
	{
		assert method >= 0 && method <= 2;
		hareResidualMethod = method;
		aktualisiereHareAnzeige();
	}

	public void aktualisiereHareAnzeige()
	{

		if (hareMethod > 0 || hareResidualMethod > 0)
		{
			String[] name = { "", "gR1", "WTA" };
			if (hareResidualMethod == 0)
			{
				// this.hareMethod != 0
				methodc[0].setName(Resource.getString("bazi.gui.method.hq" + hareMethod + "grr"));
				methodc[0].setColor(Color.RED);
				methodl[0].setForeground(Color.RED);
				methodl[0].setText(Resource.getString("bazi.gui.method.hq" + hareMethod + "grr.help"));
			}
			else if (hareMethod == 0)
			{
				// this.hareResidualMethod != 0
				methodc[0].setName(Resource.getString("bazi.gui.method.haq" + name[hareResidualMethod]));
				methodc[0].setColor(Color.RED);
				methodl[0].setForeground(Color.RED);
				methodl[0].setText(Resource.getString("bazi.gui.method.haq" + name[hareResidualMethod] + ".help"));
			}
			else
			{
				// this.droopResidualMethod != 0 && this.droopMethod != 0
				methodc[0].setName(Resource.getString("bazi.gui.method.hq" + hareMethod + name[hareResidualMethod]));
				methodc[0].setColor(Color.RED);
				methodl[0].setForeground(Color.RED);
				methodl[0].setText(Resource.getString("bazi.gui.method.hq" + hareMethod + name[hareResidualMethod] + ".help"));
			}
		}
		else
		{
			methodc[0].setName(Resource.getString("bazi.gui.method.haqgrr"));
			methodc[0].setColor(Color.BLACK);
			methodl[0].setForeground(Color.BLACK);
			methodl[0].setText(Resource.getString("bazi.gui.method.haqgrr.help"));
		}
	}

	public boolean isBMM()
	{
		return BMM;
	}

	public boolean isPowerWeighted()
	{
		return powerWeighted;
	}

	public int[] getBMMData()
	{
		int[] out =
		{ base, min, max };
		return out;
	}

	public void setBMM(boolean b1, boolean b2, int[] i, boolean setOptions)
	{
		if (i == null)
			i = new int[] { 0, 0, Integer.MAX_VALUE };
		if (BMM == b1 && powerWeighted == b2
				&& i[0] == base && i[1] == min && i[2] == max)
			return;

		BMM = b1;
		powerWeighted = b2;
		base = i[0];
		min = i[1];
		max = i[2];

		if (BMM)
			setSubapportionment(SubapportionmentSelector.DEFAULT);

		String s = base + "+" + min;
		s += ".." + (max < Integer.MAX_VALUE ? max : "oo");
		s += (powerWeighted ? "(Pwr)" : "");
		for (int j = 1; j < 6; j++)
		{
			methodl[j].setVisible(!BMM);
			methodl2[j].setText(s);
			methodl2[j].setVisible(BMM);
		}

		for (int j : new int[] { 0, 6, 7, 8 })
		{
			methodc[j].setEnabled(!BMM);
			methodl[j].setEnabled(!BMM);
		}

		tpTable.setCondBMM(BMM);

		mHareAlts.setEnabled(setOptions);
		mDroopAlts.setEnabled(setOptions);
		mSubapportionmentAlts.setEnabled(setOptions);
		//mMinPlus.setEnabled(setOptions);
		mSortBiprop.setEnabled(setOptions);
		mBipropAlts.setEnabled(setOptions);
		mSep.setEnabled(setOptions);

	}

	public String getSubapportionment()
	{
		return subapportionment;
	}

	public void setSubapportionment(String s)
	{
		if (s.equals(subapportionment))
			return;

		subapportionment = s;

		if (!s.equals(SubapportionmentSelector.DEFAULT))
			setBMM(false, false, null, true);


		boolean def = SubapportionmentSelector.DEFAULT.equals(s);

		for (int i = 0; i < 6; i++)
		{
			boolean b = def || methodc[i].getName().equals(s);
			methodl[i].setVisible(b);
			methodl2[i].setText("+" + s);
			methodl2[i].setVisible(!b);
		}
	}

	/** <b>Title:</b> Klasse IListener<br>
	 * <b>Description:</b> Überwachung des Status der Checkboxen<br>
	 * <b>Copyright:</b> Copyright (c) 2001<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @author Jan Petzold
	 * @version 2.0 */
	private class IListener
			implements ItemListener
	{

		/** Aufruf, wenn der Status der Checkboxen geändert wurde.
		 * 
		 * @param ie ItemEvent */
		public void itemStateChanged(ItemEvent ie)
		{

			if (ie.getItemSelectable() == methodc[7])
			{
				if (methodc[7].getState())
				{
					setVisibleQ(true);
				}
				else
				{
					setVisibleQ(false);
				}
			}

			if (ie.getItemSelectable() == methodc[6])
			{
				if (methodc[6].getState())
				{
					setVisibleP(true);
				}
				else
				{
					setVisibleP(false);
				}
			}

		}
	}

	static public RoundFrame getRoundFrame()
	{
		return roundFrame;
	}

	/** <b>Title:</b> Klasse HelpListener<br>
	 * <b>Description:</b> Überwachung der Maus-Ereignisse auf der Oberfläche<br>
	 * <b>Copyright:</b> Copyright (c) 2001<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @author Jan Petzold
	 * @version 2.0 */
	private class HelpListener
			extends MouseAdapter
	{

		/** Koordinaten des Fensters */
		int x;

		/** Koordinaten des Fensters */
		int y;

		/** Koordinaten relativ im Fenster */
		int xRel;

		/** Koordinaten relativ im Fenster */
		int yRel;

		/** wurde schon mal mit dem Hilfscursor geclickt? */
		boolean click = false;

		/** Aufruf, wenn mit der Maus geklickt wurde.
		 * 
		 * @param me MouseEvent */
		@Override
		public void mouseClicked(MouseEvent me)
		{
			Component glass = roundFrame.getGlassPane();
			if (!click)
			{
				if (glass.isVisible())
				{
					Point p = glass.getLocationOnScreen();
					xRel = me.getX();
					yRel = me.getY();
					x = p.x + xRel;
					y = p.y + yRel;

					for (int i = 0; i < methodc.length; i++)
					{
						if (isInside(methodc[i]) || isInside(methodl[i]) || isInside(methodl2[i]))
						{
							showHelp(Resource.getString("bazi.help." + keys[i]),
									Resource.getString("bazi.help." + keys[i] + ".text"));
							return;
						}
					}

					// über welcher Komponente befindet sich der Cursor
					if (isInside(lTitle) || isInside(tTitle))
					{
						showHelp(Resource.getString("bazi.help.title"),
								Resource.getString("bazi.help.title.text"));
					}
					else if (isInside(tAccuracy))
					{
						showHelp(Resource.getString("bazi.help.accuracy"),
								Resource.getString("bazi.help.accuracy.text"));
					}
					else if (isInside(tpTable))
					{
						showHelp(Resource.getString("bazi.help.table"),
								Resource.getString("bazi.help.table.text"));
					}
					else if (isInside(bCalculate))
					{
						showHelp(Resource.getString("bazi.help.calculation"),
								Resource.getString("bazi.help.calculation.text"));
					}
					else if (isInside(lOutput))
					{
						showHelp(Resource.getString("bazi.help.outputformat"),
								Resource.getString("bazi.help.outputformat.text"));
					}
					else if (isInside(tOutput))
					{
						showHelp(Resource.getString("bazi.help.output"),
								Resource.getString("bazi.help.output.text"));
					}
					else
					{ // kein Tooltip vorhanden
						glass.setCursor(Cursor.getDefaultCursor());
						glass.setVisible(false);
						click = false;
						return;
					}
					glass.setCursor(Cursor.getDefaultCursor());
					click = true;
				}

			}
			else
			{
				glass.setVisible(false);
				click = false;
			}
		}

		/** Angabe, ob sich die Maus auf der Komponente comp befindet.
		 * 
		 * @param comp Komponente
		 * @return <b>true</b> wenn sich der Cursor in der Komponente befindet */
		private boolean isInside(Component comp)
		{
			if (comp.isVisible())
			{
				Point p = comp.getLocationOnScreen();
				Dimension d = comp.getPreferredSize();
				if (x >= p.x && x <= p.x + d.width && y >= p.y && y <= p.y + d.height)
				{
					return true;
				}
			}
			return false;
		}

		/** Anzeigen des Hilfetextes zu einer angeklickten Komponente.
		 * 
		 * @param headline Überschrift der Hilfe.
		 * @param help Eigentlicher Hilfetext. */
		private void showHelp(String headline, String help)
		{
			Component glass = roundFrame.getGlassPane();
			Graphics g = glass.getGraphics();

			// Zerlegen des Strings
			StringTokenizer st = new StringTokenizer(help, "\n");
			FontMetrics fm = g.getFontMetrics(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
			int lines = st.countTokens();
			int h = fm.getHeight();
			int w = g.getFontMetrics(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD)).stringWidth(headline);
			while (st.hasMoreTokens())
			{
				int temp = fm.stringWidth(st.nextToken());
				if (temp > w)
				{
					w = temp;
				}
			}

			// Fenster zeichnen
			Dimension d = glass.getSize();
			if (d.width < (xRel + 10 + w + 20))
			{
				xRel = d.width - (w + 20 + 10) - 3;
			}
			if (d.height < (yRel + (h * (lines + 1)) + 20))
			{
				yRel = d.height - ((h * (lines + 1)) + 20) - 3;

			}
			g.setColor(Color.decode("#fffff0"));
			g.fillRect(xRel + 10, yRel, w + 20, (h * (lines + 1)) + 20);
			g.setColor(Color.black);
			g.drawRect(xRel + 10, yRel, w + 20, (h * (lines + 1)) + 20);
			g.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
			g.drawString(headline, xRel + 10 + 10, yRel + h + 5);
			g.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
			st = new StringTokenizer(help, "\n");
			int i = 1;
			while (st.hasMoreTokens())
			{
				g.drawString(st.nextToken(), xRel + 10 + 10, yRel + h + 10 + (i * h));
				i++;
			}
		}

	}

	/** <b>Title:</b> Klasse AListener<br>
	 * <b>Description:</b> Überwachung der Button und Menüs<br>
	 * <b>Copyright:</b> Copyright (c) 2001<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @author Jan Petzold
	 * @version 2.0 */
	private class AListener
			implements ActionListener
	{

		/** Aufruf, wenn ein Button oder ein Menu-Eintrag angeklickt wurde.
		 * 
		 * @param event ActionEvent */
		public void actionPerformed(ActionEvent event)
		{

			// "Calculate" - Button
			if (event.getSource() == bCalculate)
			{
				if (calculateThread == null)
				{
					// auch hier tritt wohl noch der Fehler auf, daß eine nicht beendete
					// Eingabe in der Tabelle nicht bearbeitet wird.
					// deswegen muß vor dem Start des Threads die Eingabe mit "Gewalt"
					// beendet werden:
					tpTable.getTable(tpTable.getSelectedIndex()).finishInput();

					calculateThread = new Thread(roundFrame);
					calculateThread.start();
					bCalculate.setText(Resource.getString("bazi.gui.calculation.stop"));
				}
				else
				{
					calculateThread.interrupt();
					try
					{
						calculateThread.join();
					}
					catch (InterruptedException ie)
					{
						System.err.println(ie);
					}
					calculateThread = null;
				}
			}

			// Menusteuerung
			// Menu Datei
			if (event.getSource() == mOpen)
			{
				AbstractInputData aid = bfio.open();
				if (aid != null)
				{
					open(aid);
					setCurrentFile(bfio.getLoadedFile());
					String file = currentFile.getAbsolutePath();
					setTitle(title + " [" + file + "]");
					mReOpen.setEnabled(true);
					Dialog_Min_Plus.minPlus(RoundFrame.minPlusValue, RoundFrame.this.gettpTable().getTables());
				}
			}
			// Erneut öffnen
			else if (event.getSource() == mReOpen)
			{
				if (currentFile == null)
				{
					mReOpen.setEnabled(false);
					JOptionPane.showMessageDialog(RoundFrame.this, "Es ist keine Datei geladen!", "Achtung", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				AbstractInputData aid = bfio.open(currentFile);
				if (aid != null)
				{
					open(aid);
					currentFile = bfio.getLoadedFile();
					String file = currentFile.getAbsolutePath();
					setTitle(title + " [" + file + "]");
					Dialog_Min_Plus.minPlus(RoundFrame.minPlusValue, RoundFrame.this.gettpTable().getTables());
				}
			}
			// Eingabe Speichern
			else if (event.getSource() == mSave)
			{
				AbstractInputData aid;
				if (isMultiple())
				{
					aid = readDistrictInputData();
				}
				else
				{
					// Monoproportional in einem Distrikt
					aid = new InputData(roundFrame, roundFrame.dd);
					boolean parse = ((InputData) aid).parseInputData(tpTable.getFirstTable(), tTitle.getText(), tAccuracy.getText(), readOutputFormat(),
							BMM, powerWeighted, base, min, max, minPlusValue);
					if (!parse)
						aid = null;
				}
				if (aid != null)
				{
					bfio.saveAs(tTitle.getText(), aid);
					setCurrentFile(bfio.getLoadedFile());
					String file = currentFile.getAbsolutePath();
					setTitle(title + " [" + file + "]");
				}
			}
			else if (event.getSource() == mExport)
			{
				bfio.export(getOutput());
			}
			else if (event.getSource() == mExit)
			{
				close();
			}

			// Menu Bearbeiten
			else if (event.getSource() == mLinesDelete)
			{
				tpTable.deleteSelectedRows();
			}
			else if (event.getSource() == mInputDelete)
			{
				clearInput();
				database.closeInfoFrame();
			}
			else if (event.getSource() == mOutputDelete)
			{
				tOutput.setText("");
				scroll_positions.clear();
				mOutputDelete.setEnabled(false);
			}
			else if (event.getSource() == mRestart)
			{
				restart();
			}
			else if (event.getSource() == mSep)
			{
				districts();
			}
			else if (event.getSource() == mBipropAlts)
			{
				bipropSelector();
			}
			else if (event.getSource() == mDroopAlts)
			{
				droopSelection();
			}
			else if (event.getSource() == mHareAlts)
			{
				hareSelection();
			}
			else if (event.getSource() == mSubapportionmentAlts)
			{
				new SubapportionmentSelector(RoundFrame.this);
			}
			else if (event.getSource() == mBBM)
			{
				new Dialog_BMM(RoundFrame.this);
			}
			else if (event.getSource() == mMinPlus)
			{
				minPlusValue = new Dialog_Min_Plus(RoundFrame.this, minPlusValue, gettpTable().getTables()).getValue();
			}
			// Menu Hilfe
			else if (event.getSource() == mDirecthelp)
			{
				Cursor c = roundFrame.getToolkit().createCustomCursor(helpcursor.
						getImage(), new Point(0, 0), "help");
				roundFrame.getGlassPane().setCursor(c);
				roundFrame.getGlassPane().setVisible(true);
			}
			else if (event.getSource() == mInfo)
			{
				new AboutDialog(roundFrame, bazilogo);
			}
			else if (event.getSource() == mLicense)
			{
				new LicenseDialog(roundFrame, bazilogo);
			}
			else if (event.getSource() == mUpdate)
			{
				try 
				{
					Desktop d = Desktop.getDesktop();
					d.browse(new URI(Resource.getString("bazi.download.url")));
				}
				catch(Exception e) {}
				//VersionControl.check();

			}
			else if (event.getSource() == mSettings)
			{
				new Dialog_Settings(roundFrame);
			}
		}

	}

	private final AbstractAction scroll_up = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0)
		{
			scroll(-1);
		}
	};

	private final AbstractAction scroll_down = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent arg0)
		{
			scroll(1);
		}
	};

	/** Scrollt die Ausgabe. d > 0 bedeutet nach unten zu scrollen
	 * 
	 * @param d Anzahl der Einträge */
	private void scroll(int d)
	{
		int value = scrollOutput.getVerticalScrollBar().getValue();

		int less = 0, greater = 0;
		for (int i = 0; i < scroll_positions.size(); i++)
		{
			if (value == scroll_positions.get(i))
			{
				less = greater = i;
				break;
			}
			if (value > scroll_positions.get(i))
			{
				less = i;
				greater = i + 1;
			}
			else
				break;
		}

		int index = (d > 0 ? less : greater) + d;
		index = Math.min(scroll_positions.size() - 1, index);
		index = Math.max(0, index);
		value = scroll_positions.get(index);
		scrollOutput.getVerticalScrollBar().setValue(value);
	}

	/** <b>Title:</b> Klasse SListener<br>
	 * <b>Description:</b> Überwacht die Tabelle auf Selektionen<br>
	 * <b>Copyright:</b> Copyright (c) 2002<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @author Florian Kluge
	 * @version 2.0 */
	private class SListener
			implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent d)
		{
			// System.out.println("SListener called");
			// bDel.setEnabled(true);
			// System.out.println(d.getFirstIndex() + " " + d.getLastIndex() + " " + d.getValueIsAdjusting());
			if (!d.getValueIsAdjusting())
			{
				WeightsTable wtInput = tpTable.getTable(tpTable.getSelectedIndex());
				int[] a = wtInput.getSelectedRows();
				// for (int i=0; i<a.length; i++) System.out.print(a[i] + " ");
				// System.out.println("\\");
				if (a.length == 0)
				{
					// bDel.setEnabled(false);
					mLinesDelete.setEnabled(false);
					return;
				}
				if (a[0] < wtInput.getRowTotal())
				{
					// bDel.setEnabled(true);
					mLinesDelete.setEnabled(true);
					return;
				}
				// bDel.setEnabled(false);
				mLinesDelete.setEnabled(false);
			}
		}
	}

	/** <b>Title:</b> MListener<br>
	 * <b>Description:</b> MouseListener für das TabbedPane<br>
	 * <b>Copyright:</b> Copyright (c) 2002<br>
	 * <b>Company:</b><br>
	 * 
	 * @author Florian Kluge
	 * @version 1.0 */
	private class MListener
			extends MouseAdapter
	{

		@Override
		public void mouseClicked(MouseEvent e)
		{
			// Bei einem Doppelklick auf die Tabs soll der Distrikt-Dialog geöffnet
			// werden
			if (e.getClickCount() == 2)
			{
				districts();
			}
		}
	}

	private class QuotaMouseAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getSource() == methodl[0])
			{
				hareSelection();
			}
			else if (e.getSource() == methodl[8])
			{
				droopSelection();
			}
		}
	}

	private class SubMouseAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (BMM)
				new Dialog_BMM(RoundFrame.this);
			else
				new SubapportionmentSelector(RoundFrame.this);
		}
	}

	public void setMenuOptions(boolean option) {
		mMinPlus.setEnabled(option);
	}

}