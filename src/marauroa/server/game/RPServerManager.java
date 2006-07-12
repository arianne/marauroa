/* $Id: RPServerManager.java,v 1.33 2006/07/12 16:56:43 nhnb Exp $ */
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.IRPZone;
import marauroa.common.game.Perception;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.net.MessageS2CPerception;
import marauroa.common.net.MessageS2CTransferREQ;
import marauroa.common.net.TransferContent;
import marauroa.server.net.NetworkServerManager;
import marauroa.server.createaccount;

import org.apache.log4j.Logger;


/** This class is responsible for adding actions to scheduler, and to build and
 *  sent perceptions */
public class RPServerManager extends Thread
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(RPServerManager.class);
  
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
    Log4J.startMethod(logger, "RPServerManager");
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
      logger.warn("ABORT: Unable to create RPZone, RPRuleProcessor or RPAIManager instances",e);
      throw e;
      }
    finally
      {
      Log4J.finishMethod(logger, "RPServerManager");
      }
    }

  public int getTurn()
    {
    return turn;
    }

  /** This method finish the thread that run the RPServerManager */
  public void finish()
    {
    Log4J.startMethod(logger, "finish");
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
      logger.error("error while finishing RPServerManager",e);
      }

    Log4J.finishMethod(logger, "finish");
    }

  /** Adds an action for the next turn */
  public void addRPAction(RPAction action) throws ActionInvalidException
    {
    Log4J.startMethod(logger, "addRPAction");
    try
      {
      if(logger.isDebugEnabled())
        {
        logger.debug("Added action: "+action);
        }

      scheduler.addRPAction(action,ruleProcessor);
      }
    finally
      {
      Log4J.finishMethod(logger, "addRPAction");
      }
    }

  /** Returns an object of the world */
  public RPObject getRPObject(RPObject.ID id) throws RPObjectNotFoundException
    {
    Log4J.startMethod(logger, "getRPObject");
    try
      {
      IRPZone zone=world.getRPZone(id);
      return zone.get(id);
      }
    finally
      {
      Log4J.finishMethod(logger, "getRPObject");
      }
    }
  
  public boolean checkGameVersion(String game, String version)
    {
    return ruleProcessor.checkGameVersion(game,version);
    }
  
  public createaccount.Result createAccount(String username, String password, String email)
    {
    return ruleProcessor.createAccount(username,password,email);
    }

  private Perception getPlayerPerception(PlayerEntryContainer.RuntimePlayerEntry playerEntry)
    {
    Perception perception=null;

    IRPZone zone=world.getRPZone(playerEntry.characterid);

    if(playerEntry.perception_OutOfSync==false)
      {
      logger.debug("Perception DELTA for player ("+playerEntry.characterid+")");
      perception=zone.getPerception(playerEntry.characterid,Perception.DELTA);
      }
    else
      {
      playerEntry.perception_OutOfSync=false;
      logger.debug("Perception SYNC for player ("+playerEntry.characterid+")");
      perception=zone.getPerception(playerEntry.characterid,Perception.SYNC);
      }

    return perception;
    }

  private void sendPlayerPerception(PlayerEntryContainer.RuntimePlayerEntry entry,Perception perception, RPObject object)
    {
    if(perception!=null)
      {
      MessageS2CPerception messages2cPerception=new MessageS2CPerception(entry.source, perception);
      
      stats.add("Perceptions "+(perception.type==0?"DELTA":"SYNC"),1);

      RPObject copy=(RPObject)object.clone();

      if(perception.type==Perception.SYNC)
        {
        copy.clearVisible();      
        messages2cPerception.setMyRPObject(copy,null);
        }
      else
        {
        RPObject added=new RPObject();
        RPObject deleted=new RPObject();
        
        try
          {
          copy.getDifferences(added,deleted);
          added.clearVisible();
          deleted.clearVisible();
  
          if(added.size()==0)
            {
            added=null;
            }
  
          if(deleted.size()==0)
            {
            deleted=null;
            }            
          }
        catch(Exception e)
          {
          logger.error("Error getting object differences", e);
          logger.error(object);
          logger.error(copy);
          added=null;
          deleted=null;
          }
  
        messages2cPerception.setMyRPObject(added,deleted);
        }
        
      messages2cPerception.setClientID(entry.clientid);
      messages2cPerception.setPerceptionTimestamp(entry.getPerceptionTimestamp());

      netMan.sendMessage(messages2cPerception);
      }
    }

  private void buildPerceptions()
    {
    Log4J.startMethod(logger, "buildPerceptions");

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

        if(entry.state==PlayerEntryContainer.ClientState.GAME_BEGIN)
          {
          Perception perception=getPlayerPerception(entry);
          IRPZone zone=world.getRPZone(entry.characterid);
          RPObject object=zone.get(entry.characterid);

          sendPlayerPerception(entry,perception,object);

          /** We check if we need to update player in the database */
// BUG: A fix to reduce database lag related.          
//          if(entry.shouldStoredUpdate(object))
//            {
//            playersToUpdate.add(new Integer(clientid));
//            }
          }

        if(entry.isTimedout())
          {
          playersToRemove.add(new Integer(clientid));
          }
        }
      catch(Exception e)
        {
        logger.error("Removing player("+clientid+") because it caused a Exception while contacting it",e);
        playersToRemove.add(new Integer(clientid));
        }
      }

    notifyUpdatesOnPlayer(playersToUpdate);

    Log4J.finishMethod(logger, "buildPerceptions");
    }

  private void notifyUpdatesOnPlayer(List playersToNotify)
    {
    Log4J.startMethod(logger, "notifyUpdatesOnPlayer");
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
          logger.error("Can't update the player("+clientid+")",e);
          }
        }
      }
    catch(Exception e)
      {
      logger.error("error while notifyUpdatesOnPlayer",e);
      }
    finally
      {
      Log4J.finishMethod(logger, "notifyUpdatesOnPlayer");
      }
    }

  private void notifyTimedoutPlayers(List playersToNotify)
    {
    Log4J.startMethod(logger, "notifyTimedoutPlayers");
    try
      {
      Iterator it_notified=playersToNotify.iterator();

      while(it_notified.hasNext())
        {
        int clientid=((Integer)it_notified.next()).intValue();

        try
          {
          stats.add("Players timeout",1);

          RPObject.ID id=playerContainer.getRPObjectID(clientid);
          if(id==null)
            {
            logger.debug("Can't notify a player("+clientid+") that timedout because it never completed login");
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
          logger.error("Can't notify a player("+clientid+") that timedout");
          }
        finally
          {
          playerContainer.removeRuntimePlayer(clientid);
          logger.debug("Notified player ("+clientid+")");
          }
        }
      }
    catch(Exception e)
      {
      logger.error("Can't notify a player(-not available-) that timedout",e);
      }
    finally
      {
      Log4J.finishMethod(logger, "notifyTimedoutPlayers");
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

          netMan.sendMessage(mes);
          }
        catch(NoSuchClientIDException e)
          {
          logger.error("Unable to find client id for player: "+id,e);
          }
        }

      contentsToTransfer.clear();
      }
    }


  /** This method is triggered to send content to the clients */
  public void transferContent(RPObject.ID id, List<TransferContent> content)
    {
    Log4J.startMethod(logger, "transferContent");
    synchronized(contentsToTransfer)
      {
      contentsToTransfer.put(id,content);
      }
    Log4J.finishMethod(logger, "transferContent");
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
      Log4J.startMethod(logger, "run");
      long start = System.nanoTime();
      long stop;
      long delay;

      while(keepRunning)
        {
        stop=System.nanoTime();
        try
          {
          logger.info("Turn time elapsed: "+((stop-start)/1000)+" microsecs");
          delay=turnDuration-((stop-start)/1000000);
          if(delay<0)
            {
            logger.warn("Turn duration overflow by "+(-delay)+" ms");
            }
          else if(delay>turnDuration)
            {
            logger.error("Delay bigger than Turn duration. [delay: "+delay+"] [turnDuration:"+turnDuration+"]");
            delay=0;
            }
          
          // only sleep when the turn delay is > 0
          if (delay > 0)
            {
            Thread.sleep(delay);
            }
          }
        catch(InterruptedException e)
          {
          }
        start=System.nanoTime();

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

        stats.set("Objects now",world.size());
        }
      }
    catch(Throwable e)
      {
      logger.fatal("Unhandled exception, server will shut down.",e);
      }
    finally
      {
      isfinished=true;
      Log4J.finishMethod(logger, "run");
      }
    }
  }
