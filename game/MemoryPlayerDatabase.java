package marauroa.game;

import java.util.*;
import java.net.*;
import java.io.*;
import marauroa.marauroad;

public class MemoryPlayerDatabase implements PlayerDatabase
  {
  private final static byte MAX_NUMBER_OF_LOGIN_EVENTS=5;
  private static PlayerDatabase playerDatabase;
  
  static class LoginEvent
    {
    public String address;
    public Date time;
    public boolean correct;
    
    public String toString()
      {
      return "Login "+(correct?"SUCESSFULL":"FAILED")+" at "+time.toString()+" from "+address;
      }
    }  

  static class RPCharacter
    {
    public String character;
    public RPObject object;
    }
    
  static class PlayerEntry
    {
    public String username;
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
  
  private MemoryPlayerDatabase()
    {
    players=new HashMap();
    }
    
  public static PlayerDatabase getDatabase()
    {
    if(playerDatabase==null)
      {
      playerDatabase=new MemoryPlayerDatabase();
      }
      
    return playerDatabase;
    }
  
  public int getPlayerCount()
    {
    return players.size();
    }
    
  public boolean verifyAccount(String username, String password)
    {
    marauroad.trace("MemoryPlayerDatabase::verifyAccount",">");
    if(players.containsKey(username))
      {
      PlayerEntry player=(PlayerEntry)players.get(username);
      if(player.password.equals(password))
        {
        marauroad.trace("MemoryPlayerDatabase::verifyAccount","I","Username-password correct");
        marauroad.trace("MemoryPlayerDatabase::verifyAccount","<");
        return true;
        }
      else
        {
        marauroad.trace("MemoryPlayerDatabase::verifyAccount","I","Username-password incorrect");
        marauroad.trace("MemoryPlayerDatabase::verifyAccount","<");
        return false;
        }
      }

    marauroad.trace("MemoryPlayerDatabase::verifyAccount","<");
    return false;
    }
  
  public void addLoginEvent(String username,InetSocketAddress source, boolean correctLogin) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::addLoginEvent",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::addLoginEvent","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    LoginEvent loginEvent=new LoginEvent();
    loginEvent.address=source.toString();
    loginEvent.time=new Date();
    loginEvent.correct=correctLogin;
    
    player.lastLogins.add(loginEvent);
    
    if(player.lastLogins.size()>MAX_NUMBER_OF_LOGIN_EVENTS)
      {
      player.lastLogins.remove(0);
      }
      
    marauroad.trace("MemoryPlayerDatabase::addLoginEvent","<");
    }

  public String[] getLoginEvent(String username) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::getLoginEvent",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::getLoginEvent","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    String[] events=new String[player.lastLogins.size()];
    
    Iterator it=player.lastLogins.iterator();
    
    int i=0;
    while(it.hasNext())
      {
      LoginEvent entry=(LoginEvent)it.next();
      events[i]=entry.toString();
      }
      
    marauroad.trace("MemoryPlayerDatabase::getLoginEvent","<");
    return events;
    }
    
  public String[] getCharactersList(String username) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::getCharactersList",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::getCharactersList","E","Database doesn't contains that username("+username+")");
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
      ++i;
      }
      
    marauroad.trace("MemoryPlayerDatabase::getCharactersList","<");
    return characters;
    }
    
  public void setRPObject(String username,String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::setRPObject",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::setRPObject","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    
    if(!player.characters.containsKey(character))
      {
      marauroad.trace("MemoryPlayerDatabase::setRPObject","E","Player("+username+") doesn't contains that character("+character+")");
      throw new CharacterNotFoundException();
      }
      
    player.characters.put(character,object);
    marauroad.trace("MemoryPlayerDatabase::setRPObject","<");
    }
    
  public RPObject getRPObject(String username,String character) throws PlayerNotFoundException, CharacterNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::getRPObject",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::getRPObject","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    
    if(!player.characters.containsKey(character))
      {
      marauroad.trace("MemoryPlayerDatabase::getRPObject","E","Player("+username+") doesn't contains that character("+character+")");
      throw new CharacterNotFoundException();
      }
      
    RPObject object=(RPObject)player.characters.get(character);
    
    marauroad.trace("MemoryPlayerDatabase::getRPObject","<");
    return object;
    }
    
  public void addPlayer(String username, String password) throws PlayerAlreadyAddedException
    {
    marauroad.trace("MemoryPlayerDatabase::addPlayer",">");
    if(players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::addPlayer","E","Database already contains that username("+username+")");
      throw new PlayerAlreadyAddedException();
      }
      
    PlayerEntry player=new PlayerEntry();
    player.password=password;
    
    players.put(username,player);
    marauroad.trace("MemoryPlayerDatabase::addPlayer","<");
    }
  
  public void removePlayer(String username) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::removePlayer",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::removePlayer","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    players.remove(username);
    marauroad.trace("MemoryPlayerDatabase::removePlayer","<");
    }

  public boolean hasCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::hasCharacter",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::hasCharacter","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    boolean has=player.characters.containsKey(character);
    
    marauroad.trace("MemoryPlayerDatabase::hasCharacter","<");
    return has;
    }
    
  public void addCharacter(String username, String character, RPObject object) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::addCharacter",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::addCharacter","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    player.characters.put(character,object);
    marauroad.trace("MemoryPlayerDatabase::addCharacter","<");
    }
    
  public void removeCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::removeCharacter",">");
    if(!players.containsKey(username))
      {
      marauroad.trace("MemoryPlayerDatabase::removeCharacter","E","Database doesn't contains that username("+username+")");
      throw new PlayerNotFoundException();
      }
      
    PlayerEntry player=(PlayerEntry)players.get(username);
    player.characters.remove(character);
    marauroad.trace("MemoryPlayerDatabase::removeCharacter","<");
    }
    
  public boolean hasPlayer(String username)
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