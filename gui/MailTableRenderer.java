/*  MailTableRenderer.java - The renderer for the mailtable
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
package org.gjt.fredde.yamm.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.Utilities;

/**
 * The renderer for the mailtable
 * @author Fredrik Ehnbom
 * @version $Id: MailTableRenderer.java,v 1.2 2000/07/16 17:48:36 fredde Exp $
 */
public class MailTableRenderer extends DefaultTableCellRenderer {

	/**
	 * Used for various stuff
	 */
	private YAMM yamm;

	/**
	 * Creates a new MailTableRenderer
	 */
	public MailTableRenderer(YAMM yamm) {
		super();
		this.yamm = yamm;
	}

	public Component getTableCellRendererComponent(
				JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column) {

		setValue(value);

		Vector v = (Vector) yamm.listOfMails.elementAt(row);
		String outbox = Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"));

		if (!v.elementAt(4).toString().equals("Unread") ||
				outbox.equals(yamm.selectedbox)) {
			setForeground(Color.black);
		} else {
			setForeground(Color.blue);
		}

		if (isSelected) {
			setBackground(new Color(204, 204, 255));
		} else {
			setBackground(Color.white);
		}

		return this;
	}
}
/*
 * Changes:
 * $Log: MailTableRenderer.java,v $
 * Revision 1.2  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.1  2000/03/19 17:18:33  fredde
 * The renderer that used to be in main/mainTable.java
 *
 */
