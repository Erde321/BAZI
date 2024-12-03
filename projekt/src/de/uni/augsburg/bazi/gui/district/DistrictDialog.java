/*
 * @(#)DistrictDialog.java 2.1 18/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui.district;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.gui.GUIConstraints;
import de.uni.augsburg.bazi.gui.MultiplicityEvent;
import de.uni.augsburg.bazi.gui.MultiplicityListener;
import de.uni.augsburg.bazi.gui.RoundFrame;

/** <b>Title:</b> Klasse DistrictDialog<br>
 * <b>Description:</b> Eingabedialog für "Wahl in getrennten Bezirken"<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher
 * @version 2.1 */
public class DistrictDialog extends JDialog implements MultiplicityListener
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** RoundFrame-Referenz */
	private RoundFrame rf;

	/** Hauptpanel */
	private JPanel pContent;

	/** Eingabe-Tabelle */
	private DistrictTable dtInput;

	/** Panel mit den Buttons */
	private JPanel pButt;

	/** Buttons */
	private JButton bDel;

	/** Buttons */
	private JButton bOK;

	/** Buttons */
	private JButton bEsc;

	/** Listener */
	private BListener al = new BListener();

	/** Rückgabe-kontrolle */
	private boolean bRet = true;

	/** Schlange für die Rückgabe */
	private DistrictQueue dq = new DistrictQueue();

	/** Checkbox um die Biproportionale Zuteilung auch bei einem Distrikt zu aktivieren */
	private JCheckBox cbForceBiprop;

	/** Konstruktor
	 * @param owner Aufrufender RoundFrame */
	public DistrictDialog(RoundFrame owner)
	{
		super(owner, Resource.getString("bazi.gui.dd.title"));
		// self = this;
		rf = owner;

		// dCount = 1;

		pContent = new JPanel();
		pContent.setLayout(new BorderLayout());

		dtInput = new DistrictTable("Nr.", "Name", "", "Mandate", dq,
				new SListener());
		dtInput.addMultiplicityListener(this);
		pContent.add(dtInput, BorderLayout.NORTH);

		cbForceBiprop = new JCheckBox(Resource.getString("bazi.gui.dd.forcebiprop"));
		cbForceBiprop.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		cbForceBiprop.setToolTipText(Resource.getString("bazi.gui.dd.forcebiprop.tooltip"));
		pContent.add(cbForceBiprop, BorderLayout.CENTER);

		pButt = new JPanel(new FlowLayout());
		bDel = new JButton(Resource.getString("bazi.gui.del"));
		bDel.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bDel.addActionListener(al);
		bDel.setEnabled(false);
		pButt.add(bDel);
		bOK = new JButton(Resource.getString("bazi.gui.ok"));
		bOK.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bOK.addActionListener(al);
		pButt.add(bOK);
		bEsc = new JButton(Resource.getString("bazi.gui.cancel"));
		bEsc.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		bEsc.addActionListener(al);
		pButt.add(bEsc);
		pContent.add(pButt, BorderLayout.SOUTH);

		setContentPane(pContent);

		// Zum schließen des Fensters
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				bRet = false;
				setVisible(false);
				dispose();
			}
		});

		setModal(true);

		pack();
		// setResizable(false);

		this.setLocationRelativeTo(rf);
	}

	/** Konstruktor
	 * @param owner Aufrufender RoundFrame
	 * @param vd Vector mit den District-Daten */
	public DistrictDialog(RoundFrame owner, Vector<District> vd, boolean forced)
	{
		this(owner);

		dtInput.setRowTotal(vd.size());

		for (int i = 0; i < vd.size(); i++)
		{
			District tmp = (District) vd.elementAt(i);
			dtInput.setValueAt((i + 1) + "", i, 0);
			dtInput.setValueAt(tmp.name, i, 1);
			dtInput.setValueAt(tmp.mandate, i, 2);
		}

		dtInput.setCount(vd.size());

		if (forced)
			cbForceBiprop.setSelected(true);
	}

	/** Start des Dialogs
	 * @return <b>true</b>, falls OK gedrückt wurde */
	public boolean start()
	{
		setVisible(true);
		// System.out.println("Rückgabe: " + bRet);
		return bRet;
	}

	public int getSelectedDistrict()
	{
		int[] rows = dtInput.getSelectedRows();
		if (rows == null || rows.length == 0)
			return -1;
		return rows[0];
	}

	/** Auslesen der Daten
	 * 
	 * @return DistrictQueue */
	public DistrictQueue getData()
	{
		return dq;
	}

	/** Liefert die Summe aller Mandate über die Distrikte.
	 * 
	 * @return Summe aller Mandate */
	public int getSum()
	{
		return dtInput.getTotal();
	}

	/** Überprüft, ob die Algorithmen für mehrere Distrikte auch für den einen
	 * Distrikt angewendet werden soll.
	 * 
	 * @return <b>true</b> wenn Biprop auf jeden Fall angewendet werden soll */
	public boolean forceBiprop()
	{
		return cbForceBiprop.isSelected();
	}

	/** Zur Überwachung der Anzahl der Distrikte in der Distrikttabelle.
	 * (aus Interface MultiplicityListener)
	 * 
	 * @param me MultiplicityEvent */
	public void multiplicityChanged(MultiplicityEvent me)
	{
		if (me.getMultiplicity() == 1)
		{
			cbForceBiprop.setEnabled(true);
			cbForceBiprop.setSelected(true);
		}
		else if (me.getMultiplicity() > 1)
		{
			cbForceBiprop.setEnabled(false);
			cbForceBiprop.setSelected(false);
		}
	}

	/** <b>Title:</b> Klasse AListener<br>
	 * <b>Description:</b> Überwachung der Buttons<br>
	 * <b>Copyright:</b> Copyright (c) 2002<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Florian Kluge
	 * @version 2.0 */
	private class BListener
			implements ActionListener
	{

		/** Aufruf, wenn ein Button angeklickt wurde. */
		public void actionPerformed(ActionEvent event)
		{
			if (event.getSource() == bDel)
			{
				dtInput.deleteSelectRows();
			}
			else if (event.getSource() == bOK)
			{
				bRet = true;
				// noch schnell die Selection ändern
				// damit die letzte Änderunge auch wirklich übernommen wird
				/* dtInput.setRowSelectionInterval(0,0);
				 * JOptionPane.showMessageDialog(self, "hallo");
				 * dtInput.setRowSelectrionInterval(1,1); */

				// dtInput.getTable().editingStopped(new ChangeEvent(dtInput.getTable()));

				// funktioniert so nicht!
				// es muß noch irgendwie das editingStopped ausgeführt/ausgelöst(?) werden

				setVisible(false);
				dispose();
			}
			else if (event.getSource() == bEsc)
			{
				bRet = false;
				setVisible(false);
				dispose();
			}
		}
	}

	/** <b>Title:</b> Klasse SListener<br>
	 * <b>Description:</b> Überwacht die Tabelle auf Selektionen<br>
	 * <b>Copyright:</b> Copyright (c) 2002<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Florian Kluge
	 * @version 2.0 */
	private class SListener
			implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent d)
		{
			if (!d.getValueIsAdjusting())
			{
				int[] a = dtInput.getSelectedRows();
				// falls keine Zeile markiert ist, schalte den DEL-Button aus!
				if (a.length == 0)
				{
					bDel.setEnabled(false);
					return;
				}
				if (a[0] < dtInput.getRowTotal())
				{
					// nur wenn die Markierung im zulässigen Bereich liegt,
					// darf auch gelöscht werden
					bDel.setEnabled(true);
					return;
				}
				bDel.setEnabled(false);
			}
		}
	}

}
