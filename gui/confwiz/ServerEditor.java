/*  $Id: ServerEditor.java,v 1.4 2003/04/16 12:40:17 fredde Exp $
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.YAMM;


/**
 * Editor for the serversettings
 * @author Fredrik Ehnbom
 * @version $Revision: 1.4 $
 */
public class ServerEditor
	extends JDialog
{

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

	private JComboBox type;
	private JComboBox profile;
	private JCheckBox authentication;
	private JPanel pop3Panel;
	private JCheckBox delBox;

	private JPanel cPanel;

	/**
	 * The textfield for the username
	 */
	private JTextField userField;
	private JLabel userLabel;

	/**
	 * The textfield for the password
	 */
	private JPasswordField JPField;
	private JLabel passwordLabel;

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
		setBounds((screen.width - 300)/2, (screen.height - 300)/2, 340, 340);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c1 = new GridBagConstraints();

		getContentPane().setLayout(gridbag);

		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.weightx = 1;
		c1.gridwidth = GridBagConstraints.REMAINDER;

		// Type
		JPanel panel = new JPanel();
		type = new JComboBox(new Object[] {"pop3", "smtp"});
		type.addActionListener(ComboListener);
		panel.add(type);
		panel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), YAMM.getString("confwiz.servers.type")));
		gridbag.setConstraints(panel, c1);
		getContentPane().add(panel);

		// Address
		panel = new JPanel(gridbag);
		JLabel myLabel = new JLabel(YAMM.getString("options.address") + ":");
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 0;
		gridbag.setConstraints(myLabel, c1);
		panel.add(myLabel);

		addrField = new JTextField();
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1;
		gridbag.setConstraints(addrField, c1);
		panel.add(addrField);

		myLabel = new JLabel(YAMM.getString("options.port") + ":");
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 0;
		gridbag.setConstraints(myLabel, c1);
		panel.add(myLabel);

		portField = new JTextField();
		portField.setText("110");
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1;
		gridbag.setConstraints(portField, c1);
		panel.add(portField);
		panel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), YAMM.getString("confwiz.servers.address")));
		gridbag.setConstraints(panel, c1);
		getContentPane().add(panel);

		// Authentication
		panel = new JPanel(gridbag);
		authentication = new JCheckBox(YAMM.getString("confwiz.servers.reqauth"), true);
		authentication.setEnabled(false);
		authentication.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (authentication.isSelected()) {
					userField.setEnabled(true);
					JPField.setEnabled(true);
					passwordLabel.setEnabled(true);
					userLabel.setEnabled(true);
				} else {
					userField.setEnabled(false);
					JPField.setEnabled(false);
					passwordLabel.setEnabled(false);
					userLabel.setEnabled(false);
				}
			}
		});
		c1.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(authentication, c1);

		panel.add(authentication);
		userLabel = new JLabel(YAMM.getString("options.username") + ": ");
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 0;
		gridbag.setConstraints(userLabel, c1);
		panel.add(userLabel);

		userField = new JTextField();
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1;
		gridbag.setConstraints(userField, c1);
		panel.add(userField);

		passwordLabel = new JLabel(YAMM.getString("options.password") + ":");
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 0;
		gridbag.setConstraints(passwordLabel, c1);
		panel.add(passwordLabel);

		JPField = new JPasswordField();
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.weightx = 1;
		gridbag.setConstraints(JPField, c1);
		panel.add(JPField);
		panel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), YAMM.getString("confwiz.servers.auth")));
		gridbag.setConstraints(panel, c1);
		getContentPane().add(panel);

		// pop3 specific stuff
		pop3Panel = new JPanel(gridbag);
		delBox = new JCheckBox(YAMM.getString("confwiz.servers.delete"), true);
		pop3Panel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), YAMM.getString("confwiz.servers.pop3")));	
		gridbag.setConstraints(delBox, c1);
		pop3Panel.add(delBox);
		gridbag.setConstraints(pop3Panel, c1);
		getContentPane().add(pop3Panel);

		// OK / Cancel buttons
		JButton b = new JButton(YAMM.getString("button.ok"), new ImageIcon(getClass().getResource("/images/buttons/ok.png")));
		b.addActionListener(BListener);
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 1;
		gridbag.setConstraints(b, c1);
		getContentPane().add(b);

		b = new JButton(YAMM.getString("button.cancel"), new ImageIcon(getClass().getResource("/images/buttons/cancel.png")));
		b.addActionListener(BListener);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(b, c1);
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

		String t = props.getProperty("type", "pop3");
		type.setSelectedItem(t);
		if (t.equals("pop3")) {
			delBox.setSelected(props.getProperty("delete", "true").equals("true"));
		} else {
			authentication.setSelected(props.getProperty("authentication", "false").equals("true"));
		}
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
			String t = type.getSelectedItem().toString();

			props.setProperty("type",	t);
			props.setProperty("server",	addrField.getText());
			props.setProperty("port",	portField.getText());

			if (authentication.isSelected()) {
				props.setProperty("authentication", "true");
				props.setProperty("username",	userField.getText());
				props.setProperty("password",	crypt.encrypt(new String(JPField.getPassword())));
			}
			if (t.equals("pop3")) {
				props.setProperty("delete", "" + delBox.isSelected());
			}

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

	private ActionListener ComboListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (type.getSelectedItem().toString().equals("pop3")) {
				authentication.setEnabled(false);
				authentication.setSelected(true);
				portField.setText("110");
				pop3Panel.setEnabled(true);
				delBox.setEnabled(true);
			} else if (type.getSelectedItem().toString().equals("smtp")) {
				authentication.setEnabled(true);
				pop3Panel.setEnabled(false);
				delBox.setEnabled(false);
				portField.setText("25");
			}
		}
	};

}
/*
 * Changes:
 * $Log: ServerEditor.java,v $
 * Revision 1.4  2003/04/16 12:40:17  fredde
 * updated to include smtp-configuration also
 *
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
