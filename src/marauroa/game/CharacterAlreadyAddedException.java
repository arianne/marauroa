package marauroa.game;

class CharacterAlreadyAddedException extends Exception
  {
  public CharacterAlreadyAddedException(String character)
    {
    super("Character ["+character+"] already added to the database");
    }
  }
	