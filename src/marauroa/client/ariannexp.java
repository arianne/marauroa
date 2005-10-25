/* $Id: ariannexp.java,v 1.17 2005/10/25 18:19:15 arianne_rpg Exp $ */
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
package marauroa.client;
import java.util.*;
import marauroa.client.net.NetworkClientManager;
import marauroa.common.Log4J;

import marauroa.common.crypto.RSAPublicKey;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPAction;
import marauroa.common.net.*;
import org.apache.log4j.Logger;

public abstract class ariannexp
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(ariannexp.class);
  
  public final static long TIMEOUT=10000;
  private NetworkClientManager netMan;
  private List<Message> messages;

  /** Constructor.
   *  @param logging ariannexp will write to a file if this is true. */
  public ariannexp(String loggingProperties)
    {
    Log4J.init(loggingProperties);

    messages=new LinkedList<Message>();
    }

  /** Call this method to connect to server.
   *  This method just configure the connection, it doesn't send anything
   *  @param host server host name
   *  @param port server port number  */
  public void connect(String host, int port) throws java.net.SocketException
    {
    Log4J.startMethod(logger, "connect");
    netMan=new NetworkClientManager(host,port);
    Log4J.finishMethod(logger, "connect");
    }

  private Message getMessage() throws InvalidVersionException,ariannexpTimeoutException
    {
    Message msg=null;
    long delta=System.currentTimeMillis();

    while(msg==null)
      {
      msg=netMan.getMessage();

      if(msg==null && System.currentTimeMillis()-delta>TIMEOUT)
        {
        throw new ariannexpTimeoutException();
        }
      }

    return msg;
    }
  
  public void resync()
    {
    Log4J.startMethod(logger, "resync");
    MessageC2SOutOfSync msg=new MessageC2SOutOfSync();
    netMan.addMessage(msg);
    Log4J.startMethod(logger, "resync");
    }

  /** Login to server using the given username and password.
   *  @param username Player username
   *  @param password Player password
   *  @return true if login is successful. */
  public boolean login(String username, String password) throws ariannexpTimeoutException
    {
    Log4J.startMethod(logger, "login");
    try
      {
      int received = 0;
      RSAPublicKey key = null;
      byte[] clientNonce = null;
      byte[] serverNonce = null;
      
      netMan.addMessage(new MessageC2SLoginRequestKey(null,getGameName(),getVersionNumber()));

      while(received < 3) 
        {
        Message msg;
        if(messages.size()>0)
          {
          msg=messages.remove(0);
          }
        else
          {
          msg=getMessage();
          }
       
        switch(msg.getType())
          {
          case S2C_LOGIN_SENDKEY:
            {
            logger.debug("Recieved Key");
            key = ((MessageS2CLoginSendKey)msg).getKey();
            
            clientNonce = Hash.random(Hash.hashLength());
            netMan.addMessage(new MessageC2SLoginSendPromise(msg.getAddress(), Hash.hash(clientNonce)));
            break;
            }
          case S2C_LOGIN_SENDNONCE:
            {
            logger.debug("Recieved Server Nonce");
            if(serverNonce != null) 
              {
              return false;
              }
              
            serverNonce = ((MessageS2CLoginSendNonce)msg).getHash();
            byte[] b1 = Hash.xor(clientNonce, serverNonce);
            if(b1 == null) 
              {
              return false;
              }
              
            byte[] b2 = Hash.xor(b1, Hash.hash(password));
            if(b2 == null) 
              {
              return false;
              }
              
            byte[] cryptedPassword = key.encodeByteArray(b2);
            netMan.addMessage(new MessageC2SLoginSendNonceNameAndPassword(msg.getAddress(), clientNonce, username, cryptedPassword));
            break;
            }
          case S2C_LOGIN_ACK:
            logger.debug("Login correct");
            received++;
            break;
          case S2C_CHARACTERLIST:
            logger.debug("Recieved Character list");
            String[] characters=((MessageS2CCharacterList)msg).getCharacters();
            onAvailableCharacters(characters);
            received++;
            break;
          case S2C_SERVERINFO:
            logger.debug("Recieved Server info");
            String[] info=((MessageS2CServerInfo)msg).getContents();
            onServerInfo(info);
            received++;
            break;
          case S2C_LOGIN_NACK:
            MessageS2CLoginNACK msgNACK=(MessageS2CLoginNACK)msg;
            logger.debug("Login failed. Reason: "+msgNACK.getResolution());
            event=msgNACK.getResolution();
            return false;
          default:
            messages.add(msg);
          }
        }
        return true;
      }
    catch(InvalidVersionException e)
      {
      event="Invalid client version to connect to this server.";
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    finally
      {
      Log4J.startMethod(logger, "login");
      }
    }
  
  private String event;
  public String getEvent()
    {
    return event;
    }

  /** After login allows you to choose a character to play
   *  @param character name of the character we want to play with.
   *  @return true if choosing character is successful. */
  public boolean chooseCharacter(String character) throws ariannexpTimeoutException
    {
    Log4J.startMethod(logger, "chooseCharacter");
    try
      {
      Message msgCC=new MessageC2SChooseCharacter(netMan.getAddress(),character);

      netMan.addMessage(msgCC);

      int recieved=0;

      while(recieved!=1)
        {
        Message msg=getMessage();
        switch(msg.getType())
          {
          case S2C_CHOOSECHARACTER_ACK:
            logger.debug("Choose Character ACK");
            return true;
          case S2C_CHOOSECHARACTER_NACK:
            logger.debug("Choose Character NACK");
            return false;
          default:
            messages.add(msg);
          }
        }

      return false;
      }
    catch(InvalidVersionException e)
      {
      logger.error("Invalid client version to connect to this server.",e);
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    finally
      {
      Log4J.finishMethod(logger, "chooseCharacter");
      }
    }

  /** Sends a RPAction to server */
  public void send(RPAction action)
    {
    try
      {
      send(action, false);
      }
    catch(ariannexpTimeoutException e)
      {
      /** This will never happen */
      }
    }

  /** Sends a RPAction to server and blocks until server confirms it. */
  public void send(RPAction action, boolean block) throws ariannexpTimeoutException
    {
    /** TODO: Useless we need to return something or disable blocking */
    Log4J.startMethod(logger, "send");
    try
      {
      MessageC2SAction msgAction=new MessageC2SAction(netMan.getAddress(),action);
      netMan.addMessage(msgAction);

      if(block)
        {
        int recieved=0;
        while(recieved!=1)
          {
          Message msg=getMessage();
          switch(msg.getType())
            {
            case S2C_ACTION_ACK:
              recieved++;
              break;
            default:
              messages.add(msg);
            }
          }
        }
      }
    catch(InvalidVersionException e)
      {
      logger.error("Invalid client version to connect to this server.",e);
      onError(1,"Invalid client version to connect to this server.");
      }
    finally
      {
      Log4J.finishMethod(logger, "send");
      }
    }

  /** Request logout of server
   *  @return true if we have successfully logout. */
  public boolean logout()
    {
    Log4J.startMethod(logger, "logout");

    try
      {
      Message msgL=new MessageC2SLogout(netMan.getAddress());

      netMan.addMessage(msgL);
      int recieved=0;

      while(recieved!=1)
        {
        Message msg=getMessage();
        switch(msg.getType())
          {
          case S2C_LOGOUT_ACK:
            logger.debug("Logout ACK");
            return true;
          case S2C_LOGOUT_NACK:
            logger.debug("Logout NACK");
            return false;
          default:
            messages.add(msg);
          }
        }

      return false;
      }
    catch(InvalidVersionException e)
      {
      logger.error("Invalid client version to connect to this server.",e);
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    catch(ariannexpTimeoutException e)
      {
      onError(1,"ariannexp can't connect to server. Server down?");
      return false;
      }
    finally
      {
      Log4J.finishMethod(logger, "logout");
      }
    }

  /** Call this method to get and apply messages */
  public void loop(int delta)
    {
    Log4J.startMethod(logger, "loop");

    try
      {
      Message newmsg=netMan.getMessage();
      if(newmsg!=null)
        {
        messages.add(newmsg);
        }

      for(Message msg: messages)
        {
        switch(msg.getType())
          {
          case S2C_PERCEPTION:
            {
            logger.debug("Processing Message Perception");
            MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
            netMan.addMessage(reply);

            MessageS2CPerception msgPer=(MessageS2CPerception)msg;
            onPerception(msgPer);

            break;
            }
      
          case S2C_TRANSFER_REQ:
            {
            logger.debug("Processing Content Transfer Request");
            List<TransferContent> items=((MessageS2CTransferREQ)msg).getContents();

            items=onTransferREQ(items);

            MessageC2STransferACK reply=new MessageC2STransferACK(msg.getAddress(),items);
            netMan.addMessage(reply);

            break;
            }
      
          case S2C_TRANSFER:
            {
            logger.debug("Processing Content Transfer");
            List<TransferContent> items=((MessageS2CTransfer)msg).getContents();
            onTransfer(items);
      
            break;
            }
          }
        }

      messages.clear();
      }
    catch(ConcurrentModificationException e)
      {
      logger.warn(e);
      }
    catch(InvalidVersionException e)
      {
      logger.error("Invalid client version to connect to this server.",e);
      onError(1,"Invalid client version to connect to this server.");
      }
    finally
      {
      Log4J.finishMethod(logger, "loop");
      }
    }
   
  /** It is called when a perception arrives so you can choose how to apply the perception */
  abstract protected void onPerception(MessageS2CPerception message);
  /** It is called on a transfer request so you can choose what items to approve or reject */
  abstract protected List<TransferContent> onTransferREQ(List<TransferContent> items);
  /** It is called when we get a transfer of content */
  abstract protected void onTransfer(List<TransferContent> items);
  /** It is called when we get the list of characters */
  abstract protected void onAvailableCharacters(String[] characters);
  /** It is called when we get the list of server information strings */
  abstract protected void onServerInfo(String[] info);
  /** It is called on error conditions so you can improve the handling of the error */
  abstract protected void onError(int code, String reason);

  abstract protected String getGameName();
  abstract protected String getVersionNumber();
  }

