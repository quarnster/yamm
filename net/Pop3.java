/*  Pop3.java - The Post Office Protocol version 3 class
 *  Copyright (C) 1999, 2000 Fredrik Ehnbom
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.gjt.fredde.util.net;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * The Post Office Protocol version 3 class.
 * Code example:
 * <code><pre>
 * import org.gjt.fredde.util.net.Pop3;
 * import java.io.IOException;
 *
 *	public class Test {
 *		public static void main(String args[]) {
 *			try {
 *				// Connect to server 'pop3.northpole.org' with
 *				// username 'santa' and password 'rudolf'.
 *				// Messages will be saved to '/inbox'
 *				Pop3 pop = new Pop3("santa", "rudolf", "pop3.northpole.org", "/inbox"); 
 *
 *				// Get the number of messages on this server
 *				int messages = pop.getMessageCount();
 *
 *
 *				for (int j = 1; j<=messages;j++) {
 *					// get the message
 *					pop.getMessage(j);
 *
 *					// delete the message
 *					pop.deleteMessage(j);
 *				}
 *
 *				// bye, bye server.
 *				pop.close();
 *			}
 *			catch (IOException ioe) { System.err.println(ioe); }
 *		}
 *	}
 * </pre></code>
 * That wasn't to hard! Now go and make your own email client!
 * @author Fredrik Ehnbom <fredde@gjt.org>
 * @version $Id: Pop3.java,v 1.4 2000/04/01 14:59:56 fredde Exp $
 */
public class Pop3 {

	/** From server */
	private BufferedReader in;

	/** To server */
	private PrintWriter out;

	/** to write the message to a file */
	private PrintWriter outFile;

	/** the socket to use for this connection */
	private Socket socket;

	/** If the "conversation" between client and server should be showed */
	protected boolean debug = false;

	/**
	 * Connects to the server.
	 * @param user The username
	 * @param password The password for this user
	 * @param server The server to connect to
	 * @param box the file to download the messages to
	 */
	public Pop3(String user, String password, String server, String box) 
			throws IOException {
		this(user, password, server, box, 110, false);
	}
  
	/**
	 * Connects to the server.
	 * @param user The username
	 * @param password The password for this user
	 * @param server The server to connect to
	 * @param box The file to download messages to
	 * @param port The port to use
	 */
	public Pop3(String user, String password, String server, 
			String box, int port) throws IOException {
		this(user, password, server, box, port, false);
	}

	/**
	 * Connects to the server.
	 * @param user The username
	 * @param password The password for this user
	 * @param server The server to connect to
	 * @param box The file to download messages to
	 * @param port The port to use
	 * @param debug Show the "conversation" between client and server
	 */
	public Pop3(String user, String password, String server, String box, 
			int port, boolean debug) throws IOException {

		this.debug = debug;

		Debug("Creating socket... (" + server + ", " + port + ")");
		socket = new Socket(server, port);

		Debug("Creating input stream...");
		in = new BufferedReader(
			new InputStreamReader(socket.getInputStream()));

		Debug("Creating output stream...");
		out = new PrintWriter(
			new BufferedWriter(
				new OutputStreamWriter(
					socket.getOutputStream())), true);

		Debug("Creating File output stream...");
		outFile = new PrintWriter(
				new BufferedOutputStream(
					new FileOutputStream(box, true)));

		Debug(in.readLine());

		sendCommand("USER " + user);
		sendCommand("PASS " + password);
	}

	/**
	 * Say goodbye to the server...
	 */
	public void close() throws IOException {
		sendCommand("QUIT");                           

		Debug("Closing input stream...");
		in.close();

		Debug("Closing output stream...");
		out.close();

		Debug("Closing File output stream...");
		outFile.close();                      

		Debug("Closing socket...");
		socket.close();           
	}

	/**
	 * Returns the numbers of messages for this user.
	 */
	public int getMessageCount() throws IOException {
		sendCommand("STAT", false);
		int messages = 0;
		int i = 1;

		String answer = in.readLine();
		Debug("Reply: " + answer);

		StringTokenizer tok = new StringTokenizer(answer);

		if ("-err".equalsIgnoreCase(tok.nextToken())) {
			throw new IOException(answer);
		} else {
			try { 
				messages = Integer.parseInt(
					tok.nextToken().trim());
			} catch (Exception e) { 
				messages = -1;
			}
		}

		return messages;
	}

	/**
	 * Sends a command to the server
	 * @param command The command to send
	 */
	public void sendCommand(String command) throws IOException {
		sendCommand(command, true);
	}

	/**
	 * Sends a command to the server.
	 * @param command The command to send
	 * @param showAnswer If it should get the answer or not
	 */
	public void sendCommand(String command, boolean showAnswer)
						throws IOException {

		Debug("Sending: " + command);
		out.println(command);

		if (showAnswer == true) {
			String answer = in.readLine();
			Debug("Reply: " + answer);

			if (answer.toLowerCase().startsWith("-err")) {
				throw new IOException(answer);
			}
		}
	}


	/**
	 * Gets the first few lines of a message. Good if it's a big message
	 * and you want to check if you want to download it or not.
	 * @param msg The message to get the intro from
	 * @param lines How many lines to get after the header
	 */
	public String getIntro(int msg, int lines) throws IOException {
		sendCommand("TOP " + msg + " " + lines, false);

		String answer = in.readLine();
		Debug("Reply: " + answer);

		String ranswer = "";
		for (;;) {
			answer = in.readLine();
			Debug("Reply: " + answer);

			if (answer.toLowerCase().startsWith("-err")) {
				throw new IOException(answer);
			} else if (answer.equals(".")) {
				break;
			} else if (answer.equals("")) {
				while (!".".equals(answer)) {
					answer = in.readLine();
					Debug("Reply: " + answer);

					ranswer += answer + "\n";
				}
				break;
			}
		}
		return ranswer;
	}

	/**
	 * Gets the specified message from the server.
	 * @param msg The message to get
	 */
	public boolean getMessage(int msg) throws IOException {
		sendCommand("RETR " + msg, false);
		String answer = in.readLine();
		Debug("Reply: " + answer);

		if (!answer.toLowerCase().startsWith("-err")) {    
			while (!".".equals(answer)) {
				answer = in.readLine();
				outFile.println(answer);
			}
			return true;
		}
		return false;
	}

	/**
	 * Deletes the specified message from the server.
	 * @param msg The message to delete
	 */
	public void deleteMessage(int msg) throws IOException {
		sendCommand("DELE " + msg);
	}


	/**
	 * Prints out debugging info.
	 * @param info The info to print out
	 */
	public void Debug(String info) {
		if (debug) {
			System.err.println(info);
		}
	}
}
/*
 * ChangeLog:
 * $Log: Pop3.java,v $
 * Revision 1.4  2000/04/01 14:59:56  fredde
 * license for the package changed to LGPL
 *
 */
