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


/**
 * Class used for creating new groups
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
		createGroupList(vect, new File(System.getProperty("user.home") + "/.yamm/boxes/"));

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
		String sep = YAMM.sep;
		String home = YAMM.home;

		if ((f.toString()).equals(home + sep + "boxes")) {
			vect.add(sep);
			String list[] = f.list();
			for (int i = 0; i < list.length; i++)
				createGroupList(vect, new File(f, list[i]));
		} else if (f.isDirectory() && f.toString().endsWith(".g")) {
			String dir = f.toString();
			dir = dir.substring((home + sep + "boxes").length(), dir.length() -2);
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
				File box = new File(System.getProperty("user.home") +
							"/.yamm/boxes/" +
							gName +
							jtfield.getText() + ".g");

				if (box.exists()) {
					new MsgDialog(yamm, YAMM.getString("msg.error"),
						YAMM.getString("msg.file.exists"));
				} else {
					if (!box.mkdir()) {
						Object[] args = { box.toString() };
						new MsgDialog(yamm, YAMM.getString("msg.error"),
							YAMM.getString("msg.file.create-error",args));
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
