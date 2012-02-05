/*  $Id: QuotedPrintable.java,v 1.1 2003/04/04 15:35:48 fredde Exp $
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

public class QuotedPrintable
	implements Decoder
{

	public static String decodeLine(String m, boolean html) {
		char[] check = "0123456789ABCDEF".toCharArray();
		StringTokenizer tok = new StringTokenizer(m, " ", true);
		String mime2 = "";

		while (tok.hasMoreTokens()) {
			String mime = tok.nextToken();

			if (mime.indexOf("=") != -1) {
				for (int index = mime.indexOf("=") + 1;;
					index = mime.indexOf("=", index) + 1) {

					if (index == 0) {
						break;
					} else if (index + 2 > mime.length()) {
						break;
					}
					char[] hex = mime.substring(index,
						index + 2).toCharArray();

					boolean char1 = false;
					boolean char2 = false;

					for (int i = 0; i < 16; i++) {
						if (hex[0] == check[i]) {
							char1 = true;
						}
						if (hex[1] == check[i]) {
							char2 = true;
						}
					}

					if (char1 && char2) {
						int asciiChar =
							Integer.parseInt(
							new String(hex), 16);
						String begin = mime.substring(0,
								index - 1);
						String end = mime.substring(
								index + 2,
								mime.length());
						if (html) {
							mime = begin + "&#" +
								asciiChar +
								";" + end;
						} else {
							mime = begin +
								(char) asciiChar
								+ end;
						}
					}
				}
					mime2 += mime;
			} else {
					mime2 += mime;
			}
		}

		return mime2;
	}


	public void decode(InputStream is, OutputStream os)
		throws IOException
	{
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(is));
			out = new PrintWriter(os);

			String temp = in.readLine();

			while (true) {
				if (temp == null) break;

				if (temp.endsWith("=")) {
					temp = temp.substring(0, temp.length() - 1);
					temp += in.readLine();
					continue;
				}

				if (temp.indexOf("=") != -1) {
					temp = decodeLine(temp, false);
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

