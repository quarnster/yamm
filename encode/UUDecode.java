/*  UUDecode.java - Decodes uuencoded files
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
import java.io.*;
import javax.swing.JFrame;
import org.gjt.fredde.util.UUDecoder;
import org.gjt.fredde.util.gui.MsgDialog;
import org.gjt.fredde.yamm.gui.imageViewer;

/**
 * A class to Decode uuencoded files
 */
public class UUDecode extends Thread{
	protected String  filename;
	protected String  target;
	protected JFrame  frame;
	protected boolean view;

	/**
	 * Initializes some stuff
	 * @param frame The frame to use when displaying errors etc.
	 * @param name The name of this Thread
	 * @param whichFile The file to Decode
	 * @param wannaView If the user wants to View the file
	 */
	public UUDecode(JFrame frame1, String name, String whichFile,
					String target,  boolean wannaView) {
		super(name);
		filename = whichFile;
		frame = frame1;
		view = wannaView;
		this.target = target;
	}

	/**
	 * Decodes the file
	 */
	public void run() {
		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(filename)));
			DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(
						new FileOutputStream(target)));


			in.readLine();
			in.readLine();

			new UUDecoder(in, out);

			in.close();
			out.close();
		} catch (IOException ioe) {
			new MsgDialog(frame, "Error!", ioe.toString());
		}

		if (view) {
			String end = target.substring(target.length() - 4,
							target.length());

			if (end.equalsIgnoreCase(".jpg") ||
						end.equalsIgnoreCase(".gif")) {
				new imageViewer(target);
			}
		}
	}
}
