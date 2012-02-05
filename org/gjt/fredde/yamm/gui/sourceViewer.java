/*  sourceViewer.java - A message source window
 *  Copyright (C) 1999 Fredrik Ehnbom
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

package org.gjt.fredde.yamm.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;

/**
 * A class to display the source of a message.
 * @author Fredrik Ehnbom
 * @version $Id: sourceViewer.java,v 1.3 2000/03/05 18:42:32 fredde Exp $
 */
public class sourceViewer extends JFrame {

	/** The JTextArea to be used when viewing the source */
	public JTextArea jtarea;
	public JScrollPane jsp;

	/**
	 * Creates the window
	 */
	public sourceViewer() {
		setBounds(140, 0, 550, 560);
		getContentPane().setLayout(new BorderLayout());

		jtarea = new JTextArea();
		jtarea.setEditable(false);
		jsp = new JScrollPane(jtarea);
		getContentPane().add("Center", jsp);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				dispose();
			}
		});

		show();
	}
}
/*
 * Changes:
 * $Log: sourceViewer.java,v $
 * Revision 1.3  2000/03/05 18:42:32  fredde
 * tried to make it go all the way up on fileload
 *
 */
