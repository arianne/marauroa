/* $Id: RPSlot.java,v 1.15 2004/01/07 16:26:07 arianne_rpg Exp $ */
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import marauroa.marauroad;

public class RPSlot implements marauroa.net.Serializable
{
  public static class RPObjectNotFoundException extends Exception
  {
    public RPObjectNotFoundException(RPObject.ID id)
    {
      super("RP Object ["+id+"] not found");
    }
    
    public RPObjectNotFoundException(String id)
    {
      super("RP Object ["+id+"] not found");
    }
  }
  
  private String name;
  /** A List<RPObject> of objects */
  private List objects;
  
  public RPSlot()
  {
    name=new String();
    objects=new LinkedList();
  }
  
  public RPSlot(String name)
  {
    this.name=name;
    objects=new LinkedList();
  }
  
  public void setName(String name)
  {
    this.name=name;
  }
  
  public String getName()
  {
    return name;
  }
  
  public void add(RPObject object)
  {
    objects.add(object);
  }
  
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
    catch(Attributes.AttributeNotFoundException e)
    {
      marauroad.trace("RPSlot::add","X",e.getMessage());
      throw new RPObjectNotFoundException(id);
    }
  }
  
  public RPObject get() throws RPObjectNotFoundException
  {
    if(objects.size()>0)
    {
      return (RPObject)objects.get(0);
    }
    
    throw new RPObjectNotFoundException("- not available -");
  }
  
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
          it.remove();
          return object;
        }
      }
      
      throw new RPObjectNotFoundException(id);
    }
    catch(Attributes.AttributeNotFoundException e)
    {
      throw new RPObjectNotFoundException(id);
    }
  }
  
  public void clear()
    {
    objects.clear();
    }
  
  
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
    catch(Attributes.AttributeNotFoundException e)
    {
      return false;
    }
  }
  
  public int size()
  {
    return objects.size();
  }
  
  public Iterator iterator()
  {
    return objects.iterator();
  }
  
  public boolean equals(Object object)
  {
    RPSlot slot=(RPSlot)object;
    
    return name.equals(slot.name) && objects.equals(slot.objects);
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
    out.write(name);
    
    Iterator  it=objects.iterator();
    out.write((int)objects.size());
    
    while(it.hasNext())
    {
      RPObject entry=(RPObject)it.next();
      entry.writeObject(out);
    }
  }
  
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
  {
    name = in.readString();
    int size=in.readInt();
    objects.clear();
      
    for(int i=0;i<size;++i)
      {
      objects.add(in.readObject(new RPObject())); 
      }
  }
}
