/* $Id: RPSlot.java,v 1.2 2005/02/09 20:22:28 arianne_rpg Exp $ */
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
package marauroa.common.game;

import java.io.IOException;
import java.util.*;

import marauroa.server.*;
import marauroa.common.*;

/** This class represent a slot in an object */
public class RPSlot implements marauroa.common.net.Serializable, Iterable<RPObject>
  {
  private List<RPObject> added;
  private List<RPObject> deleted;
  
  public void resetAddedAndDeletedRPObjects()
    {
    added.clear();
    deleted.clear();
    }

  public void setAddedRPObject(RPSlot slot)
    {
    
    for(RPObject object: slot.added)
      {
      try
        {
        add((RPObject)object.copy());
        }
      catch(Exception e)
        {
        Logger.thrown("RPObject::setAddedRPSlot","X",e);
        }
      }       
    
    slot.added.clear();
    }

  public void setDeletedRPObject(RPSlot slot)
    {
    
    for(RPObject object: slot.deleted)
      {
      try
        {
        add((RPObject)object.copy());
        }
      catch(Exception e)
        {
        Logger.thrown("RPObject::setAddedRPSlot","X",e);
        }
      }       
    
    slot.deleted.clear();
    }

   
  private String name;
  /** A List<RPObject> of objects */
  private List<RPObject> objects;
  
  public RPSlot()
    {
    name="";
    initialize();
    }
  
  public RPSlot(String name)
    {
    this.name=name;
    initialize();
    }
  
  private void initialize()
    {
    objects=new LinkedList<RPObject>();
    added=new LinkedList<RPObject>();
    deleted=new LinkedList<RPObject>();
    }
  
  /** This method create a copy of the slot */
  public Object copy()
    {
    RPSlot slot=new RPSlot();

    slot.name=name;

    for(RPObject object: objects)
      {
      slot.add((RPObject)object.copy());
      }
    return slot;
    }
  
  /** Sets the name of the slot */
  public void setName(String name)
    {
    this.name=name;
    }
  
  /** Get the name of the slot */
  public String getName()
    {
    return name;
    }
  
  /** Add an object to the slot */
  public void add(RPObject object)
    {
    try
      {
      Iterator<RPObject> it=objects.iterator();
      boolean found=false;
      
      while(!found && it.hasNext())
        {
        RPObject data=it.next();
        if(data.get("id").equals(object.get("id")))
          {
          it.remove();
          found=true;
          }
        }

      if(!found)
        {
        added.add(object);      
        }
  
      objects.add(object);
      }
    catch(AttributeNotFoundException e)
      {
      Logger.thrown("RPSlot::add","X",e);
      }
    }
  
  /** Gets the object from the slot */
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException
    {
    try
      {
      for(RPObject object: objects)
        {
        if(id.equals(new RPObject.ID(object)))
          {
          return object;
          }
        }
      throw new RPObjectNotFoundException(id);
      }
    catch(AttributeNotFoundException e)
      {
      Logger.thrown("RPSlot::get","X",e);
      throw new RPObjectNotFoundException(id);
      }
    }
  
  // TODO: Consider removing this method
  public RPObject get() throws RPObjectNotFoundException
    {
    if(objects.size()>0)
      {
      return (RPObject)objects.get(0);
      }
      
    throw new RPObjectNotFoundException(RPObject.INVALID_ID);
    }
  
  /** This method removes the object of the slot */
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException
    {
    try
      {
      Iterator<RPObject> it=objects.iterator();
			
      while(it.hasNext())
        {
        RPObject object=it.next();

        if(id.equals(new RPObject.ID(object)))
          {
          /* HACK: This is a hack to avoid a problem that happens when on the 
           *  same turn an object is added and deleted, causing the client to confuse. */
          boolean found_in_added_list=false;
          Iterator<RPObject> added_it=added.iterator();
          while(!found_in_added_list && added_it.hasNext())
            {
            RPObject added_object=added_it.next();
            if(id.equals(new RPObject.ID(added_object)))
              {
              added_it.remove();
              found_in_added_list=true;
              }
            }

          if(!found_in_added_list)
            {
            deleted.add(new RPObject(new RPObject.ID(object)));
            }
            
          it.remove();
          return object;
          }
        }
      throw new RPObjectNotFoundException(id);
      }
    catch(AttributeNotFoundException e)
      {
      Logger.thrown("RPSlot::remove","X",e);
      throw new RPObjectNotFoundException(id);
      }
    }
  
  /** This method empty the slot */
  public void clear()
    {
    for(RPObject object: objects)
      {
      try
        {
        deleted.add(new RPObject(new RPObject.ID(object)));
        }
      catch(AttributeNotFoundException e)
        {
        }
      }
      
    objects.clear();
    }
  
  /** This method returns true if the slot has the object whose id is id */
  public boolean has(RPObject.ID id)
    {
    try
      {
      for(RPObject object: objects)
        {
        if(id.equals(new RPObject.ID(object)))
          {
          return true;
          }
        }
      return false;
      }
    catch(AttributeNotFoundException e)
      {
      return false;
      }
    }
  
  /** Return the number of elements in the slot */
  public int size()
    {
    return objects.size();
    }

  /** Iterate over the objects of the slot */
  public Iterator<RPObject> iterator()
    {
    return Collections.unmodifiableList(objects).iterator();
    }
  
  /** Returns true if both objects are equal */
  public boolean equals(Object object)
    {
    RPSlot slot=(RPSlot)object;
		
    return name.equals(slot.name) && objects.equals(slot.objects);
    }

  public int hashCode()
    {
    return name.hashCode()+objects.hashCode();
    }   
  
  
  public String toString()
    {
    StringBuffer str=new StringBuffer();

    str.append("RPSlot named("+name+") with [");


    for(RPObject object: objects)
      {
      str.append(object.toString());
      }
    str.append("]");
    return str.toString();
    }
  
  public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException
    {
    writeObject(out,false);
    }
	
  public void writeObject(marauroa.common.net.OutputSerializer out,boolean fulldata) throws java.io.IOException
    {
    out.write(name);
    out.write((int)objects.size());
    for(RPObject object: objects)
      {
      object.writeObject(out,fulldata);
      }
    }
  
  public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    name = in.readString();

    int size=in.readInt();
		
    if(size>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(size)+" size");
      }
    objects.clear();
    for(int i=0;i<size;++i)
      {
      objects.add((RPObject)in.readObject(new RPObject()));
      }
    }
  }
