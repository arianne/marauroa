/* $Id: RPCode.java,v 1.57 2004/02/25 18:49:23 arianne_rpg Exp $ */
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
  final public static byte GLADIATORS_PER_FIGHT=2;  
  
  final public static String var_object_id="object_id";
  final public static String var_type="type";
  final public static String var_name="name";
  final public static String var_hp="hp";
  final public static String var_initial_hp="!hp";  
  final public static String var_attack="attack";
  final public static String var_fame="fame";
  final public static String var_num_victory="num_victory";
  final public static String var_num_defeat="num_defeat";
  final public static String var_l_hand="l_hand";
  final public static String var_r_hand="r_hand";
  final public static String var_defend="defend";
  final public static String var_price="price";
  final public static String var_gladiators="gladiators";
  final public static String var_items="items";
  final public static String var_fighting="fighting";
  final public static String var_requested="requested";
  final public static String var_status="status";
  final public static String var_waiting="waiting";
  final public static String var_choose="choose";
  final public static String var_hidden_vote="!vote";
  final public static String var_rock="rock";
  final public static String var_paper="paper";
  final public static String var_scissor="scissor";
  final public static String var_hidden_combat_mode="!mode";
  final public static String var_request_fame="request_fame";
  final public static String var_timeout="timeout";
  final public static String var_thumbs_up="thumbs_up";
  final public static String var_thumbs_down="thumbs_down";
  final public static String var_damage="?damage";
  final public static String var_voted_up="up";
  final public static String var_winner="winner";  
  final public static String var_gladiator_id="gladiator_id";  
  final public static String var_vote="vote";  
  final public static String var_combat_mode="mode";  
  final public static String var_look="look";  
  final public static String var_chat="chat";  
  final public static String var_content="content";  
  final public static String var_text="?text";  
  
  
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
   *    gladiator=Get gladiator from Player in Slot RPCode.var_gladiators
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
      
      if(!player.getSlot(RPCode.var_gladiators).has(gladiator_id))
        {
        /** Failed because player does not own that object */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") does not own that object("+gladiator_id.toString()+")");
        marauroad.trace("RPCode::RequestFight","D",status.toString());
        return status;
        }

      if(player.has(RPCode.var_fighting))
        {
        /** Failed because player is already fighting */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") is already fighting");
        marauroad.trace("RPCode::RequestFight","D",status.toString());
        return status;
        }

      if(player.has(RPCode.var_requested))
        {
        /** Failed because player is already fighting */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") has already requested to fight");
        marauroad.trace("RPCode::RequestFight","D",status.toString());
        return status;
        }
      
      RPObject gladiator=player.getSlot(RPCode.var_gladiators).get(gladiator_id);

      if(arena.get(RPCode.var_status).equals(RPCode.var_waiting) && arena.getSlot(RPCode.var_gladiators).size()<GLADIATORS_PER_FIGHT)
        {
        marauroad.trace("RPCode::RequestFight","D","Player("+player_id.toString()+") PROCEED for fighting with gladiator("+gladiator_id.toString()+")");        
        player.put(RPCode.var_fighting,"");
        player.put(RPCode.var_choose,gladiator_id.getObjectID());
        arena.getSlot(RPCode.var_gladiators).add(gladiator);
        playersFighting.add(player);

        /** We check now if Arena is complete */
        if(arena.getSlot(RPCode.var_gladiators).size()==GLADIATORS_PER_FIGHT)
          {
          marauroad.trace("RPCode::RequestFight","D","Arena has "+GLADIATORS_PER_FIGHT+" gladiators and FIGHT begins");        
          arena.put(RPCode.var_status,RPCode.var_fighting);
          }        
        }
      else
        {
        marauroad.trace("RPCode::RequestFight","D","Player("+player_id.toString()+") has to WAIT for fighting with gladiator("+gladiator_id.toString()+")");        
        player.put(RPCode.var_requested,ruleProcessor.getTurn());
        player.put(RPCode.var_choose,gladiator_id.getObjectID());
        arena.put(RPCode.var_waiting,arena.getInt(RPCode.var_waiting)+1);
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

  public static void AddPlayer(RPObject player)
    {
    marauroad.trace("RPCode::AddPlayer",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     

      if(player.has(RPCode.var_fighting))
        {
        player.remove(RPCode.var_fighting);
        }

      if(player.has(RPCode.var_choose))
        {
        player.remove(RPCode.var_choose);
        }

      if(player.has(RPCode.var_requested))
        {
        player.remove(RPCode.var_requested);
        }

      if(player.has(RPCode.var_hidden_combat_mode))
        {
        player.remove(RPCode.var_hidden_combat_mode);
        }

      if(player.has(RPCode.var_hidden_vote))
        {
        player.remove(RPCode.var_hidden_vote);
        }

      if(player.has(RPCode.var_damage))
        {
        player.remove(RPCode.var_damage);
        }

      Iterator it=player.getSlot("gladiators").iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(object.has(RPCode.var_damage))
          {
          object.remove(RPCode.var_damage);
          }

        if(object.has(RPCode.var_hidden_combat_mode))
          {
          object.remove(RPCode.var_hidden_combat_mode);
          }
        }
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::AddPlayer","X",e.getMessage());
      e.printStackTrace(System.out);
      }
    finally
      {
      marauroad.trace("RPCode::AddPlayer","<");
      }
    }
  
  /** This method removes players that have requested to fight but that have logout
   *  before the fight happens.
   *  @param player_id the id of the player.
   *  @return true if the player has been removed or false otherwise. */
  public static boolean RemovePlayer(RPObject.ID player_id)
    {
    marauroad.trace("RPCode::RemovePlayer",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject arena=zone.getArena();
      RPObject player=zone.get(player_id);
      
      if(player.has(RPCode.var_requested))
        {
        marauroad.trace("RPCode::RemovePlayer","D","Player("+player_id.toString()+") removed from Waiting Gladiators List");
        arena.put(RPCode.var_waiting,arena.getInt(RPCode.var_waiting)-1);
        player.remove(RPCode.var_requested);
        player.remove(RPCode.var_choose);
        
        playersWaiting.remove(player);
        }
       
      if(player.has(RPCode.var_fighting))
        {
        marauroad.trace("RPCode::RemovePlayer","D","Player("+player_id.toString()+") removed from Fighting Gladiators List");
        
        /** Player abandon the fight. */        
        playersFighting.remove(player);
       
        player.remove(RPCode.var_fighting);
        int gladiator_id=player.getInt(RPCode.var_choose);          
        arena.getSlot(RPCode.var_gladiators).remove(new RPObject.ID(gladiator_id));
        marauroad.trace("RPCode::RemovePlayer","D","Gladiator("+gladiator_id+") removed from Arena");
        player.remove(RPCode.var_choose);

        playersFighting.remove(player);
        }
        
      if(player.has(RPCode.var_hidden_vote))
        {
        marauroad.trace("RPCode::RemovePlayer","D","Player("+player_id.toString()+") removed from Vote Gladiators List");
        player.remove(RPCode.var_hidden_vote);
        }
       
       
      Iterator it=player.getSlot("gladiators").iterator();
      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        if(object.has(RPCode.var_damage))
          {
          object.remove(RPCode.var_damage);
          }

        if(object.has(RPCode.var_hidden_combat_mode))
          {
          object.remove(RPCode.var_hidden_combat_mode);
          }
        }
      
      return true;
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::RemovePlayer","X",e.getMessage());
      e.printStackTrace(System.out);
      return false;
      }
    finally
      {
      marauroad.trace("RPCode::RemovePlayer","<");
      }
    }

  /** This action is used to request a change in the fight style by a player that owns a gladiator.
   *
   *  Pseudocode:
   *
   *  BEGIN  
   *    Check that gladiator exists
   *    gladiator=Get gladiator from Player in Slot RPCode.var_gladiators
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
      
      if(!player.getSlot(RPCode.var_gladiators).has(gladiator_id))
        {
        /** Failed because player does not own that object */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") does not own that object("+gladiator_id.toString()+")");
        marauroad.trace("RPCode::FightMode","D",status.toString());
        return status;
        }

      if(!arena.getSlot(RPCode.var_gladiators).has(gladiator_id))
        {
        /** Failed because gladiator is not fighting on the arena*/
        RPAction.Status status=RPAction.Fail("Failed because gladiator("+gladiator_id.toString()+") is not fighting on the arena");
        marauroad.trace("RPCode::FightMode","D",status.toString());
        return status;
        }
      
      /** NOTE: Here is the combat type choosal. */
      if(!(fight_mode.equals(RPCode.var_rock) || fight_mode.equals(RPCode.var_paper) || fight_mode.equals(RPCode.var_scissor)))
        {
        /** Failed because gladiator is fighting using an unsupported mode. */
        RPAction.Status status=RPAction.Fail("Failed because gladiator("+gladiator_id.toString()+") is fighting using an unsupported mode("+fight_mode+")");
        marauroad.trace("RPCode::FightMode","D",status.toString());
        return status;
        }
      
      marauroad.trace("RPCode::FightMode","D","Player("+player_id.toString()+") choose MODE ("+fight_mode+") for gladiator("+gladiator_id.toString()+")");
      RPObject gladiator=arena.getSlot(RPCode.var_gladiators).get(gladiator_id);
      gladiator.put(RPCode.var_hidden_combat_mode,fight_mode);
      
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
      
      if(arena.get(RPCode.var_status).equals(RPCode.var_fighting))
        {
        RPObject[] gladiators=new RPObject[arena.getSlot(RPCode.var_gladiators).size()];
        int i=0;

        
        Iterator it=arena.getSlot(RPCode.var_gladiators).iterator();
        while(it.hasNext())
          {
          gladiators[i]=(RPObject)it.next();
          ++i;
          }

        marauroad.trace("RPCode::ResolveFight","D",String.valueOf(gladiators.length)+" gladiators on Arena");
          
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
            if(gladiators[i].getInt(RPCode.var_hp)<=0)
              {
              fame+=gladiators[i].getInt(RPCode.var_fame);
              gladiators[i].put(RPCode.var_num_defeat,gladiators[i].getInt(RPCode.var_num_defeat)+1);
              }
            else
              {
              arena.put(RPCode.var_winner,gladiators[i].get(RPCode.var_object_id));
              gladiators[i].put(RPCode.var_num_victory,gladiators[i].getInt(RPCode.var_num_victory)+1);
              }
            }
            
          arena.put(RPCode.var_status,RPCode.var_request_fame);
          arena.put(RPCode.var_fame,(int)Math.log((fame>0?fame:1)));
          arena.put(RPCode.var_timeout,30);
          arena.put(RPCode.var_thumbs_up,0);
          arena.put(RPCode.var_thumbs_down,0);
          }
  
        zone.modify(arena);      
        }
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::ResolveFight","X",e.getMessage());
      e.printStackTrace(System.out);
      }
    finally
      {
      marauroad.trace("RPCode::ResolveFight","<");
      }
    }      
  
  /** This method returns true if the combat is completed */  
  private static boolean combatCompleted(RPObject[] gladiators) throws Exception
    {
    int gladiatorsStillFighting=gladiators.length;
        
    /** We check for dead players and determine if combat is finished. */
    for(int i=0;i<gladiators.length;++i)
      {
      if(gladiators[i].getInt(RPCode.var_hp)<=0)          
        {
        --gladiatorsStillFighting;
        }
      }
    
    return (gladiatorsStillFighting<=1)?true:false;
    }
  
  /** This method is the combat system itself, and it is an implementation of the 
   *  Rock-Paper-Scissor game with a bit of RP features. */  
  private static void computeDamageGladiators(RPObject gladiator1, RPObject gladiator2) throws Exception
    {
    marauroad.trace("RPCode::computeDamageGladiators",">");
    
    try
      {    
      if((gladiator1.getInt(RPCode.var_hp)<=0) || (gladiator2.getInt(RPCode.var_hp)<=0))
        {
        /** Failed because the gladiator is dead */
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE 0 because gladiator is dead (hp<=0)");
        return;
        }
    
      if(gladiator1.has(RPCode.var_hidden_combat_mode)==false)
        {
        /** Gladiator1 has not begin to fight. */
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE 0 because gladiator("+gladiator1.get(var_object_id)+") has not choose fight mode");
        return;
        }
      else if(gladiator2.has(RPCode.var_hidden_combat_mode)==false)
        {
        /** Gladiator2 is idle, no combat, just hit */
        int damage=gladiator1.getInt(RPCode.var_attack);
        marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE "+damage+" because gladiator("+gladiator2.get(var_object_id)+") has not choose fight mode");
        gladiator2.put(RPCode.var_hp,gladiator2.getInt(RPCode.var_hp)-damage);
        gladiator2.put(RPCode.var_damage,damage);        
        ruleProcessor.trackObject(gladiator2);
        return;
        }
      else
        {
        String mode_g1=gladiator1.get(RPCode.var_hidden_combat_mode);
        String mode_g2=gladiator2.get(RPCode.var_hidden_combat_mode);
    
        if((mode_g1.equals(RPCode.var_rock) && mode_g2.equals(RPCode.var_scissor)) ||
          (mode_g1.equals(RPCode.var_paper) && mode_g2.equals(RPCode.var_rock))    ||
          (mode_g1.equals(RPCode.var_scissor) && mode_g2.equals(RPCode.var_paper)) ||
          (mode_g1.equals(mode_g2)))
          {
          int damage=Math.abs(rand.nextInt()%gladiator1.getInt(RPCode.var_attack))+1;
          marauroad.trace("RPCode::computeDamageGladiators","D","DAMAGE "+damage+" because gladiators has WIN to ("+mode_g1+") vs ("+mode_g2+")");
          gladiator2.put(RPCode.var_hp,gladiator2.getInt(RPCode.var_hp)-damage);
          gladiator2.put(RPCode.var_damage,damage);
          ruleProcessor.trackObject(gladiator2);
          }  
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
      
      if(player.has(RPCode.var_hidden_vote))
        {
        /** Failed because player already voted */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") already voted");
        marauroad.trace("RPCode::Vote","D",status.toString());
        return status;
        }

      if(!arena.get(RPCode.var_status).equals(RPCode.var_request_fame))
        {
        /** Failed because arena is not still requesting fame */
        RPAction.Status status=RPAction.Fail("Failed because arena ("+arena.get(RPCode.var_status)+") is not still requesting fame");
        marauroad.trace("RPCode::Vote","D",status.toString());
        return status;
        }
        
      if(playersVoted.contains(player))
        {
        /** Failed because player is exploiting a bug: Logout and Login to vote again */
        RPAction.Status status=RPAction.Fail("Failed because player("+player_id.toString()+") is exploiting a bug: Logout and Login to vote again");
        marauroad.trace("RPCode::Vote","D",status.toString());
        return status;
        }
      
      if(vote.equals(RPCode.var_voted_up))
        {
        marauroad.trace("RPCode::Vote","D","Player("+player_id.toString()+") voted UP");
        arena.put(RPCode.var_thumbs_up,arena.getInt(RPCode.var_thumbs_up)+1);
        }
      else
        {
        marauroad.trace("RPCode::Vote","D","Player("+player_id.toString()+") voted DOWN");
        arena.put(RPCode.var_thumbs_down,arena.getInt(RPCode.var_thumbs_down)+1);
        }
      
      player.put(RPCode.var_hidden_vote,"");  
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
      
      if(arena.get(RPCode.var_status).equals(RPCode.var_request_fame))
        {
        if(arena.getInt(RPCode.var_timeout)==0)
          {
          marauroad.trace("RPCode::RequestFame","D","Arena REQUEST FAME completed");
          int up=arena.getInt(RPCode.var_thumbs_up);
          int down=arena.getInt(RPCode.var_thumbs_down);
          int fame=arena.getInt(RPCode.var_fame);
          int total=up+down;          
          /* If none vote we fake the result to give everything to the winner. */
          if(total==0)
            {
            up=1;
            total=1;
            }
          
          int fame_result=fame*up/total;

          RPObject.ID winner_id;
          if(arena.has(RPCode.var_winner))
            {
            winner_id=new RPObject.ID(arena.getInt(RPCode.var_winner));
            }
          else
            {
            winner_id=RPObject.INVALID_ID;
            }
            
          if(arena.getSlot(RPCode.var_gladiators).has(winner_id))
            {
            RPObject winner=arena.getSlot(RPCode.var_gladiators).get(winner_id);
            marauroad.trace("RPCode::RequestFame","D","Fame("+fame_result+") assigned to "+winner.get("name"));            
            winner.put(RPCode.var_fame,winner.getInt(RPCode.var_fame)+fame_result);    
            }
          else
            {
            /* Winner was not present... */
            marauroad.trace("RPCode::RequestFame","D","Fame("+fame_result+") not assigned to because winner wasn't logged in");
            }
        
          SetUpNextCombat();
          }
        else
          {
          int timeout=arena.getInt(RPCode.var_timeout)-1;
          marauroad.trace("RPCode::RequestFame","D","Arena REQUEST FAME timer ("+timeout+")");
          arena.put(RPCode.var_timeout,timeout);
          }
        
        zone.modify(arena);
        }
      }
    catch(Exception e)
      {
      marauroad.trace("RPCode::RequestFame","X",e.getMessage());
      e.printStackTrace(System.out);
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
        if(player.has(RPCode.var_hidden_vote))
          {
          player.remove(RPCode.var_hidden_vote);          
          /* NOTE: It will use some objects that are not really in the world now.
           * but, who cares? :-) */
          }
        }
          
      playersVoted.clear();

      arena.remove(RPCode.var_thumbs_up);
      arena.remove(RPCode.var_thumbs_down);
      arena.remove(RPCode.var_fame);
      arena.remove(RPCode.var_timeout);
      if(arena.has(RPCode.var_winner))
        {
        arena.remove(RPCode.var_winner);
        }
      
      marauroad.trace("RPCode::SetUpNextCombat","D","Restore fighters HP and set them to idle");
      /* Restore fighthing players */
      it=playersFighting.iterator();
      while(it.hasNext())
        {
        RPObject player=(RPObject)it.next();
        RPObject gladiator=player.getSlot(RPCode.var_gladiators).get(new RPObject.ID(player.getInt(RPCode.var_choose)));
        gladiator.put(RPCode.var_hp,gladiator.get(RPCode.var_initial_hp));

        player.remove(RPCode.var_fighting);
        player.remove(RPCode.var_choose);
        
        zone.modify(player);
        }
      
      marauroad.trace("RPCode::SetUpNextCombat","D","Setup Arena to waiting status");
      playersFighting.clear();
      arena.getSlot(RPCode.var_gladiators).clear();
      arena.put(RPCode.var_status,RPCode.var_waiting);          
      
      /* Choose new fighters if available */ 
      if(arena.getInt(RPCode.var_waiting)>0)
        {
        marauroad.trace("RPCode::SetUpNextCombat","D","Add waiting fighters to Arena");
        it=playersWaiting.iterator();
       
        while(it.hasNext())
          {
          /** Closely related to RequestFight code. We should avoid duplication */
          RPObject player=(RPObject)it.next();
          RPObject gladiator=player.getSlot(RPCode.var_gladiators).get(new RPObject.ID(player.getInt(RPCode.var_choose)));

          marauroad.trace("RPCode::SetUpNextCombat","D","Added player("+new RPObject.ID(player).toString()+") with gladiator("+new RPObject.ID(gladiator).toString()+")");
          
          player.remove(RPCode.var_requested);
          arena.put(RPCode.var_waiting,arena.getInt(RPCode.var_waiting)-1);
          
          player.put(RPCode.var_fighting,"");
          arena.getSlot(RPCode.var_gladiators).add(gladiator);
          
          playersFighting.add(player);
          it.remove();
          
          if(arena.getSlot(RPCode.var_gladiators).size()==GLADIATORS_PER_FIGHT)
            {
            marauroad.trace("RPCode::SetUpNextCombat","D","Arena has "+GLADIATORS_PER_FIGHT+" gladiators and FIGHT begins");        
            arena.put(RPCode.var_status,RPCode.var_fighting);
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

   public static RPAction.Status Chat(RPObject.ID player_id, String text) throws Exception
    {
    marauroad.trace("RPCode::Chat",">");
   
    try
      {
      the1001RPZone zone=ruleProcessor.getRPZone();     
      RPObject player=zone.get(player_id);
      
      player.put(RPCode.var_text,text);  
            
      ruleProcessor.trackObject(player);
      zone.modify(player);
      
      return RPAction.STATUS_SUCCESS;
      }
    finally
      {
      marauroad.trace("RPCode::Chat","<");
      }
    }
  }
  
  