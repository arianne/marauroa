package marauroa;

import marauroa.game.*;

/**
 * The launcher of the whole Marauroa Server.
 *
 */
public class marauroad extends Thread
  {
  private static marauroad marauroa;
  private marauroa.net.NetworkServerManager netMan;
  private marauroa.game.GameServerManager gameMan;
  
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
      playerDatabase.addCharacter("Test Player", "Son Goku",SonGoku);

      RPObject MrBean=new RPObject();
      MrBean.put("object_id","2");
      MrBean.put("name","Mr Bean");
      playerDatabase.addCharacter("Another Test Player", "MrBean",MrBean);
      
      RPObject DrCoreDump=new RPObject();
      DrCoreDump.put("object_id","3");
      DrCoreDump.put("name","Dr CoreDump");
      playerDatabase.addCharacter("Test Player", "Dr CoreDump",DrCoreDump);
      }
    catch(Exception e)
      {
      marauroad.trace("marauroad::setTestDatabase","X",e.getMessage());
      marauroad.trace("PlayerEntryContainer","!","ABORT: marauroad can't allocate database");
      System.exit(-1);
      }
    finally
      {
      marauroad.trace("marauroad::setTestDatabase","<");
      }
    }
    
  public static void main (String[] args)
    {
    marauroad.trace("marauroad::main",">");
    marauroad.getMarauroa().start();
    marauroad.trace("marauroad::main","<");
  }
 
  public synchronized void run()
    {
    marauroad.trace("marauroad::run",">");
    boolean finish=false;
    println("Marauroa server       - An open source MMORPG Server -");
    println("Running on [@version@]");
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
    
    marauroad instance=marauroad.getMarauroa();
    instance.setTestDatabase();

    instance.init();

    while(!finish)
      {
      try
        {
        wait();
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
      marauroad.trace("PlayerEntryContainer","!","ABORT: marauroad can't allocate server socket");
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
    
  public static void trace(String module,String event)
    {
    getMarauroa().message(new java.sql.Timestamp(new java.util.Date().getTime())+"\t"+event+"\t"+
       module);
    }
    
  public static void trace(String module,String event,String text)
    {
    getMarauroa().message(new java.sql.Timestamp(new java.util.Date().getTime())+"\t"+event+"\t"+
       module+"\t"+text);
    }
    
  public static void report(String text)
    {
    getMarauroa().message(new java.sql.Timestamp(new java.util.Date().getTime())+": "+text);
    }
  }
