package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
public class MessageS2CLoginFailure extends Message
  {
  public static short UNKNOWN_REASON=0;
  public static short USERNAME_WRONG=1;
  public static short SERVER_IS_FULL=2;  
  
  static private String[] text=
    {
    "Unknown reason",
    "Username/Password incorrect.",
    "Server is full."    
    };
    
  private byte reason;    
  
  MessageS2CLoginFailure(InetSocketAddress source,byte reason)
    {
    super(source);
    
    type=TYPE_S2C_LOGINFAILURE;
    this.reason=reason;
    }  
  
  String getResolution()
    {
    return text[reason];    
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(reason);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    reason=in.readByte();
    }    
  };


  