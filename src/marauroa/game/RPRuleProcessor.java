package marauroa.game;



public interface RPRuleProcessor
  {
  public RPAction.Status execute(RPObject.ID id, RPActionList list);
  }
