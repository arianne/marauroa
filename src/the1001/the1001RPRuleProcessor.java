/* $Id: the1001RPRuleProcessor.java,v 1.52 2004/05/07 17:16:59 arianne_rpg Exp $ */
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

public class the1001RPRuleProcessor implements RPRuleProcessor
  {
  private the1001RPZone zone;
  private List trackedObjects;
  private int turn;
  public the1001RPRuleProcessor()
    {
    zone=null;
    turn=0;
    trackedObjects=new LinkedList();
    RPCode.setCallback(this);
    }

  /** Set the context where the actions are executed.
   *  @param zone The zone where actions happens. */
  public void setContext(RPZone zone)
    {
    this.zone=(the1001RPZone)zone;
    }
    
  public the1001RPZone getRPZone()
    {
    return zone;
    }
    
  /** Pass the whole list of actions so that it can approve or deny the actions in it.
   *  @param id the id of the object owner of the actions.
   *  @param actionList the list of actions that the player wants to execute. */
  public void approvedActions(RPObject.ID id, RPActionList actionList)
    {
    }
    
  /** Execute an action in the name of a player.
   *  @param id the id of the object owner of the actions.
   *  @param action the action to execute
   *  @returns the action status, that can be Success, Fail or incomplete, please 
   *      refer to Actions Explained for more info. */
  public RPAction.Status execute(RPObject.ID id, RPAction action)
    {
    marauroad.trace("the1001RPRuleProcessor::execute",">");

    RPAction.Status status=RPAction.STATUS_FAIL;
    
    try
      {
      if(action.get(RPCode.var_type).equals("request_fight"))
        {
        int gladiator_id=action.getInt(RPCode.var_gladiator_id);

        status=RPCode.RequestFight(id, new RPObject.ID(gladiator_id));
        }
      else if(action.get(RPCode.var_type).equals("fight_mode"))
        {
        int gladiator_id=action.getInt(RPCode.var_gladiator_id);
        String fight_mode=action.get("fight_mode");

        status=RPCode.FightMode(id, new RPObject.ID(gladiator_id),fight_mode);
        }
      else if(action.get(RPCode.var_type).equals(RPCode.var_vote))
        {
        String vote=action.get(RPCode.var_vote);

        status=RPCode.Vote(id, vote);
        }
      else if(action.get(RPCode.var_type).equals(RPCode.var_chat))
        {
        String text=action.get(RPCode.var_content);

        status=RPCode.Chat(id,text);
        }
      else if(action.get(RPCode.var_type).equals(RPCode.var_buyItem))
        {
        String item_id=action.get(RPCode.var_choosen_item);        

        status=RPCode.BuyItem(id,new RPObject.ID(Integer.parseInt(item_id)));
        }
      else if(action.get(RPCode.var_type).equals(RPCode.var_buyGladiator))
        {
        String item_id=action.get(RPCode.var_choosen_item);        

        status=RPCode.BuyGladiator(id,new RPObject.ID(Integer.parseInt(item_id)));
        }
      else
        {
        marauroad.trace("the1001RPRuleProcessor::execute","W","Action("+action.get(RPCode.var_type)+") not supported");
        }
      
      /** We notify the player about the action result */
      RPObject player=zone.get(id);
      player.put("?"+action.get("action_id"), "" /* TODO: Replace status.toString() for a numerical code */ );
      trackObject(player);
      zone.modify(player);
      
      return status;
      }
    catch(Exception e)
      {
      marauroad.thrown("the1001RPRuleProcessor::execute","X",e);
      return RPAction.STATUS_FAIL;      
      }      
    finally
      {
      marauroad.trace("the1001RPRuleProcessor::execute","<");
      }
    }
  
  public int getTurn()
    {
    return turn;
    }
    
  /** Notify it when a new turn happens */
  synchronized public void nextTurn()
    {
    marauroad.trace("the1001RPRuleProcessor::nextTurn",">");        
    ++turn;
    try
      {
      removeOneTurnAttributes();      

      RPObject arena=zone.getArena();

      if(arena.get(RPCode.var_status).equals(RPCode.var_fighting))
        {
        RPCode.ResolveFight();
        }
      else if(arena.get(RPCode.var_status).equals(RPCode.var_request_fame))
        {
        RPCode.RequestFame();
        }
      else if(arena.get(RPCode.var_status).equals(RPCode.var_setting_next_combat))
        {
        RPCode.SetUpGladiatorsNextCombat();
        }
      }
    catch(Exception e)
      {
      marauroad.thrown("the1001RPRuleProcessor::nextTurn","X",e);
      e.printStackTrace(System.out);
      }
    marauroad.trace("the1001RPRuleProcessor::nextTurn","<");
    }
  
  private void removeOneTurnAttributes()
    {
    marauroad.trace("the1001RPRuleProcessor::removeOneTurnAttributes",">");        
    try
      {
      List attrToDelete=new LinkedList();
      Iterator it=trackedObjects.iterator();

      while(it.hasNext())
        {
        RPObject object=(RPObject)it.next();
        
        Iterator attributesit=object.iterator();
      
        while(attributesit.hasNext())
          {
          String attr=(String)attributesit.next();

          if(attr.charAt(0)=='?')
            {
            attrToDelete.add(attr);
            }
          }
        
        Iterator removeit=attrToDelete.iterator();

        while(removeit.hasNext())
          {
          object.remove((String)removeit.next());
          }
          
        attrToDelete.clear();
        
        if(zone.has(new RPObject.ID(object)))
          {
          zone.modify(object);
          }
        }
      }
    catch(Exception e)
      {
      marauroad.thrown("the1001RPRuleProcessor::removeOneTurnAttributes","X",e);
      e.printStackTrace(System.out);
      }
    finally
      {
      trackedObjects.clear();
      marauroad.trace("the1001RPRuleProcessor::removeOneTurnAttributes","<");
      }
    }
    
  public void trackObject(RPObject object)
    {
    trackedObjects.add(object);
    }

  synchronized public boolean onInit(RPObject object) throws RPZone.RPObjectInvalidException
    {
    marauroad.trace("the1001RPRuleProcessor::onInit",">");
    try
      {
      object.put("?joined","");
      
      trackObject(object);
      
      if(marauroad.loggable("the1001RPRuleProcessor::onInit","D"))
        {
        marauroad.trace("the1001RPRuleProcessor::onInit","D",object.toString());     
        }
        
      RPCode.AddPlayer(object);
      zone.add(object);
      return true;
      }
    catch(Exception e)
      {
      marauroad.thrown("the1001RPRuleProcessor::onInit","X",e);
      return false;
      }
    finally
      {
      marauroad.trace("the1001RPRuleProcessor::onInit","<");
      }
    }
    
  synchronized public boolean onExit(RPObject.ID id)
    {
    try
      {
      marauroad.trace("the1001RPRuleProcessor::onExit",">");
      /** TODO: Deny logout to players that are in combat */
      RPCode.RemovePlayer(id);

      RPObject removed=zone.remove(id);

      if(marauroad.loggable("the1001RPRuleProcessor::onExit","D"))
        {
        marauroad.trace("the1001RPRuleProcessor::onExit","D",removed.toString());     
        }
      }
    catch(Exception e)
      {
      marauroad.thrown("the1001RPRuleProcessor::onExit","X",e);
      }
    finally
      {
      marauroad.trace("the1001RPRuleProcessor::onExit","<");
      }
    return true;
    }
    
  synchronized public boolean onTimeout(RPObject.ID id)
    {
    return onExit(id);
    }

  public byte[] serializeMap(RPObject.ID id)
    {
    return new byte[0];
    }      
  }  

