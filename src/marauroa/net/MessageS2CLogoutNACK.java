package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the client that the server has rejected its Logout Message
 *  @see marauroa.net.Message
 */
public class MessageS2CLogoutNACK extends Message
  {
  /** Constructor for allowing creation of an empty message */
  public MessageS2CLogoutNACK()
    {
    super(null);
    
    type=TYPE_S2C_LOGOUT_NACK;
    }

  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message */
  public MessageS2CLogoutNACK(InetSocketAddress source)
    {
    super(source);
    
    type=TYPE_S2C_LOGOUT_NACK;
    }  

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Logout NACK) from ("+source.toString()+") CONTENTS: ()";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    if(type!=TYPE_S2C_LOGOUT_NACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  };


  