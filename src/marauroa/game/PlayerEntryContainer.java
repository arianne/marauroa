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
    
  static public class NoSuchClientIDException extends Throwable
    {
    public NoSuchClientIDException()
      {
      super("Unable to find the requested client id");
      }
    }
  
  static public class NoSuchCharacterException extends Throwable
    {
    public NoSuchCharacterException()
      {
      super("Unable to find the requested character");
      }
    }
  
  static public class NoSuchPlayerException extends Throwable
    {
    public NoSuchPlayerException()
      {
      super("Unable to find the requested player");
      }
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
      System.exit(-1);
      }
    }
    
  /** This method returns an instance of PlayerDatabase 
   *  @return A shared instance of PlayerDatabase */
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
    

  public byte getRuntimeState(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getRuntimeState",">");
    if(hasRuntimePlayer(clientid))
      {
      RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
      entry.timestamp=new Date();    
      marauroad.trace("PlayerEntryContainer::getRuntimeState","<");
      return entry.state;
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getRuntimeState","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
    
  public byte changeRuntimeState(short clientid,byte newState) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::changeRuntimeState",">");
    if(hasRuntimePlayer(clientid))
      {
      RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
    
      byte oldState=entry.state;
      entry.state=newState;
      entry.timestamp=new Date();
      
      marauroad.trace("PlayerEntryContainer::changeRuntimeState","<");
      return oldState;
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::changeRuntimeState","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
    
  public boolean verifyAccount(String username, String password)
    {
    return playerDatabase.verifyAccount(username,password);
    }
    
  public void addLoginEvent(short clientid, InetSocketAddress source, boolean correctLogin) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::addLoginEvent",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
    
        playerDatabase.addLoginEvent(entry.username,source,correctLogin);
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::addLoginEvent","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::addLoginEvent","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
      
    marauroad.trace("PlayerEntryContainer::addLoginEvent","<");
    }
    
  public String[] getLoginEvent(short clientid) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::getLoginEvent",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
    
        marauroad.trace("PlayerEntryContainer::getLoginEvent","<");
        return playerDatabase.getLoginEvent(entry.username);
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getLoginEvent","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getLoginEvent","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
  
  public boolean hasPlayer(String username)
    {
    marauroad.trace("PlayerEntryContainer::hasPlayer",">");
    Iterator it=listPlayerEntries.entrySet().iterator();
    
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      RuntimePlayerEntry playerEntry=(RuntimePlayerEntry)entry.getValue();
      
      if(playerEntry.username.equals(username))
        {
        marauroad.trace("PlayerEntryContainer::hasPlayer","<");
        return true;
        }      
      }
    
    marauroad.trace("PlayerEntryContainer::hasPlayer","<");
    return false;
    }    
    
  public boolean hasCharacter(short clientid,String character) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::hasCharacter",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        
        boolean has=playerDatabase.hasCharacter(entry.username,character);
        
        marauroad.trace("PlayerEntryContainer::hasCharacter","<");
        return has;
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::hasCharacter","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::hasCharacter","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
    
  public void setChoosenCharacter(short clientid,String character) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::setChoosenCharacter",">");
    if(hasRuntimePlayer(clientid))
      {
      RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
      entry.choosenCharacter=character;
    
      marauroad.trace("PlayerEntryContainer::setChoosenCharacter","<");
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::setChoosenCharacter","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
  
  public String[] getCharacterList(short clientid) throws NoSuchClientIDException, NoSuchPlayerException
    {
    marauroad.trace("PlayerEntryContainer::getCharacterList",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
    	String[] characters=playerDatabase.getCharactersList(entry.username);
    	
        marauroad.trace("PlayerEntryContainer::getCharacterList","<");
        return characters;
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getCharacterList","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getCharacterList","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
    
  public RPObject getRPObject(short clientid, String character) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::getRPObject",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        RPObject object=playerDatabase.getRPObject(entry.username,character);
    
        marauroad.trace("PlayerEntryContainer::getRPObject","<");
        return object;
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getRPObject","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      catch(PlayerDatabase.CharacterNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getRPObject","E","No such Character(unknown)");
        throw new NoSuchCharacterException();
        }        
      catch(PlayerDatabase.GenericDatabaseException e)
        {
        marauroad.trace("PlayerEntryContainer::getRPObject","E","Generic Database problem: "+e.getMessage());
        throw new NoSuchCharacterException();
        }        
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getRPObject","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
    
  public void setRPObject(short clientid, RPObject object) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::setRPObject",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        playerDatabase.setRPObject(entry.username,entry.choosenCharacter,object);
    
        marauroad.trace("PlayerEntryContainer::setRPObject","<");
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::setRPObject","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      catch(PlayerDatabase.CharacterNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::setRPObject","E","No such Character(unknown)");
        throw new NoSuchCharacterException();
        }        
      catch(PlayerDatabase.GenericDatabaseException e)
        {
        marauroad.trace("PlayerEntryContainer::setRPObject","E","Generic Database problem: "+e.getMessage());
        throw new NoSuchCharacterException();
        }        
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::setRPObject","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }    
    }
    
  public RPObject.ID getRPObjectID(short clientid) throws NoSuchClientIDException, NoSuchPlayerException, NoSuchCharacterException
    {
    marauroad.trace("PlayerEntryContainer::getRPObjectID",">");
    if(hasRuntimePlayer(clientid))
      {
      try
        {
        RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
        RPObject.ID id=new RPObject.ID(playerDatabase.getRPObject(entry.username,entry.choosenCharacter));
    
        marauroad.trace("PlayerEntryContainer::getRPObjectID","<");
        return id;
        }
      catch(PlayerDatabase.PlayerNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getRPObjectID","E","No such Player(unknown)");
        throw new NoSuchPlayerException();
        }
      catch(PlayerDatabase.CharacterNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getRPObjectID","E","No such Character(unknown)");
        throw new NoSuchCharacterException();
        }        
      catch(Attributes.AttributeNotFoundException e)
        {
        marauroad.trace("PlayerEntryContainer::getRPObjectID","E","No such attribute(object_id)");
        throw new NoSuchCharacterException();
        }        
      catch(PlayerDatabase.GenericDatabaseException e)
        {
        marauroad.trace("PlayerEntryContainer::setRPObject","E","Generic Database problem: "+e.getMessage());
        throw new NoSuchCharacterException();
        }        
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getRPObjectID","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
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

  public String getUsername(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getUsername",">");
    if(hasRuntimePlayer(clientid))
      {
      RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
         
      marauroad.trace("PlayerEntryContainer::getUsername","<");
      return entry.username;
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getUsername","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }
    
  public InetSocketAddress getInetSocketAddress(short clientid) throws NoSuchClientIDException
    {
    marauroad.trace("PlayerEntryContainer::getInetSocketAddress",">");
    if(hasRuntimePlayer(clientid))
      {
      RuntimePlayerEntry entry=(RuntimePlayerEntry)listPlayerEntries.get(new Short(clientid));
         
      marauroad.trace("PlayerEntryContainer::getInetSocketAddress","<");
      return entry.source;
      }
    else
      {
      marauroad.trace("PlayerEntryContainer::getInetSocketAddress","E","No such RunTimePlayer("+clientid+")");
      throw new NoSuchClientIDException();
      }
    }  
  }