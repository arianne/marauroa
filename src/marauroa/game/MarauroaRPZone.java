package marauroa.game;

import java.util.*;

public class MarauroaRPZone implements RPZone
  {
  private HashMap objects;
  /** TODO: This is not delta perception */
  private List listObjects;
  
  public MarauroaRPZone()
    {
    objects=new HashMap();
    listObjects=new LinkedList();
    }
  
  public void add(RPObject object) throws RPObjectInvalidException
    {
    try
      {
      RPObject.ID id=new RPObject.ID(object);
      objects.put(id,object);
      listObjects.add(object);
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
      Object object=objects.remove(id);
      listObjects.remove(object);
      }
    else
      {
      throw new RPObjectNotFoundException();
      }
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

  public Perception getPerception(RPObject.ID id)
    {
    /** Using TOTAL perception per turn */
    RPZone.Perception perception=new RPZone.Perception(RPZone.Perception.TOTAL);
    perception.modifiedList=listObjects;
    perception.deletedList=new LinkedList();    
    
    return perception;
    }  
  }

