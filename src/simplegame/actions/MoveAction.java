/**
 * MoveAction.java
 *
 * @author Waldemar Tribus
 */

package simplegame.actions;

import marauroa.game.Attributes;
import marauroa.game.RPAction;

public class MoveAction
  extends RPAction
{
  public final static int ACTION_MOVE=1;
  
  public MoveAction()
  {
    put("type",ACTION_MOVE);
  }
  
  public void setRow(int row)
  {
    put("row",row);
  }
  
  public void setColumn(int column)
  {
    put("column",column);
  }
  
  public int getRow()
  {
    int row = -1;
    try
    {
      row = Integer.parseInt(get("row"));
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(row);
  }
  
  public int getColumn()
  {
    int column = -1;
    try
    {
      column = Integer.parseInt(get("column"));
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(column);
  }
}

