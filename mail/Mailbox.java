/*  Mailbox.java - box handling system
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

package org.gjt.fredde.yamm.mail;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.awt.Graphics;
import java.io.*;
import java.awt.print.Book;
import java.util.*;
import javax.swing.JTextArea;

/**
 * A class that handels messages and information about messages
 */
public class Mailbox {

  /**
   * puts the source code in a JTextArea.
   * @param whichBox Which box the message to view is in
   * @param whichmail Which mail to view
   * @param jtarea Which JTextArea to append the source to
   */
  public static void viewSource(String whichBox, int whichmail, JTextArea jtarea) {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
      int i = 0;
      String temp;

      for(;;) {
        temp = in.readLine();
      
        if(temp == null) break;

        if(whichmail != i && temp.equals(".")) i++;
	else if(whichmail == i) {
          if(!temp.equals(".")) jtarea.append(temp + "\n");
	  else  break;
        }
      }
      in.close();
    }
    catch (IOException ioe) { System.err.println(ioe); }
  }

  /**
   * Creates a list with the mails in this box.
   * @param whichBox Which box to get messages from.
   * @param mailList The vector that should contain the information
   */
  public static void createList(String whichBox, Vector mailList) {
    String subject = null, from = null, date = null;

    String temp = null;
    mailList.clear(); // = new Vector();
    Vector vec1 = new Vector();
    int makeItem = 0;

    int i = 0;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));

      for(;;) {
        temp = in.readLine();
      
        if(temp == null)
          break;

        else if(temp.startsWith("From:") && from == null) {
          from = temp.substring(6, temp.length());
          makeItem++;
        }

        else if(temp.startsWith("Subject:") && subject == null) {
          subject = temp.substring(8, temp.length());
          makeItem++;
        }
        else if(temp.startsWith("Date: ") && date == null) {
          date = temp.substring(6, temp.lastIndexOf(":") + 2);
          SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss", Locale.US);
          SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
          try {
            Date nisse = dateFormat2.parse(date);

            date = dateFormat3.format(nisse);
          } catch (ParseException pe) { System.err.println(pe); date = " ";}
          makeItem++;
        }

        else if(temp.equals(".") && makeItem > 0) {
          vec1.insertElementAt(Integer.toString(i), 0);
          vec1.insertElementAt(subject, 1);
          vec1.insertElementAt(from, 2);
          vec1.insertElementAt(date, 3);

          mailList.insertElementAt(vec1, i);
          vec1 = new Vector();
          subject = null;
          from = null;
          date = null;
          makeItem = 0;
          i++;
        }
      }
      in.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
    }
  }

  /**
   * Creates a list with the mails in the filter box.
   * @param mailList The vector that should contain the information
   */
  public static void createFilterList(Vector list) {
    String subject = null, from = null, to = null, reply = null;

    String temp = null;
    list.clear();
    Vector vec1 = new Vector();
    boolean makeItem = false;

    int i = 0;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + "/.yamm/boxes/.filter")));

      for(;;) {
        temp = in.readLine();
      
        if(temp == null)
          break;

        else if(temp.startsWith("From:")) {
          if(from == null) from = temp.substring(6, temp.length());
        }

        else if(temp.startsWith("Subject:")) {
          if(subject == null) subject = temp.substring(9, temp.length());
        }
        else if(temp.startsWith("To:")) {
          if(to == null) to = temp.substring(4, temp.length());
        }
        else if(temp.startsWith("Reply-To:")) {
          if(reply == null) reply = temp.substring(10, temp.length());
        }
        else if(temp.equals(".")) { makeItem = true; }

        if(subject != null && from != null && to != null && makeItem) {
          vec1.insertElementAt(from, 0);
          vec1.insertElementAt(to, 1);
          vec1.insertElementAt(subject, 2);
          if(reply != null) vec1.insertElementAt(reply, 3);
          else vec1.insertElementAt("",3);

          list.insertElementAt(vec1, i);
          vec1 = new Vector();
          subject = null;
          from = null;
          to = null;
          reply = null;
          makeItem = false;
          i++;
        }
      }
      in.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
    }
  }

  /**
   * Exports the specified attachment.
   * @param whichBox Which box to use
   * @param whichmail Which mail in the box that the attachment is in
   * @param filename The filename of the attachment to export
   */
  public static void export_attach(String whichBox, int whichmail, String filename) {
    String  temp = null;
    String  boundary = null;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
      PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(System.getProperty("user.home") + "/.yamm/tmp/encode")));
      int i = 0;
      boolean correctFile = false, checked = false;

      for(;;) {
        temp = in.readLine();
      
        if(temp == null)
          break;

        else if(temp.equals(".")) i++;


        if (i == whichmail) {
          for(;;) {
            temp = in.readLine();
            if (temp == null) break;

            else if(temp.indexOf("boundary=\"") != -1) {
              boundary = temp.substring(temp.indexOf("boundary=\"") + 10, temp.indexOf("\"", temp.indexOf("boundary=\"") + 11));
            }

            else if(temp.startsWith("Content-Type:")) {
              for(;;) {
                temp = in.readLine();

                if(temp == null) break;

                if(temp.indexOf(filename) != -1) correctFile = true;

                if(temp.equals("") && correctFile) {
                  for(;;) {
                    temp = in.readLine();
       
                    if(temp == null) break;
        
                    else if(temp.equals(".")) {checked = true; break; }
                    else if(temp.startsWith("--" + boundary)) { checked = true; break; }
                    else outFile.println(temp);
                  }
                }
                if(checked) break;
              }
            }
            if(checked) break;
          }
        }
        if(checked) break;
      }
      in.close();
      outFile.close();
    }
    catch(IOException ioe) {
      System.err.println(ioe);
    }
  }

  /**
   * Prints the mail to ~home/.yamm/tmp/mail.html
   * @param whichBox Which box the message is in
   * @param whichmail Whichmail to export
   * @param attach Which vector to add attachments to
   */
  public static void getMail(String whichBox, int whichmail, Vector attach, boolean mailName) {

    String  boundary = null;
    String  temp = null;
    boolean wait = true;
    boolean html = false;
    ResourceBundle res = null;

    String yammhome = System.getProperty("user.home") + "/.yamm";

    try {
      res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault());
    }
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.exit(1);
    }
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
      PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(yammhome + "/tmp/" + mailName + ".html")));
      int i = 0;
      outFile.println("<html>\n<body>");

      for(;;) {
        temp = in.readLine();
      
        if(temp == null)
          break;

        else if(temp.startsWith("Date: ")) {
          if(i == whichmail) {
            String date = temp.substring(6, temp.lastIndexOf(":") + 2);
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss", Locale.US);
            SimpleDateFormat dateFormat3 = new SimpleDateFormat("EEEEEEEE, dd MMMMMMMM yyyy HH:mm:ss", Locale.getDefault());
            try {
              Date nisse = dateFormat2.parse(date);

              date = dateFormat3.format(nisse);
            } catch (ParseException pe) { System.err.println(pe); date = " ";}
	    outFile.println("<b>" + res.getString("mail.date") + "</b> " + date + "<br>");
          }
        }

        else if(temp.equals(".")) i++;

        else if(temp.startsWith("From:")) {

          if (i == whichmail) {
            String from = "test";
            if(temp.indexOf("<") != -1 && temp.indexOf(">") != -1) {
              from = temp.substring(temp.lastIndexOf("<") + 1, temp.lastIndexOf(">"));
		outFile.println("<b>" + res.getString("mail.from") + "</b> <a href=\"mailto:" + from + "\">" + temp.substring(6, temp.length()) + "</a><br>");
            }
            else {
              from = temp.substring(6, temp.length());
              outFile.println("<b>" + res.getString("mail.from") + "</b> <a href=\"mailto:" + from + "\">" + temp.substring(6, temp.length()) + "</a><br>");
            }
            wait = false;
          }
        }

        else if(temp.startsWith("To:")) {
          if(i == whichmail) {
            
            if(temp.indexOf("<") != -1) temp = temp.substring(temp.indexOf("<")  +1, temp.lastIndexOf(">"));
            else temp = temp.substring(3, temp.length());

            while(temp.indexOf(",", temp.length() - 5) != -1) {
              String temp2 = in.readLine();

              if(temp2.indexOf("<") != -1) temp2 = temp2.substring(temp2.indexOf("<")  +1, temp2.lastIndexOf(">"));

              temp += temp2;
            }

            if(temp.indexOf("<br>") == -1) temp += "<br>";
            outFile.println("<b>" + res.getString("mail.to") + "</b> " + temp);
          }
        }

        else if(temp.startsWith("Reply-To:") && i == whichmail) {
          String reply = "test";
          if(temp.indexOf("<") != -1 && temp.indexOf(">") != -1) {
            reply = temp.substring(temp.lastIndexOf("<") + 1, temp.lastIndexOf(">"));
            outFile.println("<b>" + res.getString("mail.reply_to") + "</b> <a href=\"mailto:" + reply + "\">" + temp.substring(10, temp.length()) + "</a><br>");
          }
          else {
            reply = temp.substring(6, temp.length());
            outFile.println("<b>" + res.getString("mail.reply_to") + "</b> <a href=\"mailto:" + reply + "\">" + temp.substring(10, temp.length()) + "</a><br>");
          }
        }

        else if(temp.indexOf("boundary=\"") != -1) {
          if(i == whichmail) {
            boundary = temp.substring(temp.indexOf("boundary=\"") + 10, temp.indexOf("\"", temp.indexOf("boundary=\"") + 11));
          }
        }


        else if(temp.startsWith("Subject:")) {
          if(i == whichmail) {
            if(temp.indexOf("<br>") == -1) temp += "<br>";
            outFile.println("<b>" + res.getString("mail.subject") + "</b> " + temp.substring(8, temp.length()));
          }
        }

        else if(temp.equals("")) {
          if(i == whichmail && wait == false) {

	    outFile.println("<pre>");
            for(;;) {
              outFile.println(temp);
              outFile.flush();

              temp = in.readLine();

              if(temp == null) break;


              else if(temp.startsWith("<!doctype")) temp = in.readLine();
              else if(temp.toLowerCase().indexOf("</html>") != -1) { html = false; temp = in.readLine(); }

              else if(temp.toLowerCase().indexOf("<html>") != -1) { html = true; temp = in.readLine(); }
              
              else if(temp.indexOf("MIME") != -1 || temp.indexOf("mime") != -1) {
                temp = in.readLine();
                temp = in.readLine();

                if(temp.startsWith("--" + boundary)) {
                  for(;;) {
                    temp = in.readLine();
                    if(temp == null) break;

                    else if(temp.equals("")) break;
                  }
                }
              }

              if(!html && (temp.indexOf("<") != -1 || temp.indexOf(">") != -1 || temp.indexOf("=") != -1)) temp = removeTags(temp);

              if(!html && temp.indexOf("://") != -1 && temp.indexOf("href=") == -1 && temp.indexOf("HREF=") == -1) {
                int    protBegin = temp.indexOf("://");
                int    space     = temp.indexOf(" ", protBegin + 3);
                String prot      = temp.substring( (((protBegin - 4) != -1) ? protBegin - 4 : 0), protBegin + 3).trim();
                String link      = temp.substring(protBegin + 3, ((space == -1) ? temp.length() : space));
                if(link.endsWith(".")) link = link.substring(0, link.length() -1);
                int    dot       = temp.indexOf(".", protBegin + 3 + link.length());
                int    tag       = temp.indexOf("&gt;", protBegin +3);

                if(tag != -1) link = temp.substring(protBegin +3, tag);
                String begin = temp.substring(0, temp.indexOf(prot));
                String end   = "";

                if(space != -1) {
                  if(dot < space && dot != -1) end = temp.substring(dot, temp.length());
                  else end = temp.substring(space, temp.length());
                }
                if(link.endsWith(">")) {
                  link = link.substring(0, link.length()-1);
                }
                else if(dot != -1) end = temp.substring(dot, temp.length());
                else if(tag != -1) end = temp.substring(tag, temp.length());
                

                temp = begin + "<a href=\"" + prot + link + "\">" + prot + link + "</a>" + end;
              }

              else if(!html && temp.indexOf("@") != -1 && temp.indexOf("href=") == -1 && temp.indexOf("HREF=") == -1) {
                String temp2 = null;

                StringTokenizer tok = new StringTokenizer(temp);
                for(;tok.hasMoreTokens();) {
                  temp2 = tok.nextToken();
                  if(temp2.indexOf("@") != -1) break;
                }
                
                String begin = temp.substring(0, temp.indexOf(temp2));
                String end = temp.substring(temp.indexOf(temp2) + temp2.length(), temp.length());
                if(temp2.endsWith("&gt;")) temp2 = temp2.substring(0, temp2.length()-4);
                if(temp2.startsWith("&lt;")) temp = temp2.substring(4, temp2.length());

                temp = begin + "<a href=\"mailto:" + temp2 + "\">" + temp2 + "</a>" + end;
              }

              else if(temp.startsWith("--" + boundary)) {
                String encode = null, filename = null;
                temp = in.readLine();

                if(!temp.equals("") && temp.indexOf("message") == -1) {
                  for(;;) {
                    if(temp == null) break;

                    if(temp.equals(".")) break;

                    if (temp.startsWith("Content-Transfer-Encoding: ")) {
                      encode = temp.substring(27, temp.length());
                    }

                    if(temp.indexOf("name=") != -1) {
                      filename = temp.substring(temp.indexOf("=\"") + 2, temp.indexOf("\"", temp.indexOf("=")+ 2));
                    }
                    if(encode != null && filename != null) {
                      Vector vect1 = new Vector();
                      vect1.add(encode);
                      vect1.add(filename);

                      attach.add(vect1);
                      encode = null;
                      filename = null;
                    }
                    temp = in.readLine();
                  }
                  wait = true;
                  break;
                }
                for(;;) {
                  temp = in.readLine();

                  if(temp.equals("")) break;
                  else if(temp.equals(".")) break;
                  else if(temp == null) break;
                }
              }

              else if(temp.equals(".")) {
                wait = true;
                break;
              }
            }
            break;
          }
        }
      }
      outFile.println("</pre></body></html>");
      in.close();
      outFile.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
    }
  }

  protected static String removeTags(String html) {
    int index = 0;

    for(;html.indexOf("<", index) != -1;) {
      index = html.indexOf("<", index) + 1;

      String begin = html.substring(0, index -1);
      String end = html.substring(index, html.length());
      html = begin + "&lt;" + end;
    }
    index = 0;
    for(;html.indexOf(">", index) != -1;) {
      index = html.indexOf(">", index) + 1;

      String begin = html.substring(0, index -1);
      String end = html.substring(index, html.length());
      html = begin + "&gt;" + end;
    }
    if(html.indexOf("=") != -1) {
      index = 0;
      char[] check = "0123456789ABCDEF".toCharArray();
      for(;html.indexOf("=", index) != -1 && html.indexOf("=", index) + 2 < html.length();) {
        index = html.indexOf("=", index) + 1;
        char[] hex = html.substring(index, index+2).toCharArray();
        boolean char1 = false, char2 = false;

        for(int i = 0; i < 16;i++) {
          if(hex[0] == check[i]) { char1 = true; break; }
        }
        for(int i = 0; i < 16;i++) {
          if(hex[1] == check[i]) { char2 = true; break; }
        }

        if(char1 && char2) {
          int htmlchar = Integer.parseInt(new String(hex), 16);
          String begin = html.substring(0, index -1);
          String end = html.substring(index + 2, html.length());
          html = begin + "&#" + htmlchar + ";" + end;
        }
      }
    }

    return html;
  }

  /**
   * Gets the from and subject field from specified message
   * @param whichBox Which box the message is in
   * @param whichmail Which mail to get the headers from
   */
  public static String[] getMailForReplyHeaders(String whichBox, int whichmail) {

    String  temp = null, from = null, subject = null;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
      int i = 0;

      for(;;) {
        temp = in.readLine();
      
        if(temp == null)
          break;

        else if(temp.startsWith("From:")) {
          if (i == whichmail) {
            from = "";

            if(temp.indexOf("<") != -1 && temp.indexOf(">") != -1)
              from = temp.substring(temp.lastIndexOf("<") + 1, temp.lastIndexOf(">"));
            else  from = temp.substring(6, temp.length());
          }
        }
        else if(temp.equals(".")) i++;

        else if(temp.startsWith("Subject:")) {
          if(i == whichmail) {
            subject = temp.substring(9, temp.length());
          }
        }

        else if(temp.equals("") && from != null && subject != null) {
	  break;
        }
      }
      in.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
    }
    String[] ret = {from,subject};
    return ret;
  }

  /**
   * Gets the content of this mail and adds to the specified JTextArea
   * @param whichBox Which box the message is in
   * @param whichmail Which mail to export
   * @param jtarea The JTextArea to append the message to
   */
  public static void getMailForReply(String whichBox, int whichmail, JTextArea jtarea) {

    String  temp = null, boundary = null;
    boolean wait = true;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
      int i = 0;

      for(;;) {
        temp = in.readLine();
      
        if(temp == null)
          break;
        else if(temp.equals(".")) i++;
        else if(temp.startsWith("From: ") && i == whichmail) wait = false;
        else if(temp.equals("") && i == whichmail && !wait) {
          for(;;) {
            jtarea.append(">" + temp + "\n");

            temp = in.readLine();

            if(temp.indexOf("=") != -1) {
              int index = 0;
              char[] check = "0123456789ABCDEF".toCharArray();
              for(;temp.indexOf("=", index) != -1 && temp.indexOf("=", index) + 2 < temp.length();) {
                index = temp.indexOf("=", index) + 1;
                char[] hex = temp.substring(index, index+2).toCharArray();
                boolean char1 = false, char2 = false;

                for(int j = 0; j < 16;j++) {
                  if(hex[0] == check[j]) { char1 = true; break; }
                }
                for(int j = 0; j < 16;j++) {
                  if(hex[1] == check[j]) { char2 = true; break; }
                }

                if(char1 && char2) {
                  int htmlchar = Integer.parseInt(new String(hex), 16);
                  String begin = temp.substring(0, index -1);
                  String end = temp.substring(index + 2, temp.length());
                  temp = begin + (int)htmlchar + end;
                }
              }
            }
            if(temp == null) break;
            else if(temp.indexOf("MIME") != -1 || temp.indexOf("mime") != -1) {
              temp = in.readLine();
              temp = in.readLine();

              if(temp.startsWith("--" + boundary)) {
                for(;;) {
                  temp = in.readLine();
                  if(temp == null) break;
                  else if(temp.equals("")) break;
                }
              }
            }

            else if(temp.startsWith("--" + boundary) && !temp.substring(14, 15).equals("-") && !temp.substring(14,15).equals(" ")) break;
            else if(temp.equals(".")) {
              break;
            }
          }
          break;
        }
      }
      in.close();
    } catch(IOException ioe) { System.out.println("Error: " + ioe); }
  }

  /**
   * Gets the content of this mail and adds to the specified JTextArea
   * @param whichBox Which box the message is in
   * @param whichmail Which mail to export
   * @param jtarea The JTextArea to append the message to
   */
/*
  public static void getMailForPrint(String whichBox, int whichmail, Book bk) {

    String  temp = null, boundary = null;
    ResourceBundle res = null;
    boolean wait = true;
    int y = 10;

    try {
      res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault());
    }
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.exit(1);
    }

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
      int i = 0;

      for(;;) {
        temp = in.readLine();
      
        if(temp == null) break;
        else if(temp.equals(".")) i++;

        else if(temp.startsWith("Date: ") && i == whichmail) {
          String date = temp.substring(6, temp.lastIndexOf(":") + 2);
          SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss", Locale.US);
          SimpleDateFormat dateFormat3 = new SimpleDateFormat("EEEEEEEE, dd MMMMMMMM yyyy HH:mm:ss", Locale.getDefault());

          try {
            Date nisse = dateFormat2.parse(date);

            date = dateFormat3.format(nisse);
          } catch (ParseException pe) { System.err.println(pe); date = " ";}
          System.out.println(res.getString("DATE") + ": " + date);
//          g.drawString(res.getString("DATE") + ": " + date, 5, y);
          y+=15;
        }

        else if(temp.equals(".")) i++;

        else if(temp.startsWith("From:") && i == whichmail) {
          String from = temp.substring(6, temp.length());
          System.out.println(res.getString("FROM") + ": " + from);
//          g.drawString(res.getString("FROM") + ": " + from, 5, y);
          y+=15;
	  wait = false;
        }

        else if(temp.startsWith("To:") && i == whichmail) {
          temp = temp.substring(4, temp.length());

          while(temp.indexOf(",", temp.length() - 5) != -1) {
            String temp2 = in.readLine();
            if(temp2.indexOf("<") != -1) temp2 = temp2.substring(temp2.indexOf("<")  +1, temp2.lastIndexOf(">"));
            temp += temp2;
          }
          System.out.println(res.getString("TO") + ": " +  temp);
//          g.drawString(res.getString("TO") + ": " +  temp, 5, y);
          y+=15;
        }

        else if(temp.startsWith("Subject:") && i == whichmail) {
          System.out.println(res.getString("SUBJECT") + ": " + temp.substring(9, temp.length()));
//          g.drawString(res.getString("SUBJECT") + ": " + temp.substring(9, temp.length()), 5, y);
          y+=15;
        }

        else if(temp.indexOf("boundary=\"") != -1 && i == whichmail) {
          boundary = temp.substring(temp.indexOf("boundary=\"") + 10, temp.indexOf("\"", temp.indexOf("boundary=\"") + 11));
        }

        else if(temp.equals("") && i == whichmail && !wait) {
          for(;;y+=15) {
            System.out.println(temp);
//            if(temp.equals("")) g.drawString("\n", 5, y);
//            else g.drawString(temp, 5, y);

            temp = in.readLine();

            if(temp == null) break;
            else if(temp.indexOf("MIME") != -1 || temp.indexOf("mime") != -1) {
              temp = in.readLine();

              if(temp.equals("--" + boundary)) {
                for(;;) {
                  temp = in.readLine();
                  if(temp == null) break;
                  else if(temp.equals("")) break;
                }
              }
            }

            else if(temp.equals("--" + boundary)) {
              temp = in.readLine();
              temp = in.readLine();
              if(temp.indexOf("7bit") != -1) { temp = in.readLine(); continue; }
              else break;
            }
            else if(temp.equals(".")) {
              break;
            }
          }
          break;
        }
      }
      in.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
    }
  }
*/

  /**
   * Deletes the specified mail. Returns false if it fails.
   * @param whichBox The box the message is in
   * @param whichmail The mail to delete
   */ 
  public static boolean deleteMail(String whichBox, int whichmail) {
    String temp = null;
    boolean firstmail = true;
    String home = System.getProperty("user.home");
    String sep = System.getProperty("file.separator");

    if(!whichBox.equals(home + sep + ".yamm" + sep + "boxes" + sep + "trash")) {
      try {
	File inputFile = new File(whichBox);
	File outputFile = new File(whichBox + ".tmp");
        PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
        PrintWriter outFile2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(System.getProperty("user.home") + "/.yamm/boxes/trash", true)));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));

        int i = 0;

        for(;;) {
          temp = in.readLine();

          if(temp == null) break;

          else if(firstmail) {
            if(i != whichmail) {
              for(;;) {
                outFile.println(temp);
                temp = in.readLine();
          
                if(temp == null) break;
                else if(temp.equals(".")) {
                  outFile.println(temp);
                  break;
                }
              } 
            }
            else if(i == whichmail) {
              for(;;) {
                outFile2.println(temp);
                temp = in.readLine();

                if(temp == null) break;
                else if(temp.equals(".")) {
                  outFile2.println(temp);
                  break;
                }
              }
            }
            firstmail = false;
          }
          else if(!firstmail) {
            i++;
       
            if(i != whichmail) {
              for(;;) {
                outFile.println(temp);
                temp = in.readLine();

                if(temp == null) break;
                else if(temp.equals(".")) {
                  outFile.println(temp);
                  break;
                }
              }
            }
            else if(i == whichmail) {
              for(;;) {
                outFile2.println(temp);
                temp = in.readLine();

                if(temp == null) break;
                else if(temp.equals(".")) {
                  outFile2.println(temp);
                  break;
                }
              }
            }
          }
        }
        in.close();
        outFile.close();
        outFile2.close();

        inputFile.delete();
        if(!outputFile.renameTo(inputFile)) {
          System.err.println("ERROR: Couldn't rename " + whichBox + ".tmp to " + whichBox);
        }
      }
      catch(IOException ioe) {
        System.out.println("Error: " + ioe);
        return false;
      }
    }
    else if(whichBox.equals(home + sep + ".yamm" + sep + "boxes" + sep + "trash")) {
      try {
        File inputFile = new File(home + "/.yamm/boxes/trash");
	File outputFile = new File(home + "/.yamm/boxes/trash.tmp");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));

        int i = 0;

        for(;;) {
          temp = in.readLine();

          if(temp == null) break;
          else if(firstmail) {
            if(i != whichmail) {
              for(;;) {
                outFile.println(temp);
                temp = in.readLine();
          
                if(temp == null) break;
                else if(temp.equals(".")) {
                  outFile.println(temp);
                  break;
                }
              } 
            }
            else if(i == whichmail) {
              for(;;) {
                temp = in.readLine();

                if(temp == null) break;
                else if(temp.equals(".")) break;
              }
            }
            firstmail = false;
          }
          else if(!firstmail) {
            i++;
       
            if(i != whichmail) {
              for(;;) {
                outFile.println(temp);
                temp = in.readLine();

                if(temp == null) break;
                else if(temp.equals(".")) {
                  outFile.println(temp);
                  break;
                }
              }
            }
            else if(i == whichmail) {
              for(;;) {
                temp = in.readLine();

                if(temp == null) break;
                else if(temp.equals(".")) break;
              }
            }
          }
        }

        in.close();
        outFile.close();

        inputFile.delete();
        if(!outputFile.renameTo(inputFile)) {
          System.err.println("ERROR: Couldn't rename " + whichBox + ".tmp to " + whichBox);
        }
      }
      catch(IOException ioe) {
        System.out.println("Error: " + ioe);
        return false;
      }
    }
    return true;
  }

  /**
   * This function tries to copy a mail from a box to another.
   * @param fromBox From which box
   * @param toBox To which box
   * @param whichmail The mail to copy from fromBox to toBox
   */
  public static boolean copyMail(String fromBox, String toBox, int whichmail) {

    String  temp = null;
    boolean firstmail = true;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fromBox)));
      PrintWriter outFile2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(toBox, true)));

      int i = 0;

      for(;;) {
        temp = in.readLine();
        

        if(temp == null) break;

        else if(firstmail) {
          if(i != whichmail) {
            for(;;) {
              temp = in.readLine();
        
              if(temp == null) break;
              else if(temp.equals(".")) break;
            } 
          }

          else if(i == whichmail) {
            for(;;) {
              outFile2.println(temp);
              temp = in.readLine();

              if(temp == null) break;
              else if(temp.equals(".")) {
                outFile2.println(temp);
                break;
              }
            }
          }
          firstmail = false;
        }

        else if(!firstmail) {
          i++;
       
          if(i != whichmail) {
            for(;;) {
              temp = in.readLine();

              if(temp == null) break;
              else if(temp.equals(".")) break;
            }
          }

          else if(i == whichmail) {
            for(;;) {
              outFile2.println(temp);
              temp = in.readLine();

              if(temp == null) break;
              else if(temp.equals(".")) {
                outFile2.println(temp);
                break;
              }
            }
          }
        }
      }

      in.close();
      outFile2.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
      return false;
    }
    return true;
  }

  /**
   * Moves a mail from one box to another.
   * @param fromBox The box to move the mail from
   * @param toBox The box to move the mail to
   * @param whichmail The mail to move
   */
  public static boolean moveMail(String fromBox, String toBox, int whichmail) {
    String temp = null;
    boolean firstmail = true;

    try {
      File inputFile = new File(fromBox);
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
      File outputFile = new File(fromBox + ".tmp");
      PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
      PrintWriter outFile2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(toBox, true)));

      int i = 0;

      for(;;) {
        temp = in.readLine();
        

        if(temp == null) break;

        else if(firstmail) {
          if(i != whichmail) {
            for(;;) {
              outFile.println(temp);
              temp = in.readLine();
        
              if(temp == null) break;
              else if(temp.equals(".")) {
                outFile.println(temp);
                break;
              }
            } 
          }

          else if(i == whichmail) {
            for(;;) {
              outFile2.println(temp);
              temp = in.readLine();

              if(temp == null) break;
              else if(temp.equals(".")) {
                outFile2.println(temp);
                break;
              }
            }
          }
          firstmail = false;
        }

        else if(!firstmail) {
          i++;
       
          if(i != whichmail) {
            for(;;) {
              outFile.println(temp);
              temp = in.readLine();

              if(temp == null) break;
              else if(temp.equals(".")) {
                outFile.println(temp);
                break;
              }
            }
          }

          else if(i == whichmail) {
            for(;;) {
              outFile2.println(temp);
              temp = in.readLine();

              if(temp == null) break;
              else if(temp.equals(".")) {
                outFile2.println(temp);
                break;
              }
            }
          }
        }
      }
      in.close();
      outFile.close();
      outFile2.close();

      inputFile.delete();
      if(!outputFile.renameTo(inputFile)) {
        System.err.println("ERROR: Couldn't rename " + fromBox + ".tmp to " + fromBox);
      }
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
      return false;
    }
    return true;
  }

  /**
   * Counts how many mails the box have.
   * @param whichbox The box to count the mails in
   */
  public static int mailsInBox(String whichbox) {
    String temp = null;

    int i = 0;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichbox)));

      for(;;) {
        temp = in.readLine();

        if(temp == null) break;

        if(temp.equals(".")) i++;
      }
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
    }
    return i;
  }

  /**
   * Checks if the box has mail in it.
   * @param whichBox The box to check for mail
   */
  public static boolean hasMail(String whichBox) {
    String temp = null;
    boolean mail = false;

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));

      for(;;) {
        temp = in.readLine();
        
        if(temp == null) break;
        else if(temp.startsWith("Date:")) { mail = true; break; }
      }
      in.close();
    }
    catch(IOException ioe) {
      System.out.println("Error: " + ioe);
      return false;
    }
    if(mail) return true;
    else return false;
  }
}
