/* $Id: ariannexp.java,v 1.2 2005/02/09 20:22:27 arianne_rpg Exp $ */
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
  private NetworkClientManager netMan;
  private int clientid;
  private List<Message> messages;
  
  public ariannexp()
    {
    messages=new LinkedList<Message>();
    }
  
  public void connect(String host, int port) throws java.net.SocketException
    {    
    netMan=new NetworkClientManager(host,port);
    }
  
  private Message getMessage() throws InvalidVersionException
    {
    Message msg=null;
    while(msg==null) msg=netMan.getMessage();    
    return msg;     
    }
  
  public boolean login(String username, String password)
    {
    try
      {
      netMan.addMessage(new MessageC2SLogin(netMan.getAddress(),username,password));
      int recieved=0;

      while(recieved!=3)
        {
        Message msg=getMessage();
        if(msg instanceof MessageS2CLoginACK)
          {
          clientid=msg.getClientID();
          ++recieved;
          }
        else if(msg instanceof MessageS2CCharacterList)
          {
          String[] characters=((MessageS2CCharacterList)msg).getCharacters();
          onAvailableCharacters(characters);
          ++recieved;
          }
        else if(msg instanceof MessageS2CServerInfo)
          {
          String[] info=((MessageS2CServerInfo)msg).getContents();
          onServerInfo(info);
          ++recieved;
          }
        else if(msg instanceof MessageS2CLoginNACK)
          {
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
    }
  
  public boolean chooseCharacter(String character)
    {
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
          return true;
          }
        else if(msg instanceof MessageS2CChooseCharacterNACK)
          {
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
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    }
  
  public void send(RPAction action)
    {
    send(action, false);
    }
  
  public void send(RPAction action, boolean block)
    {
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
      onError(1,"Invalid client version to connect to this server.");
      }
    }
  
  public boolean logout()
    {
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
          return true;
          }
        else if(msg instanceof MessageS2CLogoutNACK)
          {
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
      onError(1,"Invalid client version to connect to this server.");
      return false;
      }
    }
  
  public void loop(int delta)
    {
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
          MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          
          MessageS2CPerception msgPer=(MessageS2CPerception)msg;
          onPerception(msgPer);
          }
        else if(msg instanceof MessageS2CTransferREQ)        
          {
          List<TransferContent> items=((MessageS2CTransferREQ)msg).getContents();

          items=onTransferREQ(items);
          
          MessageC2STransferACK reply=new MessageC2STransferACK(msg.getAddress(),items);
          reply.setClientID(clientid);
          netMan.addMessage(reply);
          }
        else if(msg instanceof MessageS2CTransfer)        
          {
          List<TransferContent> items=((MessageS2CTransfer)msg).getContents();
          onTransfer(items);
          }
        }
      
      messages.clear();
      }
    catch(InvalidVersionException e)
      {      
      onError(1,"Invalid client version to connect to this server.");
      }
    }

  abstract protected void onPerception(MessageS2CPerception message);
  abstract protected List<TransferContent> onTransferREQ(List<TransferContent> items);
  abstract protected void onTransfer(List<TransferContent> items);
  abstract protected void onAvailableCharacters(String[] characters);  
  abstract protected void onServerInfo(String[] info);
  abstract protected void onError(int code, String reason);
  }

