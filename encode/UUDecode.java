/*  UUDecode.java - Decodes uuencoded files
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
import javax.swing.JFrame;
import org.gjt.fredde.util.gui.MsgDialog;
import org.gjt.fredde.yamm.gui.imageViewer;

/**
 * A class to Decode uuencoded files
 */
public class UUDecode extends Thread{
  String  temp;
  byte[]  outputBuffer;
  int     count = 0;
  String    filename;
  String  target;
  JFrame  frame;
  boolean view;

  /**
   * Initializes some stuff
   * @param frame The frame to use when displaying errors etc.
   * @param name The name of this Thread
   * @param whichFile The file to Decode
   * @param wannaView If the user wants to View the file
   */
  public UUDecode(JFrame frame1, String name, String whichFile, String target,  boolean wannaView) {
    super(name);
    filename = whichFile;
    frame = frame1;
    view = wannaView;
    this.target = target;
  }

  /**
   * Decodes the file
   */
  public void run() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
      DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(target)));

      for(;;) {
        temp = in.readLine();

        if(temp.equals("")) { in.readLine(); break; }
      }

      for(;;) {
        temp = in.readLine();

        if (temp == null) break;

        if(!temp.equalsIgnoreCase("end")) {
          byte[] chunk = temp.getBytes();
          outputBuffer = new byte[100];
          count = 0;

          for(int i = 1; i<temp.length();i+=4) {
            printout(chunk[i], chunk[i+1], chunk[i+2], chunk[i+3]);
          }
          out.write(outputBuffer, 0, count);
          out.flush();
        }
        else break;
      }
      in.close();
      out.close();
    }
    catch (IOException ioe) {
      new MsgDialog(frame, "Error!", ioe.toString());
    }

    if(view) {
      String end = target.substring(target.length() - 4, target.length());
      if(end.equalsIgnoreCase(".jpg") || end.equalsIgnoreCase(".gif")) {
        new imageViewer(target);
      }
    }
  }

  void printout(byte b1, byte b2, byte b3, byte b4) {
    byte char1 = (byte)(((b1 - ' ') & 0x3F) << 2 | ((b2 - ' ') & 0x3F) >> 4);
    byte char2 = (byte)(((b2 - ' ') & 0x3F) << 4 | ((b3 - ' ') & 0x3F) >> 2);
    byte char3 = (byte)(((b3 - ' ') & 0x3F) << 6 | ((b4 - ' ') & 0x3F));

    outputBuffer[count++] = char1;
    outputBuffer[count++] = char2;
    outputBuffer[count++] = char3;
  }
}















