/*  $Id: mainJTree.java,v 1.37 2003/03/12 20:20:54 fredde Exp $
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

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.*;
import org.gjt.fredde.yamm.Utilities;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.gui.*;
import org.gjt.fredde.util.gui.*;

/**
 * The tree for the main window
 * @author Fredrik Ehnbom
 * @version $Revision: 1.37 $
 */
public class mainJTree
	extends JTable
	implements DropTargetListener
{

	private JPopupMenu treepop;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode(YAMM.getString("box.boxes"));
	private static YAMM yamm;
	private static mainToolBar tbar;
	private boolean sentbox = false;
	private TreeTableCellRenderer tree;

	public Hashtable unreadTable = new Hashtable();

	private final AbstractTableModel dataModel = new AbstractTableModel() {
		private final String headername[] = {
	        	YAMM.getString("box.boxes"),
			YAMM.getString("box.unread")
		};

		public final int getColumnCount() {
	        	return 2;
		}

		public final int getRowCount() {
			return tree.getRowCount();
		}

		protected TreeNode nodeForRow(int row) {
			TreePath treePath = tree.getPathForRow(row);
			return (TreeNode)treePath.getLastPathComponent();
		}

		public Object getValueAt(int row, int column) {
			TreeNode node = nodeForRow(row);

			if (column == 1) {
				int[] s = (int[]) unreadTable.get(node.toString());
				if (s == null) return "";
				if (s[2] > 0)
					return s[1] + "+" + s[2];
				return "" + s[1];
			} else return node;
		}

		public final boolean isCellEditable(int col) {
			return false;
		}

		public final String getColumnName(int column) {
			return headername[column];
		}
		public final Class getColumnClass(int column) {
			if (column == 0) return TreeTableCellRenderer.class;
			else return int.class;
		}
	};

	/**
	 * Creates the tree and adds all the treestuff to the tree.
	 * @param yamm2 The JFrame that will be used for error messages etc
	 * @param top2 The TreeNode that this tree will use.
	 * @param tbar2 The mainToolBar to disable/enable buttons on.
	 */
	public mainJTree(YAMM yamm2, mainToolBar tbar2) {
		this.yamm = yamm2;
		this.tbar = tbar2;
		sentbox = YAMM.getProperty("sentbox", "true").equals("true");

		setModel(dataModel);

		setColumnSelectionAllowed(false);
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, 0));

		TableColumn column = getColumnModel().getColumn(1);
		column.setIdentifier("num");
		column.setMinWidth(5);
		column.setMaxWidth(100);
		column.setPreferredWidth(50);

		tree = new TreeTableCellRenderer(this);
		tree.setModel(new DefaultTreeModel(top));
		BoxTreeRenderer brend = new BoxTreeRenderer();
		brend.setFont(new Font("SansSerif", Font.PLAIN, 12));
		tree.setCellRenderer(brend);
		updateNodes();
		setDefaultRenderer(TreeTableCellRenderer.class, tree);
		DefaultTableCellRenderer rend = new DefaultTableCellRenderer();
		rend.setHorizontalAlignment(JLabel.RIGHT);
		setDefaultRenderer(int.class, rend);


		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addMouseListener(mouseListener2);

		treepop = new JPopupMenu("Test");
		treepop.setInvoker(this);

		// adds items to the myPopup JPopupMenu
		JMenuItem mi = new JMenuItem(YAMM.getString("tree.new.box"));

		mi.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mi.addActionListener(treepoplistener);
		treepop.add(mi);

		mi = new JMenuItem(YAMM.getString("tree.new.group"));
		mi.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mi.addActionListener(treepoplistener);
		treepop.add(mi);
		treepop.addSeparator();

		mi = new JMenuItem(YAMM.getString("button.delete"));
		mi.setFont(new Font("SansSerif", Font.PLAIN, 10));
		mi.addActionListener(treepoplistener);
		treepop.add(mi);
		tree.setRowHeight(getRowHeight());

		if (YAMM.getProperty("main.tree.switch", "false").equals("true"))
			getColumnModel().moveColumn(1, 0);

		new DropTarget(
			this, // component
			DnDConstants.ACTION_COPY_OR_MOVE, // actions
			this //DropTargetListener
		);
		tree.setSelectionModel(new DefaultTreeSelectionModel() {
			{
				setSelectionModel(mainJTree.this.getSelectionModel());
			}
		});

	}

	public void save() {
		YAMM.setProperty("main.tree.switch", "" + getColumnModel().getColumn(0).getIdentifier().equals("num"));
	}

	/**
	 * Updates the box list
	 */
	public void updateNodes() {
		unreadTable.clear();
		top.removeAllChildren();
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/inbox"))));
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/outbox"))));

		if (sentbox) {
			top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/sent"))));
		}

		createNodes(top, new File(Utilities.replace(YAMM.home + "/boxes/")));
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/trash"))));

		tree.expandRow(0);
		((DefaultTreeModel) tree.getModel()).reload();
		dataModel.fireTableDataChanged();
	}

	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor stringFlavor = DataFlavor.stringFlavor;
			Transferable tr = e.getTransferable();

			if (e.isDataFlavorSupported(stringFlavor)) {
				String mails = (String)tr.getTransferData(stringFlavor);

				e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				doAction(mails, e.getLocation(), e.getDropAction());
				e.dropComplete(true);
			} else {
				e.rejectDrop();
			}
		} catch (IOException ioe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
						ioe,
						YAMM.exceptionNames);
		} catch (UnsupportedFlavorException ufe) {
			new ExceptionDialog(YAMM.getString("msg.error"),
						ufe,
						YAMM.exceptionNames);
		}
	}

	public void dragEnter(DropTargetDragEvent e) { }
	public void dragExit(DropTargetEvent e) { }
	public void dragOver(DropTargetDragEvent e) { }
	public void dropActionChanged(DropTargetDragEvent e) { }

	protected void doAction(String mails, Point p, int act) {
		String action = (act == DnDConstants.ACTION_MOVE ? "move" : "copy");

		TreePath tp = tree.getPathForRow(p.y / getRowHeight());
		String box = null;

		if (tp != null) {
			box = tp.getLastPathComponent().toString();
		}

		if (box != null && !box.endsWith(".g") && !box.equals(YAMM.getString("box.boxes"))) {
			StringTokenizer tok = new StringTokenizer(mails);
			int[] list = new int[tok.countTokens()];

			for (int i = 0; tok.hasMoreTokens(); i++) {
				list[i] = Integer.parseInt(tok.nextToken());
			}

			if (list.length == 0) return;
			if (action.equals("move")) {
				Mailbox.moveMail(yamm.selectedbox, box, list);
				Mailbox.createList(yamm.selectedbox, yamm);
				yamm.mailList.update();
			} else {
				Mailbox.copyMail(yamm.selectedbox, box, list);
			}

			Utilities.delUnNeededFiles();
			dataModel.fireTableDataChanged();
		}
	}

	public void update() {
		int row = getSelectedRow();
		if (row != -1)
			dataModel.fireTableRowsUpdated(row, row);
		else
			fullUpdate();
	}
	public void fullUpdate() {
		dataModel.fireTableRowsUpdated(1, dataModel.getRowCount());
	}

	public void createNodes(DefaultMutableTreeNode top, File f) {
		DefaultMutableTreeNode dir = null;
		DefaultMutableTreeNode box  = null;
		String home = YAMM.home;

		if ((f.toString()).equals(Utilities.replace(home + "/boxes"))) {
			String list[] = f.list();
			for (int i = 0; i < list.length; i++) {
				createNodes(top, new File(f, list[i]));
			}
		} else if (f.isDirectory()) {
			dir = new DefaultMutableTreeNode(f);

			top.add(dir);

			String list[] = f.list();
			for (int i = 0; i < list.length; i++) {
				createNodes(dir, new File(f, list[i]));
			}
		} else {
			if (	!(f.toString()).equals(Utilities.replace(home + "/boxes/outbox")) &&
				!(f.toString()).equals(Utilities.replace(home +	"/boxes/trash")) &&
				!(f.toString()).equals(Utilities.replace(home + "/boxes/inbox")) &&
				!(f.toString()).equals(Utilities.replace(home + "/boxes/sent")) &&
				 (f.toString()).indexOf(File.separator + ".", (f.toString()).indexOf("boxes")) == -1)
			{
				box = new DefaultMutableTreeNode(f);
				top.add(box);
			}
			if ((f.toString()).indexOf(File.separator + ".", (f.toString()).indexOf("boxes")) == -1) {
				unreadTable.put(f.toString(), Mailbox.getUnread(f.toString()));
			}
		}
	}

	private ActionListener treepoplistener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String kommando = ((JMenuItem)ae.getSource()).getText();

			if (kommando.equals(YAMM.getString("tree.new.box"))) {
				new NewBoxDialog(yamm);
			} else if (kommando.equals(YAMM.getString("tree.new.group"))) {
				new NewGroupDialog(yamm);
			} else 	if (getSelectedRow() != -1) {
				File del = new File(tree.getPathForRow(getSelectedRow()).getLastPathComponent().toString());

				if (!del.isDirectory() && del.exists()) {
					String file = del.toString();

					if (		!file.endsWith(Utilities.replace("/inbox")) &&
							!file.endsWith(Utilities.replace("/outbox")) &&
							!file.endsWith(Utilities.replace("/sent")) &&
							!file.endsWith(Utilities.replace("/trash"))) {

						yamm.selectedbox = "deleted";
						del.delete();
						new File(Mailbox.getIndexName(file)).delete();
						updateNodes();

						yamm.mailList.popup = new JPopupMenu();
						yamm.mailList.popup.setInvoker(yamm.mailList);
						yamm.mailList.createPopup(((mainTable)yamm.mailList).popup);
					}
				} else if(del.exists()) {
					if (!del.delete()) {
						Object[] args = {del.toString()};
						JOptionPane.showMessageDialog(
							yamm,
							YAMM.getString("msg.file.delete-dir", args),
							YAMM.getString("msg.error"),
							JOptionPane.ERROR_MESSAGE
						);
					} else {
						yamm.selectedbox = "deleted";
						updateNodes();
					}
				}
			}
		}
	};

	private MouseListener mouseListener2 = new MouseAdapter() {
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
			else if (getSelectedRow() != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)(tree.getPathForRow(getSelectedRow()).getLastPathComponent());

				if (!(node.toString()).equals(YAMM.getString("box.boxes"))) {
					File box = new File(node.toString());

					if (node.toString().equals("deleted") || !box.exists()) {
						yamm.selectedbox = Utilities.replace(YAMM.home + "/boxes/inbox");
						Mailbox.createList(yamm.selectedbox, yamm);
						yamm.mailList.clearSelection();
						yamm.mailList.update();
					} else if (!box.isDirectory()) {
						yamm.selectedbox = node.toString();
						Mailbox.createList(yamm.selectedbox, yamm);
						yamm.mailList.clearSelection();
						yamm.mailList.update();
					}

					if (box.isDirectory()) {
						int row = getSelectedRow();
						if (tree.isCollapsed(row))
							tree.expandRow(row);
						else
							tree.collapseRow(row);
						dataModel.fireTableDataChanged();
					}
				}

				if ( ((mainTable)yamm.mailList).getSelectedRow() != -1 &&
						!(((JTable)yamm.mailList).getSelectedRow() >= yamm.listOfMails.length)) {
					tbar.reply.setEnabled(true);
					tbar.forward.setEnabled(true);
					//tbar.print.setEnabled(true);
				} else {
					tbar.reply.setEnabled(false);
					tbar.forward.setEnabled(false);
					//tbar.print.setEnabled(false);
				}

			}
		}
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
		}
	};
}
/*
 * Changes:
 * $Log: mainJTree.java,v $
 * Revision 1.37  2003/03/12 20:20:54  fredde
 * removed MsgDialog
 *
 * Revision 1.36  2003/03/11 15:17:48  fredde
 * added update methods. Also delete index when deleting a box
 *
 * Revision 1.35  2003/03/10 20:00:05  fredde
 * only update mailList when needed
 *
 * Revision 1.34  2003/03/10 12:31:01  fredde
 * show number of new messages
 *
 * Revision 1.33  2003/03/10 09:45:11  fredde
 * non localized box filenames
 *
 * Revision 1.32  2003/03/09 17:50:34  fredde
 * no need for Mailbox.updateIndex with the new index system
 *
 * Revision 1.31  2003/03/09 14:05:56  fredde
 * drag and drop fixed. variable frame renamed to yamm
 *
 * Revision 1.30  2003/03/08 21:44:07  fredde
 * saves column index. drag and drop working again
 *
 * Revision 1.29  2003/03/08 20:56:30  fredde
 * Localization fixes
 *
 * Revision 1.28  2003/03/08 15:21:12  fredde
 * unreadTable and dataModel made public. Update models instead of updateUI
 *
 * Revision 1.27  2003/03/08 13:55:41  fredde
 * updated for the new TreeTable stuff
 *
 * Revision 1.26  2000/12/26 11:22:55  fredde
 * YAMM.listOfMails is now of type String[][]
 *
 * Revision 1.25  2000/11/03 11:17:58  fredde
 * sentbox should be shown by default
 *
 * Revision 1.24  2000/08/09 16:31:56  fredde
 * calls mailList.update() instead of mailList.updateUI()
 *
 * Revision 1.23  2000/07/16 17:48:36  fredde
 * lots of Windows compatiblity fixes
 *
 * Revision 1.22  2000/03/18 17:08:15  fredde
 * update tree on drag 'n' drop
 *
 * Revision 1.21  2000/03/15 13:42:30  fredde
 * moved out newBoxDialog and newGroupDialog with methods
 *
 * Revision 1.20  2000/02/28 13:46:21  fredde
 * added some javadoc tags and changelog. Cleaned up
 *
 */
