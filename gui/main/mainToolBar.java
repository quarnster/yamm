/*  mainToolBar.java - The tool bar for the main-window
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

package org.gjt.fredde.yamm.gui.main;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Rectangle;
import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;

import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.YAMMWrite;
import org.gjt.fredde.yamm.SHMail;
import org.gjt.fredde.yamm.Print;
// import gui.printPreview;

/**
 * The toolbar for the main class
 */
public class mainToolBar extends JToolBar {

  /** The forward button */
  public JButton forward;

  /** The reply button */
  public JButton reply;

  /** The print button */
  public JButton print;

  JButton b;
  YAMM frame;

  /** The ResourceBundle to get menu names. */
  static protected ResourceBundle res;

  /** The Properties that loads and saves information. */
  static protected Properties     props = new Properties();

  /**
   * Creates the toolbar.
   * @param frame2 The JFrame to use for error messages etc.
   */
  public mainToolBar(JFrame frame2) {
    frame = (YAMM)frame2;

    try {
      InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
      props.load(in);
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

    try {
      res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault());
    }
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.exit(1);
    }

    /* send mails in outbox get mail to inbox */
    b = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/recycle.gif"));
    b.setBorderPainted(false);
    b.setToolTipText(res.getString("SENDGET"));
    b.addActionListener(BListener);
    add(b);
    addSeparator();


    /* button to write a new mail */
    b = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/new_mail.gif"));
    b.addActionListener(BListener);
    b.setBorderPainted(false);
    b.setToolTipText(res.getString("NMAIL"));
    add(b);

    /* reply button */
    reply = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/reply.gif"));
    reply.addActionListener(BListener);
    reply.setBorderPainted(false);
    reply.setToolTipText(res.getString("REPLY"));
    reply.setEnabled(false);
    add(reply);

    /* forward button */
    forward = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/forward.gif"));
    forward.addActionListener(BListener);
    forward.setBorderPainted(false);
    forward.setToolTipText(res.getString("FORWARD"));
    forward.setEnabled(false);
    add(forward);

    /* button to print page */
    print = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/print.gif"));
    print.setBorderPainted(false);
    print.setToolTipText(res.getString("PRINT"));
    print.addActionListener(BListener);
    print.setEnabled(false);
    addSeparator();
    add(print);

    /* button to exit from program */
    b = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/exit.gif"));
    b.setBorderPainted(false);
    b.setToolTipText(res.getString("EXIT"));
    b.addActionListener(BListener);
    addSeparator();
    add(b);
  }

  ActionListener BListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      String arg = ((JButton)e.getSource()).getToolTipText();

      if(arg.equals(res.getString("NMAIL"))) {
        new YAMMWrite();
      }
 
      else if(arg.equals(res.getString("SENDGET"))) {
        new SHMail(frame, "mailthread", (JButton)e.getSource()).start();
      }

      else if(arg.equals(res.getString("REPLY"))) {
  
      int i = 0;

        while(i<4) {
          if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
          i++;
        }

        String[] mail = Mailbox.getMailForReplyHeaders(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()));
//        String from = mail[0]; //.substring(0, mail.indexOf("\n", 0));
//        String subject = res.getString("RE") + mail.substring(mail.indexOf("Subject:") + 8, mail.indexOf("\n", mail.indexOf("Subject:") + 8));
//        String actualmail = from + " " + res.getString("WROTE") + "\n" + mail.substring(mail.indexOf("Begin:") + 8, mail.length());

        YAMMWrite yam = new YAMMWrite(mail[0], mail[1], mail[0] + " " + res.getString("WROTE") + "\n");
        Mailbox.getMailForReply(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()), yam.myTextArea);
      }

      else if(arg.equals(res.getString("FORWARD"))) {

        int i = 0;

        while(i<4) {
          if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
          i++;
        }

        String mail[] = Mailbox.getMailForReplyHeaders(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()));
//        String from = mail.substring(0, mail.indexOf("\n", 0));
//        String subject = res.getString("RE") + mail.substring(mail.indexOf("Subject:") + 8, mail.indexOf("\n", mail.indexOf("Subject:") + 8));
//        String actualmail = from + " " + res.getString("WROTE") + "\n";

        YAMMWrite yam = new YAMMWrite("", mail[1], mail[0] + " " + res.getString("WROTE") + "\n");
        Mailbox.getMailForReply(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()), yam.myTextArea);
      }
      else if(arg.equals(res.getString("PRINT"))) {
        int i = 0;

        while(i<4) {
          if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
          i++;
        }

        new Print(frame, i);
      }
      else if(arg.equals(res.getString("EXIT"))) {
        Rectangle rv = new Rectangle();
        frame.getBounds(rv);

        try {
          InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
          props.load(in);
          in.close();
        } catch (IOException propsioe) { System.err.println(propsioe); }

        props.setProperty("mainx", new Integer(rv.x).toString());
        props.setProperty("mainy", new Integer(rv.y).toString());
        props.setProperty("mainw", new Integer(rv.width).toString());
        props.setProperty("mainh", new Integer(rv.height).toString());
//        props.setProperty("vsplit", new Integer(((JSplitPane)frame.SPane2).getDividerLocation()).toString());
//        props.setProperty("hsplit", new Integer(((JSplitPane)frame.SPane).getDividerLocation()).toString());

        try {
          OutputStream out = new FileOutputStream(System.getProperty("user.home") + "/.yamm/.config");
          props.store(out, "YAMM configuration file");
          out.close();
        } catch(IOException propsioe) { System.err.println(propsioe); }

        frame.dispose();
        System.exit(0);
      }
    }
  };

}
