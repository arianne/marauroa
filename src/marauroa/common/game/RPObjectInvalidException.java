package marauroa.common.game;

public class RPObjectInvalidException extends RuntimeException
  {
  private static final long serialVersionUID = 2413566633754598291L;

  public RPObjectInvalidException(String attribute)
    {
    super("Object is invalid: It lacks of mandatory attribute ["+attribute+"]");
    }
  }
	