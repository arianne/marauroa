package marauroa.net;

import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;

/** MessageFactory is the class that is in charge of building the messages from
 *  the stream of bytes.
 */
public class MessageFactory
  {  
  private static Map factoryArray;
  private static MessageFactory messageFactory;
  
  private MessageFactory()
    {
    factoryArray= new HashMap();
    }
  
  static MessageFactory getFactory()
    {
    if(messageFactory==null)
      {
      messageFactory=new MessageFactory();
      }
      
    return messageFactory;
    }
  
  public void register(int index,Object messageClass)
    {
    factoryArray.put(new Integer(index),messageClass);
    }
    
  /** Returns a object of the right class from a stream of serialized data.
      @param data the serialized data
      @param source the source of the message needed to build the object. */  
  public Message getMessage(byte[] data, InetSocketAddress source)
    {
    if(data[0]==NetConst.NETWORK_PROTOCOL_VERSION)
      {
      if(factoryArray.containsKey(new Integer(data[1])))
        {
        Message tmp=(Message) factoryArray.get(new Integer(data[1]));

	    try
	      {
 	      ByteArrayInputStream in=new ByteArrayInputStream(data);
 	      InputSerializer s=new InputSerializer(in);

          tmp.readObject(s);
          }
        catch(java.io.IOException e)
          {
          return null;
          }
        catch(java.lang.ClassNotFoundException e)
          {
          return null;
          }
        
        return tmp;
        }
      else
        {
        return null;
        }
      }
      
    return null;
    }
  };