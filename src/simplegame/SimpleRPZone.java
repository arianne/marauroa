/**
 * SimpleRPZone.java
 *
 * @author Created by wt
 */

package simplegame;

import marauroa.game.MarauroaRPZone;
import marauroa.game.RPObject;

public class SimpleRPZone
  extends MarauroaRPZone
{
  protected SimpleGameDataModel gameDataModel;
  
  public SimpleRPZone()
  {
    gameDataModel = new SimpleGameDataModel(3,3);
  }
  
}

