/*  statusRow.java - a simple status row
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

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.border.*;

public class statusRow extends JPanel {

  protected JLabel status;
  protected JProgressBar progress;
  public    JButton button;

  public statusRow() {
    this("", 0);
  }

  public statusRow(String text) {
    this(text, 0);
  }

  public statusRow(String text, int prog) {
    setLayout(new GridLayout(1, 2));

    status = new JLabel(text);
    status.setFont(new Font("SansSerif", Font.PLAIN, 10));
    status.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    add(status);

    JPanel p = new JPanel();
    p.setLayout(new GridLayout(1, 2));
    add(p);
 
    progress = new JProgressBar();
    progress.setValue(prog);
    progress.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    p.add(progress);

    button = new JButton();
    button.setFont(new Font("SansSerif", Font.PLAIN, 10));
    button.setEnabled(false);
    p.add(button);
  }

  public void setStatus(String text) {
    status.setText(text);
  }

  public void progress(int prog) {
    progress.setValue(prog);
  }
}
