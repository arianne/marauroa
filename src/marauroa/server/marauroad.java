/* $Id: marauroad.java,v 1.1 2005/01/23 21:00:45 arianne_rpg Exp $ */
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

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import marauroa.common.*;
import marauroa.server.game.*;

/** the launcher of the whole Marauroa Server. */
public class marauroad extends Thread
  {
  final private static boolean DEBUG=true;
  final private static String VERSION="1.00";
  
  private static marauroad marauroa;

  private marauroa.server.net.NetworkServerManager netMan;
  private marauroa.server.game.GameServerManager gameMan;
  
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
          Logger.println("ERROR: Can't find configuraciont file: "+args[i+1]);
          Logger.println("Server must abort.");
          System.exit(0);
          }
        }
      else if(args[i].equals("-l"))
        {
        Logger.initialize("logs","server_log_");
        }
      else if(args[i].equals("-h"))
        {
        Logger.println("Marauroa - an open source multiplayer online framework for game development -");
        Logger.println("Running on version "+VERSION);
        Logger.println("(C) 2003-2004 Miguel Angel Blanch Lardin");
        Logger.println();
        Logger.println("usage: [-c gamefile] [-l]");        
        Logger.println("\t-c: to choose a configuration file different of marauroa.ini or to use a");
        Logger.println("\t    different location to the file.");
        Logger.println("\t-l: to make the server log the output into a file");
        Logger.println("\t-h: print this help message");        
        System.exit(0);
        }
      ++i;
      }
    }
    
  public static void main (String[] args)
    {
    Logger.println("Marauroa - arianne's open source multiplayer online framework for game development -");
    Logger.println("Running on version "+VERSION);
    Logger.println("(C) 1999-2004 Miguel Angel Blanch Lardin");
    Logger.println();
    Logger.println("This program is free software; you can redistribute it and/or modify");
    Logger.println("it under the terms of the GNU General Public License as published by");
    Logger.println("the Free Software Foundation; either version 2 of the License, or");
    Logger.println("(at your option) any later version.");
    Logger.println();
    Logger.println("This program is distributed in the hope that it will be useful,");
    Logger.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
    Logger.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
    Logger.println("GNU General Public License for more details.");
    Logger.println();
    Logger.println("You should have received a copy of the GNU General Public License");
    Logger.println("along with this program; if not, write to the Free Software");
    Logger.println("Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
    
    Logger.trace("marauroad::main",">");
    marauroad.setArguments(args);
    marauroad.getMarauroa().start();
    Logger.trace("marauroad::main","<");
    }
 
  public synchronized void run()
    {
    Logger.trace("marauroad::run",">");

    boolean finish=false;
    marauroad instance=marauroad.getMarauroa();

    instance.init();
    while(!finish)
      {
      try
        {
        // TODO: Statistics here         
        Statistics.getStatistics().print();
        wait(30000);
        }
      catch(InterruptedException e)
        {
        finish=true;
        }
      }
      
    instance.finish();
    Logger.trace("marauroad::run","<");
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
  
  public void init()
    {
    Logger.trace("marauroad::init",">");
    try
      {
      netMan=new marauroa.server.net.NetworkServerManager();
      gameMan= new marauroa.server.game.GameServerManager(netMan);
      
      Runtime.getRuntime().addShutdownHook(new Thread()
        {
        public void run()
          {
          Logger.trace("marauroad::init","!","User requesting shutdown");
          finish();
          Logger.trace("marauroad::init","!","Shutdown completed. See you later");
          }
        });
      }
    catch(Exception e)
      {
      Logger.thrown("marauroad::init","X",e);
      System.exit(-1);
      }
    finally
      {
      Logger.trace("marauroad::init","<");
      }
    }
    
  public void finish()
    {
    Logger.trace("marauroad::finish",">");
    netMan.finish();
    gameMan.finish();
    Logger.trace("marauroad::finish","<");
    }
  }
