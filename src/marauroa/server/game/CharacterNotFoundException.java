package marauroa.server.game;

public class CharacterNotFoundException extends Exception
  {
  private static final long serialVersionUID = 4421144516681943172L;

  public CharacterNotFoundException(String character)
    {
    super("Character ["+character+"] not found on the database");
    }
  }
	