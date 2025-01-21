/*
 * @(#)NumberCheckbox.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.AWTEventMulticaster;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** <b>Title:</b> Klasse NumberCheckbox<br>
 * <b>Description:</b> Checkbox mit einer Nummer als Markierung<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @version 2.1
 * @author Jan Petzold, Christian Brand */
public class NumberCheckbox extends JPanel implements ItemSelectable, Icon
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Abfangen der Item-Ereignisse. */
	protected ItemListener itemListener = null;

	/** Gruppe, in der diese Checkbox registriert ist. */
	protected NumberCheckboxGroup ncg = null;

	/** Referenz auf sich selbst. */
	private NumberCheckbox itself = this;

	/** Breite und Höhe der Checkbox. */
	int w;

	/** Breite und Höhe der Checkbox. */
	int h;

	/** Zustand der Checkbox. */
	boolean click;

	/** Name der Checkbox. */
	private String name;

	/** Label neben der eigentlichen Checkbox. */
	private JLabel label;

	/** Nummer der Checkbox. */
	int number = 0;

	/** Aktivierungsstatus */
	private boolean bEnabled = true;

	/** Erzeugt eine Instanz einer NumberCheckbox.
	 * 
	 * @param name Name der Checkbox.
	 * @param ncg Gruppe, in der die Checkbox registriert werden soll. */
	public NumberCheckbox(String name, NumberCheckboxGroup ncg)
	{
		this.name = name;
		this.ncg = ncg;
		click = false;
		setLayout(new BorderLayout());
		label = new JLabel(name, this, JLabel.HORIZONTAL);
		label.setFont(GUIConstraints.getFont(GUIConstraints.LABEL_FONT_BOLD));
		label.setForeground(Color.black);
		Dimension d = label.getPreferredSize();
		w = d.width;
		h = d.height;
		setSize(w, h);
		addMouseListener(new NumberCheckboxMouseListener());
		addMouseMotionListener(new NumberCheckboxMouseMotionListener());
		add("West", label);
	}

	/** Zeichnen der eigentlichen Checkbox ohne Label.
	 * Implementierung von Icon.
	 * 
	 * @param c Component
	 * @param g Graphics
	 * @param a int
	 * @param b int */
	public void paintIcon(Component c, Graphics g, int a, int b)
	{
		int x = a + 4;
		int y = b + 4;
		g.setColor(Color.black);
		g.drawLine(x + 1, y + 1, x + 1, y + 12);
		g.drawLine(x + 1, y + 1, x + 10, y + 1);
		g.setColor(Color.gray);
		g.drawLine(x, y, x, y + 13);
		g.drawLine(x, y, x + 11, y);
		g.setColor(Color.white);
		g.drawLine(x + 12, y + 14, x, y + 14);
		g.drawLine(x + 12, y + 14, x + 12, y);

		if (click)
		{
			g.setColor(Color.lightGray);
		}
		else if (!bEnabled)
		{
			g.setColor(Color.lightGray);
		}
		g.fillRect(x + 2, y + 2, 9, 11);

		float f[] = Color.RGBtoHSB(220, 220, 220, null);
		g.setColor(Color.getHSBColor(f[0], f[1], f[2]));
		g.drawLine(x + 11, y + 13, x + 1, y + 13);
		g.drawLine(x + 11, y + 13, x + 11, y + 1);

		g.setColor(Color.black);
		if (number != 0)
		{
			Font tempFont = g.getFont();
			g.setFont(new Font("Dialog", Font.BOLD, 12));
			g.drawString("" + number, x + 3, y + 12);
			g.setFont(tempFont);
		}
	}

	/** Breite der eigentlichen Checkbox.
	 * Implementierung von Icon.
	 * 
	 * @return Breite */
	public int getIconWidth()
	{
		return 22;
	}

	/** Höhe der eigentlichen Checkbox.
	 * Implementierung von Icon.
	 * 
	 * @return Höhe */
	public int getIconHeight()
	{
		return 24;
	}

	/** Hinzufügen des ItemListener.
	 * Implemtierung von ItemSelectable.
	 * 
	 * @param l ItemListener */
	public void addItemListener(ItemListener l)
	{
		itemListener = AWTEventMulticaster.add(itemListener, l);
	}

	/** Rückgabe der selektierten Objekte.
	 * Implemtierung von ItemSelectable.
	 * 
	 * @return Object[] */
	public Object[] getSelectedObjects()
	{
		return null;
	}

	/** Entfernen des ItemListener.
	 * Implemtierung von ItemSelectable.
	 * 
	 * @param l ItemListener */
	public void removeItemListener(ItemListener l)
	{
		itemListener = AWTEventMulticaster.remove(itemListener, l);
	}

	/** Registrieren bei der Gruppe, dass die Checkbox markiert wurde. */
	private void register()
	{
		number = ncg.register(this);
		repaint();
	}

	/** Enfernen der Registrierung, da die Checkbox demarkiert wurde. */
	private void cancel()
	{
		ncg.cancel(this);
		number = 0;
		repaint();
	}

	/** Setzen der Nummer der Checkbox, die von der Gruppe vergeben wurde.
	 * 
	 * @param state Nummer */
	void setNumberState(int state)
	{
		number = state;
		repaint();
	}

	/** Setzen eines neuen Textes neben der Checkbox.
	 * 
	 * @param text Text */
	public void setLabel(String text)
	{
		label.setText(text);
		repaint();
	}

	/** Setzen des gespeicherten Namen neben der Checkbox. */
	public void setLabel()
	{
		label.setText(name);
		repaint();
	}

	/** Beim ItemListener das Ereignis auslösen, dass sich der Status der Checkbox geändert hat. */
	private void itemStateChanged()
	{
		ItemEvent ie = new ItemEvent(this, 0, this, number);
		if (itemListener != null)
		{
			itemListener.itemStateChanged(ie);
		}
	}

	/** <b>Title:</b> Klasse NumberCheckboxMouseListener<br>
	 * <b>Description:</b> Abfangen von Maus-Klicks<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @version 2.1
	 * @author Jan Petzold, Christian Brand */
	public class NumberCheckboxMouseListener extends MouseAdapter
	{

		/** Aufruf bei dem Ereignis "Maus ist gedrückt".
		 * 
		 * @param e MouseEvent */
		public void mousePressed(MouseEvent e)
		{
			Point p = e.getPoint();
			if ((p.x < w) && (p.y < h) && (p.x > 0) && (p.y > 0) && bEnabled)
			{
				itself.grabFocus();
				click = true;
				repaint();
			}
		}

		/** Aufruf bei dem Ereignis "Maus wurde losgelassen".
		 * 
		 * @param e MouseEvent */
		public void mouseReleased(MouseEvent e)
		{
			Point p = e.getPoint();
			if ((p.x < w) && (p.y < h) && (p.x > 0) && (p.y > 0) && bEnabled)
			{
				click = false;
				if (number == 0)
				{
					register();
				}
				else
				{
					cancel();
				}
				itemStateChanged();
			}
		}
	}

	/** <b>Title:</b> Klasse NumberCheckboxMouseMotionListener<br>
	 * <b>Description:</b> Abfangen von Maus-Bewegungen<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * 
	 * @version 2.0
	 * @author Jan Petzold, Christian Brand */
	public class NumberCheckboxMouseMotionListener extends MouseMotionAdapter
	{

		/** Aufruf, wenn die Maus gedrückt verschoben wird.
		 * 
		 * @param e MouseEvent */
		public void mouseDragged(MouseEvent e)
		{
			Point p = e.getPoint();
			if ((p.x < w) && (p.y < h) && (p.x > 0) && (p.y > 0))
			{
				if (!click)
				{
					click = true;
					repaint();
				}
			}
			else
			{
				if (click)
				{
					click = false;
					repaint();
				}
			}
		}
	}

	/** Rückgabe der bevorzugten Größe der gesamten Checkbox.
	 * 
	 * @return Bevorzugte Größe der Checkbox */
	public Dimension getPreferredSize()
	{
		return (new Dimension(w, h));
	}

	/** Rückgabe der minimalen Größe der gesamten Checkbox.
	 * 
	 * @return Minimale Größe der Checkbox */
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	/** Rückgabe des Namen der Checkbox.
	 * @return Name */
	public String getName()
	{
		return name;
	}

	public void setName(String n)
	{
		this.name = n;
		label.setText(n);
	}

	public void setColor(java.awt.Color c)
	{
		label.setForeground(c);
	}

	/** Angabe, ob Checkbox markiert ist oder nicht.
	 * 
	 * @return <b>true</b> wenn diese Checkbox markiert ist */
	public boolean getState()
	{
		if (number == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/** Markieren oder Demarkieren der Checkbox.
	 * 
	 * @param b boolean */
	public void setState(boolean b)
	{
		if (number == 0 && b)
		{
			register();
		}
		else if (number != 0 && !b)
		{
			cancel();
		}
	}

	/** Rückgabe der Nummer der Checkbox.
	 * @return Nummer
	 * @uml.property name="number" */
	public int getNumber()
	{
		return number;
	}

	/** De-/Aktivieren der Checkbox
	 * @param b Aktivierungsstatus */
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		bEnabled = b;
		if (!b)
		{
			setState(false);
			label.setForeground(Color.gray);
		}
		else
		{
			label.setForeground(Color.black);
		}
		repaint();
	}
}
