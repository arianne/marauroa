package marauroa.common.game;

public class AttributeNotFoundException extends RuntimeException
  {
  private static final long serialVersionUID = -6055703660022859631L;
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
	