package marauroa.game;

public interface RPRuleProcessor
  {
  public void setContext(RPZone zone);
  public RPAction.Status execute(RPObject.ID id, RPAction list);
  }
