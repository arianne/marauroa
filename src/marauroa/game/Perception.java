/* $Id: Perception.java,v 1.3 2004/06/24 10:40:55 arianne_rpg Exp $ */
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
import marauroa.*;

public class Perception
  {
  final public static byte DELTA=0;
  final public static byte SYNC=1;
  
  public byte type;
  public List addedList;
  public List modifiedAddedAttribsList;
  public List modifiedDeletedAttribsList;
  public List deletedList;
  public Perception(byte type)
    {
    this.type=type;
    addedList=new LinkedList();
    modifiedAddedAttribsList=new LinkedList();
    modifiedDeletedAttribsList=new LinkedList();
    deletedList=new LinkedList();
    }
  
  public void added(RPObject object)
    {
    if(!addedHas(object))
      {
      addedList.add(object);
      }
    }
    
  public void modified(RPObject modified) throws Exception
    {
    if(!removedHas(modified) && !addedHas(modified))
      {
      RPObject added=new RPObject();
      RPObject deleted=new RPObject();
    
      modified.getDifferences(added,deleted);
      if(added.size()>0)
        {
        modifiedAddedAttribsList.add(added);
        }
  
      if(deleted.size()>0)
        {
        modifiedDeletedAttribsList.add(deleted);
        }
      }
    else
      {
      modified.resetAddedAndDeleted();
      }
    }
  
  public void removed(RPObject object)
    {
    if(addedHas(object))
      {
      try
        {
        Iterator it=addedList.iterator();
        while(it.hasNext())
          {
          RPObject added=(RPObject)it.next();
          if(added.get("id").equals(object.get("id")))
            {
            it.remove();
            /* NOTE: If object was added and now remove we simply don't mention the object at all */
            return;
            }
          }
        }
      catch(Attributes.AttributeNotFoundException e)
        {
        marauroad.thrown("RPZone::Perception::removed","X",e);
        }        
      }

    if(!removedHas(object))
      {
      deletedList.add(object);
      }
    }
  
  private boolean removedHas(RPObject object)
    {
    try
      {
      Iterator it=deletedList.iterator();
      while(it.hasNext())
        {
        RPObject deleted=(RPObject)it.next();
        if(deleted.get("id").equals(object.get("id")))
          {
          return true;
          }
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      }
    
    return false;
    }
  
  
  private boolean addedHas(RPObject object)
    {
    try
      {
      Iterator it=addedList.iterator();
      while(it.hasNext())
        {
        RPObject added=(RPObject)it.next();
        if(added.get("id").equals(object.get("id")))
          {
          return true;
          }
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      }
    
    return false;
    }
  
  public int size()
    {
    return (addedList.size()+deletedList.size());
    }
  
  public void clear()
    {
    addedList.clear();
    modifiedAddedAttribsList.clear();
    modifiedDeletedAttribsList.clear();
    deletedList.clear();
    }
  }
  
  