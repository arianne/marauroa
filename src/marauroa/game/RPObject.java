package marauroa.game;

import java.util.*;

/** This class implements an Object. Please refer to Objects Explained document */
public class RPObject extends Attributes
  {
  private RPSlot[] slots;
  
  public static class NoSlotFoundException extends Exception
    {
    public NoSlotFoundException()
      {
      super("Slot not found");
      }
    }
  
  /** An iterator for properly acceding all the Slots. */
  public class SlotsIterator implements Iterator
    {
    int i;
    
    /** Constructor */
    public SlotsIterator()
      {
      i=0;
      }
    
    /** This method returns true if there are still most elements.
     *  @return true if there are more elements. */    
    public boolean hasNext()
      {
      return (i<slots.length);
      }
    
    /** This method returs the RPSlot and move the pointer to the next element
     *  @return an RPSlot */ 
    public Object next() throws NoSuchElementException
      {
      if(i==slots.length) 
        {
        throw new NoSuchElementException("Acceding slots beyond limit");
        }
        
      RPSlot obj=slots[i];
      ++i;
      return obj;
      }
    
    public void remove()
      {
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
     *  @param attr an Attributes object( RPObject or RPAction ) containing object_id attribute */
    public ID(Attributes attr) throws Attributes.AttributeNotFoundException
      {
      id=new Integer(attr.get("object_id")).intValue();
      }
    
    /** Constructor 
     *  @param attr an Attributes object( RPObject or RPAction ) containing source_id attribute */
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
    /* TODO: Not sure it is the best thing... */
    slots=new RPSlot[0];
    }

  public RPObject(ID id)
    {
    super();
    /* TODO: Not sure it is the best thing... */
    slots=new RPSlot[0];
    
    put("object_id",id.getObjectID());
    }
  
  public boolean hasSlot(String name)
    {
    for(int i=0;i!=slots.length;++i)
      {
      if(name.equals(slots[i].getName()))
        {
        return true;
        }
      }
    
    return false;
    }
  
  public void addSlots(RPSlot[] slots)
    {
    this.slots=slots;
    }
  
  public RPSlot getSlot(String name) throws NoSlotFoundException
    {
    for(int i=0;i!=slots.length;++i)
      {
      if(name.equals(slots[i].getName()))
        {
        return slots[i];
        }
      }
    
    throw new NoSlotFoundException();
    }
  
  public Iterator slotsIterator()
    {
    return new SlotsIterator();
    }
  
  public String toString()
    {
    StringBuffer tmp=new StringBuffer("RPObject with ");
    tmp.append(super.toString());
    tmp.append(" and RPSlots ");
    
    Iterator it=slotsIterator();
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
    
    out.write((int)slots.length);
    for(int i=0;i<slots.length;++i)
      {
      out.write(slots[i]);
      }
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    int size=in.readInt();
    slots=new RPSlot[size];
    
    for(int i=0;i<size;++i)
      {
      slots[i]=(RPSlot)in.readObject(new RPSlot());
      }
    }
  }
