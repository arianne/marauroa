/* $Id: RPScheduler.java,v 1.21 2004/11/21 14:17:31 root777 Exp $ */
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
  private HashMap<RPObject.ID,RPActionList> actualTurn;
  /** a HashMap<RPObject.ID,RPActionList> of entries for next turn */
  private HashMap<RPObject.ID,RPActionList> nextTurn;
  /** Turn we are executing now */
  private int turn;
  
  /** Constructor */
  public RPScheduler()
    {
    turn=0;
    actualTurn=new HashMap<RPObject.ID,RPActionList>();
    nextTurn=new HashMap<RPObject.ID,RPActionList>();
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
        RPActionList list=nextTurn.get(id);

        list.add(action);
        }
      else
        {
        RPActionList list=new RPActionList();

        list.add(action);
        nextTurn.put(id,list);
        }
      }
    catch(AttributeNotFoundException e)
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
      for(Map.Entry<RPObject.ID,RPActionList> entry: actualTurn.entrySet())
        {
        RPObject.ID id=entry.getKey();
        RPActionList list=entry.getValue();

        ruleProcessor.approvedActions(id,list);
      
        for(RPAction action: list)
          {
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
    actualTurn=nextTurn;
    nextTurn.clear();
    marauroad.trace("RPScheduler::nextTurn","<");
    }
  }
