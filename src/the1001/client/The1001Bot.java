/* $Id: The1001Bot.java,v 1.23 2004/04/04 21:50:37 root777 Exp $ */
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import marauroa.game.RPObject;
import marauroa.game.RPSlot;
import marauroa.game.RPZone;
import marauroa.marauroad;
import the1001.RPCode;
import marauroa.game.Attributes;import marauroa.game.Attributes;

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
  public static long startTS;
  private boolean continueGamePlay;
  private transient GameDataModel gm;
  private static Random random=new Random();
  private boolean doPrint;
  private boolean loggedOut;
  private The1001Bot(NetworkClientManager netman,boolean do_print)
  {
    netMan = netman;
    gm = new GameDataModel(netMan);
    doPrint=do_print;
    loggedOut = false;
    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
          public void run()
          {
            try
            {
              if(!loggedOut&&netMan!=null)
              {
                System.out.println("Shutting down while not logged out, trying to logout anyway...");
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
  
  /**
   * adds a message into reportPane
   */
  private void addChatMessage(String name,String msg)
  {
    String text = name+":"+msg;
    
    System.out.println(text);
  }
  
  public void run()
  {
    int time_out_max_count = 20;
    int timeout_count = 0;
    
    continueGamePlay = true;
    
    startTS = System.currentTimeMillis();
    boolean synced = false;
    try
    {
      while(continueGamePlay)
      {
        if(netMan!=null)
        {
          Message msg = netMan.getMessage();
          
          if(msg!=null)
          {
            if(msg instanceof MessageS2CPerception)
            {
              timeout_count = 0;
              MessageC2SPerceptionACK replyMsg=new MessageC2SPerceptionACK(msg.getAddress());
              
              replyMsg.setClientID(msg.getClientID());
              netMan.addMessage(replyMsg);
              
              MessageS2CPerception perception = (MessageS2CPerception)msg;
              boolean full_perception = perception.getTypePerception()==RPZone.Perception.TOTAL;
              
              if(!synced)
              {
                synced=full_perception;
                marauroad.trace("The1001Bot::messageLoop","D",synced?"Synced.":"Unsynced!");
              }
              marauroad.trace("The1001Bot::messageLoop","D",full_perception?"TOTAL PRECEPTION":"DELTA PERCEPTION");
              
              if(synced)
              {
                System.out.println(full_perception?"TOTAL PRECEPTION":"DELTA PERCEPTION");
                if(full_perception)
                {
                  gm.clearAllObjects();
                  //full perception contains all objects???
                }
                
                RPObject my_object = perception.getMyRPObject();
                if(my_object!=null)
                {
                  gm.setOwnCharacter(my_object);
                  if(my_object.hasSlot(RPCode.var_myGladiators))
                  {
                    for (Iterator iter = my_object.getSlot(RPCode.var_myGladiators).iterator(); iter.hasNext(); )
                    {
                      RPObject my_glad = (RPObject)iter.next();
                      marauroad.trace("The1001Bot::messageLoop","D","My Gladiator: "+my_glad);
                      gm.addMyGladiator(my_glad);
                    }
                  }
                }
                if(!full_perception)
                {
                  List deleted_objects = perception.getDeletedRPObjects();
                  for (int i = 0; i < deleted_objects.size(); i++)
                  {
                    RPObject obj = (RPObject)deleted_objects.get(i);
                    gm.deleteSpectator(obj);
                    gm.deleteFighter(obj);
                    gm.deleteShopGladiator(obj);
                  }
                  
                  List deleted_attrib_objects = perception.getModifiedDeletedRPObjects();
                  for (Iterator iter=deleted_attrib_objects.iterator();iter.hasNext();)
                  {
                    RPObject rp_obj_deleted = (RPObject)iter.next();
                    marauroad.trace("The1001Bot::messageLoop","D","Del attrs:"+rp_obj_deleted);
                    RPObject gm_object = gm.getObject(rp_obj_deleted.get(RPCode.var_object_id));
                    if(gm_object!=null)
                    {
                      gm_object.applyDifferences(null,rp_obj_deleted);
                    }
                    else
                    {
                      marauroad.trace("The1001Bot::messageLoop","D","deleted attr object without orig_object: " + rp_obj_deleted);
                    }
                  }
                  List added_attrib_objects = perception.getModifiedAddedRPObjects();
                  for (Iterator iter=added_attrib_objects.iterator();iter.hasNext();)
                  {
                    RPObject rp_obj_added = (RPObject)iter.next();
                    marauroad.trace("The1001Bot::messageLoop","D","Added attrs:"+rp_obj_added);
                    RPObject gm_object = gm.getObject(rp_obj_added.get(RPCode.var_object_id));
                    
                    if(gm_object!=null)
                    {
                      gm_object.applyDifferences(rp_obj_added,null);
                    }
                    else
                    {
                      marauroad.trace("The1001Bot::messageLoop","D","added attr object without orig_object: " + rp_obj_added);
                    }
                  }
                }
                List added_objects = perception.getAddedRPObjects();
                if(added_objects!=null && added_objects.size()>0)
                {
                  marauroad.trace("The1001Bot::messageLoop","D","List of added objects is not null: " + added_objects);
                  applyAddedObjects(added_objects);
                }
                gm.react(doPrint);
              }
              else
              {
                marauroad.trace("The1001Bot::messageLoop","D","Waiting for sync...");
              }
            }
            else if(msg instanceof MessageS2CLogoutACK)
            {
              loggedOut=true;
              System.out.println("Logged out...");
              sleep(20);
              System.exit(-1);
            }
            else if(msg instanceof MessageS2CActionACK)
            {
              MessageS2CActionACK msg_act_ack = (MessageS2CActionACK)msg;
              marauroad.trace("The1001Bot::messageLoop","D",msg_act_ack.toString());
            }
            else
            {
              // something other than
            }
          }
          else
          {
            timeout_count++;
            if(timeout_count>=time_out_max_count)
            {
              System.out.println("TIMEOUT. EXIT.");
              System.exit(1);
            }
            // System.out.println("TIMEOUT. SLEEPING.");
            sleep(1);
          }
        }
        else
        {
          sleep(5);
        }
      }
    }
    catch(Exception e)
    {
      marauroad.trace("The1001Bot::messageLoop","X",e.getMessage());
      e.printStackTrace();
    }
  }
  
  /**
   * Method applyAddedObjects
   *
   * @param    added_objects       a  List
   *
   */
  private void applyAddedObjects(List added_objects) throws Attributes.AttributeNotFoundException, RPObject.NoSlotFoundException
  {
    for (int i = 0; i < added_objects.size(); i++)
    {
      RPObject obj = (RPObject)added_objects.get(i);
      
      if("arena".equals(obj.get("type")))
      {
        gm.setArena(obj);
        
        String name = obj.get("name");
        String status = obj.get("status");
        
        if(RPCode.var_waiting.equals(status))
        {
          if(System.currentTimeMillis()>startTS+TIME_TO_RUN_BEFORE_LOGOUT)
          {
            gm.logout();
          }
        }
        marauroad.trace("The1001Bot::messageLoop","D","Arena: " + name + " [" + status+"]" +obj);
        try
        {
          RPSlot slot = obj.getSlot(RPCode.var_gladiators);
          for (Iterator iter = slot.iterator(); iter.hasNext() ; )
          {
            RPObject gladiator = (RPObject)iter.next();
            
            if("gladiator".equalsIgnoreCase(gladiator.get("type")))
            {
              gm.addFighter(gladiator);
              marauroad.trace("The1001Bot::messageLoop","D","Arena Gladiator: "+gladiator);
            }
            else
            {
              marauroad.trace("The1001Bot::messageLoop","D","Ignored wrong object in arena "+gladiator) ;
            }
          }
        }
        catch (RPObject.NoSlotFoundException e)
        {
          marauroad.trace("The1001Bot::messageLoop","X","Arena has no slot gladiators");
        }
      }
      else if("character".equals(obj.get("type")))
      {
        marauroad.trace("The1001Bot::messageLoop","D","character: "+obj);
        gm.addSpectator(obj);
      }
      else if("shop".equals(obj.get("type")))
      {
        marauroad.trace("The1001Bot::messageLoop","D","Shop: "+obj);
        if(obj.hasSlot("!gladiators"))
        {
          RPSlot slot = obj.getSlot("!gladiators");
          Iterator iter = slot.iterator();
          
          while(iter.hasNext())
          {
            RPObject shop_object = (RPObject)iter.next();
            
            if("gladiator".equals(shop_object.get(RPCode.var_type)))
            {
              gm.addShopGladiator(shop_object);
            }
            else
            {
              marauroad.trace("The1001Bot::messageLoop","D","Uknown object in shop "+shop_object);
            }
          }
        }
      }
      else
      {
        marauroad.trace("The1001Bot::messageLoop","D","Ignored wrong object in perception"+obj);
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
          System.out.println("Timeout "+recieved+"...");
          sleep(1);
        }
      }
      if(!complete)
      {
        System.out.println("Failed to connect to server. Exiting.");
        System.exit(-1);
      }
      marauroad.trace("The1001Bot::connectAndChooseCharacter","D","characters: "+characters);
      if(characters!=null && characters.length>0)
      {
        chooseCharacter(net_man, client_id, characters[0],doPrint);
      }
      else
      {
        System.out.println("No characters received from server - wrong username/password?");
        System.exit(-1);
      }
    }
    catch(MessageFactory.InvalidVersionException e)
    {
      System.out.println("Not able to connect to server because you are using an outdated client");
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
        // Should never happens
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
        System.out.println("Timeout "+recieved+"...");
        sleep(1);
      }
    }
    if(!complete)
    {
      System.out.println("Failed to connect to server. Exiting.");
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
