/* $Id: SimpleRPZone.java,v 1.16 2003/12/19 10:54:34 arianne_rpg Exp $ */
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

import java.util.LinkedList;
import java.util.List;
import marauroa.game.MarauroaRPZone;
import marauroa.game.RPObject;
import marauroa.game.RPZone;
import marauroa.marauroad;
import simplegame.actions.ChallengeAction;
import simplegame.actions.ChallengeAnswer;
import simplegame.actions.GetCharacterListAction;
import simplegame.actions.MoveAction;
import simplegame.objects.CharacterList;
import simplegame.objects.GameBoard;

public class SimpleRPZone
  extends MarauroaRPZone
{
  //we will never send a list with deleted items
  //in this game, so create once the empty list
  //and send it all the time.
  private List deletedList;
  
  
  public SimpleRPZone()
  {
    marauroad.trace("SimpleRPZone::<init>",">");
    deletedList=new LinkedList();
    marauroad.trace("SimpleRPZone::<init>","<");
  }
  
  public Perception getPerception(RPObject.ID id, byte type)
  {
    marauroad.trace("SimpleRPZone::getPerception",">");
    /** Using TOTAL perception per turn */
    RPZone.Perception perception=new RPZone.Perception(RPZone.Perception.TOTAL);
    perception.modifiedList = new LinkedList();
    perception.deletedList  = deletedList;
    try
    {
      RPObject player = get(id);
      perception.modifiedList.add(player);
    }
    catch (RPObjectNotFoundException e)
    {
      e.printStackTrace();
    }
    marauroad.trace("SimpleRPZone::getPerception","<");
    return perception;
  }
}

