/*  Base64Decode.java - For base64 decoding 
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

package encode;

//import sun.misc.*;
import java.io.*;
import gui.imageViewer;

public class Base64Decode extends Thread {

  File filename;
  static protected char[] base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
  static protected byte[] base64Convert = new byte[256];
  byte[] decode = new byte[4];

  public Base64Decode(String name, File whichfile) {
    super(name);
    for(int i = 0; i < 255; i++)
      base64Convert[i] = -1;

    for(int j = 0; j < base64.length; j++)
      base64Convert[base64[j]] = (byte)j;

    filename = whichfile;
  }

  public void start() {
    super.start();
  }

  public void run() {

    try{
      FileInputStream in = new FileInputStream("temp.encode");
      DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

      for(;;) {
        if(!decodeIt(in, out)) break;
        else out.flush();
      }
      in.close();
      out.close();
    }
    catch (IOException e) {
      System.err.println(e);
    }

    String end = filename.toString().substring(filename.toString().length() - 4, filename.toString().length());
    if(end.equalsIgnoreCase(".jpg") || end.equalsIgnoreCase(".gif")) {
      new imageViewer(filename.toString());
    }

  }

  protected boolean decodeIt(InputStream inputstream, OutputStream outputstream)  throws IOException { //, int length)
    int length = 4;
    byte byte0 = -1;
    byte byte1 = -1;
    byte byte2 = -1;
    byte byte3 = -1;
        

    int j;
    do {
      j = inputstream.read();
      if(j == -1) return false; // throw new IOException();
    } while(j == 10 || j == 13);

    decode[0] = (byte)j;
    j = inputstream.read(decode);

    if(j == -1) return false; //throw new IOException();

    if(length > 3 && decode[3] == 61)
      length = 3;
    if(length > 2 && decode[2] == 61)
      length = 2;

//    System.out.println("0: " + decode[0] + " 1: " + decode[1] + " 2: " + decode[2] + " 3: " + decode[3]);
    switch(length) {
      case 4: /* '\004' */
        byte3 = base64Convert[decode[3] & 0xff];
        // fall through

      case 3: /* '\003' */
        byte2 = base64Convert[decode[2] & 0xff];
            // fall through

      case 2: /* '\002' */
        byte1 = base64Convert[decode[1] & 0xff];
        byte0 = base64Convert[decode[0] & 0xff];
        break;

    }
//    System.out.println("0: " + byte0 + " 1: " + byte1 + " 2: " + byte2 + " 3: " + byte3);
    switch(length) {
      default:
        break;

      case 2: /* '\002' */
        outputstream.write((byte)(byte0 << 2 & 0xfc | byte1 >>> 4 & 0x3));
        break;

      case 3: /* '\003' */
        outputstream.write((byte)(byte0 << 2 & 0xfc | byte1 >>> 4 & 0x3));
        outputstream.write((byte)(byte1 << 4 & 0xf0 | byte2 >>> 2 & 0xf));
        break;

      case 4: /* '\004' */
        outputstream.write((byte)(byte0 << 2 & 0xfc | byte1 >>> 4 & 0x3));
        outputstream.write((byte)(byte1 << 4 & 0xf0 | byte2 >>> 2 & 0xf));
        outputstream.write((byte)(byte2 << 6 & 0xc0 | byte3 & 0x3f));
        break;
    }
    System.out.println("test: " + (char)(byte0 << 2 & 0xfc | byte1 >>> 4 & 0x3));
    System.out.println("test: " + (char)(byte1 << 4 & 0xf0 | byte2 >>> 2 & 0xf));
    System.out.println("test: " + (char)(byte2 << 6 & 0xc0 | byte3 & 0x3f));

    return true;
  }
}


