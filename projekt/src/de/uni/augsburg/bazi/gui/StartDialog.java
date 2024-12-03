/*
 * @(#)StartDialog.java 2.1 07/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.VersionControl;

/** <b>Title:</b> Klasse StartDialog<br>
 * <b>Description:</b> Start Dialog zur Bestimmung der Sprache<br>
 * <b>Copyright:</b> Copyright (c) 2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public class StartDialog extends JDialog implements ActionListener
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Panel für die Anzeige */
	private final JPanel pContent;

	/** Panel für die Anzeige */
	private final JPanel pButton;

	/** Buttons zur Auswahl der Sprache */
	private final JButton pDe;

	/** Buttons zur Auswahl der Sprache */
	private final JButton pEs;

	/** Buttons zur Auswahl der Sprache */
	private final JButton pEn;

	/** Buttons zur Auswahl der Sprache */
	private final JButton pFr;

	/** Buttons zur Auswahl der Sprache */
	private final JButton pIt;
	
	/** Buttons zur Auswahl der Sprache */
	private final JButton pSK;

	/** Bilder der Flaggen */
	private final ImageIcon iDe;

	/** Bilder der Flaggen */
	private final ImageIcon iEs;

	/** Bilder der Flaggen */
	private final ImageIcon iEn;

	/** Bilder der Flaggen */
	private final ImageIcon iFr;

	/** Bilder der Flaggen */
	private final ImageIcon iIt;
	
	/** Bilder der Flaggen */
	private final ImageIcon iSK;

	/** Standardkonstruktor */
	public StartDialog()
	{
		setTitle("BAZI");
		setModal(true);
		setAlwaysOnTop(true);
		pContent = new JPanel(new BorderLayout());

		pButton = new JPanel();
		pButton.setLayout(new FlowLayout());

		iDe = new ImageIcon(getClass().getResource("images/flag_d.gif"));
		pDe = new JButton("Deutsch", iDe);
		pDe.addActionListener(this);
		pButton.add(pDe);

		iEn = new ImageIcon(getClass().getResource("images/flag_gb.gif"));
		pEn = new JButton("English", iEn);
		pEn.addActionListener(this);
		pButton.add(pEn);

		iEs = new ImageIcon(getClass().getResource("images/flag_es.gif"));
		pEs = new JButton("Español", iEs);
		pEs.addActionListener(this);
		pButton.add(pEs);

		iFr = new ImageIcon(getClass().getResource("images/flag_fr.gif"));
		pFr = new JButton("Français", iFr);
		pFr.addActionListener(this);
		pButton.add(pFr);

		iIt = new ImageIcon(getClass().getResource("images/flag_it.gif"));
		pIt = new JButton("Italiano", iIt);
		pIt.addActionListener(this);
		pButton.add(pIt);
		
		iSK = new ImageIcon(getClass().getResource("images/flag_sk.jpg"));
		pSK = new JButton("Slovák", iSK);
		pSK.addActionListener(this);
		pButton.add(pSK);

		pContent.setBackground(Color.WHITE);

		pContent.add(pButton, BorderLayout.SOUTH);

		// ImageIcon startImage = new ImageIcon(getClass().getResource("images/bazi_start.png"));
		JPanel pImg = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g.drawImage(new ImageIcon(getClass().getResource("images/bazi_start.png")).getImage(), 0, 0, null);
				g.setFont(new FontUIResource("SansSerif", Font.BOLD, 15));
				String version = String.format(Resource.getString("bazi.gui.version"), VersionControl.getVersion());
				g.drawString(version, 186, 220);
			}
		};
		pImg.setPreferredSize(new Dimension(484, 236));

		JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		wrapper.setBackground(Color.WHITE);
		wrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
		wrapper.add(pImg);
		pContent.add(wrapper);

		setContentPane(pContent);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/** Event-Handling
	 * (Interface ActionListener)
	 * 
	 * @param ae ActionEvent */
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == pDe)
		{
			Resource.setLang("de");
			Language.setLanguage("de");
		}
		else if (ae.getSource() == pEn)
		{
			Resource.setLang("en");
			Language.setLanguage("en");
		}
		else if (ae.getSource() == pEs)
		{
			Resource.setLang("es");
			Language.setLanguage("es");
		}
		else if (ae.getSource() == pFr)
		{
			Resource.setLang("fr");
			Language.setLanguage("fr");
		}
		else if (ae.getSource() == pIt)
		{
			Resource.setLang("it");
			Language.setLanguage("it");
		}
		else if (ae.getSource() == pSK)
		{
			Resource.setLang("sk");
			Language.setLanguage("sk");
		}
		// und jetzt returnen...
		dispose();
	}
}
