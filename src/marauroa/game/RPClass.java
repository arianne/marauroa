package marauroa.game;

import marauroa.net.*;
import java.util.*;

public class RPClass implements Serializable
  {
  /** Visibility **/
  final public static byte VISIBLE=1;
  final public static byte HIDDEN=2;
  
  /** Type **/
  final public static byte STRING=1;
  final public static byte INT=2;
  final public static byte SHORT=3;
  final public static byte BYTE=4;  
  final public static byte FLAG=5;

  private String name;
  private Map attributes;
  
  static public class SyntaxException extends java.io.IOException
    {
    public SyntaxException()
      {
      super();
      }
    }
  
  static private class AttributeDesc implements Serializable
    {
    private static short lastCode=0;
    private static Map attributeIntegerMap;
    static
      {
      attributeIntegerMap=new HashMap();
      }      
    
    private static short getValidCode(String name)
      {
      if(!attributeIntegerMap.containsKey(name))
        {
        attributeIntegerMap.put(name,new Short(++lastCode));
        }

      return ((Short)attributeIntegerMap.get(name)).shortValue();
      }
      
    public AttributeDesc()
      {
      }
      
    public AttributeDesc(String name, byte type, byte visibility)
      {
      code=getValidCode(name);
      this.name=name;
      this.type=type;
      this.visibility=visibility;
      }

    public short code;
    public String name;
    public byte type;
    public byte visibility;   

    public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
      {
      out.write(code);
      out.write(name);
      out.write(type);
      out.write(visibility);
      }

    public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
      {
      code=in.readShort();
      name=in.readString();
      type=in.readByte();
      visibility=in.readByte();
      }
    }
  
  private static Map rpClassList;
  
  static
    {
    rpClassList=new HashMap();
    }
  
  public RPClass()
    {    
    attributes=new HashMap();
    }
    
  public RPClass(String type)
    {    
    name=type;
    attributes=new HashMap();

    add("id",INT);
    add("type",STRING);    
    
    rpClassList.put(type,this);
    }
  
  static RPClass defaultRPClass;
  
  public static RPClass getBaseRPObjectDefault()
    {
    if(defaultRPClass==null)
      {
      defaultRPClass=new RPClass("")
        {
        public short getCode(String name)
          {
          return -1;
          }
          
        public byte getType(String name)
          {
          return RPClass.STRING;
          }
          
        public byte getVisibility(String name)
          {
          if(name.charAt(0)=='!')
            {
            return RPClass.HIDDEN;
            }
          else
            {
            return RPClass.VISIBLE;
            }
          }
        };
      }
    
    return defaultRPClass;
    }

  public static RPClass getBaseRPActionDefault()
    {
    if(defaultRPClass==null)
      {
      defaultRPClass=getBaseRPObjectDefault();
      }
    
    return defaultRPClass;
    }
  
  
  public boolean add(String name, byte type)
    {    
    return add(name, type, VISIBLE);
    }
    
  public boolean add(String name, byte type, byte visibility)
    {    
    AttributeDesc desc=new AttributeDesc(name,type,visibility);
    attributes.put(name,desc);
    
    return true;
    }

  public String getName()
    {
    return name;
    }
    
  public short getCode(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=(AttributeDesc)attributes.get(name);
      return desc.code;
      }
    
    /* TODO: Throw exception */
    throw new SyntaxException();  
    }

  public String getName(short code) throws SyntaxException
    {
    Iterator it=attributes.values().iterator();
    while(it.hasNext())
      {
      AttributeDesc desc=(AttributeDesc)it.next();
      if(desc.code==code)
        {
        return desc.name;
        }
      }
    
    /* TODO: Throw exception */
    throw new SyntaxException();  
    }
  
  public byte getType(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=(AttributeDesc)attributes.get(name);
      return desc.type;
      }
    
    /* TODO: Throw exception */
    throw new SyntaxException();  
    }

  public byte getVisibility(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=(AttributeDesc)attributes.get(name);
      return desc.visibility;
      }
    
    /* TODO: Throw exception */
    throw new SyntaxException();  
    }

  public boolean hasAttribute(String name)
    {
    if(attributes.containsKey(name))
      {
      return true;
      }
    
    return false;
    }
  
  public static boolean hasRPClass(String name)
    {    
    if(rpClassList.containsKey(name))
      {
      return true;
      }
    
    return false;
    }
    
  public static RPClass getRPClass(String name) throws SyntaxException
    {
    if(rpClassList.containsKey(name))
      {
      return (RPClass)rpClassList.get(name);
      }
    
    /* TODO: Throw exception */
    throw new SyntaxException();  
    }
  
  public RPObject getInstance(RPObject.ID id)
    {
    RPObject object=new RPObject(id,this);
    
    Iterator it=attributes.entrySet().iterator();
    while(it.hasNext())
      {
      Map.Entry entry=(Map.Entry)it.next();
      AttributeDesc desc=(AttributeDesc)entry.getValue();
      
      if(!desc.name.equals("id"))      
        {
        if(desc.type==STRING)
          {
          object.put(desc.name,"");
          }
        else
          {
          object.put(desc.name,0);
          }
        }
      }
    
    return object;
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    out.write((int)attributes.size());
    out.write(name);
    
    Iterator it=attributes.values().iterator();
    while(it.hasNext())
      {
      AttributeDesc desc=(AttributeDesc)it.next();
      out.write(desc);      
      }
    }

  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    int size=in.readInt();
    name=in.readString();
    
    for(int i=0;i<size;++i)
      {
      AttributeDesc desc=(AttributeDesc)in.readObject(new AttributeDesc());
      attributes.put(desc.name, desc);
      }
    
    rpClassList.put(name,this);
    }
    
  public static Iterator iterator()
    {
    return rpClassList.values().iterator();
    }
  
  public static int size()
    {
    return rpClassList.size();
    }
  }
