/* $Id: PerceptionHandler.java,v 1.22 2004/12/23 10:34:07 arianne_rpg Exp $ */
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
  private IPerceptionListener listener;
  private List<MessageS2CPerception> previousPerceptions;
  private int previousTimestamp;
  private boolean synced;
  
  public PerceptionHandler()
    {
    this.listener=new DefaultPerceptionListener();
    synced=false;
    previousPerceptions=new LinkedList<MessageS2CPerception>();
    }

  public PerceptionHandler(IPerceptionListener listener)
    {
    this.listener=listener;
    previousPerceptions=new LinkedList<MessageS2CPerception>();
    previousTimestamp=-1;
    synced=false;
    }
  
  public void apply(MessageS2CPerception message, Map<RPObject.ID,RPObject> world_instance) throws Exception
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
      
      for(Iterator<MessageS2CPerception> it=previousPerceptions.iterator(); it.hasNext();)
        {
        MessageS2CPerception previousmessage=it.next();
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
  private void applyPerceptionAddedRPObjects(MessageS2CPerception message,Map<RPObject.ID,RPObject> world) throws RPObjectNotFoundException
    {
    try
      {
      if(message.getTypePerception()==Perception.SYNC)
        {
        if(!listener.onClear())
          {
          world.clear();
          }
        }
    
      for(RPObject object: message.getAddedRPObjects())
        {
        if(!listener.onAdded(object))
          {
          world.put(object.getID(),object);
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionAddedRPObjects","X",e.getMessage());
      throw new RPObjectNotFoundException(RPObject.INVALID_ID);
      }
    }

  /** This method applys perceptions deleted to the Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionDeletedRPObjects(MessageS2CPerception message,Map<RPObject.ID,RPObject> world) throws RPObjectNotFoundException
    {
    try
      {
      for(RPObject object: message.getDeletedRPObjects())
        {
        if(!listener.onDeleted(object))
          {
          world.remove(object.getID());
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("MessageS2CPerception::applyPerceptionDeletedRPObjects","X",e.getMessage());
      throw new RPObjectNotFoundException(RPObject.INVALID_ID);
      }
    }

  /** This method applys perceptions modified added and modified deleted to the
   *  Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionModifiedRPObjects(MessageS2CPerception message,Map<RPObject.ID,RPObject> world) throws RPObjectNotFoundException
    {
    try
      {
      for(RPObject object: message.getModifiedDeletedRPObjects())
        {
        RPObject w_object=world.get(object.getID());
        if(!listener.onModifiedDeleted(w_object,object))
          {
          w_object.applyDifferences(null,object);
          }
        }
     
      for(RPObject object: message.getModifiedAddedRPObjects())
        {
        RPObject w_object=world.get(object.getID());
        if(!listener.onModifiedAdded(w_object,object))
          {          
          System.out.println("ApplyDifferences");
          System.out.println(w_object);
          System.out.println(object);
          w_object.applyDifferences(object,null);
          }
        }
      }
    catch(RPObjectNotFoundException e)
      {
      System.out.println(world);
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw e;
      }
    catch(Exception e)
      {
      System.out.println(world);
      e.printStackTrace();
      marauroad.trace("MessageS2CPerception::applyModifiedRPObjects","X",e.getMessage());
      throw new RPObjectNotFoundException(RPObject.INVALID_ID);
      }
    }
  
  /** This method applys perceptions for our RPObject to the Map<RPObject::ID,RPObject> passed as argument. */
  private void applyPerceptionMyRPObject(MessageS2CPerception message,Map<RPObject.ID,RPObject> world) throws RPObjectNotFoundException
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
      throw new RPObjectNotFoundException(RPObject.INVALID_ID);
      }
    }
  }
