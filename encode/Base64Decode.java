/*  Base64Decode.java - For base64 decoding
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

package org.gjt.fredde.yamm.encode;

import sun.misc.*;
import java.io.*;
import org.gjt.fredde.yamm.gui.imageViewer;

public class Base64Decode extends Thread {

	String filename, target;

	public Base64Decode(String name, String filename, String target) {
		super(name);
		this.filename = filename;
		this.target = target;
	}

	public void start() {
		super.start();
	}

	public void run() {
		makeBase64(target);

		try {
			InputStream is = new BufferedInputStream(
					new FileInputStream(target + ".tmp"));
			OutputStream os = new BufferedOutputStream(
					new FileOutputStream(target));
			BASE64Decoder b64dc = new BASE64Decoder();
			b64dc.decodeBuffer(is, os);
			String end = target.substring(target.lastIndexOf("."),
						target.length()).toLowerCase();
			is.close();
			os.close();

			if (end.equals(".gif") || end.equals(".jpg")) {
				new imageViewer(target);
			}

			new File(target + ".tmp").delete();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	protected void makeBase64(String t) {
		String temp;
		 try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(filename)));
			PrintWriter out = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(target + ".tmp")));

			in.readLine(); // Name of attachment
			in.readLine(); // The attachment encoding

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				}

				out.println(temp);
			}
			in.close();
			out.close();
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}
}
