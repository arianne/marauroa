package marauroa.client;


public class ariannexpTimeoutException extends Exception
  {
  public ariannexpTimeoutException()
    {
    super("Timeout happened while waiting server reply");
    }
  }
