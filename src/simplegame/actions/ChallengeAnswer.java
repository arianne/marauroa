/* $Id: ChallengeAnswer.java,v 1.2 2003/12/08 01:12:20 arianne_rpg Exp $ */
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

public class ChallengeAnswer
  extends ChallengeAction
{
  public final static int ACTION_CHALLENGE_ANSWER=4;
  
  public ChallengeAnswer()
  {
    actionType = ACTION_CHALLENGE_ANSWER;
  }
  
  public void setAccept(boolean accepted)
  {
    put("accept",Boolean.toString(accepted));
  }
  
  public boolean isAccepted()
  {
    boolean ret = false;
    try
    {
      ret = Boolean.getBoolean(get("accept"));
    }
    catch (Attributes.AttributeNotFoundException e)
    {
      ret = false;
    }
    return(ret);
  }
  
  
}

