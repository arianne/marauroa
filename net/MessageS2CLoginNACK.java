package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the client that the server has reject its login Message
 *  @see marauroa.net.Message
 */
public class MessageS2CLoginNACK extends Message
  {
  public static byte UNKNOWN_REASON=0;
  public static byte USERNAME_WRONG=1;
  public static byte SERVER_IS_FULL=2;  
  
  static private String[] text=
    {
    "Unknown reason",
    "Username/Password incorrect.",
    "Server is full."    
    };
    
  private byte reason;  
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CLoginNACK()
    {
    super(null);
    
    type=TYPE_S2C_LOGIN_NACK;
    }

  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message
   *  @param resolution the reason to deny the login */
  public MessageS2CLoginNACK(InetSocketAddress source, byte resolution)
    {
    super(source);
    
    type=TYPE_S2C_LOGIN_NACK;
    
    reason=resolution;
    }  
  
  public byte getResolutionCode()
    {
    return reason;
    }
  
  public String getResolution()
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
 
  static private boolean registered=register();
  
  static private boolean register()
    {
    MessageFactory msgFactory=MessageFactory.getFactory();
    msgFactory.register(TYPE_S2C_LOGIN_NACK,new MessageS2CLoginNACK());
    return true;
    }
  };


  