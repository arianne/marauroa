
package marauroa.game;

public class Transaction
  {
  static public class TransactionException extends Exception
    {
    public TransactionException(String msg)
      {
      super(msg);
      }
    };
    
  public void commit() throws TransactionException
    {
    }
  
  public void rollback()
    {
    }
  }
