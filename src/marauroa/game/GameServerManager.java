/* $Id: GameServerManager.java,v 1.39 2004/04/20 15:11:32 arianne_rpg Exp $ */
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
package marauroa.game;

import java.util.*;
import java.io.*;
import marauroa.net.*;
import marauroa.*;

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
  public GameServerManager(NetworkServerManager netMan)
    {
    super("GameServerManager");
    marauroad.trace("GameServerManager",">");    
    keepRunning=true;    
    this.netMan=netMan;
    playerContainer=PlayerEntryContainer.getContainer();    
    rpMan=new RPServerManager(netMan);
    stats=Statistics.getStatistics();
    start();
    marauroad.trace("GameServerManager","<");
    }

  /** Constructor that initialize also the RPManager 
   *  @param netMan a NetworkServerManager instance.
   *  @param rpMan a RPManager instance */
  public GameServerManager(NetworkServerManager netMan, RPServerManager rpMan)
    {
    super("GameServerManager");
    marauroad.trace("GameServerManager",">");    
    keepRunning=true;    
    this.netMan=netMan;
    this.rpMan=rpMan;
    playerContainer=PlayerEntryContainer.getContainer();    
    start();
    marauroad.trace("GameServerManager","<");
    }

  public void finish()
    {
    marauroad.trace("GameServerManager::finish",">");
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
    marauroad.trace("GameServerManager::finish","<");
    }
    
  public void run()
    {
    marauroad.trace("GameServerManager::run",">");
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
              marauroad.trace("GameServerManager::run","D","Processing C2S Login Message");
              processLoginEvent((MessageC2SLogin)msg);
              break;
            case Message.TYPE_C2S_CHOOSECHARACTER:
              marauroad.trace("GameServerManager::run","D","Processing C2S Choose Character Message");
              processChooseCharacterEvent((MessageC2SChooseCharacter)msg);
              break;
            case Message.TYPE_C2S_LOGOUT:
              marauroad.trace("GameServerManager::run","D","Processing C2S Logout Message");
              processLogoutEvent((MessageC2SLogout)msg);
              break;
            case Message.TYPE_C2S_ACTION:
              marauroad.trace("GameServerManager::run","D","Processing C2S Action Message");
              processActionEvent((MessageC2SAction)msg);
              break;
            case Message.TYPE_C2S_PERCEPTION_ACK:
              marauroad.trace("GameServerManager::run","D","Processing C2S Perception ACK Message");
              processPerceptionACKEvent((MessageC2SPerceptionACK)msg);
              break;
            default:
              marauroad.trace("GameServerManager::run","W","Unknown Message["+msg.getType()+"]");
              break;
            }
          playerContainer.getLock().releaseLock();
          }
        stats.setOnlinePlayers(playerContainer.size());
        }
      }
    finally
      {
      isfinished=true;
      marauroad.trace("GameServerManager::run","<");
      }
    }
  private static class ServerInfo
    {
    static private String typeGame;
    static private String name;
    static private String version;
    static private String contact;
    static
      {
      marauroad.trace("GameServerManager::ServerInfo::(static)",">");
      try
        {
        Configuration conf=Configuration.getConfiguration();
	    
        typeGame=conf.get("server_typeGame");
        name=conf.get("server_name");
        version=conf.get("server_version");
        contact=conf.get("server_contact");
        }
      catch(Exception e)
        {
        marauroad.trace("GameServerManager::ServerInfo::(static)","X",e.getMessage());
        marauroad.trace("GameServerManager::ServerInfo::(static)","!","ABORT: Unable to load Server info");
        System.exit(-1);
        }    
      finally
        {
        marauroad.trace("GameServerManager::ServerInfo::(static)","<");
        }
      }
    public static String[] get()
      {
      String[] result=new String[4];
      
      result[0]=typeGame;
      result[1]=name;
      result[2]=version;
      result[3]=contact;      
      return result;
      }
    }  
  private void processLoginEvent(MessageC2SLogin msg)
    {
    marauroad.trace("GameServerManager::processLoginEvent",">");
    try
      {
      if(playerContainer.hasRuntimePlayer(msg.getClientID()) || playerContainer.hasPlayer(msg.getUsername()))
        {
        /* Warning: Player is already logged. */
        marauroad.trace("GameServerManager::processLoginEvent","W","Client("+msg.getAddress().toString()+") trying to login twice");

        /* Notify player of the event: We send him/her a new ACK */
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
          marauroad.trace("GameServerManager::processLogoutEvent","D","Player trying to logout without choosing character");
          }
        playerContainer.removeRuntimePlayer(clientid);
        }
      if(playerContainer.size()==GameConst.MAX_NUMBER_PLAYERS)
        {
        /* Error: Too many clients logged on the server. */
        marauroad.trace("GameServerManager::processLoginEvent","W","Server is full, Client("+msg.getAddress().toString()+") can't login");
      
        /* Notify player of the event. */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.SERVER_IS_FULL);

        netMan.addMessage(msgLoginNACK);
        return;
        }
      if(playerContainer.verifyAccount(msg.getUsername(),msg.getPassword()))
        {
        marauroad.trace("GameServerManager::processLoginEvent","D","Correct username/password");

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
        marauroad.trace("GameServerManager::processLoginEvent","W","Incorrect username/password");
        stats.addPlayerInvalidLogin(msg.getUsername());
        playerContainer.addLoginEvent(msg.getUsername(),msg.getAddress(),false);

        /* Send player the Login NACK message */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.USERNAME_WRONG);

        netMan.addMessage(msgLoginNACK);
        }
      }
    catch(Exception e)      
      {
      marauroad.trace("GameServerManager::processLoginEvent","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("GameServerManager::processLoginEvent","<");      
      }
    }

  private void processChooseCharacterEvent(MessageC2SChooseCharacter msg)
    {
    marauroad.trace("GameServerManager::processChooseCharacterEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        marauroad.trace("GameServerManager::processChooseCharacterEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_LOGIN_COMPLETE)
        {
        /* Error: Player has not completed login yet, or he/she has logout already. */
        marauroad.trace("GameServerManager::processChooseCharacterEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        marauroad.trace("GameServerManager::processChooseCharacterEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;	    
        }
      if(playerContainer.hasCharacter(clientid,msg.getCharacter()))
        {
        marauroad.trace("GameServerManager::processChooseCharacterEvent","D","Client("+msg.getAddress().toString()+") has character("+msg.getCharacter()+")");
        /* We set the character in the runtime info */
        playerContainer.setChoosenCharacter(clientid,msg.getCharacter());

        /* We restore back the character to the world */
        RPObject object=playerContainer.getRPObject(clientid,msg.getCharacter());

        rpMan.onInit(object);
        playerContainer.changeRuntimeState(clientid,playerContainer.STATE_GAME_BEGIN);

        /* Correct: Character exist */
        MessageS2CChooseCharacterACK msgChooseCharacterACK=new MessageS2CChooseCharacterACK(msg.getAddress(),new RPObject.ID(object));

        msgChooseCharacterACK.setClientID(clientid);
        netMan.addMessage(msgChooseCharacterACK);
        }
      else
        {
        marauroad.trace("GameServerManager::processChooseCharacterEvent","W","Client("+msg.getAddress().toString()+") hasn't character("+msg.getCharacter()+")");
        playerContainer.changeRuntimeState(clientid,playerContainer.STATE_LOGIN_COMPLETE);

        /* Error: There is no such character */
        MessageS2CChooseCharacterNACK msgChooseCharacterNACK=new MessageS2CChooseCharacterNACK(msg.getAddress());

        msgChooseCharacterNACK.setClientID(clientid);
        netMan.addMessage(msgChooseCharacterNACK);
        }
      }
    catch(Exception e)      
      {
      marauroad.trace("GameServerManager::processChooseCharacterEvent","X",e.getMessage());
      }
    finally
      {    
      marauroad.trace("GameServerManager::processChooseCharacterEvent","<");
      }
    }

  private void processLogoutEvent(MessageC2SLogout msg)
    {
    marauroad.trace("GameServerManager::processLogoutEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        marauroad.trace("GameServerManager::processLogoutEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        marauroad.trace("GameServerManager::processLogoutEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
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
          marauroad.trace("GameServerManager::processLogoutEvent","X","Exception while storing character: "+e.getMessage());
          }
        }
      else
        {
        marauroad.trace("GameServerManager::processLogoutEvent","D","Player trying to logout without choosing character");
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
      marauroad.trace("GameServerManager::processLogoutEvent","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("GameServerManager::processLogoutEvent","<");
      }
    }
  static int lastActionIdGenerated=0;
  private void processActionEvent(MessageC2SAction msg)
    {
    marauroad.trace("GameServerManager::processActionEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        marauroad.trace("GameServerManager::processActionEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_GAME_BEGIN)
        {
        /* Error: Player has not choose a character yey. */
        marauroad.trace("GameServerManager::processActionEvent","W","Client("+msg.getAddress().toString()+") has not chose a character yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        marauroad.trace("GameServerManager::processActionEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
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

      action.put("source_id",id.getObjectID());
      if(action.has("type"))
        {
        stats.addActionsAdded(action.get("type"),clientid,action.toString());
        }
      else
        {
        stats.addActionsAdded(action.get("invalid"),clientid);
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
      marauroad.trace("GameServerManager::processActionEvent","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("GameServerManager::processActionEvent","<");
      }
    }       

  private void processPerceptionACKEvent(MessageC2SPerceptionACK msg)
    {
    marauroad.trace("GameServerManager::processPerceptionACKEvent",">");
    try
      {
      int clientid=msg.getClientID();
      
      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        marauroad.trace("GameServerManager::processPerceptionACKEvent","W","Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)!=playerContainer.STATE_GAME_BEGIN)
        {
        /* Error: Player has not choose a character yey. */
        marauroad.trace("GameServerManager::processPerceptionACKEvent","W","Client("+msg.getAddress().toString()+") has not chose a character yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        marauroad.trace("GameServerManager::processPerceptionACKEvent","E","Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;	    
        }
      playerContainer.updateTimestamp(clientid);
      }
    catch(Exception e)      
      {
      marauroad.trace("GameServerManager::processPerceptionACKEvent","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("GameServerManager::processPerceptionACKEvent","<");
      }
    }         
  }