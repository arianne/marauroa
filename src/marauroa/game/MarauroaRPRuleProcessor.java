/* $Id: MarauroaRPRuleProcessor.java,v 1.8 2003/12/08 12:43:52 arianne_rpg Exp $ */
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
  }