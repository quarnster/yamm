/*  YAMM.java - main class
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

import java.awt.*;
import java.awt.event.*;
// import java.awt.print.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;


import org.gjt.fredde.yamm.mail.*;
import org.gjt.fredde.yamm.gui.*;
import org.gjt.fredde.util.gui.*;
import org.gjt.fredde.util.net.Browser;
import org.gjt.fredde.yamm.gui.main.*;
import org.gjt.fredde.yamm.encode.*;

/**
 * The big Main-class of YAMM
 * @author Fredrik Ehnbom
 * @version $Id: YAMM.java,v 1.41 2000/02/28 13:45:44 fredde Exp $
 */
public class YAMM extends JFrame implements HyperlinkListener /*, Printable */ {

	/** The file separator */
	public static String sep = File.separator;

	/** The home of yamm */
	public static String home = System.getProperty("user.home") + sep + ".yamm";

	/** To get the Language strings for buttons, menus, etc... */
	static protected ResourceBundle         res;

	/** The box the user has selected. */
	public static String selectedbox = home + sep + "boxes" + sep;

	/** The version of YAMM */
	public static String version  = Utilities.cvsToVersion("$Name:  $");

	/** The compileDate of YAMM */
	public static String compDate = Utilities.cvsToDate("$Date: 2000/02/28 13:45:44 $");

	/** the file that contains the current mail */
	public String mailPageString = "file:///" + home + "/tmp/cache/";
	public URL mailPage;

	/** The vector containing the attaced files. */
	public Vector attach;

	/** To check if the user has sun.misc.Base64Decoder */
	static protected boolean base64 = false;

	/** The vector containing the mails of a box */
	public Vector listOfMails = new Vector();

	/** The properties to get configuration stuff from */
	static protected Properties props = new Properties();

	/** What's in the tree */
	static protected DefaultMutableTreeNode top;

	/** The Table that lists the mails in listOfMails */
	public mainTable mailList;

	/** If the buttons should be painted with icons, text or both */
	public static boolean ico = true, text = true;

	/** The JEditorPane for this frame */
	public JEditorPane mail;

	/** Attachment list */
	public JList myList;

	/** The toolbar */
	public mainToolBar tbar;

	/** The splitpanes */
	protected JSplitPane   SPane, SPane2;

	/** The statusrow */
	public statusRow status;

	/** The mail/attach tabbedpane */
	protected JTabbedPane JTPane;

	/** The names for the exception dialogs buttons */
	public static String[] exceptionNames = null;

	public static PrintStream debug = null;

	/**
	 * Returns the translated string.
	 * @param s The string to get translation for.
	 */
	public static final String getString(String s) {
		return res.getString(s);
	}

	/**
	 * Returns the translated string
	 * @param s The string to get translation for.
	 * @param args The arguments to send to MessageFormat.format
	 */
	public static final String getString(String s, Object[] args) {
		return MessageFormat.format(res.getString(s), args);
	}

	/**
	 * Returns the property
	 * @param property The property to get.
	 */
	public static String getProperty(String property) {
		return props.getProperty(property);
	}

	/** 
	 * Returns the property
	 * @param prop The property to get.
	 * @param def The default string to return if prop is null.
	 */
	public static String getProperty(String prop, String def) {
		return props.getProperty(prop, def);
	}

	/**
	 * Sets a property
	 * @param property The property to set.
	 * @param value The value of the property.
	 */
	public static void setProperty(String property, String value) {
		props.setProperty(property, value);
	}

	/**
	 * Creates the main-window and adds all the components in it.
	 */
	public YAMM() {
		try { 
			mailPage=new URL(mailPageString +
				res.getString("box.inbox") + "/0.html");
		} catch (MalformedURLException mue) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					mue,
					YAMM.exceptionNames);
		}

		System.out.println("Locale: " + getProperty("locale",
							Locale.getDefault().toString()));

		boolean result = false;
		System.out.println("Checking for sun classes...");

		if (ClassLoader.getSystemResource(
				"sun/misc/BASE64Decoder.class") != null) {
			result = true;
			base64 = true;
		}
		System.out.println("sun.misc.BASE64Decoder: " +
						(result ? "yes" : "no"));

		// get the main window's settings and default them if they
		// don't exist 
		int mainx = Integer.parseInt(props.getProperty("mainx", "0"));
		int mainy = Integer.parseInt(props.getProperty("mainy", "0"));
		int mainw = Integer.parseInt(props.getProperty("mainw", "650"));
		int mainh = Integer.parseInt(props.getProperty("mainh", "380"));
		int hsplit = Integer.parseInt(props.getProperty("hsplit", "80"));
		int vsplit = Integer.parseInt(props.getProperty("vsplit", "125"));

		setBounds(mainx, mainy, mainw, mainh);
		getContentPane().setLayout(new BorderLayout());

		// the menubar
		setJMenuBar(new mainMenu(this));

		// the toolbar
		tbar = new mainToolBar(this);
		getContentPane().add("North", tbar);

		// create a list of mails in the selected box
		Mailbox.createList(selectedbox, listOfMails);

		// the tablemodel for the maillist table
		TableModel dataModel = new AbstractTableModel() {
			final String headername[] = { 
				"#",
				res.getString("table.subject"),
				res.getString("table.from"),
				res.getString("table.date")
			};

			public int getColumnCount() {
				return 4;
			}
			public int getRowCount() {
				return  listOfMails.size();
			}
			public Object getValueAt(int row, int col) {
				if (row >= listOfMails.size()) {
					return null;
				} else {
					return  ((Vector)listOfMails.elementAt(row)).
							elementAt(col);
				}
			}
			public boolean isCellEditable(int col) {
				return false;
			}
			public String getColumnName(int column) {
				return headername[column];
			}
		};

		mail = new JEditorPane();
		mail.setContentType("text/html");
		Mailbox.getMail(selectedbox, 0, 0);
		try {
			mail.setPage(mailPage);
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		}
		mail.setEditable(false);
		mail.addHyperlinkListener(this);

		JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
		JTPane.setFont(new Font("SansSerif", Font.PLAIN, 10));
		if (text && !ico) {
			JTPane.addTab(res.getString("mail"),
					new JScrollPane(mail));
		} else if (ico && !text) {
			JTPane.addTab("", 
			new ImageIcon("org/gjt/fredde/yamm/images/buttons/" +
								"mail.gif"), 
			new JScrollPane(mail));
		} else {
			JTPane.addTab(res.getString("mail"),
			new ImageIcon("org/gjt/fredde/yamm/images/buttons/" +
								"mail.gif"),
			new JScrollPane(mail));
		}

		JPanel myPanel = new JPanel(new BorderLayout());

		Box hori1 = Box.createHorizontalBox();

		JButton b = new JButton();
		b.setFont(new Font("SansSerif", Font.PLAIN, 10));
		if (text) {
			b.setText(res.getString("button.view_extract"));
		}
		if (ico) {
			b.setIcon(new ImageIcon("org/gjt/fredde/yamm/images/" +
							"buttons/search.gif"));
		}
		b.setToolTipText(res.getString("button.view_extract"));
		b.addActionListener(BListener);
		hori1.add(b);

		attach = new Vector();

		// The ListModel for the attachment list
		ListModel attachModel = new AbstractListModel() {
			public Object getElementAt(int index) {
				return ((Vector)attach.elementAt(index)).
								elementAt(0);
			}
			public int getSize() {
				return attach.size();
			}
		};

		myList = new JList(attachModel);
		myList.setCellRenderer(new AttachListRenderer());
		myPanel.add("Center", myList);
		myPanel.add("South", hori1);

		Border ram = BorderFactory.createEtchedBorder();
		myList.setBorder(ram);

		if (ico && !text) {
			JTPane.addTab("", new ImageIcon("org/gjt/fredde/yamm/" +
						"images/buttons/attach.gif"),
			myPanel);
		} else if (text && !ico) {
			JTPane.addTab(res.getString("mail.attachment"),myPanel);
		} else {
			JTPane.addTab(res.getString("mail.attachment"),
				new ImageIcon("org/gjt/fredde/yamm/images/" +
							"buttons/attach.gif"),
				myPanel);
		}

		mailList = new mainTable(this, dataModel, listOfMails);
		mailList.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		createAttachList();

		// When the mails in the maillist doesn't take all the
		// maillists space, this fix still paints the background
		// in the maillists background color.
		JPanel bgFix = new JPanel(new BorderLayout());
		bgFix.add("Center", new JScrollPane(mailList));
		bgFix.setBackground(mailList.getBackground());

		SPane = new JSplitPane(0, bgFix, JTPane);
		SPane.setDividerLocation(hsplit);
		SPane.setMinimumSize(new Dimension(0, 0));
		SPane.setPreferredSize(new Dimension(300, 50));
		SPane.setOneTouchExpandable(true);

		top = new DefaultMutableTreeNode(YAMM.getString("box.boxes"));
		mainJTree tree = new mainJTree(this, top, tbar);

		SPane2 = new JSplitPane(1, tree, SPane);
		SPane2.setDividerLocation(vsplit);
		SPane2.setOneTouchExpandable(true);
		getContentPane().add("Center", SPane2);

		status = new statusRow();
		status.button.setText(res.getString("button.cancel"));
		getContentPane().add("South", status);
		addWindowListener(new FLyssnare());
		show();
	}


	/**
	 * Checks if the link is a mailto:-link. If it is, it starts a
	 * write-window with the specified mailto:-address,
	 * if it isn't, it starts the webbrowser for that platform.
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String link = e.getURL().toString();

			if ((e.getURL().toString()).startsWith("mailto:")) {
				link = link.substring(link.indexOf("mailto:") +
							7, link.length());
				new YAMMWrite(link);
			} else {
				try {
					new Browser(link);
				} catch (InterruptedException ie) { 
					new ExceptionDialog(
						YAMM.getString("msg.error"), 
						ie,
						YAMM.exceptionNames);
				} catch(IOException ioe) { 
					new ExceptionDialog(
						YAMM.getString("msg.error"), 
						ioe,
						YAMM.exceptionNames);
				}
			}
		} else if (e.getEventType() ==
					HyperlinkEvent.EventType.ENTERED) {
			mail.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else if (e.getEventType() ==
					HyperlinkEvent.EventType.EXITED) {
			mail.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/**
	 * The actionListener for the view/extract button
	 */
	ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int whichAtt = myList.getSelectedIndex();

			if (whichAtt != -1) {
				String filename =
					((Vector)attach.elementAt(whichAtt)).elementAt(0).toString();
				String encode =
					((Vector)attach.elementAt(whichAtt)).elementAt(1).toString();
				String file =
					((Vector)attach.elementAt(whichAtt)).elementAt(2).toString();
				String target = home  + "/tmp/" + filename;

				if (filename.toLowerCase().endsWith(".jpg") ||
					filename.toLowerCase().endsWith(".gif")) {
					if (encode.equalsIgnoreCase("base64")) {
						if (base64)
							new Base64Decode("B64Decode " + filename, file, target).start();
						else new MsgDialog("No Support for base64 encoded files...");
					} else if (encode.equalsIgnoreCase("x-uuencode")) {
						new UUDecode(null, "UUDecode " + filename, file,
							target, true).start();
					}
				} else if (encode.equalsIgnoreCase("base64")) {
					if (base64) {
						JFileChooser jfs = new JFileChooser();
						jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
						jfs.setMultiSelectionEnabled(false);
						jfs.setSelectedFile(new File(filename));
						int ret = jfs.showSaveDialog(null);

						if (ret == JFileChooser.APPROVE_OPTION) {
							if (jfs.getSelectedFile() != null) {
								new Base64Decode("B64Decode " + filename, file,
								jfs.getSelectedFile().toString()).start();
							}
						}
					} else System.out.println("No support for base64 encoded files...");
				} else if(encode.equalsIgnoreCase("x-uuencode")) {
					JFileChooser jfs = new JFileChooser();
					jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfs.setMultiSelectionEnabled(false);
					jfs.setSelectedFile(new File(filename));
					int ret = jfs.showSaveDialog(null);

					if (ret == JFileChooser.APPROVE_OPTION) {
						if (jfs.getSelectedFile() != null) {
							new UUDecode(null, "UUDecode " + filename, file,
								jfs.getSelectedFile().toString(), false).start();
						}
					}
				}
			}
		}
	};

	public void createAttachList() {
		attach = new Vector();

		String boxName = selectedbox.substring(
				selectedbox.indexOf("boxes") + 6, 
				selectedbox.length()) + "/";

		String base = home +  "/tmp/cache/" + boxName;

		int msgNum = (mailList.getSelectedRow() != -1) ? mailList.getSelectedRow() : 0;
		if (msgNum == -1) msgNum = 0;

		String[] test = new File(base).list();

		for(int i = 0; i < test.length; i++ ) {
			if (test[i].indexOf(msgNum + ".attach.") != -1) 
				addinfo(base + test[i], attach);
		}
		if (attach.size() == 0) {
			JTPane.setEnabledAt(1, false);
		}
		else {
			JTPane.setEnabledAt(1, true);
		}
	}

	private void addinfo(String where, Vector attach) {
		Vector tmp = new Vector();

		try {
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				new FileInputStream(where)));


			tmp.add(in.readLine()); // Name of the attachment
			tmp.add(in.readLine()); // The attachments encoding

        
			in.close();
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
		}

		tmp.add(where);
		attach.add(tmp);
	}
/*
  public int print(Graphics g, PageFormat pageFormat,
                        int pageIndex) throws PrinterException {
    mail.paint(g);

    return Printable.PAGE_EXISTS;
  }
*/

	public void Exit() {
		Rectangle rv = new Rectangle();
		getBounds(rv);

		mailList.save();
		props.setProperty("mainx", new Integer(rv.x).toString());
		props.setProperty("mainy", new Integer(rv.y).toString());
		props.setProperty("mainw", new Integer(rv.width).toString());
		props.setProperty("mainh", new Integer(rv.height).toString());
		props.setProperty("vsplit", 
			new Integer(SPane2.getDividerLocation()).toString());
		props.setProperty("hsplit",
			new Integer(SPane.getDividerLocation()).toString());


		try {
			OutputStream out = new FileOutputStream(home + "/.config");
			props.store(out, "YAMM configuration file");
			out.close();
		} catch (IOException propsioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
					propsioe,
					YAMM.exceptionNames);
		}

		if (debug != System.err) {
			debug.flush();
			debug.close();
		}
		dispose();
		Utilities.delUnNeededFiles();
		System.exit(0);
	}

	/**
	 * The WindowListener.
	 */
	class FLyssnare extends WindowAdapter {
		public void windowClosing(WindowEvent event) {
			Exit();
		}
	}

	/**
	 * Shows a SplashScreen while starting the program.
	 * It checks if the user has config-files, if not it'll
	 * create such and write a little welcome message in
	 * ~home/.yamm/boxes/local/inbox.
	 */
	public static void main(String argv[]) {
		YAMM nFrame = null;
		SplashScreen splash = null;
		boolean firstRun = false;

		if (!(new File(home)).exists()) {
			firstRun = true;

			try {
				(new File(home + "/servers")).mkdirs();
				(new File(home + "/boxes")).mkdirs();
				(new File(home + "/.config")).createNewFile();
				(new File(home + "/.filters")).createNewFile();
				(new File(home + "/tmp")).mkdirs();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(1);
			}
		}

		try {             
			InputStream in = new FileInputStream(home + "/.config");
			props.load(in);
			in.close();    
		} catch (IOException propsioe) {
			propsioe.printStackTrace();
			System.exit(1);
		}
		Locale l = Locale.getDefault();
		String resLanguage = l.getLanguage();
		String resCountry = l.getCountry();

		if (props.getProperty("locale.language") != null) {
			resLanguage = props.getProperty("locale.language");
		}

		if (props.getProperty("locale.country") != null) {
			resCountry = props.getProperty("locale.country");
		}

		l = new Locale(resLanguage, resCountry);
		Locale.setDefault(l);
		
		try {
			res = ResourceBundle.getBundle("org.gjt.fredde.yamm." +
						"resources.YAMM", l);
		} catch (MissingResourceException mre) {
			mre.printStackTrace();
			System.exit(1);
		}

		if (props.getProperty("debug.file") != null) {
			try {
				debug = new PrintStream(
					new BufferedOutputStream(
					new FileOutputStream(props.getProperty("debug.file"))));
			} catch (IOException ioe) {
				debug = System.err;
			}
		} else debug = System.err;

		if (props.getProperty("plaf") != null) {
			try {
				UIManager.setLookAndFeel(props.getProperty("plaf"));
			} catch (Exception e) {};
		} else {
			try {
				UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName()
				);
			} catch (Exception e) {};
		}

		if (props.getProperty("splashscreen", "yes").equals("yes")) {
			splash = new SplashScreen("YAMM " + version + 
				" Copyright (c) 1999 Fredrik Ehnbom",
				"org/gjt/fredde/yamm/images/logo.gif");
		}

		selectedbox += res.getString("box.inbox");

		String[] tmp = {
			res.getString("exception.ok"),
			res.getString("exception.more"),
			res.getString("exception.less")
		};

		exceptionNames = tmp;


		if (firstRun) {
			try {
				String user = System.getProperty("user.name");
				SimpleDateFormat dateFormat =
					new SimpleDateFormat("EEE, dd MMM yyyy"+						" HH:mm:ss zzz", Locale.US);

				InetAddress myInetaddr =
						InetAddress.getLocalHost();
				String host = myInetaddr.getHostName();
				if (host == null) host = "localhost";

				PrintWriter out = new PrintWriter(
					new BufferedOutputStream(
					new FileOutputStream(home + "/boxes/" + 
					YAMM.getString("box.inbox"))));

				out.println("Date: " + dateFormat.format(
								new Date()));
				out.println("From: Fredrik Ehnbom " +
							"<fredde@gjt.org>");
				out.println("To: " + user + "@" + host);
				Object[] args = {
					YAMM.version,
					user
				}; 
				out.println(YAMM.getString("msg.welcome", args));

				out.close();

				(new File(home + "/boxes/" +
					getString("box.outbox"))).createNewFile();
				(new File(home + "/boxes/" +
					getString("box.trash"))).createNewFile();
				(new File(home + "/boxes/" +
					getString("box.sent"))).createNewFile();
			} catch(IOException ioe) { 
				new ExceptionDialog(YAMM.getString("msg.error"),
					ioe,
					YAMM.exceptionNames);
			}
		}

		if (props.getProperty("button.mode", "both").equals("text")) {
			ico = false;
		} else if (props.getProperty("button.mode", "both").equals("icon")) {
			text = false;
		}

		nFrame = new YAMM();
		nFrame.setTitle("Yet Another Mail Manager " + version);
		if (splash != null) splash.dispose();
	}
}

/*
 * Changes
 * $Log: YAMM.java,v $
 * Revision 1.41  2000/02/28 13:45:44  fredde
 * added changelog and some javadoc tags. Moved some functions to Utilities. Added a debug PrintStream and smart version control. fixed some startup bugs. cleaned up a little
 *
 */
