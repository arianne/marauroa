/* $Id: generateini.java,v 1.16 2007/03/02 10:02:31 arianne_rpg Exp $ */
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
 *
 */
public abstract class generateini {
	/** Where data is read from */
	private BufferedReader in;
	/** Where we print data to */
	private PrintWriter out;

	/** Name of the file where stats are saved */
	private String file;

	/**
	 * reads a String from the input. When no String is choosen the defaultValue
	 * is used.
	 * @param input the buffered input, usually System.in
	 * @param defaultValue if no value is written.
	 * @return the string readed or default if none was read.
	 */
	public static String getStringWithDefault(BufferedReader input,	String defaultValue) {
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
	 * @param input the input stream, usually System.in
	 */
	public static String getStringWithoutDefault(BufferedReader input, String errorMessage) {
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

	public generateini() {
		in=new BufferedReader(new InputStreamReader(System.in));
	}


	public void write() throws FileNotFoundException {
		out= new PrintWriter(new FileOutputStream(file));

		System.out.println("Marauroa - arianne's open source multiplayer online framework for game development -");
		System.out.println("(C) 1999-2007 Miguel Angel Blanch Lardin");
		System.out.println();
		System.out.println("This program is free software; you can redistribute it and/or modify");
		System.out.println("it under the terms of the GNU General Public License as published by");
		System.out.println("the Free Software Foundation; either version 2 of the License, or");
		System.out.println("(at your option) any later version.");
		System.out.println();
		System.out.println("This program is distributed in the hope that it will be useful,");
		System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		System.out.println("GNU General Public License for more details.");
		System.out.println();
		System.out.println("You should have received a copy of the GNU General Public License");
		System.out.println("along with this program; if not, write to the Free Software");
		System.out.println("Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
		System.out.println();
		System.out.println();

		System.out.println("This is the configuration utility for marauroa.");
		System.out.println("Default values for each question will be printed in brackets.\n\n");

		/*
		 *  Create a .ini file for storing options
		 */
		String filename = getIniFilename();
		File file = new File(filename);
		System.out.println("Writing to \"" + file.getAbsolutePath() + "\"\n");

		/** Write configuration for database */
		String databasename = getDatabaseName();
		System.out.println("Using \"" + databasename + "\" as database name\n");

		String databasehost = getDatabaseHost();
		System.out.println("Using \"" + databasehost + "\" as database host\n");

		String databaseuser = getDatabaseUser();
		System.out.println("Using \"" + databaseuser + "\" as database user\n");

		String databasepassword = getDatabasePassword();
		System.out.println("Using \"" + databasepassword+ "\" as database user password\n");

		System.out.println("In order to make efective these options please run:");
		System.out.println("# mysql");
		System.out.println("  create database " + databasename + ";");
		System.out.println("  grant all on " + databasename + ".* to "
				+ databaseuser + "@localhost identified by '"
				+ databasepassword + "';");
		System.out.println("  exit");

		System.out.println();

		/** Choose port that will be used */
		String udpport = getServerPort();
		System.out.println("Using \"" + udpport + "\" as UDP port for Marauroa\n");

		/** Choose RP Content that will be used */
		System.out.println("Marauroa is a server middleware to run multiplayer games. You need to"
				+ "add a game to the system so that server can work. Actually Arianne has implemented several games:");
		System.out.println("- stendhal");
		System.out.println("- mapacman");
		System.out.println("- the1001");
		System.out.println("If you write your own game, just write its name here.");
		System.out.println("You will be asked for more info.\n");

		String gamename = getGameName();
		System.out.println("Using \"" + gamename + "\" as game\n");

		String world = getGameRPWorld();
		String ruleprocessor = getGameRPRuleProcessor();
		String databaseType = getGameDatabaseClass();

		System.out.println();

		/** Choose turn time that will be used */
		String turntime = getGameTurnTime();
		System.out.println("Using turn of " + turntime + " milliseconds\n");

		/* Where the statistics file is */
		String statistics_path = getStatisticsFile();
		System.out.println("Using path \"" + statistics_path+ "\" for statistics generation\n");

		/* The size of the RSA Key  in bits, usually 512 */
		String keySize = getRSAKeyBits();
		System.out.println("Using key of " + keySize + " bits.");
		System.out.println("Please wait while the key is generated.");
		RSAKey key = RSAKey.generateKey(Integer.valueOf(keySize));


		System.out.println("\n--- COMPLETE ---");

		System.out.println("Generating \"" + file.getAbsolutePath() + "\" file");

		out.println("### Configuration file for " + gamename);
		out.println("### Automatically generated by generateini");
		out.println();
		out.println("marauroa_DATABASE=" + databaseType);
		out.println();
		out.println("jdbc_url=jdbc:mysql://" + databasehost + "/"+ databasename);
		out.println("jdbc_class=com.mysql.jdbc.Driver");
		out.println("jdbc_user=" + databaseuser);
		out.println("jdbc_pwd=" + databasepassword);
		out.println();
		out.println("marauroa_PORT=" + udpport);
		out.println();
		out.println("rp_RPWorldClass=" + world);
		out.println("rp_RPRuleProcessorClass="+ ruleprocessor);
		out.println("rp_turnDuration=" + turntime);
		out.println();
		out.println("server_typeGame=" + gamename);
		out.println("server_name=" + gamename + " Marauroa server");
		out.println("server_version=stable");
		out.println("server_contact=https://sourceforge.net/tracker/?atid=514826&group_id=66537&func=browse");
		out.println();
		out.println("server_stats_directory=" + statistics_path);
		out.println("server_logs_allowed=");
		out.println("server_logs_rejected=");
		out.println();
		key.print(out);
		out.close();

		System.out.println("Generated.");
	}

	protected String getRSAKeyBits() {
		System.out.print("Write size for the RSA key of the server. Be aware that a key bigger than 1024 could be very long to create [512]: ");
		String keySize = getStringWithDefault(in, "512");
		return keySize;
	}

	protected String getStatisticsFile() {
		System.out.print("Write path for statistics generation [./]: ");
		String statistics_path = getStringWithDefault(in, "./");
		return statistics_path;
	}

	protected String getGameTurnTime() {
		System.out.print("Write turn time duration in milliseconds (200<time<1000) [300]: ");
		String turntime = getStringWithDefault(in, "300");
		return turntime;
	}

	protected String getGameDatabaseClass() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getGameRPRuleProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getGameRPWorld() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getGameName() {
		System.out.print("Write name of the game server will run: ");
		String gamename = getStringWithoutDefault(in,"Please enter the name of a game.");
		return gamename;
	}

	protected String getServerPort() {
		System.out.print("Write UDP port >1024 used by Marauroa. Note: Stendhal uses port 32160: ");
		String udpport = getStringWithoutDefault(in,"Please choose a UDP port.");
		return udpport;
	}

	protected String getDatabasePassword() {
		System.out.print("Write value of the database user password: ");
		String databasepassword = getStringWithoutDefault(in,"Please enter a database password");
		return databasepassword;
	}

	protected String getDatabaseUser() {
		System.out.print("Write name of the database user: ");
		String databaseuser = getStringWithoutDefault(in,"Please enter a database user");
		return databaseuser;
	}

	protected String getDatabaseHost() {
		System.out.print("Write name of the database host [localhost]: ");
		String databasehost = getStringWithDefault(in, "localhost");
		return databasehost;
	}

	protected String getDatabaseName() {
		System.out.print("Write name of the database [marauroa]: ");
		String databasename = getStringWithDefault(in, "marauroa");
		return databasename;
	}

	protected String getIniFilename() {
		System.out.print("Write name of the .ini file [marauroa.ini]: ");
		String filename = getStringWithDefault(in, "marauroa.ini");
		return filename;
	}
}
