/*  MessageParser.java - Parses messages
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
import javax.swing.JTextArea;
import org.gjt.fredde.yamm.YAMM;

/**
 * Parses messages
 */
public class ReplyMessageParser {


	protected int attachments = 0;

	public ReplyMessageParser(BufferedReader in, JTextArea out)
							throws IOException,
							MessageParseException {

		MessageHeaderParser mhp = new MessageHeaderParser();
		mhp.parse(in);

		String boundary = null;

		String temp = mhp.getHeaderField("Content-Type");
		String contentType = "";
		if (temp != null) {
			contentType = temp.toLowerCase();
			if (temp.indexOf("boundary=\"") != -1) {
				boundary = temp.substring(
					temp.indexOf("boundary=\"") + 10,
					temp.indexOf("\"",
					temp.indexOf("boundary=\"") + 11));
			}
		}

		if (mhp.getHeaderField("MIME-Version") != null) {
			// expect a little mime message
			for (;;) {
				if (boundary != null) {
					if (in.readLine().equals("--" +
								boundary)) {
						new Attachment().parse(in,
						null);
						break;
					}
				} else {
					break;
				}
			}
		}

		ReplyMessageBodyParser mbp = new ReplyMessageBodyParser(false);

		for (;;) {
			int test = mbp.parse(in, out, boundary);

			if (test == MessageBodyParser.ATTACHMENT) {
				break;
			} else if (test == MessageBodyParser.END) {
				// end reached
				break;
			}
		}
	}
}
