/* $Id: PythonRPRuleProcessor.java,v 1.1 2005/01/23 21:00:47 arianne_rpg Exp $ */
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
package marauroa.server.game.python;

import org.python.util.PythonInterpreter;
import org.python.core.*;

import java.util.*;
import java.io.*;

import marauroa.common.*;
import marauroa.common.game.*;
import marauroa.server.game.*;

public class PythonRPRuleProcessor implements IRPRuleProcessor
  {
  private GameScript gameScript;
  private PythonRP pythonRP;
  private RPServerManager rpman; 

  public PythonRPRuleProcessor() throws FileNotFoundException
    {
    }


  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPServerManager rpman, RPWorld world)
    {
    try
      {
      this.rpman=rpman;
      
      gameScript=GameScript.getGameScript();
      gameScript.setRPWorld(world);
      pythonRP=gameScript.getGameRules();
      }
    catch(Exception e)
      {
      Logger.thrown("PythonRPRuleProcessor::setContext","!",e);
      //@@@@@ System.exit(-1);
      }
    }

  /** Pass the whole list of actions so that it can approve or deny the actions in it.
   *  @param id the id of the object owner of the actions.
   *  @param actionList the list of actions that the player wants to execute. */
  public void approvedActions(RPObject.ID id, List<RPAction> actionList)
    {
    }

  /** Execute an action in the name of a player.
   *  @param id the id of the object owner of the actions.
   *  @param action the action to execute
   *  @return the action status, that can be Success, Fail or incomplete, please
   *      refer to Actions Explained for more info. */
  public RPAction.Status execute(RPObject.ID id, RPAction action)
    {
    Logger.trace("PythonRPRuleProcessor::execute",">");

    RPAction.Status status=RPAction.Status.FAIL;

    try
      {
      if(pythonRP.execute(id,action)==1)
        {
        status=RPAction.Status.SUCCESS;
		}
      }
    catch(Exception e)
      {
      Logger.thrown("PythonRPRuleProcessor::execute","X",e);
      }
    finally
      {
      Logger.trace("PythonRPRuleProcessor::execute","<");
      }

    return status;
    }

  /** Notify it when a new turn happens */
  synchronized public void nextTurn()
    {
    Logger.trace("PythonRPRuleProcessor::nextTurn",">");
    pythonRP.nextTurn();
    Logger.trace("PythonRPRuleProcessor::nextTurn","<");
    }

  synchronized public boolean onInit(RPObject object) throws RPObjectInvalidException
    {
    Logger.trace("PythonRPRuleProcessor::onInit",">");
    try
      {
      return pythonRP.onInit(object);
      }
    finally
      {
      Logger.trace("PythonRPRuleProcessor::onInit","<");
      }
    }

  synchronized public boolean onExit(RPObject.ID id)
    {
    Logger.trace("PythonRPRuleProcessor::onExit",">");
    try
      {
      return pythonRP.onExit(id);
      }
    catch(Exception e)
      {
      Logger.thrown("PythonRPRuleProcessor::onExit","X",e);
      return true;
      }
    finally
      {
      Logger.trace("PythonRPRuleProcessor::onExit","<");
      }
    }

  synchronized public boolean onTimeout(RPObject.ID id)
    {
    Logger.trace("PythonRPRuleProcessor::onTimeout",">");
    try
      {
      return pythonRP.onTimeout(id);
      }
    catch(Exception e)
      {
      Logger.thrown("PythonRPRuleProcessor::onTimeout","X",e);
      return true;
      }
    finally
      {
      Logger.trace("PythonRPRuleProcessor::onTimeout","<");
      }
    }
  }


