/* $Id: RPServerManager.java,v 1.18 2005/05/12 19:34:37 arianne_rpg Exp $ */
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

import java.util.*;
import java.io.*;
import java.net.*;

import marauroa.server.*;
import marauroa.server.net.*;
import marauroa.common.*;
import marauroa.common.net.*;
import marauroa.common.game.*;

/** This class is responsible for adding actions to scheduler, and to build and
 *  sent perceptions */
public class RPServerManager extends Thread
  {
  /** The thread will be running while keepRunning is true */
  private volatile boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private volatile boolean isfinished;
  /** The time elapsed between 2 turns. */
  private long turnDuration;
  /** The number of the turn that we are executing now */
  private int turn;
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

  private List<Integer> playersToRemove;


  private Map<RPObject.ID, List<TransferContent>> contentsToTransfer;

  /** Constructor
   *  @param netMan the NetworkServerManager so that we can send message */
  public RPServerManager(NetworkServerManager netMan) throws Exception
    {
    super("RPServerManager");
    Logger.trace("RPServerManager",">");
    try
      {
      stats=Statistics.getStatistics();
      keepRunning=true;
      isfinished=false;
      scheduler=new RPScheduler();
      contentsToTransfer=new HashMap<RPObject.ID, List<TransferContent>>();
      playerContainer=PlayerEntryContainer.getContainer();
      playersToRemove=new LinkedList<Integer>();
      this.netMan=netMan;

      Configuration conf=Configuration.getConfiguration();
      Class worldClass=Class.forName(conf.get("rp_RPWorldClass"));
      world=(RPWorld)worldClass.newInstance();
      world.setPlayerContainer(playerContainer);
      world.onInit();

      Class ruleProcessorClass=Class.forName(conf.get("rp_RPRuleProcessorClass"));
      ruleProcessor=(IRPRuleProcessor)ruleProcessorClass.newInstance();
      ruleProcessor.setContext(this,world);

      String duration=conf.get("rp_turnDuration");

      turnDuration=Long.parseLong(duration);
      turn=0;

      start();
      }
    catch(Exception e)
      {
      Logger.thrown("RPServerManager","X",e);
      Logger.trace("RPServerManager","!","ABORT: Unable to create RPZone, RPRuleProcessor or RPAIManager instances");
      throw e;
      }
    finally
      {
      Logger.trace("RPServerManager","<");
      }
    }

  public int getTurn()
    {
    return turn;
    }

  /** This method finish the thread that run the RPServerManager */
  public void finish()
    {
    Logger.trace("RPServerManager::finish",">");
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
      world.onFinish();
      }
    catch(Exception e)
      {
      Logger.thrown("RPServerManager::finish","X",e);
      }

    Logger.trace("RPServerManager::finish","<");
    }

  /** Adds an action for the next turn */
  public void addRPAction(RPAction action) throws ActionInvalidException
    {
    Logger.trace("RPServerManager::addRPAction",">");
    try
      {
      if(Logger.loggable("RPServerManager::addRPAction","D"))
        {
        Logger.trace("RPServerManager::addRPAction","D","Added action: "+action.toString());
        }

      scheduler.addRPAction(action,ruleProcessor);
      }
    finally
      {
      Logger.trace("RPServerManager::addRPAction","<");
      }
    }

  /** Returns an object of the world */
  public RPObject getRPObject(RPObject.ID id) throws RPObjectNotFoundException
    {
    Logger.trace("RPServerManager::getRPObject",">");
    try
      {
      IRPZone zone=world.getRPZone(id);
      return zone.get(id);
      }
    finally
      {
      Logger.trace("RPServerManager::getRPObject","<");
      }
    }
  
  public boolean checkGameVersion(String game, String version)
    {
    return ruleProcessor.checkGameVersion(game,version);
    }

  private Perception getPlayerPerception(PlayerEntryContainer.RuntimePlayerEntry entry)
    {
    Perception perception=null;

    IRPZone zone=world.getRPZone(entry.characterid);

    if(entry.perception_OutOfSync==false)
      {
      Logger.trace("RPServerManager::getPlayerPerception","D","Perception DELTA for player ("+entry.characterid.toString()+")");
      perception=zone.getPerception(entry.characterid,Perception.DELTA);
      }
    else
      {
      entry.perception_OutOfSync=false;
      Logger.trace("RPServerManager::getPlayerPerception","D","Perception SYNC for player ("+entry.characterid.toString()+")");
      perception=zone.getPerception(entry.characterid,Perception.SYNC);
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
    Logger.trace("RPServerManager::buildPerceptions",">");

    List<Integer> playersToUpdate=new LinkedList<Integer>();
    playersToRemove.clear();

    /** We reset the cache at Perceptions */
    MessageS2CPerception.clearPrecomputedPerception();

    PlayerEntryContainer.ClientIDIterator it=playerContainer.iterator();
    
    while(it.hasNext())
      {
      int clientid=it.next();

      try
        {
        PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);

        if(entry.state==PlayerEntryContainer.ClientStats.GAME_BEGIN)
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
        Logger.thrown("RPServerManager::buildPerceptions","X",e);
        Logger.trace("RPServerManager::buildPerceptions","X","Removing player("+clientid+") because it caused a Exception while contacting it");
        playersToRemove.add(new Integer(clientid));
        }
      }

    notifyUpdatesOnPlayer(playersToUpdate);

    Logger.trace("RPServerManager::buildPerceptions","<");
    }

  private void notifyUpdatesOnPlayer(List playersToNotify)
    {
    Logger.trace("RPServerManager::notifyUpdatesOnPlayer",">");
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
          Logger.thrown("RPServerManager::notifyUpdatesOnPlayer","X",e);
          Logger.trace("RPServerManager::notifyUpdatesOnPlayer","X","Can't update the player("+clientid+")");
          }
        }
      }
    catch(Exception e)
      {
      Logger.thrown("RPServerManager::notifyUpdatesOnPlayer","X",e);
      }
    finally
      {
      Logger.trace("RPServerManager::notifyUpdatesOnPlayer","<");
      }
    }

  private void notifyTimedoutPlayers(List playersToNotify)
    {
    Logger.trace("RPServerManager::notifyTimedoutPlayers",">");
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
            Logger.trace("RPServerManager::notifyTimedoutPlayers","W","Can't notify a player("+clientid+") that timedout because it never completed login");
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
          Logger.thrown("RPServerManager::notifyTimedoutPlayers","X",e);
          Logger.trace("RPServerManager::notifyTimedoutPlayers","X","Can't notify a player("+clientid+") that timedout");
          }
        finally
          {
          playerContainer.removeRuntimePlayer(clientid);
          Logger.trace("RPServerManager::notifyTimedoutPlayers","D","Notified player ("+clientid+")");
          }
        }
      }
    catch(Exception e)
      {
      Logger.thrown("RPServerManager::notifyTimedoutPlayers","X",e);
      Logger.trace("RPServerManager::notifyTimedoutPlayers","X","Can't notify a player(-not available-) that timedout");
      }
    finally
      {
      Logger.trace("RPServerManager::notifyTimedoutPlayers","<");
      }
    }

  /** This method is called when a player is added to the game */
  public boolean onInit(RPObject object) throws RPObjectInvalidException
    {
    return ruleProcessor.onInit(object);
    }

  /** This method is called when a player leave to the game */
  public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException
    {
    scheduler.clearRPActions(id);
    return ruleProcessor.onExit(id);
    }

  private void deliverTransferContent()
    {
    synchronized(contentsToTransfer)
      {
      for(RPObject.ID id: contentsToTransfer.keySet())
        {
        try
          {
          List<TransferContent> content=contentsToTransfer.get(id);

          int clientid=playerContainer.getClientidPlayer(id);
          PlayerEntryContainer.RuntimePlayerEntry entry=playerContainer.get(clientid);

          entry.contentToTransfer=content;

          MessageS2CTransferREQ mes=new MessageS2CTransferREQ(entry.source,content);
          mes.setClientID(entry.clientid);

          netMan.addMessage(mes);
          }
        catch(NoSuchClientIDException e)
          {
          Logger.thrown("RPServerManager::deliverTransferContent","X",e);
          }
        }

      contentsToTransfer.clear();
      }
    }


  /** This method is triggered to send content to the clients */
  public void transferContent(RPObject.ID id, List<TransferContent> content)
    {
    Logger.trace("RPServerManager::transferContent",">");
    synchronized(contentsToTransfer)
      {
      contentsToTransfer.put(id,content);
      }
    Logger.trace("RPServerManager::transferContent","<");
    }

  /** This method is triggered to send content to the clients */
  public void transferContent(RPObject.ID id, TransferContent content)
    {
    List<TransferContent> list=new LinkedList<TransferContent>();
    list.add(content);

    transferContent(id, list);
    }

  public void run()
    {
    try
      {
      Logger.trace("RPServerManager::run",">");
      long start=System.currentTimeMillis(),stop,delay;

      while(keepRunning)
        {
        stop=System.currentTimeMillis();
        try
          {
          Logger.trace("RPServerManager::run","D","Turn time elapsed: "+Long.toString(stop-start));
          delay=turnDuration-(stop-start);
          if(delay<0)
            {
            Logger.trace("RPServerManager::run","W","Turn duration overflow by "+(-delay)+" ms");
            }
            
          Thread.sleep(delay<0?0:delay);
          }
        catch(InterruptedException e)
          {
          }
        start=System.currentTimeMillis();

        try
          {
          playerContainer.getLock().requestWriteLock();
            {
            /** Get actions that players send */
            scheduler.nextTurn();

            /** Execute them all */
            scheduler.visit(ruleProcessor);

            /** Compute game RP rules to move to the next turn */
            ruleProcessor.endTurn();

            /** Send content that is waiting to players */
            deliverTransferContent();

            /** Tell player what happened */
            buildPerceptions();

            /** wait until all messages are sent away **/
            /** NOTE: Disabled while NetworkServerManager is fixed.
            netMan.flushMessages();
            */

            /** Move zone to the next turn */
            world.nextTurn();

            /** Remove timeout players */
            notifyTimedoutPlayers(playersToRemove);
            
            turn++;

            ruleProcessor.beginTurn();
            }
          }
        finally
          {
          playerContainer.getLock().releaseLock();
          }

        stats.setObjectsNow(world.size());
        }
      }
    catch(Throwable e)
      {
      Logger.trace("RPServerManager::run", "!", "Unhandled exception, server will shut down.");
      Logger.thrown("RPServerManager::run", "!", e);
      }
    finally
      {
      isfinished=true;
      Logger.trace("RPServerManager::run","<");
      }
    }
  }
