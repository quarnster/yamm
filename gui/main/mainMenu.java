/*  mainMenu.java - mainMenu class
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
import javax.swing.filechooser.FileFilter;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.gjt.fredde.util.gui.MsgDialog;
import org.gjt.fredde.yamm.gui.sourceViewer;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.YAMMWrite;
import org.gjt.fredde.yamm.Options;
import org.gjt.fredde.yamm.YAMM;

/**
 * The mainMenu class.
 * This is the menu that the mainwindow uses.
 */
public class mainMenu extends JMenuBar {

  /**
   * This frame is used to do som framestuff
   */
  static YAMM frame = null;
  /**
   * The ResourceBundle to get menu names.
   */
  static protected ResourceBundle res;
  /**
   * The Properties that loads and saves information.
   */
  static protected Properties     props = new Properties();

  /**
   * Makes the menu, adds a menulistener etc...
   * @param frame2 The JFrame that will be used when displaying error messages etc
   */
  public mainMenu(JFrame frame2, ResourceBundle res) {
    frame = (YAMM)frame2;
    this.res = res;

    try {
      InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
      props.load(in);
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

/*
    try {
      res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault());
    }
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.exit(1);
    }
*/
    JMenu file = new JMenu(res.getString("file"));
    JMenuItem rad;
    add(file);


    // the file menu
    rad = new JMenuItem(res.getString("file.new"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/new_mail.gif"));
    rad.addActionListener(MListener);
    file.add(rad);

    rad = new JMenuItem(res.getString("file.save_as"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/save_as.gif"));
    rad.addActionListener(MListener);
    file.add(rad);

    file.addSeparator();

    rad = new JMenuItem(res.getString("file.exit"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/exit.gif"));
    rad.addActionListener(MListener);
    file.add(rad);

    // the edit menu
    JMenu edit = new JMenu(res.getString("edit"));
    add(edit);

    rad = new JMenuItem(res.getString("edit.settings"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/prefs.gif"));
    rad.addActionListener(MListener);
    edit.add(rad);

    rad = new JMenuItem(res.getString("edit.view_source"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.gif"));
    rad.addActionListener(MListener);
    edit.add(rad);

    // the help menu
    JMenu help = new JMenu(res.getString("help"));
    add(help);

    rad = new JMenuItem(res.getString("help.about_you"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/help.gif"));
    rad.addActionListener(MListener);
    help.add(rad);

    rad = new JMenuItem(res.getString("help.about"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/help.gif"));
    rad.addActionListener(MListener);
    help.add(rad);

    rad = new JMenuItem(res.getString("help.license"), new ImageIcon("org/gjt/fredde/yamm/images/types/text.gif"));
    rad.addActionListener(MListener);
    help.add(rad);

    rad = new JMenuItem(res.getString("help.bug_report"));
    rad.addActionListener(MListener);
    help.add(rad);
  }

  ActionListener MListener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      String kommando = ((JMenuItem)ae.getSource()).getText();

      if(kommando.equals(res.getString("file.exit"))) {

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
      else if(kommando.equals(res.getString("help.about_you"))) {
        String host = null, ipaddress = null;

        try {
          InetAddress myInetaddr = InetAddress.getLocalHost();

          ipaddress = myInetaddr.getHostAddress();
          host = myInetaddr.getHostName();
        }
        catch (UnknownHostException uhe) {};
        if (ipaddress == null) ipaddress = "unknown";
        if (host == null) host = "unknown";

        new MsgDialog(null, res.getString("help.about_you"),
                      res.getString("info.os") + " : " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n"
                    + res.getString("info.cpu") + " : " + System.getProperty("os.arch") + "\n"
                    + res.getString("info.ipaddress") + " : " + ipaddress + "\n"
                    + res.getString("info.host") + " : " + host + "\n"
                    + res.getString("info.java.version") + " : " + System.getProperty("java.version") + "\n"
                    + res.getString("info.java.vendor") + " : " + System.getProperty("java.vendor") + "\n"
                    + res.getString("info.java.vendor.url") + " : " + System.getProperty("java.vendor.url") + "\n"
                    + res.getString("info.username") + " : " + System.getProperty("user.name") + "\n"
                    + res.getString("info.user.home") + " : " + System.getProperty("user.home"));
      }

      else if(kommando.equals(res.getString("help.about"))) {
        new MsgDialog(null, res.getString("help.about"),
                      "Copyright (C) 1999 Fredrik Ehnbom\n"
                    + "YAMM-version: " + YAMM.yammVersion + "\n"
                    + "Compiledate: " + YAMM.compDate + "\n"
                    + "Homepage: http://www.gjt.org/~fredde/\n"
                    + "E-mail: <fredde@gjt.org>\n"
                    + "\n"
                    + "Most icons are made or based on icons made\n"
                    + "by Tuomas Kuosmanen <tigert@gimp.org>");
                    
      }
      else if(kommando.equals(res.getString("help.bug_report"))) {
        YAMMWrite yw = new YAMMWrite("fredde@gjt.org", "Bug report");
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
      }
      else if(kommando.equals(res.getString("help.license"))) {
        new MsgDialog(null, res.getString("help.license"), 
                      "Yet Another Mail Manager " + YAMM.yammVersion + " E-Mail Client\nCopyright (C) 1999 Fredrik Ehnbom\n" +
                      "\n" +
                      "This program is free software; you can redistribute it and/or modify\n" +
                      "it under the terms of the GNU General Public License as published by\n" +
                      "the Free Software Foundation; either version 2 of the License, or\n" +
                      "(at your option) any later version.\n" +
                      "\n" +
                      "This program is distributed in the hope that it will be useful,\n" +
                      "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                      "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                      "GNU General Public License for more details.\n" +
                      "\n" +
                      "You should have received a copy of the GNU General Public License\n" +
                      "along with this program; if not, write to the Free Software\n" +
                      "Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA", MsgDialog.OK, JLabel.LEFT);
      }  
      else if(kommando.equals(res.getString("edit.settings"))) {
        new Options(frame);
      }
      else if(kommando.equals(res.getString("file.new"))) {
        new YAMMWrite();
      }
      else if(kommando.equals(res.getString("edit.view_source"))) {
        int test = ((JTable)frame.mailList).getSelectedRow();
        if(test >= 0 && test < frame.listOfMails.size()) {
          int i = 0;

          while(i<4) {
            if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
            i++;
          }

          int msg = Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString());
          if(msg != -1) Mailbox.viewSource(frame.selectedbox, msg, new sourceViewer().jtarea);
        }
      }
      else if(kommando.equals(res.getString("file.save_as"))) {
        int test = ((JTable)frame.mailList).getSelectedRow();
        if(test != -1 && test <= frame.listOfMails.size()) {
          JFileChooser jfs = new JFileChooser();
          jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
          jfs.setMultiSelectionEnabled(false);
          jfs.setFileFilter(new filter());
          jfs.setSelectedFile(new File("mail.html"));
          int ret = jfs.showSaveDialog(frame);

          if(ret == JFileChooser.APPROVE_OPTION) {
              new File(System.getProperty("user.home") + "/.yamm/tmp/" + frame.mailName + ".html").renameTo(jfs.getSelectedFile());
          }
        }
      }
    }
    void p(String prop, JTextArea ta) {
      ta.append(prop + " : " + System.getProperty(prop) + "\n");
    }

  };

  protected class filter extends FileFilter {
    public boolean accept(File f) {
      if(f.isDirectory()) {
         return true;
      }

      String s = f.getName();
      int i = s.lastIndexOf('.');
      if(i > 0 &&  i < s.length() - 1) {
        String extension = s.substring(i+1).toLowerCase();
        if ("htm".equals(extension) || "html".equals(extension)) return true;
        else return false;
      }
      return false;
    }
       
    // The description of this filter
    public String getDescription() {
      return "HTML files";
    }
  }
}
   
