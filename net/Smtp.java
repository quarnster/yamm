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
 * 	public static void main(String args[]) {
 * 		try {
 *			// use the server 'server.your.domain'
 * 			Smtp smtp = new Smtp("server.your.domain");
 *
 *			// send from 'you@your.domain'
 * 			smtp.from("you@your.domain");
 *
 * 			// send to 'your-friend@your.domain'
 * 			smtp.to("your-friend@your.domain");
 *
 * 			// also send to 'your-second-friend@your.domain'
 *			smtp.to("your-second-friend@your.domain");
 *
 *			// OutputStream to server
 * 			PrintWriter out = smtp.getOutputStream();
 *
 *			// The subject of this message
 * 			out.println("Subject: party!!!");
 *
 *			// blank line separates the header from the message
 * 			out.println();
 *
 *			// The message
 *			out.println("I have a party next week!!!");
 *
 *			// sends the message
 *			smtp.sendMessage();
 *
 *			// closes the connection
 *			smtp.close();
 *		} catch (IOException ioe) { 
 *			System.err.println(ioe);
 *		}
 * 	}
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

	/** If the "conversation" between client and server should be showed */
	protected boolean debug = false;

	/**
	 * Connects to the specified server
	 * @param server The server to use
	 * @param file The file to get messages from
	 */
	public Smtp(String server) throws IOException {
		this(server, 25, false);
	}

	/**
	 * Connects to the specified server
	 * @param server The server to use
	 * @param file The file to get messages from
	 */
	public Smtp(String server, int port) throws IOException {
		this(server, 25, false);
	}

	public Smtp(String server, int port, boolean debug) throws IOException {
		this.debug = debug;

		Debug("Creating socket... (" + server + ", " + port + ")");
		socket = new Socket(server, port);

		Debug("Creating input stream...");
		in = new BufferedReader(
			new InputStreamReader(socket.getInputStream()));

		Debug("Creating output stream...");
		out = new PrintWriter(new BufferedWriter(
			new OutputStreamWriter(
				socket.getOutputStream())), true);

		Debug(in.readLine());
		sendCommand("HELO " +
			InetAddress.getLocalHost().getHostName(), 250);
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

		Debug("Closing input stream...");
		in.close();                     

		Debug("Closing output stream...");
		out.close();

		Debug("Closing socket...");
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
	public void sendCommand(String c, int reply) throws IOException {
		Debug("Sending: " + c);
		out.println(c);
   
		String temp = in.readLine();
		Debug("Reply: " + temp);

		if (!temp.startsWith("" + reply)) {
			throw new IOException ("Expected " + reply + 
							", got " + temp);
		}
	}

	/**
	 * Prints out the debugging info
	 * @param info The debuginfo to print out.
	 */
	protected void Debug(String info) {
		if (debug) {
			System.err.println(info);
		}
	}
}
