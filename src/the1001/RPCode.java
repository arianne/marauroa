/* $Id: RPCode.java,v 1.15 2004/01/01 22:51:06 arianne_rpg Exp $ */
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
  private static Random rand;
  
  static
    {
    rand=new Random();
    }
  
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
        return RPAction.Fail("Failed because player does not own that object");
        }

      if(player.get("status").equals("onArena"))
        {
        /** Failed because player is already fighting */
        return RPAction.Fail("Failed because player is already fighting");
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

  /** This action is used to request a change in the fight style by a player that owns a gladiator.
   *
   *  Pseudocode:
   *
   *  BEGIN  
   *    Check that gladiator exists
   *    gladiator=Get gladiator from Player in Slot "gladiators"
   *    
   *    Change style
   *
   *  END
   *
   *  The idea by now it the Rock, Paper, Scissor game. It is pretty simple, but 
   *  interactive and can be a good way of handling fights until we can add realtime support.
   *
   *  @param player_id the Object id of the player 
   *  @param gladiator_id the Object id of the gladiator the player choosed to fight.
   *  @param fight_mode the mode of fighting that the player will have.
   *  @return the result of executing the action, either success or fail. */
  public static RPAction.Status FightMode(RPObject.ID player_id, RPObject.ID gladiator_id, String fight_mode) throws Exception
    {
    marauroad.trace("RPCode::FightMode",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(player_id);
      
      if(!player.getSlot("gladiators").has(gladiator_id))
        {
        /** Failed because player does not own that object */
        return RPAction.Fail("Failed because player does not own that object");
        }

      if(!arena.getSlot("gladiators").has(gladiator_id))
        {
        /** Failed because gladiator is not fighting on the arena*/
        return RPAction.Fail("Failed because gladiator is not fighting on the arena");
        }
      
      /** NOTE: Here is the combat type choosal. */
      if(!(fight_mode.equals("rock") || fight_mode.equals("paper") || fight_mode.equals("scissor")))
        {
        /** Failed because gladiator is fighting using an unsupported mode. */
        return RPAction.Fail("Failed because gladiator is fighting using an unsupported mode");
        }
      
      RPObject gladiator=arena.getSlot("gladiators").get(gladiator_id);
      gladiator.put("!mode",fight_mode);
      
      return RPAction.STATUS_SUCCESS;
      }
    finally
      {
      marauroad.trace("RPCode::FightMode","<");
      }
    }
    
  public static void ResolveFight()
    {
    marauroad.trace("RPCode::ResolveFight",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      
      if(arena.get("status").equals("fighting"))
        {
        RPObject[] gladiators=new RPObject[GLADIATORS_PER_FIGHT];
        int i=0;
        
        Iterator it=arena.getSlot("gladiators").iterator();
        while(it.hasNext())
          {
          gladiators[i]=(RPObject)it.next();
          ++i;
          }
          
        for(i=0;i<gladiators.length;++i)
          {
          for(int j=0;j<gladiators.length;++j)
            {
            if(i!=j)
              {
              computeDamageGladiators(gladiators[i],gladiators[j]);
              }
            }
          }
        
        /** We check for dead players and determine if combat is finished. */
        for(i=0;i<gladiators.length;++i)
          {
          /* TODO */
          }
        }      
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::ResolveFight","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("RPCode::ResolveFight","<");
      }
    }      
  
  private static void computeDamageGladiators(RPObject gladiator1, RPObject gladiator2) throws Exception
    {
    if((gladiator1.getInt("hp")<=0) || (gladiator2.getInt("hp")<=0))
      {
      /** Failed because the gladiator is dead */
      return;
      }
      
    String mode_g1=gladiator1.get("!mode");
    String mode_g2=gladiator1.get("!mode");
    
    if(mode_g1.equals(mode_g2))
      {
      /** Draw: We substract damage to each side */
      int damage;
      
      damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("damage",damage);

      damage=Math.abs(rand.nextInt()%gladiator2.getInt("attack"));
      gladiator1.put("hp",gladiator1.getInt("hp")-damage);
      gladiator1.put("damage",damage);
      }
    else if(mode_g1.equals("rock") && mode_g2.equals("scissor"))
      {
      int damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("damage",damage);
      }  
    else if(mode_g1.equals("paper") && mode_g2.equals("rock"))
      {
      int damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("damage",damage);
      }  
    else if(mode_g1.equals("scissor") && mode_g2.equals("paper"))
      {
      int damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("damage",damage);
      }  
    else
      {
      /** Whatever other valid combination means gladiator 2 wins */
      int damage=Math.abs(rand.nextInt()%gladiator2.getInt("attack"));
      gladiator1.put("hp",gladiator1.getInt("hp")-damage);
      gladiator1.put("damage",damage);
      }
    
    }  
  }
  
  