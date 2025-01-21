/*
 * @(#)QElem.java 2.1 18/04/05
 * 
 * Copyright (c) 2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui.district;

/** <b>Title:</b> Klasse QElem<br>
 * <b>Description:</b> Element einer DistrictQueue. Enthält Informationen, ob dieser Distrikt eingefügt,
 * aktualisiert oder gelöscht wird.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007
 * <b>Company:</b> Universität Augsburg
 * @author Florian Kluge, Christian Brand
 * @version 2.1 */
public class QElem
{

	/** Action: Distrikt wird neu eingefügt */
	public static final int INS = 1;

	/** Action: Distrikt wird aktualisiert */
	public static final int UPD = 2;

	/** Action: Distrikt wird gelöscht */
	public static final int DEL = 3;

	/** Konstruktor erstellt ein neues Element ohne Verweise auf vorheriges und
	 * nächstes Element.
	 * 
	 * @param dn Ein Distrikt
	 * @param m Wie soll mit diesem Distrikt Verfahren werden? (INS|DEL|UPD) */
	public QElem(District dn, int m)
	{
		this.d = dn;
		this.action = m;
		this.next = null;
		this.prev = null;
	}

	/** Verweis auf das nächste Element */
	public QElem next;

	/** Verweis auf das vorherige Element */
	public QElem prev;

	/** Der District */
	public District d;

	/** Aktion */
	public int action;

	/** Erzeugt eine String-Repräsentation dieses QElems
	 * 
	 * @return Ein Queue-Element */
	public String toString()
	{
		String tmp = "action " + action + " ";
		tmp += d.toString();
		return tmp;
	}
}
