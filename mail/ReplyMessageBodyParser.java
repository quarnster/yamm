/*  MessageBodyParser.java - Parses the body of a message
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

/**
 * Parses the body of a message
 */
public class ReplyMessageBodyParser {

	public static final int END = 0;
	public static final int ERROR = 1;
	public static final int ATTACHMENT = 2;

	protected boolean html = false;

	public ReplyMessageBodyParser(boolean html) {
		this.html = html;
	}

	public int parse(BufferedReader in, JTextArea out, String attachment)
		throws IOException, MessageParseException
	{
		return -1;
	}
}
