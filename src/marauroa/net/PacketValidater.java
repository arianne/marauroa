/* $Id: PacketValidater.java,v 1.3 2004/11/12 15:39:16 arianne_rpg Exp $ */
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
package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;
import marauroa.*;
import marauroa.game.*;
import java.sql.*;

/** The PacketValidater validates the ariving packets,
 *  (currently it can only check if the address is banned,
 *   may be it will check more later)
 *
 */
public final class PacketValidater
{
  private InetAddressMask[] banList;
  
  /* timestamp of last reload */
  private long lastLoadTS;
  
  /* */
  private long reloadAfter;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public PacketValidater()
  {
    marauroad.trace("PacketValidater",">");
    
    /* at most each 5 minutes */
    reloadAfter=5*60*1000;
    
    try
    {
      /* read ban list from configuration */
      loadBannedIPNetworkListFromDB();
    }
    finally
    {
      marauroad.trace("PacketValidater","<");
    }
  }
  
  /** returns true if the source ip is banned */
  public synchronized boolean checkBanned(DatagramPacket packet)
  {
    boolean banned = false;
    marauroad.trace("PacketValidater::checkBanned",">");
    checkReload();
    if(banList!=null)
      try
      {
        for(int i=0; i<banList.length; i++)
        {
          InetAddressMask iam=banList[i];
          if(iam.matches(packet.getAddress()))
          {
            marauroad.trace("PacketValidater::checkBanned","D","Packet from "+ packet.getAddress()+" is banned by "+iam);
            banned=true;
            break;
          }
        }
      }
    finally
    {
      marauroad.trace("PacketValidater::checkBanned","<");
    }
    return(banned);
  }
  
  /** loads and initializes the ban list from a database
   *
   */
  public synchronized void loadBannedIPNetworkListFromDB()
  {
    marauroad.trace("PacketValidater::loadBannedIPNetworkListFromDB",">");
    try
    {
      /* read ban list from DB */
      Connection connection = createDBConnection();
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("select address,mask from banlist");
      banList=null;
      List<InetAddressMask> ban_list_tmp = new ArrayList<InetAddressMask>();
      while(rs.next())
      {
        String address = rs.getString("address");
        String mask    = rs.getString("mask");
        InetAddressMask iam = new InetAddressMask(address, mask);
        ban_list_tmp.add(iam);
      }
      if(ban_list_tmp.size()>0)
      {
        banList = new InetAddressMask[ban_list_tmp.size()];
        banList = (InetAddressMask[])ban_list_tmp.toArray(banList);
      }
      marauroad.trace("PacketValidater::loadBannedIPNetworkListFromDB","D","loaded "+ban_list_tmp.size() + " entries from ban table");
      connection.close();
    }
    catch(Exception e)
    {
      marauroad.thrown("PacketValidater::loadBannedIPNetworkListFromDB","X",e);
    }
    finally
    {
      lastLoadTS = System.currentTimeMillis();
      marauroad.trace("PacketValidater::loadBannedIPNetworkListFromDB","<");
    }
  }
  
  /** checks if reload is neccesary and performs it
   *
   */
  public synchronized void checkReload()
  {
    marauroad.trace("PacketValidater::checkReload",">");
    try
    {
      if (System.currentTimeMillis()-lastLoadTS>=reloadAfter)
      {
        loadBannedIPNetworkListFromDB();
      }
    }
    finally
    {
      marauroad.trace("PacketValidater::checkReload","<");
    }
  }
  /** creates a database connection
   *
   */
  private Connection createDBConnection() throws Exception
  {
    marauroad.trace("PacketValidater::createDBConnection",">");
    try
    {
      /* read the database configuration from Configuration*/
      Configuration conf=Configuration.getConfiguration();
      
      String jdbc_class = conf.get("jdbc_class");
      Class.forName(jdbc_class).newInstance();
      
      Properties props  = new Properties();
      props.put("user",conf.get("jdbc_user"));
      props.put("password",conf.get("jdbc_pwd"));
      props.put("charSet", "UTF-8");
      
      String jdbc_url   = conf.get("jdbc_url");
      
      /* create and return a new database connection */
      Connection conn = DriverManager.getConnection(jdbc_url, props);
      return conn;
    }
    catch (Exception e)
    {
      marauroad.thrown("PacketValidater::createDBConnection","X",e);
      throw e;
    }
    finally
    {
      marauroad.trace("PacketValidater::createDBConnection","<");
    }
  }
}
