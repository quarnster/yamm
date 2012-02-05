/*  FiltersConfTab.java - The configurationtab for the filters
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
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.YAMM;

/**
 * The configurationtab for the filters
 *
 * @author Fredrik Ehnbom
 * @version $Id: FiltersConfTab.java,v 1.2 2003/03/15 19:34:48 fredde Exp $
 */
public class FiltersConfTab
	extends JPanel
{
	/**
	 * The filterlist in Vector form
	 */
	private Vector filterVector = new Vector();

	/**
	 * The filers rows
	 */
	private Vector filterRows = new Vector();

	/**
	 * The list of filters
	 */
	private JList filterList = null;

	/**
	 * The parent JDialog
	 */
	private JDialog frame;

	/**
	 * Creates a new FiltersConfTab
	 */
	public FiltersConfTab(JDialog frame) {
		super();
		setLayout(new BorderLayout());

		this.frame = frame;
		Box vert = Box.createVerticalBox();

		JButton b = new JButton(YAMM.getString("button.add"), new ImageIcon(getClass().getResource("/images/buttons/new.png")));
		b.addActionListener(BListener);
		vert.add(b);
		vert.add(Box.createRigidArea(new Dimension(10, 10)));

		b = new JButton(YAMM.getString("edit"), new ImageIcon(getClass().getResource("/images/buttons/edit.png")));
		b.addActionListener(BListener);
		vert.add(b);
		vert.add(Box.createRigidArea(new Dimension(10, 10)));

		b = new JButton(YAMM.getString("button.delete"), new ImageIcon(getClass().getResource("/images/buttons/delete.png")));
		b.addActionListener(BListener);
		vert.add(b);
		vert.add(Box.createRigidArea(new Dimension(10, 10)));

		add("East", vert);


		try {
			BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(YAMM.home + "/.filters"))
			);

			String tmp = null;
			while ((tmp = in.readLine()) != null) {
				filterRows.add(tmp);
				tmp = tmp.substring(tmp.lastIndexOf(";") + 1, tmp.length());
				filterVector.add(tmp);
			}
			in.close();
		} catch (IOException propsioe) {
			new ExceptionDialog(
				YAMM.getString("msg.error"),
				propsioe,
				YAMM.exceptionNames
			);
		}

		final ListModel dataModel = new AbstractListModel() {
			public final int getSize() {
				return filterVector.size();
			}
			public final Object getElementAt(int index) {
				return filterVector.elementAt(index);
			}
		};

		filterList = new JList(dataModel);

		JScrollPane JSPane = new JScrollPane(filterList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add("Center", JSPane);
	}

	/**
	 * The ActionListener for the buttons
	 */
	ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String text = ((JButton) e.getSource()).getText();

			int idx = filterList.getSelectedIndex();

			if (text.equals(YAMM.getString("button.add"))) {
				FilterEditor f = new FilterEditor(frame);

				if (f.changed) {
					String tmp = f.save();
	        			filterRows.add(tmp);
					tmp = tmp.substring(tmp.lastIndexOf(";") + 1, tmp.length());
			        	filterVector.add(tmp);

					save();
					filterList.updateUI();
					f.dispose();
				}
			} else if (text.equals(YAMM.getString("edit")) && idx != -1) {
				FilterEditor f = new FilterEditor(frame, filterRows.get(idx).toString());

				if (f.changed) {
					String tmp = f.save();
	        			filterRows.set(idx, tmp);
					tmp = tmp.substring(tmp.lastIndexOf(";") + 1, tmp.length());
			        	filterVector.set(idx, tmp);

					save();
					filterList.updateUI();
					f.dispose();
				}
			} else if (idx != -1) {
				filterList.setSelectedIndex(-1);
				filterVector.remove(idx);
				filterRows.remove(idx);

				save();
				filterList.updateUI();
			}
		}

		private final void save() {
		        PrintWriter out = null;
			try {
				out = new PrintWriter(
					new BufferedOutputStream(
						new FileOutputStream(YAMM.home + "/.filters")
					)
				);

				Enumeration e = filterRows.elements();
				while (e.hasMoreElements()) {
					out.println(e.nextElement());
				}
				out.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				try {
					if (out != null) out.close();
				} catch (Throwable t) {}
			}
		}
	};
}
/*
 * ChangeLog:
 * $Log: FiltersConfTab.java,v $
 * Revision 1.2  2003/03/15 19:34:48  fredde
 * .gif -> .png
 *
 * Revision 1.1  2000/12/31 14:07:38  fredde
 * files for configurating filters
 *
 */
