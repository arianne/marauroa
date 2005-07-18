package marauroa.server.game;

public class NoDatabaseConfException extends IllegalStateException
  {
  public NoDatabaseConfException(Throwable cause)
    {
    super("Database configuration file not found.", cause);
    }
  public NoDatabaseConfException()
    {
    super("Database configuration file not found.");
    }
  }
	