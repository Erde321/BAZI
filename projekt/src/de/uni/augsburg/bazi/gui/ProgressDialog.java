/*
 * @(#)ProgressDialog.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.lib.Debug;
import de.uni.augsburg.bazi.lib.IterationListener;
import de.uni.augsburg.bazi.lib.PermutationsCommunicator;

/** <b>Title:</b> Klasse ProgressDialog<br>
 * <b>Description:</b> Ein Dialog, der den Fortschritt bei Biprop Berechnungen anzeigt. Dient auch als Kommunikator für die Berechnung aller Permutationen bei Ties.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * @version 2.1
 * @author Florian Kluge, Robert Bertossi, Christian Brand */

public class ProgressDialog extends JDialog
		implements IterationListener, PermutationsCommunicator
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Array, das die Label zur Anzeige des Titels der verwendeten Methode enthält */
	private JLabel[] lTitle;

	/** Array, das die Label der momentanen Iteration enthält */
	private JLabel[] lIteration;

	/** Array, das die Label der momentanen Anzahl der berechnenten Permutationen anzeigt */
	private JLabel[] lPermutation;

	/** Panel, auf dem die Labels angezeigt werden */
	private JPanel pContent;

	/** Aufrufender Frame */
	private RoundFrame rf;

	// private boolean finished = false;

	/** Index der aktuellen Methode */
	private int method = 0;

	/** Erzeugt einen neuen ProgressDialog
	 * 
	 * @param owner Aufrufeneder Frame */
	public ProgressDialog(RoundFrame owner)
	{
		super(owner, "Biprop/NZZ", false);
		rf = owner;
	}

	/** Initialisiert die Label mit den übergebenen Methodennamen
	 * 
	 * @param methods Alle verwendeten Methodennamen */
	public void initialize(String[] methods)
	{
		int len = methods.length;
		pContent = new JPanel(new GridLayout(len, 3));
		lTitle = new JLabel[len];
		lIteration = new JLabel[len];
		lPermutation = new JLabel[len];
		// this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		for (int i = 0; i < len; i++)
		{
			lTitle[i] = new JLabel(methods[i]);
			pContent.add(lTitle[i]);
			lIteration[i] = new JLabel(" 0");
			pContent.add(lIteration[i]);
			lPermutation[i] = new JLabel("");
			pContent.add(lPermutation[i]);
		}
		setContentPane(pContent);
		pack();
		setLocationRelativeTo(rf);
		// wird nur noch bei Debug angezeigt
		if (Debug.BIPROP)
			setVisible(true);
	}

	/** iterationChanged
	 * (Interface IterationListener)
	 * 
	 * @param cnt Momentane Anzahl der Iterationen
	 * @param finished Ist die Berechnung beendet? */
	public synchronized void iterationChanged(int cnt, boolean finished)
	{
		lIteration[method].setText(" " + cnt + "");

		/* if (!this.isVisible()) {
		 * //pack();
		 * setVisible(true);
		 * } */
		if (finished)
		{
			method++;
		}
	}

	/** iterationFinished
	 * (Interface IterationListener) */
	public synchronized void iterationFinished()
	{
		setVisible(false);
		// dispose();
		// entfernt, da das Programm hier immer hängenblieb
	}

	/** Zeigt einen Auswahldialog an, um zu bestimmen, ob alle Permutationen berechnet werden sollen
	 * (Interface PermutationsCommunicator)
	 * 
	 * @return <b>true</b> wenn 'Ja' ausgewählt wurde */
	public boolean calcAllPermutations()
	{

		int val = JOptionPane.showConfirmDialog(rf,
				Resource.getString("bazi.biprop.permutations.dialog"),
				Resource.getString(
						"bazi.error.attention"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return val != JOptionPane.YES_OPTION;
	}

	/** permutationChanged
	 * (Interface PermutationsCommunicator)
	 * 
	 * @param cnt Momentane Anzahl der Permutationen */
	public void permutationChanged(int cnt)
	{
		// method wurde schon erhöht!
		try
		{
			lPermutation[method - 1].setText(" " + cnt);
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		if (!this.isVisible())
		{
			pack();
			setVisible(true);
		}

	}

}
