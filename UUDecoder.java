/*  UUDecoder.java - Decodes uuencoded files.
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
 * This class decodes uuencoded files.
 */
public class UUDecoder {

	protected static byte[] outputBuffer = null;
	protected static int count = 0;

	/**
	 * Decodes an uuencoded stream.
	 * @param in The inputstream to read from
	 * @param out The outputstream to write to
	 */
	public UUDecoder(BufferedReader in, OutputStream out)
							throws IOException {

		for (;;) {
			String temp = in.readLine();

			if (temp == null) {
				break;
			} else if (temp.startsWith("begin")) {
				continue;
			} else if (!temp.equalsIgnoreCase("end") &&
					!temp.equals("") && !temp.equals("`")) {
				byte[] b = temp.getBytes();
				outputBuffer = new byte[100];
				count = 0;
				int length = (int) ((b[0] - ' ') & 0x3F);

				if (!Float.toString(((float) length / 3)).
							endsWith(".0")) {
					for (int i = 1, j = 0; j < length;
								j += 3,
								i += 4) {

						if (j + 3 > length) {
							for (int x = i; x <
									length;
									j++,
									x++) {
								makeSBuf(b[x]);
							}
							break;
						} else {
							makeBuf(b, i);
						}
					}
				} else {
					for (int i = 1; i < temp.length();
									i +=4) {
						makeBuf(b, i);
					}
				}
				out.write(outputBuffer, 0, count);
				out.flush();
			} else {
				break;
			}
		}
	}

	protected void makeSBuf(byte b) {
		outputBuffer[count++] = (byte) ((b - ' ') & 0x3F);
	}

	protected void makeBuf(byte[] b, int i) {
		outputBuffer[count++] = (byte) (((b[i] - ' ') & 0x3F) << 2 |
					((b[i + 1] - ' ') & 0x3F) >> 4);
		outputBuffer[count++] = (byte) (((b[i + 1] - ' ') & 0x3F) << 4 |
					((b[i + 2] - ' ') & 0x3F) >> 2);
		outputBuffer[count++] = (byte) (((b[i + 2] - ' ') & 0x3F) << 6 |
					((b[i + 3] - ' ') & 0x3F));
	}
}
