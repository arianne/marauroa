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
  private int gameBoard[][];
  private Vector vUpdateListener;
  private int size;
  
  public SimpleGameDataModel(int size)
  {
    this.size = size;
    gameBoard = new int[size][size];
    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
      {
        gameBoard[i][j]=-1;
      }
    }
    vUpdateListener = new Vector(1,1);
  }
  
  private void fireUpdate(int row, int column, int id)
  {
    for (int i = 0; i < vUpdateListener.size(); i++)
    {
      GameUpdateListener ul = (GameUpdateListener)vUpdateListener.elementAt(i);
      ul.updateReceived(row,column,id);
    }
  }
  
  public void addGameUpdateListener(GameUpdateListener ul)
  {
    if(ul!=null)
    {
      vUpdateListener.add(ul);
    }
  }
  
  public void setRPCharacterAt(int row, int column, int id)
  {
    if(id!=gameBoard[row][column])
    {
      gameBoard[row][column]=id;
      fireUpdate(row,column,id);
    }
  }
  
  public int getSize()
  {
    return(size);
  }
    
  public int getRPCharacterAt(int row, int column)
  {
    return(gameBoard[row][column]);
  }
  
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer("\n");
    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
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
  
  public int getWinner()
  {
    //TODO implement the win conditions check.
    //it should return the byte value of the winner
    //or -1 if none won
    return(-1);
  }
  
}



