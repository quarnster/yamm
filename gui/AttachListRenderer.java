/*  AttachListRenderer.java - The renderer for Attachment lists
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

import java.awt.*;
import javax.swing.*;

/**
 * This is the renderer for the Attach list.
 * It gives text files a text icon, sound files a sound icon etc. etc.
 */
public class AttachListRenderer extends JLabel implements ListCellRenderer {

	/** Tells if the last item configurated was selected or not */
	protected boolean selected;

	/** Images */
	public final static ImageIcon image = new ImageIcon("org/gjt/fredde/" +
						"yamm/images/types/image.gif");

	/** Text */
	public final static ImageIcon text = new ImageIcon("org/gjt/fredde/" +
						"yamm/images/types/text.gif");
                                 
	/** Sound */
	public final static ImageIcon sound = new ImageIcon("org/gjt/fredde/" +
						"yamm/images/types/sound.gif");

	/** Packed */
	public final static ImageIcon packed = new ImageIcon("org/gjt/fredde/" +
						"yamm/images/types/packed.gif");

	/** Unknown */
	public final static ImageIcon unknown = new ImageIcon("org/gjt/fredde" +
						"/yamm/images/types/" +
								"unknown.gif");

	/**
	 * The Renderer
	 */
	public Component getListCellRendererComponent(
				JList   list,
				Object  value,
				int     index,
				boolean selected,
				boolean cellHasFocus) {

		String s = value.toString();
		setText(s);

		String end = s;

		if (end.indexOf(".") != -1) {
			end = end.substring(end.lastIndexOf(".") + 1,
						end.length());
		}

		if (end.equalsIgnoreCase("gif")
			|| end.equalsIgnoreCase("jpg")
			|| end.equalsIgnoreCase("bmp")
			|| end.equalsIgnoreCase("xpm")
			|| end.equalsIgnoreCase("jpeg")
			|| end.equalsIgnoreCase("xcf")
			|| end.equalsIgnoreCase("png")) {

			setIcon(image);
		} else if (end.equalsIgnoreCase("htm")
			|| end.equalsIgnoreCase("html")
			|| end.equalsIgnoreCase("txt")
			|| end.equalsIgnoreCase("doc")
			|| end.equalsIgnoreCase("c")
			|| end.equalsIgnoreCase("cc")
			|| end.equalsIgnoreCase("cpp")
			|| end.equalsIgnoreCase("h")
			|| end.equalsIgnoreCase("java")) {

			setIcon(text);
		} else if (end.equalsIgnoreCase("wav")
			|| end.equalsIgnoreCase("au")
			|| end.equalsIgnoreCase("mp3")
			|| end.equalsIgnoreCase("mod")
			|| end.equalsIgnoreCase("xm")
			|| end.equalsIgnoreCase("midi")
			|| end.equalsIgnoreCase("mid")) {

			setIcon(sound);
		} else if (end.equalsIgnoreCase("zip")
			|| end.equalsIgnoreCase("jar")
			|| end.equalsIgnoreCase("rpm")
			|| end.equalsIgnoreCase("deb")
			|| end.equalsIgnoreCase("arj")
			|| end.equalsIgnoreCase("gz")
			|| end.equalsIgnoreCase("tar")
			|| end.equalsIgnoreCase("tgz")
			|| end.equalsIgnoreCase("z")) {

			setIcon(packed);
		} else {
			setIcon(unknown);
		}

		this.selected = selected;
		return this;
	}

	/**
	 * paint is subclassed to draw the background correctly.  JLabel
	 * currently does not allow backgrounds other than white, and it
	 * will also fill behind the icon.  Something that isn't desirable.
	 */
	public void paint(Graphics g) {
		Color            bColor;
		Icon             currentI = getIcon();

		if (selected) {
			bColor = new Color(204, 204, 255);
		} else if (getParent() != null) {
			bColor = getParent().getBackground();
		} else {
			bColor = getBackground();
		}
		g.setColor(bColor);

		if (currentI != null && getText() != null) {
			int offset = (currentI.getIconWidth()
					+ getIconTextGap());

			g.fillRect(offset, 0, getWidth() - 1 - offset,
							getHeight() - 1);
		} else {
			g.fillRect(0, 0, getWidth()-1, getHeight()-1);
		}
		super.paint(g);
	}
}

