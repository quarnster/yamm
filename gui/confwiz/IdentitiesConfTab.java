/*  IdentitiesConfTab.java - The configurationtab for identitiessettings
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

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

import org.gjt.fredde.yamm.YAMM;

/**
 * The configurationtab for the identitessettings
 * @author Fredrik Ehnbom
 * @version $Id: IdentitiesConfTab.java,v 1.2 2000/03/18 17:07:13 fredde Exp $
 */
public class IdentitiesConfTab extends JPanel {

	/**
	 * The name textfield
	 */
	private JTextField name;

	/**
	 * The email textfield
	 */
	private JTextField email;

	/**
	 * The signature textfield
	 */
	private JTextField signatur;

	/**
	 * Creates a new IdentitesConfTab
	 */
	public IdentitiesConfTab() {
		super(new GridLayout(6, 1, 2, 2));

		add(new JLabel(YAMM.getString("options.name") + ":"));

		name = new JTextField(YAMM.getProperty("username", System.getProperty("user.name")));
		name.setMaximumSize(new Dimension(400, 20));
		name.setMinimumSize(new Dimension(200, 20));
		name.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("username", name.getText());
			}
		});
		add(name);

		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch(UnknownHostException uhe) {
			host = "unknown.org";
		}

		add(new JLabel("Email:"));

		email = new JTextField(YAMM.getProperty("email", System.getProperty("user.name") + "@" + host));
		email.setMaximumSize(new Dimension(400, 20));
		email.setMinimumSize(new Dimension(200, 20));
		email.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("email", email.getText());
			}
		});
		add(email);

		Box hori = Box.createHorizontalBox();

		add(new JLabel(YAMM.getString("options.signatur") + ":"));
		signatur = new JTextField();
		signatur.setMaximumSize(new Dimension(400, 90));
		signatur.setMinimumSize(new Dimension(190, 90));
		signatur.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("signatur", signatur.getText());
			}
		});
		signatur.setText(YAMM.getProperty("signatur"));

		hori.add(signatur);

		hori.add(Box.createRigidArea(new Dimension(5, 5)));

		JButton b = new JButton(YAMM.getString("button.browse"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.gif"));
		b.setMaximumSize(new Dimension(230, 1000)); 
		b.setMinimumSize(new Dimension(115, 1000));
		b.addActionListener(BListener);                                    
		hori.add(b);
		add(hori);
	}

	private ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfs = new JFileChooser();
			jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfs.setMultiSelectionEnabled(false);
			int ret = jfs.showOpenDialog(IdentitiesConfTab.this);

			if (ret == JFileChooser.APPROVE_OPTION) {
				if (jfs.getSelectedFile() != null) {
					signatur.setText(jfs.getSelectedFile().toString());
					YAMM.setProperty("signatur", jfs.getSelectedFile().toString());
				}
			}
		}
	};
}
/*
 * Changes:
 * $Log: IdentitiesConfTab.java,v $
 * Revision 1.2  2000/03/18 17:07:13  fredde
 * Now works...
 *
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
