package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the server that the client wants to login and send the
 *  needed info: username and password to login to server.
 *  @see marauroa.net.Message
 */
public class MessageC2SLogin extends Message
  {
  private String username;
  private String password;
  
  /** Constructor for allowing creation of an empty message */
  public MessageC2SLogin()
    {
    super(null);
    
    type=TYPE_C2S_LOGIN;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param username the username of the user that wants to login
   *  @param password the plain password of the user that wants to login
   */
  public MessageC2SLogin(InetSocketAddress source,String username, String password)
    {
    super(source);
    
    type=TYPE_C2S_LOGIN;
    this.username=username;
    this.password=password;
    }  
  
  /** This method returns the username
   *  @return the username */
  public String getUsername()
    {
    return username;    
    }
    
  /** This method returns the password
   *  @return the password */
  public String getPassword()
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

  static private boolean registered=register();
  
  static private boolean register()
    {
    MessageFactory msgFactory=MessageFactory.getFactory();
    msgFactory.register(TYPE_C2S_LOGIN,new MessageC2SLogin());
    return true;
    }
  };


  