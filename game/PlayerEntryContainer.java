package marauroa.game;

import java.util.*;
import java.net.*;


public class PlayerEntryContainer
  {
  public static final byte STATE_NULL=0;
  public static final byte STATE_LOGIN_COMPLETE=1;
  public static final byte STATE_GAME_BEGIN=2;
  
  public class PlayerEntry
    {
    public byte state;
    public InetSocketAddress source;
    }
    
  public class NoSuchClientIDException extends Throwable
    {
    public NoSuchClientIDException()
      {
      super("Unable to find the requested client id");
      }
    }
  
  HashMap listPlayerEntries;
  
  private static PlayerEntryContainer playerEntryContainer;
  
  private PlayerEntryContainer()
    { 
    listPlayerEntries=new HashMap();
    }
    
  public static PlayerEntryContainer getContainer()
    {
    if(playerEntryContainer==null)
      {
      playerEntryContainer=new PlayerEntryContainer();
      }
      
    return playerEntryContainer;
    }
    
  public boolean containsPlayer(short clientid)
    {
    return listPlayerEntries.containsKey(new Short(clientid));
    }    
    
  public int size()
    {
    /* Perhaps should consider only already logged players. */
    return listPlayerEntries.size();
    }   
    
  public short addPlayer(InetSocketAddress source)
    {
    PlayerEntry entry=new PlayerEntry();
    entry.state=STATE_NULL;
    entry.source=source;
    
    short clientid=generateClientID(source);    
    
    listPlayerEntries.put(new Short(clientid),entry);
    
    return clientid;
    }
  
  private static short maxClientID=0;
  
  private short generateClientID(InetSocketAddress source)
    {
    return ++maxClientID;    
    }
    
  public void setState(short clientid, byte newState) throws NoSuchClientIDException
    {
    if(containsPlayer(clientid))
      {
      PlayerEntry entry=(PlayerEntry)listPlayerEntries.get(new Short(clientid));
    
      entry.state=newState;
      }
    else
      {
      throw new NoSuchClientIDException();
      }
    }

  public byte getState(short clientid) throws NoSuchClientIDException
    {
    if(containsPlayer(clientid))
      {
      PlayerEntry entry=(PlayerEntry)listPlayerEntries.get(new Short(clientid));
    
      return entry.state;
      }
    else
      {
      throw new NoSuchClientIDException();
      }
    }
  }