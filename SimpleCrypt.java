/*  YAMM.java - main class
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

public class SimpleCrypt {

	protected char[] key;

	public SimpleCrypt(String key) {
		this.key = key.toCharArray();
	}

	public String encrypt(String enc) {
		return encrypt(enc.toCharArray());
	}

	public String encrypt(char enc[]) {
		int j = 0;
		int c = 0;

		for (int i = 0; i < enc.length; i++) {
			c = (enc[i]-32) + (key[j]-32);

			if (c >= 0 && c <= 94) {
				enc[i] = (char) (c + 32);
			} else {
				enc[i] = (char) ((c % 95) + 32);
			}

			if ((j + 1) == key.length) {
				j = 0;
			} else {
				j++;
			}
		}
		return new String(enc);
	}

	public String decrypt(String enc) {
		return decrypt(enc.toCharArray());
	}

	public String decrypt(char[] enc) {
		int j = 0;
		int c = 0;

		for (int i = 0; i < enc.length; i++) {
			c = (enc[i] - key[j]);

			if (c < 0) {
				enc[i] = (char) (c + 95 + 32);
			} else {
				enc[i] = (char) (c + 32);
			}

			if (key.length == j + 1) {
				j = 0;
			} else {
				j++;
			}
		}
		return new String(enc);
	}

	public static void main(String args[]) {

		if (args.length != 2) {
			SimpleCrypt crypt = new SimpleCrypt("test");

			String hello = crypt.encrypt("Hello World!");
			System.out.println("\"Hello World!\" encrypted"
						+ " with \"test\": " + hello);

			System.out.println("back to normal: "
						+ crypt.decrypt(hello));
		} else {
			SimpleCrypt crypt = new SimpleCrypt(args[1]);

			String hello = crypt.encrypt(args[0]);
			System.out.println("\"" + args[0] + "\" encrypted with "
					 	+ "\"" + args[1] + "\": "
						+ hello);

			System.out.println("back to normal: "
						+ crypt.decrypt(hello));
		}
	}
}
