package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;

public class MessageS2CPerception extends Message
  {
  /* TODO: Complete it */
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CPerception()
    {
    super(null);
    
    type=TYPE_S2C_PERCEPTION;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param 
   *  @see marauroa.net.MessageS2CCharacters
   */
  public MessageS2CPerception(InetSocketAddress source,String[] characters)
    {    
    super(source);
    
    type=TYPE_S2C_PERCEPTION;
    }  
  

  public String toString()
    {
    return "Message (S2C Perception) from ("+source.toString()+") CONTENTS: (TODO)";
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
    msgFactory.register(TYPE_S2C_PERCEPTION,new MessageS2CPerception());
    return true;
    }
  };


  