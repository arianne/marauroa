/* $Id: MarauroaRPRuleProcessor.java,v 1.10 2003/12/29 11:19:14 arianne_rpg Exp $ */
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
package marauroa.game;

import java.util.List;
import marauroa.marauroad;

public class MarauroaRPRuleProcessor implements RPRuleProcessor
  {
  private RPZone zone;
  
  public MarauroaRPRuleProcessor()
    {
    }

  public void setContext(RPZone zone)
    {
    this.zone=zone;
    }

  public void approvedActions(RPObject.ID id, RPActionList actionList)
    {
    }
  
  public RPAction.Status execute(RPObject.ID id, RPAction list)
    {
    marauroad.trace("RPRuleProcessor::execute",">");
    /* TODO: Implement action procession code */
    marauroad.trace("RPRuleProcessor::execute","<");
    
    return new RPAction.Status(RPAction.Status.FAIL);
    }

  public void nextTurn()
    {
    }

  public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    zone.add(object);
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