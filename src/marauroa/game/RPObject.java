/* $Id: RPObject.java,v 1.20 2003/12/08 12:39:53 arianne_rpg Exp $ */
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
import java.util.NoSuchElementException;
import marauroa.marauroad;

/** This class implements an Object. Please refer to Objects Explained document */
public class RPObject extends Attributes
  {
  /**  object type used by factory to choose a  right class*/
  protected int objectType;
  /** a List<RPSlot> of slots */
  private List slots;
  
  public static class NoSlotFoundException extends Exception
    {
    public NoSlotFoundException(String slot)
      {
      super("Slot ["+slot+"] not found");
      }
    }

  public static class SlotAlreadyAddedException extends Exception
    {
    public SlotAlreadyAddedException(String slot)
      {
      super("Slot ["+slot+"] already added.");
      }
    }
  
  /** An iterator for properly acceding all the Slots. */
  public class SlotsIterator
    {
    Iterator it;
    
    /** Constructor */
    public SlotsIterator()
      {
      it=slots.iterator();
      }
    
    /** This method returns true if there are still most elements.
     *  @return true if there are more elements. */
    public boolean hasNext()
      {
      return it.hasNext();
      }
    
    /** This method returs the RPSlot and move the pointer to the next element
     *  @return an RPSlot */
    public RPSlot next() throws NoSuchElementException
      {
      return (RPSlot)it.next();
      }
    }
  
  public static ID INVALID_ID=new ID(-1);
    
  /** Constructor */
  public RPObject()
    {
    super();

    slots=new LinkedList();
      objectType=0;
    }

  /** Constructor
   *  @param id the id of the object */
  public RPObject(ID id)
    {
    super();

    slots=new LinkedList();
    put("object_id",id.getObjectID());
    }
  
  /** This method returns true if the object has that slot
   *  @param name the name of the slot
   *  @return true if slot exists or false otherwise */
  public boolean hasSlot(String name)
    {
    SlotsIterator it=slotsIterator();
    
    while(it.hasNext())
      {
      RPSlot slot=it.next();
      if(name.equals(slot.getName()))
        {
        return true;
        }
      }
    
    return false;
    }
  
  /** This method add the slot to the object
   *  @param slot the RPSlot object 
   *  @throws SlotAlreadyAddedException if the slot already exists */
  public void addSlot(RPSlot slot) throws SlotAlreadyAddedException
    {
    if(!hasSlot(slot.getName()))
      {
      slots.add(slot);
      }
    else
      {
      throw new SlotAlreadyAddedException(slot.getName());
      }
    }
  
  /** This method returns a slot whose name is name 
   *  @param name the name of the slot
   *  @return the slot
   *  @throws NoSlotFoundException if the slot is not found */
  public RPSlot getSlot(String name) throws NoSlotFoundException
    {
    SlotsIterator it=slotsIterator();
    while(it.hasNext())
      {
      RPSlot slot=it.next();
      if(name.equals(slot.getName()))
        {
        return slot;
        }
      }
   
    throw new NoSlotFoundException(name);
    }

  /** Returns a iterator over the slots 
   *  @return an iterator over the slots*/  
  public SlotsIterator slotsIterator()
    {
    return new SlotsIterator();
    }
  
  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    StringBuffer tmp=new StringBuffer("RPObject with ");
    tmp.append(super.toString());
    tmp.append(" and RPSlots ");
    
    SlotsIterator it=slotsIterator();
    while(it.hasNext())
      {
      RPSlot slot=(RPSlot)it.next();
      tmp.append("["+slot.toString()+"]");
      }
    
    return tmp.toString();
    }
  
  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    super.writeObject(out);
    
    out.write((int)slots.size());

    SlotsIterator it=slotsIterator();
    while(it.hasNext())
      {
      out.write(it.next());
      }
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    int size=in.readInt();
    marauroad.trace("RPObject.readObject()","D",size+" slots found");
    slots=new LinkedList();
    
    for(int i=0;i<size;++i)
      {
      slots.add((RPSlot)in.readObject(new RPSlot()));
      }
    }

  /** This class stores the basic identification for a RPObject */
  public static class ID implements marauroa.net.Serializable
    {
    private int id;
    
    /** Constructor
     *  @param oid the object id */
    public ID(int oid)
      {
      id=oid;
      }
    
    /** Constructor
     *  @param attr an RPObject containing object_id attribute */
    public ID(RPObject attr) throws Attributes.AttributeNotFoundException
      {
      id=new Integer(attr.get("object_id")).intValue();
      }
    
    /** Constructor
     *  @param attr an RPAction containing source_id attribute */
    public ID(RPAction attr) throws Attributes.AttributeNotFoundException
      {
      id=new Integer(attr.get("source_id")).intValue();
      }

    /** This method returns the object id
     *  @return the object id. */
    public int getObjectID()
      {
      return id;
      }
    
    /** This method returns true of both ids are equal.
     *  @param anotherid another id object
     *  @return true if they are equal, or false otherwise. */
    public boolean equals(Object anotherid)
      {
      if(anotherid!=null)
        {
        return (id==((RPObject.ID)anotherid).id);
        }
      else
        {
        return(false);
        }
      }
      
    /** We need it for HashMap */
    public int hashCode()
      {
      return id;
      }
    
    /** This method returns a String that represent the object
     *  @return a string representing the object.*/
    public String toString()
      {
      return "RPObject.ID [id="+id+"]";
      }

    public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
      {
      out.write(id);
      }
  
    public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
      {
      id=in.readInt();
      }
    }  
  }
