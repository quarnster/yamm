/*  $Id: SHMail.java,v 1.39 2003/04/19 11:52:52 fredde Exp $
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
package org.gjt.fredde.yamm;

import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.net.*;
import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.mail.*;
import org.gjt.fredde.yamm.YAMM;

/**
 * Sends and gets mail
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Revision: 1.39 $
 */
public class SHMail
	extends Thread
{
	/** Properties for smtpserver etc */
	static protected Properties props = new Properties();

	/** If pop-debugging info should be printed */
	static private boolean popdebug = false;

	/** If smtp-debugging info should be printed */
	static private boolean smtpdebug = false;

	private int sentnum;
	private int receivednum;
	private int sentbytes;
	private int receivedbytes;

	JButton knappen;
	YAMM yamm;

	/**
	 * Disables the send/get-button and inits some stuff
	 * @param yamm1 the yamm that will be used for error-msg etc.
	 * @param name the name of this thread.
	 * @param knapp the button to disable.
	 */
	public SHMail(YAMM yamm1, String name, JButton knapp) {
		super(name);
		knappen = knapp;
		knappen.setEnabled(false);
		yamm = yamm1;

		popdebug	= YAMM.getProperty("debug.pop",  "false").equals("true");
		smtpdebug	= YAMM.getProperty("debug.smtp", "false").equals("true");
	}

	/**
	 * Creates a new progress-dialog, checks for server configuration-files
	 * and connects to the servers with a config-file.
	 */
	public void run() {
		try { sentnum = Integer.parseInt(YAMM.getProperty("stat.sentnum", "0")); } catch (Exception e) { sentnum = 0; }
		try { receivednum = Integer.parseInt(YAMM.getProperty("stat.receivednum", "0")); } catch (Exception e) { receivednum = 0; }
		try { sentbytes = Integer.parseInt(YAMM.getProperty("stat.sentbytes", "0")); } catch (Exception e) { sentbytes = 0; }
		try { receivedbytes = Integer.parseInt(YAMM.getProperty("stat.receivedbytes", "0")); } catch (Exception e) { receivedbytes = 0; }

		String outbox = Utilities.replace(YAMM.home + "/boxes/outbox");
		yamm.status.progress(0);
		yamm.status.setStatus("");

		File files[] = new File(YAMM.home + "/servers/").listFiles();

		for (int i = 0; i < files.length; i++) {
			/* load the config */

			try {
				InputStream in = new FileInputStream(files[i]);
				props.load(in);
				in.close();
			} catch (IOException propsioe) {
				new ExceptionDialog(YAMM.getString("msg.error"),
						propsioe,
						YAMM.exceptionNames);
			}

			String type     = props.getProperty("type");
			if (!type.equals("pop3")) continue;

			String server   = props.getProperty("server");
			String username = props.getProperty("username");
			String password = new SimpleCrypt("myKey").decrypt(props.getProperty("password"));
			boolean del	= props.getProperty("delete", "true").equals("true");

			int port = 110;
			try {
				port = Integer.parseInt(props.getProperty("port"));
			} catch (Exception e) {
				port = 110;
			}

			if (type != null && server != null && username != null && password != null) {
				if (type.equals("pop3")) {
					Object[] argstmp = {server};
					yamm.status.setStatus(YAMM.getString("server.contact", argstmp));

					YammPop3 pop = null;

					try {
						pop = new YammPop3(username, password, server, YAMM.home + "/boxes/.filter", port, popdebug);
						int messages = pop.getMessageCount();

						for (int j = 1; j <= messages; j++) {
							pop.getMessage(j, messages, yamm);

							if (del) pop.deleteMessage(j);
						}
						receivednum += messages;

						yamm.status.setStatus(YAMM.getString("msg.done"));
						yamm.status.progress(100);
					} catch (IOException ioe) {
						new ExceptionDialog(YAMM.getString("msg.error"),
							ioe, YAMM.exceptionNames);
					} finally {
						try {
							if (pop != null) {
								pop.close();
							}
						} catch (Exception e) {}
					}
				}
			}
		}
		receivedbytes += new File(Utilities.replace(YAMM.home + "/boxes/.filter")).length();


		if (
			YAMM.getProperty("smtpserver") != null &&
			Mailbox.hasMail(outbox)
		) {
			Smtp smtp = null;
			BufferedReader in = null;
			PrintWriter mail = null;

			try {
				File boxFile = new File(outbox);
				smtp = new YammSmtp(YAMM.getProperty("smtpserver"), 25, smtpdebug);
				in = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(boxFile)
					)
				);

				String temp = null, from2 = null, to2 = null;
				Index idx = new Index(boxFile.toString());
				idx.open();
				IndexEntry[] entries = idx.getEntries();
				idx.close();

				long last = entries[0].skip;
				long length = 0;
				long read = 0;

				int i = 1;
				if (i >= entries.length) {
					length = (boxFile.length() - last);
				} else {
					length = entries[i].skip - last;
					last = entries[i].skip;
				}

				String mailStatus = YAMM.getString("server.send", new Object[] {"1"});

	        		yamm.status.setStatus(mailStatus);
		        	yamm.status.progress(0);


				for (;;) {
					temp = in.readLine();

					if (temp == null) break;

					read += temp.length() + 1;

					yamm.status.setStatus(mailStatus + "  " + read + "/" + length);
					yamm.status.progress((int) (((float) read / length) * 100));

					if (temp.startsWith("From:")) {
						String from = MessageParser.getEmail(temp);
						smtp.from(from);
						from2 = temp;
					} else if (temp.startsWith("To:")) {
						to2 = temp;

						if (temp.indexOf(",") == -1) {
							smtp.to(MessageParser.getEmail(temp.substring(4, temp.length())));
						} else {
							temp.trim();
							temp = temp.substring(4, temp.length());

							while (temp.endsWith(",")) {
								smtp.to(MessageParser.getEmail(temp.substring(0, temp.length()-1)));
								temp = in.readLine().trim();
								to2 += "\n      " + temp;
							}
							smtp.to(MessageParser.getEmail(temp.substring(0, temp.length())));
						}
					} else if (temp.startsWith("Subject:")) {
						mail = smtp.getOutputStream();
						mail.println(from2 + "\n" + to2 + "\n" + temp);

						for (;;) {
							temp = in.readLine();

							if (temp != null) {
								read += temp.length() + 1;
								yamm.status.setStatus(mailStatus + "  " + read + "/" + length);
								yamm.status.progress((int) (((float) read / length) * 100));
							}


							if (temp == null || Pattern.matches("From .* [a-zA-Z]{3} [a-zA-Z]{3} {1,2}[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2} [0-9]{4}", temp)) {
								smtp.sendMessage();

								i++;
								read = 0;

								mailStatus = YAMM.getString("server.send", new Object[] {"" + i});

								if (i >= entries.length) {
										length = (boxFile.length() - last);
								} else {
									length = entries[i].skip - last;
					        			last = entries[i].skip;
								}

								break;
							}
							mail.println(temp);
						}
						mail = null;
					} else {
						if (mail != null)
							mail.println(temp);
					}
				}
				in.close();
			} catch (IOException ioe) {
				new ExceptionDialog(YAMM.getString("msg.error"), ioe, YAMM.exceptionNames);
			} finally {
				try {
					if (in != null) in.close();
					if (smtp != null) smtp.close();
					if (mail != null) mail.close();
				} catch (IOException ioe) {}
			}

			int[] move = new int[Mailbox.getUnread(outbox)[0]];
			for (int i = 0; i < move.length; i++) {
				move[i] = i;
			}

			String sent = Utilities.replace(YAMM.home + "/boxes/sent");
			sentbytes += new File(outbox).length();
			sentnum += move.length;
			Mailbox.moveMail(outbox, sent, move);
		}
		yamm.status.setStatus(YAMM.getString("msg.done"));
		yamm.status.progress(100);

		yamm.status.setStatus("");
		yamm.status.progress(0);

		if (Mailbox.hasMail(YAMM.home + "/boxes/.filter")) {
			yamm.status.setStatus(YAMM.getString("server.filter"));
			yamm.status.progress(0);

			try {
				new Filter(yamm);
			} catch (IOException ioe) {
				new ExceptionDialog(
					YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames
				);
			}

			yamm.status.setStatus(YAMM.getString("msg.done"));
			yamm.status.progress(100);

			yamm.tree.repaint();
			yamm.status.setStatus(YAMM.getString("msg.new"));
			yamm.status.progress(0);
		}

		knappen.setEnabled(true);
		YAMM.setProperty("stat.sentnum", "" + sentnum);
		YAMM.setProperty("stat.receivednum", "" + receivednum);
		YAMM.setProperty("stat.sentbytes", "" + sentbytes);
		YAMM.setProperty("stat.receivedbytes", "" + receivedbytes);
	}
}
/*
 * Changes
 * $Log: SHMail.java,v $
 * Revision 1.39  2003/04/19 11:52:52  fredde
 * updated to work with the new mbox-format
 *
 * Revision 1.38  2003/04/16 12:37:35  fredde
 * gets delete-property from server configuration
 *
 * Revision 1.37  2003/03/15 19:50:20  fredde
 * localized 'you have new mail!'-string
 *
 * Revision 1.36  2003/03/12 20:19:22  fredde
 * added sent/received stats
 *
 * Revision 1.35  2003/03/11 15:16:34  fredde
 * Filter needs yamm reference in its constructor
 *
 * Revision 1.34  2003/03/10 11:00:09  fredde
 * now uses the new index system better. removed sent-check
 *
 * Revision 1.33  2003/03/10 09:41:00  fredde
 * non localized box filenames
 *
 * Revision 1.32  2003/03/09 17:45:04  fredde
 * now uses the new index system
 *
 * Revision 1.31  2003/03/09 14:09:05  fredde
 * variable frame renamed to yamm
 *
 * Revision 1.30  2003/03/07 20:20:58  fredde
 * moved more getProperty stuff to init. Now tells the user that he got mail if he did
 *
 * Revision 1.29  2003/03/06 23:04:42  fredde
 * only send email information with the smtp.to-command
 *
 * Revision 1.28  2000/12/31 14:05:07  fredde
 * better progressbars
 *
 * Revision 1.27  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.26  2000/04/01 21:26:14  fredde
 * email parsing fixed...
 *
 * Revision 1.25  2000/04/01 20:50:09  fredde
 * fixed to make the profiling system work
 *
 * Revision 1.24  2000/03/18 17:43:03  fredde
 * forgot a ")"... ofcourse...
 *
 * Revision 1.23  2000/03/18 17:04:49  fredde
 * updates the tree when sending/getting mail
 *
 * Revision 1.22  2000/03/18 14:51:29  fredde
 * removed unused code, gets the port-number for pop3 from config
 *
 * Revision 1.21  2000/03/14 21:18:14  fredde
 * better network cleanup
 *
 * Revision 1.20  2000/03/12 19:37:02  fredde
 * can now send attachments that other mail clients can view...
 *
 * Revision 1.19  2000/03/12 17:20:28  fredde
 * now multiple receivers work... ARGH
 *
 * Revision 1.18  2000/02/28 13:44:29  fredde
 * added new classes
 *
 */
