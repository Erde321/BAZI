package de.uni.augsburg.bazi.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.uni.augsburg.bazi.Resource;

/** <b>Title:</b> Innere Klasse InfoFrame<br>
 * <b>Description:</b> Informationfenster für eine .bazi Datei.<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * @author Jan Petzold, Christian Brand, Marco Schumacher
 * @version 2.2 */
public class InfoFrame extends JFrame implements HyperlinkListener
{

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/** Zur Anzeige des Inhalts der Infodatei */
	private JEditorPane editorPane;

	/** Singleton-Instanz **/
	private static InfoFrame unique;

	/** Erzeugung eines leeren InfoFrames. */
	private InfoFrame()
	{
		super();
		setTitle(Resource.getString("bazi.database.info.title"));

		Image image = (new ImageIcon(getClass().getResource("images/bazi_icon.gif")).getImage());
		setIconImage(image);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		JPanel content = new JPanel();
		SuperLayout layout = new SuperLayout(content);

		editorPane = new JEditorPane();
		editorPane.setContentType("text/plain");
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(this);
		editorPane.setFont(GUIConstraints.getFont(GUIConstraints.OUTPUT_FONT));

		JScrollPane scrollPane = new JScrollPane(editorPane);
		int w = Integer.parseInt(Resource.getString("bazi.database.info.width"));
		int h = Integer.parseInt(Resource.getString("bazi.database.info.height"));
		if(GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) >= GUIConstraints.FONTSIZE_MIN)
		{
			w = w + (GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) - GUIConstraints.FONTSIZE_MIN ) * 15;
			h = h + (GUIConstraints.getFontSize(GUIConstraints.OUTPUT_FONT) - GUIConstraints.FONTSIZE_MIN ) * 10;
		}
		scrollPane.setPreferredSize(new Dimension(w, h));

		layout.add(scrollPane, 0, 0, 1, 1, 1, 1, GridBagConstraints.BOTH,
				GridBagConstraints.NORTHWEST);

		setContentPane(content);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		int x = screenSize.width - frameSize.width;
		int y = screenSize.height - frameSize.height;
		setLocation(x, y);
	}

	/** Aufrufen der InfoFrame Instanz **/
	public static InfoFrame getInfoFrame()
	{
		if (unique == null)
			unique = new InfoFrame();

		return unique;
	}

	/** Reaktion auf das Anklicken eines Links in Text.
	 * (Interface HyperlinkListener)
	 * 
	 * @param hle HyperlinkEvent */
	public void hyperlinkUpdate(HyperlinkEvent hle)
	{
		// do nothing
	}

	/** Setzen des Textes des Informationsfensters.
	 * 
	 * @param info HTML-Code der anzuzeigenden Info-Datei als Text. */
	public void setText(String info)
	{
		editorPane.setText(info);
		editorPane.setCaretPosition(0);
	}

	public String getText()
	{
		return editorPane.getText();
	}

	/** Setzen des Textes des Informationsfensters.
	 * 
	 * @param info URL der anzuzeigenden Info-Datei.
	 * @throws IOException */
	public void setText(URL info) throws IOException
	{
		editorPane.setPage(info);
		editorPane.setCaretPosition(0);
	}

	/** Setzen des Textes des Informationsfensters.
	 * 
	 * @param info URL oder HTML-Code als Text der anzuzeigenden Info-Datei. */
	public void setText(Object info) throws IOException
	{
		if (info instanceof URL)
		{
			editorPane.setPage((URL) info);
		}
		else if (info instanceof String)
		{
			editorPane.setText((String) info);
		}
		editorPane.setCaretPosition(0);
	}
}