/*  MessageParser.java - Parses messages
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

package org.gjt.fredde.yamm.mail;

import java.text.*;
import java.io.*;
import java.util.*;
import org.gjt.fredde.yamm.YAMM;

/**
 * Parses messages
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: MessageParser.java,v 1.14 2001/03/18 14:26:42 fredde Exp $
 */
public class MessageParser {

	private int attachments = 0;

	public String makeClickable(String click) {
		return MessageBodyParser.makeEmailLink(click);
	}

	public static String getEmail(String link) {
		StringTokenizer tok = new StringTokenizer(link);

		while (tok.hasMoreTokens()) {
			String temp = tok.nextToken();

			if (temp.indexOf("@") != -1) {
				return MessageParser.parseLink(temp)[1];
			}
		}

		return null;
	}

	public static String[] parseLink(String link) {
		String begin = "";
		String end   = "";
		String temp = link;
		boolean stuff = false;

		for (;;) {

			if (temp.endsWith(".")) {
				temp = temp.substring(0, temp.length() - 1);
				end = ".";

				stuff = true;
			}

			if (temp.endsWith(",")) {
				temp = temp.substring(0, temp.length() - 1);
				end = "," + end;
				stuff = true;
			}

			if (temp.startsWith("mailto:")) {
				temp = temp.substring(7, temp.length());
				begin = "mailto:";
				stuff = true;
			}

			if (temp.startsWith("(")) {
				temp = temp.substring(1, temp.length());
				begin = begin + "(";
				stuff = true;
			}

			if (temp.endsWith(")")) {
				temp = temp.substring(0, temp.length() - 1);
				end = ")" + end;
				stuff = true;
			}

			if (temp.startsWith("<")) {
				temp = temp.substring(1, temp.length());
				begin = begin + "&lt;";
				stuff = true;
			}

			if (temp.endsWith(">")) {
				temp = temp.substring(0, temp.length() - 1);
				end = "&gt;" + end;
				stuff = true;
			}

			if (temp.startsWith("&lt;")) {
				temp = temp.substring(4, temp.length());
				begin = begin + "&lt;";
				stuff = true;
			}

			if (temp.endsWith("&gt;")) {
				temp = temp.substring(0, temp.length() - 4);
				end  = "&gt;" + end;
				stuff = true;
			}

			if (temp.startsWith("\"")) {
				temp = temp.substring(1, temp.length());
				begin = begin + "\"";
				stuff = true;
			}

			if (temp.endsWith("\"")) {
				temp = temp.substring(0, temp.length() - 1);
				end = "\"" + end;
				stuff = true;
			}

			if (temp.startsWith("\'")) {
				temp = temp.substring(1, temp.length());
				begin = begin + "\'";
				stuff = true;
			}

			if (temp.endsWith("\'")) {
				temp = temp.substring(0, temp.length() - 1);
				end = "\'" + end;
				stuff = true;
			}

			if (stuff != true) {
				break;
			} else {
				stuff = false;
			}
		}

		String[] tmp = { begin, temp, end };
		return tmp;
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
		if (mhp.getHeaderField("cc") != null) {
			String cc = makeClickable(mhp.getHeaderField("cc"));
			out.println("<tr><td>cc:</td><td>" + cc + "</td></tr>");
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
				Mailbox.unMime(mhp.getHeaderField("Subject")) +
				"</td></tr>");
		}
		out.println("</table>");
		out.println("<br><pre>");

		String boundary = null;

		String temp = mhp.getHeaderField("Content-Type");
		String contentType = "";
		if (temp != null) {
			contentType = temp.toLowerCase();
			if (contentType.indexOf("boundary=") != -1) {
				boundary = temp.substring(
					contentType.indexOf("boundary=") + 9,
					temp.length());
				if (boundary.startsWith("\"")) {
					boundary = boundary.substring(1, boundary.length() - 1);
				}
			}
		}

		if (mhp.getHeaderField("MIME-Version") != null) {
			// expect a little mime message
			for (;;) {
				if (boundary != null) {
					if (in.readLine().equals("--" +	boundary)) {
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

		bigLoop:
		for (;;) {
			int test = mbp.parse(in, out, boundary);

			if (test == MessageBodyParser.ATTACHMENT &&
							contentType.indexOf("multipart/alternative") == -1) {
				Attachment a = new Attachment();
				PrintWriter atOut = null;
				try {
					atOut = new PrintWriter(
						new BufferedOutputStream(
						new FileOutputStream(file +
							".attach." +
							attachments)));

				
					for (;;) {
						// Attachment found
						test = a.parse(in, atOut);

						if (test == Attachment.MESSAGE &&
								contentType.indexOf("multipart/alternative") == -1) {
							break;
						} else if (test == Attachment.ATTACHMENT) {
							attachments++;
							atOut.close();
							atOut = new PrintWriter(new
								BufferedOutputStream(
								new FileOutputStream(
									file +
									".attach." +
									attachments)));
							continue;
						} else /* if (test == Attachment.END) */{
							break bigLoop;
						}
					}
				} finally {
					if (atOut != null) {
						atOut.close();
					}
				}
			} else if (test == MessageBodyParser.END) {
				// end reached
				break;
			} else if (test == MessageBodyParser.ATTACHMENT) {
				break;
			}
		}

		out.println("</pre></body>");
		out.println("</html>");
	}
}
/*
 * ChangeLog:
 * $log$
 */
