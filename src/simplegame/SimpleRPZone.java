/**
 * SimpleRPZone.java
 *
 * @author Created by wt
 */

package simplegame;

import marauroa.game.MarauroaRPZone;
import marauroa.game.RPObject;
import marauroa.game.RPZone;

public class SimpleRPZone
  extends MarauroaRPZone
  implements SimpleGameDataModelIF
{
  private int rows;
  private int columns;
  
  
  public SimpleRPZone()
  {
    rows    = 3;
    columns = 3;
  }
  
  public int getColumnsCount()
  {
    return columns;
  }
  
  public int getRowsCount()
  {
    return rows;
  }
  
  public void setColorAt(int row, int column, byte color)
  {
    RPObject cell = null;
    RPObject.ID id = new  RPObject.ID(row*columns+column);
    try
    {
      cell = get(id);
      //if we reach this point...
      //something went wrong - the color is already set
    }
    catch (RPZone.RPObjectNotFoundException e)
    {
      cell = new RPObject();
      cell.put("object_id",String.valueOf(id.getObjectID()));
      cell.put("type","cell");
      cell.put("color",String.valueOf(color));
    }
  }
  
  public byte getColorAt(int row, int column)
  {
    RPObject cell = null;
    byte color = -1;
    RPObject.ID id = new  RPObject.ID(row*columns+column);
    try
    {
      cell = get(id);
      color = Byte.parseByte(cell.get("color"));
    }
    catch (RPZone.RPObjectNotFoundException e)
    {
      color = -1;
    }
    catch (NumberFormatException e)
    {
      color = -1;
    }
    catch (marauroa.game.Attributes.AttributeNotFoundException e)
    {
      color = -1;
    }
    return color;
  }
  
  public byte checkWinCondition()
  {
    return -1;
  }
  
  public void addGameUpdateListener(simplegame.SimpleGameDataModelIF.GameUpdateListener ul)
  {
    //no need to implement it
  }
  
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer("\n");
    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < columns; j++)
      {
        if(getColorAt(i,j)==-1)
        {
          sb.append('-');
        }
        else
        {
          sb.append(getColorAt(i,j));
        }
      }
      sb.append('\n');
    }
    sb.append('\n');
    return(sb.toString());
  }
  
  
}

