/* $Id: RPCode.java,v 1.25 2004/01/07 11:16:24 arianne_rpg Exp $ */
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
        
      zone.modify(player);      
      zone.modify(arena);      
      
      return RPAction.STATUS_SUCCESS;
      }
    finally
      {
      marauroad.trace("RPCode::RequestFight","<");
      }
    }
  
  /** This method removes players that have requested to fight but that have logout
   *  before the fight happens.
   *  @param player_id the id of the player.
   *  @return true if the player has been removed or false otherwise. */
  public static boolean RemoveWaitingPlayer(RPObject.ID player_id)
    {
    marauroad.trace("RPCode::RemoveWaitingPlayer",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(player_id);
      
      if(player.has("requested"))
        {
        arena.put("waiting",arena.getInt("waiting")-1);
        arena.remove("requested");
        return true;
        }
      
      return false;
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::RemoveWaitingPlayer","X",e.getMessage());
      return false;
      }
    finally
      {
      marauroad.trace("RPCode::RemoveWaitingPlayer","<");
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
  
  /** This method does the fight for this turn, assigning damage to the fighters and in 
   *  case of a winner happens, setting the arena to request fame. */  
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
        
        /** Check if Combat is completed */
        if(combatCompleted(gladiators))
          {
          int fame=0;
          
          for(i=0;i<gladiators.length;++i)
            {
            if(gladiators[i].getInt("hp")<=0)
              {
              fame+=gladiators[i].getInt("fame");
              }
            else
              {
              arena.put("winner",gladiators[i].get("object_id"));
              }
            }
            
          arena.put("status","request_fame");
          arena.put("fame",fame);
          arena.put("timeout",60);
          arena.put("thumbs_up",0);
          arena.put("thumbs_down",0);
          }
  
        zone.modify(arena);      
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
  
  /** This method returns true if the combat is completed */  
  private static boolean combatCompleted(RPObject[] gladiators) throws Exception
    {
    /** NOTE: This routine fails if both players die. Is that possible? */
    int gladiatorsStillFighting=gladiators.length;
        
    /** We check for dead players and determine if combat is finished. */
    for(int i=0;i<gladiators.length;++i)
      {
      if(gladiators[i].getInt("hp")<=0)          
        {
        --gladiatorsStillFighting;
        }
      }
    
    return (gladiatorsStillFighting==1)?true:false;
    }
  
  /** This method is the combat system itself, and it is an implementation of the 
   *  Rock-Paper-Scissor game with a bit of RP features. */  
  private static void computeDamageGladiators(RPObject gladiator1, RPObject gladiator2) throws Exception
    {
    if((gladiator1.getInt("hp")<=0) || (gladiator2.getInt("hp")<=0))
      {
      /** Failed because the gladiator is dead */
      return;
      }
    
    if(!gladiator1.has("!mode"))
      {
      /** Gladiator1 has not begin to fight. */
      return;
      }
         
    if(!gladiator2.has("!mode"))
      {
      /** Gladiator2 is idle, no combat, just hit */
      int damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("damage",damage);
      }
      
    String mode_g1=gladiator1.get("!mode");
    String mode_g2=gladiator2.get("!mode");
    
    if(mode_g1.equals(mode_g2))
      {
      /** Draw: We substract damage to each side */
      int damage;
      
      damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("?damage",damage);
      ruleProcessor.trackObject(gladiator2);

      damage=Math.abs(rand.nextInt()%gladiator2.getInt("attack"));
      gladiator1.put("hp",gladiator1.getInt("hp")-damage);
      gladiator1.put("?damage",damage);
      ruleProcessor.trackObject(gladiator1);
      }
    else if((mode_g1.equals("rock") && mode_g2.equals("scissor")) ||
      (mode_g1.equals("paper") && mode_g2.equals("rock"))   ||
      (mode_g1.equals("scissor") && mode_g2.equals("paper")))
      {
      int damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"));
      gladiator2.put("hp",gladiator2.getInt("hp")-damage);
      gladiator2.put("?damage",damage);
      ruleProcessor.trackObject(gladiator2);
      }  
    }
  
  private static List playersVoted;
  
  static
    {
    playersVoted=new LinkedList();
    }
  
  public static RPAction.Status Vote(RPObject.ID player_id, String vote) throws Exception
    {
    marauroad.trace("RPCode::Vote",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(player_id);
      
      if(player.has("!vote"))
        {
        /** Failed because player already voted */
        return RPAction.Fail("Failed because player already voted");
        }

      if(!arena.get("status").equals("request_fame"))
        {
        /** Failed because arena is not still requesting fame */
        return RPAction.Fail("Failed because arena is not still requesting fame");
        }
      
      if(vote.equals("up"))
        {
        arena.put("thumbs_up",arena.getInt("thumbs_up")+1);
        }
      else
        {
        arena.put("thumbs_down",arena.getInt("thumbs_down")+1);
        }
      
      player.put("!vote","");  
      playersVoted.add(player);
      
      return RPAction.STATUS_SUCCESS;
      }
    finally
      {
      marauroad.trace("RPCode::Vote","<");
      }
    }
  
  public static void RequestFame()
    {
    marauroad.trace("RPCode::RequestFame",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      
      if(arena.get("status").equals("request_fame"))
        {
        if(arena.getInt("timeout")==0)
          {
          int up=arena.getInt("thumbs_up");
          int down=arena.getInt("thumbs_up");
          int fame=arena.getInt("fame");
          int total=up+down;          
          
          int fame_result=fame*up/total;
          RPObject winner=arena.getSlot("gladiators").get(new RPObject.ID(arena.getInt("winner")));
          winner.put("fame",winner.getInt("fame")+fame_result);    
          
          arena.put("status","waiting");
          arena.getSlot("gladiators").clear();
          
          Iterator it=playersVoted.iterator();
          while(it.hasNext())
            {
            RPObject player=(RPObject)it.next();
            player.remove("!vote");
            }
          
          playersVoted.clear();

          arena.remove("thumbs_up");
          arena.remove("thumbs_down");
          arena.remove("fame");
          arena.remove("timeout");
          arena.remove("winner");
          }
        else
          {
          arena.put("timeout",arena.getInt("timeout")-1);
          }
        }
      }
    catch(Exception e)
      {
      e.printStackTrace();
      marauroad.trace("RPCode::RequestFame","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("RPCode::RequestFame","<");
      }
    }
  }
  
  