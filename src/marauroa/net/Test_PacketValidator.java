/* $Id: Test_PacketValidator.java,v 1.2 2004/11/27 11:04:12 root777 Exp $ */
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

import marauroa.marauroad;
import marauroa.Configuration;
import java.util.Properties;
import junit.framework.*;
import java.sql.*;
import java.net.*;

public class Test_PacketValidator extends TestCase 
  {
  public static Test suite ( ) 
    {
    return new TestSuite(Test_PacketValidator.class);
    }

  public void testValidator()
    {
    try
      {
      setupTestEnvironment();
      }
    catch(Exception e)
      {
      marauroad.thrown("Test_PacketValidator::setupTestEnvironment","X",e);
      fail("Cannot create test environment - "+e.getMessage());
      }
    try
      { 
      //banned are: 129.0.0.*, 192.168.100.100,  172.*, 192.168.200.*
      //so lets test it: 
      PacketValidator validator = new PacketValidator();
      InetAddress address;
    
      //127.0.0.113
      address = InetAddress.getByAddress(new byte[]{(byte)127,(byte)0,(byte)0,(byte)113});    
      assertFalse("127.0.0.113 schould not be banned",validator.checkBanned(address));

      //129.0.0.113
      address = InetAddress.getByAddress(new byte[]{(byte)129,(byte)0,(byte)0,(byte)113});
      assertTrue("129.0.0.113 schould be banned",validator.checkBanned(address));

      //192.168.100.3
      address = InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)100,(byte)3});
      assertFalse("192.168.100.3 schould not be banned",validator.checkBanned(address));
    
      //192.168.100.99
      address = InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)100,(byte)99});
      assertFalse("192.168.100.99 schould not be banned",validator.checkBanned(address)); 
    
      //192.168.100.100
      address = InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)100,(byte)100});
      assertTrue("192.168.100.100 schould be banned",validator.checkBanned(address));    

      //192.168.100.101
      address = InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)100,(byte)101});
      assertFalse("192.168.100.101 schould not be banned",validator.checkBanned(address));

      for(int i=1;i<=255; i++)
        {
        //172.168.*.100
        address = InetAddress.getByAddress(new byte[]{(byte)172,(byte)168,(byte)i,(byte)100});
        assertTrue("172.168."+i+".100 schould be banned",validator.checkBanned(address));
        }

      for(int i=1;i<=255; i++)
        {
        //192.168.200.*
        address = InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)200,(byte)i});
        assertTrue("192.168.200."+i+"+ schould be banned",validator.checkBanned(address));
        }
       
      //just one performance check...
      //192.168.213.100 should not be banned - the worst case, as the whole banlist must be checked
      address = InetAddress.getByAddress(new byte[]{(byte)192,(byte)168,(byte)213,(byte)100});
      boolean banned = false;
      int count = 1000;
      long start_ts = System.currentTimeMillis();
      for(int i=0;i<count; i++)
        {
        banned = banned || validator.checkBanned(address);
        }
      long duration = System.currentTimeMillis() - start_ts;
      marauroad.trace("Test_PacketValidator::testValidator","D",""+count+" checks are took "+duration + " ms.");
      marauroad.trace("Test_PacketValidator::testValidator","D","It makes about "+(100*duration/count)+" ms per 100 calls.");
      
   
      }
    catch(Exception e)
      {
      marauroad.thrown("Test_PacketValidator::testValidator","X",e);  
      fail(e.getMessage());
      }
   
    try
      {
      clearTestEnvironment();
      }
    catch(Exception e)
      {
      marauroad.thrown("Test_PacketValidator::clearTestEnvironment","X",e);
      fail("Cannot clear test environment - "+e.getMessage());
      }  
    }


  private void setupTestEnvironment() throws Exception
    {
/*
mysql> describe banlist;
+---------+-------------+------+-----+---------+----------------+
| Field   | Type        | Null | Key | Default | Extra          |
+---------+-------------+------+-----+---------+----------------+
| id      | int(11)     |      | PRI | NULL    | auto_increment |
| address | varchar(15) | YES  |     | NULL    |                |
| mask    | varchar(15) | YES  |     | NULL    |                |
+---------+-------------+------+-----+---------+----------------+
*/
    int table_entries = 0;
    Connection conn = createDBConnection();
    //clear all bans
    Statement stmt  = conn.createStatement();
    stmt.execute("delete from banlist");
    marauroad.trace("Test_PacketValidator::clearTestEnvironment","D","Table cleared");
 
    PreparedStatement p_stmt = conn.prepareStatement("insert into banlist values(NULL, ? , ? )");
    //129.0.0.*
    p_stmt.setString(1,"129.0.0.1");
    p_stmt.setString(2,"255.255.255.0");
    p_stmt.executeUpdate();
    table_entries++;
    marauroad.trace("Test_PacketValidator::clearTestEnvironment","D","129.0.0.1/255.255.255.0 inserted");

    //one host only - 192.168.100.100
    p_stmt.setString(1,"192.168.100.100");
    p_stmt.setString(2,"255.255.255.255");
    p_stmt.executeUpdate();
    table_entries++;
    marauroad.trace("Test_PacketValidator::clearTestEnvironment","D","192.168.100.100/255.255.255.255 inserted");
    
    //all 172.*
    p_stmt.setString(1,"172.1.2.3");
    p_stmt.setString(2,"255.0.0.0");
    p_stmt.execute();
    table_entries++;
    marauroad.trace("Test_PacketValidator::clearTestEnvironment","D","172.1.2.3/255.0.0.0 inserted");
    
    //whole 192.168.200.* - one by one
    for(int i = 1; i<=255; i++)
      {
      p_stmt.setString(1,"192.168.200."+i);
      p_stmt.setString(2,"255.255.255.255");
      p_stmt.execute();
      table_entries++;
      }
    marauroad.trace("Test_PacketValidator::clearTestEnvironment","D","192.168.200.1-255/255.255.255.255 inserted");
    conn.commit();
    stmt  = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select count(*) from banlist");
    if(rs.next())
      {
      int db_table_entries = rs.getInt(1);
      if(db_table_entries!=table_entries)
        {
        throw new Exception("Table entries are not ok: "+table_entries+"!="+db_table_entries);  
        }
      }
    else
      {
      throw new Exception("Table entries are not ok.");
      }
    }

  private void clearTestEnvironment() throws Exception
    {
    Connection conn = createDBConnection();
    //clear all bans
    Statement stmt  = conn.createStatement();
    stmt.execute("delete from banlist");
    conn.commit();
    }

  private Connection createDBConnection() throws Exception
    {
    marauroad.trace("Test_PacketValidator::createDBConnection",">");
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
      conn.setAutoCommit(false);
      return conn;
      }
    catch (Exception e)
      {
      marauroad.thrown("Test_PacketValidator::createDBConnection","X",e);
      throw e;
      }
    finally
      {
      marauroad.trace("Test_PacketValidator::createDBConnection","<");
      }
    }
  }
