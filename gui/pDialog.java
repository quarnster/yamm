/*  pDialog.java - a progress monitor
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

package org.gjt.fredde.yamm.gui;

import javax.swing.*;
import java.awt.*;

/**
 * A progress dialog
 */
public class pDialog extends JFrame {

  JLabel       status;
  JProgressBar pBar;

  /**
   * Creates a dialog with the specified status text
   * @param text The text to use for status text
   */
  public pDialog(String text) {
    Dimension screen = getToolkit().getScreenSize();
    setBounds((screen.width -300)/2, (screen.height - 75)/2, 300, 75);
    setResizable(false);
    setTitle("Status");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());

    status = new JLabel(text);
    getContentPane().add("North", status);

    pBar = new JProgressBar();
    pBar.setStringPainted(true);
    getContentPane().add("South", pBar);

    show();
  }

  /**
   * Sets the progress to specified int
   * @param prog The value to set the progressbar to
   */
  public void progress(int prog) {
    pBar.setValue(prog);
    pBar.setString(prog + "%");
  }

  /**
   * Sets the string to text
   * @param text The text to set the string to
   */
  public void setString(String text) {
    status.setText(text);
  }
}
