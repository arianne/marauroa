/* $Id: Attributes.java,v 1.23 2004/03/22 18:31:48 arianne_rpg Exp $ */
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import marauroa.TimeoutConf;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** This class host a list of Attributes stored as a pair String=String */
public class Attributes implements marauroa.net.Serializable
  {
  /** A Map<String,String> that contains the attributes */
  private Map content;
  
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
    Attributes attr=new Attributes();
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
    Iterator it=attr.content.entrySet().iterator();
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      put((String)entry.getKey(),(String)entry.getValue());
      }    
    
    return this;
    }
    
  /** Constructor */
  public Attributes()
	{
	content=new HashMap();
	}
  
  public boolean isEmpty()
    {
    return content.isEmpty();
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
	
  /** This method set the value of an attribute
	*  @param attribute the attribute to be set.
	*  @param value the value we want to set. */
  public void put(String attribute, int value)
	{
	content.put(attribute,Integer.toString(value));
	}
	
  /** This method set the value of an attribute
	*  @param attribute the attribute to be set.
	*  @param value the value we want to set. */
  public void put(String attribute, List value)
	{
	content.put(attribute,Attributes.ListToString(value));
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
	*  @throw AttributesNotFoundException if the attributes doesn't exist. */
  public void remove(String attribute) throws AttributeNotFoundException
	{
	if(content.containsKey(attribute))
	  {
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
			if(fulldata==false && key.charAt(0)=='!')
			{
				--size;
			}
		}
		
		out.write(size);
		it=content.entrySet().iterator();
		
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			
			/** NOTE: The attributes that begin with ! are not stored */
			String key=(String)entry.getKey();
			if(fulldata==true || key.charAt(0)!='!')
			{
				out.write(key);
				out.write((String)entry.getValue());
			}
		}
	}
	
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
	{
		int size=in.readInt();
		
		if(size>TimeoutConf.MAX_ARRAY_ELEMENTS)
		{
			throw new IOException("Illegal request of an list of "+String.valueOf(size)+" size");
		}
		
		content.clear();
		
		for(int i=0;i<size;++i)
		{
			String key=in.readString();
			String value=in.readString();
			content.put(key,value);
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
	
}
