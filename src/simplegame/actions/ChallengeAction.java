/**
 * RejectsAction.java
 *
 * @author Waldemar Tribus
 */

package simplegame.actions;

import marauroa.game.Attributes;
import marauroa.game.RPAction;

public class ChallengeAction
  extends RPAction
{
  public final static int ACTION_CHALLENGE=2;
  
  public ChallengeAction()
  {
    actionType=ACTION_CHALLENGE;
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
  
  public int getWho()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("who"));
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(ret);
  }
  
  public int getWhom()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("whom"));
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(ret);
  }
  
  
  
}

