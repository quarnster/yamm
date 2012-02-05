/*  $Id: Base64sun.java,v 1.1 2003/03/06 20:18:46 fredde Exp $
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

package org.gjt.fredde.yamm.encode;

import sun.misc.*;
import java.io.*;

public class Base64sun
	implements Encoder, Decoder
{

	public void encode(InputStream in, OutputStream out)
		throws IOException
	{
		BASE64Encoder enc = new BASE64Encoder();
		enc.encodeBuffer(in, out);
		in.close();
		out.close();
	}


	public void decode(InputStream in, OutputStream out)
		throws IOException
	{
		BASE64Decoder b64dc = new BASE64Decoder();
		b64dc.decodeBuffer(in, out);
		in.close();
		out.close();
	}
}

