package marauroa.game;

import java.util.*;
import marauroa.marauroad;

public class RPScheduler
{
  private HashMap actualTurn;
  private HashMap nextTurn;
  private int turn;
  
  static class ActionInvalidException extends Throwable
  {
    ActionInvalidException()
    {
      super("Action is invalid: It lacks of mandatory attributes");
    }
  }
  
  public RPScheduler()
  {
    turn=0;
    actualTurn=new HashMap();
    nextTurn=new HashMap();
  }
  
  public synchronized void addRPAction(RPAction action) throws ActionInvalidException
  {
    marauroad.trace("RPScheduler::addRPAction",">");
    try
    {
      RPObject.ID id=new RPObject.ID(action);
      marauroad.trace("RPScheduler::addRPAction","D","Add RPAction("+action+") to RPObject("+id+")");
      
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
      marauroad.trace("RPScheduler::addRPAction","E","Action("+action+") has not requiered attributes");
      throw new ActionInvalidException();
    }
    
    marauroad.trace("RPScheduler::addRPAction","<");
  }
  
  public void visit(RPRuleProcessor ruleProcessor)
  {
    marauroad.trace("RPScheduler::visit",">");
    Iterator it=actualTurn.entrySet().iterator();
    
    while(it.hasNext())
    {
      Map.Entry val=(Map.Entry)it.next();
      
      RPObject.ID id=(RPObject.ID)val.getKey();
      RPActionList list=(RPActionList)val.getValue();
      ruleProcessor.execute(id,list);
    }
    
    marauroad.trace("RPScheduler::visit","<");
  }
  
  public synchronized void nextTurn()
  {
    marauroad.trace("RPScheduler::nextTurn",">");
    
    ++turn;
    actualTurn=nextTurn;
    nextTurn=new HashMap();
    
    marauroad.trace("RPScheduler::nextTurn","<");
  }
}
