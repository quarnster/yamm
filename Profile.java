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
 * @version $Id: Profile.java,v 1.1 2000/03/26 15:26:00 fredde Exp $
 */
public class Profile {

	public String name;
	public String email;
	public String reply;
	public String sign;

	public Profile() {
		this("", "", "", "");
	}

	public Profile(String name, String email) {
		this(name, email, "", "");
	}

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
 * Revision 1.1  2000/03/26 15:26:00  fredde
 * Files for the new profiling system
 *
 */
