/**
 * ChallengeAnswer.java
 *
 * @author Created by wt
 */

package simplegame.actions;

import marauroa.game.Attributes;

public class ChallengeAnswer
  extends ChallengeAction
{
  public final static int ACTION_CHALLENGE_ANSWER=4;
  
  public ChallengeAnswer()
  {
    actionType = ACTION_CHALLENGE_ANSWER;
  }
  
  public void setAccept(boolean accepted)
  {
    put("accept",Boolean.toString(accepted));
  }
  
  public boolean isAccepted()
  {
    boolean ret = false;
    try
    {
      ret = Boolean.getBoolean(get("accept"));
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      ret = false;
    }
    return(ret);
  }
  
  
}

