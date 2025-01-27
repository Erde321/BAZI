/*
 * @(#)TablePane.java 2.1 07/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.gui.district.District;
import de.uni.augsburg.bazi.gui.district.DistrictQueue;
import de.uni.augsburg.bazi.gui.district.QElem;

/** <b>Title:</b> Klasse TablePane<br>
 * <b>Description:</b> Enthält die WeightsTables für die Stimmeneingabe und überwacht diese.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Florian Kluge, Robert Bertossi, Christian Brand, Marco Schumacher */

public class TablePane extends JPanel
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/* Kurze Notizen:
	 * -Änderungen am Zustand einer Tabelle werden mit dem TableModelListener überwacht */

	/** Selbstreferenz */
	private final TablePane self;

	/** Referenz auf RoundFrame */
	private final RoundFrame rf;

	/** ListSelectionListener */
	private final ListSelectionListener lsl;

	/** Referenz auf das Accuracy-Feld */
	private final JTextField tAccuracy;

	/** Vector mit den vorhandenen Tabellen */
	private final Vector<WeightsTable> vTables;

	/** Vector mit den zugehörigen Accuracies */
	private final Vector<String> vAcc;

	/** Vector mit den District-Namen */
	private final Vector<String> vDistrict;

	/** Index des zuletzt benutzten Panes */
	private int lastIndex;

	/** Das TabbedPane */
	private JTabbedPane jtp;

	/** Titel der Namenspalte */
	private String sName;

	/** Titel der Stimmenspalte */
	private String sVotes;

	/** Combobox der 3. Spalte */
	private final ConditionItem[] sItems;

	/** Combobox der 3. Spalte bei Biprop */
	private final ConditionItem[] sBipropItems;

	private final ConditionItem[] sBMMItems;

	/** Bezeichnung der Summe */
	private final String sSum;

	/** Kontrollvariable, wird nur im Konstruktor benötigt */
	private boolean bConst;

	/** Wurde nach dem Reset schon mal ein District eingefügt? */
	private boolean bDistrict = false;

	/** Sind mehrere Distrikte aktiv? */
	private boolean bMultiple = false;

	/** Counter */
	private int count = 1;

	/** Bezeichnungsstring */
	private final String desc;

	/** Changelistener für das JTP */
	private final JTPChangeListener jtpc = new JTPChangeListener();

	/** Mouselistener für das JTP */
	private final MouseListener jtpm;

	/** Vector mit den MultiplicityListenern */
	private final Vector<MultiplicityListener> vMultList = new Vector<MultiplicityListener>();

	/** Erstellt ein neues TablePane. Diese enthält eine leere Tabelle
	 * 
	 * @param name Titel der Namenspalte
	 * @param votes Titel der Stimmenspalte
	 * @param sum Bezeichnung der Summe
	 * @param items Combobox der 3. Spalte in der initialen Tabelle für den 1-dimensionalen Fall
	 * @param bipropItems Combobox der 3. Spalte für den 2-dimensionalen Fall
	 * @param rfref Referenz auf das RoundFrame
	 * @param ta Feld mit der Mandataszahl
	 * @param l ListSelectionListener
	 * @param ml Listener, der bei Doppelklick auf ein Tab aufgerufen wird */
	public TablePane(String name, String votes, String sum, ConditionItem[] items, ConditionItem[] bipropItems,
			ConditionItem[] BBMItems, RoundFrame rfref, JTextField ta, ListSelectionListener l,
			MouseListener ml)
	{
		super();

		bConst = true;
		self = this;
		sName = name;
		sVotes = votes;
		sSum = sum;
		sItems = items;
		sBipropItems = bipropItems;
		sBMMItems = BBMItems;
		rf = rfref;
		tAccuracy = ta;
		lsl = l;
		jtpm = ml;

		desc = Resource.getString("bazi.gui.district");

		// jtp = new JTabbedPane(JTabbedPane.TOP);
		// jtp.addChangeListener(jtpc);
		vTables = new Vector<WeightsTable>();
		vAcc = new Vector<String>();
		vDistrict = new Vector<String>();

		WeightsTable iwt = new WeightsTable(name, votes, sum, items, self, rf);
		String tableName = desc + " " + (count++);

		// iwt.addlsl(rf.sltp);
		iwt.addlsl(lsl);

		// jtp.addTab(tableName, iwt);
		vTables.add(iwt);
		vAcc.add(new String());
		vDistrict.add(tableName);

		// setLayout(new BorderLayout());
		// setLayout(new FlowLayout());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// add(jtp);
		// add(iwt, BorderLayout.CENTER);
		add(iwt);

		lastIndex = 0;

		// rf.setEnabledDelTable(false);
		// setMultiple(false);
		// setMultiple(true);
		bConst = false;
		// rf.tableSwitched();

	}

	/** Hinzufügen eines Distrikts
	 * @param name Name des Distrikts
	 * @return die eingefügte <code>WeightsTable</code> */
	public WeightsTable addDistrict(String name)
	{
		// System.out.println("adding District: " + name);
		WeightsTable wt;
		// addDistrict(name, "", null, null, null);
		// return wt;

		if (!bDistrict)
		{
			setMultiple(true);
			setTitleAt(0, name);
			bDistrict = true;
			wt = getFirstTable();
		}
		// ansonsten schon :-)
		else
		{
			wt = addTable(name);
		}
		int neu = jtp.getTabCount() - 1;
		// jtp.setToolTipTextAt(neu, name);

		vAcc.setElementAt(tAccuracy.getText(), neu);
		adaptRows();

		fireMultiplicityChanged(new MultiplicityEvent(this, 0, vTables.size()));

		return wt;
	}

	/** Zugriff auf eine einzelne Tabelle
	 * @param n Index der Tabelle
	 * @return Referenz auf diese WeightsTable */
	public WeightsTable getTable(int n)
	{
		return vTables.elementAt(n);
	}

	/** Zugriff auf die erste Tabelle
	 * @return Referenz auf die erste WeightsTable */
	public WeightsTable getFirstTable()
	{
		return vTables.firstElement();
	}
	
	/** Zugriff auf Vektor der Tabellen
	 * @return Referenz auf Vektor der WeightsTables */
	public Vector<WeightsTable> getTables()
	{
		return vTables;
	}

	/** Löschen der markierten Zeilen */
	public void deleteSelectedRows()
	{
		int akt = (bMultiple ? jtp.getSelectedIndex() : 0);
		WeightsTable aktwt = vTables.elementAt(akt);
		int[] rows = aktwt.getSelectedRows();
		aktwt.deleteRows(rows);
		// war für die Synchronisation der Tabellen
		/* for (int i=0; i<vTables.size(); i++) {
		 * WeightsTable tmp = (WeightsTable) vTables.elementAt(i);
		 * tmp.deleteRows(rows);
		 * } */
	}

	/** Änderungen aus der DistrictQueue übernehmen
	 * @param dq Schlange mit den Änderungsdaten */
	public void flushQueue(DistrictQueue dq)
	{
		while (dq.hasMoreElements())
		{
			// System.out.println("in flush   ac:" + tAccuracy.getText());
			QElem qtmp = dq.removeFirst();
			// System.out.println("QElem: " + qtmp);
			// District hinzufügen
			if (qtmp.action == QElem.INS)
			{
				// System.out.println("INS: " + qtmp);
				District dtmp = qtmp.d;
				addDistrictAt(dtmp.nummer - 1, dtmp);
			}
			// Distrikt updaten
			else if (qtmp.action == QElem.UPD)
			{
				// System.out.println("UPD: " + qtmp);
				District dtmp = qtmp.d;
				setTitleAt(dtmp.nummer - 1, dtmp.name);
				setAcc(dtmp.nummer - 1, dtmp.mandate);
			}
			// Distrikt löschen
			else if (qtmp.action == QElem.DEL)
			{
				// System.out.println("DEL: " + qtmp);
				deleteDistrictAt(qtmp.d.nummer - 1);
			}
			// System.out.println("in flush-e   ac:" + tAccuracy.getText());
		}
		if (vTables.size() == 1)
		{
			setMultiple(false);
		}
		else
		{
			jtp.setSelectedIndex(0);
		}
	}

	/** Hinzufügen eines MultiplicityListeners
	 * @param ml der neue Listener */
	public void addMultiplicityListener(MultiplicityListener ml)
	{
		vMultList.add(ml);
	}

	/** Entfernen eines MultiplicityListener
	 * @param ml der zu löschende Listener
	 * @return <code>true</code>, falls der Listener registriert war */
	public boolean removeMultiplicityListener(MultiplicityListener ml)
	{
		return vMultList.remove(ml);
	}

	/** Auslösen eines MultiplicityEvents
	 * @param me der Event */
	public void fireMultiplicityChanged(MultiplicityEvent me)
	{
		for (int i = 0; i < vMultList.size(); i++)
		{
			vMultList.elementAt(i).multiplicityChanged(me);
		}
	}

	/** Zurücksetzen auf Startzustand */
	public void reset()
	{
		if (vTables.size() > 1)
		{
			setSelectedIndex(0);
			// System.out.println("Reset");
			for (int i = vTables.size() - 1; i > 0; i--)
			{
				// System.out.println("löschen von Tab " + i);
				jtp.removeTabAt(i);
				vTables.removeElementAt(i);
				vAcc.removeElementAt(i);
				vDistrict.removeElementAt(i);
			}
			// rf.setEnabledDelTable(false);
			setMultiple(false);
		}
		count = 1;
		WeightsTable tmp = vTables.firstElement();
		tmp.reset();
		vAcc.setElementAt(new String(), 0);
		// jtp.setTitleAt(0, count+"");
		vDistrict.setElementAt(desc + " " + count, 0);
		updateUI();
		tmp.adaptRows();
		bDistrict = false;
		setMultiple(false);
	}

	/** Bezeichnung eines Tabs
	 * @param id Index des Tabs
	 * @return Titel des Tabs */
	public String getTabTitle(int id)
	{
		// return jtp.getTitleAt(id);
		return vDistrict.elementAt(id);
	}

	/** liefert die Anzahl der vorhandenen Tabellen
	 * @return Anzahl */
	public int length()
	{
		return vTables.size();
	}

	/** Condition setzen
	 * @param id Nummer der Condition */
	public void setMinDir(int id)
	{
		for (int i = 0; i < vTables.size(); i++)
		{
			WeightsTable tmp = vTables.elementAt(i);
			tmp.setMinDir(id);
		}
	}

	/** (De-)Aktivieren der Nebenbedingungs-Combobox
	 * @param state Status */
	public void setCondEnabeled(boolean state)
	{
		for (int i = 0; i < vTables.size(); i++)
		{
			vTables.elementAt(i).setCondEnabled(state);
		}
	}

	/** Setzen der Nebenbedingungs-Combobox für Biprop.
	 * @param state Status */
	public void setCondBiprop(boolean state)
	{
		ConditionItem[] its = state ? sBipropItems : sItems;
		for (int i = 0; i < vTables.size(); i++)
		{
			vTables.elementAt(i).setCondItems(its);
		}
	}

	/** Setzen der Nebenbedingungs-Combobox für BMM.
	 * @param state Status */
	public void setCondBMM(boolean state)
	{
		ConditionItem[] its = state ? sBMMItems : sItems;
		for (int i = 0; i < vTables.size(); i++)
		{
			vTables.elementAt(i).setCondItems(its);
		}
	}

	/** Setzen eines Spaltennamen
	 * @param value Neue Bezeichnung der Spalte
	 * @param col Nummer der Spalte */
	public void setColumnName(String value, int col)
	{
		// int akt = jtp.getSelectedIndex();
		// WeightsTable aktwt = (WeightsTable) vTables.elementAt(akt);
		// if (vTables.size()==1) return;
		for (int i = 0; i < vTables.size(); i++)
		{
			WeightsTable tmp = vTables.elementAt(i);
			tmp.setColumnName(value, col);
		}
		if (col == 0)
		{
			sName = value;
		}
		if (col == 1)
		{
			sVotes = value;
		}
	}

	/** Auslesen der Districtdaten
	 * @return Ein Vector mit Districten */
	public Vector<District> getDistrictData()
	{
		Vector<District> v = new Vector<District>();
		for (int i = 0; i < vTables.size(); i++)
		{
			String dacc = vAcc.elementAt(i);
			// Beim aktiven District muß die Accuracy aus dem RoundFrame genommen werden (konnte geändert werden)
			if (jtp != null)
			{
				dacc = (i == jtp.getSelectedIndex()) ? tAccuracy.getText() :
						(String) vAcc.elementAt(i);
			}
			else
			{
				dacc = tAccuracy.getText();
			}
			District tmp = new District(i, vDistrict.elementAt(i), dacc);
			v.add(tmp);
		}
		return v;
	}

	/** Hinzufügen einer neuen Tabelle
	 * Nach dem Hinzufügen wird die Tabelle automatisch aktiviert
	 * @param name Tab-Bezeichnung
	 * @return Die neu hinzugefügte Tabelle */
	private WeightsTable addTable(String name)
	{
		WeightsTable nwt = new WeightsTable(sName, sVotes, sSum, sItems, self, rf);
		// jtp.addTab((++count)+"", nwt);
		jtp.addTab(name, null, nwt, name);
		count++;
		vTables.add(nwt);
		vAcc.add(new String());
		vDistrict.add(name);
		// jetzt müssen noch die Bezeichnungen gesetzt werden:
		WeightsTable tmp = vTables.firstElement();
		// zuerst der Kopf
		nwt.setColumnName(tmp.getColumnName(0), 0);
		nwt.setColumnName(tmp.getColumnName(1), 1);
		nwt.setMinDir(tmp.getSelectedConditionIndex());
		// nun die Namen-Spalte
		// nwt.setColumnValues(tmp.getColumnValues(0), 0);
		// Nachricht an RoundFrame zum entsperren des Menübefehls "Löschen"
		// rf.setEnabledDelTable(true);
		// setMultiple(true);
		setSelectedIndex(jtp.getTabCount() - 1);
		return nwt;
	}

	/** Setzen der Einstellungen für Multiple
	 * @param b <b>true</b> entspricht Multiple */
	private void setMultiple(boolean b)
	{
		if (b == bMultiple)
		{
			return;
		}
		// rf.setEnabledDelTable(b);
		WeightsTable wtt = vTables.firstElement();

		// Einschalten von Multiple
		if (b)
		{
			// TabbedPane anzeigen
			this.remove(wtt);
			jtp = new JTabbedPane(SwingConstants.TOP);
			jtp.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
			jtp.addChangeListener(jtpc);
			jtp.addMouseListener(jtpm);
			// this.add(jtp, BorderLayout.CENTER);
			this.add(jtp);
			/* for (int i=0; i<vTables.size(); i++) {
			 * //jtp.addTab((String) vDistrict.elementAt(i), null, (WeightsTable)vTables.elementAt(i), (String) vDistrict.elementAt(i));
			 * jtp.addTab((String) vDistrict.elementAt(i), (WeightsTable)vTables.elementAt(i));
			 * //((WeightsTable)vTables.elementAt(i)).updateUI();
			 * } */
			jtp.addTab(vDistrict.firstElement(),
					vTables.firstElement());
		}
		else
		{
			this.remove(jtp);
			jtp.removeChangeListener(jtpc);
			jtp = null;
			// this.add(wtt, BorderLayout.CENTER);
			this.add(wtt);
			wtt.updateUI();
			wtt.adaptRows();
			// rf.componentResized();
		}
		bMultiple = b;
	}

	/** Multiple? */
	public boolean getMultiple()
	{
		return bMultiple;
	}

	/** liefert die Acc an bestimmter Stelle
	 * @param i Offset */
	public String getAccAt(int i)
	{
		if (bMultiple)
		{
			if (i == getSelectedIndex())
			{
				return tAccuracy.getText();
			}
			else
			{
				return vAcc.elementAt(i);
			}
		}
		else
		{
			return tAccuracy.getText();
		}
	}

	/** liefert die erste Acc */
	public String getFirstAcc()
	{
		return vAcc.firstElement();
	}

	/** Hinzufügen eines kompletten Districts
	 * für den Aufruf von FileIO aus.
	 * @param name Bezeichnung des Bezirks
	 * @param acc Mandatszahl des Bezirks
	 * @param vNames Vector mit den Parteien
	 * @param vWeights Vector mit den Gewichten
	 * @param vMin Vector mit den Zusatzbedingungen */
	public void addDistrict(String name, String acc, Vector<?> vNames,
			Vector<?> vWeights, Vector<?> vMin)
	{
		// Falls dies der erste District ist, der hinzugefügt wird,
		// muß keine Tabelle erstellt werden
		if (!bDistrict)
		{
			setMultiple(true);
			setTitleAt(0, name);
			bDistrict = true;
		}
		// ansonsten schon :-)
		else
		{
			addTable(name);
		}
		int neu = jtp.getTabCount() - 1;
		// jtp.setToolTipTextAt(neu, name);
		if (vNames != null)
		{
			setValues(vNames, neu, 0);
		}
		if (vWeights != null)
		{
			setValues(vWeights, neu, 1);
		}
		if (vMin != null)
		{
			setValues(vMin, neu, 2);
		}
		tAccuracy.setText(acc);
		vAcc.setElementAt(tAccuracy.getText(), neu);
		adaptRows();

		fireMultiplicityChanged(new MultiplicityEvent(this, 0, vTables.size()));
	}

	/** Einfügen eines Districts
	 * @param id Stelle, an der die neue Tabelle eingefügt wird, falls zu groß, wird am Ende angehängt
	 * @param Name Bezeichnung des Districts
	 * @param acc Mandatszahl für diesen District */
	private void addDistrictAt(int id, String Name, String acc)
	{
		// System.out.println("before setmult   ac:" + tAccuracy.getText());
		// Prüfen, ob schon mehrere Tabellen vorhanden sind
		if (!bMultiple)
		{
			// Wenn nicht, dann setzen des JTabbedPanes
			setMultiple(true);
		}

		int index = 0;
		if (id > vTables.size())
		{
			// System.out.println(" at EOT");
			index = vTables.size();
			// addDistrict(Name, acc);
		}
		else
		{
			index = id;
			// System.out.println(" at " + index);
		}
		WeightsTable nwt = new WeightsTable(sName, sVotes, sSum, sItems, self, rf);
		/* if (vTables.size()==1) {
		 * setMultiple(true);
		 * } */
		// System.out.println("Size: " + jtp.getTabCount());
		// System.out.println("before insert   ac:" + tAccuracy.getText());
		jtp.insertTab("" + (index + 1), null, nwt, Name, index);
		// System.out.println("tab inserted   ac:" + tAccuracy.getText());
		// jtp.addTab(""+(index+1), null, nwt, Name);
		count++;
		vTables.insertElementAt(nwt, index);
		vAcc.insertElementAt(new String(acc), index);
		vDistrict.insertElementAt(Name, index);
		// jetzt müssen noch die Bezeichnungen gesetzt werden:
		WeightsTable tmp = vTables.firstElement();
		// zuerst der Kopf
		nwt.setColumnName(tmp.getColumnName(0), 0);
		nwt.setColumnName(tmp.getColumnName(1), 1);
		nwt.setMinDir(tmp.getSelectedConditionIndex());
		jtp.setSelectedIndex(index);
		// System.out.println("selection   ac:" + tAccuracy.getText());
		// Jetzt noch die Köpfe der nachfolgenden Tabs anpassen
		for (int i = index + 1; i < jtp.getTabCount(); i++)
		{
			jtp.setTitleAt(i, (i + 1) + "");
		}
		fireMultiplicityChanged(new MultiplicityEvent(this, 0, vTables.size()));

	}

	/** Überladung von addDistrictAt, die mit einem gekapselten District arbeitet
	 * @param index Stelle, an der der neue District eingefügt wird
	 * @param d Die genauen District-Daten */
	private void addDistrictAt(int index, District d)
	{
		addDistrictAt(index, d.name, d.mandate + "");
	}

	/** Löschen eines Districts
	 * @param index Index des Panes */
	private void deleteDistrictAt(int index)
	{
		int ind = index;
		if (index >= vTables.size())
		{
			ind = vTables.size() - 1;
		}

		String store_acc2 = null;
		String store_acc = null;
		// Auf jeden Fall, das erste Pane selektieren, dass nicht geloescht wird.
		if (ind == 0)
		{
			store_acc2 = vAcc.get(1);
			if (vAcc.size() > 2)
			{
				store_acc = vAcc.get(2);
			}
			lastIndex = 1;
			tAccuracy.setText(vAcc.get(1));
			jtp.setSelectedIndex(1);
		}
		else
		{
			lastIndex = 0;
			tAccuracy.setText(vAcc.get(0));
			jtp.setSelectedIndex(0);
		}

		jtp.removeTabAt(ind);
		vTables.removeElementAt(ind);
		vAcc.removeElementAt(ind);
		vDistrict.removeElementAt(ind);
		count--;
		if (lastIndex > ind)
		{
			// Dieser Fall tritt nur ein, falls ind == 0
			tAccuracy.setText(vAcc.get(0));
			jtp.setSelectedIndex(0);
			lastIndex = 0;
		}
		fireMultiplicityChanged(new MultiplicityEvent(this, 0, vTables.size()));

		if (bMultiple)
		{
			jtp.setTitleAt(0, "1: " + vDistrict.elementAt(0));
			for (int i = 1; i < jtp.getTabCount(); i++)
			{
				jtp.setTitleAt(i, (i + 1) + "");
			}

			if (store_acc2 != null)
			{
				this.setAcc(0, store_acc2);
			}

			if (store_acc != null)
			{
				this.setAcc(1, store_acc);
			}
		}
	}

	/** Setzen der Werte einer Spalte
	 * @param data Vector mit den neuen Einträgen
	 * @param id Nummer der Tabelle
	 * @param col Nummer der Spalte */
	public void setValues(Vector<?> data, int id, int col)
	{
		// if ((col==1) || (col==2)) {
		WeightsTable tmp = vTables.elementAt(id);
		tmp.setColumnValues(data, col);
		/* }
		 * else if (col==0) {
		 * WeightsTable tmp = (WeightsTable) vTables.elementAt(jtp.getSelectedIndex());
		 * tmp.setColumnValues(data, col);
		 * }
		 * else {
		 * // irgendwas schiefgelaufen
		 * //System.out.println("Falsche Spaltennummer: " + col + "in setValues()");
		 * } */
	}

	/** Änderung der Condition
	 * @param id neue Bedingung */
	public void changeCondition(ConditionItem id)
	{
		// if (vTables.size() == 1) {
		// return;
		// }
		for (int i = 0; i < vTables.size(); i++)
		{
			WeightsTable tmp = vTables.elementAt(i);
			tmp.setMinDir(id);
		}
	}

	/** Änderung der Condition
	 * @param id Nummer der neuen Condition */
	public void changeCondition(int id)
	{
		// if (vTables.size() == 1) {
		// return;
		// }
		for (int i = 0; i < vTables.size(); i++)
		{
			WeightsTable tmp = vTables.elementAt(i);
			tmp.setMinDir(id);
		}
	}

	/** Löschen einer Zeile.
	 * @param row Index der gelöschten Zeile */
	/* public void deleteRow(int row) {
	 * System.out.println("Löschen einer Zeile ist noch nicht implementiert!");
	 * } */

	/** Löschen der aktuellen Tabelle */
	/* private void deleteTable() {
	 * int i = jtp.getSelectedIndex();
	 * if (vTables.size() > 1) {
	 * jtp.removeTabAt(i);
	 * vTables.removeElementAt(i);
	 * vAcc.removeElementAt(i);
	 * vDistrict.removeElementAt(i);
	 * if (vTables.size() == 1) {
	 * // Nachricht an RoundFrame, damit Menu gesperrt wird
	 * //rf.setEnabledDelTable(false);
	 * setMultiple(false);
	 * }
	 * }
	 * } */
	/** Zeilenhöhe
	 * für die Tabellen */
	public int getRowHeight()
	{
		WeightsTable tmp = vTables.firstElement();
		return tmp.getRowHeight();
	}

	/** Auslesen der des Tab-Titels
	 * @return Titel des Tabs (Nummer oder Bezeichnung) */
	/* private String getTabNumber(int id) {
	 * return jtp.getTitleAt(id);
	 * } */
	/** Zusatzbedingung
	 * @return Zusatzbedingung */
	public ConditionItem getSelectedCondition()
	{
		return vTables.firstElement().getSelectedCondition();
	}

	/** Spaltenbezeichnung
	 * @param col Index der Spalte
	 * @return Bezeichnung der Spalte */
	public String getColumnName(int col)
	{
		return vTables.firstElement().getColumnName(col);
	}

	/** Werte einer Spalte
	 * @param id Index des Tabs
	 * @param col Spalte in der Tabelle
	 * @return Vector mit den Daten */
	public Vector<Object> getColumnValuesAt(int id, int col)
	{
		return vTables.elementAt(id).getColumnValues(col);
	}

	/** Zeilen anpassen
	 * Sollte eigentlich die Zeilenzahl aller vorhandenen Tabellen
	 * so einstellen, daß kein (grauer) Panelbereich zu sehen ist.
	 * Funktioniert aber leider nicht richtig. */
	public void adaptRows()
	{
		/* for (int i=0; i<vTables.size(); i++) {
		 * WeightsTable tmp = (WeightsTable) vTables.elementAt(i);
		 * tmp.updateUI();
		 * tmp.adaptRows();
		 * } */
	}

	/** Setzen des Titels eines Tabs
	 * @param id Index des Tabs
	 * @param title Neuer Titel */
	private void setTitleAt(int id, String title)
	{
		// jtp.setTitleAt(id, title);
		// System.out.println("setTitleAt: " + id + " = " + title);
		String t = (title == null ? (desc + " " + id) : title);
		vDistrict.setElementAt(t, id);
		// jtp kann null sein!!!
		if (jtp != null)
		{
			if (jtp.getSelectedIndex() == id)
			{
				jtp.setTitleAt(id, t);
				jtp.setToolTipTextAt(id, t);
			}
		}
	}

	/** Setzen der Mandatszahl eines Districts
	 * @param id Index des Tabs
	 * @param acc Mandatszahl */
	private void setAcc(int id, String acc)
	{
		// System.out.println("setAcc: " + id + " val: " + acc);
		vAcc.setElementAt(acc, id);
		if (jtp != null)
		{
			if (jtp.getSelectedIndex() == id)
			{
				tAccuracy.setText(acc);
			}
		}
		else
		{
			tAccuracy.setText(acc);
		}
	}

	/** Setzen der Mandatszahl eines Districts
	 * @param id Index des Tabs
	 * @param acc Mandatszahl */
	public void setAcc(int id, int acc)
	{
		setAcc(id, acc + "");
	}

	/** Setzen des aktiven Tabs
	 * @param index Index des Tabs */
	public void setSelectedIndex(int index)
	{
		if (bMultiple && jtp.getTabCount() > index)
		{
			jtp.setSelectedIndex(index);
		}
	}

	/** aktuelle Tabelle
	 * @return Index des aktuellen Tabs */
	public int getSelectedIndex()
	{
		if (bMultiple)
		{
			return jtp.getSelectedIndex();
		}
		else
		{
			return 0;
		}
	}

	/** forceBiprop
	 * @return <b>true</b> wenn auf mehrere Distrikte umgeschaltet wurde */
	public boolean forceBiprop()
	{
		if (!bMultiple && vTables.size() == 1)
		{
			setMultiple(true);
			return true;
		}
		return false;
	}

	/** <b>Title:</b> Klasse JTPChangeListener<br>
	 * <b>Description:</b> Überwachung des TabbedPanes auf Änderungen in des Selektion<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @author Florian Kluge, Christian Brand
	 * @version 2.1 */
	class JTPChangeListener
			implements ChangeListener
	{
		private final boolean aValid = true;

		/** Implementierung von stateChanged
		 * wird nur bei Änderungen in der Selection aufgerufen,
		 * die vom Benutzer durchgeführt wurden. Beim Löschen und
		 * der darauffolgenden Selektionsänderung erfolgt kein Aufruf!
		 * @param e der ChangeEvent */
		@Override
		public void stateChanged(ChangeEvent e)
		{
			if (!bConst)
			{
				if (aValid)
				{
					// System.out.println("stateChanged()");
					// System.out.println("vAcc: " + (vAcc==null));
					// System.out.println("tacc: " + (tAccuracy==null));
					// Speichern der Mandatszahl
					vAcc.setElementAt(tAccuracy.getText(), lastIndex);
					if (jtp.getTabCount() == 1)
					{
						return;
					}
					jtp.setTitleAt(lastIndex, (lastIndex + 1) + "");
					// SelectionListener von der letzten Tabelle entfernen
					vTables.elementAt(lastIndex).remlsl(lsl);
					// jetzt auf aktuelle Tabelle wechseln
					lastIndex = jtp.getSelectedIndex();
					tAccuracy.setText(vAcc.elementAt(lastIndex));
					jtp.setTitleAt(lastIndex,
							(lastIndex + 1) + ": " +
									vDistrict.elementAt(lastIndex));
					// ((WeightsTable) vTables.elementAt(lastIndex)).adaptRows();
					vTables.elementAt(lastIndex).addlsl(lsl);
					vTables.elementAt(lastIndex).updateUI();
					rf.tableSwitched();
					// JOptionPane.showMessageDialog(rf, "JTP", "Pause", JOptionPane.WARNING_MESSAGE);
					// System.out.println("Call adaptrows()");
					vTables.elementAt(lastIndex).adaptRows();

					// Debug-Code für TabbedPaneUI
					/* javax.swing.plaf.TabbedPaneUI tpu = jtp.getUI();
					 * int rc = tpu.getTabRunCount(jtp);
					 * System.out.println("TabRunCount: " + rc);
					 * for (int i=0; i<rc; i++) {
					 * Rectangle ra = tpu.getTabBounds(jtp, i);
					 * System.out.println(" RA: " + ra);
					 * }
					 * System.out.println("tfc: " + tpu.tabForCoordinate(jtp, 2, 25)); */

					/* }
					 * else {
					 * // ungültige Accuracy
					 * aValid = false;
					 * jtp.setSelectedIndex(lastIndex);
					 * aValid = true;
					 * } */
				}
			}
		}
	}

	public JTabbedPane getTabbedPane()
	{
		return jtp;
	}

}
