/*  $Id: AttachListRenderer.java,v 1.3 2003/03/15 19:31:01 fredde Exp $
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

package org.gjt.fredde.yamm.gui;

import java.awt.*;
import javax.swing.*;

/**
 * This is the renderer for the Attach list.
 * It gives text files a text icon, sound files a sound icon etc. etc.
 * @author Fredrik Ehnbom
 * @version $Revision: 1.3 $
 */
public class AttachListRenderer
	extends JLabel
	implements ListCellRenderer
{

	/** Images */
	public final ImageIcon image = new ImageIcon(getClass().getResource("/images/types/image.png"));

	/** Text */
	public final ImageIcon text = new ImageIcon(getClass().getResource("/images/types/text.png"));
                                 
	/** Sound */
	public final ImageIcon sound = new ImageIcon(getClass().getResource("/images/types/sound.png"));

	/** Packed */
	public final ImageIcon packed = new ImageIcon(getClass().getResource("/images/types/packed.png"));

	/** Unknown */
	public final ImageIcon unknown = new ImageIcon(getClass().getResource("/images/types/unknown.png"));

	public AttachListRenderer() {
		super();
		setOpaque(true);
	}

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

		if (selected) {
			setBackground(UIManager.getColor("textHighlight"));
			setForeground(UIManager.getColor("textHighlightText"));
		} else {
			setBackground(UIManager.getColor("control"));
			setForeground(UIManager.getColor("controlText"));
		}

		return this;
	}
}
/*
 * Changes:
 * $Log: AttachListRenderer.java,v $
 * Revision 1.3  2003/03/15 19:31:01  fredde
 * .gif -> .png. get colors from UIManager
 *
 * Revision 1.2  2000/03/05 18:02:53  fredde
 * now gets the images used for the jar-file
 *
 */
