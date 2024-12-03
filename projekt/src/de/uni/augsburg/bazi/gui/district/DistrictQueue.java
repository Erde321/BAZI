/*
 * @(#)DistrictQueue.java 2.1 18/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui.district;

/** <b>Title:</b> Klasse DistrictQueue<br>
 * <b>Description:</b> Eine Liste, die alle Änderungen an Distrikten, neue Distrikte und gelöschte sammelt.
 * Diese Klasse wird benutzt um die Tabelle im Hauptfenster zu aktualisieren.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @version 2.1
 * @author Florian Kluge, Christian Brand */
public class DistrictQueue
{

	/** Kopf der Schlange, hier wird gelesen */
	private QElem head;

	/** Schwanz der Schlange, hier wird angehängt */
	private QElem tail;

	/** Standardkonstruktor. Erzeugt eine leere Queue */
	public DistrictQueue()
	{
		head = null;
		tail = null;
	}

	/** Anhängen eines neuen Elements
	 * @param d der Wert des Districts
	 * @param m Methoden-Code */
	public void enqueue(District d, int m)
	{
		QElem tmp = new QElem(d, m);
		if (tail != null)
		{
			// Zuerst noch die Verkettung richtig setzen
			tail.next = tmp;
			tmp.prev = tail;
		}
		// neuer Schwanz
		tail = tmp;
		// Falls die Schlange leer war, ist tmp automatisch auch gleich der Kopf
		if (head == null)
		{
			head = tmp;
		}
	}

	/** Lesen des ersten Elements
	 * @return Liefert das erste Element in der Schlange */
	public QElem getFirst()
	{
		return new QElem(head.d, head.action);
	}

	/** Entfernen des ersten Elements liefert dieses auch gleich zurück.
	 * Diese Methode wird beim Schreiben in der Datenbank verwendet.
	 * @return gibt das entfernte Element zurück */
	public QElem removeFirst()
	{
		QElem tmp = head;
		// Einelementige Schlange
		if (head == tail)
		{
			head = null;
			tail = null;
		}
		// >= 2 Elemente vorhanden
		else
		{
			head = tmp.next;
			head.prev = null;
		}
		return new QElem(tmp.d, tmp.action);
	}

	/** Entfernen des letzten Elements liefert dieses auch gleich zurück.
	 * Diese Methode ist für die Rückängig-Funktionalität nötig.
	 * @return das entfernte Element */
	public QElem removeTail()
	{
		QElem tmp = tail;
		// Einelementige Schlange
		if (head == tail)
		{
			head = null;
			tail = null;
		}
		// >= 2 Elemente vorhanden
		else
		{
			tail = tmp.prev;
			tail.next = null;
		}
		return tmp;
	}

	/** ist die Schlange leer?
	 * 
	 * @return <b>true</b> wenn die Schlange leer ist */
	public boolean isEmpty()
	{
		return ((head == null) && (tail == null));
	}

	/** sind noch Elemente vorhanden?
	 * 
	 * @return <b>true</b> wenn die Schlange noch mindestens ein Element hat */
	public boolean hasMoreElements()
	{
		return ((head != null) && (tail != null));
	}

	/** leeren der Schlange */
	public void removeAll()
	{
		head = null;
		tail = null;
	}

	/** Erzeugt eine String-Repräsentation dieser Queue.
	 * 
	 * @return Queue als String */
	public String toString()
	{
		String tmp = "Q: ";
		if (head == null)
		{
			return tmp + "empty";
		}
		QElem qt = head;
		while (qt != null)
		{
			District dt = qt.d;
			tmp += "dn:" + dt.nummer + ";A:" + qt.action;
			tmp += ";dna:" + dt.name + ";ac:" + dt.mandate;
			tmp += "||";
			qt = qt.next;
		}
		return tmp;
	}
}
