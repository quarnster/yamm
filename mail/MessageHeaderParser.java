/*  MessageHeaderParser.java - Parses the header of a message
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

package org.gjt.fredde.yamm.mail;

import java.text.*;
import java.io.*;
import java.util.*;

/**
 * This class parses the headerfields of a message
 */
public class MessageHeaderParser {

	protected Hashtable headers = null;

	public String getHeaderField(String header) {
		Object tmp = headers.get(header.toLowerCase());
		if (tmp == null) {
			return null;
		} else {
			return tmp.toString();
		}
	}

	public MessageHeaderParser() {
	}

	public long parse(BufferedReader in) throws IOException,
							MessageParseException {
		String field = null;
		String value = null;
		boolean headersStarted = false;
		headers = new Hashtable();
		long skipnum = 0;

		for (;;) {
			String temp = in.readLine();

			if (temp == null) {
				throw new MessageParseException("Unexpected " +
							"end of message.");
			}
			skipnum += temp.length() + 1;

			if (temp.startsWith(" ") || temp.startsWith("\t")) {
				value += " " + temp.trim();
			} else if (temp.indexOf(":") != -1) {
				if (!headersStarted) {
					headersStarted = true;
				}
				if (field != null && value != null) {
					headers.put(field, value);
				}
				field = temp.substring(0, temp.indexOf(":"));
				field = field.toLowerCase();
				value = temp.substring(temp.indexOf(":") + 1,
							temp.length()).trim();
			} else {
				if (!headersStarted) {
					continue;
				} else {
					if (field != null && value != null) {
						headers.put(field, value);
					}
					break;
				}
			}
		}

		return skipnum;
	}

}
