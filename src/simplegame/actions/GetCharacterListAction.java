/* $Id: GetCharacterListAction.java,v 1.3 2003/12/08 01:12:20 arianne_rpg Exp $ */
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

import marauroa.game.RPAction;

public class GetCharacterListAction
  extends RPAction
{
  public final static int ACTION_GETCHARLIST=3;
  public GetCharacterListAction()
  {
    actionType=ACTION_GETCHARLIST;
  }
}

