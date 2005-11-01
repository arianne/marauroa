package marauroa.client;

public class ariannexpTimeoutException extends Exception
{
  private static final long serialVersionUID = -6977739824675973192L;

  public ariannexpTimeoutException()
  {
    super("Timeout happened while waiting server reply");
  }
}
