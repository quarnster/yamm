/*  $Id: Index.java,v 1.7 2003/06/06 10:51:18 fredde Exp $
 *  Copyright (C) 2003 Fredrik Ehnbom
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

package org.gjt.fredde.yamm.mail;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

import org.gjt.fredde.yamm.YAMM;
import org.gjt.fredde.util.gui.ExceptionDialog;

public class Index {

	private final byte[] fileIdentification = new byte[] {'Y','I',0,1};

	public final static int HEADERSIZE =
		4 + 	// size of file identification
		2 +	// number of messages size
		2 +	// unread messages size
		2;	// new unread messages size

	public RandomAccessFile raf = null;
	private String boxFile;
	private String indexFile;

	public int messageNum = 0;
	public int unreadNum = 0;
	public int newNum = 0;

	public Index(String boxFile)
		throws IOException
	{
		this.boxFile = boxFile;
		indexFile = Mailbox.getIndexName(boxFile);


		if (!checkVersion())
			create();
	}

	public void open()
		throws IOException
	{
		raf = new RandomAccessFile(indexFile, "rw");
		raf.seek(fileIdentification.length);
		messageNum = read16(raf);
		unreadNum = read16(raf);
		newNum = read16(raf);
	}

	public void close()
		throws IOException
	{
		if (raf != null)
			raf.close();
		raf = null;
	}

	public void rewrite()
		throws IOException
	{
		raf.close();
		raf = new RandomAccessFile(indexFile, "rw");
		raf.seek(fileIdentification.length);
		write16(raf, messageNum);
		write16(raf, unreadNum);
		write16(raf, newNum);
	}

	public void write()
		throws IOException
	{
		raf.seek(0);
		raf.write(fileIdentification);
		write16(raf, messageNum);
		write16(raf, unreadNum);
		write16(raf, newNum);
	}

	public IndexEntry getEntry(int num)
		throws IOException
	{
		raf.seek(HEADERSIZE + num * IndexEntry.SIZE);
		return new IndexEntry(raf);
	}

	public IndexEntry[] getEntries()
		throws IOException
	{
		IndexEntry[] e = new IndexEntry[messageNum];
		raf.seek(HEADERSIZE);

		for (int i = 0; i < e.length; i++) {
			e[i] = new IndexEntry(raf);
		}
		return e;
	}
	private boolean checkVersion() {
		BufferedInputStream in = null;
		byte[] b;

		try {
			in = new BufferedInputStream(new FileInputStream(indexFile));
			b = new byte[fileIdentification.length];
			in.read(b);
			in.close();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {}
		}

		for (int i = 0; i < fileIdentification.length; i++) {
			if (fileIdentification[i] != b[i])
				return false;
		}

		return true;
	}

	public void create()
		throws IOException
	{
		new File(indexFile).delete();

		long skip = 0;
		long skipnum = 0;


		raf = new RandomAccessFile(indexFile, "rw");
		if (Mailbox.hasMail(boxFile)) {
			RandomAccessFile in = null;

			try {
				MessageHeaderParser mhp = new MessageHeaderParser();
				DateParser dp = new DateParser();
				String temp = null;

				int length = boxFile.length();
				in = new RandomAccessFile(new File(boxFile), "r");
				write();



				try {
					mhp.parse(in);
				} catch (MessageParseException mpe) {
					new ExceptionDialog(
						YAMM.getString("msg.error"),
						mpe,
						YAMM.exceptionNames
					);
				}


				Date d = null;
				String date = null;
				boolean attachment = false;

				for (;;) {
					temp = in.readLine();
					if (temp != null && temp.startsWith("Content-Disposition") && temp.toLowerCase().indexOf("attachment") != -1) {
						attachment = true;
					}
					if (temp == null || Pattern.matches("From .* [a-zA-Z]{3} [a-zA-Z]{3} {1,2}[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2} [0-9]{4}", temp)) {
						date = mhp.getHeaderField("Date");
						if (date != null) {
							try {
								d = dp.getDate(date);
							} catch (ParseException pe) {
								d = new Date(0);
							}
						} else {
							d = new Date(0);
						}

						IndexEntry e = new IndexEntry(mhp);
						e.skip = skip;
						e.date = d.getTime();
						e.id = messageNum++;
						if (attachment)
							e.status |= IndexEntry.STATUS_ATTACHMENT;
						if ((e.status & IndexEntry.STATUS_READ) == 0) {
							newNum++;
							unreadNum++;
						}
						e.write(raf);

						skip = skipnum;
						try {
							skipnum += mhp.parse(in);
						} catch (MessageParseException mpe) {
							break;
						}
						attachment = false;
					}
					if (temp != null) {
						skipnum = in.getFilePointer();
					}
				}
				in.close();
				raf.close();
			} finally {
				if (raf != null) raf.close();
				if (in != null) in.close();
			}

		}
		raf.close();
		raf = new RandomAccessFile(indexFile, "rw");
		write();
		raf.close();
	}

	public void saveEntry(IndexEntry e, int idx)
		throws IOException
	{
		raf.seek(HEADERSIZE + idx * IndexEntry.SIZE);
		e.write(raf);
	}

	protected static int read16(RandomAccessFile in)
		throws IOException
	{
		return (in.read() << 8) + (in.read());
	}

	protected static long read32(RandomAccessFile in)
		throws IOException
	{
		return (long) ((in.read() << 24) + (in.read() << 16) + (in.read() << 8) + (in.read()));
	}
	protected static void write16(RandomAccessFile out, int i)
		throws IOException
	{
		out.write((byte) ((i >> 8) & 0xff));
		out.write((byte) (i & 0xff));
	}

	protected static void write32(RandomAccessFile out, long l)
		throws IOException
	{
		out.write((byte) ((l >> 24) & 0xff));
		out.write((byte) ((l >> 16) & 0xff));
		out.write((byte) ((l >>  8) & 0xff));
		out.write((byte) ((l >>  0) & 0xff));
	}
}

