/**
 * RejectsAction.java
 *
 * @author Waldemar Tribus
 */

package simplegame.actions;

import marauroa.game.RPAction;

public class ChallengeAction
  extends RPAction
{
  public final static int ACTION_CHALLENGE=2;
  
  public ChallengeAction()
  {
    put("type",ACTION_CHALLENGE);
  }
  
  //Who is challenge
  public void setWho(int charID)
  {
    put("who",charID);
  }
  
  //Who was challenged
  public void setWhom(int charID)
  {
    put("whom",charID);
  }
  
  //  public int getWho()
  //  {
  //    returnput("who",charID);
  //  }
  
  
}

