/**
 * SimpleRPZone.java
 *
 * @author Created by wt
 */

package simplegame;

import java.util.LinkedList;
import java.util.List;
import marauroa.game.MarauroaRPZone;
import marauroa.game.RPObject;
import marauroa.game.RPZone;

public class SimpleRPZone
  extends MarauroaRPZone
{
  //we will never send a list with deleted items
  //in this game, so create once the empty list
  //and send it all the time.
  private List deletedList;
  
  
  public SimpleRPZone()
  {
  deletedList=new LinkedList();
  }
  
  public Perception getPerception(RPObject.ID id)
  {
    /** Using TOTAL perception per turn */
    RPZone.Perception perception=new RPZone.Perception(RPZone.Perception.TOTAL);
    perception.modifiedList=new LinkedList();
    perception.deletedList =deletedList;
    try
    {
      RPObject player = get(id);
      perception.modifiedList.add(player);
    }
    catch (RPObjectNotFoundException e)
    {
    }
    return perception;
  }
}

