package marauroa.server.game;

public class NoSuchPlayerException extends Exception
  {
  private static final long serialVersionUID = 4411913867653287301L;

  NoSuchPlayerException(String player)
    {
    super("Unable to find the requested player ["+player+"]");
    }
  }
	