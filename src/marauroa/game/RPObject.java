package marauroa.game;

import java.util.*;

public class RPObject extends Attributes
{
  private RPSlot[] slots;
  
  public static class NoSlotFoundException extends Throwable
  {
    public NoSlotFoundException()
    {
      super("Slot not found");
    }
  }
  
  public class SlotsIterator implements Iterator
  {
    int i;
    
    public SlotsIterator()
    {
      i=0;
    }
    
    public boolean hasNext()
    {
      return (i<slots.length);
    }
    
    public Object next()
    {
      Object obj=slots[i];
      i++;
      return obj;
    }
    
    public void remove()
    {
    }
  }
  
  public static class ID implements marauroa.net.Serializable
  {
    private int id;
    
    public ID(int oid)
    {
      id=oid;
    }
    
    public ID(Attributes attr) throws Attributes.AttributeNotFoundException
    {
      id=new Integer(attr.get("object_id")).intValue();
    }
    
    public int getObjectID()
    {
      return id;
    }
    
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
    
    public int hashCode()
    {
      return id;
    }
    
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
  
  public RPObject()
  {
    super();
    /* TODO: Not sure it is the best thing... */
    slots=new RPSlot[0];
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
    /* TODO: Print slots too. */
    
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
