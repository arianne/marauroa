package marauroa.game;

import java.util.*;

public interface RPZone
  {  
  public static class RPObjectNotFoundException extends Exception
    {
    public RPObjectNotFoundException()
      {
      super("RP Object not found");
      }
    }
  
  public static class RPObjectInvalidException extends Exception
    {
    public RPObjectInvalidException()
      {
      super("Object is invalid: It lacks of mandatory attributes");
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

    public void deleted(RPObject object)
      {
      modifiedList.add(object);
      }
    }
  
  public void add(RPObject object) throws RPObjectInvalidException;
  public void remove(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  public boolean has(RPObject.ID id);
  public RPObject create();
  public Iterator iterator();
  public Perception getPerception(RPObject.ID id, byte type);
  public void nextTurn();
  }
