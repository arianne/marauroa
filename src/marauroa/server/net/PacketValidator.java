/* $Id: PacketValidator.java,v 1.3 2005/04/03 11:34:42 arianne_rpg Exp $ */
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
package marauroa.server.net;

import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.sql.*;

import marauroa.common.*;
import marauroa.server.*;

/** The PacketValidator validates the ariving packets,
 *  (currently it can only check if the address is banned,
 *   may be it will check more later)
 *
 */
public class PacketValidator
{
  private InetAddressMask[] banList;
  
  /* timestamp of last reload */
  private long lastLoadTS;
  
  /* */
  private long reloadAfter;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public PacketValidator()
  {
    Logger.trace("PacketValidator",">");
    
    /* at most each 5 minutes */
    reloadAfter=5*60*1000;
    
    try
    {
      /* read ban list from configuration */
      loadBannedIPNetworkListFromDB();
    }
    finally
    {
      Logger.trace("PacketValidator","<");
    }
  }
  
  /** returns true if the source ip is banned */
  public boolean checkBanned(DatagramPacket packet)
  {
    boolean banned = false;
    banned = checkBanned(packet.getAddress());
    return(banned);
  }

  /** returns true if the address is banned */
  public synchronized boolean checkBanned(InetAddress address)
  {
    boolean banned = false;
    Logger.trace("PacketValidator::checkBanned",">");
    checkReload();
    if(banList!=null)
      try
      {
        for(int i=0; i<banList.length; i++)
        {
          InetAddressMask iam=banList[i];
          if(iam.matches(address))
          {
            Logger.trace("PacketValidator::checkBanned","D","Address "+ address+" is banned by "+iam);
            banned=true;
            break;
          }
        }
      }
    finally
    {
      Logger.trace("PacketValidator::checkBanned","<");
    }
    return(banned);
  }
  
  /** loads and initializes the ban list from a database
   *
   */
  public synchronized void loadBannedIPNetworkListFromDB()
  {
    Logger.trace("PacketValidator::loadBannedIPNetworkListFromDB",">");
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
      Logger.trace("PacketValidator::loadBannedIPNetworkListFromDB","D","loaded "+ban_list_tmp.size() + " entries from ban table");
      connection.close();
    }
    catch(Exception e)
    {
      Logger.thrown("PacketValidator::loadBannedIPNetworkListFromDB","X",e);
    }
    finally
    {
      lastLoadTS = System.currentTimeMillis();
      Logger.trace("PacketValidator::loadBannedIPNetworkListFromDB","<");
    }
  }
  
  /** checks if reload is neccesary and performs it
   *
   */
  public synchronized void checkReload()
  {
    Logger.trace("PacketValidator::checkReload",">");
    try
    {
      if (System.currentTimeMillis()-lastLoadTS>=reloadAfter)
      {
        loadBannedIPNetworkListFromDB();
      }
    }
    finally
    {
      Logger.trace("PacketValidator::checkReload","<");
    }
  }
  /** creates a database connection
   *  TODO: Refactor to use the available Database class
   */
  private Connection createDBConnection() throws Exception
  {
    Logger.trace("PacketValidator::createDBConnection",">");
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
      Logger.thrown("PacketValidator::createDBConnection","X",e);
      throw e;
    }
    finally
    {
      Logger.trace("PacketValidator::createDBConnection","<");
    }
  }
}
