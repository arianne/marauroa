package marauroa.game;

public class PlayerAlreadyAddedException extends Exception
  {
  PlayerAlreadyAddedException(String player)
    {
    super("Player ["+player+"] already added to the database.");
    }
  }  
	