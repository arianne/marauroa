/* $Id: MessageFactory.java,v 1.7 2005/12/18 11:48:09 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import marauroa.common.Log4J;
import marauroa.common.Utility;
import marauroa.common.game.Attributes;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/** MessageFactory is the class that is in charge of building the messages from
 *  the stream of bytes. */
public class MessageFactory
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(Attributes.class);
  
  private static Map<Integer,Class> factoryArray;
  private static MessageFactory messageFactory;

  private MessageFactory()
    {
    factoryArray=new HashMap<Integer,Class>();
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
    Log4J.startMethod(logger,"register");
    register(Message.MessageType.C2S_ACTION,              MessageC2SAction.class);
    register(Message.MessageType.C2S_CHOOSECHARACTER,     MessageC2SChooseCharacter.class);
    register(Message.MessageType.C2S_LOGOUT,              MessageC2SLogout.class);
    register(Message.MessageType.S2C_ACTION_ACK,          MessageS2CActionACK.class);
    register(Message.MessageType.S2C_CHARACTERLIST,       MessageS2CCharacterList.class);
    register(Message.MessageType.S2C_CHOOSECHARACTER_ACK, MessageS2CChooseCharacterACK.class);
    register(Message.MessageType.S2C_CHOOSECHARACTER_NACK,MessageS2CChooseCharacterNACK.class);
    register(Message.MessageType.S2C_LOGIN_ACK,           MessageS2CLoginACK.class);
    register(Message.MessageType.S2C_LOGIN_NACK,          MessageS2CLoginNACK.class);
    register(Message.MessageType.S2C_LOGOUT_ACK,          MessageS2CLogoutACK.class);
    register(Message.MessageType.S2C_LOGOUT_NACK,         MessageS2CLogoutNACK.class);
    register(Message.MessageType.S2C_PERCEPTION,          MessageS2CPerception.class);
    register(Message.MessageType.C2S_PERCEPTION_ACK,      MessageC2SPerceptionACK.class);
    register(Message.MessageType.C2S_OUTOFSYNC,           MessageC2SOutOfSync.class);
    register(Message.MessageType.S2C_SERVERINFO,          MessageS2CServerInfo.class);
    register(Message.MessageType.S2C_INVALIDMESSAGE,      MessageS2CInvalidMessage.class);
    register(Message.MessageType.S2C_TRANSFER_REQ,        MessageS2CTransferREQ.class);
    register(Message.MessageType.C2S_TRANSFER_ACK,        MessageC2STransferACK.class);
    register(Message.MessageType.S2C_TRANSFER,            MessageS2CTransfer.class);
    register(Message.MessageType.C2S_LOGIN_REQUESTKEY,    MessageC2SLoginRequestKey.class);
    register(Message.MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD,MessageC2SLoginSendNonceNameAndPassword.class);
    register(Message.MessageType.S2C_LOGIN_SENDKEY,       MessageS2CLoginSendKey.class);
    register(Message.MessageType.S2C_LOGIN_SENDNONCE,     MessageS2CLoginSendNonce.class);
    register(Message.MessageType.C2S_LOGIN_SENDPROMISE,   MessageC2SLoginSendPromise.class);
    Log4J.finishMethod(logger,"register");
    }

  private void register(Message.MessageType index,Class messageClass)
    {
    factoryArray.put(new Integer(index.ordinal()),messageClass);
    }

  /** Returns a object of the right class from a stream of serialized data.
   @param data the serialized data
   @param source the source of the message needed to build the object.
   @return a message of the right class
   @throws IOException in case of problems with the message
   @throws InvalidVersionException if the message version doesn't match */
  public Message getMessage(byte[] data, InetSocketAddress source) throws IOException, InvalidVersionException
    {
    Log4J.startMethod(logger,"getMessage");
    try
      {
      if(data[0]==NetConst.NETWORK_PROTOCOL_VERSION)
        {
        if(factoryArray.containsKey(new Integer(data[1])))
          {
          Message tmp=null;
          try
            {
            Class messageType=(Class) factoryArray.get(new Integer(data[1]));
            tmp = (Message) messageType.newInstance();
            ByteArrayInputStream in=new ByteArrayInputStream(data);
            InputSerializer s=new InputSerializer(in);

            tmp.readObject(s);
            tmp.setAddress(source);
            return tmp;
            }
          catch(Exception e)
            {
            NDC.push("message is ["+tmp+"]\n");
            NDC.push("message dump is [\n"+Utility.dumpByteArray(data)+"\n]\n");
            logger.error("error in getMessage",e);
            NDC.pop();
            NDC.pop();
            throw new IOException(e.getMessage());
            }
          }
        else
          {
          logger.warn("Message type ["+data[1]+"] is not registered in the MessageFactory");
          throw new IOException("Message type ["+data[1]+"] is not registered in the MessageFactory");
          }
        }
      else
        {
        logger.warn("Message has incorrect protocol version("+data[0]+") expected ("+NetConst.NETWORK_PROTOCOL_VERSION+")");
        logger.debug("Message is: "+Utility.dumpByteArray(data));
        throw new InvalidVersionException(data[0]);
        }
      }
    finally
      {
      Log4J.finishMethod(logger,"getMessage");
      }
    }
  }


;
