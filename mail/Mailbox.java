/*  Mailbox.java - box handling system
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

import java.text.ParseException;
import java.io.*;
import java.util.*;
import javax.swing.JTextArea;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.util.gui.ExceptionDialog;

/**
 * A class that handels messages and information about messages
 * @author Fredrik Ehnbom
 * @version $Id: Mailbox.java,v 1.33 2000/04/01 20:50:09 fredde Exp $
 */
public class Mailbox {

	/**
	 * removes mime from the subject
	 * @param subject The subject to remove the mime tags from
	 */
	public static String unMime(String subject) {
		if (subject == null) {
			return null;
		}

		if (subject.indexOf("=?") == -1) {
			return subject;
		}

		String start = subject.substring(0, subject.indexOf("=?")); 
		String end   = subject.substring(subject.lastIndexOf("?=") + 2,
							subject.length());
		subject      = subject.substring(subject.indexOf("Q?") + 2,
						subject.lastIndexOf("?="));

		subject = subject.replace('_', ' ');
		start = start.replace('_', ' ');
		end = end.replace('_', ' ');
		subject = MessageBodyParser.unMime(subject, false);

		return start + subject + end;
	}

	/**
	 * puts the source code in a JTextArea.
	 * @param whichBox Which box the message to view is in
	 * @param whichmail Which mail to view
	 * @param jtarea Which JTextArea to append the source to
	 * @return The number of rows added to the JTextArea
	 */
	public static int viewSource(String whichBox, int whichmail, long skip,
							JTextArea jtarea) {
		BufferedReader in = null;

		try {
			int ret = 0;
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(whichBox)));
			String temp;

			in.skip(skip);

			while ((temp = in.readLine()) != null) {
				if (!temp.equals(".")) {
					jtarea.append(temp + "\n");
					ret++;
				} else  {
					break;
				}
			}
			return ret;
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
			return -1;
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}
	}

	private static String removeQuote(String quote) {
		quote = quote.replace('\"', '|');

		while (quote.indexOf("|") != -1) {
			quote = quote.substring(0, quote.indexOf("|")) +
				"\\\"" +
				quote.substring(quote.indexOf("|") + 1,
								quote.length());
		}

		return quote;
	}

	/**
	 * Updates the index for the mails in the current box
	 * @param whichBox The box to update the index for
	 */
	public static void updateIndex(String whichBox) {
		long skipnum = 0;
		long skipped = 0;

		String subject = null;
		String from = null;
		String date = null;
		String status = null;
		String temp = null;
		int sep = whichBox.lastIndexOf(YAMM.sep);

		String target = whichBox.substring(0, sep + 1) +
				"." + 
				whichBox.substring(sep + 1, whichBox.length()) +
				".index";

		BufferedReader in = null;
		PrintWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(whichBox)));

			out   = new PrintWriter(new BufferedOutputStream(
						new FileOutputStream(target)));
			int i = 0;
			int unread = 0;

			if (hasMail(whichBox)) {
				MessageHeaderParser mhp = new MessageHeaderParser();
				DateParser dp = new DateParser();
				dp.setTargetFormat(YAMM.getString("shortdate"));

				try {
					skipnum = mhp.parse(in);
				} catch (MessageParseException mpe) {
					new ExceptionDialog(YAMM.getString("msg.error"),
						mpe,
						YAMM.exceptionNames);
				}

				subject = mhp.getHeaderField("Subject");
				from    = mhp.getHeaderField("From");
				date    = mhp.getHeaderField("Date");
				status  = mhp.getHeaderField("YAMM-Status");

				if (status == null) {
					status = "Unread";
					unread++;
				}
				if (from == null)
					from = "";
				if (subject == null)
					subject = "";


				subject = removeQuote(subject);
				subject = unMime(subject);
				from = removeQuote(from);


				for (;;) {
					temp = in.readLine();
					if (temp == null)
						break;

					skipnum += temp.length() + System.getProperty
							("line.separator").length();

					if (temp.equals(".")) {
						out.print(i + " ");

						if (subject != null) {
							out.print("\"" + subject +
								"\" ");
						} else {
							out.print("\"\" ");
						}
                                         
						if (from != null) {
							out.print("\"" + from + "\" ");
						} else {
							out.print("\"\" ");
						}

						if (date != null) {
							try {
								date = dp.parse(date);
							} catch (ParseException pe) {
								date = mhp.
								getHeaderField("Date");
							}
							out.print("\"" +date + "\" ");
						} else {
							out.print("\"\" ");
						}                                   

						out.print("\"" + status + "\" ");

						out.println(skipped);
						out.flush();
						i++;

						skipped = skipnum;

						try {
							skipnum += mhp.parse(in);
						} catch (MessageParseException mpe) {
							break;
						}
						subject = mhp.getHeaderField("Subject");
						from    = mhp.getHeaderField("From");
						date    = mhp.getHeaderField("Date");
						status  = mhp.getHeaderField("YAMM-" +
								"Status");
						if (status == null) {
							status = "Unread";
							unread++;
						}
						if (from == null)
							from = "";
						if (subject == null)
							subject = "";

						subject = removeQuote(subject);
						subject = unMime(subject);
						from = removeQuote(from);
					}
				}
				in.close();
				out.close();

				in = new BufferedReader(new InputStreamReader(
							new FileInputStream(target)));

				out = new PrintWriter(new BufferedOutputStream(
							new FileOutputStream(target + ".tmp")));

				out.println(i + ", " + unread);

				for (;;) {
					String tmp = in.readLine();

					if (tmp == null) break;
					else out.println(tmp);
				}

				File tmp = new File(target + ".tmp");
				File old = new File(target);
				old.delete();
				tmp.renameTo(old);
			} else {
				out.println("0, 0");
			}
		} catch(IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (IOException ioe) {}
		}
	}

	public static int[] getUnread(String box) {

		int sep = box.lastIndexOf(YAMM.sep);

		String box2 = box.substring(0, sep + 1) +
				"." + 
				box.substring(sep + 1, box.length()) +
				".index";

		File indexfile = new File(box2);

		if (!indexfile.exists()) {
			updateIndex(box);
		}

		BufferedReader in = null;

		String tmp = null;
		int unread[] = new int[2];

		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(box2)));

			tmp = in.readLine();
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}

		if (tmp != null) {
//			tmp = tmp.substring(tmp.indexOf(",") + 2, tmp.length()).trim();
			unread[0] = Integer.parseInt(tmp.substring(0, tmp.indexOf(",")));
			unread[1] = Integer.parseInt(tmp.substring(tmp.indexOf(",") + 2, tmp.length()));
		}

		return unread;
	}
	

	/**
	 * Creates a list with the mails in this box.
	 * @param whichBox Which box to get messages from.
	 * @param mailList The vector that should contain the information
	 */
	public static void createList(String whichBox, Vector mailList) {
		String subject = null;
		String from = null;
		String date = null;
		String status = null;

//		String temp = null;
		mailList.clear();
		mailList.ensureCapacity(getUnread(whichBox)[0]);
		Vector vec1 = new Vector(6);
		int sep = whichBox.lastIndexOf(YAMM.sep);

		String box = whichBox.substring(0, sep + 1) +
				"." + 
				whichBox.substring(sep + 1, whichBox.length()) +
				".index";

		int i = 0;

		if (!hasMail(whichBox)) {
			return;
		}
		File indexfile = new File(box);

		Date boxdate = new Date(new File(whichBox).lastModified());
		Date indexdate = new Date(indexfile.lastModified());

		if (!indexfile.exists() || boxdate.after(indexdate)) {
			updateIndex(whichBox);
		}

		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(box)));

			in.readLine(); // skip messages, unread messages header
			StreamTokenizer tok = new StreamTokenizer(in);
			
			for (;;) {
				if (tok.nextToken() == StreamTokenizer.TT_EOF) {
					break;
				}



				int num = (int) tok.nval;
				vec1.insertElementAt(
					Integer.toString(num), 0);

				tok.nextToken();
				vec1.insertElementAt(tok.sval, 1);

				tok.nextToken();
				vec1.insertElementAt(tok.sval, 2);

				tok.nextToken();
				vec1.insertElementAt(tok.sval, 3);

				tok.nextToken();
				vec1.insertElementAt(tok.sval, 4);

				tok.nextToken();
				vec1.insertElementAt("" + (long) tok.nval, 5);

				mailList.insertElementAt(vec1, i);
				vec1 = new Vector(6);
				i++;

			}
		} catch(IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}
	}

	/**
	 * Creates a list with the mails in the filter box.
	 * @param mailList The vector that should contain the information
	 */
	public static void createFilterList(Vector list) {
		String subject = null;
		String from = null;
		String to = null;
		String reply = null;

		String temp = null;
		list.clear();
		Vector vec1 = new Vector(4);
		boolean makeItem = false;

		int i = 0;

		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(YAMM.home +
							"/boxes/.filter")));

			MessageHeaderParser mhp = new MessageHeaderParser();

			try {
				mhp.parse(in);
			} catch (MessageParseException mpe) {
				new ExceptionDialog(YAMM.getString("msg.error"),
						mpe,
						YAMM.exceptionNames);
			}

			subject = mhp.getHeaderField("Subject");
			from    = mhp.getHeaderField("From");
			to      = mhp.getHeaderField("To");
			reply   = mhp.getHeaderField("Reply-To");
			subject = unMime(subject);

			for (;;) {
				temp = in.readLine();
      
				if (temp == null) {
					break;
				} else if(temp.equals(".")) {
					if (from != null) {
						vec1.insertElementAt(from, 0);
					} else {
						vec1.insertElementAt("", 0);
					}
					if (to != null) {
						vec1.insertElementAt(to, 1);
					} else {
						vec1.insertElementAt("", 1);
					}
					if (subject != null) {
						vec1.insertElementAt(subject, 2);
					} else {
						vec1.insertElementAt("", 2);
					}
					if (reply != null) {
						vec1.insertElementAt(reply, 3);
					} else {
						vec1.insertElementAt("", 3);
					}
					list.insertElementAt(vec1, i);
					vec1 = new Vector(4);
					makeItem = false;
					i++;

					try {
						mhp.parse(in);
					} catch (MessageParseException mpe) {
						break;
					}

					subject = mhp.getHeaderField("Subject");
					from = mhp.getHeaderField("From");
					to = mhp.getHeaderField("To");
					reply = mhp.getHeaderField("Reply-To");

				}
			}
		} catch(IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}
	}

	private static void setIndexStatus(String whichBox, int whichmail,
								String status) {

		int sep = whichBox.lastIndexOf(YAMM.sep);
		String box = whichBox.substring(0, sep + 1) +
				"." + 
				whichBox.substring(sep + 1, whichBox.length()) +
				".index";

		boolean add = false;

		File source = new File(box);
		File target = new File(box + ".tmp");

		BufferedReader in = null;
		PrintWriter out = null;

		int[] unread = getUnread(whichBox);
		unread[1]--;
		
		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(source)));
			out   = new PrintWriter(new BufferedOutputStream(
						new FileOutputStream(target)));

			in.readLine(); // skip unread, total msg header
			out.println(unread[0] + ", " + unread[1]);
			StreamTokenizer tok = new StreamTokenizer(in);

			for (int i = 0; i < whichmail; i++) {
				out.println(in.readLine());
			}

			tok.nextToken();
			out.print((int) tok.nval + " ");

			tok.nextToken();
			out.print("\"" + removeQuote(tok.sval) + "\" ");
				
			tok.nextToken();
			out.print("\"" + removeQuote(tok.sval) + "\" ");

			tok.nextToken();
			out.print("\"" + tok.sval + "\" ");

			tok.nextToken();
			out.print("\"" + status + "\" ");
			if (tok.sval.equals("Unread")) {
				add = true;
			}

			tok.nextToken();
			out.println((long) tok.nval + "");


			String tmp = null;

			if (!add) {
				while ((tmp = in.readLine()) != null) {
					out.println(tmp);
				}
			} else {
				while ((tmp = in.readLine()) != null) {
					if (tmp.equals("")) continue;

					long skip = Long.parseLong(
						tmp.substring(
						tmp.lastIndexOf(" ") + 1,
						tmp.length()));


					skip += status.length() + 13 +
						System.getProperty
						("line.separator").length();

					tmp = tmp.substring(0,
						tmp.lastIndexOf(" ") + 1);

					out.print(tmp);
					out.println(skip + "");
				}
			}
			source.delete();
			target.renameTo(source);
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (IOException ioe) {}
		}
	} 

		

	/**
	 * This function sets the status of the mail.
	 * @param whichBox The box the message is in.
	 * @param whichmail The mail to set the status for.
	 * @param status The status string.
	 */
	public static void setStatus(String whichBox, int whichmail, long skip,
								String status) {
		File source = new File(whichBox);
		File target = new File(whichBox + ".tmp");
		String temp = null;

		BufferedReader in = null;
		PrintWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(source)));
			out   = new PrintWriter(new BufferedOutputStream(
						new FileOutputStream(target)));
			int i = 0;

			for (;;) {
				temp = in.readLine();
  
				if (temp == null) {
					break;
				} else if (temp.equals(".")) {
					 i++;
				}

				if (i == whichmail) {
					out.println(temp);
					boolean header = false;

					for (;;) {
						temp = in.readLine();

						if (temp == null) {
							break;
						}

						if (temp.startsWith("YAMM-" +
								"Status:")) {
							i++;
							temp = "YAMM-Status: " +
									status;
							break;
						} else if (temp.equals("") &&
								header) {
							i++;
							temp = "YAMM-Status: " +
								status + "\n";
							break;
						} else if (!temp.equals("")) {
							header = true;
						}
						out.println(temp);
					}
				}
				out.println(temp);
			}
			source.delete();
			target.renameTo(source);
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
				ioe,
				YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (IOException ioe) {}
		}
		setIndexStatus(whichBox, whichmail, status);
	}
 
	/**
	 * Prints the mail to ~home/.yamm/tmp/cache/<whichBox>/<whichmail>.html
	 * @param whichBox Which box the message is in
	 * @param whichmail Whichmail to export
	 * @param attach Which vector to add attachments to
	 */
	public static void getMail(String whichBox, int whichmail, long skip) {
		String  temp = YAMM.home + YAMM.sep;
		int attaches = 0;

		String tempdir = temp + "tmp/";
		String boxpath = whichBox.substring(whichBox.indexOf("boxes") +
							6, whichBox.length());
		File   cache = new File(tempdir + "cache/" + boxpath + "/");
		File   out   = new File(cache, whichmail + ".html");


		if (!new File(cache, whichmail + ".html").exists()) {
			if (!cache.exists()) {
				if (!cache.mkdirs()) {
					System.err.println("Couldn't create" +
						" dir: " + cache.toString());
				}
			}
		}

		if (!hasMail(whichBox)) {
			return;
		}

		BufferedReader in = null;
		PrintWriter outFile = null;

		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(whichBox)));
			outFile = new PrintWriter(new BufferedOutputStream(
						new FileOutputStream(out)));

			in.skip(skip);

			MessageParser mp = new MessageParser(in,
						outFile, out.toString());

		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} catch (MessageParseException mpe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					mpe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
				if (outFile != null) outFile.close();
			} catch (IOException ioe) {}
		}
	}

	/**
	 * Gets the from and subject field from specified message
	 * @param whichBox Which box the message is in
	 * @param whichmail Which mail to get the headers from
	 */
	public static String[] getMailForReplyHeaders(String whichBox, long skip) {
		String  temp = null;
		String  from = null;
		String  finalFrom = "";
		String  email = null;
		String  subject = null;
		String  to = null;

		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(whichBox)));

			in.skip(skip);

			MessageHeaderParser mhp = new MessageHeaderParser();
			try {
				mhp.parse(in);
			} catch (MessageParseException mpe) {
				new ExceptionDialog(YAMM.getString("msg.error"),
						mpe,
						YAMM.exceptionNames);
			}

			if (mhp.getHeaderField("Reply-To") != null) {
				from = mhp.getHeaderField("Reply-To");
			} else {
				from = mhp.getHeaderField("From");
			}

			subject = mhp.getHeaderField("Subject");
			subject = unMime(subject);

			if (from == null) {
				from = "";
			} else {
				StringTokenizer tok = new StringTokenizer(from);

				while (tok.hasMoreTokens()) {
					email = tok.nextToken();

					if (email.indexOf("@") != -1) {
						email = MessageParser.
							parseLink(email)[1];
					//		break;
					} else {
						finalFrom += email + " ";
					}
				}
				if (finalFrom.trim().equals("")) {
					finalFrom = email + " ";
				}
			}

			if (subject == null) {
				subject = "";
			}

			to = mhp.getHeaderField("To");

			if (to == null) {
				to = "";
			}
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
				ioe,
				YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}

		String[] ret = {
				finalFrom,
				to,
				email,
				subject
		};

		return ret;
	}

	/**
	 * Gets the content of this mail and adds to the specified JTextArea
	 * @param whichBox Which box the message is in
	 * @param whichmail Which mail to export
	 * @param jtarea The JTextArea to append the message to
	 */
	public static void getMailForReply(String whichBox, int whichmail,
							long skip,
							JTextArea jtarea) {

		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(whichBox)));

			in.skip(skip);


			ReplyMessageParser rmp = new ReplyMessageParser(in,
									jtarea);
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} catch (MessageParseException mpe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					mpe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}
	}

	/**
	 * Deletes the specified mail. Returns false if it fails.
	 * @param whichBox The box the message is in
	 * @param whichmail The mail to delete
	 */ 
	public static boolean deleteMail(String whichBox, int[] whichmail) {
		String temp = null;
		boolean firstmail = true;
		String home = System.getProperty("user.home");
		String sep = System.getProperty("file.separator");
		int next = 0;

		if (!whichBox.equals(YAMM.home + sep + "boxes" + sep +
						YAMM.getString("box.trash"))) {

			BufferedReader in = null;
			PrintWriter outFile = null;
			PrintWriter outFile2 = null;

			try {
				File inputFile = new File(whichBox);
				File outputFile = new File(whichBox + ".tmp");

				outFile = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(outputFile)));

				outFile2 = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(YAMM.home +
							"/boxes/" +
						YAMM.getString("box.trash"),
									true)));
				in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));

				int i = 0;

				for (;;) {
					temp = in.readLine();

					if (temp == null) {
						break;
					} else if (firstmail && i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();
          
							if (temp == null) {
								break;
							} else if(temp.equals(".")) {
								outFile.println(temp);
								break;
							}
						} 
						firstmail = false;
					} else if (firstmail && i == whichmail[next]) {
						for (;;) {
							outFile2.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if(temp.equals(".")) {
								outFile2.println(temp);
								break;
							}
						}
						if (!(next++ < whichmail.length	- 1)) {
							break;
						}
						firstmail = false;
					} else if (!firstmail && i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals(".")) {
								outFile.println(temp);
								break;
							}
						}
					} else if(!firstmail && i == whichmail[next]) {

						for (;;) {
							outFile2.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals(".")) {
								outFile2.println(temp);
								break;
							}
						}
						if (!(next++ < whichmail.length	- 1)) {
							break;
						}
					}
					i++;
				}
				for (;;) {
					temp = in.readLine();

					if (temp == null) {
						break;
					} else {
						outFile.println(temp);
					}
				}
				inputFile.delete();
				if (!outputFile.renameTo(inputFile)) {
					System.err.println("ERROR: Couldn't " +
							"rename " + whichBox +
							".tmp to " + whichBox);
				}
			} catch (IOException ioe) {
				new ExceptionDialog(YAMM.getString("msg.error"),
						ioe,
						YAMM.exceptionNames);
				return false;
			} finally {
				try {
					if (in != null) in.close();
					if (outFile != null) outFile.close();
					if (outFile2 != null) outFile2.close();
				} catch (IOException ioe) {}
			}
		} else if (whichBox.equals(YAMM.home + sep + "boxes" + sep +
						YAMM.getString("box.trash"))) {

			BufferedReader in = null;
			PrintWriter outFile = null;

			try {
				File inputFile = new File(YAMM.home + "/boxes/"
						+ YAMM.getString("box.trash"));
				File outputFile = new File(YAMM.home + "/boxes/"
					+ YAMM.getString("box.trash") +	".tmp");

				in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));

				outFile = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream(outputFile)));

				int i = 0;

				for (;;) {
					temp = in.readLine();

					if (temp == null) {
						break;
					} else if (firstmail && i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals(".")) {
								outFile.println(temp);
								break;
							}
						} 
						firstmail = false;
					} else if(firstmail && i == whichmail[next]) {

						for (;;) {
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals(".")) {
								break;
							}
						}
						if (!(next++ < whichmail.length	- 1)) {
							break;
						}
						firstmail = false;
					} else if (!firstmail && i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals(".")) {
								outFile.println(temp);
								break;
							}
						}
					} else if (!firstmail && i == whichmail[next]) {
						for (;;) {
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals(".")) {
								break;
							}
						}
						if (!(next++ < whichmail.length - 1)) {
							break;
						}
					}
					i++;
				}
				for (;;) {
					temp = in.readLine();

					if (temp == null) {
						break;
					} else {
						outFile.println(temp);
					}
				}

				inputFile.delete();

				if (!outputFile.renameTo(inputFile)) {
					System.err.println("ERROR: Couldn't " +
							"rename " + whichBox +
							".tmp to " + whichBox);
				}
			} catch(IOException ioe) {
				new ExceptionDialog(YAMM.getString("msg.error"),
						ioe,
						YAMM.exceptionNames);
				return false;
			} finally {
				try {
					if (in != null) in.close();
					if (outFile != null) outFile.close();
				} catch (IOException ioe) {}
			}
		}
		return true;
	}

	/**
	 * This function tries to copy a mail from a box to another.
	 * @param fromBox From which box
	 * @param toBox To which box
	 * @param whichmail The mail to copy from fromBox to toBox
	 */
	public static boolean copyMail(String fromBox, String toBox,
							int[] whichmail) {

		String  temp = null;
		boolean firstmail = true;
		int next = 0;

		BufferedReader in = null;
		PrintWriter outFile2 = null;

		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fromBox)));

			outFile2 = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream(toBox, true)));

			int i = 0;

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (firstmail && i != whichmail[next]) {
					for (;;) {
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							break;
						}
					}
					firstmail = false;
				} else if (firstmail && i == whichmail[next]) {
					for (;;) {
						outFile2.println(temp);
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							outFile2.println(temp);
							break;
						}
					}
					if (!(next++ < whichmail.length - 1)) {
						break;
					}
					firstmail = false;
				} else if(!firstmail && i != whichmail[next]) {
					for (;;) {
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							break;
						}
					}
				} else if(!firstmail && i == whichmail[next]) {
					for (;;) {
						outFile2.println(temp);
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							outFile2.println(temp);
							break;
						}
					}
					if (!(next++ < whichmail.length - 1)) {
						break;
					}
				}
				i++;
			}
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
						ioe,
						YAMM.exceptionNames);
			return false;
		} finally {
			try {
				if (in != null) in.close();
				if (outFile2 != null) outFile2.close();
			} catch (IOException ioe) {}
		}
		return true;
	}

	/**
	 * Moves a mail from one box to another.
	 * @param fromBox The box to move the mail from
	 * @param toBox The box to move the mail to
	 * @param whichmail The mail to move
	 */
	public static boolean moveMail(String fromBox, String toBox,
							int[] whichmail) {
		String temp = null;
		boolean firstmail = true;
		int next = 0;


		if (fromBox.equals(toBox)) {
			return false;
		}

		BufferedReader in = null;
		PrintWriter outFile = null;
		PrintWriter outFile2 = null;

		try {
			File inputFile = new File(fromBox);

			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));

			File outputFile = new File(fromBox + ".tmp");

			outFile = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream(outputFile)));

			outFile2 = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream(toBox, true)));

			int i = 0;

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (firstmail && i != whichmail[next]) {
					for (;;) {
						outFile.println(temp);
						temp = in.readLine();
        
						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							outFile.println(temp);
							break;
						}
					} 
					firstmail = false;
				} else if (firstmail && i == whichmail[next]) {
					for (;;) {
						outFile2.println(temp);
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							outFile2.println(temp);
							break;
						}
					}
					if (!(next++ < whichmail.length - 1)) {
						break;
					}
					firstmail = false;
				} else if (!firstmail && i != whichmail[next]) {
					for (;;) {
						outFile.println(temp);
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							outFile.println(temp);
							break;
						}
					}
				} else if (i == whichmail[next]) {
					for (;;) {
						outFile2.println(temp);
						temp = in.readLine();

						if (temp == null) {
							break;
						} else if (temp.equals(".")) {
							outFile2.println(temp);
							break;
						}
					}
					if (!(next++ < whichmail.length - 1)) {
						break;
					}
				}
				i++;
			}
			for (;;) {
				temp = in.readLine();
				if (temp == null) {
					break;
				} else {
					outFile.println(temp);
				}
			}
			inputFile.delete();

			if (!outputFile.renameTo(inputFile)) {
				System.err.println("Couldn't rename " +
							fromBox + ".tmp to " +
								fromBox);
			}
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
			return false;
		} finally {
			try {
				if (in != null) in.close();
				if (outFile != null) outFile.close();
				if (outFile2 != null) outFile2.close();
			} catch (IOException ioe) {}
		}
		return true;
	}

	/**
	 * Counts how many mails the box have.
	 * @param whichbox The box to count the mails in
	 */
	public static int mailsInBox(String whichbox) {
		String temp = null;

		int i = 0;

		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(
						new FileInputStream(whichbox)));

			for(;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (temp.equals(".")) {
					i++;
				}
			}
		} catch(IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}
		return i;
	}

	/**
	 * Checks if the box has mail in it.
	 * @param whichBox The box to check for mail
	 */
	public static boolean hasMail(String whichBox) {
		String temp = null;
		boolean mail = false;

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(whichBox)));

			for(;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (temp.indexOf(":") != -1) {
					mail = true;
					break;
				}
			}
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
						ioe,
						YAMM.exceptionNames);

			return false;
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {}
		}
		return mail;
	}
}
/*
 * Changes:
 * $Log: Mailbox.java,v $
 * Revision 1.33  2000/04/01 20:50:09  fredde
 * fixed to make the profiling system work
 *
 * Revision 1.32  2000/03/25 15:46:52  fredde
 * updated the getMailForReplyHeaders method
 *
 * Revision 1.31  2000/03/15 11:13:45  fredde
 * boxes now show if and how many unread messages they have
 *
 * Revision 1.30  2000/03/14 21:19:12  fredde
 * better io-cleanup
 *
 * Revision 1.29  2000/03/12 17:18:05  fredde
 * still creates index when there's no subject/from field
 *
 */
