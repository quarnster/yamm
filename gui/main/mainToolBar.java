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
import java.awt.print.PrinterJob;
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

  /** The Properties that loads and saves information. */
  static protected Properties     props = new Properties();

  /**
   * Creates the toolbar.
   * @param frame2 The JFrame to use for error messages etc.
   */
  public mainToolBar(YAMM frame2) {
    frame = frame2;

    try {
      InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
      props.load(in);
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

    /* send mails in outbox get mail to inbox */
    b = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/recycle.gif"));
    b.setBorderPainted(false);
    b.setToolTipText(YAMM.getString("button.send_get"));
    b.addActionListener(BListener);
    add(b);
    addSeparator();


    /* button to write a new mail */
    b = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/new_mail.gif"));
    b.addActionListener(BListener);
    b.setBorderPainted(false);
    b.setToolTipText(YAMM.getString("button.new_mail"));
    add(b);

    /* reply button */
    reply = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/reply.gif"));
    reply.addActionListener(BListener);
    reply.setBorderPainted(false);
    reply.setToolTipText(YAMM.getString("button.reply"));
    reply.setEnabled(false);
    add(reply);

    /* forward button */
    forward = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/forward.gif"));
    forward.addActionListener(BListener);
    forward.setBorderPainted(false);
    forward.setToolTipText(YAMM.getString("button.forward"));
    forward.setEnabled(false);
    add(forward);

    /* button to print page */
    print = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/print.gif"));
    print.setBorderPainted(false);
    print.setToolTipText(YAMM.getString("button.print"));
    print.addActionListener(BListener);
    print.setEnabled(false);
    addSeparator();
    add(print);

    /* button to exit from program */
    b = new JButton(new ImageIcon("org/gjt/fredde/yamm/images/buttons/exit.gif"));
    b.setBorderPainted(false);
    b.setToolTipText(YAMM.getString("button.exit"));
    b.addActionListener(BListener);
    addSeparator();
    add(b);
  }

  ActionListener BListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      String arg = ((JButton)e.getSource()).getToolTipText();

      if(arg.equals(YAMM.getString("button.new_mail"))) {
        new YAMMWrite();
      }
 
      else if(arg.equals(YAMM.getString("button.send_get"))) {
        new SHMail(frame, "mailthread", (JButton)e.getSource()).start();
      }

      else if(arg.equals(YAMM.getString("button.reply"))) {
  
      int i = 0;

        while(i<4) {
          if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
          i++;
        }

        String[] mail = Mailbox.getMailForReplyHeaders(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()));

        YAMMWrite yam = new YAMMWrite(mail[0], mail[1], mail[0] + " " + YAMM.getString("mail.wrote") + "\n");
        Mailbox.getMailForReply(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()), yam.myTextArea);
      }

      else if(arg.equals(YAMM.getString("button.forward"))) {

        int i = 0;

        while(i<4) {
          if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
          i++;
        }

        String mail[] = Mailbox.getMailForReplyHeaders(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()));

        YAMMWrite yam = new YAMMWrite("", mail[1], mail[0] + " " + YAMM.getString("mail.wrote") + "\n");
        Mailbox.getMailForReply(frame.selectedbox, Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString()), yam.myTextArea);
      }
      else if(arg.equals(YAMM.getString("button.print"))) {
/*
        int i = 0;

        while(i<4) {
          if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
          i++;
        }
*/

        PrinterJob pj=PrinterJob.getPrinterJob();

        pj.setPrintable(frame);
        if(pj.printDialog()) {
          try{
            pj.print();
          }catch (Exception PrintException) {}
        }
//        new Print(frame, i);
      }
      else if(arg.equals(YAMM.getString("button.exit"))) {
        frame.Exit();
      }
    }
  };
}
