package marauroa.common;

public class PropertyNotFoundException extends RuntimeException
  {
  public PropertyNotFoundException(String property)
    {
    super("Property ["+property+"] not found");
    }
  }
	