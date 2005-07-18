package marauroa.server.game;

public class GenericDatabaseException extends Exception
  {
  public GenericDatabaseException(Throwable cause)
    {
    super(cause);
    }
  
  public GenericDatabaseException(String message, Throwable cause)
    {
    super(message,cause);
    }
  
  /** Do not use
   * <code>throw new GenericDatabaseException(cause.getMessage())</code>.
   * use:
   * <code>throw new GenericDatabaseException(cause)</code>.
   */
  public GenericDatabaseException(String message)
    {
    super(message);
    }  
  }
	