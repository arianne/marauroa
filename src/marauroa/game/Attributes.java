package marauroa.game;

import java.util.*;

public class Attributes implements marauroa.net.Serializable
  {
  private Map content;
  
  public static class AttributeNotFoundException extends Throwable
    {
    public AttributeNotFoundException()
      {
      super("Attribute not found");
      }
    }
    
  public Attributes()
    {
    content=new HashMap();
    }

  public boolean has(String attribute)
    {
    return content.containsKey(attribute);
    }
    
  public void put(String attribute, String value)
    {
    content.put(attribute,value);
    }

  public String get(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      return (String)content.get(attribute);
      }
    else
      {
      throw new AttributeNotFoundException();
      }
    }
    
  public void remove(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      content.remove(attribute);
      }
    else
      {
      throw new AttributeNotFoundException();
      }
    }

  public boolean equals(Object attr)
    {
    return content.equals(((Attributes)attr).content);
    }

  public String toString()
    {
    StringBuffer tmp=new StringBuffer("Attributes: ");
    
    Iterator  it=content.entrySet().iterator();
    
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      tmp.append("["+(String)entry.getKey());
      tmp.append("="+(String)entry.getValue()+"]");
      }
      
    return tmp.toString();
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    Iterator  it=content.entrySet().iterator();
    out.write((int)content.size());
    
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      out.write((String)entry.getKey());
      out.write((String)entry.getValue());
      }
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    int size=in.readInt();
    content.clear();
        
    for(int i=0;i<size;++i)
      {
      content.put(in.readString(),in.readString());
      }
    }
  }
