/* $Id: JDBCRPObjectDatabase.java,v 1.10 2004/03/25 13:29:18 arianne_rpg Exp $ */
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

import java.sql.*;
import java.io.*;
import java.util.*;
import marauroa.*;

public class JDBCRPObjectDatabase implements GameDatabaseException
  {
  /** connection info **/
  private Properties connInfo;
  
  private static JDBCRPObjectDatabase rpObjectDatabase=null;

  public static String EscapeString(String text)
    {
    StringBuffer result=new StringBuffer();
    
    for(int i=0;i<text.length();++i)
      {
      if(text.charAt(i)=='\'' || text.charAt(i)=='\"' || text.charAt(i)=='\\')
        {
        result.append("\\");
        }
      result.append(text.charAt(i));
      }
    return result.toString();
    }
  
  public static String UnescapeString(String text)
    {
    StringBuffer result=new StringBuffer();
    
    for(int i=0;i<text.length();++i)
      {
      if(text.charAt(i)!='\\' || (text.charAt(i)=='\\' && text.charAt(((i-1)>0?i-1:0))=='\\'))
        {
        result.append(text.charAt(i));
        }
      }
    return result.toString();
    }
  
  private JDBCRPObjectDatabase(Properties connInfo) throws NoDatabaseConfException, GenericDatabaseException
    {
    this.connInfo=connInfo;
    random=new Random();
    initDB();
    }
  
  private static JDBCRPObjectDatabase resetDatabaseConnection() throws Exception
    {
    Configuration conf=Configuration.getConfiguration();
    Properties props = new Properties();

    props.put("jdbc_url",conf.get("jdbc_url"));
    props.put("jdbc_class",conf.get("jdbc_class"));
    props.put("jdbc_user",conf.get("jdbc_user"));
    props.put("jdbc_pwd",conf.get("jdbc_pwd"));
    return new JDBCRPObjectDatabase(props);
    }
  
  /** This method returns an instance of PlayerDatabase
   *  @return A shared instance of PlayerDatabase */
  public static JDBCRPObjectDatabase getDatabase() throws NoDatabaseConfException
    {
    marauroad.trace("JDBCRPObjectDatabase::getDatabase",">");
    try
      {
      if(rpObjectDatabase==null)
        {
        rpObjectDatabase=resetDatabaseConnection();
        }
      return rpObjectDatabase;
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("JDBCRPObjectDatabase::getDatabase","X",e.getMessage());
      throw new NoDatabaseConfException();
      }
    finally
      {
      marauroad.trace("JDBCRPObjectDatabase::getDatabase","<");
      }
    }
  public static class RPObjectIterator
    {
    private ResultSet set;
    public RPObjectIterator(ResultSet set)
      {
      this.set=set;
      }
    
    public boolean hasNext()
      {
      try
        {
        return set.next();
        }
      catch(SQLException e)
        {
        return false;
        }
      }
    
    public RPObject.ID next() throws SQLException
      {
      return new RPObject.ID(set.getInt(1));
      }
    }
  public RPObjectIterator iterator(Transaction trans)
    {
    marauroad.trace("JDBCPlayerDatabase::iterator",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select id from rpobject where slot_id=0";

      marauroad.trace("JDBCRPObjectDatabase::hasRPObject","D",query);
      
      ResultSet result = stmt.executeQuery(query);

      return new RPObjectIterator(result);
      }
    catch(SQLException e)
      {
      marauroad.trace("JDBCPlayerDatabase::iterator","X",e.getMessage());
      return null;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::iterator","<");
      }
    }
  
  public boolean hasRPObject(Transaction trans, RPObject.ID id)
    {
    marauroad.trace("JDBCPlayerDatabase::hasRPObject",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from rpobject where id="+id.getObjectID();

      marauroad.trace("JDBCRPObjectDatabase::hasRPObject","D",query);
      
      ResultSet result = stmt.executeQuery(query);
      
      if(result.next())
        {
        if(result.getInt(1)!=0)
          {
          return true;
          }
        }
      return false;
      }
    catch(SQLException e)
      {
      marauroad.trace("JDBCRPObjectDatabase::hasRPObject","X",e.getMessage());
      return false;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::hasRPObject","<");
      }
    }
  
  public RPObject loadRPObject(Transaction trans, RPObject.ID id) throws Exception
    {
    marauroad.trace("JDBCPlayerDatabase::loadRPObject",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();

      if(hasRPObject(trans,id))
        {
        RPObject object=new RPObject();
        
        loadRPObject(trans,object,id.getObjectID());
        return object;
        }
      else
        {
        throw new SQLException("RPObject not found");
        }
      }
    catch(Exception e)
      {
      marauroad.trace("JDBCRPObjectDatabase::loadRPObject","X",e.getMessage());
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::loadRPObject","<");
      }
    }
  
  private void loadRPObject(Transaction trans, RPObject object,int object_id) throws SQLException, RPObject.SlotAlreadyAddedException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt = connection.createStatement();
    String query=null;
    
    query = "select name,value from rpattribute where object_id="+object_id+";";
    marauroad.trace("JDBCRPObjectDatabase::loadRPObject","D",query);
    
    ResultSet result = stmt.executeQuery(query);

    while(result.next())
      {
      object.put(UnescapeString(result.getString(1)),UnescapeString(result.getString(2)));
      }
    query = "select name,slot_id from rpslot where object_id="+object_id+";";
    marauroad.trace("JDBCRPObjectDatabase::loadRPObject","D",query);
    result = stmt.executeQuery(query);
    while(result.next())
      {
      RPSlot slot=new RPSlot(UnescapeString(result.getString(1)));

      object.addSlot(slot);
      
      int slot_id=result.getInt(2);
      
      query = "select id from rpobject where slot_id="+slot_id+";";
      marauroad.trace("JDBCRPObjectDatabase::loadRPObject","D",query);

      ResultSet resultSlot = connection.createStatement().executeQuery(query);
      
      while(resultSlot.next())
        {
        RPObject slotObject=new RPObject();

        loadRPObject(trans,slotObject,resultSlot.getInt(1));
        slot.add(slotObject);
        }
      }
    }
  
  public void deleteRPObject(Transaction trans, RPObject.ID id) throws SQLException
    {
    marauroad.trace("JDBCPlayerDatabase::deleteRPObject",">");
    Connection connection = ((JDBCTransaction)trans).getConnection();

    try
      {
      if(hasRPObject(trans,id))
        {
        deleteRPObject(trans,id.getObjectID());
        }
      else
        {
        throw new SQLException("RPObject not found");
        }
      connection.commit();
      }
    catch(SQLException e)
      {
      connection.rollback();
      marauroad.trace("JDBCRPObjectDatabase::deleteRPObject","X",e.getMessage());
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::deleteRPObject","<");
      }
    }
  
  private void deleteRPObject(Transaction trans, int id) throws SQLException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt = connection.createStatement();
    String query=null;
    
    query = "select id from rpobject,rpslot where object_id="+id+" and rpobject.slot_id=rpslot.slot_id;";
    marauroad.trace("JDBCRPObjectDatabase::deleteRPObject","D",query);
    
    ResultSet result = stmt.executeQuery(query);

    while(result.next())
      {
      deleteRPObject(trans,result.getInt(1));
      }
    query = "delete from rpslot where object_id="+id+";";
    marauroad.trace("JDBCRPObjectDatabase::deleteRPObject","D",query);
    stmt.execute(query);
    query = "delete from rpattribute where object_id="+id+";";
    marauroad.trace("JDBCRPObjectDatabase::deleteRPObject","D",query);
    stmt.execute(query);
    query = "delete from rpobject where id="+id+";";
    marauroad.trace("JDBCRPObjectDatabase::deleteRPObject","D",query);
    stmt.execute(query);
    }
  
  public void storeRPObject(Transaction trans, RPObject object) throws SQLException
    {
    marauroad.trace("JDBCPlayerDatabase::storeRPObject",">");
    Connection connection = ((JDBCTransaction)trans).getConnection();

    try
      {
      if(hasRPObject(trans,new RPObject.ID(object)))
        {
        deleteRPObject(trans,new RPObject.ID(object));
        }
      
      List attribToRemove=new LinkedList();
      Iterator it=object.iterator();

      while(it.hasNext())
        {
        String attrib=(String)it.next();

        if(attrib.charAt(0)=='?')
          {
          attribToRemove.add(attrib);
          }
        }
      it=attribToRemove.iterator();
      while(it.hasNext())
        {
        object.remove((String)it.next());
        }
      storeRPObject(trans,object,0);
      connection.commit();
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      connection.rollback();
      marauroad.trace("JDBCPlayerDatabase::storeRPObject","X",e.getMessage());
      throw new SQLException(e.getMessage());
      }
    catch(SQLException e)
      {
      connection.rollback();
      marauroad.trace("JDBCPlayerDatabase::storeRPObject","X",e.getMessage());
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::storeRPObject","<");
      }
    }
  
  private void storeRPObject(Transaction trans, RPObject object, int slot_id) throws SQLException, Attributes.AttributeNotFoundException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt = connection.createStatement();
    String query=null;
    String object_id=object.get("object_id");

    query = "insert into rpobject values("+object_id+","+slot_id+");";
    marauroad.trace("JDBCRPObjectDatabase::storeRPObject","D",query);
    stmt.execute(query);
    
    Iterator it=object.iterator();

    while(it.hasNext())
      {
      String attrib=(String) it.next();
      String value=object.get(attrib);
      
      query = "insert into rpattribute values("+object_id+",'"+EscapeString(attrib)+"','"+EscapeString(value)+"');";
      marauroad.trace("JDBCRPObjectDatabase::storeRPObject","D",query);
      stmt.execute(query);
      }
    
    RPObject.SlotsIterator sit=object.slotsIterator();

    while(sit.hasNext())
      {
      RPSlot slot=(RPSlot) sit.next();
      
      query = "insert into rpslot values("+object_id+",'"+EscapeString(slot.getName())+"',NULL);";
      marauroad.trace("JDBCRPObjectDatabase::storeRPObject","D",query);
      stmt.execute(query);
      query = "select slot_id from rpslot where object_id="+object_id+" and name like '"+EscapeString(slot.getName())+"';";
      marauroad.trace("JDBCRPObjectDatabase::storeRPObject","D",query);

      int object_slot_id;
      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        object_slot_id = result.getInt(1);
        }
      else
        {
        throw new SQLException("Not able to select RPSlot("+slot.getName()+") that have just been inserted");
        }
      
      Iterator oit=slot.iterator();

      while(oit.hasNext())
        {
        RPObject objectInSlot=(RPObject)oit.next();

        storeRPObject(trans,objectInSlot,object_slot_id);
        }
      }
    }

  private Random random;
  public RPObject.ID getValidRPObjectID(Transaction trans)
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    RPObject.ID id=new RPObject.ID(random.nextInt());

    while(hasRPObject(trans,id))
      {
      id=new RPObject.ID(random.nextInt());
      }
    return id;
    }
  
  private Connection createConnection(Properties props) throws GenericDatabaseException
    {
    marauroad.trace("JDBCRPObjectDatabase::createConnection",">");
    try
      {
      Class.forName((String)props.get("jdbc_class")).newInstance();
      
      Properties connInfo = new Properties();

      connInfo.put("user", props.get("jdbc_user"));
      connInfo.put("password", props.get("jdbc_pwd"));
      connInfo.put("charSet", "UTF-8");

      Connection conn = DriverManager.getConnection((String)props.get("jdbc_url"), connInfo);

      conn.setAutoCommit(false);
      return conn;
      }
    catch (Exception e)
      {
      marauroad.trace("JDBCRPObjectDatabase::createConnection","X",e.getMessage());
      throw new GenericDatabaseException(e.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCRPObjectDatabase::createConnection","<");
      }
    }
  
  private boolean initDB() throws GenericDatabaseException
    {
    marauroad.trace("JDBCRPObjectDatabase::initDB",">");

    Transaction trans = getTransaction();
    Connection connection = ((JDBCTransaction)trans).getConnection();

    try
      {
      Statement stmt = connection.createStatement();

      String query = "create table if not exists  rpobject(id integer not null primary key, slot_id integer) TYPE=INNODB;";
      stmt.addBatch(query);
      query = "create table if not exists rpattribute(object_id integer not null, name varchar(64) not null, value varchar(255), primary key(object_id,name)) TYPE=INNODB;";
      stmt.addBatch(query);
      query = "create table if not exists  rpslot(object_id integer not null, name varchar(64) not null, slot_id integer auto_increment primary key) TYPE=INNODB;";
      stmt.addBatch(query);
      
      int ret_array[] = stmt.executeBatch();

      for (int i = 0; i < ret_array.length; i++)
        {
        if(ret_array[i]<0)
          {
          return false;
          }
        }
      return true;
      }
    catch (SQLException e)
      {
      marauroad.trace("JDBCRPObjectDatabase::initDB","X",e.getMessage());
      throw new GenericDatabaseException(e.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCRPObjectDatabase::initDB","<");
      }
    }
  
  private JDBCTransaction transaction;
  
  public Transaction getTransaction()
    throws GameDatabaseException.GenericDatabaseException
    {
    if(transaction==null || !transaction.isValid())
      {
      transaction=new JDBCTransaction(createConnection(connInfo));
      if(transaction==null || !transaction.isValid())
        {
        throw new GenericDatabaseException("can't create connection");
        }
      }
    return(transaction);
    }
  }
