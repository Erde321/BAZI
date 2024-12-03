/*
 * @(#)DebugDialog.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */

package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** <b>Title:</b> Klasse DebugDialog<br>
 * <b>Description:</b> Ein Frame, in den die generierten Debug Informationen geschrieben werden. Implementiert das MethodListener Interface.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg
 * @version 2.1
 * @author Florian Kluge, Christian Brand */

public class DebugDialog extends JFrame implements de.uni.augsburg.bazi.lib.MethodListener
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** TextArea für die Debug-Ausgabe */
	private JTextArea jtaOut;

	/** Scrollpane für das TextArea */
	private JScrollPane jsc;

	/** Button zum Speichern der Ausgabe */
	private JButton bSave;

	/** Eigentümer */
	private RoundFrame rf;

	/** Erzeugt einen neuen DebugDialog, mit dem übergebenen RoundFrame als Owner.
	 * 
	 * @param owner RoundFrame */
	public DebugDialog(RoundFrame owner)
	{
		// super(owner, "DebugOutput", false);
		super("DebugOutput");
		rf = owner;
		JPanel pc = new JPanel(new BorderLayout());
		jtaOut = new JTextArea(25, 80);
		jtaOut.setFont(new Font("Monospaced", 0, 12));
		jtaOut.setEditable(false);
		jsc = new JScrollPane(jtaOut);
		pc.add(jsc, BorderLayout.CENTER);
		bSave = new JButton("Speichern");
		bSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save();
			}
		});
		pc.add(bSave, BorderLayout.SOUTH);
		setContentPane(pc);
		pack();
	}

	/** Fügt den übergebenen String dem Textfeld hinzu und fügt zusätzlich einen
	 * Zeilenumbruch ein. Der Cursor wird auf das Ende gesetzt.
	 * 
	 * @param s String */
	private void println(String s)
	{
		if (this.isVisible())
		{
			jtaOut.append(s + "\n");
			jtaOut.setCaretPosition(jtaOut.getText().length());
		}
	}

	/** Löscht den Inhalt der Textbox. */
	public void clear()
	{
		jtaOut.setText("");
		jtaOut.setCaretPosition(jtaOut.getText().length());
	}

	/** Speichern des Textinhalts */
	private void save()
	{
		rf.getFileIO().export(jtaOut.getText());
	}

	/** Implemtiert von MethodListener.
	 * Fügt den übergebenen String und zusätzlich einen Zeilenumbruch zu der
	 * Textbox hinzu.
	 * 
	 * @param msg String */
	public void printMessage(String msg)
	{
		println(msg);
	}

}
