package marauroa.game;

public class AttributeNotFoundException extends Exception
  {
  private String attribute;
  public AttributeNotFoundException(String attrib)
    {
    super("Attribute ["+attrib+"] not found");
    attribute=attrib;
    }
	
  public String getAttribute()
    {
    return attribute;
    }
  }
	