/* $Id: Statistics.java,v 1.24 2004/05/07 17:16:58 arianne_rpg Exp $ */
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

import marauroa.game.*;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class Statistics
  {
  public static class GatheredVariables
    {
    public long bytesRecv=0;
    public long bytesSend=0;
    public long bytesSavedByCompression=0;
    
    public long messagesRecv=0;
    public long messagesSend=0;
    public long messagesIncorrect=0;
    
    public long playersLogin=0;
    public long playersInvalidLogin=0;
    public long playersLogout=0;
    public long playersTimeout=0;
    public long playersOnline=0;

    public long objectsNow=0;
    public long actionsAdded=0;
    public long actionsInvalid=0;
    
    public void print(PrintWriter out, double diff)
      {
      out.println("Bytes RECV: "+String.valueOf(bytesRecv));
      out.println("Bytes RECV (avg hour): "+String.valueOf((int)(bytesRecv/(diff/3600))));
      out.println("Bytes SEND: "+String.valueOf(bytesSend));
      out.println("Bytes saved by compression: "+String.valueOf(bytesSavedByCompression));
      out.println("Bytes SEND (avg hour): "+String.valueOf((int)(bytesSend/(diff/3600))));
      out.println("Messages RECV: "+String.valueOf(messagesRecv));
      out.println("Messages RECV (avg hour): "+String.valueOf((int)(messagesRecv/(diff/3600))));
      out.println("Messages SEND: "+String.valueOf(messagesSend));
      out.println("Messages SEND (avg hour): "+String.valueOf((int)(messagesSend/(diff/3600))));
      out.println("Messages INCORRECT: "+String.valueOf(messagesIncorrect));
      out.println();
      out.println("Players LOGIN: "+String.valueOf(playersLogin));
      out.println("Players LOGIN INVALID: "+String.valueOf(playersInvalidLogin));
      out.println("Players LOGOUT: "+String.valueOf(playersLogout));
      out.println("Players TIMEDOUT: "+String.valueOf(playersTimeout));
      out.println("Players ONLINE: "+String.valueOf(playersOnline));
      out.println();
      out.println("Objects ONLINE: "+String.valueOf(objectsNow));
      out.println("Actions ADDED: "+String.valueOf(actionsAdded));
      out.println("Actions INVALID: "+String.valueOf(actionsInvalid));
      }

    public void add(GatheredVariables var)
      {
      bytesRecv=var.bytesRecv+bytesRecv;
      bytesSend=var.bytesSend+bytesSend;
      bytesSavedByCompression=var.bytesSavedByCompression+bytesSavedByCompression;
      messagesRecv=var.messagesRecv+messagesRecv;
      messagesSend=var.messagesSend+messagesSend;
      messagesIncorrect=var.messagesIncorrect+messagesIncorrect;
      playersLogin=var.playersLogin+playersLogin;
      playersInvalidLogin=var.playersInvalidLogin+playersInvalidLogin;
      playersTimeout=var.playersTimeout+playersTimeout;
      playersLogout=var.playersLogout+playersLogout;
      playersOnline=Math.round((var.playersOnline+playersOnline)/2.0f);
      objectsNow=Math.round((var.objectsNow+objectsNow)/2.0f);
      actionsAdded=var.actionsAdded+actionsAdded;
      actionsInvalid=var.actionsInvalid+actionsInvalid;
      }
    }

  private Date startTime;

  private GatheredVariables nowVar;
  private GatheredVariables allTimeVar;
  private GatheredVariables meanMinuteVar;
  
  private PrintWriter eventfile;
  private Date timestamp;
  private Date lastStatisticsEventAdded;
  private SimpleDateFormat formatter;
    
  private Statistics()
    {
    timestamp=new Date();
    formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    startTime=new Date();
    
    lastStatisticsEventAdded=new Date();
    
    nowVar=new GatheredVariables();
    allTimeVar=new GatheredVariables();
    meanMinuteVar=new GatheredVariables();
    
    try
      {
      eventfile=new PrintWriter(new FileOutputStream("logs/"+"server_events.txt",true));
      }
    catch(Exception e)
      {
      marauroad.thrown("Statistics::static","!",e);
      System.exit(-1);
      }
    }
  private static Statistics stats;
  public static Statistics getStatistics()
    {
    if(stats==null)
      {
      stats=new Statistics();
      }
    return stats;
    }
 
  public void addEvent(String event,int session_id,String text)
    {
    marauroad.trace("Statistics::addEvent",">");
    timestamp.setTime(System.currentTimeMillis());

    String ts = formatter.format(timestamp);
    
    eventfile.println(ts+"\t/"+String.valueOf(session_id)+"\t"+event+"\t"+text);
    eventfile.flush();
    marauroad.trace("Statistics::addEvent","<");
    }
  
  public void addBytesRecv(long bytes)
    {
    nowVar.bytesRecv+=bytes;
    }
  
  public void addBytesSend(long bytes)
    {
    nowVar.bytesSend+=bytes;
    }
  
  public void addBytesSaved(long bytes)
    {
    nowVar.bytesSavedByCompression+=bytes;
    }
  
  public void addMessageRecv()
    {
    ++nowVar.messagesRecv;
    }
  
  public void addMessageSend()
    {
    ++nowVar.messagesSend;
    }
  
  public void addMessageIncorrect()
    {
    ++nowVar.messagesIncorrect;
    }
  
  public void addPlayerLogin(String username, int id)
    {
    addEvent("login OK",id,"username="+username);
    ++nowVar.playersLogin;
    }
  
  public void addPlayerLogout(String username, int id)
    {
    addEvent("logout",id,"username="+username);
    ++nowVar.playersLogout;
    }
  
  public void addPlayerInvalidLogin(String username)
    {
    addEvent("login FAIL",0,"username="+username);
    ++nowVar.playersInvalidLogin;
    }

  public void addPlayerTimeout(String username, int id)
    {
    addEvent("timeout",id,"username="+username);
    ++nowVar.playersTimeout;
    }
  
  public void setOnlinePlayers(long online)
    {
    nowVar.playersOnline=online;
    }
  
  public void setObjectsNow(long now)
    {
    nowVar.objectsNow=now;
    }

  public void addActionsAdded(String action, int id)
    {
    addEvent("action",id,"type="+action);
    ++nowVar.actionsAdded;
    }
  
  public void addActionsAdded(String action, int id, String text)
    {
    addEvent("action",id,"type="+action+"&extra=\""+text+"\"");
    ++nowVar.actionsAdded;
    }
  
  public void addActionsInvalid()
    {
    ++nowVar.actionsInvalid;
    }
  
  public GatheredVariables getVariables()
    {
    return(nowVar);
    }
  
  public void print()
    {
    try
      {
      Configuration conf=Configuration.getConfiguration();
      String webfolder=conf.get("server_stats_directory");
      PrintWriter out=new PrintWriter(new FileOutputStream(webfolder+"server_stats.txt"));
      Date actualTime=new Date();
      double diff=(actualTime.getTime()-startTime.getTime())/1000;
      
      allTimeVar.add(nowVar);
      meanMinuteVar.add(nowVar);
      
      if((actualTime.getTime()-lastStatisticsEventAdded.getTime())>60000)
        {
        lastStatisticsEventAdded=new Date();
        
        JDBCPlayerDatabase database=(JDBCPlayerDatabase)JDBCPlayerDatabase.getDatabase();
        Transaction transaction=database.getTransaction();
        
        database.addStatisticsEvent(transaction,meanMinuteVar);
        meanMinuteVar=new GatheredVariables();
        }
      
      out.println("-- Statistics ------");
      out.println("Uptime: "+String.valueOf(diff));
      out.println();
      allTimeVar.print(out,diff);
      out.println("-- Statistics ------");
      out.close();
      out=new PrintWriter(new FileOutputStream(webfolder+"server_up.txt"));
      out.println(actualTime.getTime()/1000);
      out.close();
      
      nowVar=new GatheredVariables();
      }
    catch(Exception e)
      {
      }
    }
  }
