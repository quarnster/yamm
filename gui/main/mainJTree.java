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
import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.gui.BoxTreeRenderer;
import org.gjt.fredde.util.gui.*;

/**
 * The tree for the main window
 */
public class mainJTree extends JTree implements DropTargetListener {

  JPopupMenu treepop;

  DefaultMutableTreeNode top;
  protected static YAMM frame;
  protected static mainToolBar tbar;
  DefaultTreeModel tm;
	protected boolean sentbox = false;

  
	/**
	 * Creates the tree and adds all the treestuff to the tree.
	 * @param frame2 The JFrame that will be used for error messages etc
	 * @param top2 The TreeNode that this tree will use.
	 * @param tbar2 The mainToolBar to disable/enable buttons on.
	 */
	public mainJTree(YAMM frame2,  DefaultMutableTreeNode top2, mainToolBar tbar2) { 
		frame = frame2;
		tbar = tbar2;
		top = top2;
		sentbox = YAMM.getProperty("sentbox", "false").equals("true");

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
						frame.selectedbox = YAMM.home + YAMM.sep + "boxes" + YAMM.sep + YAMM.getString("box.inbox");
						Mailbox.createList(frame.selectedbox, frame.listOfMails);
						frame.mailList.clearSelection();
						((JTable)frame.mailList).updateUI();
					} else if (!box.isDirectory()) {
						frame.selectedbox = node.toString();
						Mailbox.createList(frame.selectedbox, frame.listOfMails);
						frame.mailList.clearSelection();
						((JTable)frame.mailList).updateUI();
					}
				}

				if ( ((mainTable)frame.mailList).getSelectedRow() != -1 &&
						!(((JTable)frame.mailList).getSelectedRow() >= frame.listOfMails.size())) {
					((JButton)tbar.reply).setEnabled(true);
					((JButton)tbar.forward).setEnabled(true);
					//((JButton)tbar.print).setEnabled(true);
				} else {
					((JButton)tbar.reply).setEnabled(false);
					((JButton)tbar.forward).setEnabled(false);
					//((JButton)tbar.print).setEnabled(false);
				}
			}
		});

		String sep = YAMM.sep;

		top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/" +
						YAMM.getString("box.inbox"))));
		top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/" +
    						YAMM.getString("box.outbox"))));

		if (sentbox) {
			top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/" +
						YAMM.getString("box.sent"))));
		}

		createNodes(top, new File(YAMM.home + sep + "boxes" + sep));
		top.add(new DefaultMutableTreeNode(new File(YAMM.home + "/boxes/" +
						YAMM.getString("box.trash")))); 
		treepop = new JPopupMenu("Test");
		treepop.setInvoker(this);
		expandRow(0);

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
    if(tp != null) {
      box = tp.getLastPathComponent().toString();
    }
    else {
      box = null;
    }

    if(box != null && !box.endsWith(".g") &&
       !box.equals(YAMM.getString("box.boxes"))) {
      StringTokenizer tok = new StringTokenizer(mails);
      int[] list = new int[tok.countTokens()];

      for(int i = 0; tok.hasMoreTokens(); i++) {
        list[i] = Integer.parseInt(tok.nextToken());
      }

      if(list.length == 0) return;
      if(action.equals("move"))
        Mailbox.moveMail(frame.selectedbox, box, list);
      else
        Mailbox.copyMail(frame.selectedbox, box, list);

      frame.delUnNeededFiles();
      Mailbox.createList(frame.selectedbox, frame.listOfMails);
      frame.mailList.updateUI();
    }
  }


  public void createNodes(DefaultMutableTreeNode top, File f) {
    DefaultMutableTreeNode dir = null;
    DefaultMutableTreeNode box  = null;
    String sep = YAMM.sep;
    String home = YAMM.home;

    if((f.toString()).equals(home + sep + "boxes")) {
      String list[] = f.list();
      for(int i = 0; i < list.length; i++)
        createNodes(top, new File(f, list[i]));
    }
    else if(f.isDirectory()) {
      dir = new DefaultMutableTreeNode(f);

      top.add(dir);

      String list[] = f.list();
      for(int i = 0; i < list.length; i++)
        createNodes(dir, new File(f, list[i]));
    }
    else if(!(f.toString()).equals(home + sep + 
            "boxes" + sep + YAMM.getString("box.outbox")) &&
            !(f.toString()).equals(home + sep + 
            "boxes" + sep + YAMM.getString("box.trash")) &&
	    !(f.toString()).equals(home + sep + 
            "boxes" + sep + YAMM.getString("box.inbox")) &&
	    !(f.toString()).equals(home + sep + 
            "boxes" + sep + YAMM.getString("box.sent")) &&
            (f.toString()).indexOf(sep + ".", (f.toString()).indexOf("boxes")) == -1) {

      box = new DefaultMutableTreeNode(f);
      top.add(box);
    }
  }

  public void createGroupList(Vector vect, File f) {
    String sep = YAMM.sep;
    String home = YAMM.home;

    if((f.toString()).equals(home + sep + "boxes")) {
      vect.add(sep);
      String list[] = f.list();                                      
      for(int i = 0; i < list.length; i++)
        createGroupList(vect, new File(f, list[i]));
    }                                          
    else if(f.isDirectory() && f.toString().endsWith(".g")) {
      String dir = f.toString();
      dir = dir.substring((home + sep + "boxes").length(), dir.length() -2);
      vect.add(dir);                       
                   
      String list[] = f.list();
      for(int i = 0; i < list.length; i++)
        createGroupList(vect, new File(f, list[i]));
    }                                          
  }  

	ActionListener treepoplistener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			String kommando = ((JMenuItem)ae.getSource()).getText();
      
			if (kommando.equals(YAMM.getString("tree.new.box"))) {
				new newBoxDialog(frame);
			} else if (kommando.equals(YAMM.getString("tree.new.group"))) {
				new newGroupDialog(frame);
			} else {
				if (getLastSelectedPathComponent() != null) {
					File del = new File(getLastSelectedPathComponent().toString());

					if (!del.isDirectory() && del.exists()) {
						String file = getLastSelectedPathComponent().toString();
						String sep  = YAMM.sep;
 
						if (!file.endsWith(sep + YAMM.getString("box.inbox")) &&
								!file.endsWith(sep + YAMM.getString("box.outbox")) &&
								!file.endsWith(sep + YAMM.getString("box.sent")) &&
								!file.endsWith(sep + YAMM.getString("box.trash"))) {
									
							frame.selectedbox = "deleted";
							top.removeAllChildren();
							del.delete();

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

							((mainTable)frame.mailList).popup = new JPopupMenu();
							((mainTable)frame.mailList).popup.setInvoker(frame.mailList);
							((mainTable)frame.mailList).createPopup(((mainTable)frame.mailList).popup);
						}
					} else if(del.exists()) {
						if (!del.delete()) {
							Object[] args = {del.toString()};
							new MsgDialog(frame, YAMM.getString("msg.error"), 
								YAMM.getString("msg.file.delete-dir", args));
						} else {
							frame.selectedbox = "deleted";
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
						}
					}
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

  protected void removeDotG(Vector vect) {
    for(int i = 0; i < vect.size(); i++) {
      String temp = vect.elementAt(i).toString();

      while(temp.indexOf(".g") != -1) {
        temp = temp.substring(0, temp.indexOf(".g")) + temp.substring(temp.indexOf(".g") + 2, temp.length());
      }
      vect.setElementAt(temp, i);
    }
  }

  class newGroupDialog extends JDialog {
    JButton    b;
    JComboBox  group;
    JTextField jtfield;

    public newGroupDialog(JFrame frame) {
      super(frame, true);
      setBounds(0, 0, 300, 100);
//      setResizable(false);
      setTitle(YAMM.getString("title.new.group"));

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
      getContentPane().setLayout(new GridLayout(3, 2));

      getContentPane().add(new JLabel(YAMM.getString("options.group")));
      Vector vect = new Vector();
      createGroupList(vect, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
      removeDotG(vect);
      group = new JComboBox( vect );
      getContentPane().add(group);

      getContentPane().add(new JLabel(YAMM.getString("options.name")));
      jtfield = new JTextField();
      getContentPane().add(jtfield);


      b = new JButton(YAMM.getString("button.ok"));
      b.addActionListener(BListener2);
      getContentPane().add(b);

      b = new JButton(YAMM.getString("button.cancel"));
      b.addActionListener(BListener2);
      getContentPane().add(b);

      show();
    }

	ActionListener BListener2 = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String arg = ((JButton)e.getSource()).getText();
			String sep = System.getProperty("file.separator");

			if (arg.equals(YAMM.getString("button.ok"))) {
				String gName = group.getSelectedItem().toString();
				String temp = "";

				if (!gName.equals(sep)) {
					StringTokenizer tok = new StringTokenizer(gName, sep);

					while (tok.hasMoreTokens()) {
						temp +=  tok.nextToken() + ".g" + sep;
					}
					gName = temp;
				}
				File box = new File(System.getProperty("user.home") +
							"/.yamm/boxes/" +
							gName +
							jtfield.getText() + ".g");

				if (box.exists()) {
					new MsgDialog(frame, YAMM.getString("msg.error"),
						YAMM.getString("msg.file.exists"));
				} else {
					if (!box.mkdir()) {
						Object[] args = { box.toString() };
						new MsgDialog(frame, YAMM.getString("msg.error"),
							YAMM.getString("msg.file.create-error",args));
					}

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
					dispose();
				}
			} else if (arg.equals(YAMM.getString("button.cancel"))) {
				dispose();
			}
		}
	};
}

  class newBoxDialog extends JDialog {
    JButton    b;
    JComboBox  group;
    JTextField jtfield;
  
    public newBoxDialog(JFrame frame) {
      super(frame, true);  
      setBounds(0, 0, 300, 100);
//      setResizable(false);      
      setTitle(YAMM.getString("title.new.box"));
 
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
      getContentPane().setLayout(new GridLayout(3, 2));
 
      getContentPane().add(new JLabel(YAMM.getString("options.group")));
      Vector vect = new Vector();
      createGroupList(vect, new File(YAMM.home + "/boxes/"));
      removeDotG(vect);
      group = new JComboBox(vect);
      getContentPane().add(group);

      getContentPane().add(new JLabel(YAMM.getString("options.name")));
      jtfield = new JTextField();
      getContentPane().add(jtfield);
 

      b = new JButton(YAMM.getString("button.ok"));
      b.addActionListener(BListener2);
      getContentPane().add(b);
 
      b = new JButton(YAMM.getString("button.cancel"));
      b.addActionListener(BListener2);
      getContentPane().add(b);
 
      show();
    }
 
		ActionListener BListener2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String arg = ((JButton)e.getSource()).getText();
				String sep = System.getProperty("file.separator");

				if (arg.equals(YAMM.getString("button.ok"))) {
					String gName = group.getSelectedItem().toString();
					String temp = "";

					if (!gName.equals(sep)) {
						StringTokenizer tok = new StringTokenizer(gName, sep);

						while (tok.hasMoreTokens()) {
							temp +=  tok.nextToken() + ".g" + sep;
						}
						gName = temp;
					}

					File box = new File(YAMM.home +
							"/boxes/" +
							gName +
							jtfield.getText());

					if (box.exists()) {
						new MsgDialog(frame, YAMM.getString("msg.error"),
							YAMM.getString("msg.file.exists"));
					} else {
						try {
							box.createNewFile();
						} catch (IOException ioe) {
							new ExceptionDialog(YAMM.getString("msg.error"),
								ioe,
								YAMM.exceptionNames);
						}

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

						((mainTable)frame.mailList).popup = new JPopupMenu();
						((mainTable)frame.mailList).popup.setInvoker(frame.mailList);
						((mainTable)frame.mailList).createPopup(((mainTable)frame.mailList).popup);

						dispose();
					}
				} else if (arg.equals(YAMM.getString("button.cancel"))) {
					dispose();
				}
			}
		};
	}
}
