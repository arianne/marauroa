package marauroa.game;

/** This class represent an action. Please refer to Actions Explained document */
public class RPAction extends Attributes
  {
  public final static Status STATUS_SUCCESS=new Status(Status.SUCCESS);
  public final static Status STATUS_FAIL=new Status(Status.FAIL);
  public final static Status STATUS_INCOMPLETE=new Status(Status.INCOMPLETE);
  
  /** This class represent the status of the action */
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
    
    public boolean equals(Object status)
      {
      return val==((Status)status).val;
      }
    
    public String toString()
      {
      if(val==0) return "success";
      if(val==1) return "fail";
      if(val==2) return "incomplete";
      
      return "-incoherent status-";
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
