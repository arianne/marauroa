/* $Id: JDBCPlayerDatabase.java,v 1.33 2004/04/26 16:01:00 arianne_rpg Exp $ */
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
import java.net.InetSocketAddress;
import java.util.*;
import marauroa.net.*;
import marauroa.*;

/** This is JDBC interface to the database.
 *  Actually it is limited to MySQL because we are using the AUTO_INCREMENT keyword. */
public class JDBCPlayerDatabase implements PlayerDatabase
  {
  /** Class to store the login events */
  private static class LoginEvent
    {
    /** TCP/IP address of the source of the login message */
    public String address;
    /** Time and date of the login event */
    public java.util.Date time;
    /** True if login was correct */
    public boolean correct;
    /** This method returns a String that represent the object
     *  @return a string representing the object.*/
    public String toString()
      {
      return "Login "+(correct?"SUCESSFULL":"FAILED")+" at "+time.toString()+" from "+address;
      }
    }
    
  public boolean validString(String string)
    {
    if(string.indexOf('\\')!=-1) return false;
    if(string.indexOf('\'')!=-1) return false;
    if(string.indexOf('"')!=-1) return false;
    if(string.indexOf('%')!=-1) return false;
    if(string.indexOf(';')!=-1) return false;
    if(string.indexOf(':')!=-1) return false;
    if(string.indexOf('#')!=-1) return false;
    return true;
    }
    
  private static PlayerDatabase playerDatabase=null;

  /** connection info **/
  private Properties connInfo;

  /** Constructor that connect using a set of Properties.
   *  @param connInfo a Properties set with the options to create the database.
   *  Refer to JDBC Database HOWTO document. */
  private JDBCPlayerDatabase(Properties connInfo) throws NoDatabaseConfException, GenericDatabaseException
    {
    this.connInfo=connInfo;
    random=new Random();
    runDBScript("marauroa_init.sql");
    }
  
  private static PlayerDatabase resetDatabaseConnection() throws Exception
    {
    Configuration conf=Configuration.getConfiguration();
    Properties props = new Properties();

    props.put("jdbc_url",conf.get("jdbc_url"));
    props.put("jdbc_class",conf.get("jdbc_class"));
    props.put("jdbc_user",conf.get("jdbc_user"));
    props.put("jdbc_pwd",conf.get("jdbc_pwd"));
    return new JDBCPlayerDatabase(props);
    }
  
  /** This method returns an instance of PlayerDatabase
   *  @return A shared instance of PlayerDatabase */
  public static PlayerDatabase getDatabase() throws NoDatabaseConfException
    {
    marauroad.trace("JDBCPlayerDatabase::getDatabase",">");
    try
      {
      if(playerDatabase==null)
        {
        playerDatabase=resetDatabaseConnection();
        }
      return playerDatabase;
      }
    catch(Exception e)
      {
      marauroad.trace("JDBCPlayerDatabase::getDatabase","X",e.getMessage());
      throw new NoDatabaseConfException();
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::getDatabase","<");
      }
    }
  
  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public boolean hasPlayer(Transaction trans, String username) throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::hasPlayer",">");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }
      
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player where username like '"+username+"'";

      marauroad.trace("JDBCPlayerDatabase::hasPlayer","D",query);

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
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::hasPlayer","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::hasPlayer","<");
      }
    }
  
  /** This method returns the lis of character that the player pointed by username has.
   *  @param username the name of the player from which we are requesting the list of characters.
   *  @return an array of String with the characters
   *  @throw PlayerNotFoundException if that player does not exists. */
  public String[] getCharactersList(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::getCharacterList",">");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }
      
      String[] characters = null;
      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select charname from characters where player_id="+id;

      marauroad.trace("JDBCPlayerDatabase::getCharacterList","D",query);

      ResultSet charactersSet = stmt.executeQuery(query);
      Vector vector = new Vector();

      while(charactersSet.next())
        {
        vector.add(charactersSet.getString("characters.charname"));
        }
      characters = new String[vector.size()];
      characters = (String[])vector.toArray(characters);
      return characters;
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getCharacterList","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::getCharactersList","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::getCharacterList","<");
      }
    }
  
  /** This method add the player to database with username and password as identificator.
   *  @param username is the name of the player
   *  @param password is a string used to verify access.
   *  @throws PlayerAlreadyAddedExceptio if the player is already in database */
  public void addPlayer(Transaction trans, String username, String password, String email) throws PlayerAlreadyAddedException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::addPlayer",">");
    try
      {
      if(!validString(username) || !validString(password) || !validString(email) )
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and password:'"+password+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      marauroad.trace("JDBCPlayerDatabase::addPlayer","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        marauroad.trace("JDBCPlayerDatabase::addPlayer","W","Database already contains that username("+username+")");
        throw new PlayerAlreadyAddedException(username);
        }
      else
        {
        query = "insert into player values(NULL,'"+username+"','"+password+"','"+email+"',NULL)";
        stmt.execute(query);
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addPlayer","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::addPlayer","<");
      }
    }
  
  /** This method remove the player with usernae from database.
   *  @param username is the name of the player
   *  @throws PlayerNotFoundException if the player doesn't exist in database. */
  public void removePlayer(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::removePlayer",">");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }
      
      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "delete from player where id="+id;

      stmt.execute(query);
      query = "delete from characters where player_id="+id;
      stmt.execute(query);
      query = "delete from loginEvent where player_id="+id;
      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::removePlayer","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::removePlayer","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::removePlayer","<");
      }
    }
  
  /** This method removes a character asociated with a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player owns.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException if the character doesn't exist or it is not owned by the player. */
  public void removeCharacter(Transaction trans, String username, String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::removeCharacter",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }
      
      int id=getDatabasePlayerId(trans,username);
      
      if(!hasCharacter(trans,username,character))
        {
        marauroad.trace("JDBCPlayerDatabase::removeCharacter","X","Database doesn't contains that username("+username+")-character("+character+")");
        throw new CharacterNotFoundException(username);
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "delete from characters where player_id="+id+" and charname like '"+character+"'";

      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::removeCharacter","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::removeCharacter","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::removeCharacter","<");
      }
    }
  
  /** This method returns true if the username/password match with any of the accounts in
   *  database or false if none of them match.
   *  @param username is the name of the player
   *  @param password is the string used to verify access.
   *  @return true if username/password is correct, false otherwise. */
  public boolean verifyAccount(Transaction trans, String username, String password) throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::verifyAccount",">");
    try
      {
      if(!validString(username) || !validString(password))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and password:'"+password+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player where username like '"+username+"' and password like '"+password+"'";
      
      marauroad.trace("JDBCPlayerDatabase::verifyAccount","D",query);

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
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::verifyAccount","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::verifyAccount","<");
      }
    }
  
  /** This method returns the list of Login events as a array of Strings
   *  @param username is the name of the player
   *  @return an array of String containing the login events.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public String[] getLoginEvent(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::getLoginEvent",">");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }
      
      String[] loginEvents = null;
      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select address,timedate,result from loginEvent where player_id="+id+" order by timedate limit 5";
      
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","D",query);

      ResultSet result = stmt.executeQuery(query);
      Vector vector = new Vector();
      
      while(result.next())
        {
        LoginEvent login_event = new LoginEvent();

        login_event.address = result.getString("address");
        login_event.time    = (java.util.Date)result.getTimestamp("timedate");
        login_event.correct = result.getInt("result")!=0;
        vector.add(login_event.toString());
        }
      loginEvents = new String[vector.size()];
      loginEvents = (String[])vector.toArray(loginEvents);
      return loginEvents;
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","<");
      }
    }
  
  /** This method returns true if the player has that character or false if it hasn't
   *  @param username is the name of the player
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(Transaction trans, String username, String character) throws PlayerNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::hasCharacter",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }
      
      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player,characters where username like '"+username+"' and charname like '"+character+"' and player.id=characters.player_id";
      
      marauroad.trace("JDBCPlayerDatabase::hasCharacter","D",query);

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
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::hasCharacter","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::hasCharacter","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::hasCharacter","<");
      }
    }
  
  /** This method add a Login event to the player
   *  @param username is the name of the player
   *  @param source the IP address of the player
   *  @param correctLogin true if the login has been correct.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public void addLoginEvent(Transaction trans, String username, InetSocketAddress source, boolean correctLogin) throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::addLoginEvent",">");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }
      
      int id=-1;
      try
        {
        id=getDatabasePlayerId(trans,username);
        }
      catch(PlayerNotFoundException e)
        {
        }
      
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "insert into loginEvent values("+id+",'"+source.getHostName()+"',NULL,"+(correctLogin?1:0)+")";
      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","<");
      }
    }
  
  /** This method add a character asociated to a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterAlreadyAddedException if that player-character exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void addCharacter(Transaction trans, String username, String character, RPObject object) throws PlayerNotFoundException, CharacterAlreadyAddedException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::addCharacter",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }
      if(hasCharacter(trans,username,character))
        {
        marauroad.trace("JDBCPlayerDatabase::addCharacter","X","Database does contains that username("+username+")-character("+character+")");
        throw new CharacterAlreadyAddedException(username);
        }
      else
        {
        Connection connection = ((JDBCTransaction)trans).getConnection();
        Statement stmt = connection.createStatement();
        int id=getDatabasePlayerId(trans,username);
        String query = "insert into characters values("+id+",'"+character+"',"+object.get("id")+")";

        stmt.execute(query);
        storeRPObject(trans,object);
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      trans.rollback();
      marauroad.trace("JDBCPlayerDatabase::addCharacter","X","Invalid RPObject: Lacks of attribute "+e.getAttribute());
      throw new PlayerNotFoundException(username);
      }
    catch(Exception sqle)
      {
      trans.rollback();
      marauroad.trace("JDBCPlayerDatabase::addCharacter","X",sqle.getMessage());
      throw new GenericDatabaseException(username);
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::addCharacter","<");
      }
    }
  
  /** This method returns the number of Players that exist on database
   *  @return the number of players that exist on database */
  public int getPlayerCount(Transaction trans) throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::getPlayerCount",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player";
      
      marauroad.trace("JDBCPlayerDatabase::getPlayerCount","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        return result.getInt(1);
        }
      return 0;
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getPlayerCount","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::getPlayerCount","<");
      }
    }
  
  /** This method is the opposite of getRPObject, and store in Database the object for
   *  an existing player and character.
   *  The difference between setRPObject and addCharacter are that setRPObject update it
   *  while addCharacter add it to database and fails if it already exists
   *.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @param object is the RPObject that represent this character in game.
   *
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException  if the player-character doesn't exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void setRPObject(Transaction trans, String username, String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::setRPObject",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }
      
      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from characters where charname like '"+character+"'";
      
      marauroad.trace("JDBCPlayerDatabase::setRPObject","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        if(result.getInt(1)==0)
          {
          marauroad.trace("JDBCPlayerDatabase::setRPObject","X","Database doesn't contains that username("+username+")-character("+character+")");
          throw new CharacterNotFoundException(username);
          }
        }
      
      storeRPObject(getTransaction(),object);
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::setRPObject","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::setRPObject","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::setRPObject","<");
      }
    }
  
  /** This method retrieves from Database the object for an existing player and character.
   *.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @return a RPObject that is the RPObject that represent this character in game.
   *
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException  if the player-character doesn't exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public RPObject getRPObject(Transaction trans, String username, String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::getRPObject",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }
      
      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select object_id from characters where player_id="+id+" and charname like '"+character+"'";

      marauroad.trace("JDBCPlayerDatabase::getRPObject","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        int object_id=result.getInt(1);
        
        return loadRPObject(getTransaction(),new RPObject.ID(object_id));
        }
      else
        {
        throw new CharacterNotFoundException(character);
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    catch(CharacterNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","X","Player("+username+") doesn't contains that character("+character+")");
      throw e;
      }
    catch (Exception e)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","X","Error serializing character: "+e.getMessage());
      throw new GenericDatabaseException("Error serializing character: "+e.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","<");
      }
    }
  
  private int getDatabasePlayerId(Transaction trans, String username) throws PlayerNotFoundException, SQLException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt=connection.createStatement();
    int id;
    
    if(!validString(username))
      {
      throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
      }
    
    String query = "select id from player where username like '"+username+"'";

    marauroad.trace("JDBCPlayerDatabase::getDatabasePlayerId","D",query);

    ResultSet result = stmt.executeQuery(query);

    if(result.next())
      {
      id = result.getInt(1);
      }
    else
      {
      throw new PlayerNotFoundException(username);
      }
    return(id);
    }
  
  private boolean reInitDB() throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::reInitDB",">");
    try
      {
      return (runDBScript("marauroa_drop.sql") && runDBScript("marauroa_init.sql"));
      }
    catch(GenericDatabaseException e)
      {
      marauroad.trace("JDBCPlayerDatabase::reInitDB","X",e.getMessage());
      throw e;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::reInitDB","<");
      }
    }
  
  private boolean runDBScript(String file) throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::runDBScript",">");

    boolean ret = true;
    JDBCTransaction transaction = (JDBCTransaction)getTransaction();
    Connection con = transaction.getConnection();
    BufferedReader in=null;

    try
      {
      Statement stmt = con.createStatement();
      InputStream init_file=getClass().getClassLoader().getResourceAsStream(file);
      in= new BufferedReader(new InputStreamReader(init_file));
      
      String line;
      StringBuffer is=new StringBuffer();
      while((line=in.readLine())!=null)
        {
        is.append(line);
        if(line.indexOf(';')!=-1)
          {
          String query=is.toString();
          marauroad.trace("JDBCPlayerDatabase::runDBScript","D",query);
          stmt.addBatch(query);
          is=new StringBuffer();
          }
        }
      
      int ret_array[] = stmt.executeBatch();

      for (int i = 0; i < ret_array.length; i++)
        {
        if(ret_array[i]<0)
          {
          ret = false;
          break;
          }
        }

      return ret;
      }
    catch(Exception e)
      {
      marauroad.trace("JDBCPlayerDatabase::runDBScript","X",e.getMessage());
      throw new GenericDatabaseException(e.getMessage());
      }
    finally
      {
      try
        {
        if(in!=null)
          {
          in.close();
          }
        }
      catch(IOException e)
        {
        }
      
        
      marauroad.trace("JDBCPlayerDatabase::runDBScript","<");
      }
    }
  
  private JDBCTransaction transaction;
  
  public Transaction getTransaction() throws GameDatabaseException.GenericDatabaseException
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

        return object;
        }
      else
        {
        throw new SQLException("RPObject not found: "+id.toString());
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
        throw new SQLException("RPObject not found: "+id.toString());
        }
      }
    catch(SQLException e)
      {
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
        
      storeRPObject(trans,object,0);
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::storeRPObject","X",e.getMessage());
      throw new SQLException(e.getMessage());
      }
    catch(SQLException e)
      {
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
    String object_id=object.get("id");

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
  private static int last_idAssigned=1;
  
  public RPObject.ID getValidRPObjectID(Transaction trans)
    {
    if(last_idAssigned==1)
      {
      try
        {
        Connection connection = ((JDBCTransaction)trans).getConnection();
        Statement stmt=connection.createStatement();
      
        String query = "select max(id) from player";
        marauroad.trace("JDBCPlayerDatabase::getDatabasePlayerId","D",query);

        ResultSet result = stmt.executeQuery(query);

        if(result.next())
          {
          last_idAssigned=result.getInt(1);
          }
        }
      catch(Exception e)
        {
        marauroad.trace("JDBCRPObjectDatabase::storeRPObject","X",e.getMessage());
        }
      }

    RPObject.ID id=new RPObject.ID(last_idAssigned++);

    while(hasRPObject(trans,id))
      {
      id=new RPObject.ID(last_idAssigned++);
      }

    return id;
    }


  public void addStatisticsEvent(Transaction trans, Statistics.GatheredVariables var) throws GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::addStatisticsEvent",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      
      String query = "insert into statistics values(NULL,"+
        var.bytesSend+","+
        var.bytesRecv+","+
        var.playersLogin+","+
        var.playersLogout+","+
        var.playersTimeout+","+
        var.playersOnline+")";
      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","<");
      }
    }
  }
