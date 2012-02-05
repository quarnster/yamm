/*  AttachList.java - A list for attachments
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
package org.gjt.fredde.yamm.gui;

import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.gjt.fredde.yamm.*;

/**
 * A list for attachments
 *
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: AttachList.java,v 1.2 2003/03/07 14:07:50 fredde Exp $
 */
public class AttachList
	extends JList
	implements DropTargetListener,
				DragGestureListener,
				DragSourceListener

{
	public static final int DROPMODE = 0;
	public static final int DRAGMODE = 1;

	private Vector data;

	private DragSource drag = null;

	private YAMM yamm = null;

	public AttachList(ListModel model, Vector data, int mode) {
		this(null, model, data, mode);
	}

	public AttachList(YAMM yamm, ListModel model, Vector data, int mode) {
		super(model);
		this.yamm = yamm;
		this.data = data;
		setCellRenderer(new AttachListRenderer());

		if (mode == DROPMODE) {
			new DropTarget(this, DnDConstants.ACTION_COPY, this);
		} else if (mode == DRAGMODE) {
			drag = DragSource.getDefaultDragSource();

			drag.createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_COPY,
				this
			);
		}
	}

	public void dragEnter(DropTargetDragEvent e) {
		if(isFileList(e)) {
			e.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			e.rejectDrag();
		}
	}

	public void add(File[] files) {
		for (int i = 0; i < files.length; i++) {
			data.add(files[i].toString());
		}
		updateUI();
	}

	public void drop(DropTargetDropEvent e) {
		try {
			Transferable tr = e.getTransferable();

			if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
				e.acceptDrop(DnDConstants.ACTION_COPY);

				List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);

				File[] files = new File[ fileList.size() ];
				fileList.toArray( files );

				add(files);

				e.getDropTargetContext().dropComplete(true);
			} else {
				e.rejectDrop();
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			e.rejectDrop();
		}
	}

	public void dragOver(DropTargetDragEvent e) {}
	public void dragExit(DropTargetEvent e) {}

	public void dropActionChanged(DropTargetDragEvent e) {
		// Is this an acceptable drag event?
		if( isFileList(e) ) {
			e.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			e.rejectDrag();
		}
	}

	private boolean isFileList(DropTargetDragEvent e) {
		DataFlavor[] flavors = e.getCurrentDataFlavors();

		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(DataFlavor.javaFileListFlavor)) return true;
		}
		return false;
	}

	private class FileTransferable
		implements Transferable
	{
		Vector data;

		public FileTransferable(Vector data) {
			this.data = data;
		}

		public boolean isDataFlavorSupported(DataFlavor f) {
			return f.equals(DataFlavor.javaFileListFlavor);
		}

		public Object getTransferData(DataFlavor d) {
			if (!isDataFlavorSupported(d)) return null;

			return data;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.javaFileListFlavor };
		}
	}

	public void dragGestureRecognized(DragGestureEvent e) {
		ListModel model = getModel();
		Vector data = new Vector(model.getSize());

		for (int i = 0; i < model.getSize(); i++) {
			String filename	= ((Vector) this.data.elementAt(i)).elementAt(1).toString();

			data.add(filename);
		}

		FileTransferable f = new FileTransferable(data);
		try {
			drag.startDrag(
				e,
				DragSource.DefaultMoveDrop,
				f,
				this
			);
		} catch (InvalidDnDOperationException idoe) {
		}
	}

	public void dragDropEnd(DragSourceDropEvent e) {}
	public void dragEnter(DragSourceDragEvent e) {}
	public void dragExit(DragSourceEvent e) {}
	public void dragOver(DragSourceDragEvent e) {}
	public void dropActionChanged(DragSourceDragEvent e) {}

}
