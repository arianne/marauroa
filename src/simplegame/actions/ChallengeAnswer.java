/* $Id: ChallengeAnswer.java,v 1.4 2003/12/17 16:05:29 arianne_rpg Exp $ */
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
import marauroa.marauroad;

public class ChallengeAnswer
  extends ChallengeAction
{
  public final static int ACTION_CHALLENGE_ANSWER=4;
  
  public ChallengeAnswer()
  {
    put("type",ACTION_CHALLENGE_ANSWER);
  }
  
  public void setAccept(boolean accepted)
  {
    put("accept",Boolean.toString(accepted));
  }
  
  public boolean isAccepted()
  {
//    marauroad.trace("ChallengeAnswer::isAccepted",">");
    boolean ret = false;
    try
    {
      String acc = get("accept");
//      marauroad.trace("ChallengeAnswer::isAccepted","D","["+acc+"]");
      ret = Boolean.valueOf(acc).booleanValue();
    }
    catch (Attributes.AttributeNotFoundException e)
    {
//      marauroad.trace("ChallengeAnswer::isAccepted","D","AttributeNotFoundException");
      ret = false;
    }
//    marauroad.trace("ChallengeAnswer::isAccepted","D","returning " +ret);
//    marauroad.trace("ChallengeAnswer::isAccepted","<");
    return(ret);
  }
  
  
}

