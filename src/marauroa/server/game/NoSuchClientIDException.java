package marauroa.server.game;

public class NoSuchClientIDException extends Exception
  {
  public NoSuchClientIDException(int clientid)
    {
    super("Unable to find the requested client id ["+clientid+"]");
    }
  }
	