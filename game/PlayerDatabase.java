package marauroa.game;

import java.util.*;
import java.net.*;


public class PlayerDatabase
  {
  private static PlayerDatabase playerDatabase;
  
  private PlayerDatabase()
    {
    }
    
  public static PlayerDatabase getDatabase()
    {
    if(playerDatabase==null)
      {
      playerDatabase=new PlayerDatabase();
      }
      
    return playerDatabase;
    }
    
  public boolean isCorrect(String username, String password)
    {
    /** @TODO: Implement the database handler */
    return false;
    }
  
  public void updateLastLogin(String username,InetSocketAddress source)
    {
    /** @TODO: Implement the database handler */
    }
    
  public String[] getCharactersList(String username)
    {
    /** @TODO: Implement the database handler */
    return null;
    }
  }