/* $Id: RPServerManager.java,v 1.99 2004/06/07 17:21:21 arianne_rpg Exp $ */
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
  /** We send 1 TOTAL perception each TOTAL_PERCEPTION_RELATION DELTA perceptions */
  private final static int TOTAL_PERCEPTION_RELATION=10;
  
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
  private IRPAIManager aiMan;
  /** The place where the objects are stored */
  private IRPZone zone;
  private Statistics stats;
  /** The networkServerManager so that we can send perceptions */
  private NetworkServerManager netMan;
  /** The PlayerEntryContainer so that we know where to send perceptions */
  private PlayerEntryContainer playerContainer;
  
  private List incubator;
  
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
      Class zoneClass=Class.forName(conf.get("rp_RPZoneClass"));
      zone=(IRPZone)zoneClass.newInstance();
      
      zone.onInit();
      
      
      Class AIManagerClass=Class.forName(conf.get("rp_RPAIClass"));
      aiMan=(IRPAIManager)AIManagerClass.newInstance();
      aiMan.setContext(zone,scheduler);

      Class ruleProcessorClass=Class.forName(conf.get("rp_RPRuleProcessorClass"));
      ruleProcessor=(IRPRuleProcessor)ruleProcessorClass.newInstance();
      ruleProcessor.setContext(zone);
      
      String duration =conf.get("rp_turnDuration");

      turnDuration = Long.parseLong(duration);
      incubator=new LinkedList();
      
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
  
  /** Constructor
   *  @param netMan the NetworkServerManager so that we can send message */
  public RPServerManager(NetworkServerManager netMan, IRPZone zone, IRPRuleProcessor ruleProcessor, long turnDuration)
    {
    super("RPServerManager");
    marauroad.trace("RPServerManager",">");
    try
      {
      keepRunning=true;
      isfinished=false;
      scheduler=new RPScheduler();
      playerContainer=PlayerEntryContainer.getContainer();
      this.netMan=netMan;
      this.zone=zone;
      this.ruleProcessor=ruleProcessor;
      this.turnDuration=turnDuration;
      start();
      }
    finally
      {
      marauroad.trace("RPServerManager","<");
      }
    }

  public void finish()
    {
    marauroad.trace("RPServerManager::finish",">");
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
    
    try
      {
      zone.onFinish();
      }
    catch(Exception e)
      {
      marauroad.thrown("RPServerManager::finish","X",e);
      }
    
    marauroad.trace("RPServerManager::finish","<");
    }
  
  public void addRPAction(RPAction action) throws RPScheduler.ActionInvalidException
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
    
  public void addRPObject(RPObject object) throws IRPZone.RPObjectInvalidException
    {
    marauroad.trace("RPServerManager::addRPObject",">");
    try
      {
      if(marauroad.loggable("RPServerManager::addRPObject","D"))
        {
        marauroad.trace("RPServerManager::addRPObject","D","Added object: "+object.toString());
        }
        
      zone.add(object);
      }
    finally
      {
      marauroad.trace("RPServerManager::addRPObject","<");
      }
    }
  
  public RPObject getRPObject(RPObject.ID id) throws IRPZone.RPObjectNotFoundException
    {
    marauroad.trace("RPServerManager::getRPObject",">");
    try
      {
      return zone.get(id);
      }
    finally
      {
      marauroad.trace("RPServerManager::getRPObject","<");
      }
    }
  
  public boolean hasRPObject(RPObject.ID id)
    {
    marauroad.trace("RPServerManager::hasRPObject",">");
    try
      {
      return zone.has(id);
      }
    finally
      {
      marauroad.trace("RPServerManager::hasRPObject","<");
      }
    }
  
  public RPObject removeRPObject(RPObject.ID id) throws IRPZone.RPObjectNotFoundException
    {
    marauroad.trace("RPServerManager::removeRPObject",">");
    try
      {
      marauroad.trace("RPServerManager::removeRPObject","D","Removed object: "+id.toString());
      return zone.remove(id);
      }
    finally
      {
      marauroad.trace("RPServerManager::removeRPObject","<");
      }
    }
    
  private int deltaPerceptionSend=0;
  
  synchronized private void buildPerceptions()
    {
    marauroad.trace("RPServerManager::buildPerceptions",">");

    List playersToRemove=new LinkedList();
    List playersToUpdate=new LinkedList();
    MessageS2CPerception.clearPrecomputedPerception();
    
    try
      {
      ++deltaPerceptionSend;
      
      if(deltaPerceptionSend>TOTAL_PERCEPTION_RELATION)
        {
        Iterator it=incubator.iterator();
        while(it.hasNext())
          {
          RPObject object=(RPObject)it.next();          
          try
            {
            marauroad.trace("RPServerManager::buildPerceptions","D","Adding object("+object.get("id")+") from incubator");
            if(!ruleProcessor.onInit(object))
              {
              marauroad.trace("RPServerManager::buildPerceptions","W","Can't add to game "+object.toString());
              }
            }
          catch(Exception e) 
            {
            marauroad.thrown("RPServerManager::buildPerceptions","X",e);
            }
          }
        
        incubator.clear();
        }

      PlayerEntryContainer.ClientIDIterator it=playerContainer.iterator();
      
      while(it.hasNext())
        {
        int clientid=it.next();
        
        try
          {
          if(playerContainer.getRuntimeState(clientid)==playerContainer.STATE_GAME_LOADED && deltaPerceptionSend>TOTAL_PERCEPTION_RELATION)
            {
            marauroad.trace("RPServerManager::buildPerception","D","Changing state to BEGIN because we are to send a SYNC perception"); 
            playerContainer.changeRuntimeState(clientid,playerContainer.STATE_GAME_BEGIN);
            playerContainer.setOutOfSync(clientid,false);
            }
            
          if(playerContainer.getRuntimeState(clientid)==playerContainer.STATE_GAME_BEGIN && !playerContainer.isOutOfSync(clientid))
            {
            InetSocketAddress source=playerContainer.getInetSocketAddress(clientid);
            Perception perception;
            RPObject object=zone.get(playerContainer.getRPObjectID(clientid));

            if(deltaPerceptionSend>TOTAL_PERCEPTION_RELATION)
              {
              marauroad.trace("RPServerManager::buildPerceptions","D","Perception TOTAL for player ("+playerContainer.getRPObjectID(clientid).toString()+")");
              perception=zone.getPerception(playerContainer.getRPObjectID(clientid),Perception.SYNC);
              }
            else
              {
              marauroad.trace("RPServerManager::buildPerceptions","D","Perception DELTA for player ("+playerContainer.getRPObjectID(clientid).toString()+")");
              perception=zone.getPerception(playerContainer.getRPObjectID(clientid),Perception.DELTA);
              }
            
            int timestamp=playerContainer.getPerceptionTimestamp(clientid);
            
            MessageS2CPerception messages2cPerception=new MessageS2CPerception(source, perception);
            
            if(perception.type==Perception.SYNC || playerContainer.isPerceptionModifiedRPObject(clientid,object))
              {
              messages2cPerception.setMyRPObject(object);
              }
            else
              {
              messages2cPerception.setMyRPObject(null);
              }
              
            messages2cPerception.setClientID(clientid);
            messages2cPerception.setPerceptionTimestamp(timestamp);
            
            netMan.addMessage(messages2cPerception);
          
            /** We check if we need to update player in the database */
            if(playerContainer.shouldStoredUpdate(clientid,object))
              {
              playersToUpdate.add(new Integer(clientid));
              }
            }
            
          if(playerContainer.timedout(clientid))
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
      if(deltaPerceptionSend>TOTAL_PERCEPTION_RELATION)
        {
        deltaPerceptionSend=0;
        }
 
      notifyUpdatesOnPlayer(playersToUpdate);
      notifyTimedoutPlayers(playersToRemove);
      }
    finally
      {
      marauroad.trace("RPServerManager::buildPerceptions","<");
      }
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
          RPObject object=zone.get(playerContainer.getRPObjectID(clientid));
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

  public boolean onInit(RPObject object) throws IRPZone.RPObjectInvalidException
    {
    incubator.add(object);
    return true;
    }
    
  public boolean onExit(RPObject.ID id) throws IRPZone.RPObjectNotFoundException
    {
    scheduler.clearRPActions(id);
    return ruleProcessor.onExit(id);
    }
  
  public List buildMapObjectsList(RPObject.ID id)
    {
    scheduler.clearRPActions(id);
    return ruleProcessor.buildMapObjectsList(id);
    }
  
  public void run()
    {
    marauroad.trace("RPServerManager::run",">");
    long start=System.currentTimeMillis(),stop;
    long delay=0;
    
    while(keepRunning)
      {
      scheduler.visit(ruleProcessor);
      playerContainer.getLock().requestWriteLock();
        {
        stop=System.currentTimeMillis(); 
        delay=turnDuration-(stop-start);
        aiMan.compute(delay<0?0:delay);

        buildPerceptions();
        }
      playerContainer.getLock().releaseLock();
            
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
        zone.nextTurn();
        scheduler.nextTurn();
        ruleProcessor.nextTurn();
        }
      playerContainer.getLock().releaseLock();
      
      stats.setObjectsNow(zone.size());
      }
    
    isfinished=true;
    marauroad.trace("RPServerManager::run","<");
    }
  }
