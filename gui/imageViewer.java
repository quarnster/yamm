/*  imageViewer.java - To view .gif and .jpg files
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

package org.gjt.fredde.yamm.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * A class to view images (jpg/gif).
 */
public class imageViewer extends JFrame {
	String filename;

	class imagePanel extends JPanel {
		Image iImage;

		public imagePanel(String whichimage) {
			iImage = Toolkit.getDefaultToolkit().getImage(
								whichimage);

			MediaTracker t = new MediaTracker(this);
			t.addImage(iImage, 1);
			try {
				t.waitForID(1);
			} catch(InterruptedException ie) {
				System.err.println(ie.toString());
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(iImage.getWidth(this),
							iImage.getHeight(this));
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public void paint(Graphics g) {
			g.drawImage(iImage, 0, 0, this);
		}
	}

	/**
	 * Creates a window and adds the specified image in it.
	 * @param image The path to the image to view.
	 */
	public imageViewer(String image) {
		setTitle(image);
		getContentPane().setLayout(new BorderLayout());
		filename = image;

		imagePanel ip = new imagePanel(image);
		JScrollPane jspane = new JScrollPane(ip);

		getContentPane().add("Center", jspane);

		Dimension screen = getToolkit().getScreenSize();

		Dimension im = ip.getMaximumSize();
		int width = im.width;
		int height = im.height;

		while (width + 15 > screen.width - 100) {
			width -= 100;
		}
		while (height + 55 > screen.height - 75) {
			height -= 75;
		}

		setBounds((screen.width - width - 15)/2, 
				(screen.height - height - 55)/2, 
							width + 15, 
								height + 55);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				dispose(); 
			}
		});

		Box box = Box.createHorizontalBox();
      
		JButton b = new JButton("Save");
		b.addActionListener(BListener);
		box.add(b);

		b = new JButton("Exit");
		b.addActionListener(BListener);
		box.add(b);

		getContentPane().add("South", box);
		show();
	}

	ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String arg = ((JButton)e.getSource()).getText();

			if (arg.equals("Save")) {
				JFileChooser jfs = new JFileChooser();
				jfs.setFileSelectionMode(
						JFileChooser.FILES_ONLY);
				jfs.setMultiSelectionEnabled(false);
				jfs.setSelectedFile(new File(filename));
				int ret = jfs.showSaveDialog(null);

				if (ret == JFileChooser.APPROVE_OPTION) {
					new File(filename).renameTo(
							jfs.getSelectedFile());
				}

				dispose();
			} else if(arg.equals("Exit")) {
				new File(filename).delete();
				dispose();
			}
		}
	};
}
