/*  $Id: TreeTableCellRenderer.java,v 1.2 2003/03/15 19:32:39 fredde Exp $
 *  Copyright (C) 2003 Fredrik Ehnbom
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
 * A Tree TableCellRenderer 
 * @author Fredrik Ehnbom
 * @version $Revision: 1.2 $
 */
public class TreeTableCellRenderer
	extends JTree
	implements TableCellRenderer
{
	private int row;
	private JTable table;

	public TreeTableCellRenderer(JTable table) {
		this.table = table;
	}

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, 0, w, table.getHeight());
	}


	public void paint(Graphics g) {
		g.translate(0, -row * getRowHeight());
	    	super.paint(g);
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column
	) {
		if (isSelected) {
			setBackground(UIManager.getColor("textHighlight"));
		} else {
			setBackground(UIManager.getColor("control"));
		}


		this.row = row;

		return this;
	}
}
/*
 * Changes:
 * $Log: TreeTableCellRenderer.java,v $
 * Revision 1.2  2003/03/15 19:32:39  fredde
 * get colors from UIManager
 *
 * Revision 1.1  2003/03/08 13:53:16  fredde
 * A TreeTableCellRenderer
 *
 */
