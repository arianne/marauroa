package marauroa.server.game;

public class PlayerNotFoundException extends Exception
  {
  public PlayerNotFoundException(String player)
    {
    super("Player ["+player+"] not found on the database");
    }
  }  
	