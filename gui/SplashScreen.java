/*  SplashScreen.java - a splashscreen
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

package org.gjt.fredde.util.gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

/**
 * A splashscreen to display while YAMM loads
 * @author Fredrik Ehnbom
 * @version $Id: SplashScreen.java,v 1.2 2000/03/05 17:57:17 fredde Exp $
 */
public class SplashScreen extends JWindow {

	/** Creates and show the splashscreen */
	public SplashScreen(String text, URL pic) {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setFont(new Font("Monospaced",0,14));

		JLabel logo = new JLabel(new ImageIcon(pic));
		Border ram = BorderFactory.createBevelBorder(BevelBorder.RAISED);
		logo.setBorder(ram);

		getContentPane().add("Center", logo);
		getContentPane().add("South",new JLabel(text, SwingConstants.CENTER));
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2, (screen.height - getSize().height) / 2);
		show();
	}
}
/*
 * Changes:
 * $Log: SplashScreen.java,v $
 * Revision 1.2  2000/03/05 17:57:17  fredde
 * cleaned up and now uses urls instead of strings for the image
 *
 */
