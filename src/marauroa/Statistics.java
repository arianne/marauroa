/* $Id: Statistics.java,v 1.9 2004/02/16 15:27:28 arianne_rpg Exp $ */
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

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class Statistics
  {
  static class GatheredVariables
    {
    public long bytesRecv=0;
    public long bytesSend=0;
    public long messagesRecv=0;
    public long messagesSend=0;
    public long messagesIncorrect=0;
    
    public long playersLogin=0;
    public long playersInvalidLogin=0;
    public long playersLogout=0;
    public long playersOnline=0;
    
    public long objectsAdded=0;
    public long objectsRemoved=0;
    public long objectsNow=0;
    public long actionsAdded=0;
    public long actionsInvalid=0;
    
    public void print(PrintWriter out, double diff)
      {
      out.println("Bytes RECV: "+String.valueOf(bytesRecv));
      out.println("Bytes RECV (avg secs): "+String.valueOf((int)(bytesRecv/diff)));
      out.println("Bytes SEND: "+String.valueOf(bytesSend));
      out.println("Bytes SEND (avg secs): "+String.valueOf((int)(bytesSend/diff)));
      out.println("Messages RECV: "+String.valueOf(messagesRecv));
      out.println("Messages RECV (avg secs): "+String.valueOf((int)(messagesRecv/diff)));
      out.println("Messages SEND: "+String.valueOf(messagesSend));
      out.println("Messages SEND (avg secs): "+String.valueOf((int)(messagesSend/diff)));
      out.println("Messages INCORRECT: "+String.valueOf(messagesIncorrect));
      out.println();
      out.println("Players LOGIN: "+String.valueOf(playersLogin));
      out.println("Players LOGIN INVALID: "+String.valueOf(playersInvalidLogin));
      out.println("Players LOGOUT: "+String.valueOf(playersLogout));
      out.println("Players TIMEDOUT: "+String.valueOf(playersLogin-playersLogout-playersOnline));
      out.println("Players ONLINE: "+String.valueOf(playersOnline));
      out.println();
      out.println("Objects ADDED: "+String.valueOf(objectsAdded));
      out.println("Objects REMOVED: "+String.valueOf(objectsRemoved));
      out.println("Objects ONLINE: "+String.valueOf(objectsNow));
      out.println("Actions ADDED: "+String.valueOf(actionsAdded));
      out.println("Actions INVALID: "+String.valueOf(actionsInvalid));
      }
    
    public void avg(GatheredVariables var)
      {
      bytesRecv=(var.bytesRecv+bytesRecv)/2;
      bytesSend=(var.bytesSend+bytesSend)/2;
      messagesRecv=(var.messagesRecv+messagesRecv)/2;
      messagesSend=(var.messagesSend+messagesSend)/2;
      messagesIncorrect=(var.messagesIncorrect+messagesIncorrect)/2;
      
      playersLogin=(var.playersLogin+playersLogin)/2;
      playersInvalidLogin=(var.playersInvalidLogin+playersInvalidLogin)/2;
      playersLogout=(var.playersLogout+playersLogout)/2;
      playersOnline=(var.playersOnline+playersOnline)/2;
      
      objectsAdded=(var.objectsAdded+objectsAdded)/2;
      objectsRemoved=(var.objectsRemoved+objectsRemoved)/2;
      objectsNow=(var.objectsNow+objectsNow)/2;
      actionsAdded=(var.actionsAdded+actionsAdded)/2;
      actionsInvalid=(var.actionsInvalid+actionsInvalid)/2;
      }
    }
  
  private static Date startTime;  
  private static GatheredVariables nowVar;
  private static PrintWriter eventfile;

  private static Date timestamp;
  private static SimpleDateFormat formatter;
    
  static
    {
    timestamp=new Date();
    formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    startTime=new Date();
    nowVar=new GatheredVariables();
    try
      {
      eventfile=new PrintWriter(new FileOutputStream("logs/"+"server_events.txt"));
      }
    catch(Exception e)
      {
      marauroad.trace("Statistics::static","!",e.getMessage());
      System.exit(1);      
      }
    }

  public static void addEvent(String event,int session_id,String text)
    {
    marauroad.trace("Statistics::addEvent",">");
    timestamp.setTime(System.currentTimeMillis());
    String ts = formatter.format(timestamp);
    
    eventfile.println(ts+"\t"+event+"\t"+String.valueOf(session_id)+"\t"+text);
    eventfile.flush();
    marauroad.trace("Statistics::addEvent","<");
    }
  
  public static void addBytesRecv(long bytes)
    {
    nowVar.bytesRecv+=bytes;
    }
  
  public static void addBytesSend(long bytes)
    {
    nowVar.bytesSend+=bytes;
    }
  
  public static void addMessageRecv()
    {
    ++nowVar.messagesRecv;
    }
  
  public static void addMessageSend()
    {
    ++nowVar.messagesSend;
    }
  
  public static void addMessageIncorrect()
    {
    ++nowVar.messagesIncorrect;
    }
  
  public static void addPlayerLogin(String username, int id)
    {
    addEvent("login OK",id,username);
    ++nowVar.playersLogin;
    }
  
  public static void addPlayerLogout(String username, int id)
    {
    addEvent("logout",id,username);
    ++nowVar.playersLogout;
    }
  
  public static void addPlayerInvalidLogin(String username)
    {
    addEvent("login FAIL",0,username);
    ++nowVar.playersInvalidLogin;
    }
  
  public static void setOnlinePlayers(long online)
    {
    nowVar.playersOnline=online;
    }
  
  public static void addObjectAdded()
    {
    ++nowVar.objectsAdded;
    }
  
  public static void addObjectRemoved()
    {
    ++nowVar.objectsRemoved;
    }
  
  public static void setObjectsNow(long now)
    {
    nowVar.objectsNow=now;
    }
  
  public static void addActionsAdded(String action, int id)
    {
    addEvent("action",id,action);
    ++nowVar.actionsAdded;
    }
  
  public static void addActionsInvalid()
    {
    ++nowVar.actionsInvalid;
    }
  
  public static GatheredVariables getVariables()
    {
    return(nowVar);
    }
  
  public static void print()
    {
    
    try
      {
      Configuration conf=Configuration.getConfiguration();
      String webfolder=conf.get("server_webdirectory");
        
      PrintWriter out=new PrintWriter(new FileOutputStream(webfolder+"server_stats.txt"));
      
      Date actualTime=new Date();
      double diff=(actualTime.getTime()-startTime.getTime())/1000;
      
      out.println("-- Statistics ------");
      out.println("Uptime: "+String.valueOf(diff));
      out.println();
      nowVar.print(out,diff);
      out.println("-- Statistics ------");      
      out.close();
      
      out=new PrintWriter(new FileOutputStream(webfolder+"server_up.txt"));
      out.println(actualTime.getTime()/1000);
      out.close();      
      }
    catch(Exception e)
      {
      }
    }
  }
