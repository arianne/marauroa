package marauroa.game;

import java.util.*;
import java.io.*;

import marauroa.net.*;
import marauroa.marauroad;


public class GameServerManager extends Thread
  {
  private NetworkServerManager netMan;
  private PlayerEntryContainer playerContainer;
  private PlayerDatabase playerDatabase;
  private boolean keepRunning;
  
  public GameServerManager(NetworkServerManager netMan)
    {
    super("GameServerManager");
    
    keepRunning=true;    
    this.netMan=netMan;
    this.playerContainer=PlayerEntryContainer.getContainer();
    this.playerDatabase=PlayerDatabase.getDatabase();
    }

  public void finish()
    {
    keepRunning=false;
    }
    
  public void run()
    {
    marauroa.marauroad.report("Start thread "+this.getName());
    while(keepRunning)
      {
      Message msg=netMan.getMessage(1000);
      
      if(msg!=null)
        {    
        switch(msg.getType()) 
          {
          case Message.TYPE_C2S_LOGIN:
            processLoginEvent((MessageC2SLogin)msg);
            break;
          case Message.TYPE_C2S_CHOOSECHARACTER:
            break;
          case Message.TYPE_C2S_LOGOUT:
            break;
          default:
            marauroad.report("Not valid messaged recieved");
            break;
          }
        }
      }

    marauroa.marauroad.report("End thread "+this.getName());
    }
    
  private void processLoginEvent(MessageC2SLogin msg)
    {
    if(playerContainer.containsPlayer(msg.getClientID()))
      {
      /* Error: Player is already logged. */
      marauroad.report("Client "+msg.getClientID()+" is trying to login twice: IGNORE");
      
      return;
      }
      
    if(playerContainer.size()==GameConst.MAX_NUMBER_PLAYERS)
      {
      /* Error: Too many clients logged on the server. */
      marauroad.report("Client is trying to login on full server: NOTIFY");
      
      /* Notify player of the event. */
      MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.SERVER_IS_FULL);
      netMan.addMessage(msgLoginNACK);
      
      return;
      }
      
    try  
      {
	  if(playerDatabase.isCorrect(msg.getUsername(),msg.getPassword()))
	    {
	    /* Correct: The login is correct */
	    short clientid=playerContainer.addPlayer(msg.getUsername(),msg.getAddress());
	    playerDatabase.updateLastLogin(msg.getUsername(),msg.getAddress(),true);
	      
	    /* Send player the Login ACK message */
	    MessageS2CLoginACK msgLoginACK=new MessageS2CLoginACK(msg.getAddress());
	    netMan.addMessage(msgLoginACK);
	      
	    /* Build player character list and send it to client */
	    String[] characters=playerDatabase.getCharactersList(msg.getUsername());
	    MessageS2CCharacterList msgCharacters=new MessageS2CCharacterList(msg.getAddress(),characters);
	    netMan.addMessage(msgCharacters);

	    playerContainer.setState(clientid,PlayerEntryContainer.STATE_LOGIN_COMPLETE);
	    }
	  else
	    {
	    /* Error: User supplied wrong username/password */
	    playerDatabase.updateLastLogin(msg.getUsername(),msg.getAddress(),false);

	    /* Send player the Login NACK message */
	    MessageS2CLoginNACK msgLoginNACK=new MessageS2CLoginNACK(msg.getAddress(),MessageS2CLoginNACK.USERNAME_WRONG);
	    netMan.addMessage(msgLoginNACK);
	    }
	  }
    catch(PlayerEntryContainer.NoSuchClientIDException e)      
      {
      marauroad.report(e.getMessage());
      }
    }

  private void processChooseCharacterEvent(MessageC2SChooseCharacter msg)
    {
    short clientid=msg.getClientID();
    
    try
      {
	  if(!playerContainer.containsPlayer(clientid))
	    {
	    /* Error: Player didn't login. */
	    marauroad.report("Client "+clientid+" has not login yet: IGNORE");
	      
	    return;
	    }
	    
	  if(playerContainer.getState(clientid)!=playerContainer.STATE_LOGIN_COMPLETE)
	    {
	    /* Error: Player has not completed login yet, or he/she has logout already. */
	    marauroad.report("Client "+clientid+" has not login yet : IGNORE");
	     
	    return;
	    }
	      
	  if(playerDatabase.hasCharacter(playerContainer.getUsername(clientid),msg.getCharacter()))
	    {
	    /* Correct: Character exist */
	    MessageS2CChooseCharacterACK msgChooseCharacterACK=new MessageS2CChooseCharacterACK(msg.getAddress());
	    netMan.addMessage(msgChooseCharacterACK);
	    
	    playerContainer.setState(clientid,playerContainer.STATE_GAME_BEGIN);
	    }
	  else
	    {
	    /* Error: There is no such character */
	    marauroad.report("Client "+clientid+" has choosen a not valid character: NOTIFY");
	    MessageS2CChooseCharacterNACK msgChooseCharacterNACK=new MessageS2CChooseCharacterNACK(msg.getAddress());
	    netMan.addMessage(msgChooseCharacterNACK);
	    
	    playerContainer.setState(clientid,playerContainer.STATE_LOGIN_COMPLETE);
	    }
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)      
      {
      marauroad.report(e.getMessage());
      }
    }

  private void processLogoutEvent(MessageC2SLogout msg)
    {
    short clientid=msg.getClientID();
    
    try
      {
	  if(!playerContainer.containsPlayer(clientid))
	    {
	    /* Error: Player didn't login. */
	    marauroad.report("Client "+clientid+" has not login yet: IGNORE");
	      
	    return;
	    }
	    
	  MessageS2CLogoutACK msgLogout=new MessageS2CLogoutACK(msg.getAddress());
	  netMan.addMessage(msgLogout);
	  
	  playerContainer.removePlayer(clientid);
      }
    catch(PlayerEntryContainer.NoSuchClientIDException e)      
      {
      marauroad.report(e.getMessage());
      }
    }
      
  }