package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the client that the server has reject its login Message
 *  @see marauroa.net.Message
 */
public class MessageS2CLoginNACK extends Message
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
  
  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message
   *  @param resolution the reason to deny the login */
  MessageS2CLoginNACK(InetSocketAddress source, byte resolution)
    {
    super(source);
    
    type=TYPE_S2C_LOGIN_NACK;
    
    reason=resolution;
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


  