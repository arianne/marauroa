/* $Id: RPObject.java,v 1.29 2004/03/22 18:31:48 arianne_rpg Exp $ */
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
import java.util.NoSuchElementException;
import marauroa.TimeoutConf;
import marauroa.marauroad;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** This class implements an Object. Please refer to Objects Explained document */
public class RPObject extends Attributes
{
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
  
  public final static ID INVALID_ID=new ID(-1);
	
  /** Constructor */
  public RPObject()
	{
		super();
		
		slots=new LinkedList();
	}
	
  /** Constructor
	 *  @param id the id of the object */
  public RPObject(ID id)
	{
		super();
		
		slots=new LinkedList();
		put("object_id",id.getObjectID());
	}
	
  public boolean isEmpty()
    {
    return super.isEmpty() && slots.isEmpty();
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

  public void removeSlot(String name) throws NoSlotFoundException
    {
        if(hasSlot(name))
        {
        Iterator it=slots.iterator();
        while(it.hasNext())
        {
            RPSlot slot=(RPSlot)it.next();
            if(name.equals(slot.getName()))
            {
                it.remove();
                return;                                
            }
        }
        }
        else
        {
            throw new NoSlotFoundException(name);
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
			RPSlot slot=it.next();
			tmp.append("["+slot.toString()+"]");
		}
		
		return tmp.toString();
	}
  
  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
	{
		writeObject(out,false);
	}
	
  public void writeObject(marauroa.net.OutputSerializer out,boolean fulldata) throws java.io.IOException
	{
		super.writeObject(out,fulldata);
		
		int size=slots.size();

        SlotsIterator it=slotsIterator();
        while(it.hasNext())
        {
            RPSlot entry=it.next();

            if(fulldata==false && entry.getName().charAt(0)=='!')
            {
                --size;
            }
        }
        
		out.write(size);
		
		it=slotsIterator();
		while(it.hasNext())
		{
			RPSlot slot=(RPSlot)it.next();
            if(fulldata==true || slot.getName().charAt(0)!='!')
              {
              slot.writeObject(out,fulldata);
              }              
		}
	}
  
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
	{
		super.readObject(in);
		
		int size=in.readInt();
		if(size>TimeoutConf.MAX_ARRAY_ELEMENTS)
		{
			throw new IOException("Illegal request of an list of "+String.valueOf(size)+" size");
		}
		
		slots=new LinkedList();
		
		for(int i=0;i<size;++i)
		{
			slots.add((RPSlot)in.readObject(new RPSlot()));
		}
	}

  /** TODO: Refactor this method. Looks like it claims for bugs!" */	
  public void getDifferencesFrom(RPObject object, RPObject added, RPObject deleted) throws Exception 
    {
    added.put("object_id",get("object_id"));
    deleted.put("object_id",get("object_id"));
    
    Iterator it=object.iterator();
    
    /* get modified or deleted attributes */
    while(it.hasNext())
      {
      String attrib=(String)it.next();
      if(has(attrib))
        {
        if(!get(attrib).equals(object.get(attrib)))
          {
          added.put(attrib,get(attrib));        
          }
        }    
      else
        {
        deleted.put(attrib,"");
        }    
      }    

    /* get new attributes */
    it=iterator();
    
    while(it.hasNext())
      {
      String attrib=(String)it.next();
      if(!object.has(attrib))
        {
        added.put(attrib,get(attrib));        
        }    
      }    
    
    /* get deleted or modified slots */ 
    SlotsIterator sit=object.slotsIterator();     
    while(sit.hasNext())
      {
      RPSlot slot=(RPSlot)sit.next();
      if(hasSlot(slot.getName()))
        {
        RPSlot actualSlot=getSlot(slot.getName());
        
        /* Check if objects match: deleted and modified*/
        Iterator objects=slot.iterator();
        while(objects.hasNext())
          {
          RPObject objectInSlot=(RPObject)objects.next();
          if(actualSlot.has(new ID(objectInSlot)))
            {
            RPObject slotAdded=new RPObject();
            RPObject slotDeleted=new RPObject();
            
            actualSlot.get(new ID(objectInSlot)).getDifferencesFrom(objectInSlot,slotAdded,slotDeleted);
            
            /** TODO: If there are no changes it shouldn't be added */
            String id=slotAdded.get("object_id");
            slotAdded.remove("object_id");
            if(!slotAdded.isEmpty())
              {
              slotAdded.put("object_id",id);
              if(!added.hasSlot(slot.getName()))
                {
                added.addSlot(new RPSlot(slot.getName()));
                }            
              added.getSlot(slot.getName()).add(slotAdded);
              }
            
            id=slotDeleted.get("object_id");
            slotDeleted.remove("object_id");
            if(!slotDeleted.isEmpty())
              {
              slotDeleted.put("object_id",id);
              if(!deleted.hasSlot(slot.getName()))
                {
                deleted.addSlot(new RPSlot(slot.getName()));
                }            
              deleted.getSlot(slot.getName()).add(slotDeleted);
              }
            }
          else
            {            
            if(!added.hasSlot(slot.getName()))
              {
              added.addSlot(new RPSlot(slot.getName()));
              }
            
            added.getSlot(slot.getName()).add((RPObject)objectInSlot.copy());
            }
          }        
          
        /** New objects only */
        objects=actualSlot.iterator();
        while(objects.hasNext())
          {
          RPObject objectInSlot=(RPObject)objects.next();
          if(!slot.has(new ID(objectInSlot)))
            {
            if(!added.hasSlot(slot.getName()))
              {
              added.addSlot(new RPSlot(slot.getName()));
              }
            
            added.getSlot(slot.getName()).add((RPObject)objectInSlot.copy());
            }
          }            
        }
      else
        {
        deleted.addSlot(new RPSlot(slot.getName()));
        }
      }  
    
    /* get new slots */  
    sit=slotsIterator();     
    while(sit.hasNext())
      {
      RPSlot slot=(RPSlot)sit.next();
      if(!object.hasSlot(slot.getName()))
        {
        added.addSlot((RPSlot)slot.copy());
        }
      }  
    }
    
  public RPObject applyDifferences(RPObject added, RPObject deleted) throws Exception
    {
    if(deleted!=null)
      {
      Iterator it=deleted.iterator();
      while(it.hasNext())
        {
        String attrib=(String)it.next();
        if(!attrib.equals("object_id"))
          {
          remove(attrib);
          }
        }
      
      SlotsIterator sit=deleted.slotsIterator();
      while(sit.hasNext())
        {
        RPSlot slot=(RPSlot)sit.next();
        if(slot.size()==0)
          {
          removeSlot(slot.getName());
          }
        else
          {
          /** for each of the objects, delete it*/
          Iterator objects=slot.iterator();
          while(objects.hasNext())
            {
            RPObject object=(RPObject)objects.next();
            getSlot(slot.getName()).get(new ID(object)).applyDifferences(null,object);
            }
          }
        }        
      }

    if(added!=null)
      {
      Iterator it=added.iterator();
      while(it.hasNext())
        {
        String attrib=(String)it.next();
        if(!attrib.equals("object_id"))
          {
          put(attrib,added.get(attrib));
          }
        }
      
      SlotsIterator sit=added.slotsIterator();
      while(sit.hasNext())
        {
        RPSlot slot=(RPSlot)sit.next();
        
        if(!hasSlot(slot.getName()))
          {
          addSlot(new RPSlot(slot.getName()));
          }

        /** for each of the objects, add it*/
        Iterator objects=slot.iterator();
        while(objects.hasNext())
          {
          RPObject object=(RPObject)objects.next();
          if(getSlot(slot.getName()).has(new ID(object)))
            {
            getSlot(slot.getName()).get(new ID(object)).applyDifferences(object,null);
            }
          else
            {
            getSlot(slot.getName()).add(object);            
            }
          }
        }        
      }
    
    return this;  
    }
  
  public Object copy()
    {
    RPObject object=new RPObject();
    
    object.copy((Attributes)this);
    
    try
      {
      Iterator it=slots.iterator();
      while(it.hasNext())
        {
        RPSlot slot=(RPSlot)it.next();
        addSlot((RPSlot)slot.copy());
        }
      }
    catch(SlotAlreadyAddedException e)
      {
      //Should never happen
      }
    
    return object;
    }
    
  public boolean equals(Object obj) 
    {
    RPObject object=(RPObject)obj;
    
    if(super.equals(obj) && slots.equals(object.slots))
      {
      return true;
      }
    else
      {
      return false;
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
			id=attr.getInt("object_id");
		}
		
		/** Constructor
		 *  @param attr an RPAction containing source_id attribute */
		public ID(RPAction attr) throws Attributes.AttributeNotFoundException
		{
			id=attr.getInt("source_id");
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
		
	public void toXML(Element xml_rp_object)
	{
		if(xml_rp_object!=null)
		{
			Document xml_doc = xml_rp_object.getOwnerDocument();
			//first attributes ...
			super.toXML(xml_rp_object);
			//then slots ...
			SlotsIterator  it = slotsIterator();
			while(it.hasNext())
			{
				RPSlot slot = it.next();
				Element elem_rpslot = xml_doc.createElement("rp_slot");
				slot.toXML(elem_rpslot);
				xml_rp_object.appendChild(elem_rpslot);
			}
		}
	}
	
	public void fromXML(Element xml_rp_object)
	{
		if(xml_rp_object!=null)
		{
			super.fromXML(xml_rp_object);
			NodeList nl = xml_rp_object.getElementsByTagName("rp_slot");
			int count = nl.getLength();
			for (int i = 0; i < count; i++)
			{
				Element attr_elem = (Element)nl.item(i);
				RPSlot rp_slot = new RPSlot();
				rp_slot.fromXML(attr_elem);
				try
				{
					addSlot(rp_slot);
				}
				catch (RPObject.SlotAlreadyAddedException e)
				{
					marauroad.trace("RPObject::fromXML","X",e.getMessage());
				}
			}
		}
	}
	
	
}
