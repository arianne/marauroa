/* $Id: ChallengeAction.java,v 1.10 2004/03/24 15:25:35 arianne_rpg Exp $ */
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
import java.util.*;

public class ChallengeAction extends RPAction
  {
  public final static int ACTION_CHALLENGE=2;
  public ChallengeAction()
    {
    put("type",ACTION_CHALLENGE);
    }

  public ChallengeAction(RPAction action) throws Attributes.AttributeNotFoundException
    {
    Iterator it=action.iterator();

    while(it.hasNext())
      {
      String attr=(String)it.next();

      put(attr,action.get(attr));
      }
    put("type",ACTION_CHALLENGE);
    }
  
  // Who is challenge
  public void setWho(int charID)
    {
    put("who",charID);
    }
  
  // Who was challenged
  public void setWhom(int charID)
    {
    put("whom",charID);
    }
  
  public int getWho()
    {
    int ret = -1;

    try
      {
      ret = getInt("who");
      }
    catch (Exception e)
      {
      e.printStackTrace(System.out);
      }
    return(ret);
    }
  
  public int getWhom()
    {
    int ret = -1;

    try
      {
      ret = getInt("whom");
      }
    catch (NumberFormatException e)
      {
      e.printStackTrace(System.out);
      }
    catch (Attributes.AttributeNotFoundException e)
      {
      e.printStackTrace(System.out);
      }
    return(ret);
    }
  }
