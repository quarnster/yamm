/*  MsgDialog.java - for displaying messages
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

package org.gjt.fredde.util.gui;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;

/** A message dialog used for displaying error messages and questions etc. */
public class MsgDialog extends JDialog {

  int result = -1;

  /** To get language strings */
  static protected ResourceBundle res;

  /** The value of an ok button */
  public static final int OK     = 1;

  /** The value of an yes button */
  public static final int YES    = 2;

  /** The value of a no button */
  public static final int NO     = 4;

  JPanel  bpanel;
  JButton b1, b2;

  /**
   * Creates a new message dialog with the specified text.
   * @param text The text to display
   */
  public MsgDialog(String text) {
    this(null, "Message", text, OK, JLabel.CENTER);
  }

  /**
   * Creates a new message dialog with the specified text.
   * @param frame The frame to use as parent
   * @param text The text to display
   */
  public MsgDialog(JFrame frame, String text) {
    this(frame, "Message", text, OK, JLabel.CENTER);
  }

  /**
   * Creates a new message dialog with the specified text and title
   * @param frame The frame to use as parent
   * @param title The title to use
   * @param text The text to display
   */
  public MsgDialog(JFrame frame, String title, String text) {
    this(frame, title, text, OK, JLabel.CENTER);
  }

  /**
   * Creates a new message dialog with the specified text, title and buttons
   * @param frame The frame to use as parent
   * @param title The title to use
   * @param text The text to display
   * @param buttons The buttons to use
   */
  public MsgDialog(JFrame frame, String title, String text, int buttons) {
    this(frame, title, text, buttons, JLabel.CENTER);
  }

  /**
   * Creates a new message dialog with the specified text, title, buttons and text alignment
   * @param frame The frame to use as parent
   * @param title The title to use
   * @param text The text to display
   * @param buttons The buttons to use
   * @param alignment The alignment of the text
   */
  public MsgDialog(JFrame frame, String title, String text, int buttons, int alignment) {
    super(frame, true);

    setResizable(false);
    setTitle(title);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    FontMetrics fm = getFontMetrics(new Font("Dialog", Font.BOLD, 12));

    getContentPane().setLayout(new BorderLayout(0, 5));

    int width = 100, height = 70;

    int rows = 0, check = 0;
    do {
      check = text.indexOf('\n', check+1);
      height += fm.getHeight();
      rows++;
    } while(check > 0);

    JPanel panel = new JPanel(new GridLayout(rows, 1));
    JLabel msg[] = new JLabel[rows];

    String label;


    int end = 0, start = 0, widest = 0;

    for(int i = 0; i < rows;i++) {
      end = text.indexOf("\n", start + 1);
      if(end == -1) end = text.length();
      label = text.substring(start, end);

      widest = fm.stringWidth(label) + 10; // + 10 to avoid ...
      if(widest > width) width = widest;
      msg[i] = new JLabel(label);
      start = end + 1;
      msg[i].setHorizontalAlignment(alignment);
      panel.add(msg[i]);

      if(start < text.length() && text.substring(start, start + 1).equals("\n")) {
        do {
          i++;
          msg[i] = new JLabel("");
          msg[i].setHorizontalAlignment(JLabel.CENTER);
          panel.add(msg[i]);
          start++;
        } while(start < text.length() && text.substring(start, start + 1).equals("\n"));
      }
    }
    getContentPane().add("Center", panel);

    Dimension screen = getToolkit().getScreenSize();
    setBounds((screen.width - width)/2, (screen.height - height)/2, width, height);

    if((buttons & OK) > 0) {
      b1 = new JButton("Ok", new ImageIcon("images/default/buttons/ok.gif"));
      b1.addActionListener(BListener);
      b1.setMaximumSize(new Dimension(20,20));
      bpanel = new JPanel(new GridLayout(1, 1, 7, 10));
      bpanel.setMaximumSize(new Dimension(50, 20));
      bpanel.add(b1);
    }
    else if((buttons & YES) > 0 && (buttons & NO) > 0) {
      b1 = new JButton("Yes", new ImageIcon("images/default/buttons/ok.gif"));
      b2 = new JButton("No", new ImageIcon("images/default/buttons/cancel.gif"));
      b1.setMaximumSize(new Dimension(20,20));
      b1.addActionListener(BListener);
      b2.setMaximumSize(new Dimension(20,20));
      b2.addActionListener(BListener);
      bpanel = new JPanel(new GridLayout(1, 2, 7, 10));
      bpanel.setMaximumSize(new Dimension(50, 20));
      bpanel.add(b1);
      bpanel.add(b2);
    }

    getContentPane().add("South", bpanel);
    show();
  }

  ActionListener BListener = new ActionListener() {
    public void actionPerformed(ActionEvent event) {
      String button = event.getActionCommand();
    
      if(button == "Ok") {
        dispose();
      }
      if(button == "Yes") result = YES;
      else if(button == "No") result = NO;
      setVisible(false);
    }
  };

  public int getResult() {
    return result;
  }
}
