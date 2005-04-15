/* $Id: JDBCPlayerDatabase.java,v 1.5 2005/04/15 07:06:54 quisar Exp $ */
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

import java.sql.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

import marauroa.server.*;
import marauroa.common.*;
import marauroa.common.game.*;
import marauroa.common.crypto.Hash;
import java.math.BigInteger;


/** This is JDBC interface to the database.
 *  Actually it is limited to MySQL because we are using the AUTO_INCREMENT keyword. */
public class JDBCPlayerDatabase implements IPlayerDatabase
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

  private static IPlayerDatabase playerDatabase=null;

  /** connection info **/
  private Properties connInfo;

  /** Constructor that connect using a set of Properties.
   *  @param connInfo a Properties set with the options to create the database.
   *  Refer to JDBC Database HOWTO document. */
  private JDBCPlayerDatabase(Properties connInfo) throws NoDatabaseConfException, GenericDatabaseException
    {
    this.connInfo=connInfo;
    runDBScript("marauroa/server/marauroa_init.sql");
    }

  private static IPlayerDatabase resetDatabaseConnection() throws Exception
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
    Logger.trace("JDBCPlayerDatabase::getDatabase",">");
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
      Logger.thrown("JDBCPlayerDatabase::getDatabase","X",e);
      throw new NoDatabaseConfException();
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::getDatabase","<");
      }
    }

  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public boolean hasPlayer(Transaction trans, String username) throws GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::hasPlayer",">");
    try
      {
      if(!validString(username))
        {
        throw new SQLException("Trying to use invalid characters at username:'"+username+"'");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from  player where username like '"+username+"'";

      Logger.trace("JDBCPlayerDatabase::hasPlayer","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        if(result.getInt("amount")!=0)
          {
          return true;
          }
        }
      return false;
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::hasPlayer","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::hasPlayer","<");
      }
    }

  /** This method returns the lis of character that the player pointed by username has.
   *  @param username the name of the player from which we are requesting the list of characters.
   *  @return an array of String with the characters
   *  @exception PlayerNotFoundException if that player does not exists. */
  public String[] getCharactersList(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::getCharacterList",">");
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

      Logger.trace("JDBCPlayerDatabase::getCharacterList","D",query);

      ResultSet charactersSet = stmt.executeQuery(query);
      Vector<String> vector = new Vector<String>();

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
      Logger.thrown("JDBCPlayerDatabase::getCharacterList","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.trace("JDBCPlayerDatabase::getCharactersList","X","Database doesn't contains that username("+username+")");
      Logger.thrown("JDBCPlayerDatabase::getCharactersList","X",e);
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::getCharacterList","<");
      }
    }

    /** This method add the player to database with username and password as identificator.
     *  @param username is the name of the player
     *  @param password is a string used to verify access.
     *  @exception PlayerAlreadyAddedExceptio if the player is already in database */
    public void addPlayer(Transaction trans, String username, byte[] password, String email) throws PlayerAlreadyAddedException, GenericDatabaseException
      {
      Logger.trace("JDBCPlayerDatabase::addPlayer",">");
      try
        {
        if(!validString(username) || !validString(email) )
          {
          throw new SQLException("Trying to use invalid characters username':"+username+"' and email:'"+email+"'");
          }

        Connection connection = ((JDBCTransaction)trans).getConnection();
        Statement stmt = connection.createStatement();
        String query = "select id from player where username like '"+username+"'";

        Logger.trace("JDBCPlayerDatabase::addPlayer","D",query);

        ResultSet result = stmt.executeQuery(query);

        if(result.next())
          {
          Logger.trace("JDBCPlayerDatabase::addPlayer","W","Database already contains that username("+username+")");
          throw new PlayerAlreadyAddedException(username);
          }
        else
          {
          /* We store the hashed version of the password */
          password = Hash.hash(password);

          query = "insert into player values(NULL,'"+username+"','"+ Hash.toHexString(password)+"','"+email+"',NULL,DEFAULT)";
          stmt.execute(query);
          }
        }
      catch(SQLException sqle)
        {
        Logger.thrown("JDBCPlayerDatabase::addPlayer","X",sqle);
        throw new GenericDatabaseException(sqle.getMessage());
        }
      finally
        {
        Logger.trace("JDBCPlayerDatabase::addPlayer","<");
        }
      }

  /** This method remove the player with usernae from database.
   *  @param username is the name of the player
   *  @exception PlayerNotFoundException if the player doesn't exist in database. */
  public void removePlayer(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::removePlayer",">");
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
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::removePlayer","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::removePlayer","X",e);
      Logger.trace("JDBCPlayerDatabase::removePlayer","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::removePlayer","<");
      }
    }

  /** This method removes a character asociated with a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player owns.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database.
   *  @exception CharacterNotFoundException if the character doesn't exist or it is not owned by the player. */
  public void removeCharacter(Transaction trans, String username, String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::removeCharacter",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      int id=getDatabasePlayerId(trans,username);

      if(!hasCharacter(trans,username,character))
        {
        Logger.trace("JDBCPlayerDatabase::removeCharacter","X","Database doesn't contains that username("+username+")-character("+character+")");
        throw new CharacterNotFoundException(username);
        }

      /** BUG: Remove the RPObject too */

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "delete from characters where player_id="+id+" and charname like '"+character+"'";

      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::removeCharacter","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::removeCharacter","X",e);
      Logger.trace("JDBCPlayerDatabase::removeCharacter","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::removeCharacter","<");
      }
    }

  /** This method sets the account into one of the predefined states:
   *  active,inactive,banned
   *  don't forget to commit the changes.
   * @param username is the name of the player
   * @param status   the new status of the account
  **/
  public void setAccountStatus(Transaction trans, String username, String status) throws GenericDatabaseException
    {
    try
      {
      Logger.trace("JDBCPlayerDatabase::setAccountStatus",">");
      if(!validString(username) || !validString(status))
        {
        throw new SQLException("Trying to use invalid username '"+username+"' or status '"+status+"'.");
        }

      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "update player set status='"+status+"' where username like '"+username+"'";
      Logger.trace("JDBCPlayerDatabase::setAccountStatus","D",query);
      stmt.executeUpdate(query);
      }
    catch(SQLException sqle)
      {
      Logger.trace("JDBCPlayerDatabase::setAccountStatus","X",sqle.getMessage());
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::setAccountStatus","<");
      }
    }

    public boolean verifyAccount(Transaction trans, PlayerEntryContainer.RuntimePlayerEntry.SecuredLoginInfo informations) throws GenericDatabaseException
    {
      Logger.trace("JDBCPlayerDatabase::verifyAccount",">");
      try {
        if(Hash.compare(Hash.hash(informations.clientNonce), informations.clientNonceHash) != 0) {
          return false;
        }
        byte[] b1 = informations.key.decodeByteArray(informations.password);
        byte[] b2 = Hash.xor(informations.clientNonce, informations.serverNonce);
        if(b2 == null) {
          return false;
        }
        byte[] password = Hash.xor(b1, b2);
        if(password == null) {
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
      Logger.trace("JDBCPlayerDatabase::verifyAccount","D",query);
      ResultSet result = stmt.executeQuery(query);

      if(result.next())
      {
        String account_status = result.getString("status");

        if("active".equals(account_status))
        {
          return true;
        }
        else
        {
          Logger.trace("JDBCPlayerDatabase::verifyAccount","D","Username/password is ok, but account is in status {"+account_status+"}");
          return false;
        }
      }

      return false;
    }
    catch(Exception e)
    {
      Logger.thrown("JDBCPlayerDatabase::verifyAccount","X",e);
      throw new GenericDatabaseException(e.getMessage());
    }
    finally
    {
      Logger.trace("JDBCPlayerDatabase::verifyAccount","<");
    }
  }


  /** This method returns the list of Login events as a array of Strings
   *  @param username is the name of the player
   *  @return an array of String containing the login events.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database. */
  public String[] getLoginEvent(Transaction trans, String username) throws PlayerNotFoundException, GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::getLoginEvent",">");
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

      Logger.trace("JDBCPlayerDatabase::getLoginEvent","D",query);

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

      loginEvents = new String[vector.size()];
      loginEvents = (String[])vector.toArray(loginEvents);
      return loginEvents;
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::getLoginEvent","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::getLoginEvent","X",e);
      Logger.trace("JDBCPlayerDatabase::getLoginEvent","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::getLoginEvent","<");
      }
    }

  /** This method returns true if the player has that character or false if it hasn't
   *  @param username is the name of the player
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *  @exception PlayerNotFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(Transaction trans, String username, String character) throws PlayerNotFoundException, GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::hasCharacter",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      int id=getDatabasePlayerId(trans,username);
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from  player,characters where username like '"+username+"' and charname like '"+character+"' and player.id=characters.player_id";

      Logger.trace("JDBCPlayerDatabase::hasCharacter","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        if(result.getInt("amount")!=0)
          {
          return true;
          }
        }
      return false;
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::hasCharacter","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::hasCharacter","X",e);
      Logger.trace("JDBCPlayerDatabase::hasCharacter","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::hasCharacter","<");
      }
    }

  /** This method add a Login event to the player
   *  @param username is the name of the player
   *  @param source the IP address of the player
   *  @param correctLogin true if the login has been correct.
   *  @exception PlayerNotFoundException  if the player doesn't exist in database. */
  public void addLoginEvent(Transaction trans, String username, InetSocketAddress source, boolean correctLogin) throws GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::addLoginEvent",">");
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
      String query = "insert into loginEvent values("+id+",'"+source.getAddress().getHostAddress()+"',NULL,"+(correctLogin?1:0)+")";
      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::addLoginEvent","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::addLoginEvent","<");
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
    Logger.trace("JDBCPlayerDatabase::addCharacter",">");
    try
      {
      if(!validString(username) || !validString(character))
        {
        throw new SQLException("Trying to use invalid characters username':"+username+"' and character:'"+character+"'");
        }

      if(hasCharacter(trans,username,character))
        {
        Logger.trace("JDBCPlayerDatabase::addCharacter","X","Database does contains that username("+username+")-character("+character+")");
        throw new CharacterAlreadyAddedException(username);
        }
      else
        {
        Connection connection = ((JDBCTransaction)trans).getConnection();
        Statement stmt = connection.createStatement();

        int id=getDatabasePlayerId(trans,username);
        int object_id=storeRPObject(trans,object);

        String query = "insert into characters values("+id+",'"+character+"',"+object_id+")";
        stmt.execute(query);
        }
      }
    catch(Exception sqle)
      {
      trans.rollback();
      Logger.thrown("JDBCPlayerDatabase::addCharacter","X",sqle);
      throw new GenericDatabaseException(username);
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::addCharacter","<");
      }
    }

  /** This method returns the number of Players that exist on database
   *  @return the number of players that exist on database */
  public int getPlayerCount(Transaction trans) throws GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::getPlayerCount",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from player";

      Logger.trace("JDBCPlayerDatabase::getPlayerCount","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        return result.getInt("amount");
        }
      return 0;
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::getPlayerCount","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::getPlayerCount","<");
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
    Logger.trace("JDBCPlayerDatabase::setRPObject",">");
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

      Logger.trace("JDBCPlayerDatabase::setRPObject","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        if(result.getInt(1)==0)
          {
          Logger.trace("JDBCPlayerDatabase::setRPObject","X","Database doesn't contains that username("+username+")-character("+character+")");
          throw new CharacterNotFoundException(username);
          }
        }

      storeRPObject(getTransaction(),object);
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::setRPObject","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::setRPObject","X",e);
      Logger.trace("JDBCPlayerDatabase::setRPObject","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::setRPObject","<");
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
    Logger.trace("JDBCPlayerDatabase::getRPObject",">");
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

      Logger.trace("JDBCPlayerDatabase::getRPObject","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        int object_id=result.getInt("object_id");
        return loadRPObject(getTransaction(),object_id);
        }
      else
        {
        throw new CharacterNotFoundException(character);
        }
      }
    catch(SQLException sqle)
      {
      Logger.thrown("JDBCPlayerDatabase::getRPObject","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::getRPObject","X",e);
      Logger.trace("JDBCPlayerDatabase::getRPObject","X","Database doesn't contains that username("+username+")");
      throw e;
      }
    catch(CharacterNotFoundException e)
      {
      Logger.thrown("JDBCPlayerDatabase::getRPObject","X",e);
      Logger.trace("JDBCPlayerDatabase::getRPObject","X","Player("+username+") doesn't contains that character("+character+")");
      throw e;
      }
    catch (Exception e)
      {
      Logger.thrown("JDBCPlayerDatabase::getRPObject","X",e);
      Logger.trace("JDBCPlayerDatabase::getRPObject","X","Error serializing character: "+e.getMessage());
      throw new GenericDatabaseException("Error serializing character: "+e.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::getRPObject","<");
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

    Logger.trace("JDBCPlayerDatabase::getDatabasePlayerId","D",query);

    ResultSet result = stmt.executeQuery(query);

    if(result.next())
      {
      id = result.getInt("id");
      }
    else
      {
      throw new PlayerNotFoundException(username);
      }
    return(id);
    }

  private boolean reInitDB() throws GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::reInitDB",">");
    try
      {
      return (runDBScript("marauroa/server/marauroa_drop.sql") && runDBScript("marauroa/server/marauroa_init.sql"));
      }
    catch(GenericDatabaseException e)
      {
      Logger.thrown("JDBCPlayerDatabase::reInitDB","X",e);
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::reInitDB","<");
      }
    }

  private boolean runDBScript(String file) throws GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::runDBScript",">");

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
          Logger.trace("JDBCPlayerDatabase::runDBScript","D",query);
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
      Logger.thrown("JDBCPlayerDatabase::runDBScript","X",e);
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

      Logger.trace("JDBCPlayerDatabase::runDBScript","<");
      }
    }

  private JDBCTransaction transaction;

  public Transaction getTransaction() throws GenericDatabaseException
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
    Logger.trace("JDBCPlayerDatabase::createConnection",">");
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
      Logger.thrown("JDBCPlayerDatabase::createConnection","X",e);
      throw new GenericDatabaseException(e.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::createConnection","<");
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
    }

  public RPObjectIterator iterator(Transaction trans)
    {
    Logger.trace("JDBCPlayerDatabase::iterator",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select object_id from rpobject where slot_id=0";

      Logger.trace("JDBCPlayerDatabase::hasRPObject","D",query);
      ResultSet result = stmt.executeQuery(query);
      return new RPObjectIterator(result);
      }
    catch(SQLException e)
      {
      Logger.thrown("JDBCPlayerDatabase::iterator","X",e);
      return null;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::iterator","<");
      }
    }

  public boolean hasRPObject(Transaction trans, int id)
    {
    Logger.trace("JDBCPlayerDatabase::hasRPObject",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();
      Statement stmt = connection.createStatement();
      String query = "select count(*) as amount from rpobject where object_id="+id;

      Logger.trace("JDBCPlayerDatabase::hasRPObject","D",query);

      ResultSet result = stmt.executeQuery(query);

      if(result.next())
        {
        if(result.getInt("amount")!=0)
          {
          return true;
          }
        }

      return false;
      }
    catch(SQLException e)
      {
      Logger.thrown("JDBCPlayerDatabase::hasRPObject","X",e);
      return false;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::hasRPObject","<");
      }
    }


  public RPObject loadRPObject(Transaction trans, int id) throws Exception
    {
    Logger.trace("JDBCPlayerDatabase::loadRPObject",">");
    try
      {
      Connection connection = ((JDBCTransaction)trans).getConnection();

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
      Logger.thrown("JDBCPlayerDatabase::loadRPObject","X",e);
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::loadRPObject","<");
      }
    }

  private void loadRPObject(Transaction trans, RPObject object,int object_id) throws SQLException, SlotAlreadyAddedException
    {
    Connection connection = ((JDBCTransaction)trans).getConnection();
    Statement stmt = connection.createStatement();
    String query="select name,value from rpattribute where object_id="+object_id+";";
    Logger.trace("JDBCPlayerDatabase::loadRPObject","D",query);

    ResultSet result = stmt.executeQuery(query);

    while(result.next())
      {
      String name=UnescapeString(result.getString("name"));
      String value=UnescapeString(result.getString("value"));
      object.put(name, value);
      }

    query = "select name,slot_id from rpslot where object_id="+object_id+";";
    Logger.trace("JDBCPlayerDatabase::loadRPObject","D",query);
    result = stmt.executeQuery(query);
    while(result.next())
      {
      RPSlot slot=new RPSlot(UnescapeString(result.getString("name")));

      object.addSlot(slot);

      int slot_id=result.getInt("slot_id");

      query = "select object_id from rpobject where slot_id="+slot_id+";";
      Logger.trace("JDBCPlayerDatabase::loadRPObject","D",query);
      ResultSet resultSlot = connection.createStatement().executeQuery(query);

      while(resultSlot.next())
        {
        RPObject slotObject=new RPObject();

        loadRPObject(trans,slotObject,resultSlot.getInt("object_id"));
        slot.add(slotObject);
        }
      }
    }

  public void deleteRPObject(Transaction trans, int id) throws SQLException
    {
    Logger.trace("JDBCPlayerDatabase::deleteRPObject",">");
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
      Logger.thrown("JDBCPlayerDatabase::deleteRPObject","X",e);
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::deleteRPObject","<");
      }
    }

  private void deleteRPObject(Connection connection, int id) throws SQLException
    {
    Statement stmt = connection.createStatement();
    String query=null;

    query = "select rpobject.object_id from rpobject, rpslot where rpslot.object_id="+id+" and rpobject.slot_id=rpslot.slot_id;";
    Logger.trace("JDBCPlayerDatabase::deleteRPObject","D",query);

    ResultSet result = stmt.executeQuery(query);

    while(result.next())
      {
      deleteRPObject(connection,result.getInt(1));
      }

    query = "delete from rpslot where object_id="+id+";";
    Logger.trace("JDBCPlayerDatabase::deleteRPObject","D",query);
    stmt.execute(query);
    query = "delete from rpattribute where object_id="+id+";";
    Logger.trace("JDBCPlayerDatabase::deleteRPObject","D",query);
    stmt.execute(query);
    query = "delete from rpobject where object_id="+id+";";
    Logger.trace("JDBCPlayerDatabase::deleteRPObject","D",query);
    stmt.execute(query);
    }

  public int storeRPObject(Transaction trans, RPObject object) throws SQLException
    {
    Logger.trace("JDBCPlayerDatabase::storeRPObject",">");
    Connection connection = ((JDBCTransaction)trans).getConnection();

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
      Logger.thrown("JDBCPlayerDatabase::storeRPObject","X",e);
      throw new SQLException(e.getMessage());
      }
    catch(SQLException e)
      {
      Logger.thrown("JDBCPlayerDatabase::storeRPObject","X",e);
      throw e;
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::storeRPObject","<");
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

      query="insert into rpobject values("+object_id+","+slot_id+")";
      Logger.trace("JDBCPlayerDatabase::storeRPObject","D",query);
      stmt.execute(query);
      }
    else
      {
      query="insert into rpobject values(NULL,"+slot_id+")";
      Logger.trace("JDBCPlayerDatabase::storeRPObject","D",query);
      stmt.execute(query);

      /* We get the stored id */
      query = "select LAST_INSERT_ID() as inserted_id from rpobject";
      Logger.trace("JDBCPlayerDatabase::getDatabasePlayerId","D",query);
      ResultSet result = stmt.executeQuery(query);

      result.next();
      object_id=result.getInt("inserted_id");

      /** We update the object to contain the database reference, so we can know in
       *  the future if this object was stored on database. */
      object.put("#db_id",object_id);
      }

    Iterator it=object.iterator();

    while(it.hasNext())
      {
      String attrib=(String) it.next();
      String value=object.get(attrib);

      query = "insert into rpattribute values("+object_id+",'"+EscapeString(attrib)+"','"+EscapeString(value)+"');";
      Logger.trace("JDBCPlayerDatabase::storeRPObject","D",query);
      stmt.execute(query);
      }

    for(Iterator<RPSlot> sit=object.slotsIterator(); sit.hasNext();)
      {
      RPSlot slot= sit.next();

      query = "insert into rpslot values("+object_id+",'"+EscapeString(slot.getName())+"',NULL);";
      Logger.trace("JDBCPlayerDatabase::storeRPObject","D",query);
      stmt.execute(query);
      query = "select slot_id from rpslot where object_id="+object_id+" and name like '"+EscapeString(slot.getName())+"';";
      Logger.trace("JDBCPlayerDatabase::storeRPObject","D",query);

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

      Iterator oit=slot.iterator();

      while(oit.hasNext())
        {
        RPObject objectInSlot=(RPObject)oit.next();

        storeRPObject(trans,objectInSlot,object_slot_id);
        }
      }

    return object_id;
    }

  public void addStatisticsEvent(Transaction trans, Statistics.GatheredVariables var) throws GenericDatabaseException
    {
    Logger.trace("JDBCPlayerDatabase::addStatisticsEvent",">");
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
      Logger.thrown("JDBCPlayerDatabase::addLoginEvent","X",sqle);
      throw new GenericDatabaseException(sqle.getMessage());
      }
    finally
      {
      Logger.trace("JDBCPlayerDatabase::addLoginEvent","<");
      }
    }
  }
