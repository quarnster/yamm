/*  mainJTree.java - The JTree for the main-window
 *  Copyright (C) 1999, 2000 Fredrik Ehnbom
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
import javax.swing.event.*;
import org.gjt.fredde.yamm.Utilities;
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.gui.BoxTreeRenderer;
import org.gjt.fredde.util.gui.*;

/**
 * The tree for the main window
 * @author Fredrik Ehnbom
 * @version $Id: mainJTree.java,v 1.25 2000/11/03 11:17:58 fredde Exp $
 */
public class mainJTree extends JTree implements DropTargetListener {

	private JPopupMenu treepop;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode(YAMM.getString("box.boxes"));
	private static YAMM frame;
	private static mainToolBar tbar;
	private DefaultTreeModel tm;
	private boolean sentbox = false;

	/**
	 * Creates the tree and adds all the treestuff to the tree.
	 * @param frame2 The JFrame that will be used for error messages etc
	 * @param top2 The TreeNode that this tree will use.
	 * @param tbar2 The mainToolBar to disable/enable buttons on.
	 */
	public mainJTree(YAMM frame2, mainToolBar tbar2) { 
		this.frame = frame2;
		this.tbar = tbar2;
		sentbox = YAMM.getProperty("sentbox", "true").equals("true");

		new DropTarget(this, // component
				DnDConstants.ACTION_COPY_OR_MOVE, // actions
				this); //DropTargetListener

		tm = new DefaultTreeModel(top);
		setModel(tm);
		BoxTreeRenderer rend = new BoxTreeRenderer();
		rend.setFont(new Font("SansSerif", Font.PLAIN, 12));
		setCellRenderer(rend); //new myTreeRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addMouseListener(mouseListener2);

		addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)(e.getPath().getLastPathComponent());

				if (!(node.toString()).equals(YAMM.getString("box.boxes"))) {
					File box = new File(node.toString());

					if (node.toString().equals("deleted") || !box.exists()) {
						frame.selectedbox = Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.inbox"));
						Mailbox.createList(frame.selectedbox, frame.listOfMails);
						frame.mailList.clearSelection();
						frame.mailList.update();
					} else if (!box.isDirectory()) {
						frame.selectedbox = node.toString();
						Mailbox.createList(frame.selectedbox, frame.listOfMails);
						frame.mailList.clearSelection();
						frame.mailList.update();
					}
				}

				if ( ((mainTable)frame.mailList).getSelectedRow() != -1 &&
						!(((JTable)frame.mailList).getSelectedRow() >= frame.listOfMails.size())) {
					tbar.reply.setEnabled(true);
					tbar.forward.setEnabled(true);
					//tbar.print.setEnabled(true);
				} else {
					tbar.reply.setEnabled(false);
					tbar.forward.setEnabled(false);
					//tbar.print.setEnabled(false);
				}
			}
		});

		updateNodes();
//		String sep = YAMM.sep;
/*
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.inbox")))));
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.outbox")))));

		if (sentbox) {
			top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.sent")))));
		}

		createNodes(top, new File(Utilities.replace(YAMM.home + "/boxes/")));
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.trash"))))); 
		expandRow(0);
*/
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
	}

	/**
	 * Updates the box list
	 */
	public void updateNodes() {
		top.removeAllChildren();
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.inbox")))));
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.outbox")))));

		if (sentbox) {
			top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.sent")))));
		}

		createNodes(top, new File(Utilities.replace(YAMM.home + "/boxes/")));
		top.add(new DefaultMutableTreeNode(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.trash")))));

		updateUI();
		expandRow(0);
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

		TreePath tp = getPathForLocation(p.x, p.y);
		String box = null; // = tp.getLastPathComponent().toString();

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
				Mailbox.moveMail(frame.selectedbox, box, list);
			} else {
				Mailbox.copyMail(frame.selectedbox, box, list);
			}

			Utilities.delUnNeededFiles();
			Mailbox.createList(frame.selectedbox, frame.listOfMails);
			Mailbox.updateIndex(box);
			updateUI();
			frame.mailList.updateUI();
		}
	}


	public void createNodes(DefaultMutableTreeNode top, File f) {
		DefaultMutableTreeNode dir = null;
		DefaultMutableTreeNode box  = null;
//		String sep = YAMM.sep;
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
		} else if (	!(f.toString()).equals(Utilities.replace(home + "/boxes/" + YAMM.getString("box.outbox"))) &&
				!(f.toString()).equals(Utilities.replace(home +	"/boxes/" + YAMM.getString("box.trash"))) &&
				!(f.toString()).equals(Utilities.replace(home + "/boxes/" + YAMM.getString("box.inbox"))) &&
				!(f.toString()).equals(Utilities.replace(home + "/boxes/" + YAMM.getString("box.sent"))) &&
				 (f.toString()).indexOf(File.separator + ".", (f.toString()).indexOf("boxes")) == -1) {

			box = new DefaultMutableTreeNode(f);
			top.add(box);
		}
	}

	private ActionListener treepoplistener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String kommando = ((JMenuItem)ae.getSource()).getText();

			if (kommando.equals(YAMM.getString("tree.new.box"))) {
				new NewBoxDialog(frame);
			} else if (kommando.equals(YAMM.getString("tree.new.group"))) {
				new NewGroupDialog(frame);
			} else {
				if (getLastSelectedPathComponent() != null) {
					File del = new File(getLastSelectedPathComponent().toString());

					if (!del.isDirectory() && del.exists()) {
						String file = getLastSelectedPathComponent().toString();
//						String sep  = YAMM.sep;
 
						if (		!file.endsWith(Utilities.replace("/" + YAMM.getString("box.inbox"))) &&
								!file.endsWith(Utilities.replace("/" + YAMM.getString("box.outbox"))) &&
								!file.endsWith(Utilities.replace("/" + YAMM.getString("box.sent"))) &&
								!file.endsWith(Utilities.replace("/" + YAMM.getString("box.trash")))) {
									
							frame.selectedbox = "deleted";
							del.delete();
							updateNodes();
/*
							top.removeAllChildren();

							top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
								+ YAMM.getString("box.inbox"))));
							top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
								+ YAMM.getString("box.outbox"))));

							if (sentbox) {
								top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
									+ YAMM.getString("box.sent"))));
							}

							createNodes(top, new File(YAMM.home + "/boxes/"));
							top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
								+ YAMM.getString("box.trash")))); 

							updateUI();

							expandRow(0);
*/
							frame.mailList.popup = new JPopupMenu();
							frame.mailList.popup.setInvoker(frame.mailList);
							frame.mailList.createPopup(((mainTable)frame.mailList).popup);
						}
					} else if(del.exists()) {
						if (!del.delete()) {
							Object[] args = {del.toString()};
							new MsgDialog(frame, YAMM.getString("msg.error"), 
								YAMM.getString("msg.file.delete-dir", args));
						} else {
							frame.selectedbox = "deleted";
							updateNodes();
/*
							top.removeAllChildren();

							top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
									+ YAMM.getString("box.inbox"))));
							top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
								+ YAMM.getString("box.outbox"))));
						
							if (sentbox) {
								top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
									+ YAMM.getString("box.sent"))));
							}

							createNodes(top, new File(YAMM.home + "/boxes/"));
							top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/"
								+ YAMM.getString("box.trash"))));

							updateUI();

							expandRow(0);
*/
						}
					}
				}
			}
		}
	};

	private MouseListener mouseListener2 = new MouseAdapter() {
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
		}
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
		}
	};
}
/*
 * Changes:
 * $Log: mainJTree.java,v $
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
