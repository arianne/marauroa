package marauroa.common.game;

public class RPObjectInvalidException extends Exception
  {
  public RPObjectInvalidException(String attribute)
    {
    super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
    }
  }
	