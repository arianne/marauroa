/* $Id: the1001RPRuleProcessor.java,v 1.7 2003/12/30 08:10:47 arianne_rpg Exp $ */
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
import java.util.*;

public class the1001RPRuleProcessor implements RPRuleProcessor
  {
  private the1001RPZone zone;
  private List trackedObjects;
  
  public the1001RPRuleProcessor()
    {
    zone=null;
    trackedObjects=new LinkedList();
    RPCode.setCallback(this);
    }

  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPZone zone)
    {
    this.zone=(the1001RPZone)zone;
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
      marauroad.trace("the1001RPRuleProcessor::execute","X",e.getMessage());
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
    removeOneTurnAttributes();      
    marauroad.trace("the1001RPRuleProcessor::nextTurn","<");
    }
  
  private void removeOneTurnAttributes()
    {
    marauroad.trace("the1001RPRuleProcessor::removeOneTurnAttributes",">");        
    try
      {
      Iterator it=trackedObjects.iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        Iterator attributesit=object.iterator();
      
        while(attributesit.hasNext())
          {
          String attr=(String)attributesit.next();
          if(attr.charAt(0)=='?')
            {
            object.remove(attr);
            }
          }
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.trace("the1001RPRuleProcessor::removeOneTurnAttributes","!",e.getMessage());
      }
    
    trackedObjects.clear();
      
    marauroad.trace("the1001RPRuleProcessor::removeOneTurnAttributes","<");
    }

  public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    marauroad.trace("the1001RPRuleProcessor::onInit",">");
    try
      {
      object.put("?joined","");
      zone.add(object);
      trackedObjects.add(object);
  
      return true;
      }
    finally
      {
      marauroad.trace("the1001RPRuleProcessor::onInit","<");
      }
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
    
  