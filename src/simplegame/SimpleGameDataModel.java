/**
 * SimpleGameDataModel.java
 *
 * @author Waldemar Tribus
 */

package simplegame;

import java.awt.Color;
import java.util.Vector;

public class SimpleGameDataModel
  implements SimpleGameDataModelIF
{
  private byte gameBoard[][];
  private Vector vUpdateListener;
  private int rows;
  private int columns;
  
  public SimpleGameDataModel(int rows, int columns)
  {
    this.rows = rows;
    this.columns = columns;
    gameBoard = new byte[rows][columns];
    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < columns; j++)
      {
        gameBoard[i][j]=-1;
      }
    }
    vUpdateListener = new Vector(1,1);
  }
  
  private void fireUpdate(int row, int column, byte color)
  {
    for (int i = 0; i < vUpdateListener.size(); i++)
    {
      GameUpdateListener ul = (GameUpdateListener)vUpdateListener.elementAt(i);
      ul.updateReceived(row,column,color);
    }
  }
  
  public void addGameUpdateListener(GameUpdateListener ul)
  {
    if(ul!=null)
    {
      vUpdateListener.add(ul);
    }
  }
  
  public void setColorAt(int row, int column,byte color)
  {
    if(color!=gameBoard[row][column])
    {
      gameBoard[row][column]=color;
      fireUpdate(row,column,color);
    }
  }
  
  public int getColumnsCount()
  {
    return(columns);
  }
  
  public int getRowsCount()
  {
    return(rows);
  }
  
  public byte getColorAt(int row, int column)
  {
    return(gameBoard[row][column]);
  }
  
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer("\n");
    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < columns; j++)
      {
        if(gameBoard[i][j]==-1)
        {
          sb.append('-');
        }
        else
        {
          sb.append(gameBoard[i][j]);
        }
      }
      sb.append('\n');
    }
    sb.append('\n');
    return(sb.toString());
  }
  
  public byte checkWinCondition()
  {
    //TODO implement the win conditions check.
    //it should return the byte value of the winner
    //or -1 if none won
    return(-1);
  }
  
}



