/* $Id: RPCode.java,v 1.7 2003/12/31 12:16:17 arianne_rpg Exp $ */
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

public class RPCode
  {
  private static byte GLADIATORS_PER_FIGHT=2;
  private static the1001RPRuleProcessor ruleProcessor;
  
  public static void setCallback(the1001RPRuleProcessor rpu)
    {
    ruleProcessor=rpu;
    }
  
  /** This action is used to request a fight by a player that owns a gladiator.
   *
   *  Pseudocode:
   *
   *  BEGIN  
   *    Check that gladiator exists
   *    gladiator=Get gladiator from Player in Slot "gladiators"
   *
   *    if combat has not began and there is still room
   *      player is on Arena
   *      add gladiator to Arena
   *    else
   *      player requested fight on turn X
   *      add player to waiting queue
   *    endif
   *
   *    Check if the arena is completed and so the fight can begin
   *  END
   *
   *  @param player_id the Object id of the player 
   *  @param gladiator_id the Object id of the gladiator the player choosed to fight.
   *  @return the result of executing the action, either success or fail. */
  public static RPAction.Status RequestFight(RPObject.ID player_id, RPObject.ID gladiator_id) throws Exception
    {
    marauroad.trace("RPCode::RequestFight",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(player_id);
      
      if(!player.getSlot("gladiators").has(gladiator_id))
        {
        /** Failed because player does not own that object */
        return RPAction.STATUS_FAIL;
        }

      if(player.get("status").equals("onArena"))
        {
        /** Failed because player is already fighting */
        return RPAction.STATUS_FAIL;
        }
      
      RPObject gladiator=player.getSlot("gladiators").get(gladiator_id);

      if(arena.get("status").equals("waiting") && arena.getSlot("gladiators").size()<GLADIATORS_PER_FIGHT)
        {
        player.put("status","onArena");
        arena.getSlot("gladiators").add(gladiator);
        }
      else
        {
        player.put("requested",ruleProcessor.getTurn());
        arena.put("waiting",arena.getInt("waiting")+1);
        }
      
      /** We check now if Arena is complete */
      if(arena.getSlot("gladiators").size()==GLADIATORS_PER_FIGHT)
        {
        arena.put("status","fighting");
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