/* $Id: marauroad.java,v 1.87 2004/05/16 10:37:41 arianne_rpg Exp $ */
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

/**
 * The launcher of the whole Marauroa Server.
 */
public class marauroad extends Thread
  {
  final private static boolean DEBUG=false;
  final private static String VERSION="0.34";
  
  private static PrintWriter out;
  private static marauroad marauroa;
  private static Date timestamp;
  private static SimpleDateFormat formatter;
  private marauroa.net.NetworkServerManager netMan;
  private marauroa.game.GameServerManager gameMan;
  
  private static String filename="";
  
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
      if(args[i].equals("-c"))
        {
        Configuration.setConfigurationFile(args[i+1]);
        }
      else if(args[i].equals("-l"))
        {
        try
          {
          String time=String.valueOf(new Date().getTime());

          new File("logs").mkdir();
          out=new PrintWriter(new FileOutputStream("logs/"+"server_log_"+time+".txt"));
          filename="logs/"+"server_log_"+time+".txt";
          }
        catch(FileNotFoundException e)
          {
          marauroad.thrown("marauroad::setArguments","X",e);
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
    
  public static void main (String[] args)
    {
    println("Marauroa           - An open source MORPG Framework -");
    println("Running on version "+VERSION);
    println("(C) 2003-2004 Miguel Angel Blanch Lardin");
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

    /** TODO: Set database for MemoryDatabase, nice option would be to load it from a XML file */

    instance.init();
    while(!finish)
      {
      try
        {
        Statistics.getStatistics().print();
        if(new File(filename).length()>67108864)
          {
          try
            {
            out.close();

            String time=String.valueOf(new Date().getTime());

            out=new PrintWriter(new FileOutputStream("logs/"+"server_log_"+time+".txt"));
            filename="logs/"+"server_log_"+time+".txt";
            }
          catch(FileNotFoundException e)
            {
            marauroad.thrown("marauroad::run","X",e);
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
      
      Runtime.getRuntime().addShutdownHook(new Thread()
        {
        public void run()
          {
          marauroad.trace("marauroad::init","!","User requesting shutdown");
          finish();
          marauroad.trace("marauroad::init","!","Shutdown completed. See you later");
          }
        });
      }
    catch(java.net.SocketException e)
      {
      marauroad.thrown("marauroad::init","X",e);
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

  private static String[] allowed;
  private static String[] rejected;
  
  static
    {
    if(DEBUG)
      {
      // Debug setting
      String[] _allowed={"*"};
      allowed=_allowed;
      String[] _rejected={};
      rejected=_rejected;
      }
    else
      {
      // Production setting
      String[] _allowed={"RPServerManager::run","RPCode"};
      allowed=_allowed;
      String[] _rejected={};
      rejected=_rejected;
      }
    }

  public static void trace(String module,String event)
    {
    trace(module,event,"");
    }
    
  public static void trace(String module,String event,String text)
    {
    if(filter(module) || event.equals("X") || event.equals("!"))
      {
      timestamp.setTime(System.currentTimeMillis());

      String ts = formatter.format(timestamp);

      getMarauroa().message(ts+"\t"+event+"\t"+module+"\t"+text);
      }
    }
  
  public static void thrown(String module, String event, Throwable exception)
    {
    StringBuffer sb=new StringBuffer("Exception stackTrace:\n");
    StackTraceElement[] ste=exception.getStackTrace();
    for(int i=0;i<ste.length;++i)
      {
      sb.append("  "+ste[i].toString()+"\n");
      }    
    
    trace(module,"X",exception.getMessage());
    trace(module,"X",sb.toString());
    }
    
  public static boolean loggable(String module,String event)
    {
    boolean result=false;
    
    if(filter(module) || event.equals("X") || event.equals("!"))
      {
      result=true;
      }
    
    return result;
    }
    
  public static void report(String text)
    {
    getMarauroa().message(new java.sql.Timestamp(new java.util.Date().getTime())+": "+text);
    }
  }
