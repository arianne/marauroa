/* $Id: MoveAction.java,v 1.5 2003/12/30 10:26:42 arianne_rpg Exp $ */
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
      row = getInt("row");
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
      column = getInt("column");
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(column);
  }
}

