/*  mainTable.java - The JTable for the main-window
 *  Copyright (C) 1999-2001 Fredrik Ehnbom
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
import javax.swing.event.*;
import javax.swing.table.*;

import java.awt.event.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.io.*;
import java.net.*;

import org.gjt.fredde.yamm.*;
import org.gjt.fredde.yamm.gui.MailTableRenderer;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.util.gui.ExceptionDialog;

/**
 * The Table for listing the mails subject, date and sender.
 *
 * @author Fredrik Ehnbom
 * @version $Id: mainTable.java,v 1.38 2003/03/05 15:07:02 fredde Exp $
 */
public class mainTable
	extends JTable
	implements DragGestureListener,
				DragSourceListener
{
	/** If it should sort 1 to 10 or 10 to 1*/
	private static boolean	firstSort = true;

	/** Which column that was sorted */
	private static int		sortedCol = 0;

	private static YAMM			frame = null;
	protected JPopupMenu		popup = null;
	private static DragSource	drag = null;

	private final AbstractTableModel dataModel = new AbstractTableModel() {
		private final String headername[] = {
	        	"#",
				YAMM.getString("table.subject"),
		        YAMM.getString("table.from"),
	        	YAMM.getString("table.date")
		};

		public final int getColumnCount() {
	        	return 4;
		}

		public final int getRowCount() {
			return  frame.listOfMails.length;
		}

		public final Object getValueAt(int row, int col) {
			if (row >= frame.listOfMails.length) {
				return null;
			} else {
				return  frame.listOfMails[frame.keyIndex[row]][col];
			}
		}

		public final boolean isCellEditable(int col) {
			return false;
		}

		public final String getColumnName(int column) {
			return headername[column];
		}
	};

	/**
	 * Creates a new mainTable
	 */
	public mainTable(YAMM frame) {
		super();
		this.frame = frame;
		setModel(dataModel);

		firstSort = YAMM.getProperty("sorting.type", "true").equals("true");
		sortedCol = Integer.parseInt(YAMM.getProperty("sorting.col", "0"));
		drag = DragSource.getDefaultDragSource();

		drag.createDefaultDragGestureRecognizer(
			this,
			DnDConstants.ACTION_MOVE,
			this
		);

		TableColumn column = getColumnModel().getColumn(0);
		column.setIdentifier("num");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("num.width", "20")));

		column = getColumnModel().getColumn(1);
		column.setIdentifier("subject");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("subject.width", "200")));

		column = getColumnModel().getColumn(2);
		column.setIdentifier("from");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("from.width", "200")));

		column = getColumnModel().getColumn(3);
		column.setIdentifier("date");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("date.width", "125")));


		setRowHeight(12);
		setSelectionMode(2);
		setColumnSelectionAllowed(false);
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, 0));

		MailTableRenderer rend = new MailTableRenderer(frame);
		rend.setFont(new Font("SansSerif", Font.PLAIN, 12));
		setDefaultRenderer(getColumnClass(0), rend);

		TMListener(this);

		addMouseListener(mouseListener);

		registerKeyboardAction(
			keyListener, "Del",
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), 0
		);

		popup = new JPopupMenu();
		popup.setInvoker(this);
		createPopup(popup);
		update();
	}

	public void save() {
		TableColumn c = getColumnModel().getColumn(0);
		YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
		c = getColumnModel().getColumn(1);
		YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
		c = getColumnModel().getColumn(2);
		YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");
		c = getColumnModel().getColumn(3);
		YAMM.setProperty(c.getIdentifier().toString() + ".width", c.getWidth()+"");

		YAMM.setProperty("sorting.col", "" + sortedCol);
		YAMM.setProperty("sorting.type", "" + firstSort);
	}

	public int getSelectedMessage() {
		int i = 0;
		int sel = getSelectedRow();
		if (sel == -1) return -1;

		while (i < 4) {
			if (getColumnName(i).equals("#")) {
				break;
			}
			i++;
		}

		return Integer.parseInt(getValueAt(sel, i).toString());
	}

	private String getSelected() {
		String selected = "";

		int i = 0;

		while (i < 4) {
			if (getColumnName(i).equals("#")) {
				break;
			}
			i++;
		}

		int[] mlist = getSelectedRows();
		int[] dragList = new int[mlist.length];

		for (int j = 0; j < mlist.length; j++) {
			dragList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
		}

		Arrays.sort(dragList);
		for (int j = 0; j < dragList.length; j++) {
			selected += dragList[j] + " ";
		}

		return selected;
	}

	public void dragGestureRecognized(DragGestureEvent e) {
		StringSelection text = new StringSelection(getSelected());

		try {
			drag.startDrag(e,
				DragSource.DefaultMoveDrop,
				text,
				this
			);
		} catch (InvalidDnDOperationException idoe) {
			new ExceptionDialog(
				YAMM.getString("msg.error"),
				idoe,
				YAMM.exceptionNames
			);
		}
	}

	public void dragDropEnd(DragSourceDropEvent e) {}
	public void dragEnter(DragSourceDragEvent e) {}
	public void dragExit(DragSourceEvent e) {}
	public void dragOver(DragSourceDragEvent e) {}
	public void dropActionChanged(DragSourceDragEvent e) {}


	/**
	 * Sorts 1 -> 10
	 */
	private void SortFirst(int col) {
		if (frame.listOfMails == null) return;

		int temp = -1;
		if (col == 0) {
			for (int i = 0; i < frame.listOfMails.length; i++) {
				for (int j = 0; j < frame.listOfMails.length; j++) {
					int one = Integer.parseInt(frame.listOfMails[frame.keyIndex[i]][col]);
					int two = Integer.parseInt(frame.listOfMails[frame.keyIndex[j]][col]);

					if (one < two) {
						temp = frame.keyIndex[j];
						frame.keyIndex[j] = frame.keyIndex[i];
						frame.keyIndex[i] = temp;
					}
				}
			}
		} else {
			for (int i = 0; i < frame.listOfMails.length; i++) {
				for (int j = 0; j < frame.listOfMails.length; j++) {
					String s1 = frame.listOfMails[frame.keyIndex[i]][col].toLowerCase();
					String s2 = frame.listOfMails[frame.keyIndex[j]][col].toLowerCase();

					if (s1.compareTo(s2.toLowerCase()) < 0) {
						temp = frame.keyIndex[j];
						frame.keyIndex[j] = frame.keyIndex[i];
						frame.keyIndex[i] = temp;
					}
				}
			}
		}
	}

	/**
	 * Sorts 10 -> 1
	 */
	private void SortLast(int col) {
		if (frame.listOfMails == null) return;
		int temp = -1;

		if (col == 0) {
			for (int i = 0; i < frame.listOfMails.length; i++) {
				for (int j = 0; j < frame.listOfMails.length; j++) {
					int one = Integer.parseInt(frame.listOfMails[frame.keyIndex[i]][col]);
					int two = Integer.parseInt(frame.listOfMails[frame.keyIndex[j]][col]);

					if (one > two) {
						temp = frame.keyIndex[j];
						frame.keyIndex[j] = frame.keyIndex[i];
						frame.keyIndex[i] = temp;
					}
				}
			}
		}  else {
			for (int i = 0; i < frame.listOfMails.length; i++) {
				for (int j = 0; j < frame.listOfMails.length; j++) {
					String s1 = frame.listOfMails[frame.keyIndex[i]][col].toLowerCase();
					String s2 = frame.listOfMails[frame.keyIndex[j]][col].toLowerCase();

					if (s1.toLowerCase().compareTo(s2.toLowerCase()) > 0) {
						temp = frame.keyIndex[j];
						frame.keyIndex[j] = frame.keyIndex[i];
						frame.keyIndex[i] = temp;
					}
				}
			}
		}
	}

	public void createPopup(JPopupMenu jpmenu) {
		Vector list = new Vector(), list2 = new Vector();
		String sep = System.getProperty("file.separator");
		String boxHome = Utilities.replace(YAMM.home + "/boxes");

		fileList(list, new File(boxHome + sep));
		fileList(list2, new File(boxHome + sep));

		JMenuItem row = null;
		JMenuItem delete = new JMenuItem(YAMM.getString("button.delete"));
		JMenuItem reply = new JMenuItem(YAMM.getString("button.reply"));
		delete.addActionListener(OtherMListener);
		delete.setFont(new Font("SansSerif", Font.PLAIN, 10));
		reply.addActionListener(OtherMListener);
		reply.setFont(new Font("SansSerif", Font.PLAIN, 10));

		jpmenu.add(reply);
		jpmenu.addSeparator();
		createPopCommand(jpmenu, YAMM.getString("edit.copy"), list, boxHome, KMListener);
		createPopCommand(jpmenu, YAMM.getString("edit.move"), list2, boxHome, FMListener);
		jpmenu.addSeparator();
		jpmenu.add(delete);
	}

	private void createPopCommand(JMenu menu, Vector flist,	String base, ActionListener list) {
		String dname = base.substring(base.lastIndexOf(File.separator) + 1, base.length());

		if (dname.endsWith(".g")) {
			dname = dname.substring(0, dname.length() - 2);
		}
		JMenu m = new JMenu(dname);
		m.setFont(new Font("SansSerif", Font.PLAIN, 10));

		for (int j = 0; j < flist.size(); j++) {
			String fpath = flist.elementAt(j).toString();

			if (fpath.indexOf(base) != -1) {
				String fname = fpath.substring(base.length() + 1, fpath.length());

				if (fname.indexOf(File.separator) != -1) {
					menu.add(m);
					String path = flist.elementAt(j).toString();
					path = path.substring(0, path.lastIndexOf(File.separator));
					createPopCommand(m, flist, path, list);
					j--;
				} else {
					extMItem mitem = new extMItem(fname);
					mitem.setFullName(fpath);
					mitem.addActionListener(list);
					mitem.setFont(new Font("SansSerif", Font.PLAIN, 10));
					flist.remove(j);
					j--;
					m.add(mitem);
				}
			} else {
				menu.add(m);
				j--;
				break;
			}
		}
		menu.add(m);
	}

	private void createPopCommand(JPopupMenu menu, String menuName, Vector flist, String base, ActionListener list) {
		JMenu m = new JMenu(menuName);
		m.setFont(new Font("SansSerif", Font.PLAIN, 10));

		for (int j = 0; j < flist.size(); j++) {
			String fpath = flist.elementAt(j).toString();

			if (fpath.indexOf(base) != -1) {
				String fname = fpath.substring(base.length() + 1, fpath.length());

				if (fname.indexOf(File.separator) != -1) {
					menu.add(m);
					String path = flist.elementAt(j).toString();
					path = path.substring(0, path.lastIndexOf(File.separator));
					createPopCommand(m, flist, path, list);
					j--;
				} else {
					extMItem mitem = new extMItem(fname);
					mitem.setFullName(fpath);
					mitem.setFont(new Font("SansSerif", Font.PLAIN, 10)
					);
					mitem.addActionListener(list);
					flist.remove(j);
					j--;
					m.add(mitem);
				}
			} else {
				menu.add(m);
				j--;
				break;
			}
		}
		menu.add(m);
	}

	private void fileList(Vector vect, File f) {
		if ((f.toString()).equals(Utilities.replace(System.getProperty("user.home") + "/.yamm/boxes"))) {
			String list[] = f.list();
			for (int i = 0; i < list.length; i++) {
				fileList(vect, new File(f, list[i]));
			}
		} else if(f.isDirectory()) {
			String list[] = f.list();
			for (int i = 0; i < list.length; i++) {
				fileList(vect, new File(f, list[i]));
			}
		} else {
			String test = f.toString();
			test = test.substring(test.lastIndexOf(File.separator) + 1, test.length());
			if (!test.startsWith(".")) {
				vect.add(f.toString());
			}
		}
	}



	/**
	 * Adds the sorting listener to the table header
	 * @param table The JTable to add the sorting listener to
	 */
	private void TMListener(JTable table) {
		final JTable tableView = table;
		tableView.setColumnSelectionAllowed(false);

		MouseAdapter lmListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if (column != -1) {
					sortedCol = column;
					firstSort = !firstSort;
					update();
				}
			}
		};
		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(lmListener);
	}

	public void sort() {
		if (!firstSort) SortFirst(sortedCol);
		else SortLast(sortedCol);
	}

	public void update() {
		sort();
		dataModel.fireTableDataChanged();
	}

	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				popup.show(mainTable.this, me.getX(), me.getY());
			} else if (getSelectedRow() != -1) {
				get_mail();

				Thread t = new Thread() {
					public void run() {
						String outbox = Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"));
						int row = getSelectedRow();

						if (frame.listOfMails[frame.keyIndex[row]][4].equals("Unread") && !frame.selectedbox.equals(outbox)) {
							long skip = Long.parseLong(frame.listOfMails[frame.keyIndex[row]][5]);

							Mailbox.setStatus(frame, frame.selectedbox, getSelectedMessage(), skip, "Read");

							sort();
							dataModel.fireTableRowsUpdated(row, row);
//							update();
							frame.tree.updateUI();
							setEditingRow(row);
						}
					}
				};
				SwingUtilities.invokeLater(t);
				changeButtonMode(true);
			} else {
				changeButtonMode(false);
			}
		}
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				popup.show(mainTable.this, me.getX(), me.getY());
			}
		}

		void get_mail() {
			long skip = Long.parseLong(frame.listOfMails[frame.keyIndex[getSelectedRow()]][5]);

			Mailbox.getMail(frame.selectedbox, getSelectedMessage(), skip);
			try {
				String boxName = frame.selectedbox.substring(
					frame.selectedbox.indexOf("boxes") + 6,
					frame.selectedbox.length()) + "/";

				frame.mailPage = new URL(frame.mailPageString +	boxName + getSelectedMessage() + ".html");
			} catch (MalformedURLException mue) {
				new ExceptionDialog(YAMM.getString("msg.error"), mue, YAMM.exceptionNames);
			}

			try {
				frame.mail.setPage(frame.mailPage);
			} catch (IOException ioe) {
				new ExceptionDialog(YAMM.getString("msg.error"), ioe, YAMM.exceptionNames);
			}

			frame.createAttachList();
			frame.myList.updateUI();
		}
	};

	private ActionListener keyListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String text = ae.getActionCommand();
			int i = 0;

			while (i < 4) {
				if (getColumnName(i).equals("#")) {
					break;
				}
				i++;
			}

			if (text.equals("Del")) {
				if (getSelectedRow() == -1) {
					return;
				}

				Utilities.delUnNeededFiles();
				int[] mlist = getSelectedRows();
				int[] deleteList = new int[mlist.length];

				for (int j = 0; j < mlist.length; j++) {
					deleteList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
				}

				Arrays.sort(deleteList);
				Mailbox.deleteMail(frame.selectedbox, deleteList);
				Mailbox.createList(frame.selectedbox, frame);

				Mailbox.updateIndex(frame.selectedbox);
				Mailbox.updateIndex(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.trash")));
				update();
				frame.tree.updateUI();

				if (frame.listOfMails.length < 0) {
					return;
				}

				changeButtonMode(false);
				clearSelection();
			}

		}
	};

	private ActionListener OtherMListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String kommando = ((JMenuItem)ae.getSource()).getText();
			int i = 0;

			while (i < 4) {
				if (getColumnName(i).equals("#")) {
					break;
				}
				i++;
			}

			if (kommando.equals(YAMM.getString("button.delete"))) {
				if (getSelectedRow() == -1) {
					return;
				}

				Utilities.delUnNeededFiles();
				int[] mlist = getSelectedRows();
				int[] deleteList = new int[mlist.length];

				for (int j = 0; j < mlist.length; j++) {
					deleteList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
				}

				Arrays.sort(deleteList);

				Mailbox.deleteMail(frame.selectedbox, deleteList);
				Mailbox.createList(frame.selectedbox, frame);

				Mailbox.updateIndex(frame.selectedbox);
				Mailbox.updateIndex(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.trash")));
				update();
				frame.tree.updateUI();

				changeButtonMode(false);
				clearSelection();
			} else if (kommando.equals(YAMM.getString("button.reply"))) {
				if (getSelectedRow() == -1) {
					return;
				}

				int msgnum = Integer.parseInt(getValueAt(getSelectedRow(), i).toString());
				long skip = Long.parseLong(frame.listOfMails[msgnum][5]);

				String[] mail = Mailbox.getMailForReplyHeaders(frame.selectedbox, skip);


 				if (!mail[3].startsWith(YAMM.getString("mail.re")) && !mail[3].startsWith("Re:")) {
					mail[3] = YAMM.getString("mail.re") + " " + mail[3];
				}

				YAMMWrite yam = new YAMMWrite(mail[2], mail[1], mail[3], mail[0] + YAMM.getString("mail.wrote") + "\n");

				Mailbox.getMailForReply(frame.selectedbox, msgnum, skip, yam.myTextArea);
			}
		}
	};


	public void changeButtonMode(boolean b) {
		frame.tbar.reply.setEnabled(b);
		//((JButton)frame.tbar.print).setEnabled(b);
		frame.tbar.forward.setEnabled(b);
	}

	private ActionListener KMListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String name = ((extMItem)ae.getSource()).getFullName();
			int i = 0;

			while (i < 4) {
				if (getColumnName(i).equals("#")) {
					break;
				}
				i++;
			}

			if (getSelectedRow() == -1) {
				return;
			}

			int[] mlist = getSelectedRows();
			int[] copyList = new int[mlist.length];

			for (int j = 0; j < mlist.length; j++) {
				copyList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
			}
			Arrays.sort(copyList);
			Mailbox.copyMail(frame.selectedbox, name, copyList);
			Mailbox.updateIndex(name);
			update();
			frame.tree.updateUI();
		}
	};

	private ActionListener FMListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String name = ((extMItem)ae.getSource()).getFullName();

			int i = 0;

			while (i < 4) {
				if (getColumnName(i).equals("#")) {
					break;
				}
				i++;
			}

			if (getSelectedRow() == -1) {
				return;
			}

			int[] mlist = getSelectedRows();
			int[] moveList = new int[mlist.length];

			for (int j = 0; j < mlist.length; j++) {
				moveList[j] = Integer.parseInt(getValueAt(mlist[j], i).toString());
			}

			Arrays.sort(moveList);
			Mailbox.moveMail(frame.selectedbox, name, moveList);
			Mailbox.createList(frame.selectedbox, frame);
			Mailbox.updateIndex(name);
			update();
			frame.tree.updateUI();
		}
	};
}
/*
 * Changes:
 * $Log: mainTable.java,v $
 * Revision 1.38  2003/03/05 15:07:02  fredde
 * Now uses YAMM.keyIndex. Uses a thread to update Mailstatus for previously unread mail.
 *
 * Revision 1.37  2001/03/18 17:07:21  fredde
 * updated
 *
 * Revision 1.36  2000/12/31 14:10:59  fredde
 * replaced updateUI's with repaint where I could.
 *
 * Revision 1.35  2000/12/26 11:23:49  fredde
 * YAMM.listOfMails is now of type String[][]
 *
 * Revision 1.34  2000/12/25 09:54:16  fredde
 * a little cleaned up
 *
 * Revision 1.33  2000/08/09 16:36:28  fredde
 * readability fixes and fixed better sorting
 *
 * Revision 1.32  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.31  2000/04/11 13:28:43  fredde
 * fixed the nullpointer when selecting unread mails
 *
 * Revision 1.30  2000/04/01 20:50:09  fredde
 * fixed to make the profiling system work
 *
 * Revision 1.29  2000/03/25 15:46:24  fredde
 * uses the new getMailForReplyHeaders method
 *
 * Revision 1.28  2000/03/19 17:20:16  fredde
 * moved the renderer to ../MailTableRenderer.java and cleaned up a little
 *
 * Revision 1.27  2000/03/18 14:54:46  fredde
 * updates the tree and mailindex on mail- move/copy/delete
 *
 * Revision 1.26  2000/03/15 11:13:45  fredde
 * boxes now show if and how many unread messages they have
 *
 * Revision 1.25  2000/03/12 17:23:17  fredde
 * clear selection when deleting files
 *
 * Revision 1.24  2000/02/28 13:47:35  fredde
 * Added changelog and some javadoc tags. Now uses the Utilities class for removing unneeded files
 *
 */
