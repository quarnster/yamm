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
import org.gjt.fredde.yamm.YAMM;

/**
 * Parses messages
 */
public class MessageParser {


	protected int attachments = 1;

	public String makeClickable(String click) {
		return MessageBodyParser.makeEmailLink(click);
	}

	public MessageParser(BufferedReader in, PrintWriter out, String file)
							throws IOException,
							MessageParseException {

		file = file.substring(0, file.length() - 5);
		MessageHeaderParser mhp = new MessageHeaderParser();
		mhp.parse(in);

		out.println("<html>");
		out.println("<body>");
		out.println("<table>");

		if (mhp.getHeaderField("Date") != null) {
			DateParser dp = new DateParser();
			dp.setTargetFormat(YAMM.getString("longdate"));
			String date = null;

			try {
				date = dp.parse(mhp.getHeaderField("Date"));
			} catch (ParseException pe) {
				date = "";
			}

			out.println("<tr><td>" + YAMM.getString("mail.date") +
				"</td><td>" + date + "</td></tr>");
		}
		if (mhp.getHeaderField("From") != null) {
			String from = makeClickable(mhp.getHeaderField("From"));

			out.println("<tr><td>" + YAMM.getString("mail.from") +
				"</td><td>" + from + "</td></tr>");
		}
		if (mhp.getHeaderField("To") != null) {
			String to = makeClickable(mhp.getHeaderField("To"));
			out.println("<tr><td>" + YAMM.getString("mail.to") +
				"</td><td>" + to + "</td></tr>");
		}
		if (mhp.getHeaderField("Reply-To") != null) {
			String replyto = makeClickable(
						mhp.getHeaderField("Reply-To"));

			out.println("<tr><td>" + 
				YAMM.getString("mail.reply_to") + "</td><td>" +
				replyto + "</td></tr>");
		}
		if (mhp.getHeaderField("Subject") != null) {
			out.println("<tr><td>" + 
				YAMM.getString("mail.subject") + "</td><td>" +
				mhp.getHeaderField("Subject") + "</td></tr>");
		}
		out.println("</table>");
		out.println("<br><pre>");

		String boundary = null;

		String temp = mhp.getHeaderField("Content-Type");
		if (temp != null) {
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

		MessageBodyParser mbp = new MessageBodyParser(true);

		for (;;) {
			int test = mbp.parse(in, out, boundary);

			if (test == MessageBodyParser.ATTACHMENT) {
				Attachment a = new Attachment();
				PrintWriter AtOut = new PrintWriter(
						new BufferedOutputStream(
						new FileOutputStream(file +
							".attach." +
							attachments)));
				
				for (;;) {
					// Attachment found
					test = a.parse(in, AtOut);

					if (test == Attachment.MESSAGE) {
						break;
					} else if (test == Attachment.
								ATTACHMENT) {
						attachments++;
						AtOut.close();
						AtOut = new PrintWriter(new
							BufferedOutputStream(
							new FileOutputStream(
								file +
								".attach." +
								attachments)));
						continue;
					} else if ( test == Attachment.END) {
						return;
					}
				}
			} else if (test == MessageBodyParser.END) {
				// end reached
				break;
			}
		}

		out.println("</pre></body>");
		out.println("</html>");
	}
}
