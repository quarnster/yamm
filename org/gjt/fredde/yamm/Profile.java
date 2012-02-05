/*  Profiler.java - class for managing profiles
 *  Copyright (C) 2000 Fredrik Ehnbom
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
package org.gjt.fredde.yamm;

/**
 * The profile class
 * @author Fredrik Ehnbom
 * @version $Id: Profile.java,v 1.2 2000/04/01 20:50:09 fredde Exp $
 */
public class Profile {

	/**
	 * This profiles name
	 */
	public String name;

	/**
	 * This profiles email-address
	 */
	public String email;

	/**
	 * This profiles reply-address
	 */
	public String reply;

	/**
	 * This profiles signatur
	 */
	public String sign;

	/**
	 * Creates a new empty profile
	 */
	public Profile() {
		this("", "", "", "");
	}

	/**
	 * Creates a new profile with the specified name and email
	 * @param name The name for the profile
	 * @param email The email for the profile
	 */
	public Profile(String name, String email) {
		this(name, email, "", "");
	}

	/**
	 * Creates a new profile
	 * @param name The name for the profile
	 * @param email The email for the profile
	 * @param reply The reply-address for the profile
	 * @param sign The signatur for the profile
	 */
	public Profile(String name, String email, String reply, String sign) {
		this.name = name;
		this.email = email;
		this.reply = reply;
		this.sign = sign;
	}
}
/*
 * Changes:
 * $Log: Profile.java,v $
 * Revision 1.2  2000/04/01 20:50:09  fredde
 * fixed to make the profiling system work
 *
 * Revision 1.1  2000/03/26 15:26:00  fredde
 * Files for the new profiling system
 *
 */
