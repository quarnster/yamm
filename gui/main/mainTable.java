/*  $Id: mainTable.java,v 1.50 2003/04/04 15:39:23 fredde Exp $
 *  Copyright (C) 1999-2003 Fredrik Ehnbom
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
import org.gjt.fredde.yamm.gui.*;
import org.gjt.fredde.yamm.mail.*;
import org.gjt.fredde.util.gui.ExceptionDialog;

/**
 * The Table for listing the mails subject, date and sender.
 *
 * @author Fredrik Ehnbom
 * @version $Revision: 1.50 $
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

	private static YAMM		yamm = null;
	protected JPopupMenu		popup = null;
	private static DragSource	drag = null;

	private final static int COLUMN_NUM = 0;
	private final static int COLUMN_INFO = 1;
	private final static int COLUMN_ATTACHMENT = 2;
	private final static int COLUMN_SUBJECT = 3;
	private final static int COLUMN_FROM = 4;
	private final static int COLUMN_DATE = 5;

	private final AbstractTableModel dataModel = new AbstractTableModel() {
		DateParser dp = new DateParser(YAMM.getString("shortdate"));

		public final ImageIcon inew = new ImageIcon(getClass().getResource("/images/info/new.png"));
		public final ImageIcon iread = new ImageIcon(getClass().getResource("/images/info/read.png"));
		public final ImageIcon ireplied = new ImageIcon(getClass().getResource("/images/info/replied.png"));
		public final ImageIcon iattachment = new ImageIcon(getClass().getResource("/images/info/attachment.png"));

		private final String headername[] = {
	        	"#",
			YAMM.getString("table.S"),
			YAMM.getString("table.A"),
			YAMM.getString("table.subject"),
		        YAMM.getString("table.from"),
	        	YAMM.getString("table.date")
		};

		public final int getColumnCount() {
	        	return 6;
		}

		public final int getRowCount() {
			return  yamm.listOfMails.length;
		}

		public final Object getValueAt(int row, int col) {
			int status = yamm.listOfMails[yamm.keyIndex[row]].status;

			if (row >= yamm.listOfMails.length) {
				return null;
			}
			switch (col) {
				case COLUMN_NUM: return "" + yamm.keyIndex[row];
				case COLUMN_INFO:
					if ((status & IndexEntry.STATUS_READ) != 0) {
						return iread;
					} else {
						return inew;
					}
				case COLUMN_ATTACHMENT:
					if ((status & IndexEntry.STATUS_ATTACHMENT) != 0) {
						return iattachment;
					} else {
						return null;
					}
				case COLUMN_SUBJECT: return yamm.listOfMails[yamm.keyIndex[row]].subject;
				case COLUMN_FROM: return yamm.listOfMails[yamm.keyIndex[row]].from;
				case COLUMN_DATE:
					String blah = "";
					try {
						blah = dp.parse(new Date(yamm.listOfMails[yamm.keyIndex[row]].date));
					} catch (Exception e) {
						blah = "";
					}
					return blah;
			}
			return null;
		}

		public final boolean isCellEditable(int col) {
			return false;
		}

		public final String getColumnName(int column) {
			return headername[column];
		}
		public final Class getColumnClass(int column) {
			if (column == COLUMN_INFO || column == COLUMN_ATTACHMENT) return ImageIcon.class;
			else return String.class;
		}
	};

	/**
	 * Creates a new mainTable
	 */
	public mainTable(YAMM yamm) {
		super();
		this.yamm = yamm;
		setModel(dataModel);

		firstSort = YAMM.getProperty("sorting.type", "true").equals("true");
		sortedCol = Integer.parseInt(YAMM.getProperty("sorting.col", "0"));
		drag = DragSource.getDefaultDragSource();

		drag.createDefaultDragGestureRecognizer(
			this,
			DnDConstants.ACTION_MOVE,
			this
		);

		TableColumnModel model = getColumnModel();
		TableColumn column = model.getColumn(COLUMN_NUM);
		column.setIdentifier("num");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("num.width", "20")));

		column = model.getColumn(COLUMN_INFO);
		column.setIdentifier("info");
		column.setMinWidth(16);
		column.setMaxWidth(16);
		column.setResizable(false);
		column.setPreferredWidth(16);

		column = model.getColumn(COLUMN_ATTACHMENT);
		column.setIdentifier("attachment");
		column.setMinWidth(16);
		column.setMaxWidth(16);
		column.setResizable(false);
		column.setPreferredWidth(16);

		column = model.getColumn(COLUMN_SUBJECT);
		column.setIdentifier("subject");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("subject.width", "200")));

		column = model.getColumn(COLUMN_FROM);
		column.setIdentifier("from");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("from.width", "200")));

		column = model.getColumn(COLUMN_DATE);
		column.setIdentifier("date");
		column.setMinWidth(5);
		column.setMaxWidth(1024);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("date.width", "125")));

		int index = model.getColumnIndex("num");
		model.moveColumn(index, Integer.parseInt(YAMM.getProperty("num.index", "" + COLUMN_NUM)));

		index = model.getColumnIndex("info");
		model.moveColumn(index, Integer.parseInt(YAMM.getProperty("info.index", "" + COLUMN_INFO)));

		index = model.getColumnIndex("attachment");
		model.moveColumn(index, Integer.parseInt(YAMM.getProperty("attachment.index", "" + COLUMN_ATTACHMENT)));

		index = model.getColumnIndex("subject");
		model.moveColumn(index, Integer.parseInt(YAMM.getProperty("subject.index", "" + COLUMN_SUBJECT)));

		index = model.getColumnIndex("from");
		model.moveColumn(index, Integer.parseInt(YAMM.getProperty("from.index", "" + COLUMN_FROM)));

		index = model.getColumnIndex("date");
		model.moveColumn(index, Integer.parseInt(YAMM.getProperty("date.index", "" + COLUMN_DATE)));

		setRowHeight(16);
		setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setColumnSelectionAllowed(false);
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, 0));

		MailTableRenderer rend = new MailTableRenderer(yamm);
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
		for (int i = 0; i < 6; i++) {
			TableColumn c = getColumnModel().getColumn(i);
			String id = c.getIdentifier().toString();
			YAMM.setProperty(id + ".width", c.getWidth()+"");
			YAMM.setProperty(id + ".index", "" + getColumnModel().getColumnIndex(id));
		}

		YAMM.setProperty("sorting.col", "" + sortedCol);
		YAMM.setProperty("sorting.type", "" + firstSort);
	}

	public int getSelectedMessage() {
		int sel = getSelectedRow();
		if (sel == -1) return -1;
		return yamm.keyIndex[sel];
	}

	private String getSelected() {
		String selected = "";

		int[] mlist = getSelectedRows();
		int[] dragList = new int[mlist.length];

		for (int j = 0; j < mlist.length; j++) {
			dragList[j] = yamm.keyIndex[mlist[j]];
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


	private void mergeSort(int[] tmp, int start, int end, int col) {
		if (start < end)  {
			int mid = (start + end) / 2;
			mergeSort(tmp, start, mid, col);
			mergeSort(tmp, mid+1, end, col);

			copy(tmp, start, mid, end);
			switch (col) {
				default:		mergeNum(tmp, start, mid, end); break;
				case COLUMN_INFO:	mergeInfo(tmp, start, mid, end); break;
				case COLUMN_ATTACHMENT: mergeAttachment(tmp, start, mid, end); break;
				case COLUMN_SUBJECT:	mergeSubject(tmp, start, mid, end); break;
				case COLUMN_FROM:	mergeFrom(tmp, start, mid, end); break;
				case COLUMN_DATE:	mergeDate(tmp, start, mid, end); break;
			}
		}
	}

	void copy(int[] tmp, int start, int mid, int end) {
		System.arraycopy(yamm.keyIndex, start, tmp, 0, mid-start+1);

		for (int i = end, pos = mid-start + 1; i >= mid + 1;)
			tmp[pos++] = yamm.keyIndex[i--];
	}

	void mergeNum(int[] tmp, int start, int mid, int end) {
		int num = end - start;

		for (int i = 0, j = num, k = start; i <= j; )
			if (tmp[i] <= tmp[j])
				yamm.keyIndex[k++] = tmp[i++];
			else
				yamm.keyIndex[k++] = tmp[j--];
	}

	void mergeInfo(int[] tmp, int start, int mid, int end) {
		int num = end - start;

		for (int i = 0, j = num, k = start; i <= j; ) {
			int s1 = yamm.listOfMails[tmp[i]].status & (~IndexEntry.STATUS_ATTACHMENT&0xff);
			int s2 = yamm.listOfMails[tmp[j]].status & (~IndexEntry.STATUS_ATTACHMENT&0xff);
			if (s1 <= s2)
				yamm.keyIndex[k++] = tmp[i++];
			else
				yamm.keyIndex[k++] = tmp[j--];
		}
	}

	void mergeAttachment(int[] tmp, int start, int mid, int end) {
		int num = end - start;

		for (int i = 0, j = num, k = start; i <= j; )
			if ((yamm.listOfMails[tmp[i]].status & IndexEntry.STATUS_ATTACHMENT) <= (yamm.listOfMails[tmp[j]].status & IndexEntry.STATUS_ATTACHMENT))
				yamm.keyIndex[k++] = tmp[i++];
			else
				yamm.keyIndex[k++] = tmp[j--];
	}

	void mergeFrom(int[] tmp, int start, int mid, int end) {
		int num = end - start;

		for (int i = 0, j = num, k = start; i <= j; )
			if (yamm.listOfMails[tmp[i]].from.compareTo(yamm.listOfMails[tmp[j]].from) <= 0)
				yamm.keyIndex[k++] = tmp[i++];
			else
				yamm.keyIndex[k++] = tmp[j--];
	}

	void mergeSubject(int[] tmp, int start, int mid, int end) {
		int num = end - start;

		for (int i = 0, j = num, k = start; i <= j; )
			if (yamm.listOfMails[tmp[i]].subject.compareTo(yamm.listOfMails[tmp[j]].subject) <= 0)
				yamm.keyIndex[k++] = tmp[i++];
			else
				yamm.keyIndex[k++] = tmp[j--];
	}

	void mergeDate(int[] tmp, int start, int mid, int end) {
		int num = end - start;

		for (int i = 0, j = num, k = start; i <= j; )
			if (yamm.listOfMails[tmp[i]].date <= yamm.listOfMails[tmp[j]].date)
				yamm.keyIndex[k++] = tmp[i++];
			else
				yamm.keyIndex[k++] = tmp[j--];
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
		if (yamm.listOfMails != null) {
			int[] tmp = new int[yamm.keyIndex.length];
			mergeSort(tmp, 0, tmp.length-1, sortedCol);
			if (!firstSort) {
				System.arraycopy(yamm.keyIndex, 0, tmp, 0, tmp.length);
				for (int i = 0; i < tmp.length; i++) {
					yamm.keyIndex[i] = tmp[tmp.length-1-i];
				}
			}
		}
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

				String outbox = Utilities.replace(YAMM.home + "/boxes/outbox");
				int row = getSelectedRow();
				int msg = yamm.keyIndex[row];

				if ((yamm.listOfMails[msg].status & IndexEntry.STATUS_READ) == 0 && !yamm.selectedbox.equals(outbox)) {
					yamm.listOfMails[msg].status |= IndexEntry.STATUS_READ;

					Mailbox.setStatus(yamm, yamm.selectedbox, getSelectedMessage(), yamm.listOfMails[msg], IndexEntry.STATUS_READ);

					dataModel.fireTableRowsUpdated(row, row);
					yamm.tree.unreadTable.put(yamm.selectedbox, Mailbox.getUnread(yamm.selectedbox));
					yamm.tree.update();
					yamm.status.setStatus("");
				}
				changeButtonMode(true);
				if (me.getClickCount() == 2) new MailReader(yamm.mailPage);
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
			long skip = yamm.listOfMails[yamm.keyIndex[getSelectedRow()]].skip;

			Mailbox.getMail(yamm.selectedbox, getSelectedMessage(), skip);
			try {
				String boxName = yamm.selectedbox.substring(
					yamm.selectedbox.indexOf("boxes") + 6,
					yamm.selectedbox.length()) + "/";

				yamm.mailPage = new URL(yamm.mailPageString +	boxName + getSelectedMessage() + ".html");
			} catch (MalformedURLException mue) {
				new ExceptionDialog(YAMM.getString("msg.error"), mue, YAMM.exceptionNames);
			}

			try {
				yamm.mail.setPage(yamm.mailPage);
			} catch (IOException ioe) {
				new ExceptionDialog(YAMM.getString("msg.error"), ioe, YAMM.exceptionNames);
			}

			yamm.createAttachList();
			yamm.myList.updateUI();
		}
	};

	private ActionListener keyListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String text = ae.getActionCommand();

			if (text.equals("Del")) {
				if (getSelectedRow() == -1) {
					return;
				}

				Utilities.delUnNeededFiles();
				int[] mlist = getSelectedRows();
				int[] deleteList = new int[mlist.length];

				for (int j = 0; j < mlist.length; j++) {
					deleteList[j] = yamm.keyIndex[mlist[j]];
				}

				Arrays.sort(deleteList);
				Mailbox.deleteMail(yamm.selectedbox, deleteList);
				Mailbox.createList(yamm.selectedbox, yamm);

				update();
				yamm.tree.unreadTable.put(yamm.selectedbox, Mailbox.getUnread(yamm.selectedbox));
				yamm.tree.fullUpdate();

				if (yamm.listOfMails.length < 0) {
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

			if (kommando.equals(YAMM.getString("button.delete"))) {
				if (getSelectedRow() == -1) {
					return;
				}

				Utilities.delUnNeededFiles();
				int[] mlist = getSelectedRows();
				int[] deleteList = new int[mlist.length];

				for (int j = 0; j < mlist.length; j++) {
					deleteList[j] = yamm.keyIndex[mlist[j]];;
				}

				Arrays.sort(deleteList);

				Mailbox.deleteMail(yamm.selectedbox, deleteList);
				Mailbox.createList(yamm.selectedbox, yamm);

				update();
				yamm.tree.unreadTable.put(yamm.selectedbox, Mailbox.getUnread(yamm.selectedbox));
				yamm.tree.fullUpdate();

				changeButtonMode(false);
				clearSelection();
			} else if (kommando.equals(YAMM.getString("button.reply"))) {
				if (getSelectedRow() == -1) {
					return;
				}

				int msgnum = yamm.keyIndex[getSelectedRow()];
				long skip = yamm.listOfMails[msgnum].skip;

				String[] mail = Mailbox.getMailForReplyHeaders(yamm.selectedbox, skip);


 				if (!mail[2].startsWith(YAMM.getString("mail.re")) && !mail[2].startsWith("Re:")) {
					mail[2] = YAMM.getString("mail.re") + " " + mail[2];
				}

				YAMMWrite yam = new YAMMWrite(mail[0], mail[1], mail[2], mail[0] + " " + YAMM.getString("mail.wrote") + "\n");

				Mailbox.getMailForReply(yamm.selectedbox, msgnum, skip, yam.myTextArea);
				yam.sign();
			}
		}
	};


	public void changeButtonMode(boolean b) {
		yamm.tbar.reply.setEnabled(b);
		//((JButton)yamm.tbar.print).setEnabled(b);
		yamm.tbar.forward.setEnabled(b);
	}

	private ActionListener KMListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String name = ((extMItem)ae.getSource()).getFullName();

			if (getSelectedRow() == -1) {
				return;
			}

			int[] mlist = getSelectedRows();
			int[] copyList = new int[mlist.length];

			for (int j = 0; j < mlist.length; j++) {
				copyList[j] = yamm.keyIndex[mlist[j]];
			}
			Arrays.sort(copyList);
			Mailbox.copyMail(yamm.selectedbox, name, copyList);
			yamm.tree.unreadTable.put(yamm.selectedbox, Mailbox.getUnread(yamm.selectedbox));
			yamm.tree.unreadTable.put(name, Mailbox.getUnread(name));
			yamm.tree.fullUpdate();
		}
	};

	private ActionListener FMListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String name = ((extMItem)ae.getSource()).getFullName();


			if (getSelectedRow() == -1) {
				return;
			}

			int[] mlist = getSelectedRows();
			int[] moveList = new int[mlist.length];

			for (int j = 0; j < mlist.length; j++) {
				moveList[j] = yamm.keyIndex[mlist[j]];
			}

			Arrays.sort(moveList);
			Mailbox.moveMail(yamm.selectedbox, name, moveList);
			Mailbox.createList(yamm.selectedbox, yamm);
			update();
			yamm.tree.unreadTable.put(yamm.selectedbox, Mailbox.getUnread(yamm.selectedbox));
			yamm.tree.unreadTable.put(name, Mailbox.getUnread(name));
			yamm.tree.fullUpdate();
		}
	};
}
/*
 * Changes:
 * $Log: mainTable.java,v $
 * Revision 1.50  2003/04/04 15:39:23  fredde
 * added space before mail.wrote
 *
 * Revision 1.49  2003/03/16 11:03:39  fredde
 * resizable false for imageicon columns
 *
 * Revision 1.48  2003/03/15 19:36:38  fredde
 * added attachment column. added sorting by status and sorting by attachment
 *
 * Revision 1.47  2003/03/14 23:18:05  fredde
 * save/restore column index. added status column with icons.
 *
 * Revision 1.46  2003/03/11 15:17:15  fredde
 * better updating of the tree when doing various mail operations
 *
 * Revision 1.45  2003/03/10 20:00:59  fredde
 * sorting now done by a mergesort routine
 *
 * Revision 1.44  2003/03/10 11:02:42  fredde
 * added sorting for datecolumn. check if row != -1 before fireTableRowsUpdated on the boxtree
 *
 * Revision 1.43  2003/03/10 09:46:34  fredde
 * non localized box filenames. Uses dataModel.fireTableRowsUpdated instead of DataChanged
 *
 * Revision 1.42  2003/03/09 17:51:35  fredde
 * now uses the new index system. variable frame renamed to yamm
 *
 * Revision 1.41  2003/03/08 18:11:41  fredde
 * signs messages
 *
 * Revision 1.40  2003/03/08 16:56:03  fredde
 * added support for the MailReader. Updated for the new getMailForReplyHeaders
 *
 * Revision 1.39  2003/03/08 13:55:41  fredde
 * updated for the new TreeTable stuff
 *
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
