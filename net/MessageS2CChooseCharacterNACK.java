package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the client that the server has rejected its ChooseCharacter Message
 *  @see marauroa.net.Message
 */
public class MessageS2CChooseCharacterNACK extends Message
  {
  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message */
  MessageS2CChooseCharacterNACK(InetSocketAddress source)
    {
    super(source);
    
    type=TYPE_S2C_CHOOSECHARACTER_NACK;
    }  

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    }    
  };


  