/*  Attachment.java - Parses attachments
 *  Copyright (C) 1999-2001 Fredrik Ehnbom
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

package org.gjt.fredde.yamm.mail;

import java.io.*;

/**
 * This class parses attachments
 * @author Fredrik Ehnbom
 * @version $Id: Attachment.java,v 1.5 2001/03/18 17:07:59 fredde Exp $
 */
public class Attachment {

	public static final int ERROR		= -1;
	public static final int END			=  0;
	public static final int MESSAGE 	=  1;
	public static final int ATTACHMENT 	=  2;

	public Attachment() {
	}

	public int parse(BufferedReader in, PrintWriter out)
		throws IOException, MessageParseException
	{
		MessageHeaderParser mhp = new MessageHeaderParser();
		mhp.parse(in);

		String name     = null;  // Name of the attachment
		String encoding = null;  // Encoding used for this attachment

		if (mhp.getHeaderField("Content-Type") != null) {
			name = mhp.getHeaderField("Content-Type").trim();

			if (name.indexOf("name=") != -1) {
				// remove unwanted info
				name = name.substring(name.indexOf("name=") + 5, name.length()).trim();

				if (name.indexOf(";") != -1) {
					name = name.substring(0, name.indexOf(";")).trim();
				}

				if (name.startsWith("\"") && name.endsWith("\"")) {
					name = name.substring(1, name.length() - 1);
				}
			} else {
				name = null;
			}
		}

		if (mhp.getHeaderField("Content-Disposition") != null && name == null) {
			name = mhp.getHeaderField("Content-Disposition");

			if (name.indexOf("filename=") != -1) {
				name = name.substring(name.indexOf("filename=") + 9, name.length()).trim();

				if (name.indexOf(";") != -1) {
					name = name.substring(0, name.indexOf(";"));
				}

				if (name.startsWith("\"") && name.endsWith("\"")) {
					name = name.substring(1, name.length() - 1);
				}
			} else {
				name = null;
			}

		}

		if (mhp.getHeaderField("Content-Transfer-Encoding") != null) {
			encoding = mhp.getHeaderField("Content-Transfer-Encoding").trim();
		}

		if (encoding != null) {
			if (encoding.equalsIgnoreCase("base64") ||
				encoding.equalsIgnoreCase("x-uuencode")) {

				String temp = null;

				out.println(name);
				out.flush();
				out.println(encoding);
				out.flush();

				while ( (temp  = in.readLine()) != null) {
					if (temp.startsWith("--")) {
						if (!temp.endsWith("--")) {
							return ATTACHMENT;
						} else {
							return END;
						}
					} else {
						out.println(temp);
						out.flush();
					}
				}
			}
		}
		return MESSAGE;
	}
}
/*
 * Changes:
 * $Log: Attachment.java,v $
 * Revision 1.5  2001/03/18 17:07:59  fredde
 * cleaned up
 *
 * Revision 1.4  2000/03/17 17:12:10  fredde
 * some fixes
 *
 */
