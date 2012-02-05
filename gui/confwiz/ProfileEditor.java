/*  $Id: ProfileEditor.java,v 1.3 2003/03/15 19:34:48 fredde Exp $
 *  Copyright (C) 2000-2003 Fredrik Ehnbom
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
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Properties;
import javax.swing.*;

import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.Profile;
import org.gjt.fredde.yamm.YAMM;


/**
 * Editor for profilesettings
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Revision: 1.3 $
 */
public class ProfileEditor
	extends JDialog
{

	/**
	 * Wheter or not we made a change
	 */
	public boolean changed = false;

	private int num = -1;

	private JTextField name;
	private JTextField email;
	private JTextField reply;
	private JTextField signature;

	/**
	 * The properties
	 */
	private Properties props = null;

	public ProfileEditor(JDialog owner) {
		this(owner, null);
	}

	/**
	 * Creates a new ProfileEditor.
	 * Use this when you want to configure an existing profile
	 */
	public ProfileEditor(JDialog owner, String file) {
		super(owner, true);
		Dimension screen = getToolkit().getScreenSize();
		setBounds((screen.width - 300)/2, (screen.height - 250)/2, 300, 250);

//		getContentPane().setLayout(new GridLayout(5, 2, 10, 10));
		getContentPane().setLayout(new GridLayout(9, 1, 2, 2));

		getContentPane().add(new JLabel(YAMM.getString("options.name") + ":"));

		name = new JTextField(); // YAMM.getProperty("username", System.getProperty("user.name")));
		name.setMaximumSize(new Dimension(400, 20));
		name.setMinimumSize(new Dimension(200, 20));
		name.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("username", name.getText());
			}
		});
		getContentPane().add(name);

		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch(UnknownHostException uhe) {
			host = "unknown.org";
		}

		getContentPane().add(new JLabel("Email:"));

		email = new JTextField(); //YAMM.getProperty("email", System.getProperty("user.name") + "@" + host));
		email.setMaximumSize(new Dimension(400, 20));
		email.setMinimumSize(new Dimension(200, 20));
		getContentPane().add(email);

		getContentPane().add(new JLabel(YAMM.getString("mail.reply_to")));

		reply = new JTextField();
		reply.setMaximumSize(new Dimension(400, 20));
		reply.setMinimumSize(new Dimension(200, 20));
		getContentPane().add(reply);

		Box hori = Box.createHorizontalBox();

		getContentPane().add(new JLabel(YAMM.getString("options.signatur") + ":"));
		signature = new JTextField();
		signature.setMaximumSize(new Dimension(400, 90));
		signature.setMinimumSize(new Dimension(190, 90));

//		signature.setText(YAMM.getProperty("signatur"));

		hori.add(signature);

		hori.add(Box.createRigidArea(new Dimension(5, 5)));

		JButton b = new JButton(YAMM.getString("button.browse"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.png"));
		b.setMaximumSize(new Dimension(230, 1000)); 
		b.setMinimumSize(new Dimension(50, 1000));
		b.setPreferredSize(b.getMinimumSize());
		b.addActionListener(BListener);                                    
		hori.add(b);
		getContentPane().add(hori);

		hori = Box.createHorizontalBox();


		b = new JButton(YAMM.getString("button.ok"), new ImageIcon(getClass().getResource("/images/buttons/ok.png")));
		b.addActionListener(BListener);
		hori.add(b);

		b = new JButton(YAMM.getString("button.cancel"), new ImageIcon(getClass().getResource("/images/buttons/cancel.png")));
		b.addActionListener(BListener);
		hori.add(b);
		getContentPane().add(hori);

		if (file != null) {
			load(Integer.parseInt(file));
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
			}
		});

		show();
	}

	/**
	 * Loads properties for the specified profile
	 * @param file The profile to load properties from
	 */
	private void load(int num) {
		Profile p = YAMM.profiler.getProfiles()[num];

		this.num = num;

		name.setText(p.name);
		email.setText(p.email);
		reply.setText(p.reply);
		signature.setText(p.sign);
	}

	/**
	 * Saves the config
	 */
	private void save() {
		if (num == -1) {
			YAMM.profiler.add(name.getText(), email.getText(),
					reply.getText(), signature.getText());
			YAMM.profiler.reload();
		} else {
			YAMM.profiler.props.setProperty("profile." + num + ".name", name.getText());
			YAMM.profiler.props.setProperty("profile." + num + ".email", email.getText());
			YAMM.profiler.props.setProperty("profile." + num + ".reply", reply.getText());
			YAMM.profiler.props.setProperty("profile." + num + ".sign", signature.getText());
			YAMM.profiler.reload();
		}
	}

	private ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String which = ((JButton) e.getSource()).getText();

			if (which.equals(YAMM.getString("button.ok"))) {
				save();
				changed = true;
				dispose();
			} else if (which.equals(YAMM.getString("button.cancel"))) {
				dispose();
			} else {
				JFileChooser jfs = new JFileChooser();
				jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int ret = jfs.showOpenDialog(ProfileEditor.this);

				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = jfs.getSelectedFile();

					if (file != null) {
						signature.setText(file.toString());
					}
				}

			}
		}
	};
}
/*
 * ChangeLog:
 * $Log: ProfileEditor.java,v $
 * Revision 1.3  2003/03/15 19:34:48  fredde
 * .gif -> .png
 *
 * Revision 1.2  2003/03/08 18:10:47  fredde
 * now the browse button actually does something :)
 *
 * Revision 1.1  2000/04/01 20:52:14  fredde
 * the profile editor
 *
 */
