/*  time.java - methods for converting from/to internet time
 *  Copyright (C) 1999, 2000 Fredrik Ehnbom
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
 * <br><br>
 * This package is uses code from Martin Dahl's
 * <a href="http://xbeats.sourceforge.net">xbeats</a> program.
 * @author Fredrik Ehnbom
 * @version $Id: time.java,v 1.4 2000/03/27 12:46:55 fredde Exp $
 */
public abstract class time {

	/**
	 * The calendar to use for the various convertions
	 */
	private static Calendar c = new GregorianCalendar();


	/**
	 * Converts a time into Swatch beats.
	 * @param time The number of milliseconds since January 1, 1970, 00:00:00 GMT
	 * that you'd like to convert to Swatch beats.
	 * @return The number of Swatch beats since that time.
	 */
	public static int toInternetTime(long time) {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

		Date d = new Date(time);
		String time2 = df.format(d);

		int hour = Integer.parseInt(time2.substring(0, 2));
		int min  = Integer.parseInt(time2.substring(3, 5));
		int sec  = Integer.parseInt(time2.substring(6, 8));   

		long it = (hour * 3600 + min * 60 + sec);
		if (c.get(Calendar.DST_OFFSET) > 0) it -= 3600;
		it = (it*1000)/86400;

		if(it >= 1000) {
			it -= 1000;
		} else if(it < 0) {
			it += 1000;
		}

		return (int) it;
	}

	/**
	 * Returns the number of Swatch beats at the specified clock beat
	 * @param hour An hour between 1-24
	 * @return The number of beats at that hour
	 */
	public static int timeAt(int hour) {

		long time = (long) ( (hour * 3600000) - c.get(Calendar.ZONE_OFFSET));
		
		return toInternetTime(time);
	}

	/**
	 * Converts Swatch beats into regular time
	 * @param it The swatch beats to convert
	 * @return The converted time.
	 */ 
	public static long toRegularTime(int it) {
		int mo = c.get(Calendar.ZONE_OFFSET);
		if (c.get(Calendar.DST_OFFSET) > 0) mo += 3600;
		long millis1 = it * 86400  + (mo / 3600000) * 60000;

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
		if (args.length > 0) {
			System.out.println("    Time at " + args[0] + ": @" + timeAt(Integer.parseInt(args[0])));
		}
	}
}
/*
 * Changes:
 * $Log: time.java,v $
 * Revision 1.4  2000/03/27 12:46:55  fredde
 * changed xbeats homepage to xbeats.sourceforge.net
 *
 * Revision 1.3  2000/03/26 11:32:23  fredde
 * gave Martin Dahl the credits he deserves, now should work correctly...
 *
 * Revision 1.2  2000/03/20 19:19:44  fredde
 * cleaned up and fixed
 *
 */
