package marauroa.game;

import java.util.*;

/** This class host a list of Attributes stored as a pair String=String */
public class Attributes implements marauroa.net.Serializable
  {
  /* A Map<String,String> that contains the attributes */
  private Map content;
  
  public static class AttributeNotFoundException extends Exception
    {
    public AttributeNotFoundException()
      {
      super("Attribute not found");
      }
    }
    
  /** Constructor */
  public Attributes()
    {
    content=new HashMap();
    }

  /** This method returns true if the attribute exists 
   *  @param attribute the attribute name to check
   *  @return true if it exist or false otherwise */
  public boolean has(String attribute)
    {
    return content.containsKey(attribute);
    }

  /** This method set the value of an attribute 
   *  @param attribute the attribute to be set.
   *  @param value the value we want to set. */    
  public void put(String attribute, String value)
    {
    content.put(attribute,value);
    }

  /** This methods return the value of an attribute 
   *  @param attribute the attribute we want to get
   *  @return the value of the attribute
   *  @throw AttributesNotFoundException if the attributes doesn't exist. */
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
    
  /** This methods remove the attribute from the container
   *  @param attribute the attribute we want to remove
   *  @throw AttributesNotFoundException if the attributes doesn't exist. */
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

  /** This method returns true of both object are equal.
   *  @param attr another Attributes object
   *  @return true if they are equal, or false otherwise. */
  public boolean equals(Object attr)
    {
    return content.equals(((Attributes)attr).content);
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
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
