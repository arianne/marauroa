package marauroa.common.game;

public class AttributeNotFoundException extends RuntimeException
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
	