/* $Id: JDBCPlayerDatabase.java,v 1.25 2006/03/21 13:19:31 arianne_rpg Exp $ */
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
package marauroa.server.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.game.SlotAlreadyAddedException;

/** This is JDBC interface to the database.
 *  Actually it is limited to MySQL because we are using the AUTO_INCREMENT keyword. */
public class JDBCPlayerDatabase implements IPlayerDatabase
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(JDBCPlayerDatabase.class);
  
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
    if(string.indexOf('<')!=-1) return false;
    if(string.indexOf('>')!=-1) return false;
    return true;
    }

  private static IPlayerDatabase playerDatabase=null;

  /** connection info **/
  private Properties connInfo;

  /** Constructor that connect using a set of Properties.
   *  @param connInfo a Properties set with the options to create the database.
   *  Refer to JDBC Database HOWTO document. */
  protected JDBCPlayerDatabase(Properties connInfo) throws NoDatabaseConfException, GenericDatabaseException
    {
    this.connInfo=connInfo;
    runDBScript("marauroa/server/marauroa_init.sql");
    }

  protected static IPlayerDatabase resetDatabaseConnection() throws Exception
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
  public static IPlayerDatabase getDatabase() throws NoDatabaseConfException
    {
    Log4J.startMethod(logger, "getDatabase");
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
      logger.error("cannot get database connection",e);
      throw new NoDatabaseConfException(e);
      }
    finally
      {
      Log4J.finishMethod(logger, "getDatabase");
      }
    }

  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public boolean hasPlayer(Transaction trans, String username) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "hasPlayer");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from  player where username like '"+username+"'";

      logger.debug("hasPlayer is using query: "+query);

      ResultSet result = stmt.executeQuery(query);
      
      boolean playerExists=false;

      if(result.next())
        {
        if(result.getInt("amount")!=0)
          {
          playerExists=true;
          }
        }

      stmt.close();
      return playerExists;
      }
    catch(SQLException sqle)
      {
      logger.error("cannot get player from database",sqle);
      throw new GenericDatabaseException(sqle);
      }
    finally
      {
      Log4J.startMethod(logger, "hasPlayer");
      }
    }

  /** This method returns the lis of character that the player pointed by username has.
   *  @param username the name of the player from which we are requesting the list of characters.
   *  @return an array of String with the characters
   *  @exception PlayerNotFoundException if that player does not exists. */
  public String[] getCharactersList(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "getCharacterList");
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

      logger.debug("getCharacterList is executing query "+query);

      ResultSet charactersSet = stmt.executeQuery(query);
      Vector<String> vector = new Vector<String>();

      while(charactersSet.next())
        {
        vector.add(charactersSet.getString("characters.charname"));
        }
        
      charactersSet.close();

      characters = new String[vector.size()];
      characters = (String[])vector.toArray(characters);
      
      stmt.close();
      
      Log4J.finishMethod(logger, "getCharacterList");
      
      return characters;
      }
    catch(SQLException sqle)
      {
      logger.error("database error",sqle);
      throw new GenericDatabaseException(sqle);
      }
    catch(PlayerNotFoundException e)
      {
      logger.warn("cannot get get character list: Database doesn't contains that username("+username+")",e);
      throw e;
      }
    }

  /** This method add the player to database with username and password as identificator.
   *  @param username is the name of the player
   *  @param password is a string used to verify access.
   *  @exception PlayerAlreadyAddedExceptio if the player is already in database */
  public void addPlayer(Transaction trans, String username, byte[] password, String email) throws PlayerAlreadyAddedException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "addPlayer");
    try
      {
      if(!validString(username) || !validString(email) )
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and email:'"+email+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      logger.debug("addPlayer is executing query "+query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        result.close();
        stmt.close();
  
        logger.warn("Database already contains that username("+username+")");
        throw new PlayerAlreadyAddedException(username);
        }
      else
        {
        result.close();
      
        /* We store the hashed version of the password */
        password = Hash.hash(password);

        query = "insert into player(id,username,password,email,timedate,status) values(NULL,'"+username+"','"+ Hash.toHexString(password)+"','"+email+"',NULL,DEFAULT)";
        stmt.execute(query);
        stmt.close();
        }
      }
    catch(SQLException sqle)
      {
      logger.error("error adding Player",sqle);
      throw new GenericDatabaseException(sqle);
      }
    Log4J.finishMethod(logger, "addPlayer");
    }

  /** This method remove the player with usernae from database.
   *  @param username is the name of the player
   *  @exception PlayerNotFoundException if the player doesn't exist in database. */
  public void removePlayer(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "removePlayer");
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

      /* BUG: Remove the RPObject too */

      stmt.execute(query);
      query = "delete from characters where player_id="+id;
      stmt.execute(query);
      query = "delete from loginEvent where player_id="+id;
      stmt.execute(query);

      stmt.close();
      }
    catch(SQLException sqle)
      {
      logger.error("error while removing player",sqle);
      throw new GenericDatabaseException(sqle);
      }
    catch(PlayerNotFoundException e)
      {
      logger.warn("Database doesn't contains that username("+username+")",e);
      throw e;
      }
    Log4J.finishMethod(logger, "removePlayer");
    }

  /** This method removes a character asociated with a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player owns.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database.
   *  @exception CharacterNotFoundException if the character doesn't exist or it is not owned by the player. */
  public void removeCharacter(Transaction trans, String username, String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "removeCharacter");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      int id=getDatabasePlayerId(trans,username);

      if(!hasCharacter(trans,username,character))
        {
        logger.warn("Database doesn't contains that username("+username+")-character("+character+")");
        throw new CharacterNotFoundException(username);
        }

      /** BUG: Remove the RPObject too */

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "delete from characters where player_id="+id+" and charname like '"+character+"'";

      stmt.execute(query);
      stmt.close();
      }
    catch(SQLException sqle)
      {
      logger.error("cannot remove character "+username+"-"+character,sqle);
      throw new GenericDatabaseException(sqle);
      }
    catch(PlayerNotFoundException e)
      {
      logger.warn("Database doesn't contains that username("+username+")");
      throw e;
      }
    Log4J.finishMethod(logger, "removeCharacter");
    }

  /** This method sets the account into one of the predefined states:
   *  active,inactive,banned
   *  don't forget to commit the changes.
   * @param username is the name of the player
   * @param status   the new status of the account
  **/
  public void setAccountStatus(Transaction trans, String username, String status) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "setAccountStatus");
    try
      {
      if(!validString(username) || !validString(status))
        {
        throw new SQLException("Trying to use invalid username '"+username+"' or status '"+status+"'.");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "update player set status='"+status+"' where username like '"+username+"'";
      logger.debug("setAccountStatus is executing query "+query);
      stmt.executeUpdate(query);
      stmt.close();
      }
    catch(SQLException sqle)
      {
      logger.error("setAccountStatus",sqle);
      throw new GenericDatabaseException(sqle);
      }
    Log4J.finishMethod(logger, "setAccountStatus");
    }

  public boolean verifyAccount(Transaction trans, PlayerEntryContainer.RuntimePlayerEntry.SecuredLoginInfo informations) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "verifyAccount");
    try
      {
      if(Hash.compare(Hash.hash(informations.clientNonce), informations.clientNonceHash) != 0)
        {
        logger.info("Different hashs for client Nonce");
        return false;
        }

      byte[] b1 = informations.key.decodeByteArray(informations.password);
      byte[] b2 = Hash.xor(informations.clientNonce, informations.serverNonce);
      if(b2 == null)
        {
        logger.info("B2 is null");
        return false;
        }

      byte[] password = Hash.xor(b1, b2);
      if(password == null)
        {
        logger.debug("Password is null");
        return false;
        }

      if(!validString(informations.userName))
        {
        throw new SQLException("Trying to use invalid characters username':"+informations.userName+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String hexPassword = Hash.toHexString(Hash.hash(password));
      String query = "select status from player where username like '"+informations.userName+"' and password like '"+hexPassword+"'";
      logger.debug("verifyAccount is executing query "+query);
      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        String account_status = result.getString("status");
        result.close();
        stmt.close();

        if("active".equals(account_status))
          {
          return true;
          }
        else
          {
          logger.info("Username/password is ok, but account is in status {"+account_status+"}");
          return false;
          }
        }
// This code is unused. I think it was a workaround some transaction issues on 0.90
//      else
//        {
//        Connection connection1 = ((JDBCTransaction)trans).getConnection();
//        Statement stmt1 = connection1.createStatement();
//        String query1 = "select * from player;";
//        logger.debug("verifyAccount is executing query "+query1);
//        ResultSet result1 = stmt1.executeQuery(query1);
//        while(result1.next())
//          {
//          logger.debug(result1.getString("id")+"\t"+result1.getString("username")+"\t"+result1.getString("password"));
//          }
//        result1.close();
//        }

      return false;
      }
    catch(Exception e)
      {
      logger.warn("error verifying account ",e);
      throw new GenericDatabaseException(e);
      }
    finally
      {
      Log4J.finishMethod(logger, "verifyAccount");
      }
    }


  /** This method returns the list of Login events as a array of Strings
   *  @param username is the name of the player
   *  @return an array of String containing the login events.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database. */
  public String[] getLoginEvent(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "getLoginEvent");
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

      logger.debug("getLoginEvent is executing query "+query);

      ResultSet result = stmt.executeQuery(query);
      Vector<String> vector = new Vector<String>();

      while(result.next())
        {
        LoginEvent login_event = new LoginEvent();

        login_event.address = result.getString("address");
        login_event.time    = (java.util.Date)result.getTimestamp("timedate");
        login_event.correct = result.getInt("result")!=0;
        vector.add(login_event.toString());
        }

      result.close();
      stmt.close();

      loginEvents = new String[vector.size()];
      loginEvents = (String[])vector.toArray(loginEvents);
      Log4J.finishMethod(logger, "getLoginEvent");
      return loginEvents;
      }
    catch(SQLException sqle)
      {
      logger.error("error in getLoginEvent",sqle);
      throw new GenericDatabaseException(sqle);
      }
    catch(PlayerNotFoundException e)
      {
      logger.warn("Database doesn't contains that username("+username+")");
      throw e;
      }
    }

  /** This method returns true if the player has that character or false if it hasn't
   *  @param username is the name of the player
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *  @exception PlayerNotFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(Transaction trans, String username, String character) throws PlayerNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "hasCharacter");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from  player,characters where username like '"+username+"' and charname like '"+character+"' and player.id=characters.player_id";

      logger.debug("hasCharacter is executing query "+query);

      ResultSet result = stmt.executeQuery(query);
      
      boolean characterExists=false;

      if(result.next())
        {
        if(result.getInt("amount")!=0)
          {
          characterExists=true;
          }
        }

      result.close();
      stmt.close();
      
      return characterExists;
      }
    catch(SQLException sqle)
      {
      logger.warn("error while checking if player "+username+" has character "+character,sqle);
      throw new GenericDatabaseException(sqle);
      }
    finally
      {
      Log4J.finishMethod(logger, "hasCharacter");
      }
    }

  /** This method add a Login event to the player
   *  @param username is the name of the player
   *  @param source the IP address of the player
   *  @param correctLogin true if the login has been correct.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database. */
  public void addLoginEvent(Transaction trans, String username, InetSocketAddress source, boolean correctLogin) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "addLoginEvent");
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
      String query = "insert into loginEvent(player_id,address,timedate,result) values("+id+",'"+source.getAddress().getHostAddress()+"',NULL,"+(correctLogin?1:0)+")";      
      stmt.execute(query);
      
      stmt.close();
      }
    catch(SQLException sqle)
      {
      logger.warn("error adding LoginEvent",sqle);
      throw new GenericDatabaseException(sqle);
      }
    finally
      {
      Log4J.finishMethod(logger, "addLoginEvent");
      }
    }

  /** This method add a character asociated to a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database.
   *  @exception CharacterAlreadyAddedException if that player-character exist in database.
   *  @exception GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void addCharacter(Transaction trans, String username, String character, RPObject object) throws PlayerNotFoundException, CharacterAlreadyAddedException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "addCharacter");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      if(hasCharacter(trans,username,character))
        {
        logger.debug("Database does contains that username("+username+")-character("+character+") already");
        throw new CharacterAlreadyAddedException(username);
        }
      else
        {
        Connection connection = ((JDBCTransaction)trans).getConnection();
        Statement stmt = connection.createStatement();

        int id=getDatabasePlayerId(trans,username);
        int object_id=storeRPObject(trans,object);

        String query = "insert into characters(player_id,charname,object_id) values("+id+",'"+character+"',"+object_id+")";
        stmt.execute(query);
        stmt.close();
        }
      }
    catch(Exception sqle)
      {
      // TODO: NOTE: HACK: IMHO it should be rolledback at the caller.
      trans.rollback();
      logger.warn("error addint Character "+username+", "+character,sqle);
      throw new GenericDatabaseException(username, sqle);
      }
    finally
      {
      Log4J.finishMethod(logger, "addCharacter");
      }
    }

  /** This method returns the number of Players that exist on database
   *  @return the number of players that exist on database */
  public int getPlayerCount(Transaction trans) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "getPlayerCount");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from player";

      logger.debug("getPlayerCount is executing query "+query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        int amount=result.getInt("amount");
        result.close();
        
        return amount;
        }

      result.close();
      stmt.close();

      return 0;
      }
    catch(SQLException sqle)
      {
      logger.warn("error getting player count",sqle);
      throw new GenericDatabaseException(sqle);
      }
    finally
      {
      Log4J.finishMethod(logger, "getPlayerCount");
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
   *  @exception PlayerNotFoundException  if the player doesn't exist in database.
   *  @exception CharacterNotFoundException  if the player-character doesn't exist in database.
   *  @exception GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void setRPObject(Transaction trans, String username, String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "setRPObject");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) from characters where charname like '"+character+"'";

      logger.debug("setRPObject is executing query "+query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        if(result.getInt(1)==0)
          {
          result.close();
          stmt.close();
          logger.debug("Database doesn't contains that username("+username+")-character("+character+")");
          throw new CharacterNotFoundException(username);
          }
        }

      result.close();
      stmt.close();

      storeRPObject(getTransaction(),object);
      }
    catch(SQLException sqle)
      {
      logger.warn("error setting RPObject",sqle);
      throw new GenericDatabaseException(sqle);
      }
//    catch(PlayerNotFoundException e)
//      {
//      logger.warn("Database doesn't contains that username("+username+")");
//      throw e;
//      }
    finally
      {
      Log4J.finishMethod(logger, "setRPObject");
      }
    }

  /** This method retrieves from Database the object for an existing player and character.
   *.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @return a RPObject that is the RPObject that represent this character in game.
   *
   *  @exception PlayerNotFoundException  if the player doesn't exist in database.
   *  @exception CharacterNotFoundException  if the player-character doesn't exist in database.
   *  @exception GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public RPObject getRPObject(Transaction trans, String username, String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    Log4J.startMethod(logger, "getRPObject");
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

      logger.debug("getRPObject is executing query "+query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        int object_id=result.getInt("object_id");
        result.close();
        stmt.close();

        return loadRPObject(getTransaction(),object_id);
        }
      else
        {
        result.close();
        stmt.close();
        
        throw new CharacterNotFoundException(character);
        }
      }
    catch(SQLException sqle)
      {
      logger.warn("error getting RPObject",sqle);
      throw new GenericDatabaseException(sqle);
      }
//    catch(PlayerNotFoundException e)
//      {
//      logger.warn("Database doesn't contains that username("+username+")",e);
//      throw e;
//      }
    catch(CharacterNotFoundException e)
      {
      logger.warn("Player("+username+") doesn't contains that character("+character+")",e);
      throw e;
      }
    catch (Exception e)
      {
      logger.warn("Error serializing character: ("+username+"/"+character+")",e);
      throw new GenericDatabaseException("Error serializing character("+username+"/"+character+")",e);
      }
    finally
      {
      Log4J.finishMethod(logger, "getRPObject");
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

    logger.debug("getDatabasePlayerId is executing query "+query);

    ResultSet result = stmt.executeQuery(query);

    if(result.next())
      {
      id = result.getInt("id");
      result.close();
      stmt.close();
      }
    else
      {
      result.close();
      stmt.close();
      throw new PlayerNotFoundException(username);
      }

    return(id);
    }

  protected boolean runDBScript(String file) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "runDBScript");

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
          logger.debug("runDBScript is executing query "+query);
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

      stmt.close();

      return ret;
      }
    catch(Exception e)
      {
      logger.warn("error running DBScript (file: "+file+")",e);
      throw new GenericDatabaseException(e);
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

      Log4J.finishMethod(logger, "runDBScript");
      }
    }

  private JDBCTransaction transaction;

  public Transaction getTransaction() throws GenericDatabaseException
    {
    if(transaction==null || !transaction.isValid())
      {
      logger.debug("Creating a new transaction with a new connection");
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
    Log4J.startMethod(logger, "createConnection");
    try
      {
      Class.forName((String)props.get("jdbc_class")).newInstance();

      Properties connInfo = new Properties();

      connInfo.put("user", props.get("jdbc_user"));
      connInfo.put("password", props.get("jdbc_pwd"));
      connInfo.put("charSet", "UTF-8");

      Connection conn = DriverManager.getConnection((String)props.get("jdbc_url"), connInfo);

      conn.setAutoCommit(false);
      conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

      return conn;
      }
    catch (Exception e)
      {
      logger.warn("error creating Connection",e);
      throw new GenericDatabaseException(e);
      }
    finally
      {
      Log4J.finishMethod(logger, "createConnection");
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

    public int next() throws SQLException
      {
      return set.getInt("object_id");
      }
    
    public void finalize()
      {
      try
        {
        set.close();
        }
      catch(SQLException e)
        {
        logger.error("Finalize RPObjectIterator: ",e);
        }
      }
    }

  public RPObjectIterator iterator(Transaction trans)
    {
    Log4J.startMethod(logger, "iterator");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select object_id from rpobject where slot_id=0";

      logger.debug("iterator is executing query "+query);
      ResultSet result = stmt.executeQuery(query);
      return new RPObjectIterator(result);
      }
    catch(SQLException e)
      {
      logger.warn("error executing query",e);
      return null;
      }
    finally
      {
      Log4J.finishMethod(logger, "iterator");
      }
    }

  public boolean hasRPObject(Transaction trans, int id)
    {
    Log4J.startMethod(logger, "hasRPObject");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from rpobject where object_id="+id;

      logger.debug("hasRPObject is executing query "+query);

      ResultSet result = stmt.executeQuery(query);
      
      boolean rpObjectExists=false;

      if(result.next())
        {
        if(result.getInt("amount")!=0)
          {
          rpObjectExists=true;
          }
        }

      result.close();
      stmt.close();
      
      return rpObjectExists;
      }
    catch(SQLException e)
      {
      logger.error("error checking if database has RPObject ("+id+")",e);
      return false;
      }
    finally
      {
      Log4J.finishMethod(logger, "hasRPObject");
      }
    }


  public RPObject loadRPObject(Transaction trans, int id) throws Exception
    {
    Log4J.startMethod(logger, "loadRPObject");
    try
      {
      if(hasRPObject(trans,id))
        {
        RPObject object=new RPObject();

        loadRPObject(trans,object,id);

        return object;
        }
      else
        {
        throw new SQLException("RPObject not found: "+id);
        }
      }
    catch(Exception e)
      {
      logger.error("error loading RPObject ("+id+")",e);
      throw e;
      }
    finally
      {
      Log4J.finishMethod(logger, "loadRPObject");
      }
    }

  private void loadRPObject(Transaction trans, RPObject object,int object_id) throws SQLException, SlotAlreadyAddedException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt = connection.createStatement();
    String query="select name,value from rpattribute where object_id="+object_id+";";
    logger.debug("loadRPObject is executing query "+query);

    ResultSet result = stmt.executeQuery(query);

    while(result.next())
      {
      String name=UnescapeString(result.getString("name"));
      String value=UnescapeString(result.getString("value"));
      object.put(name, value);
      }

    result.close();

    query = "select name,capacity, slot_id from rpslot where object_id="+object_id+";";
    logger.debug("loadRPObject is executing query "+query);
    result = stmt.executeQuery(query);
    while(result.next())
      {
      RPSlot slot=new RPSlot(UnescapeString(result.getString("name")));
      slot.setCapacity(result.getInt("capacity"));

      object.addSlot(slot);

      int slot_id=result.getInt("slot_id");

      query = "select object_id from rpobject where slot_id="+slot_id+";";
      logger.debug("loadRPObject is executing query "+query);
      ResultSet resultSlot = connection.createStatement().executeQuery(query);

      while(resultSlot.next())
        {
        RPObject slotObject=new RPObject();

        loadRPObject(trans,slotObject,resultSlot.getInt("object_id"));
        slot.add(slotObject);
        }

      resultSlot.close();
      }

    result.close();
    stmt.close();
    }

  public void deleteRPObject(Transaction trans, int id) throws SQLException
    {
    Log4J.startMethod(logger, "deleteRPObject");
    Connection connection = ((JDBCTransaction)trans).getConnection();

    try
      {
      if(hasRPObject(trans,id))
        {
        deleteRPObject(connection,id);
        }
      else
        {
        throw new SQLException("RPObject not found: "+id);
        }
      }
    catch(SQLException e)
      {
      logger.warn("error deleting RPObject ("+id+")",e);
      throw e;
      }
    finally
      {
      Log4J.finishMethod(logger, "deleteRPObject");
      }
    }

  private void deleteRPObject(Connection connection, int id) throws SQLException
    {
    Statement stmt = connection.createStatement();
    String query=null;

    query = "select rpobject.object_id from rpobject, rpslot where rpslot.object_id="+id+" and rpobject.slot_id=rpslot.slot_id;";
    logger.debug("deleteRPObject is executing query "+query);

    ResultSet result = stmt.executeQuery(query);

    while(result.next())
      {
      deleteRPObject(connection,result.getInt(1));
      }

    result.close();

    query = "delete from rpslot where object_id="+id+";";
    logger.debug("deleteRPObject is executing query "+query);
    stmt.execute(query);
    query = "delete from rpattribute where object_id="+id+";";
    logger.debug("deleteRPObject is executing query "+query);
    stmt.execute(query);
    query = "delete from rpobject where object_id="+id+";";
    logger.debug("deleteRPObject is executing query "+query);
    stmt.execute(query);

    stmt.close();
    }

  public synchronized int storeRPObject(Transaction trans, RPObject object) throws SQLException
    {
    Log4J.startMethod(logger, "storeRPObject");

    try
      {
      if(object.has("#db_id"))
        {
        int object_id=object.getInt("#db_id");

        if(hasRPObject(trans,object_id))
          {
          deleteRPObject(trans,object_id);
          }
        }

      return storeRPObject(trans,object,0);
      }
    catch(AttributeNotFoundException e)
      {
      logger.warn("attribute not found from object ("+object+")",e);
      throw new SQLException(e.getMessage());
      }
    catch(SQLException e)
      {
      logger.warn("cannot store object ("+object+")",e);
      throw e;
      }
    finally
      {
      Log4J.finishMethod(logger, "storeRPObject");
      }
    }

  private int storeRPObject(Transaction trans, RPObject object, int slot_id) throws SQLException, AttributeNotFoundException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt = connection.createStatement();

    String query;
    int object_id;

    if(object.has("#db_id"))
      {
      object_id=object.getInt("#db_id");

      query="insert into rpobject(object_id,slot_id) values("+object_id+","+slot_id+")";
      logger.debug("storeRPObject is executing query "+query);
      stmt.execute(query);
      }
    else
      {
      query="insert into rpobject(object_id,slot_id) values(NULL,"+slot_id+")";
      logger.debug("storeRPObject is executing query "+query);
      stmt.execute(query);

      /* We get the stored id */
      query = "select LAST_INSERT_ID() as inserted_id from rpobject";
      logger.debug("storeRPObject is executing query "+query);
      ResultSet result = stmt.executeQuery(query);

      result.next();
      object_id=result.getInt("inserted_id");

      /** We update the object to contain the database reference, so we can know in
       *  the future if this object was stored on database. */
      object.put("#db_id",object_id);
      }

    Iterator it=object.iterator();
    RPClass rpClass = object.getRPClass();

    while(it.hasNext())
      {
      String attrib=(String) it.next();
      if(rpClass.isStorable(attrib))
        {
        String value = object.get(attrib);

        query = "insert into rpattribute(object_id,name,value) values(" + object_id + ",'" + EscapeString(attrib) + "','" + EscapeString(value) + "');";
        logger.debug("storeRPObject is executing query "+query);
        stmt.execute(query);
        }
      }

    for(Iterator<RPSlot> sit=object.slotsIterator(); sit.hasNext();)
      {
      RPSlot slot= sit.next();

      query = "insert into rpslot(object_id,name,capacity,slot_id) values("+object_id+",'"+EscapeString(slot.getName())+"',"+slot.getCapacity()+",NULL);";
      logger.debug("storeRPObject is executing query "+query);
      stmt.execute(query);
      query = "select slot_id from rpslot where object_id="+object_id+" and name like '"+EscapeString(slot.getName())+"';";
      logger.debug("storeRPObject is executing query "+query);

      int object_slot_id;
      ResultSet slot_result = stmt.executeQuery(query);

      if(slot_result.next())
        {
        object_slot_id = slot_result.getInt(1);
        }
      else
        {
        throw new SQLException("Not able to select RPSlot("+slot.getName()+") that have just been inserted");
        }
  
      slot_result.close();

      Iterator oit=slot.iterator();

      while(oit.hasNext())
        {
        RPObject objectInSlot=(RPObject)oit.next();

        storeRPObject(trans,objectInSlot,object_slot_id);
        }
      }

    stmt.close();

    return object_id;
    }

  public void addStatisticsEvent(Transaction trans, Statistics.Variables var) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "addStatisticsEvent");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();

      String query = "insert into statistics(timedate, bytes_send, bytes_recv, players_login, players_logout, players_timeout, players_online) values(NULL,"+
        var.get("Bytes send")+","+
        var.get("Bytes recv")+","+
        var.get("Players login")+","+
        var.get("Players logout")+","+
        var.get("Players timeout")+","+
        var.get("Players online")+")";
      stmt.execute(query);
      stmt.close();
      }
    catch(SQLException sqle)
      {
      logger.warn("error adding statistics event",sqle);
      throw new GenericDatabaseException(sqle);
      }
    finally
      {
      Log4J.finishMethod(logger, "addStatisticsEvent");
      }
    }

  public void addGameEvent(Transaction trans, String source, String event, String... params) throws GenericDatabaseException
    {
    Log4J.startMethod(logger, "addStatisticsEvent");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      
      StringBuffer param=new StringBuffer();
      
      if(params.length>1)
        {
        for(int i=1;i<params.length;i++)
          {
          param.append(params[i]);
          param.append(" ");
          }
        }
      
      try
        {
        if(!validString(source) || !validString(event) || !validString(param.toString()))
          {
          logger.info("Game event not logged because invalid strings: \""+source+"\",\""+event+"\",\""+param+"\"");
          return;
          }
        }
      catch(Exception e)
        {
        logger.info("Game event not logged because invalid strings: \""+source+"\",\""+event+"\",\""+param+"\"",e);
        return;
        }
        
      String firstParam=(params.length>0?params[0]:"");

      String query = "insert into gameEvents(timedate, source, event, param1, param2) values(NULL,'"+source+"','"+event+"','"+firstParam+"','"+param.toString()+"')";
      stmt.execute(query);
      stmt.close();
      }
    catch(SQLException sqle)
      {
      logger.warn("error adding game event",sqle);
      throw new GenericDatabaseException(sqle);
      }
    finally
      {
      Log4J.finishMethod(logger, "addStatisticsEvent");
      }
    }
  }
