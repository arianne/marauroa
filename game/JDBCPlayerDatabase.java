package marauroa.game;

import java.sql.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Vector;
import marauroa.net.InputSerializer;
import marauroa.net.OutputSerializer;

import marauroa.marauroad;



/* SQL Tables used for storing information.
 
CREATE TABLE player
  (
  id BIGINT PRIMARY KEY NOT NULL AUTOINCREMENT,
  username VARCHAR(30) NOT NULL,
  password VARCHAR(30) NOT NULL
  );
 
CREATE TABLE characters
  (
  player_id BIGINT NOT NULL,
  charname VARCHAR(30) NOT NULL,
  contents VARCHAR(4096)
 
  PRIMARY KEY(id,charname)
  );
 
CREATE TABLE loginEvent
  (
  player_id BIGINT NOT NULL,
  address VARCHAR(20),
  timedate TIMEDATE,
  result TINYINT
  );
*/

/**
 *
 */
public class JDBCPlayerDatabase implements PlayerDatabase
  {  
  static class LoginEvent
    {
    public String address;
    public java.util.Date time;
    public boolean correct;
    
    public String toString()
      {
      return "Login "+(correct?"SUCESSFULL":"FAILED")+" at "+time.toString()+" from "+address;
      }
    }  

  private Connection connection;  
  private static PlayerDatabase playerDatabase;

  /**
   *
   * String url = "jdbc:odbc:mysql-darkstar";
   * Properties connInfo = new Properties();
   * connInfo.put("user", "marauroa");
   * connInfo.put("password", "marauroa");
   * connInfo.put("charSet", "UTF-8");
   *
   */
  private JDBCPlayerDatabase(Properties connInfo) throws NoDatabaseConfException
    {
    // String db_drv_name = "sun.jdbc.odbc.JdbcOdbcDriverX";
    // String db_url = "jdbc:odbc:mysql-darkstar";
    connection=createConnection(connInfo);
    if(connection==null)
      {
      throw new NoDatabaseConfException();
      }
    }
  
  public static PlayerDatabase getDatabase() throws NoDatabaseConfException
    {
    if(playerDatabase==null)
      {
      /* TODO: Define a default configuration file or a default configuration. */
      playerDatabase=new JDBCPlayerDatabase(null);
      }
      
    return playerDatabase;
    }


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
      marauroad.trace("JDBCPlayerDatabase::hasPlayer","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::hasPlayer","<");

    return has;
    }
  
  public String[] getCharactersList(String username) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::getCharacterList",">");
    String[] characters = null;
    
    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";
      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        
        query = "select charname from characters where player_id="+id;
        ResultSet charactersSet = stmt.executeQuery(query);
        
        Vector vector = new Vector();
        while(charactersSet.next())
          {
          vector.add(charactersSet.getString("characters.charname"));
          }
          
        characters = new String[vector.size()];
        characters = (String[])vector.toArray(characters);
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::getCharactersList","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getCharacterList","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::getCharacterList","<");
    return characters;
    }
  
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
        marauroad.trace("JDBCPlayerDatabase::addPlayer","E","Database already contains that username("+username+")");
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
      marauroad.trace("JDBCPlayerDatabase::addPlayer","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::addPlayer","<");
    }
  
  public void removePlayer(String username) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::removePlayer",">");

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";
      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        query = "delete from player where id="+id;
  
        stmt.execute(query);
        query = "delete from characters where player_id="+id;
  
        stmt.execute(query);
        query = "delete from loginEvent where player_id="+id;
  
        stmt.execute(query);
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::removePlayer","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::removePlayer","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::removePlayer","<");
    }
  
  public void removeCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::removeCharacter",">");
    
    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        query = "delete from characters where player_id="+id+" and charname like '"+character+"'";
  
        stmt.execute(query);
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::removeCharacter","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::removeCharacter","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::removeCharacter","<");
    }
  
  public boolean verifyAccount(String username, String password)
    {
    marauroad.trace("JDBCPlayerDatabase::verifyAccount",">");

    boolean ret = false;
    try
      {
      Statement stmt = connection.createStatement();
      String query = "select count(*) from  player where username like '"+username+"' and password like '"+password+"'";

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
      marauroad.trace("JDBCPlayerDatabase::verifyAccount","E",sqle.getMessage());
      }
      
    marauroad.trace("JDBCPlayerDatabase::verifyAccount","<");
    return(ret);
    }
  
  public String[] getLoginEvent(String username) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::getLoginEvent",">");

    String[] loginEvents = null;
    try
      {
      /* TODO: Check that player exist */
      Statement stmt = connection.createStatement();
      String query = "select address,timedate,result from player,loginEvent where player.username like '"+username+"' and loginEvent.player_id=player.id order by timedate";

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
    
    marauroad.trace("JDBCPlayerDatabase::getLoginEvent","<");
    return(loginEvents);
    }
  
  public boolean hasCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::hasCharacter",">");

    boolean ret = false;
    try
      {
      /* TODO: Check that player exist */      
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
      marauroad.trace("JDBCPlayerDatabase::getLoginEvent","E",sqle.getMessage());
      }
    
    marauroad.trace("JDBCPlayerDatabase::hasCharacter","<");
    return(ret);
    }
  
  public void addLoginEvent(String username, InetSocketAddress source, boolean correctLogin) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::addLoginEvent",">");

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        PreparedStatement prep_stmt = connection.prepareStatement(query);
        query = "insert into logEvent values(player_id,'"+source.getHostName()+"',?,"+(correctLogin?1:0)+")";
        prep_stmt.setTimestamp(1,new Timestamp(System.currentTimeMillis()));
  
        prep_stmt.execute();
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::removeCharacter","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addLoginEvent","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::addLoginEvent","<");
    }
  
  public void addCharacter(String username, String character, RPObject object) throws PlayerNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::addCharacter",">");

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputSerializer os = new OutputSerializer(baos);
        try
          {
          object.writeObject(os);
          }
        catch (IOException e)
          {
          /* TODO: need to drop an exception */
          marauroad.trace("JDBCPlayerDatabase::addCharacter","E","Error serializing character: "+e.getMessage());
          }
        
        query = "insert into characters values("+id+",'"+character+"',?)";
        PreparedStatement prep_stmt = connection.prepareStatement(query);
        prep_stmt.setBytes(1,baos.toByteArray());
  
        prep_stmt.execute();
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::addCharacter","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::addCharacter","E",sqle.getMessage());
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
      marauroad.trace("JDBCPlayerDatabase::addCharacter","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::getPlayerCount","<");
    return(ret);
    }
  
  public void setRPObject(String username, String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::setRPObject",">");

    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputSerializer os = new OutputSerializer(baos);
        try
          {
          object.writeObject(os);
          }
        catch (IOException e)
          {
          /* TODO: Need to drop an exception */
          //dont know now what to do
          marauroad.trace("JDBCPlayerDatabase::setRPObject","E","Error serializing character: "+e.getMessage());
          }
        
        query = "update characters set contents=? where player_id="+id+" and charname like '"+character+"'";
        PreparedStatement prep_stmt = connection.prepareStatement(query);
        prep_stmt.setBytes(1,baos.toByteArray());
  
        prep_stmt.execute();
        } 
      else
        {
        marauroad.trace("JDBCPlayerDatabase::setRPObject","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::setRPObject","E",sqle.getMessage());
      }

    marauroad.trace("JDBCPlayerDatabase::setRPObject","<");
    }
  
  public RPObject getRPObject(String username, String character) throws PlayerNotFoundException, CharacterNotFoundException
    {
    marauroad.trace("JDBCPlayerDatabase::getRPObject",">");

    RPObject rp_object = null;
    try
      {
      Statement stmt = connection.createStatement();
      String query = "select id from player where username like '"+username+"'";

      ResultSet result = stmt.executeQuery(query);
      if(result.next())
        {
        int id = result.getInt(1);
        query = "select contents from characters where player_id="+id+" and charname like '"+character+"'";
  
        ResultSet rs2 = stmt.executeQuery(query);
        if(rs2.next())
          {
          ByteArrayInputStream bais = new ByteArrayInputStream(rs2.getBytes(1));
          InputSerializer is = new InputSerializer(bais);
          rp_object = new RPObject();
          try
            {
            rp_object.readObject(is);
            }
          catch (IOException e)
            {
            /* TODO: Need to drop an exception */
            }          
          catch (ClassNotFoundException e)
            {
            /* TODO: Need to drop an exception */
            }
          }
        else
          {
          marauroad.trace("JDBCPlayerDatabase::getRPObject","E","Player("+username+") doesn't contains that character("+character+")");
         throw new CharacterNotFoundException();
          }
        }
      else
        {
        marauroad.trace("JDBCPlayerDatabase::getRPObject","E","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      }
    catch(SQLException sqle)
      {
      marauroad.trace("JDBCPlayerDatabase::getRPObject","E",sqle.getMessage());
      }
    
    marauroad.trace("JDBCPlayerDatabase::getRPObject","<");
    return(rp_object);
    }
  
  
  private Connection createConnection(Properties props)
    {
    Connection conn = null;
    
    try
      {
      Class.forName((String)props.get("jdbc_class"));
      Properties connInfo = new Properties();
      connInfo.put("user", props.get("jdbc_user"));
      connInfo.put("password", props.get("jdbc_pwd"));
      connInfo.put("charSet", "UTF-8");
      conn = DriverManager.getConnection((String)props.get("jdbc_url"), connInfo);
      conn.setAutoCommit(true);
      }
    catch (ClassNotFoundException e)
      {
      e.printStackTrace();
      }
    catch (SQLException e)
      {
      e.printStackTrace();
      }
    
    return(conn);
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
      
      String query = "drop table player";

      stmt.addBatch(query);
      query = "drop table characters";

      stmt.addBatch(query);
      query = "drop table loginEvent";

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
      
      String query = "CREATE TABLE player (id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL , username VARCHAR(30) NOT NULL, password VARCHAR(30) NOT NULL )";
      stmt.addBatch(query);

      query = "CREATE TABLE characters (player_id BIGINT NOT NULL, charname VARCHAR(30) NOT NULL, contents BLOB, PRIMARY KEY(id,charname))";
      stmt.addBatch(query);

      query = "CREATE TABLE loginEvent ( player_id BIGINT NOT NULL,address VARCHAR(20), timedate TIMESTAMP, result TINYINT)";
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
 
  public static void main(String argv[])
    {
    Properties props = new Properties();
    props.put("jdbc_url","jdbc:mysql://mariamat/marauroa");
    props.put("jdbc_class","com.mysql.jdbc.Driver");
    props.put("jdbc_user","marauroa_dbuser");
    props.put("jdbc_pwd","marauroa_dbpwd");
    
    try
      {
      PlayerDatabase db = new JDBCPlayerDatabase(props);
      ((JDBCPlayerDatabase)db).reInitDB();
      }
    catch(PlayerDatabase.NoDatabaseConfException e)
      {
      }
    }
  }

