/*
 * @(#)EditableHeaderUI.java 2.1 07/04/11
 * 
 * Copyright (c) 2000-2007 Lehrstuhl für Stochastik und ihre Anwendungen
 * Institut für Mathematik, Universität Augsburg
 * D-86135 Augsburg, Germany
 */


package de.uni.augsburg.bazi.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.TableColumnModel;

/** <b>Title:</b> Klasse EditableHeaderUI<br>
 * <b>Description:</b> UI der EditableHeader<br>
 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
 * <b>Company:</b> Universität Augsburg<br>
 * Achtung: Nach Erzeugung muß noch setWT(...) aufgerufen werden!
 * @version 2.1
 * @author Jan Petzold, Christian Brand */
public class EditableHeaderUI extends BasicTableHeaderUI
{

	// /** Tabellenmodell für die Sortieraufrufe */
	// private WeightsTable wt;

	public EditableHeaderUI()
	{
		super();
		// System.out.println("EditableHeaderUI::<init>");
	}

	public void setWT(WeightsTable w)
	{
		// wt = w;
		// System.out.println("EditableHeaderUI::setWT");
	}

	/** Erstellen des Listener für Maus-Eingaben. */
	@Override
	protected MouseInputListener createMouseInputListener()
	{
		return new MouseInputHandler((EditableHeader) header);
	}

	/** <b>Title:</b> Klasse EditableHeaderUI.MouseInputHandler<br>
	 * <b>Description:</b> Verwaltung der Maus-Ereignisse<br>
	 * <b>Copyright:</b> Copyright (c) 2000-2007<br>
	 * <b>Company:</b> Universität Augsburg<br>
	 * @author Jan Petzold, Christian Brand
	 * @version 2.1 */
	public class MouseInputHandler
			extends BasicTableHeaderUI.MouseInputHandler
	{
		/** Komponente, auf die das Maus-Ereignis ausgeübt wurde. */
		private Component dispatchComponent;

		/** Kopf der Tabelle, welcher die Komponente enthält. */
		protected EditableHeader header;

		/** Ablaufsteuerung, einzelne Klicks müssen zur Sortierung registriert werden! */
		private boolean edited = false;

		/** Erzeugt eine Instanz von MouseInputHandler. */
		public MouseInputHandler(EditableHeader header)
		{
			this.header = header;
		}

		/** Setzen der Komponente, auf die das Maus-Ereignis ausgeübt wurde. */
		private void setDispatchComponent(MouseEvent e)
		{
			Component editorComponent = header.getEditorComponent();
			Point p = e.getPoint();
			Point p2 = SwingUtilities.convertPoint(header, p, editorComponent);
			dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent,
					p2.x, p2.y);
			// System.out.println("setDispatchComponent");
		}

		/** Senden des Maus-Ereignisses an dispatchComponent. */
		private boolean repostEvent(MouseEvent e)
		{
			// System.out.print("repostEvent: ");
			if (dispatchComponent == null)
			{
				// System.out.println("false");
				return false;
			}
			MouseEvent e2 = SwingUtilities.convertMouseEvent(header, e,
					dispatchComponent);
			dispatchComponent.dispatchEvent(e2);
			// System.out.println("true");
			return true;
		}

		/** Ereignis-Behandlung: Maus wurde gedrückt. */
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (!SwingUtilities.isLeftMouseButton(e))
			{
				return;
			}
			super.mousePressed(e);

			// System.out.println("MouseClicked: " + e.getClickCount());

			Point p = e.getPoint();
			TableColumnModel columnModel = header.getColumnModel();
			int index = columnModel.getColumnIndexAtX(p.x);
			if (header.getResizingColumn() == null)
			{
				// System.out.println("null");
				// System.out.println("index: " + index);
				if (index != -1)
				{
					if (header.editCellAt(index, e))
					{
						setDispatchComponent(e);
						edited = repostEvent(e);
					}
				}
			}
			if ((e.getClickCount() == 1) && !edited)
			{ // Sollte nur ausgelöst werden, wenn durch den Klick keine Editierung beendet wurde!
				// System.out.println("Call Sort for column " + index);
				// beachte nur Ereignisse, die die erste oder zweite Spalte betreffen:
				// System.out.println(wt);
				if (index == 0)
				{
					// wt.getModel().sort0();
				}
				else if (index == 1)
				{
					// wt.getModel().sort1();
				}
				// wt.updateUI();
			}
		}

		/** Ereignis-Behandlung: Maus wurde gelöst. */
		@Override
		public void mouseReleased(MouseEvent e)
		{
			super.mouseReleased(e);
			if (!SwingUtilities.isLeftMouseButton(e))
			{
				return;
			}
			edited = repostEvent(e);
			dispatchComponent = null;
		}

	}

}
