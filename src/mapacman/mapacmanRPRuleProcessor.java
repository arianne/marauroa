/* $Id: mapacmanRPRuleProcessor.java,v 1.1 2004/04/23 18:45:22 arianne_rpg Exp $ */
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
package mapacman;

import org.python.util.PythonInterpreter;
import org.python.core.*;

import marauroa.game.*;
import marauroa.*;
import java.util.*;

public class mapacmanRPRuleProcessor implements RPRuleProcessor
  {
  private mapacmanRPZone zone;
  private PythonInterpreter interpreter;
  private PythonRP pythonRP;
 
  public mapacmanRPRuleProcessor()
    {
    Configuration.setConfigurationFile("mapacman.ini");
    
    zone=null;
    interpreter=new PythonInterpreter();
    interpreter.execfile("mapacman_script.py");

    PyInstance object=(PyInstance)interpreter.eval("RealPythonRP()");
    pythonRP=(PythonRP)object.__tojava__(PythonRP.class);
    }

  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPZone zone)
    {
    this.zone=(mapacmanRPZone)zone;
    interpreter.set("zone",this.zone);
    interpreter.set("ruleprocessor",this);
    }
    
  public mapacmanRPZone getRPZone()
    {
    return zone;
    }
    
  /** Pass the whole list of actions so that it can approve or deny the actions in it.
   *  @param id the id of the object owner of the actions.
   *  @param actionList the list of actions that the player wants to execute. */
  public void approvedActions(RPObject.ID id, RPActionList actionList)
    {
    }

  /** Execute an action in the name of a player.
   *  @param id the id of the object owner of the actions.
   *  @param action the action to execute
   *  @returns the action status, that can be Success, Fail or incomplete, please 
   *      refer to Actions Explained for more info. */
  public RPAction.Status execute(RPObject.ID id, RPAction action)
    {
    marauroad.trace("mapacmanRPRuleProcessor::execute",">");

    RPAction.Status status=RPAction.STATUS_FAIL;
    
    try
      {
      pythonRP.execute(id,action);
      
      return status;
      }
    catch(Exception e)
      {
      marauroad.trace("mapacmanRPRuleProcessor::execute","X",e.getMessage());
      e.printStackTrace();
      return RPAction.STATUS_FAIL;      
      }      
    finally
      {
      marauroad.trace("mapacmanRPRuleProcessor::execute","<");
      }
    }
  
  /** Notify it when a new turn happens */
  synchronized public void nextTurn()
    {
    marauroad.trace("mapacmanRPRuleProcessor::nextTurn",">");    
    pythonRP.nextTurn();    
    marauroad.trace("mapacmanRPRuleProcessor::nextTurn","<");
    }
  
  synchronized public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    marauroad.trace("mapacmanRPRuleProcessor::onInit",">");
    try
      {
      return pythonRP.onInit(object);
      }
    finally
      {
      marauroad.trace("mapacmanRPRuleProcessor::onInit","<");
      }
    }
    
  synchronized public boolean onExit(RPObject.ID id)
    {
    marauroad.trace("mapacmanRPRuleProcessor::onExit",">");
    try
      {
      return pythonRP.onExit(id);
      }
    catch(Exception e)
      {
      e.printStackTrace(System.out);
      }
    finally
      {
      marauroad.trace("mapacmanRPRuleProcessor::onExit","<");
      }
    return true;
    }
    
  synchronized public boolean onTimeout(RPObject.ID id)
    {
    return onExit(id);
    }

  public static void main(String[] args) throws Exception
    {
    try
      {
      long init=System.currentTimeMillis();
      mapacmanRPRuleProcessor pacmanRP=new mapacmanRPRuleProcessor();
//      pacmanRP.interpreter.eval("mapacmanRPMap('map_definition.txt').get(0,0)");

      RPObject player=new RPObject();
      player.put("id",1);
      player.put("name",1);
      player.put("x",0);
      player.put("y",0);
      player.put("dir","N");
      player.put("score",0);
    
      pacmanRP.setContext(new mapacmanRPZone());
      pacmanRP.onInit(player);
      
      long start=System.currentTimeMillis();
      for(int j=0;j<1;++j)
        {
        RPAction action=new RPAction();
        action.put("source_id","1");
        action.put("type","turn");
        action.put("dir","S");
        
        pacmanRP.execute(new RPObject.ID(action),action);        
        System.out.println(player);
        }        
      
      pacmanRP.nextTurn();
      System.out.println(player);

      pacmanRP.nextTurn();
      System.out.println(player);

      pacmanRP.nextTurn();
      System.out.println(player);

      pacmanRP.nextTurn();
      System.out.println(player);

      pacmanRP.nextTurn();
      System.out.println(player);

      long stop=System.currentTimeMillis();
      System.out.println("Python load: "+(start-init));
      System.out.println("Python execute: "+(stop-start));
      }
    catch(Exception e)
      {
      e.printStackTrace();
      }
    }
    
  }  

