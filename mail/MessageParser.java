/*  MessageParser.java - Parses messages
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

import java.text.*;
import java.io.*;
import java.util.*;
import org.gjt.fredde.yamm.YAMM;

/**
 * Parses messages
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: MessageParser.java,v 1.17 2003/03/05 15:10:43 fredde Exp $
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

	private static String[] startList = new String[] {
		":",
		";",
		" ",
		"(",
		"<",
		"\"",
		"\'"
	};
	private static String[] endList = new String[] {
		")",
		">",
		"<",
		"&gt;",
		"\"",
		"\'",
		"&nbsp;",
		"!",
	};

	public static String[] parseLink(String link) {
		String begin = "";
		String end   = "";
		String temp = link;
		boolean stuff = false;
		int startIndex = 0;

		if ((startIndex = link.indexOf("://")) == -1) {
			if ((startIndex = link.indexOf("www.")) == -1) {
				if ((startIndex = link.indexOf("@")) == -1) {
					System.err.println("WARNING: Unsupported URL");
					return new String[] {begin,temp,end};
				}
			}
		}
			

		for (;;) {
			for (int i = 0; i < startList.length; i++) {
				int index = temp.indexOf(startList[i]);
				if (index != -1 && index < startIndex) {
					index += startList[i].length();
					begin += temp.substring(0, index);
					temp = temp.substring(index);
					stuff = true;
					startIndex -= index;
					break;
				}
			}

			if (stuff != true) {
				break;
			}
			stuff = false;
		}
		stuff = false;

		for (;;) {
			for (int i = 0; i < endList.length; i++) {
				int index = temp.indexOf(endList[i]);
				if (index != -1) {
					end = temp.substring(index) + end;
					temp = temp.substring(0, index);
					stuff = true;
					break;
				}
			}

			if (stuff != true) {
				break;
			} else {
				stuff = false;
			}
		}
		System.out.println("temp: " + temp);
		String[] tmp = { begin, temp, end };
		return tmp;
	}

	public MessageParser(BufferedReader in, PrintWriter out, String file)
		throws IOException, MessageParseException
	{
		boolean html = false;
		file = file.substring(0, file.length() - 5);
		MessageHeaderParser mhp = new MessageHeaderParser();
		mhp.parse(in);

		out.println("<html>");
		out.println("<body>");
		out.println("<table width=\"100%\" bgcolor=\"#BBBBBB\">");
		out.println("<tr><td>");
		out.println("<table bgcolor=\"#BBBBBB\">");

		if (mhp.getHeaderField("Date") != null) {
			DateParser dp = new DateParser();
			dp.setTargetFormat(YAMM.getString("longdate"));
			String date = null;

			try {
				date = dp.parse(mhp.getHeaderField("Date"));
			} catch (ParseException pe) {
				date = "";
			}

			out.println("<tr><td align=right><b>" + YAMM.getString("mail.date") +
				"</b></td><td>" + date + "</td></tr>");
		}
		if (mhp.getHeaderField("From") != null) {
			String from = makeClickable(mhp.getHeaderField("From"));

			out.println("<tr><td align=right><b>" + YAMM.getString("mail.from") +
				"</b></td><td>" + from + "</td></tr>");
		}
		if (mhp.getHeaderField("To") != null) {
			String to = makeClickable(mhp.getHeaderField("To"));
			out.println("<tr><td align=right><b>" + YAMM.getString("mail.to") +
				"</b></td><td>" + to + "</td></tr>");
		}
		if (mhp.getHeaderField("cc") != null) {
			String cc = makeClickable(mhp.getHeaderField("cc"));
			out.println("<tr><td align=right><b>cc:</b></td><td>" + cc + "</td></tr>");
		}
		if (mhp.getHeaderField("Reply-To") != null) {
			String replyto = makeClickable(
						mhp.getHeaderField("Reply-To"));

			out.println("<tr><td align=right><b>" + 
				YAMM.getString("mail.reply_to") + "</b></td><td>" +
				replyto + "</td></tr>");
		}
		if (mhp.getHeaderField("Subject") != null) {
			out.println("<tr><td align=right><b>" + 
				YAMM.getString("mail.subject") + "</b></td><td>" +
				Mailbox.unMime(mhp.getHeaderField("Subject")) +
				"</td></tr>");
		}
		out.println("</table>");
		out.println("</td></tr>");
		out.println("</table>");

		String boundary = null;

		String temp = mhp.getHeaderField("Content-Type");
		String contentType = "";
		if (temp != null) {
			contentType = temp.toLowerCase();
			if (contentType.indexOf("boundary=") != -1) {
				boundary = temp.substring(contentType.indexOf("boundary=") + 9, temp.length());
				if (boundary.startsWith("\"")) {
					boundary = boundary.substring(1, boundary.length() - 1);
				}
				if (boundary.indexOf("\"") != -1) {
					boundary = boundary.substring(0, boundary.indexOf("\""));
				}
			}
		}

		if (mhp.getHeaderField("MIME-Version") != null) {
			// expect a little mime message
			if (boundary != null) {
				for (;;) {
					if (in.readLine().equals("--" +	boundary)) {
						Attachment a = new Attachment();
						a.parse(in, null);
						if (a.contentType != null && a.contentType.indexOf("text/html") != -1) html = true;
						break;
					}
				}
			}
		}

		if (!html)
			out.println("<br><pre>");

		MessageBodyParser mbp = new MessageBodyParser(true, html);

		bigLoop:
		for (;;) {
			int test = mbp.parse(in, out, boundary);

			if (test == MessageBodyParser.ATTACHMENT && contentType.indexOf("multipart/alternative") == -1) {
				Attachment a = new Attachment();
				PrintWriter atOut = null;
				try {
					atOut = new PrintWriter(
						new BufferedOutputStream(
							new FileOutputStream(file +	".attach." + attachments)
						)
					);


					for (;;) {
						// Attachment found
						test = a.parse(in, atOut);

						if (test == Attachment.MESSAGE && contentType.indexOf("multipart/alternative") == -1) {
							break;
						} else if (test == Attachment.ATTACHMENT) {
							attachments++;
							atOut.close();
							atOut = new PrintWriter(
								new	BufferedOutputStream(
									new FileOutputStream(
										file + ".attach." +	attachments
									)
								)
							);
							continue;
						} else if (test == Attachment.AMESSAGE) {
							if (a.name != null)
								out.println("<b>" + a.name + "</b>:<br>");
							atOut.close();
							new File(file +	".attach." + attachments).delete();
							break;
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

		if (html)
			out.println("</pre>");
		out.println("</body>");
		out.println("</html>");
	}
}
/*
 * ChangeLog:
 * $log$
 */
