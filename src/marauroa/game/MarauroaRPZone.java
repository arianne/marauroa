/* $Id: MarauroaRPZone.java,v 1.38 2004/04/16 12:23:58 arianne_rpg Exp $ */
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
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import marauroa.marauroad;

public class MarauroaRPZone implements RPZone
  {
  private Map objects;
  private List modified;
  private Perception perception;

  private JDBCPlayerDatabase rpobjectDatabase;
  private Transaction transaction;

  private static Random rand=new Random();

  public MarauroaRPZone()
    {
    rand.setSeed(new Date().getTime());
    objects=new LinkedHashMap();
    modified=new LinkedList();
    perception=new Perception(Perception.DELTA);
    try
      {
      rpobjectDatabase=(JDBCPlayerDatabase)JDBCPlayerDatabase.getDatabase();
      transaction=rpobjectDatabase.getTransaction();
      }
    catch(Exception e)
      {
      marauroad.trace("MarauroaRPZone::MarauroaRPZone","!",e.getMessage());
      System.exit(1);
      }
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
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("MarauroaRPZone::add","X",e.getMessage());
      throw new RPObjectInvalidException(e.getAttribute());
      }
    }
  
  public void modify(RPObject object) throws RPObjectInvalidException
    {
    try 
      {
      /** Uncoment to disable Delta-delta: */
//      perception.added(object);
      boolean already_added=false;

      Iterator it=modified.iterator();
      while(it.hasNext() && !already_added)
        {
        RPObject previous=(RPObject)it.next();        
        if(previous.get("id").equals(object.get("id")))
          {
          already_added=true;
          }
        }
      
      if(!already_added)
        {
        modified.add(object);
        }      
      }
    catch(Exception e)
      {
      e.printStackTrace();
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
  
  public RPObject create()
    {
    return new RPObject(rpobjectDatabase.getValidRPObjectID(transaction));
    }
	
  public Iterator iterator()
    {
    return objects.values().iterator();
    }
  
  private Perception prebuildDeltaPerception=null;
  private Perception prebuildTotalPerception=null;
 
  public Perception getPerception(RPObject.ID id, byte type)
    {
    StringBuffer world=new StringBuffer("World content: \n");
    
    Iterator world_it=objects.values().iterator();
    while(world_it.hasNext())
      {
      RPObject object=(RPObject)world_it.next();
      world.append("  "+object.toString()+"\n");
      }
    marauroad.trace("World content--","D",world.toString());

    if(type==Perception.DELTA)
      {
      if(prebuildDeltaPerception==null)
        {
        prebuildDeltaPerception=perception;
        
        Iterator it=modified.iterator();
        while(it.hasNext())
          {
          try
            {          
            prebuildDeltaPerception.modified(((RPObject)it.next()));
            }
          catch(Exception e)
            {
            e.printStackTrace();
            }
          }

        it=prebuildDeltaPerception.addedList.iterator();
        while(it.hasNext())
          {
          ((RPObject)it.next()).resetAddedAndDeleted();
          }

        modified.clear();
        }
      
      return prebuildDeltaPerception;
      }
    else
      {
      if(prebuildTotalPerception==null)
        {
        prebuildTotalPerception=new Perception(Perception.SYNC);
        prebuildTotalPerception.addedList=new ArrayList(objects.values());
        
        Iterator it=prebuildTotalPerception.addedList.iterator();
        while(it.hasNext())
          {
          ((RPObject)it.next()).resetAddedAndDeleted();
          }
        }
        
      return prebuildTotalPerception;
      }
    }
  
  public void reset()
    {
    Iterator it=objects.values().iterator();
    while(it.hasNext())
      {
      ((RPObject)it.next()).resetAddedAndDeleted();
      }
    }
	
  public long size()
    {
    return objects.size();
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
    prebuildTotalPerception=null;
    prebuildDeltaPerception=null;
    modified.clear();

    perception=new Perception(Perception.DELTA);
    }
  }
