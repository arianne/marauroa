package marauroa.game;

import java.util.*;

public class RPRuleProcessor
  {
  private RPZone zone;
  
  public RPRuleProcessor(RPZone zone)
    {
    this.zone=zone;
    }
    
  public RPAction.Status execute(RPObject.ID id, LinkedList list)
    {
    /* TODO: Implement action procession code */
    return new RPAction.Status(RPAction.Status.FAIL);
    }
  }