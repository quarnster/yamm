/*  $Id: mainMenu.java,v 1.36 2003/04/13 16:38:28 fredde Exp $
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
import javax.swing.filechooser.FileFilter;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import org.gjt.fredde.yamm.gui.sourceViewer;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.YAMMWrite;
// import org.gjt.fredde.yamm.Options;
import org.gjt.fredde.yamm.gui.confwiz.ConfigurationWizard;
import org.gjt.fredde.yamm.YAMM;

/**
 * The mainMenu class.
 * This is the menu that the mainwindow uses.
 * @author Fredrik Ehnbom
 * @version $Revision: 1.36 $
 */
public class mainMenu
	extends JMenuBar
{
	/**
	 * Makes the menu, adds a menulistener etc...
	 */
	public mainMenu() {
		JMenu file = new JMenu(YAMM.getString("file"));
		file.setFont(new Font("SansSerif", Font.BOLD, 12));
		JMenuItem rad;
		add(file);


		// the file menu
		rad = new JMenuItem(YAMM.getString("file.new"),
				new ImageIcon(getClass().getResource("/images/buttons/new_mail.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		file.add(rad);

		rad = new JMenuItem(YAMM.getString("file.save_as"),
				new ImageIcon(getClass().getResource("/images/buttons/save_as.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		file.add(rad);

		file.addSeparator();

		rad = new JMenuItem(YAMM.getString("file.exit"),
				new ImageIcon(getClass().getResource("/images/buttons/exit.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		file.add(rad);

		// the edit menu
		JMenu edit = new JMenu(YAMM.getString("edit"));
		edit.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(edit);

		rad = new JMenuItem(YAMM.getString("edit.settings"),
				new ImageIcon(getClass().getResource("/images/buttons/prefs.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		edit.add(rad);

		rad = new JMenuItem(YAMM.getString("edit.view_source"),
				new ImageIcon(getClass().getResource("/images/buttons/search.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		edit.add(rad);

		// the help menu
		JMenu help = new JMenu(YAMM.getString("help"));
		help.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(help);

		rad = new JMenuItem(YAMM.getString("help.about_you"),
				new ImageIcon(getClass().getResource("/images/buttons/help.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		help.add(rad);

		rad = new JMenuItem(YAMM.getString("help.about"),
				new ImageIcon(getClass().getResource("/images/buttons/help.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		help.add(rad);

		rad = new JMenuItem(YAMM.getString("help.license"),
				new ImageIcon(getClass().getResource("/images/types/text.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		help.add(rad);

		help.addSeparator();

		rad = new JMenuItem(YAMM.getString("help.bug_report"),
				new ImageIcon(getClass().getResource("/images/buttons/bug.png")));
		rad.addActionListener(MListener);
		rad.setFont(new Font("SansSerif", Font.PLAIN, 10));
		help.add(rad);
	}

	ActionListener MListener = new ActionListener() {
		protected String format(String src) {
			String[] end = new String[] {"B", "kB", "MB", "GB"};
			double num = 0;
			int i = 0;
			try { num = (double) Integer.parseInt(src); } catch (Exception e) {num = 0;}

			while (num > 1024 && i < end.length) {
				num /= 1024;
				i++;
			}
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);

			return nf.format(num) + " " + end[i];
		}

		public void actionPerformed(ActionEvent ae) {
			String kommando = ((JMenuItem)ae.getSource()).getText();

			if (kommando.equals(YAMM.getString("file.exit"))) {
				YAMM.getInstance().exit();
			} else if (kommando.equals(YAMM.getString("help.about_you"))) {
				String host = null;
				String ipaddress = null;

				try {
					InetAddress myInetaddr = InetAddress.getLocalHost();

					ipaddress = myInetaddr.getHostAddress();
					host = myInetaddr.getHostName();
				} catch (UnknownHostException uhe) {}

				if (ipaddress == null) {
					ipaddress = YAMM.getString("unknown");
				}
				if (host == null) {
					host = YAMM.getString("unknown");
				}

				Object[] args = {
					System.getProperty("os.name"),
					System.getProperty("os.version"),
					System.getProperty("os.arch"),
					ipaddress,
					host,
					System.getProperty("java.version"),
					System.getProperty("java.vendor"),
					System.getProperty("java.vendor.url"),
					System.getProperty("user.name"),
					System.getProperty("user.home"),
					YAMM.getProperty("stat.sentnum", "0"),
					YAMM.getProperty("stat.receivednum", "0"),
					format(YAMM.getProperty("stat.sentbytes", "0")),
					format(YAMM.getProperty("stat.receivedbytes", "0")),
				};

				JOptionPane.showMessageDialog(
					YAMM.getInstance(),
					YAMM.getString("info.about.you", args),
					YAMM.getString("help.about_you"),
					JOptionPane.INFORMATION_MESSAGE
				);
			} else if (kommando.equals(YAMM.getString("help.about"))) {

				Object[] args = {
					YAMM.version,
					YAMM.compDate,
					"http://www.gjt.org/~fredde/yamm/",
					"<fredde@gjt.org>"
				};

				JOptionPane.showMessageDialog(
					YAMM.getInstance(),
					"Copyright (C) 1999-2003 Fredrik Ehnbom\n" +
					YAMM.getString("info.about", args) + "\n" + 
					"Tuomas Kuosmanen\n" +
					"Rafael Escovar\n" + 
					"Ricardo A Mattar\n" + 
					"Daniel Johnson",
					YAMM.getString("help.about"),
					JOptionPane.INFORMATION_MESSAGE
				);
			} else if (kommando.equals(YAMM.getString("help.bug_report"))) {
				YAMMWrite yw = new YAMMWrite("\"Fredrik Ehnbom\" <fredde@gjt.org>", "Bug report");
				JTextArea jt = yw.myTextArea;
				jt.append("What is the problem?\n\n");
				jt.append("How did you make it happen?\n\n");
				jt.append("Can you make it happen again?\n\n");
				jt.append("\nAnd now some info about your system:\n");
				p("java.version", jt);
				p("java.vendor", jt);
				p("java.vendor.url", jt);
				p("java.home", jt);
				p("java.vm.specification.version", jt);
				p("java.vm.specification.vendor", jt);
				p("java.vm.specification.name", jt);
				p("java.vm.version", jt);
				p("java.vm.vendor", jt);
				p("java.vm.name", jt);
				p("java.specification.version", jt);
				p("java.specification.vendor", jt);
				p("java.specification.name", jt);
				p("java.class.version", jt);
				p("java.class.path", jt);
				p("os.name", jt);
				p("os.arch", jt);
				p("os.version", jt);
				jt.append("YAMM.version: " + YAMM.version + "\n");
				jt.append("YAMM.compDate: " + YAMM.compDate);
			} else if (kommando.equals(YAMM.getString("help.license"))) {

				JOptionPane.showMessageDialog(
					YAMM.getInstance(),
					"Yet Another Mail Manager " +
					YAMM.version + " E-Mail Client\n" +
					"Copyright (C) 1999-2003 Fredrik Ehnbom\n" +
					YAMM.getString("license"),
					YAMM.getString("help.license"),
					JOptionPane.INFORMATION_MESSAGE
				);
			}  else if (kommando.equals(YAMM.getString("edit.settings"))) {
				new ConfigurationWizard(YAMM.getInstance());
			} else if (kommando.equals(YAMM.getString("file.new"))) {
				new YAMMWrite();
			} else if (kommando.equals(YAMM.getString("edit.view_source"))) {
				YAMM yamm = YAMM.getInstance();
				mainTable tmp = yamm.mailList;
				int test = tmp.getSelectedRow();

				if (test >= 0 && test < yamm.listOfMails.length) {

					int msg = tmp.getSelectedMessage();

					long skip = yamm.listOfMails[msg].skip;

					if (msg != -1) {
						sourceViewer sv = new sourceViewer();
						int ret = Mailbox.viewSource(
							yamm.getMailbox(), msg,
							skip,
							sv.jtarea
						);
						if (ret != -1) {
							JScrollBar jsb = sv.jsp.getVerticalScrollBar();
							jsb.updateUI();

							jsb.setValue(0);
						} else {
							sv.dispose();
							sv = null;
						}
					}
				}
			} else if (kommando.equals(YAMM.getString("file.save_as"))) {
				YAMM yamm = YAMM.getInstance();
				int test = yamm.mailList.getSelectedMessage();

				if (test != -1 && test <= yamm.listOfMails.length) {
					JFileChooser jfs = new JFileChooser();
					jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfs.setMultiSelectionEnabled(false);
					jfs.setFileFilter(new filter());
					jfs.setSelectedFile(new File("mail.html"));
					int ret = jfs.showSaveDialog(yamm);

					if (ret == JFileChooser.APPROVE_OPTION) {
						String tmp = yamm.getMailbox();
						String boxName = tmp.substring(
							tmp.indexOf("boxes") +
							6,
							tmp.length()
						);
						tmp = yamm.mailPageString;
						boxName = tmp.substring(8,
							tmp.length()) +
							boxName + File.separator +
							yamm.mailList.
							getSelectedMessage() +
							".html";

						new File(boxName).renameTo(
							jfs.getSelectedFile()
						);
					}
				}
			}
		}
		void p(String prop, JTextArea ta) {
			ta.append(
				prop + " : " +
				System.getProperty(prop) + "\n"
			);
		}

	};

	private final class filter
		extends FileFilter
	{
		public final boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 &&  i < s.length() - 1) {
				String extension = s.substring(i + 1).toLowerCase();
				if ("htm".equals(extension) || "html".equals(extension)) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}

		// The description of this filter
		public String getDescription() {
			return "HTML files";
		}
	}
}
/*
 * Changes:
 * $Log: mainMenu.java,v $
 * Revision 1.36  2003/04/13 16:38:28  fredde
 * now uses yamm.set/getMailbox
 *
 * Revision 1.35  2003/04/04 18:03:48  fredde
 * updated for Singleton stuff
 *
 * Revision 1.34  2003/03/15 19:35:28  fredde
 * .gif -> .png. Added Daniel Johnson
 *
 * Revision 1.33  2003/03/14 23:18:46  fredde
 * added formating of mailsizes in about_me dialog
 *
 * Revision 1.32  2003/03/12 20:21:43  fredde
 * removed MsgDialog. added sent/received stats. frame -> yamm
 *
 * Revision 1.31  2003/03/09 17:51:08  fredde
 * now uses the new index system
 *
 * Revision 1.30  2003/03/08 16:54:48  fredde
 * added my name to the to-field when submitting bug reports
 *
 * Revision 1.29  2003/03/05 15:58:31  fredde
 * now displays the correct message when viewing source
 *
 * Revision 1.28  2001/03/18 17:07:02  fredde
 * updated
 *
 * Revision 1.27  2000/12/31 14:08:19  fredde
 * additional credits in here instead of in resource files
 *
 * Revision 1.26  2000/12/26 11:23:27  fredde
 * YAMM.listOfMails is now of type String[][]
 *
 * Revision 1.25  2000/12/25 09:53:05  fredde
 * changed homepage url, cleaned up a little
 *
 * Revision 1.24  2000/08/09 16:34:46  fredde
 * readability fixes and changed 1999 to 1999-2000
 *
 * Revision 1.23  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.22  2000/03/15 13:43:00  fredde
 * added YAMM.compDate to bug report
 *
 * Revision 1.21  2000/03/05 18:02:53  fredde
 * now gets the images used for the jar-file
 *
 * Revision 1.20  2000/02/28 13:46:42  fredde
 * added some javadoc tags and changelog. Cleaned up
 *
 */
