package marauroa.server.game;

public class NoSuchCharacterException extends Exception
  {
  public NoSuchCharacterException(String character)
    {
    super("Unable to find the requested character ["+character+"]");
    }
  }
	