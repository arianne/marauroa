package marauroa.common.game;

public class RPObjectNotFoundException extends Exception
  {
  public RPObjectNotFoundException(RPObject.ID id)
    {
    super("RP Object ["+id+"] not found");
    }
  }
	