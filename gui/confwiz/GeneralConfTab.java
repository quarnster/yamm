/*  GeneralConfTab.java - Configurationtab for the genralsettings
 *  Copyright (C) 2000 Fredrik Ehnbom
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.gjt.fredde.yamm.gui.confwiz;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import javax.swing.*;

import org.gjt.fredde.yamm.YAMM;

/**
 * The configurationtab for the generalsettings
 * @author Fredrik Ehnbom
 * @version $Id: GeneralConfTab.java,v 1.1 2000/02/28 13:49:33 fredde Exp $
 */
public class GeneralConfTab extends JPanel {

	/**
	 * Creates a new GeneralConfTab
	 */
	public GeneralConfTab() {
		super();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbs = new GridBagConstraints();
		setLayout(gb);

		gbs.anchor    = gbs.WEST;
		gbs.gridx     = gbs.REMAINDER;
		gbs.gridwidth = gbs.REMAINDER;
		JLabel modes = new JLabel(YAMM.getString("confwiz.general.modes"));
		gb.setConstraints(modes, gbs);
		add(modes);

		ButtonGroup buttonModes = new ButtonGroup();
		JRadioButton rb = new JRadioButton(YAMM.getString("confwiz.general.textmode"),
					YAMM.getProperty("button.mode", "both").equals("text"));
		rb.addActionListener(listener);
		buttonModes.add(rb);
		gb.setConstraints(rb, gbs);
		add(rb);

		rb = new JRadioButton(YAMM.getString("confwiz.general.imagemode"),
					YAMM.getProperty("button.mode", "both").equals("icon"));
		rb.addActionListener(listener);
		buttonModes.add(rb);
		gb.setConstraints(rb, gbs);
		add(rb);

		rb = new JRadioButton(YAMM.getString("confwiz.general.bothmode"),
					YAMM.getProperty("button.mode", "both").equals("both"));
		rb.addActionListener(listener);
		buttonModes.add(rb);
		gb.setConstraints(rb, gbs);
		add(rb);

		JLabel plaf = new JLabel(YAMM.getString("confwiz.general.plaf"));
//		gbs.gridx = gbs.RELATIVE;
		gbs.gridwidth = gbs.RELATIVE;
		gb.setConstraints(plaf, gbs);
		add(plaf);

		final JTextField plafField = new JTextField(YAMM.getProperty("plaf", ""));
		plafField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("plaf", plafField.getText());
			}
		});
		gbs.gridx = gbs.RELATIVE;
		gbs.fill  = gbs.HORIZONTAL;
		gbs.gridwidth = gbs.REMAINDER;
		gbs.weightx = 0.1;
		gb.setConstraints(plafField, gbs);
		add(plafField);
	}


	/**
	 * The ActionListener for the radiobuttons
	 */
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String text = ((JRadioButton) e.getSource()).getText();

			if (text.equals(YAMM.getString("confwiz.general.textmode")))
				YAMM.setProperty("button.mode", "text");
			else if (text.equals(YAMM.getString("confwiz.general.imagemode")))
				YAMM.setProperty("button.mode", "icon");
			else
				YAMM.setProperty("button.mode", "both");
		}
	};

}
/*
 * Changes:
 * $Log: GeneralConfTab.java,v $
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
