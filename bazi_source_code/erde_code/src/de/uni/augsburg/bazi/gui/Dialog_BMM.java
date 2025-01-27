package de.uni.augsburg.bazi.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import de.uni.augsburg.bazi.Resource;

public class Dialog_BMM extends JDialog implements ActionListener
{
	/** Default UID */
	private static final long serialVersionUID = 1L;

	private RoundFrame rf;

	private JTextField tf_base, tf_min, tf_max;
	private JRadioButton rb_None, rb_BMM, rb_BMMP;
	private ButtonGroup bg = new ButtonGroup();

	private JButton b_Ok, b_Cancel;

	public Dialog_BMM(RoundFrame rf)
	{
		super(rf, Resource.getString("bazi.gui.bmm"), true);
		this.rf = rf;

		if (rf.isMultiple())
		{
			JOptionPane.showMessageDialog(this, "Das ist mit aktivierten Distriktoptionen nicht möglich!",
					Resource.getString("bazi.error.title"), JOptionPane.WARNING_MESSAGE);
			dispose();
			return;
		}

		// ///////////////////////////////////////
		// init Buttons und Lister
		// ///////////////////////////////////////
		rb_None = new JRadioButton("None");
		rb_BMM = new JRadioButton("Base+Min..Max");
		rb_BMMP = new JRadioButton("Base+Min..Max(Pwr)");

		bg = new ButtonGroup();
		bg.add(rb_None);
		bg.add(rb_BMM);
		bg.add(rb_BMMP);

		rb_None.setSelected(true);
		rb_BMM.setSelected(rf.isBMM());
		rb_BMMP.setSelected(rf.isPowerWeighted());

		tf_base = new JTextField(5);
		tf_min = new JTextField(5);
		tf_max = new JTextField(5);

		int[] data = rf.getBMMData();
		tf_base.setText(data[0] + "");
		tf_min.setText(data[1] + "");
		tf_max.setText(data[2] < Integer.MAX_VALUE ? (data[2] + "") : "oo");

		b_Ok = new JButton(Resource.getString("bazi.gui.ok"));
		b_Ok.addActionListener(this);
		b_Cancel = new JButton(Resource.getString("bazi.gui.cancel"));
		b_Cancel.addActionListener(this);

		// ///////////////////////////////////////
		// init Grafik
		// ///////////////////////////////////////
		add(getContent());

		JPanel buttons = new JPanel();
		buttons.add(b_Ok);
		buttons.add(b_Cancel);
		add(buttons, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(rf);
		setVisible(true);
	}

	private JPanel getContent()
	{
		JPanel master = new JPanel(new GridBagLayout());
		master.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridwidth = 6;
		JPanel p = new JPanel();
		p.add(rb_BMM);
		p.add(rb_BMMP);
		p.add(rb_None);
		master.add(p, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		c.insets = new Insets(10, 10, 0, 0);

		master.add(new JLabel("Base:"), c);
		c.gridx++;
		master.add(tf_base, c);

		c.gridx++;
		master.add(new JLabel("Minimum:"), c);
		c.gridx++;
		master.add(tf_min, c);

		c.gridx++;
		master.add(new JLabel("Maximum:"), c);
		c.gridx++;
		master.add(tf_max, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 0, 10);

		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;

		JEditorPane pane = new JEditorPane("text/html",
				Resource.getString("bazi.gui.bmm.description"));
		pane.setEditable(false);
		pane.setBackground(master.getBackground());
		pane.setPreferredSize(new Dimension(800, 230));
		pane.setBorder(new EtchedBorder());
		master.add(pane, c);

		return master;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == b_Ok)
		{
			String fehler = "Base";
			try
			{
				if (!rb_None.isSelected())
				{
					int i1 = Integer.parseInt(tf_base.getText().trim());
					fehler = "Min";
					int i2 = Integer.parseInt(tf_min.getText().trim());
					fehler = "Max";
					int i3 = tf_max.getText().trim().equals("oo") ? Integer.MAX_VALUE : Integer.parseInt(tf_max.getText().trim());
					rf.setBMM(true, rb_BMMP.isSelected(), new int[]
					{ i1, i2, i3 }, false);
				}
				else
				{
					rf.setBMM(false, false, null, true);
				}
				dispose();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "Fehlerhafte Eingabe: " + fehler, Resource.getString("bazi.error.title"), JOptionPane.WARNING_MESSAGE);
			}
		}
		else if (e.getSource() == b_Cancel)
		{
			dispose();
		}
	}
}
