/*
 * @(#)SubapportionmentSelector.java 1.0 09/10/05
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import de.uni.augsburg.bazi.Resource;

/** <b>Überschrift:</b> Subapportion Dialog<br>
 * <b>Beschreibung:</b> Dialog zum auswählen verschiedener Methoden für die Unterzuteilung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2009<br>
 * <b>Organisation:</b> Universität Augsburg
 * 
 * @version 1.0
 * @author Christian Brand */
public class SubapportionmentSelector extends JDialog implements ActionListener
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT = "Def.";

	public static final String[] methods = {
			DEFAULT,
			Resource.getString("bazi.gui.method.haqgrr"),
			Resource.getString("bazi.gui.method.divabr"),
			Resource.getString("bazi.gui.method.divstd"),
			Resource.getString("bazi.gui.method.divgeo"),
			Resource.getString("bazi.gui.method.divhar"),
			Resource.getString("bazi.gui.method.divauf")
	};

	public static final String[] help = {
			"",
			Resource.getString("bazi.gui.method.haqgrr.help"),
			Resource.getString("bazi.gui.method.divabr.help"),
			Resource.getString("bazi.gui.method.divstd.help"),
			Resource.getString("bazi.gui.method.divgeo.help"),
			Resource.getString("bazi.gui.method.divhar.help"),
			Resource.getString("bazi.gui.method.divauf.help")
	};

	/** Radio Buttons für die verschiedenen Droop-methoden */
	private JRadioButton[] rb = new JRadioButton[methods.length];

	/** ButtonGroup für die RadioButtons */
	private ButtonGroup bg = new ButtonGroup();

	private JButton b_Ok, b_Cancel;

	private RoundFrame rf;

	public SubapportionmentSelector(RoundFrame rf)
	{
		super(rf, Resource.getString("bazi.gui.biprop.sub.alt"), true);
		this.rf = rf;


		// ///////////////////////////////////////
		// init Buttons und Lister
		// ///////////////////////////////////////
		String sub = rf.getSubapportionment();
		for (int i = 0; i < methods.length; i++)
		{

			String name = i == 0 ? "Same as superapportionment method" : methods[i];
			rb[i] = new JRadioButton(name);
			rb[i].setForeground(i == 0 ? Color.BLUE : Color.RED);
			rb[i].addActionListener(this);
			bg.add(rb[i]);

			if (methods[i].equals(sub))
				rb[i].setSelected(true);
		}

		b_Ok = new JButton(Resource.getString("bazi.gui.ok"));
		b_Ok.addActionListener(this);
		b_Cancel = new JButton(Resource.getString("bazi.gui.cancel"));
		b_Cancel.addActionListener(this);

		// ///////////////////////////////////////
		// init Grafik
		// ///////////////////////////////////////
		add(getContent());

		JPanel buttons = new JPanel();
		buttons.add(b_Ok);
		buttons.add(b_Cancel);
		add(buttons, BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(rf);
		setVisible(true);
	}

	private JPanel getContent()
	{
		JPanel master = new JPanel(new GridBagLayout());
		master.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;

		// Zeile 1: Überschrift
		c.gridwidth = 3;
		c.insets = new Insets(0, 0, 20, 0);
		master.add(new JLabel("Select subapportionment method:"), c);
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridy++;

		// Zeile 2: Default
		JLabel l_def = new JLabel(Resource.getString("bazi.gui.default") + ":");
		l_def.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		l_def.setForeground(Color.BLUE);
		master.add(l_def, c);
		c.gridx++;
		c.gridwidth = 2;
		master.add(rb[0], c);

		// Zeile 3: Trenner
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		master.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		// Zeile 4-?: Restliche Methoden
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridx = 1;

		for (int i = 1; i < methods.length; i++)
		{
			c.gridy++;
			master.add(rb[i], c);
			c.gridx++;
			JLabel l = new JLabel(help[i]);
			l.setForeground(Color.RED);
			master.add(l, c);
			c.gridx--;
		}

		return master;
	}


	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(Resource.getString("bazi.gui.ok")))
		{
			for (JRadioButton temp : rb)
				if (temp.isSelected() && temp == rb[0])
					rf.setSubapportionment(DEFAULT);
				else if (temp.isSelected())
					rf.setSubapportionment(temp.getText());
			dispose();
		}
		else if (e.getActionCommand().equals(Resource.getString("bazi.gui.cancel")))
		{
			dispose();
		}
	}
}
