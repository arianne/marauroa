package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the client that the server has accepted its ChooseCharacter Message
 *  @see marauroa.net.Message
 */
public class MessageS2CChooseCharacterACK extends Message
  {
  /** Constructor for allowing creation of an empty message */
  public MessageS2CChooseCharacterACK()
    {
    super(null);
    
    type=TYPE_S2C_CHOOSECHARACTER_ACK;
    }

  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message */
  public MessageS2CChooseCharacterACK(InetSocketAddress source)
    {
    super(source);
    
    type=TYPE_S2C_CHOOSECHARACTER_ACK;
    }  

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Choose Character ACK) from ("+source.toString()+") CONTENTS: ()";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    if(type!=TYPE_S2C_CHOOSECHARACTER_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  };


  