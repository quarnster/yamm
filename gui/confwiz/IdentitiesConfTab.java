/*  IdentitiesConfTab.java - The configurationtab for the profiles
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
import java.io.*;
import java.util.Properties;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.Profile;
import org.gjt.fredde.yamm.YAMM;

/**
 * The configurationtab for the profiles
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: IdentitiesConfTab.java,v 1.4 2000/04/11 13:27:39 fredde Exp $
 */
public class IdentitiesConfTab extends JPanel {

	/**
	 * The profilelist in Vector form
	 */
	private Vector vect2 = new Vector();

	/**
	 * The list of profiles
	 */
	private JList profileList = null;

	/**
	 * The parent JDialog
	 */
	private JDialog frame;

	/**
	 * Creates a new IdentitiesConfTab
	 */
	public IdentitiesConfTab(JDialog frame) {
		super(new BorderLayout());

		this.frame = frame;
		Box vert = Box.createVerticalBox();
    
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

		Profile[] profiles = YAMM.profiler.getProfiles();

		for (int i = 0; i < profiles.length; i++) {
			vect1.add(profiles[i].email);
			vect1.add("" + i);
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

		profileList = new JList(dataModel);

		JScrollPane JSPane = new JScrollPane(profileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JSPane.setMaximumSize(new Dimension(300,300));
		JSPane.setMinimumSize(new Dimension(300,300));

		add("Center", JSPane);
		add("East", vert);

		ComboBoxModel cmodel = new DefaultComboBoxModel() {
			public int getSize() {
				return vect2.size();
			}
			public Object getElementAt(int index) {
				return ((Vector)vect2.elementAt(index)).elementAt(0);
			}
		};

		Box hori = Box.createHorizontalBox();

		final JComboBox def = new JComboBox(cmodel);
		def.setSelectedIndex(YAMM.profiler.getDefault());
		def.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				YAMM.profiler.props.setProperty("profile.default", "" + def.getSelectedIndex());
				YAMM.profiler.save();
			}
		});
		hori.add(new JLabel(YAMM.getString("confwiz.ident.default")));
		hori.add(def);
		add("South", hori);
	}

	/**
	 * The ActionListener for the buttons
	 */
	ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String text = ((JButton) e.getSource()).getText();

			int idx = profileList.getSelectedIndex();

			if (text.equals(YAMM.getString("button.add"))) {
				ProfileEditor s = new ProfileEditor(frame);
	
				if (s.changed) {

					vect2.clear();
					Vector vect1 = new Vector();

					Profile[] profiles = YAMM.profiler.getProfiles();

					for (int i = 0; i < profiles.length; i++) {
						vect1.add(profiles[i].email);
						vect1.add("" + i);
						vect2.add(vect1);
						vect1 = new Vector();
					}

					profileList.updateUI();
				}
			} else if (text.equals(YAMM.getString("edit")) && idx != -1) {
				String file = ((Vector) vect2.elementAt(idx)).elementAt(1).toString();

				ProfileEditor s = new ProfileEditor(frame, file);
				if (s.changed) {

					vect2.clear();
					Vector vect1 = new Vector();
					Profile[] profiles = YAMM.profiler.getProfiles();

					for (int i = 0; i < profiles.length; i++) {
						vect1.add(profiles[i].email);
						vect1.add("" + i);
						vect2.add(vect1);
						vect1 = new Vector();
					}

					profileList.updateUI();
				}
			} else if (idx != -1) {
				profileList.setSelectedIndex(-1);
				String file = ((Vector) vect2.elementAt(idx)).elementAt(1).toString();
				YAMM.profiler.remove(file);
				vect2.remove(idx);
				profileList.updateUI();
			}
		}
	};
}
/*
 * ChangeLog:
 * $Log: IdentitiesConfTab.java,v $
 * Revision 1.4  2000/04/11 13:27:39  fredde
 * confwiz.ident.default -> YAMM.getString("...
 *
 * Revision 1.3  2000/04/01 20:51:21  fredde
 * fixed to make the profiling system work
 *
 */
