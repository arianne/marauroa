package marauroa.game;

import java.util.*;

/** This class implements an Object. Please refer to Objects Explained document */
public class RPObject extends Attributes
  {
  /** a List<RPSlot> of slots */
  private List slots;
  
  public static class NoSlotFoundException extends Exception
    {
    public NoSlotFoundException()
      {
      super("Slot not found");
      }
    }

  public static class SlotAlreadyAddedException extends Exception
    {
    public SlotAlreadyAddedException()
      {
      super("Slot already added.");
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
    
  /** Constructor */
  public RPObject()
    {
    super();

    slots=new LinkedList();
    }

  public RPObject(ID id)
    {
    super();

    slots=new LinkedList();    
    put("object_id",id.getObjectID());
    }
  
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
  
  public void addSlot(RPSlot slot) throws SlotAlreadyAddedException
    {
    if(!hasSlot(slot.getName()))
      {
      slots.add(slot);
      }
    else
      {
      throw new SlotAlreadyAddedException();
      }
    }
  
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
   
    throw new NoSlotFoundException();
    }
  
  public SlotsIterator slotsIterator()
    {
    return new SlotsIterator();
    }
  
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
    slots=new LinkedList();
    
    for(int i=0;i<size;++i)
      {
      slots.add((RPSlot)in.readObject(new RPSlot()));
      }
    }
  }
