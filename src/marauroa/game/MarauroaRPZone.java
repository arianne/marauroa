/* $Id: MarauroaRPZone.java,v 1.26 2004/03/25 16:41:49 arianne_rpg Exp $ */
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
  private Map previous_turn;
  private Map objects;
  private Perception perception;
  private JDBCPlayerDatabase rpobjectDatabase;
  private Transaction transaction;
  private static Random rand=new Random();
  public MarauroaRPZone()
    {
    rand.setSeed(new Date().getTime());
    objects=new LinkedHashMap();
    previous_turn=new LinkedHashMap();
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
        // if(previous_turn.containsKey(new RPObject.ID(object)))
        // {
        // RPObject previous=(RPObject)previous_turn.get(new RPObject.ID(object));		
        // perception.modified(object,previous);
        // previous_turn.remove(new RPObject.ID(object));      
        // }
        // else
        {
        // TODO: This
        // marauroad.trace("MarauroaRPZone::modify","X","Unable to use DELTA perception for this object because it was never get. Sending FULL instead");
        perception.added(object);
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
      
      if(!previous_turn.containsKey(id))
        {
        previous_turn.put(id,object.copy());
        }
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
	
  public Perception getPerception(RPObject.ID id, byte type)
    {
    if(type==Perception.DELTA)
      {
      return perception;
      }
    else
      {
      Perception p=new Perception(Perception.TOTAL);

      p.addedList=new ArrayList(objects.values());
      return p;
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
    previous_turn.clear();
    perception=new Perception(Perception.DELTA);
    }
  }
