/* $Id: RPCode.java,v 1.3 2003/12/30 09:27:59 arianne_rpg Exp $ */
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

public class RPCode
  {
  private static byte GLADIATORS_PER_FIGHT=2;
  private static the1001RPRuleProcessor ruleProcessor;
  
  public static void setCallback(the1001RPRuleProcessor rpu)
    {
    ruleProcessor=rpu;
    }
  
  public static RPAction.Status RequestFight(RPObject.ID id, String gladiator_id) throws Exception
    {
    marauroad.trace("RPCode::RequestFight",">");
    
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(id);
/*      
     if(arena[status] is WAITING && arena[gladiators].size < GLADIATORS_PER_FIGHT)
       {
       player[status]="onArena"
       arena[gladiators].add gladiator
       }
     else
       {
       player[requestedFight]=turn
       arena[waitingGladiators]=arena[waitingGladiators]+1;
       }
*/  
      if(arena.get("status").equals("waiting") && arena.getSlot("gladiators").size()<GLADIATORS_PER_FIGHT)
        {
        player.put("status","onArena");
        arena.getSlot("gladiators").add(player);
        }
      else
        {
        player.put("requested",/** TODO: Put turn here */0);
        arena.put("waiting",Integer.parseInt(arena.get("waiting"))+1);
        }
      
      zone.modify(new RPObject.ID(player));
      zone.modify(new RPObject.ID(arena));
      
      return RPAction.STATUS_SUCCESS;
      }
    finally
      {
      marauroad.trace("RPCode::RequestFight","<");
      }
    }
  }