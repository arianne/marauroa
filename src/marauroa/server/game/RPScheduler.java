/* $Id: RPScheduler.java,v 1.1 2005/01/23 21:00:46 arianne_rpg Exp $ */
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
import marauroa.common.*;
import marauroa.common.game.*;

/** This class represents a scheduler to deliver action by turns, so every action
 *  added to the scheduler is executed on the next turn.
 *  Each object can cast as many actions as it wants. */
public class RPScheduler
  {
  /** a HashMap<RPObject.ID,RPActionList> of entries for this turn */
  private HashMap<RPObject.ID,List<RPAction>> actualTurn;
  /** a HashMap<RPObject.ID,RPActionList> of entries for next turn */
  private HashMap<RPObject.ID,List<RPAction>> nextTurn;
  /** Turn we are executing now */
  private int turn;
  
  /** Constructor */
  public RPScheduler()
    {
    turn=0;
    actualTurn=new HashMap<RPObject.ID,List<RPAction>>();
    nextTurn=new HashMap<RPObject.ID,List<RPAction>>();
    }
  
  /** Add an RPAction to the scheduler for the next turn
   *  @param action the RPAction
   *  @throws ActionInvalidException if the action lacks of sourceid attribute.*/
  public synchronized void addRPAction(RPAction action) throws ActionInvalidException
    {
    Logger.trace("RPScheduler::addRPAction",">");
    try
      {
      RPObject.ID id=new RPObject.ID(action);

      Logger.trace("RPScheduler::addRPAction","D","Add RPAction("+action+") from RPObject("+id+")");
      if(nextTurn.containsKey(id))
        {
        List<RPAction> list=nextTurn.get(id);

        list.add(action);
        }
      else
        {
        List<RPAction> list=new LinkedList<RPAction>();
        list.add(action);
        
        nextTurn.put(id,list);
        }
      }
    catch(AttributeNotFoundException e)
      {
      Logger.thrown("RPScheduler::addRPAction","X",e);
      Logger.trace("RPScheduler::addRPAction","X","Action("+action+") has not requiered attributes");
      throw new ActionInvalidException(e.getAttribute());
      }
    finally
      {
      Logger.trace("RPScheduler::addRPAction","<");
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
    Logger.trace("RPScheduler::visit",">");
    try
      {
      for(Map.Entry<RPObject.ID,List<RPAction>> entry: actualTurn.entrySet())
        {
        RPObject.ID id=entry.getKey();
        List<RPAction> list=entry.getValue();

        ruleProcessor.approvedActions(id,list);
      
        for(RPAction action: list)
          {
          Logger.trace("RPScheduler::visit","D",action.toString());
          try
            {
            RPAction.Status status=ruleProcessor.execute(id,action);
                        
            /* If state is incomplete add for next turn */
            if(status.equals(RPAction.Status.INCOMPLETE))
              {
              addRPAction(action);
              }
            }
          catch(Exception e)
            {
            Logger.thrown("RPScheduler::visit","X",e);
            }
          }
        }
      }
    catch(Exception e)
      {
      Logger.thrown("RPScheduler::visit","X",e);
      }
    finally
      {
      Logger.trace("RPScheduler::visit","<");
      }
    }
  
  /** This method moves to the next turn and deletes all the actions in the
   *  actual turn */
  public synchronized void nextTurn()
    {
    Logger.trace("RPScheduler::nextTurn",">");
    ++turn;
    
    /* we cross-exchange the two turns and erase the contents of the next turn */
    HashMap<RPObject.ID,List<RPAction>> tmp=actualTurn;
    actualTurn=nextTurn;
    nextTurn=tmp;
    nextTurn.clear();
    
    Logger.trace("RPScheduler::nextTurn","<");
    }
  }
