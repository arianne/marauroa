package marauroa.game;

class ActionInvalidException extends Exception
  {
  ActionInvalidException(String attribute)
    {
    super("Action is invalid: It lacks of mandatory attribute ["+attribute+"]");
    }
  }
	