/* $Id: The1001Bot.java,v 1.33 2004/04/30 20:37:52 root777 Exp $ */
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

package the1001.client;

import marauroa.net.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import marauroa.game.Attributes;
import marauroa.game.RPObject;
import marauroa.game.RPSlot;
import marauroa.game.RPZone;
import marauroa.marauroad;
import the1001.RPCode;

/**
 *
 *
 *@author Waldemar Tribus
 */
public class The1001Bot
  implements Runnable
{
  private final static long serialVersionUID = 4715;
  private transient NetworkClientManager netMan;
  public static long TIME_TO_RUN_BEFORE_LOGOUT=1*60*60*1000;// 1*60*60*1000; //one hour
  private boolean continueGamePlay;
  private transient GameDataModel gm;
  private boolean loggedOut;
  
  private The1001Bot(NetworkClientManager netman,boolean do_print)
  {
    netMan = netman;
    gm = new GameDataModel(netMan, do_print, true);
    loggedOut = false;
    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
          public void run()
          {
            try
            {
              if(!loggedOut&&netMan!=null)
              {
                marauroad.trace("The1001Bot::messageLoop","D","Shutting down while not logged out, trying to logout anyway...");
                netMan.addMessage(new MessageC2SLogout());
                loggedOut = true;
              }
            }
            catch(Throwable thr)
            {
            }
          }
        });
  }
  
  public void run()
  {
    continueGamePlay = true;
    PerceptionHandler handler=new PerceptionHandler(gm);
    Message msg;
    while(continueGamePlay)
    {
      try
      {
        msg=netMan.getMessage();
        if(msg!=null)
        {
          if(msg instanceof MessageS2CPerception)
          {
            MessageC2SPerceptionACK reply=new MessageC2SPerceptionACK(msg.getAddress());
            reply.setClientID(msg.getClientID());
            netMan.addMessage(reply);
            MessageS2CPerception msgPer=(MessageS2CPerception)msg;
            handler.apply(msgPer,gm.getAllObjects());
          }
          else if(msg instanceof MessageS2CLogoutACK)
          {
            loggedOut=true;
            marauroad.trace("The1001Bot::messageLoop","D","Logged out...");
            sleep(30);
            System.exit(-1);
          }
          else if(msg instanceof MessageS2CActionACK)
          {
            MessageS2CActionACK msg_act_ack = (MessageS2CActionACK)msg;
            gm.actionAck(msg_act_ack);
            marauroad.trace("The1001Bot::messageLoop","D",msg_act_ack.toString());
          }
          else
          {
            marauroad.trace("The1001Bot::messageLoop","D","Unknown message: "+msg.toString());
          }
        }
        else // null message - sleep a little to not abuse cpu
        {
          sleep(5);
        }
      }
      catch (MessageFactory.InvalidVersionException e)
      {
        marauroad.trace("The1001Bot.messageLoop","X","Invalid protocol version");
        System.exit(-1);
      }
    }
  }
  
  
  
  /**
   * causes the calling thread to sleep the specified amount of <b>seconds</b>
   * @param timeout the amount of seconds to sleep
   **/
  private static void sleep(long timeout)
  {
    try
    {
      Thread.sleep(timeout*1000);
    }
    catch (InterruptedException e)
    {
    }
  }
  
  /**
   *
   */
  public static void main(final String[] args)
  {
    if(args.length<3)
    {
      System.out.println("Usage: java -classpath <cp> the1001.client.The1001Bot server user password");
    }
    
    int thr_count = args.length/3;
    
    for (int i = 0; i < thr_count; i++)
    {
      final String host = args[i*3];
      final String user = args[i*3+1];
      final String pwd  = args[i*3+2];
      final boolean first=(i==0);
      
      new Thread(new Runnable()
                 {
            public void run()
            {
              connectAndChooseCharacter(host,user,pwd,first);
            }
          }).start();
      try
      {
        Thread.sleep(5000);
      }
      catch (InterruptedException e)
      {
      }
    }
  }
  
  private static void connectAndChooseCharacter(String hostname, String user, String pwd, boolean doPrint)
  {
    NetworkClientManager net_man;
    int client_id = -1;
    
    try
    {
      net_man=new NetworkClientManager(hostname);
      
      MessageC2SLogin msg=new MessageC2SLogin(null,user,pwd);
      
      net_man.addMessage(msg);
      
      boolean complete=false;
      int recieved=0;
      String[] characters=null;
      String[] serverInfo=null;
      
      client_id=-1;
      while(!complete && recieved<40)
      {
        Message message=net_man.getMessage();
        
        ++recieved;
        if(message!=null)
        {
          marauroad.trace("The1001Bot::connectAndChooseCharacter","D","new message, waiting for "+Message.TYPE_S2C_LOGIN_ACK + ", receivied "+message.getType());
          switch(message.getType())
          {
            case Message.TYPE_S2C_LOGIN_NACK:
              complete=true;
              break;
            case Message.TYPE_S2C_LOGIN_ACK: // 10
              client_id=message.getClientID();
              break;
            case Message.TYPE_S2C_CHARACTERLIST: // 2
              characters=((MessageS2CCharacterList)message).getCharacters();
              break;
            case Message.TYPE_S2C_SERVERINFO: // 7
              serverInfo=((MessageS2CServerInfo)message).getContents();
              break;
          }
          complete = complete || ((serverInfo!=null) && (characters!=null) && (client_id!=-1));
        }
        else
        {
          marauroad.trace("The1001Bot::messageLoop","D","Timeout "+recieved+"...");
          sleep(1);
        }
      }
      if(!complete)
      {
        marauroad.trace("The1001Bot::messageLoop","X","Failed to connect to server. Exiting.");
        System.exit(-1);
      }
      marauroad.trace("The1001Bot::connectAndChooseCharacter","D","characters: "+characters);
      if(characters!=null && characters.length>0)
      {
        chooseCharacter(net_man, client_id, characters[0],doPrint);
      }
      else
      {
        marauroad.trace("The1001Bot::messageLoop","X","No characters received from server - wrong username/password?");
        System.exit(-1);
      }
    }
    catch(MessageFactory.InvalidVersionException e)
    {
      marauroad.trace("The1001Bot::messageLoop","X","Not able to connect to server because you are using an outdated client");
      System.exit(-1);
    }
    catch(SocketException e)
    {
      marauroad.trace("The1001Bot::connectAndChooseCharacter","X",e.getMessage());
    }
  }
  
  private static void chooseCharacter(NetworkClientManager netman, int client_id, String character, boolean doPrint)
  {
    Message msg=new MessageC2SChooseCharacter(null,character);
    
    msg.setClientID(client_id);
    netman.addMessage(msg);
    
    Message message=null;
    boolean complete=false;
    int recieved=0;
    
    while(!complete && recieved<40)
    {
      try
      {
        message=netman.getMessage();
      }
      catch(MessageFactory.InvalidVersionException e)
      {
        marauroad.trace("The1001Bot::messageLoop","X",e.getMessage());
      }
      
      recieved++;
      if(message!=null)
      {
        marauroad.trace("The1001Bot::chooseCharacter","D","new message, waiting for "+Message.TYPE_S2C_CHOOSECHARACTER_ACK + ", receivied "+message.getType());
        if(message.getType()==Message.TYPE_S2C_CHOOSECHARACTER_ACK)
        {
          The1001Bot game = new The1001Bot(netman,doPrint);
          
          new Thread(game,"Game thread...").start();
          complete = true;
        }
        else if(message.getType()==Message.TYPE_S2C_CHOOSECHARACTER_NACK)
        {
          marauroad.trace("The1001Bot::chooseCharacter","E","server nacks the character, exiting...");
          System.exit(-1);
        }
      }
      else
      {
        marauroad.trace("The1001Bot::messageLoop","D","Timeout "+recieved+"...");
        sleep(1);
      }
    }
    if(!complete)
    {
      marauroad.trace("The1001Bot::messageLoop","X","Failed to connect to server. Exiting.");
      System.exit(-1);
    }
  }
  
  public static String getCite()
  {
    String cite = "";
    try
    {
      Process process = Runtime.getRuntime().exec("fortune -s");
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String readline = null;
      
      while((readline=br.readLine())!=null)
      {
        cite+=readline;
      }
      process.destroy();
    }
    catch (IOException e) {}
    return(cite);
  }
}
