/* $Id: Logger.java,v 1.11 2005/08/02 19:49:31 mtotz Exp $ */
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
package marauroa.common;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

/**
 * a logger
 * @deprecated use Log4J instead
 */
public class Logger
  {
  private static String[] allowed={};
  private static String[] rejected={};
  
  private static Logger logger;
  
  private PrintWriter out;
  private Date timestamp;
  private SimpleDateFormat formatter;
  
  private Logger()
    {
    timestamp=new Date();
    formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
 
  public static void setAllowed(String[] allowed)  
    {
    Logger.allowed=allowed;
    }
    
  public static void setRejected(String[] rejected)  
    {
    Logger.rejected=rejected;
    }

  public static void initialize()
    {    
    getLogger().out=null;
    }
    
  public static void initialize(String base, String prefix)
    {    
    try
      {
      String time=String.valueOf(new Date().getTime());

      new File(base).mkdir();
      getLogger().out=new PrintWriter(new FileOutputStream(base+"/"+prefix+time+".txt"));
      }
    catch(FileNotFoundException e)
      {
      Logger.thrown("Logger::initialize","X",e);
      Logger.trace("Logger::initialize","!","ABORT: Logger can't open log file");
      System.exit(-1);
      }
    }

  public static Logger getLogger()
    {
    if(logger==null)
      {
      logger=new Logger();
      }
      
    return logger;
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
    getLogger().message(text);
    }

  public static void println()
    {
    getLogger().message("");
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

  public static void trace(String module,String event)
    {
    trace(module,event,"");
    }
    
  public static void trace(String module,String event,String text)
    {
    if(filter(module) || event.equals("X") || event.equals("!"))
      {
      getLogger().timestamp.setTime(System.currentTimeMillis());

      String ts = getLogger().formatter.format(getLogger().timestamp);
      long threadid=Thread.currentThread().getId();

      getLogger().message(ts+"\t("+threadid+")\t"+event+"\t"+module+"\t"+text);
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
  }
