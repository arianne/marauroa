/* $Id: PythonRPRuleProcessor.java,v 1.5 2004/05/31 14:13:09 arianne_rpg Exp $ */
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
package marauroa.game.python;

import org.python.util.PythonInterpreter;
import org.python.core.*;

import marauroa.game.*;
import marauroa.*;
import java.util.*;
import java.io.*;

public class PythonRPRuleProcessor implements RPRuleProcessor
  {
  private GameScript gameScript;
  private PythonRP pythonRP;

  public PythonRPRuleProcessor() throws Configuration.PropertyNotFoundException, Configuration.PropertyFileNotFoundException
    {
    }


  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPZone zone)
    {
    try
      {
      gameScript=GameScript.getGameScript();
      gameScript.setRPZone(zone);
      pythonRP=gameScript.getGameRules();
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPRuleProcessor::setContext","!",e);
      System.exit(-1);
      }
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
   *  @return the action status, that can be Success, Fail or incomplete, please
   *      refer to Actions Explained for more info. */
  public RPAction.Status execute(RPObject.ID id, RPAction action)
    {
    marauroad.trace("PythonRPRuleProcessor::execute",">");

    RPAction.Status status=RPAction.STATUS_FAIL;

    try
      {
      if(pythonRP.execute(id,action)==1)
        {
		status=RPAction.STATUS_SUCCESS;
		}
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPRuleProcessor::execute","X",e);
      }
    finally
      {
      marauroad.trace("PythonRPRuleProcessor::execute","<");
      }

    return status;
    }

  /** Notify it when a new turn happens */
  synchronized public void nextTurn()
    {
    marauroad.trace("PythonRPRuleProcessor::nextTurn",">");
    pythonRP.nextTurn();
    marauroad.trace("PythonRPRuleProcessor::nextTurn","<");
    }

  synchronized public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    marauroad.trace("PythonRPRuleProcessor::onInit",">");
    try
      {
      return pythonRP.onInit(object);
      }
    finally
      {
      marauroad.trace("PythonRPRuleProcessor::onInit","<");
      }
    }

  synchronized public boolean onExit(RPObject.ID id)
    {
    marauroad.trace("PythonRPRuleProcessor::onExit",">");
    try
      {
      return pythonRP.onExit(id);
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPRuleProcessor::onExit","X",e);
      return true;
      }
    finally
      {
      marauroad.trace("PythonRPRuleProcessor::onExit","<");
      }
    }

  synchronized public boolean onTimeout(RPObject.ID id)
    {
    return onExit(id);
    }

  synchronized public List buildMapObjectsList(RPObject.ID id)
    {
    marauroad.trace("PythonRPRuleProcessor::serializeMap",">");
    try
      {
      return pythonRP.buildMapObjectsList();
      }
    catch(Exception e)
      {
      marauroad.thrown("PythonRPRuleProcessor::serializeMap","X",e);
      return null;
      }
    finally
      {
      marauroad.trace("PythonRPRuleProcessor::serializeMap","<");
      }
    }
  }


