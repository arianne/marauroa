/**
 * MarauroaRPRuleProcessor.java
 *
 * @author Created by wt
 */

package marauroa.game;

import java.util.List;
import marauroa.marauroad;

public class MarauroaRPRuleProcessor implements RPRuleProcessor
{
  private RPZone zone;
  
  public MarauroaRPRuleProcessor()
  {
    this.zone=zone;
  }
  
  public RPAction.Status execute(RPObject.ID id, RPActionList list)
  {
    marauroad.trace("RPRuleProcessor::execute",">");
    /* TODO: Implement action procession code */
    marauroad.trace("RPRuleProcessor::execute","<");
    
    return new RPAction.Status(RPAction.Status.FAIL);
  }
}

