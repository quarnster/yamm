/*  ServerEditor.java - Editor for the serversettings
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
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;
import javax.swing.*;

import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.YAMM;


/**
 * Editor for the serversettings
 * @author Fredrik Ehnbom
 * @version $Id: ServerEditor.java,v 1.3 2003/03/15 19:34:48 fredde Exp $
 */
public class ServerEditor extends JDialog {

	/**
	 * Wheter or not this is a new server
	 */
	private boolean newServer = true;

	/**
	 * Wheter or not we made a change
	 */
	public boolean changed = false;

	/**
	 * Wheter or not we have finished configuration
	 */
	private boolean done = false;

	/**
	 * The textfield for the username
	 */
	private JTextField userField;

	/**
	 * The textfield for the password
	 */
	private JPasswordField JPField;

	/**
	 * The textfield for the address
	 */
	private JTextField addrField;

	/**
	 * The textfield for the port
	 */
	private JTextField portField;

	/**
	 * Cryptation...
	 */
	private SimpleCrypt crypt = new SimpleCrypt("myKey");

	/**
	 * The properties
	 */
	private Properties props = null;

	/**
	 *  The name of the server
	 */
	private String confName = null;

	public ServerEditor(JDialog owner) {
		this(owner, null);
	}

	/**
	 * Creates a new ServerEditor.
	 * Use this when you want to configure an existing server
	 */
	public ServerEditor(JDialog owner, String file) {
		super(owner, true);
		Dimension screen = getToolkit().getScreenSize();
		setBounds((screen.width - 300)/2, (screen.height - 175)/2, 300, 175);

		getContentPane().setLayout(new GridLayout(5, 2, 10, 10));

		JLabel myLabel = new JLabel(YAMM.getString("options.address") + ":");
		getContentPane().add(myLabel);

		addrField = new JTextField();
		addrField.setMaximumSize(new Dimension(200, 20));
		addrField.setMinimumSize(new Dimension(200, 20));
		getContentPane().add(addrField);

		myLabel = new JLabel(YAMM.getString("options.port") + ":");
		getContentPane().add(myLabel);

		portField = new JTextField();
		portField.setText("110");
		portField.setMaximumSize(new Dimension(200, 20));
		portField.setMinimumSize(new Dimension(200, 20));
		getContentPane().add(portField);

		myLabel = new JLabel(YAMM.getString("options.username") + ": ");
		getContentPane().add(myLabel);

		userField = new JTextField();
		userField.setMaximumSize(new Dimension(200, 20));
		userField.setMinimumSize(new Dimension(200, 20));

		getContentPane().add(userField);

		myLabel = new JLabel(YAMM.getString("options.password") + ":");
		getContentPane().add(myLabel);

		JPField = new JPasswordField();
		JPField.setMaximumSize(new Dimension(200, 20));
		JPField.setMinimumSize(new Dimension(200, 20));

		getContentPane().add(JPField);

		JButton b = new JButton(YAMM.getString("button.ok"), new ImageIcon(getClass().getResource("/images/buttons/ok.png")));
		b.addActionListener(BListener);
		getContentPane().add(b);

		b = new JButton(YAMM.getString("button.cancel"), new ImageIcon(getClass().getResource("/images/buttons/cancel.png")));
		b.addActionListener(BListener);
		getContentPane().add(b);

		if (file != null) load(file);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
			}
		});

		show();
	}

	/**
	 * Loads properties from the specified file
	 * @param file The file to load properties from
	 */
	private void load(String file) {
		newServer = false;
		confName = file;
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			props = new Properties();
			props.load(in);
			in.close();
		} catch (IOException propsioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					propsioe, YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ioe) {}
		}

		addrField.setText(props.getProperty("server"));
		portField.setText(props.getProperty("port", "110"));
		userField.setText(props.getProperty("username"));
		JPField.setText(crypt.decrypt(props.getProperty("password")));
	}

	/**
	 * Saves the config
	 */
	private void save() {
		String config = confName; // YAMM.home + "/servers/" + addrField.getText();
		int tmp = 0;

		if (newServer) {
			config = YAMM.home + "/servers/" + addrField.getText();
			while (new File(config).exists()) {
				tmp++;
				config = YAMM.home + "/servers/" + addrField.getText() + tmp;
			}
		}

		OutputStream out = null;
		try {
			out = new FileOutputStream(config);
			props = new Properties();

			props.setProperty("server",	addrField.getText());
			props.setProperty("port",	portField.getText());
			props.setProperty("username",	userField.getText());
			props.setProperty("password",	crypt.encrypt(new String(JPField.getPassword())));
			props.setProperty("type",	"pop3");

			props.store(out, "Server Config file");
		} catch (IOException propsioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					propsioe, YAMM.exceptionNames);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {}
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
			}
		}
	};
}
/*
 * Changes:
 * $Log: ServerEditor.java,v $
 * Revision 1.3  2003/03/15 19:34:48  fredde
 * .gif -> .png
 *
 * Revision 1.2  2000/03/18 14:55:56  fredde
 * the server editor now works
 *
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
