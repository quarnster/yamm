/*  NewBoxDialog.java - A dialog that creates a new box
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

public class NewBoxDialog extends JDialog {
	private JComboBox  group;
	private JTextField jtfield;
	private YAMM yamm = null;

	public NewBoxDialog(YAMM yamm) {
		super(yamm, true);

		this.yamm = yamm;

		setBounds(0, 0, 300, 100);
		setTitle(YAMM.getString("title.new.box"));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
		getContentPane().setLayout(new GridLayout(3, 2));

		getContentPane().add(new JLabel(YAMM.getString("options.group")));
		Vector vect = new Vector();
		NewGroupDialog.createGroupList(vect, new File(YAMM.home + "/boxes/"));
		removeDotG(vect);
		group = new JComboBox(vect);
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
	 * removes .g from all the groups in the vector
	 */
 	public static void removeDotG(Vector vect) {
		for (int i = 0; i < vect.size(); i++) {
			String temp = vect.elementAt(i).toString();

			while (temp.indexOf(".g") != -1) {
				temp = temp.substring(0, temp.indexOf(".g")) + temp.substring(temp.indexOf(".g") + 2, temp.length());
			}
			vect.setElementAt(temp, i);
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

				File box = new File(YAMM.home +
						"/boxes/" +
						gName +
						jtfield.getText());

				if (box.exists()) {
					new MsgDialog(yamm, YAMM.getString("msg.error"),
						YAMM.getString("msg.file.exists"));
				} else {
					try {
						box.createNewFile();
					} catch (IOException ioe) {
						new ExceptionDialog(YAMM.getString("msg.error"),
							ioe,
							YAMM.exceptionNames);
					}

					yamm.tree.updateNodes();

					yamm.mailList.popup = new JPopupMenu();
					yamm.mailList.popup.setInvoker(yamm.mailList);
					yamm.mailList.createPopup(yamm.mailList.popup);

					dispose();
				}
			} else if (arg.equals(YAMM.getString("button.cancel"))) {
				dispose();
			}
		}
	};
}
