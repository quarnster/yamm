/*  DateParser.java - Parses dates
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

import java.text.*;
import java.io.*;
import java.util.*;

/**
 * Parses dates in common mail-dateformats.
 */
public class DateParser {

	/** The target format */
	protected String  DateFormat = "EEEEEEE, MMMMMMMM dd yyyy HH:mm:ss";

	/**
	 * Parses the date
	 * @param date The date to parse from
	 */
	public String parse(String date) throws ParseException {
		SimpleDateFormat dateParser = null;

		if (date.charAt(2) == ' ' || date.charAt(1) == ' ') {
			dateParser = new SimpleDateFormat("dd MMM yyyy"
						+ "HH:mm:ss", Locale.US);
		} else {
			dateParser = new SimpleDateFormat("EEE, dd MMM yyyy"
						+ "HH:mm:ss", Locale.US);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat);

		date = dateFormat.format(dateParser.parse(date));

		return date;
	}

	/**
	 * Sets the target format
	 */
	public void setTargetFormat(String DateFormat) {
		this.DateFormat = DateFormat;
	}

	public DateParser() {
	}
}
