package marauroa.game;

import java.util.*;

public class RPClass 
  {
  final public static int HIDDEN=1;
  final public static int PUBLIC=2;
  final public static int CLASS=4;
  
  final public static int STRING=1;
  final public static int INT=2;
  final public static int FLOAT=3;

  private String name;
  private Map attributes;
  
  static private class AttributeDesc
    {
    private static int lastCode=0;
    private static int getValidCode()
      {
      return ++lastCode;
      }
    
    public AttributeDesc(String name, int type, int scope)
      {
      code=getValidCode();
      this.name=name;
      this.type=type;
      this.scope=scope;
      }
      
    public int code;
    public String name;
    public int type;
    public int scope;   
    }
  
  private static Map rpClassList;
  
  static
    {
    rpClassList=new HashMap();
    }
  
  public RPClass(String type)
    {    
    name=type;
    attributes=new HashMap();
    
    rpClassList.put(type,this);
    }
  
  public boolean addAttribute(String name, int type, int scope)
    {    
    AttributeDesc desc=new AttributeDesc(name,type,scope);
    attributes.put(name,desc);
    
    return true;
    }

  public int getCode(String name)
    {
    if(attributes.containsKey(name))
      {
      AttributeDesc desc=(AttributeDesc)attributes.get(name);
      return desc.code;
      }
    
    /* TODO: Throw exception */
    return -1;  
    }
  
  public boolean hasAttribute(String name)
    {
    if(attributes.containsKey(name))
      {
      return true;
      }
    
    return false;
    }
  
  public static RPClass getRPClass(String name)
    {
    if(rpClassList.containsKey(name))
      {
      return (RPClass)rpClassList.get(name);
      }
    
    /* TODO: Throw exception */
    return null;
    }
  }
