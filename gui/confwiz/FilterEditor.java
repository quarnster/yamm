/*  FilterEditor.java - Editor for filters
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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.gjt.fredde.util.SimpleCrypt;
import org.gjt.fredde.util.gui.ExceptionDialog;
import org.gjt.fredde.yamm.*;


/**
 * Editor for filters
 *
 * @author Fredrik Ehnbom
 * @version $Id: FilterEditor.java,v 1.1 2000/12/31 14:07:38 fredde Exp $
 */
public class FilterEditor
	extends JDialog
{
	/**
	 * Wheter or not this is a new filter
	 */
	private boolean newFilter = true;

	/**
	 * Wheter or not we made a change
	 */
	public boolean changed = false;

	/**
	 * Wheter or not we have finished configuration
	 */
	private boolean done = false;

	private JDialog owner = null;

	private JTextField filterName = new JTextField();
	private JTextField ruleValue = new JTextField();
	private JTextField target = new JTextField();

	private JComboBox field = new JComboBox(
		new String[] {
	        	YAMM.getString("mail.to"),
			YAMM.getString("mail.from"),
			YAMM.getString("mail.reply_to"),
			YAMM.getString("mail.subject")
		}
	);

	private JComboBox rule = new JComboBox(
		new String[] {
			YAMM.getString("confwiz.filter.is"),
			YAMM.getString("confwiz.filter.contains")
		}
	);

	public FilterEditor(JDialog owner) {
		this(owner, null);
	}

	/**
	 * Creates a new FilterEditor.
	 * Use this when you want to configure an existing filter
	 */
	public FilterEditor(JDialog owner, String row) {
		super(owner, true);
		this.owner = owner;
		Dimension screen = getToolkit().getScreenSize();
		setBounds((screen.width - 300)/2, (screen.height - 175)/2, 300, 175);

		getContentPane().setLayout(new GridLayout(7, 1, 2, 2));

		Box hori = Box.createHorizontalBox();
		hori.add(new JLabel(YAMM.getString("confwiz.filter.name")));
		hori.add(filterName);
		getContentPane().add(hori);

		getContentPane().add(new JLabel(YAMM.getString("confwiz.filter.rules")));

		hori = Box.createHorizontalBox();
		hori.add(new JLabel(YAMM.getString("confwiz.filter.if")));
		hori.add(field);
		hori.add(new JLabel(YAMM.getString("confwiz.filter.field")));
		getContentPane().add(hori);

		hori = Box.createHorizontalBox();
		hori.add(rule);
		hori.add(ruleValue);
		getContentPane().add(hori);

		getContentPane().add(new JLabel(YAMM.getString("confwiz.filter.move")));

		hori = Box.createHorizontalBox();
		hori.add(target);
		JButton b = new JButton(YAMM.getString("button.browse"), new ImageIcon(getClass().getResource("/images/buttons/search.gif")));
		b.addActionListener(BListener);
		hori.add(b);
		getContentPane().add(hori);

		hori = Box.createHorizontalBox();
		b = new JButton(YAMM.getString("button.ok"), new ImageIcon(getClass().getResource("/images/buttons/ok.gif")));
		b.addActionListener(BListener);
		hori.add(b);
		b = new JButton(YAMM.getString("button.cancel"), new ImageIcon(getClass().getResource("/images/buttons/cancel.gif")));
		b.addActionListener(BListener);
		hori.add(b);
		getContentPane().add(hori);

		if (row != null) load(row);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
			}
		});

		show();
	}

	/**
	 * Loads properties from the specified string
	 */
	private void load(String row) {
		newFilter = false;

		// parse...
		StringTokenizer tok = new StringTokenizer(row, ";");

		String sField = tok.nextToken();
		if (sField.equals("to")) field.setSelectedIndex(0);
		else if (sField.equals("from")) field.setSelectedIndex(1);
		else if (sField.equals("reply")) field.setSelectedIndex(2);
		else if (sField.equals("subject")) field.setSelectedIndex(3);

		String sRule = tok.nextToken();
		if (sRule.equals("is"))	rule.setSelectedIndex(0);
		else rule.setSelectedIndex(1);

		ruleValue.setText(tok.nextToken());
		target.setText(tok.nextToken());
		filterName.setText(tok.nextToken());
	}

	/**
	 * Saves the config
	 */
	public String save() {
		StringBuffer sb = new StringBuffer(100);

		if (field.getSelectedIndex() == 0) sb.append("to");
		else if (field.getSelectedIndex() == 1) sb.append("from");
		else if (field.getSelectedIndex() == 2) sb.append("reply");
		else if (field.getSelectedIndex() == 3) sb.append("subject");
		sb.append(';');

		if (rule.getSelectedIndex() == 0) sb.append("is");
		else if (rule.getSelectedIndex() == 1) sb.append("contains");
		sb.append(';');

		sb.append(ruleValue.getText());
		sb.append(';');

		sb.append(target.getText());
		sb.append(';');
		sb.append(filterName.getText());

		return sb.toString();
	}

	private ActionListener BListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String which = ((JButton) e.getSource()).getText();

			if (which.equals(YAMM.getString("button.ok"))) {
				changed = true;
				dispose();
			} else if (which.equals(YAMM.getString("button.cancel"))) {
				dispose();
			} else if (which.equals(YAMM.getString("button.browse"))) {
				JFileChooser jfs = new JFileChooser();
				jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfs.setMultiSelectionEnabled(false);
				jfs.setFileFilter(new BoxFilter());
				jfs.setSelectedFile(new File(Utilities.replace(YAMM.home + "/boxes/" + YAMM.getString("box.inbox"))));

				int ret = jfs.showOpenDialog(owner);
				if (ret == JFileChooser.APPROVE_OPTION) {
					String text = jfs.getSelectedFile().toString();
					text = text.substring(Utilities.replace(YAMM.home + "/boxes/").length());
					target.setText(text);
				}
			}
		}
	};

	private final class BoxFilter
		extends FileFilter
	{
		public final boolean accept(File f) {
			String sf = f.toString();
			if (f.isDirectory()) {
				return sf.endsWith(".g");
			}

			return sf.startsWith(Utilities.replace(YAMM.home + "/boxes/"));
		}

		// The description of this filter
		public String getDescription() {
			return "YAMM mailboxes";
		}
	}
}
/*
 * ChangeLog:
 * $Log: FilterEditor.java,v $
 * Revision 1.1  2000/12/31 14:07:38  fredde
 * files for configurating filters
 *
 */
