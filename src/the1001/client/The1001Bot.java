/* $Id: The1001Bot.java,v 1.12 2004/03/15 20:42:22 root777 Exp $ */
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

import java.net.SocketException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import marauroa.game.Attributes;
import marauroa.game.RPObject;
import marauroa.game.RPSlot;
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
  private boolean continueGamePlay;
  private transient GameDataModel gm;
  private static Random random=new Random();
  private static String rndMsg[]=
  {
    "What are the goals for this release of Marauroa?","","","",
      "Why is Marauroa using Java?","","","","",
      "Is there any estimation of when it will be released?","","","","",""
  };
  
  
  
  private The1001Bot(NetworkClientManager netman)
  {
    netMan = netman;
    gm = new GameDataModel(netMan);
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
    boolean i_am_fighting = false;
    boolean voted = false;
    try
    {
      while(continueGamePlay)
      {
        if(netMan!=null)
        {
          Message msg = netMan.getMessage();
          if(msg!=null && msg instanceof MessageS2CPerception)
          {
            System.out.println("---------------------------------------------");
            timeout_count = 0;
            MessageC2SPerceptionACK replyMsg=new MessageC2SPerceptionACK(msg.getAddress());
            replyMsg.setClientID(msg.getClientID());
            netMan.addMessage(replyMsg);
            
            MessageS2CPerception perception = (MessageS2CPerception)msg;
            RPObject my_object = perception.getMyRPObject();
            if(my_object!=null)
            {
              String name = my_object.get(RPCode.var_name);
              String fame = my_object.get(RPCode.var_fame);
              System.out.println("Me   Name: " +name);
              System.out.println("Me   Fame: " +fame);
              gm.setOwnCharacter(my_object);
              if(my_object.hasSlot(RPCode.var_myGladiators))
              {
                for (Iterator iter = my_object.getSlot(RPCode.var_myGladiators).iterator(); iter.hasNext(); )
                {
                  RPObject my_glad = (RPObject)iter.next();
                  gm.addMyGladiator(my_glad);
                  
                  name = my_glad.get(RPCode.var_name);
                  String karma = my_glad.get(RPCode.var_karma);
                  String hp = my_glad.get(RPCode.var_hp);
                  String victories = my_glad.get(RPCode.var_num_victory);
                  String defeats = my_glad.get(RPCode.var_num_defeat);
                  
                  System.out.println("My G Name     : " +name);
                  System.out.println("My G Karma    : " +karma);
                  System.out.println("My G Health   : " +hp);
                  System.out.println("My G Victories: " +victories);
                  System.out.println("My G Defeats  : " +defeats);
                }
              }
              else
              {
                RPObject glads_in_shop[] = gm.getShopGladiators();
                if(glads_in_shop.length>0)
                {
                  RPObject first_avail_glad = glads_in_shop[Math.abs(random.nextInt()%glads_in_shop.length)];
                  gm.buyGladiator(first_avail_glad.get(RPCode.var_object_id));
                }
              }
            }
            List modified_objects = perception.getModifiedRPObjects();
            for (int i = 0; i < modified_objects.size(); i++)
            {
              RPObject obj = (RPObject)modified_objects.get(i);
              if("arena".equals(obj.get("type")))
              {
                gm.setArena(obj);
                String name = obj.get("name");
                String status = obj.get("status");
                gm.setStatus(status);
                if(RPCode.var_waiting.equals(status))
                {
                  if(i_am_fighting)
                  {
                    i_am_fighting = false;
                  }
                  System.out.println("Arena waiting...");
                  if(Math.random()>0.1)
                  {
                    gm.requestFight();
                    System.out.println("Requesting fight...");
                  }
                  voted = false;
                }
                else if(RPCode.var_request_fame.equals(status))
                {
                  if(i_am_fighting)
                  {
                    i_am_fighting = false;
                  }
                  try
                  {
                    String timeout      = obj.get(RPCode.var_timeout);
                    String thumbs_up    = obj.get(RPCode.var_thumbs_up);
                    String thumbs_down  = obj.get(RPCode.var_thumbs_down);
                    String waiting      = obj.get(RPCode.var_waiting);
                    String fame         = obj.get(RPCode.var_karma);
                    System.out.println("Request fame("+fame+"): "+timeout + " Up: "+thumbs_up+" Down: "+thumbs_down+" Wait: "+waiting);
                  }
                  catch (Attributes.AttributeNotFoundException e)
                  {
                    
                  }
                  if(!voted)
                  {
                    gm.vote(Math.random()>0.5?RPCode.var_voted_up:"VOTE_DOWN");
                    voted = true;
                  }
                }
                else if(RPCode.var_fighting.equals(status))
                {
                  System.out.println("Fighting!!!");
                  if(!i_am_fighting && Math.random()>0.9 )
                  {
                    gm.requestFight();
                    System.out.println("Requesting fight...");
                  }
                  voted = false;
                }
                if(Math.random()>0.95)
                {
                  gm.sendMessage(rndMsg[Math.abs(random.nextInt()%rndMsg.length)]);
                }
                //                gm.setWaiting("waiting".equalsIgnoreCase(status));
                marauroad.trace("The1001Bot::messageLoop","D","Arena: " + name + " [" + status+"]" +obj);
                try
                {
                  RPSlot slot = obj.getSlot(RPCode.var_gladiators);
                  RPObject[] old_fighters = gm.getFighters();
                  RPObject[] new_fighters = new RPObject[slot.size()];
                  int k = 0;
                  HashSet hs = new HashSet();
                  System.out.println("---------------------------------------------------------------");
                  System.out.println(" Name           \tKarma\tHealth\tDamage\tWon\tLost\tW/L%");
                  System.out.println("---------------------------------------------------------------");
                  for (Iterator iter = slot.iterator(); iter.hasNext() ; )
                  {
                    RPObject gladiator = (RPObject)iter.next();
                    if("gladiator".equalsIgnoreCase(gladiator.get("type")))
                    {
                      //gm.addFighter(gladiator);
                      new_fighters[k++]=gladiator;
                      hs.add(gladiator.get(RPCode.var_object_id));
                      name = gladiator.get(RPCode.var_name);
                      while(name.length()<15)
                      {
                        name+=" ";
                      }
                      if(name.length()>15)
                      {
                        name=name.substring(0,15);
                      }
                      RPObject own_gl  = gm.getFirstOwnGladiator();
                      char sign = own_gl.get(RPCode.var_object_id).equals(gladiator.get(RPCode.var_object_id))?'*':' ';
                      String karma = gladiator.get(RPCode.var_karma);
                      String hp = gladiator.get(RPCode.var_hp);
                      String victories = gladiator.get(RPCode.var_num_victory);
                      String defeats = gladiator.get(RPCode.var_num_defeat);
                      String dam = " ";
                      int winp = Integer.parseInt(victories)/((Integer.parseInt(victories)+(Integer.parseInt(defeats)+1)))*100;
                      if(gladiator.has(RPCode.var_damage))
                      {
                        dam = gladiator.get(RPCode.var_damage);
                      }
                      System.out.println(sign+name+"\t"+karma+"\t"+hp+"\t"+dam+"\t"+victories+"\t"+defeats+"\t"+winp+"%");
                    }
                    else
                    {
                      marauroad.trace("The1001Bot::messageLoop","D","Ignored wrong object in arena");
                    }
                  }
                  System.out.println("---------------------------------------------------------------");
                  for (int x = 0; x < old_fighters.length; x++)
                  {
                    if(!hs.contains(old_fighters[x].get(RPCode.var_object_id)))
                    {
                      gm.deleteFighter(old_fighters[x]);
                    }
                  }
                  
                  RPObject own_gl  = gm.getFirstOwnGladiator();
                  for (int x = 0; x < new_fighters.length; x++)
                  {
                    gm.addFighter(new_fighters[x]);
                    if(own_gl!=null)
                    {
                      String own_gl_id = own_gl.get(RPCode.var_object_id);
                      if(own_gl_id.equals(new_fighters[x].get(RPCode.var_object_id)))
                      {
                        if(!i_am_fighting)
                        {
                          gm.setRandomFightMode();
                          i_am_fighting = true;
                        }
                        if(new_fighters[x].has(RPCode.var_hp))
                        {
                          int hp     = new_fighters[x].getInt(RPCode.var_hp);
                          if(hp>0)
                          {
                            if(new_fighters[x].has(RPCode.var_damage))
                            {
                              int damage = new_fighters[x].getInt(RPCode.var_damage);
                              if (damage>0)
                              {
                                gm.setRandomFightMode();
                              }
                            }
                          }
                        }
                      }
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
                String name = obj.get(RPCode.var_name);
                String fame = obj.get(RPCode.var_fame);
                System.out.println("C Name: " +name);
                System.out.println("C Fame: " +fame);
                if(obj.has(RPCode.var_text))
                {
                  String text = obj.get(RPCode.var_text);
                  if(!"".equals(text))
                  {
                    addChatMessage(name,text);
                  }
                }
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
            
            List deleted_objects = perception.getDeletedRPObjects();
            for (int i = 0; i < deleted_objects.size(); i++)
            {
              RPObject obj = (RPObject)deleted_objects.get(i);
              gm.deleteSpectator(obj);
              gm.deleteFighter(obj);
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
            //            System.out.println("TIMEOUT. SLEEPING.");
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
      new Thread(new Runnable()
                 {
            public void run()
            {
              connectAndChooseCharacter(host,user,pwd);
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
  
  private static void connectAndChooseCharacter(String hostname, String user, String pwd)
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
          case Message.TYPE_S2C_LOGIN_ACK: //10
            client_id=message.getClientID();
            break;
          case Message.TYPE_S2C_CHARACTERLIST: //2
            characters=((MessageS2CCharacterList)message).getCharacters();
            break;
          case Message.TYPE_S2C_SERVERINFO: //7
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
        chooseCharacter(net_man, client_id, characters[0]);
      }
      else
      {
        JOptionPane.showMessageDialog(null,"No characters received from server - wrong username/password?");
        System.exit(-1);
      }
    }
    catch(SocketException e)
    {
      marauroad.trace("The1001Bot::connectAndChooseCharacter","X",e.getMessage());
    }
  }
  
  private static void chooseCharacter(NetworkClientManager netman, int client_id, String character)
  {
    Message msg=new MessageC2SChooseCharacter(null,character);
    msg.setClientID(client_id);
    
    netman.addMessage(msg);
    
    Message message=null;
    boolean complete=false;
    int recieved=0;
    
    while(!complete && recieved<40)
    {
      message=netman.getMessage();
      recieved++;
      if(message!=null)
      {
        marauroad.trace("The1001Bot::chooseCharacter","D","new message, waiting for "+Message.TYPE_S2C_CHOOSECHARACTER_ACK + ", receivied "+message.getType());
        if(message.getType()==Message.TYPE_S2C_CHOOSECHARACTER_ACK)
        {
          The1001Bot game = new The1001Bot(netman);
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
  
  
}



