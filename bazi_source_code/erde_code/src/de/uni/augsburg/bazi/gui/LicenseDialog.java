/*
 * @(#)LicenseDialog.java 10/04/2011
 * 
 * Copyright (c) 2000-2011 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.uni.augsburg.bazi.Resource;

/** <b>Title:</b> Klasse LicenseDialog<br>
 * <b>Description:</b> Anzeigen der Lizenzen<br>
 * <b>Copyright:</b> Copyright (c) 2000-2011<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @author Monika Sussmann */
public class LicenseDialog extends JDialog
{
	/** Default UID */
	private static final long serialVersionUID = 1L;
	/**public static final int HORIZONTAL_SCROLLBAR_NEVER = 31; */
		/** Erzeugt eine Instanz von LicenseDialog.
	 * 
	 * @param roundFrame Referenz auf das RoundFrame. */
	public LicenseDialog(final RoundFrame roundFrame, ImageIcon logo)
	{
		super(roundFrame, Resource.getString("bazi.gui.menu.license"), true);

		JPanel pMain = new JPanel(new BorderLayout());
		JLabel label = new JLabel(logo);
		pMain.add(label, BorderLayout.NORTH);

		JEditorPane jepLcn = new JEditorPane("text/txt", getText());
		jepLcn.setFont(new Font("Monospaced", 0, 12));
		jepLcn.setCaretPosition(0);
		jepLcn.setEditable(false);
		jepLcn.setBackground(pMain.getBackground());
		jepLcn.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					roundFrame.showDocument(e.getDescription());
				}
			}
		});
		int w = Integer.parseInt(Resource.getString("bazi.gui.license.width"));
		int h = Integer.parseInt(Resource.getString("bazi.gui.license.height"));
		jepLcn.setPreferredSize(new Dimension(w, h));
		jepLcn.setRequestFocusEnabled(false);
		

		JScrollPane sp = (JScrollPane) pMain.add(new JScrollPane(jepLcn)) ; 
		setContentPane(pMain);
		pack();
		setLocationRelativeTo(roundFrame);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setVisible(true);
	}

	private String getText()
	{
		try
		{
			String out = "";
			InputStreamReader in = new InputStreamReader(new FileInputStream("gnugpl.txt"));
			BufferedReader reader = new BufferedReader(in);
			String temp;
			while ((temp = reader.readLine()) != null)
			{
				out += temp + "\n";
			}
			reader.close();
			in.close();
			return out;
		}
		catch (IOException e)
		{
			System.out.println("IO Error:" + e.getMessage());
		}
		return "Not found: gnugpl.txt";
	}
}
