/*  AboutDialog - Displays text in a JEditorPane
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

package org.gjt.fredde.util.gui;

import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import org.gjt.fredde.util.net.Browser;

public class AboutDialog extends JDialog implements HyperlinkListener {

	public AboutDialog(String text) {
		setTitle("About");
		setSize(300, 200);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JEditorPane jep = new JEditorPane();
		jep.setContentType("text/html");
		jep.setEditable(false);
		jep.addHyperlinkListener(this);

		if (text.toLowerCase().startsWith("<html>")) {
			jep.setText(text);
		} else {
			try {
				jep.setPage(text);
			} catch (IOException ioe) {
				System.err.println(ioe);
			}
		}

		getContentPane().add(new JScrollPane(jep));
		show();
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				new Browser(e.getURL().toString());
			} catch (InterruptedException ie) {
				System.err.println(ie);
			} catch (IOException ioe) {
				System.err.println(ioe);
			}
		}
	}
}
