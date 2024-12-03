/*
 * @(#)BipropSelector.java 3.3 09/09/04
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.DistrictInputData;

/** <b>Überschrift:</b> Biprop Algorithmen Dialog<br>
 * <b>Beschreibung:</b> Dialog zum auswählen verschiedener Biprop Algorithmen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 3.4
 * @author Robert Bertossi, Christian Brand */
public class BipropSelector extends JDialog implements ActionListener
{
	/* Versionshistorie:
	 * 2010.05-b-01: Version 3.4
	 * - Einbinden des primalen Algorithms
	 * 2009.09-b-01: Version 3.3
	 * - Das Fenster wird nun in einer anderen Größe aufgebaut,
	 * um auch auf dem MacPC ordentlich auszzusehen
	 * 2009.01-b-01: Version 3.2
	 * - Einführung der Versionshistorie
	 * - Einführen eines reinen IPFP Algorithmus */

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Referenz auf RoundFrame */
	private RoundFrame rf;

	/** zeigt an, ob sich die Auswahl geändert hat */
	private boolean changed = false;

	/** der zu Beginn ausgewählte Algorithmus */
	private ButtonModel rbSelected = null;

	/** Infos zum ausgewählten Algorithmus */
	private JLabel algoInfo = null;

	/** Infos zum ausgewählten Algorithmus */
	private JLabel asNote = null;
	private String s_asNote = null;
	private String s_ipfpNote = null;

	/** Default Textfarbe; wird für das Unsichtbarmachen des AS-Hinweises verwendet */
	private Color foreground = null;

	/** Buttons */
	private JButton bOK;

	/** Buttons */
	private JButton bCancel;

	private boolean asText = true;

	private JPanel rbPanel;

	private JTextField error_IPFP = new JTextField("  " + de.uni.augsburg.bazi.lib.PureIPFPHelper.getPureIPFPHelper().getError());
	private boolean textFieldShown_IPFP = false;

	private JTextField error_Newton = new JTextField("  " + de.uni.augsburg.bazi.lib.newton.ReducedNewtonProcedure.getMaxError());
	private boolean textFieldShown_Newton = false;

	/** Erzeugt einen Dialog zum auswählen von Biprop Algorithmen, macht diesen
	 * aber noch nicht sichtbar.
	 * 
	 * @param _rf RoundFrame */
	public BipropSelector(RoundFrame _rf)
	{
		super(_rf, Resource.getString("bazi.gui.biprop.alt"), true);

		rf = _rf;

		rf.mASmdpt.addActionListener(this);
		rf.mASextr.addActionListener(this);
		rf.mASrand.addActionListener(this);
		rf.mTTflpt.addActionListener(this);
		rf.mTTinte.addActionListener(this);
		rf.mIPFP.addActionListener(this);
		rf.mNewton.addActionListener(this);
		rf.mPrimal.addActionListener(this);
		rf.mH_ASmp_TTfp.addActionListener(this);
		rf.mH_IPFP_TTfp.addActionListener(this);
		rf.mH_IPFP_TTin.addActionListener(this);
		rf.mH_ASrd_TTfp.addActionListener(this);
		rf.mH_ASex_TTfp.addActionListener(this);
		rf.mH_ASmp_TTin.addActionListener(this);
		rf.mH_ASex_TTin.addActionListener(this);
		rf.mH_ASrd_TTin.addActionListener(this);

		rbSelected = rf.bipropBG.getSelection();

		Container cPane = getContentPane();
		cPane.setLayout(new BorderLayout());

		rbPanel = new JPanel(new GridBagLayout());

		// Spalten Überschriften
		JLabel dummy = new JLabel("Finish");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		dummy.setForeground(Color.BLUE);
		addToPanel(rbPanel, dummy, 0, 0, 2, 1, GridBagConstraints.EAST);

		dummy = new JLabel("Start");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 0, 1, 2, 1, GridBagConstraints.WEST);

		dummy = new JLabel("Tie-and-Transfer (TT) algorithm in...");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.BLUE);
		addToPanel(rbPanel, dummy, 2, 0, 2, 1, GridBagConstraints.CENTER);

		dummy = new JLabel("...floating point arithmetic", JLabel.CENTER); // <html><center>
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.BLUE);
		addToPanel(rbPanel, dummy, 2, 1, 1, 1, GridBagConstraints.CENTER); // ,GridBagConstraints.BOTH);

		dummy = new JLabel("...integer arithmetic", JLabel.CENTER);
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.BLUE);
		addToPanel(rbPanel, dummy, 3, 1, 1, 1, GridBagConstraints.CENTER); // ,GridBagConstraints.BOTH);

		dummy = new JLabel("None (= pure Method)");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.BLUE);
		addToPanel(rbPanel, dummy, 4, 0, 1, 2, GridBagConstraints.WEST);

		// Zeilentitel
		dummy = new JLabel("<html>Alternating<br>Scaling (AS)<br>algorithm with...</html>");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 0, 2, 1, 3, GridBagConstraints.WEST);

		dummy = new JLabel("...midpoint divisors");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 1, 2, 1, 1, GridBagConstraints.WEST);

		dummy = new JLabel("...extreme divisors");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 1, 3, 1, 1, GridBagConstraints.WEST);

		dummy = new JLabel("...random divisors");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 1, 4, 1, 1, GridBagConstraints.WEST);

		dummy = new JLabel("Iterative Proportional Fitting Procedure");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 0, 5, 2, 1, GridBagConstraints.WEST);

		dummy = new JLabel("Newton Method");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 0, 6, 2, 1, GridBagConstraints.WEST);

		dummy = new JLabel("Primal Method");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 0, 7, 2, 1, GridBagConstraints.WEST);

		dummy = new JLabel("None (= pure TT)");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		dummy.setForeground(Color.RED);
		addToPanel(rbPanel, dummy, 0, 8, 2, 1, GridBagConstraints.WEST);

		// Radio-Buttons
		addToPanel(rbPanel, rf.mH_ASmp_TTfp, 2, 2, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mH_ASmp_TTin, 3, 2, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mASmdpt, 4, 2, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mH_ASex_TTfp, 2, 3, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mH_ASex_TTin, 3, 3, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mASextr, 4, 3, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mH_ASrd_TTfp, 2, 4, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mH_ASrd_TTin, 3, 4, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mASrand, 4, 4, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mH_IPFP_TTfp, 2, 5, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mH_IPFP_TTin, 3, 5, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mPrimal, 4, 7, 1, 1, GridBagConstraints.CENTER);

		addToPanel(rbPanel, rf.mTTflpt, 2, 8, 1, 1, GridBagConstraints.CENTER);
		addToPanel(rbPanel, rf.mTTinte, 3, 8, 1, 1, GridBagConstraints.CENTER);


		// IPFP Info
		s_ipfpNote = "<html><font size=2><i>Note:</i> The procedure      <br>calculates an ideal share   <br>matrix, up to the error      <br>specified.</font></html>";

		// AS-Info
		s_asNote = "<html><font size=2><i>Note:</i> Pure AS algorithms <br>generally work fine, but may<br>stall when many ties end up  <br>in a weird pattern.  </font></html>";
		asNote = new JLabel(s_asNote);
		asNote.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_PLAIN));
		foreground = asNote.getForeground();
		addToPanel(rbPanel, asNote, 4, 8, 1, 2, GridBagConstraints.CENTER);


		// Infozeile
		dummy = new JLabel("The output panel will contain this algorithmic information:");
		dummy.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		addToPanel(rbPanel, dummy, 0, 9, 5, 1, GridBagConstraints.CENTER);

		algoInfo = new JLabel("Dummy text to fix pack()");
		algoInfo.setFont(GUIConstraints.getFont(GUIConstraints.OUTPUT_FONT));
		addToPanel(rbPanel, algoInfo, 0, 10, 5, 1, GridBagConstraints.CENTER);

		cPane.add(rbPanel, BorderLayout.CENTER);

		JPanel pButtons = new JPanel(new FlowLayout());
		bOK = new JButton(Resource.getString("bazi.gui.ok"));
		bOK.addActionListener(this);
		pButtons.add(bOK);
		bCancel = new JButton(Resource.getString("bazi.gui.cancel"));
		bCancel.addActionListener(this);
		pButtons.add(bCancel);
		cPane.add(pButtons, BorderLayout.SOUTH);

		// Zum schließen des Fensters
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				changed = false;
				rbSelected.setSelected(true);
				setVisible(false);
				dispose();
			}
		});

		setAlgorithmicInfo();
		pack();
		Dimension d = this.getSize();
		d.height += 20;
		d.width += 20;
		this.setSize(d);
		setLocationRelativeTo(rf);
		setVisible(true);
	}

	/** Fügt eine Komponente zum übergebenen Panel mit den angegebenen
	 * GridBagConstraints hinzu. Bei jp muss ein GridBagLayout gesetzt sein.
	 * 
	 * @param jp JPanel
	 * @param comp JComponent
	 * @param x Spalte
	 * @param y Zeile
	 * @param width Breite
	 * @param height Höhe
	 * @param anchor Ausrichtung */
	private void addToPanel(JPanel jp, JComponent comp, int x, int y, int width, int height, int anchor)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = anchor;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.insets = new Insets(0, 5, 2, 5);

		jp.add(comp, gbc);
	}

	/** actionPerformed
	 * (Interface ActionListener)
	 * 
	 * @param e ActionEvent */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == bCancel)
		{
			changed = false;
			rbSelected.setSelected(true);

			rf.mASmdpt.removeActionListener(this);
			rf.mASextr.removeActionListener(this);
			rf.mASrand.removeActionListener(this);
			rf.mTTflpt.removeActionListener(this);
			rf.mTTinte.removeActionListener(this);
			rf.mIPFP.removeActionListener(this);
			rf.mNewton.removeActionListener(this);
			rf.mPrimal.removeActionListener(this);
			rf.mH_ASmp_TTfp.removeActionListener(this);
			rf.mH_IPFP_TTfp.removeActionListener(this);
			rf.mH_IPFP_TTin.removeActionListener(this);
			rf.mH_ASrd_TTfp.removeActionListener(this);
			rf.mH_ASex_TTfp.removeActionListener(this);
			rf.mH_ASmp_TTin.removeActionListener(this);
			rf.mH_ASex_TTin.removeActionListener(this);
			rf.mH_ASrd_TTin.removeActionListener(this);

			setVisible(false);
			dispose();
		}
		else if (e.getSource() == bOK)
		{

			changed = !(rf.bipropBG.getSelection() == rbSelected) || rbSelected == rf.mIPFP;

			rf.mASmdpt.removeActionListener(this);
			rf.mASextr.removeActionListener(this);
			rf.mASrand.removeActionListener(this);
			rf.mTTflpt.removeActionListener(this);
			rf.mTTinte.removeActionListener(this);
			rf.mIPFP.removeActionListener(this);
			rf.mNewton.removeActionListener(this);
			rf.mPrimal.removeActionListener(this);
			rf.mH_ASmp_TTfp.removeActionListener(this);
			rf.mH_IPFP_TTfp.removeActionListener(this);
			rf.mH_IPFP_TTin.removeActionListener(this);
			rf.mH_ASrd_TTfp.removeActionListener(this);
			rf.mH_ASex_TTfp.removeActionListener(this);
			rf.mH_ASmp_TTin.removeActionListener(this);
			rf.mH_ASex_TTin.removeActionListener(this);
			rf.mH_ASrd_TTin.removeActionListener(this);

			if (rf.mIPFP.isSelected())
			{
				try
				{
					double d_error = Double.parseDouble(error_IPFP.getText().trim().replace(',', '.'));
					de.uni.augsburg.bazi.lib.PureIPFPHelper.getPureIPFPHelper().setError(d_error);
				}
				catch (NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(this, "Fehler beim Einlesen der Toleranz für die IPFP Methode.\nBitte Eingabe korrigieren.",
							"Fehler!", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			if (rf.mNewton.isSelected())
			{
				try
				{
					double d_error = Double.parseDouble(error_Newton.getText().trim().replace(',', '.'));
					de.uni.augsburg.bazi.lib.newton.ReducedNewtonProcedure.setMaxError(d_error);
				}
				catch (NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(this, "Fehler beim Einlesen der Toleranz für die Newton Methode.\nBitte Eingabe korrigieren.",
							"Fehler!", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			setVisible(false);
			dispose();
		}
		else
			// es war ein RadioButton
			setAlgorithmicInfo();
	}

	/** Prüft ob sich die Auswahl geändert hat
	 * 
	 * @return <b>true</b>, wenn sich der ausgewählte Algorithmus geändert hat */
	public boolean selectionChanged()
	{
		return changed;
	}

	/** Setzt die Info-Zeile entsprechend für den ausgewählten Algorithmus */
	private void setAlgorithmicInfo()
	{
		String statOut = "[";
		switch (Integer.parseInt(rf.bipropBG.getSelection().getActionCommand()))
		{
		case (DistrictInputData.TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.ttflpt.short") +
					": " + Resource.getString("bazi.gui.biprop.updates") + "=a, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=b";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.ttinte.short") +
					": " + Resource.getString("bazi.gui.biprop.updates") + "=a, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=b";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.IPFP):
			statOut += Resource.getString("bazi.gui.biprop.alt.ipfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") + "=a, " +
					Resource.getString("bazi.gui.biprop.error") + "=b";
			if (this.asText)
			{
				asNote.setForeground(asNote.getBackground());
				asNote.setText(s_ipfpNote);
				this.asText = false;
			}
			if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			asNote.setForeground(foreground);
			rbPanel.remove(rf.mIPFP);
			rbPanel.repaint();
			addToPanel(rbPanel, error_IPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
			textFieldShown_IPFP = true;
			break;
		case (DistrictInputData.ASMDPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.asmdpt.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a";
			if (!this.asText)
			{
				asNote.setForeground(asNote.getBackground());
				asNote.setText(s_asNote);
				this.asText = true;
			}
			asNote.setForeground(foreground);
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.ASRAND):
			statOut += Resource.getString("bazi.gui.biprop.alt.asrand.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a";
			if (!this.asText)
			{
				asNote.setForeground(asNote.getBackground());
				asNote.setText(s_asNote);
				this.asText = true;
			}
			asNote.setForeground(foreground);
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.ASEXTR):
			statOut += Resource.getString("bazi.gui.biprop.alt.asextr.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a";
			if (!this.asText)
			{
				asNote.setForeground(asNote.getBackground());
				asNote.setText(s_asNote);
				this.asText = true;
			}
			asNote.setForeground(foreground);
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_ASMDPT_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asmp_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_ASRAND_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asrd_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_ASEXTR_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asex_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_IPFP_TTFLPT):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_ipfp_ttfp.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_IPFP_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_ipfp_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_ASMDPT_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asmp_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_ASRAND_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asrd_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.H_ASEXTR_TTINTE):
			statOut += Resource.getString("bazi.gui.biprop.alt.h_asex_ttin.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") +
					"=a | " + Resource.getString("bazi.gui.biprop.updates") + "=b, " +
					Resource.getString("bazi.gui.biprop.transfers") + "=c";
			asNote.setForeground(asNote.getBackground());
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;
		case (DistrictInputData.NEWTON):
			statOut += Resource.getString("bazi.gui.biprop.alt.newton.short") +
					": " + Resource.getString("bazi.gui.biprop.iterations") + "=a, " +
					Resource.getString("bazi.gui.biprop.error") + "=b";
			if (this.asText)
			{
				asNote.setForeground(asNote.getBackground());
				asNote.setText(s_ipfpNote);
				this.asText = false;
			}
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			asNote.setForeground(foreground);
			rbPanel.remove(rf.mNewton);
			rbPanel.repaint();
			addToPanel(rbPanel, error_Newton, 4, 6, 1, 1, GridBagConstraints.CENTER);
			textFieldShown_Newton = true;
			break;
		case (DistrictInputData.PRIMAL):
			statOut += Resource.getString("bazi.gui.biprop.alt.primal.short") +
					": " + Resource.getString("bazi.gui.biprop.primal.iterations") + "=(a,b)";
			if (this.asText)
			{
				asNote.setForeground(asNote.getBackground());
				asNote.setText(s_ipfpNote);
				this.asText = false;
			}
			if (textFieldShown_IPFP)
			{
				rbPanel.remove(error_IPFP);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mIPFP, 4, 5, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_IPFP = false;
			}
			else if (textFieldShown_Newton)
			{
				rbPanel.remove(error_Newton);
				rbPanel.repaint();
				addToPanel(rbPanel, rf.mNewton, 4, 6, 1, 1, GridBagConstraints.CENTER);
				this.textFieldShown_Newton = false;
			}
			break;

		default:
			statOut += "-Missing!-";
		}
		statOut += "]";
		algoInfo.setText(statOut);
	}
}
