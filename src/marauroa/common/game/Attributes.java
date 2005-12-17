/* $Id: Attributes.java,v 1.15 2005/12/17 23:13:27 arianne_rpg Exp $ */
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
package marauroa.common.game;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import org.apache.log4j.Logger;


/** This class hosts a list of Attributes stored as pairs String=String */
public class Attributes implements marauroa.common.net.Serializable, Iterable<String>
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(Attributes.class);
  
  private Map<String,String> added;
  private Map<String,String> deleted;

  /** A Map<String,String> that contains the attributes */
  private Map<String,String> content;
  private RPClass rpClass;

  public Object copy()
    {
    Attributes attr=new Attributes(this.rpClass);

    for(Map.Entry<String,String> entry: content.entrySet())
      {
      attr.put(entry.getKey(),entry.getValue());
      }
    return attr;
    }

  public Object copy(Attributes attr)
    {
    setRPClass(attr.rpClass);

    for(Map.Entry<String,String> entry: attr.content.entrySet())
      {
      put(entry.getKey(),entry.getValue());
      }
    return this;
    }

  /** Constructor */
  public Attributes(RPClass rpclass)
    {
    rpClass=rpclass;

    content=new HashMap<String,String>();
    added=new HashMap<String,String>();
    deleted=new HashMap<String,String>();
    }

  public void setRPClass(RPClass rpclass)
    {
    rpClass=rpclass;
    }

  public RPClass getRPClass()
    {
    return rpClass;
    }

  public boolean instanceOf(RPClass baseclass)
    {
    return rpClass.subclassOf(baseclass.getName());
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
        logger.error("cannot put attribute ["+attribute+"] value: ["+value+"], Syntax error",e);
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
  public void put(String attribute, double value)
    {
    put(attribute,Double.toString(value));
    }

  /** This method set the value of an attribute
   *  @param attribute the attribute to be set.
   *  @param value the value we want to set. */
  public void put(String attribute, List<String> value)
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
      return content.get(attribute);
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
      return Integer.parseInt(content.get(attribute));
      }
    else
      {
      throw new AttributeNotFoundException(attribute);
      }
    }

  public double getDouble(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      return Double.parseDouble(content.get(attribute));
      }
    else
      {
      throw new AttributeNotFoundException(attribute);
      }
    }

  public List<String> getList(String attribute) throws AttributeNotFoundException
    {
    if(content.containsKey(attribute))
      {
      return StringToList(content.get(attribute));
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
        /* This is for Delta^2 feature, as if it is empty it fails. */
        deleted.put(attribute,"0");
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

    for(Map.Entry<String,String> entry: content.entrySet())
      {
      tmp.append("["+entry.getKey());
      tmp.append("="+entry.getValue()+"]");
      }

    return tmp.toString();
    }

  private static String ListToString(List<String> list)
    {
    StringBuffer buffer=new StringBuffer("[");

    for(Iterator it=list.iterator(); it.hasNext();)
      {
      String value=(String)it.next();

      buffer.append(value);
      if(it.hasNext())
        {
        buffer.append("\t");
        }
      }

    buffer.append("]");
    return buffer.toString();
    }

  private static List<String> StringToList(String list)
    {
    String[] array=list.substring(1,list.length()-1).split("\t");
    List<String> result=new LinkedList<String>();

    for(int i=0;i<array.length;++i)
      {
      result.add(array[i]);
      }

    return result;
    }

  /** returns an iterator over the attribute names */
  public Iterator<String> iterator()
    {
    return content.keySet().iterator();
    }

  public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException
    {
    writeObject(out,false);
    }

  public void writeObject(marauroa.common.net.OutputSerializer out,boolean fulldata) throws java.io.IOException
    {
    int size=content.size();

    for(String key: content.keySet())
      {
      if(fulldata==false && (rpClass.isVisible(key)==false)) 
        {
        //If this attribute is Hidden or private and full data is false
        --size;
        }
      else if(fulldata==true && rpClass.isHidden(key)) 
        {
        //If this attribute is Hidden and full data is true.
        //This way we hide some attribute to player.
        --size;
        }
      }

    out.write(rpClass.getName());
    out.write(size);
    for(Map.Entry<String,String> entry: content.entrySet())
      {
      String key=entry.getKey();

      if((fulldata==true && !rpClass.isHidden(key)) || (rpClass.isVisible(key)))
        {
        short code=-1;

        try
          {
          code=rpClass.getCode(key);
          }
        catch(RPClass.SyntaxException e)
          {
          logger.error("cannot writeObject, Attribute ["+key+"] not found",e);
          code=-1;
          }

        out.write(code);

        if(code==-1)
          {
          out.write(key);
          }

        try
          {
          switch (rpClass.getType(key))
            {
            case RPClass.VERY_LONG_STRING:
              out.write(entry.getValue());
              break;
            case RPClass.LONG_STRING:
              out.write65536LongString(entry.getValue());
              break;
            case RPClass.STRING:
              out.write255LongString(entry.getValue());
              break;
            case RPClass.FLOAT:
              out.write(Float.parseFloat(entry.getValue()));
              break;
            case RPClass.INT:
              out.write(Integer.parseInt(entry.getValue()));
              break;
            case RPClass.SHORT:
              out.write(Short.parseShort(entry.getValue()));
              break;
            case RPClass.BYTE:
              out.write(Byte.parseByte(entry.getValue()));
              break;
            case RPClass.FLAG:
              /* It is empty because it is a flag and so, it is already present. */
              break;
            default:
            /* NOTE: Must never happen */
            logger.fatal("got unknown attribute type "+rpClass.getType(key));
            break;
            }
          }
        catch(Exception e)
          {
          String className=(rpClass!=null?rpClass.getName():null);
          logger.error("Attribute "+key+" ["+className+"] caused an exception",e);
          throw new java.io.IOException(e.getMessage());
          }
        }
      }
    }

  public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
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

      if(rpClass.getType(key)==RPClass.VERY_LONG_STRING)
        {
        content.put(key,in.readString());
        }
      else if(rpClass.getType(key)==RPClass.LONG_STRING)
        {
        content.put(key,in.read65536LongString());
        }
      else if(rpClass.getType(key)==RPClass.STRING)
        {
        content.put(key,in.read255LongString());
        }
      else if(rpClass.getType(key)==RPClass.FLOAT)
        {
        content.put(key,Float.toString(in.readFloat()));
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

  public void clearVisible()
    {
    Iterator<Map.Entry<String,String>> it=content.entrySet().iterator();
    while(it.hasNext())
      {
      Map.Entry<String,String> entry=it.next();

      if(rpClass.isVisible(entry.getKey()) && !entry.getKey().equals("id"))
        {
        it.remove();
        }
      }
    }

  public void resetAddedAndDeletedAttributes()
    {
    added.clear();
    deleted.clear();
    }

  public void setAddedAttributes(Attributes attr) throws AttributeNotFoundException, RPClass.SyntaxException
    {
    rpClass=attr.rpClass;
    int i=0;
    for(Map.Entry<String,String> entry: attr.added.entrySet())
      {
      ++i;
      put(entry.getKey(),entry.getValue());
      }

    if(i>0)
      {
      put("id",attr.get("id"));
      put("zoneid",attr.get("zoneid"));
      }

    attr.added.clear();
    }

  public void setDeletedAttributes(Attributes attr) throws AttributeNotFoundException, RPClass.SyntaxException
    {
    rpClass=attr.rpClass;

    int i=0;
    for(Map.Entry<String,String> entry: attr.deleted.entrySet())
      {
      ++i;
      put(entry.getKey(),entry.getValue());
      }

    if(i>0)
      {
      put("id",attr.get("id"));
      put("zoneid",attr.get("zoneid"));
      }

    attr.deleted.clear();
    }
  }
