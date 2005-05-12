/* $Id: MarauroaRPRuleProcessor.java,v 1.4 2005/05/12 19:34:37 arianne_rpg Exp $ */
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
package marauroa.server.game;

import java.util.*;
import marauroa.common.game.*;

public class MarauroaRPRuleProcessor implements IRPRuleProcessor
  {
  private RPWorld world;
  private RPServerManager rpman;
  
  public MarauroaRPRuleProcessor()
    {
    }

  public void setContext(RPServerManager rpman, RPWorld world)
    {
    this.world=world;
    this.rpman=rpman;
    }

  public boolean checkGameVersion(String game, String version)
    {
    return false;
    }

  public boolean onActionAdd(RPAction action, List<RPAction> actionList)
    {
    return true;
    }

  public boolean onIncompleteActionAdd(RPAction action, List<RPAction> actionList)
    {
    return true;
    }
  
  public RPAction.Status execute(RPObject.ID id, RPAction list)
    {
    return RPAction.Status.FAIL;
    }

  public void beginTurn()
    {
    }
    
  public void endTurn()
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
