package marauroa.game;

import java.util.*;

public class RPObject extends Attributes
  {
  private RPSlot[] slots;
  
  class NoSlotFoundException extends Throwable
    {
    public NoSlotFoundException()
      {
      super("Slot not found");
      }
    }
    
  class SlotsIterator implements Iterator
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
    
  public RPObject()
    {
    super();
    slots=null;
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