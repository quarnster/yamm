/*  mainJTree.java - The JTree for the main-window
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

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;

/**
 * The tree for the main window
 */
public class mainJTree extends JTree {

  /** The ResourceBundle to get menu names. */
  static protected ResourceBundle res;

  /** The Properties that loads and saves information. */
  static protected Properties     props = new Properties();

  JTree tree;
  JPopupMenu treepop;

  DefaultMutableTreeNode top;
  YAMM frame;
  mainToolBar tbar;

  /**
   * Creates the tree and adds all the treestuff to the tree.
   * @param frame2 The JFrame that will be used for error messages etc
   * @param top2 The TreeNode that this tree will use.
   * @param tbar2 The mainToolBar to disable/enable buttons on.
   */
  public mainJTree(JFrame frame2, DefaultMutableTreeNode top2, mainToolBar tbar2) { 
   frame = (YAMM)frame2;
   tbar = tbar2;
   top = top2;
   tree = this;

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

    setModel(new DefaultTreeModel(top));
    setCellRenderer(new myTreeRenderer());
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    addMouseListener(mouseListener2);

    addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)(e.getPath().getLastPathComponent());


        if(!(node.toString()).equals("Mail Boxes")) {
          if(!(new File(node.toString())).isDirectory()) {
            frame.selectedbox = node.toString();
            Mailbox.createList(frame.selectedbox, frame.listOfMails);
            ((JTable)frame.mailList).updateUI();
          }
        }
       if(((JTable)frame.mailList).getSelectedRow() != -1 && !(((JTable)frame.mailList).getSelectedRow() >= frame.listOfMails.size())) { ((JButton)tbar.reply).setEnabled(true); ((JButton)tbar.forward).setEnabled(true); ((JButton)tbar.print).setEnabled(true); }
       else { ((JButton)tbar.reply).setEnabled(false); ((JButton)tbar.forward).setEnabled(false); ((JButton)tbar.print).setEnabled(false); }
      }
    });

    String sep = System.getProperty("file.separator");

    top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
    top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
    createNodes(top, new File(System.getProperty("user.home") + sep + ".yamm" + sep + "boxes" + sep));
    top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash"))); 
    treepop = new JPopupMenu("Test");
    treepop.setInvoker(this);
    expandRow(0);

    // adds items to the myPopup JPopupMenu
    JMenuItem NEW = new JMenuItem(res.getString("NEW")), delete = new JMenuItem(res.getString("DELETE"));

    NEW.addActionListener(treepoplistener);
    delete.addActionListener(treepoplistener);

    treepop.add(NEW);
    treepop.addSeparator();
    treepop.add(delete);
  }

  /**
   * The renderer for the tree
   */
  class myTreeRenderer extends JLabel implements TreeCellRenderer {

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
      StringTokenizer tok = new StringTokenizer(s, System.getProperty("file.separator"));
      String thisbox = null;

      while(tok.hasMoreTokens()) thisbox = tok.nextToken();

      setText(thisbox);

      if(!s.equals("Mail Boxes") && leaf) {
          if(thisbox.equals("inbox")) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/inbox.gif"));
          else if(thisbox.equals("outbox")) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/outbox.gif"));
          else if(thisbox.equals("trash")) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/trash.gif"));
          else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/box.gif"));
      }
      else if(!leaf) {
        if(expanded) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir2.gif"));
        else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir.gif"));
      }
      else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/mailbox.gif"));

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
	    int          offset = (currentI.getIconWidth() + getIconTextGap());

	    g.fillRect(offset, 0, getWidth() - 1 - offset,
		       getHeight() - 1);
	}
	else
	    g.fillRect(0, 0, getWidth()-1, getHeight()-1);
	super.paint(g);
    }
  }

  public void createNodes(DefaultMutableTreeNode top, File f) {
    DefaultMutableTreeNode dir = null;
    DefaultMutableTreeNode box  = null;
    String sep = System.getProperty("file.separator");
    String home = System.getProperty("user.home");

    if((f.toString()).equals(home + sep + ".yamm" + sep + "boxes")) {
      String list[] = f.list();
      for(int i = 0; i < list.length; i++)
        createNodes(top, new File(f, list[i]));
    }
    else if(f.isDirectory()) {
      dir = new DefaultMutableTreeNode(f);

      top.add(dir);
      System.out.println("f: " + f.toString());

      String list[] = f.list();
      for(int i = 0; i < list.length; i++)
        createNodes(dir, new File(f, list[i]));
    }
    else if(!(f.toString()).equals(home + sep + ".yamm" + sep + "boxes" + sep + "outbox") &&
            !(f.toString()).equals(home + sep + ".yamm" + sep + "boxes" + sep + "trash") &&
	    !(f.toString()).equals(home + sep + ".yamm" + sep + "boxes" + sep + "inbox") &&
            (f.toString()).indexOf(sep + ".", (f.toString()).indexOf("boxes")) == -1) {

      box = new DefaultMutableTreeNode(f);
      top.add(box);
    }
  }

  ActionListener treepoplistener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      String kommando = ((JMenuItem)ae.getSource()).getText();
      
      if(kommando.equals(res.getString("NEW"))) {
//        new newBoxDialog(null);
      }
      else {

        File del = new File((tree.getLastSelectedPathComponent()).toString());

        if(!del.isDirectory()) {
          top.removeAllChildren();

          del.delete();

          top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
          top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
          createNodes(top, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
          top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash"))); 
          tree.updateUI();
          tree.expandRow(0);
        }
      }
    }
  };

  MouseListener mouseListener2 = new MouseAdapter() {
    public void mouseReleased(MouseEvent me) {
      if(me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
    }
    public void mousePressed(MouseEvent me) {
      if(me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
    }

  };
}
