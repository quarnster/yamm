/*  YAMMWrite.java - Writing mail
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

import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;
import org.gjt.fredde.yamm.encode.UUEncode;

/**
 * The class for writing mails
 */
public class YAMMWrite extends JFrame {

  /** Used for Language stuff */
  static protected ResourceBundle res;

  /** To load and save the configuration */
  static protected Properties     props  = new Properties();

  /** A list of files to attach to the mail */
  static protected Vector         attach;

  /** The textarea for writing in the message */
  public JTextArea      myTextArea;

  JLabel      myLabel;
  JButton     myButton;
  JTextField  myTextField1;
  JTextField  myTextField2;
  JList       myList;

  Box         vert1, vert2, vert3, hori1, hori2;
  int         writex, writey, writew, writeh;

  /**
   * Creates a new empty mail with no subject and recipent
   */
  public YAMMWrite() {
    this("","","");
  }

  /**
   * Creates a new empty mail with no subject and with the specified recipent
   * @param to The recipents address
   */
  public YAMMWrite(String to) {
    this(to, "","");
  }

  /**
   * Creates a new empty mail with the specified subject and recipent
   * @param to The recipents address
   * @param subject The subject
   */
  public YAMMWrite(String to, String subject) {
    this(to,subject,"");
  }

  public YAMMWrite(String to, String subject, String body) {
    setTitle(subject);
    try {
      InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
      props.load(in);
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

    writex = Integer.parseInt(props.getProperty("writex", "0"));
    writey = Integer.parseInt(props.getProperty("writey", "0"));
    writew = Integer.parseInt(props.getProperty("writew", "500"));
    writeh = Integer.parseInt(props.getProperty("writeh", "300"));

    setBounds(writex, writey, writew, writeh);
    
    try { res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault()); }
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.exit(1);
    }
   
    JMenuBar  Meny = new JMenuBar();
    JMenu     arkiv = new JMenu(res.getString("file"));
    JMenuItem rad;
    Meny.add(arkiv);

    // Arkiv menyn
    rad = new JMenuItem(res.getString("button.cancel"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/cancel.gif"));
    rad.addActionListener(MListener);
    arkiv.add(rad);

    setJMenuBar(Meny);

    vert1 = Box.createVerticalBox();
    vert2 = Box.createVerticalBox();
    vert3 = Box.createVerticalBox();
    hori1 = Box.createHorizontalBox();
    hori2 = Box.createHorizontalBox();

    myButton = new JButton(res.getString("button.send"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/send.gif"));
    myButton.addActionListener(BListener);
    hori1.add(myButton);

    myButton = new JButton(res.getString("button.cancel"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/cancel.gif"));
    myButton.addActionListener(BListener);
    hori1.add(myButton);

    myLabel = new JLabel(res.getString("mail.to"));
    vert1.add(myLabel);

    myTextField1 = new JTextField();
    myTextField1.setMaximumSize(new Dimension(1200, 20));
    myTextField1.setMinimumSize(new Dimension(75, 20));
    myTextField1.setText(to);
    myTextField1.setToolTipText(res.getString("tofield.tooltip"));
    vert2.add(myTextField1);

    myLabel = new JLabel(res.getString("mail.subject"));
    vert1.add(myLabel);

    myTextField2 = new JTextField();
    myTextField2.addKeyListener(new KeyAdapter(){
      public void keyReleased(KeyEvent ke) { setTitle(myTextField2.getText()); }
      public void keyPressed(KeyEvent ke) { setTitle(myTextField2.getText()); }
    });
    myTextField2.setMaximumSize(new Dimension(1200, 20));
    myTextField2.setMinimumSize(new Dimension(75, 20));
    myTextField2.setText(subject);
    vert2.add(myTextField2);

    hori2.add(vert1);
    hori2.add(vert2);

    vert3.add(hori1);
    vert3.add(hori2);

    myTextArea = new JTextArea();
    myTextArea.setText(body);

    if(props.getProperty("signatur") != null && !props.getProperty("signatur").equals("")) {
      try {
        FileInputStream in = new FileInputStream(props.getProperty("signatur"));
        String tmp = null;
        byte[] singb = new byte[in.available()];

        in.read(singb);
        myTextArea.append(new String(singb));  
        in.close();
      } catch (IOException ioe) { System.err.println(ioe); }
    }

    JTabbedPane JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
    JTPane.addTab(res.getString("mail"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/mail.gif"), new JScrollPane(myTextArea));

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
        String end = s.substring(s.lastIndexOf(System.getProperty("file.separator")), s.length());
        if(end.indexOf(".") != -1) end = end.substring(end.lastIndexOf(".") + 1, end.length());

        if(end.equalsIgnoreCase("gif") || end.equalsIgnoreCase("jpg") || end.equalsIgnoreCase("bmp") || end.equalsIgnoreCase("xpm") || end.equalsIgnoreCase("jpeg") || end.equalsIgnoreCase("xcf") || end.equalsIgnoreCase("png"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/image.gif"));

        else if(end.equalsIgnoreCase("htm") || end.equalsIgnoreCase("html") || end.equalsIgnoreCase("txt") || end.equalsIgnoreCase("doc") || end.equalsIgnoreCase("c") || end.equalsIgnoreCase("cc") || end.equalsIgnoreCase("cpp") || end.equalsIgnoreCase("h") || end.equalsIgnoreCase("java"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/text.gif"));

        else if(end.equalsIgnoreCase("wav") || end.equalsIgnoreCase("au") || end.equalsIgnoreCase("mp3") || end.equalsIgnoreCase("mod") || end.equalsIgnoreCase("xm") || end.equalsIgnoreCase("midi") || end.equalsIgnoreCase("mid"))
          setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/sound.gif"));


        else if(end.equalsIgnoreCase("zip") || end.equalsIgnoreCase("jar") || end.equalsIgnoreCase("rpm") || end.equalsIgnoreCase("deb") || end.equalsIgnoreCase("arj") || end.equalsIgnoreCase("gz") || end.equalsIgnoreCase("tar") || end.equalsIgnoreCase("tgz") || end.equalsIgnoreCase("z"))
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
	else
	    bColor = getBackground();
	g.setColor(bColor);

	if(currentI != null && getText() != null) {
	    int          offset = (currentI.getIconWidth() + getIconTextGap());

	    g.fillRect(offset, 0, getWidth() - 1 - offset,
		       getHeight() - 1);
	}
	else
	    g.fillRect(0, 0, getWidth()-1, getHeight()-1);
	super.paint(g);
      }
    }

    JPanel myPanel = new JPanel(new BorderLayout());

    hori1 = Box.createHorizontalBox();

    myButton = new JButton(res.getString("button.add"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/new.gif"));
    myButton.addActionListener(BListener);
    hori1.add(myButton);

    myButton = new JButton(res.getString("button.delete"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/delete.gif"));
    myButton.addActionListener(BListener);
    hori1.add(myButton);

    attach = new Vector();
    ListModel dataModel = new AbstractListModel() {
      public Object getElementAt(int index) { return attach.elementAt(index); }
      public int    getSize() { return attach.size(); }
    };

    myList = new JList(/* attach */ dataModel);
    myList.setCellRenderer(new MyCellRenderer());
    myPanel.add("Center", myList);
    myPanel.add("South", hori1);

    Border ram = BorderFactory.createEtchedBorder();
//    hori1.setBorder(ram);
//    ram = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    myList.setBorder(ram);


    JTPane.addTab(res.getString("button.attach"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/attach.gif"), myPanel);
   
    vert3.add(JTPane);
    getContentPane().add(vert3);

    addWindowListener(new FLyssnare());
    show();
  }

  boolean isSendReady() {
    
    String TF1 = myTextField1.getText();
    String TF2 = myTextField2.getText();
    String TA  = myTextArea.getText();

    if(TF1.indexOf('@') != -1) {
      if(TF2.length() == 0) myTextField2.setText("(no subject)");
      if(TA.length() == 0) myTextArea.setText("(no body)");

      return true;
    }
    else return false;
  }

  ActionListener BListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      String arg = ((JButton)e.getSource()).getText();

      if(arg.equals(res.getString("button.send"))) {
        if(isSendReady()) {
          try {
            PrintWriter outFile = new PrintWriter(new FileOutputStream(System.getProperty("user.home") + "/.yamm/boxes/outbox", true));
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

            String to = myTextField1.getText(), to2 = "", temp = null;
            StringTokenizer tok = new StringTokenizer(to, ",");

            while(tok.hasMoreTokens()) {
              temp = tok.nextToken().trim();

              if(to2.equals("")) to2 = temp;
              else to2 += "      " + temp;

              if(tok.hasMoreTokens()) to2 += ",\n";
            }

            if(attach.size() == 0) outFile.println("Date: " + dateFormat.format(new Date()) + "\n" 
                                                 + "From: " + props.getProperty("username", "Anonymous") + " <" + props.getProperty("email", " ") + ">\n"
                                                 + "To: " + to2 + "\n"
                                                 + "Subject: " + myTextField2.getText() + "\n"
                                                 + "X-Mailer: Yet Another Mail Manager " + YAMM.yammVersion + "\n"
                                                 + "\n"
                                                 + myTextArea.getText() + "\n"
                                                 + "\n"
                                                 + ".\n");
            else {
              outFile.println("Date: " + dateFormat.format(new Date()) + "\n"
                            + "From: " + props.getProperty("username", "Anonymous") + " <" + props.getProperty("email", " ") + ">" + "\n" 
                            + "To: " + to2 +"\n"
                            + "Subject: " + myTextField2.getText() + "\n"
                            + "X-Mailer: Yet Another Mail Manager " + YAMM.yammVersion + "\n" 
                            + "Content-Type: multipart/mixed; boundary=\"AttachThis\"\n"
                            + "\n"
                            + "This is a multi-part message in MIME format.\n"
                            + "\n"
                            + "--AttachThis\nContent-Type: text/plain; charset=us-ascii\n"
                            + "Content-Transfer-Encoding: 7bit\n"
                            + "\n"
                            + myTextArea.getText()
                            + "\n"
                            + "\n");
            }
            outFile.close();
            if(attach.size() > 0) new UUEncode(attach);
          }
          catch (IOException ioe) { System.out.println("Error: " + ioe); }

          Rectangle rv = new Rectangle();
          getBounds(rv);

          props.setProperty("writex", new Integer(rv.x).toString());
          props.setProperty("writey", new Integer(rv.y).toString());
          props.setProperty("writew", new Integer(rv.width).toString());
          props.setProperty("writeh", new Integer(rv.height).toString());

          try {
            OutputStream out = new FileOutputStream(System.getProperty("user.home") + "/.yamm/.config");
            props.store(out, "YAMM configuration file");
            out.close();
          } catch(IOException propsioe) { System.err.println(propsioe); }

          dispose();
        }
      }

      else if(arg.equals(res.getString("button.add"))) {
        addAttach();
      }

      else if(arg.equals(res.getString("button.delete"))) {
        int rem = myList.getSelectedIndex();
        if(rem != -1) {
          attach.remove(rem);
          myList.updateUI();
        }
      }

      else if(arg.equals(res.getString("button.cancel"))) {
        Rectangle rv = new Rectangle();
        getBounds(rv);

        try {
          InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
          props.load(in);
          in.close();
        } catch (IOException propsioe) { System.err.println(propsioe); }

        props.setProperty("writex", new Integer(rv.x).toString());
        props.setProperty("writey", new Integer(rv.y).toString());
        props.setProperty("writew", new Integer(rv.width).toString());
        props.setProperty("writeh", new Integer(rv.height).toString());

        try {
          OutputStream out = new FileOutputStream(System.getProperty("user.home") + "/.yamm/.config");
          props.store(out, "YAMM configuration file");
          out.close();
        } catch(IOException propsioe) { System.err.println(propsioe); }

        dispose();
      }
    }
  };

  void addAttach() {
    JFileChooser jfs = new JFileChooser();
    jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jfs.setMultiSelectionEnabled(false);
    int ret = jfs.showOpenDialog(this);

    if(ret == JFileChooser.APPROVE_OPTION) {
      if(jfs.getSelectedFile() != null) {

        attach.add(jfs.getSelectedFile());
        myList.updateUI();
      }
    }
  }

  ActionListener MListener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      String kommando = ((JMenuItem)ae.getSource()).getText();

      if(kommando.equals(res.getString("button.cancel"))) {
        Rectangle rv = new Rectangle();
        getBounds(rv);

        try {
          InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
          props.load(in);
          in.close();
        } catch (IOException propsioe) { System.err.println(propsioe); }

        props.setProperty("writex", new Integer(rv.x).toString());
        props.setProperty("writey", new Integer(rv.y).toString());
        props.setProperty("writew", new Integer(rv.width).toString());
        props.setProperty("writeh", new Integer(rv.height).toString());

        try {
          OutputStream out = new FileOutputStream(System.getProperty("user.home") + "/.yamm/.config");
          props.store(out, "YAMM configuration file");
          out.close();
        } catch(IOException propsioe) { System.err.println(propsioe); }

        dispose();
      }
    }
  };

  class FLyssnare extends WindowAdapter {
    public void windowClosing(WindowEvent event) {
      Rectangle rv = new Rectangle();
      getBounds(rv);

      try {
        InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
        props.load(in);
        in.close();
      } catch (IOException propsioe) { System.err.println(propsioe); }

      props.setProperty("writex", new Integer(rv.x).toString());
      props.setProperty("writey", new Integer(rv.y).toString());
      props.setProperty("writew", new Integer(rv.width).toString());
      props.setProperty("writeh", new Integer(rv.height).toString());

      try {
        OutputStream out = new FileOutputStream(System.getProperty("user.home") + "/.yamm/.config");
        props.store(out, "YAMM configuration file");
        out.close();
      } catch(IOException propsioe) { System.err.println(propsioe); }

      dispose();
    }
  }
}

