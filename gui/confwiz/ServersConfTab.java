/*  ServerConfTab.java - The configurationtab for the serversettings
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
import java.io.*;
import java.util.Properties;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.YAMM;

/**
 * The configurationtab for the serversettings
 * @author Fredrik Ehnbom
 * @version $Id: ServersConfTab.java,v 1.3 2000/03/18 14:55:56 fredde Exp $
 */
public class ServersConfTab extends JPanel {

	/**
	 * The serverlist in Vector form
	 */
	private Vector vect2 = new Vector();

	/**
	 * The list of servers
	 */
	private JList serverList = null;

	/**
	 * The parent JDialog
	 */
	private JDialog frame;

	/**
	 * Creates a new ServersConfTab
	 */
	public ServersConfTab(JDialog frame) {
		super();

		this.frame = frame;
		Box vert = Box.createVerticalBox();
		Box hori = Box.createHorizontalBox();
		Box vert2 = Box.createVerticalBox();
    
		JButton b = new JButton(YAMM.getString("button.add"), new ImageIcon(getClass().getResource("/images/buttons/new.gif")));
		b.addActionListener(BListener);
		vert.add(b);
		vert.add(Box.createRigidArea(new Dimension(10, 10)));

		b = new JButton(YAMM.getString("edit"), new ImageIcon(getClass().getResource("/images/buttons/edit.gif")));
		b.addActionListener(BListener);
		vert.add(b);
		vert.add(Box.createRigidArea(new Dimension(10, 10)));

		b = new JButton(YAMM.getString("button.delete"), new ImageIcon(getClass().getResource("/images/buttons/delete.gif")));
		b.addActionListener(BListener);
		vert.add(b);
		vert.add(Box.createRigidArea(new Dimension(10, 10)));

		Vector vect1 = new Vector();

		File[] files = new File(YAMM.home + "/servers/").listFiles();

		for(int i=0;i<files.length;i++) {
			Properties props = null;

			try {
				InputStream in = new FileInputStream(files[i]); //  + "/.config");
				props = new Properties();
				props.load(in);
				in.close();
			} catch (IOException propsioe) {
				new ExceptionDialog(YAMM.getString("msg.error"),
					propsioe, YAMM.exceptionNames);
			}
			vect1.add(props.getProperty("server"));
			vect1.add(files[i]);
			vect2.add(vect1);
			vect1 = new Vector();
		}

		ListModel dataModel = new AbstractListModel() {
			public int getSize() {
				return vect2.size();
			}
			public Object getElementAt(int index) {
				return ((Vector)vect2.elementAt(index)).elementAt(0);
			}
		};

		serverList = new JList(dataModel);

		JScrollPane JSPane = new JScrollPane(serverList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JSPane.setMaximumSize(new Dimension(300,300));
		JSPane.setMinimumSize(new Dimension(300,300));
    
		hori.add(Box.createRigidArea(new Dimension(10, 10)));
		hori.add(JSPane);
		hori.add(Box.createRigidArea(new Dimension(10, 10)));
		hori.add(vert);
		vert2.add(hori);

		hori = Box.createHorizontalBox();
		JLabel smtpserv = new JLabel(YAMM.getString("confwiz.servers.smtplabel"));
		hori.add(smtpserv);

		final JTextField jtf = new JTextField(YAMM.getProperty("smtpserver", ""));
		jtf.setMaximumSize(new Dimension(300, 20));
		jtf.setMinimumSize(new Dimension(300,20));
		jtf.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				YAMM.setProperty("smtpserver", jtf.getText());
			}
		});
		hori.add(jtf);
		vert2.add(Box.createRigidArea(new Dimension(10, 30)));
		vert2.add(hori);

		add(vert2);
	}

	/**
	 * The ActionListener for the buttons
	 */
	ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String text = ((JButton) e.getSource()).getText();

			int idx = serverList.getSelectedIndex();

			if (text.equals(YAMM.getString("button.add"))) {
				ServerEditor s = new ServerEditor(frame);
				if (s.changed) {
					vect2.clear();
					Vector vect1 = new Vector();
					File[] files = new File(YAMM.home + "/servers/").listFiles();

					for (int i = 0; i < files.length; i++) {
						Properties props = null;

						try {
							InputStream in = new FileInputStream(files[i]); //  + "/.config");
							props = new Properties();
							props.load(in);
							in.close();
						} catch (IOException propsioe) {
							new ExceptionDialog(YAMM.getString("msg.error"),
								propsioe, YAMM.exceptionNames);
						}
						vect1.add(props.getProperty("server"));
						vect1.add(files[i]);
						vect2.add(vect1);
						vect1 = new Vector();
					}
					serverList.updateUI();
				}
			} else if (text.equals(YAMM.getString("edit")) && idx != -1) {
				String file = ((Vector) vect2.elementAt(idx)).elementAt(1).toString();

				ServerEditor s = new ServerEditor(frame, file);
				if (s.changed) {
					vect2.clear();
					Vector vect1 = new Vector();
					File[] files = new File(YAMM.home + "/servers/").listFiles();

					for (int i = 0; i < files.length; i++) {
						Properties props = null;

						try {
							InputStream in = new FileInputStream(files[i]); //  + "/.config");
							props = new Properties();
							props.load(in);
							in.close();
						} catch (IOException propsioe) {
							new ExceptionDialog(YAMM.getString("msg.error"),
								propsioe, YAMM.exceptionNames);
						}
						vect1.add(props.getProperty("server"));
						vect1.add(files[i]);
						vect2.add(vect1);
						vect1 = new Vector();
					}
					serverList.updateUI();
				}
			} else if (idx != -1) {
				serverList.setSelectedIndex(-1);
				String file = ((Vector) vect2.elementAt(idx)).elementAt(1).toString();
				new File(file).delete();
				vect2.remove(idx);
				serverList.updateUI();
			}
		}
	};
}
/*
 * Changes:
 * $Log: ServersConfTab.java,v $
 * Revision 1.3  2000/03/18 14:55:56  fredde
 * the server editor now works
 *
 * Revision 1.2  2000/03/05 18:02:53  fredde
 * now gets the images used for the jar-file
 *
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
