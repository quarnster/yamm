/*  BoxTreeRenderer.java - The renderer for the boxtree
 *  Copyright (C) 1999, 2000 Fredrik Ehnbom
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
 * @version $Id: BoxTreeRenderer.java,v 1.7 2000/12/26 11:22:00 fredde Exp $
 */
public class BoxTreeRenderer
	extends JLabel
	implements TreeCellRenderer
{
	/** Whether or not the item that was last configured is selected. */
	protected boolean selected;

	/** The icon for the inbox */
	public final ImageIcon inbox = new ImageIcon(getClass().getResource("/images/boxes/inbox.gif"));

	/** The icon for the outbox */
	public final ImageIcon outbox = new ImageIcon(getClass().getResource("/images/boxes/outbox.gif"));

	/** The icon for the trashcan */
	public final ImageIcon trash = new ImageIcon(getClass().getResource("/images/boxes/trash.gif"));

	/** The icon for other boxes */
	public final ImageIcon box = new ImageIcon(getClass().getResource("/images/boxes/box.gif"));

	/** The icon for closed dirs */
	public final ImageIcon dir = new ImageIcon(getClass().getResource("/images/boxes/dir.gif"));

	/** The icon for open dirs */
	public final ImageIcon dir2 = new ImageIcon(getClass().getResource("/images/boxes/dir2.gif"));

	public Component getTreeCellRendererComponent(
		JTree   tree,
		Object  value,
		boolean selected,
		boolean expanded,
		boolean leaf,
		int     row,
		boolean hasFocus
	) {
		String s = value.toString();
		StringTokenizer tok = new StringTokenizer(s, System.getProperty("file.separator"));
		String thisbox = null;

		while (tok.hasMoreTokens()) {
			thisbox = tok.nextToken();
		}

		if (leaf && !thisbox.endsWith(".g")) {
			if (thisbox.equals(YAMM.getString("box.inbox"))) {
				setIcon(inbox);
			} else if (thisbox.equals(YAMM.getString("box.outbox"))	|| thisbox.equals(YAMM.getString("box.sent"))) {
				setIcon(outbox);
			} else if (thisbox.equals(YAMM.getString("box.trash"))) {
				setIcon(trash);
			} else setIcon(box);
		} else if (!leaf) {
			if(expanded) {
				setIcon(dir2);
			} else setIcon(dir);
		} else setIcon(dir);

		if (thisbox.endsWith(".g")) {
			setText(thisbox.substring(0, thisbox.length() -2));
		} else if (!thisbox.equals(YAMM.getString("box.boxes")) && !thisbox.equals(YAMM.getString("box.outbox"))) {
			int unread[] = Mailbox.getUnread(s);
			if (unread[1] != -1 && unread[1] != 0 && YAMM.getProperty("showunread", "true").equals("true")) thisbox += " " + unread[1] + "/" + unread[0];
			setText(thisbox);
		} else {
			setText(thisbox);
		}

		setForeground(Color.black);
		this.selected = selected;
		return this;
	}

	/**
	 * paint is subclassed to draw the background correctly.  JLabel
	 * currently does not allow backgrounds other than white, and it
	 * will also fill behind the icon.  Something that isn't desirable.
	 */
	public void paint(Graphics g) {
		Color            bColor;
		Icon             currentI = getIcon();

		if (selected) {
			bColor = new Color(204, 204, 255);
		} else if (getParent() != null) {
			// Pick background color up from parent (which will
			// come from the JTree we're contained in).
			bColor = getParent().getBackground();
		} else {
			bColor = getBackground();
		}

		g.setColor(bColor);

		if (currentI != null && getText() != null) {
			int offset = (currentI.getIconWidth() +
							getIconTextGap());

			g.fillRect(offset, 0, getWidth() - 1 - offset,
							getHeight() - 1);
		} else {
			g.fillRect(0, 0, getWidth()-1, getHeight()-1);
		}
		super.paint(g);
	}
}
/*
 * Changes:
 * $Log: BoxTreeRenderer.java,v $
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
