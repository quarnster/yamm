/*  SHMail.java - sends and gets mail
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
package org.gjt.fredde.yamm;

import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.net.*;
import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.mail.*;
import org.gjt.fredde.yamm.YAMM;

/**
 * Sends and gets mail
 * @author Fredrik Ehnbom
 * @version $Id: SHMail.java,v 1.29 2003/03/06 23:04:42 fredde Exp $
 */
public class SHMail extends Thread {

	/** Properties for smtpserver etc */
	static protected Properties props = new Properties();

	/**
	 * Wheter or not the messages should be putted in the "sent"-box
	 * after sending it
	 */
	static protected boolean sent = false;

	/** If pop-debugging info should be printed */
	static private boolean popdebug = false;

	/** If smtp-debugging info should be printed */
	static private boolean smtpdebug = false;


	JButton knappen;
	YAMM frame;

	/**
	 * Disables the send/get-button and inits some stuff
	 * @param frame1 the frame that will be used for error-msg etc.
	 * @param name the name of this thread.
	 * @param knapp the button to disable.
	 */
	public SHMail(YAMM frame1, String name, JButton knapp) {
		super(name);
		knappen = knapp;
		knappen.setEnabled(false);
		frame = frame1;

		if (YAMM.getProperty("debug.pop", "false").equals("true")) popdebug = true;
		if (YAMM.getProperty("debug.smtp", "false").equals("true")) smtpdebug = true;
	}

	/**
	 * Creates a new progress-dialog, checks for server configuration-files
	 * and connects to the servers with a config-file.
	 */
	public void run() {
		frame.status.progress(0);
		frame.status.setStatus("");

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
			String server   = props.getProperty("server");
			String username = props.getProperty("username");
			String password = new SimpleCrypt("myKey").decrypt(
						props.getProperty("password"));
			boolean del = false;
			if (YAMM.getProperty("delete", "true").equals("true")) {
				del = true;
			}
			if (YAMM.getProperty("sentbox","true").equals("true")) {
				sent = true;
			}

			int port = 110;
			try {
				port = Integer.parseInt(props.getProperty("port"));
			} catch (Exception e) {
				port = 110;
			}

			if (type != null && server != null && username != null && password != null) {
				if (type.equals("pop3")) {
					Object[] argstmp = {server};
					frame.status.setStatus(YAMM.getString("server.contact", argstmp));

					YammPop3 pop = null;

					try {
						pop = new YammPop3(username, password, server, YAMM.home + "/boxes/.filter", port, popdebug);
						int messages = pop.getMessageCount();

						for (int j = 1; j <= messages; j++) {
							pop.getMessage(j, messages, frame);

							if (del) pop.deleteMessage(j);
						}
						frame.status.setStatus(YAMM.getString("msg.done"));
						frame.status.progress(100);
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


		if (
			YAMM.getProperty("smtpserver") != null &&
			Mailbox.hasMail(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"))
		) {
			Smtp smtp = null;
			BufferedReader in = null;
			PrintWriter out = null;
			PrintWriter mail = null;

			try {
				File boxFile = new File(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"));
				smtp = new YammSmtp(YAMM.getProperty("smtpserver"), 25, smtpdebug);
				in = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(boxFile)
					)
				);

				if (sent) {
					out = new PrintWriter(
						new BufferedOutputStream(
	        					new FileOutputStream(YAMM.home + "/boxes/" + YAMM.getString("box.sent"), true)
						)
					);
				}

				String temp = null, from2 = null, to2 = null;
				String[][] listOfMails = Mailbox.createList(boxFile.toString());

				long last = Long.parseLong(listOfMails[0][5]);
				long length = 0;
				long read = 0;

				int i = 1;
				if (i >= listOfMails.length) {
					length = (boxFile.length() - last);
				} else {
					length = Long.parseLong(listOfMails[i][5]) - last;
					last = Long.parseLong(listOfMails[i][5]);
				}

				String mailStatus = YAMM.getString("server.send", new Object[] {"1"});

	        		frame.status.setStatus(mailStatus);
		        	frame.status.progress(0);


				for (;;) {
					temp = in.readLine();

					if (temp == null) break;

					read += temp.length() + 1;

					frame.status.setStatus(mailStatus + "  " + read + "/" + length);
					frame.status.progress((int) (((float) read / length) * 100));

					if (temp.startsWith("From:")) {
						String from = MessageParser.getEmail(temp);
						System.out.println("form: " + from);
						smtp.from(from);
						from2 = temp;
						if (sent) out.println(from2);
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
						if (sent) out.println(to2);
					} else if (temp.startsWith("Subject:")) {
						mail = smtp.getOutputStream();
						mail.println(from2 + "\n" + to2 + "\n" + temp);
						if (sent) out.println(temp);

						for (;;) {
							temp = in.readLine();

							if (temp == null) break;

							read += temp.length() + 1;
							frame.status.setStatus(mailStatus + "  " + read + "/" + length);
							frame.status.progress((int) (((float) read / length) * 100));

							if (sent) out.println(temp);

							if (temp.equals(".")) {
								smtp.sendMessage();

								i++;
								read = 0;

								mailStatus = YAMM.getString("server.send", new Object[] {"" + i});

								if (i >= listOfMails.length) {
										length = (boxFile.length() - last);
								} else {
									length = Long.parseLong(listOfMails[i][5]) - last;
					        			last = Long.parseLong(listOfMails[i][5]);
								}

								break;
							}
							mail.println(temp);
						}
						mail = null;
					} else {
						if (sent)
							out.println(temp);
						if (mail != null)
							mail.println(temp);
					}
				}
				in.close();

				File file = new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.outbox")));
				file.delete();
				file.createNewFile();
			} catch (IOException ioe) {
				new ExceptionDialog(YAMM.getString("msg.error"), ioe, YAMM.exceptionNames);
			} finally {
				try {
					if (in != null) in.close();
					if (sent && out != null) out.close();
					if (smtp != null) smtp.close();
					if (mail != null) mail.close();
				} catch (IOException ioe) {}
			}
			if (sent) {
				Mailbox.updateIndex(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.sent")));
				frame.tree.repaint();
			}
		}
		frame.status.setStatus(YAMM.getString("msg.done"));
		frame.status.progress(100);

		if (Mailbox.hasMail(YAMM.home + "/boxes/.filter")) {
			frame.status.setStatus(YAMM.getString("server.filter"));
			frame.status.progress(0);

			try {
				new Filter();
			} catch (IOException ioe) {
				new ExceptionDialog(
					YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames
				);
			}

			frame.status.setStatus(YAMM.getString("msg.done"));
			frame.status.progress(100);

			frame.tree.repaint();
		}
		frame.status.setStatus("");
		frame.status.progress(0);

		knappen.setEnabled(true);
	}
}
/*
 * Changes
 * $Log: SHMail.java,v $
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
