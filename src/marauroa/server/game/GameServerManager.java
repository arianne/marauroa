/* $Id: GameServerManager.java,v 1.22 2006/01/10 22:04:37 arianne_rpg Exp $ */
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.PropertyNotFoundException;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.*;
import marauroa.server.net.NetworkServerManager;
import org.apache.log4j.Logger;

/** The GameServerManager is a active entity of the marauroa.game package,
 *  it is in charge of processing all the messages and modify PlayerEntry Container accordingly. */
public final class GameServerManager extends Thread
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(GameServerManager.class);
  
  private NetworkServerManager netMan;
  private RPServerManager rpMan;
  private PlayerEntryContainer playerContainer;
  private Statistics stats;
  private RSAKey key;

  /** The thread will be running while keepRunning is true */
  private boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;

  /** Constructor that initialize also the RPManager
   *  @param netMan a NetworkServerManager instance. */
  public GameServerManager(RSAKey key, NetworkServerManager netMan, RPServerManager rpMan) throws Exception
    {
    super("GameServerManager");
    Log4J.startMethod(logger,"GameServerManager");
    keepRunning=true;
    this.key = key;
    this.netMan=netMan;
    this.rpMan=rpMan;
    playerContainer=PlayerEntryContainer.getContainer();
    stats=Statistics.getStatistics();
    start();
    Log4J.finishMethod(logger,"GameServerManager");
    }

  public void finish()
    {
    Log4J.startMethod(logger,"finish");
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
    
    PlayerEntryContainer.ClientIDIterator it=playerContainer.iterator();
    while(it.hasNext())
      {
      try
        {
        int clientid=it.next();
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
        logger.error("GameServerManager::finish",e);
        }
      }
    
    Log4J.finishMethod(logger,"finish");
    }

  public void run()
    {
    Log4J.startMethod(logger,"run");
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
            case C2S_LOGIN_REQUESTKEY:
              logger.debug("Processing C2S Login Request Key Message");
              processLoginRequestKey(msg);
              break;
            case C2S_LOGIN_SENDPROMISE:
              logger.debug("Processing C2S Login Send Promise Message");
              processLoginSendPromise(msg);
              break;
            case C2S_LOGIN_SENDNONCENAMEANDPASSWORD:
              logger.debug("Processing C2S Secured Login Message");
              processSecuredLoginEvent(msg);
              break;
            case C2S_CHOOSECHARACTER:
              logger.debug("Processing C2S Choose Character Message");
              processChooseCharacterEvent((MessageC2SChooseCharacter)msg);
              break;
            case C2S_LOGOUT:
              logger.debug("Processing C2S Logout Message");
              processLogoutEvent((MessageC2SLogout)msg);
              break;
            case C2S_ACTION:
              logger.debug("Processing C2S Action Message");
              processActionEvent((MessageC2SAction)msg);
              break;
            case C2S_PERCEPTION_ACK:
              logger.debug("Processing C2S Perception ACK Message");
              processPerceptionACKEvent((MessageC2SPerceptionACK)msg);
              break;
            case C2S_OUTOFSYNC:
              logger.debug("Processing C2S Out Of Sync Message");
              processOutOfSyncEvent((MessageC2SOutOfSync)msg);
              break;
            case C2S_TRANSFER_ACK:
              logger.debug("Processing C2S Transfer ACK Message");
              processTransferACK((MessageC2STransferACK)msg);
              break;
            case C2S_CREATEACCOUNT:
              logger.debug("Processing C2S Create Account Message");
              processCreateAccount((MessageC2SCreateAccount)msg);
              break;
            default:
              logger.debug("Unknown Message["+msg.getType()+"]");
              break;
            }
          playerContainer.getLock().releaseLock();
          }
        stats.set("Players online",playerContainer.size());
        }
      }
    catch(Throwable e)
      {
      logger.fatal("Unhandled exception, server will shut down.",e);
      }

    isfinished=true;
    Log4J.finishMethod(logger,"run");
    }

  /**
   * This checks if the message is valid to trigger the event. 
   * The player has to:
   * <ul>
   *  <li>Be known to the Server (logged in)</li>
   *  <li>Completed the login procedure</li>
   *  <li>Must have the correct IP<->clientid relation </li>
   * </ul>
   *
   * @param msg the message to check
   * @return true, the event is valid, else false
   */
  private boolean isValidEvent(Message msg, PlayerEntryContainer.ClientState state) throws NoSuchClientIDException
  {
    int clientid = msg.getClientID();
    if(!playerContainer.hasRuntimePlayer(clientid))
      {
      /* Error: Player didn't login. */
      logger.debug("Client("+msg.getAddress().toString()+") has not login yet");
      return false;
      }
    if(playerContainer.getRuntimeState(clientid) != state)
      {
      /* Error: Player has not completed login yet, or he/she has logout already. */
      logger.debug("Client("+msg.getAddress().toString()+") is not in the required state ("+state+")");
      return false;
      }
    if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
      {
      /* Error: Player has not correct IP<->clientid relation */
      logger.debug("Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
      return false;
      }
    return true;
  }
  
  
  private void processChooseCharacterEvent(MessageC2SChooseCharacter msg)
    {
    Log4J.startMethod(logger,"processChooseCharacterEvent");
    try
      {
      int clientid=msg.getClientID();

      // verify event
      if (!isValidEvent(msg, PlayerEntryContainer.ClientState.LOGIN_COMPLETE))
        return;

      if(playerContainer.hasCharacter(clientid,msg.getCharacter()))
        {
        logger.debug("Client("+msg.getAddress().toString()+") has character("+msg.getCharacter()+")");
        /* We set the character in the runtime info */
        playerContainer.setChoosenCharacter(clientid,msg.getCharacter());

        /* We restore back the character to the world */
        RPObject object=playerContainer.getRPObject(clientid,msg.getCharacter());

        /* We set the clientid attribute to link easily the object with is player runtime information */
        object.put("clientid",clientid);

        rpMan.onInit(object);
        playerContainer.changeRuntimeState(clientid,PlayerEntryContainer.ClientState.GAME_BEGIN);

        /* Correct: Character exist */
        MessageS2CChooseCharacterACK msgChooseCharacterACK=new MessageS2CChooseCharacterACK(msg.getAddress());
        msgChooseCharacterACK.setClientID(clientid);
        netMan.addMessage(msgChooseCharacterACK);
        }
      else
        {
        logger.debug("Client("+msg.getAddress().toString()+") hasn't character("+msg.getCharacter()+")");
        playerContainer.changeRuntimeState(clientid,PlayerEntryContainer.ClientState.LOGIN_COMPLETE);

        /* Error: There is no such character */
        MessageS2CChooseCharacterNACK msgChooseCharacterNACK=new MessageS2CChooseCharacterNACK(msg.getAddress());

        msgChooseCharacterNACK.setClientID(clientid);
        netMan.addMessage(msgChooseCharacterNACK);
        }
      }
    catch(Exception e)
      {
      logger.error("error when processing character event",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processChooseCharacterEvent");
      }
    }

  private void processLogoutEvent(MessageC2SLogout msg)
    {
    Log4J.startMethod(logger,"processLogoutEvent");
    try
      {
      int clientid=msg.getClientID();

      if(!playerContainer.hasRuntimePlayer(clientid))
        {
        /* Error: Player didn't login. */
        logger.debug("Client("+msg.getAddress().toString()+") has not login yet");
        return;
        }
      if(!playerContainer.verifyRuntimePlayer(clientid,msg.getAddress()))
        {
        /* Error: Player has not correct IP<->clientid relation */
        logger.debug("Client("+msg.getAddress().toString()+") has not correct IP<->clientid relation");
        return;
        }
      if(playerContainer.getRuntimeState(clientid)==PlayerEntryContainer.ClientState.GAME_BEGIN)
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
          logger.error("Exception while storing character",e);
          }
        }
      else
        {
        logger.debug("Player trying to logout without choosing character");
        }

      stats.add("Players logout",1);
      playerContainer.removeRuntimePlayer(clientid);

      /* Send Logout ACK message */
      MessageS2CLogoutACK msgLogout=new MessageS2CLogoutACK(msg.getAddress());

      msgLogout.setClientID(clientid);
      netMan.addMessage(msgLogout);
      }
    catch(Exception e)
      {
      logger.error("error while processing LogoutEvent",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processLogoutEvent");
      }
    }

  static int lastActionIdGenerated=0;
  private void processActionEvent(MessageC2SAction msg)
    {
    Log4J.startMethod(logger,"processActionEvent");
    try
      {
      int clientid=msg.getClientID();

      // verify event
      if (!isValidEvent(msg, PlayerEntryContainer.ClientState.GAME_BEGIN))
        return;

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
      
      stats.add("Actions added",1);

      if(action.has("type"))
        {
        stats.add("Actions "+action.get("type"),1);
        }
      else
        {
        stats.add("Actions invalid",1);
        }
        
      rpMan.addRPAction(action);

      /* Notify client that we recieved the action */
      MessageS2CActionACK msgAction=new MessageS2CActionACK(msg.getAddress(),action.getInt("action_id"));

      msgAction.setClientID(clientid);
      netMan.addMessage(msgAction);
      }
    catch(Exception e)
      {
      stats.add("Actions invalid",1);
      logger.error("error while processing ActionEvent",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processActionEvent");
      }
    }

  private void processPerceptionACKEvent(MessageC2SPerceptionACK msg)
    {
    Log4J.startMethod(logger,"processPerceptionACKEvent");
    try
      {
      int clientid=msg.getClientID();

      // verify event
      if (!isValidEvent(msg, PlayerEntryContainer.ClientState.GAME_BEGIN))
        return;
      
      /** TODO: Compute client lag here */

      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      entry.timestamp=System.currentTimeMillis();
      }
    catch(Exception e)
      {
      logger.error("error while processing processPerceptionACKEvent",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processPerceptionACKEvent");
      }
    }

  private void processLoginRequestKey(Message msg)
    {
    Log4J.startMethod(logger,"processLoginRequestKey");
    
    MessageC2SLoginRequestKey msgRequest=(MessageC2SLoginRequestKey)msg;
    if(rpMan.checkGameVersion(msgRequest.getGame(),msgRequest.getVersion()))
      {
      MessageS2CLoginSendKey msgLoginSendKey=new MessageS2CLoginSendKey(msg.getAddress(),key);
      msgLoginSendKey.setClientID(Message.CLIENTID_INVALID);
      netMan.addMessage(msgLoginSendKey);
      }
    else
      {
      /* Error: Incompatible game version. Update client */
      logger.debug("Server is running an incompatible game version. Client("+msg.getAddress().toString()+") can't login");

      /* Notify player of the event. */
      MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.Reasons.GAME_MISMATCH);

      netMan.addMessage(msgLoginNACK);
      }
    
    
    Log4J.finishMethod(logger,"processLoginRequestKey");
    }
  
  private void processCreateAccount(MessageC2SCreateAccount msg)
    {
    Log4J.startMethod(logger,"processCreateAccount");
    try
      {
      if(rpMan.createAccount(msg.getUsername(),msg.getPassword(), msg.getEmail()))
        {
        logger.debug("Account ("+msg.getUsername()+") created.");
        MessageS2CCreateAccountACK msgCreateAccountACK=new MessageS2CCreateAccountACK(msg.getAddress());  
        netMan.addMessage(msgCreateAccountACK);        
        }
      else
        {
        MessageS2CCreateAccountNACK msgCreateAccountNACK=new MessageS2CCreateAccountNACK(msg.getAddress(),MessageS2CCreateAccountNACK.Reasons.USERNAME_EXISTS);  
        netMan.addMessage(msgCreateAccountNACK);        
        }
      }
    catch(Exception e)
      {
      logger.error(e);
      }
    Log4J.finishMethod(logger,"processCreateAccount");
    }

  private void processLoginSendPromise(Message msg)
    {
    Log4J.startMethod(logger,"processLoginSendPromise");
    try
      {
      if(playerContainer.size()==GameConst.MAX_NUMBER_PLAYERS)
        {
        /* Error: Too many clients logged on the server. */
        logger.debug("Server is full, Client("+msg.getAddress().toString()+") can't login");
  
        /* Notify player of the event. */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.Reasons.SERVER_IS_FULL);
  
        netMan.addMessage(msgLoginNACK);
        return;
        }
        
      MessageC2SLoginSendPromise msgLoginSendPromise = (MessageC2SLoginSendPromise) msg;
      
      byte[] nonce = Hash.random(Hash.hashLength());

      int clientid=playerContainer.addRuntimePlayer(key,msgLoginSendPromise.getHash(),msg.getAddress());      
      PlayerEntryContainer.RuntimePlayerEntry player=playerContainer.get(clientid);
      player.loginInformations.serverNonce = nonce;
      
      MessageS2CLoginSendNonce msgLoginSendNonce = new MessageS2CLoginSendNonce(msg.getAddress(), nonce);
      msgLoginSendNonce.setClientID(clientid);
      netMan.addMessage(msgLoginSendNonce);
      }
    catch(NoSuchClientIDException e)
      {
      logger.error("client not found",e);
      }

    Log4J.finishMethod(logger,"processLoginSendPromise");
    }
    
  private void processSecuredLoginEvent(Message msg)
    {    
    Log4J.startMethod(logger,"processSecuredLoginEvent");
    try 
      {
      MessageC2SLoginSendNonceNameAndPassword msgLogin = (MessageC2SLoginSendNonceNameAndPassword)msg;
      
      // verify event
      if (!isValidEvent(msg, PlayerEntryContainer.ClientState.NULL))
        return;
      
      int clientid = msg.getClientID();
      PlayerEntryContainer.RuntimePlayerEntry player = playerContainer.get(clientid);
      player.loginInformations.clientNonce = msgLogin.getHash();
      player.loginInformations.userName = msgLogin.getUsername();
      player.loginInformations.password = msgLogin.getPassword();

      if(!playerContainer.verifyAccount(player.loginInformations)) 
        {
        logger.info("Incorrect username/password for player "+msgLogin.getUsername());
        stats.add("Players invalid login",1);
        playerContainer.addLoginEvent(msgLogin.getUsername(),msg.getAddress(),false);

        /* Send player the Login NACK message */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.Reasons.USERNAME_WRONG);

        netMan.addMessage(msgLoginNACK);
        playerContainer.removeRuntimePlayer(clientid);
        return;
        }
        
      if(playerContainer.hasPlayer(msgLogin.getUsername())) 
        {
        /* Warning: Player is already logged. */
        logger.warn("Client("+msg.getAddress().toString()+") trying to login twice");
        int clientoldid=playerContainer.getClientidPlayer(msgLogin.getUsername());

        if(playerContainer.getRuntimeState(clientoldid)==PlayerEntryContainer.ClientState.GAME_BEGIN)
          {
          RPObject.ID id=playerContainer.getRPObjectID(clientoldid);
          RPObject object=rpMan.getRPObject(id);

          if(rpMan.onExit(id))
            {
            /* NOTE: Set the Object so that it is stored in Database */
            playerContainer.setRPObject(clientoldid,object);
            }
          }
        else
          {
          logger.info("Player trying to logout without choosing character");
          }
          
        playerContainer.removeRuntimePlayer(clientoldid);
        }

      logger.debug("Correct username/password");
      
      /* Correct: The login is correct */
      player.username = player.loginInformations.userName;
      player.loginInformations = null;
      playerContainer.addLoginEvent(msgLogin.getUsername(),msg.getAddress(),true);
      
      stats.add("Players login",1);

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
      playerContainer.changeRuntimeState(clientid,PlayerEntryContainer.ClientState.LOGIN_COMPLETE);
      playerContainer.get(clientid).loginInformations = null;
      }
    catch(Exception e) 
      {
      logger.error("error while processing SecuredLoginEvent",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processSecuredLoginEvent");
      }
    }

  private void processOutOfSyncEvent(MessageC2SOutOfSync msg)
    {
    Log4J.startMethod(logger,"processOutOfSyncEvent");
    try
      {
      int clientid=msg.getClientID();

      // verify event
      if (!isValidEvent(msg, PlayerEntryContainer.ClientState.GAME_BEGIN))
        return;

      /** Notify PlayerEntryContainer that this player is out of Sync */
      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      entry.perception_OutOfSync=true;
      }
    catch(Exception e)
      {
      logger.error("error while processing OutOfSyncEvent",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processOutOfSyncEvent");
      }
    }

  private void processTransferACK(MessageC2STransferACK msg)
    {
    Log4J.startMethod(logger,"processTransferACK");
    try
      {
      int clientid=msg.getClientID();

      // verify event
      if (!isValidEvent(msg, PlayerEntryContainer.ClientState.GAME_BEGIN))
        return;

      /** Handle Transfer ACK here */
      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      for(TransferContent content: msg.getContents())
        {
        if(content.ack==true)
          {
          logger.debug("Trying transfer content "+content);
          
          content=entry.getContent(content.name);
          if(content!=null)
            {
            stats.add("Transfer content",1);
            stats.add("Tranfer content size",content.data.length);
            
            logger.debug("Transfering content "+content);
            MessageS2CTransfer msgTransfer=new MessageS2CTransfer(entry.source, content);
            msgTransfer.setClientID(clientid);
            netMan.addMessage(msgTransfer);
            }
          else
            {
            logger.debug("CAN'T transfer content "+content);
            }
          }
        else
          {
          stats.add("Transfer content cache",1);
          }
        }

      entry.clearContent();
      }
    catch(Exception e)
      {
      logger.error("error while processing TransferACK",e);
      }
    finally
      {
      Log4J.finishMethod(logger,"processTransferACK");
      }
    }
  
  /** */
  private static class ServerInfo
    {
    static Configuration config;
    static
      {
      Log4J.startMethod(logger,"ServerInfo[static]");
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
        logger.error("ABORT: Unable to load Server info",e);
        }
      Log4J.finishMethod(logger,"ServerInfo[static]");
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
            logger.debug("Property "+prop_name+" is not set???");
            }
          }
        }
      String[] result = new String[l_result.size()];
      return (String[])l_result.toArray(result);
      }
    }
  
  }
