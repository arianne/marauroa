package marauroa.game;

import java.util.*;
import java.net.*;

import marauroa.marauroad;

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
    public short clientid;
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
    }
    
  static public class NoSuchClientIDException extends Exception
    {
    public NoSuchClientIDException()
      {
      super("Unable to find the requested client id");
      }
    }
  
  static public class NoSuchCharacterException extends Exception
    {
    public NoSuchCharacterException()
      {
      super("Unable to find the requested character");
      }
    }
  
  static public class NoSuchPlayerException extends Exception
    {
    public NoSuchPlayerException()
      {
      super("Unable to find the requested player");
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
    public short next()
      {
      Map.Entry entry=(Map.Entry)entryIter.next();
      return ((Short)entry.getKey()).shortValue();
      }
    
    public void remove()
      {
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
  private static PlayerEntryContainer playerEntryContainer;
  
  /** Constructor */
  private PlayerEntryContainer()
    {
    /* Initialize the random number generator */
    rand.setSeed(new Date().getTime());
    
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
  public boolean hasRuntimePlayer(short clientid)
    {
    marauroad.trace("PlayerEntryContainer::hasRuntimePlayer",">");

    try
      {
      return listPlayerEntries.containsKey(new Short(clientid));
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
  public short addRuntimePlayer(String username, InetSocketAddress source)
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
    
      listPlayerEntries.put(new Short(entry.clientid),entry);
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
  public void removeRuntimePlayer(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::removeRuntimePlayer",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        listPlayerEntries.remove(new Short(clientid));
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::removeRuntimePlayer","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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
  public boolean verifyRuntimePlayer(short clientid, InetSocketAddress source)
    {
    marauroad.trace("PlayerEntryContainer::verifyRuntimePlayer",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        entry.timestamp=new Date();    
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
  public byte getRuntimeState(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getRuntimeState",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        entry.timestamp=new Date();    
        return entry.state;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getRuntimeState","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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
  public byte changeRuntimeState(short clientid,byte newState) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::changeRuntimeState",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
     
        byte oldState=entry.state;
        entry.state=newState;
        entry.timestamp=new Date();
      
        return oldState;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::changeRuntimeState","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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
  public void addLoginEvent(short clientid, InetSocketAddress source, boolean correctLogin) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::addLoginEvent",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
    
          playerDatabase.addLoginEvent(entry.username,source,correctLogin);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::addLoginEvent","X","No such Player(unknown)");
          marauroad.trace("PlayerEntryContainer::addLoginEvent","!","This should never happens");
          throw new NoSuchPlayerException();
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::addLoginEvent","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
        }
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
  public String[] getLoginEvent(short clientid) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::getLoginEvent",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));    
          return playerDatabase.getLoginEvent(entry.username);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::getLoginEvent","X","No such Player(unknown)");
          marauroad.trace("PlayerEntryContainer::getLoginEvent","!","This should never happens");
          throw new NoSuchPlayerException();
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getLoginEvent","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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

  /** This method returns true if the player has that character or false if it hasn't
   *  @param clientid the runtime id of the player
   *  @param character is the name of the character
   *  @return true if player has the character or false if it hasn't
   *
   *  @throws NoSuchClientIDException if clientid is not found
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public boolean hasCharacter(short clientid,String character) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::hasCharacter",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));        
          return playerDatabase.hasCharacter(entry.username,character);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::hasCharacter","X","No such Player(unknown)");
          throw new NoSuchPlayerException();
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::hasCharacter","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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
  public void setChoosenCharacter(short clientid,String character) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::setChoosenCharacter",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        entry.choosenCharacter=character;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::setChoosenCharacter","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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
  public String[] getCharacterList(short clientid) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::getCharacterList",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        try
          {
          RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
      	  return playerDatabase.getCharactersList(entry.username);
          }
        catch(PlayerDatabase.PlayerNotFoundException e)
          {
          marauroad.trace("PlayerEntryContainer::getCharacterList","X","No such Player(unknown)");
          throw new NoSuchPlayerException();
          }
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getCharacterList","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
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
   *  @throws NoSuchPlayerFoundException  if the player doesn't exist in database. */
  public RPObject getRPObject(short clientid, String character) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::getRPObject",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        return playerDatabase.getRPObject(entry.username,character);
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getRPObject","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
        }
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X","No such Player(unknown)");
      throw new NoSuchPlayerException();
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X","No such Character(unknown)");
      throw new NoSuchCharacterException();
      }        
    catch(PlayerDatabase.GenericDatabaseException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","X","Generic Database problem: "+e.getMessage());
      throw new NoSuchCharacterException();
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
  public void setRPObject(short clientid, RPObject object) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::setRPObject",">");
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        playerDatabase.setRPObject(entry.username,entry.choosenCharacter,object);
        }      
      else
        {
        marauroad.trace("PlayerEntryContainer::setRPObject","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
        }    
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","No such Player(unknown)");
      throw new NoSuchPlayerException();
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","No such Character(unknown)");
      throw new NoSuchCharacterException();
      }        
    catch(PlayerDatabase.GenericDatabaseException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","Generic Database problem: "+e.getMessage());
      throw new NoSuchCharacterException();
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
  public RPObject.ID getRPObjectID(short clientid) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::getRPObjectID",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        return new RPObject.ID(playerDatabase.getRPObject(entry.username,entry.choosenCharacter));
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getRPObjectID","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
        }
      }
    catch(PlayerDatabase.PlayerNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObjectID","X","No such Player(unknown)");
      throw new NoSuchPlayerException();
      }
    catch(PlayerDatabase.CharacterNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObjectID","X","No such Character(unknown)");
      throw new NoSuchCharacterException();
      }        
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("PlayerEntryContainer::getRPObjectID","X","No such attribute(object_id)");
      throw new NoSuchCharacterException();
      }        
    catch(PlayerDatabase.GenericDatabaseException e)
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","X","Generic Database problem: "+e.getMessage());
      throw new NoSuchCharacterException();
      }        
    finally
      {
      marauroad.trace("PlayerEntryContainer::getRPObjectID","<");
      }
    }

  private static short maxClientID=0;
  private static Random rand=new Random();
  
  private short generateClientID(InetSocketAddress source)
    {
    short clientid=(short)rand.nextInt();
    while(hasRuntimePlayer(clientid))    
      {
      clientid=(short)rand.nextInt();
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
  public String getUsername(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getUsername",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));         
        return entry.username;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getUsername","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getUsername","<");
      }
    }
    
  /** The method returns the IP address of the player represented by clientid
   *  @param clientid the runtime id of the player   *
   *  @throws NoSuchClientIDException if clientid is not found */
  public InetSocketAddress getInetSocketAddress(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getInetSocketAddress",">");
    
    try
      {
      if(hasRuntimePlayer(clientid))
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));         
        return entry.source;
        }
      else
        {
        marauroad.trace("PlayerEntryContainer::getInetSocketAddress","X","No such RunTimePlayer("+clientid+")");
        throw new NoSuchClientIDException();
        }
      }
    finally
      {
      marauroad.trace("PlayerEntryContainer::getInetSocketAddress","<");
      } 
    }  
  }