/* $Id: SimpleRPZone.java,v 1.14 2003/12/10 22:49:46 root777 Exp $ */
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
import marauroa.game.RPActionFactory;
import marauroa.game.RPObject;
import marauroa.game.RPObjectFactory;
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
    //register our objects
    RPObjectFactory.getFactory().register(CharacterList.TYPE_CHARACTER_LIST,CharacterList.class);
    RPObjectFactory.getFactory().register(CharacterList.TYPE_CHARACTER_LIST_ENTRY,CharacterList.CharEntry.class);
    RPObjectFactory.getFactory().register(GameBoard.TYPE_GAME_BOARD,GameBoard.class);
    
    //register our actions
    RPActionFactory.getFactory().register(ChallengeAction.ACTION_CHALLENGE,ChallengeAction.class);
    RPActionFactory.getFactory().register(ChallengeAnswer.ACTION_CHALLENGE_ANSWER,ChallengeAnswer.class);
    RPActionFactory.getFactory().register(MoveAction.ACTION_MOVE,MoveAction.class);
    RPActionFactory.getFactory().register(GetCharacterListAction.ACTION_GETCHARLIST,GetCharacterListAction.class);
    marauroad.trace("SimpleRPZone::<init>","<");
  }
  
  public Perception getPerception(RPObject.ID id, byte type)
  {
    //    if(type==Perception.DELTA)
    //    {
//
    //    }
    //    else
    //    {
//
    //    }
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
  
  public Perception getPerception(RPObject.ID id)
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

