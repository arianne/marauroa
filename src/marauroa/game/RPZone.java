package marauroa.game;

import java.util.*;

public interface RPZone
  {  
  public static class RPObjectNotFoundException extends Exception
    {
    public RPObjectNotFoundException(RPObject.ID id)
      {
      super("RP Object ["+id+"] not found");
      }
    }
  
  public static class RPObjectInvalidException extends Exception
    {
    public RPObjectInvalidException(String attribute)
      {
      super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
      }
    }
    
  public static class Perception
    {
    final public static byte DELTA=0;
    final public static byte TOTAL=1;
    
    public byte type;
    public List modifiedList;
    public List deletedList;
    
    public Perception(byte type)
      {
      this.type=type;
      modifiedList=new LinkedList();
      deletedList=new LinkedList();
      }
    
    public void modified(RPObject object)
      {
      modifiedList.add(object);
      }

    public void removed(RPObject object)
      {
      deletedList.add(object);
      }
    
    public int size()
      {
      return (modifiedList.size()+deletedList.size());
      }
    }
  
  public void add(RPObject object) throws RPObjectInvalidException;
  public void modify(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  public boolean has(RPObject.ID id);
  
  public RPObject create();
  public Iterator iterator();
  public Perception getPerception(RPObject.ID id, byte type);
  public void nextTurn();
  }
