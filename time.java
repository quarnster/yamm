/*  time.java - methods for converting from/to internet time
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This class provides methods for converting regular time to internet time and
 * internet time to regular time. Our regular 24 hours has been converted to
 * 1000 so called "Swatch beats", which is exactly 1 minute and 26,4 seconds 
 * long.<br><br>
 * The timemedian has been set to Biel in Switzerland, and when the clock
 * strikes 00.00 the internet time begins. At 12.00 am in Biel the beats has 
 * gone as far as @500. Because of this all timezones are eliminated.
 * <br><br>
 * Now what's so good about this? For example if you have a friend in a country
 * far away from yours that you'd like to chat with, you can tell that friend
 * that you'll meet him/her at @700 and he/she will know what the time for that
 * is in his/her country.
 * <br><br>
 * For more information, please visit 
 * <a href="http://www.swatch.com">http://www.swatch.com</a>.
 */
public abstract class time {

  /**
   * Converts a time into Swatch beats.
   * @param time The number of milliseconds since January 1, 1970, 00:00:00 GMT
   * that you'd like to convert to Swatch beats.
   * @return The number of Swatch beats since that time.
   */
  public static int toInternetTime(long time) {
    Calendar c = new GregorianCalendar();
    Date d = new Date(c.get(Calendar.ZONE_OFFSET));

    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    String time1 = df.format(d);

    d = new Date(time);
    String time2 = df.format(d);

    int hour = Integer.parseInt(time2.substring(0, 2)) - 
               Integer.parseInt(time1.substring(0, 2));
    int min  = Integer.parseInt(time2.substring(3, 5)) - 
               Integer.parseInt(time1.substring(3, 5));
    int sec  = Integer.parseInt(time2.substring(6, 8));   

    long it = (((hour*3600 + min * 60 + sec) + 3600)*1000)/86400;

    if(it >= 1000)
      it -= 1000;
    else if(it < 0)
      it += 1000;

    return (int)it;
  }

  /**
   * Converts Swatch beats into regular time
   * @param it The swatch beats to convert
   * @return The converted time.
   */ 
  public static long toRegularTime(int it) {
    Calendar c = new GregorianCalendar();
    int mo = c.get(Calendar.ZONE_OFFSET);
    long millis1 = it * 86400  + ( (mo/(60*60*1000)))  * 60000;

    return millis1;
  }

  /**
   * Prints out the current time, converted into internet time and then 
   * converted back to regular time. Works as an example.
   */
  public static void main(String args[]) {
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

    System.out.println("    Local time: " + df.format(new Date()));
    System.out.println(" Internet time: @" + time.toInternetTime(new Date().getTime())); 
    
    System.out.println("Converted time: " + df.format(new Date(time.toRegularTime(time.toInternetTime(new Date().getTime())))));
  }
} 
