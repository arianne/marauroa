package marauroa.net;
  
import java.net.InetSocketAddress;
import java.io.*;
import marauroa.game.*;
  
public class MessageC2SAction extends Message
  {
  private RPAction action;
  
  /** Constructor for allowing creation of an empty message */
  public MessageC2SAction()
    {
    super(null);
    
    type=TYPE_C2S_ACTION;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param action the username of the user that wants to login
   */
  public MessageC2SAction(InetSocketAddress source,RPAction action)
    {
    super(source);
    
    type=TYPE_C2S_ACTION;
    this.action=action;
    }  
  
  /** This method returns the action
   *  @return the action */
  public RPAction getRPAction()
    {
    return action;    
    }
    
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    action.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    action=(RPAction)in.readObject(new RPAction());
    }    

  static private boolean registered=register();
  
  static private boolean register()
    {
    MessageFactory msgFactory=MessageFactory.getFactory();
    msgFactory.register(TYPE_C2S_ACTION,new MessageC2SAction());
    return true;
    }
  };


  