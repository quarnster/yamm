/*  Browser.java - A browser launcher.
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
package org.gjt.fredde.util.net;

import java.io.IOException;

/**
 * This class is used for launching the current platforms browser.
 */
public class Browser {

	/**
	 * Starts the browser for the current platform.
	 * @param url The link to point the browser to.
	 */
	public Browser(String url) throws InterruptedException, IOException {
		String cmd = null;

		if (isWindows()) {
			cmd = "start " + url;
			Runtime.getRuntime().exec(cmd);
		} else {
			cmd = "netscape -remote openURL(" + url + ")";

			Process p = Runtime.getRuntime().exec(cmd);

			int exitcode = p.waitFor();

			if (exitcode != 0) {
				cmd = "netscape " + url;

				Runtime.getRuntime().exec(cmd);
			}
		}
	}

	/**
	 * Checks if the OS is windows.
	 * @return true if it is, false if it's not.
	 */
	public static boolean isWindows() {
		if (System.getProperty("os.name").indexOf("Windows") != -1) {
			return true;
		} else {
			return false;
		}
	}
}
