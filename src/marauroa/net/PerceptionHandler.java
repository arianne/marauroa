/* $Id: PerceptionHandler.java,v 1.2 2004/04/30 20:36:40 root777 Exp $ */
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
package marauroa.net;

import java.io.*;
import java.net.*;
import java.util.*;
import marauroa.game.*;
import marauroa.*;

public class PerceptionHandler
  {    
  /*** HACK: From here and below all the stuff about applying perceptions, I 
   *  think it is asking for a refactoring ***/

  public interface IPerceptionListener
    {
    public boolean onAdded(RPObject object);
    public boolean onModifiedAdded(RPObject object, RPObject changes);
    public boolean onModifiedDeleted(RPObject object, RPObject changes);
    public boolean onDeleted(RPObject object);    
    public boolean onMyRPObject(boolean changed,RPObject object);    
    public boolean onClear();

    public int onTimeout();
    public int onSynced();
    public int onUnsynced();
    
    public int onPerceptionBegin(byte type, int timestamp);
    public int onPerceptionEnd(byte type, int timestamp);
    public int onException(Exception e);
    }
  
  static public class DefaultPerceptionListener implements IPerceptionListener
    {
    public DefaultPerceptionListener()
      {
      }
      
    public boolean onAdded(RPObject object)
      {
      return false;
      }
      
    public boolean onModifiedAdded(RPObject object, RPObject changes)
      {
      return false;
      }
      
    public boolean onModifiedDeleted(RPObject object, RPObject changes)
      {
      return false;
      }
      
    public boolean onDeleted(RPObject object)
      {
      return false;
      }
      
    public boolean onMyRPObject(boolean changed,RPObject object)
      {
      return false;
      }
      
    public boolean onClear()
      {
      return false;
      }
      

    public int onTimeout()
      {
      return 0;
      }
      
    public int onSynced()
      {
      return 0;
      }
      
    public int onUnsynced()
      {
      return 0;
      }
     
    
    public int onPerceptionBegin(byte type, int timestamp)
      {
      return 0;
      }
 
    public int onPerceptionEnd(byte type, int timestamp)
      {
      return 0;
      }
      
    public int onException(Exception e)
      {
      return 0;
      }      
    }
    
  private IPerceptionListener listener;
  private int previousTimestamp;
  private boolean synced;
  
  public PerceptionHandler()
    {
    this.listener=new DefaultPerceptionListener();
    synced=false;
    }

  public PerceptionHandler(IPerceptionListener listener)
    {
    this.listener=listener;
    previousTimestamp=-1;
    synced=false;
    }
  
  /**
  TODO: Still not ready for this one 
  public void apply(MessageS2CPerception message)
    {
    apply(message,null)
    }
  **/
    
  public void apply(MessageS2CPerception message, Map world_instance)
    {
    listener.onPerceptionBegin(message.getTypePerception(), message.getPerceptionTimestamp());
    
    if(message.getTypePerception()==RPZone.Perception.SYNC)
      {
      try
        {
        /** OnSync: Keep processing */
        previousTimestamp=message.getPerceptionTimestamp();
        
        applyPerceptionAddedRPObjects(message,world_instance);
        applyPerceptionMyRPObject(message,world_instance);

        if(!synced)
          {
          synced=true;
          listener.onSynced();
          }
        }
      catch(Exception e)
        {
        listener.onException(e);
        }
      }
    else if(message.getTypePerception()==RPZone.Perception.DELTA && previousTimestamp+1==message.getPerceptionTimestamp())
      {
      try
        {
        /** OnSync: Keep processing */
        previousTimestamp=message.getPerceptionTimestamp();
        
        applyPerceptionDeletedRPObjects(message,world_instance);
        applyPerceptionModifiedRPObjects(message,world_instance);
        applyPerceptionAddedRPObjects(message,world_instance);
        applyPerceptionMyRPObject(message,world_instance);
        }
      catch(Exception e)
        {
        listener.onException(e);
        }
      }
    else
      {
      synced=false;
      listener.onUnsynced();
      }

    if(message.getPerceptionTimestamp()-previousTimestamp>50)
      {
      listener.onTimeout();
      }  

    listener.onPerceptionEnd(message.getTypePerception(), message.getPerceptionTimestamp());
    }


  /** This method applys perceptions addedto the Map<RPObject::ID,RPObject> passed as argument.
   *  It clears the map if this is a sync perception */
  private void applyPerceptionAddedRPObjects(MessageS2CPerception message,Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
      
      if(message.getTypePerception()==RPZone.Perception.SYNC)
        {
        if(!listener.onClear())
          {
          world.clear();
          }
        }
    
      it=message.getAddedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(!listener.onAdded(object))
          {
          world.put(object.get("id"),object);    
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionAddedRPObjects","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }

  /** This method applys perceptions deleted to the Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionDeletedRPObjects(MessageS2CPerception message,Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
    
      it=message.getDeletedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(!listener.onDeleted(object))
          { 
          world.remove(object.get("id"));    
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionDeletedRPObjects","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }

  /** This method applys perceptions modified added and modified deleted to the
   *  Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionModifiedRPObjects(MessageS2CPerception message,Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
    
      it=message.getModifiedDeletedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        RPObject w_object=(RPObject)world.get(object.get("id"));    
        if(!listener.onModifiedDeleted(w_object,object))
          {
          w_object.applyDifferences(null,object);        
          }
        }

      it=message.getModifiedAddedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        RPObject w_object=(RPObject)world.get(object.get("id"));    
        if(!listener.onModifiedAdded(w_object,object))
          {
          w_object.applyDifferences(object,null);        
          }
        }
      }
    catch(RPZone.RPObjectNotFoundException e)
      {
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw e;
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }
  
  private void applyPerceptionMyRPObject(MessageS2CPerception message,Map world) throws RPZone.RPObjectNotFoundException
    {
    try
      {
      RPObject myObject=message.getMyRPObject();
      if(myObject!=null)
        {
        if(!listener.onMyRPObject(true,myObject))
          {
          world.put(myObject.get("id"),myObject);
          }        
        }
      else
        {
        listener.onMyRPObject(false,myObject);
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyPerceptionMyRPObject","X",e.getMessage());
      throw new RPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }
      
  }
