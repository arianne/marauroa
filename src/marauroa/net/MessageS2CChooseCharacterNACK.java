package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the client that the server has rejected its ChooseCharacter Message
 *  @see marauroa.net.Message
 */
public class MessageS2CChooseCharacterNACK extends Message
  {
  /** Constructor for allowing creation of an empty message */
  public MessageS2CChooseCharacterNACK()
    {
    super(null);
    
    type=TYPE_S2C_CHOOSECHARACTER_NACK;
    }

  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message */
  public MessageS2CChooseCharacterNACK(InetSocketAddress source)
    {
    super(source);
    
    type=TYPE_S2C_CHOOSECHARACTER_NACK;
    }  

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C ChooseCharacter NACK) from ("+source.toString()+") CONTENTS: ()";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    if(type!=TYPE_S2C_CHOOSECHARACTER_NACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  };


  