/*  $Id: mainJTree.java,v 1.46 2003/06/08 08:55:31 fredde Exp $
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
 * @version $Revision: 1.46 $
 */
public class mainJTree
	extends JTable
	implements DropTargetListener
{

	private JPopupMenu treepop;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode(YAMM.getString("box.boxes"));
	private boolean sentbox = false;
	private TreeTableCellRenderer tree;

	public Hashtable unreadTable = new Hashtable();

	private final AbstractTableModel dataModel = new AbstractTableModel() {
		private final String headername[] = {
	        	YAMM.getString("box.boxes"),
			YAMM.getString("box.unread"),
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
			int[] s = (int[]) unreadTable.get(node.toString());

			if (column == 1) {
				if (s == null) return "";
				return s[1] + "/" + s[0];
			} else return node;
		}

		public final boolean isCellEditable(int row, int col) {
			if (col == 0)
				return true;
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
	 */
	public mainJTree() {
	}

	public void init() {
		sentbox = YAMM.getProperty("sentbox", "true").equals("true");

		setModel(dataModel);

		setColumnSelectionAllowed(false);
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setIntercellSpacing(new Dimension(0, 0));

		TableColumn column = getColumnModel().getColumn(0);
		column.setIdentifier("boxes");
		column.setMinWidth(5);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("main.tree.boxes.width", "50")));

		column = getColumnModel().getColumn(1);
		column.setIdentifier("unread");
		column.setMinWidth(5);
		column.setPreferredWidth(Integer.parseInt(YAMM.getProperty("main.tree.unread.width", "30")));

		tree = new TreeTableCellRenderer(this);
		tree.setModel(new DefaultTreeModel(top));
		BoxTreeRenderer brend = new BoxTreeRenderer();
		brend.setFont(new Font("SansSerif", Font.PLAIN, 12));
		tree.setCellRenderer(brend);
		updateNodes();
		setDefaultRenderer(TreeTableCellRenderer.class, tree);
		setDefaultEditor(TreeTableCellRenderer.class, new TreeTableCellEditor());


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

		if (YAMM.getProperty("main.tree.switch", "false").equals("true"))
			getColumnModel().moveColumn(1, 0);

		new DropTarget(
			this, // component
			DnDConstants.ACTION_COPY_OR_MOVE, // actions
			this //DropTargetListener
		);
		TreeTableSelectionModel model = new TreeTableSelectionModel(tree);
		tree.setSelectionModel(model);
		setSelectionModel(model.getListSelectionModel());
		setRowHeight(16);
		tree.setRowHeight(getRowHeight());
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(TreeExpansionEvent event) {  
				dataModel.fireTableDataChanged(); 
			}
			public void treeCollapsed(TreeExpansionEvent event) {  
				dataModel.fireTableDataChanged(); 
			}
		});
	}
	public class TreeTableCellEditor
		implements CellEditor, TableCellEditor
	{
		protected EventListenerList listenerList = new EventListenerList();

		public Object getCellEditorValue() { return null; }
		public boolean shouldSelectCell(EventObject e) { return false; }
		public boolean stopCellEditing() { return true; }
		public void cancelCellEditing() {}

		public void addCellEditorListener(CellEditorListener l) {
			listenerList.add(CellEditorListener.class, l);
		}

		public void removeCellEditorListener(CellEditorListener l) {
			listenerList.remove(CellEditorListener.class, l);
		}

		/*
		* Notify all listeners that have registered interest for
		* notification on this event type.
		* @see EventListenerList
		*/
		protected void fireEditingStopped() {
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==CellEditorListener.class) {
					((CellEditorListener)listeners[i+1]).editingStopped(new ChangeEvent(this));
				}
			}
		}

		/*
		* Notify all listeners that have registered interest for
		* notification on this event type.  
		* @see EventListenerList
		*/
		protected void fireEditingCanceled() {
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==CellEditorListener.class) {
					((CellEditorListener)listeners[i+1]).editingCanceled(new ChangeEvent(this));
				}
			}
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
			return tree;
		}
		public boolean isCellEditable(EventObject e) {
			if (e instanceof MouseEvent) {
				for (int counter = getColumnCount() - 1; counter >= 0; counter--) {
					if (getColumnClass(counter) == TreeTableCellRenderer.class) {
						MouseEvent me = (MouseEvent)e;
						MouseEvent newME = new MouseEvent(tree, me.getID(),
						me.getWhen(), me.getModifiers(),
						me.getX() - getCellRect(0, counter, true).x,
						me.getY(), me.getClickCount(),
						me.isPopupTrigger());
						tree.dispatchEvent(newME);
						break;
					}
				}
			}
			return false;
		}
	}

	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableCellRenderer.class) ? -1 : editingRow;  
	}

	public void save() {
		YAMM.setProperty("main.tree.switch", "" + getColumnModel().getColumn(0).getIdentifier().equals("unread"));
		for (int i = 0; i < 2; i++) {
			TableColumn c = getColumnModel().getColumn(i);
			String id = c.getIdentifier().toString();
			YAMM.setProperty("main.tree." + id + ".width", c.getWidth()+"");
		}

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
			YAMM yamm = YAMM.getInstance();
			StringTokenizer tok = new StringTokenizer(mails);
			int[] list = new int[tok.countTokens()];

			for (int i = 0; tok.hasMoreTokens(); i++) {
				list[i] = Integer.parseInt(tok.nextToken());
			}

			if (list.length == 0) return;
			if (action.equals("move")) {
				Mailbox.moveMail(yamm.getMailbox(), box, list);
				Mailbox.createList(yamm.getMailbox(), yamm);
				yamm.mailList.update();
				yamm.mail.setText("");
			} else {
				Mailbox.copyMail(YAMM.getInstance().getMailbox(), box, list);
			}

			Utilities.delUnNeededFiles();
			dataModel.fireTableDataChanged();
			yamm.tree.unreadTable.put(yamm.getMailbox(), Mailbox.getUnread(yamm.getMailbox()));
			yamm.tree.unreadTable.put(box, Mailbox.getUnread(box));

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
		((DefaultTreeModel) tree.getModel()).reload();
		dataModel.fireTableRowsUpdated(1, dataModel.getRowCount());
	}

	public void createNodes(DefaultMutableTreeNode top, File f) {
		DefaultMutableTreeNode dir = null;
		DefaultMutableTreeNode box  = null;
		String home = YAMM.home;

		if ((f.toString()).equals(Utilities.replace(home + "/boxes"))) {
			String list[] = f.list();
			Arrays.sort(list);
			for (int i = 0; i < list.length; i++) {
				createNodes(top, new File(f, list[i]));
			}
		} else if (f.isDirectory()) {
			dir = new DefaultMutableTreeNode(f);

			top.add(dir);

			String list[] = f.list();
			Arrays.sort(list);
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
			YAMM yamm = YAMM.getInstance();

			if (kommando.equals(YAMM.getString("tree.new.box"))) {
				new NewBoxDialog(YAMM.getInstance());
			} else if (kommando.equals(YAMM.getString("tree.new.group"))) {
				new NewGroupDialog(YAMM.getInstance());
			} else 	if (getSelectedRow() != -1) {
				File del = new File(tree.getPathForRow(getSelectedRow()).getLastPathComponent().toString());

				if (!del.isDirectory() && del.exists()) {
					String file = del.toString();

					if (		!file.endsWith(Utilities.replace("/inbox")) &&
							!file.endsWith(Utilities.replace("/outbox")) &&
							!file.endsWith(Utilities.replace("/sent")) &&
							!file.endsWith(Utilities.replace("/trash"))) {

						yamm.setMailbox("deleted");
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
						yamm.setMailbox("deleted");
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
				YAMM yamm = YAMM.getInstance();
				mainToolBar tbar = yamm.tbar;

				if (!(node.toString()).equals(YAMM.getString("box.boxes"))) {
					File box = new File(node.toString());

					if (node.toString().equals("deleted") || !box.exists()) {
						String mbox = Utilities.replace(YAMM.home + "/boxes/inbox");
						yamm.setMailbox(mbox);
					} else if (!box.isDirectory()) {
						yamm.setMailbox(node.toString());
					}
				}

				if ( ((mainTable)yamm.mailList).getSelectedRow() != -1 &&
						!(((JTable)yamm.mailList).getSelectedRow() >= yamm.listOfMails.length)) {
					yamm.mailList.changeButtonMode(true);
				} else {
					yamm.mailList.changeButtonMode(false);
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
 * Revision 1.46  2003/06/08 08:55:31  fredde
 * sets a blank document when moving mail
 *
 * Revision 1.45  2003/06/07 11:30:15  fredde
 * now uses changeButtonMode
 *
 * Revision 1.44  2003/06/07 09:40:37  fredde
 * update unread/total-status when drag'n'dropping mail
 *
 * Revision 1.43  2003/06/06 17:07:01  fredde
 * now Unread/Total
 *
 * Revision 1.42  2003/06/06 10:48:40  fredde
 * unread messages now in format new/unread. now saves column widths.
 *
 * Revision 1.41  2003/04/27 08:01:25  fredde
 * black on new-mail now works correctly
 *
 * Revision 1.40  2003/04/13 16:37:43  fredde
 * now uses yamm.set/getMailbox
 *
 * Revision 1.39  2003/04/04 18:03:48  fredde
 * updated for Singleton stuff
 *
 * Revision 1.38  2003/03/15 19:49:15  fredde
 * sorts boxlist. works properly as a treetable now
 *
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
