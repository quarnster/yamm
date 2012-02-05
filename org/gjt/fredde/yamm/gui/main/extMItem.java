/*  extMItem.java - extended menu Item
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

package org.gjt.fredde.yamm.gui.main;

import javax.swing.JMenuItem;

/*
 * Extended Menu Item
 */
public class extMItem extends JMenuItem {

	/** the full name of this extMItem */
	protected String name = null;

	/**
	 * Creates a new extended menu item
	 * @param name Sets the text of this menu to 'name'
	 */
	public extMItem(String name) {
		super(name);
	}

	/**
	 * Sets the full name of this extMItem to 'name'
	 */
	public void setFullName(String name) {
		this.name = name;
	}

	/**
	 * Gets the full name of this extMItem
	 */
	public String getFullName() {
		return name;
	}
}
