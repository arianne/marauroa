package marauroa.game;

import java.util.*;

public class RPScheduler
  {
  private HashMap actualTurn;
  private HashMap nextTurn;
  private int turn;
  
  class ActionInvalidException extends Throwable
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
    try
      {
      RPObject.ID id=new RPObject.ID(action);
      if(nextTurn.containsKey(id))
        {
        LinkedList list=(LinkedList)nextTurn.get(id);
        list.add(action);
        }
      else
        {
        LinkedList list=new LinkedList();
        list.add(action);
        nextTurn.put(id,list);
        }
      }
    catch(Attributes.AttributeNotFoundException e)
      {
      throw new ActionInvalidException();
      }
    }
    
  public void visit(RPRuleProcessor ruleProcessor)
    {
    Iterator it=actualTurn.entrySet().iterator();
    
    while(it.hasNext())
      {
      Map.Entry val=(Map.Entry)it.next();
      
      RPObject.ID id=(RPObject.ID)val.getKey();
      LinkedList list=(LinkedList)val.getValue();
      
      ruleProcessor.execute(id,list);       
      }
    }
    
  public synchronized void nextTurn()
    {
    ++turn;
    actualTurn=nextTurn;
    nextTurn=new HashMap();
    }
  }