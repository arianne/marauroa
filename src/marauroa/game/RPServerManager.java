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
  /** The thread will be running while keepRunning is true */
  private boolean keepRunning;
  /** The time elapsed between 2 turns. */
  private long turnDuration;
  
  /** The scheduler needed to organize actions */
  private RPScheduler scheduler;
  /** The ruleProcessor that the scheduler will use to execute the actions */
  private RPRuleProcessor ruleProcessor;
  /** The place where the objects are stored */
  private RPZone zone;
  
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
      keepRunning=true;
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
  
  public void finish()
    {
    marauroad.trace("RPServerManager::finish",">");
    keepRunning=false;
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
  
  public void removeRPObject(RPObject.ID id) throws RPZone.RPObjectNotFoundException
    {
    marauroad.trace("RPServerManager::removeRPObject",">");
    
    try
      {
      marauroad.trace("RPServerManager::removeRPObject","D","Removed object: "+id.toString());
      zone.remove(id);
      }
    finally
      {
      marauroad.trace("RPServerManager::removeRPObject","<");
      }
    }
    
  private void buildPerceptions()
    {
    marauroad.trace("RPServerManager::buildPerceptions",">");
    
    try
      {
      PlayerEntryContainer.ClientIDIterator it=playerContainer.iterator();
    
      while(it.hasNext())
        {
        short clientid=it.next();
        
        try
          {
          if(playerContainer.getRuntimeState(clientid)==playerContainer.STATE_GAME_BEGIN)
            {
            InetSocketAddress source=playerContainer.getInetSocketAddress(clientid);
            RPZone.Perception perception=zone.getPerception(playerContainer.getRPObjectID(clientid));
            Message messages2cPerception=new MessageS2CPerception(source, perception.modifiedList, perception.deletedList);
            netMan.addMessage(messages2cPerception);
            }
          }
        catch(Exception e)
          {
          marauroad.trace("RPServerManager::buildPerceptions","D",e.getMessage());
          }
        }
      }      
    finally
      {
      marauroad.trace("RPServerManager::buildPerceptions","<");
      }
    }
  
  public void run()
    {
    marauroad.trace("RPServerManager::run",">");
    
    while(keepRunning)
      {
      scheduler.visit(ruleProcessor);
      scheduler.nextTurn();
      
      buildPerceptions();
      
      try
        {
        Thread.sleep(turnDuration);
        }
      catch(InterruptedException e)
        {
        }
      }
    
    marauroad.trace("RPServerManager::run","<");
    }
  }

