package marauroa.net;

import java.net.InetSocketAddress;
import java.io.*;

/** Message is a class to represent all the kind of messages that are possible
 *  to exist in marauroa.
 */
public class Message implements marauroa.net.Serializable
  {
  public static byte TYPE_INVALID=-1;
  
  public static byte TYPE_C2S_LOGIN=1;
  public static byte TYPE_S2C_LOGIN_ACK=10;
  public static byte TYPE_S2C_LOGIN_NACK=11;
  
  public static byte TYPE_S2C_CHARACTERLIST=2;
  
  public static byte TYPE_C2S_CHOOSECHARACTER=3;
  public static byte TYPE_S2C_CHOOSECHARACTER_ACK=30;
  public static byte TYPE_S2C_CHOOSECHARACTER_NACK=31;
  
  public static byte TYPE_C2S_LOGOUT=4;
  public static byte TYPE_S2C_LOGOUT_ACK=40;
  public static byte TYPE_S2C_LOGOUT_NACK=41;
    
  protected byte type;
  protected short clientid;
   
  protected InetSocketAddress source;
  
  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message
   */
  public Message(InetSocketAddress source)
    {
    this.type=TYPE_INVALID;
    this.source=source;
    }

  /** Returns the TCP/IP address associatted with this message 
   *  @return the TCP/IP address associatted with this message*/  
  public InetSocketAddress getAddress()
    {
    return source;
    }

  /** Returns the type of the message
   *  @return the type of the message  */
  public byte getType()
    {
    return type;
    }
    
  /** Set the clientID so that we can identify the client to which the
      message is target, as only IP is easy to Fake
      @param clientid a short that reprents the client id. */    
  public void setClientID(short clientid)
    {
    this.clientid=clientid;
    }
  
  /** Returns the clientID of the Message.
      @returns the ClientID */
  public short getClientID()
    {
    return clientid;
    }

  /** Serialize the object into an ObjectOutput 
   *  @throws IOException if the serializations fails 
   */
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    out.write(NetConst.NETWORK_PROTOCOL_VERSION);
    out.write(type);
    out.write(clientid);
    }
    
  /** Serialize the object from an ObjectInput 
   *  @throws IOException if the serializations fails 
   *  @throws java.lang.ClassNotFoundException if the serialized class doesn't exist.
   */
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    if(in.readByte()!=NetConst.NETWORK_PROTOCOL_VERSION)
      {
      throw new IOException();
      }
      
    type=in.readByte();
    clientid=in.readShort();
    }  
  };
