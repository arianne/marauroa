/* $Id: RPCode.java,v 1.35 2004/01/08 13:31:34 arianne_rpg Exp $ */
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
  public static byte GLADIATORS_PER_FIGHT=2;
  
  private static the1001RPRuleProcessor ruleProcessor;
  private static Random rand;
  private static List playersFighting;
  private static List playersWaiting;
  private static List playersVoted;
  
  static
    {
    rand=new Random();
    playersFighting=new LinkedList();
    playersWaiting=new LinkedList();
    playersVoted=new LinkedList();
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
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") does not own that object("+gladiator_id.toString()+")");
        marauroad.trace("RPCode::RequestFight","D",status.toString());
        return status;
        }

      if(player.has("fighting"))
        {
        /** Failed because player is already fighting */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") is already fighting");
        marauroad.trace("RPCode::RequestFight","D",status.toString());
        return status;
        }

      if(player.has("requested"))
        {
        /** Failed because player is already fighting */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") has already requested to fight");
        marauroad.trace("RPCode::RequestFight","D",status.toString());
        return status;
        }
      
      RPObject gladiator=player.getSlot("gladiators").get(gladiator_id);

      if(arena.get("status").equals("waiting") && arena.getSlot("gladiators").size()<GLADIATORS_PER_FIGHT)
        {
        marauroad.trace("RPCode::RequestFight","D","Player("+player_id.toString()+") PROCEED for fighting with gladiator("+gladiator_id.toString()+")");        
        player.put("fighting","");
        player.put("choose",gladiator_id.getObjectID());
        arena.getSlot("gladiators").add(gladiator);
        playersFighting.add(player);

        /** We check now if Arena is complete */
        if(arena.getSlot("gladiators").size()==GLADIATORS_PER_FIGHT)
          {
          marauroad.trace("RPCode::RequestFight","D","Arena has "+GLADIATORS_PER_FIGHT+" gladiators and FIGHT begins");        
          arena.put("status","fighting");
          }        
        }
      else
        {
        marauroad.trace("RPCode::RequestFight","D","Player("+player_id.toString()+") has to WAIT for fighting with gladiator("+gladiator_id.toString()+")");        
        player.put("requested",ruleProcessor.getTurn());
        player.put("choose",gladiator_id.getObjectID());
        arena.put("waiting",arena.getInt("waiting")+1);
        playersWaiting.add(player);
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
  public static boolean RemovePlayer(RPObject.ID player_id)
    {
    marauroad.trace("RPCode::RemoveWaitingPlayer",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(player_id);
      
      if(player.has("requested"))
        {
        marauroad.trace("RPCode::RemoveWaitingPlayer","D","Player("+player_id.toString()+") removed from Waiting Gladiators List");
        arena.put("waiting",arena.getInt("waiting")-1);
        player.remove("requested");
        player.remove("choose");
        
        playersWaiting.remove(player);
        }
       
      if(player.has("fighting"))
        {
        marauroad.trace("RPCode::RemoveWaitingPlayer","D","Player("+player_id.toString()+") removed from Fighting Gladiators List");
        /** TODO: Player abandon the fight. */        
        playersFighting.remove(player);
        }
        
      if(player.has("!vote"))
        {
        marauroad.trace("RPCode::RemoveWaitingPlayer","D","Player("+player_id.toString()+") removed from Vote Gladiators List");
        player.remove("!vote");
        playersVoted.remove(player);
        }
      
      return true;
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
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") does not own that object("+gladiator_id.toString()+")");
        marauroad.trace("RPCode::FightMode","D",status.toString());
        return status;
        }

      if(!arena.getSlot("gladiators").has(gladiator_id))
        {
        /** Failed because gladiator is not fighting on the arena*/
        RPAction.Status status=RPAction.Fail("Failed because gladiator("+gladiator_id.toString()+") is not fighting on the arena");
        marauroad.trace("RPCode::FightMode","D",status.toString());
        return status;
        }
      
      /** NOTE: Here is the combat type choosal. */
      if(!(fight_mode.equals("rock") || fight_mode.equals("paper") || fight_mode.equals("scissor")))
        {
        /** Failed because gladiator is fighting using an unsupported mode. */
        RPAction.Status status=RPAction.Fail("Failed because gladiator("+gladiator_id.toString()+") is fighting using an unsupported mode("+fight_mode+")");
        marauroad.trace("RPCode::FightMode","D",status.toString());
        return status;
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
          
        marauroad.trace("RPCode::ResolveFight","D","Compute damage that each gladiator does");
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
          marauroad.trace("RPCode::ResolveFight","D","Combat is FINISHED");
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
    marauroad.trace("RPCode::computeDamageGladiators",">");
    
    try
      {    
      if((gladiator1.getInt("hp")<=0) || (gladiator2.getInt("hp")<=0))
        {
        /** Failed because the gladiator is dead */
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE 0 because gladiator is dead (hp<=0)");
        return;
        }
    
      if(!gladiator1.has("!mode"))
        {
        /** Gladiator1 has not begin to fight. */
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE 0 because gladiator("+gladiator1.get("object_id")+") has not choose fight mode");
        return;
        }
         
      if(!gladiator2.has("!mode"))
        {
        /** Gladiator2 is idle, no combat, just hit */
        int damage=gladiator1.getInt("attack");
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE "+damage+" because gladiator("+gladiator2.get("object_id")+") has not choose fight mode");
        gladiator2.put("hp",gladiator2.getInt("hp")-damage);
        gladiator2.put("damage",damage);
        }
      
      String mode_g1=gladiator1.get("!mode");
      String mode_g2=gladiator2.get("!mode");
    
      if((mode_g1.equals("rock") && mode_g2.equals("scissor")) ||
        (mode_g1.equals("paper") && mode_g2.equals("rock"))    ||
        (mode_g1.equals("scissor") && mode_g2.equals("paper")) ||
        (mode_g1.equals(mode_g2)))
        {
        int damage=Math.abs(rand.nextInt()%gladiator1.getInt("attack"))+1;
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE "+damage+" because gladiators has WIN to ("+mode_g1+") vs ("+mode_g2+")");
        gladiator2.put("hp",gladiator2.getInt("hp")-damage);
        gladiator2.put("?damage",damage);
        ruleProcessor.trackObject(gladiator2);
        }  
      }
    finally
      {
      marauroad.trace("RPCode::computeDamageGladiators","<");
      }
    }
    
  /** This action is used to vote for a gladiator once the fight is over.
   *
   *  Pseudocode:
   *
   *  BEGIN  
   *    Check that player has not voted
   *    Vote UP or DOWN
   *
   *  END
   *
   *  @param player_id the Object id of the player 
   *  @param vote the type of vote: UP or DOWN.
   *  @return the result of executing the action, either success or fail. 
   **/
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
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") already voted");
        marauroad.trace("RPCode::Vote","D",status.toString());
        return status;
        }

      if(!arena.get("status").equals("request_fame"))
        {
        /** Failed because arena is not still requesting fame */
        RPAction.Status status=RPAction.Fail("Failed because arena ("+arena.get("status")+") is not still requesting fame");
        marauroad.trace("RPCode::Vote","D",status.toString());
        return status;
        }
      
      if(vote.equals("up"))
        {
        marauroad.trace("RPCode::Vote","D","Player("+player_id.toString()+") voted UP");
        arena.put("thumbs_up",arena.getInt("thumbs_up")+1);
        }
      else
        {
        marauroad.trace("RPCode::Vote","D","Player("+player_id.toString()+") voted DOWN");
        arena.put("thumbs_down",arena.getInt("thumbs_down")+1);
        }
      
      player.put("!vote","");  
      playersVoted.add(player);
      
      zone.modify(arena);
      
      return RPAction.STATUS_SUCCESS;
      }
    finally
      {
      marauroad.trace("RPCode::Vote","<");
      }
    }
  
  /** This method is the one that collect the fame scores from viewers and assign
   *  it to the winner. */
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
          marauroad.trace("RPCode::RequestFame","D","Arena REQUEST FAME completed");
          int up=arena.getInt("thumbs_up");
          int down=arena.getInt("thumbs_up");
          int fame=arena.getInt("fame");
          int total=up+down;          
          
          int fame_result=fame*up/total;
          RPObject winner=arena.getSlot("gladiators").get(new RPObject.ID(arena.getInt("winner")));
          winner.put("fame",winner.getInt("fame")+fame_result);    
        
          SetUpNextCombat();
          }
        else
          {
          int timeout=arena.getInt("timeout")-1;
          marauroad.trace("RPCode::RequestFame","D","Arena REQUEST FAME timer ("+timeout+")");
          arena.put("timeout",timeout);
          }
        
        zone.modify(arena);
        }
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::RequestFame","X",e.getMessage());
      }
    finally
      {
      marauroad.trace("RPCode::RequestFame","<");
      }
    }
  
  /** This method take care of initializing the arena for the next combat */
  private static void SetUpNextCombat() throws Exception
    {
    marauroad.trace("RPCode::SetUpNextCombat",">");
    
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();

      Iterator it;

      marauroad.trace("RPCode::SetUpNextCombat","D","Clean players votes");
      it=playersVoted.iterator();
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
      
      marauroad.trace("RPCode::SetUpNextCombat","D","Restore fighters HP and set them to idle");
      /* Restore fighthing players */
      it=playersFighting.iterator();
      while(it.hasNext())
        {
        RPObject player=(RPObject)it.next();
        RPObject gladiator=player.getSlot("gladiators").get(new RPObject.ID(player.getInt("choose")));
        gladiator.put("hp",gladiator.get("!hp"));

        player.remove("fighting");
        player.remove("choose");
        
        zone.modify(player);
        }
      
      marauroad.trace("RPCode::SetUpNextCombat","D","Setup Arena to waiting status");
      playersFighting.clear();
      arena.getSlot("gladiators").clear();
      arena.put("status","waiting");          
      
      /* Choose new fighters if available */ 
      if(arena.getInt("waiting")>0)
        {
        marauroad.trace("RPCode::SetUpNextCombat","D","Add waiting fighters to Arena");
        it=playersWaiting.iterator();
       
        while(it.hasNext())
          {
          /** Closely related to RequestFight code. We should avoid duplication */
          RPObject player=(RPObject)it.next();
          RPObject gladiator=player.getSlot("gladiators").get(new RPObject.ID(player.getInt("choose")));

          marauroad.trace("RPCode::SetUpNextCombat","D","Added player("+new RPObject.ID(player).toString()+") with gladiator("+new RPObject.ID(gladiator).toString()+")");
          
          player.remove("requested");
          arena.put("waiting",arena.getInt("waiting")-1);
          
          player.put("fighting","");
          arena.getSlot("gladiators").add(gladiator);
          
          playersFighting.add(player);
          it.remove();
          
          if(arena.getSlot("gladiators").size()==GLADIATORS_PER_FIGHT)
            {
            marauroad.trace("RPCode::SetUpNextCombat","D","Arena has "+GLADIATORS_PER_FIGHT+" gladiators and FIGHT begins");        
            arena.put("status","fighting");
            break;
            }
          }        
        } 
      }
    finally
      {
      marauroad.trace("RPCode::SetUpNextCombat","<");
      }
    }
  }
  
  