/* $Id: RPSlot.java,v 1.33 2004/11/12 15:39:15 arianne_rpg Exp $ */
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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import marauroa.TimeoutConf;
import marauroa.marauroad;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** This class represent a slot in an object */
public class RPSlot implements marauroa.net.Serializable, Cloneable
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
    Iterator it=slot.added.iterator();
    
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      try
        {
        add((RPObject)object.copy());
        }
      catch(Exception e)
        {
        marauroad.thrown("RPObject::setAddedRPSlot","X",e);
        }
      }       
    
    slot.added.clear();
    }

  public void setDeletedRPObject(RPSlot slot)
    {
    Iterator it=slot.deleted.iterator();
    
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      try
        {
        add((RPObject)object.copy());
        }
      catch(Exception e)
        {
        marauroad.thrown("RPObject::setAddedRPSlot","X",e);
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

    Iterator it=objects.iterator();

    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
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
      Iterator it=objects.iterator();
      boolean found=false;
      
      while(!found && it.hasNext())
        {
        RPObject data=(RPObject)it.next();
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
      marauroad.thrown("RPSlot::add","X",e);
      }
    }
  
  /** Gets the object from the slot */
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException
    {
    try
      {
      Iterator it=objects.iterator();
			
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();

        if(id.equals(new RPObject.ID(object)))
          {
          return object;
          }
        }
      throw new RPObjectNotFoundException(id);
      }
    catch(AttributeNotFoundException e)
      {
      marauroad.thrown("RPSlot::get","X",e);
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
      Iterator it=objects.iterator();
			
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();

        if(id.equals(new RPObject.ID(object)))
          {
          /* HACK: This is a hack to avoid a problem that happens when on the 
           *  same turn an object is added and deleted, causing the client to confuse. */
          boolean found_in_added_list=false;
          Iterator added_it=added.iterator();
          while(!found_in_added_list && added_it.hasNext())
            {
            RPObject added_object=(RPObject)added_it.next();
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
      marauroad.thrown("RPSlot::remove","X",e);
      throw new RPObjectNotFoundException(id);
      }
    }
  
  /** This method empty the slot */
  public void clear()
    {
    Iterator it=objects.iterator();
            
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
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
      Iterator it=objects.iterator();
			
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();

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
  public Iterator iterator()
    {
    return objects.iterator();
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

    Iterator it=iterator();

    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();

      str.append(object.toString());
      }
    str.append("]");
    return str.toString();
    }
  
  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    writeObject(out,false);
    }
	
  public void writeObject(marauroa.net.OutputSerializer out,boolean fulldata) throws java.io.IOException
    {
    out.write(name);
		
    Iterator  it=objects.iterator();

    out.write((int)objects.size());
    while(it.hasNext())
      {
      RPObject entry=(RPObject)it.next();

      entry.writeObject(out,fulldata);
      }
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
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
