/*  YammPop3.java - get mail and print out debugging info
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
package org.gjt.fredde.yamm;

import java.io.IOException;
import org.gjt.fredde.util.net.Pop3;
import org.gjt.fredde.yamm.YAMM;

/**
 * This class gets mail from a server and prints out the debugging messages to
 * YAMM.debug.
 * @author Fredrik Ehnbom
 * @version $Id: YammPop3.java,v 1.1 2000/02/28 13:51:24 fredde Exp $
 */
public class YammPop3 extends Pop3 {

	public YammPop3(String user, String password, String server, String file, int port, boolean debug) throws IOException {
		super(user, password, server, file, port, debug);
	}

	/**
	 * Print the debugmessages to YAMM.debug
	 * @param msg The message to print
	 */
	public void Debug(String msg) {
		if (debug) {
			YAMM.debug.println("pop3: " + msg);
			YAMM.debug.flush();
		}
	}
}
/*
 * Changes:
 * $Log: YammPop3.java,v $
 * Revision 1.1  2000/02/28 13:51:24  fredde
 * initial commit
 *
 */
