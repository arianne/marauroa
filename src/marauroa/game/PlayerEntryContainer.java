/* $Id: PlayerEntryContainer.java,v 1.20 2004/03/02 19:16:51 arianne_rpg Exp $ */
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

import java.util.*;
import java.net.*;

import marauroa.*;

/** This class contains a list of the Runtime players existing in Marauroa, but it
 *  also links them with their representation in game and in database, so this is 
 *  the point to manage them all. */
public class PlayerEntryContainer
  {
  public static final byte STATE_NULL=0;
  public static final byte STATE_LOGIN_COMPLETE=1;
  public static final byte STATE_GAME_BEGIN=2;
    
  /** A class to store all the object information to use in runtime and access database */
  static public class RuntimePlayerEntry
    {
    /** The runtime clientid */
    public int clientid;
    /** The state in which this player is */ 
    public byte state;
    /** The initial address of this player */
    public InetSocketAddress source;
    /** The time when the latest event was done in this player */
    public Date timestamp;
    
    /** The name of the choosen character */
    public String choosenCharacter;   
    /** The name of the player */
    public String username;
    
    /** The rp object of the player */
    public RPObject.ID characterid;
    }
    
  static public class NoSuchClientIDException extends Exception
    {
    public NoSuchClientIDException(int clientid)
      {
      super("Unable to find the requested client id ["+clientid+"]");
      }
    }
  
  static public class NoSuchCharacterException extends Exception
    {
    public NoSuchCharacterException(String character)
      {
      super("Unable to find the requested character ["+character+"]");
      }
    }
  
  static public class NoSuchPlayerException extends Exception
    {
    public NoSuchPlayerException(String player)
      {
      super("Unable to find the requested player ["+player+"]");
      }
    }
    
  /** This class is a iterator over the player in PlayerEntryContainer */
  static public class ClientIDIterator
    {
    private Iterator entryIter;
    
    /** Constructor */
    private ClientIDIterator(Iterator iter)
      {
      entryIter = iter;
      }
     
    /** This method returns true if there are still most elements.
     *  @return true if there are more elements. */    
    public boolean hasNext()
      {
      return(entryIter.hasNext());
      }
     
    /** This method returs the clientid and move the pointer to the next element
     *  @return an clientid */
    public int next()
      {
      Map.Entry entry=(Map.Entry)entryIter.next();
      return ((Integer)entry.getKey()).intValue();
      }
    }
  
  /** This method returns an iterator of the players in the container */  
  public ClientIDIterator iterator()
    {
    return new ClientIDIterator(listPlayerEntries.entrySet().iterator());
    }
    
  /** A HashMap<clientid,RuntimePlayerEntry to store RuntimePlayerEntry objects */
  private HashMap listPlayerEntries;
  /** A object representing the database */
  private PlayerDatabase playerDatabase;
  /** A reader/writers lock for controlling the access */    
  private RWLock lock;
  
  private static PlayerEntryContainer playerEntryContainer;
  
  /** Constructor */
  private PlayerEntryContainer()
    {
    /* Initialize the random number generator */
    rand.setSeed(new Date().getTime());
    
    lock=new RWLock();
    
    listPlayerEntries=new HashMap();

	/* Choose the database type using configuration file */
	try
	  {
      playerDatabase=PlayerDatabaseFactory.getDatabase();
      }
    catch(PlayerDatabase.NoDatabaseConfException e)
      {
      marauroad.trace("PlayerEntryContainer","X", e.getMessage());
      marauroad.trace("PlayerEntryContainer","!","ABORT: marauroad can't allocate database");
      System.exit(-1);
      }
    }
    
  /** This method returns an instance of PlayerEntryContainer 
   *  @return A shared instance of PlayerEntryContainer */
  public static PlayerEntryContainer getContainer()
    {
    if(playerEntryContainer==null)
      {
      playerEntryContainer=new PlayerEntryContainer();
      }
      
    return playerEntryContainer;
    }
    
  /** This method returns true if exist a player with that clientid.
   *  @param clientid a player runtime id
   *  @return true if player exist or false otherwise. */
  public boolean hasRuntimePlayer(int clientid)
    {
    marauroad.trace("PlayerEntryContainer::hasRuntimePlayer",">");

    try
      {
      return listPlayerEntries.containsKey(new Integer(clientid));
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::hasRuntimePlayer","<");
      }   
    }
    
  /** This method creates a new instance of RuntimePlayerEntry and add it.
   *  @param username the name of the player
   *  @param source the IP address of the player.
   *  @return the clientid for that runtimeplayer */
  public int addRuntimePlayer(String username, InetSocketAddress source)
    {
    marauroad.trace("PlayerEntryContainer::addRuntimePlayer",">");
    
    try  
      {
      RuntimePlayerEntry entry=new RuntimePlayerEntry();
      entry.state=STATE_NULL;
      entry.timestamp=new Date();
      entry.source=source;
      entry.username=username;
      entry.choosenCharacter=null;
    
      entry.clientid=generateClientID(source);
    
      listPlayerEntries.put(new Integer(entry.clientid),entry);
      return entry.clientid;
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::addRuntimePlayer","<");
      }
    }

  /** This method remove the entry if it exists.
   *  @param clientid is the runtime id of the player
   *  @throws NoSuchClientIDException if clientid is not found */
  public void removeRuntimePlayer(int clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::removeRuntimePlayer",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        listPlayerEntries.remove(new Integer(clientid));
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::removeRuntimePlayer","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {     
      marauroad.trace("PlayerEntryContainer::removeRuntimePlayer","<");
      }
    }

  /** This method returns true if the clientid and the source address match.
   *  @param clientid the runtime id of the player
   *  @param source the IP address of the player.
   *  @return true if they match or false otherwise */
  public boolean verifyRuntimePlayer(int clientid, InetSocketAddress source)
    {
    marauroad.trace("PlayerEntryContainer::verifyRuntimePlayer",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
        if(source.equals(entry.source))
          {
          return true;
          }
        else
          {
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
      marauroad.trace("PlayerEntryContainer::verifyRuntimePlayer","<");
      }
    }
    
  /** This method returns a byte that indicate the state of the player from the 3 possible options:
   *  - STATE_NULL
   *  - STATE_LOGIN_COMPLETE
   *  - STATE_GAME_BEGIN
   *  @param clientid the runtime id of the player
   *  @throws NoSuchClientIDException if clientid is not found */
  public byte getRuntimeState(int clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getRuntimeState",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
        return entry.state;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getRuntimeState","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getRuntimeState","<");
      }
    }
    
  /** This method set the state of the player from the 3 possible options:
   *  - STATE_NULL
   *  - STATE_LOGIN_COMPLETE
   *  - STATE_GAME_BEGIN
   *  @param clientid the runtime id of the player
   *  @param newState the new state to which we move.
   *  @throws NoSuchClientIDException if clientid is not found */
  public byte changeRuntimeState(int clientid,byte newState) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::changeRuntimeState",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
     
        byte oldState=entry.state;
        entry.state=newState;
      
        return oldState;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::changeRuntimeState","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::changeRuntimeState","<");
      }
    }
    
  /** This method returns true if the username/password match with any of the accounts in 
   *  database or false if none of them match.
   *  @param username is the name of the player
   *  @param password is the string used to verify access.
   *  @return true if username/password is correct, false otherwise. */
  public boolean verifyAccount(String username, String password)
    {
    marauroad.trace("PlayerEntryContainer::verifyAccount",">");
    
    try
      {
      return playerDatabase.verifyAccount(username,password);
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::verifyAccount","<");
      }
    }
    
  /** This method add a Login event to the player
   *  @param clientid the runtime id of the player
   *  @param source the IP address of the player
   *  @param correctLogin true if the login has been correct.
   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public void addLoginEvent(String username, InetSocketAddress source, boolean correctLogin) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::addLoginEvent",">");
    
    try
      {
      playerDatabase.addLoginEvent(username,source,correctLogin);
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::addLoginEvent","X","No such Player(unknown)");
      throw new NoSuchPlayerException(username);
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::addLoginEvent","<");
      }
    }
    
  /** This method returns the list of Login events as a array of Strings
   *  @param clientid the runtime id of the player
   *  @return an array of String containing the login events.
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public String[] getLoginEvent(int clientid) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::getLoginEvent",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));    
          return playerDatabase.getLoginEvent(entry.username);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::getLoginEvent","X","No such Player(unknown)");
          throw new NoSuchPlayerException("- not available -");
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getLoginEvent","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getLoginEvent","<");
      }
    }
  

  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public boolean hasPlayer(String username)
    {
    marauroad.trace("PlayerEntryContainer::hasPlayer",">");
    
    try
      {
      Iterator it=listPlayerEntries.entrySet().iterator();
    
      while(it.hasNext())
        {
        Map.Entry entry=(Map.Entry)it.next();
        RuntimePlayerEntry playerEntry=(RuntimePlayerEntry)entry.getValue();
      
        if(playerEntry.username.equals(username))
          {
          return true;
          }      
        }
    
      return false;
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::hasPlayer","<");
      }
    }    

  /** This method returns true if the database has the player pointed by username
   *  @param username the name of the player we are asking if it exists.
   *  @return true if player exists or false otherwise. */
  public int getClientidPlayer(String username)
    {
    marauroad.trace("PlayerEntryContainer::getClientidPlayer",">");
    
    try
      {
      Iterator it=listPlayerEntries.entrySet().iterator();
    
      while(it.hasNext())
        {
        Map.Entry entry=(Map.Entry)it.next();
        RuntimePlayerEntry playerEntry=(RuntimePlayerEntry)entry.getValue();
      
        if(playerEntry.username.equals(username))
          {
          return playerEntry.clientid;
          }      
        }
    
      return -1;
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getClientidPlayer","<");
      }
    }    

  /** This method returns true if the player has that character or false if it hasn't
   *  @param clientid the runtime id of the player
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(int clientid,String character) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::hasCharacter",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));        
          return playerDatabase.hasCharacter(entry.username,character);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::hasCharacter","X","No such Player(-not available-)");
          throw new NoSuchPlayerException("- not available -");
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::hasCharacter","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }      
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::hasCharacter","<");
      }
    }
    
  /** This method assign the character to the playerEntry.
   *  @param clientid the runtime id of the player
   *  @param character is the name of the character
   *
   *  @throws NoSuchClientIDException if clientid is not found */
  public void setChoosenCharacter(int clientid,String character) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::setChoosenCharacter",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
        entry.choosenCharacter=character;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::setChoosenCharacter","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::setChoosenCharacter","<");
      }
    }
  
  /** This method returns the lis of character that the player pointed by username has.
   *  @param clientid the runtime id of the player
   *  @return an array of String with the characters
   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public String[] getCharacterList(int clientid) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::getCharacterList",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
      	  return playerDatabase.getCharactersList(entry.username);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::getCharacterList","X","No such Player(unknown)");
          throw new NoSuchPlayerException("- not available -");
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getCharacterList","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getCharacterList","<");
      }
    }
    
  /** This method retrieves from Database the object for an existing player and character.
   *  @param clientid the runtime id of the player
   *  @param character is the name of the character that the username player wants to add.
   *  @return a RPObject that is the RPObject that represent this character in game.
   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchCharacterException if character is not found
   *  @throws NoSuchPlayerException  if the player doesn't exist in database. */
  public RPObject getRPObject(int clientid, String character) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::getRPObject",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
        RPObject object=playerDatabase.getRPObject(entry.username,character);
        entry.characterid=new RPObject.ID(object);
        return object;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getRPObject","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X","No such Player(unknown)");
      throw new NoSuchPlayerException("- not available -");
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X",e.getMessage());
      throw new NoSuchPlayerException("- not available -");
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X","No such Character(unknown)");
      throw new NoSuchCharacterException(character);
      }        
    catch(PlayerDatabase.GenericDatabaseException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X","Generic Database problem: "+e.getMessage());
      throw new NoSuchCharacterException(character);
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","<");
      }
    }
    
  /** This method is the opposite of getRPObject, and store in Database the object for
   *  an existing player and character.
   *  The difference between setRPObject and addCharacter are that setRPObject update it
   *  while addCharacter add it to database and fails if it already exists
   *  @param clientid the runtime id of the player
   *  @param object is the RPObject that represent this character in game.
   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchCharacterException if character is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public void setRPObject(int clientid, RPObject object) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::setRPObject",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
        playerDatabase.setRPObject(entry.username,entry.choosenCharacter,object);
        entry.characterid=new RPObject.ID(object);
        }      
      else
        {
        marauroad.trace("PlayerEntryContainer::setRPObject","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }    
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","No such Player(unknown)");
      throw new NoSuchPlayerException("- not available -");
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X",e.getMessage());
      throw new NoSuchPlayerException("- not available -");
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","No such Character(unknown)");
      throw new NoSuchCharacterException("- not available -");
      }        
    catch(PlayerDatabase.GenericDatabaseException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","Generic Database problem: "+e.getMessage());
      throw new NoSuchCharacterException("- not available -");
      }        
    finally
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","<");
      }    
    }
    
  /** This method returns the RPObject.ID of the object the player whose clientid is clientid owns.
   *  @param clientid the runtime id of the player
   *  @return the RPObject.ID of the object that this player uses.
   *   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchCharacterException if character is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public RPObject.ID getRPObjectID(int clientid) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::getRPObjectID",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));
        return entry.characterid;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getRPObjectID","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getRPObjectID","<");
      }
    }

  private static Random rand=new Random();
  
  private int generateClientID(InetSocketAddress source)
    {
    int clientid=rand.nextInt();
    while(hasRuntimePlayer(clientid))    
      {
      clientid=rand.nextInt();
      }
      
    return clientid;    
    }
    
  protected int size()
    {
    return listPlayerEntries.size();
    }

  /** This method returns the username of the player with runtime id equals to clientid.
   *  @param clientid the runtime id of the player   *
   *  @throws NoSuchClientIDException if clientid is not found */
  public String getUsername(int clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getUsername",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));         
        return entry.username;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getUsername","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getUsername","<");
      }
    }
  
  /** The method update the timestamp value of clientid
   *  @param clientid the runtime id of the player   *
   *  @throws NoSuchClientIDException if clientid is not found */
  public void updateTimestamp(int clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::updateTimestamp",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));         
        entry.timestamp=new Date();
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::updateTimestamp","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::updateTimestamp","<");
      }
    }
     
  /** The method returns true if the clientid has timed out.
   *  @param clientid the runtime id of the player   *
   *  @return true if the player timed out.
   *  @throws NoSuchClientIDException if clientid is not found */
  public boolean timedout(int clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::timedout",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));         
        long value=new Date().getTime()-entry.timestamp.getTime();
        if(value>TimeoutConf.GAMESERVER_PLAYER_TIMEOUT)
          {
          return true;
          }
        else
          {
          return false;
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::timeodut","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::timedout","<");
      }
    }
     
    
  /** The method returns the IP address of the player represented by clientid
   *  @param clientid the runtime id of the player   *
   *  @throws NoSuchClientIDException if clientid is not found */
  public InetSocketAddress getInetSocketAddress(int clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getInetSocketAddress",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Integer(clientid));         
        return entry.source;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getInetSocketAddress","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException(clientid);
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getInetSocketAddress","<");
      } 
    }  
    
  /** This method returns the lock so that you can control how the resource is used 
   *  @return the RWLock of the object */
  public RWLock getLock()
    {
    return lock;
    }
  }