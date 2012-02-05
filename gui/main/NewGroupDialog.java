/*  NewGroupDialog.java - A dialog that creates a new group
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

package org.gjt.fredde.yamm.gui.main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.gjt.fredde.util.gui.*;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.Utilities;


/**
 * Class used for creating new groups
 * @author Fredrik Ehnbom
 * @version $Id: NewGroupDialog.java,v 1.5 2003/03/12 20:20:35 fredde Exp $
 */
public class NewGroupDialog extends JDialog {
	private JComboBox  group = null;
	private JTextField jtfield = null;
	private YAMM yamm = null;

	public NewGroupDialog(YAMM yamm) {
		super(yamm, true);

		this.yamm = yamm;
		setBounds(0, 0, 300, 100);
		setTitle(YAMM.getString("title.new.group"));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
		getContentPane().setLayout(new GridLayout(3, 2));

		getContentPane().add(new JLabel(YAMM.getString("options.group")));
		Vector vect = new Vector();
		createGroupList(vect, new File(Utilities.replace(YAMM.home + "/boxes/")));

		NewBoxDialog.removeDotG(vect);
		group = new JComboBox( vect );
		getContentPane().add(group);

		getContentPane().add(new JLabel(YAMM.getString("options.name")));
		jtfield = new JTextField();
		getContentPane().add(jtfield);

		JButton b = new JButton(YAMM.getString("button.ok"));
		b.addActionListener(BListener);
		getContentPane().add(b);

		b = new JButton(YAMM.getString("button.cancel"));
		b.addActionListener(BListener);
		getContentPane().add(b);

		show();
	}

	/**
	 * Creates a list of all the groups
	 */
	public static void createGroupList(Vector vect, File f) {
//		String sep = YAMM.sep;
		String home = YAMM.home;

		if ((f.toString()).equals(Utilities.replace(home + "/boxes"))) {
			vect.add(System.getProperty("file.separator"));
			String list[] = f.list();
			for (int i = 0; i < list.length; i++)
				createGroupList(vect, new File(f, list[i]));
		} else if (f.isDirectory() && f.toString().endsWith(".g")) {
			String dir = f.toString();
			dir = dir.substring((Utilities.replace(home + "/boxes")).length(), dir.length() -2);
			vect.add(dir);

			String list[] = f.list();
			for (int i = 0; i < list.length; i++)
				createGroupList(vect, new File(f, list[i]));
		}
	}

	private ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String arg = ((JButton)e.getSource()).getText();
			String sep = System.getProperty("file.separator");

			if (arg.equals(YAMM.getString("button.ok"))) {
				String gName = group.getSelectedItem().toString();
				String temp = "";

				if (!gName.equals(sep)) {
					StringTokenizer tok = new StringTokenizer(gName, sep);

					while (tok.hasMoreTokens()) {
						temp +=  tok.nextToken() + ".g" + sep;
					}
					gName = temp;
				}
				File box = new File(Utilities.replace(YAMM.home + "/boxes/" + gName + jtfield.getText() + ".g"));

				if (box.exists()) {
					JOptionPane.showMessageDialog(
						yamm,
						YAMM.getString("msg.file.exists"),
						YAMM.getString("msg.error"),
						JOptionPane.ERROR_MESSAGE
					);
				} else {
					if (!box.mkdir()) {
						Object[] args = { box.toString() };
						JOptionPane.showMessageDialog(
							yamm,
							YAMM.getString("msg.file.create-error",args),
							YAMM.getString("msg.error"),
							JOptionPane.ERROR_MESSAGE
						);
					}

					yamm.tree.updateNodes();
					dispose();
				}
			} else if (arg.equals(YAMM.getString("button.cancel"))) {
				dispose();
			}
		}
	};
}
/*
 * Changes:
 * $Log: NewGroupDialog.java,v $
 * Revision 1.5  2003/03/12 20:20:35  fredde
 * removed MsgDialog
 *
 * Revision 1.4  2001/05/24 08:08:55  fredde
 * Changed some strings from YAMM.home + "/.yamm/..."
 * to just YAMM.home + "/...", thanks to wYRd
 *
 * Revision 1.3  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.2  2000/03/15 14:03:54  fredde
 * oops, forgot the log and id tags...
 *
 */
