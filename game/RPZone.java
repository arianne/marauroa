package marauroa.game;

import java.util.*;

public class RPZone 
  {
  private HashMap objects;
  
  class RPObjectNotFoundException extends Throwable
    {
    public RPObjectNotFoundException()
      {
      super("RP Object not found");
      }
    }
    
  class RPObjectInvalidException extends Throwable
    {
    public RPObjectInvalidException()
      {
      super("Object is invalid: It lacks of mandatory attributes");
      }
    }
  
  public RPZone()
    {
    objects=new HashMap();
    }
    
  public void add(RPObject object) throws RPObjectInvalidException
    {
    try 
      {
      RPObject.ID id=new RPObject.ID(object);
      objects.put(id,object);
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      throw new RPObjectInvalidException();
      }
    }
    
  public void remove(RPObject.ID id) throws RPObjectNotFoundException
    {
    if(objects.containsKey(id))
      {
      objects.remove(id);
      }    
    
    throw new RPObjectNotFoundException();
    }
    
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException
    {
    if(objects.containsKey(id))
      {
      return (RPObject)objects.get(id);
      }    
    
    throw new RPObjectNotFoundException();
    }
    
  public boolean has(RPObject.ID id)
    {
    if(objects.containsKey(id))
      {
      return true;
      }
    else
      {
      return false;
      }
    }
  }