/**
 * SimpleRPRuleProcessor.java
 *
 * @author Created by wt
 */

package simplegame;

import marauroa.game.*;

import java.awt.Color;
import marauroa.marauroad;

public class SimpleRPRuleProcessor
  implements RPRuleProcessor
{
  private SimpleRPZone zone;
  private RPObject.ID lastPlayerID;
  
  public SimpleRPRuleProcessor(SimpleRPZone zone)
  {
    this.zone=zone;
    lastPlayerID = null;
  }
  
  
  public RPAction.Status execute(RPObject.ID id, RPActionList list)
  {
    marauroad.trace("SimpleRPRuleProcessor::execute",">");
    RPAction.Status status = RPAction.STATUS_FAIL;
    try
    {
      RPObject rp_player = null;
      try
      {
        rp_player = zone.get(id);
      }
      catch (RPZone.RPObjectNotFoundException e)
      {
        e.printStackTrace();
        rp_player = new RPObject();
        rp_player.put("object_id",""+id.getObjectID());
        try
        {
          zone.add(rp_player);
        }
        catch (marauroa.game.RPZone.RPObjectInvalidException ex)
        {
          ex.printStackTrace();
        }
      }
      if(id.equals(lastPlayerID))
      {
        //this player already did a move, so ignore
        marauroad.trace("SimpleRPRuleProcessor::execute","Player "+id +" already did a move, ignore this action.");
      }
      else
      {
        String objid = rp_player.get("object_id");
        if(list!=null && list.size()>0)
        {
          RPAction action = list.get(0);
          marauroad.trace("SimpleRPRuleProcessor::execute","Player "+id +" sent an action " + action);
          int row = Integer.parseInt(action.get("row"));
          int column = Integer.parseInt(action.get("column"));
          if(zone.gameDataModel.getColorAt(row,column)==-1)
          {
            zone.gameDataModel.setColorAt(row,column,(byte)1);
            lastPlayerID = id;
            status = RPAction.STATUS_SUCCESS;
          }
        }
        marauroad.trace("SimpleRPRuleProcessor::execute","Player "+id +" - no actions???.");
      }
    }
//    catch (RPZone.RPObjectNotFoundException e)
//    {
//      e.printStackTrace();
//    }
    catch (Attributes.AttributeNotFoundException e)
    {
      e.printStackTrace();
    }
    finally
    {
      marauroad.trace("SimpleRPRuleProcessor::execute","<");
    }
    return status;
  }
  
  //  private class Turn
  //    extends RPObject
  //  {
  //    public Turn()
  //    {
  //      this.put("object_id","TurnObject");
  //    }
  //  }
  
}

