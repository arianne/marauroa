/* $Id: GameBoard.java,v 1.7 2003/12/17 16:05:29 arianne_rpg Exp $ */
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
package simplegame.objects;

import marauroa.game.Attributes;
import marauroa.game.RPObject;
import marauroa.marauroad;
import simplegame.SimpleGameDataModelIF;

public class GameBoard
  extends RPObject
  implements SimpleGameDataModelIF
{
  public final static int TYPE_GAME_BOARD=3;
  
  public GameBoard()
  {
    this(3);
  }
  
  public GameBoard(int size)
  {
    put("type",TYPE_GAME_BOARD);
    put("size",size);
    //last player id
    put("last_id",-1);
    //winner id
    put("winner_id",-1);
  }
  
  /**
   * returns the character id of the player who made the last move
   * @return the character id of the last player or -1 if nobody has made a move
   **/
  public int getLastPlayerID()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("last_id"));
    }
    catch (NumberFormatException e)
    {
    }
    catch (Attributes.AttributeNotFoundException e)
    {
    }
    return(ret);
  }
  
  /**
   * returns the winner id previously set by checkWinCondition
   * @return the character id of the winner or -1
   **/
  public int getWinnerID()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("winner_id"));
    }
    catch (NumberFormatException e)
    {
    }
    catch (Attributes.AttributeNotFoundException e)
    {
    }
    return(ret);
  }
  
  /**
   * returns the size of the board. currently should always return 3 :)
   * @return the game board size
   **/
  public int getSize()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("size"));
    }
    catch (NumberFormatException e)
    {
      ret = -1;
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      ret = -1;
    }
    return(ret);
  }
  
  
  /**
   * makes a move for character.
   * sets the field with coordinates row,column assigned to the characterID
   * does nothing if the field is already set.
   * @param row the row
   * @param column the column
   * @param characterID - id to set into field(row,column)
   **/
  public void setRPCharacterAt(int row, int column, int characterID)
  {
    try
    {
      get(row+"X"+column);
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      put(row+"X"+column,characterID);
      put("last_id",characterID);
    }
  }
  
  
  /**
   * @param row the row
   * @param column the column
   * @return
   **/
  public int getRPCharacterAt(int row, int column)
  {
    int id = -1;
    try
    {
      id = Integer.parseInt(get(row+"X"+column));
    }
    catch (NumberFormatException e)
    {
      id = -1;
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      id = -1;
    }
    return id;
  }
  
  /**
   * @see Interface SimpleGameDataModelIF
   **/
  public void addGameUpdateListener(simplegame.SimpleGameDataModelIF.GameUpdateListener ul)
  {
    //no need to implement it
  }
  
  
  /**
   * checks if someone already has won.
   * @param gb GameBoard to look for a winner
   * @return the id of the winner, or -1 if nobody wins
   * @exception RemisException if nobody won and no more moves can be made
   **/
  public int checkWinCondition()
  {
    int winner = -1;
    try
    {
      winner = checkRows();
      if(winner==-1)
      {
        winner = checkColumns();
      }
      if(winner==-1)
      {
        winner = checkDiagonals();
      }
      if(winner!=-1)
      {
        put("winner_id",winner);
      }
    }
    finally
    {
      marauroad.trace("SimpleRPRuleProcessor::checkWinCondition","D","Winner is "+winner+"\n"+toString());
    }
    return(winner);
  }
  
  public boolean hasFreeMoves()
  {
    int size = getSize();
    boolean has_free_moves = true;
    for (int i = 0; i < size && has_free_moves; i++)
    {
      for (int j = 0; j < size && has_free_moves; j++)
      {
        if(getRPCharacterAt(i,j)==-1)
        {
          has_free_moves = false;
        }
      }
    }
    return(has_free_moves);
  }
  
  private int checkDiagonals()
  {
    int size = getSize();
    int winner = -1;
    boolean the_same = true;
    int first = getRPCharacterAt(0,0);
    if(first!=-1)
    {
      for (int i = 1; i < size && the_same; i++)
      {
        the_same = the_same && first == getRPCharacterAt(i,i);
      }
    }
    if(the_same && first!=-1)
    {
      winner = first;
    }
    else
    {
      the_same = true;
      first = getRPCharacterAt(0,size-1);
      if(first!=-1)
      {
        for (int i = 1; i < size && the_same; i++)
        {
          the_same = the_same && first == getRPCharacterAt(i,size-i-1);
        }
        if(the_same)
        {
          winner = first;
        }
      }
    }
    return(winner);
  }
  
  private int checkColumns()
  {
    int size = getSize();
    int winner = -1;
    for (int i = 0; i < size; i++)
    {
      int first = getRPCharacterAt(0,i);
      if(first!=-1)
      {
        boolean the_same = true;
        for (int j = 1; j < size && the_same; j++)
        {
          the_same = the_same && first == getRPCharacterAt(j,i);
        }
        if(the_same)
        {
          winner = first;
          break;
        }
      }
    }
    return(winner);
  }
  
  private int checkRows()
  {
    int size = getSize();
    int winner = -1;
    for (int i = 0; i < size; i++)
    {
      int first = getRPCharacterAt(i,0);
      if(first!=-1)
      {
        boolean the_same = true;
        for (int j = 1; j < size && the_same; j++)
        {
          the_same = the_same && first == getRPCharacterAt(i,j);
        }
        if(the_same)
        {
          winner = first;
          break;
        }
      }
    }
    return(winner);
  }
  
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer("\n");
    int size = getSize();
    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
      {
        if(getRPCharacterAt(i,j)==-1)
        {
          sb.append('-');
        }
        else
        {
          sb.append(getRPCharacterAt(i,j));
        }
      }
      sb.append('\n');
    }
    sb.append('\n');
    return(sb.toString());
  }
  
}

