/* $Id: SimpleGameDataModel.java,v 1.7 2003/12/08 01:12:20 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
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



