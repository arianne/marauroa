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
    public String username;
    public Date timestamp;
    }
    
  public class NoSuchClientIDException extends Throwable
    {
    public NoSuchClientIDException()
      {
      super("Unable to find the requested client id");
      }
    }
  
  private HashMap listPlayerEntries;
  
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
    
  public short addPlayer(String username, InetSocketAddress source)
    {
    PlayerEntry entry=new PlayerEntry();
    entry.state=STATE_NULL;
    entry.username=username;
    entry.source=source;
    entry.timestamp=new Date();
    
    short clientid=generateClientID(source);    
    
    listPlayerEntries.put(new Short(clientid),entry);
    
    return clientid;
    }
    
  public void removePlayer(short clientid) throws NoSuchClientIDException
    {
    if(containsPlayer(clientid))
      {
      listPlayerEntries.remove(new Short(clientid));
      }
    else
      {
      throw new NoSuchClientIDException();
      }
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
      entry.timestamp=new Date();
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
      entry.timestamp=new Date();    
      return entry.state;
      }
    else
      {
      throw new NoSuchClientIDException();
      }
    }
    
  public boolean isPlayer(short clientid, InetSocketAddress source)
    {
    if(containsPlayer(clientid))
      {
      PlayerEntry entry=(PlayerEntry)listPlayerEntries.get(new Short(clientid));
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
    
  public String getUsername(short clientid) throws NoSuchClientIDException
    {
    if(containsPlayer(clientid))
      {
      PlayerEntry entry=(PlayerEntry)listPlayerEntries.get(new Short(clientid));
         
      return entry.username;
      }
    else
      {
      throw new NoSuchClientIDException();
      }
    }
    
  public InetSocketAddress getInetSocketAddress(short clientid) throws NoSuchClientIDException
    {
    if(containsPlayer(clientid))
      {
      PlayerEntry entry=(PlayerEntry)listPlayerEntries.get(new Short(clientid));
         
      return entry.source;
      }
    else
      {
      throw new NoSuchClientIDException();
      }
    }  
  }