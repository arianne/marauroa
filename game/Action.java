package marauroa.game;

import java.util.*;

public class Action implements marauroa.net.Serializable
  {
  private Attributes content;
  
  class AttributeNotFoundException extends Throwable
    {
    public AttributeNotFoundException()
      {
      super("Attribute not found");
      }
    }
    
  public Action()
    {
    content=new Attributes();
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    }
  }