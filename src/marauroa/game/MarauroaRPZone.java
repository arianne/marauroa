/* $Id: MarauroaRPZone.java,v 1.67 2004/11/21 14:17:31 root777 Exp $ */
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
import java.io.*;
import marauroa.marauroad;

public class MarauroaRPZone implements IRPZone
  {
  protected ID zoneid;	
  private Map<RPObject.ID,RPObject> objects;
  private Map<RPObject.ID,RPObject> modified;
  private Perception perception;

  private static Random rand=new Random();

  public MarauroaRPZone()
    {
    initialize("");
    }
    
  public MarauroaRPZone(String zoneid)
    {
    initialize(zoneid);
    }
  
  private void initialize(String zoneid)
    {    
    this.zoneid=new ID(zoneid);
    rand.setSeed(new Date().getTime());
    
    objects=new LinkedHashMap<RPObject.ID,RPObject>();    
    modified=new LinkedHashMap<RPObject.ID,RPObject>();
    
    perception=new Perception(Perception.DELTA,getID());
    }
  
  public ID getID()
    {
    return zoneid;
    }
  
  public void onInit() throws Exception
    {
    }

  public void onFinish() throws Exception
    {
    }
  
  public void add(RPObject object) throws RPObjectInvalidException
    {
    try
      {
      RPObject.ID id=new RPObject.ID(object);
    
      object.resetAddedAndDeleted();
      objects.put(id,object);
    
      perception.added(object);
      }
    catch(AttributeNotFoundException e)
      {
      throw new RPObjectInvalidException(e.getMessage());
      }
    }
  
  public void modify(RPObject object) throws RPObjectInvalidException
    {
    try
      {
      RPObject.ID id=new RPObject.ID(object);

      if(!modified.containsKey(id) && has(id))
        {
        modified.put(id,object);
        }
      }
    catch(Exception e)
      {
      throw new RPObjectInvalidException(e.getMessage());
      }
    }
    
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException
    {
    if(objects.containsKey(id))
      {
      RPObject object=(RPObject)objects.remove(id);
      perception.removed(object);

      return object;
      }
    else
      {
      throw new RPObjectNotFoundException(id);
      }
    }
  
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException
    {
    if(objects.containsKey(id))
      {
      RPObject object=(RPObject)objects.get(id);
      return object;
      }
    throw new RPObjectNotFoundException(id);
    }
  
  public boolean has(RPObject.ID id)
    {
    if(objects.containsKey(id))
      {
      return true;
      }
    else
      {
      return false;
      }
    }
  
  static private int lastNonPermanentIdAssigned=0;
  
  public RPObject create()
    {
    RPObject.ID id=new RPObject.ID(++lastNonPermanentIdAssigned,zoneid);
    while(has(id))
      {
      id=new RPObject.ID(++lastNonPermanentIdAssigned,zoneid);
      }
      
    return new RPObject(id);
    }

  public void assignRPObjectID(RPObject object)
    {
    RPObject.ID id=new RPObject.ID(++lastNonPermanentIdAssigned,zoneid);
    while(has(id))
      {
      id=new RPObject.ID(++lastNonPermanentIdAssigned,zoneid);
      }
      
    /** TODO: Ugly */
    object.put("id",id.getObjectID());
    object.put("zoneid",zoneid.getID());
    }
    
  public Iterator<RPObject> iterator()
    {
    return objects.values().iterator();
    }
  
  private Perception prebuildDeltaPerception=null;
  private Perception prebuildTotalPerception=null;
 
  public Perception getPerception(RPObject.ID id, byte type)
    {
    if((prebuildDeltaPerception==null || prebuildTotalPerception==null) && marauroad.loggable("MarauroaRPZone::getPerception","D"))
      {
      StringBuffer world=new StringBuffer("World content: \n");
      
      for(RPObject object: objects.values())
        {
        world.append("  "+object.toString()+"\n");
        }
      marauroad.trace("MarauroaRPZone::getPerception","D",world.toString());
      }

    if(type==Perception.DELTA)
      {
      if(prebuildDeltaPerception==null)
        {
        prebuildDeltaPerception=perception;
        
        for(RPObject modified_obj: modified.values())
          {
          try
            {
            prebuildDeltaPerception.modified(modified_obj);
            }
          catch(Exception e)
            {
            marauroad.thrown("MarauroaRPZone::getPerception","X",e);
            }
          }
        }
      
      return prebuildDeltaPerception;
      }
    else /* type==Perception.SYNC */
      {
      if(prebuildTotalPerception==null)
        {
        prebuildTotalPerception=new Perception(Perception.SYNC,getID());
        prebuildTotalPerception.addedList=new ArrayList<RPObject>(objects.values());
        }
        
      return prebuildTotalPerception;
      }
    }
  
  public void reset()
    {
    for(RPObject object: objects.values())
      {
      object.resetAddedAndDeleted();
      }
    }
    
  public long size()
    {
    return objects.size();
    }
  
  public void print(PrintStream out)
    {
      for(RPObject object: objects.values())  
      {
      out.println(object);
      }
    }
  
  public void nextTurn()
    {
    reset();
    
    prebuildTotalPerception=null;
    prebuildDeltaPerception=null;
    modified.clear();

    perception.clear();
    }
  }
