/*  $Id: MailReader.java,v 1.1 2003/03/08 16:53:07 fredde Exp $
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
package org.gjt.fredde.yamm.gui;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.*;

import org.gjt.fredde.yamm.*;
import org.gjt.fredde.util.gui.*;
import org.gjt.fredde.util.net.*;


public class MailReader
	extends JFrame
	implements HyperlinkListener
{
	JEditorPane mail;

	public MailReader(URL url) {
		super();
		int width = 400;
		int height = 400;

		try { width = Integer.parseInt(YAMM.getProperty("mail.width", "400")); } catch (Exception e) {}
		try { height = Integer.parseInt(YAMM.getProperty("mail.height", "400")); } catch (Exception e) {}

		setSize(width, height);

		mail = new JEditorPane();
		mail.setContentType("text/html");
		mail.setEditable(false);
		mail.addHyperlinkListener(this);
		getContentPane().add(new JScrollPane(mail));

		try {
			mail.setPage(url);
		} catch (Exception e) {
			new ExceptionDialog(
				YAMM.getString("msg.error"),
				e,
				YAMM.exceptionNames
			);
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				Rectangle rv = new Rectangle();
				getBounds(rv);

				YAMM.setProperty("mail.width", "" + rv.width);
				YAMM.setProperty("mail.height", "" + rv.height);
				dispose();
			}
		});

		show();
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String link = e.getURL().toString();

			if ((e.getURL().toString()).startsWith("mailto:")) {
				link = link.substring(link.indexOf("mailto:") +	7, link.length());
				new YAMMWrite(link);
			} else {
				try {
					Browser.open(link);
				} catch (InterruptedException ie) {
					new ExceptionDialog(
						YAMM.getString("msg.error"),
						ie,
						YAMM.exceptionNames
					);
				} catch(IOException ioe) {
					new ExceptionDialog(
						YAMM.getString("msg.error"),
						ioe,
						YAMM.exceptionNames
					);
				}
			}
		} else if (e.getEventType() ==	HyperlinkEvent.EventType.ENTERED) {
			mail.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else if (e.getEventType() ==	HyperlinkEvent.EventType.EXITED) {
			mail.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
}

/*
 * Changes
 * $Log: MailReader.java,v $
 * Revision 1.1  2003/03/08 16:53:07  fredde
 * reads messages in a new window
 *
 */

