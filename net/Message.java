package marauroa.net;

import java.net.InetSocketAddress;
import java.io.*;

/** Message is a class to represent all the kind of messages that are possible
 *  to exist in marauroa.
 *  
 * @author Miguel Angel Blanch Lardin <miguelangelblanchlardin@hotmail.com>
 */
public class Message implements marauroa.net.Serializable
  {
  public static byte TYPE_INVALID=-1;
  public static byte TYPE_C2S_LOGIN=1;
  public static byte TYPE_S2C_CHARACTERLIST=2;
  public static byte TYPE_S2C_LOGINFAILURE=3;
  public static byte TYPE_C2S_LOGOUT=4;
    
  protected byte type;
  protected byte flags;
  protected short clientid;
   
  protected InetSocketAddress source;
  
  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message
   */
  public Message(InetSocketAddress source)
    {
    this.source=source;
    }

  /** Returns the TCP/IP address associatted with this message 
   *  @return the TCP/IP address associatted with this message*/  
  public InetSocketAddress getAddress()
    {
    return source;
    }
    
  public void setClientID(short clientid)
    {
    this.clientid=clientid;
    }
  
  public short getClientID()
    {
    return clientid;
    }

  /** Serialize the object into an ObjectOutput 
   *  @throws IOException if the serializations fails 
   */
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    out.write(clientid);
    out.write(type);
    out.write(flags);
    }
    
  /** Serialize the object from an ObjectInput 
   *  @throws IOException if the serializations fails 
   *  @throws java.lang.ClassNotFoundException if the serialized class doesn't exist.
   */
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    clientid=in.readShort();
    type=in.readByte();
    flags=in.readByte();
    }  
  };
