/**
 * SimpleRPRuleProcessor.java
 *
 * @author Created by wt
 */

package simplegame;

import marauroa.game.*;
import marauroa.marauroad;

public class SimpleRPRuleProcessor implements RPRuleProcessor
{
  private SimpleRPZone zone;
  private RPObject.ID lastPlayerID;
  private byte color;
  
  public SimpleRPRuleProcessor()
  {
    marauroad.trace("SimpleRPRuleProcessor::<init>",">");
    lastPlayerID = null;
    color = 0;
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
      marauroad.trace("SimpleRPRuleProcessor::setContext","X","Wrong class for RPZone, exiting.");
      System.exit(-1);
    }
    marauroad.trace("SimpleRPRuleProcessor::setContext","<");
  }
  
  public RPAction.Status execute(RPObject.ID id, RPActionList list)
  {
    marauroad.trace("SimpleRPRuleProcessor::execute",">");
    RPAction.Status status = RPAction.STATUS_FAIL;
    try
    {
      RPObject rp_player = null;
      rp_player = zone.get(id);
      if(id.equals(lastPlayerID))
      {
        //this player already did a move, so ignore
        marauroad.trace("SimpleRPRuleProcessor::execute","D","Player "+id +" already did a move, ignore this action.");
      }
      else
      {
        String objid = rp_player.get("object_id");
        if(list!=null && list.size()>0)
        {
          RPAction action = list.get(list.size()-1);
          marauroad.trace("SimpleRPRuleProcessor::execute","D","Player "+id +" sent an action " + action);
          int row = Integer.parseInt(action.get("row"));
          int column = Integer.parseInt(action.get("column"));
          if(zone.gameDataModel.getColorAt(row,column)==-1)
          {
            zone.gameDataModel.setColorAt(row,column,(byte)1);
            lastPlayerID = id;
            status = RPAction.STATUS_SUCCESS;
            //swap color
            color = color==1?(byte)0:(byte)1;
            marauroad.trace("SimpleRPRuleProcessor::execute","D",zone.gameDataModel.toString());
          }
          else
          {
            //this field is already set
          }
        }
        marauroad.trace("SimpleRPRuleProcessor::execute","D","Player "+id +" - no actions???.");
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
  
  //  private class Turn
  //    extends RPObject
  //  {
  //    public Turn()
  //    {
  //      this.put("object_id","TurnObject");
  //    }
  //  }
  
}


