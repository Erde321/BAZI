/*
 * @(#)DroopSelector.java 1.0 09/09/04
 * 
 * Copyright (c) 2000-2009 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */
package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import de.uni.augsburg.bazi.Resource;

/** <b>Überschrift:</b> Droop-Quota Dialog<br>
 * <b>Beschreibung:</b> Dialog zum auswählen verschiedener Droop Quota<br>
 * <b>Copyright:</b> Copyright (c) 2000-2010<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 1.2
 * @author Christian Brand, Marco Schumacher */
public class DroopSelector extends JDialog
{
	/* Versionshistorie:
	 * 2010.05-b-01: Version 1.2
	 * - Einbau neuer Droop Methoden
	 * 2009.09-b-02: Version 1.1
	 * - kleinere BugFixes an den Texten
	 * - Die Bilder werden nun nicht mehr als Buttons realisiert.
	 * 2009.09-b-01: Version 1.0
	 * - Initialversion, die sich stark am BipropSelector orientiert. */

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/* Radio Buttons für die verschiedenen Droop-methoden */
	private JRadioButton rb_DrQ;
	private JRadioButton rb_DQ1;
	private JRadioButton rb_DQ2;
	private JRadioButton rb_DQ3;
	private JRadioButton rb_DQ4;

	private JRadioButton rb_grR;
	private JRadioButton rb_WTA;
	private JRadioButton rb_gR1;
	// private JRadioButton rb_gR3;

	private JButton b_Ok = new JButton(Resource.getString("bazi.gui.ok"));
	private JButton b_Cancel = new JButton(Resource.getString("bazi.gui.cancel"));

	/** ButtonGroup für die RadioButtons */
	private ButtonGroup bg = new ButtonGroup();
	private ButtonGroup bg_2 = new ButtonGroup();

	private RoundFrame rf;

	public DroopSelector(RoundFrame rf)
	{
		super(rf, Resource.getString("bazi.gui.biprop.droop.alt"), true);

		this.rf = rf;
		this.rb_DrQ = new JRadioButton("DrQ");
		this.rb_DrQ.setForeground(Color.BLUE);
		this.bg.add(this.rb_DrQ);

		this.rb_DQ1 = new JRadioButton("DQ1");
		this.rb_DQ1.setForeground(Color.RED);
		this.bg.add(this.rb_DQ1);

		this.rb_DQ2 = new JRadioButton("DQ2");
		this.rb_DQ2.setForeground(Color.RED);
		this.bg.add(this.rb_DQ2);

		this.rb_DQ3 = new JRadioButton("DQ3");
		this.rb_DQ3.setForeground(Color.RED);
		this.bg.add(this.rb_DQ3);
		
		this.rb_DQ4 = new JRadioButton("DQ4");
		this.rb_DQ4.setForeground(Color.RED);
		this.bg.add(this.rb_DQ4);

		/* this.rb_DQ4 = new JRadioButton("DQ4");
		 * this.rb_DQ4.setForeground(Color.BLACK);
		 * this.rb_DQ4.setEnabled(false);
		 * this.bg.add(this.rb_DQ4); */

		this.rb_grR = new JRadioButton("grR");
		this.rb_grR.setForeground(Color.BLUE);
		this.bg_2.add(this.rb_grR);

		this.rb_WTA = new JRadioButton("WTA");
		this.rb_WTA.setForeground(Color.RED);
		this.bg_2.add(this.rb_WTA);

		this.rb_gR1 = new JRadioButton("gR1");
		this.rb_gR1.setForeground(Color.RED);
		this.bg_2.add(this.rb_gR1);

		/* this.rb_gR3 = new JRadioButton("gR3");
		 * this.rb_gR3.setForeground(Color.BLACK);
		 * this.rb_gR3.setEnabled(false);
		 * this.bg_2.add(this.rb_gR3); */

		this.b_Cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DroopSelector.this.dispose();
			}
		});

		this.b_Ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int method;
				if (rb_DrQ.isSelected())
				{
					method = 0;
				}
				else if (rb_DQ1.isSelected())
				{
					method = 1;
				}
				else if (rb_DQ2.isSelected())
				{
					method = 2;
				}
				else if (rb_DQ3.isSelected())
				{
					method = 3;
				}
				else 
				{
					method = 4;
				}
				DroopSelector.this.rf.setDroopMethode(method);

				if (rb_grR.isSelected())
				{
					method = 0;
				}
				else if (rb_gR1.isSelected())
				{
					method = 1;
				}
				else
				{
					method = 2;
				}
				DroopSelector.this.rf.setDroopResidualMethod(method);

				DroopSelector.this.dispose();
			}
		});

		JPanel master = new JPanel(new GridBagLayout());
		master.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;

		JLabel l_def = new JLabel(Resource.getString("bazi.gui.default") + ":");
		l_def.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		l_def.setForeground(Color.BLUE);
		master.add(l_def, c);

		c.gridx++;
		master.add(this.rb_DrQ, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		JLabel b_1 = new JLabel();
		ImageIcon ic_1 = new ImageIcon(getClass().getResource(
				"images/DrQ.jpg"));
		b_1.setIcon(ic_1);
		master.add(b_1, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JLabel l_descr_DrQ = new JLabel("H.R. Droop (1881, page 172)");
		l_descr_DrQ.setForeground(Color.BLUE);
		master.add(l_descr_DrQ, c);

		c.gridy--;
		c.gridx++;
		c.fill = GridBagConstraints.VERTICAL;
		c.gridheight = 14;
		c.insets = new Insets(0, 5, 0, 5);
		master.add(new JSeparator(SwingConstants.VERTICAL), c);

		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		c.gridheight = 1;
		c.insets = new Insets(0, 0, 0, 5);
		master.add(this.rb_grR, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		JPanel p_desct_grR = new JPanel(new BorderLayout());
		JLabel l_descr_grR = new JLabel("<html>One residual seat to greatest remainder,<br>" +
				"one seat to second-greatest remainder, etc.</html>");
		l_descr_grR.setForeground(Color.BLUE);
		p_desct_grR.add(l_descr_grR, BorderLayout.PAGE_START);
		p_desct_grR.setBackground(Color.WHITE);
		p_desct_grR.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		master.add(p_desct_grR, c);

		c.gridx = 0;
		c.gridy += 2;
		c.gridwidth = 7;
		master.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.fill = GridBagConstraints.NONE;
		c.gridy++;
		c.gridwidth = 1;
		JLabel l_def2 = new JLabel(Resource.getString("bazi.gui.variant") + ":");
		l_def2.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		l_def2.setForeground(Color.RED);
		master.add(l_def2, c);

		c.gridx++;
		master.add(this.rb_DQ1, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		JLabel b_2 = new JLabel();
		ImageIcon ic_2 = new ImageIcon(getClass().getResource(
				"images/DQ1.jpg"));
		b_2.setIcon(ic_2);
		master.add(b_2, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JPanel desc_pane = new JPanel(new BorderLayout());
		JLabel l_descr_DQ1 = new JLabel("Solothurn, KR 1896 - 1977");
		l_descr_DQ1.setForeground(Color.RED);
		desc_pane.add(l_descr_DQ1, BorderLayout.PAGE_START);
		master.add(desc_pane, c);

		c.gridy--;
		c.gridx += 2;
		c.insets = new Insets(0, 0, 0, 5);
		master.add(this.rb_gR1, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		JPanel p_desc_gR1 = new JPanel(new BorderLayout());
		JLabel l_descr_gR1 = new JLabel("<html>Same as grR, but only among those lists that<br>" +
				"received at least one seat in main apportionment.</html>");
		l_descr_gR1.setForeground(Color.RED);
		p_desc_gR1.add(l_descr_gR1, BorderLayout.PAGE_START);
		p_desc_gR1.setBackground(Color.WHITE);
		p_desc_gR1.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		master.add(p_desc_gR1, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JLabel l_bsp_gR1 = new JLabel("Solothurn 1921 - 1993");
		l_bsp_gR1.setForeground(Color.RED);
		master.add(l_bsp_gR1, c);

		c.gridy += 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		JLabel l_def3 = new JLabel("");
		master.add(l_def3, c);

		c.gridx++;
		master.add(this.rb_DQ2, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		JLabel b_3 = new JLabel();
		ImageIcon ic_3 = new ImageIcon(getClass().getResource(
				"images/DQ2.jpg"));
		b_3.setIcon(ic_3);
		master.add(b_3, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JLabel l_descr_DQ2 = new JLabel("Solothurn, KR 1981 - 1993");
		l_descr_DQ2.setForeground(Color.RED);
		master.add(l_descr_DQ2, c);

		c.gridy--;
		c.gridx += 2;
		c.insets = new Insets(0, 0, 0, 5);
		master.add(this.rb_WTA, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		JPanel p_descr_WTA = new JPanel(new BorderLayout());
		JLabel l_descr_WTA = new JLabel("All residual seats to strongest list.");
		l_descr_WTA.setForeground(Color.RED);
		p_descr_WTA.add(l_descr_WTA, BorderLayout.PAGE_START);
		p_descr_WTA.setBackground(Color.WHITE);
		p_descr_WTA.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		master.add(p_descr_WTA, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JLabel l_bsp_WTA = new JLabel("Solothurn 1896 - 1917");
		l_bsp_WTA.setForeground(Color.RED);
		master.add(l_bsp_WTA, c);

		c.gridy += 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		JLabel l_def4 = new JLabel("");
		master.add(l_def4, c);

		c.gridx++;
		master.add(this.rb_DQ3, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		JLabel b_4 = new JLabel();
		ImageIcon ic_4 = new ImageIcon(getClass().getResource(
				"images/DQ3.jpg"));
		b_4.setIcon(ic_4);
		master.add(b_4, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JLabel l_descr_DQ3 = new JLabel("Slovak Republic, EP 2009 - 2019");
		l_descr_DQ3.setForeground(Color.RED);
		master.add(l_descr_DQ3, c);

		
		c.gridy += 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		JLabel l_def6 = new JLabel("");
		master.add(l_def6, c);

		c.gridx++;
		master.add(this.rb_DQ4, c);

		c.gridx++;
		c.insets = new Insets(5, 0, 5, 5);
		JLabel b_6 = new JLabel();
		ImageIcon ic_6 = new ImageIcon(getClass().getResource(
				"images/DQ4.jpg"));
		b_6.setIcon(ic_6);
		master.add(b_6, c);
		
		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JLabel l_descr_DQ4 = new JLabel("");
		l_descr_DQ4.setForeground(Color.RED);
		master.add(l_descr_DQ4, c);
		
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 7;
		c.fill = GridBagConstraints.BOTH;
		master.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		JLabel l_def5 = new JLabel(Resource.getString("bazi.gui.notimplemented") + ":");
		l_def5.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		l_def5.setForeground(Color.BLACK);
		master.add(l_def5, c);

		c.gridx++;
		c.fill = GridBagConstraints.BOTH;
		JLabel l_dq4 = new JLabel("DQ5");
		l_dq4.setHorizontalAlignment(JLabel.CENTER);
		master.add(l_dq4, c);

		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(5, 0, 5, 5);
		JLabel b_5 = new JLabel();
		ImageIcon ic_5 = new ImageIcon(getClass().getResource(
				"images/DQ5.jpg"));
		b_5.setIcon(ic_5);
		master.add(b_5, c);

		c.gridy++;
		c.insets = new Insets(0, 0, 15, 0);
		JPanel p_desc_DQ4 = new JPanel(new GridLayout(1, 1));
		JLabel l_descr_DQ4_1 = new JLabel("Greece, EP 2009");
		l_descr_DQ4_1.setForeground(Color.BLACK);
		// JLabel l_descr_DQ4_2 = new JLabel("part of the main-apportionment");
		// l_descr_DQ4_2.setForeground(Color.BLACK);
		p_desc_DQ4.add(l_descr_DQ4_1);
		// p_desc_DQ4.add(l_descr_DQ4_2);
		master.add(p_desc_DQ4, c);

		/* JLabel l_descr_DQ4 = new JLabel("Slovak Republic, EP 2009");
		 * l_descr_DQ4.setForeground(Color.BLACK);
		 * master.add(l_descr_DQ4, c); */

		c.gridy--;
		c.gridx += 2;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 0, 5);
		JLabel l_gr3 = new JLabel("gR3");
		l_gr3.setHorizontalAlignment(JLabel.CENTER);
		master.add(l_gr3, c);

		c.gridx++;
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel p_desc_gR3 = new JPanel(new GridLayout(1, 1));
		JLabel l_descr_gR3 = new JLabel("Greece, EP 2009");
		l_descr_gR3.setForeground(Color.BLACK);
		p_desc_gR3.add(l_descr_gR3);
		/* JLabel l_descr_gR3_2 = new JLabel("the remainders are considered, whose parties have not gained");
		 * l_descr_gR3_2.setForeground(Color.BLACK);
		 * p_desc_gR3.add(l_descr_gR3_2);
		 * JLabel l_descr_gR3_3 = new JLabel("any seats in the second part of the main-apportionment.");
		 * l_descr_gR3_3.setForeground(Color.BLACK);
		 * p_desc_gR3.add(l_descr_gR3_3);
		 * p_desc_gR3.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createLineBorder(Color.BLACK, 1),
		 * BorderFactory.createEmptyBorder(5, 5, 5, 5))); */
		master.add(p_desc_gR3, c);

		JPanel sub = new JPanel();
		sub.add(b_Ok);
		sub.add(b_Cancel);

		c.gridy += 2;
		c.gridx = 0;
		c.gridwidth = 7;
		c.anchor = GridBagConstraints.CENTER;
		master.add(sub, c);

		switch (rf.getDroopMethod())
		{
		case 0:
			this.rb_DrQ.setSelected(true);
			break;
		case 1:
			this.rb_DQ1.setSelected(true);
			break;
		case 2:
			this.rb_DQ2.setSelected(true);
			break;
		case 3:
			this.rb_DQ3.setSelected(true);
			break;
		case 4:
			this.rb_DQ4.setSelected(true);
			break;
		}

		switch (rf.getDroopResidualMethod())
		{
		case 0:
			this.rb_grR.setSelected(true);
			break;
		case 1:
			this.rb_gR1.setSelected(true);
			break;
		case 2:
			this.rb_WTA.setSelected(true);
			break;
		}

		this.setContentPane(master);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(rf);
		this.setVisible(true);
	}
}
