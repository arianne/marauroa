/* $Id: MarauroaRPRuleProcessor.java,v 1.23 2004/08/29 11:07:42 arianne_rpg Exp $ */
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

import java.util.*;
import marauroa.marauroad;

public class MarauroaRPRuleProcessor implements IRPRuleProcessor
  {
  private IRPZone zone;
  private RPServerManager rpman;
  
  public MarauroaRPRuleProcessor()
    {
    }

  public void setContext(RPServerManager rpman, IRPZone zone)
    {
    this.zone=zone;
    this.rpman=rpman;
    }

  public void approvedActions(RPObject.ID id, RPActionList actionList)
    {
    }
  
  public RPAction.Status execute(RPObject.ID id, RPAction list)
    {
    return new RPAction.Status(RPAction.Status.FAIL);
    }

  public void nextTurn()
    {
    }

  public boolean onInit(RPObject object) throws RPObjectInvalidException
    {
    return false;
    }
    
  public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException
    {
    return false;
    }
    
  public boolean onTimeout(RPObject.ID id) throws RPObjectNotFoundException
    {
    return false;
    }
  }
