/* $Id: ChallengeAction.java,v 1.4 2003/12/08 01:12:20 arianne_rpg Exp $ */
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

public class ChallengeAction
  extends RPAction
{
  public final static int ACTION_CHALLENGE=2;
  
  public ChallengeAction()
  {
    actionType=ACTION_CHALLENGE;
  }
  
  //Who is challenge
  public void setWho(int charID)
  {
    put("who",charID);
  }
  
  //Who was challenged
  public void setWhom(int charID)
  {
    put("whom",charID);
  }
  
  public int getWho()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("who"));
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(ret);
  }
  
  public int getWhom()
  {
    int ret = -1;
    try
    {
      ret = Integer.parseInt(get("whom"));
    }
    catch (NumberFormatException e) {}
    catch (Attributes.AttributeNotFoundException e) {}
    return(ret);
  }
  
  
  
}

