package marauroa.common.game;

import java.util.*;
import marauroa.common.*;

/** The RPClass class implements a container of attributes with its code, name,
 *  type and visibility. It adds support for strict type definition and class 
 *  hierarchy */  
public class RPClass implements marauroa.common.net.Serializable
  {
  /* Visibility */
  /** The attribute is visible */
  final public static byte VISIBLE=1;
  /** The attribute is invisible and so only server related */ 
  final public static byte HIDDEN=2;
  
  /* Type */
  /** a string */
  final public static byte LONG_STRING=1;
  /** a string of up to 255 chars long */
  final public static byte STRING=2;  
  /** an float number of 32 bits */
  final public static byte FLOAT=3;
  /** an integer of 32 bits */
  final public static byte INT=4;
  /** an integer of 16 bits */
  final public static byte SHORT=5;
  /** an integer of 8 bits */
  final public static byte BYTE=6;  
  /** an boolean attribute that either is present or not. */
  final public static byte FLAG=7;

  static public class SyntaxException extends java.io.IOException
    {
    public SyntaxException()
      {
      super();
      }
    }
  static private class AttributeDesc implements marauroa.common.net.Serializable
    {
    private static short lastCode=0;
    private static Map<String,Short> attributeIntegerMap;
    static
      {
      attributeIntegerMap=new HashMap<String,Short>();
      }      
    
    private static short getValidCode(String name)
      {
      if(!attributeIntegerMap.containsKey(name))
        {
        attributeIntegerMap.put(name,new Short(++lastCode));
        }

      return (attributeIntegerMap.get(name)).shortValue();
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

    public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException
      {
      out.write(code);
      out.write(name);
      out.write(type);
      out.write(visibility);
      }

    public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
      {
      code=in.readShort();
      name=in.readString();
      type=in.readByte();
      visibility=in.readByte();
      }
    }
  
  private static Map<String,RPClass> rpClassList;
  
  static
    {
    rpClassList=new LinkedHashMap<String,RPClass>();
    }

  private String name;
  private RPClass parent;  
  private Map<String,AttributeDesc> attributes;  
  
  public RPClass()
    {    
    parent=null;
    attributes=new HashMap<String,AttributeDesc>();
    }
  
  /** This constructor adds the rpclass to the global list of rpclasses. */  
  public RPClass(String type)
    {    
    parent=null;
    name=type;
    attributes=new HashMap<String,AttributeDesc>();

    add("id",INT);
    add("clientid",INT,HIDDEN);
    add("#db_id",INT,HIDDEN);
    add("zoneid",STRING);
    add("type",STRING);    
    
    rpClassList.put(type,this);
    }
  
  /** This method sets the parent of this rpclass */
  public void isA(RPClass parent)
    {
    this.parent=parent;
    }
  
  /** This method sets the parent of this rpclass */
  public void isA(String parent) throws SyntaxException
    {
    this.parent=getRPClass(parent);
    }
  
  /** This method returns true if it is a subclass of parentClass or if it is the
   *  class itself. */
  public boolean subclassOf(String parentClass)
    {
    if(!hasRPClass(parentClass))
      {
      return false;
      }
    
    if(name.equals(parentClass))
      {
      return true;
      }
    else if(parent!=null)
      {
      return parent.subclassOf(parentClass);
      }
    
    return false;     
    }
  
  static RPClass defaultRPClass;
  
  /** Returns a default rpclass for lazy developers. You won't get any advantages
   *  on the engine by using it. */
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

  /** Returns a default rpclass for lazy developers. You won't get any advantages
   *  on the engine by using it. */
  public static RPClass getBaseRPActionDefault()
    {
    if(defaultRPClass==null)
      {
      defaultRPClass=getBaseRPObjectDefault();
      }
    
    return defaultRPClass;
    }
  
  /** Adds a visible attribute to the rpclass */
  public boolean add(String name, byte type)
    {    
    return add(name, type, VISIBLE);
    }
    
  /** Adds a attribute to the rpclass */
  public boolean add(String name, byte type, byte visibility)
    {    
    AttributeDesc desc=new AttributeDesc(name,type,visibility);
    attributes.put(name,desc);
    
    return true;
    }

  /** Returns the name of the rpclass */
  public String getName()
    {
    return name;
    }
    
  /** Returns the code of the attribute whose name is name for this rpclass */
  public short getCode(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=attributes.get(name);
      return desc.code;
      }
    
    if(parent!=null)
      {
      return parent.getCode(name);
      }
    
    throw new SyntaxException();  
    }

  /** Returns the name of the attribute whose code is code for this rpclass */
  public String getName(short code) throws SyntaxException
    {
    for(AttributeDesc desc: attributes.values())
      {
      if(desc.code==code)
        {
        return desc.name;
        }
      }
    
    if(parent!=null)
      {
      return parent.getName(code);
      }

    throw new SyntaxException();  
    }
  
  /** Returns the type of the attribute whose name is name for this rpclass */
  public byte getType(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=attributes.get(name);
      return desc.type;
      }
    
    if(parent!=null)
      {
      return parent.getType(name);
      }

    throw new SyntaxException();  
    }

  /** Returns the visibility of the attribute whose name is name for this rpclass */
  public byte getVisibility(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=attributes.get(name);
      return desc.visibility;
      }
    
    if(parent!=null)
      {
      return parent.getVisibility(name);
      }

    throw new SyntaxException();  
    }

  /** Returns true if the attribute whose name is name exists for this rpclass */
  public boolean hasAttribute(String name)
    {
    if(attributes.containsKey(name))
      {
      return true;
      }
    
    if(parent!=null)
      {
      return parent.hasAttribute(name);
      }

    return false;
    }
  

  /** Returns true if the global list contains the name rpclass */
  public static boolean hasRPClass(String name)
    {    
    if(rpClassList.containsKey(name))
      {
      return true;
      }
    
    return false;
    }
    
  /** Returns the name rpclass from the global list */
  public static RPClass getRPClass(String name) throws SyntaxException
    {
    if(rpClassList.containsKey(name))
      {
      return rpClassList.get(name);
      }
    
    /* TODO: Throw exception */
    throw new SyntaxException();  
    }
  
  public RPObject getInstance(RPObject.ID id)
    {
    RPObject object=new RPObject(id,this);
    
    for(AttributeDesc desc: attributes.values())
      {
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

  public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException
    {
    out.write((int)attributes.size());
    out.write(name);
    
    if(parent==null)
      {
      out.write((byte)0);      
      }
    else
      {
      out.write((byte)1);      
      out.write(parent.name);
      }
    
    for(AttributeDesc desc: attributes.values())
      {
      out.write(desc);      
      }
    }

  public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    int size=in.readInt();
    name=in.readString();
    
    byte parentPresent=in.readByte();    
    if(parentPresent==1)
      {
      isA(in.readString());
      }
    
    for(int i=0;i<size;++i)
      {
      AttributeDesc desc=(AttributeDesc)in.readObject(new AttributeDesc());
      attributes.put(desc.name, desc);
      }
    
    rpClassList.put(name,this);
    }
    
  /** Iterates over the global list of rpclasses */
  public static Iterator<RPClass> iterator()
    {
    return rpClassList.values().iterator();
    }
  
  /** Returns the size of the rpclass global list */
  public static int size()
    {
    return rpClassList.size();
    }
  }
