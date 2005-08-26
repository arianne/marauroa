/* $Id: MarauroaRPZone.java,v 1.7 2005/08/26 16:06:37 mtotz Exp $ */
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
package marauroa.server.game;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import marauroa.common.Log4J;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.IRPZone;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import org.apache.log4j.Logger;

/** default implementation of <code>IRPZone</code> */
public abstract class MarauroaRPZone implements IRPZone
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(MarauroaRPZone.class);

  protected ID zoneid;	
  protected Map<RPObject.ID,RPObject> objects;
  
  private Map<RPObject.ID,RPObject> modified;
  private Perception perception;
  private Perception prebuildDeltaPerception=null;
  private Perception prebuildTotalPerception=null;

  private static int lastNonPermanentIdAssigned=0;
  
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
  
  abstract public void onInit() throws Exception;
  abstract public void onFinish() throws Exception;
 
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
      
      // If objects has been removed, remove from modified
      modified.remove(object.getID());
      
      perception.removed((RPObject)object.copy());

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
      
    object.put("id",id.getObjectID());
    object.put("zoneid",zoneid.getID());
    }
    
  public Iterator<RPObject> iterator()
    {
    return objects.values().iterator();
    }
 
  public Perception getPerception(RPObject.ID id, byte type)
    {
    if ((prebuildDeltaPerception==null || prebuildTotalPerception==null) && logger.isDebugEnabled())
      {
      StringBuffer world=new StringBuffer("World content: \n");
      
      for(RPObject object: objects.values())
        {
        world.append("  "+object.toString()+"\n");
        }
      logger.debug("getPerception(), "+world.toString());
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
            logger.error("cannot add object to modified list (object is: ["+modified_obj+"])",e);
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
