/**
 * SimpleRPRuleProcessor.java
 *
 * @author Created by wt
 */

package simplegame;

import marauroa.game.*;

import java.util.Iterator;
import marauroa.marauroad;
import simplegame.actions.ChallengeAction;
import simplegame.actions.GetCharacterListAction;
import simplegame.actions.MoveAction;
import simplegame.objects.GameBoard;

public class SimpleRPRuleProcessor implements RPRuleProcessor
{
  private SimpleRPZone zone;
  
  public SimpleRPRuleProcessor()
  {
    marauroad.trace("SimpleRPRuleProcessor::<init>",">");
    marauroad.trace("SimpleRPRuleProcessor::<init>","<");
  }
  
  public void setContext(RPZone zone)
  {
    marauroad.trace("SimpleRPRuleProcessor::setContext",">");
    try
    {
      this.zone = (SimpleRPZone)zone;
    }
    catch(ClassCastException cce)
    {
      marauroad.trace("SimpleRPRuleProcessor::setContext","!","Wrong class for RPZone, exiting.");
      System.exit(-1);
    }
    marauroad.trace("SimpleRPRuleProcessor::setContext","<");
  }
  
  public void approvedActions(RPActionList actionList)
  {
    while(actionList.size()>1)
    {
      actionList.remove(0);
    }
  }
  
  
  public RPAction.Status execute(RPObject.ID id, RPAction action)
  {
    marauroad.trace("SimpleRPRuleProcessor::execute",">");
    RPAction.Status status = RPAction.STATUS_FAIL;
    try
    {
      RPObject rp_player = null;
      rp_player = zone.get(id);
      
      int action_type = Integer.parseInt(action.get("type"));
      
      switch(action_type)
      {
        case MoveAction.ACTION_MOVE:
          status = makeMove(id, action);
          break;
        case GetCharacterListAction.ACTION_GETCHARLIST:
          status = getCharacterList(id, action);
          break;
        case ChallengeAction.ACTION_CHALLENGE:
          ;
          break;
        default: //unknown type
      }
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (RPZone.RPObjectNotFoundException e)
    {
      e.printStackTrace();
    }
    finally
    {
      marauroad.trace("SimpleRPRuleProcessor::execute","<");
    }
    return status;
  }
  
  private RPAction.Status makeMove(RPObject.ID id, RPAction action)
    throws NumberFormatException,
    Attributes.AttributeNotFoundException,
    RPZone.RPObjectNotFoundException
  {
    marauroad.trace("SimpleRPRuleProcessor::makeMove",">");
    RPAction.Status status = RPAction.STATUS_FAIL;
    try
    {
      RPObject rp_player = zone.get(id);
      GameBoard gb=null;
      int last_id = -1;
      try
      {
        gb = (GameBoard)rp_player.getSlot("hand").get();
        last_id = gb.getLastPlayerID();
      }
      catch (RPObject.NoSlotFoundException e)
      {
        gb = new GameBoard(3);
        RPSlot rp_slot = new RPSlot("hand");
        rp_slot.add(gb);
        try
        {
          rp_player.addSlot(rp_slot);
        }
        catch (RPObject.SlotAlreadyAddedException ex)
        {
        }
      }
      catch (RPSlot.RPObjectNotFoundException e)
      {
        // TODO: There is no object on the slot. Decide what to do
      }
      
      if(id.getObjectID()==last_id)
      {
        marauroad.trace("SimpleRPRuleProcessor::execute","D","Player "+id +" already did a move, ignore this action.");
      }
      else
      {
        int row = Integer.parseInt(action.get("row"));
        int column = Integer.parseInt(action.get("column"));
        if(gb.getRPCharacterAt(row,column)==-1)
        {
          gb.setRPCharacterAt(row,column,id.getObjectID());
          status = RPAction.STATUS_SUCCESS;
          marauroad.trace("SimpleRPRuleProcessor::makeMove","D",zone.toString());
          //          int winner_id  = gb.getWinner();
          //          if(winner_id!=-1)
          //          {
          //            //TODO
          //            RPObject rp_winner = zone.get(null);
          ////            marauroad.trace("SimpleRPRuleProcessor::makeMove","D","And the winner is "+winner);
          //          }
        }
        else
        {
          //this field is already set
        }
        marauroad.trace("SimpleRPRuleProcessor::makeMove","D","Player "+id +" - no actions???.");
      }
    }
    finally
    {
      marauroad.trace("SimpleRPRuleProcessor::makeMove","<");
    }
    return status;
  }
  
  private RPAction.Status getCharacterList(RPObject.ID id, RPAction action)
    throws NumberFormatException,
    Attributes.AttributeNotFoundException,
    RPZone.RPObjectNotFoundException
  {
    marauroad.trace("SimpleRPRuleProcessor::getCharacterList",">");
    RPAction.Status status = RPAction.STATUS_FAIL;
    try
    {
      RPObject rp_player = zone.get(id);
      Iterator iter = zone.iterator();
      if(iter!=null)
      {
        RPSlot playerlist = new RPSlot("ear");
        while(iter.hasNext())
        {
          RPObject object = (RPObject)iter.next();
          int oid = Integer.parseInt(object.get("object_id"));
          if(oid!=id.getObjectID())
          {
          }
        }
      }
    }
    finally
    {
      marauroad.trace("SimpleRPRuleProcessor::getCharacterList",">");
    }
    return(status);
  }
}


