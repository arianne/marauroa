package marauroa.game;

import java.util.*;
import java.io.*;

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
    keepRunning=false;
  }
  
  public void addRPAction(RPAction action) throws RPScheduler.ActionInvalidException
  {
    marauroad.trace("RPServerManager::addRPAction",">");
    marauroad.trace("RPServerManager::addRPAction","D","Added action: "+action.toString());
    scheduler.addRPAction(action);
    marauroad.trace("RPServerManager::addRPAction","<");
  }
  
  public void addRPObject(RPObject object) throws RPZone.RPObjectInvalidException
  {
    marauroad.trace("RPServerManager::addRPObject",">");
    marauroad.trace("RPServerManager::addRPObject","D","Added object: "+object.toString());
    zone.add(object);
    marauroad.trace("RPServerManager::addRPObject","<");
  }
  
  public RPObject getRPObject(RPObject.ID id) throws RPZone.RPObjectNotFoundException
  {
    marauroad.trace("RPServerManager::getRPObject",">");
    RPObject object=zone.get(id);
    marauroad.trace("RPServerManager::getRPObject","<");
    return object;
  }
  
  public boolean hasRPObject(RPObject.ID id)
  {
    marauroad.trace("RPServerManager::hasRPObject",">");
    boolean has=zone.has(id);
    marauroad.trace("RPServerManager::hasRPObject","<");
    return has;
  }
  
  public void removeRPObject(RPObject.ID id) throws RPZone.RPObjectNotFoundException
  {
    marauroad.trace("RPServerManager::removeRPObject",">");
    marauroad.trace("RPServerManager::removeRPObject","D","Removed object: "+id.toString());
    zone.remove(id);
    marauroad.trace("RPServerManager::removeRPObject","<");
  }
  
  public void run()
  {
    marauroad.trace("RPServerManager::run",">");
    
    while(keepRunning)
    {
      scheduler.visit(ruleProcessor);
      scheduler.nextTurn();
      
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

