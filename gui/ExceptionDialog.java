/*  ExceptionDialog.java - for displaying exceptions
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

package org.gjt.fredde.util.gui;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;

/**
 * This class is used for displaying exception messages.
 * It has a "more" button which will show the stacktrace.
 */
public class ExceptionDialog extends JDialog {

	/** The Throwable... */
	protected Throwable error;

	/** The name for the ok button */
	protected String okName = "Ok";

	/** The name for the more button */
	protected String moreName = "More...";

	/** The name for the less button */
	protected String lessName = "Less...";

	/** The textarea that the exception message will be shown in */
	protected JTextArea textArea;


	/**
	 * Creates a new ExceptionDialog with the default button names.
	 * @param title The title of the Dialog.
	 */
/*
	public ExceptionDialog(String title, Throwable error) {
		this (title, error, {okName, moreName, lessName});
	}
*/

	/**
	 * Creates a new Exception with the button names of your choice.
	 * @param title The title of this dialog
	 * @param error The exception or Error to display in this dialog
	 * @param names The names of the buttons. names[0] is the ok button,
	 * names[1] is the more button and names[2] is the less button.
	 */
	public ExceptionDialog(String title, Throwable error, String[] names) {
		this.error = error;
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());

		Dimension screen = getToolkit().getScreenSize();
		setBounds((screen.width - 500) / 2, (screen.height - 200) / 2,
				500, 200);

		okName   = names[0];
		moreName = names[1];
		lessName = names[2];

		textArea = new JTextArea();
		textArea.setText(error.toString());
		getContentPane().add("Center", new JScrollPane(textArea));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));

		JButton b = new JButton(okName);
		b.addActionListener(buttonListener);
		buttonPanel.add(b);

		b = new JButton(moreName);
		b.addActionListener(buttonListener);
		buttonPanel.add(b);

		getContentPane().add("South", buttonPanel);

		show();
	}

	protected ActionListener buttonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();

			if (b.getText().equals(okName)) {
				dispose();
			} else if (b.getText().equals(moreName)) {
				b.setText(lessName);
				textArea.setText(getStackTrace());
			} else if (b.getText().equals(lessName)) {
				b.setText(moreName);
				textArea.setText(getMessage());
			}
		}
	};

	protected String getStackTrace() {
		StringWriter str = new StringWriter();
		error.printStackTrace(new PrintWriter(str));
		str.write(System.getProperty("line.separator"));
		return str.toString();
	}

	protected String getMessage() {
		return error.toString();
	}

	/**
	 * Just a little test to see how it's working.
	 */
	public static void main(String args[]) {
		int[] abc = null;

		try {
			int c = abc[0];
		} catch (Exception e) {
			String[] butnam = {"Ok", "more...", "less..."};
			new ExceptionDialog("Error", e, butnam);
		}
	}
}
