package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
public class MessageC2SLogin extends Message
  {
  private String username;
  private String password;
  
  MessageC2SLogin(InetSocketAddress source,String username, String password)
    {
    super(source);
    
    type=TYPE_C2S_LOGIN;
    this.username=username;
    this.password=password;
    }  
  
  String getUsername()
    {
    return username;    
    }
    
  String getPassword()
    {
    return password;
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(username);
    out.write(password);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    username=in.readString();
    password=in.readString();
    }    
  };


  