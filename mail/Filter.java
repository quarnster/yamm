/*  $Id: Filter.java,v 1.10 2003/03/09 17:43:51 fredde Exp $
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

import java.io.*;
import java.util.*;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.Utilities;

/**
 * A class that filters incoming messages
 */
public class Filter {

	/**
	 * Starts the filtering
	 */
	public Filter()
		throws IOException
	{
		Hashtable changedBoxes = new Hashtable();
		int type = 0;
		int cheat[] = new int[1];
		Vector list = new Vector();
		BufferedReader in = null;
		String filterBox = Utilities.replace(YAMM.home + "/boxes/.filter");
		String inBox = Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.inbox"));

		Mailbox.createFilterList(list);

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(Utilities.replace(YAMM.home + "/.filters"))));

			String temp = null;

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				}

				StringTokenizer tok = new StringTokenizer(temp, ";");
				String con1 = tok.nextToken();
				String con2 = tok.nextToken();
				String con3 = tok.nextToken();
				String exec = tok.nextToken();

				if (con1.equals("from")) {
					 type = 0;
				} else if (con1.equals("to")) {
					type = 1;
				} else if (con1.equals("subject")) {
					type = 2;
				} else if (con1.equals("reply")) {
					type = 3;
				}

				if (con2.equals("is")) {
					for (int i = 0; i < list.size(); i++) {
						Vector v = (Vector) list.elementAt(i);
						String temp2 = v.elementAt(type).toString();
						if (temp2.equalsIgnoreCase(con3)) {
							list.remove(i);
							cheat[0] = i;
							String target = Utilities.replace(YAMM.home + "/boxes/" + exec);
							Mailbox.moveMail(filterBox, target , cheat);
							changedBoxes.put(target, target);
							i--;
						}
					}
				} else if (con2.equals("contains")) {
					for (int i = 0; i < list.size(); i++) {
						Vector v = (Vector) list.elementAt(i);
						String temp2 = v.elementAt(type). toString();

						if (temp2.toLowerCase().indexOf(con3.toLowerCase()) != -1) {
							list.remove(i);
							cheat[0] = i;

							String target = Utilities.replace(YAMM.home + "/boxes/" + exec);

							Mailbox.moveMail(filterBox, target, cheat);
							changedBoxes.put(target, target);
							i--;
						}
					}
				}
			}
		} finally {
			in.close();
		}

		cheat[0] = 0;
		if (Mailbox.hasMail(filterBox)) {
			int[] msg = new int[Mailbox.getUnread(filterBox)[0]];
			for (int i = 0; i < msg.length; i++) msg[i] = i;
			Mailbox.moveMail(filterBox, inBox, msg);
		}
	}
}
