/*  $Id: MessageParser.java,v 1.22 2003/03/07 10:53:29 fredde Exp $
 *  Copyright (C) 1999-2003 Fredrik Ehnbom
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
import org.gjt.fredde.yamm.encode.*;

/**
 * Parses messages
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: MessageParser.java,v 1.22 2003/03/07 10:53:29 fredde Exp $
 */
public class MessageParser {

	private int attachments = 0;

	public String makeClickable(String click) {
		return Html.makeEmailLink(click);
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
		"[",
		"\"",
		"\'",
	};
	private static String[] endList = new String[] {
		")",
		">",
		"]",
		"<",
		"&gt;",
		"\"",
		"\'",
		"&nbsp;",
		"!",
		",",
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
		String[] tmp = { begin, temp, end };
		return tmp;
	}

	public MessageParser(BufferedReader in, PrintWriter out, String file)
		throws IOException, MessageParseException
	{

		String contentType = null;
		String encoding = null;

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
			String from = Html.makeEmailLink(Html.makeHtml(Mailbox.unMime(mhp.getHeaderField("From"))));

			out.println("<tr><td align=right><b>" + YAMM.getString("mail.from") +
				"</b></td><td>" + from + "</td></tr>");
		}
		if (mhp.getHeaderField("To") != null) {
			String to = Html.makeEmailLink(Html.makeHtml(Mailbox.unMime(mhp.getHeaderField("To"))));
			out.println("<tr><td align=right><b>" + YAMM.getString("mail.to") +
				"</b></td><td>" + to + "</td></tr>");
		}
		if (mhp.getHeaderField("cc") != null) {
			String cc = Html.makeEmailLink(Html.makeHtml(Mailbox.unMime(mhp.getHeaderField("cc"))));
			out.println("<tr><td align=right><b>cc:</b></td><td>" + cc + "</td></tr>");
		}
		if (mhp.getHeaderField("Reply-To") != null) {
			String replyto = Html.makeEmailLink(Html.makeHtml(Mailbox.unMime(mhp.getHeaderField("Reply-To"))));

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
			if (contentType.indexOf("text/html") != -1) html = true;
		}
		if (mhp.getHeaderField("Content-Transfer-Encoding") != null) {
			encoding = mhp.getHeaderField("Content-Transfer-Encoding").trim();
		}



		if (boundary != null) {
			// expect a little mime message
			for (;;) {
				if (in.readLine().equals("--" +	boundary)) {
					Attachment a = new Attachment();
					a.parse(in, boundary, file);
	
					break;
				}
			}
		}

		if (boundary == null) {

			PrintWriter o2 = null;
			String name = null;
			try {
				name = file + ".message.html";
				if (!html)
					name = file + ".message.txt";

				o2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(name)));
				while ((temp = in.readLine()) != null) {
					if (temp.equals(".")) break;

					o2.println(temp);
				}
			} finally {
				if (o2 != null) o2.close();
			}
			filter(contentType, encoding, name);
		}

		if (new File(file + ".message.html").exists())
			file += ".message.html";
		else if (new File(file + ".message.txt.html").exists())
			file += ".message.txt.html";
		else
			file += ".message.txt";

		temp = null;
		BufferedReader in2 = null;
		try {
			in2 = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((temp = in2.readLine()) != null) {
				out.println(temp);
			}
		} finally {
			if (in2 != null) in2.close();
		}

		out.println("</body>");
		out.println("</html>");
	}

	public static void filter(String contentType, String encoding, String fileName)
		throws IOException
	{
		if (encoding == null && fileName.indexOf(".message.") != -1) {
			if (contentType == null || contentType.indexOf("text/html") == -1) {
				File source = new File(fileName);
				File target = new File(fileName + ".html");

				new Html().encode(
					new BufferedInputStream(new FileInputStream(source)),
					new BufferedOutputStream(new FileOutputStream(target))
				);
			}
		}

		if (encoding == null) return;

		File source = new File(fileName);
		File target = new File(fileName + ".tmp");

		if (encoding.equalsIgnoreCase("base64")) {
			new Base64sun().decode(
				new BufferedInputStream(new FileInputStream(source)),
				new BufferedOutputStream(new FileOutputStream(target))
			);
			source.delete();
			target.renameTo(source);
		}

		if (encoding.equalsIgnoreCase("quoted-printable")/*fileName.indexOf(".message.") != -1*/) {
			new Mime().decode(
				new BufferedInputStream(new FileInputStream(source)),
				new BufferedOutputStream(new FileOutputStream(target))
			);
			source.delete();
			target.renameTo(source);
		}
		if (fileName.indexOf(".message.") != -1 && (contentType == null || contentType.indexOf("text/html") == -1)) {
			new Html().encode(
				new BufferedInputStream(new FileInputStream(source)),
				new BufferedOutputStream(new FileOutputStream(source + ".html"))
			);
		}
	}

}
/*
 * ChangeLog:
 * $Log: MessageParser.java,v $
 * Revision 1.22  2003/03/07 10:53:29  fredde
 * filter fixes
 *
 * Revision 1.21  2003/03/06 23:52:26  fredde
 * encode text messages to *.message.txt.html instead of just .txt
 *
 * Revision 1.20  2003/03/06 20:14:32  fredde
 * rewrote mailparsing system
 *
 */
