package marauroa.game;

public class NoSuchPlayerException extends Exception
  {
  NoSuchPlayerException(String player)
    {
    super("Unable to find the requested player ["+player+"]");
    }
  }
	