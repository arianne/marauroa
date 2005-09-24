/* $Id: marauroad.java,v 1.30 2005/09/24 17:51:57 arianne_rpg Exp $ */
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

// marauroa stuff
import java.io.FileNotFoundException;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.crypto.RSAKey;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.RPServerManager;
import marauroa.server.game.Statistics;
// java misc
import java.text.SimpleDateFormat;
import java.math.BigInteger;
//java management stuff
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import marauroa.server.net.NetworkServerManager;



/** the launcher of the whole Marauroa Server. */
public class marauroad extends Thread
  {
  /** the logger instance. */
  private static final org.apache.log4j.Logger logger = Log4J.getLogger(marauroad.class);

  final private static boolean DEBUG=true;
  final private static String VERSION="1.10";
  
  private static marauroad marauroa;

  private NetworkServerManager netMan;
  private GameServerManager gameMan;
  private RPServerManager rpMan;

  private static void setArguments(String[] args)
    {
    int i=0;
    
    while(i!=args.length)
      {
      if(args[i].equals("-c"))
        {
        Configuration.setConfigurationFile(args[i+1]);
        try
          {
          Configuration.getConfiguration();          
          }
        catch(Exception e)
          {
          logger.fatal("Can't find configuration file: "+args[i+1],e);
          System.exit(1);
          }
        }
      else if(args[i].equals("-h"))
        {
        System.out.println("Marauroa - an open source multiplayer online framework for game development -");
        System.out.println("Running on version "+VERSION);
        System.out.println("(C) 1999-2005 Miguel Angel Blanch Lardin");
        System.out.println();
        System.out.println("usage: [-c gamefile] [-l]");        
        System.out.println("\t-c: to choose a configuration file different of marauroa.ini or to use a");
        System.out.println("\t    different location to the file.");
        System.out.println("\t-h: print this help message");        
        System.exit(0);
        }
      ++i;
      }
    }
  

  public static void main (String[] args)
    {
    System.out.println("Marauroa - arianne's open source multiplayer online framework for game development -");
    System.out.println("Running on version "+VERSION);
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
    
    // Initialize Loggging
    Log4J.init();
    marauroad.setArguments(args);

    marauroad.getMarauroa().start();
    }
 
  public synchronized void run()
    {
    logger.debug("marauroad thread started");

    try 
      {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      // Unique identification of MBeans
      Statistics statBean = Statistics.getStatistics();
      // Uniquely identify the MBeans and register them with the platform MBeanServer 
      ObjectName statName = new ObjectName("marauroad:name=Statistics");
      mbs.registerMBean(statBean, statName);
      logger.debug("Statistics bean registered.");
      } 
    catch(Exception e) 
      {
      logger.error("cannot register statistics bean, continuing anyway.",e);
      }


    boolean finish=false;
    marauroad instance=marauroad.getMarauroa();

    if (!instance.init())
    {
      // initialize failed
      System.exit(-1);
    }

    logger.info("marauroa is up and running...");
    while(!finish)
      {
      try
        {
        Statistics.getStatistics().print();
        wait(30000);
        }
      catch(InterruptedException e)
        {
        finish=true;
        }
      }
      
    instance.finish();
    logger.debug("exiting marauroad thread");
    }

  private marauroad()
    {
    super("marauroad");
    }
    
  public static marauroad getMarauroa()
    {
    if(marauroa==null)
      {
      marauroa=new marauroad();
      }
      
    return marauroa;
    }
  
  /** initializes the game. returns true when all is ok, else false (this may terminate the server). */
  public boolean init()
    {
    logger.debug("staring initialize");
    try
      {
      netMan=new marauroa.server.net.NetworkServerManager();
      }
    catch(Exception e)
      {
      logger.fatal(
              "Marauroa can't create NetworkServerManager.\n"+
              "Reasons:\n"+
              "- You are already running a copy of Marauroa on the same UDP port\n"+
              "- You haven't specified a valid configuration file\n"+
              "- You haven't create database\n"+
              "- You have invalid username and password to connnect to database\n",e);
      return false;
      }
      
    try
      {
      rpMan = new RPServerManager(netMan);
      }
    catch(Exception e)
      {
      logger.fatal(
              "Marauroa can't create RPServerManager.\n"+
              "Reasons:\n"+
              "- You haven't specified a valid configuration file\n"+
              "- You haven't correctly filled the values related to game configuration. Use generateini application to create a valid configuration file.\n"+
              "- There may be an error in the Game startup method.\n",e);
      return false;
      }

    try
      {
      RSAKey key = new RSAKey(new BigInteger(Configuration.getConfiguration().get("n")),
      new BigInteger(Configuration.getConfiguration().get("d")),
			new BigInteger(Configuration.getConfiguration().get("e")));
      gameMan = new GameServerManager(key,netMan,rpMan);
      }
    catch(Exception e)
      {
      logger.fatal("Marauroa can't create GameServerManager.\n"+
      "Reasons:\n"+
      "- You haven't specified a valid configuration file\n"+
      "- You haven't correctly filled the values related to server information configuration. Use generateini application to create a valid configuration file.\n"
      ,e);
      return false;
      }
      
    Runtime.getRuntime().addShutdownHook(new Thread()
      {
      public void run()
        {
        // Note: Log4J ist shutdown already at this point
        System.out.println("User requesting shutdown");
        finish();
        System.out.println("Shutdown completed. See you later");
        }
      });

    logger.debug("initialize finished");
    return true;
    }
    
  public void finish()
    {
    netMan.finish();
    gameMan.finish();
    }
  }
