package marauroa.server.game;

public class ActionInvalidException extends Exception
  {
  private static final long serialVersionUID = -2287105367089095987L;

  ActionInvalidException(String attribute)
    {
    super("Action is invalid: It lacks of mandatory attribute ["+attribute+"]");
    }
  }
	