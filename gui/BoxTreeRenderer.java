/*  $Id: BoxTreeRenderer.java,v 1.11 2003/03/15 20:38:37 fredde Exp $
 *  Copyright (C) 1999-2003 Fredrik Ehnbom
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
import javax.swing.tree.*;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;

/**
 * The renderer for the mailbox tree
 * @author Fredrik Ehnbom
 * @version $Revision: 1.11 $
 */
public class BoxTreeRenderer
	extends JLabel
	implements TreeCellRenderer
{
	/** The icon for the inbox */
	public final ImageIcon inbox = new ImageIcon(getClass().getResource("/images/boxes/inbox.png"));

	/** The icon for the outbox */
	public final ImageIcon outbox = new ImageIcon(getClass().getResource("/images/boxes/outbox.png"));

	/** The icon for the trashcan */
	public final ImageIcon trash = new ImageIcon(getClass().getResource("/images/boxes/trash.png"));

	/** The icon for other boxes */
	public final ImageIcon box = new ImageIcon(getClass().getResource("/images/boxes/box.png"));

	/** The icon for closed dirs */
	public final ImageIcon dir = new ImageIcon(getClass().getResource("/images/boxes/dir.png"));

	/** The icon for open dirs */
	public final ImageIcon dir2 = new ImageIcon(getClass().getResource("/images/boxes/dir2.png"));

	public Component getTreeCellRendererComponent(
		JTree   tree,
		Object  value,
		boolean selected,
		boolean expanded,
		boolean leaf,
		int     row,
		boolean hasFocus
	) {
		String thisbox = value.toString();

		if (thisbox.indexOf(System.getProperty("file.separator")) != -1) {
			thisbox = thisbox.substring(thisbox.lastIndexOf(System.getProperty("file.separator")) + System.getProperty("file.separator").length());
		}

		if (leaf && !thisbox.endsWith(".g")) {
			if (thisbox.equals("inbox")) {
				setIcon(inbox);
				thisbox = YAMM.getString("box.inbox");
			} else if (thisbox.equals("outbox")) {
				setIcon(outbox);
				thisbox = YAMM.getString("box.outbox");
			} else if (thisbox.equals("sent")) {
				setIcon(outbox);
				thisbox = YAMM.getString("box.sent");
			} else if (thisbox.equals("trash")) {
				setIcon(trash);
				thisbox = YAMM.getString("box.trash");
			} else setIcon(box);
		} else if (!leaf) {
			if(expanded) {
				setIcon(dir2);
			} else setIcon(dir);
		} else setIcon(dir);

		if (thisbox.endsWith(".g")) {
			setText(thisbox.substring(0, thisbox.length() -2));
		} else {
			setText(thisbox);
		}
		if (selected) {
			setBackground(UIManager.getColor("textHighlight"));
			setForeground(UIManager.getColor("textHighlightText"));
		} else {
			setBackground(UIManager.getColor("window"));
			setForeground(UIManager.getColor("windowText"));
		}

		return this;
	}
}
/*
 * Changes:
 * $Log: BoxTreeRenderer.java,v $
 * Revision 1.11  2003/03/15 20:38:37  fredde
 * fixed plaf stuff
 *
 * Revision 1.10  2003/03/15 19:31:35  fredde
 * .gif -> .png. get colors from UIManager
 *
 * Revision 1.9  2003/03/10 09:43:48  fredde
 * non localized box filenames
 *
 * Revision 1.8  2003/03/08 13:55:04  fredde
 * updated for the new TreeTable stuff
 *
 * Revision 1.7  2000/12/26 11:22:00  fredde
 * cleaned up a little
 *
 * Revision 1.6  2000/08/09 16:29:44  fredde
 * don't show unread messages for the outbox
 *
 * Revision 1.5  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.4  2000/03/15 11:13:45  fredde
 * boxes now show if and how many unread messages they have
 *
 * Revision 1.3  2000/03/05 18:02:53  fredde
 * now gets the images used for the jar-file
 *
 */
