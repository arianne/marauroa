package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;

/** The CharacterListMessage is sent from server to client to inform client about
 *  the possible election of character to play with.*/  
public class MessageS2CCharacterList extends Message
  {
  private String[] characters;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CCharacterList()
    {
    super(null);
    
    type=TYPE_S2C_CHARACTERLIST;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param characters the list of characters of the player
   *  @see marauroa.net.MessageS2CCharacters
   */
  public MessageS2CCharacterList(InetSocketAddress source,String[] characters)
    {    
    super(source);
    
    type=TYPE_S2C_CHARACTERLIST;
    this.characters=characters;
    }  
  
  /** This method returns the list of characters that the player owns 
   *  @return the list of characters that the player owns */
  public String[] getCharacters()
    {
    return characters;    
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    StringBuffer text=new StringBuffer(" ");
    for(int i=0;i<characters.length;++i)
      {
      text.append(characters[i]+",");
      }
      
    return "Message (S2C Character List) from ("+source.toString()+") CONTENTS: ("+text.substring(0,text.length()-1)+")";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(characters);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    characters=in.readStringArray();
    
    if(type!=TYPE_S2C_CHARACTERLIST)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  };


  