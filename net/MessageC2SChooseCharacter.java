package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;

/** This message indicate the server what of the available characters is chosen
 *  for the session to play.
 *  @see marauroa.net.Message
 */
public class MessageC2SChooseCharacter extends Message
  {
  private String character;
  
  /** Constructor for allowing creation of an empty message */
  public MessageC2SChooseCharacter()
    {
    super(null);
    
    type=TYPE_C2S_CHOOSECHARACTER;
    }  
    
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param character The name of the choosen character that <b>MUST</b> be one
   *  of the returned by the marauroa.net.MessageS2CCharacters
   *  @see marauroa.net.MessageS2CCharacters
   */
  public MessageC2SChooseCharacter(InetSocketAddress source,String character)
    {
    super(source);
    
    type=TYPE_C2S_CHOOSECHARACTER;
    this.character=character;
    }  
  
  /** This methods returns the name of the chosen character 
      @return the character name*/
  public String getCharacter()
    {
    return character;    
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);    
    out.write(character);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    character=in.readString();
    }    
  };
