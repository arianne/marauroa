/* $Id: generateini.java,v 1.11 2006/08/20 15:40:09 wikipedian Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import marauroa.common.crypto.RSAKey;

/**
 * generates the ini file
 */
public class generateini {
	/**
	 * reads a String from the input. When no String is choosen the defaultValue
	 * is used.
	 */
	public static String getStringWithDefault(BufferedReader input,
			String defaultValue) {
		String ret = "";
		try {
			ret = input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (ret.length() == 0 && defaultValue != null) {
			ret = defaultValue;
		}
		return ret;
	}

	/**
	 * reads a String from the input. When no String is choosen the errorMessage
	 * is is displayed and the application is terminated.
	 */
	public static String getStringWithoutDefault(BufferedReader input,
			String errorMessage) {
		String ret = "";
		try {
			ret = input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (ret.length() == 0) {
			System.out.println(errorMessage);
			System.out.println("Terminating...");
			System.exit(1);
		}
		return ret;
	}

	/** makes the first letter of the source uppercase */
	public static String uppcaseFirstLetter(String source) {
		return (source.length() > 0) ? Character.toUpperCase(source.charAt(0))
				+ source.substring(1) : source;
	}

	public static void main(String[] args) {
		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));

		System.out
				.println("Marauroa - arianne's open source multiplayer online framework for game development -");
		System.out.println("(C) 1999-2005 Miguel Angel Blanch Lardin");
		System.out.println();
		System.out
				.println("This program is free software; you can redistribute it and/or modify");
		System.out
				.println("it under the terms of the GNU General Public License as published by");
		System.out
				.println("the Free Software Foundation; either version 2 of the License, or");
		System.out.println("(at your option) any later version.");
		System.out.println();
		System.out
				.println("This program is distributed in the hope that it will be useful,");
		System.out
				.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		System.out
				.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		System.out.println("GNU General Public License for more details.");
		System.out.println();
		System.out
				.println("You should have received a copy of the GNU General Public License");
		System.out
				.println("along with this program; if not, write to the Free Software");
		System.out
				.println("Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
		System.out.println();
		System.out.println();

		System.out.println("This is the configuration utility for marauroa.");
		System.out
				.println("Default values for each question will be printed in brackets.\n\n");

		/** Create a .ini file for storing options */
		System.out.print("Write name of the .ini file [marauroa.ini]: ");
		String filename = getStringWithDefault(input, "marauroa.ini");
		File file = new File(filename);
		System.out.println("Writing to \"" + file.getAbsolutePath() + "\"\n");

		/** Write configuration for database */
		System.out.print("Write name of the database [marauroa]: ");
		String databasename = getStringWithDefault(input, "marauroa");
		System.out.println("Using \"" + databasename + "\" as database name\n");

		System.out.print("Write name of the database host [localhost]: ");
		String databasehost = getStringWithDefault(input, "localhost");
		System.out.println("Using \"" + databasehost + "\" as database host\n");

		System.out.print("Write name of the database user: ");
		String databaseuser = getStringWithoutDefault(input,
				"Please enter a database user");
		System.out.println("Using \"" + databaseuser + "\" as database user\n");

		System.out.print("Write value of the database user password: ");
		String databasepassword = getStringWithoutDefault(input,
				"Please enter a database password");
		System.out.println("Using \"" + databasepassword
				+ "\" as database user password\n");

		System.out
				.println("In order to make efective these options please run:");
		System.out.println("# mysql");
		System.out.println("  create database " + databasename + ";");
		System.out.println("  grant all on " + databasename + ".* to "
				+ databaseuser + "@localhost identified by '"
				+ databasepassword + "';");
		System.out.println("  exit");
		System.out.println();

		/** Choose port that will be used */
		System.out
				.print("Write UDP port >1024 used by Marauroa. Note: Stendhal uses port 32160: ");
		String udpport = getStringWithoutDefault(input,
				"Please choose a UDP port.");
		System.out.println("Using \"" + udpport
				+ "\" as UDP port for Marauroa\n");

		/** Choose RP Content that will be used */
		System.out
				.println("Marauroa is a server middleware to run multiplayer games. You need to"
						+ "add a game to the system so that server can work. Actually Arianne has implemented several games:");
		System.out.println("- stendhal");
		System.out.println("- mapacman");
		System.out.println("- the1001");
		System.out
				.println("If you write your own game, just write its name here.");
		System.out.println("You will be asked for more info.\n");

		System.out.print("Write name of the game server will run: ");
		String gamename = getStringWithoutDefault(input,
				"Please enter the name of a game.");
		System.out.println("Using \"" + gamename + "\" as game\n");

		String rp_RPWorldClass = "";
		String rp_RPRuleProcessorClass = "";
		String databaseType = "marauroa.server.game.JDBCPlayerDatabase";

		if (gamename.equals("stendhal")) {
			System.out
					.println("NOTE: Setting RPWorld and RPRuleProcessor for Stendhal");
			System.out
					.println("NOTE: Make sure Marauroa can find in CLASSPATH the folder games/stendhal/*");
			System.out
					.println("NOTE: Copy stendhal's games folder inside folder that contains marauroa.jar file");

			rp_RPWorldClass = "games.stendhal.server.StendhalRPWorld";
			rp_RPRuleProcessorClass = "games.stendhal.server.StendhalRPRuleProcessor";
			databaseType = "games.stendhal.server.StendhalPlayerDatabase";
		} else {
			System.out.println("Setting RPWorld and RPRuleProcessor for "
					+ gamename);
			String defaultRpWorld = "games/stendhal/" + gamename + "/server/"
					+ uppcaseFirstLetter(gamename) + "RPWorld.class";
			System.out.print("Write game's RPWorld class file  ["
					+ defaultRpWorld + "]: ");
			rp_RPWorldClass = getStringWithDefault(input, defaultRpWorld);
			System.out.println("Using RPWorld class \"" + rp_RPWorldClass
					+ "\"\n");

			String defaultRpRuleProcessor = "games/stendhal/" + gamename
					+ "/server/" + uppcaseFirstLetter(gamename)
					+ "RPRuleProcessor.class";
			System.out.print("Write game's RPRuleProcessor class file  ["
					+ defaultRpRuleProcessor + "]: ");
			rp_RPRuleProcessorClass = getStringWithDefault(input,
					defaultRpRuleProcessor);
			System.out.println("Using RPRuleProcessor class \""
					+ rp_RPWorldClass + "\"\n");
		}

		System.out.println();

		/** Choose turn time that will be used */
		System.out
				.print("Write turn time duration in milliseconds (200<time<1000) [300]: ");
		String turntime = getStringWithDefault(input, "300");
		System.out.println("Using turn of " + turntime + " milliseconds\n");

		/** Choose where logs and statistics are generated */
		System.out.print("Write path for logs generation [./]: ");
		String logs_path = getStringWithDefault(input, "./");
		System.out.println("Using path \"" + logs_path
				+ "\" for log generation\n");

		System.out.print("Write path for statistics generation [./]: ");
		String statistics_path = getStringWithDefault(input, "./");
		System.out.println("Using path \"" + statistics_path
				+ "\" for statistics generation\n");

		System.out
				.print("Write size for the RSA key of the server. Be aware that a key bigger than 1024 could be very long to create [512]: ");
		String keySize = getStringWithDefault(input, "512");
		System.out.println("Using key of " + keySize + " bits.");
		System.out.println("Please wait while the key is generated.");
		RSAKey key = RSAKey.generateKey(Integer.valueOf(keySize));
		System.out.println("\n--- COMPLETE ---");

		System.out
				.println("Generating \"" + file.getAbsolutePath() + "\" file");
		try {
			PrintWriter output = new PrintWriter(new FileOutputStream(file));
			output.println("### Configuration file for " + gamename);
			output.println("### Automatically generated by generateini");
			output.println();
			output.println("marauroa_DATABASE=" + databaseType);
			output.println();
			output.println("jdbc_url=jdbc:mysql://" + databasehost + "/"
					+ databasename);
			output.println("jdbc_class=com.mysql.jdbc.Driver");
			output.println("jdbc_user=" + databaseuser);
			output.println("jdbc_pwd=" + databasepassword);
			output.println();
			output.println("marauroa_PORT=" + udpport);
			output.println();
			output.println("rp_RPWorldClass=" + rp_RPWorldClass);
			output
					.println("rp_RPRuleProcessorClass="
							+ rp_RPRuleProcessorClass);
			output.println("rp_turnDuration=" + turntime);
			output.println();
			output.println("server_typeGame=" + gamename);
			output.println("server_name=" + gamename + " Marauroa server");
			output.println("server_version=stable");
			output
					.println("server_contact=https://sourceforge.net/tracker/?atid=514826&group_id=66537&func=browse");
			output.println();
			output.println("server_stats_directory=" + statistics_path);
			output.println("server_logs_directory=" + logs_path);
			output.println("server_logs_allowed=");
			output.println("server_logs_rejected=");
			output.println();
			key.print(output);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Generated.");
	}
}
