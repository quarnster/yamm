/*  $Id: Html.java,v 1.1 2003/03/06 20:18:46 fredde Exp $
 *  Copyright (C) 2003 Fredrik Ehnbom
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
package org.gjt.fredde.yamm.encode;

import org.gjt.fredde.yamm.mail.*;
import java.io.*;
import java.util.*;

public class Html
	implements Encoder
{
	public static String makeLink(String link) {
		StringTokenizer tok = new StringTokenizer(link, " ", true);
		link = "";

		while (tok.hasMoreTokens()) {
			String temp = tok.nextToken();

			if (temp.indexOf("://") != -1 || temp.indexOf("www.") != -1) {
				String tmp[] = MessageParser.parseLink(temp);

				temp = "<a href=\"" + tmp[1] + "\">" + tmp[1] +
						"</a>";
				link += tmp[0] + temp + tmp[2];
			} else {
				link += temp;
			}
		}

		return link;
	}

	public static String makeEmailLink(String mail) {
		StringTokenizer tok = new StringTokenizer(mail, " ", true);

		mail = "";

		while (tok.hasMoreTokens()) {
			String temp = tok.nextToken();

			if (temp.indexOf("@") != -1) {
				String tmp[] = MessageParser.parseLink(temp);

				temp = "<a href=mailto:" + tmp[1] + ">" +
								tmp[1] + "</a>";

				mail += tmp[0] + temp + tmp[2];
			} else {
				mail += temp;
			}
		}

		return mail;
	}

	public static String makeHtml(String html) {
		StringTokenizer tok = new StringTokenizer(html, " ", true);

		html = "";


		while (tok.hasMoreTokens()) {
			String temp = tok.nextToken();
			boolean change = true;

			while (change) {
				change = false;

				if (temp.indexOf("<") != -1) {
					String begin = temp.substring(0, temp.indexOf("<"));
					temp = temp.substring(temp.indexOf("<") + 1, temp.length());
					temp = begin + "&lt;" + temp;

					change = true;
				}

				if (temp.indexOf(">") != -1) {
					String begin = temp.substring(0, temp.indexOf(">"));
					temp = temp.substring(temp.indexOf(">") + 1, temp.length());
					temp = begin + "&gt;" + temp;

					change = true;
				}
			}
			html += temp;
		}
		return html;
	}

	public void encode(InputStream is, OutputStream os)
		throws IOException
	{
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(is));
			out = new PrintWriter(os);

			out.println("<pre>");

			String temp = in.readLine();

			while (true) {
				if (temp == null) {
					out.println("</pre>");
					break;
				}

				if ((temp.indexOf("<") != -1 || temp.indexOf(">") != -1)) {
					temp = makeHtml(temp);
				}

				if (temp.indexOf("@") != -1) {
					if (temp.toLowerCase().indexOf("href=") == -1) {
						temp = makeEmailLink(temp);
					}
				}

				if (temp.indexOf("://") != -1 || temp.indexOf("www.") != -1) {
					if (temp.toLowerCase().indexOf("href=") == -1) {
						temp = makeLink(temp);
					}
				}

				out.println(temp);
				temp = in.readLine();
			}
		} finally {
			if (in != null) in.close();
			if (out != null) out.close();
		}
	}
}

