package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
public class MessageS2CCharacterList extends Message
  {
  private String[] characters;
  
  MessageS2CCharacterList(InetSocketAddress source,String[] characters)
    {
    super(source);
    
    type=TYPE_S2C_CHARACTERLIST;
    this.characters=characters;
    }  
  
  String[] getCharacters()
    {
    return characters;    
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
    }    
  };


  