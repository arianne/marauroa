/* $Id: GameServerManager.java,v 1.4 2005/03/18 07:49:05 arianne_rpg Exp $ */
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
package marauroa.server.game;

import java.util.*;
import java.io.*;
import marauroa.server.net.*;
import marauroa.common.*;
import marauroa.common.net.*;
import marauroa.common.game.*;
import marauroa.server.*;


/** The GameServerManager is a active entity of the marauroa.game package,
 *  it is in charge of processing all the messages and modify PlayerEntry Container accordingly. */
public final class GameServerManager extends Thread
  {
  private NetworkServerManager netMan;
  private RPServerManager rpMan;
  private PlayerEntryContainer playerContainer;
  private Statistics stats;
  
  /** The thread will be running while keepRunning is true */
  private boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;
  
  /** Constructor that initialize also the RPManager
   *  @param netMan a NetworkServerManager instance. */
  public GameServerManager(NetworkServerManager netMan, RPServerManager rpMan) throws Exception
    {
    super("GameServerManager");
    Logger.trace("GameServerManager",">");
    keepRunning=true;
    this.netMan=netMan;
    this.rpMan=rpMan;
    playerContainer=PlayerEntryContainer.getContainer();
    stats=Statistics.getStatistics();
    start();
    Logger.trace("GameServerManager","<");
    }

  public void finish()
    {
    Logger.trace("GameServerManager::finish",">");
    rpMan.finish();
    keepRunning=false;
    while(isfinished==false)
      {
      try
        {
        Thread.sleep(1000);
        }
      catch(java.lang.InterruptedException e)
        {
        }
      }
    Logger.trace("GameServerManager::finish","<");
    }
    
  public void run()
    {
    Logger.trace("GameServerManager::run",">");
    try
      {
      while(keepRunning)
        {
        Message msg=netMan.getMessage(TimeoutConf.GAMESERVER_MESSAGE_GET_TIMEOUT);
       
        if(msg!=null)
          {
          playerContainer.getLock().requestWriteLock();
          switch(msg.getType())
            {
            case Message.TYPE_C2S_LOGIN:
              Logger.trace("GameServerManager::run","D","Processing C2S Login Message");
              processLoginEvent((MessageC2SLogin)msg);
              break;
            case Message.TYPE_C2S_CHOOSECHARACTER:
              Logger.trace("GameServerManager::run","D","Processing C2S Choose Character Message");
              processChooseCharacterEvent((MessageC2SChooseCharacter)msg);
              break;
            case Message.TYPE_C2S_LOGOUT:
              Logger.trace("GameServerManager::run","D","Processing C2S Logout Message");
              processLogoutEvent((MessageC2SLogout)msg);
              break;
            case Message.TYPE_C2S_ACTION:
              Logger.trace("GameServerManager::run","D","Processing C2S Action Message");
              processActionEvent((MessageC2SAction)msg);
              break;
            case Message.TYPE_C2S_PERCEPTION_ACK:
              Logger.trace("GameServerManager::run","D","Processing C2S Perception ACK Message");
              processPerceptionACKEvent((MessageC2SPerceptionACK)msg);
              break;
            case Message.TYPE_C2S_OUTOFSYNC:
              Logger.trace("GameServerManager::run","D","Processing C2S Out Of Sync Message");
              processOutOfSyncEvent((MessageC2SOutOfSync)msg);
              break;
            case Message.TYPE_C2S_TRANSFER_ACK:
              Logger.trace("GameServerManager::run","D","Processing C2S Transfer ACK Message");
              processTransferACK((MessageC2STransferACK)msg);
              break;
            default:
              Logger.trace("GameServerManager::run","W","Unknown Message["+msg.getType()+"]");
              break;
            }
          playerContainer.getLock().releaseLock();
          }
        stats.setOnlinePlayers(playerContainer.size());
        }
      }
    catch(Throwable e)
      {
      Logger.trace("GameServerManager::run", "!", "Unhandled exception, server will shut down.");
      Logger.thrown("GameServerManager::run", "!", e);
      }
    finally
      {
      isfinished=true;
      Logger.trace("GameServerManager::run","<");
      }
    }
    
  private static class ServerInfo
    {
    static Configuration config;
    static
      {
      Logger.trace("GameServerManager::ServerInfo::(static)",">");
      try
        {
        config=Configuration.getConfiguration();
        //just check if mandatory properties are set
    	config.get("server_typeGame");
	    config.get("server_name");
	    config.get("server_version");
	    config.get("server_contact");
        }
      catch(Exception e)
        {
        Logger.trace("GameServerManager::ServerInfo::(static)","!","ABORT: Unable to load Server info");
        Logger.thrown("GameServerManager::ServerInfo::(static)","X",e);
        }
      finally
        {
        Logger.trace("GameServerManager::ServerInfo::(static)","<");
        }
      }
      
    public static String[] get()
      {
      List<String> l_result = new ArrayList<String>();
      
      Enumeration props = config.propertyNames();
      while(props.hasMoreElements())
        {
        String prop_name = String.valueOf(props.nextElement());
        if(prop_name.startsWith("server_"))
	      {
	      try
	        {
	        l_result.add(config.get(prop_name));
	        }
	      catch(PropertyNotFoundException pnfe)
	        {
	        //cant be. only in multithreaded emvironment possible
	        Logger.trace("GameServerManager::ServerInfo::get","!","Property "+prop_name+" is not set???");
	        }
	      }
        }
      String[] result = new String[l_result.size()];
      return (String[])l_result.toArray(result);
      }
    }
    
  private void processLoginEvent(MessageC2SLogin msg)
    {
    Logger.trace("GameServerManager::processLoginEvent",">");
    try
      {
      /** NOTE: We need to avoid that another player sends a fake login to login a player. */
      boolean tryingToLoginAgain=(playerContainer.hasPlayer(msg.getUsername()) && playerContainer.verifyAccount(msg.getUsername(),msg.getPassword()));
      if(playerContainer.hasRuntimePlayer(msg.getClientID()) || tryingToLoginAgain)
        {
        /* Warning: Player is already logged. */
        Logger.trace("GameServerManager::processLoginEvent","W","Client("+msg.getAddress().toString()+") trying to login twice");

        int clientid=playerContainer.getClientidPlayer(msg.getUsername());
        
        if(playerContainer.getRuntimeState(clientid)==PlayerEntryContainer.STATE_GAME_BEGIN)
          {
          RPObject.ID id=playerContainer.getRPObjectID(clientid);
          RPObject object=rpMan.getRPObject(id);

          if(rpMan.onExit(id))
            {
            /* NOTE: Set the Object so that it is stored in Database */
            playerContainer.setRPObject(clientid,object);
            }
          }
        else
          {
          Logger.trace("GameServerManager::processLoginEvent","D","Player trying to logout without choosing character");
          }
          
        playerContainer.removeRuntimePlayer(clientid);
        }
        
      if(playerContainer.size()==GameConst.MAX_NUMBER_PLAYERS)
        {
        /* Error: Too many clients logged on the server. */
        Logger.trace("GameServerManager::processLoginEvent","W","Server is full, Client("+msg.getAddress().toString()+") can't login");
      
        /* Notify player of the event. */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.SERVER_IS_FULL);

        netMan.addMessage(msgLoginNACK);
        return;
        }
        
      if(playerContainer.verifyAccount(msg.getUsername(),msg.getPassword()))
        {
        Logger.trace("GameServerManager::processLoginEvent","D","Correct username/password");

        /* Correct: The login is correct */
        int clientid=playerContainer.addRuntimePlayer(msg.getUsername(),msg.getAddress());

        playerContainer.addLoginEvent(msg.getUsername(),msg.getAddress(),true);
        stats.addPlayerLogin(msg.getUsername(),clientid);
          
        /* Send player the Login ACK message */
        MessageS2CLoginACK msgLoginACK=new MessageS2CLoginACK(msg.getAddress());

        msgLoginACK.setClientID(clientid);
        netMan.addMessage(msgLoginACK);

        /* Send player the ServerInfo */
        MessageS2CServerInfo msgServerInfo=new MessageS2CServerInfo(msg.getAddress(),ServerInfo.get());

        msgServerInfo.setClientID(clientid);
        netMan.addMessage(msgServerInfo);
          
        /* Build player character list and send it to client */
        String[] characters=playerContainer.getCharacterList(clientid);
        MessageS2CCharacterList msgCharacters=new MessageS2CCharacterList(msg.getAddress(),characters);

        msgCharacters.setClientID(clientid);
        netMan.addMessage(msgCharacters);
        playerContainer.changeRuntimeState(clientid,PlayerEntryContainer.STATE_LOGIN_COMPLETE);
        }
      else
        {
        Logger.trace("GameServerManager::processLoginEvent","W","Incorrect username/password");
        stats.addPlayerInvalidLogin(msg.getUsername());
        playerContainer.addLoginEvent(msg.getUsername(),msg.getAddress(),false);

        /* Send player the Login NACK message */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.USERNAME_WRONG);

        netMan.addMessage(msgLoginNACK);
        }
      }
    catch(Exception e)
      {
      Logger.trace("GameServerManager::processLoginEvent","X",e.getMessage());
      Logger.thrown("GameServerManager::processLoginEvent","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processLoginEvent","<");
      }
    }

  private void processChooseCharacterEvent(MessageC2SChooseCharacter msg)
    {
    Logger.trace("GameServerManager::processChooseCharacterEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        Logger.trace("GameServerManager::processChooseCharacterEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_LOGIN_COMPLETE)
        {
        /* Error: Player has not completed login yet, or he/she has logout already. */
        Logger.trace("GameServerManager::processChooseCharacterEvent","W","Client("+msg.getAddress().toString()+") has not complete login yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        Logger.trace("GameServerManager::processChooseCharacterEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }
      if(playerContainer.hasCharacter(clientid,msg.getCharacter()))
        {
        Logger.trace("GameServerManager::processChooseCharacterEvent","D","Client("+msg.getAddress().toString()+") has character("+msg.getCharacter()+")");
        /* We set the character in the runtime info */
        playerContainer.setChoosenCharacter(clientid,msg.getCharacter());

        /* We restore back the character to the world */
        RPObject object=playerContainer.getRPObject(clientid,msg.getCharacter());
        
        /* We set the clientid attribute to link easily the object with is player runtime information */
        object.put("clientid",clientid);

        rpMan.onInit(object);
        playerContainer.changeRuntimeState(clientid,playerContainer.STATE_GAME_BEGIN);

        /* Correct: Character exist */
        MessageS2CChooseCharacterACK msgChooseCharacterACK=new MessageS2CChooseCharacterACK(msg.getAddress());
        msgChooseCharacterACK.setClientID(clientid);
        netMan.addMessage(msgChooseCharacterACK);
        }
      else
        {
        Logger.trace("GameServerManager::processChooseCharacterEvent","W","Client("+msg.getAddress().toString()+") hasn't character("+msg.getCharacter()+")");
        playerContainer.changeRuntimeState(clientid,playerContainer.STATE_LOGIN_COMPLETE);

        /* Error: There is no such character */
        MessageS2CChooseCharacterNACK msgChooseCharacterNACK=new MessageS2CChooseCharacterNACK(msg.getAddress());

        msgChooseCharacterNACK.setClientID(clientid);
        netMan.addMessage(msgChooseCharacterNACK);
        }
      }
    catch(Exception e)
      {
      Logger.thrown("GameServerManager::processChooseCharacterEvent","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processChooseCharacterEvent","<");
      }
    }

  private void processLogoutEvent(MessageC2SLogout msg)
    {
    Logger.trace("GameServerManager::processLogoutEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        Logger.trace("GameServerManager::processLogoutEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        Logger.trace("GameServerManager::processLogoutEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)==PlayerEntryContainer.STATE_GAME_BEGIN)
        {
        try
          {
          RPObject.ID id=playerContainer.getRPObjectID(clientid);
          RPObject object=rpMan.getRPObject(id);

          if(rpMan.onExit(id))
            {
            /* NOTE: Set the Object so that it is stored in Database */
            playerContainer.setRPObject(clientid,object);
            }
          }
        catch(Exception e)
          {
          Logger.trace("GameServerManager::processLogoutEvent","X","Exception while storing character: "+e.getMessage());
          Logger.thrown("GameServerManager::processLogoutEvent","X",e);
          }
        }
      else
        {
        Logger.trace("GameServerManager::processLogoutEvent","D","Player trying to logout without choosing character");
        }
        
      stats.addPlayerLogout(playerContainer.getUsername(clientid),clientid);
      playerContainer.removeRuntimePlayer(clientid);
      
      /* Send Logout ACK message */
      MessageS2CLogoutACK msgLogout=new MessageS2CLogoutACK(msg.getAddress());

      msgLogout.setClientID(clientid);
      netMan.addMessage(msgLogout);
      }
    catch(Exception e)
      {
      Logger.thrown("GameServerManager::processLogoutEvent","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processLogoutEvent","<");
      }
    }
    
  static int lastActionIdGenerated=0;
  private void processActionEvent(MessageC2SAction msg)
    {
    Logger.trace("GameServerManager::processActionEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        Logger.trace("GameServerManager::processActionEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_GAME_BEGIN)
        {
        /* Error: Player has not choose a character yey. */
        Logger.trace("GameServerManager::processActionEvent","W","Client("+msg.getAddress().toString()+") has not chose a character yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        Logger.trace("GameServerManager::processActionEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }
      
      /* Send the action to RP Manager */
      RPAction action=msg.getRPAction();
      
      if(!action.has("action_id"))
        {
        action.put("action_id",++lastActionIdGenerated);
        }
      
      /* Enforce source_id and action_id*/
      RPObject.ID id=playerContainer.getRPObjectID(clientid);

      action.put("sourceid",id.getObjectID());
      action.put("zoneid",id.getZoneID());
      action.put("when",rpMan.getTurn());
      
      if(action.has("type"))
        {
        stats.addActionsAdded(action.get("type"),clientid,action.toString());
        }
      else
        {
        stats.addActionsAdded("invalid",clientid);
        }
      rpMan.addRPAction(action);

      /* Notify client that we recieved the action */
      MessageS2CActionACK msgAction=new MessageS2CActionACK(msg.getAddress(),action.getInt("action_id"));

      msgAction.setClientID(clientid);
      netMan.addMessage(msgAction);
      }
    catch(Exception e)
      {
      stats.addActionsInvalid();
      Logger.thrown("GameServerManager::processActionEvent","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processActionEvent","<");
      }
    }

  private void processPerceptionACKEvent(MessageC2SPerceptionACK msg)
    {
    Logger.trace("GameServerManager::processPerceptionACKEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        Logger.trace("GameServerManager::processPerceptionACKEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_GAME_BEGIN)
        {
        /* Error: Player has not choose a character yey. */
        Logger.trace("GameServerManager::processPerceptionACKEvent","W","Client("+msg.getAddress().toString()+") has not chose a character yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        Logger.trace("GameServerManager::processPerceptionACKEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }

      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      entry.timestamp=System.currentTimeMillis();  
      }
    catch(Exception e)
      {
      Logger.thrown("GameServerManager::processPerceptionACKEvent","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processPerceptionACKEvent","<");
      }
    }


  private void processOutOfSyncEvent(MessageC2SOutOfSync msg)
    {
    Logger.trace("GameServerManager::processOutOfSyncEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        Logger.trace("GameServerManager::processOutOfSyncEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_GAME_BEGIN)
        {
        /* Error: Player has not choose a character yey. */
        Logger.trace("GameServerManager::processOutOfSyncEvent","W","Client("+msg.getAddress().toString()+") has not chose a character yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        Logger.trace("GameServerManager::processOutOfSyncEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }
      
      /** Notify PlayerEntryContainer that this player is out of Sync */  
      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      entry.perception_OutOfSync=true;
      }
    catch(Exception e)
      {
      Logger.thrown("GameServerManager::processOutOfSyncEvent","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processOutOfSyncEvent","<");
      }
    }
  
  private void processTransferACK(MessageC2STransferACK msg)
    {
    Logger.trace("GameServerManager::processTransferACK",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        Logger.trace("GameServerManager::processTransferACK","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_GAME_BEGIN)
        {
        /* Error: Player has not choose a character yey. */
        Logger.trace("GameServerManager::processTransferACK","W","Client("+msg.getAddress().toString()+") has not chose a character yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        Logger.trace("GameServerManager::processTransferACK","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }
      
      /** Handle Transfer ACK here */  
      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      for(TransferContent content: msg.getContents())
        {
        if(content.ack==true)        
          {
          Logger.trace("GameServerManager::processTransferACK","D","Trying transfer content "+content);
          content=entry.getContent(content.name);          
          if(content!=null)
            {
            Logger.trace("GameServerManager::processTransferACK","D","Transfering content "+content);
            MessageS2CTransfer msgTransfer=new MessageS2CTransfer(entry.source, content);
            msgTransfer.setClientID(clientid);
            netMan.addMessage(msgTransfer);
            }
          else
            {
            Logger.trace("GameServerManager::processTransferACK","D","CAN'T transfer content "+content);
            }
          }
        }
        
      entry.clearContent();      
      }
    catch(Exception e)
      {
      Logger.thrown("GameServerManager::processTransferACK","X",e);
      }
    finally
      {
      Logger.trace("GameServerManager::processTransferACK","<");
      }
    }
  }
