/*  BoxTreeRenderer.java - The renderer for the boxtree
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

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.gjt.fredde.yamm.YAMM;

public class BoxTreeRenderer extends JLabel implements TreeCellRenderer {
                                                                   
  /** Whether or not the item that was last configured is selected. */
  protected boolean selected;
                               
  public Component getTreeCellRendererComponent(
    JTree   tree,
    Object  value,
    boolean selected,      
    boolean expanded,
    boolean leaf,
    int     row,     
    boolean hasFocus)
  {
    String s = value.toString();
    StringTokenizer tok = new StringTokenizer(s, YAMM.sep);
    String thisbox = null;

    while(tok.hasMoreTokens()) thisbox = tok.nextToken();


    if(leaf && !thisbox.endsWith(".g")) {
      if(thisbox.equals(YAMM.getString("box.inbox")))
        setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/inbox.gif"));
      else if(thisbox.equals(YAMM.getString("box.outbox")) ||
              thisbox.equals(YAMM.getString("box.sent")))
        setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/outbox.gif"));
      else if(thisbox.equals(YAMM.getString("box.trash")))
        setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/trash.gif"));
      else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/box.gif"));
    }
    else if(!leaf) {
      if(expanded)
        setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir2.gif"));
      else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir.gif"));
    }
    else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir.gif"));

    if(thisbox.endsWith(".g"))
      setText(thisbox.substring(0, thisbox.length() -2));
    else setText(thisbox);
    setForeground(Color.black);
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
      int offset = (currentI.getIconWidth() + getIconTextGap());

      g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
    }
    else
      g.fillRect(0, 0, getWidth()-1, getHeight()-1);
    super.paint(g);
  }
}
