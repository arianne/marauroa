/* $Id: PerceptionHandler.java,v 1.12 2004/07/07 10:07:22 arianne_rpg Exp $ */
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

/** The PerceptionHandler class is in charge of applying correctly the perceptions
 *  to the world. You should always use this class because it is a complex task that
 *  is easy to do in the wrong way. */
public class PerceptionHandler
  {
  /** The IPerceptionListener interface provides methods that are called while 
   *  applying the perception */    
  public interface IPerceptionListener
    {
    /** onAdded is called when an object is added to the world for first time.
     *  Return true to stop further processing. */
    public boolean onAdded(RPObject object);
    /** onModifiedAdded is called when an object is modified by adding or changing 
     *  one of its attributes. Return true to stop further processing. */
    public boolean onModifiedAdded(RPObject object, RPObject changes);
    /** onModifiedDeleted is called each time the object has one of its attributes
     *  removed. Return true to stop further processing. */

    public boolean onModifiedDeleted(RPObject object, RPObject changes);
    /** onDeleted is called when an object is removed of the world
     *  Return true to stop further processing. */
    public boolean onDeleted(RPObject object);    
    /** onMyRPObject is called when our rpobject avatar is processed.
     *  Return true to stop further processing. */
    public boolean onMyRPObject(boolean changed,RPObject object);    
    /** onClear is called when the whole world is going to be cleared. 
     *  It happens on sync perceptions
     *  Return true to stop further processing. */
    public boolean onClear();
   
    /** onTimeout is called when the client has timeout, that is, when it is 50 turns far from server*/
    public int onTimeout();
    /** onSynced is called when the client recover sync */
    public int onSynced();
    /** onUnsynced is called when the client lose sync */
    public int onUnsynced();
    
    /** onPerceptionBegin is called when the perception is going to be applied */
    public int onPerceptionBegin(byte type, int timestamp);
    /** onPerceptionBegin is called when the perception has been applied */
    public int onPerceptionEnd(byte type, int timestamp);
    /** onException is called when an exception happens */
    public int onException(Exception e, MessageS2CPerception perception) throws Exception;
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
      
    public int onException(Exception e, MessageS2CPerception perception) throws Exception
      {
      System.out.println(e.getMessage());
      System.out.println(perception);
      e.printStackTrace();
      
      throw e;      
      }      
    }
    
  private IPerceptionListener listener;
  private List previousPerceptions;
  private int previousTimestamp;
  private boolean synced;
  
  public PerceptionHandler()
    {
    this.listener=new DefaultPerceptionListener();
    synced=false;
    previousPerceptions=new LinkedList();
    }

  public PerceptionHandler(IPerceptionListener listener)
    {
    this.listener=listener;
    previousPerceptions=new LinkedList();
    previousTimestamp=-1;
    synced=false;
    }
  
  public void apply(MessageS2CPerception message, Map world_instance) throws Exception
    {
    listener.onPerceptionBegin(message.getTypePerception(), message.getPerceptionTimestamp());
    
    if(message.getTypePerception()==Perception.SYNC)
      {
      try
        {
        /** OnSync: Keep processing */
        previousTimestamp=message.getPerceptionTimestamp();
        previousPerceptions.clear();
        
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
        listener.onException(e,message);
        }
      }
    else if(message.getTypePerception()==Perception.DELTA && previousTimestamp+1==message.getPerceptionTimestamp())
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
        listener.onException(e, message);
        }
      }
    else
      {
      previousPerceptions.add(message);
      
      Iterator it=previousPerceptions.iterator();
      while(it.hasNext())
        {
        MessageS2CPerception previousmessage=(MessageS2CPerception ) it.next();
        if(previousTimestamp+1==previousmessage.getPerceptionTimestamp())         
          {
          try
            {
            /** OnSync: Keep processing */
            previousTimestamp=previousmessage.getPerceptionTimestamp();
        
            applyPerceptionDeletedRPObjects(previousmessage,world_instance);
            applyPerceptionModifiedRPObjects(previousmessage,world_instance);
            applyPerceptionAddedRPObjects(previousmessage,world_instance);
            applyPerceptionMyRPObject(previousmessage,world_instance);
            }
          catch(Exception e)
            {
            listener.onException(e, message);
            }
          
          it.remove();
          it=previousPerceptions.iterator();
          }
        }
     
      if(previousPerceptions.size()==0)
        {
        synced=true;
        listener.onSynced();
        }
      else
        {
        synced=false;
        listener.onUnsynced();
        }
      }

    if(message.getPerceptionTimestamp()-previousTimestamp>50)
      {
      listener.onTimeout();
      }  

    listener.onPerceptionEnd(message.getTypePerception(), message.getPerceptionTimestamp());
    }


  /** This method applys perceptions addedto the Map<RPObject::ID,RPObject> passed as argument.
   *  It clears the map if this is a sync perception */
  private void applyPerceptionAddedRPObjects(MessageS2CPerception message,Map world) throws IRPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
      
      if(message.getTypePerception()==Perception.SYNC)
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
          world.put(object.getID(),object);    
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionAddedRPObjects","X",e.getMessage());
      throw new IRPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }

  /** This method applys perceptions deleted to the Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionDeletedRPObjects(MessageS2CPerception message,Map world) throws IRPZone.RPObjectNotFoundException
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
          world.remove(object.getID());    
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionDeletedRPObjects","X",e.getMessage());
      throw new IRPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }

  /** This method applys perceptions modified added and modified deleted to the
   *  Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionModifiedRPObjects(MessageS2CPerception message,Map world) throws IRPZone.RPObjectNotFoundException
    {
    try
      {
      Iterator it;
    
      it=message.getModifiedDeletedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        RPObject w_object=(RPObject)world.get(object.getID());    
        if(!listener.onModifiedDeleted(w_object,object))
          {
          w_object.applyDifferences(null,object);        
          }
        }

      it=message.getModifiedAddedRPObjects().iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        RPObject w_object=(RPObject)world.get(object.getID());    
        if(!listener.onModifiedAdded(w_object,object))
          {
          w_object.applyDifferences(object,null);        
          }
        }
      }
    catch(IRPZone.RPObjectNotFoundException e)
      {
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw e;
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw new IRPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }
  
  /** This method applys perceptions for our RPObject to the Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionMyRPObject(MessageS2CPerception message,Map world) throws IRPZone.RPObjectNotFoundException
    {
    try
      {
      RPObject myObject=message.getMyRPObject();
      if(myObject!=null)
        {
        if(!listener.onMyRPObject(true,myObject))
          {
          world.put(myObject.getID(),myObject);    
          }        
        }
      else
        {
        listener.onMyRPObject(false,null);
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyPerceptionMyRPObject","X",e.getMessage());
      throw new IRPZone.RPObjectNotFoundException(new RPObject.ID(-1));
      }
    }      
  }
