/*  Browser.java - A browser launcher.
 *  Copyright (C) 1999-2001 Fredrik Ehnbom
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package org.gjt.fredde.util.net;

import java.io.IOException;

/**
 * This class is used for launching the current platforms default browser.
 *
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: Browser.java,v 1.5 2003/03/08 21:46:24 fredde Exp $
 */
public class Browser {

	/**
	 * Starts the default browser for the current platform.
	 *
	 * @param url The link to point the browser to.
	 * @deprecated As of v1.4 the preferred way to open a browser
	 * is by using <code>Browser.open(url)</code>
	 */
	public Browser(String url)
		throws InterruptedException,
			IOException
	{
		open(url);
	}

	/**
	 * Starts the default browser for the current platform.
	 *
	 * @param url The link to point the browser to.
	 */
	public static void open(String url)
		throws InterruptedException,
			IOException
	{
		String cmd = null;

		if (isWindows()) {
			cmd = ("rundll32 url.dll,FileProtocolHandler " + maybeFixupURLForWindows(url));
			Runtime.getRuntime().exec(cmd);
		} else {
			cmd = "mozilla -remote openURL(" + url + ")";

			Process p = Runtime.getRuntime().exec(cmd);

			int exitcode = p.waitFor();

			if (exitcode != 0) {
				cmd = "mozilla " + url;

				Runtime.getRuntime().exec(cmd);
			}
		}
	}

	/**
	 * If the default browser is Internet Explorer 5.0 or greater,
	 * the URL.DLL program fails if the url ends with .htm or .html .
	 * This problem is described by Microsoft at
	 *   http://support.microsoft.com/support/kb/articles/Q283/2/25.ASP
	 * Of course, their suggested workaround is to use the classes from the
	 * microsoft Java SDK, but fortunately another workaround does exist.
	 * If you alter the url slightly so it no longer ends with ".htm",
	 * the URL can launch successfully.  The logic here appends a null query
	 * string onto the end of the URL if none is already present, or
	 * a bogus query parameter if there is already a query string ending in
	 * ".htm"
	 */
	private static String maybeFixupURLForWindows(String url) {
		// plain filenames (e.g. c:\some_file.html or \\server\filename) do
		// not need fixing.
		if (url == null || url.length() < 2 || url.charAt(0) == '\\' || url.charAt(1) == ':')
			return url;

		String lower_url = url.toLowerCase();
		int i = badEndings.length;

		while (i-- > 0)
			if (lower_url.endsWith(badEndings[i]))
				return fixupURLForWindows(url);

		return url;
	}

	private static final String[] badEndings = {
		".htm",
		".html",
		".htw",
		".mht",
		".cdf",
		".mhtml",
		".stm"
	};

	private static String fixupURLForWindows(String url) {
		if (url.indexOf('?') == -1)
			return url + "?";
		else
			return url + "&workaroundStupidWindowsBug";
	}

	/**
	 * Checks if the OS is windows.
	 *
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
/*
 * ChangeLog:
 * $Log: Browser.java,v $
 * Revision 1.5  2003/03/08 21:46:24  fredde
 * netscape -> mozilla
 *
 * Revision 1.4  2001/02/04 11:03:19  fredde
 * updated and applied "open IE"-patch from David Tuma
 *
 * Revision 1.3  2000/07/13 19:03:32  fredde
 * updated the command to startup browsers in windows
 *
 * Revision 1.2  2000/04/01 14:59:56  fredde
 * license for the package changed to LGPL
 *
 */
