/* $Id: marauroad.java,v 1.35 2004/02/06 16:08:54 arianne_rpg Exp $ */
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
package marauroa;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import marauroa.game.*;

import the1001.objects.*;

/**
 * The launcher of the whole Marauroa Server.
 *
 */
public class marauroad extends Thread
  {
  private static PrintWriter out;
  private static marauroad marauroa;
  private static Date timestamp;
  private static SimpleDateFormat formatter;
	
  private marauroa.net.NetworkServerManager netMan;
  private marauroa.game.GameServerManager gameMan;
  
  static
    {
    out=null;
	timestamp=new Date();
	formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}
	
  private static void setArguments(String[] args)
    {
    int i=0;
    
    while(i!=args.length)
      {
      System.out.println(args[i]);
      if(args[i].equals("-c"))
        {
        Configuration.setConfigurationFile(args[i+1]);
        }
      else if(args[i].equals("-l"))
        {
        try
          {
          String time=String.valueOf(new Date().getTime());
          out=new PrintWriter(new FileOutputStream("server_log_"+time+".txt"));
          }
        catch(FileNotFoundException e)
          {
          marauroad.trace("marauroad::setArguments","X",e.getMessage());
          marauroad.trace("marauroad::setArguments","!","ABORT: marauroad can't open log file");
          System.exit(-1);
          }
        }
      else if(args[i].equals("-h"))
        {
        // TODO: Write help
        }
        
      ++i;
      }
    }
	
  private void setTestDatabase()
    {
    marauroad.trace("marauroad::setTestDatabase",">");
    
    try
      {
      PlayerDatabase playerDatabase=PlayerDatabaseFactory.getDatabase();
      
      if(playerDatabase.hasPlayer("Test Player"))
        {
        playerDatabase.removePlayer("Test Player");
        }

      if(playerDatabase.hasPlayer("Another Test Player"))
        {
        playerDatabase.removePlayer("Another Test Player");
        }

      playerDatabase.addPlayer("Test Player","Test Password");
      playerDatabase.addPlayer("Another Test Player","Test Password");

      RPObject SonGoku=new RPObject();
      SonGoku.put("object_id","1");
      SonGoku.put("name","Son Goku");
      SonGoku.put("type","character");
      SonGoku.addSlot(new RPSlot("gladiators"));
      SonGoku.getSlot("gladiators").add(new Gladiator(new RPObject.ID(4)));
      playerDatabase.addCharacter("Test Player", "Son Goku",SonGoku);

      RPObject MrBean=new RPObject();
      MrBean.put("object_id","2");
      MrBean.put("name","Mr Bean");
      MrBean.put("type","character");
      MrBean.addSlot(new RPSlot("gladiators"));
      MrBean.getSlot("gladiators").add(new Gladiator(new RPObject.ID(5)));
      playerDatabase.addCharacter("Another Test Player", "MrBean",MrBean);
      
      RPObject DrCoreDump=new RPObject();
      DrCoreDump.put("object_id","3");
      DrCoreDump.put("name","Dr CoreDump");
      DrCoreDump.put("type","character");
      DrCoreDump.addSlot(new RPSlot("gladiators"));
      DrCoreDump.getSlot("gladiators").add(new Gladiator(new RPObject.ID(6)));
      playerDatabase.addCharacter("Test Player", "Dr CoreDump",DrCoreDump);
      }
    catch(Exception e)
      {
      marauroad.trace("marauroad::setTestDatabase","X",e.getMessage());
      marauroad.trace("marauroad::setTestDatabase","!","ABORT: marauroad can't allocate database");
      System.exit(-1);
      }
    finally
      {
      marauroad.trace("marauroad::setTestDatabase","<");
      }
    }
    
  public static void main (String[] args)
    {
    println("Marauroa           - An open source MORPG Framework -");
    println("Running on version @version@");
    println("(C) 2003 Miguel Angel Blanch Lardin");
    println();
    println("This program is free software; you can redistribute it and/or modify");
    println("it under the terms of the GNU General Public License as published by");
    println("the Free Software Foundation; either version 2 of the License, or");
    println("(at your option) any later version.");
    println();
    println("This program is distributed in the hope that it will be useful,");
    println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
    println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
    println("GNU General Public License for more details.");
    println();
    println("You should have received a copy of the GNU General Public License");
    println("along with this program; if not, write to the Free Software");
    println("Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");

    marauroad.trace("marauroad::main",">");
    marauroad.setArguments(args);
    marauroad.getMarauroa().start();
    marauroad.trace("marauroad::main","<");
    }
 
  public synchronized void run()
    {
    marauroad.trace("marauroad::run",">");
    boolean finish=false;
    marauroad instance=marauroad.getMarauroa();
    //instance.setTestDatabase();

    instance.init();

    while(!finish)
      {
      try
        {
        Statistics.print();
        if(new File("server_log.txt").length()>67108864)
          {
          try
            {
            out.close();
            String time=String.valueOf(new Date().getTime());
            out=new PrintWriter(new FileOutputStream("server_log_"+time+".txt"));
            }
          catch(FileNotFoundException e)
            {
            marauroad.trace("marauroad::run","X",e.getMessage());
            marauroad.trace("marauroad::run","!","ABORT: marauroad can't open log file");
            System.exit(-1);
            }
          }
          
        wait(5000);
        }
      catch(InterruptedException e)
        {
        finish=true;
        }
      }
      
    instance.finish();
    marauroad.trace("marauroad::run","<");
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
    marauroad.trace("marauroad::init",">");
    try
      {
      netMan=new marauroa.net.NetworkServerManager();
      gameMan= new marauroa.game.GameServerManager(netMan);
      }
    catch(java.net.SocketException e)
      {
      marauroad.trace("marauroad::init","X",e.getMessage());
      marauroad.trace("marauroad::init","!","ABORT: marauroad can't allocate server socket");
      System.exit(-1);
      }
    finally
      {
      marauroad.trace("marauroad::init","<");
      }
    }
    
  public void finish()
    {
    marauroad.trace("marauroad::finish",">");
    netMan.finish();
    gameMan.finish();
    marauroad.trace("marauroad::finish","<");
    }
  
  private void message(String text)
    {
    if(out!=null)
      {
      out.println(text);
      out.flush();
      }
      
    System.out.println(text);
    }
  
  public static void println(String text)
    {
    getMarauroa().message(text);
    }

  public static void println()
    {
    getMarauroa().message("");
    }
    
  private static boolean filter(String word)
    {
    for(int j=0;j<rejected.length;++j)
      {
      if(word.indexOf(rejected[j])!=-1)
        {
        return false;
        }
      }
      
    for(int i=0;i<allowed.length;++i)
      {
      if(word.indexOf(allowed[i])!=-1)
        {
        return true;
        }
      
      if(allowed[i].equals("*"))
        {
        return true;
        }
      }
    
    return false;
    }
  
  private static String[] allowed={"*","RPCode","the1001"};
  private static String[] rejected={"PlayerEntryContainer"};
  
  public static void trace(String module,String event)
    {
    trace(module,event,"");
    }
    
  public static void trace(String module,String event,String text)
    {
    if(filter(module))
      {
      timestamp.setTime(System.currentTimeMillis());
	  String ts = formatter.format(timestamp);
      getMarauroa().message(ts+"\t"+event+"\t"+module+"\t"+text);
      }
    }
    
  public static void report(String text)
    {
    getMarauroa().message(new java.sql.Timestamp(new java.util.Date().getTime())+": "+text);
    }
  }
