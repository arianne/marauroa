package marauroa.net;

public class InvalidVersionException extends Exception
  {
  private int version;

  public InvalidVersionException(int version)
    {
    super();
    this.version=version;
    }
    
  public int getVersion()
    {
    return version;
    }
  }
	