/* $Id: MarauroaRPZone.java,v 1.14 2004/01/07 14:44:38 arianne_rpg Exp $ */
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
import marauroa.*;

public class MarauroaRPZone implements RPZone
  {
  private HashMap objects;
  private List listObjects;
  private Perception perception;

  private static Random rand=new Random();
  
  public MarauroaRPZone()
    {
    rand.setSeed(new Date().getTime());
    objects=new HashMap();
    
    listObjects=new LinkedList();
    perception=new Perception(Perception.DELTA);
    }
  
  public void add(RPObject object) throws RPObjectInvalidException
    {
    try
      {
      RPObject.ID id=new RPObject.ID(object);
      objects.put(id,object);
      
      listObjects.add(object);
      perception.modified(object);
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("MarauroaRPZone::add","X",e.getMessage());
      throw new RPObjectInvalidException(e.getAttribute());
      }
    }
  
  public void modify(RPObject object) throws RPObjectNotFoundException
    {
    perception.modified(object);
    }

  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException
    {
    if(objects.containsKey(id))
      {
      RPObject object=(RPObject)objects.remove(id);
      
      listObjects.remove(object);
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
      return (RPObject)objects.get(id);
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
    RPObject.ID id=new RPObject.ID(rand.nextInt());
    
    while(has(id) || id.getObjectID()==-1)    
      {
      id=new RPObject.ID(rand.nextInt());
      }
     
    return new RPObject(id);
    }
    
  public Iterator iterator()
    {
    return listObjects.iterator();
    }

  public Perception getPerception(RPObject.ID id, byte type)
    {
    if(type==Perception.DELTA)
      {
      return perception;
      }
    else
      {
      Perception p=new Perception(Perception.TOTAL);
      p.modifiedList=listObjects;
      
      return p;
      }
    }
  
  public void print(PrintStream out)
    {
    Iterator it=iterator();
    
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      out.println(object);
      }
    }
  
  public void nextTurn() 
    {
    perception=new Perception(Perception.DELTA);
    }  
  }

