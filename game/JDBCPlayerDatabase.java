package marauroa.game;

import java.sql.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Vector;
import marauroa.net.InputSerializer;
import marauroa.net.OutputSerializer;

import marauroa.*;


/** This is JDBC interface to the database.
 *  Actually it is limited to MySQL because we are using the AUTO_INCREMENT keyword.
 */
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

  /** A connection to the JDBC database */
  private Connection connection;  
  private static PlayerDatabase playerDatabase=null;

  /** Constructor that connect using a set of Properties.
   *  @param connInfo a Properties set with the options to create the database.
   *  Refer to JDBC Database HOWTO document. */
  private JDBCPlayerDatabase(Properties connInfo) throws NoDatabaseConfException
    {
    connection=createConnection(connInfo);
    if(connection==null)
      {
      throw new NoDatabaseConfException();
      }

    /* TODO: Change for initDB() */      
    /* NOTE: By now tests expect database to be empty. */
    reInitDB();
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
        Configuration conf=Configuration.getConfiguration();
    
        Properties props = new Properties();        
        props.put("jdbc_url",conf.get("jdbc_url"));
        props.put("jdbc_class",conf.get("jdbc_class"));
        props.put("jdbc_user",conf.get("jdbc_user"));
        props.put("jdbc_pwd",conf.get("jdbc_pwd"));

        playerDatabase=new JDBCPlayerDatabase(props);
        }

      return playerDatabase;
      }
    catch(Throwable e)
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
  public boolean hasPlayer(String username)
    {
    marauroad.trace("JDBCPlayerDatabase::hasPlayer",">");
    boolean has=false;

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player where username like '"+username+"'";
      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        if(result.getInt(1)!=0)
          {
          has=true;
          }
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::hasPlayer","X",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::hasPlayer","<");
    return has;
    }
  
  /** This method returns the lis of character that the player pointed by username has.
   *  @param username the name of the player from which we are requesting the list of characters.
   *  @return an array of String with the characters
   *  @throw PlayerNotFoundException if that player does not exists. */
  public String[] getCharactersList(String username) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::getCharacterList",">");
    
    try
      {
      String[] characters = null;

      int id=getDatabasePlayerId(username);
        
      Statement stmt = connection.createStatement();
      String query = "select charname from characters where player_id="+id;
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
      throw new PlayerNotFoundException();
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
  public void addPlayer(String username, String password) throws PlayerAlreadyAddedException
    {
    marauroad.trace("JDBCPlayerDatabase::addPlayer",">");

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";
      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        marauroad.trace("JDBCPlayerDatabase::addPlayer","W","Database already contains that username("+username+")");
        throw new PlayerAlreadyAddedException();
        }
      else
        {
        query = "insert into player values(NULL,'"+username+"','"+password+"')";
        stmt.execute(query);
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addPlayer","X",sqle.getMessage());
      throw new PlayerAlreadyAddedException();
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::addPlayer","<");
      }
    }
  
  /** This method remove the player with usernae from database.
   *  @param username is the name of the player
   *  @throws PlayerNotFoundException if the player doesn't exist in database. */
  public void removePlayer(String username) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::removePlayer",">");

    try
      {
      int id=getDatabasePlayerId(username);
      
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
      throw new PlayerNotFoundException();
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
  public void removeCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::removeCharacter",">");
    
    try
      {
      int id=getDatabasePlayerId(username);
      
      Statement stmt = connection.createStatement();
      String query = "delete from characters where player_id="+id+" and charname like '"+character+"'";
      stmt.execute(query);
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::removeCharacter","X",sqle.getMessage());
      throw new PlayerNotFoundException();
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
  public boolean verifyAccount(String username, String password)
    {
    marauroad.trace("JDBCPlayerDatabase::verifyAccount",">");

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player where username like '"+username+"' and password like '"+password+"'";

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
      marauroad.trace("JDBCPlayerDatabase::verifyAccount","E",sqle.getMessage());
      return false;
      }
    finally
      {
      marauroad.trace("JDBCPlayerDatabase::verifyAccount","<");
      }
    }
  
  public String[] getLoginEvent(String username) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::getLoginEvent",">");

    String[] loginEvents = null;
    try
      {
      int id=getDatabasePlayerId(username);
      
      Statement stmt = connection.createStatement();
      String query = "select address,timedate,result from loginEvent where player_id="+id+" order by timedate";

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
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","E",sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","E","Database doesn't contains that username("+username+")");
      throw e;
      }
    
    marauroad.trace("JDBCPlayerDatabase::getLoginEvent","<");
    return(loginEvents);
    }
  
  public boolean hasCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::hasCharacter",">");

    boolean ret = false;
    try
      {
      int id=getDatabasePlayerId(username);

      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player,characters where username like '"+username+"' and charname like '"+character+"' and player.id=characters.player_id";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        if(result.getInt(1)!=0)
          {
          ret = true;
          }
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::hasCharacter","E",sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::hasCharacter","E","Database doesn't contains that username("+username+")");
      throw e;
      }
    
    marauroad.trace("JDBCPlayerDatabase::hasCharacter","<");
    return(ret);
    }
  
  public void addLoginEvent(String username, InetSocketAddress source, boolean correctLogin) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::addLoginEvent",">");

    try
      {
      int id=getDatabasePlayerId(username);

      String query = "insert into logEvent values(player_id,'"+source.getHostName()+"',?,"+(correctLogin?1:0)+")";
      PreparedStatement prep_stmt = connection.prepareStatement(query);
      prep_stmt.setTimestamp(1,new Timestamp(System.currentTimeMillis()));
  
      prep_stmt.execute();
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","E",sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","E","Database doesn't contains that username("+username+")");
      throw e;
      }

    marauroad.trace("JDBCPlayerDatabase::addLoginEvent","<");
    }
  
  public void addCharacter(String username, String character, RPObject object) throws PlayerNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::addCharacter",">");

    try
      {
      Statement stmt = connection.createStatement();
      int id=getDatabasePlayerId(username);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputSerializer os = new OutputSerializer(baos);
      try
        {
        object.writeObject(os);
        }
      catch (IOException e)
        {
        marauroad.trace("JDBCPlayerDatabase::addCharacter","E","Error serializing character: "+e.getMessage());
        throw new GenericDatabaseException("Error serializing character: "+e.getMessage());
        }
        
      String query = "insert into characters values("+id+",'"+character+"',?)";
      PreparedStatement prep_stmt = connection.prepareStatement(query);
      prep_stmt.setBytes(1,baos.toByteArray());
  
      prep_stmt.execute();
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addCharacter","E",sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::addCharacter","E","Database doesn't contains that username("+username+")");
      throw e;
      }

    marauroad.trace("JDBCPlayerDatabase::addCharacter","<");
    }
  
  public int getPlayerCount()
    {
    marauroad.trace("JDBCPlayerDatabase::getPlayerCount",">");

    int ret = 0;
    try
      {
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        ret = result.getInt(1);
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getPlayerCount","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::getPlayerCount","<");
    return(ret);
    }
  
  public void setRPObject(String username, String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::setRPObject",">");

    try
      {
      int id=getDatabasePlayerId(username);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputSerializer os = new OutputSerializer(baos);
      try
        {
        object.writeObject(os);
        }
      catch (IOException e)
        {
        marauroad.trace("JDBCPlayerDatabase::setRPObject","E","Error serializing character: "+e.getMessage());
        throw new GenericDatabaseException("Error serializing character: "+e.getMessage());
        }
        
      String query = "update characters set contents=? where player_id="+id+" and charname like '"+character+"'";
      PreparedStatement prep_stmt = connection.prepareStatement(query);
      prep_stmt.setBytes(1,baos.toByteArray());
  
      prep_stmt.execute();
      } 
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::setRPObject","E",sqle.getMessage());
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::setRPObject","E","Database doesn't contains that username("+username+")");
      throw e;
      }

    marauroad.trace("JDBCPlayerDatabase::setRPObject","<");
    }
  
  public RPObject getRPObject(String username, String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("JDBCPlayerDatabase::getRPObject",">");

    RPObject rp_object = null;
    try
      {
      int id=getDatabasePlayerId(username);

      Statement stmt = connection.createStatement();
      String query = "select contents from characters where player_id="+id+" and charname like '"+character+"'";  
      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        ByteArrayInputStream bais = new ByteArrayInputStream(result.getBytes(1));
        InputSerializer is = new InputSerializer(bais);
        rp_object = new RPObject();
        try
          {
          rp_object.readObject(is);
          }
        catch (IOException e)
          {
          marauroad.trace("JDBCPlayerDatabase::getRPObject","E","Error serializing character: "+e.getMessage());
          throw new GenericDatabaseException("Error serializing character: "+e.getMessage());
          }          
        catch (ClassNotFoundException e)
          {
          marauroad.trace("JDBCPlayerDatabase::getRPObject","E","Error serializing character: "+e.getMessage());
          throw new GenericDatabaseException("Error serializing character: "+e.getMessage());
          }
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::getRPObject","E","Player("+username+") doesn't contains that character("+character+")");
        throw new CharacterNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","E",sqle.getMessage());      
      }
    catch(PlayerNotFoundException e)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","E","Database doesn't contains that username("+username+")");
      throw e;
      }
    
    marauroad.trace("JDBCPlayerDatabase::getRPObject","<");
    return(rp_object);
    }
  
  
  private Connection createConnection(Properties props)
    {
    Connection conn = null;
    
    try
      {
      Class.forName((String)props.get("jdbc_class")).newInstance();

      Properties connInfo = new Properties();
      connInfo.put("user", props.get("jdbc_user"));
      connInfo.put("password", props.get("jdbc_pwd"));
      connInfo.put("charSet", "UTF-8");
      conn = DriverManager.getConnection((String)props.get("jdbc_url"), connInfo);
      conn.setAutoCommit(true);
      }
    catch (Throwable e)
      {
      e.printStackTrace();
      }
    
    return(conn);
    }
  
  private int getDatabasePlayerId(String username) throws PlayerNotFoundException, SQLException
    {
    Statement stmt=connection.createStatement();
    
    int id;
    
    String query = "select id from player where username like '"+username+"'";
    ResultSet result = stmt.executeQuery(query);
    if(result.next())
      {
      id = result.getInt(1);
      }
    else
      {
      throw new PlayerNotFoundException();
      }
      
    return(id);
    }

  
  private boolean reInitDB()
    {
    return (dropDB() & initDB());
    }
  
  private boolean dropDB()
    {
    boolean ret = false;

    try
      {
      Statement stmt = connection.createStatement();
      
      String query = "drop table if exists player";

      stmt.addBatch(query);
      query = "drop table if exists characters";

      stmt.addBatch(query);
      query = "drop table if exists loginEvent";

      stmt.addBatch(query);
      int ret_array[] = stmt.executeBatch();
      ret = true;
      for (int i = 0; i < ret_array.length; i++)
        {
        if(ret_array[i]<0)
          {
          ret = false;
          break;
          }
        }
      }
    catch (SQLException e)
      {
      e.printStackTrace(System.out);
      ret = false;
      }
    return ret;
    }
  
  private boolean initDB()
    {
    boolean ret = false;
    try
      {
      Statement stmt = connection.createStatement();
      
      String query = "CREATE TABLE IF NOT EXISTS player (id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL , username VARCHAR(30) NOT NULL, password VARCHAR(30) NOT NULL )";
      stmt.addBatch(query);

      query = "CREATE TABLE IF NOT EXISTS characters (player_id BIGINT NOT NULL, charname VARCHAR(30) NOT NULL, contents BLOB, PRIMARY KEY(charname))";
      stmt.addBatch(query);

      query = "CREATE TABLE IF NOT EXISTS loginEvent ( player_id BIGINT NOT NULL,address VARCHAR(20), timedate TIMESTAMP, result TINYINT)";
      stmt.addBatch(query);

      int ret_array[] = stmt.executeBatch();
      ret = true;
      for (int i = 0; i < ret_array.length; i++)
        {
        if(ret_array[i]<0)
          {
          ret = false;
          break;
          }
        }
      }
    catch (SQLException e)
      {
      e.printStackTrace(System.out);
      ret = false;
      }
  
    return ret;
    }
  }

