package marauroa.game;

import java.util.*;
import java.io.*;

import marauroa.net.*;
import marauroa.marauroad;


public class RPServerManager extends Thread
  {
  private boolean keepRunning;
  private RPScheduler scheduler;
  private RPRuleProcessor ruleProcessor;
  private RPZone zone;
  
  public RPServerManager()
    {
    super("RPServerManager");
    
    keepRunning=true;    
    scheduler=new RPScheduler();
    zone=new RPZone();
    ruleProcessor=new RPRuleProcessor(zone);
    }

  public void finish()
    {
    keepRunning=false;
    }
    
  public void addRPAction(RPAction action) throws RPScheduler.ActionInvalidException
    {
    scheduler.addRPAction(action);
    }
    
  public void addRPObject(RPObject object) throws RPZone.RPObjectInvalidException
    {
    zone.add(object);
    }
    
  public void removeRPObject(RPObject.ID id) throws RPZone.RPObjectNotFoundException
    {
    zone.remove(id);
    }
    
  public void run()
    {
    marauroa.marauroad.report("Start thread "+this.getName());
    while(keepRunning)
      {
      scheduler.visit(ruleProcessor);
      scheduler.nextTurn();
      }

    marauroa.marauroad.report("End thread "+this.getName());
    }
  }