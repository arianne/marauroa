/* $Id: SimpleGameDataModelIF.java,v 1.3 2003/12/08 01:12:20 arianne_rpg Exp $ */
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

public interface SimpleGameDataModelIF
{
  public void addGameUpdateListener(GameUpdateListener ul);
  public void setRPCharacterAt(int row, int column, int characterID);
  public int getSize();
  public int getRPCharacterAt(int row, int column);
  public int getWinner();
  public static interface GameUpdateListener
  {
    public void updateReceived(int row, int column, int characterID);
  }
}

