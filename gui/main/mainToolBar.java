/*  $Id: mainToolBar.java,v 1.25 2003/03/08 18:11:41 fredde Exp $
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

package org.gjt.fredde.yamm.gui.main;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
// import java.awt.print.PrinterJob;
import java.io.*;
import java.util.*;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.YAMMWrite;
import org.gjt.fredde.yamm.SHMail;

/**
 * The toolbar for the main class
 * @author
 * @version $Revision: 1.25 $
 */
public class mainToolBar
	extends JToolBar
{

	/** The forward button */
	public JButton forward;

	/** The reply button */
	public JButton reply;

	/** The print button */
//	public JButton print;

	private String content = YAMM.getProperty("button.content", "South");
	private YAMM frame;

	/**
	 * Creates the toolbar.
	 * @param frame2 The JFrame to use for error messages etc.
	 */
	public mainToolBar(YAMM frame2) {
		frame = frame2;
		if (!(frame.ico && !frame.text)) {
			setFloatable(false);
		}

		/* send mails in outbox get mail to inbox */
		JButton b = new JButton();
		if (frame.ico) {
			b.setIcon(new ImageIcon(getClass().getResource("/images/buttons/recycle.gif")));
		}
		if (frame.text) {
			b.setText(YAMM.getString("button.send_get"));
		}
		b.setToolTipText(YAMM.getString("button.send_get.tooltip"));
		b.setFont(new Font("SansSerif", Font.PLAIN, 10));
		setAlign(b, content);
		b.setBorderPainted(false);
		b.addActionListener(BListener);
		add(b);
		addSeparator();


		/* button to write a new mail */
		b = new JButton();
		if (frame.ico) {
			b.setIcon(new ImageIcon(getClass().getResource("/images/buttons/new_mail.gif")));
		}
		if (frame.text) {
			b.setText(YAMM.getString("button.new_mail"));
		}
		b.setFont(new Font("SansSerif", Font.PLAIN, 10));
		setAlign(b, content);
		b.addActionListener(BListener);
		b.setBorderPainted(false);
		b.setToolTipText(YAMM.getString("button.new_mail.tooltip"));
		add(b);

		/* reply button */
		reply = new JButton();
		if (frame.ico) {
			reply.setIcon(new ImageIcon(getClass().getResource("/images/buttons/reply.gif")));
		}
		if (frame.text) {
			reply.setText(YAMM.getString("button.reply"));
		}
		reply.setFont(new Font("SansSerif", Font.PLAIN, 10));
		setAlign(reply, content);
		reply.addActionListener(BListener);
		reply.setBorderPainted(false);
		reply.setToolTipText(YAMM.getString("button.reply.tooltip"));
		reply.setEnabled(false);
		add(reply);

		/* forward button */
		forward = new JButton();
		if (frame.ico) {
			forward.setIcon(new ImageIcon(getClass().getResource("/images/buttons/forward.gif")));
		}
		if (frame.text) {
			forward.setText(YAMM.getString("button.forward"));
		}
		forward.setFont(new Font("SansSerif", Font.PLAIN, 10));
		setAlign(forward, content);
		forward.addActionListener(BListener);
		forward.setBorderPainted(false);
		forward.setToolTipText(
				YAMM.getString("button.forward.tooltip"));
		forward.setEnabled(false);
		add(forward);
		addSeparator();

		/* button to print page */
/*
		print = new JButton();
		if (frame.ico) {
			print.setIcon(new ImageIcon("org/gjt/fredde/yamm/" +
						"images/buttons/print.gif"));
		}
		if (frame.text) {
			print.setText(YAMM.getString("button.print"));
		}
		print.setFont(new Font("SansSerif", Font.PLAIN, 10));
		setAlign(print, content);
		print.setBorderPainted(false);
		print.setToolTipText(YAMM.getString("button.print.tooltip"));
		print.addActionListener(BListener);
		print.setEnabled(false);
		add(print);
*/
		/* button to exit from program */
		b = new JButton();
		if (frame.ico) {
			b.setIcon(new ImageIcon(getClass().getResource("/images/buttons/exit.gif")));
		}
		if (frame.text) {
			b.setText(YAMM.getString("button.exit"));
		}
		b.setFont(new Font("SansSerif", Font.PLAIN, 10));
		setAlign(b, content);
		b.setBorderPainted(false);
		b.setToolTipText(YAMM.getString("button.exit.tooltip"));
		b.addActionListener(BListener);
		addSeparator();
		add(b);
	}

	protected void setAlign(JButton b, String content) {
		if (content.equals("North"))  {
			b.setHorizontalTextPosition(AbstractButton.CENTER);
			b.setVerticalTextPosition(AbstractButton.TOP);
		} else if (content.equals("West")) {
			b.setHorizontalTextPosition(AbstractButton.LEFT);
			b.setVerticalTextPosition(AbstractButton.CENTER);
		} else if(content.equals("East")) {
			b.setHorizontalTextPosition(AbstractButton.RIGHT);
			b.setVerticalTextPosition(AbstractButton.CENTER);
		} else {
			b.setHorizontalTextPosition(AbstractButton.CENTER);
			b.setVerticalTextPosition(AbstractButton.BOTTOM);
		}
	}


	ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String arg = ((JButton)e.getSource()).getToolTipText();

			if (arg.equals(YAMM.getString("button.new_mail.tooltip"))) {
				YAMMWrite yam = new YAMMWrite();
				yam.sign();
			} else if (arg.equals(YAMM.getString("button.send_get.tooltip"))) {
				new SHMail(frame, "mailthread",	(JButton)e.getSource()).start();
			} else if (arg.equals(YAMM.getString("button.reply.tooltip"))) {
				int i = 0;

				int selMail = frame.keyIndex[frame.mailList.getSelectedRow()];

				long skip = Long.parseLong(
					frame.listOfMails[selMail][YAMM.INDEX_SKIP]
				);

				String[] mail = Mailbox.getMailForReplyHeaders(frame.selectedbox, skip);

				if (!mail[2].startsWith(YAMM.getString("mail.re")) && !mail[2].startsWith("Re:")) {
					mail[2] = YAMM.getString("mail.re") + " " + mail[2];
				}

				YAMMWrite yam = new YAMMWrite(
					mail[0], mail[1],
					mail[2], mail[0] + YAMM.getString("mail.wrote") + "\n"
				);
				Mailbox.getMailForReply(
					frame.selectedbox,
					selMail, skip,
					yam.myTextArea
				);
				yam.sign();
			} else if (arg.equals(YAMM.getString("button.forward.tooltip"))) {
				int i = 0;

				int selMail = frame.keyIndex[frame.mailList.getSelectedRow()];

				long skip = Long.parseLong(
					frame.listOfMails[selMail][YAMM.INDEX_SKIP]
				);

				String mail[] = Mailbox.getMailForReplyHeaders(
					frame.selectedbox,
					skip
				);

				if (!mail[2].startsWith(YAMM.getString("mail.re")) && !mail[2].startsWith("Re:")) {
					mail[2] = YAMM.getString("mail.re") + " " + mail[2];
				}

				YAMMWrite yam = new YAMMWrite(
					mail[0], mail[1],
					mail[2], mail[0] + YAMM.getString("mail.wrote") + "\n"
				);
				Mailbox.getMailForReply(
					frame.selectedbox,
					selMail, skip,
					yam.myTextArea
				);
				yam.sign();
			} else if (arg.equals(YAMM.getString("button.print.tooltip"))) {
				PrintJob pj = frame.getToolkit().getPrintJob(frame, "print", null);

				if (pj != null) {
					Graphics g = pj.getGraphics();

					if (g != null) {
						frame.mail.setDoubleBuffered(false);
						frame.mail.paint(g);
						g.dispose();
					}
					pj.end();
				}
			} else if(arg.equals(YAMM.getString("button.exit.tooltip"))) {
				frame.Exit();
			}
		}
	};
}
/*
 * Changes:
 * $Log: mainToolBar.java,v $
 * Revision 1.25  2003/03/08 18:11:41  fredde
 * signs messages
 *
 * Revision 1.24  2003/03/08 16:56:27  fredde
 * Updated for the new getMailForReplyHeaders
 *
 * Revision 1.23  2003/03/06 23:05:42  fredde
 * now gets the correct message when replying/forwarding
 *
 * Revision 1.22  2000/12/26 11:24:13  fredde
 * YAMM.listOfMails is now of type String[][]
 *
 * Revision 1.21  2000/08/09 16:38:32  fredde
 * readability fixes and is now floatable in icononly mode
 *
 * Revision 1.20  2000/04/01 20:50:09  fredde
 * fixed to make the profiling system work
 *
 * Revision 1.19  2000/03/25 16:02:00  fredde
 * the email was in the subjectfield when forwarding...
 *
 * Revision 1.18  2000/03/25 15:46:24  fredde
 * uses the new getMailForReplyHeaders method
 *
 * Revision 1.17  2000/03/05 18:02:53  fredde
 * now gets the images used for the jar-file
 *
 * Revision 1.16  2000/02/28 13:47:51  fredde
 * little cleanup and added changelog and some javadoc tags
 *
 */
