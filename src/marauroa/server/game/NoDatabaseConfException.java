package marauroa.server.game;

public class NoDatabaseConfException extends Exception
  {
  public NoDatabaseConfException()
    {
    super("Database configuration file not found.");
    }
  }
	