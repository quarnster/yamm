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
import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.util.Vector;

/**
 * The Table for listing the mails subject, date and sender.
 */
public class mainTable extends JTable {

  /** If it should sort 1 to 10 or 10 to 1*/
  static protected boolean                firstSort    = true;

  /** Which column that was sorted */
  static protected int                    sortedCol    = 0;

  /** The list of mails */
  static protected Vector		  listOfMails = null;

  /**
   * Creates a new JTable
   * @param tm The TableModel to use
   * @param listOfMails the Maillist vector
   */
  public mainTable(TableModel tm, Vector listOfMails) {
    this.listOfMails = listOfMails;

    setModel(tm);

    TableColumn column = null;
    column = getColumnModel().getColumn(3);
    column.setPreferredWidth(115);
    column.setMaxWidth(130);
    column.setMinWidth(10);
    column = getColumnModel().getColumn(0);
    column.setPreferredWidth(20);
    column.setMinWidth(10);
    column.setMaxWidth(50);

    setFont(new Font("Serif", 0, 12));
    setRowHeight(14);
    setSelectionMode(2);
    setColumnSelectionAllowed(false);
    setShowHorizontalLines(false);
    setShowVerticalLines(false);
    setDefaultRenderer(getColumnClass(0), new myRenderer());

    TMListener(this);
  }

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
//      if(isSelected) setForeground(Color.white);
      setForeground(Color.black);
      if(isSelected) setBackground(new Color(204, 204, 255));
      else setBackground(Color.white);

      return this;
    }
  }
}
