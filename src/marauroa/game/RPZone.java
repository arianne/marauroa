package marauroa.game;

import java.util.*;

public interface RPZone
  {  
  static class RPObjectNotFoundException extends Throwable
    {
    public RPObjectNotFoundException()
      {
      super("RP Object not found");
      }
    }
  
  static class RPObjectInvalidException extends Throwable
    {
    public RPObjectInvalidException()
      {
      super("Object is invalid: It lacks of mandatory attributes");
      }
    }
  
  public void add(RPObject object) throws RPObjectInvalidException;
  public void remove(RPObject.ID id) throws RPObjectNotFoundException;
  public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;
  public boolean has(RPObject.ID id);
  }
