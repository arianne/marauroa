package marauroa.game;

import java.util.*;

public class MarauroaRPZone implements RPZone
  {
  private HashMap objects;
  private List listObjects;
  private Perception perception;

  private static Random rand=new Random();
  
  public MarauroaRPZone()
    {
    rand.setSeed(new Date().getTime());
    objects=new HashMap();
    
    listObjects=new LinkedList();
    perception=new Perception(Perception.DELTA);
    }
  
  public void add(RPObject object) throws RPObjectInvalidException
    {
    try
      {
      RPObject.ID id=new RPObject.ID(object);
      objects.put(id,object);
      
      listObjects.add(object);
      perception.modified(object);
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      throw new RPObjectInvalidException();
      }
    }
  
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException
    {
    if(objects.containsKey(id))
      {
      RPObject object=(RPObject)objects.remove(id);
      
      listObjects.remove(object);
      perception.removed(object);
      
      return object;
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
  
  public RPObject create()
    {
    RPObject.ID id=new RPObject.ID(rand.nextInt());
    
    while(has(id) || id.getObjectID()==-1)    
      {
      id=new RPObject.ID(rand.nextInt());
      }
     
    return new RPObject(id);
    }
    
  public Iterator iterator()
    {
    return objects.entrySet().iterator();
    }

  public Perception getPerception(RPObject.ID id, byte type)
    {
    if(type==Perception.DELTA)
      {
      return perception;
      }
    else
      {
      Perception p=new Perception(Perception.TOTAL);
      p.modifiedList=listObjects;
      
      return p;
      }
    }
  
  public void nextTurn() 
    {
    perception=new Perception(Perception.DELTA);
    }  
  }

