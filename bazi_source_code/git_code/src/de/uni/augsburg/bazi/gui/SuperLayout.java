/*
 * @(#)SuperLayout.java 2.1 07/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.*;

/** <b>Title:</b> Klasse SuperLayout<br>
 * <b>Description:</b> Umfasst mit einer Funktion alle Einstellungen des GridBagLayout<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Jan Petzold, Christian Brand */

public class SuperLayout
{

	/** Container, auf den das Layout angewendet wird. */
	private Container parent;

	/** Das eigentliche Layout. */
	private GridBagLayout gbl = new GridBagLayout();

	/** Die Layout-Bedingungen. */
	private GridBagConstraints gbc = new GridBagConstraints();

	/** Erzeugt eine Instanz von SuperLayout und setzt ein neues GridBagLayout für
	 * parent.
	 * 
	 * @param parent Ein Container, der mit einem GridBagLayout ausgestattet
	 *          werden soll */
	public SuperLayout(Container parent)
	{
		this.parent = parent;
		parent.setLayout(gbl);
	}

	/** Fügt eine Komponente in den Container mit dem GridBagLayout ein.
	 * 
	 * @param comp Einzufügende Komponente.
	 * @param gridx Gibt die Spalte an, in der der linke Teil der Komponente angezeigt wird.
	 * @param gridy Gibt die Zeile an, in der der obere Teil der Komponente angezeigt wird.
	 * @param gridwidth Gibt die Anzahl der Zellen an, in denen die Komponente in einer Zeile angezeigt wird.
	 * @param gridheight Gibt die Anzahl der Zellen an, in denen die Komponente in einer Spalte angezeigt wird.
	 * @param weightx Gibt an, wie zusätzlicher horizontaler Raum aufgeteilt wird.
	 * @param weighty Gibt an, wie zusätzlicher vertikaler Raum aufgeteilt wird.
	 * @param fill Wenn der für die Komponente benötigte Platz kleiner als der zur Verfügung stehende Platz ist,
	 *          gibt dieser Parameter an, wie die Komponente den Platz ausfüllen soll.
	 * @param anchor Wenn der für die Komponente benötigte Platz kleiner als der zur Verfügung stehende Platz ist,
	 *          gibt dieser Parameter an, wo die Komponente platziert werden soll.
	 * @param top Gibt den Raum um die Komponente an, der frei bleibt.
	 * @param left Gibt den Raum um die Komponente an, der frei bleibt.
	 * @param bottom Gibt den Raum um die Komponente an, der frei bleibt.
	 * @param right Gibt den Raum um die Komponente an, der frei bleibt.
	 * 
	 * @see java.awt.GridBagConstraints */
	public void add(Component comp, int gridx, int gridy, int gridwidth,
			int gridheight,
			int weightx, int weighty, int fill, int anchor,
			int top, int left, int bottom, int right)
	{
		// Layoutbedingungen setzen
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.fill = fill;
		gbc.anchor = anchor;
		gbc.insets = new Insets(top, left, bottom, right);

		gbl.setConstraints(comp, gbc);
		parent.add(comp);
	}

	/** Fügt eine Komponente in den Container mit dem GridBagLayout ein.
	 * 
	 * @see <A HREF="../../bazi/gui/SuperLayout.html#add(java.awt.Component, int,
	 *      int, int, int, int, int, int, int, int, int, int, int)">add</A>
	 * @param comp Einzufügende Komponente.
	 * @param gridx Gibt die Spalte an, in der der linke Teil der Komponente angezeigt wird.
	 * @param gridy Gibt die Zeile an, in der der obere Teil der Komponente angezeigt wird.
	 * @param gridwidth Gibt die Anzahl der Zellen an, in denen die Komponente in einer Zeile angezeigt wird.
	 * @param gridheight Gibt die Anzahl der Zellen an, in denen die Komponente in einer Spalte angezeigt wird.
	 * @param weightx Gibt an, wie zusätzlicher horizontaler Raum aufgeteilt wird.
	 * @param weighty Gibt an, wie zusätzlicher vertikaler Raum aufgeteilt wird.
	 * @param fill Wenn der für die Komponente benötigte Platz kleiner als der zur Verfügung stehende Platz ist,
	 *          gibt dieser Parameter an, wie die Komponente den Platz ausfüllen soll.
	 * @param anchor Wenn der für die Komponente benötigte Platz kleiner als der zur Verfügung stehende Platz ist,
	 *          gibt dieser Parameter an, wo die Komponente platziert werden soll. */
	public void add(Component comp, int gridx, int gridy, int gridwidth,
			int gridheight,
			int weightx, int weighty, int fill, int anchor)
	{

		add(comp, gridx, gridy, gridwidth, gridheight, weightx, weighty, fill,
				anchor, 5, 5, 5, 5);
	}

}
