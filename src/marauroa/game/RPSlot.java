package marauroa.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import marauroa.marauroad;

public class RPSlot implements marauroa.net.Serializable
  {
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
  
  public RPObject get(int index)
    {
    return (RPObject)objects.get(index);
    }
  
  public int size()
    {
    return objects.size();
    }
  
  public Iterator iterator()
    {
    return objects.iterator();
    }
    
  public String toString()
    {
    return "RPSlot named("+name+") with "+size()+" objects";
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    out.write(name);
    
    Iterator  it=objects.iterator();
    out.write((int)objects.size());
    
    while(it.hasNext())
      {
      RPObject entry=(RPObject)it.next();
      out.write(entry);
      }
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
      try
      {
      marauroad.trace("RPSlot.readObject()",">");
      name = in.readString();
        marauroad.trace("RPSlot.readObject()","D"," Slot name: " +name);
    int size=in.readInt();
      marauroad.trace("RPSlot.readObject()","D",size+" objects found");
    objects.clear();
        
    for(int i=0;i<size;++i)
      {
      objects.add(in.readObject(new RPObject()));
      }
    }
    finally
    {
      marauroad.trace("RPSlot.readObject()","<");
    }
    }
  }
