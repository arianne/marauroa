package marauroa.game;

public class RPAction extends Attributes
{
  public final static Status STATUS_SUCCESS=new Status(Status.SUCCESS);
  public final static Status STATUS_FAIL=new Status(Status.FAIL);
  public final static Status STATUS_INCOMPLETE=new Status(Status.INCOMPLETE);
  public static class Status
  {
    public final static byte SUCCESS=0;
    public final static byte FAIL=1;
    public final static byte INCOMPLETE=2;
    private byte val;
    
    public Status(byte val)
    {
      this.val=val;
    }
    
    public byte getStatus()
    {
      return val;
    }
  }
  
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
