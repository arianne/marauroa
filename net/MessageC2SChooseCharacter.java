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
  
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param character The name of the choosen character that <b>MUST</b> be one
   *  of the returned by the marauroa.net.MessageS2CCharacters
   *  @see marauroa.net.MessageS2CCharacters
   */
  MessageC2SChooseCharacter(InetSocketAddress source,String character)
    {
    super(source);
    
    type=TYPE_C2S_LOGIN;
    this.character=character;
    }  
  
  /** Returns the name of the chosen character */
  String getCharacter()
    {
    return character;    
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    /** @todo Make sure that this work with UTF instear of 8bits chars */
    super.writeObject(out);    
    out.write(character);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    character=in.readString();
    }    
  };
