package marauroa.server;

import java.io.*;
import marauroa.common.crypto.RSAKey;

public class generateini 
  {
  public static void main (String[] args)
    {
    BufferedReader input=new BufferedReader(new InputStreamReader(System.in));
    
    System.out.println("Marauroa - arianne's open source multiplayer online framework for game development -");
    System.out.println("(C) 1999-2005 Miguel Angel Blanch Lardin");
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
    
    /** Create a .ini file for storing options */
    System.out.print("Write name of the .ini file (marauroa.ini): ");
    String filename="";
    try
      {
      filename=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(filename.equals(""))
      {
      filename="marauroa.ini";
      }

    System.out.println("Using \""+filename+"\" as .ini file");
    System.out.println();
    
    
    /** Write configuration for database */
    System.out.print("Write name of the database (marauroa): ");
    String databasename="";
    try
      {
      databasename=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(databasename.equals(""))
      {
      System.out.println("Aborting: No database chosen");
      return;
      }

    System.out.println("Using \""+databasename+"\" as database name");
    System.out.println();

    System.out.print("Write name of the database host (localhost): ");
    String databasehost="";
    try
      {
      databasehost=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(databasehost.equals(""))
      {
      databasehost="localhost";
      }

    System.out.println("Using \""+databasehost+"\" as database host");
    System.out.println();

    System.out.print("Write name of the database user (marauroa_user): ");
    String databaseuser="";
    try
      {
      databaseuser=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(databaseuser.equals(""))
      {
      databaseuser="marauroa_user";
      }

    System.out.println("Using \""+databaseuser+"\" as database user");
    System.out.println();

    System.out.print("Write value of the database user password (marauroa_pass): ");
    String databasepassword="";
    try
      {
      databasepassword=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(databasepassword.equals(""))
      {
      databasepassword="marauroa_pass";
      }

    System.out.println("Using \""+databasepassword+"\" as database user password");
    System.out.println();
    System.out.println("In order to make efective these options please run:");
    System.out.println("# mysql");
    System.out.println("  create database "+databasename+";");
    System.out.println("  grant all on "+databasename+".* to "+databaseuser+"@localhost identified by '"+databasepassword+"';");
    System.out.println("  exit");   
    System.out.println();
    
    /** Choose port that will be used */    
    System.out.print("Write UDP port used by Marauroa (>1024): ");
    String udpport="";
    try
      {
      udpport=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(udpport.equals(""))
      {
      System.out.println("Aborting: No UDP port chosen");
      return;
      }

    System.out.println("Using \""+udpport+"\" as UDP port for Marauroa");
    System.out.println();

    /** Choose RP Content that will be used */    
    System.out.println("Marauroa is a server middleware to run multiplayer games. You need to"+
    "add a game to the system so that server can work. Actually Arianne has implemented several games:");
    System.out.println("- stendhal");
    System.out.println("- mapacman");
    System.out.println("- the1001");
    System.out.println("If you write your own game, just write its name here.");
    System.out.println ("You will be asked for more info.");
    System.out.println();
    
    System.out.print("Write name of the game server will run: ");
    String gamename="";
    try
      {
      gamename=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(gamename.equals(""))
      {
      System.out.println("Aborting: No UDP port chosen");
      return;
      }

    System.out.println("Using \""+gamename+"\" as game");
    System.out.println();
    
    String rp_RPWorldClass="";
    String rp_RPRuleProcessorClass="";
    
    if(gamename.equals("stendhal"))
      {      
      System.out.println("NOTE: Setting RPWorld and RPRuleProcessor for Stendhal");
      System.out.println("NOTE: Make sure Marauroa can find in CLASSPATH the folder games/stendhal/*");
      System.out.println("NOTE: Copy stendhal's games folder inside folder that contains marauroa.jar file");

      rp_RPWorldClass="games.stendhal.server.StendhalRPWorld";
      rp_RPRuleProcessorClass="games.stendhal.server.StendhalRPRuleProcessor";
      }
    else
      {
      System.out.println("Setting RPWorld and RPRuleProcessor for "+gamename);
      System.out.print("Write game's RPWorld class file  (games/stendhal/server/StendhalRPWorld.class): ");

      try
        {
        rp_RPWorldClass=input.readLine();
        }
      catch(IOException e)
        {
        System.exit(1);
        }
    
      if(udpport.equals(""))
        {
        System.out.println("Aborting: No RPWorld class choosen");
        return;
        }

      System.out.print("Write game's RPRuleProcessor class file  (games/stendhal/server/StendhalRPRuleProcessor.class): ");

      try
        {
        rp_RPRuleProcessorClass=input.readLine();
        }
      catch(IOException e)
        {
        System.exit(1);
        }
    
      if(udpport.equals(""))
        {
        System.out.println("Aborting: No RPRuleProcessor class choosen");
        return;
        }
      }
     
    System.out.println();
    

    /** Choose turn time that will be used */    
    System.out.print("Write turn time duration in milliseconds (200<time<1000)): ");
    String turntime="";
    try
      {
      turntime=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(udpport.equals(""))
      {      
      turntime="300";
      }

    System.out.println("Using turn of "+turntime+" milliseconds");
    System.out.println();


    /** Choose where logs and statistics are generated */    
    System.out.print("Write path for logs generation (./)): ");
    String logs_path="";
    try
      {
      logs_path=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(logs_path.equals(""))
      {      
      logs_path="./";
      }

    System.out.println("Using path \""+logs_path+"\" for log generation");
    System.out.println();

    System.out.print("Write path for statistics generation (./)): ");
    String statistics_path="";
    try
      {
      statistics_path=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(statistics_path.equals(""))
      {      
      statistics_path="./";
      }

    System.out.println("Using path \""+statistics_path+"\" for statistics generation");
    System.out.println();

    System.out.print("Write size for the RSA key of the server. Be aware that a key bigger than 1024 could be very long to create (512): ");
    String keySize="";
    try
      {
      keySize=input.readLine();
      }
    catch(IOException e)
      {
      System.exit(1);
      }
    
    if(keySize.equals("") || Integer.valueOf(keySize) <= 0)
      {      
      keySize="512";
      }

    System.out.println("Using key of " + keySize + " bits.");
    System.out.println("Please wait while the key is generated.");

    RSAKey key = RSAKey.generateKey(Integer.valueOf(keySize));
    
    System.out.println();


    System.out.println("COMPLETE---");
    System.out.println("Generating \""+filename+"\" file");
    try
      {
      PrintWriter output=new PrintWriter(new FileOutputStream(filename));
      output.println("### Configuration file for "+gamename);
      output.println("### Automatically generated by generateini");
      output.println();
      output.println("marauroa_DATABASE=JDBCPlayerDatabase");
      output.println();
      output.println("jdbc_url=jdbc:mysql://"+databasehost+"/"+databasename);
      output.println("jdbc_class=com.mysql.jdbc.Driver");
      output.println("jdbc_user="+databaseuser);
      output.println("jdbc_pwd="+databasepassword);
      output.println();
      output.println("marauroa_PORT="+udpport);
      output.println();
      output.println("rp_RPWorldClass="+rp_RPWorldClass);
      output.println("rp_RPRuleProcessorClass="+rp_RPRuleProcessorClass);
      output.println("rp_turnDuration="+turntime);
      output.println();
      output.println("server_typeGame="+gamename);
      output.println("server_name="+gamename+" Marauroa server");
      output.println("server_version=stable");
      output.println("server_contact=https://sourceforge.net/tracker/?atid=514826&group_id=66537&func=browse");
      output.println();
      output.println("server_stats_directory="+statistics_path);
      output.println("server_logs_directory="+logs_path);
      output.println("server_logs_allowed=");
      output.println("server_logs_rejected=");
      output.println();
      key.print(output);
      output.close();
      }
    catch(FileNotFoundException e)
      {
      e.printStackTrace();
      return;
      }    
    System.out.println("Generated.");
    }    
  }
