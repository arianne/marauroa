/* $Id: the1001RPRuleProcessor.java,v 1.4 2003/12/29 11:02:47 arianne_rpg Exp $ */
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
package the1001;

import marauroa.game.*;
import marauroa.*;

public class the1001RPRuleProcessor implements RPRuleProcessor
  {
  the1001RPZone zone;
  
  public the1001RPRuleProcessor()
    {
    zone=null;
    RPCode.setCallback(this);
    }

  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPZone zone)
    {
    }
    
  public the1001RPZone getRPZone()
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
    marauroad.trace("the1001RPRuleProcessor::execute",">");
    
    try
      {
      if(action.get("type")=="buy")
        {
        String item_type=action.get("item_type");
        String item_id=action.get("item_id");
        return RPCode.Buy(id, item_type, item_id);
        }
      else
        {
        return RPAction.STATUS_FAIL;
        }
      }
    catch(Exception e)
      {
      return RPAction.STATUS_FAIL;      
      }      
    finally
      {
      marauroad.trace("the1001RPRuleProcessor::execute","<");
      }
    }
    
  /** Notify it when a new turn happens */
  public void nextTurn()
    {
    marauroad.trace("the1001RPRuleProcessor::nextTurn",">");
    marauroad.trace("the1001RPRuleProcessor::nextTurn","<");
    }

  public boolean onInit(RPObject object)
    {
    return true;
    }
    
  public boolean onExit(RPObject.ID id)
    {
    return true;
    }
    
  public boolean onTimeout(RPObject.ID id)
    {
    return true;
    }
  }
    
  