/* $Id: Statistics.java,v 1.4 2004/08/29 11:07:42 arianne_rpg Exp $ */
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
package marauroa.game;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import marauroa.*;

/** This class encapsulate everything related to the statistics recollection and
 *  storage. */
public class Statistics
  {
  public static class GatheredVariables
    {
    public long bytesRecv=0;
    public long bytesSend=0;
    
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
      out.println("  <byte recv=\""+String.valueOf(bytesRecv)+"\" send=\""+String.valueOf(bytesSend)+"\"/>");
      out.println("  <message recv=\""+String.valueOf(messagesRecv)+"\" send=\""+String.valueOf(messagesSend)+"\" incorrect=\""+String.valueOf(messagesIncorrect)+"\"/>");
      out.println("  <player login=\""+String.valueOf(playersLogin)+"\" failed=\""+String.valueOf(playersInvalidLogin)+"\" logout=\""+String.valueOf(playersLogout)+"\" timeout=\""+String.valueOf(playersTimeout)+"\"/>");
      out.println("  <online players=\""+String.valueOf(playersOnline)+"\" objects=\""+String.valueOf(objectsNow)+"\"/>");
      out.println("  <action added=\""+String.valueOf(actionsAdded)+"\" invalid=\""+String.valueOf(actionsInvalid)+"\"/>");
      }

    public void add(GatheredVariables var)
      {
      bytesRecv=var.bytesRecv+bytesRecv;
      bytesSend=var.bytesSend+bytesSend;
      messagesRecv=var.messagesRecv+messagesRecv;
      messagesSend=var.messagesSend+messagesSend;
      messagesIncorrect=var.messagesIncorrect+messagesIncorrect;
      playersLogin=var.playersLogin+playersLogin;
      playersInvalidLogin=var.playersInvalidLogin+playersInvalidLogin;
      playersTimeout=var.playersTimeout+playersTimeout;
      playersLogout=var.playersLogout+playersLogout;
      playersOnline=var.playersOnline;
      objectsNow=var.objectsNow;
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
  
  public void addBytesRecv(long bytes)
    {
    nowVar.bytesRecv+=bytes;
    }
  
  public void addBytesSend(long bytes)
    {
    nowVar.bytesSend+=bytes;
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
    ++nowVar.playersLogin;
    }
  
  public void addPlayerLogout(String username, int id)
    {
    ++nowVar.playersLogout;
    }
  
  public void addPlayerInvalidLogin(String username)
    {
    ++nowVar.playersInvalidLogin;
    }

  public void addPlayerTimeout(String username, int id)
    {
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
    ++nowVar.actionsAdded;
    }
  
  public void addActionsAdded(String action, int id, String text)
    {
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
      
      PrintWriter out=new PrintWriter(new FileOutputStream(webfolder+"server_stats.xml"));
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
      
      out.println("<statistics time=\""+Long.toString(actualTime.getTime()/1000)+"\">");
      out.println("  <uptime value=\""+String.valueOf(diff)+"\"/>");
      allTimeVar.print(out,diff);
      out.println("</statistics>");
      out.close();
      
      nowVar=new GatheredVariables();
      }
    catch(Exception e)
      {
      }
    }
  }
