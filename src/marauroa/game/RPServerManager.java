/* $Id: RPServerManager.java,v 1.113 2004/11/19 20:30:06 arianne_rpg Exp $ */
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
import java.net.*;
import marauroa.net.*;
import marauroa.*;

/** This class is responsible for adding actions to scheduler, and to build and
 *  sent perceptions */
public class RPServerManager extends Thread
  {
  /** We send 1 TOTAL perception each SYNC_PERCEPTION_FRECUENCY DELTA perceptions */
  private static int SYNC_PERCEPTION_FRECUENCY;

  static
    {
    marauroad.trace("RPServerManager::(static)",">");
    try
      {
      Configuration conf=Configuration.getConfiguration();
      int value=Integer.parseInt(conf.get("syncPerception_frecuency"));

      SYNC_PERCEPTION_FRECUENCY=value;
      }
    catch(Exception e)
      {
      SYNC_PERCEPTION_FRECUENCY=30;
      }
    marauroad.trace("NetConst::(static)","<");
    }
  
  /** The thread will be running while keepRunning is true */
  private boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;
  /** The time elapsed between 2 turns. */
  private long turnDuration;
  /** The scheduler needed to organize actions */
  private RPScheduler scheduler;
  /** The ruleProcessor that the scheduler will use to execute the actions */
  private IRPRuleProcessor ruleProcessor;
  /** The place where the objects are stored */
  private RPWorld world;
  private Statistics stats;
  /** The networkServerManager so that we can send perceptions */
  private NetworkServerManager netMan;
  /** The PlayerEntryContainer so that we know where to send perceptions */
  private PlayerEntryContainer playerContainer;
  
  /** This list host players that still need to login in */
  private List<RPObject> incubator;
  
  /** Constructor
   *  @param netMan the NetworkServerManager so that we can send message */
  public RPServerManager(NetworkServerManager netMan)
    {
    super("RPServerManager");
    marauroad.trace("RPServerManager",">");
    try
      {
      stats=Statistics.getStatistics();
      keepRunning=true;
      isfinished=false;
      scheduler=new RPScheduler();
      playerContainer=PlayerEntryContainer.getContainer();
      this.netMan=netMan;
      
      Configuration conf=Configuration.getConfiguration();
      Class worldClass=Class.forName(conf.get("rp_RPWorldClass"));
      world=(RPWorld)worldClass.newInstance();      
      world.onInit();      
      
      Class ruleProcessorClass=Class.forName(conf.get("rp_RPRuleProcessorClass"));
      ruleProcessor=(IRPRuleProcessor)ruleProcessorClass.newInstance();
      ruleProcessor.setContext(this,world);
      
      String duration=conf.get("rp_turnDuration");

      turnDuration=Long.parseLong(duration);
      incubator=new LinkedList<RPObject>();
      
      start();
      }
    catch(Exception e)
      {
      marauroad.thrown("RPServerManager","X",e);
      marauroad.trace("RPServerManager","!","ABORT: Unable to create RPZone, RPRuleProcessor or RPAIManager instances");
      System.exit(-1);
      }
    finally
      {
      marauroad.trace("RPServerManager","<");
      }
    }
  
  /** This method finish the thread that run the RPServerManager */
  public void finish()
    {
    marauroad.trace("RPServerManager::finish",">");
    keepRunning=false;
    while(isfinished==false)
      {
      Thread.yield();
      }
    
    try
      {
      world.onFinish();
      }
    catch(Exception e)
      {
      marauroad.thrown("RPServerManager::finish","X",e);
      }
    
    marauroad.trace("RPServerManager::finish","<");
    }
  
  /** Adds an action for the next turn */
  public void addRPAction(RPAction action) throws ActionInvalidException
    {
    marauroad.trace("RPServerManager::addRPAction",">");
    try
      {
      if(marauroad.loggable("RPServerManager::addRPAction","D"))
        {
        marauroad.trace("RPServerManager::addRPAction","D","Added action: "+action.toString());
        }
        
      scheduler.addRPAction(action);
      }
    finally
      {
      marauroad.trace("RPServerManager::addRPAction","<");
      }
    }
  
  /** Returns an object of the world */
  public RPObject getRPObject(RPObject.ID id) throws RPObjectNotFoundException
    {
    marauroad.trace("RPServerManager::getRPObject",">");
    try
      {
      IRPZone zone=world.getRPZone(id);
      return zone.get(id);
      }
    finally
      {
      marauroad.trace("RPServerManager::getRPObject","<");
      }
    }
    
  private int deltaPerceptionSend=0;
  
  private void addIncubatorPlayers()
    {
    Iterator it=incubator.iterator();
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();          
      try
        {
        marauroad.trace("RPServerManager::addIncubatorPlayers","D","Adding object("+object.get("id")+") from incubator");
        if(!ruleProcessor.onInit(object))
          {
          marauroad.trace("RPServerManager::addIncubatorPlayers","W","Can't add to game "+object.toString());
          }
        }
      catch(Exception e) 
        {
        marauroad.thrown("RPServerManager::addIncubatorPlayers","X",e);
        }
      }
    
    incubator.clear();
    }
  
  private Perception getPlayerPerception(PlayerEntryContainer.RuntimePlayerEntry entry)
    {
    Perception perception=null;

    IRPZone zone=world.getRPZone(entry.characterid);
    
    if(deltaPerceptionSend<=SYNC_PERCEPTION_FRECUENCY && entry.perception_OutOfSync)
      {
      marauroad.trace("RPServerManager::getPlayerPerception","D","Perception DELTA for player ("+entry.characterid.toString()+")");
      perception=zone.getPerception(entry.characterid,Perception.DELTA);
      }
    else if(deltaPerceptionSend>SYNC_PERCEPTION_FRECUENCY && !entry.perception_OutOfSync)
      {
      entry.perception_OutOfSync=false;
      marauroad.trace("RPServerManager::getPlayerPerception","D","Perception SYNC for player ("+entry.characterid.toString()+")");
      perception=zone.getPerception(entry.characterid,Perception.SYNC);
      }
    else
      {
      marauroad.trace("RPServerManager::getPlayerPerception","D","NO Perception: Out of sync player ("+entry.characterid.toString()+")");
      }
    
    return perception;
    }
    
  private void sendPlayerPerception(PlayerEntryContainer.RuntimePlayerEntry entry,Perception perception, RPObject object)
    {
    if(perception!=null)
      {
      MessageS2CPerception messages2cPerception=new MessageS2CPerception(entry.source, perception);
      
      if(perception.type==Perception.SYNC || entry.isPerceptionModifiedRPObject(object))
        {
        messages2cPerception.setMyRPObject(object);
        }
      else
        {
        messages2cPerception.setMyRPObject(null);
        }
          
      messages2cPerception.setClientID(entry.clientid);
      messages2cPerception.setPerceptionTimestamp(entry.getPerceptionTimestamp());
       
      netMan.addMessage(messages2cPerception);
      }
    }
    
  private void buildPerceptions()
    {
    marauroad.trace("RPServerManager::buildPerceptions",">");

    List<Integer> playersToRemove=new LinkedList<Integer>();
    List<Integer> playersToUpdate=new LinkedList<Integer>();
	
	/** We reset the cache at Perceptions */
    MessageS2CPerception.clearPrecomputedPerception();
    
    ++deltaPerceptionSend;
      
    if(deltaPerceptionSend>SYNC_PERCEPTION_FRECUENCY)
      {
      /* We add new players on the SYNC frame. */
      addIncubatorPlayers();
      }

    PlayerEntryContainer.ClientIDIterator it=playerContainer.iterator();
      
    while(it.hasNext())
      {
      int clientid=it.next();
  
      try
        {
        PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
        
        if(entry.state==playerContainer.STATE_GAME_LOADED && deltaPerceptionSend>SYNC_PERCEPTION_FRECUENCY)
          {
          marauroad.trace("RPServerManager::buildPerception","D","Changing state to BEGIN because we are going to send a SYNC perception"); 
          entry.state=playerContainer.STATE_GAME_BEGIN;
          }
            
        if(entry.state==playerContainer.STATE_GAME_BEGIN)
          {
          Perception perception=getPlayerPerception(entry);
          IRPZone zone=world.getRPZone(entry.characterid);
          RPObject object=zone.get(entry.characterid);
          
          sendPlayerPerception(entry,perception,object);
           
          /** We check if we need to update player in the database */
          if(entry.shouldStoredUpdate(object))
            {
            playersToUpdate.add(new Integer(clientid));
            }
          }
            
        if(entry.isTimedout())
          {
          playersToRemove.add(new Integer(clientid));
          }
        }
      catch(Exception e)
        {
        marauroad.thrown("RPServerManager::buildPerceptions","X",e);
        marauroad.trace("RPServerManager::buildPerceptions","X","Removing player("+clientid+") because it caused a Exception while contacting it");
        playersToRemove.add(new Integer(clientid));
        }
      }
        
    if(deltaPerceptionSend>SYNC_PERCEPTION_FRECUENCY)
      {
      deltaPerceptionSend=0;
      }
 
    notifyUpdatesOnPlayer(playersToUpdate);
    notifyTimedoutPlayers(playersToRemove);

    marauroad.trace("RPServerManager::buildPerceptions","<");
    }

  private void notifyUpdatesOnPlayer(List playersToNotify)
    {
    marauroad.trace("RPServerManager::notifyUpdatesOnPlayer",">");
    try
      {
      Iterator it_notified=playersToNotify.iterator();

      while(it_notified.hasNext())
        {
        int clientid=((Integer)it_notified.next()).intValue();
        
        try
          {
          RPObject.ID id=playerContainer.getRPObjectID(clientid);
          IRPZone zone=world.getRPZone(id);
          RPObject object=zone.get(id);
          playerContainer.setRPObject(clientid,object);
          }
        catch(Exception e)
          {
          marauroad.thrown("RPServerManager::notifyUpdatesOnPlayer","X",e);
          marauroad.trace("RPServerManager::notifyUpdatesOnPlayer","X","Can't update the player("+clientid+")");
          }
        }
      }
    catch(Exception e)
      {
      marauroad.thrown("RPServerManager::notifyUpdatesOnPlayer","X",e);
      }
    finally
      {
      marauroad.trace("RPServerManager::notifyUpdatesOnPlayer","<");
      }
    }

  private void notifyTimedoutPlayers(List playersToNotify)
    {
    marauroad.trace("RPServerManager::notifyTimedoutPlayers",">");
    try
      {
      Iterator it_notified=playersToNotify.iterator();

      while(it_notified.hasNext())
        {
        int clientid=((Integer)it_notified.next()).intValue();
        
        try
          {
          stats.addPlayerTimeout(playerContainer.getUsername(clientid),clientid);
          
          RPObject.ID id=playerContainer.getRPObjectID(clientid);
          if(id==null)
            {
            marauroad.trace("RPServerManager::notifyTimedoutPlayers","W","Can't notify a player("+clientid+") that timedout because it never completed login");
            }
          else
            {
            RPObject object=getRPObject(id);

            if(ruleProcessor.onTimeout(id))
              {
              /* NOTE: Set the Object so that it is stored in Database */
              playerContainer.setRPObject(clientid,object);
              }
            }
          }
        catch(Exception e)
          {
          marauroad.thrown("RPServerManager::notifyTimedoutPlayers","X",e);
          marauroad.trace("RPServerManager::notifyTimedoutPlayers","X","Can't notify a player("+clientid+") that timedout");
          }
        finally
          {
          playerContainer.removeRuntimePlayer(clientid);
          marauroad.trace("RPServerManager::notifyTimedoutPlayers","D","Notified player ("+clientid+")");
          }
        }
      }
    catch(Exception e)
      {
      marauroad.thrown("RPServerManager::notifyTimedoutPlayers","X",e);
      marauroad.trace("RPServerManager::notifyTimedoutPlayers","X","Can't notify a player(-not available-) that timedout");
      }
    finally
      {
      marauroad.trace("RPServerManager::notifyTimedoutPlayers","<");
      }
    }

  /** This method is called when a player is added to the game */
  public boolean onInit(RPObject object) throws RPObjectInvalidException
    {
    incubator.add(object);
    return true;
    }
    
  /** This method is called when a player leave to the game */
  public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException
    {
    // TODO: Remove from incubator 
    scheduler.clearRPActions(id);
    return ruleProcessor.onExit(id);
    }
  
  /** This method is triggered to send content to the clients */
  public void transferContent(RPObject.ID id, List<TransferContent> content)
    {
    try
      {
      int clientid=playerContainer.getClientidPlayer(id);
      PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);
      
      entry.contentToTransfer=content;
    
      MessageS2CTransferREQ mes=new MessageS2CTransferREQ(entry.source,content);
      mes.setClientID(entry.clientid);
    
      netMan.addMessage(mes);
      }
    catch(NoSuchClientIDException e)
      {
      marauroad.thrown("RPServerManager::transferContent","X",e);
      }
    }
  
  public void run()
    {
    marauroad.trace("RPServerManager::run",">");
    long start=System.currentTimeMillis(),stop,delay;
    
    while(keepRunning)
      {
      stop=System.currentTimeMillis();
      try
        {
        marauroad.trace("RPServerManager::run","D","Turn time elapsed: "+Long.toString(stop-start));
        delay=turnDuration-(stop-start);
        Thread.sleep(delay<0?0:delay);
        }
      catch(InterruptedException e)
        {
        }
      start=System.currentTimeMillis();

      playerContainer.getLock().requestWriteLock();
        {
        /** Get actions that players send */
        scheduler.nextTurn();
        /** Execute them all */
        scheduler.visit(ruleProcessor);

        /** Compute game RP rules to move to the next turn */
        ruleProcessor.nextTurn();

        /** Tell player what happened */
        buildPerceptions();

        /** Move zone to the next turn */
        world.nextTurn();
        }
      playerContainer.getLock().releaseLock();
      
      stats.setObjectsNow(world.size());
      }
    
    isfinished=true;
    marauroad.trace("RPServerManager::run","<");
    }
  }
