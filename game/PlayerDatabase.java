package marauroa.game;

import java.util.*;
import java.net.*;
import java.io.*;
import marauroa.marauroad;

public class PlayerDatabase
  {
  private final static byte MAX_NUMBER_OF_LOGINS=5;
  private static PlayerDatabase playerDatabase;
  
  class LoginEvent
    {
    public String address;
    public Date time;
    public boolean correct;
    }  

  class PlayerEntry
    {
    public String password;
    public List characters;
    public List lastLogins;
    
    public PlayerEntry()
      {
      characters=new LinkedList();
      lastLogins=new LinkedList();
      }
    }
    
  private Map players;
  
  public class PlayerAlreadyAddedException extends Throwable
    {
    PlayerAlreadyAddedException()
      {
      super("Player already added to the database.");
      }
    }
  
  public class PlayerNotFoundException extends Throwable
    {
    PlayerNotFoundException()
      {
      super("Player not found on the database");
      }
    }
  
  private PlayerDatabase()
    {
    players=new HashMap();
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
    if(players.containsKey(username))
      {
      PlayerEntry player=(PlayerEntry)players.get(username);
      if(player.password.equals(password))
        {
        return true;
        }
      else
        {
        return false;
        }
      }

    return false;
    }
  
  public void updateLastLogin(String username,InetSocketAddress source, boolean correctLogin) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    LoginEvent loginEvent=new LoginEvent();
    loginEvent.address=source.toString();
    loginEvent.time=new Date();
    loginEvent.correct=correctLogin;
    
    player.lastLogins.add(loginEvent);
    
    if(player.lastLogins.size()>MAX_NUMBER_OF_LOGINS)
      {
      player.lastLogins.remove(0);
      }
    }
    
  public String[] getCharactersList(String username) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    String[] characters=new String[player.characters.size()];
    
    Iterator it=player.characters.iterator();
    
    int i=0;
    while(it.hasNext())
      {
      characters[i]=(String)it.next();
      }
    return characters;
    }
    
  public boolean hasCharacter(String username, String character) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    return player.characters.contains(character);
    }
    
  public void addPlayer(String username, String password) throws PlayerAlreadyAddedException
    {
    if(players.containsKey(username))
      {
      throw new PlayerAlreadyAddedException();
      }
      
    PlayerEntry player=new PlayerEntry();
    player.password=password;
    
    players.put(username,player);
    }
    
  public void removePlayer(String username) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    players.remove(username);
    }

  public void addCharacter(String username, String character) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    player.characters.add(character);
    }
    
  public void removeCharacter(String username, String character) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    player.characters.remove(character);
    }
    
  public boolean exists(String username)
    {
    if(players.containsKey(username))
      {
      return true;
      }
    else
      {
      return false;
      }      
    }
  }