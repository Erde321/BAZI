package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import de.uni.augsburg.bazi.Resource;
import de.uni.augsburg.bazi.driver.OutputFormat;

public class Dialog_Min_Plus extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	/** Referenz auf WeightsTable */
	private static Vector<WeightsTable> wt;

	private JTextField textField;
	private double value;

	public Dialog_Min_Plus(RoundFrame rf, double value, Vector<WeightsTable> wTables)
	{	
		super(rf, Resource.getString("bazi.gui.menu.minplus"), true);
		setLocationRelativeTo(rf);
		this.value = value;
		
		// Referenz auf Vektor der Weight-Tables speichern
		wt = wTables; 

		JPanel panel = new JPanel();
		panel.add(new JLabel(Resource.getString("bazi.gui.minplus.desc")));
		panel.setBorder(new EtchedBorder());
		panel.setPreferredSize(new Dimension(425, 125));
		add(panel, BorderLayout.NORTH);
		
		panel = new JPanel();
		panel.add(new JLabel("x ="), BorderLayout.WEST);
		panel.add(textField = new JTextField(10));
		textField.setText(value + "");
		panel.add(new JLabel("%"), BorderLayout.EAST);
		add(panel);

		panel = new JPanel();
		JButton b = new JButton(Resource.getString("bazi.gui.ok"));
		b.addActionListener(this);
		panel.add(b);
		b = new JButton(Resource.getString("bazi.gui.cancel"));
		b.addActionListener(this);
		panel.add(b);
		add(panel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	public double getValue()
	{
		return value;
	}
	
	public static void minPlus(double value, Vector<WeightsTable> wTables) 
	{
		wt = wTables; 
		ConditionItem[] items = new ConditionItem[WeightsTable.cMinDirlength];
		for(int i = 0; i < WeightsTable.cMinDirlength; i++) 
		{
			items[i] = (ConditionItem) wt.firstElement().getcMinDir().getItemAt(i); 
			
			if(items[i].getFormat() == OutputFormat.CONDITION_MIN_PLUS) {
				items[i] = new ConditionItem(Resource.getString("bazi.gui.table.minimum") + "+" + value + "%", 
            					Resource.getString("bazi.gui.table.minimum") + "+" + value + "%", 
            					OutputFormat.CONDITION_MIN_PLUS);
			}
		}		
		for (int i = 0; i < wt.size(); i++)
			wt.elementAt(i).setCondItems(items);
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(Resource.getString("bazi.gui.ok")))
			try
			{
				value = Double.parseDouble(textField.getText());
				minPlus(value, wt);
			}
			catch (Exception ex)
			{}
		dispose();
	}
}
