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

import org.gjt.fredde.yamm.mail.IndexEntry;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.Utilities;

/**
 * The renderer for the mailtable
 * @author Fredrik Ehnbom
 * @version $Id: MailTableRenderer.java,v 1.5 2003/03/09 17:47:59 fredde Exp $
 */
public class MailTableRenderer
	extends DefaultTableCellRenderer
{
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
		int column
	) {
		setValue(value);

		String outbox = Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"));

		if ((yamm.listOfMails[yamm.keyIndex[row]].status & IndexEntry.STATUS_READ) != 0 || outbox.equals(yamm.selectedbox)) {
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
 * Revision 1.5  2003/03/09 17:47:59  fredde
 * now uses the new index system
 *
 * Revision 1.4  2003/03/05 15:03:53  fredde
 * now uses yamm.keyIndex
 *
 * Revision 1.3  2000/12/26 11:22:17  fredde
 * YAMM.listOfMails is now of type String[][]
 *
 * Revision 1.2  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.1  2000/03/19 17:18:33  fredde
 * The renderer that used to be in main/mainTable.java
 *
 */
