package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;

/** The Logout Message is sent from client to server to indicate that it wants to 
 *  finish the session. */
public class MessageC2SLogout extends Message
  { 
  /** Constructor for allowing creation of an empty message */
  public MessageC2SLogout()
    {
    super(null);
    
    type=TYPE_C2S_LOGOUT;
    }
    
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   */
  public MessageC2SLogout(InetSocketAddress source)
    {
    super(source);
    
    type=TYPE_C2S_LOGOUT;
    }  
  
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    }    
 
  static private boolean registered=register();
  
  static private boolean register()
    {
    MessageFactory msgFactory=MessageFactory.getFactory();
    msgFactory.register(TYPE_C2S_LOGOUT,new MessageC2SLogout());
    return true;
    }
  };

