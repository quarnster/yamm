/*  YAMM.java - main class
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
package org.gjt.fredde.yamm;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
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
import org.gjt.fredde.yamm.gui.main.*;
import org.gjt.fredde.yamm.encode.*;

/**
 * The big Main-class of YAMM
 */
public class YAMM extends JFrame implements HyperlinkListener, Printable
{
  /** The file separator */
  public static String sep = File.separator;

  /** The home of yamm */
  public static String home = System.getProperty("user.home") + sep + ".yamm";

  /** To get the Language strings for buttons, menus, etc... */
  static protected ResourceBundle         res;

  /** The box the user has selected. */
  public static String                 selectedbox  = home + "/boxes/inbox";

  /** The version of YAMM */
  public static    String                 yammVersion  = "0.7.2";

  /** The compileDate of YAMM */
  public static    String                 compDate     = "1999-06-21";

  /** the file that contains the current mail */
  public String		  mailPageString   = "file:///" + home + "/tmp/cache/";
  public URL	          mailPage;

  /** The encryption key */
  public static    char[]                 encKey       = "myKey".toCharArray();

  /** The vector containing the attaced files. */
  public Vector                 attach;

  /** To check if the user has sun.misc.Base64Decoder */
  static protected boolean                base64       = false;

  /** The vector containing the mails of a box */
  public Vector                 listOfMails  = new Vector();

  /** The properties to get configuration stuff from */
  static protected Properties             props        = new Properties();

  /** What's in the tree */
  static protected DefaultMutableTreeNode top;


  /** The Table that lists the mails in listOfMails */
  public mainTable      mailList;

  /** The JEditorPane for this frame */
  public JEditorPane  mail;

  public JList        myList;

  public mainToolBar  tbar;

  JButton      b;
  JSplitPane   SPane, SPane2;
  JTree        tree;
  JTabbedPane  JTPane;
  public statusRow status;

  int          mainx, mainy, mainw, mainh, hsplit, vsplit;

  /**
   * Encrypts the String provided with the key specified in encKey.
   * @param encs The String to Encrypt
   */
  public static String encrypt(String encs) {
    int i, j=0, c;
    char enc[] = encs.toCharArray();

    for(i=0;i < enc.length;i++) {
      c = (enc[i]-32)+(encKey[j]-32);

      if(c >= 0 && c <= 94) enc[i] = (char)(c+32);
      else enc[i] = (char)((c%95)+32);

      if((j +1) == encKey.length) j = 0;
      else j++;
    }
    return new String(enc);
  }

  /**
   * Decrypts the String provided with the key specified in encKey.
   * @param encs The String to decrypt
   */
  public static String decrypt(String encs) {
    int i, j=0, c;
    char enc[] = encs.toCharArray();

    for(i=0;i < enc.length;i++) {
      c = (enc[i] - encKey[j]);

      if(c < 0) enc[i] = (char)(c+95+32);
      else enc[i] = (char)(c+32);

      if(encKey.length == (j+1)) j = 0;
      else j++;
    }
    return new String(enc);
  }

  /**
   * Returns the translated string
   */
  public static final String getString(String s) {
    return res.getString(s);
  }

  /**
   * Returns the translated string
   */
  public static final String getString(String s, Object[] args) {
    return MessageFormat.format(res.getString(s), args);
  }

  /**
   * Creates the main-window and adds all the components in it.
   */
  public YAMM() {
    try {
      res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", 
                                     Locale.getDefault());
    }          
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.exit(1);
    }

    Mailbox.getMail(selectedbox, 0);
    try { 
      mailPage = new URL(mailPageString + "/inbox/" + "0.html"); 
    }
    catch (MalformedURLException mue) { System.err.println(mue); }

    System.out.println("Locale: " + Locale.getDefault());

    boolean result = false;
    System.out.println("Checking for sun classes...");

    // check for sun classes
    if(ClassLoader.getSystemResource("sun/misc/BASE64Decoder.class") == null) { result = false; }
    else { result = true; base64 = true; }
    System.out.println("sun.misc.BASE64Decoder: " + ((result) ? "yes" : "no"));


    // load the config
    try {
      InputStream in = new FileInputStream(home + "/.config");
      props.load(in);
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

    // get the main window's settings and default them if an exception is caught 
    mainx = Integer.parseInt(props.getProperty("mainx", "0"));
    mainy = Integer.parseInt(props.getProperty("mainy", "0"));
    mainw = Integer.parseInt(props.getProperty("mainw", "550"));
    mainh = Integer.parseInt(props.getProperty("mainh", "400"));
    hsplit = Integer.parseInt(props.getProperty("hsplit", "150"));
    vsplit = Integer.parseInt(props.getProperty("vsplit", "125"));

    setBounds(mainx, mainy, mainw, mainh);
    getContentPane().setLayout(new BorderLayout());


    // if you want to redirekt the err stream to a file.

    // the menubar
    setJMenuBar(new mainMenu(this));

    // the toolbar
    tbar = new mainToolBar(this);
    getContentPane().add("North", tbar);

    // create a list of mails in the selected box
    Mailbox.createList(selectedbox, listOfMails);

    // handles some stuff for the table
    TableModel dataModel = new AbstractTableModel() {
      final String headername[] = { "#", res.getString("mail.subject"), res.getString("mail.from"), res.getString("mail.date") };

      public int getColumnCount() { return 4; }
      public int getRowCount() {  return  listOfMails.size(); }
      public Object getValueAt(int row, int col) { return  ((Vector)listOfMails.elementAt(row)).elementAt(col); }

      public boolean isCellEditable(int col) { return false; }
      public String getColumnName(int column) {  return headername[column]; }
    };

    mailList = new mainTable(this, dataModel, listOfMails);

    mail = new JEditorPane();
    mail.setContentType("text/html");
    attach = new Vector();
    Mailbox.getMail(selectedbox, 0 /*, attach, mailName */);
    try { mail.setPage(mailPage); }
    catch (IOException ioe) { System.err.println(ioe); }
    mail.setEditable(false);
    mail.addHyperlinkListener(this);
//    mail.setLineWrap(true);
//    mail.setWrapStyleWord(true);

    JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
    JTPane.addTab(res.getString("mail"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/mail.gif"), new JScrollPane(mail, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    class MyCellRenderer extends JLabel implements ListCellRenderer {
      protected boolean selected;

      public Component getListCellRendererComponent(
        JList   list,
        Object  value,
        int     index,
        boolean selected,
        boolean cellHasFocus) {

        String s = value.toString();
        setText(s);

        String end = s;

        if(end.indexOf(".") != -1) 
          end = end.substring(end.lastIndexOf(".") + 1, end.length());

        if(end.equalsIgnoreCase("gif") || 
           end.equalsIgnoreCase("jpg") || 
           end.equalsIgnoreCase("bmp") || 
           end.equalsIgnoreCase("xpm") || 
           end.equalsIgnoreCase("jpeg") || 
           end.equalsIgnoreCase("xcf") || 
           end.equalsIgnoreCase("png"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/image.gif"));

        else if(end.equalsIgnoreCase("htm") || 
                end.equalsIgnoreCase("html") || 
                end.equalsIgnoreCase("txt") || 
                end.equalsIgnoreCase("doc") || 
                end.equalsIgnoreCase("c") || 
                end.equalsIgnoreCase("cc") || 
                end.equalsIgnoreCase("cpp") || 
                end.equalsIgnoreCase("h") || 
                end.equalsIgnoreCase("java"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/text.gif"));

        else if(end.equalsIgnoreCase("wav") || 
                end.equalsIgnoreCase("au") || 
                end.equalsIgnoreCase("mp3") || 
                end.equalsIgnoreCase("mod") || 
                end.equalsIgnoreCase("xm") || 
                end.equalsIgnoreCase("midi") || 
                end.equalsIgnoreCase("mid"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/sound.gif"));

        else if(end.equalsIgnoreCase("zip") || 
                end.equalsIgnoreCase("jar") || 
                end.equalsIgnoreCase("rpm") || 
                end.equalsIgnoreCase("deb") || 
                end.equalsIgnoreCase("arj") || 
                end.equalsIgnoreCase("gz") || 
                end.equalsIgnoreCase("tar") || 
                end.equalsIgnoreCase("tgz") || 
                end.equalsIgnoreCase("z"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/packed.gif"));

        else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/unknown.gif"));

        this.selected = selected;
        return this;
      }
    /**
      * paint is subclassed to draw the background correctly.  JLabel
      * currently does not allow backgrounds other than white, and it
      * will also fill behind the icon.  Something that isn't desirable.
      */
    public void paint(Graphics g) {
	Color            bColor;
	Icon             currentI = getIcon();

	if(selected)
            bColor = new Color(204, 204, 255);
	else if(getParent() != null)
            // Pick background color up from parent (which will come from the JTree we're contained in).
	    bColor = getParent().getBackground();
	else bColor = getBackground();
	g.setColor(bColor);

	if(currentI != null && getText() != null) {
	    int          offset = (currentI.getIconWidth() + getIconTextGap());
	    g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
	}
	else g.fillRect(0, 0, getWidth()-1, getHeight()-1);
	super.paint(g);
      }
    }

    JPanel myPanel = new JPanel(new BorderLayout());

    Box hori1 = Box.createHorizontalBox();

    b = new JButton(res.getString("button.view_extract"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.gif"));
    b.setToolTipText(res.getString("button.view_extract"));
    b.addActionListener(BListener);
    hori1.add(b);

    attach = new Vector();

    ListModel attachModel = new AbstractListModel() {
      public Object getElementAt(int index) { return ((Vector)attach.elementAt(index)).elementAt(0); }
      public int    getSize() { return attach.size(); }
    };

    myList = new JList(attachModel);
    myList.setCellRenderer(new MyCellRenderer());
    myPanel.add("Center", myList);
    myPanel.add("South", hori1);

    Border ram = BorderFactory.createEtchedBorder();
    myList.setBorder(ram);

    JTPane.addTab(res.getString("mail.attachment"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/attach.gif"), myPanel);

    SPane = new JSplitPane(0, new JScrollPane(mailList), JTPane);

    SPane.setDividerLocation(hsplit);
    SPane.setBackground(Color.white);
    SPane.setMinimumSize(new Dimension(0, 0));
    SPane.setPreferredSize(new Dimension(300, 50));
    SPane.setOneTouchExpandable(true);
//    SPane.setOpaque(true);  // makes it look nicer/uglier


    top = new DefaultMutableTreeNode("Mail Boxes");
    tree = new mainJTree(this, top, tbar);

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
   * Checks if the link is a mailto:-link.
   * If it is, it starts a write-window with the specified mailto:-address,
   * if not, it starts the program that handles this filetype.
   */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

      if((e.getURL().toString()).startsWith("mailto:")) {
        String link = e.getURL().toString();
        new YAMMWrite(link.substring(link.indexOf("mailto:") + 7, link.length()));
      }

      else {
        try {
          String cmd = null;

          if(isWindows()) {
            cmd = "start " + e.getURL();
            Runtime.getRuntime().exec(cmd);
          }
          else {
            cmd = "netscape -remote openURL(" + e.getURL() + ")";

            Process p = Runtime.getRuntime().exec(cmd);

            try {
              int exitcode = p.waitFor();

              if(exitcode != 0) {
                cmd = "netscape " + e.getURL();

                Runtime.getRuntime().exec(cmd);
              }
            }
            catch (InterruptedException ie) { 
              Object[] args = {ie.toString()};
              new MsgDialog(this, YAMM.getString("msg.error"), 
                                  YAMM.getString("msg.exception", args));
            }
          }
        }
        catch(IOException ioe) { 
          Object[] args = {ioe.toString()};
          new MsgDialog(this, YAMM.getString("msg.error"), 
                              YAMM.getString("msg.exception", args)); 
        }
      }
    }
    else if(e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
      mail.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    else if(e.getEventType() == HyperlinkEvent.EventType.EXITED) {
      mail.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
  }

  /**
   * Checks if the OS is windows.
   * @return true if it is or false if it's not.
   */
  protected boolean isWindows() {
    if(System.getProperty("os.name").indexOf("Windows") != -1) return true;
    else return false;
  }

  ActionListener BListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      String arg = ((JButton)e.getSource()).getToolTipText();
     
      if(arg.equals(res.getString("button.view_extract"))) {
        int whichAtt = myList.getSelectedIndex();

        if(whichAtt != -1) {
          String filename = ((Vector)attach.elementAt(whichAtt)).elementAt(0).toString();
          String encode = ((Vector)attach.elementAt(whichAtt)).elementAt(1).toString();
          String file = ((Vector)attach.elementAt(whichAtt)).elementAt(2).toString();
          String target = home  + "/tmp/" + filename;

          if(filename.endsWith(".jpg") || filename.endsWith(".gif") || filename.endsWith(".JPG") || filename.endsWith(".GIF")) {
            if(encode.equalsIgnoreCase("base64")) {
              if(base64) {
                new Base64Decode("B64Decode " + filename, file, target).start();
              }
              else new MsgDialog("No Support for base64 encoded files..."); // System.out.println("No support for base64 encoded files...");
            }
            if(encode.equalsIgnoreCase("x-uuencode")) {
              new UUDecode(null, "UUDecode " + filename, file, target, true).start();
            }
          }
          else if(encode.equalsIgnoreCase("base64")) {
            if(base64) {
              JFileChooser jfs = new JFileChooser();
              jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
              jfs.setMultiSelectionEnabled(false);
              jfs.setSelectedFile(new File(filename));
              int ret = jfs.showSaveDialog(null);
    
              if(ret == JFileChooser.APPROVE_OPTION) {
                if(jfs.getSelectedFile() != null) {
                  new Base64Decode("B64Decode " + filename, file, jfs.getSelectedFile().toString()).start();
                }
              }
            }
            else System.out.println("No support for base64 encoded files...");
          }
          else if(encode.equalsIgnoreCase("x-uuencode")) {
            JFileChooser jfs = new JFileChooser();
            jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfs.setMultiSelectionEnabled(false);
            jfs.setSelectedFile(new File(filename));
            int ret = jfs.showSaveDialog(null);
            
            if(ret == JFileChooser.APPROVE_OPTION) {
              if(jfs.getSelectedFile() != null) {
                new UUDecode(null, "UUDecode " + filename, file, jfs.getSelectedFile().toString(), false).start();
              }
            }
          }
        }
      }
    }
  };

  public void createAttachList() {
    attach = new Vector();
    String boxName = selectedbox.substring(selectedbox.indexOf("boxes") + 6, 
                                           selectedbox.length()) + "/";

    String base = home +  "/tmp/cache/" + boxName;
    int msgNum = mailList.getSelectedRow();
    String[] test = new File(base).list();

    for(int i = 0; i < test.length; i++ ) {
      if(test[i].indexOf(msgNum + ".attach.") != -1) 
        addinfo(base + test[i], attach);
    }
  }

  protected void addinfo(String where, Vector attach) {
    Vector tmp = new Vector();
    String temp = null;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(where)));

      String name = null, encode = null;

      for(int i = 0; i < 2; i++) {
        temp = in.readLine();

        if(temp.startsWith("Content-Transfer-Encoding: "))
          encode = temp.substring(27, temp.length());
        else if(temp.indexOf("name=\"") != -1)
          name = temp.substring(temp.indexOf("name=\"") + 6, temp.lastIndexOf("\""));

        
        if(name != null && encode != null) { tmp.add(name); tmp.add(encode); }
      }
      in.close();
    }
    catch (IOException ioe) { System.err.println(ioe); }

    tmp.add(where);
    attach.add(tmp);
  }

  public int print(Graphics g, PageFormat pageFormat,
                        int pageIndex) throws PrinterException {
    mail.paint(g);

    return Printable.PAGE_EXISTS;
  }


  public void Exit() {
    Rectangle rv = new Rectangle();
    getBounds(rv);

    try {
      InputStream in = new FileInputStream(home + "/.config");
      props.load(in);
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

    props.setProperty("mainx", new Integer(rv.x).toString());
    props.setProperty("mainy", new Integer(rv.y).toString());
    props.setProperty("mainw", new Integer(rv.width).toString());
    props.setProperty("mainh", new Integer(rv.height).toString());
    props.setProperty("vsplit", new Integer(SPane2.getDividerLocation()).toString());
    props.setProperty("hsplit", new Integer(SPane.getDividerLocation()).toString());


    try {
      OutputStream out = new FileOutputStream(home + "/.config");
      props.store(out, "YAMM configuration file");
      out.close();
    } catch(IOException propsioe) { System.err.println(propsioe); }

    dispose();
    delUnNeededFiles();
    System.exit(0);
  }

  public void delUnNeededFiles(){
    Vector delFile = new Vector(), delDir = new Vector();
    System.out.println("Creating list of unneeded files...");
    createDelList(delFile, delDir, new File(home + "/tmp/"));
    System.out.println("\nDeleting unneeded files...");
    delFiles(delFile);
    delFiles(delDir);
  }


  protected void createDelList(Vector delFile, Vector delDir, File dir) {
    String files[] = dir.list();

    for(int i = 0; i < files.length;i++) {
      System.out.println("added \"" + files[i]);
      File dir2 = new File(dir, files[i]);
      if(dir2.isDirectory()) {
        delDir.add(dir2);
        createDelList(delFile, delDir, dir2);
      }
      else delFile.add(dir2);
    }
  }

  public void delFiles(Vector delFile) {
    for(int i = 0; i < delFile.size(); i++) {
      String file = delFile.elementAt(i).toString();
      if(!new File(file).delete()) {
        System.err.println("Couldn't delete \"" + file + "\"");
      }
    }
  }
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
  public static void main(String argv[])
  {
    SplashScreen splash = new SplashScreen("YAMM " + yammVersion + " Copyright (c) 1999 Fredrik Ehnbom", "org/gjt/fredde/yamm/images/logo.gif");
    YAMM nFrame = null;

    if(!(new File(System.getProperty("user.home") + "/.yamm/boxes")).exists()) {
      try {
        String user = System.getProperty("user.name");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        InetAddress myInetaddr = InetAddress.getLocalHost();
        String host = myInetaddr.getHostName();

        if(host == null) host = "localhost";

        (new File(home + "/servers")).mkdirs();
        (new File(home + "/boxes")).mkdirs();
        PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(home + "/boxes/inbox")));

        out.println("Date: " + dateFormat.format(new Date()));
        out.println("From: Fredrik Ehnbom <fredde@gjt.org>");
        out.println("To: " + user + "@" + host);
        out.println("Subject: Welcome to YAMM " + yammVersion);
        out.println("\n<html>\nWelcome " + user + "!");
        out.println("\nFeel free to <a href=\"mailto:fredde@gjt.org\">mail me</a> your questions, comments, suggestions or bug-reports.");
        out.println("\nIf you want to report a bug, choose 'bug report' from the 'help'-menu.\n");
        out.println("\nBest regards,\n\nFredrik Ehnbom\n</html>\n.\n");
        out.close();

        (new File(home + "/boxes/outbox")).createNewFile();
        (new File(home + "/boxes/trash")).createNewFile();
        (new File(home + "/.config")).createNewFile();
        (new File(home + "/.filters")).createNewFile();
        (new File(home + "/tmp")).mkdirs();
      } catch(IOException ioe) { new MsgDialog(nFrame, "Error!", ioe.toString()); }
    }
    String result;

    nFrame = new YAMM();
    nFrame.setTitle("Yet Another Mail Manager " + yammVersion);
    splash.dispose();
  }
}

