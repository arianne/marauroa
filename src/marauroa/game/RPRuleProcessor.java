package marauroa.game;

public interface RPRuleProcessor
  {
  public void setContext(RPZone zone);
  public void approvedActions(RPActionList actionList);
  public RPAction.Status execute(RPObject.ID id, RPAction action);
  }
