/*  Mailbox.java - box handling system
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

import java.text.ParseException;
import java.io.*;
import java.util.*;
import javax.swing.JTextArea;
import org.gjt.fredde.yamm.YAMM;

/**
 * A class that handels messages and information about messages
 */
public class Mailbox {

	/**
	 * puts the source code in a JTextArea.
	 * @param whichBox Which box the message to view is in
	 * @param whichmail Which mail to view
	 * @param jtarea Which JTextArea to append the source to
	 */
	public static void viewSource(String whichBox, int whichmail,
							JTextArea jtarea) {
		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(whichBox)));
			int i = 0;
			String temp;

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				}

				if (whichmail != i && temp.equals(".")) {
					i++;
				} else if(whichmail == i) {
					if (!temp.equals(".")) {
						jtarea.append(temp + "\n");
					} else  {
						break;
					}
				}
			}
			in.close();
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
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

		String temp = null;
		mailList.clear();
		Vector vec1 = new Vector();
		int makeItem = 0;

		int i = 0;

		if (!hasMail(whichBox)) {
			return;
		}

		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(whichBox)));

			MessageHeaderParser mhp = new MessageHeaderParser();
			DateParser dp = new DateParser();
			dp.setTargetFormat(YAMM.getString("shortdate"));

			try {
				mhp.parse(in);
			} catch (MessageParseException mpe) {
				System.err.println(mpe);
			}

			subject = mhp.getHeaderField("Subject");
			from    = mhp.getHeaderField("From");
			date    = mhp.getHeaderField("Date");
			status  = mhp.getHeaderField("YAMM-Status");
			
			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (temp.equals(".")) {
					vec1.insertElementAt(
							Integer.toString(i), 0);

					if (subject != null) {
						vec1.insertElementAt(subject,
									1);
					} else {
						vec1.insertElementAt("", 1);
					}

					if (from != null) {
						vec1.insertElementAt(from, 2);
					} else {
						vec1.insertElementAt("", 2);
					}

					if (date != null) {
						try {
							date = dp.parse(date);
						} catch (ParseException pe) {
							date = mhp.
							getHeaderField("Date");
						}
						vec1.insertElementAt(date, 3);
					} else {
						vec1.insertElementAt("", 3);
					}

					vec1.insertElementAt(status, 4);

					mailList.insertElementAt(vec1, i);
					vec1 = new Vector();
					i++;

					try {
						mhp.parse(in);
					} catch (MessageParseException mpe) {
						break;
					}
					subject = mhp.getHeaderField("Subject");
					from    = mhp.getHeaderField("From");
					date    = mhp.getHeaderField("Date");
					status  = mhp.getHeaderField("YAMM-" +
								"Status");
				}
			}
			in.close();
		} catch(IOException ioe) {
			System.out.println("Error: " + ioe);
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
		Vector vec1 = new Vector();
		boolean makeItem = false;

		int i = 0;

		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(YAMM.home +
							"/boxes/.filter")));

			MessageHeaderParser mhp = new MessageHeaderParser();

			try {
				mhp.parse(in);
			} catch (MessageParseException mpe) {
				System.err.println(mpe);
			}

			subject = mhp.getHeaderField("Subject");
			from    = mhp.getHeaderField("From");
			to      = mhp.getHeaderField("To");
			reply   = mhp.getHeaderField("Reply-To");

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
						vec1.insertElementAt(subject,
									2);
					} else {
						vec1.insertElementAt("", 2);
					}
					if (reply != null) {
						vec1.insertElementAt(reply, 3);
					} else {
						vec1.insertElementAt("", 3);
					}
					list.insertElementAt(vec1, i);
					vec1 = new Vector();
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
			in.close();
		} catch(IOException ioe) {
			System.err.println(ioe);
		}
	}

	/**
	 * This function sets the status of the mail.
	 * @param whichBox The box the message is in.
	 * @param whichmail The mail to set the status for.
	 * @param status The status string.
	 */
	public static void setStatus(String whichBox, int whichmail,
								String status) {
		File source = new File(whichBox);
		File target = new File(whichBox + ".tmp");
		String temp = null;
		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(source)));
			PrintWriter out   = new PrintWriter(
						new BufferedOutputStream(
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
						} else if (temp.equals(".")) {
							i++;
							out.println(temp);
							break;
						}

						if (temp.startsWith("YAMM-" +
								"Status:")) {
							i++;
							temp = "YAMM-Status: "
								+ status;
							break;
						} else if (temp.equals("") &&
								header) {
							i++;
							temp = "YAMM-Status: "
								+ status + "\n";
							break;
						}

						if (!temp.equals("")) {
							header = true; 
						}
						out.println(temp);
					}
				}
				out.println(temp);
			}
			in.close();
			out.close();

			source.delete();
			target.renameTo(source);
		} catch (IOException ioe) {
			System.err.println("test:" + ioe);
		}
	}
 
	/**
	 * Prints the mail to ~home/.yamm/tmp/cache/<whichBox>/<whichmail>.html
	 * @param whichBox Which box the message is in
	 * @param whichmail Whichmail to export
	 * @param attach Which vector to add attachments to
	 */
	public static void getMail(String whichBox, int whichmail) {
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

		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(whichBox)));
			PrintWriter outFile = new PrintWriter(
						new BufferedOutputStream(
						new FileOutputStream(out)));

			int i = 0;

			for(;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				}

				if (i == whichmail) {
					MessageParser mp = new MessageParser(in,
						outFile, out.toString());
					break;
				} else if(temp.equals(".")) {
					 i++;
				}
			}
			in.close();
			outFile.close();
		} catch (IOException ioe) {
			System.err.println("Error: " + ioe);
		} catch (MessageParseException mpe) {
			System.err.println(mpe);
		}
	}

	/**
	 * Gets the from and subject field from specified message
	 * @param whichBox Which box the message is in
	 * @param whichmail Which mail to get the headers from
	 */
	public static String[] getMailForReplyHeaders(String whichBox,
								int whichmail) {

		String  temp = null;
		String  from = null;
		String  subject = null;

		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(whichBox)));
			int i = 0;

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (i == whichmail) {
					MessageHeaderParser mhp =
						new MessageHeaderParser();
					try {
						mhp.parse(in);
					} catch (MessageParseException mpe) {
						System.err.println(mpe);
					}

					from = mhp.getHeaderField("From");
					subject = mhp.getHeaderField("Subject");

					if (from == null) {
						from = "";
					}
					if (subject == null) {
						subject = "";
					}

					break;
				} else if (temp.equals(".")) {
					i++;
				}
			}
			in.close();
		} catch (IOException ioe) {
			System.out.println("Error: " + ioe);
		}
		String[] ret = {from,subject};

		return ret;
	}

	/**
	 * Gets the content of this mail and adds to the specified JTextArea
	 * @param whichBox Which box the message is in
	 * @param whichmail Which mail to export
	 * @param jtarea The JTextArea to append the message to
	 */
	public static void getMailForReply(String whichBox, int whichmail,
							JTextArea jtarea) {

		String  temp = null, boundary = null;
		boolean wait = true;

		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(whichBox)));
			int i = 0;

			for (;;) {
				temp = in.readLine();

				if (temp == null) {
					break;
				} else if (temp.equals(".")) {
					i++;
				} else if (temp.startsWith("From: ") &&
							i == whichmail) {
					wait = false;
				} else if (temp.equals("") && i == whichmail
								&& !wait) {
					for (;;) {
						jtarea.append(">" + temp +
									"\n");

						temp = in.readLine();

						if (temp == null) {
							break;
						}

						if (temp.indexOf("=") != -1) {
							temp = MessageBodyParser
								.unMime(temp,
									false);
						} else if (temp.indexOf("MIME")
							!= -1 || temp.indexOf(
								"mime") != -1) {
							temp = in.readLine();
							temp = in.readLine();

							if (temp.startsWith("--"
								+ boundary)) {

								for (;;) {
									temp = in.readLine();
									if(temp == null) break;
									else if(temp.equals("")) break;
								}
							}
						} else if (temp.startsWith("--"
							+ boundary) &&
							!temp.substring(14, 15).
							equals("-") &&
							!temp.substring(14,15).
								equals(" ")) {
									break;
						} else if (temp.equals(".")) {
							break;
						}
					}
					break;
				}
			}
			in.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
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
			try {
				File inputFile = new File(whichBox);
				File outputFile = new File(whichBox + ".tmp");

				PrintWriter outFile = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(outputFile)));

				PrintWriter outFile2 = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(YAMM.home +
							"/boxes/" +
						YAMM.getString("box.trash"),
									true)));
				BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(inputFile)));

				int i = 0;

				for (;;) {
					temp = in.readLine();

					if (temp == null) {
						break;
					} else if (firstmail &&
							i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();
          
							if (temp == null) {
								break;
							} else if(temp.equals
									(".")) {
								outFile.println
									(temp);
								break;
							}
						} 
						firstmail = false;
					} else if (firstmail &&
							i == whichmail[next]) {
						for (;;) {
							outFile2.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if(temp.equals
									(".")) {
								outFile2.println
									(temp);
								break;
							}
						}
						if (!(next++ < whichmail.length
									- 1)) {
							break;
						}
						firstmail = false;
					} else if (!firstmail &&
							i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals
									(".")) {
								outFile.println
									(temp);
								break;
							}
						}
					} else if(!firstmail &&
							i == whichmail[next]) {

						for (;;) {
							outFile2.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals
									(".")) {
								outFile2.println
									(temp);
								break;
							}
						}
						if (!(next++ < whichmail.length
									- 1)) {
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
				in.close();
				outFile.close();
				outFile2.close();

				inputFile.delete();
				if (!outputFile.renameTo(inputFile)) {
					System.err.println("ERROR: Couldn't " +
							"rename " + whichBox +
							".tmp to " + whichBox);
				}
			} catch (IOException ioe) {
				System.out.println("Error: " + ioe);
				return false;
			}
		} else if (whichBox.equals(YAMM.home + sep + "boxes" + sep +
						YAMM.getString("box.trash"))) {
			try {
				File inputFile = new File(YAMM.home + "/boxes/"
						+ YAMM.getString("box.trash"));
				File outputFile = new File(YAMM.home + "/boxes/"						+ YAMM.getString("box.trash") +
									".tmp");

				BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(inputFile)));

				PrintWriter outFile = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(outputFile)));

				int i = 0;

				for (;;) {
					temp = in.readLine();

					if (temp == null) {
						break;
					} else if (firstmail &&
							i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals
									(".")) {
								outFile.println
									(temp);
								break;
							}
						} 
						firstmail = false;
					} else if(firstmail &&
							i == whichmail[next]) {

						for (;;) {
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals
									(".")) {
								break;
							}
						}
						if (!(next++ < whichmail.length
									- 1)) {
							break;
						}
						firstmail = false;
					} else if (!firstmail &&
							i != whichmail[next]) {
						for (;;) {
							outFile.println(temp);
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals
									(".")) {
								outFile.println
									(temp);
								break;
							}
						}
					} else if (!firstmail &&
							i == whichmail[next]) {
						for (;;) {
							temp = in.readLine();

							if (temp == null) {
								break;
							} else if (temp.equals
									(".")) {
								break;
							}
						}
						if (!(next++ < whichmail.length
									- 1)) {
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

				in.close();
				outFile.close();
				inputFile.delete();

				if (!outputFile.renameTo(inputFile)) {
					System.err.println("ERROR: Couldn't " +
							"rename " + whichBox +
							".tmp to " + whichBox);
				}
			} catch(IOException ioe) {
				System.out.println("Error: " + ioe);
				return false;
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

		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(fromBox)));

			PrintWriter outFile2 = new PrintWriter(
					new BufferedOutputStream(
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
			in.close();
			outFile2.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
			return false;
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

		try {
			File inputFile = new File(fromBox);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(inputFile)));

			File outputFile = new File(fromBox + ".tmp");

			PrintWriter outFile = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(outputFile)));

			PrintWriter outFile2 = new PrintWriter(
					new BufferedOutputStream(
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

			in.close();
			outFile.close();
			outFile2.close();

			inputFile.delete();

			if (!outputFile.renameTo(inputFile)) {
				System.err.println("Couldn't rename " +
							fromBox + ".tmp to " +
								fromBox);
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
			return false;
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

		try {
			BufferedReader in = new BufferedReader(
						new InputStreamReader(
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
			System.err.println(ioe);
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

		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
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
			in.close();
		} catch (IOException ioe) {
			System.err.println(ioe);

			return false;
		}
		return mail;
	}
}
