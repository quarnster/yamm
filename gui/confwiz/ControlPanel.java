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

import javax.swing.*;
import java.awt.event.*;

/**
 * The ControlPanel contains buttons to control the ConfigurationWizard
 * @author Fredrik Ehnbom
 * @version $Id: ControlPanel.java,v 1.1 2000/02/28 13:49:33 fredde Exp $
 */
public class ControlPanel extends JPanel {

	/**
	 * Creates a new ControlPanel
	 * @param jtpane The JTabbedPane to control
	 */
	public ControlPanel(JTabbedPane jtpane) {
		super();
	}

	/**
	 * The ActionListener for the buttons in the ControlPanel
	 */
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		}
	};
}
/*
 * Changes:
 * $Log: ControlPanel.java,v $
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
