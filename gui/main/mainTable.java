/*  mainTable.java - The JTable for the main-window
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
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.YAMMWrite;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.util.gui.MsgDialog;

/**
 * The Table for listing the mails subject, date and sender.
 */
public class mainTable extends JTable implements DragGestureListener,
                                                 DragSourceListener {

  /** If it should sort 1 to 10 or 10 to 1*/
  static protected boolean                firstSort    = true;

  /** Which column that was sorted */
  static protected int                    sortedCol    = 0;

  /** The list of mails */
  static protected Vector		  listOfMails = null;

  static protected YAMM                   frame = null;

  public JPopupMenu             popup = null;

  static protected ResourceBundle         res   = null;

  /**
   * Creates a new JTable
   * @param tm The TableModel to use
   * @param listOfMails the Maillist vector
   */
  public mainTable(YAMM frame, ResourceBundle res, TableModel tm, Vector listOfMails) {

    DragSource dragSource = DragSource.getDefaultDragSource();

    dragSource.createDefaultDragGestureRecognizer(
      this, // drag component
      DnDConstants.ACTION_COPY_OR_MOVE, // actions
      this); // drag gesture listener


    this.listOfMails = listOfMails;
    this.frame = frame;
    this.res = res;
    setModel(tm);

    TableColumn column = null;
    column = getColumnModel().getColumn(3);
    column.setPreferredWidth(125);
    column.setMaxWidth(140);
    column.setMinWidth(10);
    column = getColumnModel().getColumn(0);
    column.setPreferredWidth(20);
    column.setMinWidth(10);
    column.setMaxWidth(50);

    setRowHeight(12);
    setSelectionMode(2);
    setColumnSelectionAllowed(false);
    setShowHorizontalLines(false);
    setShowVerticalLines(false);
    setIntercellSpacing(new Dimension(0, 0));
    myRenderer rend = new myRenderer();
    rend.setFont(new Font("SansSerif", Font.PLAIN, 12));
    setDefaultRenderer(getColumnClass(0), rend /* new myRenderer() */ );

    TMListener(this);

    addMouseListener(mouseListener);

    popup = new JPopupMenu();
    popup.setInvoker(this);

    createPopup(popup);
  }

  protected String getSelected() {
    String selected = "";

    int i = 0;
      
    while(i<4) {
      if(getColumnName(i).equals("#")) { break; }
      i++;     
    }    

    int[] mlist = getSelectedRows();
    int[] dragList = new int[mlist.length];

    for(int j = 0;j < mlist.length;j++) {
      dragList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
    }

    Arrays.sort(dragList);
    for(int j = 0; j < dragList.length; j++) {
      selected += dragList[j] + " ";
    }

    return selected;
  }

  public void dragGestureRecognized(DragGestureEvent e) {
    e.startDrag(DragSource.DefaultMoveDrop, // cursor
                new StringSelection(getSelected()), // transferable
                this); // drag source listener
   }

   public void dragDropEnd(DragSourceDropEvent e) {}
   public void dragEnter(DragSourceDragEvent e) {}
   public void dragExit(DragSourceEvent e) {}
   public void dragOver(DragSourceDragEvent e) {}
   public void dropActionChanged(DragSourceDragEvent e) {}

  /**
   * Sorts 1 -> 10
   */
  protected void SortFirst(int row) {
    for(int i = 0; i<listOfMails.size();i++) {
      Object temp = null;
      for (int j=0; j<listOfMails.size(); j++) {
        if (((Vector)listOfMails.elementAt(i)).elementAt(row).toString().compareTo(((Vector)listOfMails.elementAt(j)).elementAt(row)) > 0 ) {
           temp = listOfMails.elementAt(j);
           listOfMails.setElementAt(listOfMails.elementAt(i), j);
           listOfMails.setElementAt(temp, i);
        }
      }
    }
    updateUI();
  }

  /**
   * Sorts 10 -> 1
   */
  protected void SortLast(int row) {
    for(int i = 0; i<listOfMails.size();i++) {
      Object temp = null;
      for (int j=0; j<listOfMails.size(); j++) {
        if (((Vector)listOfMails.elementAt(i)).elementAt(row).toString().compareTo(((Vector)listOfMails.elementAt(j)).elementAt(row)) < 0 ) {
           temp = listOfMails.elementAt(i);
           listOfMails.setElementAt(listOfMails.elementAt(j), i);
           listOfMails.setElementAt(temp, j);
        }
      }
    }
    updateUI();
  }



  public void createPopup(JPopupMenu jpmenu) {
    Vector list = new Vector(), list2 = new Vector();          
    String boxHome = System.getProperty("user.home") + YAMM.sep +
                     ".yamm" + YAMM.sep + "boxes";
    
    fileList(list, new File(boxHome + YAMM.sep));
    fileList(list2, new File(boxHome + YAMM.sep));
    
    JMenuItem row, delete = new JMenuItem(res.getString("button.delete")),
              reply = new JMenuItem(res.getString("button.reply")); 
    delete.addActionListener(OtherMListener);
    reply.addActionListener(OtherMListener);

    jpmenu.add(reply);
    jpmenu.addSeparator();
    createPopCommand(jpmenu, res.getString("edit.copy"), list, boxHome, KMListener);
    createPopCommand(jpmenu, res.getString("edit.move"), list2, boxHome, FMListener);
    jpmenu.add(delete);
  }

    
  private void createPopCommand(JMenu menu, Vector flist, String base, ActionListener list) {
    String dname = base.substring(base.lastIndexOf(File.separator) + 1, base.length());
    if(dname.endsWith(".g")) dname = dname.substring(0, dname.length()-2);
    JMenu m = new JMenu(dname);

    for(int j=0;j < flist.size();j++) {
      String fpath = flist.elementAt(j).toString();

      if(fpath.indexOf(base) != -1) {
        String fname = fpath.substring(base.length() +1, fpath.length());

        if(fname.indexOf(File.separator) != -1) {
          menu.add(m); 
          String path = flist.elementAt(j).toString();
          path = path.substring(0, path.lastIndexOf(File.separator)); 
          createPopCommand(m, flist, path, list); 
          j--;
        }
        else {
          extMItem mitem = new extMItem(fname);
          mitem.setFullName(fpath);
          mitem.addActionListener(list);
          flist.remove(j);
          j--;
          m.add(mitem);
        }
      }
      else { 
        menu.add(m);
        j--;
        break;
      }
    }
    menu.add(m);
  }

  private void createPopCommand(JPopupMenu menu, String menuName, Vector flist, String base, ActionListener list) {
    JMenu m = new JMenu(menuName);                                                                            
                                  
    for(int j=0;j < flist.size();j++) {
      String fpath = flist.elementAt(j).toString();
                                              
      if(fpath.indexOf(base) != -1) {         
        String fname = fpath.substring(base.length() +1, fpath.length());

        if(fname.indexOf(File.separator) != -1) {
          menu.add(m);
          String path = flist.elementAt(j).toString();
          path = path.substring(0, path.lastIndexOf(File.separator));
          createPopCommand(m, flist, path, list);
          j--;
        }     
        else {
          extMItem mitem = new extMItem(fname);
          mitem.setFullName(fpath);
          mitem.addActionListener(list);
          flist.remove(j);
          j--;
          m.add(mitem);
        }
      }  
      else {
        menu.add(m);
        j--;
        break;
      }       
    }  
    menu.add(m);
  }

  private void fileList(Vector vect, File f) {
    if((f.toString()).equals(System.getProperty("user.home") + "/.yamm/boxes")) {
      String list[] = f.list();                                                  
      for(int i = 0; i < list.length; i++)
        fileList(vect, new File(f, list[i]));
    }                                     
    
    else if(f.isDirectory()) {
      String list[] = f.list();
      for(int i = 0; i < list.length; i++)
        fileList(vect, new File(f, list[i]));
    }
    else {
      String test = f.toString();
      test = test.substring(test.lastIndexOf(File.separator) + 1, test.length());
      if(!test.startsWith(".")) vect.add(f.toString()); 
    }
  }
 


  /**
   * Adds the sorting listener to the table header
   * @param table The JTable to add the sorting listener to
   */
  protected void TMListener(JTable table) {
    final JTable tableView = table;
    tableView.setColumnSelectionAllowed(false);

    MouseAdapter lmListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
          TableColumnModel columnModel = tableView.getColumnModel();
          int viewColumn = columnModel.getColumnIndexAtX(e.getX());
          int column = tableView.convertColumnIndexToModel(viewColumn);

          if(column != -1) {
            if(column == sortedCol) {
              if(firstSort) {
                SortFirst(column);
                firstSort = false;
              }
              else {
                SortLast(column);
                firstSort = true;
              }
            }
            else {
              SortFirst(column);
              firstSort = false;
              sortedCol = column;
            }
          }
      }
    };
    JTableHeader th = tableView.getTableHeader();
    th.addMouseListener(lmListener);
  }

  /**
   * The renderer for the table
   */
  protected class myRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(
      JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int column) 
    {
      setValue(value);
      setForeground(Color.black);
      if(isSelected) setBackground(new Color(204, 204, 255));
      else setBackground(Color.white);

      return this;
    }
  }



  protected MouseListener mouseListener = new MouseAdapter() {
    public void mouseReleased(MouseEvent me) {
      if(me.isPopupTrigger()) popup.show(mainTable.this, me.getX(), me.getY());
      else if(getSelectedRow() != -1) get_mail();

 
      if(getSelectedRow() != -1 && !(getSelectedRow() >= frame.listOfMails.size())) { 
        ((JButton)frame.tbar.reply).setEnabled(true); 
        ((JButton)frame.tbar.print).setEnabled(true); 
        ((JButton)frame.tbar.forward).setEnabled(true);
      }
      else { 
        ((JButton)frame.tbar.reply).setEnabled(false); 
        ((JButton)frame.tbar.forward).setEnabled(false); 
        ((JButton)frame.tbar.print).setEnabled(false); 
      }
    }
    public void mousePressed(MouseEvent me) {
      if(me.isPopupTrigger()) popup.show(mainTable.this, me.getX(), me.getY());
    }
 
    void get_mail() {
      int i = 0;
 
      while(i<4) {
        if(getColumnName(i).equals("#")) { break; }
        i++;
      }
      int whatMail = Integer.parseInt(getValueAt(getSelectedRow(), i).toString());

      Mailbox.getMail(frame.selectedbox,whatMail);
      try {
        String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/"; 
        frame.mailPage = new URL(frame.mailPageString + boxName + whatMail + ".html");
      }
      catch (MalformedURLException mue) { new MsgDialog(frame, res.getString("msg.error"), mue.toString()); }
 
      try { frame.mail.setPage(frame.mailPage); }
      catch (IOException ioe) { new MsgDialog(frame, res.getString("msg.error"), ioe.toString()); }

      frame.createAttachList();
      frame.myList.updateUI();
    }
  };

  protected ActionListener OtherMListener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {       
      String kommando = ((JMenuItem)ae.getSource()).getText();
                                                              
      int i = 0;
      
      while(i<4) {
        if(getColumnName(i).equals("#")) { break; }
        i++;     
      }    

      if(kommando.equals(res.getString("button.delete"))) {
        frame.delUnNeededFiles();
        int[] mlist = getSelectedRows();
        int[] deleteList = new int[mlist.length];

        for(int j = 0;j < mlist.length;j++) {
          deleteList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
        }

        Arrays.sort(deleteList);

//        for (int a=deleteList.length -1; a>=0; a-- ) {
          Mailbox.deleteMail(frame.selectedbox, deleteList);
//        }
         
        Mailbox.createList(frame.selectedbox, listOfMails);

        updateUI();
        frame.attach = new Vector();


        Mailbox.getMail(frame.selectedbox, getSelectedRow() /*, frame.attach, frame.mailName */);

        try { 
          String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/"; 
          frame.mailPage = new URL(frame.mailPageString + boxName + getSelectedRow()  + ".html"); 
        }
        catch (MalformedURLException mue) { new MsgDialog(frame, res.getString("msg.error"), mue.toString()); }

        try { frame.mail.setPage(frame.mailPage); }
        catch (IOException ioe) { new MsgDialog(frame, res.getString("msg.error"), ioe.toString()); }
      }

      else if(kommando.equals(res.getString("button.reply"))) {
                 
        String[] mail = Mailbox.getMailForReplyHeaders(frame.selectedbox, 
                                Integer.parseInt(getValueAt(getSelectedRow(), i).toString()));
 
        YAMMWrite yam = new YAMMWrite(mail[0], mail[1], mail[0] + " " + res.getString("mail.wrote") + "\n");
        Mailbox.getMailForReply(frame.selectedbox, 
                                Integer.parseInt(getValueAt(getSelectedRow(), i).toString()), 
                                yam.myTextArea);
      }
    }
  };

  protected ActionListener KMListener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      String name = ((extMItem)ae.getSource()).getFullName();
                                                              
      int i = 0;                                              
                
      while(i<4) {
        if(getColumnName(i).equals("#")) { break; }
        i++;
      }     
       
      int[] mlist = getSelectedRows();
      int[] copyList = new int[mlist.length];  
                                             
      for(int j = 0;j < mlist.length;j++) {  
        copyList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
      }                                                                             
       
      Mailbox.copyMail(frame.selectedbox, name, copyList);
    }
  };

  protected ActionListener FMListener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      String name = ((extMItem)ae.getSource()).getFullName();

      int i = 0;

      while(i<4) {
        if(getColumnName(i).equals("#")) { break; }
        i++;
      }


      int[] mlist = getSelectedRows();
      int[] moveList = new int[mlist.length];

      for(int j = 0;j < mlist.length;j++) {
        moveList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
      }


      Mailbox.moveMail(frame.selectedbox, name, moveList);

      Mailbox.createList(frame.selectedbox, frame.listOfMails);

      mainTable.this.updateUI();

      Mailbox.getMail(frame.selectedbox, getSelectedRow());

      try { 
        String boxName = frame.selectedbox.substring(frame.selectedbox.indexOf("boxes") + 6, frame.selectedbox.length()) + "/"; 
        frame.mailPage = new URL(frame.mailPageString + boxName + getSelectedRow() + "html"); 
      }
      catch (MalformedURLException mue) { new MsgDialog(frame, res.getString("msg.error"), mue.toString()); }

      try { frame.mail.setPage(frame.mailPage); }
      catch (IOException ioe) { new MsgDialog(frame, res.getString("msg.error"), ioe.toString()); }    }
  };
}
