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
  private RPObject.ID cellArray[][];
  
  public SimpleRPZone()
  {
    rows    = 3;
    columns = 3;
    cellArray = new RPObject.ID[rows][columns];
    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < rows; j++)
      {
        cellArray[i][j]=null;
      }
    }
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
    RPObject.ID id = cellArray[row][column];
    if(id==null)
    {
      cell = create();
      cell.put("type","cell");
      cell.put("color",String.valueOf(color));
      cell.put("row",String.valueOf(row));
      cell.put("column",String.valueOf(column));
      try
      {
        add(cell);
      }
      catch (RPZone.RPObjectInvalidException ex)
      {
        ex.printStackTrace();
      }
    }
    else
    {
      //not allowed - the cell is already set
    }
  }
  
  public byte getColorAt(int row, int column)
  {
    RPObject cell = null;
    byte color = -1;
    RPObject.ID id = cellArray[row][column];
    if(id!=null)
    {
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

