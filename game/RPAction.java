package marauroa.game;

public class RPAction extends Attributes
  {
  public RPAction()
    {
    super();
    }
    
  public void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    }
  }