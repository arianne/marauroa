package marauroa.game;

import java.util.*;
import java.io.*;

import marauroa.net.*;
import marauroa.marauroad;

/** The GameServerManager is a active entity of the marauroa.game package,
 *  it is in charge of processing all the messages and modify PlayerEntry Container accordingly. */
public class GameServerManager extends Thread
  {
  private NetworkServerManager netMan;  
  private RPServerManager rpMan;
  private PlayerEntryContainer playerContainer;
  
  /** The thread will be running while keepRunning is true */
  private boolean keepRunning;
  
  /** Constructor that initialize also the RPManager 
   *  @param netMan a NetworkServerManager instance. */
  public GameServerManager(NetworkServerManager netMan)
    {
    super("GameServerManager");
    marauroad.trace("GameServerManager",">");    
    
    keepRunning=true;    
    this.netMan=netMan;
    playerContainer=PlayerEntryContainer.getContainer();    
    rpMan=new RPServerManager();
        
    marauroad.trace("GameServerManager","<");
    }

  public void finish()
    {
    marauroad.trace("GameServerManager::finish",">");
    
    rpMan.finish();
    keepRunning=false;
    
    marauroad.trace("GameServerManager::finish","<");
    }
    
  public void run()
    {
    marauroad.trace("GameServerManager::run",">");

    rpMan.start();    

    while(keepRunning)
      {
      Message msg=netMan.getMessage();
      
      if(msg!=null)
        {    
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
          default:
            marauroad.trace("GameServerManager::run","W","Unknown Message["+msg.getType()+"]");
            break;
          }
        }
      }

    marauroad.trace("GameServerManager::run","<");
    }
    
  private void processLoginEvent(MessageC2SLogin msg)
    {
    marauroad.trace("GameServerManager::processLoginEvent",">");
    try
      {
      if(playerContainer.hasRuntimePlayer(msg.getClientID()) || playerContainer.hasPlayer(msg.getUsername()))
        {
        /* Error: Player is already logged. */
        marauroad.trace("GameServerManager::processLoginEvent","W","Client("+msg.getAddress().toString()+") trying to login twice");

        /* Notify player of the event. */
        MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.UNKNOWN_REASON);
        netMan.addMessage(msgLoginNACK);
        return;
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
	    short clientid=playerContainer.addRuntimePlayer(msg.getUsername(),msg.getAddress());
	    playerContainer.addLoginEvent(clientid,msg.getAddress(),true);
	      
	    /* Send player the Login ACK message */
	    MessageS2CLoginACK msgLoginACK=new MessageS2CLoginACK(msg.getAddress());
	    msgLoginACK.setClientID(clientid);
	    netMan.addMessage(msgLoginACK);
	      
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

	    /* Send player the Login NACK message */
	    MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.USERNAME_WRONG);
	    netMan.addMessage(msgLoginNACK);
	    }
	  }
    catch(Throwable e)      
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
      short clientid=msg.getClientID();
      
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
	    rpMan.addRPObject(object);	    
	    
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
    catch(Throwable e)      
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
      short clientid=msg.getClientID();
      
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
	  
	  RPObject.ID id=playerContainer.getRPObjectID(clientid);	  
      RPObject object=rpMan.getRPObject(id);      
	  rpMan.removeRPObject(id);
      
      /* NOTE: Set the Object so that it is stored in Database */
      playerContainer.setRPObject(clientid,object);  
          
	  playerContainer.removeRuntimePlayer(clientid);
	  
	  /* Send Logout ACK message */
	  MessageS2CLogoutACK msgLogout=new MessageS2CLogoutACK(msg.getAddress());
      msgLogout.setClientID(clientid);
	  netMan.addMessage(msgLogout);
      }
    catch(Throwable e)      
      {
      marauroad.trace("GameServerManager::processLogoutEvent","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("GameServerManager::processLogoutEvent","<");
      }
    }
    
    
  private void processActionEvent(MessageC2SAction msg)
    {
    marauroad.trace("GameServerManager::processActionEvent",">");
    
    try
      {
      short clientid=msg.getClientID();
      
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
	  rpMan.addRPAction(msg.getRPAction());

	  /* Notify client that we recieved the action */
	  MessageS2CActionACK msgAction=new MessageS2CActionACK(msg.getAddress());
      msgAction.setClientID(clientid);
	  netMan.addMessage(msgAction);	  
      }
    catch(Throwable e)      
      {
      marauroad.trace("GameServerManager::processActionEvent","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("GameServerManager::processActionEvent","<");
      }
    }       
  }