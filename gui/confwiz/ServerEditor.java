/*  ServerEditor.java - Editor for the serversettings
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

import javax.swing.*;

/**
 * Editor for the serversettings
 * @author Fredrik Ehnbom
 * @version $Id: ServerEditor.java,v 1.1 2000/02/28 13:49:33 fredde Exp $
 */
public class ServerEditor extends JDialog {

	/**
	 * Wheter or not we are configurating a new server
	 */
	private boolean newServer = false;

	/**
	 * Creates a new ServerEditor.
	 * Use this when you want to configure a new server
	 */
	public ServerEditor() {
		newServer = true;
	}

	/**
	 * Creates a new ServerEditor.
	 * Use this when you want to configure an existing server
	 */
	public ServerEditor(String file) {
	}

	/**
	 * Get the result from the editor
	 * Usefull if you want to know if you need to update the serverlist
	 */
	public boolean result() {
		return false;
	}
}
/*
 * Changes:
 * $Log: ServerEditor.java,v $
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
