/* $Id: RPObject.java,v 1.43 2004/05/19 22:01:28 arianne_rpg Exp $ */
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

/** This class implements an Object. Please refer to "Objects Explained" document */
public class RPObject extends Attributes
  {
  private List added;
  private List deleted;
  
  public void resetAddedAndDeleted()
    {
    resetAddedAndDeletedAttributes();
    
    resetAddedAndDeletedRPSlot();
    }

  public void resetAddedAndDeletedRPSlot()
    {
    Iterator it=slots.iterator();
    while(it.hasNext())
      {
      RPSlot slot=(RPSlot)it.next();
      
      slot.resetAddedAndDeletedRPObjects();
      Iterator objects=slot.iterator();
      while(objects.hasNext())
        {
        RPObject object=(RPObject)objects.next();
        object.resetAddedAndDeleted();
        }
      }

    added.clear();
    deleted.clear();
    }

  public void setAddedRPSlot(RPObject object)
    {
    Iterator it=object.added.iterator();
    
    while(it.hasNext())
      {
      RPSlot slot=(RPSlot)it.next();
      try
        {
        addSlot((RPSlot)slot.copy());
        }
      catch(Exception e)
        {
        marauroad.thrown("RPObject::setAddedRPSlot","X",e);
        }
      }       
    
    object.added.clear();
    }

  public void setDeletedRPSlot(RPObject object)
    {
    Iterator it=object.deleted.iterator();
    
    while(it.hasNext())
      {
      RPSlot slot=new RPSlot(((RPSlot)it.next()).getName());
      try
        {
        addSlot(new RPSlot(slot.getName()));
        }
      catch(Exception e)
        {
        marauroad.thrown("RPObject::setAddedRPSlot","X",e);
        }
      }       
    
    object.deleted.clear();
    }
  
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
    added=new LinkedList();
    deleted=new LinkedList();
    }
	
  /** Constructor
   *  @param id the id of the object */
  public RPObject(ID id)
    {
    super();
    slots=new LinkedList();
    added=new LinkedList();
    deleted=new LinkedList();
    put("id",id.getObjectID());
    }
  
  public RPObject.ID getID() throws Attributes.AttributeNotFoundException
    {
    return new ID(this);
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
      added.add(slot);
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
          deleted.add(slot);
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
  public void getDifferences(RPObject oadded, RPObject odeleted) throws Exception 
    {
    oadded.setAddedAttributes(this);
    odeleted.setDeletedAttributes(this);
    
    odeleted.setDeletedRPSlot(this);
    
    Iterator it=slots.iterator();
    while(it.hasNext())
      {
      RPSlot slot=(RPSlot)it.next();
      
      RPSlot added_slot=new RPSlot(slot.getName());            
      added_slot.setAddedRPObject(slot);
      
      if(added_slot.size()>0 && !oadded.hasSlot(added_slot.getName()))
        {
        oadded.addSlot(added_slot);
        }

      RPSlot deleted_slot=new RPSlot(slot.getName());            
      deleted_slot.setDeletedRPObject(slot);
      
      if(deleted_slot.size()>0 && !odeleted.hasSlot(deleted_slot.getName()))
        {
        odeleted.addSlot(deleted_slot);
        }
     
      Iterator objects=slot.iterator();
      while(objects.hasNext())
        {
        RPObject object=(RPObject)objects.next();
        
        RPObject object_added=new RPObject();
        RPObject object_deleted=new RPObject();
         
        object.getDifferences(object_added,object_deleted);        

        if(object_added.size()>0)
          {
          if(!oadded.hasSlot(slot.getName())) 
            {
            oadded.addSlot(new RPSlot(slot.getName()));
            }
            
          if(!oadded.getSlot(slot.getName()).has(new ID(object)))
            {
            object_added.put("id",object.get("id"));
            oadded.getSlot(slot.getName()).add(object_added);
            }            
          }
        
        if(object_deleted.size()>0)
          {
          if(!odeleted.hasSlot(slot.getName())) 
            {
            odeleted.addSlot(new RPSlot(slot.getName()));
            }

          if(!odeleted.getSlot(slot.getName()).has(new ID(object)))
            {
            object_deleted.put("id",object.get("id"));
            odeleted.getSlot(slot.getName()).add(object_deleted);
            }
          }
        }
      }

    if(oadded.size()>0) 
      {
      oadded.put("id",get("id"));
      }      
    if(odeleted.size()>0 || odeleted.slots.size()>0) 
      {
      odeleted.put("id",get("id"));
      }
    }
  
  public int size()
    {
    try
      {
      int total=super.size();
    
      Iterator it=slots.iterator();
      while(it.hasNext())
        {
        RPSlot slot=(RPSlot)it.next();
        
        Iterator objects=slot.iterator();
        while(objects.hasNext())
          {
          RPObject object=(RPObject)objects.next();
          total+=object.size();
          }
        }
      
      return total;
      }
    catch(Exception e) 
      {
      return -1;
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

        if(!attrib.equals("id"))
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

            if(object.size()==1)
              {
              getSlot(slot.getName()).remove(new ID(object));
              }
            else
              {
              RPObject actualObject=getSlot(slot.getName()).get(new ID(object));

              actualObject.applyDifferences(null,object);
              }
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
        put(attrib,added.get(attrib));
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
        object.addSlot((RPSlot)slot.copy());
        }
      }
    catch(SlotAlreadyAddedException e)
      {
      // Should never happen
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

  public int hashCode()
    {
    try
      {
      return getInt("id");
      }
    catch(AttributeNotFoundException e)
      {
      return -1;
      }
    }
  
  public void removeAllButHidden()
    {
    try
      {
      Iterator it=iterator();
     
      while(it.hasNext())
        {
        String attrib=(String)it.next();
        if(!attrib.startsWith("!") && !attrib.equals("id"))
          {
          remove(attrib);
          it=iterator();
          }
        }    
     
      SlotsIterator sit=this.slotsIterator();
      while(sit.hasNext())
        {
        RPSlot slot=sit.next();
        if(!slot.getName().startsWith("!"))
          {
          removeSlot(slot.getName());
          sit=this.slotsIterator();
          }
        }
      }
    catch(Exception e)
      {
      /** NOTE: Shouldn't happen */
      }
    }  

  public void removeAllHidden()
    {
    try
      {
      Iterator it=iterator();
     
      while(it.hasNext())
        {
        String attrib=(String)it.next();
        if(attrib.startsWith("!"))
          {
          remove(attrib);
          it=iterator();
          }
        }    
     
      SlotsIterator sit=this.slotsIterator();
      while(sit.hasNext())
        {
        RPSlot slot=sit.next();
        if(slot.getName().startsWith("!"))
          {
          removeSlot(slot.getName());
          sit=this.slotsIterator();
          }
        }
      }
    catch(Exception e)
      {
      /** NOTE: Shouldn't happen */
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
      id=attr.getInt("id");
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

      // first attributes ...
      super.toXML(xml_rp_object);

      // then slots ...
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
          marauroad.thrown("RPObject::fromXML","X",e);
          }
        }
      }
    }
  }
