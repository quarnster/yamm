/*  UUEncode.java - Encodes files in the uuencode format
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

package org.gjt.fredde.yamm.encode;
import java.io.*;
import org.gjt.fredde.util.gui.MsgDialog;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.URLConnection;
import org.gjt.fredde.yamm.YAMM;

/**
 * Encodes file in the uuencode format
 */
public class UUEncode {
  byte[] ctmp =  new byte[45];
  byte[] outputBuffer = new byte[4];
  int    count = 0;
  int    i = 0;
  int    linecnt = 0;
  String filename;
  DataOutputStream out;

  /**
   * Encodes all files in the specified vector
   * @param attach The files to encode
   */
  public UUEncode(Vector attach) {
    try {
      out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(System.getProperty("user.home") + "/.yamm/boxes/" + YAMM.getString("box.outbox"), true)));

      out.writeBytes("--AttachThis");
      for(int i = 0; i < attach.size();i++) {
        StringTokenizer tok = new StringTokenizer(attach.elementAt(i).toString(), System.getProperty("file.separator"));

        URL url = new URL("file:///" + attach.elementAt(i).toString());
        URLConnection uc = url.openConnection();
        uc.connect(); 
        String ctype = uc.getContentType();

        if(!(tok.countTokens() <= 1))
          for(;1 < tok.countTokens();tok.nextToken()) {}
        filename = tok.nextToken();

        
        out.writeBytes("\nContent-Type: " + ctype + "; name=\"" + filename + "\"\nContent-Transfer-Encoding: x-uuencode\nContent-Disposition: attachment; filename=\"" + filename + "\"\n\n");
        encodeFile(attach.elementAt(i).toString());

        if(i+1 < attach.size()) out.writeBytes("\n--AttachThis");
        else out.writeBytes("\n--AttachThis--");
      }
      out.writeBytes("\n.\n\n");
      out.close();

    } catch(IOException ioe) { System.err.println(ioe); }
  }

  /**
   * Encodes the specified file
   * @param file The file to encode
   */
  protected void encodeFile(String file) {
    try {
      DataInputStream in = new DataInputStream(new FileInputStream(file));

      out.writeBytes("begin 644 " + filename + "\n");
      out.flush();

      for(;;) {
        ctmp = new byte[45];
        if(in.read(ctmp, 0, 45) == -1) break;

        int i;
        out.write(encodeByte((byte)ctmp.length));
        out.flush();

        for(linecnt = ctmp.length-1, i = 0;linecnt > 0;linecnt -=3,i +=3) {
          outputBuffer[0] = encodeByte((ctmp [i] & 0xFC) >> 2);
          outputBuffer[1] = encodeByte(((ctmp[i] & 0x03) << 4) | ((ctmp[i + 1] & 0xF0) >> 4));
          outputBuffer[2] = encodeByte(((ctmp[i + 1] & 0x0F) << 2) | ((ctmp[i + 2] & 0xC0) >> 6));
          outputBuffer[3] = encodeByte(ctmp[i + 2] & 0x3F);

          out.write(outputBuffer);
          out.flush();
        }

        out.writeBytes("\n");
        out.flush();
      } 

      out.writeBytes("`\nend\n");
      out.flush();

      in.close();
    }
    catch (IOException ioe) {
      System.err.println(ioe);
    }
  }

  /**
   * Encodes an int to an uuencoded byte
   * @param b The integer to convert
   */
  protected byte encodeByte(int b) {
    return (byte)(((b) == 0) ? ((byte)96) : ((b & 077) + 32));
  }
}
