package marauroa.server.game;

public class TransactionException extends Exception
  {
  TransactionException(String msg)
    {
    super(msg);
    }
  TransactionException(String msg, Throwable cause)
    {
    super(msg, cause);
    }
  }
