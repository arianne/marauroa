package marauroa.server.game;

public class CharacterNotFoundException extends Exception
  {
  public CharacterNotFoundException(String character)
    {
    super("Character ["+character+"] not found on the database");
    }
  }
	