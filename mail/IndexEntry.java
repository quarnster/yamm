/*  $Id: IndexEntry.java,v 1.1 2003/03/09 17:42:24 fredde Exp $
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

public class IndexEntry {

	public int id = 0;
	public int childOf = 0;

	public String subject = null;
	public String from = null;

	public long date = 0;
	public long skip = 0;
	public byte status = 0;

	public static final int STATUS_READ = 1;
	public static final int STATUS_REPLIED = 2;

	public static final int SUBJECTLENGTH = 127;
	public static final int FROMLENGTH = 128;
	public static final int SIZE =
		2 +			// bytes used for id
		2 +			// bytes used for childOf identification
		SUBJECTLENGTH +		// bytes used for the subjectfield
		FROMLENGTH + 		// bytes used for the fromfield
		8 +			// bytes used for the date
		8 + 			// bytes used for skip
		1;			// bytes used for status

	public IndexEntry(MessageHeaderParser mhp) {
		subject = mhp.getHeaderField("Subject");
		from    = mhp.getHeaderField("From");

		if (from == null) from = "";
		if (subject == null) subject = "";

		subject = Mailbox.removeQuote(Mailbox.unMime(subject));
		from = Mailbox.removeQuote(Mailbox.unMime(from));
	}

	public IndexEntry(RandomAccessFile in)
		throws IOException
	{
		read(in);
	}


	protected void read(RandomAccessFile in)
		throws IOException
	{
		id = Index.read16(in);
		childOf = Index.read16(in);

		byte[] b = new byte[SUBJECTLENGTH];
		in.readFully(b);
		subject = new String(b).trim();

		b = new byte[FROMLENGTH];
		in.readFully(b);
		from = new String(b).trim();

		date = in.readLong();
		skip = in.readLong();
		status = (byte) (in.read());
	}

	protected void write(RandomAccessFile out)
		throws IOException
	{
		Index.write16(out, id);
		Index.write16(out, childOf);

		byte[] b = new byte[SUBJECTLENGTH];
		byte[] tmp = subject.getBytes();
		System.arraycopy(tmp, 0, b, 0, Math.min(tmp.length, SUBJECTLENGTH));
		out.write(b);

		b = new byte[FROMLENGTH];
		tmp = from.getBytes();
		System.arraycopy(tmp, 0, b, 0, Math.min(tmp.length, FROMLENGTH));
		out.write(b);

		out.writeLong(date);
		out.writeLong(skip);
		out.write(status);
	}
}

