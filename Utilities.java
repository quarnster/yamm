/*  Utilities.java - Some useful utilities
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
package org.gjt.fredde.yamm;

import java.io.File;
import java.util.Vector;

/**
 * This Class provides some useful utilities
 * @author Fredrik Ehnbom
 * @version $Id: Utilities.java,v 1.1 2000/02/28 13:51:24 fredde Exp $
 */
public class Utilities {

	/**
	 * Deletes the unneededfiles in the cache directory
	 */
	public static void delUnNeededFiles(){
		boolean debug = false;
		
		if (YAMM.getProperty("debug.cachedel", "false").equals("true"))
			debug = true;
			
		Vector delFile = new Vector(), delDir = new Vector();

		if (debug)
			YAMM.debug.println("Creating list of unneeded files...");
		createDelList(delFile, delDir, new File(YAMM.home + "/tmp/"), debug);
		if (debug)
			YAMM.debug.println("\nDeleting unneeded files...");

		delFiles(delFile, debug);
		delFiles(delDir, debug);
	}


	/**
	 * Creates a list of unneeded files and addes the to a vector
	 */
	private static void createDelList(Vector delFile, Vector delDir, File dir, boolean debug) {
		String files[] = dir.list();

		for (int i = 0; i < files.length; i++) {
			if (debug)
				YAMM.debug.println("added \"" + files[i]);

			File dir2 = new File(dir, files[i]);

			if (dir2.isDirectory()) {
				delDir.add(dir2);
				createDelList(delFile, delDir, dir2, debug);
			} else {
				delFile.add(dir2);
			}
		}
	}

	/**
	 * Deletes all files in a vector
	 * @param delFile The vector to get the files from
	 * @param debug If debug info should be printed out or not
	 */
	private static void delFiles(Vector delFile, boolean debug) {
		for (int i = 0; i < delFile.size(); i++) {
			String file = delFile.elementAt(i).toString();

			if (!new File(file).delete() && debug) {
				YAMM.debug.println("Couldn't delete \"" + file + "\"");
			}
		}
	}

	/**
	 * This method converts the cvstag "&#36;Name&#36;" to a version number
	 * in the format major.minor.bugfix
	 * @param cvsTag The cvstag to convert
	 * @return "&lt;unknown&gt;" if it fails to parse the cvstag or the version number in the format major.minor.bugfix if it succeeds
	 */
	public static String cvsToVersion(String cvsTag) {
		if (cvsTag.indexOf("-") == -1) return "<unknown>";

		int index = cvsTag.indexOf("-") + 1;
		int major = Integer.parseInt(cvsTag.substring(index , cvsTag.indexOf("-", index)));
		index     = cvsTag.indexOf("-", index) + 1;
		int minor = Integer.parseInt(cvsTag.substring(index, cvsTag.indexOf("-", index)));
		index     = cvsTag.indexOf("-", index) + 1;
		int bugf  = Integer.parseInt(cvsTag.substring(index, cvsTag.length()));
		return "" + major + "." + minor + "." + bugf;
	}

	/**
	 * This method converts the cvstag "&#36;Date&#36;" to a real date
	 * @param cvsTag The cvstag to convert
	 * @return "&lt;unknown&gt;" if it fails to parse the cvstag or the real date if it succeeds
	 */
	public static String cvsToDate(String cvsTag) {
		if (cvsTag.indexOf("/") == -1) return "<unknown>";

		return cvsTag.substring(7, cvsTag.length() - 1);
	}
}
/*
 * Changes:
 * $Log: Utilities.java,v $
 * Revision 1.1  2000/02/28 13:51:24  fredde
 * initial commit
 *
 */
