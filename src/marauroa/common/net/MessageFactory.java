/* $Id: MessageFactory.java,v 1.1 2005/01/23 21:00:44 arianne_rpg Exp $ */
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

import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;

import marauroa.common.*;

/** MessageFactory is the class that is in charge of building the messages from
 *  the stream of bytes. */
public class MessageFactory
  {
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
    Logger.trace("MessageFactory::register",">");
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
    register(Message.TYPE_C2S_OUTOFSYNC,MessageC2SOutOfSync.class);
    register(Message.TYPE_S2C_SERVERINFO,MessageS2CServerInfo.class);
    register(Message.TYPE_S2C_INVALIDMESSAGE,MessageS2CInvalidMessage.class);
    register(Message.TYPE_S2C_TRANSFER_REQ,MessageS2CTransferREQ.class);
    register(Message.TYPE_C2S_TRANSFER_ACK,MessageC2STransferACK.class);
    register(Message.TYPE_S2C_TRANSFER,MessageS2CTransfer.class);
    Logger.trace("MessageFactory::register","<");
    }
      
  private void register(int index,Class messageClass)
    {
    factoryArray.put(new Integer(index),messageClass);
    }
    
  /** Returns a object of the right class from a stream of serialized data.
   @param data the serialized data
   @param source the source of the message needed to build the object.
   @return a message of the right class   
   @throws IOException in case of problems with the message 
   @throws InvalidVersionException if the message version doesn't match */
  public Message getMessage(byte[] data, InetSocketAddress source) throws IOException, InvalidVersionException
    {
    Logger.trace("MessageFactory::getMessage",">");
    try
      {
      if(data[0]==NetConst.NETWORK_PROTOCOL_VERSION)
        {
        if(factoryArray.containsKey(new Integer(data[1])))
          {
          try
            {
            Class messageType=(Class) factoryArray.get(new Integer(data[1]));
            Message tmp=(Message) messageType.newInstance();
            ByteArrayInputStream in=new ByteArrayInputStream(data);
            InputSerializer s=new InputSerializer(in);
    
            tmp.readObject(s);
            tmp.setAddress(source);
            Logger.trace("MessageFactory::getMessage","<");
            return tmp;
            }
          catch(Exception e)
            {
            Logger.thrown("MessageFactory::getMessage","X",e);
            throw new IOException(e.getMessage());
            }
          }
        else
          {
          Logger.trace("MessageFactory::getMessage","X","Message type ["+data[1]+"] is not registered in the MessageFactory");
          throw new IOException("Message type ["+data[1]+"] is not registered in the MessageFactory");
          }
        }
      else
        {
        Logger.trace("MessageFactory::getMessage","X","Message has incorrect protocol version");
        throw new InvalidVersionException(data[0]);
        }
      }
    finally
      {
      Logger.trace("MessageFactory::getMessage","<");
      }
    }
  }


;
