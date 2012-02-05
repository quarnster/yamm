/*  DebugConfTab.java - The configurationtab for debugsettings
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

import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import org.gjt.fredde.yamm.YAMM;

/**
 * DebugConfTab is used for configuration debugsettings
 * @author Fredrik Ehnbom
 * @version $Id: DebugConfTab.java,v 1.2 2000/03/05 18:05:24 fredde Exp $
 */
public class DebugConfTab extends JPanel {

	/**
	 * Creates the DebugConfTab
	 */
	public DebugConfTab() {
		super();
		Box b = Box.createVerticalBox();

		JCheckBox cb = new JCheckBox(YAMM.getString("confwiz.debug.pop"), YAMM.getProperty("debug.pop", "false").equals("true"));
		cb.addActionListener(listener);
		b.add(cb);

		cb = new JCheckBox(YAMM.getString("confwiz.debug.smtp"), YAMM.getProperty("debug.smtp", "false").equals("true"));
		cb.addActionListener(listener);
		b.add(cb);

		cb = new JCheckBox(YAMM.getString("confwiz.debug.cachedel"), YAMM.getProperty("debug.cachedel", "false").equals("true"));
		cb.addActionListener(listener);
		b.add(cb);

		Box b2 = Box.createHorizontalBox();
		b2.add(new JLabel(YAMM.getString("confwiz.debug.file")));

		final JTextField jt = new JTextField(YAMM.getProperty("debug.file", ""));
		jt.setMaximumSize(new Dimension(1024, 20));
		jt.setMinimumSize(new Dimension(50, 20));
		jt.setPreferredSize(new Dimension(300, 20));
		jt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("debug.file", jt.getText());
			}
		});
		b2.add(jt);
		b.add(b2);

		add(b);
	}

	/**
	 * The ActionListener for the checkboxes
	 */
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JCheckBox src = (JCheckBox) e.getSource();
			String msg = src.getText();

			if (msg.equals(YAMM.getString("confwiz.debug.smtp"))) {
				if (src.isSelected())
					YAMM.setProperty("debug.smtp", "true");
				else
					YAMM.setProperty("debug.smtp", "false");
			} else if (msg.equals(YAMM.getString("confwiz.debug.pop"))) {
				if (src.isSelected())
					YAMM.setProperty("debug.pop", "true");
				else
					YAMM.setProperty("debug.pop", "false");
			} else if (msg.equals(YAMM.getString("confwiz.debug.cachedel"))) {
				if (src.isSelected())
					YAMM.setProperty("debug.cachedel", "true");
				else
					YAMM.setProperty("deubg.cachedel", "false");
			}
		}
	};
}
/*
 * Changes:
 * $Log: DebugConfTab.java,v $
 * Revision 1.2  2000/03/05 18:05:24  fredde
 * added checkbox for deletion of cached files
 *
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
