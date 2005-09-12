package marauroa.common.game;

import java.util.*;
import marauroa.common.Log4J;
import org.apache.log4j.Logger;


/** The RPClass class implements a container of attributes with its code, name,
 *  type and visibility. It adds support for strict type definition and class
 *  hierarchy */
public class RPClass implements marauroa.common.net.Serializable
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(RPClass.class);
  
  /* Visibility */
  /** The attribute is visible */
  final public static byte VISIBLE = 0;
  /** The attribute is invisible and so only server related */
  final public static byte HIDDEN = 1 << 0;
  /** The attribute should be stored in the database */
  final public static byte STORABLE = 0;
  /** The attribute should not be stored in the database */
  final public static byte VOLATILE = 1 << 1;

  /* Type */
  /** a string */
  final public static byte VERY_LONG_STRING=1;
  /** a string of up to 255 chars long */
  final public static byte LONG_STRING=2;
  /** a string of up to 255 chars long */
  final public static byte STRING=3;
  /** an float number of 32 bits */
  final public static byte FLOAT=4;
  /** an integer of 32 bits */
  final public static byte INT=5;
  /** an integer of 16 bits */
  final public static byte SHORT=6;
  /** an integer of 8 bits */
  final public static byte BYTE=7;
  /** an boolean attribute that either is present or not. */
  final public static byte FLAG=8;

  static public class SyntaxException extends RuntimeException
    {
    public SyntaxException(String offendingAttribute)
      {
      super("attribute "+offendingAttribute+" isn't defined.");
      }

    public SyntaxException(short offendingAttribute)
      {
      super("attribute code "+offendingAttribute+" isn't defined.");
      }
    }

  private static class AttributeDesc implements marauroa.common.net.Serializable
    {
    private static short lastCode=0;
    private static Map<String,Short> attributeIntegerMap=new HashMap<String,Short>();


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

    public AttributeDesc(String name, byte type, byte flags)
      {
      code=getValidCode(name);
      this.name=name;
      this.type=type;
      this.flags=flags;
      }

    public short code;
    public String name;
    public byte type;
    public byte flags;

    public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException
      {
      out.write(code);
      out.write(name);
      out.write(type);
      out.write(flags);
      }

    public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
      {
      code=in.readShort();
      name=in.readString();
      type=in.readByte();
      flags=in.readByte();
      }
    }

  private static Map<String,RPClass> rpClassList=new LinkedHashMap<String,RPClass>();

  private String name;
  private RPClass parent;
  private Map<String,AttributeDesc> attributes;
  
  /** returns all currently known RPClass'es */
  public static Collection<RPClass> getAllRPClasses()
  {
    return Collections.unmodifiableCollection(rpClassList.values());
  }

  /** updates the attributes list */
  public static synchronized void updateAttributes(Map<String, Short> attributes)
  {
    // iterate over all (new) attributes
    for (String name : attributes.keySet())
    {
      // do we have this attribute already?
      if (!AttributeDesc.attributeIntegerMap.containsKey(name))
      {
        // no, add it
        AttributeDesc.attributeIntegerMap.put(name, attributes.get(name));
        System.out.println("added "+name+", "+attributes.get(name));
      }
      else
      {
        // yes, check if the unique id is equal
        if (attributes.get(name).shortValue() != AttributeDesc.attributeIntegerMap.get(name).shortValue())
        {
          // not equal...notify logger
          logger.error("attribute "+name+" failure. client id: "+AttributeDesc.attributeIntegerMap.get(name)+
                       " server id: "+attributes.get(name));
        }
      }
    }
  }

  /** returns all currently known Attributes an Object can have*/
  public static Map<String, Short> getAllAttributes()
  {
    return Collections.unmodifiableMap(AttributeDesc.attributeIntegerMap);
  }

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
    add("clientid",INT,(byte)(HIDDEN|VOLATILE));
    add("zoneid",STRING,HIDDEN);
    add("#db_id",INT,HIDDEN);
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

        public byte getFlags(String name)
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
  public boolean add(String name, byte type, byte flags)
    {
    AttributeDesc desc=new AttributeDesc(name,type,flags);
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

    //throw new SyntaxException(name);
    return AttributeDesc.getValidCode(name);
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

    // maybe its an annonymous attribute
    Map <String, Short> map = AttributeDesc.attributeIntegerMap;
    for (String name : map.keySet())
    {
      if (map.get(name).shortValue() == code)
      {
        return name;
      }
    }
    
    throw new SyntaxException(code);
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

    return STRING;
    //throw new SyntaxException(name);
    }

  /** Returns the flags of the attribute whose name is name for this rpclass */
  public byte getFlags(String name) throws SyntaxException
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=attributes.get(name);
      return desc.flags;
      }

    if(parent!=null)
      {
      return parent.getFlags(name);
      }

    //throw new SyntaxException(name);
    if(name.charAt(0) == '!')
      {
      return RPClass.HIDDEN;
      }
    else
      {
      return RPClass.VISIBLE;
      }
    }

  /** Return the visibility of the attribute whose name is name for this rpclass */
  public boolean isVisible(String name)
    {
     byte b = getFlags(name);
     return ((b & RPClass.HIDDEN) == 0);
    }

  /** Return the storability of the attribute whose name is name for this rpclass */
  public boolean isStorable(String name)
    {
     byte b = getFlags(name);
     return ((b & RPClass.VOLATILE) == 0);
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

    throw new SyntaxException(name);
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
