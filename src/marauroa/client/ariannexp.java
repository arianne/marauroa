/* $Id: ariannexp.java,v 1.5 2005/03/07 19:36:31 arianne_rpg Exp $ */
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

import marauroa.client.net.*;
import marauroa.common.*;
import marauroa.common.net.*;
import marauroa.common.game.*;

public abstract class ariannexp
  {
  public final static long TIMEOUT=4000;
  private NetworkClientManager netMan;
  private int clientid;
  private List<Message> messages;
  
  public ariannexp(boolean logging)
    {
    if(logging)
      {
      Logger.initialize("base","client_log");
      }
    else
      {
      Logger.initialize();
      }
    
    Logger.trace("ariannexp::ariannexp",">");
    messages=new LinkedList<Message>();
    Logger.trace("ariannexp::ariannexp","<");
    }
  
  public void connect(String host, int port) throws java.net.SocketException
    {    
    Logger.trace("ariannexp::connect",">");
    netMan=new NetworkClientManager(host,port);
    Logger.trace("ariannexp::connect","<");
    }
  
  private Message getMessage() throws InvalidVersionException,ariannexpTimeoutException
    {
    Message msg=null;
    long delta=System.currentTimeMillis();
    
    while(msg==null)
      {
      msg=netMan.getMessage();    
      if(System.currentTimeMillis()-delta>TIMEOUT)
        {
        throw new ariannexpTimeoutException();
        }
      }
    return msg;     
    }
  
  public boolean login(String username, String password) throws ariannexpTimeoutException
    {
    Logger.trace("ariannexp::login",">");
    try
      {
      netMan.addMessage(new MessageC2SLogin(netMan.getAddress(),username,password));
      int recieved=0;

      while(recieved!=3)
        {
        Message msg=getMessage();
        if(msg instanceof MessageS2CLoginACK)
          {
          Logger.trace("ariannexp::login","D","Login correct");
          clientid=msg.getClientID();
          ++recieved;
          }
        else if(msg instanceof MessageS2CCharacterList)
          {
          Logger.trace("ariannexp::login","D","Recieved Character list");
          String[] characters=((MessageS2CCharacterList)msg).getCharacters();
          onAvailableCharacters(characters);
          ++recieved;
          }
        else if(msg instanceof MessageS2CServerInfo)
          {
          Logger.trace("ariannexp::login","D","Recieved Server info");
          String[] info=((MessageS2CServerInfo)msg).getContents();
          onServerInfo(info);
          ++recieved;
          }
        else if(msg instanceof MessageS2CLoginNACK)
          {
          Logger.trace("ariannexp::login","D","Login failed");
          return false;
          }
        else
          {
          messages.add(msg);
          }
        }
      
      return true;
      }
    catch(InvalidVersionException e)
      {
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    finally
      {
      Logger.trace("ariannexp::login","<");
      }
    }
  
  public boolean chooseCharacter(String character) throws ariannexpTimeoutException
    {
    Logger.trace("ariannexp::chooseCharacter",">");
    try
      {
      Message msgCC=new MessageC2SChooseCharacter(netMan.getAddress(),character);

      msgCC.setClientID(clientid);
      netMan.addMessage(msgCC);

      int recieved=0;

      while(recieved!=1)
        {
        Message msg=getMessage();
        if(msg instanceof MessageS2CChooseCharacterACK)
          {
          Logger.trace("ariannexp::chooseCharacter","D","Choose Character ACK");
          return true;
          }
        else if(msg instanceof MessageS2CChooseCharacterNACK)
          {
          Logger.trace("ariannexp::chooseCharacter","D","Choose Character NACK");
          return false;
          }
        else
          {
          messages.add(msg);
          }        
        }
      
      return false;
      }
    catch(InvalidVersionException e)
      {
      Logger.thrown("ariannexp::chooseCharacter","X",e);
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    finally
      {
      Logger.trace("ariannexp::chooseCharacter","<");
      }
    }
  
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
  
  public void send(RPAction action, boolean block) throws ariannexpTimeoutException
    {
    /** TODO: Useless we need to return something or disable blocking */
    Logger.trace("ariannexp::send",">");
    try
      {
      MessageC2SAction msgAction=new MessageC2SAction(netMan.getAddress(),action);
      msgAction.setClientID(clientid);
      netMan.addMessage(msgAction);
      
      if(block)
        {
        int recieved=0;
        while(recieved!=1)
          {
          Message msg=getMessage();
          if(msg instanceof MessageS2CActionACK)
            {
            recieved++;
            }
          else
            {
            messages.add(msg);
            }        
          }        
        }
      }
    catch(InvalidVersionException e)
      {
      Logger.thrown("ariannexp::send","X",e);
      onError(1,"Invalid client version to connect to this server.");
      }
    finally
      {
      Logger.trace("ariannexp::send","<");
      }
    }
  
  public boolean logout()
    {
    Logger.trace("ariannexp::logout",">");
    
    try
      {
      Message msgL=new MessageC2SLogout(netMan.getAddress());

      msgL.setClientID(clientid);
      netMan.addMessage(msgL);
      int recieved=0;

      while(recieved!=1)
        {
        Message msg=getMessage();
        if(msg instanceof MessageS2CLogoutACK)
          {
          Logger.trace("ariannexp::logout","D","Logout ACK");
          return true;
          }
        else if(msg instanceof MessageS2CLogoutNACK)
          {
          Logger.trace("ariannexp::logout","D","Logout NACK");
          return false;
          }
        else
          {
          messages.add(msg);
          }        
        }
      
      return false;
      }
    catch(InvalidVersionException e)
      {
      Logger.thrown("ariannexp::logout","X",e);
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
      Logger.trace("ariannexp::logout","<");
      }
    }
  
  public void loop(int delta)
    {
    Logger.trace("ariannexp::loop",">");
    
    try
      {
      Message newmsg=netMan.getMessage();
      if(newmsg!=null)
        {
        messages.add(newmsg);
        }
      
      for(Message msg: messages)    
        {
        if(msg instanceof MessageS2CPerception)
          {
          Logger.trace("ariannexp::loop","D","Processing Message Perception");
          MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          
          MessageS2CPerception msgPer=(MessageS2CPerception)msg;
          onPerception(msgPer);
          }
        else if(msg instanceof MessageS2CTransferREQ)        
          {
          Logger.trace("ariannexp::loop","D","Processing Content Transfer Request");
          List<TransferContent> items=((MessageS2CTransferREQ)msg).getContents();

          items=onTransferREQ(items);
          
          MessageC2STransferACK reply=new MessageC2STransferACK(msg.getAddress(),items);
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          }
        else if(msg instanceof MessageS2CTransfer)        
          {
          Logger.trace("ariannexp::loop","D","Processing Content Transfer");
          List<TransferContent> items=((MessageS2CTransfer)msg).getContents();
          onTransfer(items);
          }
        }
      
      messages.clear();
      }
    catch(InvalidVersionException e)
      {      
      Logger.thrown("ariannexp::loop","X",e);
      onError(1,"Invalid client version to connect to this server.");
      }
    finally
      {
      Logger.trace("ariannexp::loop","<");
      }
    }

  abstract protected void onPerception(MessageS2CPerception message);
  abstract protected List<TransferContent> onTransferREQ(List<TransferContent> items);
  abstract protected void onTransfer(List<TransferContent> items);
  abstract protected void onAvailableCharacters(String[] characters);  
  abstract protected void onServerInfo(String[] info);
  abstract protected void onError(int code, String reason);
  }

