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
  
  public RPClass(String type)
    {    
    name=type;
    attributes=new HashMap();
    }
  
  public boolean addAttribute(String name, int type, int scope)
    {    
    AttributeDesc desc=new AttributeDesc(name,type,scope);
    attributes.put(name,desc);
    
    return true;
    }
  
  public int getCode(String name)
    {
    return ((AttributeDesc)attributes.get(name)).code;
    }
  }
