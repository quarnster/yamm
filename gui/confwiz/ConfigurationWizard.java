/*  ConfigurationWizard.java - The configuration wizard for YAMM
 *  Copyright (C) 2000 Fredrik Ehnbom
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
package org.gjt.fredde.yamm.gui.confwiz;

import java.awt.BorderLayout;
import javax.swing.*;
import org.gjt.fredde.yamm.YAMM;

/**
 * The ConfigurationWizard is used to configure the settings in YAMM
 *
 * @author Fredrik Ehnbom
 * @version $Id: ConfigurationWizard.java,v 1.5 2000/12/31 14:06:52 fredde Exp $
 */
public class ConfigurationWizard
	extends JDialog
{
	/**
	 * Creates a new ConfigurationWizard
	 */
	public ConfigurationWizard(JFrame f) {
		super(f, true);
		getContentPane().setLayout(new BorderLayout());
		setSize(600, 400);

		JTabbedPane jtpane = new JTabbedPane();
		jtpane.addTab(YAMM.getString("confwiz.welcometab"), new JLabel(YAMM.getString("confwiz.welcome.message")));
		jtpane.addTab(YAMM.getString("confwiz.generaltab"), new GeneralConfTab());
		jtpane.addTab(YAMM.getString("confwiz.identitiestab"), new IdentitiesConfTab(this));
		jtpane.addTab(YAMM.getString("confwiz.serverstab"), new ServersConfTab(this));
		jtpane.addTab(YAMM.getString("confwiz.filterstab"), new FiltersConfTab(this));
		jtpane.addTab(YAMM.getString("confwiz.debugtab"), new DebugConfTab());

		getContentPane().add("Center", jtpane);

		getContentPane().add("South", new ControlPanel(this));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		show();
	}
}

/*
 * Changes:
 * $Log: ConfigurationWizard.java,v $
 * Revision 1.5  2000/12/31 14:06:52  fredde
 * added filters tab
 *
 * Revision 1.4  2000/04/01 20:51:21  fredde
 * fixed to make the profiling system work
 *
 * Revision 1.3  2000/03/18 17:07:49  fredde
 * default closeoperation=dispose
 *
 * Revision 1.2  2000/03/18 14:55:56  fredde
 * the server editor now works
 *
 * Revision 1.1  2000/02/28 13:49:33  fredde
 * files for the configuration wizard
 *
 */
