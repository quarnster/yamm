/*  MessageBodyParser.java - Parses the body of a message
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

/**
 * Parses the body of a message
 * @author Fredrik Ehnbom
 * @version $Id: MessageBodyParser.java,v 1.14 2003/03/06 20:14:32 fredde Exp $
 */
public class MessageBodyParser {

	public static final int END = 0;
	public static final int ERROR = 1;
	public static final int ATTACHMENT = 2;

	protected boolean html = false;
	protected boolean mime = false;



	public MessageBodyParser(boolean html) {
		this(html, false);
	}
	public MessageBodyParser(boolean html, boolean mime) {
		this.html = html;
		this.mime = mime;
	}
}
/*
 * Changes:
 * $Log: MessageBodyParser.java,v $
 * Revision 1.14  2003/03/06 20:14:32  fredde
 * rewrote mailparsing system
 *
 * Revision 1.13  2003/03/05 21:36:23  fredde
 * Huge improvements for html, mime and multipart messages
 *
 * Revision 1.12  2003/03/05 16:00:27  fredde
 * Layout fixes
 *
 * Revision 1.11  2003/03/05 15:09:35  fredde
 * no longer unmimes links. also treats strings containing www. as links.
 *
 * Revision 1.10  2001/03/18 17:08:20  fredde
 * cleaned up
 *
 * Revision 1.9  2000/03/17 17:12:10  fredde
 * some fixes
 *
 * Revision 1.8  2000/02/28 13:42:56  fredde
 * added changelog and soem javadoc tags
 *
 */
