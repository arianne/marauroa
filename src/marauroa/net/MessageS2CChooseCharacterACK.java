package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;

import marauroa.game.*;
  
/** This message indicate the client that the server has accepted its ChooseCharacter Message
 *  @see marauroa.net.Message
 */
public class MessageS2CChooseCharacterACK extends Message
  {
  private RPObject.ID id;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CChooseCharacterACK()
    {
    super(null);
    
    type=TYPE_S2C_CHOOSECHARACTER_ACK;
    }

  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message */
  public MessageS2CChooseCharacterACK(InetSocketAddress source, RPObject.ID id)
    {
    super(source);
    this.id=id;
    
    type=TYPE_S2C_CHOOSECHARACTER_ACK;
    }
   
  /** This method returns the object id of the choosen character
   *  @returns RPObject.ID of the choosen character */
  public RPObject.ID getObjectID()
    {
    return id;
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
    id.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    id=new RPObject.ID(-1);
    id.readObject(in);
    
    if(type!=TYPE_S2C_CHOOSECHARACTER_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  };


  
