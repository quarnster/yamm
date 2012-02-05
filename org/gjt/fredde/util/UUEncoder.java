/*  UUEncoder.java - Encodes files in the uuencode format
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

package org.gjt.fredde.util;

import java.io.*;

/**
 * A class that encodes a file in the uuencode format.
 */
public class UUEncoder {

	protected String filename = null;

	/**
	 * Creates a new UUEncoder with the specifed name.
	 * @param filename The name of the file to encode. (Exclude path!)
	 */
	public UUEncoder(String filename) {
		this.filename = filename;
	}

	/**
	 * UUEncodes what's read from the DataInputStream and writes the encoded
	 * bytes to the DataOutputStream
	 * @param in The DataInputStream to read from
	 * @param out The DataOutputStream to write to
	 */
	public void encode(DataInputStream in, DataOutputStream out)
							throws IOException {
		out.writeBytes("begin 644 " + filename + "\n");
		out.flush();

		for (;;) {
			byte[] ctmp = new byte[45];
			byte[] outputBuffer = new byte[4];

			int read = in.read(ctmp, 0, 45);

			if (read == -1) {
				break;
			} else if (read != 45) {
				byte[] ctmp2 = new byte[read];

				for (int i = 0; i < ctmp2.length; i++) {
					ctmp2[i] = ctmp[i];
				}
				ctmp = ctmp2;
			}

			out.write(encodeByte((byte) ctmp.length));


			out.flush();

			for (int linecnt = ctmp.length - 1, i = 0; linecnt > 0;
							linecnt -= 3, i += 3) {
				if (i + 1 >= ctmp.length) {
					outputBuffer[0] = encodeByte((ctmp[i] &
								0xFC) >> 2);
					outputBuffer[1] = (byte) '=';
					outputBuffer[2] = (byte) '=';
					outputBuffer[3] = (byte) '=';
				} else if (i + 2 >= ctmp.length) {
					outputBuffer[0] = encodeByte((ctmp[i] &
								0xFC) >> 2);
					outputBuffer[1] = encodeByte(((ctmp[i] &
						0x03) << 4) | ((ctmp[i + 1] &
								0xF0) >> 4));
					outputBuffer[2] = (byte) '=';
					outputBuffer[3] = (byte) '=';
				} else {
					outputBuffer[0] = encodeByte((ctmp[i] &
								0xFC) >> 2);
					outputBuffer[1] = encodeByte(((ctmp[i] &
						0x03) << 4) | ((ctmp[i + 1] &
								0xF0) >> 4));
					outputBuffer[2] = encodeByte(
							((ctmp[i + 1] & 0x0F)
							<< 2) | ((ctmp[i + 2] &
								 0xC0) >> 6));
					outputBuffer[3] = encodeByte(ctmp[i + 2]
									& 0x3F);

				}
				out.write(outputBuffer);
				out.flush();
			}

			out.writeBytes("\n");
			out.flush();
		}

		out.writeBytes("`\nend\n");
		out.flush();
	}

	/**
	 * Encodes an integer to an uuencoded byte
	 * @param b The integer to encode
	 */
	protected byte encodeByte(int b) {
		return (byte) ( ( (b) == 0) ? (byte) 96 : (b & 077) + 32);
	}
}
