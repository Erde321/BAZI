/*
 * @(#)AboutDialog.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.VersionControl;

/** <b>Title:</b> Klasse AboutDialog<br>
 * <b>Description:</b> Anzeigen der Informationen über dieses Programm<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * 
 * @version 2.1
 * @author Jan Petzold, Christian Brand */
public class AboutDialog extends JDialog
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Erzeugt eine Instanz von AboutDialog. Stellt die Informationen über das Programm als Dialog dar.
	 * 
	 * @param roundFrame Referenz auf das RoundFrame.
	 * @param logo BAZI-Logo. */
	public AboutDialog(final RoundFrame roundFrame, ImageIcon logo)
	{
		super(roundFrame, Resource.getString("bazi.gui.menu.about"), true);

		JPanel pMain = new JPanel(new BorderLayout());
		JLabel label = new JLabel(logo);

		pMain.add(label, BorderLayout.NORTH);

		/*JTabbedPane jtp = new JTabbedPane(); */

		JPanel contentPanel = new JPanel();
		/*SuperLayout sl = new SuperLayout(contentPanel); */
		String content = String.format(Resource.getString("bazi.gui.info"), VersionControl.getVersion(), VersionControl.YEAR, VersionControl.YEAR, VersionControl.YEAR);
		JEditorPane jepAbt = new JEditorPane("text/html", content);
		jepAbt.setEditable(false);
		/**pMain.add(new JScrollPane(jepAbt)); 	*/
		pMain.add(jepAbt,BorderLayout.CENTER);
		jepAbt.setBackground(label.getBackground());
		jepAbt.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					roundFrame.showDocument(e.getDescription());
				}
			}
		});
		int w = Integer.parseInt(Resource.getString("bazi.gui.info.width"));
		int h = Integer.parseInt(Resource.getString("bazi.gui.info.height"));
		jepAbt.setPreferredSize(new Dimension(w, h));
		jepAbt.setRequestFocusEnabled(false);
		/*sl.add(jepAbt, 0, 1, 1, 1, 1, 1, GridBagConstraints.BOTH,
				GridBagConstraints.WEST);*/
		

	/*	jtp.add(contentPanel, Resource.getString("bazi.gui.info.about"));

		// Jetzt muß noch das Tab mit der Lizenz erstelle werden.
		// hier muß noch der richtige Link auf die Lizenz erstellt werden
		String sLic = new String();
		String tmp;
		try
		{
			InputStreamReader in = new InputStreamReader(new FileInputStream(new File(
					"gnugpl.txt")));
			BufferedReader data = new BufferedReader(in);
			while ((tmp = data.readLine()) != null)
			{
				sLic += "\n" + tmp;
			}
			data.close();
			in.close();
		}
		catch (IOException e)
		{
			System.out.println("IO Error:" + e.getMessage());
		}

		JEditorPane jepLic = new JEditorPane("text/plain", sLic);
		jepLic.setFont(new Font("Monospaced", 0, 12));
		jepLic.setCaretPosition(0);
		jepLic.setEditable(false);
		jepLic.setBackground(label.getBackground());
		jepLic.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					roundFrame.showDocument(e.getDescription());
				}
			}
		});
		jepLic.setPreferredSize(new Dimension(w, h));
		jepLic.setRequestFocusEnabled(false);

		jtp.add(new JScrollPane(jepLic), Resource.getString("bazi.gui.info.licence"));

		// und nun die Homepage
		JPanel pHp = new JPanel();
		sl = new SuperLayout(pHp);

		JEditorPane jepHp = new JEditorPane("text/html",
				Resource.getString("bazi.gui.info.hpcont"));
		jepHp.setEditable(false);
		jepHp.setBackground(label.getBackground());
		jepHp.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					roundFrame.showDocument(e.getDescription());
				}
			}
		});
		jepHp.setRequestFocusEnabled(false);
		jepHp.setPreferredSize(new Dimension(w, h));
		sl.add(jepHp, 0, 1, 1, 1, 1, 1, GridBagConstraints.BOTH,
				GridBagConstraints.WEST);

		jtp.add(pHp, Resource.getString("bazi.gui.info.hp"));

		pMain.add(jtp, BorderLayout.CENTER); */
		setContentPane(pMain);

		pack();
		setLocationRelativeTo(roundFrame);
		setVisible(true); 
	}
}
