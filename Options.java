/*  Options.java - Options editing
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
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.table.*;
import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.gui.MsgDialog;

/**
 * A class to edit options
 */
public class Options extends JDialog {

  /** configurartion */
  static protected Properties props = new Properties();

  /** a vector to add items to */
  static protected Vector vect1;

  /** another vector to add items to */
  static protected Vector vect2;

  JTabbedPane    JTPane;
  JTextField     JTField;
  JTable         ServerList;
  JComboBox      JCBox, theme;
  JTextField     addrField, userField;
  JTextField     name, email, signatur;
  JPasswordField JPField;

  /**
   * Creates the stuff.
   * @param SFrame The frame to use as parent
   */
  public Options(JFrame SFrame) {
    super(SFrame, true);
    Dimension screen = getToolkit().getScreenSize();

    setBounds((screen.width - 450)/2, (screen.height - 250)/2, 450, 250);
//    setResizable(false);

    setTitle(YAMM.getString("edit.settings"));

    JTPane = new JTabbedPane(JTabbedPane.LEFT);

    Box vert = Box.createVerticalBox();
    Box hori = Box.createHorizontalBox();
    Box vert2 = Box.createVerticalBox();
    
    JButton b = new JButton(YAMM.getString("button.add"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/new.gif") );
    b.addActionListener(BListener);
    vert.add(b);
    vert.add(Box.createRigidArea(new Dimension(10, 10)));

    b = new JButton(YAMM.getString("edit"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/edit.gif"));
    b.addActionListener(BListener);
    vert.add(b);
    vert.add(Box.createRigidArea(new Dimension(10, 10)));

    b = new JButton(YAMM.getString("button.delete"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/delete.gif"));
    b.addActionListener(BListener);
    vert.add(b);
    vert.add(Box.createRigidArea(new Dimension(10, 10)));

    vect1 = new Vector();
    vect2 = new Vector();

    File[] files = new File(System.getProperty("user.home") + "/.yamm/servers/").listFiles();

    for(int i=0;i<files.length;i++) {
      try {
        InputStream in = new FileInputStream(files[i]); //  + "/.config");
        props = new Properties();
        props.load(in);
        in.close();
      } catch (IOException propsioe) { System.err.println(propsioe); }
      vect1.add(props.getProperty("type"));
      vect1.add(props.getProperty("server"));
      vect1.add(files[i]);
      vect2.add(vect1);
      vect1 = new Vector();
    }


    vect1.add(new String("SMTP"));
    vect1.add(YAMM.getProperty("smtpserver"));
    vect1.add(new String(System.getProperty("user.home") + "/.yamm/.config"));
    vect2.add(vect1);

    TableModel dataModel = new AbstractTableModel() {
      String temp[] = { YAMM.getString("options.type"), YAMM.getString("options.address")};
      public int getColumnCount() { return 2; }
      public int getRowCount() { return ((Vector)vect2).size(); }
      public Object getValueAt(int row, int col) { return ((Vector)vect2.elementAt(row)).elementAt(col); }

      public boolean isCellEditable(int col) { return false; }
      public String getColumnName(int column) { return temp[column]; }
    };

    ServerList = new JTable(dataModel);
    TableColumn column = ServerList.getColumnModel().getColumn(0);
    column.setPreferredWidth(40);
    column.setMaxWidth(50);
    column.setMinWidth(40);


    JScrollPane JSPane = new JScrollPane(ServerList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    JSPane.setMaximumSize(new Dimension(200,100));
    JSPane.setMinimumSize(new Dimension(200,100));
    
    hori.add(Box.createRigidArea(new Dimension(10, 10)));
    hori.add(JSPane);
    hori.add(Box.createRigidArea(new Dimension(10, 10)));
    hori.add(vert);
    JTPane.add(YAMM.getString("options.servers"), hori);

    vert = Box.createVerticalBox();
    filterConf(vert);   
    JTPane.add(YAMM.getString("options.filters"), vert);

    hori = Box.createVerticalBox();
    identitetConfig(hori);
    JTPane.add(YAMM.getString("options.identity"), hori);

    hori = Box.createHorizontalBox();

    b = new JButton(YAMM.getString("button.ok"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/ok.gif"));
    b.addActionListener(BListener);
    hori.add(b);
    hori.add(Box.createRigidArea(new Dimension(10, 10)));


    vert2.add(JTPane);
    vert2.add(Box.createRigidArea(new Dimension(10, 10)));
    vert2.add(hori);

    getContentPane().add(vert2);

    show();
  }

  protected void filterConf(Box vbox) {
    Box hori = Box.createHorizontalBox();
    JTField = new JTextField(4);
    JTField.setMaximumSize(new Dimension(35, 15));
    JTField.setMinimumSize(new Dimension(35, 15));
    JTField.setText(YAMM.getProperty("msgmaxsize", "0"));

    JLabel myLabel = new JLabel(YAMM.getString("msg.bigmsg"));
    hori.add(Box.createRigidArea(new Dimension(10, 10)));
    hori.add(myLabel);
    hori.add(JTField);
    myLabel = new JLabel(" kb");
    hori.add(myLabel);
    
    vbox.add(hori);
    vbox.add(new JLabel(YAMM.getString("msg.bigmsg2")));

    final Vector list = new Vector();

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + "/.yamm/.filters")));
      String temp;
      StringTokenizer tok;

      for(;;) {
        temp = in.readLine();
        if(temp == null) break;

        tok = new StringTokenizer(temp, ";");

        for(int i = 0; i < 4;i++) { tok.nextToken(); }
        list.add(tok.nextToken());
      }
      in.close();
    } catch (IOException propsioe) { System.err.println(propsioe); }

    JList filterList = new JList(list);

    hori = Box.createHorizontalBox();
    Box vert = Box.createVerticalBox();

    JScrollPane JSPane = new JScrollPane(filterList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    JSPane.setMaximumSize(new Dimension(200,100));
    JSPane.setMinimumSize(new Dimension(200,100));    

/*
    JButton b = new JButton(YAMM.getString("ADD"), new ImageIcon("images/default/buttons/new.gif") );
    b.addActionListener(BListener2);
    vert.add(b);
    vert.add(Box.createRigidArea(new Dimension(10, 10)));

    b = new JButton(YAMM.getString("EDIT"), new ImageIcon("images/default/buttons/edit.gif"));
    b.addActionListener(BListener2);
    vert.add(b);
    vert.add(Box.createRigidArea(new Dimension(10, 10)));

    b = new JButton(YAMM.getString("DELETE"), new ImageIcon("images/default/buttons/delete.gif"));
    b.addActionListener(BListener2);
    vert.add(b);
*/
    String sep = System.getProperty("file.separator");
    vert.add(new JLabel("please change manually in"));
    vert.add(new JLabel(System.getProperty("user.home") + sep + ".yamm" + sep + ".filters"));
    hori.add(JSPane);
    hori.add(vert);
    vbox.add(hori);
  }

  protected void identitetConfig(Box vbox) {
    JPanel panel = new JPanel(new GridLayout(6, 1, 2, 2));
    panel.setMaximumSize(new Dimension(600, 100));
    panel.setMinimumSize(new Dimension(300, 100));
    panel.add(new JLabel(YAMM.getString("options.name") + ":"));

    name = new JTextField(YAMM.getProperty("username", System.getProperty("user.name")));
    name.setMaximumSize(new Dimension(400, 20));
    name.setMinimumSize(new Dimension(200, 20));
    panel.add(name);

    String host;
    try {
      host = InetAddress.getLocalHost().getHostName();
    }
    catch(UnknownHostException uhe) { host = "unknown.org"; }

    panel.add(new JLabel("Email:"));

    email = new JTextField(YAMM.getProperty("email", System.getProperty("user.name") + "@" + host));

    email.setMaximumSize(new Dimension(400, 20));
    email.setMinimumSize(new Dimension(200, 20));
    panel.add(email);

    Box hori = Box.createHorizontalBox();

    panel.add(new JLabel(YAMM.getString("options.signatur") + ":"));
    signatur = new JTextField();
    signatur.setMaximumSize(new Dimension(400, 90));
    signatur.setMinimumSize(new Dimension(190, 90));
    signatur.setText(YAMM.getProperty("signatur"));

    hori.add(signatur);

    hori.add(Box.createRigidArea(new Dimension(5, 5)));

    JButton b = new JButton(YAMM.getString("button.browse"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.gif"));
    b.setMaximumSize(new Dimension(230, 1000)); 
    b.setMinimumSize(new Dimension(115, 1000));
    b.addActionListener(BListener);                                    
    hori.add(b);
    panel.add(hori);
    vbox.add(panel);
  }

  ActionListener BListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      String arg = ((JButton)e.getSource()).getText();

      if(arg.equals(YAMM.getString("button.add"))) {
        EDIT(true);
      }

      else if(arg.equals(YAMM.getString("edit"))) {
        EDIT(false);
      }

      else if(arg.equals(YAMM.getString("button.delete"))) {
        DELETE();
      }

      else if(arg.equals(YAMM.getString("button.browse"))) {
        browse();
      }

      else if(arg.equals(YAMM.getString("button.ok"))) {
        YAMM.setProperty("username",  name.getText());
        YAMM.setProperty("email",email.getText());
        YAMM.setProperty("signatur", signatur.getText());
        YAMM.setProperty("msgmaxsize", JTField.getText());

        dispose();
      }
    }
  };

  ActionListener BListener2 = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      String arg = ((JButton)e.getSource()).getText();

      if(arg.equals(YAMM.getString("button.add"))) {
      }

      else if(arg.equals(YAMM.getString("edit"))) {
      }

      else if(arg.equals(YAMM.getString("button.delete"))) {
      }
    }
  };

  void browse() {
    JFileChooser jfs = new JFileChooser();
    jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jfs.setMultiSelectionEnabled(false);
    int ret = jfs.showOpenDialog(this);

    if(ret == JFileChooser.APPROVE_OPTION) {
      if(jfs.getSelectedFile() != null) {
        signatur.setText(jfs.getSelectedFile().toString());
      }
    }
  }

  void EDIT(boolean newserver) {
    new SOptions(this, newserver);
  }

  void DELETE() {
    if(!(ServerList.getSelectedRow() >= vect2.size()) && ServerList.getSelectedRow() != -1 && !((Vector)vect2.elementAt(ServerList.getSelectedRow())).elementAt(0).toString().equals("SMTP")) {
      File   delServer = new File(((Vector)vect2.elementAt(ServerList.getSelectedRow())).elementAt(2).toString());
      vect2.remove(ServerList.getSelectedRow());
      delServer.delete();
      ServerList.updateUI();
    }
  }

  class SOptions extends JDialog {

    String config = "new";

    public SOptions(JDialog PDialog, boolean newserver) {
      super(PDialog, true);
      Dimension screen = getToolkit().getScreenSize();

      setBounds((screen.width - 300)/2, (screen.height - 175)/2, 300, 175);

      if(!newserver && ServerList.getSelectedRow() != -1 && !(ServerList.getSelectedRow() >= vect2.size())) config = ((Vector)vect2.elementAt(ServerList.getSelectedRow())).elementAt(2).toString();

      setResizable(false);
      getContentPane().setLayout(new GridLayout(5, 2, 10, 10));

      if(!config.equals("new")) {
        try {
          InputStream in = new FileInputStream(config);
          props = new Properties();
          props.load(in);
          in.close();
        } catch (IOException propsioe) { System.err.println(propsioe); }
      }

      JLabel myLabel = new JLabel(YAMM.getString("options.type"));
      getContentPane().add(myLabel);           

      String[] items = {"Pop3"};

      JComboBox jcbox = new JComboBox(items);
      getContentPane().add(jcbox);           

      if(config.equals(System.getProperty("user.home") + "/.yamm/.config")) jcbox.setEnabled(false);

      myLabel = new JLabel(YAMM.getString("options.address") + ":");
      getContentPane().add(myLabel);           

      addrField = new JTextField();
      addrField.setMaximumSize(new Dimension(200, 20));
      addrField.setMinimumSize(new Dimension(200, 20));

      if(!config.equals(System.getProperty("user.home") + "/.yamm/.config") && !config.equals("new"))
        addrField.setText(props.getProperty("server"));

      else if(!config.equals("new")) addrField.setText(props.getProperty("smtpserver"));
      getContentPane().add(addrField);
    
      myLabel = new JLabel(YAMM.getString("options.username") + ": ");
      getContentPane().add(myLabel);

      userField = new JTextField();
      userField.setMaximumSize(new Dimension(200, 20));
      userField.setMinimumSize(new Dimension(200, 20));

      if(config.equals(System.getProperty("user.home") + "/.yamm/.config")) userField.setEnabled(false);
      else if(!config.equals("new")) userField.setText(props.getProperty("username"));
      getContentPane().add(userField);

      myLabel = new JLabel(YAMM.getString("options.password") + ":");
      getContentPane().add(myLabel);

      JPField = new JPasswordField();
      JPField.setMaximumSize(new Dimension(200, 20));
      JPField.setMinimumSize(new Dimension(200, 20));

      if(config.equals(System.getProperty("user.home") + "/.yamm/.config")) JPField.setEnabled(false);
      else if(!config.equals("new")) {
        JPField.setText(new SimpleCrypt("myKey").decrypt(props.getProperty("password")));
      }
      getContentPane().add(JPField);

      JButton b = new JButton(YAMM.getString("button.ok"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/ok.gif"));
      b.addActionListener(BListener2);
      getContentPane().add(b);

      b = new JButton(YAMM.getString("button.cancel"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/cancel.gif"));
      b.addActionListener(BListener2);
      getContentPane().add(b);

      show();
    }

    boolean saveServerConf() {
        SimpleCrypt crypt = new SimpleCrypt("myKey");

        if(config.equals("new")) {
          File server = new File(System.getProperty("user.home") + "/.yamm/servers/" + addrField.getText());

          if(server.exists()) {
            new MsgDialog("server exists...");
            return false;
          }
          else {
            try {
              server.createNewFile();

              OutputStream out = new FileOutputStream(server);
              props = new Properties();
              props.setProperty("type", "pop3");
              props.setProperty("server",  addrField.getText());
              props.setProperty("username",    userField.getText());
              props.setProperty("password", crypt.encrypt(new String(JPField.getPassword())));

              props.store(out, "Server Config file");

              out.close();
            } catch (IOException propsioe) { System.err.println(propsioe); }

            vect1 = new Vector();
            vect1.add(props.getProperty("type"));
            vect1.add(props.getProperty("server"));
            vect1.add(server.toString());
            vect2.add(vect1);
          }
        }
            
        else if(!config.equals(System.getProperty("user.home") + "/.yamm/.config")) {
          try {
            OutputStream out = new FileOutputStream(config);

            int j = 0;
  
            for(int i = 0; i <= vect2.size();i++) {
              String tmp = ((Vector)vect2.elementAt(i)).elementAt(1).toString();
              if(tmp.equals(props.getProperty("server"))) { j = i; break; }
            }

            ((Vector)vect2.elementAt(j)).setElementAt(addrField.getText(), 1);

            props = new Properties();
            props.setProperty("server",  addrField.getText());
            props.setProperty("username",    userField.getText());
            props.setProperty("password",    crypt.encrypt(new String(JPField.getPassword())));
            props.setProperty("type", "pop3");

            props.store(out, "Server Config file");

            out.close();
          } catch (IOException propsioe) { System.err.println(propsioe); }
        }

        else {
          YAMM.setProperty("smtpserver", addrField.getText());


          int j = 0;

          for(int i = 0; i <= vect2.size();i++) {
            String tmp = ((Vector)vect2.elementAt(i)).elementAt(0).toString();
            if(tmp.equals("SMTP")) { j = i; break; }
          }

          ((Vector)vect2.elementAt(j)).setElementAt(addrField.getText(), 1);
        }
        ServerList.updateUI();

        return true;
    }

    ActionListener BListener2 = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String arg = ((JButton)e.getSource()).getText();

        if(arg.equals(YAMM.getString("button.ok"))) {
          if(saveServerConf()) dispose();
        }
        else if(arg.equals(YAMM.getString("button.cancel"))) {
          dispose();
        }
      }
    };
  }
}


