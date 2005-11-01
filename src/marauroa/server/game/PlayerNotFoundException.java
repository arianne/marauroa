package marauroa.server.game;

public class PlayerNotFoundException extends Exception
  {
  private static final long serialVersionUID = -8585188248951632653L;

  public PlayerNotFoundException(String player)
    {
    super("Player ["+player+"] not found on the database");
    }
  }  
	