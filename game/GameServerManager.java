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
      
    if(playerDatabase.isCorrect(msg.getUsername(),msg.getPassword()))
      {
      /* Correct: The login is correct */
      short clientid=playerContainer.addPlayer(msg.getAddress());
      playerContainer.setState(clientid,PlayerEntryContainer.STATE_LOGIN_COMPLETE);
      playerDatabase.updateLastLogin(msg.getUsername(),msg.getAddress());
      
      /* Send player the Login ACK message */
      MessageS2CLoginACK msgLoginACK=new MessageS2CLoginACK(msg.getAddress());
      netMan.addMessage(msgLoginACK);
      
      /* Build player character list and send it to client */
      }
    else
      {
      /* Error: User supplied wrong username/password */
      }
    }
  }