/* $Id: Attributes.java,v 1.36 2004/06/22 11:47:42 arianne_rpg Exp $ */
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
import java.util.*;
import marauroa.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** This class hosts a list of Attributes stored as pairs String=String */
public class Attributes implements marauroa.net.Serializable
  {
  private Map added;
  private Map deleted;
  
  /** A Map<String,String> that contains the attributes */
  private Map content;
  private RPClass rpClass;

  public static class AttributeNotFoundException extends Exception
    {
    private String attribute;
    public AttributeNotFoundException(String attrib)
      {
      super("Attribute ["+attrib+"] not found");
      attribute=attrib;
      }
		
    public String getAttribute()
      {
      return attribute;
      }
    }
    
  public Object copy()
    {
    Attributes attr=new Attributes(this.rpClass);
    Iterator it=content.entrySet().iterator();

    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();

      attr.put((String)entry.getKey(),(String)entry.getValue());
      }    
    return attr;
    }

  public Object copy(Attributes attr)
    {
    setRPClass(attr.rpClass);
    Iterator it=attr.content.entrySet().iterator();

    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();

      put((String)entry.getKey(),(String)entry.getValue());
      }    
    return this;
    }
    
  /** Constructor */
  public Attributes(RPClass rpclass)
    {
    rpClass=rpclass;
    
    content=new HashMap();
    added=new HashMap();
    deleted=new HashMap();
    }
  
  public void setRPClass(RPClass rpclass)
    {
    rpClass=rpclass;
    }
  
  public RPClass getRPClass()
    {
    return rpClass;
    }
  
  public boolean isEmpty()
    {
    return content.isEmpty();
    }
  
  public int size()
    {
    return content.size();
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
    /* This is for Delta-delta feature */
    added.put(attribute,value);

    if(attribute.equals("type") && RPClass.hasRPClass(value))
      {
      try 
        {
        setRPClass(RPClass.getRPClass(value));
        }
      catch(RPClass.SyntaxException e)
        {
        /* NOTE: Can't ever happen */
        marauroad.trace("Attributes::Put","!","Syntax error: "+e.getMessage());
        }
      }
    
    content.put(attribute,value);
    }

  public void add(String attribute, int value) throws AttributeNotFoundException
    {
    put(attribute,getInt(attribute)+value);
    }
    
  /** This method set the value of an attribute
   *  @param attribute the attribute to be set.
   *  @param value the value we want to set. */
  public void put(String attribute, int value)
    {
    put(attribute,Integer.toString(value));
    }
	
  /** This method set the value of an attribute
   *  @param attribute the attribute to be set.
   *  @param value the value we want to set. */
  public void put(String attribute, List value)
    {
    put(attribute,Attributes.ListToString(value));
    }
	
  /** This methods return the value of an attribute
   *  @param attribute the attribute we want to get
   *  @return the value of the attribute
   *  @exception AttributesNotFoundException if the attributes doesn't exist. */
  public String get(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      return (String)content.get(attribute);
      }
    else
      {
      throw new AttributeNotFoundException(attribute);
      }
    }
	
  public int getInt(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      return Integer.parseInt((String)content.get(attribute));
      }
    else
      {
      throw new AttributeNotFoundException(attribute);
      }
    }
	
  public List getList(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      return StringToList((String)content.get(attribute));
      }
    else
      {
      throw new AttributeNotFoundException(attribute);
      }
    }
	
  /** This methods remove the attribute from the container
   *  @param attribute the attribute we want to remove
   *  @exception AttributesNotFoundException if the attributes doesn't exist. */
  public void remove(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      if(added.containsKey(attribute))
        {
        added.remove(attribute);
        }
      else
        {
        /* This is for Delta-delta feature */
        deleted.put(attribute,"");
        }
      
      content.remove(attribute);
      }
    else
      {
      throw new AttributeNotFoundException(attribute);
      }
    }
	
  /** This method returns true of both object are equal.
   *  @param attr another Attributes object
   *  @return true if they are equal, or false otherwise. */
  public boolean equals(Object attr)
    {
    return content.equals(((Attributes)attr).content);
    }  
    
  public int hashCode()
    {
    return content.hashCode();
    }
	
  /** This method returns a String that represent the object
   *  @return a string representing the object.*/
  public String toString()
    {
    StringBuffer tmp=new StringBuffer("Attributes of Class("+rpClass.getName()+"): ");
    Iterator  it=content.entrySet().iterator();
		
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();

      tmp.append("["+(String)entry.getKey());
      tmp.append("="+(String)entry.getValue()+"]");
      }
      
    return tmp.toString();
    }
	
  private static String ListToString(List list)
    {
    Iterator it=list.iterator();
    StringBuffer buffer=new StringBuffer("[");

    while(it.hasNext())
      {
      buffer.append((String)it.next());
      if(it.hasNext())
        {
        buffer.append(":");
        }
      }
    buffer.append("]");
    return buffer.toString();
    }
	
  public static List StringToList(String list)
    {
    String[] array=list.substring(1,list.length()-1).split(":");
    List result=new LinkedList();

    for(int i=0;i<array.length;++i)
      {
      result.add(array[i]);
      }
    return result;
    }
	
  public Iterator iterator()
    {
    return content.keySet().iterator();
    }
  
  public boolean instanceOf(RPClass baseclass)
    {
    return rpClass.subclassOf(baseclass.getName());
    }
	
  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    writeObject(out,false);
    }
	
  public void writeObject(marauroa.net.OutputSerializer out,boolean fulldata) throws java.io.IOException
    {
    Iterator it=content.entrySet().iterator();
    int size=content.size();
		
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      String key=(String)entry.getKey();

      if(fulldata==false && (rpClass.getVisibility(key)==RPClass.HIDDEN))
        {
        --size;
        }
      }
    
    out.write(rpClass.getName());    
    out.write(size);
    it=content.entrySet().iterator();
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      /** NOTE: The attributes that begin with ! are not stored */
      String key=(String)entry.getKey();

      if(fulldata==true || (rpClass.getVisibility(key)==RPClass.VISIBLE))
        {        
        short code=-1;
        
        try
          {
          code=rpClass.getCode(key);
          }
        catch(RPClass.SyntaxException e)
          {
          System.err.println("SSAQSDASDASDASDASDAEERWQTRW");
          code=-1;        
          }
        
        out.write(code);
        
        if(code==-1)
          {
          out.write(key);
          }

        if(rpClass.getType(key)==RPClass.STRING)
          {
          out.write((String)entry.getValue());
          }
        else if(rpClass.getType(key)==RPClass.SHORT_STRING)
          {
          out.write255LongString((String)entry.getValue());
          }
        else if(rpClass.getType(key)==RPClass.INT)
          {
          out.write(Integer.parseInt((String)entry.getValue()));
          }
        else if(rpClass.getType(key)==RPClass.SHORT)
          {
          out.write(Short.parseShort((String)entry.getValue()));
          }
        else if(rpClass.getType(key)==RPClass.BYTE)
          {
          out.write(Byte.parseByte((String)entry.getValue()));
          }
        else if(rpClass.getType(key)==RPClass.FLAG)
          {
          }
        else
          {
          /* NOTE: Must never happen */
          }
        }
      }
    }
	
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    rpClass=RPClass.getRPClass(in.readString());
    int size=in.readInt();
		
    if(size>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(size)+" size");
      }
      
    content.clear();
    
    for(int i=0;i<size;++i)
      {
      short code=in.readShort();
      String key;
      if(code==-1)
        {
        key=in.readString();
        }
      else
        {
        key=rpClass.getName(code);      
        }
        
      if(rpClass.getType(key)==RPClass.STRING)
        {
        content.put(key,in.readString());
        }
      else if(rpClass.getType(key)==RPClass.SHORT_STRING)
        {
        content.put(key,in.read255LongString());
        }
      else if(rpClass.getType(key)==RPClass.INT)
        {
        content.put(key,Integer.toString(in.readInt()));
        }
      else if(rpClass.getType(key)==RPClass.SHORT)
        {
        content.put(key,Integer.toString(in.readShort()));
        }
      else if(rpClass.getType(key)==RPClass.BYTE)
        {
        content.put(key,Integer.toString(in.readByte()));
        }
      else if(rpClass.getType(key)==RPClass.FLAG)
        {
        content.put(key,"");
        }
      }
    }
	
  public void toXML(Element attributes)
    {
    if(attributes!=null)
      {
      Document xml_doc = attributes.getOwnerDocument();
      Iterator it = content.entrySet().iterator();

      while(it.hasNext())
        {
        Map.Entry entry   = (Map.Entry)it.next();
        String key        = (String)entry.getKey();
        String value      = (String)entry.getValue();
        Element attribute = xml_doc.createElement("attribute");

        attribute.setAttribute("key",key);
        attribute.setAttribute("value",value);
        attributes.appendChild(attribute);
        }
      }
    }
	
  public void fromXML(Element attributes)
    {
    if(attributes!=null)
      {
      NodeList nl = attributes.getElementsByTagName("attribute");
      int count = nl.getLength();

      for (int i = 0; i < count; i++)
        {
        Element attr_elem = (Element)nl.item(i);

        put(attr_elem.getAttribute("key"),attr_elem.getAttribute("value"));
        }
      }
    }
  
  public void resetAddedAndDeletedAttributes()
    {
    added.clear();
    deleted.clear();
    }

  public void setAddedAttributes(Attributes attr) throws Attributes.AttributeNotFoundException
    {
    rpClass=attr.rpClass;
    Iterator it=attr.added.entrySet().iterator();
    
    int i=0;
    while(it.hasNext())
      {
      ++i;
      Map.Entry entry=(Map.Entry)it.next();
      put((String)entry.getKey(),(String)entry.getValue());
      } 
            
    if(i>0)
      {
      put("id",attr.get("id"));
      }
    
    attr.added.clear();
    }

  public void setDeletedAttributes(Attributes attr) throws Attributes.AttributeNotFoundException
    {
    rpClass=attr.rpClass;
    Iterator it=attr.deleted.entrySet().iterator();
    
    int i=0;
    while(it.hasNext())
      {
      ++i;
      Map.Entry entry=(Map.Entry)it.next();
      put((String)entry.getKey(),(String)entry.getValue());
      }       

    if(i>0)
      {
      put("id",attr.get("id"));
      }

    attr.deleted.clear();
    }
  }
