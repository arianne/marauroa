/* $Id: RPZone.java,v 1.24 2004/05/10 14:46:07 arianne_rpg Exp $ */
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public interface RPZone
  {
  public static class RPObjectNotFoundException extends Exception
    {
    public RPObjectNotFoundException(RPObject.ID id)
      {
      super("RP Object ["+id+"] not found");
      }
    }
  

  public static class RPObjectInvalidException extends Exception
    {
    public RPObjectInvalidException(String attribute)
      {
      super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
      }
    }
    

  public static class Perception
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
      if(!deletedList.contains(object))
        {
        if(addedList.contains(object))
          {
          addedList.remove(object);
          }
          
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
      deletedList.add(object);
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
  public void add(RPObject object) throws RPObjectInvalidException;
  public void modify(RPObject object) throws RPObjectInvalidException;
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  public boolean has(RPObject.ID id);
  public RPObject create();
  public Iterator iterator();
  public long size();
  
  public Perception getPerception(RPObject.ID id, byte type);
  
  public void nextTurn();
  }
