package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
public class MessageC2SLogout extends Message
  { 
  MessageC2SLogout(InetSocketAddress source)
    {
    super(source);
    
    type=TYPE_C2S_LOGOUT;
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

