package marauroa.game;

import java.util.*;
import java.net.*;
import java.io.*;
import marauroa.marauroad;

/** This is memory interface to the database, when you init it, it is empty, and when
 *  you finish the application it is emptied. */
public class MemoryPlayerDatabase implements PlayerDatabase
  {
  private final static byte MAX_NUMBER_OF_LOGIN_EVENTS=5;
  private static PlayerDatabase playerDatabase=null;
  
  /** Class to store the login events */
  static private class LoginEvent
    {
    /** TCP/IP address of the source of the login message */
    public String address;
    /** Time and date of the login event */
    public Date time;
    /** True if login was correct */
    public boolean correct;
    
    /** This method returns a String that represent the object 
     *  @return a string representing the object.*/
    public String toString()
      {
      return "Login "+(correct?"SUCESSFULL":"FAILED")+" at "+time.toString()+" from "+address;
      }
    }  
  
  /** Class that store a character and its RPObject */
  static private class RPCharacter
    {
    /** The name of the character */
    public String character;
    /** The RPObject */
    public RPObject object;    
    }
    
  /** Class that store the Player information */
  static private class PlayerEntry
    {
    /** The username */
    public String username;
    /** The password */
    public String password;
    /** A HashMap<character, RPCharacter> containing characters*/
    public HashMap characters;
    /** A List<LoginEvent> containing login events. */
    public List lastLogins;    
 
    public PlayerEntry()
      {
      characters=new HashMap();
      lastLogins=new LinkedList();
      }
    }
  
  /* A Map<username, PlayerEntry> that stores the player entries */
  private Map players;
  
  /** Constructor */
  private MemoryPlayerDatabase()
    {
    players=new HashMap();
    }
    
  /** This method returns an instance of PlayerDatabase 
   *  @return A shared instance of PlayerDatabase */
  public static PlayerDatabase getDatabase()
    {
    marauroad.trace("MemoryPlayerDatabase::getDatabase",">");
    
    try
      {
      if(playerDatabase==null)
        {
        playerDatabase=new MemoryPlayerDatabase();
        }
      
      return playerDatabase;
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::getDatabase","<");
      }
    }
  
  /** This method returns the number of Players that exist on database 
   *  @return the number of players that exist on database */
  public int getPlayerCount()
    {
    marauroad.trace("MemoryPlayerDatabase::getPlayerCount",">");

    try
      {
      return players.size();
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::getPlayerCount","<");
      }     
    }
    
  /** This method returns true if the username/password match with any of the accounts in 
   *  database or false if none of them match.
   *  @param username is the name of the player
   *  @param password is the string used to verify access.
   *  @return true if username/password is correct, false otherwise. */
  public boolean verifyAccount(String username, String password)
    {
    marauroad.trace("MemoryPlayerDatabase::verifyAccount",">");
    try 
      {
      if(players.containsKey(username))
        {
        PlayerEntry player=(PlayerEntry)players.get(username);
        if(player.password.equals(password))
          {
          marauroad.trace("MemoryPlayerDatabase::verifyAccount","D","Username-password correct");
          return true;
          }
        else
          {
          marauroad.trace("MemoryPlayerDatabase::verifyAccount","D","Username-password incorrect");
          return false;
          }
        }
      else
        {
        return false;
        }
      }
    finally
      {  
      marauroad.trace("MemoryPlayerDatabase::verifyAccount","<");
      }
    }
  
  /** This method add a Login event to the player
   *  @param username is the name of the player 
   *  @param source the IP address of the player
   *  @param correctLogin true if the login has been correct.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public void addLoginEvent(String username,InetSocketAddress source, boolean correctLogin) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::addLoginEvent",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::addLoginEvent","X","Database doesn't contains that username("+username+")");
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
      }
    finally
      {  
      marauroad.trace("MemoryPlayerDatabase::addLoginEvent","<");
      }
    }

  /** This method returns the list of Login events as a array of Strings
   *  @param username is the name of the player 
   *  @return an array of String containing the login events.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public String[] getLoginEvent(String username) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::getLoginEvent",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::getLoginEvent","X","Database doesn't contains that username("+username+")");
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
      
      return events;
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::getLoginEvent","<");
      }
    }
    
  /** This method returns the lis of character that the player pointed by username has.
   *  @param username the name of the player from which we are requesting the list of characters.
   *  @return an array of String with the characters
   *  @throw PlayerNotFoundException if that player does not exists. */
  public String[] getCharactersList(String username) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::getCharactersList",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::getCharactersList","X","Database doesn't contains that username("+username+")");
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
      
      return characters;
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::getCharactersList","<");
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
  public void setRPObject(String username,String character, RPObject object) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("MemoryPlayerDatabase::setRPObject",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::setRPObject","X","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      
      PlayerEntry player=(PlayerEntry)players.get(username);
    
      if(!player.characters.containsKey(character))
        {
        marauroad.trace("MemoryPlayerDatabase::setRPObject","X","Player("+username+") doesn't contains that character("+character+")");
        throw new CharacterNotFoundException();
        }
      
      player.characters.put(character,object);
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::setRPObject","<");
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
  public RPObject getRPObject(String username,String character) throws PlayerNotFoundException, CharacterNotFoundException, GenericDatabaseException
    {
    marauroad.trace("MemoryPlayerDatabase::getRPObject",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::getRPObject","X","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      
      PlayerEntry player=(PlayerEntry)players.get(username);
    
      if(!player.characters.containsKey(character))
        {
        marauroad.trace("MemoryPlayerDatabase::getRPObject","X","Player("+username+") doesn't contains that character("+character+")");
        throw new CharacterNotFoundException();
        }
      
      RPObject object=(RPObject)player.characters.get(character);    
      return object;
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::getRPObject","<");
      }
    }
    
  /** This method add the player to database with username and password as identificator.
   *  @param username is the name of the player
   *  @param password is a string used to verify access.
   *  @throws PlayerAlreadyAddedExceptio if the player is already in database */
  public void addPlayer(String username, String password) throws PlayerAlreadyAddedException
    {
    marauroad.trace("MemoryPlayerDatabase::addPlayer",">");
    
    try
      {
      if(players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::addPlayer","X","Database already contains that username("+username+")");
        throw new PlayerAlreadyAddedException();
        }
      
      PlayerEntry player=new PlayerEntry();
      player.password=password;
    
      players.put(username,player);
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::addPlayer","<");
      }
    }
  
  /** This method remove the player with usernae from database.
   *  @param username is the name of the player
   *  @throws PlayerNotFoundException if the player doesn't exist in database. */
  public void removePlayer(String username) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::removePlayer",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::removePlayer","X","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      
      players.remove(username);
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::removePlayer","<");
      }
    }

  /** This method returns true if the player has that character or false if it hasn't
   *  @param username is the name of the player 
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *  @throws PlayerNotFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(String username, String character) throws PlayerNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::hasCharacter",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::hasCharacter","X","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      
      PlayerEntry player=(PlayerEntry)players.get(username);
      return player.characters.containsKey(character);
      }
    finally
      {    
      marauroad.trace("MemoryPlayerDatabase::hasCharacter","<");
      }
    }
    
  /** This method add a character asociated to a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterAlreadyAddedException if that player-character exist in database.
   *  @throws GenericDatabaseException if the character doesn't exist or it is not owned by the player. */
  public void addCharacter(String username, String character, RPObject object) throws PlayerNotFoundException, CharacterAlreadyAddedException, GenericDatabaseException
    {
    marauroad.trace("MemoryPlayerDatabase::addCharacter",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::addCharacter","X","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }
      
      PlayerEntry player=(PlayerEntry)players.get(username);
      if(hasCharacter(username,character))
        {
        marauroad.trace("MemoryPlayerDatabase::addCharacter","X","Database does contains that username("+username+")-character("+character+")");
        throw new CharacterAlreadyAddedException();
        }
      else
        {
        player.characters.put(character,object);
        }
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::addCharacter","<");
      }      
    }
    
  /** This method removes a character asociated with a player.
   *  @param username is the name of the player
   *  @param character is the name of the character that the username player owns.
   *  @throws PlayerNotFoundException  if the player doesn't exist in database.
   *  @throws CharacterNotFoundException if the character doesn't exist or it is not owned by the player. */
  public void removeCharacter(String username, String character) throws PlayerNotFoundException, CharacterNotFoundException
    {
    marauroad.trace("MemoryPlayerDatabase::removeCharacter",">");
    
    try
      {
      if(!players.containsKey(username))
        {
        marauroad.trace("MemoryPlayerDatabase::removeCharacter","X","Database doesn't contains that username("+username+")");
        throw new PlayerNotFoundException();
        }

      if(!hasCharacter(username,character))
        {
        marauroad.trace("JDBCPlayerDatabase::removeCharacter","X","Database doesn't contains that username("+username+")-character("+character+")");
        throw new CharacterNotFoundException();
        }
      
      PlayerEntry player=(PlayerEntry)players.get(username);
      player.characters.remove(character);
      }
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::removeCharacter","<");
      }
    }
    
  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public boolean hasPlayer(String username)
    {
    marauroad.trace("MemoryPlayerDatabase::hasPlayer",">");

    try
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
    finally
      {
      marauroad.trace("MemoryPlayerDatabase::hasPlayer","<");
      }
    }
  }