/* $Id: RPServerManager.java,v 1.55 2004/03/27 10:54:05 arianne_rpg Exp $ */
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
class RPServerManager extends Thread
  {
  /** We send 1 TOTAL perception each TOTAL_PERCEPTION_RELATION DELTA perceptions */
  private final static int TOTAL_PERCEPTION_RELATION=60;
  
  /** The thread will be running while keepRunning is true */
  private boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;
  /** The time elapsed between 2 turns. */
  private long turnDuration;
  /** The scheduler needed to organize actions */
  private RPScheduler scheduler;
  /** The ruleProcessor that the scheduler will use to execute the actions */
  private RPRuleProcessor ruleProcessor;
  /** The place where the objects are stored */
  private RPZone zone;
  private Statistics stats;
  /** The networkServerManager so that we can send perceptions */
  private NetworkServerManager netMan;
  /** The PlayerEntryContainer so that we know where to send perceptions */
  private PlayerEntryContainer playerContainer;
  
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

      zone=(RPZone)zoneClass.newInstance();
      
      Class ruleProcessorClass=Class.forName(conf.get("rp_RPRuleProcessorClass"));

      ruleProcessor=(RPRuleProcessor)ruleProcessorClass.newInstance();
      ruleProcessor.setContext(zone);
      
      String duration =conf.get("rp_turnDuration");

      turnDuration = Long.parseLong(duration);
      start();
      }
    catch(Exception e)
      {
      marauroad.trace("RPServerManager","X",e.getMessage());
      marauroad.trace("RPServerManager","!","ABORT: Unable to create RPZone and RPRuleProcessor instances");
      System.exit(-1);
      }
    finally
      {    
      marauroad.trace("RPServerManager","<");
      }
    }
  
  /** Constructor 
   *  @param netMan the NetworkServerManager so that we can send message */
  public RPServerManager(NetworkServerManager netMan, RPZone zone, RPRuleProcessor ruleProcessor, long turnDuration)
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
    marauroad.trace("RPServerManager::finish","<");
    }
  
  public void addRPAction(RPAction action) throws RPScheduler.ActionInvalidException
    {
    marauroad.trace("RPServerManager::addRPAction",">");
    try
      {
      marauroad.trace("RPServerManager::addRPAction","D","Added action: "+action.toString());
      scheduler.addRPAction(action);
      }
    finally
      {
      marauroad.trace("RPServerManager::addRPAction","<");
      }
    }
    
  public void addRPObject(RPObject object) throws RPZone.RPObjectInvalidException
    {
    marauroad.trace("RPServerManager::addRPObject",">");
    try
      {
      stats.addObjectAdded();
      marauroad.trace("RPServerManager::addRPObject","D","Added object: "+object.toString());
      zone.add(object);
      }
    finally
      {
      marauroad.trace("RPServerManager::addRPObject","<");
      }
    }
  
  public RPObject getRPObject(RPObject.ID id) throws RPZone.RPObjectNotFoundException
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
  
  public RPObject removeRPObject(RPObject.ID id) throws RPZone.RPObjectNotFoundException
    {
    marauroad.trace("RPServerManager::removeRPObject",">");
    try
      {
      stats.addObjectRemoved();
      marauroad.trace("RPServerManager::removeRPObject","D","Removed object: "+id.toString());
      return zone.remove(id);
      }
    finally
      {
      marauroad.trace("RPServerManager::removeRPObject","<");
      }
    }
  private int deltaPerceptionSend=0;
  private void buildPerceptions()
    {
    marauroad.trace("RPServerManager::buildPerceptions",">");

    List playersToRemove=new LinkedList();
    
    try
      {
      playerContainer.getLock().requestWriteLock();
      ++deltaPerceptionSend;

      PlayerEntryContainer.ClientIDIterator it=playerContainer.iterator();
      
      while(it.hasNext())
        {
        int clientid=it.next();
        
        try
          {
          if(playerContainer.getRuntimeState(clientid)==playerContainer.STATE_GAME_BEGIN)
            {
            InetSocketAddress source=playerContainer.getInetSocketAddress(clientid);
            RPZone.Perception perception;
            RPObject object=zone.get(playerContainer.getRPObjectID(clientid));

            if(deltaPerceptionSend>TOTAL_PERCEPTION_RELATION || object.has("?joined"))
              {
              marauroad.trace("RPServerManager::buildPerceptions","D","Perception TOTAL for player ("+playerContainer.getRPObjectID(clientid).toString()+")");
              perception=zone.getPerception(playerContainer.getRPObjectID(clientid),RPZone.Perception.TOTAL);
              }
            else
              {
              marauroad.trace("RPServerManager::buildPerceptions","D","Perception DELTA for player ("+playerContainer.getRPObjectID(clientid).toString()+")");
              perception=zone.getPerception(playerContainer.getRPObjectID(clientid),RPZone.Perception.DELTA);
              }
            
            MessageS2CPerception messages2cPerception=new MessageS2CPerception(source, perception);

            messages2cPerception.setMyRPObject(object);
            messages2cPerception.setClientID(clientid);
            netMan.addMessage(messages2cPerception);            
          
            /** We check if we need to update player in the database */
            if(playerContainer.shouldStoredUpdate(clientid))
              {
              try
                {
                playerContainer.setRPObject(clientid,object);
                }
              catch(Exception e)
                {
                marauroad.trace("RPServerManager::buildPerceptions","X",e.getMessage());
                }
              }
            }
            
          if(playerContainer.timedout(clientid))
            {
            playersToRemove.add(new Integer(clientid));
            }          
          }
        catch(Exception e)
          {
          marauroad.trace("RPServerManager::buildPerceptions","X",e.getMessage());
          }
        }
      if(deltaPerceptionSend>TOTAL_PERCEPTION_RELATION)
        {
        deltaPerceptionSend=0;
        }
      // NOTE: If we use the Read/Write lock, it can happen a race condition between 
      // unlock and lock that may result in a bad operation.
      //
      // playerContainer.getLock().releaseLock();
      // /* Removing the players is a write operation */
      // playerContainer.getLock().requestWriteLock();

      notifyTimedoutPlayers(playersToRemove);
      }      
    finally
      {
      playerContainer.getLock().releaseLock();
      marauroad.trace("RPServerManager::buildPerceptions","<");
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
          RPObject.ID id=playerContainer.getRPObjectID(clientid);
          RPObject object=getRPObject(id);

          if(ruleProcessor.onTimeout(id))
            {
            /* NOTE: Set the Object so that it is stored in Database */
            playerContainer.setRPObject(clientid,object);  
            }      
          }
        catch(Exception e)
          {
          marauroad.trace("RPServerManager::notifyTimedoutPlayers","X","Can't notify a player("+clientid+") that timedout");
          marauroad.trace("RPServerManager::notifyTimedoutPlayers","X",e.getMessage());
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
      marauroad.trace("RPServerManager::notifyTimedoutPlayers","X","Can't notify a player(-not available-) that timedout");
      marauroad.trace("RPServerManager::notifyTimedoutPlayers","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("RPServerManager::notifyTimedoutPlayers","<");
      }
    }

  public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    return ruleProcessor.onInit(object);
    }
    
  public boolean onExit(RPObject.ID id) throws RPZone.RPObjectNotFoundException
    {
    return ruleProcessor.onExit(id);
    }
  
  public void run()
    {
    marauroad.trace("RPServerManager::run",">");
    while(keepRunning)
      {
      scheduler.visit(ruleProcessor);      
      try
        {
        Thread.sleep(turnDuration);
        }
      catch(InterruptedException e)
        {
        }
      buildPerceptions();

      zone.nextTurn();      
      scheduler.nextTurn();      
      ruleProcessor.nextTurn();
      stats.setObjectsNow(zone.size());
      }
    isfinished=true;    
    marauroad.trace("RPServerManager::run","<");
    }
  }
