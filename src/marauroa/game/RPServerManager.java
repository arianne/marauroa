package marauroa.game;

import java.util.*;
import java.io.*;

import marauroa.net.*;
import marauroa.*;


public class RPServerManager extends Thread
  {
  private boolean keepRunning;
  private RPScheduler scheduler;
  private RPRuleProcessor ruleProcessor;
  private RPZone zone;
  private long turnDuration;
  
  public RPServerManager()
    {
    super("RPServerManager");
    
    marauroad.trace("RPServerManager",">");
    
    keepRunning=true;
    scheduler=new RPScheduler();
    turnDuration = 60000;
        
    try
      {
      Configuration conf=Configuration.getConfiguration();
      Class zoneClass=Class.forName(conf.get("rp_RPZoneClass"));
      zone=(RPZone)zoneClass.newInstance();
    
      Class ruleProcessorClass=Class.forName(conf.get("rp_RPRuleProcessorClass"));
      ruleProcessor=(RPRuleProcessor)ruleProcessorClass.newInstance();
      ruleProcessor.setContext(zone);
      
      try
      {
        String duration =conf.get("rp_turnDuration");
        turnDuration = Long.parseLong(duration);
      }
      catch(Exception e)
      {
        turnDuration = 60000;
        marauroad.trace("RPServerManager","D","Turn duration set to default("+turnDuration+")");
      }
      
      }
    catch(Throwable e)
      {
      marauroad.trace("RPServerManager","X",e.getMessage());
      marauroad.trace("RPServerManager","!","ABORT: Unable to create RPZone and RPRuleProcessor instances");
      System.exit(-1);
      }
    
    marauroad.trace("RPServerManager","<");
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
