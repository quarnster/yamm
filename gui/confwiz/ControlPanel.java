/*  ControlPanel.java - Control panel for the Configuration Wizard
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

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

import org.gjt.fredde.yamm.YAMM;

/**
 * The ControlPanel contains buttons to control the ConfigurationWizard
 * @author Fredrik Ehnbom
 * @version $Id: ControlPanel.java,v 1.2 2000/03/18 17:07:27 fredde Exp $
 */
public class ControlPanel extends JPanel {

	/**
	 * The ConfigurationWizard that owns this ControlPanel
	 */
	private ConfigurationWizard wiz;

	/**
	 * Creates a new ControlPanel
	 * @param jtpane The JTabbedPane to control
	 */
	public ControlPanel(ConfigurationWizard wiz) {
		super();
		this.wiz = wiz;

		setLayout(new BorderLayout());

		JButton b = new JButton(YAMM.getString("button.ok"),
				new ImageIcon(getClass().getResource("/images/buttons/ok.gif")));
		b.addActionListener(listener);
		add("East", b);
	}

	/**
	 * The ActionListener for the buttons in the ControlPanel
	 */
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			wiz.dispose();
		}
	};
}
/*
 * Changes:
 * $Log: ControlPanel.java,v $
 * Revision 1.2  2000/03/18 17:07:27  fredde
 * now have a button
 *
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
