/* $Id: RPScheduler.java,v 1.16 2004/06/03 13:04:44 arianne_rpg Exp $ */
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
import marauroa.*;

/** This class represents a scheduler to deliver action by turns, so every action
 *  added to the scheduler is executed on the next turn.
 *  Each object can cast as many actions as it wants. */
public class RPScheduler
  {
  /** a HashMap<RPObject.ID,RPActionList> of entries for this turn */
  private HashMap actualTurn;
  /** a HashMap<RPObject.ID,RPActionList> of entries for next turn */
  private HashMap nextTurn;
  /** Turn we are executing now */
  private int turn;
  static class ActionInvalidException extends Exception
    {
    ActionInvalidException(String attribute)
      {
      super("Action is invalid: It lacks of mandatory attribute ["+attribute+"]");
      }
    }
  /** Constructor */
  public RPScheduler()
    {
    turn=0;
    actualTurn=new HashMap();
    nextTurn=new HashMap();
    }
  
  /** Add an RPAction to the scheduler for the next turn
   *  @param action the RPAction
   *  @throws ActionInvalidException if the action lacks of sourceid attribute.*/
  public synchronized void addRPAction(RPAction action) throws ActionInvalidException
    {
    marauroad.trace("RPScheduler::addRPAction",">");
    try
      {
      RPObject.ID id=new RPObject.ID(action);

      marauroad.trace("RPScheduler::addRPAction","D","Add RPAction("+action+") from RPObject("+id+")");
      if(nextTurn.containsKey(id))
        {
        RPActionList list=(RPActionList)nextTurn.get(id);

        list.add(action);
        }
      else
        {
        RPActionList list=new RPActionList();

        list.add(action);
        nextTurn.put(id,list);
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      marauroad.thrown("RPScheduler::addRPAction","X",e);
      marauroad.trace("RPScheduler::addRPAction","X","Action("+action+") has not requiered attributes");
      throw new ActionInvalidException(e.getAttribute());
      }
    finally
      {
      marauroad.trace("RPScheduler::addRPAction","<");
      }
    }
  
  public synchronized void clearRPActions(RPObject.ID id)
    {
    if(nextTurn.containsKey(id))
      {
      nextTurn.remove(id);
      }    

    if(actualTurn.containsKey(id))
      {
      actualTurn.remove(id);
      }    
    }
  
  /** For each action in the actual turn, make it to be run in the ruleProcessor
   *  Depending on the result the action needs to be added for next turn. */
  public void visit(IRPRuleProcessor ruleProcessor)
    {
    marauroad.trace("RPScheduler::visit",">");
    try
      {
      Iterator it=actualTurn.entrySet().iterator();
    
      while(it.hasNext())
        {
        Map.Entry val=(Map.Entry)it.next();
        RPObject.ID id=(RPObject.ID)val.getKey();
        RPActionList list=(RPActionList)val.getValue();

        ruleProcessor.approvedActions(id,list);
      
        Iterator action_it=list.iterator();

        while(action_it.hasNext())
          {
          try
            {
            RPAction action=(RPAction)action_it.next();
            RPAction.Status status=ruleProcessor.execute(id,action);
                        
            /* If state is incomplete add for next turn */
            if(status.equals(RPAction.STATUS_INCOMPLETE))
              {
              addRPAction(action);
              }
            }
          catch(Exception e)
            {
            marauroad.thrown("RPScheduler::visit","X",e);
            }
          }
        }
      }
    catch(Exception e)
      {
      marauroad.thrown("RPScheduler::visit","X",e);
      }
    finally
      {
      marauroad.trace("RPScheduler::visit","<");
      }
    }
  
  /** This method moves to the next turn and deletes all the actions in the
   *  actual turn */
  public synchronized void nextTurn()
    {
    marauroad.trace("RPScheduler::nextTurn",">");
    ++turn;
    /* we cross-exchange the two turns and erase the contents of the next turn */
    actualTurn.clear();
    actualTurn=nextTurn;
    nextTurn=new HashMap();
    marauroad.trace("RPScheduler::nextTurn","<");
    }
  }
