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

import java.io.*;
import java.util.*;

/**
 * A class for managing profiles
 * @author Fredrik Ehnbom
 * @version $Id: Profiler.java,v 1.1 2000/03/26 15:26:00 fredde Exp $
 */
public class Profiler {

	/**
	 * The properties for the profiles
	 */
	private Properties props = new Properties();

	/**
	 * The profiles
	 */
	private Profile[] profiles;

	private Hashtable phash = new Hashtable();

	public Profiler() {
		try {             
			InputStream in = new FileInputStream(YAMM.home + "/.profiles");
			props.load(in);
			in.close();    
		} catch (IOException propsioe) {
			propsioe.printStackTrace();
			System.exit(1);
		}

		profiles = new Profile[Integer.parseInt(props.getProperty("profile.num", "0"))];

		for (int i = 0; i < profiles.length; i++) {
			String name  = props.getProperty("profile." + i + ".name");
			String email = props.getProperty("profile." + i + ".email");
			String reply = props.getProperty("profile." + i + ".reply");
			String sign  = props.getProperty("profile." + i + ".sign");

			profiles[i] = new Profile(name, email, reply, sign);
			phash.put(email, "" + i);
		}
	}

	public int getDefault() {
		return Integer.parseInt(props.getProperty("profile.default", "0"))
	}

	public Profile getDefaultProfile() {
		return profiles[getDefault()];
	}

	public Profile[] getProfiles() {
		return profiles;
	}

	public Profile getProfileFor(String prof) {
		return profiles[0];
	}

}
/*
 * Changes:
 * $Log: Profiler.java,v $
 * Revision 1.1  2000/03/26 15:26:00  fredde
 * Files for the new profiling system
 *
 */
