package marauroa.net;

import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;

import marauroa.marauroad;

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
    register();
    }
  
  /** This method returns an instance of MessageFactory 
   *  @return A shared instance of MessageFactory */
  public static MessageFactory getFactory()
    {
    if(messageFactory==null)
      {
      messageFactory=new MessageFactory();
      }
      
    return messageFactory;
    }
    
  private void register()
    {
    marauroad.trace("MessageFactory::register",">");
    register(Message.TYPE_C2S_ACTION,MessageC2SAction.class);
    register(Message.TYPE_C2S_CHOOSECHARACTER,MessageC2SChooseCharacter.class);
    register(Message.TYPE_C2S_LOGIN,MessageC2SLogin.class);
    register(Message.TYPE_C2S_LOGOUT,MessageC2SLogout.class);
    register(Message.TYPE_S2C_ACTION_ACK,MessageS2CActionACK.class);
    register(Message.TYPE_S2C_CHARACTERLIST,MessageS2CCharacterList.class);
    register(Message.TYPE_S2C_CHOOSECHARACTER_ACK,MessageS2CChooseCharacterACK.class);
    register(Message.TYPE_S2C_CHOOSECHARACTER_NACK,MessageS2CChooseCharacterNACK.class);
    register(Message.TYPE_S2C_LOGIN_ACK,MessageS2CLoginACK.class);
    register(Message.TYPE_S2C_LOGIN_NACK,MessageS2CLoginNACK.class);
    register(Message.TYPE_S2C_LOGOUT_ACK,MessageS2CLogoutACK.class);
    register(Message.TYPE_S2C_LOGOUT_NACK,MessageS2CLogoutNACK.class);
    register(Message.TYPE_S2C_PERCEPTION,MessageS2CPerception.class);
    register(Message.TYPE_C2S_PERCEPTION_ACK,MessageC2SPerceptionACK.class);
    marauroad.trace("MessageFactory::register","<");
    }
      
  private void register(int index,Class messageClass)
    {
    factoryArray.put(new Integer(index),messageClass);
    }
    
  /** Returns a object of the right class from a stream of serialized data.
      @param data the serialized data
      @param source the source of the message needed to build the object. 
      
      @throws IOException in case of problems with the message */  
  public Message getMessage(byte[] data, InetSocketAddress source) throws IOException
    {
    marauroad.trace("MessageFactory::getMessage",">");
    try
      {
	  if(data[0]==NetConst.NETWORK_PROTOCOL_VERSION)
	    {
	    if(factoryArray.containsKey(new Integer(data[1])))
	      {
	      Class messageType=(Class) factoryArray.get(new Integer(data[1]));
	      Message tmp=(Message) messageType.newInstance();
	
	 	  ByteArrayInputStream in=new ByteArrayInputStream(data);
	 	  InputSerializer s=new InputSerializer(in);
	
	      tmp.readObject(s);
	      tmp.setAddress(source);
	
	      marauroad.trace("MessageFactory::getMessage","<");
	      return tmp;
	      }
	    else
	      {
	      marauroad.trace("MessageFactory::getMessage","X","Message type ["+data[1]+"] is not registered in the MessageFactory");
	      throw new IOException("Message type ["+data[1]+"] is not registered in the MessageFactory");
	      }
	    }
      else
	    {      
	    marauroad.trace("MessageFactory::getMessage","X","Message has incorrect protocol version");
	    throw new IOException("Message has incorrect protocol version: "+data[0]+" ( expected "+NetConst.NETWORK_PROTOCOL_VERSION+")");
        }
      }
	catch(Exception e)
	  {
      marauroad.trace("MessageFactory::getMessage","X",e.getMessage());
      throw new IOException(e.getMessage());
	  }
	finally
	  {
      marauroad.trace("MessageFactory::getMessage","<");
	  }
	}
  };