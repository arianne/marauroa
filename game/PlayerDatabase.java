package marauroa.game;

import java.util.*;
import java.net.*;
import java.io.*;
import marauroa.marauroad;

public class PlayerDatabase
  {
  private final static byte MAX_NUMBER_OF_LOGINS=5;
  private static PlayerDatabase playerDatabase;
  
  static class LoginEvent
    {
    public String address;
    public Date time;
    public boolean correct;
    }  

  static class RPCharacter
    {
    public String character;
    public RPObject object;
    }
    
  static class PlayerEntry
    {
    public String password;
    public HashMap characters;
    public List lastLogins;
 
    public PlayerEntry()
      {
      characters=new HashMap();
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
  
  public class CharacterNotFoundException extends Throwable
    {
    CharacterNotFoundException()
      {
      super("Character not found on the database");
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
    
    Iterator it=player.characters.entrySet().iterator();
    
    int i=0;
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      characters[i]=(String)entry.getKey();
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
    return player.characters.containsKey(character);
    }
    
  public RPObject getCharacter(String username,String character) throws PlayerNotFoundException, CharacterNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    
    if(!player.characters.containsKey(character))
      {
      throw new CharacterNotFoundException();
      }
      
    return (RPObject)player.characters.get(character);
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

  public void addCharacter(String username, String character, RPObject object) throws PlayerNotFoundException
    {
    if(!players.containsKey(username))
      {
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    player.characters.put(character,object);
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