package marauroa.server;

public class PropertyNotFoundException extends Exception
  {
  public PropertyNotFoundException(String property)
    {
    super("Property ["+property+"] not found");
    }
  }
	