/**
 * SimpleRPRuleProcessor.java
 *
 * @author Created by wt
 */

package simplegame;

import marauroa.game.*;

public class SimpleRPRuleProcessor
  implements RPRuleProcessor
{
  private SimpleRPZone zone;
  
  private RPObject.ID lastPlayerId;
  
  public SimpleRPRuleProcessor(SimpleRPZone zone)
  {
    this.zone=zone;
    lastPlayerId = null;
  }
  
  public RPAction.Status execute(RPObject.ID id, RPActionList list)
  {
    try
    {
      RPObject rp_object = zone.get(id);
      String color = rp_object.get("color");
      
      
    }
    catch (RPZone.RPObjectNotFoundException e)
    {
    }
    catch (Attributes.AttributeNotFoundException e)
    {
    }
    return null;
  }
  
}

