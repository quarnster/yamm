/*  Smtp.java - The Simple Mail Transfer Protocol class
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

package org.gjt.fredde.util.net;

import java.net.*;
import java.io.*;

/**
 * The Simple Mail Transfer Protocol class.
 * Code example:
 * <code><pre>
 * import org.gjt.fredde.util.net.Smtp;
 * import java.io.*;
 * 
 * public class Test {
 *   public static void main(String args[]) {
 *     try {
 *       Smtp smtp = new Smtp("server.your.domain"); // use the server 'server.your.domain'
 *       smtp.from("you@your.domain");               // send from 'you@your.domain'
 *       smtp.to("your-friend@your.domain");         // send to 'your-friend@your.domain' and
 *       smtp.to("your-second-friend@your.domain");  // to 'your-second-friend@your.domain'
 *
 *       PrintWriter out = smtp.getOutputStream();   // Headers and message
 *       out.println("Subject: party!!!");           // The subject
 *       out.println();                              // blank line separates the headers from message
 *       out.println("I have a party next week!!!"); // the message
 *
 *       smtp.sendMessage();                         // sends the message
 *       smtp.close();                               // closes the connection
 *     }
 *     catch (IOException ioe) { System.err.println(ioe); }
 *   }
 * }
 * </pre></code>
 */
public class Smtp {

  /** from the server */
  protected BufferedReader in;

  /** to the server */
  protected PrintWriter    out;

  /** The socket to use for this connection */
  protected Socket socket;

  /**
   * Connects to the specified server
   * @param server The server to use
   * @param file The file to get messages from
   */
  public Smtp(String server) throws IOException {
    this(server, 25);
  }

  /**
   * Connects to the specified server
   * @param server The server to use
   * @param file The file to get messages from
   */
  public Smtp(String server, int port) throws IOException {
    socket = new Socket(server, port);

    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new PrintWriter(new BufferedWriter(
                   new OutputStreamWriter(socket.getOutputStream())), true);

    in.readLine();
    sendCommand("HELO " + InetAddress.getLocalHost().getHostName(), 250);
  }

  /**
   * closes the connection
   * @deprecated Replaced by <code>Smtp.close()</code>
   */
  public void closeConnection() throws IOException {
    sendCommand("QUIT", 221);
    in.close();
    out.close();
    socket.close();
  }

  /**
   * closes the connection
   */
  public void close() throws IOException {
    sendCommand("QUIT", 221);
    in.close();
    out.close();
    socket.close();
  }

  /**
   * Who is this message from? specify with this command.
   */
  public void from(String from) throws IOException {
    sendCommand("MAIL FROM: <" + from + ">", 250);
  }

  /**
   * Who should this message go to? Specify with this command.
   */
  public void to(String to) throws IOException {
    sendCommand("RCPT TO: <" + to + ">", 250);
  }

  /**
   * gets the outputstream
   */
  public PrintWriter getOutputStream() throws IOException {
    sendCommand("DATA", 354);

    return out;
  }

  /**
   * Sends current message.
   */
  public void sendMessage() throws IOException {
    sendCommand(".", 250);
  }

  /**
   * Sends a command to the server
   * @param c The command to send
   * @param reply The expected reply-code
   */
  protected void sendCommand(String c, int reply) throws IOException {
    out.println(c);
   
    String temp = in.readLine();
    if(!temp.startsWith("" + reply)) throw new IOException ("Expected " + reply + ", got " + temp);
  }
}
