/*  $Id: BorderButton.java,v 1.1 2003/06/07 09:05:41 fredde Exp $
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

package org.gjt.fredde.util.gui;

import javax.swing.*;
import java.awt.event.*;
//import java.awt.*;

/**
 * A button that paints a border when the mouse is over it
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Revision: 1.1 $
 */
public class BorderButton
	extends JButton
{
	public BorderButton() {
		setBorderPainted(false);

		addMouseListener(
			new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					if (isEnabled())
						setBorderPainted(true);
				}

				public void mouseExited(MouseEvent e) {
					setBorderPainted(false);
				}
			}
		);
	}

	public BorderButton(String text) {
		this();
		setText(text);
	}

	public BorderButton(String text, ImageIcon ico) {
		this();
		setText(text);
		setIcon(ico);
	}
}

