package marauroa.common.game;

public class RPObjectInvalidException extends RuntimeException
  {
  public RPObjectInvalidException(String attribute)
    {
    super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
    }
  }
	